package org.gottschd.stax.processors;

import java.util.Objects;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;

public class DetectXmlTag implements EventTypeProcessor {

	private final Consumer<Integer> onTagFound;

	private final StaxParserPath localNamePath;

	public DetectXmlTag(String localNamePath, Consumer<Integer> onTagFound) {
		this.localNamePath = StaxParserPath.fromString(localNamePath);
		this.onTagFound = Objects.requireNonNull(onTagFound);
	}

	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException {
		if (xmlr.isStartElement() && context.isCurrentPath(localNamePath)) {
			onTagFound.accept(xmlr.getEventType());
		}

		if (xmlr.isEndElement() && context.isCurrentPath(localNamePath)) {
			onTagFound.accept(xmlr.getEventType());
		}
	}

}
