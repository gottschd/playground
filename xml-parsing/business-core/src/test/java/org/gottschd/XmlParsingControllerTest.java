package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.gottschd.XmlParsingController.Result;
import org.junit.jupiter.api.Test;

public class XmlParsingControllerTest {

    private static final String loremIpsumLine = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
            tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At \
            vero eos et accusam et justo duo""";

    private static final char[] loremIpsumLineChars = loremIpsumLine.toCharArray();

    @Test
    void testParsingB() throws Exception {

        byte[] containerBytes = createContainerDataBytes(512);

        String xml = """
                    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Header/>
                    <SOAP-ENV:Body>
                        <ns3:A xmlns:ns3="urn:myNamespace">
                            <B>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;yes&quot;?&gt;
                &lt;Message xmlns=&quot;http://www.myOther.org&quot;&gt;
                    &lt;MyHeader&gt;
                        &lt;C&gt;test-mail@dummy.org&lt;/C&gt;
                    &lt;/MyHeader&gt;
                    &lt;MyContent&gt;
                        &lt;D&gt;NachrichtBetreff&lt;/D&gt;
                        &lt;E&gt;
                            &lt;F&gt;9002&lt;/F&gt;
                        &lt;/E&gt;
                        &lt;Breakpoint&gt;
                            &lt;Encoding&gt;
                                &lt;F&gt;9004&lt;/F&gt;
                                &lt;Type&gt;text/plain&lt;/Type&gt;
                            &lt;/Encoding&gt;
                            &lt;Text&gt;NachrichtText&lt;/Text&gt;
                        &lt;/Breakpoint&gt;
                        &lt;MyContainer&gt;
                            &lt;Data&gt;%1$s&lt;/Data&gt;
                            &lt;FileName&gt;myFileName&lt;/FileName&gt;
                            &lt;FileType&gt;
                                &lt;F&gt;95&lt;/F&gt;
                                &lt;Type&gt;txt&lt;/Type&gt;
                            &lt;/FileType&gt;
                        &lt;/MyContainer&gt;
                        &lt;MyContainer&gt;
                            &lt;Data&gt;%1$s&lt;/Data&gt;
                            &lt;FileName&gt;FileName2&lt;/FileName&gt;
                            &lt;FileType&gt;
                                &lt;F&gt;95&lt;/F&gt;
                                &lt;Type&gt;txt&lt;/Type&gt;
                            &lt;/FileType&gt;
                        &lt;/MyContainer&gt;
                    &lt;/MyContent&gt;
                &lt;/Message&gt;</B>
                        </ns3:A>
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>
                """
                .formatted(Base64.getEncoder().encodeToString(containerBytes));

        System.out.println(xml);
        Result result = XmlParsingController
                .uploadSoap(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertFalse(result.isHoniggutXml());
        assertTrue(result.isBXml());
        assertNull(result.getErrorMessage());
        assertEquals(2, result.getExtractedDataSizes().size());
        assertEquals(512, result.getExtractedDataSizes().get(0));
        assertEquals(512, result.getExtractedDataSizes().get(1));
    }

    private static byte[] createContainerDataBytes(int bytesPerContainer) throws Exception {
        // make a template
        ByteArrayOutputStream dataTemplate = new ByteArrayOutputStream();
        dataTemplate.write(loremIpsumLine.getBytes(StandardCharsets.UTF_8));
        dataTemplate.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        dataTemplate.flush();
        byte[] line = dataTemplate.toByteArray();

        // create the resulting bytes size
        int lineIterations = bytesPerContainer / line.length;
        LeakingByteArrayOutputStream result = new LeakingByteArrayOutputStream(bytesPerContainer);
        // write line to nearby possible size
        while (lineIterations-- > 0) {
            result.write(line);
        }
        // write rest
        int lineRest = bytesPerContainer % line.length;
        result.write(line, 0, lineRest);
        result.flush();

        return result.getByteArray();
    }

    /**
     * Leaks the internal buf instead of making a distinct copy.
     */
    private static class LeakingByteArrayOutputStream extends ByteArrayOutputStream {

        LeakingByteArrayOutputStream(int bytesPerContainer) {
            super(bytesPerContainer);
        }

        byte[] getByteArray() {
            if (count != buf.length) {
                throw new IllegalStateException("that should not happen");
            }
            return buf;
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
                </SOAP-ENV:Envelope>""".formatted(createHoniggutBase64Xml(512));

        Result result = XmlParsingController
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
                </SOAP-ENV:Envelope>""".formatted(createHoniggutBase64Xml(12000));

        Result result = XmlParsingController
                .uploadSoap(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertFalse(result.isHoniggutXml());
        assertFalse(result.isBXml());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("too large"));
    }

    private String createHoniggutBase64Xml(int characterCount) {
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