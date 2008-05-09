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

package net.unicon.academus.delivery.virtuoso;

import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
// import net.unicon.util.Crypt;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides static methods to create needed encryption for 3rd Party Delivery Systems and is not instantiable.
 */
public class DeliveryEncryption {

    // Static Members.
    private static EncodingSet es = null;

    /*
     * Public API.
     */

    public static String createSCHMOO(User user) {

        String schmoo = null;

        if (user == null) {
            throw new IllegalArgumentException("No User for SCHMOO");
        }

        StringBuffer sb = new StringBuffer();

        try {
            // Encodes username concatinated with a 0 and the userType
            sb.append(URLEncoder.encode(encrypt(user.getUsername() + "0" + user.getAttribute("userType")), encoding));
            schmoo = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return schmoo;
    }

    public static User getSCHMOOUser(String schmoo) {

        User user = null;

        try {

            String decrypted = decrypt(URLDecoder.decode(schmoo, encoding));
            String username = decrypted.substring(0, decrypted.lastIndexOf('0'));
            user = UserFactory.getUser(username);

            // Sets userType attribute
            user.setAttribute("userType", decrypted.substring(decrypted.lastIndexOf('0')+1, decrypted.length()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    /*
     * Implementation.
     */

    private final static String encoding = "UTF-8";

    private static final List encodeSets = new ArrayList();

    private static EncodingSet getEncodingSet(int index) {

        // Assertions.
        if (index < 0) {
            String msg = "Argument 'index' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }
/* AW 7/20/05:  Related to old impl below....
        if (index > encodeSets.size()) {
            String msg = "Argument 'index' cannot be greater than the current "
                                    + "number of encoding sets plus one.";
            throw new IllegalArgumentException(msg);
        }
*/

/* AW 7/20/05:  Removing the original key data generation algorithm (below)
   and replacing it w/ a deterministic one b/c the first version doesn't
   work in a multi-box Academus deployment.

        // Use the existing one if we already have one.
        if (index < encodeSets.size()) {
            return (EncodingSet) encodeSets.get(index);
        }

        // Otherwise create and add.
        char[] chars = UserFactory.getAllowableUsernameCharacters();
        List pairings = new ArrayList();
        for (int i=0; i < chars.length; i++) {
            pairings.add(new Character(chars[i]));
        }
        Collections.shuffle(pairings);

*/

        // Get out if the EncodingSet is already initialized...
        if (es != null) {
            return es;
        }

        // Create it if we're still here...
        char[] chars = UserFactory.getAllowableUsernameCharacters();
        List pairings = new ArrayList(chars.length);
        for (int i=0; i < chars.length; i++) {
            pairings.add(new Character(chars[i]));
        }
        Collections.reverse(pairings);

        // Create the mappings.
        Map encodeChars = new HashMap();
        Map decodeChars = new HashMap();
        for (int i=0; i < chars.length; i++) {

            Object o1 = new Character(chars[i]);
            Object o2 = pairings.remove(0);

            encodeChars.put(o1, o2);
            decodeChars.put(o2, o1);

        }

        es = new EncodingSet(encodeChars, decodeChars);
        // encodeSets.add(rslt); // AW 7/20/05:  Related to old impl above.
        return es;

    }

    private static String encrypt(String s) {

        StringBuffer rslt = new StringBuffer();

        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        for (int i=0; i < chars.length; i++) {

            EncodingSet set = getEncodingSet(i);
            Object o = set.getEncodeMap().get(new Character(chars[i]));
            if (o == null) {
                String msg = "Illegal character '" + chars[i] + "' in input string:  " + s;
                throw new IllegalArgumentException(msg);
            }
            rslt.append(o);

        }

/*
System.out.println("--> encrypt...");
System.out.println("\ts="+s);
System.out.println("\trslt="+rslt.toString());
*/

        return rslt.toString();

    }

    private static String decrypt(String s) {

        StringBuffer rslt = new StringBuffer();

        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        for (int i=0; i < chars.length; i++) {

            EncodingSet set = getEncodingSet(i);
            Object o = set.getDecodeMap().get(new Character(chars[i]));
            if (o == null) {
                String msg = "Illegal character '" + chars[i] + "' in input string:  " + s;
                throw new IllegalArgumentException(msg);
            }
            rslt.append(o);

        }

/*
System.out.println("--> decrypt...");
System.out.println("\ts="+s);
System.out.println("\trslt="+rslt.toString());
*/

        return rslt.toString();

    }

    private DeliveryEncryption() {};

    private static final class EncodingSet {

        // Member Variables.
        private Map encodeMap;
        private Map decodeMap;

        public EncodingSet(Map encodeMap, Map decodeMap) {

            // Assertions.
            if (encodeMap == null) {
                String msg = "Argument 'encodeMap' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (decodeMap == null) {
                String msg = "Argument 'decodeMap' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Member Variables -- skip defensive copy to improve performance.
            this.encodeMap = encodeMap;
            this.decodeMap = decodeMap;

        }

        public Map getEncodeMap() {
            // Skip defensive copy to improve performance.
            return encodeMap;
        }

        public Map getDecodeMap() {
            // Skip defensive copy to improve performance.
            return decodeMap;
        }

    }

}

