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
package net.unicon.portal.util;

import net.unicon.sdk.authentication.AuthenticationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.services.Authentication;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.InitialSecurityContextFactory;
import java.util.HashMap;

/*
 * This provides an interface for user authentication within the uPortal
 * system. It is basically a wrapper around the uPortal
 * <code>Authentication</code> service.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    public AuthenticationServiceImpl() {
        super();
    }
    public boolean authenticate(String username, String password)
    throws Exception {

        IPerson person = new PersonImpl();

        ISecurityContext context =
            InitialSecurityContextFactory.getInitialContext("root");
        person.setSecurityContext(context);

        Authentication authService = new Authentication();
        HashMap principals = new HashMap(1);
        principals.put("root", username);
        HashMap credentials = new HashMap(1);
        credentials.put("root", password);
        authService.authenticate(principals, credentials, person);
        return person.getSecurityContext().isAuthenticated();
    }
}
