/*



 *******************************************************************************



 *



 * File:       Universe.java



 *



 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved



 *



 * This source code is the confidential and proprietary information of Unicon.



 * No part of this work may be modified or used without the prior written



 * consent of Unicon.



 *



 *******************************************************************************



 */
package net.unicon.portal.channels.campusannouncement.domain;



import java.sql.*;
import java.util.*;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.*;
import org.jasig.portal.security.*;
import net.unicon.sdk.util.*;
import net.unicon.sdk.db.DBUtil;
import net.unicon.portal.channels.campusannouncement.*;
import net.unicon.portal.channels.campusannouncement.common.*;
import net.unicon.portal.channels.campusannouncement.types.*;
import net.unicon.portal.channels.campusannouncement.util.*;
import net.unicon.sdk.time.*;
import net.unicon.portal.util.db.AcademusDBUtil;
import com.interactivebusiness.portal.VersionResolver;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.MemberFactory;
import net.unicon.portal.groups.UniconGroupServiceFactory;

public class Universe {
	private static final Log LOG = LogFactory.getLog(Universe.class);
	
	protected final Log log = LogFactory.getLog(getClass());

    
    private static List administrativeUserNames;
    private static IGroup adminGroup;
    private static final String CHANNEL = CampusAnnouncementChannel.CHANNEL;


    protected LMS lms = new AcademusLMS();



    protected Cache cache = null;


    private VersionResolver vr = VersionResolver.getInstance();


    static {
        try {
            String rawProperty = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.channels.campusannouncement.CampusAnnouncementChannel.administrativeUserNames");

            if (rawProperty == null) {
                rawProperty = "";
            } else {
                rawProperty = rawProperty.trim().toLowerCase();
            }

            administrativeUserNames =
                StringUtil.listFromDelimitedString(rawProperty, ",");

            adminGroup = UniconGroupServiceFactory.getService().
                getAdministratorGroup();
        } catch (Exception e) {
            LOG.error("Failed initializing Universe", e);
        }
    }

    private Universe() {



    }



    public Cache getCache() {



        return cache;



    }



    public Map getChannelPreferences(IChannel channel, ChannelStaticData staticData, IPerson person) throws Exception {



        String userName = UPortalUtil.getUserName(person);


        /* 
         * This is the legacy behavior, but it appears wrong.  Shouldn't each subsribed instance of the channel
         * have its own preferences? -apetro
         */
        String channelID = staticData.getChannelPublishId();



        List rows = AcademusDBUtil.query("select preference_name, preference_value from channel_preference where user_name = ? and channel_publish_id = ?",

        CollectionUtil.arrayList(userName, channelID));



        return asMap(rows);



    }



    public void setChannelPreferences(IChannel channel, ChannelStaticData staticData, IPerson person,

    Map preferencesMap) throws Exception {



        String userName = UPortalUtil.getUserName(person);



        String channelID = staticData.getChannelPublishId();



        Connection connection = null;



        try {



            connection = AcademusDBUtil.getDBConnection();

            //added by Jing
            if (connection.getAutoCommit())
              connection.setAutoCommit(false);

            DBUtil.executeUpdate(connection, "delete from channel_preference where user_name = ? and channel_publish_id = ?",

            CollectionUtil.arrayList(userName, channelID));



            Iterator it = preferencesMap.entrySet().iterator();



            while (it.hasNext()) {



                Map.Entry entry = (Map.Entry) it.next();



                List parameters = CollectionUtil.arrayList(userName, channelID, entry.getKey(), "" + entry.getValue());



                DBUtil.executeUpdate(connection, "insert into channel_preference (user_name, channel_publish_id, preference_name, preference_value) values (?, ?, ?, ?)",

                parameters);



            }



            connection.commit();



        } finally {



            AcademusDBUtil.safeRollback(connection);



            AcademusDBUtil.safeReleaseDBConnection(connection);



        }



    }



    public List getGroupInfo() throws Exception {



        return AcademusDBUtil.query("select group_id, group_name from up_group");



    }



    public Announcement createCampusAnnouncement() {



        return new CampusAnnouncement(new Integer(-1), "0", "Everyone", "", new Date(), null);



    }


