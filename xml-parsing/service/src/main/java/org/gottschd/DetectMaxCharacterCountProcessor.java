package org.gottschd;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;

public class DetectMaxCharacterCountProcessor implements EventTypeProcessor {

    private final String localName;

    private final long maxCharacterCountBorder;

    private final Consumer<Long> pOnTooManyCharactersCallback;

    private LongAdder adder;

    private long countedCharacters = 0;

    public DetectMaxCharacterCountProcessor(String localName, long maxCharacterCountBorder,
            Consumer<Long> pOnTooManyCharactersCallback) {
        this.localName = localName;
        this.maxCharacterCountBorder = maxCharacterCountBorder;
        this.pOnTooManyCharactersCallback = pOnTooManyCharactersCallback;
    }

    @Override
    public void processEvent(XMLStreamReader xmlr) throws Exception {

        if (xmlr.isStartElement() && localName.equals(xmlr.getLocalName())) {
            adder = new LongAdder();
            return;
        }

        if (xmlr.isEndElement() && localName.equals(xmlr.getLocalName())) {
            countedCharacters = adder.longValue();
            adder = null;
            return;
        }

        if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
            if (adder != null) {
                int textLength = xmlr.getTextLength();
                long newValue = adder.longValue() + textLength;
                if (newValue > maxCharacterCountBorder)
                    pOnTooManyCharactersCallback.accept(Long.valueOf(newValue));

                adder.add(textLength);
            }
        }
    }

    public long getCountedCharaters() {
        return countedCharacters;
    }

}
