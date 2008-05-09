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



public class SubgroupData implements Serializable 
{

    /** identifier fields */
    private String source;
    private String groupIdRef;
    private String subgroupIdRef;

        // info fields
    private String status;
    private String groupRole;

    /** default constructor */
    public SubgroupData() { }

   /**
    * Constructor for a <code>SubgroupData</code> object.
    *
    * @param  source        The subgroup's source-ref
    * @param  groupIdRef    The subgroup's groupid-ref 
    * @param  subgroupIdRef The subgroup's subgroupid-ref 
    * @param  status        The subgroup's status 
    * @param  groupRole     The subgroup's group-role
    *
    * @see    UCFImportAdapter
    */

    public SubgroupData(String source, String groupIdRef, String subgroupIdRef, 
                        String status, String groupRole)
    {
        this.source = source;
        this.groupIdRef = groupIdRef;
        this.subgroupIdRef = subgroupIdRef;
        this.status = status;
        this.groupRole = groupRole;
    }

        /** Accessor for a SubgroupData object's source field */
    public String getSource() { return this.source; }
        /** Accessor for a SubgroupData object's groupIdRef field */
    public String getGroupIdRef() { return this.groupIdRef; }
        /** Accessor for a SubgroupData object's subgroupIdRef field */
    public String getSubgroupIdRef() { return this.subgroupIdRef; }
        /** Accessor for a SubgroupData object's status field */
    public String getStatus() { return this.status; }
        /** Accessor for a SubgroupData object's groupRole field */
    public String getGroupRole() { return this.groupRole; }

        /** Mutator for a SubgroupData object's source field */
    public void setSource(String source) { this.source = source; }
        /** Mutator for a SubgroupData object's groupIdRef field */
    public void setGroupIdRef(String groupIdRef) 
    { this.groupIdRef = groupIdRef; }
        /** Mutator for a SubgroupData object's subgroupIdRef field */
    public void setSubgroupIdRef(String subgroupIdRef) 
    { this.subgroupIdRef =  subgroupIdRef; }
        /** Mutator for a SubgroupData object's status field */
    public void setStatus(String status) { this.status = status; }
        /** Mutator for a SubgroupData object's groupRole field */
    public void setGroupRole(String groupRole) { this.groupRole = groupRole; }


    /** 
     * Returns a string representation of a <code>SubgroupData</code> 
     * 
     * @return Returns a string representation of a <code>SubgroupData</code> 
     *         in the format: 
     *              "source, group id ref, subgroup id ref, status, group role"
     */
    public String toString() 
    {
        StringBuffer str = new StringBuffer("");
        str.append(source).append(",");
        str.append(groupIdRef).append(",");
        str.append(subgroupIdRef).append(",");
        str.append(status).append(",");
        str.append(groupRole);

        return str.toString();
    }


    /** 
     * Compares one <code>SubgroupData</code> object to another.
     * 
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *        
     */

    public boolean equals(Object other) {
        if ( !(other instanceof SubgroupData) ) return false;
        SubgroupData castOther = (SubgroupData) other;

        if (this.toString().equals(castOther.toString())) return true;

        return false;
    }

}
