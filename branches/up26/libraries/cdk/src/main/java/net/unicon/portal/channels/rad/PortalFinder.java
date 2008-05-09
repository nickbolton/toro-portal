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

package net.unicon.portal.channels.rad;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.sql.*;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.utils.SmartCache;
import net.unicon.sdk.properties.PropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.UniconProperties;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.portal.common.properties.PortalPropertiesType;

/**
 * The finder for the uPortal person source. It also includes the uPortal group.
 */
public class PortalFinder extends Finder {
  private static final int MAX_WHERE_IN_DEPTH = 999;
  private static final String PDA_MAIL = "mail";
  private static final String PDA_NAME = "displayName";
  private static final String PDA_PHONE = "telephoneNumber";
  private static final int CACHE_TIMEOUT = UniconPropertiesFactory.getManager(
                      PortalPropertiesType.RAD).getPropertyAsInt(
                      "PFinder.SmartCache.cache_timeout");

  private PersonDirectory m_personDir = null;
  private IEntityNameFinder m_groupFinder = null;

  private Map m_caches   = new SmartCache(CACHE_TIMEOUT); // map from userID to username
  private Map m_cacheIDs = new SmartCache(CACHE_TIMEOUT); // map from username to userID

  public PortalFinder() {
    m_personDir = PersonDirectory.instance();
    try {
      m_groupFinder = EntityNameFinderService.instance().getNameFinder( GroupData.C_GROUP);
    } catch( Exception e) {
      Channel.log(e);
    }
  }

  public boolean canFind( IdentityData id, String[] attrs) {
    String etype = id.getEntityType();
    return ( (etype.equals(GroupData.S_CHANNEL)||etype.equals(GroupData.S_GROUP)||
              etype.equals(GroupData.S_USER)||etype.equals(GroupData.S_OBJECT)));
  }

  public void fillDetails( IdentityData[] ids, String[] attrs) {

    // Select users
    Vector nonAlias = new Vector();
    Vector nonID = new Vector();
    Vector allUsers = new Vector();
    for( int i = 0; i < ids.length; i++) {
      String key = ids[i].getID();
      // Portal group
      if (GroupData.isPortalGroup(ids[i])) {
        try {
          String name = m_groupFinder.getName(key);
          ids[i].putName(name);
          ids[i].putAlias(name);
        } catch ( Exception  e) {
          Channel.log("Cannot get the name of group "+key+",excep="+e.getMessage());
          e.printStackTrace();
        }
      }

      // Portal user
      else if (GroupData.isPortalUser(ids[i])) {
        if( key != null) {
          // Get Alias ( username) from key (id)
          String alias = (String)m_caches.get(key);
          if( alias == null) // new for cache
            nonAlias.addElement(ids[i]); //Vector of IdentityData
          else {
            ids[i].putAlias(alias);
            allUsers.addElement(ids[i]);
          }
        }

        // Fine userID from alias
        else { // key == null
          String alias = ids[i].getAlias();
          if( alias != null) {
            key = (String)m_cacheIDs.get(alias);
            if( key == null)
              nonID.addElement(ids[i]); // Vector of IdentityData
            else {
              ids[i].putOID(key);
              allUsers.addElement(ids[i]);
            }
          }
        }
      }
    }

    // Query from database for not-cached user elements to get username (alias)
    if( nonAlias.size() > 0) {
      getUserNames( nonAlias);
      for( int i = 0; i < nonAlias.size(); i++) {
        IdentityData data = (IdentityData)nonAlias.elementAt(i);
        String alias = (String)m_caches.get(data.getID());
        if( alias != null) {
          data.putAlias(alias);
          allUsers.addElement(data);
        }
      }
    }

    // Query from database for not-cached user elements to get userID from username
    if( nonID.size() > 0) {
      getUserIDs( nonID);
      for( int i = 0; i < nonID.size(); i++) {
        IdentityData data = (IdentityData)nonID.elementAt(i);
        String ID = (String)m_cacheIDs.get(data.getAlias());
        if( ID != null) {
          data.putOID(ID);
          allUsers.addElement(data);
        }
      }
    }

    // Now allUsers contains all users which has valid username.
    // The next step is to find other information from person directory
    for( int i = 0; i < allUsers.size(); i++)
      fillUserDetail( (IdentityData)allUsers.elementAt(i));
  }

  public void fillDetail( IdentityData id, String[] attrs) // Thach-May15
  {
    String key = id.getID();
    String etype = id.getEntityType();

    //--- Portal Group
    if (GroupData.isPortalGroup(id))
      try {
        String name = m_groupFinder.getName(key);
        id.putName(name);
        id.putAlias(name);
      } catch ( Exception  e) {
        Channel.log("Cannot get the name of group "+id.getID());
      }

    //--- Portal User
    else if (GroupData.isPortalUser(id)) {
      // Key (id) valid. we find the alias
      if( key != null) {
        String alias = (String)m_caches.get(key);
        if( alias == null) {
          alias = getUserName(key);
          if( alias != null)
            m_caches.put(key,alias);
        }

        // Detail of user
        if( alias != null) {
          id.putAlias(alias);
          fillUserDetail(id);
        }
      }

      // Key is null, find key if alias not null
      else { // key == null
        String alias = id.getAlias();
        if( alias != null) {
          key = (String)m_cacheIDs.get(alias);
          if( key == null) {
            int iid = getUserID(alias);
            key = (iid != -1?""+iid:null);
            if( key != null)
              m_cacheIDs.put(alias, key);
          }

          // put the user id and find others...
          if( key != null) {
            id.putOID(key);
            fillUserDetail(id);
          }
        }
      }
    }
  }

