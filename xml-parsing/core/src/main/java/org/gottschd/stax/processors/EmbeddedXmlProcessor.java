package org.gottschd.stax.processors;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded xml must be processed in another thread
 */
public class EmbeddedXmlProcessor implements EventTypeProcessor {

    private PipeToParser embeddedParser;
    private final String tagNameContainingEmbeddedXml;
    private final StaxParser parserDelegate;

    public EmbeddedXmlProcessor(String localName, StaxParser parser) {
        this.tagNameContainingEmbeddedXml = localName;
        this.parserDelegate = parser;
    }

    /**
     * 
     * @param xmlr
     * @throws Exception
     */
    @Override
    public void processEvent(XMLStreamReader xmlr) throws Exception {
        if (xmlr.isStartElement() && tagNameContainingEmbeddedXml.equals(xmlr.getLocalName())) {
            embeddedParser = new PipeToParser(parserDelegate).start();
            return;
        }

        if (xmlr.isEndElement() && tagNameContainingEmbeddedXml.equals(xmlr.getLocalName())) {
            embeddedParser.stop();
            embeddedParser = null;
            return;
        }

        if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
            if (embeddedParser != null) {
                embeddedParser.feed(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
            }
        }
    }

    /**
     * 
     */
    private static class PipeToParser implements Callable<Void> {
        private static final Logger logger = LoggerFactory.getLogger(PipeToParser.class);

        private static final ExecutorService executor = Executors.newCachedThreadPool();

        private final PipedOutputStream pop;
        private final PipedInputStream pip;
        private final OutputStreamWriter dataStream;

        private Future<Void> parsingFuture;

        private final StaxParser embeddedXmlParser;

        PipeToParser(StaxParser parser) throws Exception {
            pop = new PipedOutputStream();
            pip = new PipedInputStream(pop, 8 * 1024);
            dataStream = new OutputStreamWriter(pop);
            embeddedXmlParser = parser;
        }

        PipeToParser start() {
            parsingFuture = executor.submit(this);
            return this;
        }

        void stop() throws Exception {
            dataStream.close();
            parsingFuture.get();
        }

        void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
            dataStream.write(textCharacters, textStart, textLength);
        }

        @Override
        public Void call() throws Exception {
            try (InputStream in = pip) {
                embeddedXmlParser.parse(in);
            } catch (Throwable t) {
                // due to the nature of the async parsing/feeding some errors
                // occurs lazy during the parsing, thus log any error but throw the exception
                // further up.
                logger.error("Error during parsing in another thread for parser '" + embeddedXmlParser.getName() + "'.",
                        t);
                throw t;
            }
            return null;
        }
    }
}