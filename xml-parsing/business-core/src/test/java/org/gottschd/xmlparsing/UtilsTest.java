package org.gottschd.xmlparsing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.gottschd.xmlparsing.utils.Utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UtilsTest {
    @ParameterizedTest()
    @MethodSource("provideParameters")
    void test_create_Bxml_escaped_different_sizes_new(int containerCount, int bytesPerContainer)
            throws Exception {
        Path createBigXmlFile = Utils.createEmbeddedEscapedXmlFile(containerCount,
                bytesPerContainer);
        long size = Files.size(createBigXmlFile);
        System.out.println("result: " + size);
        assertTrue(size > containerCount * bytesPerContainer); // bigger than the input
        Files.delete(createBigXmlFile);
    }

    static Stream<Arguments> provideParameters() {
        // @formatter:off
        return Stream.of(
                Arguments.of(1, 1), 
                Arguments.of(2, 512),
                // // expect no OOME when creating the following
                Arguments.of(20, 50 * 1024 * 1014), 
                Arguments.of(200, 50 * 1024 * 1014)
                );
        // @formatter:on
    }
}
