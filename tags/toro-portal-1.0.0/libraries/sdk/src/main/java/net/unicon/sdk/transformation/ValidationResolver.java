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
 * ValidationResolver sets the dtd inputsource for the external xml document.
 */
public class ValidationResolver implements EntityResolver {

    private InputSource doctypeSource = null;

    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException {

        // return the doctype source
        return doctypeSource;

    } // end resolveEntity

    public ValidationResolver() {} // default contructor

    public ValidationResolver(String doctypeURI) {
        /* ASSERTION */
        if (!(doctypeURI != null && doctypeURI.length() > 0)) {
            throw new IllegalArgumentException (
                "Argument 'doctypeURI [String]' must be provided");
        }

        this.doctypeSource = new InputSource(doctypeURI); 

    } // end ValidationResolver(doctypeURI) constructor 

} // end ValidationResolver class
