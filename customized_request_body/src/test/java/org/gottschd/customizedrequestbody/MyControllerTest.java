package org.gottschd.customizedrequestbody;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gottschd.customizedrequestbody.model.trimmed.MyTrimmedPayload;
import org.gottschd.customizedrequestbody.model.untrimmed.MyPayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
		return Stream.of(Arguments.of("/untrimmed", "  keinVorname  ", "  keinVorname  "),
				Arguments.of("/untrimmed", "vorname", "vorname"),
				Arguments.of("/trimmedByClass", "  vorname  ", "vorname")

		);
		//@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideTestData")
	void testCustomizedPayloadTrimming(String url, String givenVorname, String expectedVorname) throws Exception {

		final byte[] content;
		if (url.equals("/untrimmed")) {
			MyPayload p = new MyPayload();
			p.setVorname(givenVorname);
			content = objectMapper.writeValueAsBytes(p);
		}
		else if (url.equals("/trimmedByClass")) {
			MyTrimmedPayload p = new MyTrimmedPayload();
			p.setVorname(givenVorname);
			content = objectMapper.writeValueAsBytes(p);
		}
		else {
			throw new UnsupportedOperationException("unknown url");
		}

		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(content))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value(expectedVorname));


	}

}
