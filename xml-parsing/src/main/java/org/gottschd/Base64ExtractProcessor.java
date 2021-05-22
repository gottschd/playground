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
        private final ByteArrayOutputStream result;
        private final OutputStreamWriter sink;

        private Base64Extractor() {

            result = new ByteArrayOutputStream();
            sink = new OutputStreamWriter(new Base64OutputStream(result, false));
        }

        public byte[] getBytes() throws Exception {
            sink.flush();
            sink.close();
            return result.toByteArray();
        }

        public void feed(char[] textCharacters, int textStart, int textLength) throws Exception {
            sink.write(textCharacters, textStart, textLength);
        }
    }
}
