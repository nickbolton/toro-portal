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

package net.unicon.portal.channels.notepad.base;

import net.unicon.portal.channels.notepad.OfferingNote;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Offering note where users can store notes per offering. */

public class OfferingNoteImpl implements OfferingNote {
    private String date;
    private int id;
    private String title;
    private int instructorID;
    private String instructorName;
    private String messageBody;
    String userName = "";
    int offeringId = 0;

    /** Constructor */
    public OfferingNoteImpl (int id,
			     String title,
			     String body,
			     String date,
			     String name,
			     int offerId) {

	this.id = id;
	this.title = title;
	this.messageBody = body;
	this.date = date;
        this.userName = name;
        this.offeringId = offerId;
    }

    /** Constructor */

    public OfferingNoteImpl (int id,
			     String title,
			     String body,
			     String date,
			     String name,
			     int offerId,
			     int instructorID,
			     String instructorName) {

        this(id, title, body, date, name, offerId);
	this.instructorID = instructorID;
	this.instructorName = instructorName;
    }

    /**
     * Get the user name of the note.
     * @return <code>String</code> user name of this note
     */

    public String getUsername() {

        return userName;

    }

    /**
     * get the instructors id who issued the announcement.
     * @return <code>int</code> the instructor id
     */

    public int getOfferingId() {

        return offeringId;

    }

    /**
     * get the date the annoucement was issued.
     * @return <code>String</code> the date it was issued
     */
    public String getDate() {
        return this.date;
    }

    /**
     * get the id number of the announcement.
     * @return <code>int</code> id of the announcement
     */
    public int getId() {
        return this.id;
    }

    /**
     * get the title od the announcement.
     * @return <code>String</code> title of the announcement
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * get the instructors id who issued the announcement.
     * @return <code>int</code> the instructor id
     */
    public int getInstructorID() {
        return this.instructorID;
    }

    /**
     * get the instructors name who issued the announcement.
     * @return <code>String</code> the instructor name
     */
    public String getInstructorName() {
        return this.instructorName;
    }

    /**
     * get the body of the announcement.
     * @return <code>String</code> the announcement body
     */
    public String getMessageBody() {
        return this.messageBody;
    }


    /**
     * get the Node presentation of the xml object
     * @param Document doc - this is the Document the returned Node will belong to
     * @return <code>Node</code> the xml of the object
     */

    public Node toNode(Document doc) throws Exception {

        throw new Exception("Method not implemented!");

    }

    /**
     * get the xml presentation of the xml object
     * @return <code>String</code> the xml of the object
     */

    public String toXML() {

        StringBuffer xml = new StringBuffer();

        xml.append("<note");

        xml.append(" id=\"");

        xml.append("" + getId());

        xml.append("\"");

        xml.append(" date=\"");

        xml.append(getDate());

        xml.append("\"");

        xml.append(">");

        xml.append("<instructor");

        xml.append(" id=\"");

        xml.append("" + getInstructorID());

        xml.append("\"");

        xml.append(">");

        xml.append(getInstructorName());

        xml.append("</instructor>");

        xml.append("<note-title>");
        xml.append("<![CDATA[");
        xml.append(getTitle());
        xml.append("]]>");
        xml.append("</note-title>");

        xml.append("<note-body>");
        xml.append("<![CDATA[");
        xml.append(getMessageBody());
        xml.append("]]>");
        xml.append("</note-body>");

        xml.append("<user-id>");

        xml.append(userName);

        xml.append("</user-id>");

        xml.append("<offering-id>");

        xml.append("" + offeringId);

        xml.append("</offering-id>");

        xml.append("</note>");

        return xml.toString();

    }



}

