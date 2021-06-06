package org.gottschd.stax.processors;

import java.util.Objects;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;

public class DetectXmlTag implements EventTypeProcessor {
    private final Consumer<Integer> onTagCallback;

    private final String localName;

    public DetectXmlTag(String localName, Consumer<Integer> onTagCallback) {
        this.localName = Objects.requireNonNull(localName);
        this.onTagCallback = Objects.requireNonNull(onTagCallback);
    }

    @Override
    public void processEvent(XMLStreamReader xmlr) throws Exception {
        if (xmlr.isStartElement() && localName.equals(xmlr.getLocalName())) {
            onTagCallback.accept(Integer.valueOf(xmlr.getEventType()));
        }

        if (xmlr.isEndElement() && localName.equals(xmlr.getLocalName())) {
            onTagCallback.accept(Integer.valueOf(xmlr.getEventType()));
        }
    }

}
