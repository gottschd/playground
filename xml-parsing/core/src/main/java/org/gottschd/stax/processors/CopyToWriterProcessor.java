package org.gottschd.stax.processors;

import java.io.InputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.utils.ByteArrayOutputInputStream;

/**
 *
 */
class CopyToWriterProcessor implements EventTypeProcessor {

	private final boolean ignoreNamespaces;

	private XMLStreamWriter writer;

	private final ByteArrayOutputInputStream sink = new ByteArrayOutputInputStream();

	private boolean isActive = false;

	CopyToWriterProcessor() {
		this(false);
	}

	CopyToWriterProcessor(boolean ignoreNamespaces) {
		this.ignoreNamespaces = ignoreNamespaces;
	}

	void startWriter() throws XMLStreamException {
		isActive = true;

		// create writer in thread from which the parsing happens
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		writer = outputFactory.createXMLStreamWriter(sink);
	}

	void setActive(boolean active) {
		isActive = active;
	}

	void endWriter() throws XMLStreamException {
		isActive = false;
		writer.writeEndDocument();
	}

	InputStream getResultAsStream() {
		return sink.wrapToInputStream();
	}

	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException {
		if (isActive) {
			write(xmlr, writer);
		}
	}

	/**
	 * @param xmlReader
	 * @param writer
	 * @throws Exception
	 */
	private void write(XMLStreamReader xmlReader, XMLStreamWriter writer)
			throws XMLStreamException {
		switch (xmlReader.getEventType()) {
		case XMLStreamConstants.START_ELEMENT:
			final String localName = xmlReader.getLocalName();
			final String namespaceURI = xmlReader.getNamespaceURI();
			if (!ignoreNamespaces && namespaceURI != null
					&& namespaceURI.length() > 0) {
				final String prefix = xmlReader.getPrefix();
				if (prefix != null) {
					writer.writeStartElement(prefix, localName, namespaceURI);
				} else {
					writer.writeStartElement(namespaceURI, localName);
				}
			} else {
				writer.writeStartElement(localName);
			}

			if (!ignoreNamespaces) {
				for (int i = 0,
						len = xmlReader.getNamespaceCount(); i < len; i++) {
					writer.writeNamespace(xmlReader.getNamespacePrefix(i),
							xmlReader.getNamespaceURI(i));
				}
			}

			for (int i = 0, len = xmlReader.getAttributeCount(); i < len; i++) {
				String attUri = xmlReader.getAttributeNamespace(i);
				if (!ignoreNamespaces && attUri != null) {
					writer.writeAttribute(attUri,
							xmlReader.getAttributeLocalName(i),
							xmlReader.getAttributeValue(i));
				} else {
					writer.writeAttribute(xmlReader.getAttributeLocalName(i),
							xmlReader.getAttributeValue(i));
				}
			}
			break;
		case XMLStreamConstants.END_ELEMENT:
			writer.writeEndElement();
			break;
		case XMLStreamConstants.SPACE:
		case XMLStreamConstants.CHARACTERS:
			writer.writeCharacters(xmlReader.getTextCharacters(),
					xmlReader.getTextStart(), xmlReader.getTextLength());
			break;
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			writer.writeProcessingInstruction(xmlReader.getPITarget(),
					xmlReader.getPIData());
			break;
		case XMLStreamConstants.CDATA:
			writer.writeCData(xmlReader.getText());
			break;

		case XMLStreamConstants.COMMENT:
			writer.writeComment(xmlReader.getText());
			break;
		case XMLStreamConstants.ENTITY_REFERENCE:
			writer.writeEntityRef(xmlReader.getLocalName());
			break;
		case XMLStreamConstants.START_DOCUMENT:
			String encoding = xmlReader.getCharacterEncodingScheme();
			String version = xmlReader.getVersion();

			if (encoding != null && version != null) {
				writer.writeStartDocument(encoding, version);
			} else if (version != null) {
				writer.writeStartDocument(xmlReader.getVersion());
			}
			break;
		case XMLStreamConstants.END_DOCUMENT:
			writer.writeEndDocument();
			break;
		case XMLStreamConstants.DTD:
			writer.writeDTD(xmlReader.getText());
			break;
		default:
			break;
		}
	}

}
