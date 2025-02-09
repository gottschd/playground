package org.gottschd.customizedrequestbody;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gottschd.customizedrequestbody.model.plain.MyPlainPayload;
import org.gottschd.customizedrequestbody.model.trimmedbyclass.MyTrimmedByClassPayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MyControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	public static Stream<Arguments> provideTestData() {
		//@formatter:on
		return Stream.of(
				Arguments.of("/untrimmed", MyPlainPayload.builder().vorname("  Oskar  ").build(), MyPlainPayload.class, "  Oskar  "),
				Arguments.of("/untrimmed", MyPlainPayload.builder().vorname("").build(), MyPlainPayload.class, "must not be blank"), // test validation

				Arguments.of("/trimmedByClass", MyTrimmedByClassPayload.builder().vorname("  Franz  ").build(), MyTrimmedByClassPayload.class, "Franz"),
				Arguments.of("/trimmedByClass", MyTrimmedByClassPayload.builder().vorname("").build(), MyTrimmedByClassPayload.class, "must not be blank"), // test validation

				Arguments.of("/trimmedByCustomAnnotation", MyPlainPayload.builder().vorname("  Ben  ").build(), MyPlainPayload.class,  "Ben"),
				Arguments.of("/trimmedByCustomAnnotation", MyPlainPayload.builder().vorname("").build(), MyPlainPayload.class, "must not be blank") // test validation

		);
		//@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideTestData")
	void testCustomizedPayloadTrimming(String url, Object payload, Class<?> payloadClass, String expectedVorname) throws Exception {

		final byte[] content = objectMapper.writerFor(payloadClass).writeValueAsBytes(payload);

		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(content))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value(expectedVorname));


	}

}