  /**
   * Get user information from Person directory.
   * @param id The uPortal's user to get
   */
  void fillUserDetail( IdentityData id) {
    // Full name default is alias
    String alias = id.getAlias();
    id.putName(alias);

    //------------ Get other information from User directory -----------
    Hashtable hs = m_personDir.getUserDirectoryInformation(alias);
    Enumeration en = hs.keys();
    while (en.hasMoreElements()) {
      String key = (String)en.nextElement();
      Object value = hs.get(key);

      // Interested attributes
      if( value instanceof String) {
        if( key.equals(PDA_MAIL))
          id.putEmail((String)value);
        else if( key.equals(PDA_NAME))
          id.putName((String)value);
        else if( key.equals(PDA_PHONE))
          id.putA("telephonenumber",value);
        else
          id.putA(key, value);
      }
    }
  }

  //-----------------------------------------------------------------//

  /**
   * Get user name and stores in m_caches
  * @param ids contains all user identifiers to get
   */
  private void getUserNames( Vector ids) {
    
    // chunking up the user name retrievals
    int start=0;
    int end=Math.min(MAX_WHERE_IN_DEPTH, ids.size());
    while (start<ids.size()) {
      __getUserNames(ids.subList(start, end));
      start=end;
      end=Math.min(start+MAX_WHERE_IN_DEPTH, ids.size());
    }
  }

  private void __getUserNames( List ids) {
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    try {
      conn = RDBMServices.getConnection();
      try {
        stmnt = conn.createStatement();

        String sql = "SELECT DISTINCT USER_ID,USER_NAME FROM UP_USER WHERE USER_ID IN (";
        for( int i = 0; i < ids.size(); i++)
          sql += (i==0?"":",") + ((IdentityData)ids.get(i)).getID();
        sql += ")";
        Channel.log("[PortalFinder]sql:" + sql);

        rs = stmnt.executeQuery(sql);
        while (rs.next()) {
          String key = rs.getString(1);
          String name = rs.getString(2);

          // Store info.
          m_caches.put(key,name);
        }

      } finally {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          if (stmnt != null) stmnt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (SQLException sqle) {
      Channel.log(sqle);
    } finally {
      RDBMServices.releaseConnection(conn);
    }
  }

  private String getUserName(String idString) {
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    String name = null;
    try {
      conn = RDBMServices.getConnection();
      try {
        stmnt = conn.createStatement();

        String sql = "SELECT DISTINCT USER_NAME FROM UP_USER WHERE USER_ID="+idString;

        rs = stmnt.executeQuery(sql);
        if (rs.next())
          name = rs.getString(1);

      } finally {
        try {
          if (rs != null ) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          if (stmnt != null ) stmnt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (SQLException sqle) {}
    finally {
      RDBMServices.releaseConnection(conn);
    }

    return name;
  }

  private void getUserIDs( Vector ids) {
    
    // chunking up the user id retrievals
    int start=0;
    int end=Math.min(MAX_WHERE_IN_DEPTH, ids.size());
    while (start<ids.size()) {
      __getUserIDs(ids.subList(start, end));
      start=end;
      end=Math.min(start+MAX_WHERE_IN_DEPTH, ids.size());
    }
  }


  /**
   * Get user ID from username and stores in m_cacheIDs
  * @param ids contains all user names to get
   */
  private void __getUserIDs( List ids) {
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    try {
      conn = RDBMServices.getConnection();
      try {
        stmnt = conn.createStatement();

        String sql = "SELECT DISTINCT USER_ID,USER_NAME FROM UP_USER WHERE USER_NAME IN (";
        for( int i = 0; i < ids.size(); i++)
          sql += (i==0?"":",") + SQL.esc(((IdentityData)ids.get(i)).getAlias());
        sql += ")";
        Channel.log("[PortalFinder]sql:" + sql);

        rs = stmnt.executeQuery(sql);
        while (rs.next()) {
          String ID = rs.getString(1);
          String alias = rs.getString(2);

          // Store info.
          m_cacheIDs.put(alias, ID);
        }

      } finally {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (stmnt != null) stmnt.close();
      }
    } catch (SQLException sqle) {
      Channel.log(sqle);
    } finally {
      RDBMServices.releaseConnection(conn);
    }
  }

  private int getUserID(String userName) {
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    int id = -1;
    try {
      conn = RDBMServices.getConnection();
      try {
        stmnt = conn.createStatement();

        String sql = "SELECT DISTINCT USER_ID FROM UP_USER WHERE USER_NAME="+SQL.esc(userName);

        rs = stmnt.executeQuery(sql);
        if (rs.next())
          id = rs.getInt(1);

      } finally {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        stmnt.close();
      }
    } catch (SQLException sqle) {}
    finally {
      RDBMServices.releaseConnection(conn);
    }
    return id;
  }

  //--------------------------------------------------------------------//

  // Find refferences
  public boolean canRef( IdentityData org, IdentityData id) {
    return (id.getRefID("portal") != null && id.getRef("portal") == null);
  }

  public void putRef( IdentityData org, IdentityData[] ids) {
    for( int i = 0; i < ids.length; i++)
      putRef(org,ids[i]);
  }

  public void putRef( IdentityData org, IdentityData id) {
    String alias = id.getRefID("portal");
    int id1 = getUserID(alias);
    if( id1 >= 0)
      id.putRef("portal",new IdentityData(IdentityData.ENTITY,GroupData.S_USER,""+id1,alias,null));
  }
}
