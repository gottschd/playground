package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gottschd.XmlParsingBusinessLogic.Result;
import org.gottschd.utils.Utils;
import org.junit.jupiter.api.Test;

public class XmlParsingBusinessLogicTest {

    @Test
    void testParsingB() throws Exception {
        Path xmlFile = Utils.createEmbeddedEscapedXmlFile(2, 512);
        try {

            System.out.println(Files.readString(xmlFile));

            Result result = XmlParsingBusinessLogic.uploadSoap(Files.newInputStream(xmlFile));

            assertFalse(result.isHoniggutXml());
            assertTrue(result.isBXml());
            assertNull(result.getErrorMessage());
            assertEquals(2, result.getExtractedDataSizes().size());
            assertEquals(512, result.getExtractedDataSizes().get(0));
            assertEquals(512, result.getExtractedDataSizes().get(1));
        } finally {
            Files.delete(xmlFile);
        }
    }

    @Test
    void testParsingHoniggut() throws Exception {
        String xml = """
                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Header/>
                    <SOAP-ENV:Body>
                        <ns3:BMessage xmlns:ns3="urn:blubb:bla:honiggut">
                            <myHoniggut>%s</myHoniggut>
                        </ns3:BMessage>
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>""".formatted(Utils.createHoniggutBase64Xml(512));

        Result result = XmlParsingBusinessLogic
                .uploadSoap(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertTrue(result.isHoniggutXml());
        assertFalse(result.isBXml());
        assertNull(result.getErrorMessage());
        assertEquals(1, result.getExtractedDataSizes().size());
        assertEquals(512, result.getExtractedDataSizes().get(0));
    }

    @Test
    void testParsingHoniggut_tooBig() throws Exception {
        String xml = """
                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Header/>
                    <SOAP-ENV:Body>
                        <ns3:BMessage xmlns:ns3="urn:blubb:bla:honiggut">
                            <myHoniggut>%s</myHoniggut>
                        </ns3:BMessage>
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>""".formatted(Utils.createHoniggutBase64Xml(12000));

        Result result = XmlParsingBusinessLogic
                .uploadSoap(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertFalse(result.isHoniggutXml());
        assertFalse(result.isBXml());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("too large"));
    }
}