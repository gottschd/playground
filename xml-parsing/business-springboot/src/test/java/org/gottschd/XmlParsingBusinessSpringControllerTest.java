package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class XmlParsingBusinessSpringControllerTest {
    private static final String loremIpsumLine = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
            tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At \
            vero eos et accusam et justo duo""";

    @LocalServerPort
    int port;

    @Test
    void testSmallB() throws Exception {
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

        System.out.println("building request...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/stax"))
                .headers("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(xml)).build();

        // send streamed request
        System.out.println("performing request...");
        HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("performing request done. response: " + response.body());

        assertEquals(200, response.statusCode());
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

}