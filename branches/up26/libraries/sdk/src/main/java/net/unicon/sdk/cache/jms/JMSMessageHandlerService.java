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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Service to handle messages. It goes off a bunch of properties to establish
 * connections witha a JMS Broker. Then it starts a series of threads one for each topic. There are methods to register and
 * deregister with this Handler. So to use it  do <code>JMSMessageHandlerService.getInstance().register(
 * <subname>,<JMSMessageProcessor>) The JMSMessageProcessor has a call back method called processMessage that
 * is called to do message handling
 * @author unicon@unicon.net
 * @version $LastChangedRevision$
 */
public class JMSMessageHandlerService {
    private static JMSMessageHandlerService instance;
    private boolean startListening  = false;
    private Map subscriberMap;
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
    private static final String SUBSCRIBER_NAME =
    JMSPropertiesManager.getProperty("net.unicon.sdk.cache.jms.SubscriberName");
    private JMSMessageHandlerService() throws Exception {
        try {
            Hashtable env = new Hashtable(10);
            //env.put(Context.SECURITY_PRINCIPAL,     USERNAME);
            //env.put(Context.SECURITY_CREDENTIALS,   PASSWORD);
            env.put(Context.PROVIDER_URL,           JMS_URL );
            env.put(Context.INITIAL_CONTEXT_FACTORY, IFACTORY);
            Context context = new InitialContext(env);
            TopicConnectionFactory factory =
            (TopicConnectionFactory)context.lookup(CONNECTION_FACTORY);
            //List topics = sys.getList("JMSSubscriptionTopicList");
            List topics = new ArrayList();
            topics.add(TOPIC_LIST);
            subscriberMap = new HashMap(topics.size());
            String topicName           = null;
            Topic topic                = null;
            TopicConnection connection = null;
            TopicSession session       = null;
            TopicSubscriber subscriber = null;
            for (Iterator it = topics.iterator(); it.hasNext(); ) {
                topicName = (String)it.next();
                topic = (Topic)context.lookup(topicName);
                connection = factory.createTopicConnection();
                session = connection.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);
                StringBuffer sb = new StringBuffer();
                sb.append("publisher <> '");
                sb.append(SUBSCRIBER_NAME);
                sb.append("'");
                // Filters out JMS sent by this publisher
                String messageFilter = sb.toString();
                subscriber = session.createDurableSubscriber(
                topic,
                SUBSCRIBER_NAME,
                messageFilter,
                true);
                ListenerThread thread = new ListenerThread(SUBSCRIBER_NAME,
                subscriber);
                subscriberMap.put(topicName, thread);
                thread.start();
            }
            connection.start();
            startListening = true;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    public static JMSMessageHandlerService getInstance() throws Exception  {
        if (instance == null) {
            instance = new JMSMessageHandlerService();
        }
        return instance;
    }
    public void register(String subscriberName, JMSMessageProcessor processor)
    throws Exception {
        ListenerThread thread = (ListenerThread)subscriberMap.get(
        subscriberName);
        if (thread  == null) {
            throw new Exception(" Listener not started for " + subscriberName);
        }
        thread.register(processor);
    }
    public void deregister(String subscriberName, JMSMessageProcessor processor)
    throws Exception {
        ListenerThread thread = (ListenerThread) subscriberMap.get(
        subscriberName);
        if (thread  == null) {
            throw new Exception(" Listener not started for " +
            subscriberName);
        }
        thread.deregister(processor);
    }
    public class ListenerThread extends Thread {
        private String subName;
        private TopicSubscriber subscriber;
        private List subList;
        public ListenerThread(String subName, TopicSubscriber subscriber) {
            this.subName = subName;
            this.subscriber = subscriber;
            subList = Collections.synchronizedList(new ArrayList());
        }
        public void register(JMSMessageProcessor processor) {
            if (!subList.contains(processor)) {
                subList.add(processor);
            }
        }
        public void deregister(JMSMessageProcessor processor) {
            subList.remove(processor);
        }
        public  void run() {
            while (true) {
                if (startListening) {
                    try {
                        Message msg = subscriber.receive();
                        for ( int i = 0, max = subList.size(); i < max; i++) {
                            ((JMSMessageProcessor)subList.get(i)).processMessage(msg);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
