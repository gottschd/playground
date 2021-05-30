package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class XmlParsingControllerTest {
    /**
    * 
    */
    private static final String loremIpsumLine = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo";

    @LocalServerPort
    int port;

    @Test
    void testXmlParsing() throws Exception {
        // xml file with 20 * 50 MB data = 1GB data (approximated)
        System.out.println("building big xml...");
        Path xmlFile = createBigXmlFile(20, 50 * 1000 * 1000);
        System.out.println("building big xml finished.");

        System.out.println("building request...");
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://localhost:" + port + "/komm"))
                .headers("Content-Type", "application/xml").POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return Files.newInputStream(xmlFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })).build();
        System.out.println("building request finished.");

        System.out.println("performing request...");
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("perfoming request done.");

        assertNotNull(response.body());
    }


    /**
     * 
     */
    private static byte[] getDataLine() throws Exception {
        try (ByteArrayOutputStream line = new ByteArrayOutputStream()) {
            line.write(loremIpsumLine.getBytes(StandardCharsets.UTF_8));
            line.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            line.flush();
            return line.toByteArray();
        }
    }

    /**
     * 
     */
    public static Path createBigXmlFile(int containerCount, int bytesPerContainer)
            throws Exception {
        Path xmlFile = Files.createTempFile("big_xml", ".tmp");

        // copy first part
        try (InputStream top = new ClassPathResource("/req_cdata_embedded_template_top.xml").getInputStream()) {
            appendInputStreamToFile(top, xmlFile);
        }

        // write the data container up to containerCount (20 maybe) times
        byte[] dataLine = getDataLine();
        for (int i = 0; i < containerCount; i++) {
            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write("<MyContainer>");
                writer.write(System.lineSeparator());
                writer.write("<Data>");
            }

            try (OutputStream out = Base64.getEncoder()
                    .wrap(new BufferedOutputStream(Files.newOutputStream(xmlFile, StandardOpenOption.APPEND)))) {
                int lineIterations = bytesPerContainer / dataLine.length;
                // write line to nearby possible size
                while (lineIterations-- > 0) {
                    out.write(dataLine);
                }

                // write rest
                int lineRest = bytesPerContainer % dataLine.length;
                out.write(dataLine, 0, lineRest);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write(System.lineSeparator());
                writer.write("</Data>");
                writer.write(System.lineSeparator());
                writer.write("<FileType><F>95</F><Type>txt</Type></FileType>");
                writer.write(System.lineSeparator());
                writer.write("</MyContainer>");
                writer.write(System.lineSeparator());
            }
        }

        // copy last part
        try (InputStream bottom = new ClassPathResource("/req_cdata_embedded_template_bottom.xml").getInputStream()) {
            appendInputStreamToFile(bottom, xmlFile);
        }

        return xmlFile;
    }

    private static void appendInputStreamToFile(InputStream inputStream, Path file) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(file, StandardOpenOption.APPEND))) {
            inputStream.transferTo(outputStream);
        }
    }
}