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


package net.unicon.portal.util;

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
import org.jasig.portal.services.EntityNameFinderService;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import com.interactivebusiness.portal.VersionResolver;

public class GroupsSearch
{
  private static VersionResolver vr = VersionResolver.getInstance();
  public GroupsSearch (){}

  public Set buildSet (ArrayList selectedGroups, String groupSelected) throws GroupsException
  {

    if (!selectedGroups.isEmpty())
      selectedGroups.clear();

    // get group handle
    IEntityGroup currentGroup = GroupService.findGroup(groupSelected);


    // get iterator of all parents
    Iterator iter = currentGroup.getAllContainingGroups();

    while (iter.hasNext())
    {
      IEntityGroup parentFound = (IEntityGroup)iter.next();
      String groupID = parentFound.getKey();

    }

    iter = currentGroup.getContainingGroups();
    while (iter.hasNext())
    {
      IEntityGroup parentFound = (IEntityGroup)iter.next();
      String groupID = parentFound.getKey();

    }




    return null;
  }

  public void addSelection (ArrayList selectedGroups, String groupSelected) throws GroupsException
  {
    // this is a little link list algorithm :)
    boolean foundGroupInList = false;
    String parent = null;
    //get size of list
    int listSize = selectedGroups.size();
    // fill in ArrayList with 0's
    ArrayList temp = new ArrayList (listSize);
    for (int i=0; i < listSize; i++)
      temp.add("0");

    // Iterate through selectedGroups TreeSet ex:[0,1,5]
    // and re-order set to [5,1,0]
    Iterator iter = selectedGroups.iterator();
    for (int i = 0; iter.hasNext(); i++)
    {
      String found = (String)iter.next();
      temp.set((listSize - (i+1)), found);
    }

    for (int i = 0; i < temp.size(); i++)
    {
      // first see who is the parent of the last item in list
      parent = (String) temp.get(i);
      IEntityGroup parentGroupFound = GroupService.findGroup(parent);
      Iterator groupIter = parentGroupFound.getMembers();
      while (groupIter.hasNext())
      {
        IGroupMember member = (IGroupMember)groupIter.next();
        if (member.isGroup())
        {
          IEntityGroup memberGroup = (IEntityGroup)member;
          String key = memberGroup.getKey();
          if (groupSelected.equals(key))
            foundGroupInList = true;
        }

      }

      if (!foundGroupInList)
      {
      selectedGroups.remove(parent);}
      else
      {
        selectedGroups.add(groupSelected);
        break;
      }
    }
  }

  public Element getGroupElement (String groupSelected, Document doc) throws GroupsException
  {

    IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
    // Create a top-level group representing everyone
    Element everyoneGroupE = doc.createElement("group");
    everyoneGroupE.setAttribute("ID", everyoneGroup.getKey());
    everyoneGroupE.setAttribute("name", everyoneGroup.getName());
    everyoneGroupE.setAttribute("description", everyoneGroup.getDescription());
    everyoneGroupE.setAttribute("entity", "3");
    everyoneGroupE.setAttribute("expand", "no");

    if (groupSelected != null)
      everyoneGroupE.setAttribute("selected", (everyoneGroup.getKey().equals(groupSelected) ? "yes" : "no"));
    return everyoneGroupE;
  }

  public void expandGroup (ArrayList selectedGroups, Element parentGroup, String groupSelected) throws GroupsException
  {
    IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
    // Create a top-level group representing everyone
    Document groupsDoc = parentGroup.getOwnerDocument();
    Element everyoneGroupE = groupsDoc.createElement("group");
    everyoneGroupE.setAttribute("ID", everyoneGroup.getKey());
    everyoneGroupE.setAttribute("name", everyoneGroup.getName());
    everyoneGroupE.setAttribute("description", everyoneGroup.getDescription());
    everyoneGroupE.setAttribute("entity", "3");
    everyoneGroupE.setAttribute("expand", "yes");

    if (groupSelected != null)
      everyoneGroupE.setAttribute("selected", (everyoneGroup.getKey().equals(groupSelected) ? "yes" : "no"));

    if (!selectedGroups.isEmpty())
    {
      Iterator iter = everyoneGroup.getMembers();
      while (iter.hasNext())
      {
        IGroupMember member = (IGroupMember)iter.next();
        if (member.isGroup())
        {

          IEntityGroup memberGroup = (IEntityGroup)member;
          String groupkey = memberGroup.getKey();
          String name = memberGroup.getName();
          String description = memberGroup.getDescription();

          groupsDoc = parentGroup.getOwnerDocument();
          Element groupE = groupsDoc.createElement("group");
          groupE.setAttribute("ID", groupkey);
          groupE.setAttribute("name", name);
          groupE.setAttribute("description", description);
          groupE.setAttribute("entity", "3");
          // added to check if user has expanded group
          groupE.setAttribute("expand", (selectedGroups.contains(groupkey) ? "yes" : "no"));
          // added to check if is leafnode (contains no subgroups)
          groupE.setAttribute("leafnode", checkIfLeafNode(groupkey));
          // added to check if is last group in subgroup list (for proper image display)
          groupE.setAttribute("isLast", (iter.hasNext() ? "no" : "yes"));

          if (groupSelected != null)
            groupE.setAttribute("selected", (groupkey.equals(groupSelected) ? "yes" : "no"));

          if (selectedGroups.contains(groupkey))
          {
            IEntityGroup groupP = GroupService.findGroup(groupkey);
            expandSubGroup (selectedGroups, groupP, groupE, groupSelected);
          }
          everyoneGroupE.appendChild(groupE);
        }
      }
    }
    parentGroup.appendChild(everyoneGroupE);
  }

