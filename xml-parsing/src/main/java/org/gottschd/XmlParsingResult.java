package org.gottschd;

import java.util.ArrayList;
import java.util.List;

public class XmlParsingResult {

    private String remainingXml;

    private final List<byte[]> collectedBytes = new ArrayList<>();

    public List<byte[]> getDataOfMyContainer() {
        return collectedBytes;
    }

    public void setRemainingXml(String remainingXml) {
        this.remainingXml = remainingXml;
    }

    /**
     * @return String return the remainingXml
     */
    public String getRemainingXml() {
        return remainingXml;
    }

    /**
     * 
     */
    public void addBytesDataOfMyContainer(byte[] bytes) {
        System.out.println("addBytes:"+bytes.length);
        collectedBytes.add(bytes);
    }

}
