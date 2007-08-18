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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class PortletHelper {
    
    // instance members
    private final String handle;
    private final String label;
    private final String description;
    private final Map accessHelpers; 		// each portlet can have multiple portlet access helpers
    
    public PortletHelper(Element e) {
        Attribute attrId = e.attribute("handle");
        if (attrId == null)
            throw new IllegalArgumentException(
                    "Element <portlet> must have a 'handle' attribute");
        this.handle = attrId.getValue();

        Element e2 = (Element)e.selectSingleNode("label");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <portlet> must contain a <label> element.");
        } else {
            this.label = e2.getText();
        }

        e2 = (Element)e.selectSingleNode("description");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <portlet> must contain a <description> element.");
        } else {
            this.description = e2.getText();
        }

        List access = e.selectNodes("portlet-access");
        if (access.isEmpty()) {
            throw new IllegalArgumentException(
                    "Element <portlet> must contain at least one <portal-access> element.");
        }
        
        Attribute handle = null;
        this.accessHelpers = new HashMap();
        
        for(int i = 0; i < access.size(); i++){
            handle = ((Element)access.get(i)).attribute("handle");
            if(handle == null){
                throw new IllegalArgumentException("Element <portlet-access> must have a 'handle' attribute.");
            }
            this.accessHelpers.put(handle.getValue(), new PortletAccessHelper(((Element)access.get(i))));
        }
        
    }
    
    public String getHandle() {return this.handle;}
    
    public String getLabel() {return this.label;}
    
    public String getDescription() {return this.description;}
    
    public PortletAccessHelper getAccessHelper(String handle) {
        Object helper = this.accessHelpers.get(handle);
        if(helper == null){
            throw new IllegalArgumentException("Helper with the given handle was not found." + handle);
        }
        
        return (PortletAccessHelper)helper;
    }
    
    public PortletAccessHelper[] listAccessHelpers(){
        return (PortletAccessHelper[])this.accessHelpers.values().toArray(new PortletAccessHelper[0]);
        
    }
    
    public String toXml(){
        StringBuffer rslt = new StringBuffer();
        
        rslt.append("<portlet id=\"")
        .append(getHandle())
        .append("\">");

	    rslt.append("<label>")
	        .append(getLabel())
	        .append("</label>");
	
	    rslt.append("<description>")
	        .append(getDescription())
	        .append("</description></portlet>");
	    
	    return rslt.toString();
    }
    
}
