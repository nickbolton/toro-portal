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
import java.util.Map;

import org.dom4j.Element;

public abstract class SsoTarget {
        
    /*
     * Public API.
     */
    
	public static SsoTarget parse(Element e) {
		
		// Assertions.
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!e.getName().equals("target")) {
			String msg = "Argument 'e [Element]' must be a <target> element.";
			throw new IllegalArgumentException(msg);
		}

		Method m = null;
		SsoTarget rslt = null;
		try {
			m = ConfigHelper.getParser(e, SsoTargetSimple.class);
			rslt = (SsoTarget) m.invoke(null, new Object[] {e});
		} catch (Throwable t) {
			String msg = "Unable to create the specified SsoTarget implementation.";
			throw new RuntimeException(msg, t);
		}
			
		return rslt;
		
	}

	public abstract String getId();

    public abstract String getHandle();

    public abstract String getURL();
    
    public abstract Map getEvaluators();

    public abstract String getMethod();

    public abstract Map getParameters();

}
