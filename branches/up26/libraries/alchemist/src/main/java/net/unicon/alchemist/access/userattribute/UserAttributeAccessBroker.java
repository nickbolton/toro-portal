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

package net.unicon.alchemist.access.userattribute;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusUser;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;

import org.dom4j.Element;

public class UserAttributeAccessBroker extends AccessBroker {

	// instance members 
	private Map userAttributeMap = new HashMap();
	
	public static AccessBroker parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("access-broker")) {
            String msg = "Argument 'e [Element]' must be an <access-broker> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // User attribute
        List uList = e.selectNodes("user-attribute");
        if (uList.size() == 0) {
            String msg = "Element <access-broker> must contain at least one "
                                            + "<user-attribute> field element.";
            throw new IllegalArgumentException(msg);
        }
        
        Map attributeMap = new HashMap();
        
        try {
            for(int i = 0; i < uList.size(); i++){
                Element uAttr = (Element)uList.get(i);
                 
                // user attribute name
                String name = uAttr.attributeValue("name");

                // Class to handle the attribute
                String impl = uAttr.attributeValue("impl");
                Class cimpl = Class.forName(impl);
                Method m = cimpl.getDeclaredMethod("parse", new Class[] { Element.class });
                IUserAttribute info = (IUserAttribute)m.invoke(null, new Object[] { uAttr });

                attributeMap.put(name, info);
                
            }
        } catch (Throwable t) {
            throw new RuntimeException(
                    "Unable to successfully invoke the parse() method on the"
                  + "user-attribute implementation.", t);
        }
        
        return new UserAttributeAccessBroker(e, attributeMap);

    }

	/**
     * Constructor.
	 * @param e AccessBroker configuration data
     * @param attributeMap Mapping of attribute names to IUserAttribute implementations
	 */
	public UserAttributeAccessBroker(Element e, Map attributeMap) {
		
		super(e);
		
		// Assertions.
        if (attributeMap == null) {
            String msg = "Argument 'UserAttributeMap' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (attributeMap.size() == 0) {
            String msg = "Argument 'UserAttributeMap' cannot be empty.";
            throw new IllegalArgumentException(msg);
        }
        
        this.userAttributeMap.putAll(attributeMap);	

	}

	public IAccessEntry setAccess(Identity y, AccessRule[] rules, Object target) {
		throw new IllegalArgumentException("This Broker is allows only read-only access.");		
	}

	public void removeAccess(IAccessEntry[] entries) {
		throw new IllegalArgumentException("This Broker is allows only read-only access.");
	}

	public IAccessEntry getEntry(Identity y, Object target) {
		
		IAccessEntry result = null;
		
		IAccessEntry[] entries = getEntries(new Principal(new Identity[]{y}), new AccessRule[0]);
		
		for(int i = 0; i < entries.length; i++){
			if(entries[i].getIdentity().equals(y) && entries[i].getTarget().equals(target)){
				return entries[i];
			}
		}
		return result;
	}

    public IAccessEntry[] getEntries() { throw new UnsupportedOperationException(); }

	public IAccessEntry[] getEntries(Principal p, AccessRule[] rules) {

		List entries = new ArrayList();
		Identity[] identities = p.getIdentities();
		IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade();
		String username = null;
		String target  = null;
		IAcademusUser user = null;
		IUserAttribute info = null;
		Object factory = null;

		for(int i = 0; i < identities.length; i++){
			try{
				if(identities[i].getType().equals(IdentityType.USER))
				{
					username = identities[i].getId();
					user = facade.getUser(username);
					
					Iterator it = userAttributeMap.keySet().iterator();
					while(it.hasNext()){
						String key = (String)it.next();

						target = user.getAttribute(key);

						if(target != null){
							info = (IUserAttribute)userAttributeMap.get(key);
                            factory = info.getObject(target);
	
							if(rules.length > 0){
								// check if the types match
								List toCompare = Arrays.asList(info.getAccessRules());
								int k = 0;
								for(; k < rules.length; k++){
									if(!toCompare.contains(rules[k])){
										break;
									}
								}
								
								if(k == rules.length){
									entries.add(new AccessEntryImpl(identities[i], rules, factory));
								}
							}else{
								entries.add(new AccessEntryImpl(identities[i], info.getAccessRules(), factory));
							}
						}
					}
				}

			} catch (AcademusFacadeException afe) {
	            // do nothing
	        }
		}
		return (IAccessEntry[])(entries.toArray(new IAccessEntry[0]));
	}

	public IAccessEntry[] getEntries(Object target, AccessRule[] rules) {
		return new IAccessEntry[0];
	}
}
