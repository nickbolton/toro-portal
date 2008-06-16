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



public class MemberData implements Serializable 
{

    /** identifier fields */
    private String source;
    private String groupIdRef;
    private String personIdRef;

        // info fields
    private String status;
    private String groupRole;

    /** default constructor */
    public MemberData() { }

   /**
    * Constructor for a <code>MemberData</code> object.
    *
    * @param  source        The member's source
    * @param  groupIdRef    The members's groupid-ref 
    * @param  personIdRef   The members's personid-ref
    * @param  status        The member's status 
    * @param  groupRole     The member's group-role
    *
    * @see    UCFImportAdapter
    */

    public MemberData(String source, String groupIdRef, String personIdRef, 
                      String status, String groupRole)
    {
        this.source = source;
        this.groupIdRef = groupIdRef;
        this.personIdRef = personIdRef;
        this.status = status;
        this.groupRole = groupRole;
    }

        /** Accessor for a MemberData object's source field */
    public String getSource() { return this.source; }
        /** Accessor for a MemberData object's groupIdRef field */
    public String getGroupIdRef() { return this.groupIdRef; }
        /** Accessor for a MemberData object's personIdRef field */
    public String getPersonIdRef() { return this.personIdRef; }
        /** Accessor for a MemberData object's status field */
    public String getStatus() { return this.status; }
        /** Accessor for a MemberData object's groupRole field */
    public String getGroupRole() { return this.groupRole; }

        /** Mutator for a MemberData object's source field */
    public void setSource(String source) { this.source = source; }
        /** Mutator for a MemberData object's groupIdRef field */
    public void setGroupIdRef(String groupIdRef) 
    { this.groupIdRef = groupIdRef; }
        /** Mutator for a MemberData object's personIdRef field */
    public void setPersonIdRef(String personIdRef) 
    { this.personIdRef =  personIdRef; }
        /** Mutator for a MemberData object's status field */
    public void setStatus(String status) 
    { this.status = status; }
        /** Mutator for a MemberData object's groupRole field */
    public void setGroupRole(String groupRole) 
    { this.groupRole = groupRole; }


    /** 
     * Returns a string representation of a <code>MemberData</code> 
     * 
     * @return Returns a string representation of a <code>MemberData</code> 
     *         in the format: 
     *             "source, group id ref, person id ref, status, group role"
     */
    public String toString() 
    {
        StringBuffer str = new StringBuffer("");
        str.append(source).append(",");
        str.append(groupIdRef).append(",");
        str.append(personIdRef).append(",");
        str.append(status).append(",");
        str.append(groupRole);

        return str.toString();
    }


    /** 
     * Compares one <code>MemberData</code> object to another.
     * 
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *        
     */

    public boolean equals(Object other) {
        if ( !(other instanceof MemberData) ) return false;
        MemberData castOther = (MemberData) other;

        if (this.toString().equals(castOther.toString())) return true;

        return false;
    }

}
