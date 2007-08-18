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

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**  */
public final class JMSCacheMessageListener implements JMSMessageProcessor {
    /** Creating an instance of the JMSCacheMessage Listener */
    private static JMSCacheMessageListener instance = new JMSCacheMessageListener();
    /** Constructor */
    public JMSCacheMessageListener() {
        try {
            String topic = JMSPropertiesManager.getProperty(
            "net.unicon.sdk.cache.jms.SubscriptionTopicList");
            JMSMessageHandlerService.getInstance().register(topic, this);
        } catch (Exception e) {
            //Debug.out.println(1,"portal",
            //    "ERROR: Unable to register Listener with the JMSMessageHandlerService");
            e.printStackTrace();
        }
    }
    /**
     * Gets the name of the class
     * @return <code>String</code> - the name of the class
     */
    public String getName() {
        return "JMSCacheMessageListener";
    }
    /** @return <code>String</code> - string representation */
    public String toString() {
        return getName();
    }
    /**
     * Compares this object with something else.
     * @param <code>Object</code> - comparison object
     * @return <code>boolean</code> - is the objects are the same
     */
    public boolean equals(Object o) {
        if (o instanceof JMSCacheMessageListener) {
            return this.getName().equals(((JMSCacheMessageListener)o).getName());
        }
        return false;
    }
    public void processMessage(Message jmsMessage) {
        try {
            if (jmsMessage instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) jmsMessage;
                String className = textMessage.getJMSType();
                TextMessageHelper helper = null;
                if (className != null) {
                    helper = (TextMessageHelper) Class.forName(className).newInstance();
                    helper.setTextMessage(textMessage.getText());
                    helper.handleTextMessage();
                }
            } else
            if (jmsMessage instanceof ObjectMessage) {
                // Getting msg meat
                Serializable msg = ((ObjectMessage)jmsMessage).getObject();
                //Do object things here
            }
        } catch (Exception e) {
            //LogService.instance().log(LogService.WARN,
            //"ERROR: Unable to get Message content from the JMS Message");
            //LogService.instance().log(LogService.WARN, jmsMessage.toString());
            e.printStackTrace();
        }
    }
    public static void main (String[] args) throws Exception  {
        JMSMessageHandlerService.getInstance().register(args[0], instance);
    }
}