  // added to do person search
  public void expandGroup (ArrayList selectedGroups, Element parentGroup, String groupSelected, String isLeafNode, String userID) throws GroupsException
  {

  // DEBUG--
//   Iterator i = selectedGroups.iterator();
 //  while (i.hasNext())
 //  {
 //    String foundthis = (String)i.next();
//   }


  // End DEBUG --

    IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
    // Create a top-level group representing everyone
    Document groupsDoc = parentGroup.getOwnerDocument();
    Element everyoneGroupE = groupsDoc.createElement("group");
    everyoneGroupE.setAttribute("ID", everyoneGroup.getKey());
    everyoneGroupE.setAttribute("name", everyoneGroup.getName());
    everyoneGroupE.setAttribute("description", everyoneGroup.getDescription());
    everyoneGroupE.setAttribute("entity", "3");
    everyoneGroupE.setAttribute("expand", "yes");

    if (groupSelected != null)
      everyoneGroupE.setAttribute("selected", (everyoneGroup.getKey().equals(groupSelected) ? "yes" : "no"));

    if (!selectedGroups.isEmpty())
    {
      Iterator iter = everyoneGroup.getMembers();
      while (iter.hasNext())
      {
        IGroupMember member = (IGroupMember)iter.next();


        if (member.isGroup())
        {
          IEntityGroup memberGroup = (IEntityGroup)member;
          String groupkey = memberGroup.getKey();
          String name = memberGroup.getName();
          String description = memberGroup.getDescription();

          groupsDoc = parentGroup.getOwnerDocument();
          Element groupE = groupsDoc.createElement("group");
          groupE.setAttribute("ID", groupkey);
          groupE.setAttribute("name", name);
          groupE.setAttribute("description", description);
          groupE.setAttribute("entity", "3");
          // added to check if user has expanded group
          groupE.setAttribute("expand", (selectedGroups.contains(groupkey) ? "yes" : "no"));
          // added to check if is leafnode (contains no subgroups)
          groupE.setAttribute("leafnode", checkIfLeafNode(groupkey));
          // added to check if is last group in subgroup list (for proper image display)
          groupE.setAttribute("isLast", (iter.hasNext() ? "no" : "yes"));

          if (groupSelected != null)
            groupE.setAttribute("selected", (groupkey.equals(groupSelected) ? "yes" : "no"));

          if (selectedGroups.contains(groupkey))
          {

            IEntityGroup groupP = GroupService.findGroup(groupkey);
            expandSubGroup (selectedGroups, groupP, groupE, groupSelected, isLeafNode, userID);
          }

        if (groupSelected != null && groupSelected.equals (groupkey))
        {

          getPersonsInGroup (groupSelected, groupE, userID);
        }
          everyoneGroupE.appendChild(groupE);
        }
      }

    }
    parentGroup.appendChild(everyoneGroupE);
  }

