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

package net.unicon.portal.channels.gradebook.base;

import java.io.*;
import java.text.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.unicon.academus.common.SearchCriteria;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.Assessment;
import net.unicon.academus.delivery.AssessmentImpl;
import net.unicon.academus.delivery.AssessmentList;
import net.unicon.academus.delivery.AssessmentListImpl;
import net.unicon.academus.delivery.DeliveryAdapter;
import net.unicon.academus.delivery.DeliveryAdapterFactory;
import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.delivery.ReferenceObject;
import net.unicon.academus.delivery.Results;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.EnrollmentStatus;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.file.FileServiceFactory;
import net.unicon.portal.channels.gradebook.GradebookService;
import net.unicon.portal.channels.gradebook.GradebookItem;
import net.unicon.portal.channels.gradebook.GradebookScore;
import net.unicon.portal.channels.gradebook.IGradebookFileInfo;
import net.unicon.portal.channels.gradebook.GradebookSubmission;
import net.unicon.portal.channels.gradebook.GradebookFeedback;
import net.unicon.portal.channels.gradebook.GradebookSubmission;
import net.unicon.portal.channels.curriculum.Curriculum;
import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.channels.gradebook.GradebookActivationService;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.portal.cache.DirtyCacheRequestHandler;
import net.unicon.portal.cache.DirtyCacheRequestHandlerFactory;

import net.unicon.portal.channels.gradebook.GradebookAssessmentService;
import net.unicon.portal.channels.curriculum.CurriculumServiceFactory;
import net.unicon.portal.channels.curriculum.CurriculumService;
import net.unicon.portal.common.service.activation.ActivationServiceFactory;
import net.unicon.portal.common.service.activation.ActivationService;

import net.unicon.penelope.IDecisionCollection;

import net.unicon.sdk.properties.*;
import net.unicon.portal.common.properties.*;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.db.ADbMode;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbPageMode;
import net.unicon.sdk.time.*;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;

import net.unicon.portal.channels.gradebook.GradebookItemFactory;
import net.unicon.portal.channels.gradebook.GradebookSubmissionFactory;
import net.unicon.portal.channels.gradebook.GradebookFeedbackFactory;
import net.unicon.portal.channels.gradebook.GradebookScoreFactory;

