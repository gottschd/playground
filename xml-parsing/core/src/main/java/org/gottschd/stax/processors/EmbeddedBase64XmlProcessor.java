package org.gottschd.stax.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedOutputStream;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.gottschd.stax.utils.CheckedConsumer;

public class EmbeddedBase64XmlProcessor extends EmbeddedXmlProcessor {

	public EmbeddedBase64XmlProcessor(String localTagPath, CheckedConsumer<InputStream> onEmbeddedXmlFound) {
		super(localTagPath, onEmbeddedXmlFound);
	}

	@Override
	protected ThreadedPipe createPipe(CheckedConsumer<InputStream> onEmbeddedXmlFound) throws IOException {
		return new Base64DecoderThreadedPipe(onEmbeddedXmlFound);
	}

	private static class Base64DecoderThreadedPipe extends ThreadedPipe {

		Base64DecoderThreadedPipe(CheckedConsumer<InputStream> pipeOut) throws IOException {
			super(pipeOut);
		}

		@Override
		protected OutputStreamWriter wrapPipe(PipedOutputStream pop) {
			return new OutputStreamWriter(new Base64OutputStream(pop, false));
		}

	}

}
