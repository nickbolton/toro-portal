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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.unicon.alchemist.EntityEncoder;

/**
 * Single Sign-On handler that uses an XML configuration, and emits an XML
 * structure.
 *
 * See gateway-portlet.xml &lt;sso-entry&gt; elements for configuration
 * details.
 */
public class SsoHandlerXML extends SsoHandler {

	public SsoEntry[] parse(InputStream in) throws IOException {
        SsoEntry[] rslt = null;

        try {
            Element e = new SAXReader()
                .read(in)
                .getRootElement();
            // Resolve copy-of and imports as necessary
            e = ConfigHelper.handle(e);

            List list = e.elements("sso-entry");
            rslt = new SsoEntry[list.size()];

            Iterator it = list.iterator();
            int i = 0;
            while (it.hasNext())
                rslt[i++] = SsoEntry.parse((Element)it.next());

        } catch (Exception ex) {
            throw new RuntimeException("Unable to parse the given input stream for sso-entry elements", ex);
        }

        if (rslt == null)
            rslt = new SsoEntry[0];

        return rslt;
    }
	
    protected String evaluate(SsoEntry entry, Map urls,
        Map resolvedParams, boolean needsAuth, String sequenceId,
        boolean exposeCredentials) {
        StringBuffer otpt = new StringBuffer();

        otpt.append("<entry-point ");

        // Handle.
        otpt.append("id=\"");
        otpt.append(EntityEncoder.encodeEntities(entry.getHandle()));
        otpt.append("\" ");

        // Credentials support
        otpt.append("credentials=\"")
            .append(( needsAuth ? "needed" :
                        ( entry.getAuthentication() != null &&
                    entry.getAuthentication().supportsAuthenticate() ? "stored" : "none" )))
            .append("\" ");

        // UI Class
        otpt.append("class=\"");
        otpt.append(EntityEncoder.encodeEntities(entry.getUIClass()));
        otpt.append("\">");

        // Label.
        otpt.append("<label>");
        otpt.append(EntityEncoder.encodeEntities(entry.getLabel()));
        otpt.append("</label>");

        // Description.
        otpt.append("<description>");
        otpt.append(EntityEncoder.encodeEntities(entry.getDescription()));
        otpt.append("</description>");

        if (!needsAuth) {
            // Window.
            otpt.append(entry.getWindow());

            // sequences
            SsoSequence[] sequences = entry.getSequences();
            
            for(int i = 0; i < sequences.length; i++){
                
                otpt.append("<sequence type=\"")
                		.append(sequences[i].getType())
                		.append("\"");
                
                if(sequences[i].getType().equals(sequenceId)){
                    otpt.append(" current=\"true\"");
                }
                
                otpt.append(">");
                
                // get button information if any exists
                otpt.append(sequences[i].getButtonXml());
                
                // get all the targets in the given sequence
                String[] targets = sequences[i].getTargetIds();
	            
                for(int j = 0; j < targets.length; j++){
                    
                    SsoTarget target = entry.getTarget(targets[j]);
                    
	                // ActionUrl.
		            otpt.append("<send action=\"");
		            otpt.append(EntityEncoder.encodeEntities((String)urls.get(targets[j])));
		            otpt.append("\" ");
		
		            // id.
		            otpt.append("id=\"");
		            otpt.append(target.getId());
		            otpt.append("\" ");
                    
		            // Method.
		            otpt.append("method=\"");
		            otpt.append(EntityEncoder.encodeEntities(target.getMethod()));
		            otpt.append("\">");
		
		            // Params.
		            Iterator it = ((Map)resolvedParams.get(targets[j])).entrySet().iterator();
		            while (it.hasNext()) {
		                Map.Entry e = (Map.Entry)it.next();
		                otpt.append("<parameter name=\"")
		                    .append(EntityEncoder.encodeEntities((String)e.getKey()))
		                    .append("\"><value>");
                        if (exposeCredentials) {
		                    otpt.append(EntityEncoder.encodeEntities((String)e.getValue()));
                        }
		                otpt.append("</value></parameter>");
		            }
		
		            otpt.append("</send>");
                }
                otpt.append("</sequence>");
            }
        }

        otpt.append("</entry-point>");

        return otpt.toString();
    }

}
