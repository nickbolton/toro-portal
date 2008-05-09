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

package net.unicon.portal.channels.gradebook;

import java.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import net.unicon.academus.domain.lms.*;
import net.unicon.portal.channels.gradebook.base.*;

import org.jasig.portal.services.LogService;

public final class GradebookScoreFactory {
    public GradebookScoreFactory () {}
    
    private static final String insertGradebookScoresSQL =
        "insert into gradebook_score (gradebook_item_id, score, original_score, user_name) SELECT gi.gradebook_item_id, -1, -1, M.user_name from gradebook_item gi, membership M where gi.offering_id = M.offering_id and gradebook_item_id = ? AND M.enrollment_status = ?";

    public static void createAllUsers(
                    int gradebookItemId,
                    Connection conn) {
        __doInsertNewGBItemScoresForUsers(gradebookItemId, conn);
    }

    private static void __doInsertNewGBItemScoresForUsers(
                    int gradebookItemId,
                    Connection conn) {

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(insertGradebookScoresSQL);
            
            int i = 0;
            
            pstmt.setInt(++i, gradebookItemId);
            pstmt.setInt(++i, ENROLLED);
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
    }

    public static boolean remove (GradebookScore score, Connection conn) {
        return __removeFromDB(score.getID(), conn);
    }

    public static boolean remove (int gbScoreId, Connection conn) {
        return __removeFromDB(gbScoreId, conn);
    }

    private static final String deleteLearnersGBEntriesSQL =
    "DELETE from gradebook_score where gradebook_score_id = ?";
    
    private static boolean __removeFromDB(int gbScoreID, Connection conn) {
        boolean success = false;
        PreparedStatement pstmt = null;
        try {
            // Removing dependencies
            GradebookSubmissionFactory.remove(gbScoreID, conn);
            GradebookFeedbackFactory.remove(gbScoreID, conn);
            
            pstmt = conn.prepareStatement(deleteLearnersGBEntriesSQL);
            pstmt.setInt(1, gbScoreID);
            pstmt.executeUpdate();

            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return success;        
    }
    
    private static final String selectGradebookScoreIDSQL =
        "SELECT gradebook_score_id from gradebook_score where user_name = ? and gradebook_item_id = ?";

    public static int getGradebookScoreID (int gradebookItemID, String username, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        int gbScoreID = -1;

        try {
            pstmt = conn.prepareStatement(selectGradebookScoreIDSQL);

            int i = 0;
            pstmt.setString(++i, username);
            pstmt.setInt(++i, gradebookItemID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                gbScoreID = rs.getInt("gradebook_score_id");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }

            try {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return gbScoreID;
    }

    public static boolean insertUser(int offeringId, String username, Connection conn) {
        return __doInsertAllGBItemScoresForUser(offeringId, username, conn);
    }

    public static boolean update(
            int gbItemID, 
            String username, 
            int score,
            int origScore,
            int offeringId, 
            Connection conn) {
            
        return __updateToDB(gbItemID, username, score, origScore, offeringId, conn);
    }

    public static boolean update(
            int gbItemID, 
            String username, 
            int score,
            int offeringId, 
            Connection conn) {
            
        return __updateToDB(gbItemID, username, score, -1, offeringId, conn);
    }

    private static final String updateUserScoreSQL =
        "UPDATE gradebook_score set score = ? where user_name = ? and gradebook_item_id = ?";
        
    private static final String updateUserScoreSetSQL =
        "UPDATE gradebook_score set original_score = ?, score = ? where user_name = ? and gradebook_item_id = ?";
    
    private static boolean __updateToDB(
            int gbItemID,
            String userName,
            int score,
            int origScore,
            int offeringId,
            Connection conn) {
         boolean success = false;
         PreparedStatement pstmt = null;
         try {
             int i = 0;
             
             if (origScore == -1) {
                 // Do NOT modify existing original Score.
                 pstmt = conn.prepareStatement(updateUserScoreSQL);
             } else {
                 // Do Modify existin orginal Score.
                 pstmt = conn.prepareStatement(updateUserScoreSetSQL);
                 pstmt.setInt(++i, origScore);
             }
             
             pstmt.setInt(++i, score);
             pstmt.setString(++i, userName);
             pstmt.setInt(++i, gbItemID);
             pstmt.executeUpdate();
 
             LogService.instance().log(LogService.INFO,
             "Updated username,score" + userName + "," + score);
             success = true;
         } catch (SQLException se) {
             se.printStackTrace();
         } finally {
            try {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
         }
         return success;
    }
    
    private static final String selectGradebookScoresSQL =
        "SELECT gradebook_item_id from gradebook_score where user_name = ? and gradebook_item_id in (select gradebook_item_id from gradebook_item where offering_id = ?)";

    private static final String selectAllGradebookItemSQL =
        "SELECT gradebook_item_id from gradebook_item where offering_id = ?";

    private static final String insertAllGradebookScoresSQL =
        "insert into gradebook_score (gradebook_item_id, score, original_score, user_name) SELECT gi.gradebook_item_id, -1, -1, M.user_name from gradebook_item gi, membership M where gi.offering_id = M.offering_id and gradebook_item_id = ? and M.user_name = ? AND M.enrollment_status = ?";
        
    private static boolean __doInsertAllGBItemScoresForUser(
                        int offeringId,
                        String username,
                        Connection conn) {

        boolean success = false;

        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        LogService.instance().log(LogService.INFO,

        "GradebookScoreFactory.doInsertNewItemScoresForUsers:OFFERING= " + offeringId);

        int i = 0;

        Set currentEntries = new HashSet();

        try {
            pstmt = conn.prepareStatement(selectGradebookScoresSQL);

            i = 0;
            pstmt.setString(++i, username);
            pstmt.setInt(++i, offeringId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                currentEntries.add("" + rs.getInt("gradebook_item_id"));
            }

            rs.close();

            pstmt.close();

            rs = null;

            pstmt = conn.prepareStatement(selectAllGradebookItemSQL);

            i = 0;
            pstmt.setInt(++i, offeringId);

            rs = pstmt.executeQuery();

            pstmt2 = conn.prepareStatement(insertAllGradebookScoresSQL);

            while (rs.next()) {
                int gbItemID = rs.getInt("gradebook_item_id");
                if (currentEntries.contains("" + gbItemID)) continue;
                LogService.instance().log(LogService.INFO,
                "doInsertNewItemScoresForUsers:RS.GRADEBOOK_ITEM_ID= " + gbItemID);
                
                i = 0;
                pstmt2.setInt(++i, gbItemID);
                pstmt2.setString(++i, username);
                pstmt2.setInt(++i, ENROLLED);
                pstmt2.executeUpdate();
            }
            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (pstmt2 != null) pstmt2.close();
                pstmt2 = null;     
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return success;
    }    
    private static final int ENROLLED = EnrollmentStatus.ENROLLED.toInt();
}
