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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@JMSDestinationDefinition(name = "java:global/queue/simpleQ", interfaceName = "javax.jms.Queue", destinationName = "simpleQ")
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:global/queue/simpleQ"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class MessageConsumerMdb implements MessageListener {

	@Resource(name = "MyFancyEnvVar")
	private String envVar;

	public MessageConsumerMdb() {
		// default ctr
	}

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println("Message received by '" + envVar + "' (expected: 'foo'): " + message.getBody(String.class));
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

}
