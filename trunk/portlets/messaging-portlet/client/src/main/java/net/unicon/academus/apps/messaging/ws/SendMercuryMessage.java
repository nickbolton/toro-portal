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

package net.unicon.academus.apps.messaging.ws;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.apps.ConfigHelper;
import net.unicon.academus.apps.messaging.FactoryInfo;
import net.unicon.academus.apps.messaging.MessageCenter;
import net.unicon.academus.apps.messaging.RdbmsMessageFactoryCreator;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.MercuryException;

import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SendMercuryMessage {
    private static final String configPath = "/config/messaging-portlet.xml";

    private static MessageCenter mcenter = null;
    private static InetAddress[] allowHosts = null;

    public SendMercuryMessage() {
        if (mcenter == null) {
            bootstrap();
        }
    }

    public boolean sendNotification(String toUser, String fromUser,
                                 String subject, String message) {
        return sendNotification(new String[] {toUser}, fromUser, subject, message);
    }
    
    public boolean sendNotification(String[] userList, String fromUser,
            String subject, String message){
        return sendNotification(userList, null, fromUser, subject, message);
    }

    public boolean sendNotification(String[] userList, String[] groupList, String fromUser,
                                 String subject, String message) {
        checkSecurity();

        if (fromUser == null || fromUser.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'fromUser' cannot be null or empty.");
        if (message == null || message.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'message' cannot be null or empty.");
        if (message.length() > 4000)
            throw new IllegalArgumentException(
                    "Argument 'message' cannot exceed 4000 characters.");

        DraftMessage draft = new DraftMessage();

        if (subject == null || subject.trim().equals("")) {
            subject = message;
            if (subject.length() > 25) {
                subject = (new StringBuffer(subject.substring(0, 22)))
                            .append("...").toString();
            }
        } else if (subject.length() > 256) {
            subject = subject.substring(0, 255);
        }

        draft.setSubject(subject);
        draft.setBody(message);

        if (userList != null) {
	        for (int i = 0; i < userList.length; i++) {
	            if (userList[i] == null || userList[i].equals(""))
	                throw new IllegalArgumentException(
	                    "Argument 'userList' cannot include null or empty values.");
	
	            draft.addRecipient("To", userList[i], userList[i], EntityType.USER);
	        }
        }
        
        if (groupList != null) {
	        for (int i = 0; i < groupList.length; i++) {
	            if (groupList[i] == null || groupList[i].equals(""))
	                throw new IllegalArgumentException(
	                    "Argument 'groupList' cannot include null or empty values.");
	
	            draft.addRecipient("To", groupList[i], groupList[i], EntityType.GROUP);
	        }
        }
        
        if (draft.getRecipients().length == 0) {
           throw new IllegalArgumentException(
                 "No recipients specified.");
        }

        IMessageFactory fact = null;
        try {
            Principal p = getPrincipal(fromUser);
            FactoryInfo fInfo = mcenter.createFactory(p);
            if (fInfo == null)
                throw new RuntimeException(
                        "Unable to send notification from user '"+fromUser+"': User unknown.");

            fact = fInfo.getFactory();

//System.out.println("Sending notification: "+draft.getSubject());
            fact.sendMessage(draft);

        } catch (MercuryException ex) {
            ex.printStackTrace(System.err);
            throw new RuntimeException("Unable to send notification: "+ex.getMessage(), ex);
        } finally {
        	if (fact != null)
        		fact.cleanup();
        }

        return true;
    }

    private void checkSecurity() {
        MessageContext mctx = MessageContext.getCurrentContext();

        String remoteIP = mctx.getStrProp(Constants.MC_REMOTE_ADDR);
        if (remoteIP != null && !remoteIP.equals("127.0.0.1")) {
            try {
                InetAddress remoteAddr = InetAddress.getByName(remoteIP);

                boolean found = false;
                for (int i = 0; !found && i < allowHosts.length; i++) {
                    if (allowHosts[i].equals(remoteAddr))
                        found = true;
                }
                if (!found)
                    throw new RuntimeException("Access denied for remote host");
            } catch (UnknownHostException e) {
                throw new RuntimeException(
                        "Access denied for unknown host");
            }

        }
    }

    private void bootstrap() {
        synchronized(SendMercuryMessage.class) {
            if (mcenter == null) {
                try {
                    URL configUrl = this.getClass().getResource(configPath);
                    Element configElement =
                        (Element) (new SAXReader()).read(configUrl.toString())
                                        .selectSingleNode("messaging");
                    configElement = ConfigHelper.handle(configElement);

                    List list = configElement.selectNodes("//allowed-hosts[@service='SendMercuryMessage']/host");
                    Iterator it = list.iterator();
                    allowHosts = new InetAddress[list.size()];
                    int i = 0;
                    while (it.hasNext()) {
                        allowHosts[i++] = InetAddress.getByName(((Element)it.next()).getText());
                    }
                    
                    Element el = (Element)configElement.selectSingleNode("query-file");
                    if (el == null) {
                       throw new IllegalArgumentException("Unable to locate required <query-file> element");
                    }
                    RdbmsMessageFactoryCreator.bootstrap(el.getText());

                    el = (Element)configElement.selectSingleNode("//message-center[@id='notifications']");
                    mcenter = new MessageCenter(el);
                    
                } catch (UnknownHostException ex) {
                	throw new RuntimeException("Unknown host [" + ex.getMessage() + "] in allowed-hosts directive: "+configPath, ex);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to parse configuration file: "+configPath, ex);
                }
            }
        }
    }

    private Principal getPrincipal(String username) {
        Principal rslt = null;
        try {
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade(true);
            IAcademusGroup[] groups = facade.getAllContainingGroups(username);
            Identity[] ids = new Identity[groups.length + 1];
            for (int i=0; i < groups.length; i++) {
                ids[i] = new Identity(
                            groups[i].getGroupPaths(
                                IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR,
                                false)[0],
                            IdentityType.GROUP);
            }
            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            rslt = new Principal(ids);
        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }
        return rslt;
    }
}

