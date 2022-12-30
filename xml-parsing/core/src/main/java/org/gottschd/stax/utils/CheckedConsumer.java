package org.gottschd.stax.utils;

import org.gottschd.stax.StaxParserParsingException;

@FunctionalInterface
public interface CheckedConsumer<T> {

	void apply(T t) throws StaxParserParsingException;

}
