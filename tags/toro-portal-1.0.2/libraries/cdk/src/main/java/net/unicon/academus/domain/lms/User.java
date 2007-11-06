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
package net.unicon.academus.domain.lms;

import java.lang.reflect.Field;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.groups.IMember;

/** A user within the portal system.  Provides data such as username and permissions information. */
public final class User implements EntityObject, XMLAbleEntity, IDomainEntity {

    // Static Members.
    private static final Map contexts = Collections.synchronizedMap(new HashMap());

    /* Instance Members */
    private long id = 0;

    private IMember groupMember = null;

    private String username  = null;
    private String firstName = null;
    private String lastName  = null;
    private String email     = null;
    private String password  = null;
    // private Context uContext = null;

    private String prefix    = null;
    private String suffix    = null;
    private String address1  = null;
    private String address2  = null;
    private String city      = null;
    private String state     = null;
    private String zip       = null;
    private String role      = null;
    private String phone     = null;

    private Map attributes   = null;

    /**
     * Constructor intended for use by UserFactory
     *
     * @param id
     * @param groupMember
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     * @param attributes
     */
    User(long id, IMember groupMember, String username, String password,
        String firstName, String lastName, String email,
        String prefix, String suffix, String addr1, String addr2,
        String city, String state, String zip,
        String phone, String role, Map attributes)   {

        this(id, groupMember, username, password, firstName, lastName, email, attributes);

        // optional attributes
        this.prefix = prefix;
        this.suffix = suffix;
        this.address1 = addr1;
        this.address2 = addr2;
        this.city = city;
        this.state = state;
        this.zip   = zip;
        this.phone = phone;

        // XXX dunno about this yet
        this.role = role;
    }  // constructor

    User(long id, IMember groupMember, String username, String password,
         String firstName, String lastName, String email, Map attributes) {
             this.id          = id;
             this.username    = username;
             this.firstName   = firstName;
             this.lastName    = lastName;
             this.email       = email;
             // this.uContext    = new Context();
             this.password    = password;
             this.groupMember = groupMember;

             // clone the Map in case it changes externally
             this.attributes  = __cloneAttributes(attributes);
        }

    // This ought to become a utility method somewhere YYY
    private HashMap __cloneAttributes(Map attributes) {

        HashMap attrs = new HashMap();

        if (attributes != null) {
            try {
                attrs = new HashMap();
                attrs.putAll(attributes);
            }
            catch (Throwable t2) { // OperationNotSupported
                // ok do the long way
                Set entrySet = attributes.entrySet();
                Map.Entry entry = null;
                for (Iterator iter = entrySet.iterator(); iter.hasNext(); ) {
                    entry = (Map.Entry)iter.next();
                    attrs.put(entry.getKey(), entry.getValue());
                } // for
            } // catch t2
        }
        return attrs;
    }

    // Optional attribute accessors
    public String getPrefix()   { return prefix;   }
    public String getSuffix()   { return suffix;   }
    public String getAddress1() { return address1; }
    public String getAddress2() { return address2; }
    public String getCity()     { return city;     }
    public String getZip()      { return zip;      }
    public String getState()    { return state;    }
    public String getPhone()    { return phone;    }


    // package private user_id setter.
    // we use this when we have a set of User objects created from
    // the person directory and need to separately fetch their user ids
    void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setGroupMember(IMember groupMember) {
        this.groupMember = groupMember;
    }

    public IMember getGroupMember() {
        return groupMember;
    }

