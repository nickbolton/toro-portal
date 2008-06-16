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
package net.unicon.portal.groups.framework.initializing;

import net.unicon.portal.util.db.AcademusDBUtil;

import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class GroupsInitializer {
	
  private static String MEMBER_IS_ENTITY = "F";
  private static String MEMBER_IS_GROUP = "T";
  private static String nodeSeparator;
  private static String defaultGroupService;

  static {
    try {
      nodeSeparator = GroupServiceConfiguration.getConfiguration().getNodeSeparator();
      defaultGroupService = GroupServiceConfiguration.getConfiguration().getDefaultService();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
      nodeSeparator = IGroupConstants.NODE_SEPARATOR;
    }
  }

  private static boolean okToProceed() {
    try {
      IEntityGroup group = GroupService.getDistinguishedGroup("org.jasig.portal.security.IPerson");
      if (group == null) return false;
      return group instanceof UniconEntityGroup;
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
    return false;
  }
	
  public static void initialize() {

    if (!okToProceed()) {
      LogService.log(LogService.INFO, "GroupsInitializer : incompatible entity group store. Configure the UniconEntityGroupStoreFactory if you want to use the GroupsInitializer.");
      return;
    }

	StringBuffer sql = new StringBuffer();
    sql.append("select group_id, member_service, member_key, member_is_group from up_group_membership");

    String groupId;
    String groupKey;
    String memberService;
    String memberKey;
    boolean memberIsGroup;
    
    Set entities = new HashSet();
    Map memberMap = new HashMap();

    IEntityGroup group;
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int count=0;
    try {
      conn = AcademusDBUtil.getDBConnection();
      pstmt = conn.prepareStatement(sql.toString());
      rs = pstmt.executeQuery();
      
      // First go through and get all the groups
      while (rs.next()) {
        groupId = rs.getString("group_id");
        memberService = rs.getString("member_service");
        memberKey = rs.getString("member_key");
        memberIsGroup = MEMBER_IS_GROUP.equalsIgnoreCase(rs.getString("member_is_group"));
	    groupKey = new StringBuffer(defaultGroupService).append(nodeSeparator).
	      append(groupId).toString();
        
        
	    Map entityTypeMap = (Map)memberMap.get(groupKey);
	    if (entityTypeMap == null) {
	    	entityTypeMap = new HashMap();
	    	memberMap.put(groupKey, entityTypeMap);
	    }
	    
	    Set groupMembers = (Set)entityTypeMap.get("groups");
        if (groupMembers == null) {
        	groupMembers = new HashSet();
          	entityTypeMap.put("groups", groupMembers);
        }
        
        Set entityMembers = (Set)entityTypeMap.get("entities");
        if (entityMembers == null) {
        	entityMembers = new HashSet();
        	entityTypeMap.put("entities", entityMembers);
        }
	    
        if (memberIsGroup) {
          StringBuffer key = new StringBuffer();
          key.append(memberService).append(nodeSeparator).append(memberKey);          
          GroupService.findGroup(key.toString());
          count++;
          
          groupMembers.add(new StringBuffer(defaultGroupService).append(nodeSeparator).
      	      append(memberKey).toString());
          
        } else {
          entities.add(new EntityInfo(memberKey,groupKey));
          
          entityMembers.add(memberKey);
        }
      }
    } catch (Exception e) {
      StringBuffer msg = new StringBuffer();
      msg.append("GroupMembership initialization failed ");
      msg.append("with the following message:\n");
      msg.append(e.getMessage());
      LogService.log(LogService.ERROR, msg.toString());
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      AcademusDBUtil.safeReleaseDBConnection(conn);
      rs = null;
      pstmt = null;
      conn = null;
    }
    
    LogService.log(LogService.INFO, "GroupsInitializer loaded " + count + " groups");
    
    // Now get all the entities
    getEntities(entities);
    
    // Now initialize all the member associations
    initializeMembers(memberMap);
    
    test();
  }
 
  
  private static void test() {
  	getAllPeople();
  }
  
  private static void getAllPeople() {
  	try {
        IEntityGroup everyone =
          GroupService.getDistinguishedGroup("org.jasig.portal.security.IPerson");
    
	    Date t1 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : seeding started...");
	    Iterator itr = everyone.getAllMembers();
	    Date t2 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : seeding took " + (t2.getTime() - t1.getTime()) + " ms");
	    
	    int numPeople=0;
	    while (itr.hasNext()) {
	    	itr.next();
	    	numPeople++;
	    }
	    LogService.log(LogService.INFO, "GroupsInitializer : seeded " + numPeople + " people");
  	} catch (Exception e) {
  		e.printStackTrace();
  	}
  }

  private static void everyoneDeepContainsGroup(String groupKey) {
    
  	try {
        IEntityGroup everyone =
          GroupService.getDistinguishedGroup("org.jasig.portal.security.IPerson");
    
	    IEntityGroup group = GroupService.findGroup(groupKey);
	    Date t1 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : starting deepContains...");
	    boolean val = everyone.deepContains(group);
	    Date t2 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : deep contains " + groupKey + " (" + val + ") took " + (t2.getTime() - t1.getTime()) + " ms");
	    
  	} catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  private static void everyoneDeepContainsEntity(String key) {
    
  	try {
        IEntityGroup everyone =
          GroupService.getDistinguishedGroup("org.jasig.portal.security.IPerson");
	    IEntity entity = GroupService.getEntity(key, IPerson.class);
	    Date t1 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : starting deepContains...");
	    boolean val = everyone.deepContains(entity);
	    Date t2 = new Date();
	    LogService.log(LogService.INFO, "GroupsInitializer : deep contains " + key + " (" + val + ") took " + (t2.getTime() - t1.getTime()) + " ms");
	    

  	} catch (Exception e) {
  	  LogService.log(LogService.ERROR, e);
  	}
  }
  
  private static void initializeMembers(Map memberMap) {
  
  	LogService.log(LogService.INFO, "GroupsInitializer : initializing members...");
    Date t1 = new Date();
  	int count=0;
  	int total=0;
  	try {
	  	String groupKey;
	  	IEntityGroup group;
	  	Iterator itr = memberMap.keySet().iterator();
	  	while (itr.hasNext()) {
	  		total++;
	  		groupKey = (String)itr.next();
	  		group = GroupService.findGroup(groupKey);
	  		
	  		if (group instanceof UniconEntityGroup) {
	  			count++;
	  			Map entityTypeMap = (Map)memberMap.get(groupKey);
	  			Set entities = (Set)entityTypeMap.get("entities");
	  			Set groups = (Set)entityTypeMap.get("groups");
	  			((UniconEntityGroup)group).initializeMembers(groups, entities);
	  		}
	  	}
  	} catch (Exception e) {
  		LogService.log(LogService.ERROR, e);
  	}
  	
  	Date t2 = new Date();
  	LogService.log(LogService.INFO, "GroupsInitializer : initialized " + count + " out of " + total);
  	LogService.log(LogService.INFO, "GroupsInitializer : initializing members took " + (t2.getTime() - t1.getTime()) + " ms");
   
  }
  
  private static void getEntities(Set entities) {
  	try {
  	  IEntityGroup group;
	  String groupKey;
	  EntityInfo info;
	  Iterator itr = entities.iterator();
	  int count=0;
	  while (itr.hasNext()) {
	    info = (EntityInfo)itr.next();
	    group = GroupService.findGroup(info.getParent());

        if (group != null) {
	      GroupService.getEntity(info.getKey(), group.getEntityType());
	      count++;
        } else {
          LogService.log(LogService.WARN, "GroupsInitializer found member whose parent group doesn't exist - parent/member: " + info.getParent() + "/" + info.getKey());
        }
	  }
      LogService.log(LogService.INFO, "GroupsInitializer loaded " + count + " entities");
  	} catch (Exception e) {
  		LogService.log(LogService.ERROR, e);
  	}
  }
	
  public static void main(String[] args) {
	java.util.Date t1 = new java.util.Date();
	GroupsInitializer.initialize();
	java.util.Date t2 = new java.util.Date();
	System.out.println("GroupsInitializer took " + 
	  (t2.getTime() - t1.getTime()) + " ms");
	System.exit(0);
  }
}
