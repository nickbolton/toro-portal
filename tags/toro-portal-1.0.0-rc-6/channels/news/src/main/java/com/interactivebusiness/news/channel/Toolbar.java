/*
 *
 * Copyright (c) 2001 - 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with IBS.
 *
 * IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 *  $Log:
 *   2    Channels  1.1         12/20/2001 3:54:08 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:39 AMFreddy Lopez
 *  $
 *
 */


package com.interactivebusiness.news.channel;

import java.util.ArrayList;

import net.unicon.portal.util.GroupsSearch;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private static GroupsSearch gs = new GroupsSearch ();

  public Toolbar ()
  {
  }


  public ArrayList getToolBar (String userID)
  {
      return null;
  }

  public Element displayIcon (String IconName, Document doc) {
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

  public Element displayIcon (String IconName, IPerson person, ArrayList allGroupIDs, Document doc) throws GroupsException
  {
    
    // check if user is part of GroupID passed
    boolean userExists = canPublishArticles(person, allGroupIDs);    

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

  public static boolean checkPermission (IPerson person)
  {
    try
    {

        IAuthorizationPrincipal ap = vr.getPrincipalByPortalVersions (person);
      if (ap.hasPermission(CNews.CHANNEL, CNews.VIEW_TOPICS, "TOPICS_ICON"))
        return true;
      return false;
    } catch (Exception e)
    {
    // stay with friendly stylesheet
     return false;
    }


  }
  
  public static boolean canPublishArticles(IPerson person, ArrayList allGroupIDs){
      
      int userID = person.getID ();
      String userName = (String) person.getAttribute("username");
      String portal_version = com.interactivebusiness.portal.VersionResolver.getPortalVersion();
      String userKey = portal_version.startsWith("2.0") ? Integer.toString(userID) : userName;

      boolean userExists = false;
      ArrayList myGroups = gs.getMyGroups(userKey);

      for (int i=0; i < allGroupIDs.size() && userExists == false; i++)
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
      
      return userExists;
  }

}
