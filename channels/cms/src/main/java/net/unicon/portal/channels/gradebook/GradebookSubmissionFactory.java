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

import java.util.List;
import java.util.ArrayList;
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

public final class GradebookSubmissionFactory {
    public GradebookSubmissionFactory(){}


    static File submissionDir = null;
    
    static {
        String submissionFileS = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.gradebook.submissionFileDir");
        submissionDir = new File(submissionFileS);
    }
    
    public static void create(
        String username,
        int gbItemID,
        String submissionFileName,
        String submissionComment,
        String submissionContentType,
        InputStream submissionStream,
        Connection conn) {
        __doCreateToDB(
            username,
            gbItemID,
            submissionFileName,
            submissionComment,
            submissionContentType,
            submissionStream,
            conn);
        // XXX This should return a submission Object in the future - H2
    }

    private static void __doCreateToDB(
                String username,
                int gbItemID,
                String submissionFileName,
                String submissionComment,
                String submissionContentType,
                InputStream submissionStream,
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

            /* Gradebook submission Stream */
            int fbFileSize = 0;

            if (submissionStream != null && submissionFileName != null) {
                File dir = new File(submissionDir, "" + gbScoreID);
                File submissionFile = us.uploadFile(dir,
                submissionFileName, submissionStream);
                
                save(
                    gbScoreID,
                    (int) submissionFile.length(),
                    submissionFileName,
                    submissionComment,
                    submissionContentType,
                    null,
                    conn);
            } else {
                saveComment(
                    gbScoreID,
                    submissionComment,
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
              String data,
              Connection conn) {
        __doSaveToDB(gbScoreID, fileSize, filename, gcomment, contentType, data, conn);   
    }

    public static void saveComment(int gbScoreID, String comment, Connection conn) {
        __doSaveCommentToDB(gbScoreID, comment, conn);    
    }

    private static final String insertGradebookSubmissionSQL =
    (new StringBuffer()
    .append("INSERT into gradebook_submission (gradebook_score_id, ")
    .append("filename, filesize, gcomment, content_type, ")
    .append("submission_count, submit_date, data) values (?,?,?,?,?,1,")
    .append("?,?)")).toString();
    
    private static final String updateGradebookSubmissionSQL =
    (new StringBuffer()
    .append("UPDATE gradebook_submission set gradebook_score_id = ?,")
    .append(" filename = ?, filesize = ?, gcomment = ?, ")
    .append(" content_type = ?, submission_count = ")
    .append(" (submission_count + 1), submit_date = ?, ")
    .append(" data = ? where gradebook_submission_id = ?")).toString();
    
    private static void __doSaveToDB(
            int gbScoreID,
            int fileSize,
            String filename,
            String gcomment,
            String contentType,
            String data,
            Connection conn) {
        IGradebookFileInfo gbSubmission = getGradebookSubmission(gbScoreID, conn);
        PreparedStatement pstmt = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            int i = 0;

            if (gbSubmission != null) {
                pstmt = conn.prepareStatement(updateGradebookSubmissionSQL);
            } else {
                pstmt = conn.prepareStatement(insertGradebookSubmissionSQL);
            }

            pstmt.setInt(++i,    gbScoreID);
            pstmt.setString(++i, filename);
            pstmt.setInt(++i,    fileSize);
            pstmt.setString(++i, gcomment);
            pstmt.setString(++i, contentType);
            pstmt.setTimestamp(++i, ts.getTimestamp());
            pstmt.setString(++i, data);

            if (gbSubmission != null) {
                pstmt.setInt(++i, gbSubmission.getID());
            }

            pstmt.executeUpdate();

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

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
    private static final String insertGradebookSubmissionCommentSQL =
    (new StringBuffer()
    .append("INSERT into gradebook_submission (gradebook_score_id, ")
    .append("gcomment, submission_count) values (?,?,0)")).toString();
    
    private static final String updateGradebookSubmissionCommentSQL =
    (new StringBuffer()
    .append("UPDATE gradebook_submission set gradebook_score_id = ?, ")
    .append("gcomment = ? where gradebook_submission_id = ?")).toString();

    private static void __doSaveCommentToDB(int gbScoreID, String gcomment, Connection conn) {
        IGradebookFileInfo gbSubmission = getGradebookSubmission(gbScoreID, conn);

        PreparedStatement pstmt = null;

        try {

            if (gbSubmission != null) {
                pstmt = conn.prepareStatement(updateGradebookSubmissionCommentSQL);
            } else {
                pstmt = conn.prepareStatement(insertGradebookSubmissionCommentSQL);
            }

            int i = 0;
            pstmt.setInt(++i,    gbScoreID);
            pstmt.setString(++i, gcomment);

            if (gbSubmission != null) {
                pstmt.setInt(++i, gbSubmission.getID());
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
    
    public static boolean remove (GradebookSubmission submission, Connection conn) {
        return __removeFromDB(submission.getGradebookScoreID(), conn);
    }

    public static boolean remove (int gbScoreId, Connection conn) {
        return __removeFromDB(gbScoreId, conn);
    }

    private static final String deleteGradebookSubmissionSQL =
        "DELETE from gradebook_submission where gradebook_score_id = ?";
    
    private static boolean __removeFromDB(int gbScoreId, Connection conn) {
        boolean success = false;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(deleteGradebookSubmissionSQL);
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
    
    private static final String getSubmissionForScoreSQL =
    "SELECT gradebook_submission_id, gradebook_score_id, filename, filesize, gcomment, submission_count, submit_date, content_type, data FROM gradebook_submission WHERE gradebook_score_id = ?";

    public static GradebookSubmission getGradebookSubmission(
                        int gradebookScoreID, 
                        Connection conn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        GradebookSubmission gbSubmission = null;

        try {
            int i = 0;

            pstmt = conn.prepareStatement(getSubmissionForScoreSQL);
            pstmt.setInt(++i, gradebookScoreID);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                String data = rs.getString("data");
                if (data == null) {
                    gbSubmission = (GradebookSubmission) new GradebookSubmissionImpl(
                                rs.getInt("gradebook_submission_id"),
                                gradebookScoreID,
                                rs.getString("filename"),
                                rs.getInt("filesize"),
                                rs.getInt("submission_count"),
                                rs.getString("gcomment"),
                                rs.getTimestamp("submit_date"),
                                rs.getString("content_type"));

                } else {
                    gbSubmission = (GradebookSubmission)
                    new AssessmentSubmissionImpl(
                    rs.getInt("gradebook_submission_id"),
                    gradebookScoreID,
                    rs.getString("filename"),
                    rs.getInt("filesize"),
                    rs.getInt("submission_count"),
                    rs.getString("gcomment"),
                    rs.getTimestamp("submit_date"),
                    rs.getString("content_type"),
                    data);
                }
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
        return gbSubmission;
    }

    private static final String getAllSubmissionsForOfferingSQL =
        "SELECT gs.gradebook_submission_id, gs.gradebook_score_id, gs.filename, gs.filesize, gs.gcomment, gs.submission_count, gs.submit_date, gs.content_type, gs.data FROM gradebook_submission gs, gradebook_score sc, gradebook_item gi WHERE gs.gradebook_score_id = sc.gradebook_score_id and sc.gradebook_item_id = gi.gradebook_item_id and gi.offering_id = ?";

    public static List getAllGradebookSubmissions(Offering offering, Connection conn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (offering == null) return null;
        
        List submissions = new ArrayList();
        GradebookSubmission gbSubmission = null;

        try {
            int i = 0;

            pstmt = conn.prepareStatement(getAllSubmissionsForOfferingSQL);
            pstmt.setLong(++i, offering.getId());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String data = rs.getString("data");

                if (data == null) {
                    gbSubmission = (GradebookSubmission) new GradebookSubmissionImpl(
                    rs.getInt("gradebook_submission_id"),
                    rs.getInt("gradebook_score_id"),
                    rs.getString("filename"),
                    rs.getInt("filesize"),
                    rs.getInt("submission_count"),
                    rs.getString("gcomment"),
                    rs.getTimestamp("submit_date"),
                    rs.getString("content_type"));
                } else {
                    gbSubmission = (GradebookSubmission) new AssessmentSubmissionImpl(
                    rs.getInt("gradebook_submission_id"),
                    rs.getInt("gradebook_score_id"),
                    rs.getString("filename"),
                    rs.getInt("filesize"),
                    rs.getInt("submission_count"),
                    rs.getString("gcomment"),
                    rs.getTimestamp("submit_date"),
                    rs.getString("content_type"),
                    data);
                }
                submissions.add(gbSubmission);
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
        return submissions;
    }

}
