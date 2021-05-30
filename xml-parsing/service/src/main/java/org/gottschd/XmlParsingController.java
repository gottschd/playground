package org.gottschd;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.gottschd.stax.StaxParser;
import org.gottschd.stax.processors.Base64ExtractProcessor;
import org.gottschd.stax.processors.CopyToWriterProcessor;
import org.gottschd.stax.processors.EmbeddedXmlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XmlParsingController {
    private static final Logger logger = LoggerFactory.getLogger(XmlParsingController.class);

    @PostMapping(value = "/komm", consumes = "application/xml", produces = "application/xml")
    public String upload(HttpServletRequest request) throws Exception {
        logger.info("start parsing controller");

        // setup test files + parser
        CopyToWriterProcessor copyToWriterProcessor = new CopyToWriterProcessor();
        StaxParser embeddedStaxParser = new StaxParser("Embedded");
        embeddedStaxParser.addProcessor(copyToWriterProcessor);

        final List<byte[]> byteCountResults = new ArrayList<>();
        embeddedStaxParser.addProcessor(new Base64ExtractProcessor("Data", bytes -> {
            byteCountResults.add(bytes);
        }));
        EmbeddedXmlProcessor embeddedProcessor = new EmbeddedXmlProcessor("B", embeddedStaxParser);

        StaxParser rootParser = new StaxParser("Root");
        rootParser.addProcessor(embeddedProcessor);

        long now = System.currentTimeMillis();
        try (InputStream in = request.getInputStream()) {
            rootParser.parse(in);
        }
        logger.info("container count: {}", byteCountResults.size());
        logger.debug("container sizes: {}", byteCountResults);

        logger.info("finished parsing controller, time: {}",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now));

        return copyToWriterProcessor.getWriterResult();
    }
}
