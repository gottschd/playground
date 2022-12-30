package org.gottschd.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface EventTypeProcessor {

	void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException;

}
