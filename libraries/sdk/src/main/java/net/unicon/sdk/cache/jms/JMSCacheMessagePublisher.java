/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */
package net.unicon.sdk.cache.jms;

import java.io.Serializable;
import java.util.Hashtable;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.DeliveryMode;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
// import org.jasig.portal.services.LogService;

public class JMSCacheMessagePublisher {
    private static TopicConnection        conn    = null;
    private static TopicSession           session = null;
    private static Topic                  topic   = null;
    private static TopicConnectionFactory tcf     = null;
    private static TopicPublisher         send    = null;
    private static final String USERNAME =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.Username");
    private static final String PASSWORD =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.Password");
    private static final String JMS_URL  =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.ProviderURLSubscriber");
    private static final String IFACTORY =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.InitialContextFactorySubscriber");
    private static final String CONNECTION_FACTORY =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.TopicConnectionFactorySubscriber");
    private static final String TOPIC_LIST =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.SubscriptionTopicList");
    private static final String SERVER_NAME =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.SubscriberName");
    static {
        setupPublisher();
    }
    /**
     * Sets up the Publisher.
     * @exception javax.jms.JMSException
     * @exception javax.jms.NamingException
     */
    public static void setupPublisher()  {
        try {
            Hashtable env = new Hashtable(10);
            env.put(Context.SECURITY_PRINCIPAL,     USERNAME);
            env.put(Context.SECURITY_CREDENTIALS,   PASSWORD);
            env.put(Context.PROVIDER_URL,           JMS_URL );
            env.put(Context.INITIAL_CONTEXT_FACTORY, IFACTORY);
            Context context = new InitialContext(env);
            Object tmp = context.lookup(CONNECTION_FACTORY);
            tcf = (TopicConnectionFactory) tmp;
            conn    = tcf.createTopicConnection();
            topic   = (Topic) context.lookup(TOPIC_LIST);
            session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            conn.start();
            // Determining what topic to publish message to
            send = session.createPublisher(topic);
            send.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (NamingException ne) {
ne.printStackTrace();
//            LogService.instance().log(LogService.ERROR,
//            "JMSCacheMessagePublisher: Unable to do a Federated JMS Cache Naming Lookup.");
//            LogService.instance().log(LogService.ERROR, ne);
        } catch (JMSException je) {
            je.printStackTrace();
//            LogService.instance().log(LogService.ERROR,
//            "JMSCacheMessagePublisher: Error in Federated JMS Cache Publisher setup.");
//            LogService.instance().log(LogService.ERROR, je);
        }
    }
    /**
     * Publish a asynchronouse object message to a topic.
     * @param <code>javax.jms.Message</code> JMS Message
     * @see javax.jms.Message
     */
    public static void publishAsynchMessage(Serializable object) throws JMSException {
        ObjectMessage message = session.createObjectMessage(object);
        message.setStringProperty("publisher", SERVER_NAME);
        // Publishing the message
        send.publish(message);
    }
    /**
     * Publish a asynchronous text message to a topic.
     * @param text - a <code>String</code> message
     * @param className - the class name of the implementing TextMessageHelper
     * @see javax.jms.Message
     * @see javax.jms.TestMessage
     * @see net.unicon.common.cache.jms.TextMessageHelper
     */
    public static void publishAsynchTextMessage(String text, String className)
    throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setStringProperty("publisher", SERVER_NAME);
        // Setting the text
        message.setText(text);
        // Setting the classname as the message type
        message.setJMSType(className);
        //message.setStringProperty("pub-subName", SERVER_NAME);
        // Publishing the message
        send.publish(message);
    }
    /**
     * method closes the connection to for the publisher.
     * @exception javax.jms.JMSException
     */
    public static void stop() throws JMSException {
        conn.stop();
        session.close();
        conn.close();
    }
}
