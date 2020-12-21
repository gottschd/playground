/*

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright (c) 2016 C2B2 Consulting Limited. All rights reserved.

 The contents of this file are subject to the terms of the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License.  You can
 obtain a copy of the License at
 https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 or packager/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.

 When distributing the software, include this License Header Notice in each
 file and include the License file at packager/legal/LICENSE.txt.
 */
package org.acme;

import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// Note: create the jms queue 'queue/simpleQ' manually
public class MessageProducerEjb {

    private String envVar;

    public void sendMessage(String msg) {
        try {
            // Gets the JNDI context
            Context jndiContext = new InitialContext();
            // Looks up the administered objects
            ConnectionFactory connectionFactory = (ConnectionFactory)
                    jndiContext.lookup("java:comp/DefaultJMSConnectionFactory");
            Destination queue = (Destination) jndiContext.lookup("queue/simpleQ");
            // Sends a text message to the queue
            try (JMSContext context = connectionFactory.createContext()) {
                context.createProducer().send(queue, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void myTimer() {
        sendMessage("From v2 '" + envVar + "' (expected: 'bar') with love. " + new Date().toString());
    }
}
