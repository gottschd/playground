package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gottschd.utils.BigFileMetaEnum;
import org.gottschd.utils.CreateBigXmlFile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class XmlParsingControllerTest {
    @LocalServerPort
    int port;

    @Test
    void testXmlParsing() throws Exception {
        // xml file with 20 * 50 MB data = 1GB data (approximated)
        Path xmlFile = CreateBigXmlFile.createBigXmlFile(BigFileMetaEnum.CDATA_BASED, 20, 50 * 1000 * 1000);

        HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://localhost:" + port + "/komm"))
                .headers("Content-Type", "application/xml").POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return Files.newInputStream(xmlFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertNotNull(response.body());
    }
}