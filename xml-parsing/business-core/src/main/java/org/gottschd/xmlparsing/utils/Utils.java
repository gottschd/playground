package org.gottschd.xmlparsing.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.StringJoiner;

public class Utils {

    private static final String loremIpsumLine = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
            tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At \
            vero eos et accusam et justo duo""";

    private static final char[] loremIpsumLineChars = loremIpsumLine.toCharArray();

    private static final String MARKER = "XYXData_MarkerXYX";

    private static final byte[] dataBytesTemplate;
    static {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while (outStream.size() < 1 * 1024 * 1024) {
                outStream.write(loremIpsumLine.getBytes(StandardCharsets.UTF_8));
                outStream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }
            outStream.flush();
            dataBytesTemplate = outStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Could not create template data bytes", e);
        }
    }

    private static void insertContainerDataBytesAsBase64String(OutputStream target,
            int bytesPerContainer) throws IOException {

        OutputStream wrap = Base64.getEncoder().wrap(target);

        // write line to nearby possible size
        int lineIterations = bytesPerContainer / dataBytesTemplate.length;
        while (lineIterations-- > 0) {
            wrap.write(dataBytesTemplate);
        }
        // write rest
        int lineRest = bytesPerContainer % dataBytesTemplate.length;
        wrap.write(dataBytesTemplate, 0, lineRest);

        wrap.close();
    }

    private static String readStringFromInputStream(InputStream src) throws IOException {
        return new String(src.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Path createEmbeddedEscapedXmlFile(int containerCount, int bytesPerContainer)
            throws IOException, URISyntaxException {
        System.out.println("creating big file...");
        long now = System.currentTimeMillis();

        final Path templateFile = createTemplateFileEscaped(MARKER, containerCount,
                bytesPerContainer);

        // replace the markers with the correct content size
        Path bigFile = Files.createTempFile(
                "big_xml_" + containerCount + "_" + bytesPerContainer + "_", ".tmp");

        try (BufferedWriter fos = Files.newBufferedWriter(bigFile, StandardOpenOption.APPEND)) {
            Files.lines(templateFile, StandardCharsets.UTF_8).forEach(line -> {
                // replace the marker with the generated attachment
                try {
                    if (line.contains(MARKER)) {
                        String[] lineParts = line.split(MARKER);
                        fos.write(lineParts[0]);
                        fos.flush(); // flush anything so far

                        // long now2 = System.currentTimeMillis();
                        insertContainerDataBytesAsBase64String(
                                Files.newOutputStream(bigFile, StandardOpenOption.APPEND),
                                bytesPerContainer);

                        // System.out.println(
                        // "time per container: " + (System.currentTimeMillis() - now2));
                        fos.write(lineParts[1]);
                    } else {
                        // copy the line as is to the output
                        fos.write(line);
                    }

                    // terminate the line
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

    private static Path createTemplateFileEscaped(String dataMarker, int containerCount,
            int bytesPerContainer) throws IOException {
        // (TODO) There is potential for improvement in memory consumption when
        // containerCount is high

        String containerTemplate = """
                &lt;MyContainer&gt;
                    &lt;Data&gt;%s&lt;/Data&gt;
                    &lt;FileName&gt;myFileName&lt;/FileName&gt;
                    &lt;FileType&gt;
                        &lt;F&gt;95&lt;/F&gt;
                        &lt;Type&gt;txt&lt;/Type&gt;
                    &lt;/FileType&gt;
                &lt;/MyContainer&gt;
                """.formatted(dataMarker);

        StringJoiner containers = new StringJoiner(System.lineSeparator());
        for (int i = 0; i < containerCount; i++) {
            containers.add(containerTemplate);
        }

        final Path templateFile;
        try (InputStream in = Utils.class
                .getResourceAsStream("/req_embedded_escaped_template.xml")) {
            String outerTemplate = readStringFromInputStream(in);

            templateFile = Files.createTempFile(
                    "template_xml_" + containerCount + "_" + bytesPerContainer + "_", ".tmp");
            Files.writeString(templateFile, outerTemplate.formatted(containers.toString()));
        }
        return templateFile;
    }

    public static String createHoniggutBase64Xml(int characterCount) {
        String honiggutXml = """
                <honig>
                    <gut>%s</gut>
                </honig>
                """.formatted(Base64.getEncoder()
                .encodeToString(createLorumIpsum(characterCount).getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().encodeToString(honiggutXml.getBytes(StandardCharsets.UTF_8));
    }

    private static String createLorumIpsum(int characterCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < characterCount; i++) {
            char chartoAdd = loremIpsumLineChars[i % loremIpsumLineChars.length];
            sb.append(chartoAdd);
        }
        return sb.toString();
    }
}
