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
import java.util.Collections;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Authentication handler base class for Single Sign On.
 *
 * <p>In order for an authentication module to work with HTTP-Basic, the auth
 * variables "username" and "password" are expected (prefixed, of course, with
 * ATTRIB_PREFIX).</p>
 *
 * @see #resolve(Map)
 * @see #createAuthentication(Map,String,String)
 */
public abstract class SsoAuthentication {

	/*
	 * Public API.
	 */

	/**
     * Name prefix for all attributes added by an SsoAuthentication module
     * during the {@link #resolve(Map)} operation. This is used to avoid
     * name clashes with user attributes.
     */
    public static final String ATTRIB_PREFIX = "auth.";

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

		SsoAuthentication rslt = null;
		try {

			Class implClass = null;	// no default for authentication...
			Attribute impl = e.attribute("impl");	// check this attribute first (new pattern)...
			if (impl == null) {
				// Check the 'old' pattern -- 'handler' attribute...
				impl = e.attribute("handler");	// check this attribute first (new pattern)...
			}
			if (impl != null) {
					implClass = Class.forName(impl.getValue());
			} else {
				// This is a prblem...
				String msg = "Element <authentication> is missing required attribute 'impl'.";
				throw new IllegalArgumentException(msg);
			}
			
			Method m = implClass.getDeclaredMethod("parse", new Class[] {Element.class});
			rslt = (SsoAuthentication) m.invoke(null, new Object[] {e});

		} catch (Throwable t) {
			String msg = "Unable to create the requested SsoAuthentication instance."; 
			throw new RuntimeException(msg, t);
		}
		
		return rslt;
		
	}
	
    /**
     * Unmodifiable map containing all static parameters that the
     * SsoAuthentication module required in their configuration.
     */
    protected final Map params;
/*
    public static SsoAuthentication getInstance(String className, Map params) {
        SsoAuthentication rslt = null;

        try {
            Class c = Class.forName(className);
            Constructor ctor = c.getConstructor(new Class[]
                                        { Map.class });
            rslt = (SsoAuthentication)ctor.newInstance(new Object[] { params });
        } catch (Throwable t) {
            throw new RuntimeException(
                "Unable to instansiate SsoAuthentication handler: "+className, t);
        }

        return rslt;
    }
*/
    /**
     * Resolve a set of user attributes into a set of authentication
     * information.
     *
     * <p>This method should return a Map consisting of the union between the
     * input, userAttribs, and any variables generated as a result of the
     * resolution process. Any variables added to the map by this module should
     * be prefixed with the string {@link #ATTRIB_PREFIX}.</p>
     *
     * <p>The input map, userAttribs may be unmodifiable. A copy should be
     * created for use in the return.</p>
     *
     * <p>There are no guaranteed attribute names in the input map. The
     * expected attribute names should be mapped using the static configuration
     * of the module.</p>
     *
     * <p>If insufficient information is available to resolve the user's
     * authentication, a NeedsAuthException can be thrown. This will inform the
     * system that a {@link #createAuthentication(Map,String,String)} needs to
     * be called after retrieving input from the user.</p>
     *
     * <p>In order for an authentication module to work with HTTP-Basic, the auth
     * variables "username" and "password" are expected (prefixed, of course, with
     * ATTRIB_PREFIX).</p>
     *
     * @param userAttribs User attributes; may be unmodifiable
     * @return a union of userAttribs and the resolved authentication
     * parameters. Must be a mutable map.
     * @throws NeedsAuthException if additional information is needed from the
     * user
     */
    public abstract Map resolve(Map userAttribs) throws NeedsAuthException;

    /**
     * Create, or update, an authentication mapping.
     *
     * This method will only be called if the {@link #resolve(Map)} method
     * throws a NeedsAuthException.
     *
     * @param userAttribs User attributes
     * @param username The username to be used in the authentication mapping
     * @param password The password to be used in the authentication mapping
     */
    public abstract void createAuthentication(Map userAttribs, String username, String password);

    /**
     * Boolean affirmation that the createAuthentication method is supported.
     * This is used to prevent extraneous workflows that end up performing no
     * changes.
     */
    public abstract boolean supportsAuthenticate();

    /**
     * Constructor. Construct an SsoAuthentication object with the given static
     * parameters as retrieved from the configuration.
     */
    protected SsoAuthentication(Map params) {
        this.params = Collections.unmodifiableMap(params);
    }
    
    public String[] getCredentials(String userKey)
    throws NeedsAuthException {
        throw new UnsupportedOperationException();
    }
}
