package org.gottschd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlunit.builder.Input;
import org.xmlunit.input.WhitespaceStrippedSource;

/**
 * Unit test for simple App.
 */
public class StaxParserTest {

    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_escaped_embedded_small.xml" })
    public void parseSmallXml(String xmlFileToParse) throws Exception {
        // setup test files + parser
        final List<byte[]> byteResults = new ArrayList<byte[]>();
        CopyToWriterProcessor copyToWriterProcessor = new CopyToWriterProcessor();
        EmbeddedXmlProcessor processor = new EmbeddedXmlProcessor("B",
                new StaxParser(List.of(copyToWriterProcessor, new Base64ExtractProcessor("Data", bytes -> {
                    byteResults.add(bytes);
                })), "Embedded"));
        StaxParser rootParser = new StaxParser(List.of(processor), "Root");

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {

            rootParser.parse(in);

            // check bytes arrays
            assertEquals(2, byteResults.size());
            assertEquals(Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                    StandardCharsets.UTF_8), new String(byteResults.get(0), StandardCharsets.UTF_8));

            assertEquals(Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                    StandardCharsets.UTF_8), new String(byteResults.get(1), StandardCharsets.UTF_8));

            // check remaining xml
            assertNotNull(copyToWriterProcessor.getWriterResult());
            try (InputStream expected = this.getClass().getResourceAsStream("/remaining_expected.xml")) {
                assertThat(
                        new WhitespaceStrippedSource(Input.fromString(copyToWriterProcessor.getWriterResult()).build()),
                        isSimilarTo(new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }
    }

    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @EnumSource(value = TestFileMetadata.class)
    public void parseBigXml(TestFileMetadata metadata) throws Exception {
        // setup test files + parser
        int containerCount = 20;
        int byteCountPerContainer = 50 * 1000 * 1000; // 50MB (1000 based)
        Path bigXmlFile = createBigFile(metadata, containerCount, byteCountPerContainer);
        final List<Integer> byteCountResults = new ArrayList<>();
        CopyToWriterProcessor copyToWriterProcessor = new CopyToWriterProcessor();
        EmbeddedXmlProcessor processor = new EmbeddedXmlProcessor("B",
                new StaxParser(List.of(copyToWriterProcessor, new Base64ExtractProcessor("Data", bytes -> {
                    byteCountResults.add(Integer.valueOf(bytes.length));
                })), "Embedded"));

        StaxParser rootParser = new StaxParser(List.of(processor), "Root");

        long now = System.currentTimeMillis();
        System.out.println("starting parsing:" + metadata);
        try (InputStream in = Files.newInputStream(bigXmlFile)) {

            rootParser.parse(in);

            // check bytes arrays
            assertEquals(containerCount, byteCountResults.size());
            for (Integer content : byteCountResults) {
                assertEquals(byteCountPerContainer, content.intValue());
            }

            // check remaining xml
            assertNotNull(copyToWriterProcessor.getWriterResult());
            try (InputStream expected = this.getClass().getResourceAsStream("/remaining_expected.xml")) {
                assertThat(
                        new WhitespaceStrippedSource(Input.fromString(copyToWriterProcessor.getWriterResult()).build()),
                        isSimilarTo(new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }

        Files.delete(bigXmlFile);

        System.out.println("finished parsing :" + metadata + ", time: "
                + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now));
    }

    private Path createBigFile(TestFileMetadata pTestFileMetadata, int containerCount, int bytesPerContainer)
            throws Exception {
        Path xmlFile = Files.createTempFile("big_xml", ".tmp");

        // copy first part
        try (InputStream expected = this.getClass().getResourceAsStream(pTestFileMetadata.top_template_filename)) {
            appendInputStreamToFile(expected, xmlFile);
        }

        // write the data container up to containerCount (20 maybe) times
        for (int i = 0; i < containerCount; i++) {
            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<MyContainer>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<Data>"));
            }

            // append the lorem ipsum text as base64 stream to reach up to 50MB per data
            // container
            byte[] lineToWrite;
            try (InputStream in = this.getClass().getResourceAsStream("/lorem_ipsum_expected.txt");
                    ByteArrayOutputStream line = new ByteArrayOutputStream()) {
                line.write(in.readAllBytes());
                line.write(System.lineSeparator().getBytes());
                line.flush();
                lineToWrite = line.toByteArray();
            }

            try (OutputStream out = Base64.getEncoder()
                    .wrap(new BufferedOutputStream(Files.newOutputStream(xmlFile, StandardOpenOption.APPEND)))) {
                int lineIterations = bytesPerContainer / lineToWrite.length;
                while (lineIterations-- > 0) {
                    out.write(lineToWrite);
                }

                // write rest
                int lineRest = bytesPerContainer % lineToWrite.length;
                out.write(lineToWrite, 0, lineRest);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("</Data>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<FileType><F>95</F><Type>txt</Type></FileType>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("</MyContainer>"));
                writer.write(System.lineSeparator());
            }
        }

        // copy last part
        try (

                InputStream expected = this.getClass()
                        .getResourceAsStream(pTestFileMetadata.bottom_template_filename)) {
            appendInputStreamToFile(expected, xmlFile);
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
