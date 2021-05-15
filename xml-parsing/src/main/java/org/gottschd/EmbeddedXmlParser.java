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

    private PipedOutputStream pop;
    private PipedInputStream pip;
    private OutputStreamWriter dataStream;
    private Thread ownerThread;
    
    /**
     * 
     * @throws Exception
     */
    public EmbeddedXmlParser() throws Exception {
        
    }

    /**
     * 
     */
    public final void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
        logger.info("feed run in EmbeddedXmlParser");
        dataStream.write(textCharacters, textStart, textLength);
        dataStream.flush();
    }

    /**
     * 
     */
    public final void close() throws Exception {
        dataStream.flush();
        dataStream.close();

        ownerThread.join();
    }

    /**
     * @throws Exception
     * 
     */
    public EmbeddedXmlParser open() throws Exception {
        pop = new PipedOutputStream();
        pip = new PipedInputStream(pop);
        dataStream = new OutputStreamWriter(pop);
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
