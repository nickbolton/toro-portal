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

import java.sql.ResultSet;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class DeliveryCurriculumImpl implements DeliveryCurriculum {

    private String systemId;
    private String title;
    private String description;
    private String curriculumId;

    public DeliveryCurriculumImpl (String systemId,
    String curriculumId,
    String title,
    String description) {

        this.systemId     = systemId;

        this.curriculumId = curriculumId;

        this.title        = title;

        this.description  = description;

    }

    public String getDeliverySystemId() {

        return this.systemId;

    }

    public String getTitle() {

        return this.title;

    }

    public String getDescription() {

        return this.description;

    }

    public String getCurriculumId() {

        return this.curriculumId;

    }

    // all PortalObjects implement this

    public String toXML() {

        StringBuffer xml = new StringBuffer();

        xml.append("<deliverycurriculum systemid=\"");

        xml.append(systemId);

        xml.append("\" curriculumid=\"");

        xml.append(curriculumId);

        xml.append("\">");

        /* Title */

        xml.append("<title>");

        xml.append("<![CDATA[");

        xml.append(title);

        xml.append("]]>");

        xml.append("</title>");

        /* description */

        xml.append("<description>");

        xml.append("<![CDATA[");

        xml.append(description);

        xml.append("]]>");

        xml.append("</description>");

        xml.append("</deliverycurriculum>");

        return xml.toString();

    }

    public Node toNode(Document doc) throws Exception {

        throw new Exception("Method not implemented!");

    }
    
    public boolean equals (Object o) {
    	if (!(o instanceof DeliveryCurriculum)) {
    		System.out.println("o is not an instance of DeliveryCurriculum");
    		return false;
    	} else {
    		DeliveryCurriculum dc = (DeliveryCurriculum) o;
    		// Only compares curriculum Id, may need to compare more fields
    		if (this.getCurriculumId().equals(dc.getCurriculumId())) {
        		return true;
    		} else {
        		return false;   			
    		}
    	}
    }

}

