package org.gottschd.stax.utils;

import org.gottschd.stax.StaxParserParsingException;

@FunctionalInterface
public interface CheckedFunction<T, R> {

	R apply(T t) throws StaxParserParsingException;

}
