package org.gottschd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 *
 */
public class EmbeddedXmlParser extends AbstractXmlParser implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedXmlParser.class);

    private final PipedOutputStream pop;
    private final PipedInputStream pip;
    private final OutputStreamWriter dataStream;
    private Thread ownerThread;

    /**
     * @throws Exception
     */
    public EmbeddedXmlParser() throws Exception {
        pop = new PipedOutputStream();
        pip = new PipedInputStream(pop);
        dataStream = new OutputStreamWriter(pop);
    }

    /**
     *
     */
    public final void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
        logger.info("feed run in EmbeddedXmlParser");
        dataStream.write(textCharacters, textStart, textLength);
    }

    /**
     *
     */
    public final void stop() throws Exception {
        dataStream.flush();
        dataStream.close();

        ownerThread.join();
    }

    /**
     * @throws Exception
     */
    public EmbeddedXmlParser start() throws Exception {
        ownerThread = new Thread(this);
        ownerThread.start();
        return this;
    }

    @Override
    public void run() {
        logger.info("begin run in EmbeddedXmlParser");
        try {
            parse(pip);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info("exiting run in EmbeddedXmlParser");
    }

    @Override
    protected void processEvent(XMLStreamReader xmlr) {
        logger.info("processEvent in EmbeddedXmlParser");
        printEvent(xmlr, "Embedded");
    }

}
