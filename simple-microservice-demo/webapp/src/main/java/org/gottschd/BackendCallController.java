package org.gottschd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BackendCallController
{
    @Autowired
    BackendService backend;
    
    @RequestMapping( value = "/", method = RequestMethod.GET )
    public String callBackend() throws Exception {
        return "headless call to backend:" + backend.callBackend();
    }
}