  public void getPersonsInGroup (String groupSelected, Element parentGroup, String userID)
  {

    try{


    IEntityGroup groupFound = GroupService.findGroup(groupSelected);

    Iterator iter =  groupFound.getEntities();

    if (!iter.hasNext())
    {
      // no people found in group!
      Document groupsDoc = parentGroup.getOwnerDocument();
      Element groupE = groupsDoc.createElement("person");
      groupE.setAttribute("isPerson", "no");
      parentGroup.appendChild(groupE);
    }

    while (iter.hasNext())
    {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isEntity())
      {


        String key = member.getKey();


        if (key.equals(userID))
          continue;

        Document groupsDoc = parentGroup.getOwnerDocument();
        Element groupE = groupsDoc.createElement("person");
        groupE.setAttribute("ID", key);
        groupE.setAttribute("name", getReadableName ("person", key));
        groupE.setAttribute("entity", "2");
        groupE.setAttribute("isPerson", "yes");
        groupE.setAttribute("isLast", (iter.hasNext() ? "no" : "yes"));
       parentGroup.appendChild(groupE);
      }
    }
   }catch (GroupsException ge)
   {

   }
  }

  public String getReadableName (String type, String ID)
  {
    String nameFound = null;
    try
    {
      if (type.equals("person"))
      {
        // with this userID, find name
        nameFound = EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.security.IPerson")).getName(ID);
      }
      else
      {
        nameFound = EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.groups.IEntityGroup")).getName(ID);
      }
    }catch (java.lang.ClassNotFoundException cnfe)
    {
    }catch (GroupsException ge)
    {
    }catch (Exception e)
    {
    }

    if (nameFound == null)
    {
      // hash table is out of sync recreate it
    }
    return nameFound;
  }

  public String checkIfLeafNode (String groupToCheck) throws GroupsException
  {
    String isLeafNode = "yes";
    IEntityGroup groupFound = GroupService.findGroup(groupToCheck);
    Iterator groupIter = groupFound.getMembers();
    while (groupIter.hasNext())
    {
      IGroupMember member = (IGroupMember)groupIter.next();
      if (member.isGroup())
      {
        isLeafNode = "no";
        break;
      }
    }
    return isLeafNode;
  }

  // added to do person search
  public void expandSubGroup (ArrayList selectedGroups, IEntityGroup group, Element parentGroup, String groupSelected, String isLeafNode, String userID) throws GroupsException
  {
    Iterator iter = group.getMembers();
    while (iter.hasNext())
    {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup())
      {
        IEntityGroup memberGroup = (IEntityGroup)member;

        String key = memberGroup.getKey();
        String name = memberGroup.getName();
        String description = memberGroup.getDescription();
        Document groupsDoc = parentGroup.getOwnerDocument();
        Element groupE = groupsDoc.createElement("group");
        groupE.setAttribute("ID", key);
        groupE.setAttribute("name", name);
        groupE.setAttribute("description", description);
        groupE.setAttribute("entity", "3");
        groupE.setAttribute("expand", (selectedGroups.contains(key) ? "yes" : "no"));
        groupE.setAttribute("leafnode", checkIfLeafNode(key));
        // added to check if is last group in subgroup list (for proper image display)
        groupE.setAttribute("isLast", (iter.hasNext() ? "no" : "yes"));

        if (groupSelected != null)
          groupE.setAttribute("selected", (key.equals(groupSelected) ? "yes" : "no"));


        if (selectedGroups.contains(key))
        {
            String isLeaf = checkIfLeafNode (key);

            if (isLeaf.equals("yes"))
            {

              getPersonsInGroup (groupSelected, groupE, userID);
            }
            else
            {
            IEntityGroup groupP = GroupService.findGroup(key);
            expandSubGroup (selectedGroups, groupP, groupE, groupSelected, isLeafNode, userID);
            }
        }

        parentGroup.appendChild(groupE);


      }
    }
  }

  public void expandSubGroup (ArrayList selectedGroups, IEntityGroup group, Element parentGroup, String groupSelected) throws GroupsException
  {

    Iterator iter = group.getMembers();
    while (iter.hasNext())
    {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup())
      {
        IEntityGroup memberGroup = (IEntityGroup)member;

        String key = memberGroup.getKey();
        String name = memberGroup.getName();
        String description = memberGroup.getDescription();
        boolean expand = selectedGroups.contains(key);

        Document groupsDoc = parentGroup.getOwnerDocument();
        Element groupE = groupsDoc.createElement("group");
        groupE.setAttribute("ID", key);
        groupE.setAttribute("name", name);
        groupE.setAttribute("description", description);
        groupE.setAttribute("entity", "3");
        groupE.setAttribute("expand", (expand ? "yes" : "no"));
        groupE.setAttribute("leafnode", checkIfLeafNode(key));
        // added to check if is last group in subgroup list (for proper image display)
        groupE.setAttribute("isLast", (iter.hasNext() ? "no" : "yes"));

        groupE.setAttribute("selected", (expand ? "yes" : "no"));

        if (expand)
        {
          IEntityGroup groupsel = GroupService.findGroup(key);
          expandSubGroup (selectedGroups, groupsel, groupE, groupSelected);
        }
        parentGroup.appendChild(groupE);
      }
    }
  }

  public ArrayList userSearch (String userName)
  {

    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try
    {
        con = RDBMServices.getConnection();
        if (userName.indexOf("*") != -1)
        {
          // user entered a '*', will replace with '%'
          if (userName.trim().equals("*"))
            userName = "%";
          else
            userName = userName.replace('*', '%');
        }
        else
        {
          userName += "%";
        }
        StringBuffer queryBuf = new StringBuffer();
        String userKeyColumn = vr.getUserKeyColumnByPortalVersions(); 
        queryBuf.append("SELECT ");
        queryBuf.append(userKeyColumn);
        queryBuf.append(" FROM UP_USER WHERE USER_NAME LIKE ");
        queryBuf.append(" '%' || ? || '%' ");
        stmt = con.prepareStatement(queryBuf.toString());
        stmt.setString(1, userName);

        rs = stmt.executeQuery();
        ArrayList usersFound = new ArrayList ();

        while (rs.next())
        {
          usersFound.add(rs.getString(1));
        }

        if (usersFound.isEmpty())
          usersFound.add("notfound");

      return usersFound;
    }catch (Exception e)
    {
      e.printStackTrace();
    } finally {
        try {
            if (rs != null) rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RDBMServices.releaseConnection(con);
    }
    return null;
  }

  public ArrayList getMyGroups (String userID)
  {
  	ArrayList myGroupsList = new ArrayList ();
    try
    {

     IEntityGroup ie = GroupService.getRootGroup(Class.forName("org.jasig.portal.security.IPerson")); // the type
     /* Iterator iter = ie.getMembers();
      while (iter.hasNext())
      {
        IGroupMember member = (IGroupMember)iter.next();
        if (member.isGroup())
        {            
          IEntityGroup memberGroup = (IEntityGroup)member;
          String groupkey = memberGroup.getKey();
          String name = memberGroup.getName();
          
          IEntityGroup group = GroupService.findGroup(groupkey);
          checkSubGroup (group, userID, myGroupsList);
        }
        else if(member.isEntity())
        {
          // Do something with this child entity.
          String id = member.getKey(); // it is the id in table UP_USER
          if (id.equals(userID))
          {
             myGroupsList.add(ie.getKey());
          }          
        }
     }
   // will use this if I decide to create a TopicsAdmin and PublishAdmin group
   // IEntityGroup ie = GroupService.findGroup(groupID);

     // if user is not mapped to group Everyone in up_groups table, then add to list
     if (!myGroupsList.contains("0"))
     {
         if (com.interactivebusiness.portal.VersionResolver.getPortalVersion().startsWith("2.0"))
            myGroupsList.add("0");
         else if (com.interactivebusiness.portal.VersionResolver.getPortalVersion().startsWith("2.1"))
             myGroupsList.add("local.0");
     }     */
    	
       IGroupMember user = GroupService.getGroupMember( userID, Class.forName("org.jasig.portal.security.IPerson"));
        
       Iterator iter = user.getAllContainingGroups();
       while (iter.hasNext())
       {
         IGroupMember member = (IGroupMember)iter.next();
         myGroupsList.add(member.getKey());
       }
       
       // if user is not mapped to group Everyone in up_groups table, then add to list
       if (!myGroupsList.contains(ie.getKey()))
       {
           if (com.interactivebusiness.portal.VersionResolver.getPortalVersion().startsWith("2.0"))
              myGroupsList.add("0");
           else if (com.interactivebusiness.portal.VersionResolver.getPortalVersion().startsWith("2.1"))
              myGroupsList.add(ie.getKey());
       }     
       
    }
    catch (GroupsException ge)
    {

    return myGroupsList;
    }
    catch (java.lang.ClassNotFoundException cnfe)
    {
    return myGroupsList;
    }

    return myGroupsList;
}

  public void checkSubGroup (IEntityGroup group, String userID, ArrayList myGroupsList) throws GroupsException
  {

     Iterator iter = group.getMembers();
    while (iter.hasNext())
    {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup())
      {
        IEntityGroup memberGroup = (IEntityGroup)member;

        String key = memberGroup.getKey();
        String name = memberGroup.getName();

          IEntityGroup groupsel = GroupService.findGroup(key);
          checkSubGroup (groupsel, userID, myGroupsList);
      }
      else if(member.isEntity())
      {
        // Do something with this child entity.
        String id = member.getKey(); // it is the id in table UP_USER
          if (id.equals(userID))
          {
             myGroupsList.add(group.getKey());
          }

      }
    }
  }

  public String checkIfGroupOrPerson (String entityType)
  {

      // need to check if groupID is IEntityGroup or isGroup
    //  IEntityGroup currentGroup = GroupService.findGroup(groupID);
    if (entityType.equals("3"))
      return "group";
    else if (entityType.equals("2"))
      return "person";
    return null;
  }

}