    //get anouncements from announcement table,
    //including both offering and campuse annoucnements
    public List getAnnouncements (IPerson person, int ageLimit, List selectedGroups) throws Exception {
    	
      if (log.isTraceEnabled()) {
    	  log.trace("getAnnouncements for person [" + person + " with ageLimit [" + ageLimit + "] and selected groups [" + selectedGroups + "]");
      }
    	
      List answer = new ArrayList();
      if (selectedGroups == null)
        return answer;

      StringBuffer questionMarks = new StringBuffer ();
      for (int i = 0; i < selectedGroups.size(); i++)
        questionMarks.append("?,");
      questionMarks.append("?");

      StringBuffer queryBuffer = new StringBuffer ("select announcement.announcement_id, submit_date, revise_date, body, announcement_id_groups.group_id,announcement_id_groups.group_entity_type from announcement, announcement_id_groups where announcement.announcement_id = announcement_id_groups.announcement_id and announcement_id_groups.group_id in (");
      queryBuffer.append(questionMarks.toString()).append(")  and submit_date > ? order by announcement.announcement_id");

      String userKey = vr.getUserKeyColumnByPortalVersions(person);
      List userList = new ArrayList ();
      userList.add(userKey);

      java.sql.Date minimumDate = new java.sql.Date(DateUtil.computeDateDaysAgo(ageLimit).getTime());
      List parameters = new ArrayList ();
      parameters.add(minimumDate);

      List combineList = CollectionUtil.combine (CollectionUtil.combine(selectedGroups, userList), parameters);
      List rows = AcademusDBUtil.query(queryBuffer.toString(), combineList);

        Announcement announcement = null;
        String group_id, group_type, group_name="";
        int announcement_id;
        List group_rows, group_row;
        List id_param;

        int prev_id =0;
        String display_name = "";
        for (int i = 0; i < rows.size(); i++)
        {
          List row = (List) rows.get(i);
          try{
            announcement_id = ( (Integer) row.get(0)).intValue();
          }
          catch (ClassCastException cce){
          //Orcale returns as BigDecimal
            announcement_id = ((java.math.BigDecimal) row.get(0)).intValue();
          }
          group_id = (String) row.get(4);
          group_type = (String) row.get(5);
            if (group_type.equals("3"))
            {
              try
              {
                group_name = org.jasig.portal.services.EntityNameFinderService.
                    instance().
                    getNameFinder(Class.forName(
                    "org.jasig.portal.groups.IEntityGroup")).getName(group_id);
              }
              catch (Exception ex)
              {
                group_name = "Group Missing";
              }
            }

              else if (group_type.equals("2"))
             {
               group_name = org.jasig.portal.services.EntityNameFinderService.
                   instance().
                   getNameFinder(Class.forName(
                   "org.jasig.portal.security.IPerson")).getName(group_id);

             }

             announcement = new OfferingAnnouncement( (Number) row.get(0),
                          "" + group_id, group_name,// + " -- " + "topics_id",
                          (String) row.get(3), (Date) row.get(1), (Date) row.get(2));
             answer.add(announcement);
        }
        return answer;
    }

    public String getAnnouncementOwner(String announcementId) throws Exception {
      String owner = null;
      String sql =
        "select owner_id from announcement_id_groups where announcement_id = ?";
      
      List rows = null;
      List parameters = new ArrayList();
      parameters.add(announcementId);
      rows = AcademusDBUtil.query(sql, parameters);

      if (rows.size() == 0) return null;

      return (String)((List)rows.get(0)).get(0);
    }

    //get anouncements owned by the user
    public List getOwnedAnnouncements (IPerson person) throws Exception {
      StringBuffer queryBuffer = null;
      List rows = null;

      //check if the user has permission to access all annoucements
      IAuthorizationPrincipal ap =
        vr.getPrincipalByPortalVersions (person);

      if (ap.hasPermission(CHANNEL, CampusAnnouncementChannel.EDIT_ALL, CHANNEL)) {
        queryBuffer = new StringBuffer ("select announcement.announcement_id, submit_date, revise_date, body from announcement where announcement_id in (select announcement_id from announcement_id_groups)");
        rows = AcademusDBUtil.query(queryBuffer.toString());
      } else {
        queryBuffer = new StringBuffer ("select announcement.announcement_id, submit_date, revise_date, body from announcement where announcement_id in (select announcement_id from announcement_id_groups where owner_id=?)");
        List parameters = new ArrayList ();
        parameters.add(vr.getUserKeyColumnByPortalVersions(person));
        rows = AcademusDBUtil.query(queryBuffer.toString(), parameters);
      }

      List answer = new ArrayList(rows.size());
        Announcement announcement = null;
        Integer announcement_id;
        for (int i = 0; i < rows.size(); i++) {
          List row = (List) rows.get(i);
          try{
            announcement_id = (Integer) row.get(0);
          }
          catch (ClassCastException cce){
            announcement_id = new Integer (((java.math.BigDecimal) row.get(0)).intValue());
          }
          announcement = new OfferingAnnouncement( (Number) row.get(0),
                "","",// + group_id, group_name,// + " -- " + "topics_id",
                (String) row.get(3), (Date) row.get(1), (Date) row.get(2));
            answer.add(announcement);

        }
        return answer;
    }

/*    public List getAnnouncements(IPerson person, boolean getCampusAnnouncements, List offeringIDs,

    int ageLimit) throws Exception {



    List offeringAnnouncements = lms.getAnnouncements(person, offeringIDs, ageLimit);



        List campusAnnouncements = getCampusAnnouncements ? getCampusAnnouncements(person, ageLimit) : null;



        return CollectionUtil.combine(offeringAnnouncements, campusAnnouncements);




    }*/



