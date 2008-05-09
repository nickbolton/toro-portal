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


package com.interactivebusiness.classifieds.channel;

import org.jasig.portal.*;
import java.io.*;
import java.util.*;
import java.sql.Timestamp;
import java.sql.SQLException;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;

import org.jasig.portal.services.GroupService;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
//testing
import net.unicon.portal.util.GroupsSearch;
import com.interactivebusiness.classifieds.data.ClassifiedsDb;
//Jing Chen added
import com.interactivebusiness.portal.VersionResolver;

/**
 * This is the News Subscriber channel.
 * Users can subscribe and view new channels by topic names.
 * @author Freddy Lopez, flopez@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class Toolbar
{
  private static VersionResolver vr = VersionResolver.getInstance();
  private GroupsSearch gs = new GroupsSearch ();

  public Toolbar ()
  {
  }


  public ArrayList getToolBar (String userID)
  {

   return null;
  }

  public Element displayIcon (String IconName, Document doc)
  {
    Element IconElement = doc.createElement(IconName);
    IconElement.appendChild(doc.createTextNode(""));
    return IconElement;
  }

  public Element displayIcon (String IconName, IPerson person, String groupID, Document doc) throws GroupsException
  {
    // check if user is part of GroupID passed
    try
    {
    boolean userExists = checkIfUserInGroup (person, groupID);
    if (userExists)
    {
      Element IconElement = doc.createElement(IconName);
      IconElement.appendChild(doc.createTextNode(""));
      return IconElement;
    }
    return null;
    }catch (GroupsException ge)
    {
    throw ge;
    }
  }

  public Element displayIcon (String IconName, IPerson person, ClassifiedsDb cl, Document doc) throws GroupsException
  {

      int userID = person.getID();
    if (IconName.equals("statusIcon"))
    {
      if (cl.getUserHasItems(Integer.toString(userID)))
      {
        Element IconElement = doc.createElement(IconName);
        return IconElement;
      }
    }

    return null;
  }

  public Element displayIcon (String IconName, IPerson person, ArrayList allGroupIDs, Document doc) throws GroupsException
  {
    int userID = person.getID();
    String userName = (String) person.getAttribute("username");
    String portal_version = VersionResolver.getPortalVersion();
    // check if user is part of GroupID passed
    boolean userExists = false;
// *** JK - fix for TT03731
//    ArrayList myGroups = gs.getMyGroups(Integer.toString(userID));
    ArrayList myGroups = gs.getMyGroups(userName);

    for (int i=0; i < allGroupIDs.size(); i++)
    {
      String groupFound = (String) allGroupIDs.get(i);
      int dashat = groupFound.indexOf("-");
      String group = groupFound.substring(0, dashat);
      String entity = groupFound.substring(dashat+1);

      // if 3 then it is a group
      if (entity.equals("3"))
      {
        if (myGroups.contains(group))
        {
          userExists = true;
          break;
        }
      }
      else
      {
        // group found is a userID
        if ((portal_version.startsWith("2.0") && group.equals(Integer.toString(userID)))||(portal_version.startsWith("2.1")&&group.equals(userName)))
        {
          userExists = true;
          break;
        }
      }
    }

    if (userExists)
    {
      Element IconElement = doc.createElement(IconName);
      IconElement.appendChild(doc.createTextNode(""));
      return IconElement;
    }
    return null;
  }

  public boolean checkIfUserInGroup (IPerson person, String groupID) throws GroupsException
  {
      IEntityGroup ie = GroupService.getDistinguishedGroup (groupID);
      if (ie != null) {
          return ie.deepContains(GroupService.getGroupMember(person.getEntityIdentifier()));
      }
      return false;
  }

  public boolean checkPermission (IPerson person, String p1, String p2, String p3)
  {
    try
    {
      //AuthorizationService authService = AuthorizationService.instance();
      //IAuthorizationPrincipal ap = authService.newPrincipal(userID, IPerson.class);

      IAuthorizationPrincipal ap = vr.getPrincipalByPortalVersions (person);

      if (ap.hasPermission(p1, p2, p3))
	      return true;

      return false;
    } catch (Exception e)
    {
      return false;
    }


  }

}
