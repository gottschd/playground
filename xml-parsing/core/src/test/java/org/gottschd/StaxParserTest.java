package org.gottschd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParser;
import org.gottschd.stax.processors.Base64ExtractProcessor;
import org.gottschd.stax.processors.CopyToWriterProcessor;
import org.gottschd.stax.processors.DetectXmlTag;
import org.gottschd.stax.processors.EmbeddedXmlProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

        CopyToWriterProcessor copyToWriterProcessor = new CopyToWriterProcessor();
        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(copyToWriterProcessor);

        final List<byte[]> byteResults = new ArrayList<byte[]>();
        embeddedStaxParser.addProcessor(new Base64ExtractProcessor("Data", bytes -> {
            byteResults.add(bytes);
        }));
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("B", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

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
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_escaped_embedded_small.xml" })
    public void parseSmallXmlWithStreamSink(String xmlFileToParse) throws Exception {
        // setup test files + parser

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        CopyDocumentFilteredToByteArrayProcessor copyProcessors = new CopyDocumentFilteredToByteArrayProcessor(sink);

        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(copyProcessors);

        final List<byte[]> byteResults = new ArrayList<byte[]>();
        embeddedStaxParser.addProcessor(new Base64ExtractProcessor("Data", bytes -> {
            byteResults.add(bytes);
        }));
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("B", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {

            rootParser.parse(in);

            copyProcessors.finish();

            // check bytes arrays
            assertEquals(2, byteResults.size());
            assertEquals(Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                    StandardCharsets.UTF_8), new String(byteResults.get(0), StandardCharsets.UTF_8));

            assertEquals(Files.readString(Paths.get(this.getClass().getResource("/lorem_ipsum_expected.txt").toURI()),
                    StandardCharsets.UTF_8), new String(byteResults.get(1), StandardCharsets.UTF_8));

            // check remaining xml
            System.out.println("sink size: " + sink.size());
            assertTrue(sink.size() > 0);
            try (InputStream expected = this.getClass().getResourceAsStream("/remaining_expected.xml")) {
                assertThat(new WhitespaceStrippedSource(Input.fromString(sink.toString()).build()),
                        isSimilarTo(new WhitespaceStrippedSource(Input.fromStream(expected).build())));
            }
        }
    }

    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @ValueSource(strings = { "/req_cdata_embedded_small.xml", "/req_xml_B.xml" })
    public void parseSmallXmlCheckWhichTypeOfXml(String xmlFileToParse) throws Exception {
        // setup test files + parser

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        CopyDocumentFilteredToByteArrayProcessor copyProcessors = new CopyDocumentFilteredToByteArrayProcessor(sink);

        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(copyProcessors);

        final List<byte[]> byteResults = new ArrayList<byte[]>();
        embeddedStaxParser.addProcessor(new Base64ExtractProcessor("Data", bytes -> {
            byteResults.add(bytes);
        }));
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("B", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        final AtomicBoolean isMyHoniggutXml = new AtomicBoolean(false);
        final AtomicBoolean isBXml = new AtomicBoolean(false);

        rootParser.addProcessor(new DetectXmlTag("myHoniggut", eventType -> {
            if( XMLStreamConstants.START_ELEMENT == eventType.intValue() )
                isMyHoniggutXml.set(true); // it is a myHoniggut Xml as soon as we found the start tag
        }));
        rootParser.addProcessor(new DetectXmlTag("B", eventType -> {
            if( XMLStreamConstants.START_ELEMENT == eventType.intValue() )
                isBXml.set(true); // it is a B Xml as soon as we found the start tag
        }));

        try (InputStream in = this.getClass().getResourceAsStream(xmlFileToParse)) {
            rootParser.parse(in);
            copyProcessors.finish();
        }

        if( xmlFileToParse.equals("/req_cdata_embedded_small.xml") ) {
            assertTrue(isBXml.get());
            assertFalse(isMyHoniggutXml.get());
        } else if( xmlFileToParse.equals("/req_xml_B.xml") ) {
            assertFalse(isBXml.get());
            assertTrue(isMyHoniggutXml.get());
        } else {
            fail("unknown xml type");
        }

    }

    @Test
    public void parseXmlWithErrorInEmbeddedSection() throws Exception {
        // setup test files + parser

        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(new EventTypeProcessor() {
            @Override
            public void processEvent(XMLStreamReader xmlr) throws Exception {
                if (xmlr.isStartElement() && "Throw".equals(xmlr.getLocalName()))
                    throw new RuntimeException("Ups Throw tag found.");
            }
        });
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("C", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        String xmlString = "<A><B>ValueOuter</B><C><![CDATA[<Inner><Throw/></Inner>]]></C></A>";
        try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
            rootParser.parse(in);
            fail("expected runtime exception for embedded content not thrown");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Ups Throw tag found"));
        }
    }
}
