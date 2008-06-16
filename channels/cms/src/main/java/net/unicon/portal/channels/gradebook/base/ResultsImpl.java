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

import net.unicon.portal.channels.gradebook.Results;

import net.unicon.portal.common.PortalObject;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class ResultsImpl implements Results {

    protected String formID;

    protected String activationID;

    protected List questions = null;

    protected int score = -1;

    public ResultsImpl (

    String formID,

    List questions) {

        this.formID = formID;

        this.questions = questions;

        this.activationID = null;

    }

    public ResultsImpl (

    String formID,

    String activationID,

    List questions) {

        this.formID = formID;

        this.questions = questions;

        this.activationID = activationID;

    }

    public String getFormID() {

        return this.formID;

    }

    public void setFormID(String formID) {

        this.formID = formID;

    }

    public List getQuestions() {

        return this.questions;

    }

    public void setQuestions(List questions) {

        this.questions = questions;

    }

    public void setScore(int score) {

        this.score = score;

    }

    public int getScore() {

        return this.score;

    }

    public void setActivationID(String activationID) {

        this.activationID = activationID;

    }

    public String getActivationID() {

        return this.activationID;

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

        xml.append("<results");

        if (formID != null) {

            xml.append(" formref_id=\"");

            xml.append(formID);

            xml.append("\"");

        }

        if (activationID != null) {

            xml.append(" activationref_id=\"");

            xml.append(activationID);

            xml.append("\"");

        }

        xml.append(">");

        xml.append("<questions>");

        if (questions != null) {

            for (int ix = 0; ix < questions.size(); ++ix) {

                xml.append(((PortalObject) questions.get(ix)).toXML());

            }

        }

        xml.append("</questions>");

        xml.append("</results>");

        return xml.toString();

    }

}

