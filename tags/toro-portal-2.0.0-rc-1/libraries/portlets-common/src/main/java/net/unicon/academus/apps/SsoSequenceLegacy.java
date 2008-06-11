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

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;


public class SsoSequenceLegacy extends SsoSequence {
    
	// Instance Members.
	private final String type;
    private final String[] targetIds;
    private final String buttonXml;

	/*
	 * Public API.
	 */

	public static SsoSequence parse(Element e) {
		
		// Assertions.
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!e.getName().equals("sequence")) {
			String msg = "Argument 'e [Element]' must be a <sequence> element.";
			throw new IllegalArgumentException(msg);
		}

		
		// Type.
    	Attribute y = e.attribute("type"); 
        if (y == null) {
        	String msg ="Element <sequence> is missing required attribute 'type'."; 
            throw new IllegalArgumentException(msg);
        }    
        String type = y.getValue();
        
        // ButtonXml.
        String buttonXml = "";
        Element b = (Element) e.selectSingleNode("sequence-start-trigger");
        if(b != null){
            buttonXml = b.asXML();
        }
        
        // TargetIds.
        List list = e.selectNodes("target");	        
        String[] targetIds = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            
        	Element r = (Element) list.get(i);
        	
            Attribute h = r.attribute("handle");
            if (h == null) {
            	String msg ="Element <target> is missing required attribute 'handle'."; 
                throw new IllegalArgumentException(msg);
            }
            
            targetIds[i] = h.getValue();
             
        }    
		
		return new SsoSequenceLegacy(type, targetIds, buttonXml);
		
	}
    
    
    public String[] getTargetIds(){
        return this.targetIds;
    }
    
    public String getType(){
        return this.type;
    }
    
    public String getButtonXml(){
        return buttonXml;
    }
    
    /*
     * Implementation.
     */

    private SsoSequenceLegacy(String type, String[] targetIds, String buttonXml){
        
        // Assertions
        if(targetIds == null || targetIds.length == 0){
            throw new IllegalArgumentException("Argument 'targetIds' cannot be null or empty.");
        }
        
        this.type = type;
        this.targetIds = new String[targetIds.length];
        System.arraycopy(targetIds, 0, this.targetIds, 0, targetIds.length);
        this.buttonXml = buttonXml;
    }
    
}
