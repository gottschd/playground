package org.gottschd.stax.processors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.utils.CheckedConsumer;

public class Base64ExtractProcessor implements EventTypeProcessor {

	private Base64Extractor currentBase64Extractor;

	private final CheckedConsumer<byte[]> onExtractFinishedCallback;

	private final StaxParserPath localTagPath;

	public Base64ExtractProcessor(String localTagPath,
			CheckedConsumer<byte[]> onExtractFinishedCallback) {
		this.localTagPath = StaxParserPath.fromString(localTagPath);
		this.onExtractFinishedCallback = Objects.requireNonNull(onExtractFinishedCallback);
	}

	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException {

		if (xmlr.isStartElement() && context.isCurrentPath(localTagPath)) {
			currentBase64Extractor = new Base64Extractor();
			return;
		}

		if (xmlr.isEndElement() && context.isCurrentPath(localTagPath)) {
			try {
				onExtractFinishedCallback.apply(currentBase64Extractor.getBytesAndClose());
			} catch (IOException e) {
				throw new StaxParserParsingException(e.getMessage(), e);
			} finally {
				currentBase64Extractor = null;
			}
			return;
		}

		if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
			if (currentBase64Extractor != null) {
				try {
					currentBase64Extractor.feed(xmlr.getTextCharacters(), xmlr.getTextStart(),
							xmlr.getTextLength());
				} catch (IOException e) {
					throw new StaxParserParsingException(e.getMessage(), e);
				}
			}
		}
	}

	private static final class Base64Extractor {

		private final ByteArrayOutputStream result;

		private final OutputStreamWriter sink;

		private Base64Extractor() {
			result = new ByteArrayOutputStream();
			sink = new OutputStreamWriter(new Base64OutputStream(result, false));
		}

		byte[] getBytesAndClose() throws IOException {
			sink.close();
			return result.toByteArray();
		}

		void feed(char[] textCharacters, int textStart, int textLength) throws IOException {
			sink.write(textCharacters, textStart, textLength);
		}

	}

}