    public Announcement getCampusAnnouncement(int announcementID) throws Exception {



       // List rows = AcademusDBUtil.query("select campus_announcement.campus_announcement_id, up_group.group_id, up_group.group_name, campus_announcement.body, campus_announcement.submit_date, campus_announcement.revise_date from campus_announcement, up_group where campus_announcement.campus_announcement_id = ? and campus_announcement.group_id = up_group.group_id",

       // CollectionUtil.arrayList(new Integer(announcementID)));
       List rows = AcademusDBUtil.query("select announcement_id, body, submit_date, revise_date from announcement where announcement_id = ? ", CollectionUtil.arrayList(new Integer(announcementID)));
        if (rows.size() == 0) {
            return null;
        }
        List row = (List) rows.get(0);
        return new CampusAnnouncement((Number) row.get(0), "", "", (String) row.get(1), (Date) row.get(2), (Date) row.get(3));
    }



   /* public List getCampusAnnouncements(IPerson person, int ageLimit) throws Exception {



        Integer universalGroupID = new Integer(0);



        //String userKey = "" + person.getID();
	//in uportal 2.1, userKey is username
	String userKey = vr.getUserKeyColumnByPortalVersions (person);

        java.sql.Date minimumDate = new java.sql.Date(DateUtil.computeDateDaysAgo(ageLimit).getTime());



        List parameters = CollectionUtil.arrayList(universalGroupID, userKey, minimumDate, minimumDate);



        List rows = AcademusDBUtil.query("select distinct campus_announcement.campus_announcement_id, up_group.group_id, up_group.group_name, campus_announcement.body, campus_announcement.submit_date, campus_announcement.revise_date from campus_announcement, up_group, up_group_membership where ((up_group.group_id = ? and up_group.group_id = up_group_membership.group_id and campus_announcement.group_id = up_group.group_id) or (up_group_membership.member_key = ? and up_group.group_id = up_group_membership.group_id and campus_announcement.group_id = up_group.group_id)) and (campus_announcement.revise_date >= ? or campus_announcement.submit_date >= ?)",

        parameters);



        List answer = new ArrayList(rows.size());



        for (int i = 0; i < rows.size(); i++) {



            List row = (List) rows.get(i);



            Announcement announcement = new CampusAnnouncement((Number) row.get(0), (String) row.get(1), (String) row.get(2), (String) row.get(3), (Date) row.get(4), (Date) row.get(5));



            answer.add(announcement);



        }



        return answer;



    }*/



/*    public int updateAnnouncement(int announcementID, String groupID, String message) throws Exception {



        int count = -1;



        Connection connection = null;
        TimeService ts = TimeServiceFactory.getService();


        try {



            connection = AcademusDBUtil.getDBConnection();



            if (announcementID > 0) {



                System.out.println("groupID on update = " + groupID);

                count = DBUtil.executeUpdate(connection,
                "update campus_announcement set group_id = ?, revise_date = ?, body = ? where campus_announcement_id = ?",
                CollectionUtil.arrayList(groupID, ts.getTimestamp(), message, new Integer(announcementID)));
		//count = DBUtil.executeUpdate(connection,

                //"update campus_announcement set group_id = ?, revise_date = now(), body = ? where campus_announcement_id = ?",
		//temp. solution for hsql
                //count = DBUtil.executeUpdate(connection,

                //"update campus_announcement set group_id = ?, revise_date = curdate(), body = ? where campus_announcement_id = ?",

                //CollectionUtil.arrayList(groupID, message, new Integer(announcementID)));



            } else {



                System.out.println("groupID on insert = " + groupID);

                count = DBUtil.executeUpdate(connection,
                "insert into campus_announcement (group_id, submit_date, body) values (?, ?, ?)", CollectionUtil.arrayList(groupID, ts.getTimestamp(), message));

                //count = DBUtil.executeUpdate(connection,

                //"insert into campus_announcement (group_id, submit_date, body) values (?, now(), ?)", CollectionUtil.arrayList(groupID, message));

		//temp. solution for hsql
                //count = DBUtil.executeUpdate(connection, "insert into campus_announcement (campus_announcement_id, group_id, submit_date, body) values ((select max(campus_announcement_id)+1 from campus_announcement),?, curdate(), ?)", CollectionUtil.arrayList(groupID, message));



            }



            connection.commit();



        } catch (Error error) {



            throw error;



        } catch (Exception exception) {



            throw exception;



        } finally {



            AcademusDBUtil.safeRollback(connection);



            AcademusDBUtil.releaseDBConnection(connection);



        }



        return count;



    }*/



