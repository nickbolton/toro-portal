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
package net.unicon.academus.apps.notification;

import java.text.*;
import java.util.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;
import java.io.FileInputStream;

import org.jasig.portal.RDBMServices;

import net.unicon.academus.apps.rad.DBService;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Rdbm;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.portal.channels.rad.Info;

import java.net.InetAddress;
import javax.mail.*;
import javax.mail.internet.*;

// Unicon

// Unicon

/**
 * Notification allows a channel to notify other users by text or email. Other users will see
 * the list of notifications in the channel CNotification, from which they can go to
 * the notification's source to view/edit. The Notification channel also supports the group,i.e,
 * when a notificattion addresses to a group then all the users in this group will be notified.
 *
 * A litle for developers:
 * I. The status of Notification:
 *  - "New": When first time created.
 *  - "Save": When user clicks the "Save" button on the CNotification channel.
 *  - "DeletedByUser": When user clicks the trash image on the CNotification channel.
 *  - "DeletedBySource": When the notification's source cancel the content of notification.
 * II. All the actions of Notification:
 *  1. New notification: the parameters are m_id, m_recipient, m_type, m_sender, m_date,
 *    m_text, m_notified, m_params, m_status="New".
 *  2. The source channel ( from which the notification is issued) deletes the content of notification.
 *    The parameters are <notified> (i.e. the source channel),<type>,<params> (now is delete info.).
 *    The two things should do:
 *      a. update status="DeletedBySource"
 *         where ( <notified>,<type>,<params>) and status!="Save" and status!="DeletedByUser"
 *      b. update params=<deleteInfo>, notified=null where ( <notified>,<type>,<params>)
 *  3. The source channel changes the content of the notification ( <notified>,<type>,<params>) with the <newParams>
 *      update params=<newParams> where ( <notified>,<type>,<params>)
 *  4. list of notifications for user
 *   a. select all matching rows from upc_notification where (recipient in <userIDlist>) or recipient like '%.userID'
 *    b. for each noti of a.
 *      if noti.recipient is user (status is "New|Save|DeletedBySource") --> add noti to userOutput
 *        if noti.recipient is group ( status is "New|DeletedBySource") --> add noti to groupOutput
 *        if noti.recipient is group.user (status is "Save|DeletedByUser") --> add noti to guOutput
 *    c. for each grNoti in groupOutput
 *     userNoti = findUserNoti( guOutput, grNoti.m_notiID,grNoti.m_recipient);
 *      if( userNoti == null)
 *       add grNoti to userOutput
 *    d. for each noti in guOutput
 *     if( noti.m_status != "DeletedByUser") add to userOutput
 *  5. User save noti (<notiID>, <recipientID>, status="New")
 *    if recipientID is group
 *      Insert new noti record ( <notiID>, <recipientID>.userID, status="Save")
 *    if recipientID is user
 *     Update to ( <notiID>, <recipientID>, status="Save")
 *  6. User deletes noti( <notiID>, <recipientID>, status="New|Save")
 *    if recipientID is group
 *     if status="New":
 *       Insert new noti record ( <notiID>, <recipientID>.userID, status="DeletedByUser")
 *      if status="Save":( record for 5. exists)
 *       Update noti record to ( <notiID>, <recipientID>.userID, status="DeletedByUser")
 *    if recipientID is user
 *     Delete record ( <notiID>, <recipientID>)
 *  7. Cleanup
 *    delete from upc_notification where status="DeletedBySource|New" and expire
 *    delete from upc_notification where status="DeletedByUser" and not exist group record
 */

public class Notification {
  /**
   * The default notification expiration in days
   */
  final static int DEFAULT_EXPIRE = 7;

  final static int NOTIFICATION_ID = 1;
  final static int RECIPIENT_ID = 2;
  final static int APP_TYPE = 3;
  final static int SENDER = 4;
  final static int SENT_DATE = 5;
  final static int NOTI_TEXT = 6;
  final static int NOTIFIED = 7;
  final static int PARAMS = 8;
  final static int STATUS = 9;

  static final String SEPARATOR = ".";
  static final String STATUS_NEW = "New";
  static final String STATUS_SAVE = "Save";
  static final String STATUS_DELETEDBYUSER = "DeletedByUser";
  static final String STATUS_DELETEDBYSOURCE = "DeletedBySource";

  static final String MSG_NONE_EMAIL_SENDER = "[Notification]NONE_E_MAIL_SENDER";
  static final String MSG_MAIL_SERVER_FAILED = "[Notification]Mail Server is not working";

  // Error codes;
  public static final int EC_NONE_SENDER_EMAIL = -2;
  //

  /**
   * Internal notification id.
   */
  public int m_notificationId;

  /**
   * Who will receive the notification.
   */
  public String m_recipient;

  /**
   * The time when notification is created.
   */
  public java.sql.Timestamp m_date;

  /**
   * The type of the notification. It depends on source channel.
   */
  public String m_type;

  /**
   * The user from which notification is sent.
   */
  public IdentityData m_sender; // IdentityData (E#u#id#displayname#email)

  /**
   * The brief content to be displayed on the CNotification channel
   */
  public String m_text;   // title of notification

  /**
   * The source channel.
   */
  public String m_notified; // fully quanlify name of class of channel to be notified

  /**
   * The paramters of notification. The source channel will take care of them.
   */
  public String m_params; // store params which needed to open(view/edit) a notification

