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

import java.lang.reflect.Method;

import org.dom4j.Element;

/**
 * @author ibiswas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class SsoEntry {
        
	/*
	 * Public API.
	 */
	
	public static SsoEntry parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("sso-entry")) {
            String msg = "Argument 'e [Element]' must be an <sso-entry> "
                                                        + "element.";
            throw new IllegalArgumentException(msg);
        }

		Method m = null;
		SsoEntry rslt = null;
		try {
			m = ConfigHelper.getParser(e, SsoEntrySimple.class);
			rslt = (SsoEntry) m.invoke(null, new Object[] {e});
		} catch (Throwable t) {
			String msg = "Unable to create the specified SsoEntry implementation.";
			throw new RuntimeException(msg, t);
		}
			
		return rslt;

    }

    public abstract String getHandle();
    
    public abstract String getLabel();

    public abstract String getDescription();

    public abstract String getWindow();

    public abstract String getUIClass();

    public abstract SsoAuthentication getAuthentication();
    
    public abstract SsoTarget[] getTargets();
    
    public abstract SsoSequence[] getSequences();
    
    public abstract SsoTarget getTarget(String handle);
    
    public abstract SsoSequence getSequence(String type);
    
}