    /**
     * Provides the user's username within the portal system.
     * @return the user's username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * Modifies the user's password for the portal system.  The <code>newPswd</code> should not be encrypted.
     * @param newPswd the new password to be associated with this user.
     * @throws IllegalArgumentException if <code>newPswd</code> is zero-length or contains only whitespace.
     */
    public void setPassword(String newPswd)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        this.password = UserFactory.encodePassword(newPswd);
    }

    public String getPassword()
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        return this.password;
    }
    /**
     * Provides the user's first name.
     * @return the user's first name.
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * Modifies the user's first name.  Primarily allows someone in authority to correct an earlier misspelling.
     * @param newName the new first name for this user.
     * @throws IllegalArgumentException if <code>newName</code> is zero-length or contains only whitespace.
     */
    public void setFirstName(String newName)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        this.firstName = newName;
    }

    /**
     * Provides the user's last name.
     * @return the user's last name.
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * Modifies the user's last name.  Primarily allows someone in authority to correct an earlier misspelling.
     * @param newName the new last name for this user.
     * @throws IllegalArgumentException if <code>newName</code> is zero-length or contains only whitespace.
     */
    public void setLastName(String newName)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        this.lastName = newName;
    }

    /**
     * Convenience method to set a class member variable as I am tired of having to
     * write the same set logic over and over
     * @author KG
     * @param stateAttr the name of the class member variable
     * @param the String vaue to set it to. Could generalize to Object if needed
     * @param persist whether to write it to the backing store
     * @return nada
     * @exception DomainException if the reflection no workie
     */
    private void __setStateAttribute(String stateAttr, String value)
                    throws IllegalArgumentException, ItemNotFoundException,
                           OperationFailedException, DomainException {
/*
        if (value != null) {
            StringBuffer msg = new StringBuffer();
            msg.append(stateAttr);
            msg.append(" cannot be null.");
            throw new IllegalArgumentException(msg.toString());
        }
*/
        // double-check our Member ;)
        Field stateMember = null;
        try {
            stateMember = this.getClass().getDeclaredField(stateAttr);
            stateMember.set(this, value);
        }
        catch (Throwable t) {
            throw new DomainException(t);
        }
    }

    // optional attribute set methods
    public void setRole(String value)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("role", value);
    }

    public void setZip(String value)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("zip", value);
    }

    public void setPhone(String value)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("phone", value);
    }

    public void setCity(String city)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("city", city);
    }

    public void setState(String value)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("state", value);
    }

    public void setAddress2(String address2)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("address2", address2);
    }

    public void setAddress1(String address1)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("address1", address1);
    }

    public void setPrefix(String prefix)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("prefix", prefix);
    }

    public void setSuffix(String suffix)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException, DomainException {
        __setStateAttribute("suffix", suffix);
    }


    /**
     * Provides the user's first and last name together in the following format:  <i>last_name</i>, <i>first_name</i>.
     * @return the user's full name.
     */
    public String getFullName() {
        return lastName + ", " + firstName;
    }
    /**
     * Provides the user's e-mail address.
     * @return the user's e-mail.
     */
    public String getEmail() {
        return this.email;
    }
    /**
     * Modifies the user's e-mail address.  Allows someone in authority to
     * correct an earlier misspelling or update a user who's e-mail address
     * has changed.  An e-mail address must be in the following format:<p>
     * <blockquote><i>name</i>@<i>domain</i>.<i>extension</i></blockquote>
     * @param a valid e-mail address.
     * @throws IllegalArgumentException if newMail is not formatted properly.
     */
    public void setEmail(String newEmail)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException, DomainException {
        this.email = newEmail;
    }

    /**
     * Provides the <code>Set</code> of the attribute keys.
     * @return the <code>Set</code> of attribute keys.
     */
    public Set getAttributeKeys() {
        return new HashSet(attributes.keySet());
    }

    /**
     * gets the Map representing this User's set of metadata. Changes
     * to this Map do not reflect in the underlying User's attributes.
     * @return Map
     */
    public Map getAttributes() {
        return __cloneAttributes(this.attributes);
    }

    /**
     * Provides the user's value for the requested attribute or <code>null</code> if the attribute is undefined for this user.
     * @param key the name of the desired attribute.
     * @return the value of the requested attribute for this user.
     */
    public String getAttribute(String key) {
        return (String) attributes.get(key);
    }
    /**
     * Modifies the specified attribute's value for this user.
     * @param key the name of an attribute.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if key is zero-length or contains only whitespace or if key is <code>null</code>.
     */
    public void setAttribute(String key, String value)
    throws IllegalArgumentException, OperationFailedException,
    ItemNotFoundException, DomainException {
        if (key == null) {
            String msg = "User attribute key can't be null";
            throw new IllegalArgumentException(msg);
        }
        if (key.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("User attribute key can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }
        attributes.put(key, value);
    }

    public void clearAttributes()
        throws IllegalArgumentException, OperationFailedException,
               ItemNotFoundException, DomainException
    {
        attributes = new HashMap();
    }

    /**
     * Un-enrolls the user in the specified offering.
     * @param oldOffering the offering from which to remove the user.
     * @throws IllegalArgumentException if the user is not currently enrolled
     * in the <code>oldOffering</code> or if <code>oldOffering</code> is <code>null</code>.
     */
    public void removeOffering(Offering oldOffering)
    throws IllegalArgumentException,
    ItemNotFoundException,
    OperationFailedException {
        if (oldOffering == null) {
            String msg = "The offering can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List offerings = Memberships.getAllOfferings(this);
        if (!offerings.contains(oldOffering)) {
            String msg = "The user is not currently enrolled in this offering.";
            throw new IllegalArgumentException(msg);
        }
        Memberships.remove(this, oldOffering);
    }
    /**
     * Provides the user's context within the portal system.  The context
     * provides information relevant to the user's current session within the
     * portal system.  For example, the <code>Context</code> object can tell you what offering the user is viewing.
     * @return the user's context information.
     */
    public Context getContext() {

        Context rslt = (Context) contexts.get(username);

        if (rslt == null) {
            rslt = new Context();
            contexts.put(username, rslt);
        }

        return rslt;
    }
    public boolean isSuperUser() {
        return UserFactory.isSuperUser(username);
    }

    public boolean equals(Object otherUser) {
        return ( (otherUser instanceof User) &&
        ((User) otherUser).getUsername().equals(getUsername()) );
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<user");
        xml.append(" username=\"");
        xml.append(this.username);
        xml.append("\"");
        xml.append(">");
        xml.append("<first_name>");
        xml.append("<![CDATA[");
        xml.append(firstName);
        xml.append("]]>");
        xml.append("</first_name>");
        xml.append("<last_name>");
        xml.append("<![CDATA[");
        xml.append(lastName);
        xml.append("]]>");
        xml.append("</last_name>");
        xml.append("<password>");
        xml.append(this.password);
        xml.append("</password>");
        xml.append("<email>");
        xml.append(email);
        xml.append("</email>");

        if (attributes != null) {
             xml.append("<attributes>");

             Iterator iterator = attributes.keySet().iterator();
             String key = null;
             while (iterator.hasNext() ) {
                 key = (String) iterator.next();
                 xml.append("<attribute").append(" name=\"");

                 xml.append(key);
                 xml.append("\"").append(">");
                 xml.append("<value><![CDATA[");
                 xml.append((String) attributes.get(key));
                 xml.append("]]></value>");

                 xml.append("</attribute>");
             }
             xml.append("</attributes>");
        }

        xml.append("</user>");

        return xml.toString();
    }

}