    public int deleteAnnouncement(int announcementID) throws Exception {

    	if (log.isTraceEnabled()) {
    		log.trace("Deleting announcement with ID [" + announcementID + "]");
    	}


        int count = -1;



        Connection connection = null;



        try {



            connection = AcademusDBUtil.getDBConnection();
            if (connection.getAutoCommit())
              connection.setAutoCommit(false);

            //count = DBUtil.executeUpdate(connection, "delete from campus_announcement where campus_announcement_id = ?",

            //CollectionUtil.arrayList(new Integer(announcementID)));

            //modified by Jing
            count = DBUtil.executeUpdate(connection, "delete from announcement_id_groups where announcement_id = ?",
            CollectionUtil.arrayList(new Integer(announcementID)));
            count = DBUtil.executeUpdate(connection, "delete from announcement where announcement_id = ?",
            CollectionUtil.arrayList(new Integer(announcementID)));

            connection.commit();



        } catch (Error error) {



            throw error;



        } catch (Exception exception) {



            throw exception;



        } finally {



            AcademusDBUtil.safeRollback(connection);



            AcademusDBUtil.safeReleaseDBConnection(connection);



        }



        return count;



    }


    public boolean isAdmin(IPerson person) {
        if (person == null) return false;
        String username = UPortalUtil.getUserName(person);
        if (administrativeUserNames.contains(username.toLowerCase())) {
            return true;
        }


        try {
            return adminGroup.contains(MemberFactory.getMember(username));
        } catch (Exception uge) {
            log.error("Failed querying admin group to check if [" +
                username + "] is a member");
        }
        return false;
    }


    public List getOfferingInfo(IPerson person) throws Exception {



        return lms.getOfferingInfo(person);



    }



    protected Map asMap(List listOfPairs) {



        Map answer = new HashMap(listOfPairs.size());



        for (int i = 0; i < listOfPairs.size(); i++) {



            List pair = (List) listOfPairs.get(i);



            answer.put(pair.get(0), pair.get(1));



        }



        return answer;



    }



    public static Universe getUniverse() {



        return SOLE_INSTANCE;



    }



    protected static Universe SOLE_INSTANCE = createSoleInstance();



    protected static Universe createSoleInstance() {



        Universe universe = new Universe();



        return universe;



    }


    //added by Jing
    /*
     * return a list of annnouncer
     */
    /*public List getCurrentAnnouncer () throws Exception
    {
       List list = AcademusDBUtil.query(
            "select group_id, group_entity_type from announcement_announcer");
        return list;
    }*/

    /**
     * save announcers
     */
    /*public void saveAnnouncers (Map map) throws Exception {
      Connection connection = null;
      try {
        connection = AcademusDBUtil.getDBConnection();
        if (connection.getAutoCommit())
          connection.setAutoCommit(false);
        //delete announcers
        DBUtil.executeUpdate(connection, "delete from announcement_announcer");

        //insert newly assigned announcers
        Set set = map.keySet();
        Iterator it = set.iterator();
        String mapKey, group_id, entity_type;
        List list;
        while (it.hasNext()) {
          mapKey = (String) it.next();
          group_id = mapKey.substring(0, mapKey.indexOf("_"));
          entity_type = mapKey.substring(mapKey.indexOf("_")+1);
          list = CollectionUtil.arrayList(group_id, entity_type);
          DBUtil.executeUpdate(connection, "insert into announcement_announcer values (?,?)", list);
        }
        connection.commit();
      }
      catch (Exception e) {
        throw e;
    }
    finally {
      AcademusDBUtil.safeRollback(connection);
      AcademusDBUtil.releaseDBConnection(connection);
    }
}*/

