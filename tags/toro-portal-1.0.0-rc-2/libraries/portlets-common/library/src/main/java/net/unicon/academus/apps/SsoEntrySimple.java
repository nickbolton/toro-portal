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

import org.dom4j.Attribute;
import org.dom4j.Element;

public class SsoEntrySimple extends SsoEntry {
    
    private final String handle;
    private final String label;
    private final String description;
    private final String window;
    private final String uiClass;
    private final SsoAuthentication authentication;
    private final Map targets;
    private final Map sequences;
    

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
    	
		// Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
        	String msg = "Element <sso-entry> is missing required attribute 'handle'.";
            throw new IllegalArgumentException(msg);
        }
        String handle = h.getValue(); 
        
        // Label.
        Element b = (Element) e.selectSingleNode("label");
        if (b == null) {
        	String msg = "Element <sso-entry> is missing required child element <label>.";
            throw new IllegalArgumentException(msg);
        }
        String label = b.getText();

        // Description.
        Element d = (Element) e.selectSingleNode("description");
        if (d == null) {
        	String msg = "Element <sso-entry> is missing required child element <description>.";
            throw new IllegalArgumentException(msg);
        }
        String description = d.getText();

        // Window.
        Element w = (Element) e.selectSingleNode("window");
        if (w == null) {
        	String msg = "Element <sso-entry> is missing required child element <window>.";
            throw new IllegalArgumentException(msg);
        }
        String window = w.asXML();

        // UIClass.
        Attribute c = e.attribute("class");
        if (c == null) {
        	String msg = "Element <target> is missing required attribute 'class'.";
            throw new IllegalArgumentException(msg);
        }
        String uiClass = c.getValue(); 
        
        // Authentication.
        Element a = (Element) e.selectSingleNode("authentication");
        SsoAuthentication authentication = null;
        if (a != null) {
            authentication = SsoAuthentication.parse(a);
        }

        // map of all targets
        Map targets = new HashMap();        
        for(Iterator it = e.selectNodes("target").iterator(); it.hasNext();) {
            SsoTarget r = SsoTarget.parse((Element) it.next());
            targets.put(r.getHandle(), r);	        
        }
        
        // Sequences
        Map sequences = new HashMap();
        for(Iterator it = e.selectNodes("sequence").iterator(); it.hasNext();){
            Element s = (Element) it.next();
            SsoSequence seq = SsoSequence.parse(s); 
	        sequences.put(seq.getType(), seq);
    	}
        
        return new SsoEntrySimple(handle, label, description, window, uiClass, authentication, targets, sequences);
        
    }
        
    public String getHandle() {
        return this.handle;
    }
    
    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public String getWindow() {
        return this.window;
    }

    public String getUIClass() {
        return this.uiClass;
    }

    public SsoAuthentication getAuthentication() {
        return this.authentication;
    }
    
    public SsoTarget[] getTargets(){
        return (SsoTarget[])this.targets.values()
        .toArray(new SsoTarget[this.targets.values().size()]);
    }
    
    public SsoSequence[] getSequences(){
        return (SsoSequence[])this.sequences.values()
        .toArray(new SsoSequence[this.sequences.values().size()]);
    }
    
    public SsoTarget getTarget(String handle){
        return (SsoTarget)this.targets.get(handle);
    }
    
    public SsoSequence getSequence(String type){
        return (SsoSequence)this.sequences.get(type);
    }
    
    /*
     * Implementation.
     */
    
    private SsoEntrySimple(String handle, String label, String description,
            String window, String uiClass, SsoAuthentication auth,
            Map targets, Map sequences){
        
        // Assertions
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "Argument 'description' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (window == null) {
            String msg = "Argument 'window' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (uiClass == null) {
            String msg = "Argument 'uiClass' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if(targets == null || targets.isEmpty()){
            throw new IllegalArgumentException("Argument 'targets' cannot be null or empty.");
        }
        if(sequences == null || sequences.isEmpty()){
            throw new IllegalArgumentException("Argument 'sequences' cannot be null or empty.");
        }
        //      NB:  Argument 'auth' may be null.
        
        this.targets = targets;
        this.sequences = sequences;
        this.handle = handle;
        this.label = label;
        this.description = description;
        this.window = window;
        this.uiClass = uiClass;
        this.authentication = auth;

    }

}
