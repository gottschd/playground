package org.gottschd.stax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StaxParser {

	public static final int BUFFER_SIZE_FOR_ALL_PLACES = 8 * 1024; // 8 KiB

	private static final Logger logger = LoggerFactory
			.getLogger(StaxParser.class);

	private final List<EventTypeProcessor> processors = new ArrayList<>();

	private XMLStreamReader reader;

	private final StaxParseContext context;

	public StaxParser(String name) {
		this.context = new StaxParseContext(name);
	}

	public void addProcessor(EventTypeProcessor processor) {
		this.processors.add(processor);
	}

	protected void processEvent(XMLStreamReader xmlr)
			throws StaxParserParsingException, XMLStreamException {
		for (EventTypeProcessor p : processors) {
			p.processEvent(xmlr, context);
		}
	}

	public final void parse(InputStream in)
			throws StaxParserParsingException, XMLStreamException {
		setup(in);
		pullWhileHasNext();
		close();
	}

	public final void setup(InputStream in) throws XMLStreamException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

		// https://bugs.openjdk.java.net/browse/JDK-8175792
		xmlInputFactory.setProperty("jdk.xml.cdataChunkSize",
				BUFFER_SIZE_FOR_ALL_PLACES);

		xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		xmlInputFactory.setProperty(
				XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		reader = xmlInputFactory.createXMLStreamReader(in);
	}

	public final void pullWhileHasNext()
			throws StaxParserParsingException, XMLStreamException {
		while (reader.hasNext()) {
			int curEvent = reader.next();

			if (reader.isStartElement()) {
				context.pushElement(reader.getLocalName());
			}

			if (logger.isDebugEnabled()) {
				String eventAsString = printEventToString(curEvent, reader,
						context.getName());
				logger.debug("-{}- path: {}, event: {}", context.getName(),
						context.currentPath(), eventAsString);
			}

			processEvent(reader);

			if (reader.isEndElement()) {
				context.popElement();
			}
		}
	}

	public final void close() throws XMLStreamException {
		reader.close();
	}

	private static String printEventToString(int event, XMLStreamReader xmlr,
			String debugPrefix) {

		// Current EVENT(CHARACTERS):[LN1][CN41] [CHARACTERS(textlength:8):
		// bufferLength:
		// 32: 'erXML><D']
		StringBuilder result = new StringBuilder();

		result.append(debugPrefix).append(" EVENT(")
				.append(eventTypeToString(xmlr.getEventType())).append("):[LN")
				.append(xmlr.getLocation().getLineNumber()).append("][CN")
				.append(xmlr.getLocation().getColumnNumber()).append("] ");

		result.append(" [");

		switch (event) {
		case XMLStreamConstants.END_DOCUMENT:
			result.append("<");
			printName(result, xmlr);
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
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			result.append("<?");
			if (xmlr.hasText()) {
				result.append(xmlr.getText());
			}
			result.append("?>");
			break;
		case XMLStreamConstants.SPACE:
		case XMLStreamConstants.CHARACTERS:
			int start = xmlr.getTextStart();
			int length = xmlr.getTextLength();
			result.append("(textlength:").append(length)
					.append("): bufferLength: ")
					.append(xmlr.getTextCharacters().length);
			result.append(": '");
			if (length > 200) {
				result.append(
						new String(xmlr.getTextCharacters(), start, start + 200)
								.trim());
				result.append(" <...>");
			} else {
				result.append(
						new String(xmlr.getTextCharacters(), start, length)
								.trim());
			}
			result.append("'");
			break;
		case XMLStreamConstants.CDATA:
			start = xmlr.getTextStart();
			length = xmlr.getTextLength();
			result.append("(textlength:").append(length)
					.append("): bufferLength: ")
					.append(xmlr.getTextCharacters().length);
			result.append(": '");
			result.append(
					new String(xmlr.getTextCharacters(), start, length).trim());
			result.append("'");
			break;
		case XMLStreamConstants.COMMENT:
			result.append("<!--");
			if (xmlr.hasText()) {
				result.append(xmlr.getText());
			}
			result.append("-->");
			break;
		case XMLStreamConstants.ENTITY_REFERENCE:
			result.append(xmlr.getLocalName()).append("=");
			if (xmlr.hasText()) {
				result.append("[").append(xmlr.getText()).append("]");
			}
			break;
		case XMLStreamConstants.START_DOCUMENT:
			result.append("version='").append(xmlr.getVersion()).append("'");
			result.append(" encoding='")
					.append(xmlr.getCharacterEncodingScheme()).append("'");
			if (xmlr.isStandalone()) {
				result.append(" standalone='yes'");
			} else {
				result.append(" standalone='no'");
			}
			break;

		}
		result.append("]");

		return result.toString();
	}

	private static String eventTypeToString(int eventType) {
		switch (eventType) {
		case XMLStreamConstants.START_ELEMENT:
			return "START_ELEMENT";
		case XMLStreamConstants.END_ELEMENT:
			return "END_ELEMENT";
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			return "PROCESSING_INSTRUCTION";
		case XMLStreamConstants.CHARACTERS:
			return "CHARACTERS";
		case XMLStreamConstants.COMMENT:
			return "COMMENT";
		case XMLStreamConstants.START_DOCUMENT:
			return "START_DOCUMENT";
		case XMLStreamConstants.END_DOCUMENT:
			return "END_DOCUMENT";
		case XMLStreamConstants.ENTITY_REFERENCE:
			return "ENTITY_REFERENCE";
		case XMLStreamConstants.ATTRIBUTE:
			return "ATTRIBUTE";
		case XMLStreamConstants.DTD:
			return "DTD";
		case XMLStreamConstants.CDATA:
			return "CDATA";
		case XMLStreamConstants.SPACE:
			return "SPACE";
		}
		return "UNKNOWN_EVENT_TYPE(" + eventType + ")";
	}

	private static void printName(StringBuilder eventAsString,
			XMLStreamReader xmlr) {
		if (xmlr.hasName()) {
			String prefix = xmlr.getPrefix();
			String uri = xmlr.getNamespaceURI();
			String localName = xmlr.getLocalName();
			printName(eventAsString, prefix, uri, localName);
		}
	}

	private static void printName(StringBuilder eventAsString, String prefix,
			String uri, String localName) {
		if (uri != null && !("".equals(uri))) {
			eventAsString.append("['").append(uri).append("']:");
		}
		if (prefix != null) {
			eventAsString.append(prefix).append(":");
		}
		if (localName != null) {
			eventAsString.append(localName);
		}
	}

	private static void printAttributes(StringBuilder eventAsString,
			XMLStreamReader xmlr) {
		for (int i = 0; i < xmlr.getAttributeCount(); i++) {
			printAttribute(eventAsString, xmlr, i);
		}
	}

	private static void printAttribute(StringBuilder eventAsString,
			XMLStreamReader xmlr, int index) {
		String prefix = xmlr.getAttributePrefix(index);
		String namespace = xmlr.getAttributeNamespace(index);
		String localName = xmlr.getAttributeLocalName(index);
		String value = xmlr.getAttributeValue(index);
		eventAsString.append(" ");
		printName(eventAsString, prefix, namespace, localName);
		eventAsString.append("='").append(value).append("'");
	}

	private static void printNamespaces(StringBuilder eventAsString,
			XMLStreamReader xmlr) {
		for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
			printNamespace(eventAsString, xmlr, i);
		}
	}

	private static void printNamespace(StringBuilder eventAsString,
			XMLStreamReader xmlr, int index) {
		String prefix = xmlr.getNamespacePrefix(index);
		String uri = xmlr.getNamespaceURI(index);
		eventAsString.append(" ");
		if (prefix == null) {
			eventAsString.append("xmlns='").append(uri).append("'");
		} else {
			eventAsString.append("xmlns:").append(prefix).append("='")
					.append(uri).append("'");
		}
	}

	public String getName() {
		return context.getName();
	}

}
