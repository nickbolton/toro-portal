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

import net.unicon.portal.channels.gradebook.AssessmentSubmission;

import java.sql.Timestamp;

import java.sql.Date;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class AssessmentSubmissionImpl extends GradebookSubmissionImpl implements AssessmentSubmission {

    private String results = null;

    public AssessmentSubmissionImpl (

    int id,

    int gradebookScoreID,

    String filename,

    int filesize,

    int submissionCount,

    String comment,

    Timestamp timestamp,

    String contentType) {

        super(id,

        gradebookScoreID,

        filename,

        filesize,

        submissionCount,

        comment,

        timestamp,

        contentType);

        this.results = null;

    }

    public AssessmentSubmissionImpl (

    int id,

    int gradebookScoreID,

    String filename,

    int filesize,

    int submissionCount,

    String comment,

    Timestamp timestamp,

    String contentType,

    String results) {

        super(id,

        gradebookScoreID,

        filename,

        filesize,

        submissionCount,

        comment,

        timestamp,

        contentType);

        this.results = results;

    }

    public String getResults() {

        return this.results;

    }

    public void setResults(String assessmentResults) {

        this.results = assessmentResults;

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

        xml.append("<submission");

        /* id */

        xml.append(" id=\"");

        xml.append("" + super.id);

        xml.append("\"");

        xml.append(" gradebook-scoreid=\"");

        xml.append("" + super.gradebookScoreID);

        xml.append("\"");

        xml.append(" filename=\"");

        xml.append("" + super.filename);

        xml.append("\"");

        xml.append(" size=\"");

        xml.append("" + super.filesize);

        xml.append("\"");

        xml.append(" submission-count=\"");

        xml.append("" + super.submissionCount);

        xml.append("\"");

        xml.append(" content-type=\"");

        xml.append("" + super.contentType);

        xml.append("\"");

        xml.append(" date=\"");

        xml.append("" + super.date);

        xml.append("\">");

        xml.append("<comment>");

        xml.append("" + super.comment);

        xml.append("</comment>");

        if (results != null) {

            xml.append(this.results);

        }

        xml.append("</submission>");

        return xml.toString();

    }

}

