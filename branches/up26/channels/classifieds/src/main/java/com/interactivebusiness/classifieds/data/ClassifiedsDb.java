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

package com.interactivebusiness.classifieds.data;

import org.jasig.portal.services.LogService;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

//Jing Chen added
import com.interactivebusiness.portal.VersionResolver;
/** <p>The class that handles the database interactions for the classifiedDb channel.
 *     This is a singleton object since one instance is enough to serve
 *     multiple clients.  The class does not have any member variables that
 *     maintain state or session.
 *
 *     Ideally, this class must be deployed as a stateless session bean in a
 *     EJB container.
 *
 *     Currently, the database properties are set in the notepad.properties
 *     file in the properties directory under the portal base directory.
 *
 *     This class uses the connection pool that is supplied with the briefcase.
 *     It will better scale for higher user loads and with transactions if
 *     it were converted to an EJB.</p>
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class ClassifiedsDb
{
  private static int expireDays = 30;
  private static ClassifiedsDb         classifiedsDB;         // the singleton object
  private static Object               synchObject = new Object();
  private static java.util.ArrayList  apvdTopicList;        // the singleton object
  private static java.util.ArrayList  needApvdTopicList;    // the singleton object for topic list which need to approved
  private static java.util.ArrayList  publishedItemList;
  private static java.util.ArrayList  needApvdItemList;
  private static java.util.ArrayList  currentTopicList;

  private static final String PENDING  ="P";   // for table CLASSIFIED_ITEM column APPROVED
  private static final String DENIED   ="D";
  private static final String APPROVED ="A";

  private static DataSource classifiedsDS;
  private static String dataSource;
  private Timer m_timer;

  /**
   * Constructs a ClassifiedsDb object.
   * Note that this constructor is private in order to have a singleton object.
   */
  private ClassifiedsDb(String datasourceName)
  {
    // Initialize the connection pool.
    try
    {
      //Context ctx = new InitialContext();
      //classifiedsDS = (DataSource)ctx.lookup (datasourceName);
        dataSource = datasourceName;

      String expirePeriod = "30";
      expireDays = Integer.parseInt(expirePeriod);

      String refreshRate = "60";
      int refreshPeriod = Integer.parseInt(refreshRate) * 1000;

      m_timer = new Timer(true);
      m_timer.schedule(new TopicRefresher(), 2000, refreshPeriod);  // referesh every 5 minutes.
    }
    catch (Exception e)
    {
      LogService.instance().log (LogService.ERROR, "Unable to create Datasource for Classifieds.");
      LogService.instance().log (LogService.ERROR, e);
    }
  }

  /**
   * The access method to ClassifiedDb.  This is a static method that creates the
   * singleton object if it is not already created.
   * @param channelProps - The property values read from briefcase.properties file.
   * @return ClassifiedDb object.
   */
  public static ClassifiedsDb getInstance(String datasourceName)
  {
    if (classifiedsDB == null)
    {
      synchronized(synchObject)
      {
        if (classifiedsDB == null)  // check again.
          classifiedsDB = new ClassifiedsDb(datasourceName);
      }
    }
    return classifiedsDB;
  }

  public  java.util.ArrayList getCurrentTopicList()
  {
    ArrayList topicList = null;

    synchronized (synchObject)
    {
      if (currentTopicList == null)
      {
        currentTopicList = getAllTopic();
      }
      topicList = currentTopicList;
    }
    return topicList;
  }

  public  java.util.ArrayList getApprovedTopicList()
  {
    ArrayList topicList = null;

    synchronized (synchObject)
    {
      if (apvdTopicList == null)
      {
        apvdTopicList = getAllTopicList(APPROVED);
      }
      topicList = apvdTopicList;
    }
    return topicList;
  }

  public java.util.ArrayList getNeedApvdTopicList()
  {
    needApvdTopicList = getAllTopicList(PENDING);
    return needApvdTopicList;
  }

  /**
   * return arraylist of topic that assigned to the approver
   * @author Jing Chen
   */
  public java.util.ArrayList getNeedApvdTopicList (String approver_key)
  {
    needApvdTopicList = getNeedApvdTopicListByUser(approver_key);
    return needApvdTopicList;
  }
  private java.util.ArrayList getNeedApvdTopicListByUser (String approver_key)
  {
      ArrayList allPendingTopicList = getAllTopicList (PENDING);
      Iterator topic_it = allPendingTopicList.iterator();
      ArrayList approverList = new ArrayList();
      ArrayList groupList = new ArrayList ();
      net.unicon.portal.util.GroupsSearch gs = new net.unicon.portal.util.GroupsSearch ();
      TopicInfo topic_info;
      String approver_group_id, approver_entity_type;
      ArrayList pendingTopicList = new ArrayList();

      while (topic_it.hasNext())
      {
          topic_info = (TopicInfo) topic_it.next();
          approverList = getTopicApprover (topic_info.getTopicID());
          for (int i = 0; i < approverList.size(); i++)
          {
              approver_group_id = ((String)approverList.get(i)).substring(0, ((String)approverList.get(i)).indexOf("-"));
              approver_entity_type = ((String)approverList.get(i)).substring(((String)approverList.get(i)).indexOf("-")+1);

              if (approver_entity_type.equals("2"))
              {
                  if (approver_group_id.equals(approver_key))
// *** JK - fix for TT03476 - don't add duplicates in case of overlapping approver groups
//                      pendingTopicList.add(topic_info);
                    if (!pendingTopicList.contains(topic_info))
                      pendingTopicList.add(topic_info);
              }
              else if (approver_entity_type.equals("3"))
              {
                  groupList = gs.getMyGroups(approver_key);
                  for (int j=0; j < groupList.size(); j++)
                  {
                      //System.out.println ((String)groupList.get(j));
                      if (approver_group_id.equals ((String)groupList.get(j)))
// *** JK - fix for TT03476 - don't add duplicates in case of overlapping approver groups
//                          pendingTopicList.add(topic_info);
                        if (!pendingTopicList.contains(topic_info))
                          pendingTopicList.add(topic_info);
                  }
              }
          }
      }
      return pendingTopicList;
  }

  public  java.util.ArrayList getPublishedItemList()
  {
    java.util.ArrayList itemList = null;

    synchronized(synchObject)  // Need to synch due to periodic updates by the TimerTask
    {
      if (publishedItemList==null)
      {
        publishedItemList = getAllItemList();
      }
      itemList = publishedItemList;
    }

    return itemList;
  }

  public  java.util.ArrayList getAllTopicList(String approved)
  {
    currentTopicList=getCurrentTopicList();
    java.util.ArrayList topicEntryList = new java.util.ArrayList();
    Iterator it = currentTopicList.iterator();
    while(it.hasNext())
    {
      TopicInfo eachInfo = (TopicInfo)it.next();
      String count=null;
      count = getCountForTopic(eachInfo.getTopicID(), approved);
      if (count !=null)
      {
        // get all the topics when the approved flag is "A" - we need all the topics
        // Get only topics with items pending when approved flag is "P", - we don't need empty topics in this one.
        if (Integer.parseInt(count)>0 || !approved.equals("P") )
        {
          eachInfo.setTotalEntry(count);
          topicEntryList.add(eachInfo);
        }
      }
    }
    return topicEntryList;
  }

  public int removeTopicGroupIDs (String topicID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {

      conn = getConnection();
      String REMOVE_GROUPS = "DELETE FROM CLASSIFIED_TOPIC_APPROVER WHERE TOPIC_ID = ?";
      ps = conn.prepareStatement(REMOVE_GROUPS);
      ps.setString(1, topicID);
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while deleting topic " + topicID);
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }

  }


  public void setTopicApprover (String topicID, String userID, ArrayList groupsList) throws SQLException
  {
    Connection conn = null;

    try
    {
      PreparedStatement ps = null;
      conn = getConnection();
      String SETGROUP_QUERY = "INSERT INTO CLASSIFIED_TOPIC_APPROVER VALUES (?,?,?,?)";

      for (int i=0; i < groupsList.size(); i++)
      {
        try {
        ps = conn.prepareStatement(SETGROUP_QUERY);

        ps.setString(1, topicID);
        int found = ((String)groupsList.get(i)).indexOf("-");
        ps.setString(2, ((String)groupsList.get(i)).substring(0, found));
        ps.setString(3, userID);
        ps.setString(4, ((String)groupsList.get(i)).substring(found+1));

        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
          } catch (Exception e){
            LogService.log(LogService.ERROR, e);
          }
        }
      }

    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to setTopicGroups for topic ID " + topicID);
      throw sqe;
    }
    finally
    {
      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }

  }

  public ArrayList getTopicApprover (String topicID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
      conn = getConnection();
      String GETGROUP_QUERY = "SELECT GROUP_ID, GROUP_ENTITY_TYPE FROM CLASSIFIED_TOPIC_APPROVER WHERE TOPIC_ID = ?";
      ps = conn.prepareStatement(GETGROUP_QUERY);
      ps.setString(1, topicID);
      rs = ps.executeQuery();
      ArrayList groupsFound = new ArrayList ();
      while (rs.next())
      {
        String groupID = rs.getString(1);
        String EntityType = rs.getString(2);
        groupsFound.add(groupID+"-"+EntityType);
      }
        return groupsFound;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get news items count for topic ID " + topicID);
      return null;
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

  }

  public ArrayList getAllApproverIDs ()
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
      conn = getConnection();
      String GETGROUP_QUERY = "SELECT DISTINCT GROUP_ID, GROUP_ENTITY_TYPE FROM CLASSIFIED_TOPIC_APPROVER";
      ps = conn.prepareStatement(GETGROUP_QUERY);
      rs = ps.executeQuery();
      ArrayList groupsFound = new ArrayList ();
      while (rs.next())
      {
        String groupID = rs.getString(1);
        String EntityType = rs.getString(2);
        groupsFound.add(groupID+"-"+EntityType);
      }
      return groupsFound;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all group IDs");
      return null;
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

  }


  public TopicList getTopicList (String searchString)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    TopicList topicList = new TopicList();

    try
    {

      conn = getConnection();
      String TOPIC_SEARCH = "SELECT TOPIC_ID, NAME, DESCRIPTION FROM CLASSIFIED_TOPIC WHERE UPPER(NAME) LIKE ? order by NAME";
      String filterString = searchString.replace('*', '%');
      filterString = filterString.replace('?', '_');
      ps = conn.prepareStatement(TOPIC_SEARCH);
      ps.setString(1, filterString.toUpperCase());
      rs = ps.executeQuery();
      while (rs.next())
      {
        topicList.addTopic(rs.getString(1), rs.getString(2), rs.getString(3), true);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while retrieving topic List with filter string " + searchString);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return topicList;
  }

  public TopicInfo getTopicInfo (String topicID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    TopicInfo ti = new TopicInfo();

    try
    {
      conn = getConnection();
      String TOPIC = "SELECT NAME, DESCRIPTION, IMAGE, IMAGE_MIME_TYPE FROM CLASSIFIED_TOPIC WHERE TOPIC_ID = ?";
      ps = conn.prepareStatement(TOPIC);
      ps.setString(1, topicID);
      rs = ps.executeQuery();
      while (rs.next())
      {
        ti.setName(rs.getString(1));
        ti.setDescription(rs.getString(2));
        ti.setImageName(rs.getString(3));
        ti.setImageType(rs.getString(4));
        ti.setTopicID(topicID);
      }

    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while retrieving topicInfo for TopicID = " + topicID);
      throw sqe;
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    ti.setSelectedGroups(getTopicApprover(topicID));
    return ti;
  }



  private java.util.ArrayList getAllItemList()
  {
    if (apvdTopicList==null)
      apvdTopicList = getApprovedTopicList();
    java.util.ArrayList allTopic = new java.util.ArrayList();
    Iterator it = apvdTopicList.iterator();
    while (it.hasNext())
    {
      TopicInfo eachInfo=(TopicInfo)it.next();
      ItemList itemlist=getItems(eachInfo.getTopicID(),APPROVED);
      allTopic.add(itemlist);
    }
    return allTopic;
  }
/**
 * add parameter userKey by Jing 2/27/03
 * to get item for topic assigned only
 */
  public  java.util.ArrayList getNeedApprovedItemList(String userKey)
  {
    needApvdItemList = getAllNeedApvdItemList(userKey);
    return needApvdItemList;
  }
/**
 * add parameter userKey by Jing 2/27/03
 * to get item for topic assigned only
 */
  private java.util.ArrayList getAllNeedApvdItemList(String userKey)
  {

      needApvdTopicList = getNeedApvdTopicList(userKey);
    java.util.ArrayList allTopic = new java.util.ArrayList();

    Iterator it = needApvdTopicList.iterator();
    while (it.hasNext())
    {
      TopicInfo eachInfo=(TopicInfo)it.next();
      ItemList itemlist=getItems(eachInfo.getTopicID(), PENDING);
      allTopic.add(itemlist);
    }
    return allTopic;
  }

  /**
   * Creates a new topic entry in the database.  This method can be executed only
   * by users have the "Administrator" role.
   * @param topicName - Name of the new topic.
   * @param description - Description about the topic.
   *                      Set it to null if there is no entry available.
   * @param smallIcon - File name of the small icon for the topic.
   * @param bigIcon - File name of the big icon for the topic.
   * @return Generated unique ID for the topic.
   */
  public String createTopic (TopicInfo myTopic) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      final String INSERT_SQL =
        "INSERT INTO CLASSIFIED_TOPIC (TOPIC_ID, NAME, DESCRIPTION, CREATE_DATE, IMAGE, IMAGE_MIME_TYPE) values (?, ?, ?, ?, ?, ?)";
      ps = conn.prepareStatement(INSERT_SQL);
      String topicID = getUniqueTopicID();
      ps.setString(1, topicID);
      ps.setString(2, myTopic.getName());
      if (myTopic.getDescription() == null)
        ps.setNull(3, java.sql.Types.VARCHAR);
      else
        ps.setString(3, myTopic.getDescription());
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(4, currentTime);
      // save Icon image
      ps.setString(5, myTopic.getImageName());
      ps.setString(6, myTopic.getImageType());
      ps.executeUpdate();
      currentTopicList=null;
      return topicID;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to create Classified topic called " + myTopic.getName());
      currentTopicList = null;   // retrieve the topiclist from database
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }

  }

  public String createIcon (String imageName, String imageType)
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      final String INSERT_SQL =
        "insert into CLASSIFIED_ICONS values (?, ?, ?)";
      ps = conn.prepareStatement(INSERT_SQL);
      String iconID = getUniqueIconID();
      ps.setString(1, iconID);
      ps.setString(2, imageName);
      ps.setString(3, imageType);
      ps.executeUpdate();
      return iconID;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to create Classified icon called " + imageName + " sqe:: "+sqe);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
    return null;
  }

  /**
   * Creates a new entry in the database for a classified item that belongs
   * to a particular topic.  It is assumed that the topic ID to which the
   * item belongs to will be supplied and will not be empty.
   * @param ItemInfo - Data object containing information about the item.
   */
  public int createItem (ItemInfo itemInfo) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
     //String itemID = getUniqueItemID();
      conn = getConnection();
      String INSERT_SQL = "INSERT INTO CLASSIFIED_ITEM values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      ps = conn.prepareStatement(INSERT_SQL);

      ps.setString(1, itemInfo.getItemID());
      ps.setString(2, itemInfo.getTopicID());
      ps.setString(3, itemInfo.getAuthorID());
      ps.setString(4, itemInfo.getItemContent());

      String email = itemInfo.getEmail().trim();
      if (email == null || email.length() == 0) {
        ps.setNull(5, java.sql.Types.VARCHAR);
      } else {
        ps.setString(5, email);
      }

      String phone = itemInfo.getPhone().trim();
      if (phone == null || phone.length() == 0) {
        ps.setNull(6, java.sql.Types.VARCHAR);
      } else {
        ps.setString(6, phone);
      }

      String cost = itemInfo.getCost();
      if (cost == null)
        ps.setNull(7, java.sql.Types.VARCHAR);
      else
        ps.setString(7, cost);

      String message = itemInfo.getMessageToAuth();
      if (message == null)
        ps.setNull(8, java.sql.Types.VARCHAR);
      else
        ps.setString(8, message);

      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(9, currentTime); //create_date

      Integer days=new Integer(expireDays);
      long cTS = currentTime.getTime();
      long extendTo=cTS+days.longValue()*24L*3600L*1000L;
      Timestamp ts=new Timestamp(extendTo);
      ps.setTimestamp(10, ts);

      itemInfo.setApproved(PENDING);  // a new item has to be apporved before being published.
      String approved = itemInfo.getApproved();
      ps.setString(11, approved);

      String approvedBy = itemInfo.getApprovedBy();
       if (approvedBy == null)
        ps.setNull(12, java.sql.Types.VARCHAR);
      else
        ps.setString(12, approvedBy);

      Timestamp approvedDate = null;    // approvedDate will always be null at create stage
      ps.setNull(13, java.sql.Types.TIMESTAMP);

      String contactName = itemInfo.getContactName();
      if (contactName == null)
        ps.setNull(14, java.sql.Types.VARCHAR);
      else
        ps.setString(14, contactName);

