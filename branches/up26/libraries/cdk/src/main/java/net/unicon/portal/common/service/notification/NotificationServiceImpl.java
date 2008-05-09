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

import java.util.ArrayList;
import java.util.List;

import net.unicon.academus.apps.notification.INotified;
import net.unicon.academus.apps.notification.INotifier;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.common.service.notification.NotificationService;

import org.jasig.portal.services.LogService;

public class NotificationServiceImpl implements NotificationService {

    public void sendNotification(User toUser, User fromUser, String msg)
    throws Exception {
        sendNotifications(new User[] { toUser }, fromUser,
                          null, msg, TYPE_NOTIFICATION);
    }

    public void sendNotifications(List userList, User fromUser, String msg)
    throws Exception {
        sendNotifications((User[])userList.toArray(new User[0]), fromUser,
                          null, msg, TYPE_NOTIFICATION);
    }

    public void sendNotifications(User[] userList, User fromUser,
                                  String subject, String msg, int type)
                                  throws Exception {

        // Convert fromUser to IdentityData
        IdentityData fromUserID = new IdentityData();
        fromUserID.putType(IdentityData.ENTITY);
        fromUserID.putEntityType(GroupData.S_USER);
        fromUserID.putID(fromUser.getUsername());
        fromUserID.putName(fromUser.getUsername()); // Original code used getUsername(), continue doing so.
        fromUserID.putEmail(fromUser.getEmail());

        // Convert the userList to the equivilent IdentityData
        IdentityData[] _id = new IdentityData[userList.length];
        for (int i = 0; i < userList.length; i++) {
            _id[i] = new IdentityData();
            _id[i].putType(IdentityData.ENTITY);
            _id[i].putEntityType(GroupData.S_USER);
            _id[i].putID(userList[i].getUsername());
            _id[i].putName(userList[i].getUsername()); // Original code used getUsername(), continue doing so.
            _id[i].putEmail(userList[i].getEmail());
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
        if (shortmsg == null || shortmsg.equals(""))
            shortmsg = msg;

        // create instance
        Notification noti = new Notification();

        // subject area
        if (subject != null) {
            // Both m_type and m_subject are for the subject...
            noti.m_type = subject;
            noti.m_subject = subject;
        } else {
            // Original behaviour
            noti.m_type = "Message";
        }

        // Notification link-backs
        noti.m_params = notifyParam;
        noti.m_notified = notifyClass;

        // Generate sender information
        noti.m_sender = fromUser;

        // text sent to notification
        noti.m_text = shortmsg; // notification body
        noti.m_body = msg; // email body

        // generate date of notification
        User toUser = null;
        noti.m_date = new java.sql.Timestamp(System.currentTimeMillis());

        // send to the user of choice or group
        noti.send(userList, type);
    }
}
