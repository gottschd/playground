package org.gottschd.stax;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class StaxParser {

    private static final Logger logger = LoggerFactory.getLogger(StaxParser.class);

    private final Deque<String> breadCrumb = new LinkedList<>();

    private final String name;

    private final List<EventTypeProcessor> processors = new ArrayList<>();

    /**
     * 
     */
    public StaxParser(String name) {
        this.name = "-" + name + "-";
    }

    /**
     *  
     */
    public void addProcessor(EventTypeProcessor processor) {
        this.processors.add(processor);
    }

    /**
     * 
     * @param xmlr
     * @throws Exception
     */
    protected void processEvent(XMLStreamReader xmlr) throws Exception {
        for (EventTypeProcessor p : processors) {
            p.processEvent(xmlr);
        }
    }

    /**
     * 
     * @param in
     * @return
     * @throws Exception
     */
    public final void parse(InputStream in) throws Exception {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        // https://bugs.openjdk.java.net/browse/JDK-8175792
        xmlInputFactory.setProperty("jdk.xml.cdataChunkSize", 8 * 1024);

        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(in);

        while (reader.hasNext()) {

            updateBreadCrumb(reader);

            if( logger.isDebugEnabled() ) {
                logger.debug("{} Current breadcrumb position: {}", name, breadCrumb);

                String eventPrint = printEventToString(reader, name);
                logger.debug( eventPrint );
            }

            processEvent(reader);
            reader.next();
        }
        reader.close();
    }

    private void updateBreadCrumb(XMLStreamReader xmlr) {

        switch (xmlr.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                breadCrumb.push(xmlr.getLocalName());
                break;
            case XMLStreamConstants.END_ELEMENT:
                breadCrumb.pop();
                break;
            default:
                break;
        }
    }

    /**
     * 
     * @param xmlr
     * @param debugPrefix
     */
    protected String printEventToString(XMLStreamReader xmlr, String debugPrefix) {

        StringBuilder result = new StringBuilder();

        result.append(debugPrefix + "EVENT(" + xmlr.getEventType() + "):[" + xmlr.getLocation().getLineNumber() + "]["
                + xmlr.getLocation().getColumnNumber() + "] ");

        result.append(" [");

        switch (xmlr.getEventType()) {
            case XMLStreamConstants.END_DOCUMENT:
                result.append("<");
                printName(result, xmlr);
                printNamespaces(result, xmlr);
                printAttributes(result, xmlr);
                result.append(">");
                break;
            case XMLStreamConstants.START_ELEMENT:
                result.append("<");
                printName(result, xmlr);
                printNamespaces(result, xmlr);
                printAttributes(result, xmlr);
                result.append(">");
                break;
            case XMLStreamConstants.END_ELEMENT:
                result.append("</");
                printName(result, xmlr);
                result.append(">");
                break;
            case XMLStreamConstants.SPACE:
            case XMLStreamConstants.CHARACTERS:
                // int start = xmlr.getTextStart();
                int length = xmlr.getTextLength();
                result.append(
                        "CHARACTERS(textlength:" + length + "): bufferLength: " + xmlr.getTextCharacters().length);
                result.append(" ... ");
                // result.append(new String(xmlr.getTextCharacters(), start, length).trim());
                break;
            // case XMLStreamConstants.PROCESSING_INSTRUCTION:
            // result.append("<?");
            // if (xmlr.hasText())
            // result.append(xmlr.getText());
            // result.append("?>");
            // break;
            case XMLStreamConstants.CDATA:
                result.append("<![CDATA[");
                // start = xmlr.getTextStart();
                length = xmlr.getTextLength();
                // result.append("new String(xmlr.getTextCharacters(), start, length).trim()");
                result.append("(textlength:" + length + "): bufferLength: " + xmlr.getTextCharacters().length);
                result.append("]]>");
                break;
            // case XMLStreamConstants.COMMENT:
            // result.append("<!--");
            // if (xmlr.hasText())
            // result.append(xmlr.getText());
            // result.append("-->");
            // break;
            // case XMLStreamConstants.ENTITY_REFERENCE:
            // result.append(xmlr.getLocalName() + "=");
            // if (xmlr.hasText())
            // result.append("[" + xmlr.getText() + "]");
            // break;
            case XMLStreamConstants.START_DOCUMENT:
                result.append("<?xml");
                result.append(" version='" + xmlr.getVersion() + "'");
                result.append(" encoding='" + xmlr.getCharacterEncodingScheme() + "'");
                if (xmlr.isStandalone())
                    result.append(" standalone='yes'");
                else
                    result.append(" standalone='no'");
                result.append("?>");
                break;

        }
        result.append("]");

        return result.toString();
    }

    private static void printName(StringBuilder eventAsString, XMLStreamReader xmlr) {
        if (xmlr.hasName()) {
            String prefix = xmlr.getPrefix();
            String uri = xmlr.getNamespaceURI();
            String localName = xmlr.getLocalName();
            printName(eventAsString, prefix, uri, localName);
        }
    }

    private static void printName(StringBuilder eventAsString, String prefix, String uri, String localName) {
        if (uri != null && !("".equals(uri)))
            eventAsString.append("['" + uri + "']:");
        if (prefix != null)
            eventAsString.append(prefix + ":");
        if (localName != null)
            eventAsString.append(localName);
    }

    private static void printAttributes(StringBuilder eventAsString, XMLStreamReader xmlr) {
        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
            printAttribute(eventAsString, xmlr, i);
        }
    }

    private static void printAttribute(StringBuilder eventAsString, XMLStreamReader xmlr, int index) {
        String prefix = xmlr.getAttributePrefix(index);
        String namespace = xmlr.getAttributeNamespace(index);
        String localName = xmlr.getAttributeLocalName(index);
        String value = xmlr.getAttributeValue(index);
        eventAsString.append(" ");
        printName(eventAsString, prefix, namespace, localName);
        eventAsString.append("='" + value + "'");
    }

    private static void printNamespaces(StringBuilder eventAsString, XMLStreamReader xmlr) {
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
            printNamespace(eventAsString, xmlr, i);
        }
    }

    private static void printNamespace(StringBuilder eventAsString, XMLStreamReader xmlr, int index) {
        String prefix = xmlr.getNamespacePrefix(index);
        String uri = xmlr.getNamespaceURI(index);
        eventAsString.append(" ");
        if (prefix == null)
            eventAsString.append("xmlns='" + uri + "'");
        else
            eventAsString.append("xmlns:" + prefix + "='" + uri + "'");
    }

    /**
     * 
     */ 
    public String getName() {
        return name;
    }

}