/*      if (itemInfo.getImageInputStream() != null)
      {
        try {
          InputStream ios = itemInfo.getImageInputStream();
          BufferedInputStream bis = new BufferedInputStream(ios);
          System.out.println ("bis " + bis);
          System.out.println ("ios " + ios);
          byte[] byteArray = new byte[500];
          int size = 0;
          long tsize = 0;
          while ((size = bis.read(byteArray)) != -1)
          {
            tsize += size;
          }
          System.out.println ("long tsize. " + tsize);
      //    Long longsize = new Long(tsize);
          ps.setBinaryStream (15, ios, 2000);
        }
        catch (IOException e){
          System.out.println ("Error saving image to database. " + e);
        }
      }
      else
  **/

      String image = itemInfo.getItemImage();
      if (image == null)
        ps.setNull(15, java.sql.Types.VARCHAR);
      else
        ps.setString(15, image);

      String mimeType = itemInfo.getImageMimeType();
      if (mimeType == null)
        ps.setNull(16, java.sql.Types.VARCHAR);
      else
        ps.setString(16, mimeType);

      //needApvdTopicList=null;
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to create item for  user "+ itemInfo.getAuthorID()+" SQL == "+sqe);
      //needApvdTopicList=null;
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
  }

  private String getUniqueItemID ()
  {
    return getUniqueID ("item");
  }

  private String getUniqueIconID ()
  {
    return getUniqueID ("icon");
  }

  private String getUniqueTopicID ()
  {
    return getUniqueID ("topic");
  }

  /**
   * Gets an unique ID for the given identifier.
   * This method is static and synchronized so that only one thread can execute
   * it at one time.
   * @param idName - Name of the identifier for which an unique id is required.
   * @return An unique ID value.
   */
  private static synchronized String getUniqueID (String idName)
  {
    Connection conn = null;
    try {
    conn = getConnection();

    PreparedStatement ps = null;
    ResultSet rs = null;
    String idValue = null;

    try
    {
      final String uniqueIDSelect = "SELECT ID_VALUE from CLASSIFIED_ID where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDSelect);
      ps.setString(1, idName);
      rs = ps.executeQuery();
      if (rs.next())
      {
//        idValue = getNextID(rs.getString(1));
        idValue = rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to update unique ID for " + idName);
    }
    finally
    {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

    // increment the idValue by one
    int newValue = Integer.parseInt(idValue);

    // Store the identifier back in the table.
    try
    {
      final String uniqueIDUpdate = "UPDATE CLASSIFIED_ID set ID_VALUE = ? where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDUpdate);
      ps.setString(1, Integer.toString(++newValue));
      ps.setString(2, idName);
      ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to update unique ID for " + idName);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}
    }

    return Integer.toString(newValue);

    } finally {
      try
      {
        if (conn != null) conn.close();
      }
    catch (SQLException sqe){}
    }

  }

  // get next item ID. used to store item images to disk
  public String getNextID()
  {
    return getUniqueID ("item");
  }

  /**
   * Returns all the notes that belong to a user from all the folders.
   * @param personID - ID of the person whose notes are desired.
   * @return List of itemList object containing all the selected news items.
   */

  public ItemList getUserItem (String personID,String topicid,String approved)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ItemList itemList = new ItemList();
    try
    {
      Timestamp currentTime=new Timestamp(System.currentTimeMillis());
      conn = getConnection();

      final String ITEM_QUERY =
        "select A.ITEM_ID, A.CONTENT,A.EXPIRE_DATE, B.NAME,A.TOPIC_ID, A.EMAIL,A.PHONE,A.COST, A.MESSAGE " +
        "from CLASSIFIED_ITEM A, CLASSIFIED_TOPIC B where A.AUTHOR_ID = ? and A.APPROVED=? and A.EXPIRE_DATE > ?  "+
        "and A.TOPIC_ID = B.TOPIC_ID  order by A.CREATE_DATE";
      final String ITEM_QUERY_WITH_TOPIC =
        "select A.ITEM_ID, A.CONTENT,A.EXPIRE_DATE, B.NAME,A.TOPIC_ID, A.EMAIL,A.PHONE,A.COST, A.MESSAGE, A.CONTACT_NAME, A.IMAGE, A.IMAGE_MIME_TYPE, A.AUTHOR_ID " +
        "from CLASSIFIED_ITEM A, CLASSIFIED_TOPIC B where A.AUTHOR_ID = ? and A.APPROVED=? and A.EXPIRE_DATE > ?  "+
        "and A.TOPIC_ID = B.TOPIC_ID and A.TOPIC_ID=? order by A.CREATE_DATE";
      ps = conn.prepareStatement (topicid.equals("-1") ? ITEM_QUERY : ITEM_QUERY_WITH_TOPIC);
      ps.setString(1, personID);
      ps.setString(2,approved);
      ps.setTimestamp(3, currentTime);
      if (!topicid.equals("-1"))
        ps.setString(4,topicid);
      rs = ps.executeQuery();
      while (rs.next())
      {
        itemList.addItem(rs.getString(1),rs.getString(2),rs.getTimestamp(3),rs.getString(4),rs.getString(5),
                         rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),APPROVED,
                          rs.getString(10),    // Contact
                          rs.getString(11),    // Image
                          rs.getString(12), // Image Mime type
                          rs.getString(13));
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all published item for user " + personID);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return itemList;
  }

  public ItemList getItem (String itemID, String topicName, String approved)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ItemList itemList = new ItemList();
    try
    {
      conn = getConnection();
      final String ITEM_QUERY = "SELECT item_id, content, expire_date, topic_id, email, phone, cost, message, contact_name, image, image_mime_type, author_id FROM classified_item WHERE item_id = ?";
      ps = conn.prepareStatement (ITEM_QUERY);
      ps.setString(1, itemID);
      rs = ps.executeQuery();
      while (rs.next()) {
    	  itemList.addItem(rs.getString("item_id"),
    			  rs.getString("content"),
    			  rs.getTimestamp("expire_date"),
    			  topicName,
    			  rs.getString("topic_id"),
    			  rs.getString("email"),
    			  rs.getString("phone"),
    			  rs.getString("cost"),
    			  rs.getString("message"),
    			  approved,
    			  rs.getString("contact_name"),    // Contact
    			  rs.getString("image"),    // Image
    			  rs.getString("image_mime_type"), // Image Mime type
    			  rs.getString("author_id"));
    	  return itemList;
      }

    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to classified item with itemID::" + itemID);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return null;
  }

  public ArrayList getIconList ()
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      conn = getConnection();
      final String ICON_QUERY = "SELECT icon_id, icon_name, icon_mime_type FROM classified_icons";
      ps = conn.prepareStatement (ICON_QUERY);
      rs = ps.executeQuery();
      ArrayList iconList = new ArrayList ();
      while (rs.next())
      {
        IconInfo icon = new IconInfo();
        icon.setIconID(rs.getString("icon_id"));
        icon.setImageName(rs.getString("icon_name"));
        icon.setImageType(rs.getString("icon_mime_type"));
        iconList.add(icon);
      }
      return iconList;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get icon list from classified icons table");
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return null;
  }

  public String getItemContent (String itemID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      conn = getConnection();
      String ITEM = "select CONTENT from CLASSIFIED_ITEM where ITEM_ID = ?";
      ps = conn.prepareStatement (ITEM);
      ps.setString(1, itemID);
      rs = ps.executeQuery();
      while (rs.next())
      {
        return rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get classified item's content");
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return null;
  }

  /**
   * Returns a list of item in a topicID
   * @param topicID - ID of the topic
   * @param isApproved - "A" for all the items has been already published in a topic
   *                     "P" for all the items which is waiting for approval in a topic
   *                     "D" for all the items which has been denied for publish.
   * @return A NewsInfo data object containing the news item information.
   */
  public ItemList getItems (String topicID,String isApproved)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ItemList itemList = new ItemList();

    try
    {
      conn = getConnection();
      final String ITEM_QUERY =
        "select A.ITEM_ID, A.CONTENT,A.EXPIRE_DATE, B.NAME, A.TOPIC_ID, A.EMAIL,A.PHONE,A.COST, A.MESSAGE, A.CONTACT_NAME, A.IMAGE, A.IMAGE_MIME_TYPE, A.AUTHOR_ID " +
        "from CLASSIFIED_ITEM A, CLASSIFIED_TOPIC B " +
        "where A.TOPIC_ID=? and A.APPROVED=? and A.EXPIRE_DATE > ? and A.TOPIC_ID = B.TOPIC_ID order by A.CREATE_DATE";

      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1, topicID);
      ps.setString(2,isApproved);
      ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
      rs = ps.executeQuery();
      while (rs.next())
      {
        itemList.addItem(rs.getString(1),    // Item ID
                         rs.getString(2),    // Content
                         rs.getTimestamp(3), // Expiry Date
                         rs.getString(4),    // Name
                         rs.getString(5),    // Topic ID
                         rs.getString(6),    // email Info
                         rs.getString(7),    // Phone number
                         rs.getString(8),    // Cost
                         rs.getString(9),    // Messages
                         isApproved,
                         rs.getString(10),    // Contact
                         rs.getString(11),    // Image
                         rs.getString(12), // Image Mime type
                         rs.getString(13)); // author

      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all classified info for topic " + topicID);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return itemList;
  }
   /**
   * Returns a list of all the topic for the classifieds.
   * The list is represented as a java.util.Map.  The key contains the
   * topicID, topic name and the total published entries for each topic.
   * @return java.util.Map data structure containing the list of folder IDs and their names.
   */
  public java.util.ArrayList getAllTopic()
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.util.ArrayList topicLists = new java.util.ArrayList();
    try
    {
      conn = getConnection();
      final String ITEM_QUERY = "SELECT topic_id, name, description, create_date, image, image_mime_type FROM classified_topic";
      ps = conn.prepareStatement(ITEM_QUERY);
      rs = ps.executeQuery();
      while (rs.next())
      {
        TopicInfo topicinfo=new TopicInfo();
        String topicid=rs.getString("topic_id");
        if (topicid!=null)
        {
          topicinfo.setTopicID(topicid);
          topicinfo.setName(rs.getString("name"));
          topicinfo.setDescription(rs.getString("description"));
          topicinfo.setCreateDate(rs.getTimestamp("create_date"));
          topicinfo.setImageName(rs.getString("image"));
          topicinfo.setImageType(rs.getString("image_mime_type"));
          topicLists.add(topicinfo);
        }
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all classified topics  ");
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return topicLists;
  }
  /**
   * Returns list of topic info with the entry count for the classifieds
   *
   * @return String
   */
  public String getCountForTopic(String topicid,String isApproved)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String count = null;
      try
      {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        conn = getConnection();
        final String ITEM_QUERY =
          "select  count(*) from CLASSIFIED_ITEM where APPROVED=? and " +
          "EXPIRE_DATE > ? and TOPIC_ID=?";
        ps = conn.prepareStatement(ITEM_QUERY);
        ps.setString(1, isApproved);
        ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        ps.setString(3,topicid);
        rs = ps.executeQuery();
        if (rs.next())
        {
          count = rs.getString(1);
        }
      }
      catch (SQLException sqe)
      {
        LogService.instance().log(LogService.ERROR, "Unable to get topic entry number");
      }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

    return count;
  }

  public String getCountForTopic(String topicid)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String count = null;
      try
      {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        conn = getConnection();
        final String ITEM_QUERY =
          "select  count(*) from CLASSIFIED_ITEM where EXPIRE_DATE > ? and TOPIC_ID=?";
        ps = conn.prepareStatement(ITEM_QUERY);
        ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        ps.setString(2,topicid);
        rs = ps.executeQuery();
        if (rs.next())
        {
          count = rs.getString(1);
        }
      }
      catch (SQLException sqe)
      {
        LogService.instance().log(LogService.ERROR, "Unable to get topic entry number");
      }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

    return count;
  }

  /**
   * Deletes the desired topic from the database.This method also
   * deletes all the items contained within this topic.
   * @param topicID - ID of the topic to be deleted.
   */
  public int deleteTopic (String topicID)
  {
    Connection conn = null;
    PreparedStatement ps1 = null, ps2 = null;
    int numTopics = 0;
    try
    {
      conn = getConnection();
      ps1 = conn.prepareStatement("DELETE FROM CLASSIFIED_ITEM WHERE TOPIC_ID = ?");
      ps1.setString(1, topicID);
      int numRows = ps1.executeUpdate();

      ps2 = conn.prepareStatement("DELETE FROM CLASSIFIED_TOPIC WHERE TOPIC_ID = ?");
      ps2.setString(1, topicID);
      numTopics = ps2.executeUpdate();

    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to delete topic. ID =  " + topicID);
    }
    finally
    {
      try
      {
        if (ps1 != null) ps1.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps2 != null) ps2.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    currentTopicList = null; // need to reaccess to database
    needApvdTopicList=null;
    apvdTopicList=null;

    return numTopics;
  }


  public int deleteTopic (ArrayList topicIDList) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null, ps1 = null;

    try
    {
      conn = getConnection();
      String DELETE_ITEMS = "DELETE FROM CLASSIFIED_ITEM WHERE TOPIC_ID IN (";

      for (int i=0; i < topicIDList.size(); i++)
      {
        if (i == 0)
          DELETE_ITEMS += "?";
        else
          DELETE_ITEMS += ", ?";
      }
      DELETE_ITEMS += ")";

      ps1 = conn.prepareStatement(DELETE_ITEMS);

      for (int i=0; i < topicIDList.size(); i++)
      {
        ps1.setString((i+1), (String)topicIDList.get(i));
      }

      int numRows = ps1.executeUpdate();

      String DELETE_TOPIC_LIST = "DELETE FROM CLASSIFIED_TOPIC WHERE TOPIC_ID IN("; //'3','4')"

      for (int i=0; i < topicIDList.size(); i++)
      {
        if (i == 0)
          DELETE_TOPIC_LIST += "?";
        else
          DELETE_TOPIC_LIST += ", ?";
      }
      DELETE_TOPIC_LIST += ")";

      ps = conn.prepareStatement(DELETE_TOPIC_LIST);

      for (int i=0; i < topicIDList.size(); i++)
      {
        ps.setString((i+1), (String)topicIDList.get(i));
      }
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while deleting multiple topics: " + sqe);
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps1 != null) ps1.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
  }

  /**
   * Renames a topic to the provided name.
   * @param topicID - ID of the topic to be renamed
   * @param newName  - The new name of the folder
   */
  public void updateTopic (String topicID, String userID, TopicInfo myTopicInfo) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      ps = conn.prepareStatement("UPDATE CLASSIFIED_TOPIC SET NAME=?, DESCRIPTION=?, IMAGE=?, IMAGE_MIME_TYPE=? WHERE TOPIC_ID = ?");
      ps.setString(1, myTopicInfo.getName());
      ps.setString(2, myTopicInfo.getDescription());
      ps.setString(3, myTopicInfo.getImageName());
      ps.setString(4, myTopicInfo.getImageType());
      ps.setString(5, topicID);
      int numFolders = ps.executeUpdate();
      // now need to remove all topicApprovers and re-add topicApprovers
      removeTopicGroupIDs (topicID);
      setTopicApprover (topicID, userID, myTopicInfo.getSelectedGroups());
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to rename topic. ID =  " + topicID);
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
    currentTopicList=null;
  }

  /**
   * Deletes the supplied item from the database.
   * @param itemID - ID of the classifieds to be deleted.
   */
  public int deleteItem (ArrayList itemIDList) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      String DELETE_ITEMS = "DELETE FROM CLASSIFIED_ITEM WHERE ITEM_ID IN (";

      for (int i=0; i < itemIDList.size(); i++)
      {
        if (i == 0)
          DELETE_ITEMS += "?";
        else
          DELETE_ITEMS += ", ?";
      }
      DELETE_ITEMS += ")";

      ps = conn.prepareStatement(DELETE_ITEMS);

      for (int i=0; i < itemIDList.size(); i++)
      {
        ps.setString((i+1), (String)itemIDList.get(i));
      }

      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to delete item.");
      throw sqe;
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }

  }

  public int deleteIcon (String iconID)
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      String DELETE_ICON = "DELETE FROM CLASSIFIED_ICONS WHERE ICON_ID = ?";
      ps = conn.prepareStatement(DELETE_ICON);
      ps.setString(1, iconID);
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to delete icon. sqe::" + sqe);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
    return 0;
  }

  /**
   * Update a note content, folder info
   * @param noteID - ID of the note to be moved
   * @param folderID - ID of the updated folder.
   * @param content - content of the note
   */
  public void approvedOrDenyItem (String itemID, String isApproved, String msgToAuth,
                                  String approvedBy, String topicid)
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      final String UPDATE_SQL =
        "update CLASSIFIED_ITEM set APPROVED = ?, MESSAGE = ?,APPROVED_BY=?, APPROVED_DATE=?, "+
        "TOPIC_ID=? where ITEM_ID = ?";
      ps = conn.prepareStatement(UPDATE_SQL);
      ps.setString(1, isApproved);
      ps.setString(2,msgToAuth);
      ps.setString(3,approvedBy);
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(4, currentTime);
      ps.setString(5, topicid);
      ps.setString(6,itemID);
      int numNotes = ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to update item. ID =  " + itemID);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
  }




  public boolean hasApprovalMessages(String personid)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int count=0;
    boolean hasApvdMsg = false;
    try
    {
      conn = getConnection();
      ps = conn.prepareStatement("select  COUNT(*) from CLASSIFIED_ITEM where AUTHOR_ID=? "+
                                 " and MESSAGE is not null");
      ps.setString(1, personid);
      rs = ps.executeQuery();
      if (rs.next())
      {
        count = rs.getInt(1);
      }
      if (count>0)
        hasApvdMsg=true;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to check hasApprovedMessage for id=  " + personid);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return hasApvdMsg;
  }



  public boolean getUserHasItems(String personid)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      conn = getConnection();
      ps = conn.prepareStatement("SELECT count(topic_id) AS count_topic_id FROM classified_item WHERE author_id = ?");
      ps.setString(1, personid);
      rs = ps.executeQuery();
      
      if (rs.next()) {
          int count = rs.getInt("count_topic_id");
          if(count > 0) { return true; }
      }
      
      return false;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to check find user roles for id=  " + personid);
      return false;
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

  }

  /**
   * Returns a list of all published items count for each topic for a particular user.
   * The list is represented as a java.util.Map.  The key contains the
   * topicID, topic name and the total published entries for each topic.
   * @return java.util.Map data structure containing the list of folder IDs and their names.
   */
  public  java.util.ArrayList getUserTopicList (String personID,String approved)
  {
    java.util.ArrayList topicEntryList = new java.util.ArrayList();
    Iterator it = currentTopicList.iterator();
    while(it.hasNext())
    {
      TopicInfo eachInfo = (TopicInfo)it.next();
      String count=null;
      count = getUserPubTopicCount(personID,eachInfo.getTopicID(), approved);
      if (count !=null)
      {
        if (Integer.parseInt(count)>0)
        {
          eachInfo.setTotalEntry(count);
          topicEntryList.add(eachInfo);
        }
      }
    }
    return topicEntryList;
  }


  /**
   * Returns a count number for published items by a topic for a particular user
   *
   */
  public String getUserPubTopicCount (String personID,String topicid, String approved)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.util.ArrayList topicList = new java.util.ArrayList();
    String count=null;
    try
    {
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      final String ITEM_QUERY = "select COUNT(*) from CLASSIFIED_ITEM"+
                            " where TOPIC_ID = ? AND APPROVED=?  "+
                            " and AUTHOR_ID=? and EXPIRE_DATE > ? ";

      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1,topicid);
      ps.setString(2, approved);
      ps.setString(3,personID);
      ps.setTimestamp(4, currentTime);
      rs = ps.executeQuery();
      if (rs.next())
      {
        count = rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get count  for personid=  "+personID+"for topicid= "+topicid);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return count;
  }

  /**
   * Returns all the items that belong to a user that has message fm approver from a particular topic.
   * @param personID - ID of the person whose notes are desired.
   * @return List of ItemList object containing all the selected news items.
   */

  public ItemList getMsgToAuth (String personID,String topicID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ItemList itemList = new ItemList();
    String andTopicID="";
    /*
    if (!topicid.equals("-1"))
      andTopicID=" and A.TOPIC_ID="+topicid;
    */
    try
    {
      Timestamp currentTime=new Timestamp(System.currentTimeMillis());
      conn = getConnection();

      final String ITEM_QUERY_WITH_TOPIC =
        "select A.ITEM_ID, A.CONTENT,A.EXPIRE_DATE, B.NAME,A.TOPIC_ID, A.EMAIL,A.PHONE,A.COST,"+
        "A.MESSAGE,A.APPROVED from CLASSIFIED_ITEM A, CLASSIFIED_TOPIC B " +
        "where A.AUTHOR_ID = ? and A.EXPIRE_DATE > ?  and A.APPROVED not in (?) "+
        "and A.MESSAGE is not null and A.TOPIC_ID = B.TOPIC_ID and A.TOPIC_ID = ? order by A.CREATE_DATE";

      final String ITEM_QUERY =
        "select A.ITEM_ID, A.CONTENT,A.EXPIRE_DATE, B.NAME,A.TOPIC_ID, A.EMAIL,A.PHONE,A.COST,"+
        "A.MESSAGE,A.APPROVED from CLASSIFIED_ITEM A, CLASSIFIED_TOPIC B " +
        "where A.AUTHOR_ID = ? and A.EXPIRE_DATE > ?  and A.APPROVED not in (?) "+
        "and A.MESSAGE is not null and A.TOPIC_ID = B.TOPIC_ID order by A.CREATE_DATE";

      ps = conn.prepareStatement (topicID.equals("-1") ? ITEM_QUERY : ITEM_QUERY_WITH_TOPIC);
      ps.setString(1, personID);
      ps.setTimestamp(2, currentTime);
      ps.setString(3,PENDING);
      if (!topicID.equals("-1"))
        ps.setString(4, topicID);
      rs = ps.executeQuery();
      while (rs.next())
      {
    //    itemList.addItem(rs.getString(1),rs.getString(2),rs.getTimestamp(3),rs.getString(4),rs.getString(5),
      //                   rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getString(10));
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all published item for user " + personID);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return itemList;
  }

  /**
   * Returns a list of all the topic for the classifieds which contains msg to user.
   * The list is represented as a java.util.ArrayList. it contains the
   * topicID, topic name and the total published entries for each topic.
   * @return java.util.ArrayList data structure containing the list of topic IDs and their names.
   */
  public java.util.ArrayList getMsgToUserTopicList (String personID)
  {
    currentTopicList=getCurrentTopicList();
    java.util.ArrayList topicEntryList = new java.util.ArrayList();
    Iterator it = currentTopicList.iterator();
    while(it.hasNext())
    {
      TopicInfo eachInfo = (TopicInfo)it.next();
      String count=null;
      count = getMsgToUserTopicCount(personID,eachInfo.getTopicID());
      if (count !=null)
      {
        if (Integer.parseInt(count)>0)
        {
          eachInfo.setTotalEntry(count);
          topicEntryList.add(eachInfo);
        }
      }
    }
    return topicEntryList;
  }

  /**
   * Returns a list of all the topic for the classifieds which contains msg to user.
   * The list is represented as a java.util.ArrayList. it contains the
   * topicID, topic name and the total published entries for each topic.
   * @return java.util.ArrayList data structure containing the list of topic IDs and their names.
   */
  public String getMsgToUserTopicCount (String personID,String topicid)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.util.ArrayList topicList = new java.util.ArrayList();
    String count = null;
    try
    {
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      final String ITEM_QUERY = "select COUNT(*) from CLASSIFIED_ITEM where AUTHOR_ID=?"+
                            " and TOPIC_ID=? and MESSAGE is not null and EXPIRE_DATE > ?";


      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1,personID);
      ps.setString(2,topicid);
      ps.setTimestamp(3, currentTime);
      rs = ps.executeQuery();
      if (rs.next())
      {
        count=rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get user topic list of MSG TO USER for personid=  "+personID+" and topicid="+topicid);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return count;
  }

  /**
   * Returns a list of all the topic for the classifieds.
   * The list is represented as a java.util.Map.  The key contains the
   * topicID, topic name and the total published entries for each topic.
   * @return java.util.Map data structure containing the list of folder IDs and their names.
   */
  public TopicList getExpireItemTopicList ()
  {
    currentTopicList=getCurrentTopicList();
    TopicList topicEntryList = new TopicList();
    Iterator it = currentTopicList.iterator();
    while(it.hasNext())
    {
      TopicInfo eachInfo = (TopicInfo)it.next();
      String count=null;
      count = getExpireItemTopicCount(eachInfo.getTopicID());
      if (count !=null)
      {
        if (Integer.parseInt(count)>0)
        {
          topicEntryList.addTopic(eachInfo.getName(),eachInfo.getTopicID(),count);
        }
      }
    }
    return topicEntryList;
  }
  /**
   * Returns topic count for expired items.
   *
   */
  public String getExpireItemTopicCount (String topicid)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String count=null;
    try
    {
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      final String ITEM_QUERY =
        "select COUNT(*) from CLASSIFIED_ITEM" +
        "where TOPIC_ID = ? AND APPROVED=? and EXPIRE_DATE < ? ";

      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1,topicid);
      ps.setString(2,APPROVED);
      ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
      rs = ps.executeQuery();
      if (rs.next())
      {
        count=rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get all need deleted expire classified topics  ");
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return count;
  }


  /**
   * Deletes the desired expired item from the database.
   * @param topicID - ID of the topic .
   */
  public void deleteExpireItemByTopic (String topicID)
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      Timestamp currentTime=new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      ps = conn.prepareStatement("delete from CLASSIFIED_ITEM where TOPIC_ID = ? and EXPIRE_DATE < ?");
      ps.setString(1, topicID);
      ps.setTimestamp(2, currentTime);
      int numRows = ps.executeUpdate();

    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to delete expire item for topic ID =  " + topicID);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
  }
  /**
   * Deletes the item which has been expired from the database.
   */
  public void deleteExpireItem ()
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      Timestamp currentTime=new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      ps = conn.prepareStatement("delete from CLASSIFIED_ITEM where  EXPIRE_DATE < ?");
      ps.setTimestamp(1,currentTime);
      int numRows = ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to delete expire item for all ");
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
  }
  /**
   * update the CLASSIFIED_ITEM Table
   */
  public void updateItem (String topicid, String cost, String content, String phone, String email,
                          String itemID)
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection();
      final String ITEM_QUERY="update CLASSIFIED_ITEM set TOPIC_ID=?,CONTENT=?,COST=?,PHONE=?,"+
                               "EMAIL=?, CREATE_DATE=?,EXPIRE_DATE=?,APPROVED=? where ITEM_ID=?";
      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1,topicid);
      ps.setString(2,content);
      ps.setString(3,cost);
      ps.setString(4,phone);
      ps.setString(5,email);
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(6, currentTime); //create_date
      Integer days=new Integer(expireDays);
      long cTS = currentTime.getTime();
      long extendTo=cTS+days.longValue()*24L*3600L*1000L;
      Timestamp ts=new Timestamp(extendTo);
      ps.setTimestamp(7, ts);
      ps.setString(8,PENDING);
      ps.setString(9,itemID);
      int numRows = ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to update item id is  "+itemID);
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
    publishedItemList=null;
  }


  /**
   * Returns count for status.
   *
   */
  public String getUserStatusCount(String personID, String statusName)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String count=null;
    try
    {
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      conn = getConnection();
      final String ITEM_QUERY =
        "select COUNT(*) from CLASSIFIED_ITEM " +
        "where AUTHOR_ID = ? AND APPROVED=? and EXPIRE_DATE > ? ";

      ps = conn.prepareStatement(ITEM_QUERY);
      ps.setString(1,personID);
      ps.setString(2,statusName);
      ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
      rs = ps.executeQuery();
      if (rs.next())
      {
        count=rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get status count for the user  "+sqe);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (ps != null) ps.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }

      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    return count;
  }

  public static java.sql.Connection getConnection ()
  {
    try
    {
      //if (classifiedsDS != null)
        //return classifiedsDS.getConnection();
        return VersionResolver.getConnectionByPortalVersions(dataSource);
    }
    catch (Exception sqe)
    {
    }
    return null;
  }

  class TopicRefresher extends TimerTask
  {
    public void run()
    {
      ArrayList currentList = getAllTopic();
      ArrayList topicList = getAllTopicList(APPROVED);
      ArrayList itemList  = getAllItemList();
      synchronized (synchObject)
      {
        currentTopicList  = currentList;
        apvdTopicList     = topicList;
        publishedItemList = itemList;
      }
    }
  }
}
