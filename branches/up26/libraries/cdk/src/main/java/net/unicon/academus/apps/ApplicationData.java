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
package net.unicon.academus.apps;

import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.permissions.IPermissions;

//import org.jasig.portal.utils.SAX2BufferImpl;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Document;

public class ApplicationData {
    private String charBuffer;
    //    private SAX2BufferImpl saxBuffer;
    private String xml;
    private String errorMsg;
    private Document document;
    private String sheetName;

    private String domainUsername;
    private boolean dirty;
    private Map attributes = new HashMap();
    private IPermissions permissions;
    private boolean contextChanged;
    private AcademusBaseApp app;

    public AcademusBaseApp getApplication() {
	return app;
    }
    public void setApplication(AcademusBaseApp app) {
	this.app = app;
    }

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }
    public void putAttribute(Object key, Object value) {
        attributes.put(key, value);
    }
    public void removeAttribute(Object key) {
        attributes.remove(key);
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public User getDomainUser() {
        User domainUser = null;
        try {
            domainUser = UserFactory.getUser(domainUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainUser;
    }
    public void setDomainUser(User domainUser) {
        this.domainUsername = domainUser.getUsername();
    }
    public IPermissions getPermissions() {
        return permissions;
    }
    public void setPermissions(IPermissions p) {
        this.permissions = p;
    }
}
