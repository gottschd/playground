package org.gottschd.customizedrequestbody;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Custom WebConfig .
 */
@Configuration
@Slf4j
public class WebConfigExtended implements WebMvcConfigurer {

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
				adaptedConverters.add(new NonGottschdClassesJackson2HttpMessageConverter(c));
			}
			else {
				adaptedConverters.add(converter);
			}
		}

		// now clear and add all in the expected order but wrapped
		converters.clear();
		converters.addAll(adaptedConverters);

		// now add gottschd mapper
		MappingJackson2HttpMessageConverter gottschdConverter = new MappingJackson2HttpMessageConverter();

		SimpleModule sm = new SimpleModule();
		sm.addDeserializer(String.class, new StringDeserializer() {
			@Override
			public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
				String originalValue = super.deserialize(jsonParser, ctx);
				log.info("deserialize - trimming value: '{}'", originalValue);
				return originalValue.strip();
			}
		});

		gottschdConverter.getObjectMapper().registerModule(sm);
		converters.add(new GottscDOnlyClassesJackson2HttpMessageConverter(gottschdConverter));
	}

	private static class GottscDOnlyClassesJackson2HttpMessageConverter
			extends ClassFilteredJackson2HttpMessageConverter {

		GottscDOnlyClassesJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original) {
			super(original, readClazz -> readClazz.getName().startsWith("org.gottschd"),
					writeClazz -> writeClazz.getName().startsWith("org.gottschd"));
		}

	}

	private static class NonGottschdClassesJackson2HttpMessageConverter
			extends ClassFilteredJackson2HttpMessageConverter {

		NonGottschdClassesJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original) {
			super(original, readClazz -> !readClazz.getName().startsWith("org.gottschd"),
					writeClazz -> !writeClazz.getName().startsWith("org.gottschd"));
		}

	}

	@Slf4j
	private static class ClassFilteredJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

		private final Predicate<Class<?>> canReadPredicate;

		private final Predicate<Class<?>> canWritePredicate;

		ClassFilteredJackson2HttpMessageConverter(MappingJackson2HttpMessageConverter original,
				Predicate<Class<?>> canReadClazzPredicate, Predicate<Class<?>> canWriteClazzPredicate) {
			super(original.getObjectMapper());
			this.canReadPredicate = canReadClazzPredicate;
			this.canWritePredicate = canWriteClazzPredicate;
		}

		@Override
		public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
			log.trace("canRead type: {}, contextClass: {}, mediaType: {}", type, contextClass, mediaType);
			return canReadPredicate.test(contextClass) && super.canRead(type, contextClass, mediaType);
		}

		@Override
		public boolean canWrite(Class<?> clazz, MediaType mediaType) {
			log.trace("canWrite clazz: {}, mediaType: {}", clazz, mediaType);
			return canWritePredicate.test(clazz) && super.canWrite(clazz, mediaType);
		}

	}

}
