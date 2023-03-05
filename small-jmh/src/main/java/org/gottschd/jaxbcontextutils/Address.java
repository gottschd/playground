package org.gottschd.jaxbcontextutils;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Address {
    String street;
    String plz;
    String city;
    String country;
}
