package org.gottschd;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    @Disabled
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
