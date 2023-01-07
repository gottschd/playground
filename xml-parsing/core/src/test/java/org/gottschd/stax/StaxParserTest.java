package org.gottschd.stax;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.processors.AllowedPathsProcessor;
import org.gottschd.stax.processors.Base64ExtractProcessor;
import org.gottschd.stax.processors.CopyProcessor;
import org.gottschd.stax.processors.DetectXmlTag;
import org.gottschd.stax.processors.EmbeddedBase64XmlProcessor;
import org.gottschd.stax.processors.EmbeddedXmlProcessor;
import org.gottschd.stax.utils.ByteArrayOutputInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlunit.builder.Input;
import org.xmlunit.input.WhitespaceStrippedSource;

/**
 * Unit test for simple App.
 */
class StaxParserTest {

    @ParameterizedTest()
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_escaped_embedded_small.xml" })
    void testCopyXmlEmbedded(String xmlFileToParse) throws Exception {
        // setup test files + parser

        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        final ByteArrayOutputInputStream copiedBytes = new ByteArrayOutputInputStream();
        CopyProcessor copyProcessor = new CopyProcessor("Message", "Message/MyContent/Breakpoint",
                inputStream -> {
                    try {
                        copiedBytes.write(inputStream.readAllBytes());
                    } catch (IOException e) {
                        throw new StaxParserParsingException(e.getMessage(), e);
                    }
                });
        embeddedStaxParser.addProcessor(copyProcessor);

        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("Envelope/Body/A/B",
                inputStream -> {
                    try {
                        embeddedStaxParser.parse(inputStream);
                    } catch (XMLStreamException ex) {
                        throw new StaxParserParsingException(ex.getMessage(), ex);
                    }
                });

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {

            rootParser.parse(in);

            // check copied xml
            try (InputStream expected = this.getClass()
                    .getResourceAsStream("/remaining_expected.xml")) {
                assertThat(
                        new WhitespaceStrippedSource(
                                Input.fromStream(copiedBytes.wrapToInputStream()).build()),
                        isSimilarTo(
                                new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_escaped_embedded_small.xml" })
    void testBase64ExtractEmbedded(String xmlFileToParse) throws Exception {
        StaxParser embeddedStaxParser = new StaxParser("Embedded");

        final List<byte[]> byteResults = new ArrayList<byte[]>();
        embeddedStaxParser.addProcessor(
                new Base64ExtractProcessor("Message/MyContent/MyContainer/Data", bytes -> {
                    byteResults.add(bytes);
                }));

        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("Envelope/Body/A/B",
                inputStream -> {
                    try {
                        embeddedStaxParser.parse(inputStream);
                    } catch (XMLStreamException ex) {
                        throw new StaxParserParsingException(ex.getMessage(), ex);
                    }
                });

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {

            rootParser.parse(in);

            // check bytes arrays
            assertEquals(2, byteResults.size());
            assertEquals(
                    Files.readString(Paths
                            .get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                            StandardCharsets.UTF_8),
                    new String(byteResults.get(0), StandardCharsets.UTF_8));

            assertEquals(
                    Files.readString(Paths
                            .get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                            StandardCharsets.UTF_8),
                    new String(byteResults.get(1), StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = { "/req_xml_B.xml" })
    public void testDetectXmlTag(String xmlFileToParse) throws Exception {

        StaxParser rootParser = new StaxParser("Root");

        final var typeEventsForXmlTag = new HashSet<XmlType>();

        rootParser.addProcessor(new DetectXmlTag("Envelope/Body/BMessage/myHoniggut", eventType -> {
            typeEventsForXmlTag.add(new XmlType(eventType.intValue()));
        }));

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {
            rootParser.parse(in);
        }

        assertEquals(2, typeEventsForXmlTag.size());
        assertTrue(typeEventsForXmlTag.contains(new XmlType(XMLStreamConstants.START_ELEMENT)));
        assertTrue(typeEventsForXmlTag.contains(new XmlType(XMLStreamConstants.END_ELEMENT)));

    }

    private record XmlType(int xmlTagType) {
    }

    @Test
    public void testExceptionPropagation() throws Exception {
        // setup test files + parser

        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(new EventTypeProcessor() {
            @Override
            public void processEvent(XMLStreamReader xmlr, StaxParseContext context) {
                if (xmlr.isStartElement() && "ThrowMe".equals(xmlr.getLocalName()))
                    throw new RuntimeException("Ups ThrowMe tag found.");
            }
        });

        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("A/C", inputStream -> {
            try {
                embeddedStaxParser.parse(inputStream);
            } catch (XMLStreamException ex) {
                throw new StaxParserParsingException(ex.getMessage(), ex);
            }
        });

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        String xmlString = """
                <A>
                    <B>ValueOuter</B>
                    <C>
                        <![CDATA[<Inner><ThrowMe/></Inner>]]>
                    </C>
                </A>""";
        try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
            rootParser.parse(in);
            fail("expected runtime exception for embedded content not thrown");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Ups ThrowMe tag found"));
        }
    }

    @ParameterizedTest
    @MethodSource(value = "testAllowedPathProcessor_Source")
    public void testAllowedPathProcessor(List<String> allowedPaths, Boolean expectedResult)
            throws Exception {
        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(new AllowedPathsProcessor(allowedPaths.toArray(new String[0])));

        final String xmlString = """
                <A>
                    <B>ValueOuter</B>
                    <C>
                        what_ever
                    </C>
                </A>""";

        try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
            rootParser.parse(in);
            assertTrue(expectedResult.booleanValue());
        } catch (Exception e) {
            assertFalse(expectedResult.booleanValue());
        }
    }

    static Stream<Arguments> testAllowedPathProcessor_Source() {
        return Stream.of(Arguments.of(List.of("A", "A/B", "A/C"), Boolean.TRUE),
                Arguments.of(List.of("A/B", "A/C"), Boolean.TRUE),
                Arguments.of(List.of("F"), Boolean.FALSE),
                Arguments.of(List.of("A/C"), Boolean.FALSE));
    }

    @Test
    void testEmbeddedBase64Xml() throws Exception {
        final ByteArrayOutputInputStream copiedBytes = new ByteArrayOutputInputStream();

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(new EmbeddedBase64XmlProcessor("A/C", inputStream -> {
            try {
                copiedBytes.write(inputStream.readAllBytes());
            } catch (IOException e) {
                throw new StaxParserParsingException(e.getMessage(), e);
            }
        }));

        String base64EncodedXml = "";
        try (InputStream is = this.getClass().getResourceAsStream("/remaining_expected.xml")) {
            byte[] allBytes = is.readAllBytes();
            base64EncodedXml = Base64.getEncoder().encodeToString(allBytes);
        }

        final String xmlString = """
                <A>
                    <B>ValueOuter</B>
                    <C>%s</C>
                </A>""".formatted(base64EncodedXml);

        try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
            rootParser.parse(in);

            // check copied xml
            try (InputStream expected = this.getClass()
                    .getResourceAsStream("/remaining_expected.xml")) {
                assertThat(
                        new WhitespaceStrippedSource(
                                Input.fromStream(copiedBytes.wrapToInputStream()).build()),
                        isSimilarTo(
                                new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }

    }
}