  /**
   * The status of the notification
   */
  public String m_status; // deleted, new, replied,...

  /**
   * The subject of the email of notification.
   */
  public String m_subject;   // Subject of mail message

  /**
   * The body of email of notification.
   */
  public String m_body; // Body of message

  /**
   * The action flags indicating what to do with the notification.
   */
  public static int AC_ALL = 3; // All bit 0 , 1 is high
  public static int AC_NOTIFICATION = 1; // Bit 0 is high
  public static int AC_MAIL = 2; // Bit 1 is high

  //-------------Error Code of send method------------------//
  /**
   * No error.
   */
  public static int EC_NONE = 1;

  /**
   * Fails to send email.
   */
  public static int EC_SEND_EMAIL_FAILED = 1;

  /**
   * Start a new thread Test to check whether MailServer is working or not.
   */
  static {
    new Test().start();
  }
  static boolean m_mailServerWorking = true;

  // hibernate stubs
  public int getNotificationId() { return 0; }
  public void setNotificationId(int i) {}
  public String getReceipientId() { return null; }
  public void setReceipientId(String s) {}
  public String getAppType() { return null; }
  public void setAppType(String s) {}
  public String getSender() { return null; }
  public void setSender(String s) {}
  public java.util.Date getSendDate() { return null; }
  public void setSendDate(java.util.Date d) {}
  public String getNotiText() { return null; }
  public void setNotiText(String s) {}
  public String getNotified() { return null; }
  public void setNotified(String s) {}
  public String getParams() { return null; }
  public void setParams(String s) {}
  public String getStatus() { return null; }
  public void setStatus(String s) {}

  /**
   * Test Mail Server is working or not.
   * @author Thach
   */
  public static class Test extends Thread {
    public void run() {
      String mailHost = null;
      try {
        mailHost = getMailHost();
        InetAddress addr = InetAddress.getByName(mailHost);
        Socket sock = new Socket(addr,25); // Default Port to send e-mail
      } catch (Exception e) {
        System.out.println("[Notification] test MailServer '" + mailHost + "' failed.");
        m_mailServerWorking = false;
      }
    }
  }

  /**
   * Create a new thread to send e-mail
   * @author Thach
   */
  public class ThreadSendMail extends Thread {
    IdentityData[] m_ids;
    public ThreadSendMail(IdentityData[] ids) {
      m_ids = ids;
    }

