package org.gottschd.stax.processors;

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.utils.CheckedConsumer;

/**
 *
 */
public class CopyProcessor implements EventTypeProcessor {

	private final StaxParserPath beginCopyFrom;

	private final StaxParserPath stopOn;

	private final CheckedConsumer<InputStream> onFinished;

	private CopyToWriterProcessor copyProcessor;

	public CopyProcessor(String beginCopyFrom, String stopOn,
			CheckedConsumer<InputStream> onFinished) {
		this.beginCopyFrom = StaxParserPath.fromString(beginCopyFrom);
		this.stopOn = StaxParserPath.fromString(stopOn);
		this.onFinished = onFinished;
	}

	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException {

		if (xmlr.isStartElement() && context.isCurrentPath(beginCopyFrom)) {
			copyProcessor = new CopyToWriterProcessor();
			copyProcessor.startWriter();
		}

		if (copyProcessor != null) {
			copyProcessor.processEvent(xmlr, context);
		}

		if (xmlr.isEndElement() && context.isCurrentPath(stopOn)
				&& copyProcessor != null) {
			copyProcessor.endWriter();
			onFinished.apply(copyProcessor.getResultAsStream());
			copyProcessor = null;
		}
	}

}
