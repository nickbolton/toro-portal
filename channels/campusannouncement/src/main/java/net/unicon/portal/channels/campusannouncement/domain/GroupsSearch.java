package net.unicon.portal.channels.campusannouncement.domain;

import java.util.ArrayList;
import java.util.Iterator;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;

public class GroupsSearch
{
  public GroupsSearch (){}

  public ArrayList getMyGroups (String userID)
  {
  	//  modified to get the groups membership from the user to make it work with PAGS.
	
  	 ArrayList myGroupsList = new ArrayList ();
     try
     {
     	IGroupMember user = GroupService.getGroupMember( userID, Class.forName("org.jasig.portal.security.IPerson"));
        
       IEntityGroup ie = GroupService.getRootGroup(Class.forName("org.jasig.portal.security.IPerson")); // the type
       Iterator iter = user.getAllContainingGroups();
       while (iter.hasNext())
       {
         IGroupMember member = (IGroupMember)iter.next();
        /* if (member.isGroup())
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
           {*/
              myGroupsList.add(member.getKey());              
          /* }
         }*/
      }
    // will use this if I decide to create a TopicsAdmin and PublishAdmin group
    // IEntityGroup ie = GroupService.findGroup(groupID);

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
        //System.out.println("Group: key="+key+" name="+name);
          IEntityGroup groupsel = GroupService.findGroup(key);
          checkSubGroup (groupsel, userID, myGroupsList);
      }
      else if(member.isEntity())
      {
        // Do something with this child entity.
        String id = member.getKey(); // it is the id in table UP_USER
                //System.out.println("\tperson="+id);
          if (id.equals(userID))
          {
            //System.out.println("\t\tGot it!");
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