    public void run() {
      try {
        // Expand group to user for send mail
        m_ids = GroupData.expandGroups(m_ids, true);

        // Check e-mail of sender
        String email = m_sender.getEmail();
        if (m_sender.getEmail() == null) {
          Channel.log("[Notification.send]e-mail of sender:" + email);
          throw new Exception(MSG_NONE_EMAIL_SENDER);
        } else
          Channel.log("[Notification.send]e-mail of sender:" + email);

        Vector v = new Vector();
        for (int i=0; i < m_ids.length; i++) {
          // Thach Dec-04
          // If id is "-1", cuid is e-mail address
          if (m_ids[i] == null)
            continue;
          if (m_ids[i].getID() != null && m_ids[i].getID().equals("-1"))
            email = m_ids[i].getName();
          else
            email = Finder.getEmail(m_ids[i]);
          //Tien 0830
          if (email != null && email.trim().length() > 0) {
            v.addElement(getAddress(email, Finder.getName(m_ids[i])));
            Channel.log("User '" + m_ids[i].getName() + "' has e-mail:" + email);
          } else
            Channel.log("User '" + m_ids[i].getName() + "' no e-mail.");
        }

        // Here v contains all email addresses to send.
        if (v.size() > 0) {
          InternetAddress[] ias = new InternetAddress[v.size()];
          for (int i=0; i < ias.length; i++)
            ias[i] = (InternetAddress)v.elementAt(i);
          sendMail(ias);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  void prepareSend(IdentityData[] ids) {
    // When use id as username
    //System.out.println("PrepareSend.before");
    for( int i = 0; i < ids.length; i++) {
      if( !ids[i].getType().equals("O") && ids[i].getAlias() == ids[i].getID())
        ids[i].putOID(null);
      //System.out.println("PrepareSend:"+ids[i]);
    }

    // Find other info.
    Finder.findDetails(ids, null);

    //System.out.println("PrepareSend.after");
    for( int i = 0; i < ids.length; i++) {
      //System.out.println("PrepareSend:"+ids[i]);
    }
  }


  /**
   * Send e-mail to persons with given identifers.
   * @param ids Array of IdentityData object. These identifer is group or person
   * @param action One of values: AC_MAIL, AC_NOTIFICATION, AC_ALL. Or use bit operator such as:
   * AC_MAIL | AC_NOTIFICATION (it means AC_ALL)
   * This method get all of datum from member of this instance such as:
   * m_data, m_type, m_sender, m_text, m_notified, m_params and ids
   * @return 0 (EC_NONE) successfull. Other, error code: Wiew error codes starting with "EC_";
   * @throws Exception if it could not send message.
   */
  public int send(IdentityData[] ids, int action) throws Exception { // Thach-Apr04
    if (ids == null || ids.length == 0)
      return -1;

    // Get only distinct identities
    ids = GroupData.normalize( ids);

    // Fill details: email, alias
    prepareSend(ids);

    // Send notification...
    if ((action & AC_NOTIFICATION) != 0)
      notify(ids);

    // Send emails...
    if ((action & AC_MAIL) != 0) {
        new ThreadSendMail(ids).start();
    }
    return EC_NONE;
  }

  //----------------------------------------------------------------------//

  static final long ONE_DAY_MILLIS = 24*60*60*1000;
  static long lastCleanup = Channel.getCurrentDate().getTime() - ONE_DAY_MILLIS;
  //static final long ONE_DAY_MILLIS = 120000;//24*60*60*1000;

  /**
    * Clean up all the expired notifications. The expire duration is taken from the RAD property "notification.expire".
    * @throws Exception if error when update the database
   */
  public static void cleanup() throws Exception {
    long currentTime = Channel.getCurrentDate().getTime();
    if (currentTime > lastCleanup + ONE_DAY_MILLIS) {

      HashSet grSet = new HashSet();
      Vector userDel = new Vector();
      Connection conn = null;
      boolean oldAutoCommit = false;
      try {
        PreparedStatement ps = null;
        conn = RDBMServices.getConnection();
        oldAutoCommit = conn.getAutoCommit();
        RDBMServices.setAutoCommit(conn,false);

        // Cleanup expired records
        String sql;
        Timestamp timestamp = new Timestamp(currentTime - getNumOfDayOfExpire()*ONE_DAY_MILLIS);

        // Delete Notification of Group/Entity that their STATUS is not SAVED
        sql = "DELETE FROM UPC_NOTIFICATION WHERE SENT_DATE < ? AND STATUS IN ('" + STATUS_DELETEDBYSOURCE + "','" + STATUS_NEW + "')";
        Channel.log("[Notification.cleanup]sql=" + sql);
        //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);

        try {
        ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, timestamp);
        ps.executeUpdate();
        } finally {
          try {
            if (ps != null) ps.close();
            ps = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        // Delete all record with the status "DeletedByUser" and don't have group record
        sql = "SELECT NOTIFICATION_ID, RECIPIENT_ID FROM UPC_NOTIFICATION WHERE STATUS=?";
        //ps = new RDBMServices.PreparedStatement(conn,sql);

        ResultSet rs = null;
        try {
        ps = conn.prepareStatement(sql);
        ps.setString(1,STATUS_DELETEDBYUSER);
        rs = ps.executeQuery();
        String userSeparator = SEPARATOR + GroupData.PORTAL_USER;
        while(rs.next()) {
          int notiID = rs.getInt(1);
          String recipient = rs.getString(2);
          userDel.addElement(""+notiID+"_"+recipient);

          int idx = recipient.indexOf(userSeparator);
          if( idx != -1) {
            String gr = recipient.substring(0,idx);
            grSet.add(gr+"_"+notiID);
          }
        }

        } finally {
          try {
            if (rs != null) rs.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          try {
            if (ps != null) ps.close();
            ps = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        // Check existing group
        if( grSet.size() > 0) {
          String[] allGroups = (String[])grSet.toArray(new String[0]);
          String list = "(";
          for( int i = 0; i < allGroups.length; i++)
            list += (i==0?"":",") + SQL.esc(allGroups[i]);
          list += ")";
          
          sql = "SELECT NOTIFICATION_ID, RECIPIENT_ID FROM UPC_NOTIFICATION" +
                " WHERE cast (RECIPIENT_ID as varchar(50)) "+ SQL.CONCAT +" '_' "+ SQL.CONCAT +"  cast (NOTIFICATION_ID as varchar(50)) IN " +list;
          
          Channel.log("[Notification.cleanup]sql=" + sql);
          //ps = new RDBMServices.PreparedStatement(conn,sql);

          try {
          ps = conn.prepareStatement(sql);
          rs = ps.executeQuery();
          while(rs.next()) {
            int notiID = rs.getInt(1);
            String gr = rs.getString(2);
            String compose = ""+notiID+"_"+gr;

            // Remove from userDel
            int i = 0;
            while( userDel.size() > 0 && i < userDel.size()) {
              String user = (String)userDel.elementAt(i);
              if( user.startsWith(compose))
                userDel.removeElementAt(i);
              else
                i++;
            }
          }

          } finally {
            try {
              if (rs != null) rs.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
            try {
              if (ps != null) ps.close();
              ps = null;
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }

        // Delete userDel
        if( userDel.size() > 0) {
          int count = 0;
          String list = "(";
          for( int i = 0; i < userDel.size(); i++) {
            String recipient = (String)userDel.elementAt(i);// id_recipient
            int idx = recipient.indexOf('_');
            if( idx != -1) {
              String compose = recipient.substring(idx+1) + '_' + recipient.substring(0,idx);
              list += (count==0?"":",") + SQL.esc(compose);
              count++;
            }
          }
          list += ")";

          if( count > 0) {
            sql = "DELETE FROM UPC_NOTIFICATION" +
                  " WHERE cast (RECIPIENT_ID as varchar(50))" + SQL.CONCAT + "'_'" + SQL.CONCAT + " cast (NOTIFICATION_ID as varchar(50)) IN " + list;
            Channel.log("[Notification.cleanup]sql=" + sql);

            try {
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
            } finally {
              try {
                if (ps != null) ps.close();
                ps = null;
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }

        // commit all
        RDBMServices.commit(conn);

        // Save last cleanup time
        lastCleanup = currentTime;
      } catch (SQLException e) {
        Channel.log(e);
        RDBMServices.rollback(conn);
        throw e;
      } finally {
        if (conn != null)
          RDBMServices.setAutoCommit(conn,oldAutoCommit);
        RDBMServices.releaseConnection(conn);
      }
    }
  }

  //----------------------------------------------------------------------//

  /**
   * Notify all given IdentityData with this Notification.
   * @param ids array of distinct portal IdentityData
   * @throws Exception if there is an database error
   */
  void notify( IdentityData[] ids) throws Exception {
    String sql;
    int notiId;
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean oldAutoCommit = false;

    try {
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      for( int i = 0; i < ids.length; i++) {
        // Exclude unknown users
        if( ids[i].getID() != null && ids[i].getID().equals(IdentityData.ID_UNKNOWN))
          continue;

        // Get next Id
        sql = "SELECT MAX (NOTIFICATION_ID) FROM UPC_NOTIFICATION WHERE RECIPIENT_ID LIKE '"+ids[i].getIdentifier('_') + "%'";
        //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        notiId = 1;
        if (rs.next())
          notiId = rs.getInt(1) + 1;
        notiId = (notiId < 1)? 1: notiId;

        sql = "INSERT INTO UPC_NOTIFICATION(NOTIFICATION_ID,RECIPIENT_ID,APP_TYPE,SENDER,SENT_DATE,NOTI_TEXT,NOTIFIED,PARAMS,STATUS) VALUES(?,?,?,?,?,?,?,?,?)";
        //ps = new RDBMServices.PreparedStatement(conn,sql);
        ps = conn.prepareStatement(sql);
        ps.setInt(1, notiId);
        ps.setString(2,ids[i].getIdentifier());
        ps.setString(3,m_type);
        ps.setString(4,m_sender.toString());
        ps.setTimestamp(5,m_date);
        ps.setString(6,m_text);
        ps.setString(7,m_notified);
        ps.setString(8,m_params);
        ps.setString(9,STATUS_NEW);
        ps.executeUpdate();

		rs.close();
		rs = null;
		ps.close();
		ps = null;
      }

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (conn != null)
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
  }

  //------------------------------------------------------------//
  //------User actions------------------------------------------//
  //------------------------------------------------------------//
  /**
  * User deletes noti( <notiID>, <recipientID>, status="New|Save")
  * if recipientID is group
  *  if status="New|DeletedBySource": ( recipient is group only)
  *   Insert new noti record ( <notiID>, <recipientID>.userID, status="DeletedByUser")
  *  if status="Save":( record for 5. exists - recipient is group.user)
  *   Update noti record to ( <notiID>, <recipientID>, status="DeletedByUser")
  *
  * if recipientID is user
  *  Delete record ( <notiID>, <recipientID>)
  *
   * Delete a notification for specified recipient.
   * @param notiId The notification id to delete.
   * @param recipient field RECIPIENT_ID in table UPC_NOTIFICATION. The recipient can be a group.
   * @param owner The logon user that want to delete his notification.
   * @return true if successfully, else - false
   * @throws Exception if there are database errors.
   */
  public static boolean delete(int notiId, String recipient, IdentityData owner) throws Exception {
    // Check
    Notification noti = getNotification(notiId, recipient);
    if( noti == null)
      return false;

    int ret = 0;
    String sql = null;
    Connection conn = null;
    boolean oldAutoCommit = false;
    PreparedStatement ps = null;

    try {
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      // Group
      if( recipient.startsWith(GroupData.PORTAL_GROUP)) {
        // Insert new notification record, if status is new|DeletedBySource, recipient is group only
        int notiID = noti.m_notificationId;
        if( noti.m_status.equals(STATUS_NEW) || noti.m_status.equals(STATUS_DELETEDBYSOURCE)) {
          sql = "INSERT INTO UPC_NOTIFICATION(NOTIFICATION_ID,RECIPIENT_ID,APP_TYPE,SENDER,SENT_DATE,NOTI_TEXT,NOTIFIED,PARAMS,STATUS) VALUES(?,?,?,?,?,?,?,?,?)";
          //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
          ps = conn.prepareStatement(sql);
          ps.setInt(1, notiId);
          ps.setString(2,noti.m_recipient + SEPARATOR + owner.getIdentifier());
          ps.setString(3,noti.m_type);
          ps.setString(4,noti.m_sender.toString());
          ps.setTimestamp(5,noti.m_date);
          ps.setString(6,noti.m_text);
          ps.setString(7,noti.m_notified);
          ps.setString(8,noti.m_params);
          ps.setString(9,STATUS_DELETEDBYUSER);
          ret = ps.executeUpdate();
        }

        // Update existing notification record, if status is Save, recipient is group.user
        else if(noti.m_status.equals(STATUS_SAVE)) {
          sql = "UPDATE UPC_NOTIFICATION SET STATUS=? WHERE NOTIFICATION_ID=? AND RECIPIENT_ID=?";
          //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
          ps = conn.prepareStatement(sql);
          ps.setString(1,STATUS_DELETEDBYUSER);
          ps.setInt(2, notiID);
          ps.setString(3,recipient);
          ret = ps.executeUpdate();
        }
      } else {
        sql = "DELETE FROM UPC_NOTIFICATION WHERE NOTIFICATION_ID=? AND RECIPIENT_ID=?";
        //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
        ps = conn.prepareStatement(sql);
        ps.setInt(1, noti.m_notificationId);
        ps.setString(2,noti.m_recipient);
        ret = ps.executeUpdate();
      }
      Channel.log("******User Delete Notification********");
      Channel.log(sql);
      Channel.log("**************************");

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (conn != null) 
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }

    return (ret > 0);
  }

  /**
   * User save noti (<notiID>, <recipientID>, status="New")
   * if recipientID is group
   *  Insert new noti record ( <notiID>, <recipientID>.userID, status="Save")
   * if recipientID is user
   *  Update to ( <notiID>, <recipientID>, status="Save")
    *
    * Save a notification to view later. Normally if a notification isn't saved
    * then it will deleted when expires. The saved notification exists untill the user deletes it.
    * @param notiId The notification id to save.
    * @param recipient The recipient of notification to save. It can be a group.
    * @param owner The logon user that want to save the notification.
    * @throws Exception if there are database errors.
   */
  public static void save(int notiId, String recipient, IdentityData owner) throws Exception {
    Notification noti = getNotification(notiId, recipient);
    if( noti == null)
      return;

    Connection conn = null;
    boolean oldAutoCommit = false;
    PreparedStatement ps = null;

    try {
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      String sql;
      int notiID = noti.m_notificationId;
      // recipient is group only
      if( recipient.startsWith(GroupData.PORTAL_GROUP)) {
        // Insert new notification record
        sql = "INSERT INTO UPC_NOTIFICATION(NOTIFICATION_ID,RECIPIENT_ID,APP_TYPE,SENDER,SENT_DATE,NOTI_TEXT,NOTIFIED,PARAMS,STATUS) VALUES(?,?,?,?,?,?,?,?,?)";
        //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
        ps = conn.prepareStatement(sql);
        ps.setInt(1, notiID);
        ps.setString(2,noti.m_recipient + SEPARATOR + owner.getIdentifier());
        ps.setString(3,noti.m_type);
        ps.setString(4,noti.m_sender.toString());
        ps.setTimestamp(5,noti.m_date);
        ps.setString(6,noti.m_text);
        ps.setString(7,noti.m_notified);
        ps.setString(8,noti.m_params);
        ps.setString(9,STATUS_SAVE);
        ps.executeUpdate();
      }

      // Portal user, recipient is user
      else  {
        sql = "UPDATE UPC_NOTIFICATION SET STATUS=? WHERE NOTIFICATION_ID=? AND RECIPIENT_ID=?";
        //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
        ps = conn.prepareStatement(sql);
        ps.setString(1,STATUS_SAVE);
        ps.setInt(2, notiID);
        ps.setString(3,recipient);
        ps.executeUpdate();
      }
      Channel.log("******User Save Notification********");
      Channel.log(sql);
      Channel.log("**************************");

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (conn != null)
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
  }

  /**
   * Get list of notification for given user ( logon user)
  * a. select all matching rows from upc_notification where (recipient in <userIDlist>) or recipient like '%.userID'
  * b. for each noti of a.
  *   if noti.recipient is user (status is "New|Save|DeletedBySource") --> add noti to userOutput
  *   if noti.recipient is group ( status is "New|DeletedBySource") --> add noti to groupOutput
  *   if noti.recipient is group.user (status is "Save|DeletedByUser") --> add noti to guOutput
  * c. for each grNoti in groupOutput
  *   userNoti = findUserNoti( guOutput, grNoti.m_notiID,grNoti.m_recipient);
  *   if( userNoti == null)
  *    add grNoti to userOutput
  *
  * d. for each noti in guOutput
  *   if( noti.m_status != "DeletedByUser") add to userOutput
  *
   * @param owner The logon user that want to list his notification.
   * @return Vector of Notification
   * @throws Exception if there are database errors.
   */
  public static Vector listNotifications( IdentityData owner) throws Exception {
    if( owner.getID() == null)
      return new Vector();

    // When using id as username (alias)
    if( owner.getAlias() == owner.getID()) {
      owner.putOID(null);
      Finder.findDetail(owner,null);
      if (owner.getAlias() != null && owner.getID() == null)
        owner.putID(owner.getAlias());
    }

    // clean up notis expired
    cleanup();

    // Select all user records
    String sql = "SELECT notification_id, recipient_id, app_type, sender, sent_date, noti_text, notified, params, status FROM upc_notification WHERE recipient_id IN ("+getIDList(owner)+
                 ") OR recipient_id LIKE '%" + SEPARATOR + owner.getIdentifier('_') + "' ORDER BY sent_date DESC";
    Channel.log("******list Notification********");
    Channel.log(sql);
    Channel.log("**************************");
    Vector v = new Vector();
    Vector gr = new Vector();
    Vector grUser = new Vector();
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      conn = RDBMServices.getConnection();
      String userSeparator = SEPARATOR + GroupData.PORTAL_USER;
      stmt = conn.createStatement();
      rs = stmt.executeQuery(sql);
      while(rs.next()) {
        // Read noti record
        Notification noti = newNotification( rs);

        // Portal Group
        if( noti.m_recipient.startsWith(GroupData.PORTAL_GROUP)) {
          if( noti.m_recipient.indexOf(userSeparator) == -1)
            gr.addElement(noti);
          else
            grUser.addElement(noti);
        } else
          v.addElement(noti);
      }
    } catch(SQLException e) {
      Channel.log(e);
      throw e;
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
      RDBMServices.releaseConnection(conn);
    }

    // Add group noti
    for( int i = 0; i < gr.size(); i++) {
      Notification grNoti = (Notification)gr.elementAt(i);
      if( findUserNoti( grUser, grNoti) == null)
        v.addElement(grNoti);
    }

    // Check user noti
    for( int i = 0; i < grUser.size(); i++) {
      Notification userNoti = (Notification)grUser.elementAt(i);
      if( userNoti.m_status.equals(STATUS_DELETEDBYUSER) == false)
        v.addElement(userNoti);
    }

    return v;
  }

  /**
   * Get a Notification for given id and owner, used in Peephole screen
   * @param notiId The id of the notification to get.
   * @param recipient The recipient of notification to get. It can be a group.
   * @return Notification data
   * @throws Exception If there is database error.
   */
  public static Notification getNotification(int notiId, String recipient) throws Exception {
    Notification noti = null;
    String sql = "SELECT notification_id, recipient_id, app_type, sender, sent_date, noti_text, notified, params, status FROM upc_notification WHERE notification_id="+notiId+" AND RECIPIENT_ID="+SQL.esc(recipient);
    Channel.log("******get Notification********");
    Channel.log(sql);
    Channel.log("**************************");
    Connection conn = RDBMServices.getConnection();

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      stmt.executeQuery(sql);
      rs = stmt.getResultSet();
      if (rs.next())
        noti = newNotification(rs);
    } catch(SQLException e) {
      throw e;
    } finally {
      try {
        if (rs != null) rs.close();
		rs = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (stmt != null) stmt.close();
		stmt = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      RDBMServices.releaseConnection(conn);
    }

    return noti;
  }

  static Notification findUserNoti( Vector grUser, Notification grNoti) {
    for( int i = 0; i < grUser.size(); i++) {
      Notification noti = (Notification)grUser.elementAt(i);
      if( noti.m_notificationId == grNoti.m_notificationId &&
          noti.m_recipient.startsWith(grNoti.m_recipient))
        return noti;
    }
    return null;
  }

  static Notification newNotification( ResultSet rs) throws Exception {
    Notification noti = new Notification();
    noti.m_notificationId = rs.getInt(NOTIFICATION_ID);
    noti.m_recipient = rs.getString(RECIPIENT_ID);
    noti.m_type = rs.getString(APP_TYPE);
    noti.m_sender = new IdentityData(rs.getString(SENDER));
    noti.m_date = rs.getTimestamp(SENT_DATE);
    noti.m_text = rs.getString(NOTI_TEXT);
    noti.m_notified = rs.getString(NOTIFIED);
    noti.m_params = rs.getString(PARAMS);
    noti.m_status = rs.getString(STATUS);
    return noti;
  }

  static String getIDList( IdentityData owner) throws Exception {
    String ids = SQL.list( owner.getIdentifiers());

    // get all groups owner is belonged to if owner is portal user
    if( GroupData.isPortalUser(owner)) {
      GroupData[] ancestors = GroupData.getAncestors(owner, false);
      if( ancestors != null)
        for( int i = 0; i < ancestors.length; i++)
          ids += "," + SQL.esc(ancestors[i].getIdentifier());
    }

    // Unicon
    // need to find all Offerings that the user exists in
    try {
      String username = owner.getAlias();
      if( username != null) {
        User user = UserFactory.getUser(username);
        ArrayList allOfferings = (ArrayList) Memberships.getAllOfferings(user);
        for (int i=0; i < allOfferings.size(); i++) {
          Offering o = (Offering) allOfferings.get(i);
          ids += "," + SQL.esc("O@u@"+o.getId());
        }
      } else {
        Channel.log("Username(alias)==null for "+owner);
      }
    } catch (Exception e) {
      // do nothing, because user doesn't contain offering
      Channel.log(e);
    }
    // Unicon
    return ids;
  }

  //------------------------------------------------------------//
  //-----Noti Source actions------------------------------------//
  //------------------------------------------------------------//

  /**
  * Noti source deleted ( <notified>,<type>,<params>)
  * a. update status="DeletedBySource"
  *   where ( <notified>,<type>,<params>) and status!="Save" and status!="DeletedByUser"
  * b. update params=<deleteInfo>, notified=null where ( <notified>,<type>,<params>)
  *
   * When the source channel deletes the content of a notification, it should update the status of
   * the notification sent before, instead of deleting the notification ifself.
   * The value of field NOTIFIED will be null after deleting.
   * @param notified The source channel.
   * @param params The current parameters of notification
   * @param newApptype The new type of notification
   * @param newParams The new parameters of notification.
   * @throws Exception if there are the database errors.
   */
  public static void delete(String notified, String params, String newApptype,
                            String newParams) throws Exception {
    String sql;
    Connection conn = null;
    boolean oldAutoCommit = false;

    try {
      PreparedStatement ps = null;
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      // Update
      Timestamp ts = new Timestamp(Channel.getCurrentDate().getTime());
      sql = "UPDATE UPC_NOTIFICATION SET STATUS=?,SENT_DATE=? WHERE NOTIFIED=? AND PARAMS=? AND STATUS <> ? AND STATUS <> ?";
      //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);

      try {
      ps = conn.prepareStatement(sql);
      ps.setString(1,STATUS_DELETEDBYSOURCE);
      ps.setTimestamp(2,ts);
      ps.setString(3,notified);
      ps.setString(4,params);
      ps.setString(5,STATUS_SAVE);
      ps.setString(6,STATUS_DELETEDBYUSER);
      ps.executeUpdate();
      Channel.log("******delete by source a.********");
      Channel.log(sql);
      Channel.log("**************************");
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // Update RECIPIENT_ID is ENTITY
      sql = "UPDATE UPC_NOTIFICATION SET APP_TYPE=?,NOTIFIED=NULL,PARAMS=?,SENT_DATE=? WHERE NOTIFIED=? AND PARAMS=?";
      //ps = new RDBMServices.PreparedStatement(conn,sql);

      try {
      ps = conn.prepareStatement(sql);
      ps.setString(1,newApptype);
      ps.setString(2,newParams);
      ps.setTimestamp(3,ts);
      ps.setString(4,notified);
      ps.setString(5,params);
      ps.executeUpdate();
      Channel.log("******delete by source b********");
      Channel.log(sql);
      Channel.log("**************************");
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      if (conn != null) 
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
  }

  /**
   * Delete a set of notifications. It used by the source channel.
   * @param notified The source channel.
   * @param params The array of all parameters of notifications need to be deleted.
   * @param newApptype The new type of notification
   * @param newParams The new parameters of notification.
   * @throws Exception if there are the database errors.
   */
  public static void delete(String notified, String[] params, String newApptype,
                            String newParams) throws Exception {
    String list = "(";
    for( int i = 0; i < params.length; i++)
      list += (i==0?"":",") + SQL.esc(params[i]);
    list += ")";

    Connection conn = null;
    boolean oldAutoCommit = false;

    try {
      PreparedStatement ps = null;
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      // Update
      String sql;
      Timestamp ts = new Timestamp(Channel.getCurrentDate().getTime());
      sql = "UPDATE UPC_NOTIFICATION SET STATUS=?,SENT_DATE=? WHERE NOTIFIED=? AND PARAMS IN " + list +
            " AND STATUS <> ? AND STATUS <> ?";
      //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);

      try {
      ps = conn.prepareStatement(sql);
      ps.setString(1,STATUS_DELETEDBYSOURCE);
      ps.setTimestamp(2,ts);
      ps.setString(3,notified);
      ps.setString(4,STATUS_SAVE);
      ps.setString(5,STATUS_DELETEDBYUSER);
      ps.executeUpdate();
      Channel.log("******delete by source a.********");
      Channel.log(sql);
      Channel.log("**************************");
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // Update RECIPIENT_ID is ENTITY
      sql = "UPDATE UPC_NOTIFICATION SET APP_TYPE=?,NOTIFIED=NULL,PARAMS=?,SENT_DATE=? WHERE NOTIFIED=? AND PARAMS IN " + list;
      //ps = new RDBMServices.PreparedStatement(conn,sql);

      try {
      ps = conn.prepareStatement(sql);
      ps.setString(1,newApptype);
      ps.setString(2,newParams);
      ps.setTimestamp(3,ts);
      ps.setString(4,notified);
      ps.executeUpdate();
      Channel.log("******delete by source b********");
      Channel.log(sql);
      Channel.log("**************************");
      } finally {
        try {
          if (ps != null) ps.close();
          ps = null;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      if (conn != null)
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
  }

  /**
  * Noti source updated ( <notified>,<type>,<params>) with the <newParams>
  *  update params=<newParams> where ( <notified>,<type>,<params>)
  *
   * Update Notification with the new paramters.
   * @param notified The source channel.
   * @param params Tthe old parameters.
   * @param newApptype The new type of notification.
   * @param newNotiText The new text of notification.
   * @param newParams The new parameters of notification.
   * @throws Exception if there is a database error.
   */
  public static void update(String notified, String params,
                            String newApptype, String newNotiText, String newParams) throws Exception {
    String sql;
    Connection conn = null;
    boolean oldAutoCommit = false;
    PreparedStatement ps = null;

    try {
      conn = RDBMServices.getConnection();
      oldAutoCommit = conn.getAutoCommit();
      RDBMServices.setAutoCommit(conn,false);

      sql = "UPDATE UPC_NOTIFICATION SET APP_TYPE=?,NOTI_TEXT=?,PARAMS=?,SENT_DATE=? WHERE NOTIFIED=? AND PARAMS=?";
      //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn,sql);
      ps = conn.prepareStatement(sql);
      ps.setString(1,newApptype);
      Channel.log("[Notification]newApptype=" + newApptype);

      ps.setString(2,newNotiText);
      Channel.log("[Notification]newApptype=" + newNotiText);
      ps.setString(3,newParams);
      Channel.log("[Notification]newApptype=" + newParams);
      ps.setTimestamp(4,new Timestamp(Channel.getCurrentDate().getTime()));
      Channel.log("[Notification]time=" + new Timestamp(Channel.getCurrentDate().getTime()));
      ps.setString(5,notified);
      Channel.log("[Notification]notified=" + notified);
      ps.setString(6,params);
      Channel.log("[Notification]params=" + params);
      ps.executeUpdate();
      Channel.log("******Update by source ********");
      Channel.log(sql);
      Channel.log("**************************");

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      RDBMServices.rollback(conn);
      throw e;
    } finally {
      try {
        if (ps != null) ps.close();
        ps = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (conn != null) 
        RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
  }

  //------------------------------------------------------------------------//
  //---Mail-----------------------------------------------------------------//
  //------------------------------------------------------------------------//

  void sendMail( InternetAddress[] ias) throws Exception {
    if (!m_mailServerWorking)
      throw new Exception(MSG_MAIL_SERVER_FAILED);
    // Build a MIME Message to send to the mail server.
    Properties props = System.getProperties();
    Authenticator autor = getAuthenticator();
    props.put("mail.smtp.host",getMailHost());
    if (autor != null)
      props.put("mail.smtp.auth", "true");
    Session session = Session.getInstance(props,autor); //Nhan 0208

    MimeMessage mm = buildMessage(session, getAddress(m_sender.getEmail(),m_sender.getName()),
                                  m_date, m_subject, m_body!=null?m_body:"", ias);
    Channel.log("[Notification]send. Sending...message to " + getMailHost());
    //Transport.send(mm,ias);
    if (ias != null && ias.length != 0) {
      for (int i = 0; i < ias.length; i++) {
        try {
          Transport.send(mm, new InternetAddress[]{ias[i]});
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Get Name of Mail Server from property file of RAD
   * @return the mail host name or IP
   * @throws Exception if mail host not specified
   */
  static String getMailHost() throws Exception {
    String mailHost = Channel.getRADProperty("notification.smtp.host");
    if (mailHost == null)
      throw new Exception("[Notification] mailhost not found");
    return mailHost;
  }

  /**
   * Get user name from property file of RAD. This user name is used for
   * @return user name used when authenticate to mail server
   */
  static String getUserName() {
    return Channel.getRADProperty("notification.smtp.username");
  }

  /**
   * Get Name of Mail Server from property file of RAD
   * @return the password of user being authenticated when log into mail server
   */
  static String getPassword() {
    return Channel.getRADProperty("notification.smtp.password");
  }

  /**
   * Get Number of days of expire from property file of RAD
   */

  static int m_ndays = -1;
  static int getNumOfDayOfExpire() {
    if (m_ndays > -1)
      return m_ndays;

    // Default notification.expire
    m_ndays = DEFAULT_EXPIRE;
    String expire = Channel.getRADProperty("notification.expire");
    if( expire != null)
      try {
        m_ndays = Integer.parseInt(expire);
      } catch (NumberFormatException nfe) {
        m_ndays = DEFAULT_EXPIRE;
      }
    return m_ndays;
  }

  /**
   * Build a Mime Message from input parameters:
   *  sess,  sender, data, subject, body, recipients
   * @param sess
   * @param sender
   * @param date
   * @param subject
   * @param body
   * @param recipients
   */
  static MimeMessage buildMessage(Session sess, Address sender, java.util.Date date,
                                  String subject, String body, Address[] recipients) throws Exception {
    MimeMessage mm = new MimeMessage(sess);
    mm.setSentDate(date);
    mm.setFrom(sender);
    mm.setSubject(subject);
    mm.setRecipients(MimeMessage.RecipientType.TO,recipients);
    mm.setText(body);
    return mm;
  }
  /**
   * Return InternetAddres object from email and user name
   */
  static InternetAddress getAddress(String email, String name) throws Exception {
    if (name == null || name.length() ==0)
      return new InternetAddress(email);
    else
      return new InternetAddress(email,name);
  }

  /**
   * Create a new instance of MailAuthenticator object.
   * if getUserName() and getPassword() have values
   *  return a instace of MailAutheticator with the "username/password".
   * Otherwise, return null
   */
  MailAuthenticator getAuthenticator() {
    String username = getUserName();
    String password = getPassword();
    if (username != null && username.length() > 0 && password != null && password.length() > 0)
      return new MailAuthenticator(username,password);
    else
      return null;
  }

  class MailAuthenticator extends Authenticator {
    private String m_username, m_password;

    public MailAuthenticator(String username, String password) {
      m_username = username;
      m_password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(m_username,m_password);
    }
  }

  //-----------------------------------------------------------//
  /**
   * Convert data from old database to new format ( @ -> |)
   * @param args[0] rdbm.properties file
   * @param args[1] from version
   * @param args[2] to version
   */

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: Notification [rdbm.properties file] [fromVersion] [toVersion]");
      return;
    }

    DBService db = null;
    try {
      // Get parameters
      Properties props = new Properties();
      props.load(new FileInputStream(args[0]));
      String from = args[1];
      String to = args[2];

      // Convert
      db = new Rdbm( props);
      db.begin();
      String sql = "SELECT NOTIFICATION_ID, RECIPIENT_ID, SENDER FROM UPC_NOTIFICATION";
      ResultSet rs = db.select( sql);
      int count = 0;
      while( rs.next()) {
        int id = rs.getInt(1);
        String recipient = rs.getString(2);
        String sender = rs.getString(3);
        String newRecipient = recipient.replace('@','|');
        String newSender = (new IdentityData(sender)).toString();
        if( !newRecipient.equals(recipient) || !newSender.equals(sender)) {
          sql = "UPDATE UPC_NOTIFICATION SET RECIPIENT_ID="+SQL.esc(newRecipient)+",SENDER="+SQL.esc(newSender)+
                " WHERE NOTIFICATION_ID="+id+" AND RECIPIENT_ID="+SQL.esc(recipient);
          db.update(sql);
          count++;
        }
      }

      db.commit();
      System.out.println("Convert "+count+" records successfully.");
    } catch (Exception e) {
      e.printStackTrace();
      if( db != null)
        try {
          db.rollback(e.getMessage());
        } catch( Exception ex) {}
      System.exit(-1);
    }
  }
}
