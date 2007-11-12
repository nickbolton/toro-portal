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

package net.unicon.academus.apps.permissions;

import java.lang.reflect.Method;

import java.util.List;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessType;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PortletAccessHelper {

    // instance members
    private final String handle; 
    private final AccessBroker broker;
    private final AccessType[] accessTypes;
    
    public PortletAccessHelper(Element e){
        Attribute handle = e.attribute("handle");
        if (handle == null)
            throw new IllegalArgumentException(
                    "Element <portlet-access> must have a 'handle' attribute");
        this.handle = handle.getValue();

        // parse the broker
        Element e2 = (Element)e.selectSingleNode("access-broker");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <portlet-access> must contain an <access-broker> element.");
        }

        this.broker = AccessBroker.parse(e2);
        
        // get the accessType Enumeration class
        e2 = (Element)e.selectSingleNode("access");
        Attribute impl = e2.attribute("impl");
        if(impl == null){
            throw new IllegalArgumentException("The element <access> must contain an 'impl' attribute.");
        }
        
        Class c;
        try {
            c = Class.forName(impl.getValue());
        
	        Method m = c.getMethod("getInstances", null);
            
            // parse the accessTypes
	        /*List access = e.selectNodes("access/accesstype");
	        if (access.isEmpty()) {
	            throw new IllegalArgumentException(
	                    "Element <access> must contain at least one <accesstype> element.");
	        }
	        accessTypes = new AccessType[access.size()];
	        String accessId = null;
	        for(int i = 0; i < access.size(); i++){
	            accessId =  ((Element)access.get(i)).getText();
	            accessTypes[i] = (AccessType)m.invoke(null, new Object[] {accessId});
            } */
	        accessTypes = (AccessType[])m.invoke(null, null);
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException("Could not find the class " + impl.getValue(), e1);
        } catch (NoSuchMethodException e3) {
	        throw new RuntimeException("Could not find the Method 'getAccessType' on class " + impl.getValue(), e3);	    
	    } catch (IllegalArgumentException e3) {
	        throw new RuntimeException("Incorrect arguments passed in the Method 'getAccessType' on class " + impl.getValue(), e3);	    
	    } catch (Exception e4) {
	        throw new RuntimeException("Error in invoking the method 'getAccessType' on class " + impl.getValue(), e4);
        }	        
    }
    
    public String getHandle() {return this.handle;}
    
    public AccessBroker getAccessBroker() {return this.broker;}   
    
    public AccessType[] getAccessTypes(){
        // make a defensive copy
        AccessType[] rslt = new AccessType[this.accessTypes.length];
        System.arraycopy(this.accessTypes, 0, rslt, 0, this.accessTypes.length);
        return rslt;
    }
    
}
