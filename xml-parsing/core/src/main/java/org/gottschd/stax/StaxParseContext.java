package org.gottschd.stax;

import org.gottschd.stax.processors.StaxParserPath;

public class StaxParseContext {

	private final String name;

	private final StaxParserPath breadCrumb = new StaxParserPath();

	public StaxParseContext(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public StaxParserPath currentPath() {
		return breadCrumb;
	}

	public boolean isCurrentPath(StaxParserPath path) {
		return breadCrumb.equals(path);
	}

	public void pushElement(String localName) {
		breadCrumb.addLast(localName);
	}

	public void popElement() {
		breadCrumb.removeLast();
	}

}
