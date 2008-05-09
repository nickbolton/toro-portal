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

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.unicon.sdk.time.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.*;
import net.unicon.academus.domain.lms.*;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.file.FileServiceFactory;

import net.unicon.portal.channels.gradebook.base.*;

import org.jasig.portal.services.LogService;


public final class GradebookFeedbackFactory {
    public GradebookFeedbackFactory() {}
    static File feedbackDir = null;

    static {
        String feedbackFileS = UniconPropertiesFactory.getManager(
            PortalPropertiesType.LMS).getProperty(
                "net.unicon.portal.gradebook.feedbackFileDir");
        feedbackDir = new File(feedbackFileS);
    }    

    public static void create(
        String username,
        int gbItemID,
        String feedbackFileName,
        String feedbackComment,
        String feedbackContentType,
        InputStream feedbackStream,
        Connection conn) {
        __doCreateToDB(
            username,
            gbItemID,
            feedbackFileName,
            feedbackComment,
            feedbackContentType,
            feedbackStream,
            conn);
        // XXX This should return a Feedback Object in the future - H2
    }

    private static void __doCreateToDB(
                String username,
                int gbItemID,
                String feedbackFileName,
                String feedbackComment,
                String feedbackContentType,
                InputStream feedbackStream,
                Connection conn) {   
        
        int gbScoreID = GradebookScoreFactory.getGradebookScoreID(
        gbItemID,
        username,
        conn);

        if (gbScoreID == -1) {
            throw new IllegalArgumentException("Cannout Find gradebook_score_id for username, "
            + username + " gradebookID " + gbItemID);
        }

        try {

            FileService us = FileServiceFactory.getService();

            /* Gradebook Feedback Stream */
            int fbFileSize = 0;

            if (feedbackStream != null && feedbackFileName != null) {
                File dir = new File(feedbackDir, "" + gbScoreID);
                File feedbackFile = us.uploadFile(dir,
                feedbackFileName, feedbackStream);
                
                save(
                    gbScoreID,
                    (int) feedbackFile.length(),
                    feedbackFileName,
                    feedbackComment,
                    feedbackContentType,
                    conn);
            } else {
                saveComment(
                    gbScoreID,
                    feedbackComment,
                    conn);
            }
        } catch (IOException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        }
    }
    
    public static void save(
          int gbScoreID,
          int fileSize,
          String filename,
          String gcomment,
          String contentType,
          Connection conn) {
        __doSaveToDB(gbScoreID, fileSize, filename, gcomment, contentType, conn);   
    }

    private static final String insertGradebookFeedbackSQL = (new StringBuffer()
        .append("INSERT into gradebook_feedback (gradebook_score_id, ")
        .append("filename, filesize, gcomment, content_type, submit_date) ")
        .append("values (?,?,?,?,?,?)")).toString();
    
