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
package net.unicon.academus.apps.announcement.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.text.DateFormat;

import java.util.List;
import java.util.ArrayList;


import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.time.TimeServiceFactory;
import net.unicon.sdk.time.TimeService;
import net.unicon.academus.apps.announcement.Announcement;
import net.unicon.academus.apps.announcement.AnnouncementService;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.properties.PortalPropertiesType;


import org.jasig.portal.services.LogService;

public class AnnouncementServiceImpl implements AnnouncementService {

    // Announcement SQL
    private static final String getAnnouncementsSQL =
	(new StringBuffer().append("select Announcement.announcement_id, group_id, ")
	 .append("body, submit_date, owner_id from Announcement, Announcement_id_groups where ")
	 .append("group_id = ? AND Announcement.announcement_id=Announcement_id_groups.announcement_id"))
	.toString();

    private static final String getAnnouncementSQL =
	(new StringBuffer().append("select Announcement.announcement_id, group_id, body, ")
	 .append("submit_date, Announcement_id_groups.owner_id from ")
	 .append(" Announcement, Announcement_id_groups where ")
	 .append("Announcement.announcement_id = ? and group_id = ?")
	 .append(" AND Announcement.announcement_id=Announcement_id_groups.announcement_id")).toString();

    private static final String updateAnnouncementSQL =
	(new StringBuffer()
	 .append("update announcement set body = ?, submit_date = ?")
	 .append(" where announcement_id = ?")).toString();

    private static final String updateAnnouncementIdGroupsSQL =
	(new StringBuffer()
	 .append("update announcement_id_groups set owner_id = ?")
	 .append(" where announcement_id = ? and group_id = ?")).toString();

    private static final String addAnnouncementIdGroupsSQL =
	(new StringBuffer()
	 .append("insert into announcement_id_groups (announcement_id, group_id, group_entity_type, owner_id)")
	 .append(" values (?,?,?,?)")).toString();

    private static final String deleteAnnouncementSQL =
	"delete from announcement where announcement_id= ?";

    private static final String deleteAnnouncementIdGroupsSQL =
	"delete from announcement_id_groups where announcement_id= ?";

    private static final String deleteAllAnnouncementSQL =
	"delete from announcement where announcement_id in " +
        "(select announcement_id from announcement_id_groups where group_id = ?)";

    private static final String deleteAllAnnouncementIdGroupsSQL =
	"delete from announcement_id_groups where group_id = ?";

    /* BEGIN IMPLEMENTATION */

    private static String __groupsPrefix = "";  // default to 2.0.3

    public AnnouncementServiceImpl() {
	// read in the portal properties to get the groups prefix
	__groupsPrefix = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.groups.GroupImpl.localGroupService");
    }

    public List getAnnouncements(String contextID,
				 Connection conn) {
        return doGetAnnouncements(contextID, conn);
    }

