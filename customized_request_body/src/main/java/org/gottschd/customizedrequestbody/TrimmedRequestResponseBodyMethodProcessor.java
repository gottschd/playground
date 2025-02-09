package org.gottschd.customizedrequestbody;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

@Slf4j
public class TrimmedRequestResponseBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

    public TrimmedRequestResponseBodyMethodProcessor(MappingJackson2HttpMessageConverter trimmingConverter) {
        super(List.of(trimmingConverter));
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("supportsParameter - TrimmedRequestBody: {}", parameter.hasParameterAnnotation(TrimmedRequestBody.class));
        return parameter.hasParameterAnnotation(TrimmedRequestBody.class);
    }
}
