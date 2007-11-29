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
import java.util.HashMap;



public class RelationshipsData implements Serializable 
{

        // info fields
    private String groupid_ref;
    private String source;

    private HashMap child;
    private HashMap parent;
    private HashMap sibling;
    private HashMap alias;

    /** default constructor */
    public RelationshipsData() 
    { 
        groupid_ref = null;
        source = null;
        child = new HashMap();
        parent = new HashMap();
        sibling = new HashMap();
        alias = new HashMap();
    }

   /**
    * Constructor for a <code>RelationshipsData</code> object.
    *
    * @param  groupid_ref  The relationships groupid reference
    * @param  source       The relationships source 
    * @param  child        A HashMap containing child relationship mappings
    * @param  parent       A HashMap containing parent relationship mappings
    * @param  sibling      A HashMap containing sibling relationship mappings
    * @param  alias        A HashMap containing alias relationship mappings
    *
    * @see    UCFImportAdapter
    */

    public RelationshipsData(String groupid_ref, String source, 
                             HashMap child, HashMap parent, 
                             HashMap sibling, HashMap alias)
    {
        this.groupid_ref = groupid_ref;
        this.source = source;
        this.child = child;
        this.parent = parent;
        this.sibling = sibling;
        this.alias = alias;
    }

        /** Accessor for a RelationshipsData object's groupid_ref field*/
    public String getGroupIdRef() { return this.groupid_ref; }
        /** Accessor for a RelationshipsData object's child HashMap */
    public HashMap getChild() { return this.child; }
        /** Accessor for a RelationshipsData object's parent HashMap */
    public HashMap getParent() { return this.parent; }
        /** Accessor for a RelationshipsData object's sibling HashMap */
    public HashMap getSibling() { return this.sibling; }
        /** Accessor for a RelationshipsData object's alias HashMap */
    public HashMap getAlias() { return this.alias; }
        /** Accessor for a RelationshipsData object's source field*/
    public String getSource() { return this.source; }

        /** Mutator for a RelationshipsData object's groupid_ref field*/
    public void setGroupIdRef(String groupid_ref) 
        { this.groupid_ref = groupid_ref; }
        /** Mutator for a RelationshipsData object's child HashMap */
    public void setChild(HashMap child) { this.child = child; }
        /** Mutator for a RelationshipsData object's parent HashMap */
    public void setParent(HashMap parent) { this.parent = parent; }
        /** Mutator for a RelationshipsData object's sibling HashMap */
    public void setSibling(HashMap sibling) { this.sibling = sibling; }
        /** Mutator for a RelationshipsData object's alias HashMap */
    public void setAlias(HashMap alias) { this.alias = alias; }
        /** Mutator for a RelationshipsData object's source field*/
    public void setSource(String source) { this.source = source; }

        /** Additional Mutator for a RelationshipsData object's child HashMap */
    public void addChild(String key, String value) { child.put(key,value); }
        /** Additional Mutator for a RelationshipsData object's parent HashMap */
    public void addParent(String key, String value) { parent.put(key,value); }
        /** Additional Mutator for a RelationshipsData's sibling HashMap */
    public void addSibling(String key, String value) { sibling.put(key,value); }
        /** Additional Mutator for a RelationshipsData object's alias HashMap */
    public void addAlias(String key, String value) { alias.put(key,value); }


    /** 
     * Returns a string representation of a <code>RelationshipsData</code> objet
     * 
     * @return Returns a string representation of <code>RelationshipsData</code> 
     *         in the format: 
     *           "source,child : parent : sibling : alias"
     */

    public String toString() 
    {
        StringBuffer str = new StringBuffer("");
        str.append(source).append(",");
        str.append(child).append(": ");
        str.append(parent).append(": ");
        str.append(sibling).append(": ");
        str.append(alias);
        
        return str.toString(); 
    }


    /** 
     * Compares one <code>RelationshipsData</code> object to another.
     * 
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *        
     */

    public boolean equals(Object other) {
        if ( !(other instanceof RelationshipsData) ) return false;
        RelationshipsData castOther = (RelationshipsData) other;

        if (this.toString().equals(castOther.toString())) return true;

        return false;
    }

}
