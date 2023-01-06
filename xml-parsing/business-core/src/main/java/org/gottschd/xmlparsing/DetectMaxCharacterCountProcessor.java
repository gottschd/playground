package org.gottschd.xmlparsing;

import java.util.concurrent.atomic.LongAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.gottschd.stax.EventTypeProcessor;
import org.gottschd.stax.StaxParseContext;
import org.gottschd.stax.StaxParserParsingException;
import org.gottschd.stax.processors.StaxParserPath;
import org.gottschd.stax.utils.CheckedConsumer;

public class DetectMaxCharacterCountProcessor implements EventTypeProcessor {

    private final StaxParserPath localTagPath;

    private final long maxCharacterCountBorder;

    private final CheckedConsumer<Long> pOnTooManyCharactersCallback;

    private LongAdder adder;

    private long countedCharacters = 0;

    public DetectMaxCharacterCountProcessor(String localTagPath, long maxCharacterCountBorder,
            CheckedConsumer<Long> pOnTooManyCharactersCallback) {
        this.localTagPath = StaxParserPath.fromString(localTagPath);
        this.maxCharacterCountBorder = maxCharacterCountBorder;
        this.pOnTooManyCharactersCallback = pOnTooManyCharactersCallback;
    }

    @Override
    public void processEvent(XMLStreamReader xmlr, StaxParseContext context)
            throws StaxParserParsingException, XMLStreamException {

        if (xmlr.isStartElement() && context.isCurrentPath(localTagPath)) {
            adder = new LongAdder();
            return;
        }

        if (xmlr.isEndElement() && context.isCurrentPath(localTagPath)) {
            countedCharacters = adder.longValue();
            adder = null;
            return;
        }

        if (xmlr.isCharacters() || xmlr.isWhiteSpace()) {
            if (adder != null) {
                int textLength = xmlr.getTextLength();
                long newValue = adder.longValue() + textLength;
                if (newValue > maxCharacterCountBorder)
                    pOnTooManyCharactersCallback.apply(Long.valueOf(newValue));

                adder.add(textLength);
            }
        }
    }

    public long getCountedCharaters() {
        return countedCharacters;
    }

}
