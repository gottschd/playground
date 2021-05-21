package org.gottschd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64OutputStream;

public abstract class Base64ExtractProcessor {

    private Base64Extractor currentBase64Extractor;

    public void processEvent(XMLStreamReader xmlr) throws Exception {

        if( xmlr.isStartElement() && "Data".equals(xmlr.getLocalName()) ) {
            currentBase64Extractor = new Base64Extractor();
            return;
        }

        if( xmlr.isEndElement() && "Data".equals(xmlr.getLocalName()) ) {
            onExtractFinished(currentBase64Extractor.getBytes());
            currentBase64Extractor = null;
            return;
        }

        if( xmlr.isCharacters() || xmlr.isWhiteSpace() ) {
            if (currentBase64Extractor != null) {
                currentBase64Extractor.feed(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
            }
        }
    }

    public abstract void onExtractFinished(byte[] bytes);

    private static class Base64Extractor {
        private final ByteArrayOutputStream buf;
        private final OutputStreamWriter dos;

        private Base64Extractor() {

            buf = new ByteArrayOutputStream();
            dos = new OutputStreamWriter(new Base64OutputStream(buf, false));
        }

        public byte[] getBytes() throws Exception {
            dos.flush();
            dos.close();
            return buf.toByteArray();
        }

        public void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
            dos.write(textCharacters, textStart, textLength);
        }

    }
}
