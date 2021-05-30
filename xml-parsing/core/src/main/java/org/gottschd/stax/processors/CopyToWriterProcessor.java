package org.gottschd.stax.processors;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.gottschd.stax.EventTypeProcessor;

/**
 * 
 */
public class CopyToWriterProcessor implements EventTypeProcessor {
    private final XMLStreamWriter writer;
    private final StringWriter stringOut = new StringWriter();
    private boolean disableWriter;
    private String result;

    public CopyToWriterProcessor() throws Exception {
        // create writer in thread from which the parsing happens
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        writer = outputFactory.createXMLStreamWriter(stringOut);
    }

    /**
     * 
     */
    public String getWriterResult() throws Exception {
        if (result == null) {
            writer.flush();
            result = stringOut.toString();
        }

        return result;
    }

    @Override
    public void processEvent(XMLStreamReader xmlr) throws Exception {

        // disable the writer as soon as the element begins
        if (xmlr.isStartElement() && "MyContainer".equals(xmlr.getLocalName())) {
            disableWriter = true;
            return;
        }

        // enable the writer again _after_ hitting the end of element
        if (xmlr.isEndElement() && "MyContainer".equals(xmlr.getLocalName())) {
            disableWriter = false;
            return; // we do not want to write the current position as it is still pointing to the
                    // end element we do not want
        }

        // only write allowed sections
        if (!disableWriter) {
            write(xmlr, writer);
        }
    }

    /**
     * 
     * @param xmlr
     * @param writer
     * @throws Exception
     */
    private static void write(XMLStreamReader xmlr, XMLStreamWriter writer) throws Exception {
        switch (xmlr.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                final String localName = xmlr.getLocalName();
                final String namespaceURI = xmlr.getNamespaceURI();
                if (namespaceURI != null && namespaceURI.length() > 0) {
                    final String prefix = xmlr.getPrefix();
                    if (prefix != null)
                        writer.writeStartElement(prefix, localName, namespaceURI);
                    else
                        writer.writeStartElement(namespaceURI, localName);
                } else {
                    writer.writeStartElement(localName);
                }

                for (int i = 0, len = xmlr.getNamespaceCount(); i < len; i++) {
                    writer.writeNamespace(xmlr.getNamespacePrefix(i), xmlr.getNamespaceURI(i));
                }

                for (int i = 0, len = xmlr.getAttributeCount(); i < len; i++) {
                    String attUri = xmlr.getAttributeNamespace(i);
                    if (attUri != null)
                        writer.writeAttribute(attUri, xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                    else
                        writer.writeAttribute(xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                writer.writeEndElement();
                break;
            case XMLStreamConstants.SPACE:
            case XMLStreamConstants.CHARACTERS:
                writer.writeCharacters(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                writer.writeProcessingInstruction(xmlr.getPITarget(), xmlr.getPIData());
                break;
            case XMLStreamConstants.CDATA:
                writer.writeCData(xmlr.getText());
                break;

            case XMLStreamConstants.COMMENT:
                writer.writeComment(xmlr.getText());
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                writer.writeEntityRef(xmlr.getLocalName());
                break;
            case XMLStreamConstants.START_DOCUMENT:
                String encoding = xmlr.getCharacterEncodingScheme();
                String version = xmlr.getVersion();

                if (encoding != null && version != null)
                    writer.writeStartDocument(encoding, version);
                else if (version != null)
                    writer.writeStartDocument(xmlr.getVersion());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                writer.writeEndDocument();
                break;
            case XMLStreamConstants.DTD:
                writer.writeDTD(xmlr.getText());
                break;
            default:
                break;
        }
    }
}