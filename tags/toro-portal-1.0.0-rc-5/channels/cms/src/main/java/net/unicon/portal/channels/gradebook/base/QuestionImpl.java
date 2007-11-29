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

import java.util.*;

import net.unicon.portal.channels.gradebook.Question;

import net.unicon.portal.common.PortalObject;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class QuestionImpl implements Question {

    private String id    = null;

    private String title = null;

    private int order;

    private String type  = null;

    private int score     = -1;

    private int max_score = -1;

    private int min_score = -1;

    private Map attributes = null;

    public QuestionImpl(

    String id,

    String title,

    String type,

    int order,

    int max_score,

    int min_score) {

        this(id, title, type, order, max_score, min_score, -1);

        this.attributes = new HashMap();

    }

    public QuestionImpl(

    String id,

    String title,

    String type,

    int order,

    int max_score,

    int min_score,

    int score) {

        this.id        = id;

        this.title     = title;

        this.type      = type;

        this.order     = order;

        this.max_score = max_score;

        this.min_score = min_score;

        this.score     = score;

        this.attributes = new HashMap();

    }

    public QuestionImpl(

    String id,

    String title,

    String type,

    int order,

    int max_score,

    int min_score,

    int score,

    Map attributes) {

        this(id, title, type, order, max_score, min_score, score);

        this.attributes = attributes;

    }

    public String getID() {

        return this.id;

    }

    public String getTitle() {

        return this.title;

    }

    public String getType() {

        return this.type;

    }

    public int getOrder() {

        return this.order;

    }

    public int getMaxScore() {

        return this.max_score;

    }

    public int getMinScore() {

        return this.min_score;

    }

    public Map getAttributes() {

        return this.attributes;

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

        xml.append("<question");

        xml.append(" id=\"");

        xml.append(id);

        xml.append("\"");

        xml.append(" type=\"");

        xml.append(type);

        xml.append("\"");

        xml.append(" order=\"");

        xml.append("" + order);

        xml.append("\"");

        xml.append(" max_score=\"");

        xml.append("" + max_score);

        xml.append("\"");

        xml.append(" min_score=\"");

        xml.append("" + min_score);

        xml.append("\"");

        xml.append(" score=\"");

        if (score >= 0) {

            xml.append("" + score);

        } else {

            xml.append("" + 0);

        }

        xml.append("\"");

        xml.append(">");

        xml.append("<title>");

        xml.append("" + title);

        xml.append("</title>");

        /* attributes

         if (attributes != null) {

             xml.append("<attributes>");

             Iterator iterator = attributes.keySet().iterator();

             String key = null;

             while (iterator.hasNext() ) {

                 key = (String) iterator.next();

                 xml.append("<attribute");

                 xml.append(" name=\"");

                 xml.append(key);

                 xml.append("\"");

                 xml.append(">");

                 xml.append("<value><![CDATA[");

                 xml.append((String) attributes.get(key));

                 xml.append("]]></value>");

                 xml.append("</attribute>");

             }

             xml.append("</attributes>");

        }

        */

        xml.append("</question>");

        return xml.toString();

    }

}

