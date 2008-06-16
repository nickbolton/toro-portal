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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class SsoTargetSimple extends SsoTarget {
    
    private static final Object lock = new Object();
    private static long nextId = 0;
    private static final String idPrefix = "ssoTarget";

    // Instance Members.
    private final String id;
    private final String handle;
    private final String method;
    private final String url;
    private final Map params;
    private final Map evaluators;
    
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
		
		// Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
        	String msg = "Element <target> is missing required attribute 'handle'.";
            throw new IllegalArgumentException(msg);
        }
        String handle = h.getValue(); 
            
        // Method.
        Element m = (Element) e.selectSingleNode("method");
        if (m == null) {
        	String msg = "Element <target> is missing required child element <method>.";
            throw new IllegalArgumentException(msg);
        }
        String method = m.getText();

        // Url.
        Element u = (Element) e.selectSingleNode("url");
        if (u == null) {
        	String msg = "Element <target> is missing required child element <url>.";
            throw new IllegalArgumentException(msg);
        }
        String url = u.getText();

        // Parameters & Evaluators.
        Map params = new HashMap();
        Map evaluators = new HashMap();
        for (Iterator it = e.selectNodes("parameter").iterator(); it.hasNext();) {
            Element p = (Element) it.next();
            String key = p.attribute("name").getValue();
            String val = null;
            if(p.element("evaluate")!= null){
                try {
                    String evauluatorImpl = p.element("evaluate").attributeValue("evauluatorImpl");
                    //map evaluator to parameter key
                    evaluators.put(key, evauluatorImpl);
                    //get parameter value
                    val = p.selectSingleNode("evaluate").getStringValue();
                } catch (Exception ex) {
                        throw new RuntimeException("Problem instantiating IAttributeEvaluator for " +p.selectSingleNode("evaluate").getStringValue(), ex);
                } 
            }
            else {
                val = p.selectSingleNode("value").getText();
            }
            params.put(key, val);
        }
        
        // Evaluators.

        
        return new SsoTargetSimple(handle, method, url, params, evaluators);
		
	}

	public String getId() {
        return this.id;
    }

    public String getHandle() {
        return this.handle;
    }

    public String getURL() {
        return this.url;
    }

    public String getMethod() {
        return this.method;
    }

    public Map getParameters() {
        return new HashMap(this.params);
    }

    public Map getEvaluators() {
        return new HashMap(this.evaluators);
    }

    /*
     * Implementation.
     */

    private SsoTargetSimple(String handle, String method,
            				String url, Map params, Map evaluators) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (method == null) {
            String msg = "Argument 'method' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (params == null) {
            String msg = "Argument 'params' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (evaluators == null) {
            String msg = "Argument 'evaluators' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // Instance Members.
        this.handle = handle;
        this.method = method;
        this.url = url;
        this.params = Collections.unmodifiableMap(params);
        this.evaluators = Collections.unmodifiableMap(evaluators);
        
        this.id = idPrefix + getNextId();
    }
    
    private static long getNextId() {
        synchronized(lock) {
            return nextId++;
        }
    }

}