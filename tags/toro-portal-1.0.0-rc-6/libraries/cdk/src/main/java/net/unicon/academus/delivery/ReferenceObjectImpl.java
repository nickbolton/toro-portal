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

package net.unicon.academus.delivery;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class ReferenceObjectImpl implements ReferenceObject {

    protected String url     = null;
    protected String title   = null;
    protected Map parameters = null;
    protected String target  = DEFAULT;
    
    public ReferenceObjectImpl(
    String url,
    String title,
    final String target) {
        this.url    = url;
        this.title  = title;
        this.target = target;
    }

    public String getURL() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public Map getParameters() {
        return parameters != null ? this.parameters : new HashMap();
    }
    
    public void setParameters(Map parms) {
        this.parameters = parms;
    }

    public String getTarget() {
        return target != null ? this.target : ReferenceObject.NEW;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();

        xml.append("<reference-link");
        xml.append(" target=\"");
        xml.append(getTarget());
        xml.append("\"");
        xml.append(">");
        
        /* URL */
        xml.append("<url><![CDATA[");
        if (url != null) {
            xml.append(url);
        }
        xml.append("]]></url>");

        /* title */
        xml.append("<title><![CDATA[");
        if (title != null) {
            xml.append(title);
        }
        xml.append("]]></title>");
        
        /* parameters */
        if (parameters != null) {
            xml.append("<parameters>");
            Iterator iterator = parameters.keySet().iterator();
            String key = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                xml.append("<parameter");
                xml.append(" name=\"");
                xml.append(key);
                xml.append("\"");
                xml.append(">");

                xml.append("<value><![CDATA[");
                xml.append((String) parameters.get(key));
                xml.append("]]></value>");
                xml.append("</parameter>");
            }
            xml.append("</parameters>");
        }

        xml.append("</reference-link>");
        return xml.toString();
    }
}

