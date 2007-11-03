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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

public class SsoAuthenticationSimple extends SsoAuthentication {
	
    private static final String usernamekey = "username-key";
    private static final String passwordkey = "password-key";

    /*
     * Public API.
     */
    
	public static SsoAuthentication parse(Element e) {
		
		// Assertions.
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!e.getName().equals("authentication")) {
			String msg = "Argument 'e [Element]' must be an <authentication> element.";
			throw new IllegalArgumentException(msg);
		}
		
        Map authParams = new HashMap();
        for (Iterator it = e.selectNodes("parameter").iterator(); it.hasNext();) {
            Element p = (Element) it.next();
            String key = p.selectSingleNode("@name").getText();
            String val = p.selectSingleNode("value").getText();
            authParams.put(key, val);
        }
		
		return new SsoAuthenticationSimple(authParams);
		
	}

    public Map resolve(Map userAttribs) throws NeedsAuthException {
        Map rslt = new HashMap(userAttribs);
        rslt.put(ATTRIB_PREFIX+"username",
                userAttribs.get(
                    super.params.get(usernamekey)));
        rslt.put(ATTRIB_PREFIX+"password",
                userAttribs.get(
                    super.params.get(passwordkey)));
        return rslt;
    }

    public void createAuthentication(Map userAttribs, String username, String password) {
        // This method will not be called, as this module never throws
        // NeedsAuthException from resolve().
        throw new UnsupportedOperationException();
    }

    public boolean supportsAuthenticate() { return false; }

    /*
     * Implementation.
     */
    
    private SsoAuthenticationSimple(Map params) {
        super(params);
    }    
    
}
