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
package net.unicon.academus.service;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

import net.unicon.academus.common.AcademusException;
import net.unicon.sdk.util.CollectionUtils;

/**
 * @author Kevin Gary
 *
 */
public class UserData extends ABaseData implements Serializable {

    // NB:  Version # for serialization.  If you make significant
    // changes to this class, you *must* increment this number!
    static final long serialVersionUID = 1;

    // Required attributes - all values are Strings
    public static final String USERKEY  = "username";
    public static final String FNAMEKEY = "firstname";
    public static final String LNAMEKEY = "lastname";
    public static final String EMAILKEY = "email";
    public static final String PASSWORD = "password";

    // some common optional attributes - all values are Strings,
    // except PHONE, which has a value of a Map which has keys
    // of PHONE_* below (whose values are Strings)
    public static final String PREFIX   = "prefix";
    public static final String SUFFIX   = "suffix";
    public static final String ADDRESS1 = "address1";
    public static final String ADDRESS2 = "address2";
    public static final String CITY     = "city";
    public static final String STATE    = "state";
    public static final String ZIP      = "zip";
    public static final String COUNTRY  = "country";
    public static final String SYSTEM_ROLE = "role";
    public static final String PHONE    = "phone";

    /**
     * storage for the set of attributes on the marshalling object
     */
    protected final HashMap _fUserAttrMap = new HashMap(10);
    private String __userName;
    private String __firstName;
    private String __lastName;
    private String __email;
    private String __password;

    // these are non-state variables for efficiency
    private transient Map metadataAttributes = null;
    private transient Map optionalAttributes = null;
    private transient Map allAttributes      = null;

    /*
     * Public API.
     */

    public UserData(String id, String src,
                    String username, String firstname, String lastname,
                    String email, String password) throws AcademusException {
        super(id, src);
        __initRequiredData(username, firstname, lastname, email, password);
    }
    
    public UserData(String id, String src,
            String username, String firstname, String lastname,
            String email, String password, Map attrs) throws AcademusException {
        
        super(id, src);
        __initRequiredData(username, firstname, lastname, email, password);
        __initOptionalData(attrs);
    }

    public UserData(String id, String src, String foreignId,
                    String username, String firstname, String lastname,
                    String email, String password, Map attrs) throws AcademusException {
        super(id, src, foreignId);
        __initRequiredData(username, firstname, lastname, email, password);
        // hang on to all the other attributes
        __initOptionalData(attrs);
    }

    public UserData(String id, String src, Map attrs) throws AcademusException {
        super(id, src);

        // copy Map attrs over except for our special required ones
        if (attrs == null) {
            throw new AcademusException("Invalid attribute set: attributes are null");
        }
        // initialize the dataset
        __initOptionalData(attrs);

        // get the required attributes out of the map and into our state
        String userName  = (String)_fUserAttrMap.remove(USERKEY);
        String firstName = (String)_fUserAttrMap.remove(FNAMEKEY);
        String lastName  = (String)_fUserAttrMap.remove(LNAMEKEY);
        String email     = (String)_fUserAttrMap.remove(EMAILKEY);
        String passwd    = (String)_fUserAttrMap.remove(PASSWORD);

        __initRequiredData(userName, firstName, lastName, email, passwd);
    }

    public UserData(UserData someOtherData) throws AcademusException {
        super(someOtherData.getExternalID(), someOtherData.getSource());

        __initOptionalData(someOtherData._fUserAttrMap);
        __initRequiredData(someOtherData.getUserName(),
                           someOtherData.getFirstName(),
                           someOtherData.getLastName(),
                           someOtherData.getEmail(),
                           someOtherData.getPassword());
    }

    public String getUserName() {
        return __userName;
    }
    public String getFirstName() {
        return __firstName;
    }
    public String getLastName() {
        return __lastName;
    }
    public String getEmail() {
        return __email;
    }
    public String getPassword() {
        return __password;
    }

    public Map getOptionalAttributes() {
        if (optionalAttributes == null) {
            ArrayList keys = new ArrayList(10);

            keys.add(PREFIX);
            keys.add(SUFFIX);
            keys.add(ADDRESS1);
            keys.add(ADDRESS2);
            keys.add(CITY);
            keys.add(STATE);
            keys.add(ZIP);
            keys.add(COUNTRY);
            keys.add(SYSTEM_ROLE);
            keys.add(PHONE);

            // now get our Maps keys
            HashSet allKeys = new HashSet(_fUserAttrMap.keySet());
            optionalAttributes = CollectionUtils.constructMapFromSet(allKeys, keys,
                                                                     _fUserAttrMap,
                                                                     CollectionUtils.INTERSECTION);
        }
        return optionalAttributes;
    }

    public Map getMetadataAttributes() {

        if (metadataAttributes == null) {
            ArrayList keys = new ArrayList(15);
            keys.add(USERKEY);
            keys.add(FNAMEKEY);
            keys.add(LNAMEKEY);
            keys.add(PASSWORD);
            keys.add(EMAILKEY);
            // add the optionals too
            keys.add(PREFIX);
            keys.add(SUFFIX);
            keys.add(ADDRESS1);
            keys.add(ADDRESS2);
            keys.add(CITY);
            keys.add(STATE);
            keys.add(ZIP);
            keys.add(COUNTRY);
            keys.add(SYSTEM_ROLE);
            keys.add(PHONE);

            // now get our Map keys here
            HashSet allKeys = new HashSet(_fUserAttrMap.keySet());
            metadataAttributes = CollectionUtils.constructMapFromSet(allKeys, keys,
                                                                     _fUserAttrMap,
                                                                     CollectionUtils.DIFFERENCE);
        }
        return metadataAttributes;
    }

    public Map getAttributes() {
        if (allAttributes == null) {
            ArrayList keys = new ArrayList(15);
            keys.add(USERKEY);
            keys.add(FNAMEKEY);
            keys.add(LNAMEKEY);
            keys.add(PASSWORD);
            keys.add(EMAILKEY);

            HashSet allKeys = new HashSet(_fUserAttrMap.keySet());
            allAttributes = CollectionUtils.constructMapFromSet(allKeys, keys,
                                                                _fUserAttrMap,
                                                                CollectionUtils.UNION);
        }
        return allAttributes;
    }

    /*
     * Implementation.
     */
    private void __initRequiredData(String username, String firstname,
                                    String lastname, String email,
                                    String passwd)
                                    throws AcademusException  {

         if (username == null || username.trim().length() == 0) {
            throw new AcademusException("Invalid marshalled user data");
         }
         __userName  = username;
         __firstName = firstname;
         __lastName  = lastname;
         __email     = email;

         if (passwd == null || passwd.trim().length() == 0) {
             // set the password to the username. We want to make this
             // a configurable strategy.
             __password = __userName;
         }
         else {
             __password = passwd;
         }
    }

    private void __initOptionalData(Map attrs) {
        try {
            _fUserAttrMap.putAll(attrs);
        }
        catch (UnsupportedOperationException exc) {
            // ok, do it this way. *sigh*
            Set entrySet = attrs.entrySet();
            Map.Entry entry = null;
            for (Iterator iter = entrySet.iterator(); iter.hasNext(); ) {
                entry = (Map.Entry)iter.next();
                _fUserAttrMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getAttribute(String key) {
        try {
            return (String)_fUserAttrMap.get(key);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public String toString()
    {
        return __userName + "," + __firstName + "," + __lastName + "," + __email
               + "," + __password + "," + optionalAttributes + ","
               + metadataAttributes + "," + allAttributes + "," + _fUserAttrMap;
    }
}
