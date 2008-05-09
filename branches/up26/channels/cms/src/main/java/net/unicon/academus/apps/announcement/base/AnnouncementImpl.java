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
package net.unicon.academus.apps.announcement.base;

import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;

import net.unicon.academus.apps.announcement.Announcement;
import net.unicon.sdk.util.XmlUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

public class AnnouncementImpl implements Announcement {
    private String date;
    private int id;
    private String groupId;
    private String instructorID;
    private String messageBody;

    public AnnouncementImpl (int id,
			     String groupId,
			     String body,
			     String date,
			     String instructorID) {
        this.id = id;
	this.groupId = groupId;
        this.messageBody = body;
        this.instructorID = instructorID;
        this.date = date;
    }

    public String getDate() {
        return this.date;
    }

    /**
     * get the group id
     * @return <code>String</code> group id
     */
    public String getGroupId() {
	return this.groupId;
    }

    /**
     * get the id number of the announcement.
     * @return <code>int</code> id of the announcement
     */
    public int getId() {
        return this.id;
    }

    /**
     * get the instructors id who issued the announcement.
     * @return <code>String</code> the instructor id
     */
    public String getInstructorID() {
        return this.instructorID;
    }

    /**
     * get the body of the announcement.
     * @return <code>String</code> the announcement body
     */
    public String getMessageBody() {
        return this.messageBody;
    }

    /**
     * get the xml presentation of the xml object
     * @return <code>String</code> the xml of the object
    */
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<announcement");
        xml.append(" id=\"");
        xml.append("" + id);
        xml.append("\"");
        xml.append(" date=\"");
        xml.append(date.toString());
        xml.append("\"");
        xml.append(">");
        xml.append("<instructor");
        xml.append(" id=\"");
        xml.append(instructorID);
        xml.append("\"");
        xml.append("/>");
        xml.append("<announcement-body>");
        xml.append("<![CDATA[");
        xml.append(messageBody);
        xml.append("]]>");
        xml.append("</announcement-body>");
        xml.append("</announcement>");
        return xml.toString();
    }

    /**
     * get the Node presentation of the xml object
     * @param Document doc - this is the Document the returned Node will belong to
     * @return <code>Node</code> the xml of the object
     */
    public Node toNode(Document doc) {
        Map attrs = new HashMap();
        attrs.put("id", "" + id);
        attrs.put("date", date.toString());

	try {
	    // create <announcement> node
	    Node node = XmlUtils.addNewNode(doc,
					    doc, "announcement", attrs, null);
	    attrs.clear();
	    
	    // add <instructor> node
	    attrs.put("id", instructorID);
	    XmlUtils.addNewNode(doc, node, "instructor", attrs, null);
	    attrs.clear();
	    
	    // add <announcement-body> node
	    XmlUtils.addNewNode(doc, node, "announcement-body", null, messageBody);
	    return node;
	}
	catch (Exception e) {
	    return null;
	}
    }

    public Document toDocument() {
	String xml = this.toXML();

	// create the DOM
	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;

	try {
	    dbf = DocumentBuilderFactory.newInstance();
	    db = dbf.newDocumentBuilder();
	    return db.parse(new InputSource(new StringReader(xml)));
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
