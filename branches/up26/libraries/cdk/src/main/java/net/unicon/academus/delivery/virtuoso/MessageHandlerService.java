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
package net.unicon.academus.delivery.virtuoso;

import javax.jms.*;
import javax.naming.*;

import java.util.*;

import net.unicon.sdk.cache.jms.JMSMessageProcessor;
import net.unicon.sdk.properties.UniconPropertiesFactory;


/** 
* Service to handle messages. It goes off a bunch of properties to establish
* connections witha a JMS Broker. Then it starts a series of threads one for 
* each topic. There are methods to register and deregister with this Handler.
* So to use it  do <code>MessageHandlerService.getInstance().register(
* <subname>,<JMSMessageProcessor>)
* The JMSMessageProcessor has a call back method called processMessage that
* is called to do message handling
* @author unicon@unicon.net
* @version $Revision: 1.3 $
*/
public class MessageHandlerService { 
    private static MessageHandlerService instance;
    private boolean startListening  = false;
    private Map subscriberMap;
    private MessageHandlerService() {
        try {
            String username = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty("Username");
            String password = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty("JMSPasswordSubscriber");
            String jmsURL = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty("JMSProviderURLSubscriber");
            String iFactory = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty(
                                "JMSInitialContextFactorySubscriber");
            String topicConnFactoryName = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty(
                                 "JMSTopicConnectionFactorySubscriber");

            Hashtable env = new Hashtable(10);
            env.put(Context.SECURITY_PRINCIPAL,username);
            env.put(Context.SECURITY_CREDENTIALS,password);
            env.put(Context.PROVIDER_URL,jmsURL);
            env.put(Context.INITIAL_CONTEXT_FACTORY,iFactory);

            Context context = new InitialContext(env);
            TopicConnectionFactory factory =
                     (TopicConnectionFactory)context.lookup(topicConnFactoryName);
            List topics = parseList(UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty("JMSSubscriptionTopicList"));
        subscriberMap = new HashMap(topics.size());
            String subscriberName = UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS).getProperty("JMSSubscriberName");
            String topicName = null;
            Topic topic = null; 
            TopicConnection connection = null;
            TopicSession session = null;
            TopicSubscriber subscriber = null;
            for(Iterator it = topics.iterator();it.hasNext();) {
                topicName = (String)it.next();
                topic = (Topic)context.lookup(topicName);
                connection = factory.createTopicConnection();
                session = connection.createTopicSession(false,
                                                Session.AUTO_ACKNOWLEDGE);
                subscriber =
                session.createDurableSubscriber(topic,subscriberName);
                ListenerThread thread = new ListenerThread(subscriberName,
                                                       subscriber);    
        subscriberMap.put(topicName,thread);
                thread.start();
            }
            connection.start();
            startListening = true;
        } catch(Exception e) {
             throw new RuntimeException(e);
        }
    }

    private static List parseList(String source) {
        List retList = new ArrayList();
        if(source == null || source.length() == 0) {
            return retList;
        }
        String token;
        int startIndex = 0;
        int endIndex = 0;
        int currentIndex = 0;

        while(endIndex > -1 ) {
            endIndex = source.indexOf(",",currentIndex + 1);
            if(endIndex == -1) {
                if(startIndex != source.length()){
                    retList.add(source.substring(startIndex,source.length()).trim());
                }
                break;
            }
            if(source.charAt(endIndex-1)!='?') {
                retList.add(source.substring(startIndex,endIndex).trim());
                startIndex = endIndex + 1;
            }
            currentIndex = endIndex + 1;
        }
        return retList;
    }

    public static MessageHandlerService getInstance() {
        if(instance ==null) {
            instance = new MessageHandlerService();
        }
        return instance;
    }    
 
    public void register(String subscriberName,JMSMessageProcessor processor) {
        ListenerThread thread = (ListenerThread)subscriberMap.get(
                            subscriberName);
        if(thread  == null) {
             throw new RuntimeException(" Listener not started for " + subscriberName);
        }
        thread.register(processor);
    }

    public void deregister(String subscriberName,JMSMessageProcessor processor) {
        ListenerThread thread = (ListenerThread) subscriberMap.get(
                                                        subscriberName);
        if(thread  == null) {
             throw new RuntimeException(" Listener not started for " + 
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
            if(!subList.contains(processor)) {
                 subList.add(processor);
            } 
        }

        public void deregister(JMSMessageProcessor processor) {
             subList.remove(processor);
        }

        public  void run() {
            while(true) {
                if(startListening) {
                    try {
                        Message msg = subscriber.receive();
                        for ( int i=0,max =subList.size();i<max;i++) {
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
/*
*
* $Log: MessageHandlerService.java,v $
* Revision 1.3  2002/11/11 17:40:53  chavan
* Made processMEssage take a Message
*
* Revision 1.2  2002/09/24 19:07:28  chavan
*  Removed system.out's
*
* Revision 1.1  2002/09/24 18:58:46  chavan
*  Message Subscribption code
*
*/

