package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class XmlParsingBusinessSpringControllerTest {
    private static final String loremIpsumLine = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
            tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At \
            vero eos et accusam et justo duo""";

    private static final String MARKER = "XXXXXX";

    private static final String DATALINE_WITH_MARKER_ESCAPED = "&lt;Data&gt;XXXXXX&lt;/Data&gt;";

    @LocalServerPort
    int port;

    @Test
    void test_Bxml_escaped() throws Exception {
        Path createBigXmlFile = createBigXmlFile(20, 50 * 1024 * 1024);
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
        Path filePath = createBigXmlFile(1, 0); // 0 bytes will trigger validation error
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
        final Path xmlFile = createBigXmlFile(20, 50 * 1000 * 1000);

        int concurrentRequests = 5;

        List<HttpRequest> requests = new ArrayList<>();
        for (int i = 0; i < concurrentRequests; i++) {
            requests.add(HttpRequest.newBuilder().uri(new URI("http://localhost:" + port + "/stax"))
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

        Files.delete(xmlFile);
    }

    /**
     * Leaks the internal buf instead of making a distinct copy.
     */
    private static class LeakingByteArrayOutputStream extends ByteArrayOutputStream {

        LeakingByteArrayOutputStream(int bytesPerContainer) {
            super(bytesPerContainer);
        }

        byte[] getByteArray() {
            if (count != buf.length) {
                throw new IllegalStateException("that should not happen");
            }
            return buf;
        }
    }

    private static byte[] createContainerDataBytes(int bytesPerContainer) throws Exception {
        // make a template
        ByteArrayOutputStream dataTemplate = new ByteArrayOutputStream();
        dataTemplate.write(loremIpsumLine.getBytes(StandardCharsets.UTF_8));
        dataTemplate.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        dataTemplate.flush();
        byte[] line = dataTemplate.toByteArray();

        // create the resulting bytes size
        int lineIterations = bytesPerContainer / line.length;
        LeakingByteArrayOutputStream result = new LeakingByteArrayOutputStream(bytesPerContainer);
        // write line to nearby possible size
        while (lineIterations-- > 0) {
            result.write(line);
        }
        // write rest
        int lineRest = bytesPerContainer % line.length;
        result.write(line, 0, lineRest);
        result.flush();

        return result.getByteArray();
    }

    private static Path createBigXmlFile(int containerCount, int bytesPerContainer)
            throws Exception {
        System.out.println("creating big file...");
        long now = System.currentTimeMillis();

        String outerTemplate = Files
                .readString(Paths.get(XmlParsingBusinessSpringControllerTest.class
                        .getResource("/req_embedded_escaped_template.xml").toURI()));

        String containerTemplate = """
                &lt;MyContainer&gt;
                    &lt;Data&gt;XXXXXX&lt;/Data&gt;
                    &lt;FileName&gt;myFileName&lt;/FileName&gt;
                    &lt;FileType&gt;
                        &lt;F&gt;95&lt;/F&gt;
                        &lt;Type&gt;txt&lt;/Type&gt;
                    &lt;/FileType&gt;
                &lt;/MyContainer&gt;
                """;

        StringJoiner containers = new StringJoiner(System.lineSeparator());
        for (int i = 0; i < containerCount; i++) {
            containers.add(containerTemplate);
        }

        Path templateFile = Files.createTempFile("template_xml_", ".tmp");
        Files.write(templateFile,
                outerTemplate.formatted(containers.toString()).getBytes(StandardCharsets.UTF_8));

        // replace the markers with the correct content size
        String newInhalt = DATALINE_WITH_MARKER_ESCAPED.replace(MARKER,
                Base64.getEncoder().encodeToString(createContainerDataBytes(bytesPerContainer)));
        Path bigFile = Files.createTempFile("big_xml_", ".tmp");
        try (BufferedWriter fos = Files.newBufferedWriter(bigFile)) {
            Files.lines(templateFile, StandardCharsets.UTF_8).forEach(line -> {
                // replace the marker with the generated attachment
                if (line.contains(DATALINE_WITH_MARKER_ESCAPED)) {
                    line = line.replace(DATALINE_WITH_MARKER_ESCAPED, newInhalt);
                }
                try {
                    // copy the line to the output
                    fos.write(line);
                    fos.write(System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            fos.flush();
        }

        Files.delete(templateFile);

        System.out.println("created big file in " + (System.currentTimeMillis() - now) + " ms: "
                + bigFile.toString());
        return bigFile;
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