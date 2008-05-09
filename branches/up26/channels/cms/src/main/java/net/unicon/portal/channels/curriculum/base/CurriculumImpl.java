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

package net.unicon.portal.channels.curriculum.base;

import net.unicon.portal.channels.curriculum.Curriculum;

import net.unicon.portal.common.PortalObject;

import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class CurriculumImpl implements Curriculum {

    // Instance Members.
    private String id;
    private String name;
    private String description;
    private int offeringID;
    private String uri;
    private String type;
    private String contentType;
    private String theme;
    private String style;

    public CurriculumImpl (String id, String name, String description, int offeringID, String type, String url) {

        // Instance Members.
        this.id = id;
        this.name = name;
        this.description = description;
        this.offeringID  = offeringID;
        this.type        = type;
        this.uri         = url;
        this.contentType = null;
        this.theme = null;
        this.style = null;

    }

    public CurriculumImpl(String id, String name, String description,
                        int offeringID, String type, String url,
                        String contentType, String theme, String style) {

        // Instance Members.
        this.id = id;
        this.name = name;
        this.description = description;
        this.offeringID = offeringID;
        this.type = type;
        this.uri = url;
        this.contentType = contentType;
        this.theme = theme;
        this.style = style;

    }

    public String getId() {

        return this.id;

    }

    public String getName() {

        return this.name;

    }

    public String getDescription() {

        return this.description;

    }

    public int getOfferingID() {

        return this.offeringID;

    }

    public String getReference() {

        return this.uri;

    }

    public String getType() {

        return this.type;

    }

    public String getContentType() {

        return this.contentType;

    }

    public String getTheme() {

        return this.theme;

    }

    public String getStyle() {

        return this.style;

    }

    public void setReference(String reference) {

        this.uri = reference;

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

        xml.append("<curriculum id=\"");

        xml.append(id);

        xml.append("\"");

        xml.append(" type=\"");

        xml.append(type);

        xml.append("\"");

        xml.append(" reference=\"");

        xml.append(uri);

        xml.append("\">");

        /* Title or name */

        xml.append("<title>");

        xml.append("<![CDATA[");

        xml.append(name);

        xml.append("]]>");

        xml.append("</title>");

        /* description */

        xml.append("<description>");

        xml.append("<![CDATA[");

        xml.append(description);

        xml.append("]]>");

        xml.append("</description>");

        xml.append("</curriculum>");

        return xml.toString();

    }

}

