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




package net.unicon.portal.groups.permissions;

import net.unicon.portal.groups.IGroupPermissionsManager;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import java.util.Iterator;

/**
 *
 *@author John Bodily
 *@version 1.0
 */

public class UniconGroupPermissionsManager implements IGroupPermissionsManager {

  //singleton instance for this manager
  protected static IGroupPermissionsManager instance = null;
  //handle to the singleton instance of the authorization service.
  private IAuthorizationService ias = null;
  //constant name for the UNICON group member class
  public static String IS_USER = "net.unicon.portal.groups.IMember";
  //constant name for the UNICON group class 
  public static String IS_GROUP = "net.unicon.portal.groups.IGroup";
  //constant name for the GroupsManager...usually this will be the
  //owner of the permission while the GroupsManager channel is used.
  public static String GROUPSMANAGERCHAN = "org.jasig.portal.channels.groupsmanager.CGroupsManager"; 

 /**
  *Constructor for the UniconGroupPermissionsManager
  */

  public UniconGroupPermissionsManager(){ 
    super();
 
    ias = AuthorizationImpl.singleton();
  }

  /**
   *Gets a handle to the singleton instance of the group permissions manager.
   *@return IGroupPermissionsManager
   */

  public static synchronized IGroupPermissionsManager getInstance(){
    if(instance == null){
        instance = new UniconGroupPermissionsManager();
    }

    return instance;
  }

  /**
   *Answers if the principal can perform the activity on the target, returns true for GRANT, false for DENY, and throws an exception if the principal is found to have no permissions set.
   *@param owner The channel or framework that owns the permission.
   *@param principal A string to uniquely identify the principal (username or groupID)
   *@param target A string to uniquely identify the target (username or groupID)
   *@param principalType The class type of the principal (should be a fully-qualified package name).
   *@param activity Description for the activity being performed ex. "SELECT".
   *@return boolean
   */

  public boolean hasPermission(String owner, String principal, String target, String principalType, String activity) throws Exception{
      
    boolean ret = false;
    Class gmemberClass = null;
    IAuthorizationPrincipal iap = null;
 
    try{

       //Need to construct the principal with the correct jasig class.
       if(principalType.equalsIgnoreCase(IS_USER)){
         gmemberClass = Class.forName("org.jasig.portal.security.IPerson");
       }else if(principalType.equalsIgnoreCase(IS_GROUP)){
         gmemberClass = Class.forName("org.jasig.portal.groups.IEntityGroup");
       }

       //Get the new principal based on the class type.
       iap = ias.newPrincipal(principal, gmemberClass);

       if(iap != null){
          //Test for the permission to select.
          ret = hasPermission(owner, iap, target, activity);
       }

    }catch(Exception e){
       LogService.log(LogService.ERROR,e.toString());
       throw e;
    }

    return ret;
   
  }


