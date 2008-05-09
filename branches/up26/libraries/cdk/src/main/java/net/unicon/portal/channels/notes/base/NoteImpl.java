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
package net.unicon.portal.channels.notes.base;

import java.sql.ResultSet;

import net.unicon.portal.channels.notes.Note;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class NoteImpl implements Note {

    private String date;

    private int id;

    private int userID;

    private String messageBody;

    public NoteImpl (ResultSet rs) { }

    public NoteImpl (int id, int userID, String body, String date) {

        this.id = id;

        this.messageBody = body;

        this.userID = userID;

        this.date = date;

    }


    public String getDate() {

        return this.date;

    }

    /**
     * get the id number of the note.
     * @return <code>int</code> id of the note
     */

    public int getId() {

        return this.id;

    }

    /**
     * get the user id to which the note belongs.
     * @return <code>int</code> the user id
     */

    public int getUserID() {

        return this.userID;

    }

    /**
     * get the body of the note.
     * @return <code>String</code> the note body
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

        xml.append("" + id);

        xml.append("\"");

        xml.append(" date=\"");

        xml.append(date);

        xml.append("\"");

        xml.append(">");

        xml.append("<user");

        xml.append(" id=\"");

        xml.append("" + userID);

        xml.append("\"");

        xml.append("/>");

        xml.append("<note-body>");

        xml.append("<![CDATA[");

        xml.append(messageBody);

        xml.append("]]>");

        xml.append("</note-body>");

        xml.append("</note>");

        return xml.toString();

    }

}

