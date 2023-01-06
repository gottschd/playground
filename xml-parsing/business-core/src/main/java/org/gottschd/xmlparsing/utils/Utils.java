package org.gottschd.xmlparsing.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.StringJoiner;

public class Utils {

    private static final String loremIpsumLine = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
            tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At \
            vero eos et accusam et justo duo""";

    private static final char[] loremIpsumLineChars = loremIpsumLine.toCharArray();

    private static final String MARKER = "XXXXXX";

    private static final String DATALINE_WITH_MARKER_ESCAPED = "&lt;Data&gt;XXXXXX&lt;/Data&gt;";

    private static byte[] createContainerDataBytes(int bytesPerContainer) throws IOException {
        // make a template
        ByteArrayOutputStream dataTemplate = new ByteArrayOutputStream();
        dataTemplate.write(loremIpsumLine.getBytes(StandardCharsets.UTF_8));
        dataTemplate.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        dataTemplate.flush();
        byte[] line = dataTemplate.toByteArray();

        // create the resulting bytes size
        int lineIterations = bytesPerContainer / line.length;
        ByteArrayOutputStream result = new ByteArrayOutputStream(bytesPerContainer);
        // write line to nearby possible size
        while (lineIterations-- > 0) {
            result.write(line);
        }
        // write rest
        int lineRest = bytesPerContainer % line.length;
        result.write(line, 0, lineRest);
        result.flush();

        return result.toByteArray();
    }

    private static String readStringFromInputStream(InputStream src) throws IOException {
        return new String(src.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Path createEmbeddedEscapedXmlFile(int containerCount, int bytesPerContainer)
            throws IOException, URISyntaxException {
        System.out.println("creating big file...");
        long now = System.currentTimeMillis();

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

        final Path templateFile;
        try (InputStream in = Utils.class
                .getResourceAsStream("/req_embedded_escaped_template.xml")) {
            String outerTemplate = readStringFromInputStream(in);

            templateFile = Files.createTempFile(
                    "template_xml_" + containerCount + "_" + bytesPerContainer + "_", ".tmp");
            Files.write(templateFile, outerTemplate.formatted(containers.toString())
                    .getBytes(StandardCharsets.UTF_8));
        } finally {
            containers = null;
        }

        // replace the markers with the correct content size
        String newInhalt = DATALINE_WITH_MARKER_ESCAPED.replace(MARKER,
                Base64.getEncoder().encodeToString(createContainerDataBytes(bytesPerContainer)));
        Path bigFile = Files.createTempFile(
                "big_xml_" + containerCount + "_" + bytesPerContainer + "_", ".tmp");
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