  /**
   * Answers if the principal can perform the activity on the target, returns 
   * true for GRANT, false for DENY, and throws an exception if the principal 
   * is found to have no permissions set. This will check to see if the 
   * principal can perform the activity on the target first, then check to see 
   * if the principal can perform the activity on any of the parent groups. If
   * the activity is allowed on any parent group this method returns TRUE.
   *@param owner The channel or framework that owns the permission.
   *@param principal A string to uniquely identify the principal (username or groupID)
   *@param target A string to uniquely identify the target (username or groupID)
   *@param principalType The class type of the principal (should be a fully-qualified package name).
   *@param activity Description for the activity being performed ex. "SELECT".
   *@return boolean
   */
  public boolean inheritsPermission(String owner, String principal, String target, String principalType, String activity) throws Exception{
      
    boolean ret = false;
    Class gmemberClass = null;
    IAuthorizationPrincipal iap = null;
    IEntityGroup ieg = null;
 
    try{

       //Need to construct the principal with the correct jasig class.
       if(principalType.equalsIgnoreCase(IS_USER)){
         gmemberClass = Class.forName("org.jasig.portal.security.IPerson");
       }else if(principalType.equalsIgnoreCase(IS_GROUP)){
         gmemberClass = Class.forName("org.jasig.portal.groups.IEntityGroup");
       }

       //Get the new principal based on the class type.
       iap = ias.newPrincipal(principal, gmemberClass);

       if(iap != null){
          //Test for the permission to perform the activity.
          try{
            ret = hasPermission(owner, iap, target, activity);
          }catch(Exception e){//consume this exception
          }
       }

       if(!ret){
         //The principal didn't have the permission to perform the activity
         //explicitly. Now we need to see if they can perform the activiy
         //on _any_ of the parent groups.
         ieg = GroupService.findGroup(target);

         Iterator itr = ieg.getAllContainingGroups();
         while(itr.hasNext() && !ret){
           IEntityGroup group = (IEntityGroup)itr.next();

           if(iap != null){
             try{
               ret = hasPermission(owner, iap, group.getKey(), activity);
             }catch(Exception e){//consume this exception
             } 
           }
           
         } 
       }

    }catch(Exception e){
       LogService.log(LogService.ERROR,e.toString());
       throw e;
    }

    return ret;
   
  }


  /**
   * Check to see if the principal has permissions to select the target.
   * @param owner The channel that owns this permission. 
   * @param principal IAuthorization object for the principal.
   * @param key The username or groupID for the target.
   * @param activity The type of Activity to be performed.
   */
  public boolean hasPermission(String owner, IAuthorizationPrincipal principal, String key, String activity) throws AuthorizationException{
    boolean isAllowed = false;

    if(key != null){
      try{
         //make the check..an exception is only thrown if the principal has
         //no tuples in the database for permissions.
         isAllowed = principal.hasPermission(owner,activity,key);
      }catch(AuthorizationException ae){
         throw ae;
      }

    }

    return isAllowed;
  }

    public static void unitTest(){
     IGroupPermissionsManager ugpm = UniconGroupPermissionsManager.getInstance();
    
     boolean test = false;

     try{
       test = ugpm.hasPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","jbodily","local.8","net.unicon.portal.groups.IMember","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for jbodily");
     }

     if(test){
        System.err.println("User jbodily is allowed to select local.8\n");
     }else{
        System.err.println("User jbodily is not allowed to select local.8\n");
     }

     test = false;

     try{
        test = ugpm.hasPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","local.2024","local.2018","net.unicon.portal.groups.IGroup","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for local.2024 on local.2018\n");
     }

     if(test){
        System.err.println("Group local.2024 can select local.2018\n");
     }else{
        System.err.println("Group local.2024 cannot select local.2018\n");
     }

    test = false;

    try{
        test = ugpm.hasPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","local.2024","local.8","net.unicon.portal.groups.IGroup","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for local.2024 on local.8\n");
     }

     if(test){
        System.err.println("Group local.2024 can select local.8\n");
     }else{
        System.err.println("Group local.2024 cannot select local.8\n");
     }

    test = false;

    try{
        test = ugpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","local.128","local.0","net.unicon.portal.groups.IGroup","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for local.128 on local.0\n");
     }

     if(test){
       System.err.println("local.128 can select local.0\n");
     }else{
       System.err.println("local.128 cannot select local.0\n");
     }

     test = false;

     try{
        test = ugpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","jbodily","local.2020","net.unicon.portal.groups.IMember","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for jbodily on local.2020\n");
     }
    
    if(test){
       System.err.println("jbodily can select local.2020\n");
    }else{
      System.err.println("jbodily cannot select local.2020\n");
    }

     test = false;

     try{
        test = ugpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager","jbodily","local.2800","net.unicon.portal.groups.IMember","SELECT");
     }catch(Exception e){
       System.err.println("No permissions defined for jbodily on local.2800\n");
     }
    
    if(test){
       System.err.println("jbodily can select local.2800\n");
    }else{
      System.err.println("jbodily cannot select local.2800\n");
    }    

  }


}
