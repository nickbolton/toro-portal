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

package net.unicon.portal.common.service.notification;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.apps.messaging.ws.SendMercuryMessage_PortType;
import net.unicon.academus.apps.messaging.ws.SendMercuryMessageServiceLocator;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.User;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.mercury.fac.email.EmailAccount;
import net.unicon.mercury.fac.email.EmailMessageFactory;
import net.unicon.mercury.fac.email.EmailRecipientType;
import net.unicon.mercury.fac.email.SMTPTransportAccount;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

public class NotificationServiceMercuryImpl implements NotificationService {
    private static boolean bootstrapped = false;
    private static final String serviceUrlProperty =
    	NotificationServiceMercuryImpl.class.getName()+".service_location";
    private static final String serviceUrl =
    	UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(serviceUrlProperty);
    private static final String UNDISCLOSED_RECIPIENTS_LABEL = 
        "Undisclosed-Recipients";

    public void sendNotification(User toUser, User fromUser,
                                 String msg) throws Exception {
        sendNotifications(new User[] { toUser }, fromUser,
                          null, msg, TYPE_NOTIFICATION);
    }

    public void sendNotifications(List userList, User fromUser,
                                  String msg) throws Exception {
        sendNotifications((User[])userList.toArray(new User[0]), fromUser,
                          null, msg, TYPE_NOTIFICATION);
    }

