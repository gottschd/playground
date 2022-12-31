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

    @PostMapping(value = "/stax", consumes = MediaType.TEXT_XML_VALUE, produces = "application/json")
    public ResponseEntity<String> uploadStax(HttpServletRequest request) throws Exception {
        logger.info("Start parsing with spring boot ...");
        long now = System.currentTimeMillis();
        Result result = XmlParsingController.uploadSoap(request.getInputStream());
        logger.info("Parsing took {} ms ...", System.currentTimeMillis() - now);

        if (result.getErrorMessage() != null) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(new ErrorResponse(result.getErrorMessage())));
        }

        if (result.isHoniggutXml()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(HoniggutResponse.ofResult(result)));
        }

        if (result.isBXml()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(BResponse.ofResult(result)));
        }

        throw new IllegalStateException("Unknown state");
    }

    public record HoniggutResponse(List<Integer> extractedDataSizes) {
        static HoniggutResponse ofResult(Result result) {
            return new HoniggutResponse(result.getExtractedDataSizes());
        }
    }

    public record BResponse(List<Integer> extractedDataSizes) {
        static BResponse ofResult(Result result) {
            return new BResponse(result.getExtractedDataSizes());
        }
    }

    public record ErrorResponse(String errorMessage) {

    }
}
