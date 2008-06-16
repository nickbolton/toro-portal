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


import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.Assessment;

public class AssessmentImpl implements Assessment {

    protected String id          = null;
    protected String name        = null;
    protected String language    = null;
    protected String description = null;
    protected List assForms      = null;
    //protected Map attributes     = null;

    /*public AssessmentImpl (
    String id,
    String name,
    String language,
    String description,
    List assForms,
    Map attributes) {

        this.id = id;
        this.name        = name;
        this.language    = language;
        this.description = description;
        this.assForms    = assForms;
        this.attributes  = attributes;
    }*/

    public AssessmentImpl(
                String id,
                String name,
                String language,
                String description,
                List assForms) {

        this.id          = id != null?id.trim():"";
        this.name        = name != null?name.trim():"";
        this.language    = language != null?language.trim():"";
        this.description = description != null?description.trim():"";
        this.assForms    = assForms != null?new ArrayList(assForms):new ArrayList();
    }

    /**
     * returns the assessment id
     * @return <code>String</code> of the assessment id
     */

    public String getAssessmentID() {

        return this.id;

    }

    public String getName() {

        return this.name;

    }

    /**
     * returns the langauge of the assesment
     * @return <code>String</code> language in "en", "fr", "es", etc.
     */

    public String getLanguage() {

        return this.language;

    }

    /**
     * returns the description of the assessment
     * @return <code>String</code> the description
     */

    public String getDescription() {

        return this.description;

    }

    /*

     * returns the List of activation of subforms

     * @return <code>List</code> of AssessmentForm Objects

     */

    public List getAssessmentForms() {

        return this.assForms;

    }

    /*

     * set the List of activation of subforms

     * @param <code>List</code> of AssessmentForm Objects

     */

    public void setAssessmentForms(List asslist) {

        List assessmentlist = new ArrayList(asslist);

        this.assForms = assessmentlist;

    }

    /**
     * set the attributes of the activation
     * @param <code>Map<code> of the attributes of the activation
     */

    /*public void setAttributes(Map attributes) {

        this.attributes = new HashMap(attributes);

    }*/

    /**
     * returns the attributes of the activation
     * @return <code>Map<code> of the attributes of the activation
     */

    /*public Map getAttributes() {

        return this.attributes;

    }*/

    public String toXML() {

        StringBuffer xml = new StringBuffer();
        xml.append("<assessment");

        /* Assessment id */

        xml.append(" id=\"");
        xml.append(esc(id));
        xml.append("\"");

        /* language */
        xml.append(" language=\"");
        xml.append(esc(language));
        xml.append("\"");
        xml.append(">");

        /* name */

        xml.append("<title>");
        xml.append(esc(name));
        xml.append("</title>");

        /* decription */
        xml.append("<description>");
        xml.append(esc(description));
        xml.append("</description>");

        /* attributes */

        /*if (attributes != null) {

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
        }*/

        if (assForms != null) {
            for (int ix = 0; ix < assForms.size(); ++ix) {
                xml.append(((XMLAbleEntity) assForms.get(ix)).toXML());
            }
        }
        xml.append("</assessment>");
        return xml.toString();
    }

    // This task should be implemented somewhere else ideally.
    private static String esc(String s) {

        // Assertions.
        if (s == null) {
            String msg = "Argument 's [String]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        StringBuffer rslt = new StringBuffer();
        for (int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    rslt.append("&lt;");
                    break;
                case '>':
                    rslt.append("&gt;");
                    break;
                case '"':
                    rslt.append("&quot;");
                    break;
                case '\'':
                    rslt.append("&apos;");
                    break;
                case '&':
                    rslt.append("&amp;");
                    break;
                default:
                    rslt.append(c);
                    break;
/*
                    if (( ch >= ' ' && ch <= 0x7E && ch != 0xF7 ) ||
                        ch == '\n' || ch == '\r' || ch == '\t' )
                        rslt.append (ch);
                    else if (ch<' ')
                        rslt.append(' ');
                    else
                        rslt.append ("&#" + Integer.toString (ch) + ";");
*/
            }
        }

        return rslt.toString();

    }

}

