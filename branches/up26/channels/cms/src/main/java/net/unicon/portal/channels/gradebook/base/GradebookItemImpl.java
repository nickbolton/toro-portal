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

import java.util.Collection;

import java.util.ArrayList;

import java.util.Iterator;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.portal.common.service.activation.Activation;
//import net.unicon.academus.apps.common.activation.Activation;

import net.unicon.portal.channels.gradebook.GradebookItem;

import net.unicon.portal.channels.gradebook.GradebookScore;


import org.w3c.dom.Node;

import org.w3c.dom.Document;

public class GradebookItemImpl implements GradebookItem {

    private int id;

    private int offeringID;

    private int position;

    private int weight;

    private int maxScore;

    private int minScore;

    private int mean;

    private int median;

    private int type;

    private String name;

    private String association;

    private String feedback;

    private List gradebookScores;

    private List portalObject = new ArrayList();

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    int max,

    int min,

    int mean,

    int median,

    String name,

    String feedback,

    String  association,

    List gradebookScores) {

        this.id        = id;

        this.offeringID   = offeringID;

        this.position  = position;

        this.weight    = weight;

        this.maxScore  = max;

        this.minScore  = min;

        this.mean      = mean;

        this.median    = median;

        this.type      = type;

        this.name      = name;

        this.feedback  = feedback;

        this.association = association;

        this.gradebookScores   = gradebookScores;

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    String name) {

        this(id, offeringID, position, weight, type, -1, -1,

        -1, -1, name, null, null, new ArrayList());

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    String name,

    String feedback,

    String association) {

        this(id, offeringID, position, weight, type, -1, -1,

        -1, -1, name, feedback, association, new ArrayList());

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    int mean,

    int median,

    String name,

    List gradebookScores) {

        this(id, offeringID, position, weight, type, -1, -1,

        mean, median, name, null, null, gradebookScores);

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    int mean,

    int median,

    String name,

    String feedback,

    String association,

    List gradebookScores) {

        this(id, offeringID, position, weight, type, -1, -1,

        mean, median, name, feedback, association, gradebookScores);

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    int max,

    int min,

    String name) {

        this(id, offeringID, position, weight, type, max, min,

        -1, -1, name, null, null, new ArrayList());

    }

    public GradebookItemImpl (

    int id,

    int offeringID,

    int position,

    int weight,

    int type,

    int max,

    int min,

    String name,

    String feedback,

    String association) {

        this(id, offeringID, position, weight, type, max, min,

        -1, -1, name, null, null, new ArrayList());

    }

    /**
     * Get id of the gradebook item
     * @return <code>int</code> id of the course version
     */

    public int getId() {

        return this.id;

    }

    /**
     * Get id of the class
     * @return <code>int</code> id of the class
     */

    public int getOfferingId() {

        return this.offeringID;

    }

    /**
     * Get position of the gradebook item
     * @return <code>int</code> the position number (index starts at 0)
     */

    public int getPosition() {

        return this.position;

    }

    /**
     * Get the weight of the item for the gradebook
     * @return <code>int</code> the weight of the item
     */

    public int getWeight() {

        return this.weight;

    }

    /**
     * Get the maximum score for the gradebook item
     * @return <code>int</code> the maximum score
     */

    public int maximumScore() {

        return this.maxScore;

    }

    /**
     * Get the minimum score for the gradebook item
     * @return <code>int</code> the minimum score
     */

    public int minimumScore() {

        return this.minScore;

    }

    /**
     * get the type of gradebookitem
     * @return <code>int</code> the type
     */

    public int getType() {

        return this.type;

    }

    /**
     * Get the gradebook item name or title
     * @return <code>String</code> the title or name of the gradebook item
     */

    public String getName() {

        return this.name;

    }

    public List getGradebookScores() {

        return gradebookScores;

    }

    public void setGradebookScores(List gbScores) {

        this.gradebookScores = gbScores;

    }

    public void addGradebookScore(GradebookScore score) {

        gradebookScores.add(score);

    }

    public void addXMLAbleEntity(XMLAbleEntity obj) {

        portalObject.add(obj);

    }

    public void addAllXMLAbleEntities(Collection collection) {

        portalObject.addAll(collection);

    }

    /**
     * Get the gradebook item association string.
     * @return <code>Stirng</code> this value is could be a file, url, or delivery id
     */

    public String getAssociation() {

        return this.association;

    }

    /**
     * Display feedback
     * @return <code>boolean</code> true of false
     */

    public String displayFeedback() {

        return this.feedback;

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

        xml.append("<gradebook-item");

        /* id */

        xml.append(" id=\"");

        xml.append("" + id);

        xml.append("\"");

        /* offeringID */

        xml.append(" offeringID=\"");

        xml.append("" + offeringID);

        xml.append("\"");

        /* type */

        xml.append(" type=\"");

        xml.append("" + type);

        xml.append("\"");

        /* weight */

        xml.append(" weight=\"");

        xml.append("" + weight);

        xml.append("\"");

        /* position */

        xml.append(" position=\"");

        xml.append("" + position);

        xml.append("\"");

        /* feedback */

        xml.append(" feedback=\"");

        xml.append(feedback);

        xml.append("\"");

        /* association */

        xml.append(" association=\"");

        xml.append(association);

        xml.append("\"");

        /* mean */

        if (mean >= 0) {

            xml.append(" mean=\"");

            xml.append("" + mean);

            xml.append("\"");

        }

        /* median */

        if (median >= 0) {

            xml.append(" median=\"");

            xml.append("" + median);

            xml.append("\"");

        }

        /* minimum score */

        if (minScore >= 0) {

            xml.append(" min_score=\"");

            xml.append("" + minScore);

            xml.append("\"");

        }

        /* maximum score */

        if (maxScore >= 0) {

            xml.append(" max_score=\"");

            xml.append("" + maxScore);

            xml.append("\"");

        }

        xml.append(">");

        /* name */

        xml.append("<title>");

        xml.append(name);

        xml.append("</title>");

        /* portalObjects */

        for (int ix = 0; ix < portalObject.size(); ++ix) {

            xml.append(((XMLAbleEntity) portalObject.get(ix)).toXML());

        }

        /* building objects in the gradebook item */

        for (int ix = 0; ix < gradebookScores.size(); ix++) {

            xml.append(((XMLAbleEntity) gradebookScores.get(ix)).toXML());

        }

        xml.append("</gradebook-item>");

        return xml.toString();

    }

    public List getActivations() {

        List activations = new ArrayList();

        Iterator itr = portalObject.iterator();

        while (itr.hasNext()) {

            Object obj = itr.next();

            if (obj instanceof Activation) {

                activations.add(obj);

            }

        }

        return activations;

    }

}

