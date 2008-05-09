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

package net.unicon.portal.channels.classfolder.base;

import java.util.StringTokenizer;

import java.io.StringReader;

import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.Node;

import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Node;

import org.w3c.dom.Document;



import net.unicon.academus.common.DOMAbleEntity;
import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.InvalidXMLException;
import net.unicon.portal.channels.classfolder.ClassFolders;

/**
 * Class folders are part of related resources (and are the only related resource
 * as of the creation of this object. ClassFolders are stored as XML in the database as they are a natural tree structure.
 */
public class ClassFoldersImpl implements ClassFolders, DOMAbleEntity {
    protected String xmlString  = null;
    protected String offeringID = null;
    protected Document dom      = null;

    protected void init(String xml, String offeringID, Boolean validateMe) throws Exception {

        this.offeringID = offeringID;
        if (validateMe.booleanValue()) {
            // XXX validate the xml, throw exception if invalid
        }

        // if the XML is null or the XML is length 0, we have no such class folder
        // in the database so return an empty XML structure for it.
        if (xml == null || xml.length() == 0) {
            StringBuffer xmlSB = new StringBuffer();
            xmlSB.append("<related-resources offering_id = \"");
            xmlSB.append(offeringID);
            xmlSB.append("\" ><class-folders /></related-resources>");
            xmlString = xmlSB.toString();
        }
        else {
            xmlString = xml;
        }
    }

    // Constructor is protected as only subclasses or fectory can create

    protected ClassFoldersImpl(String xml, String offeringID) throws InvalidXMLException {

        // check a property to see if validation should be on or off

        try {
	    init(xml, offeringID, Boolean.TRUE);
        }
        catch (RuntimeException re) {
            try {
                // only get this because prop is missing, assume false
                init(xml, offeringID, Boolean.FALSE);
            }
            catch (Exception exc) {
                throw new InvalidXMLException(exc);
            }
        }
        catch (Exception exc2) {
            throw new InvalidXMLException(exc2);
        }
    }

    protected ClassFoldersImpl(String xml, String offeringID, Boolean validateMe) throws InvalidXMLException {

        try {

            init(xml, offeringID, validateMe);

        }

        catch (Exception exc) {

            throw new InvalidXMLException(exc);

        }

    }

    /**
     * Navigational. Return the offering ID so you can go to the service if you want it
     * @return <code>String</code> representing the offering id taken from the XML.
     */

    public String getOfferingID() {

        if (offeringID == null) {

            // the offering ID is the only attribute on the root node of the DOM tree

            if (dom != null) {

                Element root = dom.getDocumentElement();

                offeringID = root.getAttribute("offering_id");

            }

            else { // get it through string parsing

                int indexOf1, indexOf2;

                if ((indexOf1 = xmlString.indexOf("offering_id")) >= 0 &&

                (indexOf2 = xmlString.indexOf('>', indexOf1)) >= 0) {

                    StringTokenizer st = new StringTokenizer(xmlString.substring(indexOf1, indexOf2), "\"");

                    // 2nd token

                    st.nextToken();

                    offeringID = st.nextToken();

                }

            }

        }

        return offeringID;

    }

    /**
     * get the Node presentation of the xml object
     * @param Document doc - this is the Document the returned Node will belong to
     * @return <code>Node</code> the xml of the object
     */

    public Node toNode(Document doc) {
	return null;
        //throw new Exception("Method not implemented!");

    }

    /**
     * get the xml presentation of the xml object
     * @return <code>String</code> the xml of the object
     */

    public String toXML() {

        return xmlString;

    }

    public Document toDocument() {

        if (dom == null) {

            // create the DOM

            DocumentBuilderFactory dbf = null;

            DocumentBuilder db = null;

            try {

                dbf = DocumentBuilderFactory.newInstance();

                db = dbf.newDocumentBuilder();

                dom = db.parse(new InputSource(new StringReader(xmlString)));

            }

            catch (Exception e) {

                e.printStackTrace();

                dom = null;

            }

        }

        return dom;

    }

}

