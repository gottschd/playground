package org.gottschd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlunit.builder.Input;
import org.xmlunit.input.WhitespaceStrippedSource;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @ParameterizedTest(name = "{index}: {0}")
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_escaped_embedded_small.xml" })
    public void parseSmallXml(String xmlFileToParse) throws Exception {
        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {
            OuterXmlParser outerXmlParser = new OuterXmlParser();
            outerXmlParser.parse(in);
            XmlParsingResult result = outerXmlParser.getXmlParsingResult();

            // check bytes arrays
            assertEquals(2, result.getDataOfMyContainer().size());
            assertEquals(
                    Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                            StandardCharsets.UTF_8),
                    new String(result.getDataOfMyContainer().get(0), StandardCharsets.UTF_8));

            assertEquals(
                    Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                            StandardCharsets.UTF_8),
                    new String(result.getDataOfMyContainer().get(1), StandardCharsets.UTF_8));

            // check remaining xml
            assertNotNull(result.getRemainingXml());
            try (InputStream expected = this.getClass().getResourceAsStream("/remaining_expected.xml")) {
                assertThat(new WhitespaceStrippedSource(Input.fromString(result.getRemainingXml()).build()),
                        isSimilarTo(new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }
    }

    @Test
    public void parseBigXml() throws Exception {
        long now = System.currentTimeMillis();
     
        Path bigXmlFile = createBigFile();
     
        System.out.println("duration (sec): " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now));

        Files.delete(bigXmlFile);
    }

    private Path createBigFile() throws Exception {
        Path xmlFile = Files.createTempFile("big_xml", ".tmp");

        // copy first part
        try (InputStream expected = this.getClass().getResourceAsStream("/req_cdata_embedded_template_top.xml")) {
            appendInputStreamToFile(expected, xmlFile);
        }

        // write the data container up to 20 times
        for (int i = 0; i < 20; i++) {
            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write("<MyContainer>");
                writer.write(System.lineSeparator());
                writer.write("<Data>");
            }

            // append the lorem ipsum text as base64 stream to reach up to 50MB per data
            // container
            try (InputStream in = this.getClass().getResourceAsStream("/lorem_ipsum_expected.txt")) {
                final byte[] loremIpsumBytes = in.readAllBytes();
                try (OutputStream out = Base64.getEncoder()
                        .wrap(new BufferedOutputStream(Files.newOutputStream(xmlFile, StandardOpenOption.APPEND)))) {
                    for (int j = 0; j < 200_000; j++) {
                        out.write(loremIpsumBytes);
                        out.write(System.lineSeparator().getBytes());
                    }
                }
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
        try (InputStream expected = this.getClass().getResourceAsStream("/req_cdata_embedded_template_bottom.xml")) {
            appendInputStreamToFile(expected, xmlFile);
        }

        return xmlFile;
    }

    private static void appendInputStreamToFile(InputStream inputStream, Path file) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.APPEND))) {
            inputStream.transferTo(outputStream);
        }

    }
}
