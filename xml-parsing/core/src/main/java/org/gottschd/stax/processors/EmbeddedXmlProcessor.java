package org.gottschd.stax.processors;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParser;

/**
 * Embedded xml must be processed in another thread
 */
public class EmbeddedXmlProcessor implements EventTypeProcessor {

    private PipeToParser embeddedParser;
    private final String localName;
    private final StaxParser parserDelegate;

    public EmbeddedXmlProcessor(String localName, StaxParser parser) {
        this.localName = localName;
        this.parserDelegate = parser;
    }

    /**
     * 
     * @param xmlr
     * @throws Exception
     */
    @Override
    public void processEvent(XMLStreamReader xmlr) throws Exception {
        if (xmlr.isStartElement() && localName.equals(xmlr.getLocalName())) {
            embeddedParser = new PipeToParser(parserDelegate).start();
            return;
        }

        if (xmlr.isEndElement() && localName.equals(xmlr.getLocalName())) {
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

        private final PipedOutputStream pop;
        private final PipedInputStream pip;
        private final OutputStreamWriter dataStream;

        private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "EmbeddedParserThread");
            }

        });
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
            dataStream.flush();
            dataStream.close();
            parsingFuture.get();
        }

        void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
            dataStream.write(textCharacters, textStart, textLength);
            dataStream.flush();
        }

        @Override
        public Void call() throws Exception {
            embeddedXmlParser.parse(pip);
            return null;
        }
    }
}