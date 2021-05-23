package org.gottschd;

import javax.xml.stream.XMLStreamReader;

public class OuterXmlParser extends AbstractXmlParser {

    OuterXmlParser() {
        super("Outer");
    }

    private EmbeddedXmlParser embeddedParser;
    private XmlParsingResult result;

    @Override
    protected void processEvent(XMLStreamReader xmlr) throws Exception {
        if (xmlr.isStartElement() && "B".equals(xmlr.getLocalName())) {
            embeddedParser = new EmbeddedXmlParser().start();
            return;
        }

        if (xmlr.isEndElement() && "B".equals(xmlr.getLocalName())) {
            embeddedParser.stop();
            result = embeddedParser.getResult();
            embeddedParser = null;
            return;
        }

        if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
            if (embeddedParser != null) {
                embeddedParser.feed(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
            }
        }
    }

    public XmlParsingResult getXmlParsingResult() {
        return result;
    }
}
