package org.gottschd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64OutputStream;

public abstract class Base64ExtractProcessor {

    private DataConverter currentBase64Extractor;

    public void processEvent(XMLStreamReader xmlr) throws Exception {

        switch (xmlr.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("Data".equals(xmlr.getLocalName())) {
                    currentBase64Extractor = new DataConverter();
                }
                break;

            case XMLStreamConstants.END_ELEMENT:
                if ("Data".equals(xmlr.getLocalName())) {
                    onExtractFinished(currentBase64Extractor.getBytes());
                    currentBase64Extractor = null;
                }
                break;

            case XMLStreamConstants.SPACE:
            case XMLStreamConstants.CHARACTERS:
                if (currentBase64Extractor != null) {
                    currentBase64Extractor.feed(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
                }
                break;
            default:
                break;
        }
    }

    public abstract void onExtractFinished(byte[] bytes);

    private static class DataConverter {
        private final ByteArrayOutputStream buf;
        private final OutputStreamWriter dos;

        private DataConverter() {

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
