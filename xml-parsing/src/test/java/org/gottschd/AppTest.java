package org.gottschd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
