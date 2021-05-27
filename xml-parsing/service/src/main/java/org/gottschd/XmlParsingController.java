package org.gottschd;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XmlParsingController {

	@PostMapping(path = "/komm", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> upload(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok("blubb");
    }
}
