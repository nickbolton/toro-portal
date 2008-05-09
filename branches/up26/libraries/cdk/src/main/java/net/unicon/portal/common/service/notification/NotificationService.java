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

import java.util.List;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.User;

public interface NotificationService {
    public static final int TYPE_NOTIFICATION = 1;
    public static final int TYPE_EMAIL        = 2;
    public static final int TYPE_ALL          = 3; /* TYPE_EMAIL | TYPE_NOTIFICATION */

    /**
     * Send a notification to a single user.
     * @param userList A list of User objects to send the notification to
     * @param fromUser the sender of the notification
     * @param msg message body
     */
    public void sendNotifications(List userList, User fromUser, String msg)
    throws Exception;

    /**
     * Send a notification to a single user.
     * @param toUser the user to send the notification to
     * @param fromUser the sender of the notification
     * @param msg message body
     */
    public void sendNotification(User toUser, User fromUser, String msg)
    throws Exception;

    /**
     * Send a notification and/or email to a list of users.
     *
     * @param userList an array of Users to send a notification or email to
     * @param fromUser the sender of the notification or email
     * @param subject Message subject to use
     * @param msg Message body to send
     * @param type Type of message to send: email, notification or both
     */
    public void sendNotifications(User[] userList, User fromUser,
                                  String subject, String msg, int type)
                                  throws Exception;

    /**
     * Send a notification and/or email to a list of users.
     *
     * @param userList an array of IdentityData User Entities to send a notification or email to
     * @param fromUser the sender of the notification or email
     * @param subject Message subject to use
     * @param msg Message body to send
     * @param type Type of message to send: email, notification or both
     */
    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, int type)
                                  throws Exception;

    /**
     * Send a notification to a list of users, providing a link-back.
     *
     * @param userList an array of IdentityData User Entities to send a notification or email to
     * @param fromUser the sender of the notification or email
     * @param subject Message subject to use
     * @param msg Message body to send
     * @param notifyClass The fully qualified classname of the class to link-back to.
     * @param notifyParam The runtime data to associate with the link-back.
     * @param type Type of message to send: email, notification or both
     */
    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, String notifyClass,
                                  String notifyParam, int type)
                                  throws Exception;

    /**
     * Send a notification to a list of users, providing a link-back.
     *
     * @param userList      an array of IdentityData User Entities to send a
     *                      notification or email to
     * @param fromUser      the sender of the notification or email
     * @param subject       Message subject to use
     * @param msg           Message body to send
     * @param shortmsg      A shortened message body for use in terse display
     *                      locations
     * @param notifyClass   The fully qualified classname of the class to
     *                      link-back to.
     * @param notifyParam   The runtime data to associate with the link-back.
     * @param type          Type of message to send: email, notification or
     *                      both
     */
    public void sendNotifications(IdentityData[] userList, IdentityData fromUser,
                                  String subject, String msg, String shortmsg,
                                  String notifyClass, String notifyParam,
                                  int type) throws Exception;
}