   /**
     * save announcement
     */
    public void saveAnnouncement (String id, String date, String body, String[] groupsKey, String[] groupsType, IPerson person) throws Exception {
      Connection connection = null;
      TimeService ts = TimeServiceFactory.getService();
      try {
        connection = AcademusDBUtil.getDBConnection();
        if (connection.getAutoCommit())
          connection.setAutoCommit(false);
        List list_ann, list_group;
        String owner = vr.getUserKeyColumnByPortalVersions (person);
        Integer sequence_id;
        //insert/update annuncement table
        //insert into unique_id_groups table
        if (Integer.parseInt(id) < 0)
        {
          //announcement_id = Long.parseLong(getUniqueAnnouncementID());
          //insert into annoucement table with sequence number
          list_ann = CollectionUtil.arrayList(ts.getTimestamp(), body);
          DBUtil.executeUpdate(connection, "insert into announcement (submit_date, body) values (?, ?) ", list_ann);
          //get the announcement id generated
          List tempList = DBUtil.executeQuery(connection, "select max(announcement_id) from announcement");
          List firstRow = (List) tempList.get(0);
          try{
            sequence_id = (Integer) firstRow.get(0);
          }
          catch (ClassCastException cce){
            //when connecting to orcale, it returns as BigDecimal instead of Integer
            sequence_id = new Integer(((java.math.BigDecimal) firstRow.get(0)).intValue());
          }
        }
        else
        {
          // preserve owner
          owner = getAnnouncementOwner(id);
          if (owner == null) {
            owner = vr.getUserKeyColumnByPortalVersions (person);
            log.warn("CampusAnnouncements - Universe::saveAnnouncement : No owner found for announcement with ID [" + id + "] - pushing ownership to [" +
              owner + "]");
          }
          sequence_id = new Integer (id);
          list_ann = CollectionUtil.arrayList(ts.getTimestamp(), body, sequence_id);
          DBUtil.executeUpdate(connection, "update announcement set revise_date = ?, body = ? where announcement_id = ?", list_ann);
          List tempList = CollectionUtil.arrayList(sequence_id);
          DBUtil.executeUpdate(connection, "delete from announcement_id_groups where announcement_id = ?", tempList);
        }
        for (int i = 0; i < groupsKey.length; i++)
        {
          list_group = CollectionUtil.arrayList(sequence_id, groupsKey[i],
                                                groupsType[i], owner);
          DBUtil.executeUpdate(connection, "insert into announcement_id_groups (announcement_id, group_id, group_entity_type, owner_id) values (?,?,?,?)", list_group);
        }

        connection.commit();
      }
      catch (Exception e) {
        throw e;
    }
    finally {
      AcademusDBUtil.safeRollback(connection);
      AcademusDBUtil.safeReleaseDBConnection(connection);
    }
}

  private String getUniqueAnnouncementID ()
  {
    return getUniqueID ("announcement");
  }

  private static synchronized String getUniqueID (String idName)
  {
    long idValue = 0;
    PreparedStatement ps = null;
    Connection conn = null;
    ResultSet rs = null;

    try {
      conn = AcademusDBUtil.getDBConnection();
    try
    {
      String uniqueIDSelect = "SELECT ID_VALUE from ANNOUNCEMENT_UNIQUE_ID where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDSelect);
      ps.setString(1, idName);
      rs = ps.executeQuery();
      if (rs.next())
      {
        idValue = Long.parseLong(rs.getString(1));
      }
    }
    catch (Exception sqe)
    {
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
        rs = null;
      }
      catch (Exception e){
          e.printStackTrace();
      }
      try
      {
        if (ps != null) ps.close();
        ps = null;
      }
      catch (Exception e){
          e.printStackTrace();
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
      String uniqueIDUpdate = "UPDATE ANNOUNCEMENT_UNIQUE_ID set ID_VALUE = ? where ID_NAME = ?";
      ps = conn.prepareStatement(uniqueIDUpdate);
      ps.setString(1, idValue+"");
      ps.setString(2, idName);
      ps.executeUpdate();
    }
    catch (SQLException sqe)
    {
    }
    finally
    {
      try
      {
        if (ps != null) ps.close();
      }
      catch (Exception e){}
    }

    try
    {
      AcademusDBUtil.safeRollback(conn);
    }
    catch (Exception sqe){}


    } catch (Exception e) {
        e.printStackTrace();
    } finally {
      AcademusDBUtil.safeReleaseDBConnection(conn);
    }

    return String.valueOf(idValue);
  }

  public List getAnnouncementGroups (int announcement_id) throws Exception
  {
    List list =AcademusDBUtil.query("select group_id, group_entity_type from announcement_id_groups where announcement_id = ?", CollectionUtil.arrayList(new Integer (announcement_id)));
    return list;
  }
}

