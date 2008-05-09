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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.AssessmentForm;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class AssessmentFormImpl implements AssessmentForm {

    protected String id          = null;
    protected String name        = null;
    protected String language    = null;
    protected String description = null;
    protected List   questions   = null;
    protected Map    attributes  = null;

    public AssessmentFormImpl(

    String id,

    String name,

    String description,

    List questions,

    Map  attributes) {

        this.id = id;

        this.name = name;

        this.description = description;

        this.questions   = questions;

        this.attributes  = attributes;

    }

    public AssessmentFormImpl (

    String id,

    String name,

    String description) {

        this(id, name, description, null, null);

    }

    /**
     * returns the assessment form id
     * @return <code>String</code> of the assessment id
     */

    public String getId() {

        return this.id;

    }

    /**
     * returns the langauge of the assesment
     * @return <code>String</code> language in "en", "fr", "es", etc.
     */

    public String getLanguage() {

        return this.language;

    }

    /**
     * returns the name of the assesment
     * @return <code>String</code> name or title.
     */

    public String getName() {

        return this.name;

    }

    /**
     * returns the description of the assessment
     * @return <code>String</code> the description
     */

    public String getDescription() {

        return this.description;

    }

    /**
     * return the the questions for the assessment
     * @return <code>List</code> of assessment questions
     */

    public List getQuestions() {

        return this.questions;

    }

    /**
     * set the the questions for the assessment
     * @param <code>List</code> of assessment questions
     */

    public void setQuestions(List questions) {

        this.questions = questions;

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

        xml.append("<form");

        /* id */

        xml.append(" id=\"");

        xml.append(id);

        xml.append("\"");

        /* language */

        xml.append(" language=\"");

        xml.append(language);

        xml.append("\"");

        xml.append(">");

        /* title */

        xml.append("<title>");

        xml.append("</title>");

        /* decription */

        xml.append("<description>");

        xml.append(description);

        xml.append("</description>");

        /* attributes */

        if (attributes != null) {

            xml.append("<attributes>");

            Iterator iterator = attributes.keySet().iterator();

            String key = null;

            while (iterator.hasNext()) {

                key = (String) iterator.next();

                xml.append("<attribute");

                xml.append(" name=\"");

                xml.append(key);

                xml.append("\"");

                xml.append(">");

                xml.append("<value>");

                xml.append((String) attributes.get(key));

                xml.append("</value>");

                xml.append("</attribute>");

            }

            xml.append("</attributes>");

        }

        /* assessment questions */

        xml.append("<questions>");

        if (questions != null) {

            for (int ix = 0; ix < questions.size(); ++ix) {

                xml.append(((XMLAbleEntity) questions.get(ix)).toXML());

            }

        }

        xml.append("</questions>");

        xml.append("</form>");

        return xml.toString();

    }

}

