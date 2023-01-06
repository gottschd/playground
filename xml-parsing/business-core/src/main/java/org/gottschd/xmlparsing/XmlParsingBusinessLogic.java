package org.gottschd.xmlparsing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.gottschd.stax.StaxParser;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.processors.AllowedPathsProcessor;
import org.gottschd.stax.processors.Base64ExtractProcessor;
import org.gottschd.stax.processors.EmbeddedBase64XmlProcessor;
import org.gottschd.stax.processors.EmbeddedXmlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParsingBusinessLogic {
    private static final Logger logger = LoggerFactory.getLogger(XmlParsingBusinessLogic.class);

    /**
     * Here we expect two predefined but different XMLs.
     * 
     * @param requestInputStream
     * @return the result as inputstream
     * @throws Exception
     */
    public static Result uploadSoap(InputStream requestInputStream) {
        logger.info("start business stax parsing ...");
        long now = System.currentTimeMillis();
        final Result result = new Result();

        StaxParser soapParser = new StaxParser("Soap");

        // add primitive checker for correct payload
        soapParser.addProcessor(
                new AllowedPathsProcessor("Envelope/Header", "Envelope/Body/BMessage/myHoniggut", // <-
                                                                                                  // a
                                                                                                  // base64
                                                                                                  // encoded
                                                                                                  // embedded
                                                                                                  // xml
                        "Envelope/Body/A/B") // <- the cdata/escaped embedded xml
        );

        // add processor for the embedded (cdata or escaped) xml
        soapParser.addProcessor(new EmbeddedXmlProcessor("Envelope/Body/A/B", inputStream -> {
            List<Integer> extractedDataSizes = streamBInternal(inputStream);
            result.setBXml(true);
            result.setExtractedDataSizes(extractedDataSizes);
        }));

        // add processor for the base64 xml
        soapParser.addProcessor(
                new EmbeddedBase64XmlProcessor("Envelope/Body/BMessage/myHoniggut", inputStream -> {
                    List<Integer> extractedDataSizes = streamHoniggutInternal(inputStream);
                    result.setHoniggutXml(true);
                    result.setExtractedDataSizes(extractedDataSizes);
                }));

        DetectMaxCharacterCountProcessor characterCountProcessor = new DetectMaxCharacterCountProcessor(
                "Envelope/Body/BMessage/myHoniggut", 8 * 1000L, counted -> {
                    throw new StaxParserParsingException("too large: " + counted);
                });

        soapParser.addProcessor(characterCountProcessor);

        // begin the parsing

        try (var inputStream = requestInputStream) {
            soapParser.parse(inputStream);
        } catch (StaxParserParsingException e) {
            logger.debug("Error during business parsing!", e);
            result.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.debug("Generic Error during business parsing!", e);
            result.setErrorMessage(e.getMessage());
        }

        logger.info("container count: {}", result.extractedDataSizes.size());
        logger.info("container sizes each: {}", result.extractedDataSizes);
        logger.info("character count root for <myHoniggut>: {}",
                characterCountProcessor.getCountedCharaters());

        logger.info("finished stax parsing controller, time (ms): {}",
                TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis() - now));

        return result;
    }

    private static List<Integer> streamHoniggutInternal(InputStream honiggutInputStream)
            throws StaxParserParsingException {
        StaxParser embeddedStaxParser = new StaxParser("honig");

        final List<Integer> byteSizesResults = new ArrayList<Integer>();
        embeddedStaxParser.addProcessor(new Base64ExtractProcessor("honig/gut", bytes -> {

            byteSizesResults.add(bytes.length);
        }));

        // parse
        try {
            embeddedStaxParser.parse(honiggutInputStream);
            return byteSizesResults;
        } catch (Exception ex) {
            throw new StaxParserParsingException(ex.getMessage(), ex);
        }

    }

    private static List<Integer> streamBInternal(InputStream messageInputStream)
            throws StaxParserParsingException {

        StaxParser embeddedStaxParser = new StaxParser("Message");

        final List<Integer> byteSizesResults = new ArrayList<Integer>();
        embeddedStaxParser.addProcessor(
                new Base64ExtractProcessor("Message/MyContent/MyContainer/Data", bytes -> {
                    if (bytes.length == 0) {
                        throw new StaxParserParsingException(
                                "Zero length byte array not allowed in business.");
                    }
                    byteSizesResults.add(bytes.length);
                }));

        // parse
        try {
            embeddedStaxParser.parse(messageInputStream);
            return byteSizesResults;
        } catch (Exception ex) {
            throw new StaxParserParsingException(ex.getMessage(), ex);
        }
    }

    public static class Result {

        private boolean isHoniggutXml;
        private boolean isBXml;
        private List<Integer> extractedDataSizes = new ArrayList<>();
        private String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isHoniggutXml() {
            return isHoniggutXml;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public boolean isBXml() {
            return isBXml;
        }

        public List<Integer> getExtractedDataSizes() {
            return extractedDataSizes;
        }

        public void setExtractedDataSizes(List<Integer> extractedDataSizes) {
            this.extractedDataSizes = extractedDataSizes;
        }

        public void setHoniggutXml(boolean isHoniggutXml) {
            this.isHoniggutXml = isHoniggutXml;
        }

        public void setBXml(boolean isBXml) {
            this.isBXml = isBXml;
        }
    }
}
