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

package net.unicon.academus.apps.messaging;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class PortletSessionCleanupListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent se) {
        //HttpSession session = se.getSession();
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();

        Enumeration attrs = session.getAttributeNames();

        while (attrs.hasMoreElements()) {
            String attrName = (String)attrs.nextElement();

            if (attrName.endsWith(":CONTEXT")) {
            	Object obj = session.getAttribute(attrName);
            	if (obj instanceof MessagingUserContext) {
	            	MessagingUserContext muc = (MessagingUserContext)obj;
	            	muc.cleanup();
            	}
            } else if (attrName.endsWith(":FACTORIES")) {
                FactoryManager factMan = (FactoryManager)session.getAttribute(attrName);

                FactoryInfo[] finfo = factMan.listFactories();
                for (int i = 0; i < finfo.length; i++)
                    finfo[i].getFactory().cleanup();

                // So it doesn't get serialized
                session.removeAttribute(attrName);
            }
        }

    }
}
