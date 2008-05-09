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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class CharacterMappingHandler extends DefaultHandler {
    private String currentEl = "";
    private boolean codeUpdated = false;
    private boolean resultUpdated = false;
    private String currCodeValue = null;
    private String currResultValue = null;
    private String[] charMappings = new String[256];
    public void startElement(String namespaceURI, String localname,
    String rawName, Attributes atts) throws SAXException {
        currentEl = rawName;
    } // end startElement
    public void characters(char[] ch, int start, int length)
    throws SAXException {
        if (!currentEl.equals("")) {
            String value = (new String(ch, start, length));
            if (currentEl.equals("code")) {
                currCodeValue = value;
                codeUpdated = true;
            }
            if (currentEl.equals("result")) {
                currResultValue = value;
                resultUpdated = true;
            }
        }
    } // end characters
    public void endElement(String namespaceURI, String localname,
    String rawName) throws SAXException {
        currentEl = "";
        if (rawName.equals("mapping")) {
            if (currCodeValue != null && codeUpdated) {
                String result = "";
                if (currResultValue != null && resultUpdated) {
                    result = currResultValue;
                }
                charMappings[Integer.parseInt(currCodeValue)] = result;
            }
            codeUpdated = false;
            resultUpdated = false;
        }
    } // end endElement
    public String[] getCharacterMappings() {
        return charMappings;
    } // end getCharacterMappings
} // end CharacterMappingHandler class
