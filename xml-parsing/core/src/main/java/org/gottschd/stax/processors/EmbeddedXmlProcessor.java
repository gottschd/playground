package org.gottschd.stax.processors;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.utils.CheckedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (Embedded) xml that must be processed in another parser
 */
public class EmbeddedXmlProcessor implements EventTypeProcessor {

	private static Logger log = LoggerFactory
			.getLogger(EmbeddedXmlProcessor.class);

	private ThreadedPipe pipe;

	private final StaxParserPath localTagPath;

	private CheckedConsumer<InputStream> onEmbeddedXmlFound;

	public EmbeddedXmlProcessor(String localTagPath,
			CheckedConsumer<InputStream> onEmbeddedXmlFound) {
		this.localTagPath = StaxParserPath.fromString(localTagPath);
		this.onEmbeddedXmlFound = onEmbeddedXmlFound;
	}

	protected ThreadedPipe createPipe(
			CheckedConsumer<InputStream> onEmbeddedXmlFound)
			throws IOException {
		return new ThreadedPipe(this.onEmbeddedXmlFound);
	}

	/**
	 * @param xmlr
	 * @param context
	 * @throws Exception
	 */
	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException {
		if (xmlr.isStartElement() && context.isCurrentPath(localTagPath)) {
			try {
				pipe = createPipe(onEmbeddedXmlFound).start();
			} catch (IOException ex) {
				throw new StaxParserParsingException(ex.getMessage(), ex);
			}
			return;
		}

		if (xmlr.isEndElement() && context.isCurrentPath(localTagPath)) {
			pipe.stop();
			pipe = null;
			return;
		}

		if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
			if (pipe != null) {
				try {
					pipe.feed(xmlr.getTextCharacters(), xmlr.getTextStart(),
							xmlr.getTextLength());
				} catch (IOException ex) {
					log.warn(
							"IO Exception during feed. Might be an issue within the task ... - {}",
							ex.getMessage());
					log.debug(
							"IO Exception during feed. Might be an issue within the task ...",
							ex);
					// sth happened with the pipe, so maybe there is an
					// exception in the
					// future
					try {
						pipe.stop();
					} finally {
						pipe = null;
					}
				}
			}
		}
	}

}
