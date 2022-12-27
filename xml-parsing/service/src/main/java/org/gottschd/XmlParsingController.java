package org.gottschd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.gottschd.stax.StaxParser;
import org.gottschd.stax.processors.Base64ExtractProcessor;
import org.gottschd.stax.processors.EmbeddedXmlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;


@RestController
public class XmlParsingController {
    private static final Logger logger = LoggerFactory.getLogger(XmlParsingController.class);

    @PostMapping(value = "/stax", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<InputStreamResource> uploadStax(HttpServletRequest request) throws Exception {
        logger.info("start stax parsing controller...");

        // setup test files + parser
        StaxParser embeddedStaxParser = new StaxParser("Embedded");

        ByteArrayOutputInputStream sink = new ByteArrayOutputInputStream();
        CopyDocumentFilteredToByteArrayProcessor copyProcessor2 = new CopyDocumentFilteredToByteArrayProcessor(sink);
        embeddedStaxParser.addProcessor(copyProcessor2);

        final List<Integer> byteCountResults = new ArrayList<>();
        embeddedStaxParser.addProcessor(
                new Base64ExtractProcessor("Data", bytes -> byteCountResults.add(Integer.valueOf(bytes.length))));
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("B", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        DetectMaxCharacterCountProcessor characterCountProcessor = new DetectMaxCharacterCountProcessor("myHoniggut",
                8 * 1000L, counted -> {
                    throw new RuntimeException("too large: " + counted);
                });
        rootParser.addProcessor(characterCountProcessor);

        long now = System.currentTimeMillis();
        try (InputStream in = request.getInputStream()) {
            rootParser.parse(in);
            copyProcessor2.finish();
        }

        logger.info("container count: {}", byteCountResults.size());
        logger.debug("container sizes each: {}", byteCountResults);

        logger.info("character count root for <myHoniggut>: {}", characterCountProcessor.getCountedCharaters());

        logger.info("finished stax parsing controller, time: {}",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now));

        return ResponseEntity.ok().contentLength(sink.size()).contentType(MediaType.APPLICATION_XML)
                .body(new InputStreamResource(sink.wrapToInputStream()));
    }

    private static class ByteArrayOutputInputStream extends ByteArrayOutputStream {
        public ByteArrayOutputInputStream() {
            super();
        }

        public InputStream wrapToInputStream() {
            return new ByteArrayInputStream(buf, 0, count);
        }
    }
}
