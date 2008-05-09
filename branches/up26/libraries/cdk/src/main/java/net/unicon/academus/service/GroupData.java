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
import java.util.Iterator;
import java.util.Map;

import net.unicon.academus.service.adapters.UCFImportAdapter;



public class GroupData extends ABaseData implements Serializable
{

    /** identifier field */
    private String groupId;
    private String groupSource;

        // info fields
    private String shortName;
    private String title;
    private String description;

        // optional data
    private String[] parentIDs;
    private HashMap data;

    /** constructor */
    public GroupData(String id, String source) {
        super(id, source);
    }

   /**
    * Constructor for a <code>GroupData</code> object.
    *
    * @param  groupId       The group's ID
    * @param  groupSource   The group's source
    * @param  shortname     The group's name
    * @param  title         The group's title
    * @param  description   The group's description
    * @param  data          Extra data applicable to the group
    *
    * @see    UCFImportAdapter
    */

    public GroupData(String groupId, String groupSource,
                     String shortName, String title,
                     String description, RelationshipsData relationData,
                     HashMap data)
    {
        super(groupId, groupSource);

        /* ASSERTIONS */
        // check shortName 
        if (!(shortName != null && shortName.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Argument 'shortName [String]' must be provided.");
        }

        this.groupId = groupId;
        this.shortName = shortName;
        this.title = title;
        this.description = description;
        this.data = data;

        // turn relationData into parent data
        if (relationData != null) {
            Map parents     = relationData.getParent();
            int parentNum   = 0;

            if (parents != null && (parentNum = parents.size()) > 0) {
               parentIDs = new String[parentNum];
               parentNum = 0;

               for (Iterator iter = parents.keySet().iterator(); iter.hasNext();) {
                    parentIDs[parentNum++] = (String)iter.next();
               }
            } else {
                parentIDs = new String[0];
            }
        } else {
            parentIDs = new String[0];
        }

    } // end constructor

        /** Accessor for a Group objects parent IDs */
    public String[] getParentIDs() { return parentIDs; }
        /** Accessor for a Group objects groupId field */
    public String getGroupId() { return this.groupId; }
        /** Accessor for a Group objects groupSource field */
    public String getShortName() { return this.shortName; }
        /** Accessor for a Group objects title field */
    public String getTitle() { return this.title; }
        /** Accessor for a Group objects email field */
    public String getDescription() { return this.description; }
        /** Accessor for a Group objects data field */
    public HashMap getData() { return this.data; }

        /** Mutator for a Group objects parentIDs */
    public void setParentIDs(String[] parentIDs) {
        this.parentIDs = new String[parentIDs.length];
        System.arraycopy(parentIDs, 0, this.parentIDs, 0, parentIDs.length);
    }
        /** Mutator for a Group objects groupId field */
    public void setGroupId(String groupId) { this.groupId = groupId; }
        /** Mutator for a Group objects groupSource field */
    public void setGroupSource(String groupSource)
        { this.groupSource = groupSource; }
        /** Mutator for a Group objects first name field */
    public void setShortName(String shortName) { this.shortName = shortName; }
        /** Mutator for a Group objects title field */
    public void setTitle(String title) { this.title = title; }
        /** Mutator for a Group objects email field */
    public void setDescription(String description)
        { this.description = description; }
        /** Mutator for a Group objects data field */
    public void setData(HashMap data) { this.data = data; }

    /**
     * Returns a string representation of a <code>GroupData</code>
     *
     * @return Returns a string representation of a <code>GroupData</code> in the
     *         format: "group id, group source, short name,
     *                  title, description, data"
     */
    public String toString()
    {
        return groupId + "," + groupSource + "," + shortName + ","
                       + title + "," + description + "," + data;
    }


    /**
     * Compares one <code>GroupData</code> object to another.
     *
     * @return true if both objects have the same values for their fields and
     *         false otherwise
     *
     */

    public boolean equals(Object other) {
        if ( !(other instanceof GroupData) ) return false;
        GroupData castOther = (GroupData) other;

        if (groupId.equals(castOther.getGroupId()) &&
            groupSource.equals(castOther.getSource()) &&
            shortName.equals(castOther.getShortName()) &&
            title.equals(castOther.getTitle()) &&
            description.equals(castOther.getDescription()))
            return true;

        return false;
    }

}
