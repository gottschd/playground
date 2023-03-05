package org.gottschd.jaxbcontextutils;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class JAXBContextUtils {

    private final static Address address;

    private final static JAXBContext myStaticJAXBContext;
    static {
        // create static jaxb context
        try {
            myStaticJAXBContext = JAXBContext.newInstance(Address.class);
        } catch (JAXBException e) {
            throw new RuntimeException("ups", e);
        }

        // create test address
        address = new Address();
        address.street = "test-strasse";
        address.city = "test-city";
        address.country = "Germany";
        address.plz = "123456";
    }

    /**
     * 
     * @return
     */
    public static JAXBContext getStaticContext() {
        return myStaticJAXBContext;
    }

    /**
     * 
     * @param jaxbContext
     * @return
     * @throws JAXBException
     */
    public static String marshallExample(JAXBContext jaxbContext) throws JAXBException {
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();

        jaxbMarshaller.marshal(address, stringWriter);
        return stringWriter.toString();
    }

}
