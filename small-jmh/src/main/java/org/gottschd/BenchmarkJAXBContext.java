package org.gottschd;

import java.util.concurrent.TimeUnit;

import org.gottschd.jaxbcontextutils.Address;
import org.gottschd.jaxbcontextutils.JAXBContextUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

@Threads(20)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 4, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 4, timeUnit = TimeUnit.SECONDS)
public class BenchmarkJAXBContext {

    @Benchmark
    public String marshallNew() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Address.class);
        return JAXBContextUtils.marshallExample(jaxbContext);
    }

    @Benchmark
    public String mashallStatic() throws JAXBException {
        JAXBContext jaxbContext = JAXBContextUtils.getStaticContext();
        return JAXBContextUtils.marshallExample(jaxbContext);
    }

}