package org.gottschd;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EmbeddedXmlParser extends AbstractXmlParser implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedXmlParser.class);

    private final PipedOutputStream pop;
    private final PipedInputStream pip;
    private final OutputStreamWriter dataStream;
    private Thread embeddedParsingThread;

    private final XmlParsingResult result = new XmlParsingResult();

    /**
     * @throws Exception
     */
    public EmbeddedXmlParser() throws Exception {
        pop = new PipedOutputStream();
        pip = new PipedInputStream(pop);
        dataStream = new OutputStreamWriter(pop);
    }

    public XmlParsingResult getResult() {
        return result;
    }

    /**
     *
     */
    public final void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
        dataStream.write(textCharacters, textStart, textLength);
    }

    /**
     * @throws Exception
     */
    public EmbeddedXmlParser start() throws Exception {
        embeddedParsingThread = new Thread(this);
        embeddedParsingThread.start();
        return this;
    }

    /**
     *
     */
    public final void stop() throws Exception {
        dataStream.flush();
        dataStream.close();

        embeddedParsingThread.join();
    }

    private CopyToWriterProcessor copyToWriterProcessor;
    private Base64ExtractProcessor base64ExtractProcessor;

    @Override
    public void run() {
        try {
            copyToWriterProcessor = new CopyToWriterProcessor();
            base64ExtractProcessor = new Base64ExtractProcessor() {
                @Override
                public void onExtractFinished(byte[] bytes) {
                    result.getDataOfMyContainer().add(bytes);
                }
            };

            // parse
            parse(pip);

            result.setRemainingXml(copyToWriterProcessor.getWriterResult());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void processEvent(XMLStreamReader xmlr) throws Exception {
        copyToWriterProcessor.processEvent(xmlr);
        base64ExtractProcessor.processEvent(xmlr);
    }
}