    private static void __doSaveToDB (
          int gbScoreID,
          int fileSize,
          String filename,
          String gcomment,
          String contentType,
          Connection conn) {
        PreparedStatement pstmt = null;

        try {

            TimeService ts = TimeServiceFactory.getService();

            // Deleting previoud Feedback */

            pstmt = conn.prepareStatement(deleteGradebookFeedbackSQL);
            int i = 0;

            pstmt.setInt(++i, gbScoreID);
            pstmt.executeUpdate();

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

            // Inserting new Feedback

            pstmt = conn.prepareStatement(insertGradebookFeedbackSQL);

            i = 0 ;

            pstmt.setInt   (++i, gbScoreID);
            pstmt.setString(++i, filename);
            pstmt.setInt   (++i, fileSize);
            pstmt.setString(++i, gcomment);
            pstmt.setString(++i, contentType);
            pstmt.setTimestamp(++i, ts.getTimestamp());

            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
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
    }

    public static void saveComment(int gbScoreID, String comment, Connection conn) {
        __doSaveCommentToDB(gbScoreID, comment, conn);    
    }
    
    private static final String insertGradebookFeedbackCommentSQL =
    "INSERT into gradebook_feedback (gradebook_score_id, gcomment) values (?,?)";

    private static final String updateGradebookFeedbackCommentSQL =
    "UPDATE gradebook_feedback set gcomment = ? where gradebook_feedback_id = ?";

    private static void __doSaveCommentToDB(
                            int gbScoreID, 
                            String gcomment,
                            Connection conn) {
                            
        IGradebookFileInfo gbFeedback = getGradebookFeedback(gbScoreID, conn);
        PreparedStatement pstmt = null;

        try {
            int i = 0;

            if (gbFeedback != null) {
                pstmt = conn.prepareStatement(updateGradebookFeedbackCommentSQL);
                pstmt.setString(++i, gcomment);
                pstmt.setInt(++i, gbFeedback.getID());
            } else {
                pstmt = conn.prepareStatement(insertGradebookFeedbackCommentSQL);
                pstmt.setInt(++i,    gbScoreID);
                pstmt.setString(++i, gcomment);
            }

            pstmt.executeUpdate();
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
    }
    
    public static boolean remove (GradebookFeedback feedback, Connection conn) {
        return __removeFromDB(feedback.getGradebookScoreID(), conn);
    }

    private static final String deleteGradebookFeedbackSQL =
        "DELETE from gradebook_feedback where gradebook_score_id = ?";
        
    public static boolean remove (int gbScoreId, Connection conn) {
        return __removeFromDB(gbScoreId, conn);
    }
    
    private static boolean __removeFromDB(int gbScoreId, Connection conn) {
        boolean success = false;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(deleteGradebookFeedbackSQL);
            pstmt.setInt(1, gbScoreId);
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

    private static final String getFeedbackForScoreSQL =
        "SELECT gradebook_feedback_id, gradebook_score_id, filename, filesize, gcomment, submit_date, content_type  FROM gradebook_feedback WHERE gradebook_score_id = ?";

    public static GradebookFeedback getGradebookFeedback(    
                    int gradebookScoreID, 
                    Connection conn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        GradebookFeedback gbFeedback = null;

        try {

            int i = 0;

            pstmt = conn.prepareStatement(getFeedbackForScoreSQL);
            pstmt.setInt(++i, gradebookScoreID);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                gbFeedback = (GradebookFeedback)
                new GradebookFeedbackImpl(
                rs.getInt("gradebook_feedback_id"),
                gradebookScoreID,
                rs.getString("filename"),
                rs.getInt("filesize"),
                rs.getString("gcomment"),
                rs.getTimestamp("submit_date"),
                rs.getString("content_type"));
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
       return gbFeedback;
    }

    private static final String getAllFeedbacksForOfferingSQL =
    "SELECT gf.gradebook_feedback_id, gf.gradebook_score_id, gf.filename, gf.filesize, gf.gcomment, gf.submit_date, gf.content_type FROM gradebook_feedback gf, gradebook_score sc, gradebook_item gi WHERE gf.gradebook_score_id = sc.gradebook_score_id and sc.gradebook_item_id = gi.gradebook_item_id and gi.offering_id = ?";

    public static List getAllGradebookFeedbacks(Offering offering, Connection conn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        GradebookFeedback gbFeedback = null;

        if (offering == null) return null;
        
        List feedback = new ArrayList();

        try {

            int i = 0;

            pstmt = conn.prepareStatement(getAllFeedbacksForOfferingSQL);
            pstmt.setLong(++i, offering.getId());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                gbFeedback = (GradebookFeedback)
                new GradebookFeedbackImpl(
                rs.getInt("gradebook_feedback_id"),
                rs.getInt("gradebook_score_id"),
                rs.getString("filename"),
                rs.getInt("filesize"),
                rs.getString("gcomment"),
                rs.getTimestamp("submit_date"),
                rs.getString("content_type"));
                feedback.add(gbFeedback);
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
        return feedback;
    }

}
