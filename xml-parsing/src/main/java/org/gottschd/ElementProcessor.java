package org.gottschd;

public interface ElementProcessor {

    void feed(char[] textCharacters, int textStart, int textLength) throws Exception;

    void close() throws Exception;

}
