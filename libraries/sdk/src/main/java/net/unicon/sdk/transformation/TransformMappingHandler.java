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
package net.unicon.sdk.transformation;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.util.HashMap;
import java.util.Map;

/** 
 * TransformMappingHandler reads in the mappings of input and output XML types
 * to an XSLT transformation.
 */
public class TransformMappingHandler extends DefaultHandler {
    private Map transformMappings = null;

    /** constructor */
    public TransformMappingHandler() {
        transformMappings = new HashMap();
    }

    public void startElement(
                        String namespaceURI,
                        String localname,
                        String rawName,
                        Attributes atts) 
    throws SAXException {

        if (localname.equals("transformation")) {
            String inputURI = null;
            String outputURI = null;
            String xslPath = null;

            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("input-uri")) {
                    inputURI = atts.getValue(i);
                }
                if (atts.getLocalName(i).equals("output-uri")) {
                    outputURI = atts.getValue(i);
                }
                if (atts.getLocalName(i).equals("xsl")) {
                    xslPath = atts.getValue(i);
                }
            } // end for loop

            transformMappings.put(inputURI+outputURI, xslPath);
        } // end if

    } // end startElement

    /**
     * Provides a Map of transformations that have keys based on input and
     * output URIs
     * 
     * transformation mappings are found in TransformMappings.xml.
     *
     * @return Map of transformations.
     */
    public Map getTransformMappings() {
        return transformMappings;
    } // end getTransformMappings

} // end TransformMappingHandler class
