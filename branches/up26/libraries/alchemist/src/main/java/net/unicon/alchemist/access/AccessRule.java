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

package net.unicon.alchemist.access;

import java.lang.reflect.Method;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class AccessRule {

    private AccessType type;
    private boolean grant = false;			
    
    public AccessType getAccessType(){
        return type;
    }
    
    public boolean getStatus(){
        return grant;
    }
    
    public AccessRule(AccessType type, boolean state){
        
        // assertions
        if (type == null){
            throw new IllegalArgumentException("Argument 'type'  cannot be null");
        }
        this.type = type;
        this.grant = state;
        
    }
    
    
    /**
     * Returns an array of AccessRules from parsing the given XML.
     *  
     * @param e
     * Format of the xml
     * 			<access impl="net.unicon.academus.apps.briefcase.BriefcaseAccessType">
     *               <type handle="READ" value="GRANT"/>  
     *               <type handle="EDIT" value="GRANT"/>  
     *          </access>
     * @return An array of AccessRule objects.
     */
    public static AccessRule[] parse(Element e){
        
        // assertions 
        if (e == null){
            throw new IllegalArgumentException("Argument 'e' cannot be null.");
        }
        if(!e.getName().equalsIgnoreCase("access")){
            throw new IllegalArgumentException("Argument 'e' should be ab element with name 'access'.");
        }
        
        Attribute impl = e.attribute("impl");
        if (impl == null) {
            String msg = "Element <access> must contain the attribute "
                                        + " 'impl'.";
            throw new IllegalArgumentException(msg);
        }
        
        Class c;
        Method m;
        AccessType[] access;        
        
        List yList = e.selectNodes("type");
        AccessRule[] rules = new AccessRule[yList.size()];
        try {
            c = Class.forName(impl.getValue());
            m = c.getDeclaredMethod("getAccessType", new Class[] {String.class});
            
	        for (int i=0; i < yList.size(); i++) {
	            Element a = (Element) yList.get(i);
	            Attribute v = a.attribute("value");
	            Attribute h = a.attribute("handle");
	            if (v == null) {
	                String msg = "Element <type> is missing required attribute "
	                                                            + "'value'.";
	                throw new IllegalArgumentException(msg);
	            }
	            if (h == null) {
	                String msg = "Element <type> is missing required attribute "
	                                                            + "'handle'.";
	                throw new IllegalArgumentException(msg);
	            }
	           
	            rules[i] = new AccessRule((AccessType)m.invoke(null, new Object[] {h.getValue()})
	                      , v.getValue().equalsIgnoreCase("GRANT") ? true:false);
	        }
        } catch (Exception ex) {
            throw new RuntimeException("There was an error in parsing the Access Rule.", ex);
        } 
        
        return rules;
    }
    
    public boolean equals(Object obj){
        
        if(obj == null){
            throw new IllegalArgumentException("Argument 'obj' in the equals method cannot be null.");
        }        
        if(!(obj instanceof AccessRule)){
            throw new IllegalArgumentException("Argument 'obj' in the equals method is not an " +
            		"instance of AccessRule.");   
        }
        
        if(this.type.equals(((AccessRule)obj).getAccessType()) 
                && this.grant == (((AccessRule)obj).getStatus())){
            return true;
        }
        
        return false;
    }
}
