package org.gottschd;

import java.util.List;

import org.gottschd.XmlParsingController.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class XmlParsingBusinessSpringController {

    private static final Logger logger = LoggerFactory.getLogger(XmlParsingController.class);

    @Autowired
    private ObjectMapper mapper;

    @PostMapping(value = "/stax", consumes = "application/xml", produces = "application/json")
    public ResponseEntity<String> uploadStax(HttpServletRequest request) throws Exception {
        logger.info("Start parsing with spring boot ...");
        long now = System.currentTimeMillis();
        Result result = XmlParsingController.uploadSoap(request.getInputStream());
        logger.info("Parsing took %s ms ...", System.currentTimeMillis() - now);

        if (result.getErrorMessage() != null) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(new ErrorResponse(result.getErrorMessage())));
        }

        if (result.isHoniggutXml()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mapper
                    .writeValueAsString(new HoniggutResponse(result.getExtractedDataSizes())));
        }

        if (result.isBXml()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(new BResponse(result.getExtractedDataSizes())));
        }

        throw new IllegalStateException("Unknown state");
    }

    public record HoniggutResponse(List<Integer> extractedDataSizes) {

    }

    public record BResponse(List<Integer> extractedDataSizes) {

    }

    public record ErrorResponse(String errorMessage) {

    }
}
