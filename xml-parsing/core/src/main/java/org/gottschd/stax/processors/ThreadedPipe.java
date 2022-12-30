package org.gottschd.stax.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gottschd.stax.StaxParser;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.utils.CheckedConsumer;

/**
 *
 */
class ThreadedPipe implements Callable<Void> {

	private static final ExecutorService executor = Executors
			.newCachedThreadPool();

	private final PipedOutputStream pop;

	private final PipedInputStream pip;

	private OutputStreamWriter dataStream;

	private Future<Void> parsingFuture;

	private final CheckedConsumer<InputStream> pipeOut;

	ThreadedPipe(CheckedConsumer<InputStream> pipeOut) throws IOException {
		this.pipeOut = pipeOut;

		pop = new PipedOutputStream();
		pip = new PipedInputStream(pop, StaxParser.BUFFER_SIZE_FOR_ALL_PLACES);
	}

	final ThreadedPipe start() {
		dataStream = wrapPipe(pop);
		parsingFuture = executor.submit(this);
		return this;
	}

	protected OutputStreamWriter wrapPipe(PipedOutputStream pop) {
		return new OutputStreamWriter(pop);
	}

	final void stop() throws StaxParserParsingException {
		assert dataStream != null;
		try {
			dataStream.close();
			parsingFuture.get();
		} catch (ExecutionException ex) {
			if (ex.getCause() instanceof StaxParserParsingException) {
				throw (StaxParserParsingException) ex.getCause();
			}

			throw new StaxParserParsingException(ex.getMessage(),
					(Exception) ex.getCause());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new StaxParserParsingException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new StaxParserParsingException(ex.getMessage(), ex);
		} finally {
			dataStream = null;
		}
	}

	final void feed(char[] textCharacters, int textStart, int textLength)
			throws IOException {
		assert dataStream != null;
		dataStream.write(textCharacters, textStart, textLength);
	}

	@Override
	public Void call() throws StaxParserParsingException {
		try (InputStream in = pip) {
			pipeOut.apply(in);
		} catch (IOException e) {
			throw new StaxParserParsingException(e.getMessage(), e);
		}
		return null;
	}

}
