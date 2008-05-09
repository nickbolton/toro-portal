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
package net.unicon.academus.delivery.academus;

import java.io.StringReader;
import java.util.Hashtable;

import org.xml.sax.InputSource;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.unicon.sdk.util.NodeToString;
import net.unicon.academus.domain.assessment.*;
import net.unicon.sdk.properties.UniconPropertiesFactory;

public class JMSContentListener {
    public static void main(String[] args) {
        Queue queue = null;
        QueueConnectionFactory queueConnectionFactory = null;
        QueueConnection        queueConnection        = null;

        // Establishing Connection to the JMS Queue
        try {
            /*
            String username = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.Username");
            String password = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.Password");
            */
            String jmsURL   = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.ProviderURL");
System.out.println("JMS URL : " + jmsURL);            
            String iFactory = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.InitialContextFactory");
System.out.println("INIT FACTORY : " + iFactory);  
            String queueName = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.QueueName");
System.out.println("QUEUE NAME : " + queueName);            
            String queueConnFactory = 
                UniconPropertiesFactory.getManager(JMSContentPropertiesType.JMS).getProperty(
                        "net.unicon.academus.delivery.academus.jms.QueueConnectionFactorySubscriber");
System.out.println("QUEUE CONN : " + queueConnFactory);                
            Hashtable env = new Hashtable(10);
            //env.put(Context.SECURITY_PRINCIPAL,username);
            //env.put(Context.SECURITY_CREDENTIALS,password);
            env.put(Context.PROVIDER_URL,jmsURL);
            env.put(Context.INITIAL_CONTEXT_FACTORY,iFactory);
  
            Context context = new InitialContext(env);            
            queueConnectionFactory =(QueueConnectionFactory)context.lookup(queueConnFactory);
            queue = (Queue) context.lookup(queueName);
        }
        catch (NamingException nEx) {
            System.out.println(nEx.toString()+"\nDoes the queue exist?");
            System.exit(1);
        }
        
        try {
            // Connection to Queue Connection Factory    
            queueConnection = queueConnectionFactory.createQueueConnection();
            QueueSession queueSession = queueConnection.createQueueSession(
                                            false, 
                                            Session.CLIENT_ACKNOWLEDGE);

            // Creating Queue Receiver
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            
            // Starting Connection Queue Listener
            queueConnection.start();
      
            
            // Establishing a Text Message Receiver
            while (true) { 
                TextMessage message = (TextMessage) queueReceiver.receive();

                if(message != null) {
                    try {
                        importMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    message.acknowledge();
                } 
                else {
                    System.out.println("No message received...");
                }
            }
        } 
        catch (JMSException jmsEx) {
            System.out.print("Something went wrong with message consumption, ");
            System.out.println("please try again");
            System.out.println("Exception: " + jmsEx.toString());
        } 
        finally {
            if (queueConnection != null) {
                try {
                      queueConnection.close();
                } catch (Exception any) {}
            }
        }
    }

    public static void importMessage(TextMessage message) throws Exception {
        /* 
        String client = message.getStringProperty("Sender");
        System.out.println("Received message from "+client);
        System.out.println("--> "+message.getText());
        */

        /* Getting XML data */
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(
                                new InputSource(
                                    new StringReader(
                                        message.getText())));
        NodeList questionNodes   = document.getElementsByTagName("item");
        NodeList assessmentNodes = document.getElementsByTagName("assessment");
        
        importQuestions(questionNodes);
        importAssessments(assessmentNodes);
    }

    public static void importAssessments(NodeList assessments) throws Exception {
        if ( assessments != null) {
            for (int ix = 0; ix < assessments.getLength(); ++ix ) {
                AssessmentFactory.importAssessment( 
                                new NodeToString(
                                    (Node) assessments.item(ix)).toString());
            }
        }
    }

    public static void importQuestions(NodeList questions) throws Exception {
        if ( questions != null) {
            for (int ix = 0; ix < questions.getLength(); ++ix ) {
                QuestionFactory.importQuestion( 
                                new NodeToString(
                                    (Node) questions.item(ix)).toString());
            }
        }
    }
}



