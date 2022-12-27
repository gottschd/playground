package org.gottschd;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BackendService {

	private static Logger logger = LoggerFactory.getLogger(BackendService.class);

	@Value("${backend.url}")
	private String svcUrl;

	public String callBackend() throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		URL url = calculateUrl();
		logger.info("calling svc with: " + url);

		ResponseEntity<String> response = restTemplate.getForEntity(url.toURI(), String.class);

		return response.getBody();
	}

	private URL calculateUrl() throws Exception {
		return new URL(svcUrl);
	}
}
