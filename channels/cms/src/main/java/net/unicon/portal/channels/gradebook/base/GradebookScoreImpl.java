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
import java.util.ArrayList;

import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.gradebook.GradebookScore;
import net.unicon.portal.channels.gradebook.GradebookSubmission;
import net.unicon.portal.channels.gradebook.GradebookFeedback;
import net.unicon.portal.common.PortalObject;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class GradebookScoreImpl implements GradebookScore {

    private int id;
    private int gradebookItemID;
    private String username;
    private int score;
    private int origScore;
    private User user;
    private boolean isHidden;
    private GradebookSubmission submission;
    private GradebookFeedback feedback;

    public GradebookScoreImpl (
    int id,
    int gradebookItemID,
    String username,
    int score,
    int origScore) {
        this(id, gradebookItemID, username, score, origScore, null);
    }

    public GradebookScoreImpl (
    int id,
    int gradebookItemID,
    String username,
    int score,
    int origScore, 
    User user) {
        this.id                 = id;
        this.gradebookItemID    = gradebookItemID;
        this.username           = username;
        this.score              = score;
        this.origScore          = origScore;
        this.user               = user;
        this.submission         = null;
        this.feedback           = null;
    }

    public int getID() {

        return this.id;

    }

    public int getGradebookItemID() {

        return this.gradebookItemID;

    }

    public String getUsername() {

        return this.username;

    }

    public int getScore() {

        return this.score;

    }

    public User getUser() {

        return user;

    }

    public void setUser(User user) {

        this.user = user;

    }

    public void setGradebookSubmission(GradebookSubmission s) {

        this.submission = s;

    }

    public GradebookSubmission getGradebookSubmission() {

        return submission;

    }

    public void setFeedback(GradebookFeedback f) {

        this.feedback = f;

    }

    public GradebookFeedback getFeedback() {

        return feedback;

    }

    public void setHidden(boolean isHidden) {

        this.isHidden = isHidden;

    }

    public int getOriginalScore() {
        return origScore;
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

        xml.append("<gradebook-score");

        /* id */

        xml.append(" id=\"");
        xml.append("" + id);
        xml.append("\"");

        xml.append(" gradebook-itemid=\"");
        xml.append("" + gradebookItemID);
        xml.append("\"");

        xml.append(" username=\"");
        xml.append("" + username);
        xml.append("\"");

        xml.append(" score=\"");
        xml.append("" + score);
        xml.append("\"");
        
        xml.append(" original_score=\"");
        xml.append("" + origScore);
        xml.append("\"");

        xml.append(" hidden=\"");
        xml.append("" + isHidden);
        xml.append("\">");

        if (user != null) {
            xml.append(user.toXML());
        }

        if (submission != null) {
            xml.append(submission.toXML());
        }

        if (feedback != null) {
            xml.append(feedback.toXML());
        }

        xml.append("</gradebook-score>");
        return xml.toString();
    }
}

