package org.gottschd;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class XmlParsingController {
    private AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getVisitor() {
        return "Visitor updated " + counter.incrementAndGet();
    }
}
