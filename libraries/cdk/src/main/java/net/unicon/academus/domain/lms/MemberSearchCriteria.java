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

/**
 * <p>
 * 
 * </p>
 */
public class MemberSearchCriteria {
    private String firstName;
    private String lastName;
    private String username;
    private String email;

    boolean matchAllCriteria;

    public MemberSearchCriteria() {
        firstName = null;
        lastName  = null;
        username  = null;
        email     = null;
    }
    
    /**
     * <p>
     * Set the first name to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setFirstName(String searchFirstName) {
        firstName = searchFirstName;
    }
    
    /**
     * <p>
     * Return the first name to search for.
     * </p><p>
     * 
     * @return a String with the name to search for.
     * </p>
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * <p>
     * Set the first name to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setLastName(String searchLastName) {
        lastName = searchLastName;
    }
    
    /**
     * <p>
     * Return the last name to search for.
     * </p><p>
     * 
     * @return a String with the name to search for.
     * </p>
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * <p>
     * Set the username to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setUsername(String searchUsername) {
        username = searchUsername;
    }
    
    /**
     * <p>
     * Return the username to search for.
     * </p><p>
     * 
     * @return a String with the username to search for.
     * </p>
     */
    public String getUsername() {
        return username;
    }

    /**
     * <p>
     * Set the username to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setEmail(String searchEmail) {
        email = searchEmail;
    }
    
    /**
     * <p>
     * Return the email to search for.
     * </p><p>
     * 
     * @return a String with the email to search for.
     * </p>
     */
    public String getEmail() {
        return email;
    }

    /**
     * <p>
     * Returns a true of false if the search criteria needs
     * to match all values set, or any value that is set.
     * </p><p>
     * 
     * @return a boolean true or false..
     * </p>
     */
    public void matchAllCriteria(boolean searchValue) {
        this.matchAllCriteria = searchValue;
    }

    /**
     * <p>
     * Set a true of false if the search criteria needs
     * to match all values set, or any value that is set.
     * </p><p>
     * 
     * @param a boolean true or false..
     * </p>
     */
    public boolean matchAllCriteria() {
        return this.matchAllCriteria;
    }

}    
