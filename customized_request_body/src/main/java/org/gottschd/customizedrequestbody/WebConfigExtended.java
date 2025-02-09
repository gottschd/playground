package org.gottschd.customizedrequestbody;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.gottschd.customizedrequestbody.model.trimmedbyclass.MyTrimmedByClassPayload;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Custom WebConfig .
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class WebConfigExtended implements WebMvcConfigurer {

	private static final String CLASSES_TO_BE_TRIMMED = MyTrimmedByClassPayload.class.getPackageName();

	private static MappingJackson2HttpMessageConverter getTrimmingJackson2HttpMessageConverter(String loggingTemplate) {
		MappingJackson2HttpMessageConverter gottschdConverter = new MappingJackson2HttpMessageConverter();

		SimpleModule sm = new SimpleModule();
		sm.addDeserializer(String.class, new StringDeserializer() {
			@Override
			public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
				String originalValue = super.deserialize(jsonParser, ctx);
				log.info(loggingTemplate, originalValue);
				return originalValue.strip();
			}
		});

		gottschdConverter.getObjectMapper().registerModule(sm);
		return gottschdConverter;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

		MappingJackson2HttpMessageConverter gottschdConverter = getTrimmingJackson2HttpMessageConverter(
				"deserialize by annotation - trimming value: '{}'");

		// add first because it is the most specific and must be evaluated first
		resolvers.add(new TrimmedRequestResponseBodyMethodProcessor(gottschdConverter));
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		// wrap (all) MappingJackson2HttpMessageConverter with a converter
		// that can distinguish between gottschd and non-gottschd classes. for gottschd
		// classes,
		// an objectmapper with the trimming module must be used. For other
		// request types
		// (e.g. coming from actuator module) use the default.

		List<HttpMessageConverter<?>> adaptedConverters = new ArrayList<>();
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter c) {
				adaptedConverters.add(new DoNotTrimClassesJackson2HttpMessageConverter(c));
			}
			else {
				adaptedConverters.add(converter);
			}
		}

		// now clear and add all in the expected order but wrapped
		converters.clear();

		// now add gottschd mapper first, because it is the most specific
		MappingJackson2HttpMessageConverter gottschdConverter = getTrimmingJackson2HttpMessageConverter(
				"deserialize - trimming value: '{}'");
		converters.add(new DoTrimClassesJackson2HttpMessageConverter(gottschdConverter));

		// and later all pre-existing classes
		converters.addAll(adaptedConverters);
	}

	private static class DoTrimClassesJackson2HttpMessageConverter extends ClassFilteredJackson2HttpMessageConverter {

		DoTrimClassesJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original) {
			super(original, readType -> readType.getTypeName().startsWith(CLASSES_TO_BE_TRIMMED), "DoTrim");
		}

	}

	private static class DoNotTrimClassesJackson2HttpMessageConverter
			extends ClassFilteredJackson2HttpMessageConverter {

		DoNotTrimClassesJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original) {
			super(original, readType -> !readType.getTypeName().startsWith(CLASSES_TO_BE_TRIMMED), "DoNotTrim");
		}

	}

	@Slf4j
	private static class ClassFilteredJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

		private final Predicate<Type> allowedToReadPredicate;

		private final String debugInfo;

		ClassFilteredJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original,
				Predicate<Type> canReadClazzPredicate, String debugInfo) {
			super(original.getObjectMapper());
			this.allowedToReadPredicate = canReadClazzPredicate;
			this.debugInfo = debugInfo;
		}

		@Override
		public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
			log.info("canRead (debugInfo: {}) - type: {}, contextClass: {}, mediaType: {}", debugInfo, type,
					contextClass, mediaType);
			boolean allowedToRead = allowedToReadPredicate.test(type);
			log.info("    allowedToRead: {}, super.canRead: {}", allowedToRead,
					super.canRead(type, contextClass, mediaType));
			return allowedToRead && super.canRead(type, contextClass, mediaType);
		}

	}

}
