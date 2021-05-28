package org.gottschd;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XmlParsingController {

    @RequestMapping(value = "/komm", method = RequestMethod.POST, consumes = "application/xml", produces = "application/xml")
    public String upload(HttpServletRequest request) throws Exception {
        Thread.sleep(20 * 1000);
        return "blubb2";
    }
}