    public void sendNotifications(User[] userList, User fromUser,
                                  String subject, String msg, int type)
                                  throws Exception {
        StringBuffer buf = new StringBuffer();

        // Convert fromUser to IdentityData
        IdentityData fromUserID = new IdentityData();
        buf.append(fromUser.getFirstName())
           .append(" ")
           .append(fromUser.getLastName());
        fromUserID.putType(IdentityData.ENTITY);
        fromUserID.putEntityType(GroupData.S_USER);
        fromUserID.putID(fromUser.getUsername());
        fromUserID.putName(buf.toString());
        fromUserID.putEmail(fromUser.getEmail());
        buf.setLength(0);

        // Convert the userList to the equivilent IdentityData
        IdentityData[] _id = new IdentityData[userList.length];
        for (int i = 0; i < userList.length; i++) {
            buf.append(userList[i].getFirstName())
               .append(" ")
               .append(userList[i].getLastName());

            _id[i] = new IdentityData();
            _id[i].putType(IdentityData.ENTITY);
            _id[i].putEntityType(GroupData.S_USER);
            _id[i].putID(userList[i].getUsername());
            _id[i].putName(buf.toString());
            _id[i].putEmail(userList[i].getEmail());

            buf.setLength(0);
        }

        sendNotifications(_id, fromUserID, subject, msg, type);
    }

    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, int type)
                                  throws Exception {
        sendNotifications(userList, fromUser, subject, msg, null, null, type);
    }

    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, String notifyClass,
                                  String notifyParam, int type)
                                  throws Exception {
        sendNotifications(userList, fromUser, subject, msg, null, notifyClass,
                          notifyParam, type);
    }

    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, String shortmsg,
                                  String notifyClass, String notifyParam,
                                  int type) throws Exception {
        if (userList == null || userList.length == 0)
            throw new IllegalArgumentException(
                    "Argument 'userList' cannot be null or empty.");
        if (msg == null || msg.trim().equals(""))
            throw new IllegalArgumentException(
                    "Argument 'msg' cannot be null or empty.");
        if (fromUser == null)
            throw new IllegalArgumentException(
                    "Argument 'fromUser' cannot be null.");
        if (!fromUser.getType().equals(IdentityData.ENTITY)
                || !fromUser.getEntityType().equals(GroupData.S_USER))
            throw new IllegalArgumentException(
                    "Argument 'fromUser' must be a user entity.");
        if (((type & TYPE_NOTIFICATION) != 0) && fromUser.getID() == IdentityData.ID_UNKNOWN)
            throw new IllegalArgumentException(
                    "Argument 'fromUser' must be a valid portal user to send notifications.");
      
        Finder.findDetail(fromUser, null);

        DraftMessage emailDraft = new DraftMessage();
        List users = new ArrayList();
        List groups = new ArrayList();
        
        boolean sendEmail = (type & TYPE_EMAIL) != 0;

        if (subject == null) {
            subject = msg;
            if (subject.length() > 25) {
                subject = (new StringBuffer(subject.substring(0, 22)))
                            .append("...").toString();
            }
        }

        emailDraft.setSubject(subject);

        emailDraft.setBody(msg);
        
        String recipientType = null;
        if (userList.length == 1 && 
                !userList[0].getType().equals(IdentityData.GROUP)) { 
            // Only one recipient (not a group), so send in "TO" field
            recipientType = EmailRecipientType.TO.getLabel();
        } 
        else { // Add sender as the lone TO recipient, actual notification 
               // recipients will be BCC'd for obscurity. See JIRA Issue AC-385
            emailDraft.addRecipient(EmailRecipientType.TO.getLabel(), 
                    UNDISCLOSED_RECIPIENTS_LABEL, fromUser.getEmail(), 
                    EntityType.USER);
            recipientType = EmailRecipientType.BCC.getLabel();
        }
        
        findDetails(userList);

        IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade();
        for (int i = 0; i < userList.length; i++) {
            if (sendEmail && userList[i].getID().equals(IdentityData.ID_UNKNOWN)) {
                // Email only - email is username
                String email = userList[i].getName();
                emailDraft.addRecipient(recipientType, email, email, 
                        EntityType.USER);
            } else {
                if(userList[i].getType().equals(IdentityData.GROUP)){
                    facade.getGroup(userList[i].getID());
                    groups.add(facade.getGroup(userList[i].getID())
                            .getGroupPaths(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, 
                            false)[0]);
                    
                    // break down the group and add the email to the 
                    // email draft
                    if(sendEmail) {
                        IdentityData[] gData = 
                            GroupData.expandGroups(
                                    new IdentityData[] { userList[i] }, false);
                        findDetails(gData);
                        
                        for (int x = 0; x < gData.length; x++) {
                            if (gData[x].getEmail() != null && 
                                    !gData[x].getEmail().trim().equals("")) {
                            	emailDraft.addRecipient(recipientType,
                                        gData[x].getName(), gData[x].getEmail(), 
                                        EntityType.USER);
                            }
                            
                        }
                        
                    }
                } else {
                    users.add(userList[i].getAlias());
                    
                    String email = userList[i].getEmail();
                    final String name = userList[i].getName();
                    if (sendEmail && email != null && !email.trim().equals(""))
                        emailDraft.addRecipient(recipientType,
                                userList[i].getName(), email, EntityType.USER);                      
                }
            }
        }
        
        if ((type & TYPE_NOTIFICATION) != 0) {
            // Check for callbacks
            try {
                if (notifyClass != null && notifyParam != null) {
                    Class c = Class.forName(notifyClass);
                    boolean found = false;
                    Class[] ifaces = c.getInterfaces();
                    for (int i = 0; !found && i < ifaces.length; i++)
                        if (ifaces[i].equals(INotifyCallback.class))
                            found = true;

                    if (found) {
                        Method m = c.getDeclaredMethod("prepareNotificationCallback", new Class[] {String.class});

                        String ret = (String)m.invoke(null, new Object[] { notifyParam });

                        if (ret != null && !ret.trim().equals("")) {
                            StringBuffer bbuf = new StringBuffer(msg);
                            bbuf.append("===<<<");
                            bbuf.append(ret);
                            bbuf.append(">>>===");
                            msg = bbuf.toString();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                // Otherwise ignore.. we still need to send the notification
            }

            if (serviceUrl == null || serviceUrl.equals(""))
                throw new IllegalArgumentException(
                		"No value for property "+serviceUrlProperty+" found");

            // Acquire a reference to the binding stub for the given URL.
            SendMercuryMessage_PortType binding = null;
            try {
                binding = new SendMercuryMessageServiceLocator()
                              .getSendMercuryMessage(new URL(serviceUrl));
            } catch (javax.xml.rpc.ServiceException jre) {
                throw new RuntimeException("JAX-RPC ServiceException caught: " + jre);
            }

            String[] recs = new String[0];
            if (!users.isEmpty() || !groups.isEmpty()) {
	            // Send a notification to the users and groups
	            boolean value = binding.sendNotification((String[])users.toArray(new String[0])
	                    , (String[])groups.toArray(new String[0])
	                    , fromUser.getAlias()
	                    , subject, msg);
	            if (!value)
	                throw new Exception("Failed to send notification");
            }
            
      }

        if (sendEmail) {
            getEmailFactory(getMailHost(), fromUser.getEmail())
                .sendMessage(emailDraft);
        }
    }

    private void findDetails(IdentityData[] userList) {
        // Fill in any missing information: username, and email specifically
        for (int x = 0; x < userList.length; x++) {
            if(userList[x].getType().equals(IdentityData.ENTITY) &&
        		 (userList[x].getID() != IdentityData.ID_UNKNOWN) &&
	             ((userList[x].getEmail() == null || 
	                 userList[x].getEmail().trim().equals(""))
	                || (userList[x].getName() == null ||  
	                    userList[x].getName().trim().equals(""))
	                || (userList[x].getAlias() == null || 
	                    userList[x].getAlias().trim().equals(""))
	                || (userList[x].getID() == null ||  
	                    userList[x].getID().trim().equals(""))
	                )) {
            	Finder.findDetail(userList[x], null);
            }
        }

	}

	private String getMailHost() {
        return Channel.getRADProperty("notification.smtp.host");
    }

    private IMessageFactory getEmailFactory(String host, String from) {
        SMTPTransportAccount smtpacct = new SMTPTransportAccount();
        smtpacct.setHostname(host);
        smtpacct.setFrom(from);

        return new EmailMessageFactory(new EmailAccount(null, smtpacct));
    }
    
    protected void markAsDirty(IMessage mesg) throws MercuryException {
		Set targets = new HashSet();
		IRecipient[] recips = mesg.getRecipients();
    	for (int i = 0; i < recips.length; i++) {
    		targets.add(recips[i].getAddress().toNativeFormat());
    	}
    	targets.add(mesg.getSender().toNativeFormat());
    	MercuryCacheValidatorContainer.getInstance()
    		.markAsDirty((String[])targets.toArray(new String[targets.size()]));
	} 
}

