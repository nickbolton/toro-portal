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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * OutputDeclHandler extracts the dtd/schema declaration from the xml document.
 */
public class OutputDeclHandler implements EntityResolver {

    private String availableId = null; 
    private String systemId    = null; 
    private String publicId    = null; 
    private boolean hasURI     = false;

    public InputSource resolveEntity(
                                String publicId,
                                String systemId)
    throws SAXException {

        // get the doctype declaration uri
        if (!hasURI) {
            if (systemId != null) {
                this.systemId = systemId;
                this.availableId = systemId;
            } else if (publicId != null) {
                this.publicId = publicId;
                this.availableId = publicId;
            }
            hasURI = true;
        }

        return null;
    } // end resolveEntity

    /** 
     * Provides access to the System ID of the dtd declaration 
     * of an XML document. 
     *
     * @return System ID of the XML documents doctype declaration.
     */
    public String getSystemId() {
        return systemId;
    } // end getSystemId

    /** 
     * Provides access to the Public ID of the dtd declaration 
     * of an XML document. 
     *
     * @return Public ID of the XML documents doctype declaration.
     */
    public String getPublicId() {
        return publicId;
    } // end getPublicId

    /** 
     * Provides access to the available ID of the dtd declaration 
     * of an XML document 
     * 
     * The ID may come from a System or Public ID.
     *
     * @return URI of the XML documents doctype declaration.
     */
    public String getAvailableId() {
        return availableId;
    } // end getAvailableId

} // end OutputDeclHandler class
