package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.gottschd.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class XmlParsingBusinessSpringControllerTest {
    @LocalServerPort
    int port;

    @Test
    void test_Bxml_escaped() throws Exception {
        Path createBigXmlFile = Utils.createEmbeddedEscapedXmlFile(20, 50 * 1024 * 1024);
        try {
            HttpResponse<String> response = streamingClientWithResponse(createBigXmlFile,
                    "http://localhost:" + port + "/stax");
            System.out.println(response.body());
            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertTrue(response.body().contains("extractedDataSizes"));
            assertFalse(response.body().contains("errorMessage"));
        } finally {
            Files.delete(createBigXmlFile);
        }
    }

    @Test
    void test_business_exception() throws Exception {
        Path filePath = Utils.createEmbeddedEscapedXmlFile(1, 0); // 0 bytes will trigger validation
                                                                  // error
        try {
            HttpResponse<String> response = streamingClientWithResponse(filePath,
                    "http://localhost:" + port + "/stax");
            System.out.println(response.body());
            assertEquals(HttpStatus.OK.value(), response.statusCode());
            assertTrue(response.body().contains("errorMessage"));
            assertTrue(response.body().contains("Zero length byte array not allowed in business."));
        } finally {
            Files.delete(filePath);
        }
    }

    @Test
    void test_Bxml_escaped_parallel() throws Exception {
        // xml file with 20 * 50 MB data = 1GB data (approximated)
        final Path xmlFile = Utils.createEmbeddedEscapedXmlFile(20, 50 * 1000 * 1000);
        try {
            int concurrentRequests = 5;

            List<HttpRequest> requests = new ArrayList<>();
            for (int i = 0; i < concurrentRequests; i++) {
                requests.add(
                        HttpRequest.newBuilder().uri(new URI("http://localhost:" + port + "/stax"))
                                .headers("Content-Type", "text/xml; charset=utf-8")
                                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                                    try {
                                        return Files.newInputStream(xmlFile);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })).build());
            }

            // send streamed
            long start = System.currentTimeMillis();
            HttpClient client = HttpClient.newHttpClient();
            List<CompletableFuture<HttpResponse<String>>> futures = requests.stream()
                    .map(request -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .collect(Collectors.toList());

            // wait and assert each
            for (CompletableFuture<HttpResponse<String>> f : futures) {
                assertEquals(HttpStatus.OK.value(), f.get().statusCode());
                assertTrue(f.get().body().contains("extractedDataSizes"));
                assertFalse(f.get().body().contains("errorMessage"));
            }
            System.out.println("test took: "
                    + Duration.ofMillis(System.currentTimeMillis() - start).toSeconds() + " sec");

        } finally {
            Files.delete(xmlFile);
        }
    }

    private HttpResponse<String> streamingClientWithResponse(Path xmlfile, String url)
            throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url))
                .headers("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return Files.newInputStream(xmlfile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })).build();

        // send streamed request
        System.out.println("performing request...");
        long now = System.currentTimeMillis();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("performing request done (ms): " + (System.currentTimeMillis() - now));

        return response;
    }

}