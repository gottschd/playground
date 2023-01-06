package org.gottschd;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gottschd.utils.Utils;
import org.junit.jupiter.api.Test;

class UtilsTest {
    @Test
    void test_Bxml_escaped_big() throws Exception {
        Path createBigXmlFile = Utils.createEmbeddedEscapedXmlFile(20, 50 * 1024 * 1024);
        long size = Files.size(createBigXmlFile);
        assertTrue(size > 1024 * 1024 * 1024); // bigger than 1GB
    }

    @Test
    void test_Bxml_escaped_big_multiple_times() throws Exception {
        int count = 5;
        while (count-- > 0) {
            Path createBigXmlFile = Utils.createEmbeddedEscapedXmlFile(20, 50 * 1024 * 1024);
            long size = Files.size(createBigXmlFile);
            assertTrue(size > 1024 * 1024 * 1024); // bigger than 1GB
        }
    }

}