    private List doGetAnnouncements(String contextID,
				    Connection conn) {
        List announcements = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(getAnnouncementsSQL);
            int i = 0;
            pstmt.setString(++i, __constructGroupId(contextID));
            rs = pstmt.executeQuery();
            /* announcments */
            int announcementID = -1;
            String announcementBody = null;
	    String groupId = null;
            Date submitDate   = null;
	    String instructorID = null;

            AnnouncementImpl announcement = null;
            List tempList = null;
            boolean firstPass = true;
            while (rs.next()) {
                /* Getting announcement information from the rs */
                announcementID   = rs.getInt("announcement_id");
                groupId          = rs.getString("group_id");
                announcementBody = rs.getString("body");
                submitDate       = rs.getDate("submit_date");
		instructorID     = rs.getString("owner_id");

                /* add announcement to list */
                announcement = new AnnouncementImpl(announcementID,
						    groupId,
						    announcementBody,
						    DateFormat.getDateInstance().format(submitDate),
						    instructorID);
                announcements.add(announcement);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return announcements;
    }

    public List getAnnouncement(String contextID, int announcementID, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        /* announcements */
        String announcementBody = null;
        Date submitDate   = null;
        List announcements = new ArrayList();
	String instructorID = null;
	String groupId = null;
        try {
            pstmt = conn.prepareStatement(getAnnouncementSQL);
            int i = 0;
            pstmt.setInt(++i, announcementID);
            pstmt.setString(++i, __constructGroupId(contextID));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                /* Getting announcement information from the rs */
                announcementID   = rs.getInt   ("announcement_id");
                groupId          = rs.getString("group_id");
                announcementBody = rs.getString("body");
                submitDate       = rs.getDate  ("submit_date");
		instructorID     = rs.getString("owner_id");
                /* add announcement to list */
                announcements.add(new AnnouncementImpl(announcementID,
						       groupId,
						       announcementBody,
						       DateFormat.getDateInstance().format(submitDate),
						       instructorID));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally { 
            try { 
                if (rs != null) {
                    rs.close();
                    rs = null;
                }           
            } catch (Exception e) {
                e.printStackTrace();
            }           
            try {   
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }           
            } catch (Exception e) {
                e.printStackTrace();
            }           
        }
        return announcements;
    }

    public boolean updateAnnouncement(String contextID,
				      int announcementID,
				      String message,
				      String instructorID,
				      Connection conn) {
        boolean success = false;
	boolean oldCommitState = true;
        PreparedStatement pstmt  = null;
        PreparedStatement pstmt2 = null;

        try {
            if ( (oldCommitState = conn.getAutoCommit()) ) {
              conn.setAutoCommit(false);
	    }

            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(updateAnnouncementSQL);
            int i = 0;
            pstmt.setString(++i, message);
            pstmt.setTimestamp(++i, ts.getTimestamp());
            pstmt.setInt(++i, announcementID);
            
	    // set up update of announcement_id_groups
            pstmt2 = conn.prepareStatement(updateAnnouncementIdGroupsSQL);
	    i = 0;
	    pstmt2.setString(++i, instructorID);
            pstmt2.setInt(++i, announcementID);

	    // set the contextID structure based on uPortal version. This is
	    // a hokey check to see fi the ID we're getting is already
            pstmt2.setString(++i, __constructGroupId(contextID));
	    
	    pstmt.executeUpdate();
	    pstmt2.executeUpdate();

            conn.commit();
	    conn.setAutoCommit(true);

            success = true;
        } 
	catch (SQLException se) {
            se.printStackTrace();
        } 
	catch (FactoryCreateException fce) {
            fce.printStackTrace();
	 } 
	finally {
	    try {
		if (pstmt != null) {
		    pstmt.close();
		}
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
		if (pstmt2 != null) {
		    pstmt2.close();
		}
	    }
	    catch (SQLException sql2) {
		sql2.printStackTrace();
	    }
	}

        return success;
    }

    public boolean addAnnouncement(String contextID,
				   String message,
				   String instructorId,
				   Connection conn) {
        boolean success = false;
	boolean oldCommitState = true;
        // Statement stmt  = null;
        Statement stmt2  = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt0 = null;
	ResultSet rs = null;
	int announcementID = -1;
	// boolean supportsGeneratedKeys = false;

        try {
            if ( (oldCommitState = conn.getAutoCommit()) ) {
              conn.setAutoCommit(false);
	    }

	    /*
	      All this stuff was an attempt to use a 2.0 feature that supports resultSets from
	      DML as a way to get auto-generated keys. The Timestamp stuff made us comment it out.

	      try {
	        supportsGeneratedKeys = conn.getMetaData().supportsGetGeneratedKeys();
	      }
	    catch (Throwable t) {
		LogService.instance().log(LogService.INFO, "Exception checking for supportsGetGeneratedKeys(), leaving false");
	    } 
	    */

            TimeService ts = TimeServiceFactory.getService();

	    String addAnnouncementSQL ="insert into announcement (body,submit_date) values (?, ?)";

	    pstmt0 = conn.prepareStatement(addAnnouncementSQL);
	    pstmt0.setString(1, message);
	    pstmt0.setTimestamp(2, ts.getTimestamp());
	    pstmt0.executeUpdate();

	    stmt2 = conn.createStatement();
	    rs = stmt2.executeQuery("select max(announcement_id) from announcement");

	    if (rs.next()) {
		announcementID = rs.getInt(1);
	    }


	    /*
	      All this stuff was an attempt to use a 2.0 feature that supports resultSets from
	      DML as a way to get auto-generated keys. The Timestamp stuff made us comment it out.

		(new StringBuffer()
		 .append("insert into announcement (body,submit_date)")
		 .append(" values ('").append(message).append("','")
		 .append("" + ts.getTimestamp()).append("')")).toString();


	    if (supportsGeneratedKeys) {
		stmt.executeUpdate(addAnnouncementSQL, Statement.RETURN_GENERATED_KEYS);
		// The above insert generates a 1-field RS with the sequence value
		rs = stmt.getGeneratedKeys();
	    }
	    else {
		stmt.executeUpdate(addAnnouncementSQL);
		stmt2 = conn.createStatement();
		rs = stmt.executeQuery("select max(announcement_id) from announcement");
	    }
	    */

	    // set up the insert into announcement_id_groups
            pstmt = conn.prepareStatement(addAnnouncementIdGroupsSQL);
            int i = 0;
            pstmt.setInt(++i, announcementID);
            pstmt.setString(++i, __constructGroupId(contextID));
	    pstmt.setString(++i, "3"); // 3 == group entity type
	    pstmt.setString(++i, instructorId);

            pstmt.executeUpdate();

            conn.commit();
	    conn.setAutoCommit(true);

            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } 
	finally {
	    try {
		    if (rs != null) {
		        rs.close();
            }
		} catch (Exception e) {
            e.printStackTrace();
        }

        try {
		if (pstmt != null) {
		    pstmt.close();
		}
		} catch (Exception e) {
            e.printStackTrace();
        }

        try {
		if (pstmt0 != null) {
		    pstmt0.close();
		}
		} catch (Exception e) {
            e.printStackTrace();
        }

        try {
		if (stmt2 != null) {
		    stmt2.close();
		}
	    }
	    catch (SQLException sql2) {
		sql2.printStackTrace();
	    }
	}

        return success;
    }

    public int deleteAnnouncement(int announcementID,
				  Connection conn) {
        int rval = 0;
        PreparedStatement pstmt  = null;
        PreparedStatement pstmt2 = null;
	boolean oldCommitState   = true;

        try {
            if ( (oldCommitState = conn.getAutoCommit()) ) {
              conn.setAutoCommit(false);
	    }

            pstmt  = conn.prepareStatement(deleteAnnouncementSQL);
            pstmt2 = conn.prepareStatement(deleteAnnouncementIdGroupsSQL);

            pstmt.setInt(1, announcementID);
            pstmt2.setInt(1, announcementID);

	    // do 2 before 1, ow we might violate Referential IC
	    pstmt2.executeUpdate();
            rval = pstmt.executeUpdate();

            conn.commit();
	    conn.setAutoCommit(oldCommitState);

        } catch (SQLException se) {
            se.printStackTrace();
            rval = -1;
        }
	finally {
	    try {
		if (pstmt != null) {
		    pstmt.close();
		}
        } catch (Exception e) {
        e.printStackTrace();
        }

        try {
		if (pstmt2 != null) {
		    pstmt2.close();
		}
	    }
	    catch (SQLException sql2) {
		sql2.printStackTrace();
	    }
	}

        return rval;
    }

    public int deleteAnnouncements(String contextID,
				   Connection conn) {
        int rval = 0;
        PreparedStatement pstmt  = null;
        PreparedStatement pstmt2 = null;
	boolean oldCommitState   = true;

        try {
            if ( (oldCommitState = conn.getAutoCommit()) ) {
              conn.setAutoCommit(false);
	    }

            pstmt  = conn.prepareStatement(deleteAllAnnouncementSQL);
            pstmt2 = conn.prepareStatement(deleteAllAnnouncementIdGroupsSQL);

	    // set the group id based on uPortal version
	    contextID = __constructGroupId(contextID);
            pstmt.setString(1, contextID);
            pstmt2.setString(1, contextID);

	    // you have to do pstmt before pstmt2! Look at the queries...
            rval = pstmt.executeUpdate();
	    pstmt2.executeUpdate();

            conn.commit();
	    conn.setAutoCommit(oldCommitState);
        } catch (SQLException se) {
            se.printStackTrace();
            rval = -1;
        }
	finally {
	    try {
		if (pstmt != null) {
		    pstmt.close();
		    pstmt = null;
		}
        } catch (Exception e) {
        e.printStackTrace();
        }

        try {
		if (pstmt2 != null) {
		    pstmt2.close();
		    pstmt2 = null;
		}
	    }
	    catch (SQLException sql2) {
		sql2.printStackTrace();
	    }
	}

        return rval;
    }

    private static String __constructGroupId(String groupId) {
	// hokey check to make sure we're not prepending the 2.1.x groups prefix
	// more than once.
	if (__groupsPrefix.length() > 0 && !groupId.startsWith(__groupsPrefix)) {
	    return __groupsPrefix + groupId;
	}
	return groupId;
    }


}
