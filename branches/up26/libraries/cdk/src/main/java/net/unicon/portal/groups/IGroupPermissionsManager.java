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



package net.unicon.portal.groups;


/**
 * IGroupsPermissionsManager is used to couple together groups and permissions
 * for the unicon groups package.
 * @author John Bodily
 * @version 1.0
 */

public interface IGroupPermissionsManager {
    /**
   *Answers if the principal can perform the activity on the target, returns true for GRANT, false for DENY, and throws an exception if the principal is found to have no permissions set.
   *@param owner The channel or framework that owns the permission.
   *@param principal A string to uniquely identify the principal (username or groupID)
   *@param target A string to uniquely identify the target (username or groupID)
   *@param principalType The class type of the principal (should be a fully-qualified package name).
   *@param activity Description for the activity being performed ex. "SELECT".
   *@return boolean
   */

  public boolean hasPermission(String owner, String principal, String target, String principalType, String activity) throws Exception;
  
/**
   * Answers if the principal can perform the activity on the target, returns
   * true for GRANT, false for DENY, and throws an exception if the principal
   * is found to have no permissions set. This will check to see if the
   * principal can perform the activity on the target first, then check to see
   * if the principal can perform the activity on any of the parent groups. If
   * the activity is allowed on any parent group this method returns TRUE.
   *@param owner The channel or framework that owns the permission.
   *@param principal A string to uniquely identify the principal (username or groupID)
   *@param target A string to uniquely identify the target (username or groupID)   *@param principalType The class type of the principal (should be a fully-qualified package name).
   *@param activity Description for the activity being performed ex. "SELECT".
   *@return boolean
   */

   public boolean inheritsPermission(String owner, String principal, String target, String principalType, String activity) throws Exception;

}
