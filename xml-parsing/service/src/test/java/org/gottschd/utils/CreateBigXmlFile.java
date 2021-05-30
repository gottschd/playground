package org.gottschd.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 * 
 */ 
public class CreateBigXmlFile {

    /**
     * 
     */
    private static final String loremIpsumLine = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo";

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
    public static Path createBigXmlFile(BigFileMetaEnum pTestFileMetadata, int containerCount, int bytesPerContainer)
            throws Exception {
        Path xmlFile = Files.createTempFile("big_xml", ".tmp");

        // copy first part
        try (InputStream top = CreateBigXmlFile.class
                .getResourceAsStream(pTestFileMetadata.top_template_filename)) {
            appendInputStreamToFile(top, xmlFile);
        }

        // write the data container up to containerCount (20 maybe) times
        byte[] dataLine = getDataLine();
        for (int i = 0; i < containerCount; i++) {
            try (BufferedWriter writer = Files.newBufferedWriter(xmlFile, StandardOpenOption.APPEND)) {
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<MyContainer>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<Data>"));
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
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("</Data>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("<FileType><F>95</F><Type>txt</Type></FileType>"));
                writer.write(System.lineSeparator());
                writer.write(pTestFileMetadata.toEscapeOrNotToEscape("</MyContainer>"));
                writer.write(System.lineSeparator());
            }
        }

        // copy last part
        try (InputStream bottom = CreateBigXmlFile.class
                .getResourceAsStream(pTestFileMetadata.bottom_template_filename)) {
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