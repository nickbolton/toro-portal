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

package net.unicon.civis;

public interface IGroup extends ICivisEntity {

    final String GROUP_PATH_SEPARATOR = " - ";
    
    IPerson[] getMembers(boolean deep);

    /**
     * Obtains all the groups that are one lever under the current group.
     * @return
     */
    IGroup[] getSubgroups();
    
    /**
     * Obtains all the groups in the tree under the 
     * current group.
     * @return Array of groups
     */
    IGroup[] getDescendentGroups();
    
    
    /**
     * Obtains the path of the group from the root group
     * @return String path
     */
    String getPath();
    
    /**
     * Obtains the path of the group from the root group with
     * the groupId appended in brackets []
     * @return String path[Id]
     */
    String getPathAndId();
    
    /**
     * Obtains the parent group that was traversed
     * to retrieve this group.
     * The parent group for the root group will be null.
     * @return gets the parent group 
     */
    //IGroup getParent();				

}