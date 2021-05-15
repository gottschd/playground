package org.gottschd;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void parseCDataXml() throws Exception {
        try (InputStream in = this.getClass().getResourceAsStream("/req_cdata_embedded_small.xml")) {
            new OuterXmlParser().parse(in);
        }
    }

    @Test
    public void parseEscapedXml() throws Exception {
        try (InputStream in = this.getClass().getResourceAsStream("/req_escaped_embedded_small.xml")) {
            new OuterXmlParser().parse(in);
        }
        System.out.println("finished");  
    }
}