public class GradebookServiceImpl
implements GradebookService, GradebookActivationService, GradebookAssessmentService {

    private static final int ENROLLED = EnrollmentStatus.ENROLLED.toInt();

    private static final String updateGradebookItemMaxAndMinScoreSQL =
    "UPDATE gradebook_item set max_score = ?, min_score = ? where gradebook_item_id = ?";

    private static final String updateWeightSQL =
    "UPDATE gradebook_item set weight = ? where gradebook_item_id = ?";

    private static final String updateMeanAndMedian =
    "UPDATE gradebook_item set median = ?, mean = ? where gradebook_item_id = ?";

    private static final String getGradebookItemIdSQL =
    "select gradebook_item_id from gradebook_item where offering_id = ?";

    private static final String globalSubSelectUpdateMeanSQL =
    "SELECT round(avg(gs.score), 0) AS mean" +
    " FROM gradebook_score gs, membership m, gradebook_item gi" +
    " WHERE gi.offering_id = m.offering_id" +
    " AND m.user_name = gs.user_name" +
    " AND gs.gradebook_item_id = gi.gradebook_item_id" +
    " AND gs.score >= 0" +
    " AND gi.gradebook_item_id = ?" +
    " AND m.enrollment_status = ?";
    
    private static final String globalUpdateMeanSQL =
    "update gradebook_item set mean = ? where gradebook_item_id = ?";

    private static final String getGradebookScoresForMedianSQL =
    "select gs.score from gradebook_score gs, membership m, gradebook_item gi where gi.offering_id = m.offering_id and m.user_name = gs.user_name and gs.gradebook_item_id = gi.gradebook_item_id and gs.score >= 0 and gi.gradebook_item_id = ? AND M.enrollment_status = ? order by gs.score";

    private static final String updateMedianSQL =
    "UPDATE gradebook_item set median = ? where gradebook_item_id = ?";

    private static final String insertGradebookActivationSQL =
    "insert into gbitem_activation (gradebook_item_id, activation_id) values (?,?)";

    private static final String getGBItemActivationsByActivationIDSQL =
    "SELECT GI.gradebook_item_id, GI.offering_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name, GI.type, GI.association, GI.mean, GI.median, GI.feedback, GIA.activation_id FROM gradebook_item GI, gbitem_activation GIA WHERE GIA.activation_id = ? AND GIA.gradebook_item_id = GI.gradebook_item_id";

    private static final String getGBItemActivationsSQL =
    "SELECT GI.gradebook_item_id, GI.offering_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name, GI.type, GI.association, GI.mean, GI.median, GI.feedback, GIA.activation_id FROM gradebook_item GI, gbitem_activation GIA WHERE GIA.gradebook_item_id = ? AND GIA.gradebook_item_id = GI.gradebook_item_id";

    private static final String getOfferingActivationsSQL =
    "SELECT GI.gradebook_item_id, GI.offering_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name, GI.type, GI.association, GI.mean, GI.median, GI.feedback, GIA.activation_id FROM gradebook_item GI, gbitem_activation GIA WHERE GI.offering_id = ? AND GI.gradebook_item_id = GIA.gradebook_item_id";

    private static final String getGBItembyActivationSQL =
    "SELECT GI.gradebook_item_id, GI.offering_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name, GI.type, GI.association, GI.mean, GI.median, GI.feedback, GIA.activation_id FROM gradebook_item GI, gbitem_activation GIA WHERE GIA.activation_id = ? AND GIA.gradebook_item_id = GI.gradebook_item_id";

    private static final boolean useDeliverySystem =
    ((String) UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.academus.delivery.DeliveryAdapter")).equals("true");

    private static final String INSERT_DELIVERY_ASSESSMENT_SQL =
    "INSERT INTO acad_delivery_assessment (delivery_system_id, delivery_curriculum_id, delivery_assessment_id, assessment_title, assessment_description) VALUES (?,?,?,?,?)";

    private static final String UPDATE_DELIVERY_ASSESSMENT_SQL =
    "UPDATE acad_delivery_assessment SET assessment_title=?, assessment_description=?  WHERE delivery_system_id=? AND delivery_assessment_id=? AND delivery_curriculum_id = ?";

    private static final String DELETE_DELIVERY_ASSESSMENT_SQL =
    "DELETE FROM acad_delivery_assessment WHERE delivery_system_id=? AND delivery_assessment_id=?";

    private static final String DELETE_DELIVERY_ASSESSMENT_FROM_SYSTEM_SQL =
    "DELETE FROM acad_delivery_assessment WHERE delivery_system_id=";

    private static final String DELETE_ALL_DELIVERY_ASSESSMENTS_SQL =
    "DELETE FROM acad_delivery_assessment";


    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.domain.Offering}>
     * @see <{net.unicon.portal.domain.User}>
     */

    public List getGradebookEntries(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                Connection conn) {
        return GradebookItemFactory.getGradebookItems(
            user,
            offering,
            forAllUsers,
            null,
            conn);
    }

    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param filters - filters for Gradebook Entries if they exist.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     * @see <{net.unicon.portal.lms.domain.User}>
     */

    public List getGradebookEntries(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn) {

        String username = null;
        if (user != null) {
            username = user.getUsername();
        }
        return GradebookItemFactory.getGradebookItems(
            username,
            offering,
            forAllUsers,
            filters,
            conn);
    }

    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param username - a username
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     */

    public List getGradebookEntries(
                                    String username,
                                    Offering offering,
                                    boolean forAllUsers,
                                    IFilterMode[] filters,
                                    Connection conn) {
        return GradebookItemFactory.getGradebookItems(
            username,
            offering,
            forAllUsers,
            filters,
            conn);
    }

    /**
     * Returns a specific gradebook entries
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param gradebookItemID - the gradebook entry to return
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param filters - filters for Gradebook Entries if they exist.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of a single Gradebook Entry.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public List getGradebookEntry(
                                User user,
                                Offering offering,
                                int gradebookItemId,
                                boolean forAllUsers,
                                Connection conn) {

        return getGradebookEntry(
                                user,
                                offering,
                                gradebookItemId,
                                forAllUsers,
                                null,
                                conn);

    } // end getGradebookEntry

    /**
     * Returns a specific gradebook entries
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param gradebookItemID - the gradebook entry to return
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of a single Gradebook Entry.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     */
    public List getGradebookEntry(
                                User user,
                                Offering offering,
                                int gradebookItemId,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn) {
        return GradebookItemFactory.getGradebookItem (
                   user, offering, gradebookItemId, forAllUsers,filters, conn);
    }

    public List getGradebookPageEntries(
                                Offering offering,
                                IFilterMode[] filters,
                                IPageMode page,
                                Connection conn) {

        return GradebookItemFactory.getGradebookPageItems(
                   offering, filters, page, conn);
    }

    public int insertGradebookItem(
                        User user,
                        Offering offering,
                        int type,
                        int weight,
                        int position,
                        String name,
                        int maxScore,
                        int minScore,
                        String feedback,
                        String association,
                        Connection conn) {
        return GradebookItemFactory.create(
                    offering,
                    type,
                    weight,
                    position,
                    name,
                    maxScore,
                    minScore,
                    feedback,
                    association,
                    conn);
    }

    public boolean updateMaxAndMinScore(
                    int gbItemID,
                    int minScore,
                    int maxScore,
                    Connection conn) {
        boolean success = false;
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(updateGradebookItemMaxAndMinScoreSQL);

            int i = 0;
            pstmt.setInt(++i, maxScore);
            pstmt.setInt(++i, minScore);
            pstmt.setInt(++i, gbItemID);
            pstmt.executeUpdate();

            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return success;
    }

    public boolean saveGradebookItem(
                        int gbItemID,
                        Offering offering,
                        int type,
                        int weight,
                        int position,
                        int oldPosition,
                        String name,
                        int maxScore,
                        int minScore,
                        String feedback,
                        String association,
                        Connection conn) {
        return GradebookItemFactory.save(
                        gbItemID,
                        offering,
                        type,
                        weight,
                        position,
                        oldPosition,
                        name,
                        maxScore,
                        minScore,
                        feedback,
                        association,
                        conn);
    }

    public boolean deleteGradebookItems(Offering offering, Connection conn) {
        return GradebookItemFactory.removeAll(offering, conn);
    }

    /** Deletes a gradebookItem and its children data */
    public boolean deleteGradebookItem(
                        User user,
                        Offering offering,
                        int gbItemID,
                        int currentPosition,
                        Connection conn) {
        return GradebookItemFactory.remove(offering, gbItemID, currentPosition, conn);
    }

    public boolean updateGradebookMeanAndMedian(
                        int gbItemID,
                        Offering offering,
                        int mean,
                        int median,
                        Connection conn) {

        boolean success = false;

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(updateMeanAndMedian);

            int i = 0;
            pstmt.setInt(++i, median);
            pstmt.setInt(++i, mean);
            pstmt.setInt(++i, gbItemID);
            pstmt.executeUpdate();

            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return success;
    }

    public boolean insertUserScores(Offering offering, User user, Connection conn) {
         Long offId = new Long(offering.getId());
         return GradebookScoreFactory.insertUser(
             offId.intValue(),
             user.getUsername(),
             conn);
    }

    /**
     * Updates the users score in the gradebook.
     * @param gbItemID - the gradebook Item ID.
     * @param username - the username to be updated.
     * @param score - the new score.
     * @param Offering - the offering the user belongs in.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     */
    public boolean updateUserScore(
                int gbItemID,
                String userName,
                int score,
                Offering offering,
                Connection conn) {
        return GradebookScoreFactory.update(
                gbItemID,
                userName,
                score,
                (int) offering.getId(),
                conn);
    }

    /**
     * Updates the users score in the gradebook.
     * @param gbItemID - the gradebook Item ID.
     * @param username - the username to be updated.
     * @param score - the new score.
     * @param offeringId - the offering id in which the user belongs in.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     */
    public boolean updateUserScore(
                int gbItemID,
                String userName,
                int score,
                int offeringId,
                Connection conn) {
        return GradebookScoreFactory.update(
                gbItemID,
                userName,
                score,
                offeringId,
                conn);
    }

    public boolean recalculateStatistics(Offering offering, Connection conn) {
        return recalculateStatistics(offering.getId(), conn);
    }

    public boolean recalculateStatistics(long offeringId, Connection conn) {

        boolean success = false;

        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;
        PreparedStatement pstmt5 = null;

        ResultSet rs = null;
        ResultSet rs2 = null;

        try {
            pstmt = conn.prepareStatement(getGradebookItemIdSQL);
            pstmt2 = conn.prepareStatement(globalUpdateMeanSQL);
            pstmt3 = conn.prepareStatement(getGradebookScoresForMedianSQL);
            pstmt4 = conn.prepareStatement(updateMedianSQL);
            pstmt5 = conn.prepareStatement(globalSubSelectUpdateMeanSQL);

            int i = 0;
            pstmt.setLong(++i, offeringId);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                // recalculate mean, note: This select-update combination of queries to update the mean is not atomic
            	
            	int gradebookItemId = rs.getInt("gradebook_item_id");
                
                i = 0;
                pstmt5.setInt(++i, gradebookItemId);
                pstmt5.setInt(++i, ENROLLED);
                
                ResultSet rs5 = pstmt5.executeQuery();
                int mean = 0;
                if(rs5.next()) {
                	mean = rs5.getInt("mean");
                }
                
                i = 0;
                pstmt2.setInt(++i, mean);
                pstmt2.setInt(++i, gradebookItemId);

                pstmt2.executeUpdate();

                // read in gradebook scores for median calculation

                i = 0;
                pstmt3.setInt(++i, gradebookItemId);
                pstmt3.setInt(++i, ENROLLED);

                rs2 = pstmt3.executeQuery();

                List scoreList = new ArrayList();

                while (rs2.next()) {
                    int score = rs2.getInt("score");
                    scoreList.add(new Integer(score));
                }

                // calculate median

                int median = 0;
                int size = scoreList.size();

                if (size == 0) {
                    median = 0;
                } else if (size == 1) {
                    median = ((Integer)scoreList.get(0)).intValue();
                } else if ((size % 2) != 0) {
                    median = ((Integer)scoreList.get(size / 2)).intValue();
                } else {
                    int val1 = ((Integer)scoreList.get(size / 2)).intValue();
                    int val2 = ((Integer)scoreList.get((size / 2) - 1)).intValue();
                    median = (val1 + val2) / 2;
                }

                // update median

                i = 0;

                pstmt4.setInt(++i, median);
                pstmt4.setInt(++i, gradebookItemId);
                pstmt4.executeUpdate();

            }
            success = true;
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
                if (rs2 != null) {
                    rs2.close();
                    rs2 = null;
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

            try {
                if (pstmt2 != null) {
                    pstmt2.close();
                    pstmt2 = null;
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }

            try {
                if (pstmt3 != null) {
                    pstmt3.close();
                    pstmt3 = null;
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }

            try {
                if (pstmt4 != null) {
                    pstmt4.close();
                    pstmt4 = null;
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }

        return success;

    }

    /** add submissions and feedback to a gb score */

    public void addGradebookScoreDetails(
                                        List gradebooks,
                                        User user,
                                        int gradebookItemID,
                                        boolean forAllUsers,
                                        Offering o,
                                        Connection conn) throws DeliveryException {

        boolean found = false;

        GradebookItem gbItem = null;
        GradebookFeedback gbFeedback = null;
        GradebookSubmission gbSubmission = null;

        ReferenceObject ref = null;
        // Finding GbItem to add GradebookScoreDetails too
        for (int i = 0; i < gradebooks.size() && !found; i++) {
            gbItem = (GradebookItem) gradebooks.get(i);
            if (gradebookItemID == gbItem.getId()) {
                found = true;
            }
        }

        // IF we could not find the gbitem
        // passed in throw an exception
        if (!found) {
            throw new IllegalArgumentException(
            "Could not find gradebookItemID in list: " + gradebookItemID);
        }
        DeliveryAdapter delAdapter = null;
        try {
            if (useDeliverySystem) {
                delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Get a list of gbScores
        List scores = gbItem.getGradebookScores();
        GradebookScore gbScore = null;
        for (int i = 0; i < scores.size(); i++) {
            gbScore = (GradebookScore) scores.get(i);
            if (user.getUsername().equals(gbScore.getUsername()) && !forAllUsers) {
                gbSubmission = getGradebookSubmission(gbScore.getID(), conn);
                gbScore.setGradebookSubmission(gbSubmission);

                // get the gradebook feedback
                gbFeedback = getGradebookFeedback(gbScore.getID(), conn);

                // if the type is online then set the filename attribute to
                // be the URL for Virtuoso's exam results page
                if (gbItem.getType() == 1 && useDeliverySystem && gbFeedback != null) {
                    // why don't we have a constant for online?
                    ref = delAdapter.getReferenceLink(
                                            null,
                                            // filename == ER_ID for online
                                            gbSubmission.getFilename(),
                                            delAdapter.PERSONALIZED_FEEDBACK,
                                            user, o);

                    if (ref != null) {
                        gbFeedback.setFilename(ref.getURL());
                    }
                }
                gbScore.setFeedback(gbFeedback);
            }

            else if (forAllUsers) {
                gbSubmission = getGradebookSubmission(gbScore.getID(), conn);
                gbScore.setGradebookSubmission(gbSubmission);

                // get the gradebook feedback
                gbFeedback = getGradebookFeedback(gbScore.getID(), conn);

                // if the type is online then set the filename attribute to
                // be the URL for Virtuoso's exam results page
                if (gbItem.getType() == 1 && useDeliverySystem && gbFeedback != null) {
                    // why don't we have a constant for online?
                    try {
                        ref = delAdapter.getReferenceLink(
                                            null,
                                            // filename == ER_ID for online
                                            gbSubmission.getFilename(),
                                            delAdapter.PERSONALIZED_FEEDBACK,
                                            gbScore.getUser(), o);

                        if (ref != null) {
                            gbFeedback.setFilename(ref.getURL());
                        }

                    } catch (Exception io) {
                        System.out.println("Delivery System failed to return a reference link." + io);
                        io.printStackTrace(System.out);
                    }
                }
                gbScore.setFeedback(gbFeedback);
            }
        }
    } // end addGradebookScoreDetails

    public void addGradebookActivations(List gradebook, User user, Offering o,
        int gradebookItemID, Connection conn) {

        // search for the gradebook item for these details
        // and add the activations
        Iterator itr = gradebook.iterator();
        GradebookItem gbItem = null;
        boolean found = false;

        while (!found && itr.hasNext()) {
            gbItem = (GradebookItem)itr.next();
            if (gbItem.getId() == gradebookItemID) {
                found = true;
                List items = getGradebookItemWithActivations(
                gradebookItemID, user, o, conn);
                if (items != null && items.size() > 0) {
                    GradebookItem gbi = (GradebookItem)items.get(0);
                    gbItem.addAllXMLAbleEntities(gbi.getActivations());
                }
            }
        } // end while loop
    } // end addGradebookActivations

    public GradebookSubmission getGradebookSubmission(int gradebookScoreID, Connection conn) {
        return GradebookSubmissionFactory.getGradebookSubmission(gradebookScoreID, conn);
    }

    public List getAllGradebookSubmissions(Offering offering, Connection conn) {
        return GradebookSubmissionFactory.getAllGradebookSubmissions(offering, conn);
    }

    public GradebookFeedback getGradebookFeedback(int gradebookScoreID, Connection conn) {
        return GradebookFeedbackFactory.getGradebookFeedback(gradebookScoreID, conn);
    }

    public List getAllGradebookFeedbacks(Offering offering, Connection conn) {
        return GradebookFeedbackFactory.getAllGradebookFeedbacks(offering, conn);
    }


    public void updateSubmissionDetails(
                String username,
                int gbItemID,
                String submissionFileName,
                String submissionComment,
                String submissionContentType,
                InputStream submissionStream,
                Connection conn) {
        GradebookSubmissionFactory.create(
            username,
            gbItemID,
            submissionFileName,
            submissionComment,
            submissionContentType,
            submissionStream,
            conn);
    }
    public void updateFeedbackDetails(
                    String username,
                    int gbItemID,
                    String feedbackFileName,
                    String feedbackComment,
                    String feedbackContentType,
                    InputStream feedbackStream,
                    Connection conn) {
        GradebookFeedbackFactory.create(
            username,
            gbItemID,
            feedbackFileName,
            feedbackComment,
            feedbackContentType,
            feedbackStream,
            conn);
    }

    public void updateAssessmentDetails(
    String username,
    int gbItemID,
    String resultID,
    String results,
    Connection conn) {
        int gbScoreID = GradebookScoreFactory.getGradebookScoreID(
        gbItemID,
        username,
        conn);

        if (gbScoreID == -1) {
            throw new IllegalArgumentException(
            "Cannout Find gradebook_score_id for username, "
            + username + " gradebookID " + gbItemID);
        }
        GradebookSubmissionFactory.save(
            gbScoreID,
            0, // size
            resultID,
            "",
            "xml",
            results,
            conn);

        // added by KG 12/2/02 to save PF feedback links upon
        // completion of a Virtuoso assessment
        GradebookFeedbackFactory.save(
            gbScoreID,
            0, // size
            resultID,
            "",
            "",
            conn);
    }

    public void updateWeight(int weight, int gbItemID, Connection conn) {
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(updateWeightSQL);

            int i = 0;
            pstmt.setInt(++i, weight);
            pstmt.setInt(++i, gbItemID);
            pstmt.executeUpdate();

        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public List getGradebookItemWithActivations(
                            int gbItemID,
                            User user,
                            Offering o,
                            Connection conn) {
                                List gbItems = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(getGBItemActivationsSQL);

            int i = 0;
            pstmt.setInt(++i, gbItemID);
            rs = pstmt.executeQuery();
            gbItems = buildGradebookItemsWithActivations(rs, true, user, o, conn);

        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return gbItems;
    }

    public List getGradebookItemWithActivations(
                            Offering offering,
                            User user,
                            boolean allUsers,
                            Connection conn) {

        List gbItems = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(getOfferingActivationsSQL);

            int i = 0;
            pstmt.setInt(++i, (int) offering.getId());

            rs = pstmt.executeQuery();

            gbItems = buildGradebookItemsWithActivations(rs, allUsers, user, offering, conn);
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return gbItems;
    }

    public List getGradebookActivation(
                            int activationID,
                            User user,
                            Offering o,
                            Connection conn) {

        List gbItems = null;

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(getGBItembyActivationSQL);

            int i = 0;

            pstmt.setInt(++i, activationID);
            rs = pstmt.executeQuery();
            gbItems = buildGradebookItemsWithActivations(rs, true, user, o, conn);

        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return gbItems;
    }

    private List buildGradebookItemsWithActivations(
                            ResultSet rs,
                            boolean allUsers,
                            User user,
                            Offering o,
                            Connection conn) {

        List gradebookItems = new ArrayList();

        try {

            ActivationService actService = ActivationServiceFactory.getService();

            int gbItemID     = -1;
            int prevGbItemID = -1;
            int activationID = -1;

            GradebookItem gbItem  = null;

            Activation activation = null;

            while (rs.next()) {
                gbItemID = rs.getInt("gradebook_item_id");
                activationID = rs.getInt("activation_id");

                if (gbItemID != prevGbItemID) {
                    gbItem = new GradebookItemImpl(
                                    gbItemID,
                                    rs.getInt("offering_id"),
                                    rs.getInt("pos"),
                                    rs.getInt("weight"),
                                    rs.getInt("type"),
                                    rs.getInt("max_score"),
                                    rs.getInt("min_score"),
                                    rs.getInt("mean"),
                                    rs.getInt("median"),
                                    rs.getString("name"),
                                    rs.getString("feedback"),
                                    rs.getString("association"),
                                    new ArrayList());

                    gradebookItems.add(gbItem);

                }

                if (allUsers) {

                    activation = actService.getActivation(activationID, conn);

                } else {

                    activation = actService.getActivation(activationID, user.getUsername(), conn);

                }

                if (activation != null) {
                    if (activation.getType().equals(actService.ONLINE_ASSESSMENT)) {
                        activation.setReferenceObject(
                        this.getAssessmentListLink(
                        gbItem.getAssociation(),
                        o,
                        user,
                        conn));
                    }
                    gbItem.addXMLAbleEntity(activation);
                }
                prevGbItemID = gbItemID;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gradebookItems;
    }

    public Activation addActivation(
                    int gbItemID,
                    int offeringID,
                    String gbType,
                    Date startDate,
                    Date endDate,
                    long startTime,
                    long endTime,
                    Map attributes,
                    List userList,
                    boolean allUsers,
                    User user,
                    Connection conn,
                    IDecisionCollection dc) {

        PreparedStatement pstmt = null;
        Activation activation = null;

        try {

            ActivationService actService = ActivationServiceFactory.getService();

            String type = null;

            boolean isOnline = false;

            if (gbType.equals("1")) {
                type = actService.ONLINE_ASSESSMENT;
                isOnline = true;
            } else if (gbType.equals("2")) {
                type = actService.ASSIGNMENT;
            }

            activation = actService.createActivation(
                                    offeringID,
                                    type,
                                    startDate,
                                    endDate,
                                    startTime,
                                    endTime,
                                    attributes,
                                    userList,
                                    allUsers,
                                    conn);

            /* activating delivery system */

            if (useDeliverySystem && isOnline) {
                DeliveryAdapter delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                try {
                    boolean success = delAdapter.createActivation(activation, dc, user);

                    if (success == false) {
                        actService.deleteActivation(activation.getActivationID(), conn);
                        return null;
                    }
                } catch (DeliveryException de) {
                    de.printStackTrace();
                }
            }

            // If we get to this point we've been successful writing the activation
            // to Virtuoso if Virtuoso is available. If Virtuoso is not available
            // then we are going to fall down to here anyway.
            pstmt = conn.prepareStatement(insertGradebookActivationSQL);

            int i = 0;

            pstmt.setInt(++i, gbItemID);
            pstmt.setInt(++i, activation.getActivationID());
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
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return activation;
    }

    public void removeActivation(
        int activationID,
        Offering offering,
        User user,
        Connection conn) {
        try {

            ActivationService actService = ActivationServiceFactory.getService();
            Activation activation = actService.getActivation(activationID, conn);

            if (activation != null) {

                actService.deactivateActivation(activationID, conn);

                boolean isOnline = activation.getType().equals(actService.ONLINE_ASSESSMENT);

                /* activating delivery system */
                if (useDeliverySystem && isOnline) {
                    DeliveryAdapter delAdapter =
                        (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                    delAdapter.deactivateAssessments(activation, user);
                }
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public AssessmentList getAssessmentList(
                                User user,
                                Offering offering,
                                Connection conn) {

        List assessments = new ArrayList();

        if (useDeliverySystem) {
            try {
                CurriculumService currService = CurriculumServiceFactory.getService();
                List curriculumList = currService.getCurriculum(
                                            Curriculum.ONLINE,
                                            offering,
                                            user,
                                            conn);
                assessments = getAvailableAssessments(user, curriculumList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AssessmentList assList = (AssessmentList) new AssessmentListImpl(assessments);
        return assList;
    }

    /**
     * <p>
     * Get a list of assessment based on the curriculum.
     * </p><p>
     *
     * @param user - a domain user object.
     * @param a List with all the curriculum
     * </p><p>
     *
     * @return List with the assessments.
     * </p>
     */
    public List getAvailableAssessments(
                                User user,
                                List curriculumList) {
        List assessments = null;
        try {
            DeliveryAdapter delAdapter =
                    (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
            assessments = delAdapter.getAssessmentList(curriculumList, user);
        } catch (Exception e) {
            e.printStackTrace();
            assessments = new ArrayList();
        }
        return assessments;
    }

    public Assessment getAssessment (User user,
                                Offering offering,
                                String assessmentID,
                                boolean withForms,
                                Connection conn) {

        Assessment retAssessment = null;
        if (useDeliverySystem) {
            try {
                DeliveryAdapter delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                retAssessment = delAdapter.getAssessment(assessmentID, user.getUsername(), withForms, withForms, false, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retAssessment;
    }

    public ReferenceObject getAssessmentListLink(
                                String assessmentID,
                                Offering offering,
                                User user,
                                Connection conn) {
        ReferenceObject link = null;

        if (useDeliverySystem) {
            try {
                DeliveryAdapter delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                link = delAdapter.getReferenceLink(
                    null,
                    assessmentID,
                    delAdapter.ASSESSMENT_LIST,
                    user, offering);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return link;
    }

    public void updateAssessmentSubmission(
                            String username,
                            String resultID) {

        if (useDeliverySystem) {
            DeliveryAdapter delAdapter = null;
            try {
                delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Results results = delAdapter.getAssessmentResults(resultID, username, null, null);  // ToDo:  Must pass theme & style...

            if (results != null) {
                int score = results.getScore();
                String activationID = results.getActivationID();
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;

                try {
                    conn = RDBMServices.getConnection();

                    pstmt = conn.prepareStatement(getGBItemActivationsByActivationIDSQL);
                    int i = 0;
                    pstmt.setString(++i, activationID);
                    rs = pstmt.executeQuery();

                    if (rs.next()) {
                        int gbItemID = rs.getInt("gradebook_item_id");
                        int offeringId = rs.getInt("offering_id");

                        GradebookScoreFactory.update(
                                                gbItemID,
                                                username,
                                                score,
                                                score,
                                                offeringId,
                                                conn);

                        this.recalculateStatistics((long) offeringId, conn);

                        updateAssessmentDetails(
                        username,
                        gbItemID,
                        resultID,
                        results.toXML(),
                        conn);
                    }
                    try {
                        DirtyCacheRequestHandler cacheHandler
                                = DirtyCacheRequestHandlerFactory.getHandler();
                        cacheHandler.broadcastDirtyChannels(
                                UserFactory.getUser(username),
                                "GradebookChannel");
                    } catch (Exception e) {
                        LogService.instance().log(LogService.ERROR,
                            "GradebookServiceImpl: Unable to get DirtyCacheRequestHandler "+
                            "Implementation; DirtyCacheRequestHandlerFactory is throwing errors"+
                            "Possibly cannot find user with the username : " + username);
                        e.printStackTrace();
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (SQLException se2) {
                        se2.printStackTrace();
                    }

                    try {
                        if (pstmt != null) {
                            pstmt.close();
                        }
                    } catch (SQLException se2) {
                        se2.printStackTrace();
                    }
                    RDBMServices.releaseConnection(conn);
                }
            }
        }
    } // end updateAssessmentSubmission

    public List getGBItemQuestionDetails(
                    int gbItemID,
                    String username, // the user to lookup
                    User user,       // the user making the request
                    Offering offering,
                    Connection conn) throws DeliveryException {

        /* Getting a specific users gradebookEntries */
        List assDetails = this.getGradebookEntries(username,offering,false,null,conn);
        GradebookItem gbItem = null;

        String association = null;

        for (int ix = 0; ix < assDetails.size(); ++ix) {
            gbItem = (GradebookItem) assDetails.get(ix);

            if (gbItem.getId() == gbItemID) {

                association  = gbItem.getAssociation();

                if (association != null && gbItem.getType() == 1) {
                    XMLAbleEntity pObj = (XMLAbleEntity)
                            this.getAssessment(
                            user,
                            offering,
                            association,
                            true,
                            conn);

                    if (pObj != null) {
                        gbItem.addXMLAbleEntity(pObj);
                        ix = assDetails.size();
                    }
                }
            }
        }
        try {
            User lookupUser = null;
            lookupUser = UserFactory.getUser(username);
            this.addGradebookScoreDetails(
                    assDetails,
                    lookupUser,
                    gbItemID,
                    false,
                    offering,
                    conn);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,"User with the username "
                + username +
                " is not currently found in the system.");
            e.printStackTrace();
        }
        return assDetails;

    } // end getGBItemQuestionDetails

    public List getGBItemQuestionDetails(
                                    int gbItemID,
                                    User user,
                                    Offering offering,
                                    boolean forAllUsers,
                                    Connection conn) throws DeliveryException {

        return getGBItemQuestionDetails(
                                    gbItemID,
                                    user,
                                    offering,
                                    forAllUsers,
                                    null,
                                    conn);

    } // end getGBItemQuestionDetails

    public List getGBItemQuestionDetails(
                                    int gbItemID,
                                    User user,
                                    Offering offering,
                                    boolean forAllUsers,
                                    IFilterMode[] filters,
                                    Connection conn) throws DeliveryException {

        List assDetails = this.getGradebookEntries(
                                                user,
                                                offering,
                                                forAllUsers,
                                                filters,
                                                conn);

        GradebookItem gbItem = null;
        String association = null;
        for (int ix = 0; ix < assDetails.size(); ++ix) {
            gbItem = (GradebookItem) assDetails.get(ix);
            if (gbItem.getId() == gbItemID) {
                association  = gbItem.getAssociation();
                if (association != null && gbItem.getType() == 1) {
                    XMLAbleEntity pObj = (XMLAbleEntity)
                        this.getAssessment(
                                        user,
                                        offering,
                                        association,
                                        true,
                                        conn);

                    if (pObj != null) {
                        gbItem.addXMLAbleEntity(pObj);
                        ix = assDetails.size();
                    }
                }
            } // end outer if
        } // end assDetails loop

        // Not for All Users, the user may not be
        // enrolled in the class
        this.addGradebookScoreDetails(
                                    assDetails,
                                    user,
                                    gbItemID,
                                    forAllUsers,
                                    offering,
                                    conn);

        return assDetails;
    } // end getGBItemQuestionDetails

    public AssessmentList findAssessment(
                                SearchCriteria criteria,
                                User user,
                                Offering offering) throws Exception {
        DeliveryAdapter delAdapter = null;
        AssessmentList assList = null;
        try {
            if (useDeliverySystem) {
                delAdapter = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                List assessments = delAdapter.findAssessments(
                                            criteria,
                                            user,
                                            (int) offering.getId());

                if (assessments == null) {
                    assessments = new ArrayList();
                }
                assList = (AssessmentList) new AssessmentListImpl(assessments);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to retrieve Delivery Adapter", e);
        }
        return assList;
    }

    /**
     * <p>
     * Add the given list of assessments for a given
     * delivery system.
     * </p><p>
     * NOTE: This is very virtuoso specific table.  Then this
     * method should not be in this class - H2
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int addDeliveryAssessments(List assessmentList, String systemID) {
        Connection conn = RDBMServices.getConnection();
        PreparedStatement pstmt = null;

        int rval = 0;
        Assessment assessment = null;

        try {
            pstmt = conn.prepareStatement(INSERT_DELIVERY_ASSESSMENT_SQL);

            for (Iterator it = assessmentList.iterator(); it.hasNext(); ) {
                assessment = (Assessment) it.next();
                pstmt.clearParameters();
                String assessmentID = assessment.getAssessmentID();
                String curriculumID = assessmentID.substring(0, assessmentID.indexOf('|'));

                int i = 0;
                pstmt.setString(++i, systemID);
                pstmt.setString(++i, curriculumID); // XXX This is a virtuoso hack
                                                    // This code belongs somewhere
                                                    // else.
                pstmt.setString(++i, assessmentID);
                pstmt.setString(++i, assessment.getName());
                pstmt.setString(++i, assessment.getDescription());

                try {
                    rval += pstmt.executeUpdate();
                }   catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
        catch (SQLException se2) {
            se2.printStackTrace();
            return 0;
        }
        finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
            RDBMServices.releaseConnection(conn);
        }
        return rval;
    }

    /**
     * <p>
     * Update the given list of assessments for a given
     * delivery system.
     * </p><p>
     * NOTE: This is very virtuoso specific table.  Then this
     * method should not be in this class - H2
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int updateDeliveryAssessments(List assessmentList, String systemID) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        int rval = 0;

        Assessment assessment = null;
        try {
            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(UPDATE_DELIVERY_ASSESSMENT_SQL);
            for (Iterator it = assessmentList.iterator(); it.hasNext(); ) {

                assessment = (Assessment)it.next();
                pstmt.clearParameters();

                String assessmentID = assessment.getAssessmentID();
                String curriculumID = assessmentID.substring(0, assessmentID.indexOf('|'));

                int i = 0;
                pstmt.setString(++i, assessment.getName());
                pstmt.setString(++i, assessment.getDescription());
                pstmt.setString(++i, systemID);
                pstmt.setString(++i, assessmentID);
                pstmt.setString(++i, curriculumID); // XXX This is a virtuoso hack
                                                    // This code belongs somewhere
                                                    // else.
                try {
                    rval += pstmt.executeUpdate();
                }
                catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
        catch (SQLException se2) {
            se2.printStackTrace();
        }
        finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
            RDBMServices.releaseConnection(conn);
        }
        return rval;
    }

    /**
     * <p>
     * Delete the given list of assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteDeliveryAssessments(List contentList, String systemID) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        int rval = 0;

        Assessment assessment = null;

        try {
            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(DELETE_DELIVERY_ASSESSMENT_SQL);

            for (Iterator it = contentList.iterator(); it.hasNext(); ) {
                assessment = (Assessment)it.next();
                pstmt.clearParameters();
                int i = 0;
                pstmt.setString(++i, systemID);
                pstmt.setString(++i, assessment.getAssessmentID());

                try {
                    rval += pstmt.executeUpdate();
                }
                catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
        catch (SQLException se2) {
            se2.printStackTrace();
        }
        finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
            RDBMServices.releaseConnection(conn);
        }
        return rval;
    }

    /**
     * <p>
     * Deletes all of the delivery assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteDeliveryAssessments(String systemID) {

        Connection conn = RDBMServices.getConnection();
        Statement stmt = null;
        int rval = 0;

        try {
            // Using a Statement instead of a PreparedStatement on purpose. This query
            // does not happen often enough and with a high enough performance
            // constraint to tie up database-side resources with a PS
            stmt = conn.createStatement();

            StringBuffer queryBuf = new StringBuffer(20);
            queryBuf.append(DELETE_DELIVERY_ASSESSMENT_FROM_SYSTEM_SQL);
            queryBuf.append("'");
            queryBuf.append(systemID);
            queryBuf.append("'");

            rval = stmt.executeUpdate(queryBuf.toString());
            stmt.close();
            stmt = null;
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    RDBMServices.releaseConnection(conn);
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return rval;
    }

    /**
     * <p>
     * Deletes all of the delivery assessments in the
     * database regardless of the system ID
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteAllDeliveryAssessments() {
        Connection conn = RDBMServices.getConnection();
        Statement stmt = null;
        int rval = 0;
        Curriculum curr = null;

        try {
            // Using a Statement instead of a PreparedStatement on purpose. This query
            // does not happen often enough and with a high enough performance
            // constraint to tie up database-side resources with a PS
            stmt = conn.createStatement();
            rval = stmt.executeUpdate(DELETE_ALL_DELIVERY_ASSESSMENTS_SQL);
            stmt.close();
            stmt = null;
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    RDBMServices.releaseConnection(conn);
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return rval;
    }



    public Activation retrieveActivation(int activationID, Connection conn){
       ActivationService actService = null;

       try{
         actService = ActivationServiceFactory.getService();
       }catch(FactoryCreateException fce){
         fce.printStackTrace();
       }
       Activation act = null;
        try {
         act = actService.getActivation(activationID, conn);
 
       } catch (SQLException se) {
           se.printStackTrace();
       } 

      return act;
       
    }


    /**
    * <p>
    * This method creates the link between an activation and events
    * </p><p>
    * @param newact - an activation object
    * @param conn - A database connection.
    * @return boolean - True is success, false indicates a failure 
    * </p>
    */
    public boolean addEventsToActivation(Activation newact, Connection conn){
                                                                                
       ActivationService actService = null;
       boolean success = false;
                                                                                
       try{
         actService = ActivationServiceFactory.getService();
       }catch(FactoryCreateException fce){
         fce.printStackTrace();
       }
                                                                                
       success = actService.addEventsToActivation(newact, conn);
                                                                                
                                                                                
      return success;
                                                                                
    }

     /**
     * <p>
     * This method deletes the links between an activation and events
     * </p><p>
     * @param activationID - an integer unique identifier for an activation
     * @param conn - A database connection. 
     * </p>
     */
    public void removeEventsFromActivation(int activationID, Connection conn){
                                                                                
       ActivationService actService = null;
                                                                                
       try{
         actService = ActivationServiceFactory.getService();
       }catch(FactoryCreateException fce){
         fce.printStackTrace();
       }
                                                                                
       actService.removeEventsFromActivation(activationID, conn);
                                                                                
    }


    /**
     * <p>
     * Static method denoting use of a
     *   Delivery System
     * </p><p>
     *
     * @return a <code>boolean</code> denoting
     *   use of a Delivery System.
     * </p>
     */
    public static boolean getDeliverySystemUse() {
        return useDeliverySystem;
    }
} // end GradebookServiceImpl class

