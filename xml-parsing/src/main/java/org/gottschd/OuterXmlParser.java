package org.gottschd;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;


public class OuterXmlParser extends AbstractXmlParser {

    private EmbeddedXmlParser embeddedParser;
    private XmlParsingResult result;

    @Override
    protected void processEvent(XMLStreamReader xmlr) throws Exception {
        // printEvent(xmlr, "Outer-");

        switch (xmlr.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("B".equals(xmlr.getLocalName())) {
                    embeddedParser = new EmbeddedXmlParser().start();
                }
                break;

            case XMLStreamConstants.END_ELEMENT:
                if ("B".equals(xmlr.getLocalName())) {
                    embeddedParser.stop();
                    result = embeddedParser.getResult();
                    embeddedParser = null;
                }
                break;

            case XMLStreamConstants.SPACE:
            case XMLStreamConstants.CHARACTERS:
                if (embeddedParser != null) {
                    embeddedParser.feed(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
                }
                break;

            case XMLStreamConstants.CDATA:
                throw new IllegalStateException("CDATA event captured, what now?");

            default:
                break;
        }
    }

    public XmlParsingResult getXmlParsingResult() {
        return result;
    }
}
