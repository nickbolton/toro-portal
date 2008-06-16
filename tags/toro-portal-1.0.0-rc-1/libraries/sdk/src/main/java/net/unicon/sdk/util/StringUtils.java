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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
    protected static String[] charMappings = null;

    private final static int LAST_ASCII_CHAR = 255;

    static {
        try {
            charMappings = initCharacterMappings();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String md5String(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer(32);
            for (int i=0; i<digest.length; i++) {
                if (digest[i] < 0x10 && digest[i] >= 0x0) {
                    // add a leading 0
                    sb.append("0");
                }
                sb.append(Integer.toHexString((0xff & digest[i])));
            }
            return(sb.toString());
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return(null);
    }

    public static String replaceSpecialCharsOfString(
    String str) {
        StringBuffer sb = new StringBuffer();
        Pattern pat =
        Pattern.compile("(\\&amp;|\\&#38;|\\&#?\\w{2,6};)");
        Matcher mat = pat.matcher(str);
        boolean isEnt = false;
        int matIdx = 0;
        char c;
        int idx;
        int len = str.length();
        int i = 0;
        while (i < len) {
            c = str.charAt(i);
            idx = (int)c;
            if (mat.find(i)) {
                matIdx = mat.start();
                isEnt = (matIdx == i);
            }

            if (isEnt) {
                i = (mat.end() - 1);
                isEnt = false;
                sb.append(mat.group());
            // Ignore non-ASCII characters since they are not inluded in the character mapping
            } else if (idx <= LAST_ASCII_CHAR) {

                if (charMappings[idx] != null) {
                    sb.append(charMappings[idx]);                    
                } else {
                    sb.append(c);
                }
            }
            i++;
        } // end loop
        return sb.toString();
    } // end replaceSpecialCharsOfString
    protected static String[] initCharacterMappings()
    throws SAXException, IOException, ParserConfigurationException,
    Exception {
        XMLReader xmlReader =
        SAXParserFactory.newInstance().
        newSAXParser().getXMLReader();
        CharacterMappingHandler handler = new CharacterMappingHandler();
        xmlReader.setContentHandler(handler);
        InputSource is = ResourceLoader.getResourceAsSAXInputSource(
        StringUtils.class, "/properties/CharacterMappings.xml");
        xmlReader.parse(is);
        return handler.getCharacterMappings();
    } // end initCharacterMappings
    protected StringUtils() { }
    public static void main(String args[]) {
        String str = "!@#$%^&*()_-+=     *";
        int type = 1;
        switch (type) {
            case 1:
                System.out.println("String Result: " +
                replaceSpecialCharsOfString(str));
                break;
            case 2:
                break;
            case 3:
                break;
        }
    } // end main
} // end StringUtils class
