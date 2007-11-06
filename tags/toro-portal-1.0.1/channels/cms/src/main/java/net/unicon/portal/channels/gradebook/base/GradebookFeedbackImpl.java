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

package net.unicon.portal.channels.gradebook.base;

import java.util.List;

import net.unicon.portal.channels.gradebook.GradebookFeedback;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

import java.sql.Date;

import java.sql.Timestamp;

import java.sql.Time;

public class GradebookFeedbackImpl implements GradebookFeedback {

    private int id;

    private int gradebookScoreID;

    private String filename;

    private int filesize;

    private String comment;

    private Date date;

    private Time time;

    private Timestamp timestamp;

    private String contentType;

    public int getID()                  { return id; }

    public int getGradebookScoreID()    { return gradebookScoreID; }

    public String getFilename()         { return filename; }

    public int getFileSize()            { return filesize; }

    public String getComment()          { return comment; }

    public Date getDate()               { return date; }

    public Time getTime()               { return time; }
    
    public Timestamp getTimestamp()      { return timestamp; }

    public String getContentType()      { return contentType; }

    public void setFilename(String name)        { filename = name; }

    public void setFileSize(int size)           { filesize = size; }

    public void setComment(String comment)      { this.comment = comment; }

    public void setDate(Date date)              { this.date = date; }

    public void setTime(Time time)              { this.time = time; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }   

    public void setContentType(String ct)       { this.contentType = ct; }

    public GradebookFeedbackImpl(

    int id,

    int gradebookScoreID,

    String filename,

    int filesize,

    String comment,

    Timestamp timestamp,

    String contentType) {

        this.id = id;

        this.gradebookScoreID = gradebookScoreID;

        this.filename = filename;

        this.filesize = filesize;

        this.comment  = comment;
        
        if (timestamp != null) {
            this.date     = new Date(timestamp.getTime());
            this.timestamp = timestamp;
            this.time = new Time(timestamp.getTime());
        }

        this.contentType = contentType;

    }

    /**
     * get the Node presentation of the xml object
     * @param Document doc - this is the Document the returned Node will belong to
     * @return <code>Node</code> the xml of the object
     */

    public Node toNode(Document doc) throws Exception {

        throw new Exception("Method not implemented!");

    }

    public String toXML() {

        StringBuffer xml = new StringBuffer();

        xml.append("<feedback");

        /* id */

        xml.append(" id=\"");

        xml.append("" + id);

        xml.append("\"");

        xml.append(" content-type=\"");

        xml.append("" + contentType);

        xml.append("\"");

        xml.append(" gradebook-scoreid=\"");

        xml.append("" + gradebookScoreID);

        xml.append("\"");

        xml.append(" filename=\"");

        xml.append("" + filename);

        xml.append("\"");

        xml.append(" size=\"");

        xml.append("" + filesize);

        xml.append("\"");

        xml.append(" date=\"");

        xml.append("" + date);

        xml.append("\"");

        xml.append(" time=\"");

        xml.append("" + time);

        xml.append("\">");
        xml.append("<comment>");

        xml.append("" + comment);

        xml.append("</comment>");

        xml.append("</feedback>");

        return xml.toString();

    }

}
