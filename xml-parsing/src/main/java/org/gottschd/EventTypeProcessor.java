package org.gottschd;

import javax.xml.stream.XMLStreamReader;

public interface EventTypeProcessor {

    void processEvent(XMLStreamReader xmlr) throws Exception;

}
