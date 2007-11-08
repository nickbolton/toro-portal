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
 *   2    Channels  1.1         12/20/2001 3:54:33 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:49 AMFreddy Lopez
 *  $
 *
 */

package com.interactivebusiness.news.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.time.TimeServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;

/** <p>The class that handles the databse interactions for the news channel.
 *     This is a singleton object since one instance is enough to serve
 *     multiple clients.  The class does not have any member variables that
 *     maintain state or session.
 *
 *     Ideally, this class must be deployed as a stateless session bean in a
 *     EJB container.
 *
 *     Currently, the database properties are set in the news.properties
 *     file in the properties directory under the portal base directory.
 *
 *     This class uses the connection pool that is supplied with the briefcase.
 *     It will better scale for higher user loads and with transactions if
 *     it were converted to an EJB.</p>
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class NewsDb
{
  private static String INSERT_TOPIC = "INSERT INTO NEWS_TOPIC VALUES (?, ?, ?, ?)";

  private static String INSERT_NEWS = "INSERT INTO NEWS_CONTENT values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static String DELETE_TOPIC = "delete from NEWS_TOPIC where TOPIC_ID = ?";

  private static String DELETE_NEWS = "delete from NEWS_CONTENT where NEWS_ID = ?";

  private static String TOPIC_LIST_QUERY =
       "select TOPIC_ID, TOPIC_NAME, DESCRIPTION, CREATE_DATE from NEWS_TOPIC order by TOPIC_NAME";

  private static String TOPIC_FILERTEDLIST_QUERY =
        "select TOPIC_ID, TOPIC_NAME, DESCRIPTION from NEWS_TOPIC where UPPER(TOPIC_NAME) LIKE UPPER(?) order by TOPIC_NAME";

  private static String STORY_QUERY = "select NEWS_STORY from NEWS_CONTENT where NEWS_ID = ?";

  private static NewsDb newsDB;  // the singleton object

  private static final int MAX_WHERE_IN_DEPTH = 999;
  
  private static final Log log = LogFactory.getLog(NewsDb.class);
  
  // cache all the topics
  private static Map topicCache = new TimedCache();
  private static Map articleCache = new TimedCache();
  private static TopicList topicList = new TopicList();
  private static boolean updated = false;

  private static String DATASOURCE;

  /**
   * Constructs a NewsDB object.
   * Note that this constructor is private in order to have a singleton object.
   */
  private NewsDb (String datasourceName)  {DATASOURCE = datasourceName;}

  /**
   * The access method to BriefcaseDB.  This is a static method that creates the
   * singleton object if it is not already created.
   * @param briefcaseProps - The property values read from briefcase.properties file.
   * @return BriefcaseDb object.
   */
  public static NewsDb getInstance(String datasourceName)
  {
    if (newsDB == null)
    {
      newsDB = new NewsDb(datasourceName);
    }
    return newsDB;
  }

  public String createTopic (String topicName, String description) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      /* INSERT INTO NEWS_TOPIC (?, ?, ?, ?) */
      ps = conn.prepareStatement(INSERT_TOPIC);
      String topicID = getUniqueTopicID();
      ps.setString(1, topicID);
      ps.setString(2, topicName);
      ps.setString(3, description);
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(4, currentTime);
      ps.executeUpdate();
      
      // add the topic to the cache
      topicCache.put(topicID, new Topic(topicID, topicName, description, currentTime));
      topicList.addTopic(topicID, topicName, description);
      
      return topicID;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to create news topic called " + topicName );
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

  public String createNews (NewsInfo newsInfo) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      /* INSERT INTO NEWS_CONTENT (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) */
      ps = conn.prepareStatement(INSERT_NEWS);
      String newsID = getUniqueNewsID();
      newsInfo.setID(newsID);
      ps.setString(1, newsID);
      ps.setString(2, newsInfo.getTitle());
      if (newsInfo.getAbstract().length() > 100)
        newsInfo.setAbstract(newsInfo.getAbstract().substring(0, 99));
      ps.setString(3, newsInfo.getAbstract());

      //using stream to pass data to LONGVARCHAR type
      //added by Jing
      /* This is not supported by the JTDS driver -- KG
      String story = newsInfo.getStory();
      byte [] bytes = story.getBytes();
      InputStream instr = new ByteArrayInputStream (bytes);
      ps.setAsciiStream(4, instr, bytes.length);
      */
      ps.setString(4, newsInfo.getStory());

      ps.setString(5, newsInfo.getTopicID());
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());
      ps.setTimestamp(6, currentTime);
      ps.setString(7, newsInfo.getAuthor());
      ps.setTimestamp(8, newsInfo.getBeginDate());
      ps.setTimestamp(9, newsInfo.getEndDate());
      ps.setNull(10, java.sql.Types.CHAR);
      ps.setString(11, newsInfo.getImage());
      ps.setString(12, newsInfo.getImageContentType());
      ps.setString(13, (newsInfo.getLayoutType()));
      ps.executeUpdate();
      
      // add the newsInfo object to the cache
      if(newsInfo.getBeginDate().getTime() < System.currentTimeMillis()){
          articleCache.put(newsInfo.getID(), newsInfo);
	      Topic topic = (Topic)topicCache.get(newsInfo.getTopicID());
	      if(topic != null){
	          topic.addArticle(newsInfo);
	      }else{
	          // topic does not exist on the map         
	      }
      }
      
      return newsID;
    }
    catch (SQLException sqe)
    {
	sqe.printStackTrace();
      LogService.instance().log(LogService.ERROR, "Unable to create news for  publisher " + newsInfo.getAuthor() + ", with title " + newsInfo.getTitle());
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

  private String getUniqueTopicID ()
  {
    return getUniqueID ("topic");
  }

  private String getUniqueNewsID ()
  {
    return getUniqueID ("news");
  }

 public String getArticleID ()
  {
    Connection conn = null;

    long idValue = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String uniqueIDSelect = "SELECT ID_VALUE FROM NEWS_UNIQUE_ID WHERE ID_NAME = 'news'";
      ps = conn.prepareStatement(uniqueIDSelect);
      rs = ps.executeQuery();
      if (rs.next())
      {
        idValue = rs.getLong(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to retrieve articleID for NEWS");
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

    // Generate a new number
    if (idValue == 0)
      idValue = 1;
    else
      idValue++;

    return String.valueOf (idValue);
  }

  private static synchronized String getUniqueID (String idName)
  {
    Connection conn = null;

    long idValue = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      conn = getConnection(DATASOURCE);

    try
    {
   //  conn = DriverManager.getConnection(DB_URL, null, null);

      String uniqueIDSelect = "SELECT ID_VALUE from NEWS_UNIQUE_ID where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDSelect);
      ps.setString(1, idName);
      rs = ps.executeQuery();
      if (rs.next())
      {
        idValue = rs.getLong(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to update unique ID for " + idName);
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
    }

    // Generate a new number
    if (idValue == 0)
      idValue = 1;
    else
      idValue++;

    // Store the number back in the table.
    try
    {
      String uniqueIDUpdate = "UPDATE NEWS_UNIQUE_ID set ID_VALUE = ? where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDUpdate);
      ps.setLong(1, idValue);
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


    } catch (SQLException sqe) {
      LogService.log(LogService.ERROR, sqe);
    } finally {
      try
      {
        if (conn != null) conn.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
    }

    return String.valueOf(idValue);
  }


  /**
   * Returns all the news stories that pertain to the given topic, if
   * the current time is within the story's timeline (begin and end date).
   * @param topicID - ID of the topic whose news is desired.
   * @return List of NewsInfo object containing all the selected news items.
   */
  public java.util.List getNewsItems (String topicID, Timestamp beginDate, Timestamp endDate, ArrayList myGroups, String userID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList newsList = new ArrayList();

    try
    {
     // need to only return articles whose topics are within my groups
     // to be implemented
     conn = getConnection(DATASOURCE);
     String NEWS_QUERY = null;
     if (topicID == null)
     { // user requested to search by date
      NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, NEWS_STORY, TOPIC_ID FROM NEWS_CONTENT WHERE BEGIN_DATE >= ? and END_DATE <= ?";
      ps = conn.prepareStatement(NEWS_QUERY);
      ps.setTimestamp(1, beginDate);
      ps.setTimestamp(2, endDate);
     }
     else if (topicID != null && topicID.equals("*"))
     {
       // user requested to search for all articles
       NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, NEWS_STORY, TOPIC_ID FROM NEWS_CONTENT";
       ps = conn.prepareStatement(NEWS_QUERY);
     }
     else
     {
        // search by topicID
        NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, NEWS_STORY, TOPIC_ID FROM NEWS_CONTENT WHERE TOPIC_ID = ? ";//and BEGIN_DATE <= ? and END_DATE >= ?";
        ps = conn.prepareStatement(NEWS_QUERY);
        ps.setString(1, topicID);
        //Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        //ps.setTimestamp(2, currentTime);
        //ps.setTimestamp(3, currentTime);
     }

      rs = ps.executeQuery();
      while (rs.next())
      {
        NewsInfo newsInfo = new NewsInfo(rs.getString(1));
        newsInfo.setTitle(rs.getString(2));
        String newsAbstract = rs.getString(3);
        if (newsAbstract == null || newsAbstract.trim().equals(""))
        {
          String story = rs.getString(4);
          newsAbstract = (story.length() > 50 ? story.substring(0, 50) : story );
        }
        newsInfo.setAbstract(newsAbstract);
        String topicIDFound = rs.getString(5);
        if (checkTopicGroups (myGroups, topicIDFound, userID))
          newsList.add(newsInfo);
      }
      return newsList;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get news items for topic ID " + topicID);
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

  }

  public java.util.List getNewsItems (String topicID) throws SQLException {
      List idList = new ArrayList(1);
      idList.add(topicID);
      Map m = getNewsItems(idList);
      return (List)m.get(topicID);
  }

  public java.util.Map getNewsItems (List topicIDs) throws SQLException {
      if (topicIDs.size() <= MAX_WHERE_IN_DEPTH) {
          return __getNewsItems(topicIDs);
      }

      // chunking up the user name retrievals
      Map m = new HashMap();
      int start=0;
      int end=Math.min(MAX_WHERE_IN_DEPTH, topicIDs.size());
      while (start<topicIDs.size()) {
          m.putAll(__getNewsItems(topicIDs.subList(start, end)));
          start=end;
          end=Math.min(start+MAX_WHERE_IN_DEPTH, topicIDs.size());
      }
      return m;
  }
  
  private Timestamp getTimestamp() {
      try {
          return TimeServiceFactory.getService().getTimestamp();
      } catch (FactoryCreateException fce) {
          throw new RuntimeException("Unable to retrieve timestamp.", fce);
      }
  }

  private java.util.Map __getNewsItems (List topicIDs) throws SQLException {
      if (topicIDs == null || topicIDs.size() == 0) return new HashMap(0);

      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Map retMap = new TreeMap();
      StringBuffer sql = new StringBuffer();
      NewsInfo newsInfo = null;
      String topicId = null;
      Timestamp today = getTimestamp();
      today.setNanos(0);
      today.setSeconds(0);

      try {
          conn = getConnection(DATASOURCE);
          sql.append("SELECT NEWS_ID, TITLE, ABSTRACT, AUTHOR, TOPIC_ID, BEGIN_DATE, END_DATE, ");
          sql.append("NEWS_STORY, IMAGE_NAME, IMAGE_MIME_TYPE, LAYOUT_TYPE FROM NEWS_CONTENT WHERE ");
          sql.append(" BEGIN_DATE <= ? and END_DATE >= ? and TOPIC_ID in (");

          for (int i=0; i<topicIDs.size(); i++) {
              if (i>0) {
                  sql.append(',');
              }
              sql.append('?');
          }
          sql.append(") order by TOPIC_ID");

          if (log.isDebugEnabled()) {
              log.debug("NewsDb::__getNewsItems executing sql: " + sql);
          }

          ps = conn.prepareStatement(sql.toString());
          
          int i=1;
          ps.setTimestamp(i++, today);
          ps.setTimestamp(i++, today);

          for (int j=0; j<topicIDs.size(); j++) {
              ps.setString(i++, (String)topicIDs.get(j));
          }
          
          if (log.isDebugEnabled()) {
              log.debug("Executing sql: " + sql.toString());
              StringBuffer sb = new StringBuffer();
              sb.append(today.toString()).append(", ").append(today.toString());
              for (int j=0; j<topicIDs.size(); j++) {
                  sb.append(", ").append(topicIDs.get(j));
              }
              log.debug("With parameters: " + sb.toString());
          }
           
          rs = ps.executeQuery();

          while (rs.next()) {
              topicId = rs.getString("TOPIC_ID");
              newsInfo = new NewsInfo(rs.getString("NEWS_ID"));
              newsInfo.setTitle(rs.getString("TITLE"));
              String newsAbstract = rs.getString("ABSTRACT");
              if (newsAbstract == null || newsAbstract.trim().equals("")) {
                String story = rs.getString("NEWS_STORY");
                newsAbstract = (story.length() > 50 ? story.substring(0, 50) : story );
              }
              newsInfo.setAbstract(newsAbstract);
              newsInfo.setBeginDate(rs.getTimestamp("BEGIN_DATE"));
              newsInfo.setEndDate(rs.getTimestamp("END_DATE"));
              newsInfo.setAuthor(rs.getString("AUTHOR"));
              newsInfo.setTopicID(topicId);
              newsInfo.setStory(rs.getString("NEWS_STORY"));
              newsInfo.setImage(rs.getString("IMAGE_NAME"));
              newsInfo.setImageContentType(rs.getString("IMAGE_MIME_TYPE"));
              newsInfo.setLayoutType(rs.getString("LAYOUT_TYPE"));
        
              List newsList = (List)retMap.get(topicId);
              if (newsList == null) {
                  newsList = new ArrayList();
                  retMap.put(topicId, newsList);
              }
              newsList.add(newsInfo);
              articleCache.put(newsInfo.getID(), newsInfo);
          }
      } finally {
          RDBMServices.closeResultSet(rs);
          RDBMServices.closePreparedStatement(ps);
          RDBMServices.releaseConnection(conn);
          rs = null;
          ps = null;
          conn = null;
      }
      return retMap;
  }

  public java.util.List getNewsItems (String topicID, Timestamp beginDate, Timestamp endDate) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    ArrayList newsList = new ArrayList();
    Topic topic = null;

    try
    {
     // need to only return articles whose topics are within my groups
     // to be implemented
     conn = getConnection(DATASOURCE);
     String NEWS_QUERY = null;
     if (topicID == null)
     { // user requested to search by date
      NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, AUTHOR, TOPIC_ID, BEGIN_DATE, END_DATE, " +
        		"NEWS_STORY, IMAGE_NAME, IMAGE_MIME_TYPE, LAYOUT_TYPE FROM NEWS_CONTENT WHERE BEGIN_DATE > ? and END_DATE < ?";
      ps = conn.prepareStatement(NEWS_QUERY);
      ps.setTimestamp(1, beginDate);
      ps.setTimestamp(2, endDate);
     }
     else if (topicID != null && topicID.equals("*"))
     {
         // use topicCache if available
         if(!topicCache.isEmpty()){
             topic = (Topic)topicCache.get(topicID);
             if(topic != null && topic.getArticles().size() > 0){
                 return topic.getArticles();
             }
         }
       // user requested to search for all articles
       NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, AUTHOR, TOPIC_ID, BEGIN_DATE, END_DATE, " +
        		"NEWS_STORY, IMAGE_NAME, IMAGE_MIME_TYPE, LAYOUT_TYPE FROM NEWS_CONTENT";
       ps = conn.prepareStatement(NEWS_QUERY);
     }
     else
     {   
        // search by topicID         
        NEWS_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, AUTHOR, TOPIC_ID, BEGIN_DATE, END_DATE, " +
        		"NEWS_STORY, IMAGE_NAME, IMAGE_MIME_TYPE, LAYOUT_TYPE FROM NEWS_CONTENT WHERE TOPIC_ID = ? and BEGIN_DATE < ? and END_DATE > ?";
        ps = conn.prepareStatement(NEWS_QUERY);
        ps.setString(1, topicID);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(2, currentTime);
        ps.setTimestamp(3, currentTime);
     }
      rs = ps.executeQuery();
      while (rs.next())
      {
        NewsInfo newsInfo = new NewsInfo(rs.getString("NEWS_ID"));
        newsInfo.setTitle(rs.getString("TITLE"));
        String newsAbstract = rs.getString("ABSTRACT");
        if (newsAbstract == null || newsAbstract.trim().equals(""))
        {
          String story = rs.getString("NEWS_STORY");
          newsAbstract = (story.length() > 50 ? story.substring(0, 50) : story );
        }
        newsInfo.setAbstract(newsAbstract);
        newsInfo.setBeginDate(rs.getTimestamp("BEGIN_DATE"));
        newsInfo.setEndDate(rs.getTimestamp("END_DATE"));
        newsInfo.setAuthor(rs.getString("AUTHOR"));
        newsInfo.setTopicID(rs.getString("TOPIC_ID"));
        newsInfo.setStory(rs.getString("NEWS_STORY"));
        newsInfo.setImage(rs.getString("IMAGE_NAME"));
        newsInfo.setImageContentType(rs.getString("IMAGE_MIME_TYPE"));
        newsInfo.setLayoutType(rs.getString("LAYOUT_TYPE"));
        
        newsList.add(newsInfo);
        articleCache.put(newsInfo.getID(), newsInfo);
        if(topic != null){
            topic.addArticle(newsInfo);
        }
      }      
      return newsList;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get news items for topic ID " + topicID);
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

  }

  public boolean checkTopicGroups (ArrayList myGroups, String topicID, String userID)
  {
    // need to check if Topic's Group permission is within myGroups
    ArrayList topicGroupIds = getTopicGroupID (topicID);

    for (int k=0; k < topicGroupIds.size(); k++)
    {
      String found = (String) topicGroupIds.get(k);
      int dashat = found.indexOf("-");
      String groupID = found.substring(0, dashat);
      String entityType = found.substring(dashat+1);

      if (entityType.equals("3") && myGroups.contains(groupID))
        return true;
      else if (entityType.equals("2") && groupID.equals(userID))
        return true;
    }
    return false;
  }

  public String getNewsItemsCount (String topicID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
     conn = getConnection(DATASOURCE);
      String COUNT_QUERY = "SELECT COUNT(*) FROM NEWS_CONTENT WHERE TOPIC_ID = ?";
      ps = conn.prepareStatement(COUNT_QUERY);
      ps.setString(1, topicID);
      rs = ps.executeQuery();
      while (rs.next())
      {
        return rs.getString(1);
      }
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
    return "0";
  }

  public int removeTopicGroupIDs (String topicID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {

      conn = getConnection(DATASOURCE);
      String REMOVE_GROUPS = "DELETE FROM NEWS_TOPIC_GROUPS WHERE TOPIC_ID = ?";
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


  public void setTopicGroups (String topicID, String userID, ArrayList groupsList) throws SQLException
  {
    Connection conn = null;

    try
    {
      PreparedStatement ps = null;
      conn = getConnection(DATASOURCE);
      String SETGROUP_QUERY = "INSERT INTO NEWS_TOPIC_GROUPS VALUES (?,?,?,?)";

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
            if (ps != null) {
              ps.close();
              ps = null;
            }
          } catch (Exception e) {
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

  public ArrayList getSharedTopicID (String userID, ArrayList myGroups)
  {
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	
	    try
	    {
		      conn = getConnection(DATASOURCE);
		
		      String GET_MYTOPICS = "SELECT DISTINCT TOPIC_ID FROM NEWS_TOPIC_GROUPS WHERE GROUP_ID IN (";

		for (int i=0; i < myGroups.size(); i++)
		{
		
		  if (i == 0)
		    GET_MYTOPICS += "?";
		  else
		    GET_MYTOPICS += ", ?";
		
		}

      GET_MYTOPICS += ") OR GROUP_ID = ? AND GROUP_ENTITY_TYPE=2";
      ps = conn.prepareStatement(GET_MYTOPICS);

	//      ps.setString(1, userID);
	
	for (int i=0; i < myGroups.size(); i++)
	{
	     ps.setString((i+1), (String) myGroups.get(i));
	}

      ps.setString((myGroups.size() + 1), userID);


      rs = ps.executeQuery();
      ArrayList topicsFound = new ArrayList ();
      while (rs.next())
      {
        topicsFound.add(rs.getString(1));
      }
        return topicsFound;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get topicIDs for groups with userID = " +userID);
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

  public ArrayList getAllGroupIDs ()
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String GETGROUP_QUERY = "SELECT DISTINCT GROUP_ID, GROUP_ENTITY_TYPE FROM NEWS_TOPIC_GROUPS";
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

  public ArrayList getTopicGroupID (String topicID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String GETGROUP_QUERY = "SELECT GROUP_ID, GROUP_ENTITY_TYPE FROM NEWS_TOPIC_GROUPS WHERE TOPIC_ID = ?";
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

  /**
   * Returns all the information regarding a news item in the form of a
   * NewsInfo data object.
   * @param newsID - ID of the news item
   * @return A NewsInfo data object containing the news item information.
   */
  public NewsInfo getNewsInfo (String newsID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    NewsInfo newsInfo = null;
    
    newsInfo = (NewsInfo)articleCache.get(newsID);
    if(newsInfo != null){
        return newsInfo;
    }
    
    try
    {
      conn = getConnection(DATASOURCE);
      String NEWS_INFO_QUERY = "SELECT NEWS_ID, TITLE, ABSTRACT, AUTHOR, TOPIC_ID, BEGIN_DATE, END_DATE, NEWS_STORY, IMAGE_NAME, IMAGE_MIME_TYPE, LAYOUT_TYPE FROM NEWS_CONTENT WHERE NEWS_ID = ?";
      ps = conn.prepareStatement(NEWS_INFO_QUERY );
      ps.setString(1, newsID);
      rs = ps.executeQuery();
      if (rs.next())
      {
        newsInfo = new NewsInfo();
        newsInfo.setID(rs.getString("NEWS_ID"));
        newsInfo.setTitle(rs.getString("TITLE"));
        newsInfo.setAbstract(rs.getString("ABSTRACT"));
        newsInfo.setAuthor(rs.getString("AUTHOR"));
        newsInfo.setTopicID(rs.getString("TOPIC_ID"));
        newsInfo.setBeginDate(rs.getTimestamp("BEGIN_DATE"));
        newsInfo.setEndDate(rs.getTimestamp("END_DATE"));
	// Special Note for Oracle: You must fetch in column order or
	// the stream will be closed before you read this "LONG" column --KG
        newsInfo.setStory(rs.getString("NEWS_STORY"));
        newsInfo.setImage(rs.getString("IMAGE_NAME"));
        newsInfo.setImageContentType(rs.getString("IMAGE_MIME_TYPE"));
        newsInfo.setLayoutType(rs.getString("LAYOUT_TYPE"));

      }
    }
    catch (SQLException sqe)
    {
	sqe.printStackTrace();
      LogService.instance().log(LogService.ERROR, "Unable to get news information for news ID " + newsID);
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
    return newsInfo;
  }

  public int updateUserConfig (UserConfig userConfig, String userID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);

      if (getUserConfig(userID) == null)
      {
        // user does not have a config saved, must create new one
        String INSERT_USER_CONFIG = "INSERT INTO NEWS_USER_CONFIG VALUES (?, ?, ?)";
        ps = conn.prepareStatement(INSERT_USER_CONFIG);
        ps.setString(1, userID);
        ps.setString(2, userConfig.getLayout());
        ps.setString(3, userConfig.getItemsPerTopic());
        return ps.executeUpdate();
      }
      else
      {
        String UPDATE_USER_CONFIG = "UPDATE NEWS_USER_CONFIG SET LAYOUT = ?, ITEMS_PER_TOPIC = ? WHERE USER_ID = ?";

        ps = conn.prepareStatement(UPDATE_USER_CONFIG);
        ps.setString(1, userConfig.getLayout());
        ps.setString(2, userConfig.getItemsPerTopic());
        ps.setString(3, userID);

        return ps.executeUpdate();
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while updating user_config for userID = " + userID);
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


  public UserConfig getUserConfig (String userID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    UserConfig myConfig = null;
    try
    {
      conn = getConnection(DATASOURCE);
      String NEWS_USER_CONFIG = "SELECT LAYOUT, ITEMS_PER_TOPIC FROM NEWS_USER_CONFIG WHERE USER_ID = ?";
      ps = conn.prepareStatement(NEWS_USER_CONFIG);
      ps.setString(1, userID);
      rs = ps.executeQuery();
      if (rs.next())
      {
        myConfig = new UserConfig();
        myConfig.setLayout (rs.getString(1));
        myConfig.setItemsPerTopic (rs.getString(2));
      }
      return myConfig;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get user configuration");

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
   * Returns the complete story for the given news ID.
   * @param newsID - ID of the news item
   * @return the complete story.
   */
  public String getNewsStory (String newsID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    Object newsInfo = articleCache.get(newsID);
    if(newsInfo != null){
        return ((NewsInfo)newsInfo).getStory();
    }
    
    try
    {
     // conn = DriverManager.getConnection(DB_URL, null, null);
      conn = getConnection(DATASOURCE);
      /* select NEWS_STORY from NEWS_CONTENT where NEWS_ID = ? */
      ps = conn.prepareStatement(STORY_QUERY);
      ps.setString(1, newsID);
      rs = ps.executeQuery();
      if (rs.next())
      {
        return rs.getString(1);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to get news story for news ID " + newsID);
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
    return null;
  }

  /**
   * Returns all the topics as a TopicList data object.
   * @return List of topics in the form of a TopicList data object.
   */
  public TopicList getTopicList ()
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    //TopicList topicList = new TopicList();
    
    if(!topicCache.isEmpty()){
        if(updated == true || topicList.getCount() <= 0){
            topicList = new TopicList();
            Iterator it = topicCache.keySet().iterator();
            Topic topic;
            while(it.hasNext()){
                //String id = (String)it.next();
                topic = (Topic)topicCache.get(it.next());
                topicList.addTopic(topic.getId(), topic.getName(), topic.getDesc());
            }
            updated = false;
        }
        topicList.begin();
    }else{
        topicList.clear();
	    try
	    {
			conn = getConnection(DATASOURCE);
			/* select TOPIC_ID, TOPIC_NAME, DESCRIPTION from NEWS_TOPIC order by TOPIC_NAME */
			ps = conn.prepareStatement(TOPIC_LIST_QUERY);
			rs = ps.executeQuery();
			while (rs.next())
			{
			    topicCache.put(rs.getString(1), new Topic(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDate(4)));
			    topicList.addTopic(rs.getString(1), rs.getString(2), rs.getString(3));
			}
	    }
	    catch (SQLException sqe)
	    {
	        LogService.instance().log(LogService.ERROR, "Exception while retrieving topic List.");
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
    return topicList;
  }

  // Return TopicList Object containing topics that the user has access to
  public TopicList getTopicList (ArrayList myTopicIDs) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;
    TopicList topicList = new TopicList();

    try
    {

      ResultSet rs = null;
      conn = getConnection(DATASOURCE);
      String TopicQuery = "SELECT TOPIC_ID, TOPIC_NAME, DESCRIPTION FROM NEWS_TOPIC WHERE TOPIC_ID = ? ORDER BY TOPIC_NAME";
      ps = conn.prepareStatement(TopicQuery);

      for (int i=0; i < myTopicIDs.size(); i++)
      {
        try {
        // loop through myGroupIDs and get topics I can publish too
        ps.setString(1, (String) myTopicIDs.get(i));
        rs = ps.executeQuery();
        while (rs.next())
        {
          topicList.addTopic(rs.getString(1), rs.getString(2), rs.getString(3));
        }
        } finally {
          try
          {
            if (rs != null) rs.close();
            rs = null;
          } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
          }
        }
      }
      return topicList;
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while retrieving topic List for groupIDs.");
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

  /**
   * Returns a list of news topics as a TopicList data object matching the
   * supplied search string criteria.  The search string can have the following
   * wild cards:
   *   '*' - match one or more
   *   '?' - match exactly one.
   * e.g. 'TA*' will return all topics starting with 'TA'
   *      'TA?ER' will return topics like TAMER, TAKER, TAPER, TATER, TAXER, etc.
   * @param searchString - a search string with possible wild cards
   * @return List of topics in the form of a TopicList data object.
   */
  public TopicList getTopicList (String searchString)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    TopicList topicList = new TopicList();

    try
    {

      conn = getConnection(DATASOURCE);
      /* select TOPIC_ID, TOPIC_NAME, DESCRIPTION from NEWS_TOPIC where TOPIC_NAME LIKE ? order by TOPIC_NAME */
      String filterString = searchString.replace('*', '%');
      filterString = filterString.replace('?', '_');
      ps = conn.prepareStatement(TOPIC_FILERTEDLIST_QUERY);
      ps.setString(1, filterString);
      rs = ps.executeQuery();
      while (rs.next())
      {
        topicList.addTopic(rs.getString(1), rs.getString(2), rs.getString(3));
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

  public int deleteTopic (String topicID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {

      conn = getConnection(DATASOURCE);
      /* delete from NEWS_TOPIC where TOPIC_ID = ? */
      ps = conn.prepareStatement(DELETE_TOPIC);
      ps.setString(1, topicID);
      
      // remove the topic from the topicCache
      topicCache.remove(topicID);
      updated = true;
      
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

  public int deleteTopic (ArrayList topicIDList) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String DELETE_TOPIC_LIST = "DELETE FROM NEWS_TOPIC WHERE TOPIC_ID IN("; //'3','4')"

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
        // remove the topic from the topicCache
        topicCache.remove(topicIDList.get(i));
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
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
  }

  public int deleteNewsItem (String newsID) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      /* delete from NEWS_CONTENT where NEWS_ID = ? */
      ps = conn.prepareStatement(DELETE_NEWS);
      ps.setString(1, newsID);
      
      // invalidate the cache
      topicCache.clear();
      articleCache.remove(newsID);
      
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while deleting news item " + newsID);
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

  public int deleteNewsItem (ArrayList articleIDList) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
     String DELETE_ARTICLE_LIST = "DELETE FROM NEWS_CONTENT WHERE NEWS_ID IN("; //'3','4')"
      for (int i=0; i < articleIDList.size(); i++)
      {
        if (i == 0)
          DELETE_ARTICLE_LIST += "?";
        else
          DELETE_ARTICLE_LIST += ", ?";
      }
      DELETE_ARTICLE_LIST += ")";

      ps = conn.prepareStatement(DELETE_ARTICLE_LIST);
      for (int i=0; i < articleIDList.size(); i++)
      {
        ps.setString((i+1), (String)articleIDList.get(i));
        // update article cache
        articleCache.remove((String)articleIDList.get(i));
      }
      
      
      //will not be able to update the cache
      // invalidate the cache
      topicCache.clear();
      
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while deleting news item list");
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

  public int updateTopic (String topicID, String topicName, String topicDesc) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String UPDATE_TOPIC = "UPDATE NEWS_TOPIC SET TOPIC_NAME = ?, DESCRIPTION = ? WHERE TOPIC_ID = ?";
      ps = conn.prepareStatement(UPDATE_TOPIC);
      ps.setString(1, topicName);
      if (topicDesc == null || topicDesc.trim().equals(""))
        ps.setNull(2, java.sql.Types.VARCHAR);
      else
        ps.setString(2, topicDesc);
      ps.setString(3, topicID);
      
      // update the topic information in the topicCache
      Topic topic = (Topic)this.topicCache.get(topicID);
      if(topic != null){
          topic.setName(topicName);
          topic.setDesc(topicDesc);
          updated = true;
      }else{
          // topic does not exist in the cache         
      }
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while updating information for topic " + topicID);
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

  public int updateNewsItem (NewsInfo newsInfo) throws SQLException
  {
    Connection conn = null;
    PreparedStatement ps = null;

    try
    {
      conn = getConnection(DATASOURCE);
      String UPDATE_NEWS = "update NEWS_CONTENT set " +
                            "TITLE = ?, ABSTRACT = ?, NEWS_STORY = ?, TOPIC_ID = ?, BEGIN_DATE = ?, END_DATE = ?, " +
                            "IMAGE_NAME = ?, IMAGE_MIME_TYPE = ?, LAYOUT_TYPE = ? where NEWS_ID = ?";

      ps = conn.prepareStatement(UPDATE_NEWS);
      ps.setString(1, newsInfo.getTitle());
      ps.setString(2, newsInfo.getAbstract());

      //using stream to pass data to LONGVARCHAR type
      //added by Jing
      /* This is not supported by the JTDS driver -- KG
      String story = newsInfo.getStory();
      byte [] bytes = story.getBytes();
      InputStream instr = new ByteArrayInputStream (bytes);
      ps.setAsciiStream(3, instr, bytes.length);
      */
      ps.setString(3, newsInfo.getStory());

      ps.setString(4, newsInfo.getTopicID());
      ps.setTimestamp(5, newsInfo.getBeginDate());
      ps.setTimestamp(6, newsInfo.getEndDate());
      ps.setString (7, newsInfo.getImage());
      ps.setString (8, newsInfo.getImageContentType());
      ps.setString (9, newsInfo.getLayoutType());
      ps.setString (10, newsInfo.getID());

      // news item updated. Update the topics article list
      articleCache.put(newsInfo.getID(), newsInfo);
      Topic topic = (Topic)this.topicCache.get(newsInfo.getTopicID());
      if(topic != null){
          if(newsInfo.getBeginDate().getTime() < System.currentTimeMillis()){
              topic.updateArticle(newsInfo);
          }else{
              topic.deleteArticle(newsInfo.getID());
          }
      }else{
          // topic does not exist in the cache       
      }
      
      return ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
	sqe.printStackTrace();
      LogService.instance().log(LogService.ERROR, "Exception while updating news item " + newsInfo.getID());
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

  /**
   * Subscribes to the list of news topics supplied as a java.util.List.
   * The method ignores topics contained in the list that are  already
   * subscribed to.  Any new topics are added to the database and topics that
   * are not present in the list but were previously subscribed to are deleted.
   * @param  personID - ID of the subscribing user
   * @param  topicList - List of topic IDs that are being subscribed to
   */
  public void subscribe (String personID, java.util.List topicList)
  {
    Connection conn = null;
    PreparedStatement psList = null, psInsert = null, psDelete = null;
    ResultSet rs = null;
    ArrayList currentTopicList = new ArrayList();

    try
    {
      conn = getConnection(DATASOURCE);
    // SQL to check for the existence of the topic
      String TOPIC_LIST = "SELECT TOPIC_ID FROM NEWS_TOPIC_SUBSCRIPTION WHERE USER_ID = ?";

      psList  = conn.prepareStatement(TOPIC_LIST);
      psList.setString(1, personID);
      try {
        rs = psList.executeQuery();
        while (rs.next())
        {
          String topicID = rs.getString(1);
          currentTopicList.add(topicID);
        }
      } finally {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          LogService.log(LogService.ERROR, e);
        }
      }

      // remove the elements that are common in both the lists.

      if(currentTopicList.size() > 0){
	      ListIterator listIt = topicList.listIterator();
	      while (listIt.hasNext())
	      {
	        String topicID = (String)listIt.next();
	        for (int i=0; i < currentTopicList.size(); i++)
	        {
	          if (((String)currentTopicList.get(i)).equals(topicID))
	          {
	            currentTopicList.remove(i);
	            listIt.remove();
	            break;
	          }
	        }
	      }
      }

      // At this point currentTopicList contains elements that are no longer subscribed to.
      // topicList contains elements that are newly subscribed to.

      // Insert newly selected topics
      String INSERT_TOPIC = "INSERT INTO NEWS_TOPIC_SUBSCRIPTION VALUES (?, ?, ?, 'N')";

      psInsert = conn.prepareStatement(INSERT_TOPIC);
      psInsert.setString(1, personID);
      Timestamp ts = new Timestamp (System.currentTimeMillis());
      psInsert.setTimestamp(3, ts);

      Iterator it = topicList.iterator();
      while (it.hasNext())
      {
        String topicID = (String)it.next();
        psInsert.setString(2, topicID);
        int numRows = psInsert.executeUpdate();
      }

      // Delete unselected topics
      String DELETE_TOPIC = "DELETE FROM NEWS_TOPIC_SUBSCRIPTION WHERE USER_ID = ? and TOPIC_ID = ?";

      psDelete = conn.prepareStatement (DELETE_TOPIC);
      psDelete.setString(1, personID);

      it = currentTopicList.iterator();
      while (it.hasNext())
      {
        String topicID = (String)it.next();
        psDelete.setString(2, topicID);
        int numRows = psDelete.executeUpdate();
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while subscribing topic for user " + personID);
    }
    finally
    {
      try
      {
        if (psList != null) psList.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (psInsert != null) psInsert.close();
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      try
      {
        if (psDelete != null) psDelete.close();
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
   * Subscribes to a single new topic for the given user.
   * @param personID - ID of the user
   * @param topicID  - ID of the news topic to be subscribed
   */
  public void subscribe (String personID, String topicID)
  {
    Connection conn = null;
    PreparedStatement psInsert = null;

    try
    {
      //conn = DriverManager.getConnection(DB_URL, null, null);
      conn = getConnection(DATASOURCE);

      // SQL to insert a new topic
      String INSERT_TOPIC = "insert into NEWS_TOPIC_SUBSCRIPTION values (?, ?, ?, 'N')";

      psInsert = conn.prepareStatement(INSERT_TOPIC);

      psInsert.setString(1, personID);
      psInsert.setString(2, topicID);

      Timestamp ts = new Timestamp (System.currentTimeMillis());
      psInsert.setTimestamp(3, ts);

      int numRows = psInsert.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while subscribing topic: " + topicID + " for user " + personID);
    }
    finally
    {
      try
      {
        if (psInsert != null) psInsert.close();
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
   * Deletes all the subscribed topics for the given user.
   * @param personID - ID of the user whose subscription topics will be deleted.
   * @return number of topics deleted (which is equal to the number of topics
   * that were origianlly subscribed by the user).
   */
  public int deleteAllSubscriptionTopics (String personID)
  {
    Connection conn = null;
    PreparedStatement psDelete = null;
    int numRows = 0;

    try
    {
      //conn = DriverManager.getConnection(DB_URL, null, null);
      conn = getConnection(DATASOURCE);

      // SQL to delete all the topics
      String DELETE_TOPICS = "delete from NEWS_TOPIC_SUBSCRIPTION where USER_ID = ?";

      psDelete = conn.prepareStatement(DELETE_TOPICS);

      psDelete.setString(1, personID);

      numRows = psDelete.executeUpdate();
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while deleting subscription topics for user: " +  personID);
    }
    finally
    {
      try
      {
        if (psDelete != null) psDelete.close();
      }
      catch (Exception e){}

      try
      {
        if (conn != null) conn.close();
      }
      catch (Exception e){}
    }
    return numRows;
  }

  /**
   * Returns a list of topic IDs that are subscribed by the given user.
   * @param personID - ID of the user
   * @return java.util.List of news topics subscribed.
   */
  public TopicList getSubscribedTopics (String personID)
  {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    TopicList topicList = new TopicList();

    try
    {
   //   conn = DriverManager.getConnection(DB_URL, null, null);
      conn = getConnection(DATASOURCE);

      // SQL to delete all the topics
      String sql =
        "select A.TOPIC_ID, B.TOPIC_NAME from NEWS_TOPIC_SUBSCRIPTION A, NEWS_TOPIC B where USER_ID = ? and A.TOPIC_ID = B.TOPIC_ID";

      ps = conn.prepareStatement(sql);

      ps.setString(1, personID);

      rs = ps.executeQuery();

      while (rs.next())
      {
        String topicID = rs.getString(1);
        String topicName = rs.getString(2);
        topicList.addTopic(topicID, topicName, null);
      }
    }
    catch (SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Exception while retrieving subscribed topics for user: " +  personID);
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

  public static java.sql.Connection getConnection (String jndiDatasourceName) throws SQLException
  {
    try
    {
      /*Context ctx = new InitialContext();
      DataSource ds = (DataSource)ctx.lookup (jndiDatasourceName);
      return ds.getConnection();
       **/
        //return org.jasig.portal.RDBMServices.getConnection (jndiDatasourceName);
      return com.interactivebusiness.portal.VersionResolver.getConnectionByPortalVersions (jndiDatasourceName);
    }
    catch (Exception ne)
    {
      return null;
    }
  }
}
