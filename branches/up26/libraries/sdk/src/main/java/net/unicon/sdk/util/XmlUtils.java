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
package net.unicon.sdk.util;

import java.util.Map;
import java.util.Iterator;
import java.io.InputStream;
import java.io.Reader;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

public class XmlUtils {

    /**
     * Parses the File and returns a Document using the
     * DOM 'Flavor-of-the-Month'
     *
     * @param fileName file name of the xml document.
     * @param validate If you want the parser to validate the input or not.
     * @return complete Document.
     */
    public static Document parse(String fileName, boolean validate)
    throws Exception {
            DOMParser parser = new DOMParser();
            if (validate)
                parser.setErrorHandler(new SAXErrorHandler());
            parser.setFeature("http://xml.org/sax/features/validation",
                validate);
            parser.parse(fileName);
            return parser.getDocument();
    }

    /**
     * Parses the Reader and returns a Document using the
     * DOM 'Flavor-of-the-Month'
     *
     * @param reader Reader
     * @param validate If you want the parser to validate the input or not.
     * @return complete Document.
     */
    public static Document parse(Reader reader, boolean validate)
    throws Exception {
        return parse(new InputSource(reader), validate);
    }

    /**
     * Parses the InputStream and returns a Document using the
     * DOM 'Flavor-of-th e-Month'
     *
     * @param is InputStream
     * @param validate If you want the parser to validate the input or not.
     * @return complete Document.
     */
    public static Document parse(InputStream is, boolean validate)
    throws Exception {
        return parse(new InputSource(is), validate);
    }

    /**
     * Parses the InputSource and returns a Document using the
     * DOM 'Flavor-of-th e-Month'
     *
     * @param is org.xml.sax.InputSource (wrapper for a
     * reader or an input stream).
     * @param validate If you want the parser to validate the input or not.
     * @return complete Document.
     */
    public static Document parse(InputSource is, boolean validate)
    throws Exception {
            DOMParser parser = new DOMParser();
            if (validate)
                parser.setErrorHandler(new SAXErrorHandler());
            parser.setFeature("http://xml.org/sax/features/validation",
                validate);
            parser.parse(is);
            return parser.getDocument();
    }

    /**
       This static method has no utility whatsoever other than to 
       provide a single call to get XML from either a PortalObject
       or an XMLAbleEntity. This is needed as a stopgap solution
       as we are refactoring as both types exist in the system 
       today. Eventually we should convert to an XMLAbleEntity and
       remove PortalObject (and this method)

    public static String toXML(Object obj) {
	// object ahs to be a PortalObject or an XMLAbleEntity
	if (obj instanceof PortalObject) {
	    return ((PortalObject)obj).toXML();
	}
	else if (obj instanceof XMLAbleEntity) {
	    return ((XMLAbleEntity)obj).toXML();
	}
	else {
	    return "";
	}
    }
    */

    public static String makeStringXMLSafe(String str) {
        return "<![CDATA[" + str +  "]]>";
    }
    public static String makeStringHTMLSafe(String str) {
        StringBuffer s = new StringBuffer();
        char c;
        for (int pos = 0; pos < str.length(); ++pos) {
            switch ( c = str.charAt(pos)) {
                case '<' :
                    s.append("&lt;");
                    break;
                case '>' :
                    s.append("&gt;");
                    break;
                case '"' :
                    s.append("&quot;");
                    break;
                default :
                    s.append(c);
                    break;
            }
        }
        return (s.toString());
    }
    public static Node addNewNode(Document doc, Node node,
    String name, Map attrs, String value)
    throws Exception {
        if (doc == null) {
            throw new Exception("Document is null!");
        }
        if (node == null) {
            throw new Exception("Node is null!");
        }
        if (name == null) {
            throw new Exception("name is null!");
        }
        Element el = doc.createElement(name);
        if (attrs != null) {
            Iterator itr = attrs.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String)itr.next();
                String val = (String)attrs.get(key);
                el.setAttribute(key, val);
            }
        }
        if (value != null) {
            el.appendChild(doc.createTextNode(value));
        }
        node.appendChild(el);
        return el;
    }
    protected XmlUtils() { }
}
