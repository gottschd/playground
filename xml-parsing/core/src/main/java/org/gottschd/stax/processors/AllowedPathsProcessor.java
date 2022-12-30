package org.gottschd.stax.processors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;

public class AllowedPathsProcessor implements EventTypeProcessor {

	private final List<StaxParserPath> allowedPaths;

	public AllowedPathsProcessor(String... allowedPaths) {
		this.allowedPaths = Arrays.stream(allowedPaths)
				.map(StaxParserPath::fromString).collect(Collectors.toList());
	}

	@Override
	public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
			throws StaxParserParsingException, XMLStreamException {
		if (!isAllowedPath(context.currentPath())) {
			throw new StaxParserParsingException(
					"unsupported path: " + context.currentPath());
		}
	}

	private boolean isAllowedPath(StaxParserPath currentPath) {
		for (StaxParserPath allowedPath : allowedPaths) {
			if (allowedPath.startWith(currentPath)) {
				return true;
			}
		}
		return false;
	}

}
