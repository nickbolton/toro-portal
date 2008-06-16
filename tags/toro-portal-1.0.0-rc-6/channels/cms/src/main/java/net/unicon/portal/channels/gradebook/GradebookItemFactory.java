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
import java.util.Comparator;
import java.util.Collections;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import net.unicon.sdk.catalog.*;
import net.unicon.sdk.catalog.db.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.academus.domain.lms.*;
import net.unicon.portal.channels.gradebook.base.*;
import org.jasig.portal.services.LogService;

public final class GradebookItemFactory {

    public GradebookItemFactory () {}

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
    public static List getGradebookItems(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                Connection conn) {

        return getGradebookItems(
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
    public static List getGradebookItems(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn) {

        String username = null;
        if (user != null) {
            username = user.getUsername();
        }

        return getGradebookItems(
            username,
            offering,
            forAllUsers,
            filters,
            conn);
    }

    private static final String getOfferingGradebookSQL  =
    "SELECT GI.offering_id, GI.gradebook_item_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name AS itemname, GI.type, GS.gradebook_score_id, GS.score, GS.original_score, GS.user_name, GI.mean, GI.median, GI.feedback, GI.association FROM gradebook_item GI, gradebook_score GS, membership M WHERE GI.offering_id = ? AND M.offering_id = GI.offering_id AND M.user_name = GS.user_name AND GI.gradebook_item_id = GS.gradebook_item_id AND M.enrollment_status = ?";

    private static final String getUserGradebookSQL =
        "SELECT GI.offering_id, GI.gradebook_item_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name AS itemname, GI.type, GS.gradebook_score_id, GS.score, GS.original_score, GS.user_name, GI.mean, GI.median, GI.feedback, GI.association FROM gradebook_item GI, gradebook_score GS, membership M WHERE GS.user_name = ? AND GI.offering_id = ? AND M.offering_id = GI.offering_id AND M.user_name = GS.user_name AND GI.gradebook_item_id = GS.gradebook_item_id AND M.enrollment_status = ?";

    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param username - a username
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     */
    public static List getGradebookItems(
                                    String username,
                                    Offering offering,
                                    boolean forAllUsers,
                                    IFilterMode[] filters,
                                    Connection conn) {

        List offeringGradebook = new ArrayList();

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            int i = 0;

            if (forAllUsers) {
                // Gradebook for everyone in the class
                String sql = __buildQuery(
                                getOfferingGradebookSQL,
                                __entrySorts,
                                filters);
                pstmt = conn.prepareStatement(sql);
            } else { 
                // Gradebook for a user only
                String sql = __buildQuery(
                                getUserGradebookSQL,
                                __entrySorts,
                                filters);
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(++i, username);
            }
            
            pstmt.setInt(++i, (int) offering.getId());
            pstmt.setInt(++i, ENROLLED);

            // Parameters including Mode values
            Object[] objs = __gatherParameters(__entrySorts, filters);
            for (int j = 0; j < objs.length; j++) {
                pstmt.setObject(++i, objs[j]);
            }            
            rs = pstmt.executeQuery();

            offeringGradebook = __buildGradebookObjects(rs, null);
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
        return offeringGradebook;
    }

    private static final String getOfferingGradebookItemSQL  =
    "SELECT GI.offering_id, GI.gradebook_item_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name AS itemname, GI.type, GS.gradebook_score_id, GS.score, GS.original_score, GI.mean, GI.median, GI.feedback, GI.association, GS.user_name FROM gradebook_item GI, gradebook_score GS, membership M WHERE GI.offering_id = ? AND GI.gradebook_item_id = ? AND M.offering_id = GI.offering_id AND M.user_name = GS.user_name AND GI.gradebook_item_id = GS.gradebook_item_id AND M.enrollment_status = ?";

    private static final String getUserGradebookItemSQL =
    "SELECT GI.offering_id, GI.gradebook_item_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name AS itemname, GI.type, GS.gradebook_score_id, GS.score, GS.original_score, GI.mean, GI.median, GI.feedback, GI.association, GS.user_name FROM gradebook_item GI, gradebook_score GS,  membership M WHERE GS.user_name = ? AND GI.offering_id = ? AND GI.gradebook_item_id = ? AND M.offering_id = GI.offering_id AND M.user_name = GS.user_name AND GI.gradebook_item_id = GS.gradebook_item_id AND M.enrollment_status = ?";

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
    public static List getGradebookItem(
                                User user,
                                Offering offering,
                                int gradebookItemId,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn) {

        List offeringGradebook = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int i = 0;
            if (forAllUsers) {
                // Gradebook for everyone in the class
                String sql = __buildQuery(
                                getOfferingGradebookItemSQL,
                                __entrySorts,
                                filters);
                pstmt = conn.prepareStatement(sql);
            } else {
                // Gradebook for a user only
                String sql = __buildQuery(
                                getUserGradebookItemSQL,
                                __entrySorts,
                                filters);
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(++i, user.getUsername());
            }

            pstmt.setInt(++i, (int) offering.getId());
            pstmt.setInt(++i, gradebookItemId);
            pstmt.setInt(++i, ENROLLED);

            // Parameters including Mode values
            Object[] objs = __gatherParameters(__entrySorts, filters);
            for (int j = 0; j < objs.length; j++) {
                pstmt.setObject(++i, objs[j]);
            }

            rs = pstmt.executeQuery();
            offeringGradebook = __buildGradebookObjects(rs, null);

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

        return offeringGradebook;
    } // end getGradebookEntry
    
    private static String __buildQuery(
                    String gradebookSQL,
                    ISortMode[] sorts,
                    IFilterMode[] filters) {

        // Evaluate to see if queryBase has joins (where clauses)
        boolean hasJoins = false;
        if (gradebookSQL.toUpperCase().indexOf(" WHERE ") != -1)
        {
            hasJoins = true;
        }


        // Start the query.
        StringBuffer sql = new StringBuffer(gradebookSQL);

        // Add the filters.
        if (filters != null)
        {
            for (int i = 0; i < filters.length; i++) {
                sql.append((!hasJoins && i == 0) ? " WHERE " : " AND ");
                sql.append(((ADbMode)filters[i]).toSql());
            }
        }

        // Add the sorts.
        if (sorts != null)
        {
            for (int i = 0; i < sorts.length; i++) {
                sql.append((i == 0) ? " ORDER BY " : ", ");
                sql.append(((ADbMode)sorts[i]).toSql());
            }
        }

        return sql.toString();
    } // end buildQuery(sql,sorts,filters)

    private static Object[] __gatherParameters(
                                    ISortMode[] sorts,
                                    IFilterMode[] filters) {
        List objs = new ArrayList();
        Object[] oAry = null;

        // Filters
        if (filters != null)
        {
            for (int i = 0; i < filters.length; i++) {
                oAry = ((ADbMode)filters[i]).getParameters();
                for (int j = 0; j < oAry.length; j++) objs.add(oAry[j]);
            }
        }

        // Sorts
        if (sorts != null)
        {
            for (int i = 0; i < sorts.length; i++) {
                oAry = ((ADbMode)sorts[i]).getParameters();
                for (int j = 0; j < oAry.length; j++) objs.add(oAry[j]);
            }
        }

        return objs.toArray();
    } // end __gatherParameters

    private static final String getOfferingGradebookPageSQL =
    "SELECT GI.OFFERING_ID, GI.GRADEBOOK_ITEM_ID, GI.POS, GI.WEIGHT, GI.MAX_SCORE, GI.MIN_SCORE, GI.NAME AS ITEMNAME, GI.TYPE, GS.GRADEBOOK_SCORE_ID, GS.SCORE, GS.ORIGINAL_SCORE, GI.MEAN, GI.MEDIAN, GI.FEEDBACK, GI.ASSOCIATION, GS.USER_NAME FROM GRADEBOOK_ITEM GI, GRADEBOOK_SCORE GS,  MEMBERSHIP M WHERE M.OFFERING_ID = GI.OFFERING_ID AND M.USER_NAME = GS.USER_NAME AND GI.GRADEBOOK_ITEM_ID = GS.GRADEBOOK_ITEM_ID";
    
    /**
     * Returns a offerings gradebook entries
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of a all offering Gradebook Entries.
     * @see <{net.unicon.portal.lms.domain.Offering}>
     */
    public static List getGradebookPageItems(
                                Offering offering,
                                IFilterMode[] filters,
                                IPageMode page,
                                Connection conn) {

        List offeringGradebook = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Gradebook SQL with Modes applied
            String sql = __buildQuery(
                                getOfferingGradebookPageSQL,
                                __entrySorts,
                                filters);

            pstmt = conn.prepareStatement(sql);

            // Parameters including Mode values
            Object[] objs = __gatherParameters(__entrySorts, filters);
            for (int i = 0; i < objs.length; i++) {
                pstmt.setObject(i + 1, objs[i]);
            }

            // Execute!
            rs = pstmt.executeQuery();

            // Get Gradebook Page objects
            offeringGradebook =
                __buildGradebookObjects(rs, page);

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

        return offeringGradebook;
    } // end getGradebookPageEntries

    private static List __buildGradebookObjects(ResultSet rs, IPageMode page) {

        List gradebookEntries = new ArrayList();
        try {

            /* gradebook item */
            int gradebookItemID  = -1;
            int gradebookScoreID = -1;
            int position = -1;
            int weight   = -1;
            int maxScore = -1;
            int minScore = -1;
            int type     = -1;
            int mean     = -1;
            int median   = -1;

            String name  =  null;
            String association = null;
            String feedback    = null;

            /* gradebook score */
            int score        = -1;
            int origScore    = -1;
            int learnerID    = -1;
            int offeringID   = -1;
            String userName  = null;
            String firstName = null;
            String lastName  = null;
            String email     = null;
            long roleId      = -1; // needed to create user object
            List gbScoreList = null;

            /* temp Variables */
            int prevGBitemID     = -1;
            User user = null;
            GradebookItem gbItem      = null;
            GradebookScore gbScore = null;
            List gradebook = null;
            List tempList = null;
            boolean firstPass = true;
            
            // Loop & Load
            while (rs.next()) {
                /* Getting gradebook entries from the rs */
                gradebookItemID  = rs.getInt("gradebook_item_id");
                gradebookScoreID = rs.getInt("gradebook_score_id");
                position         = rs.getInt("pos");
                weight           = rs.getInt("weight");
                type             = rs.getInt("type");
                name             = rs.getString("itemname");
                offeringID       = rs.getInt("offering_id");
                mean             = rs.getInt("mean");
                median           = rs.getInt("median");
                association      = rs.getString("association");
                feedback         = rs.getString("feedback");
                maxScore = rs.getInt("max_score");
                minScore = rs.getInt("min_score");

                /* Getting gradebookscores from the rs */
                userName = rs.getString("user_name");

// XXX May want to get a Map of All users in the offering
// instead and retrieve those instead of going to the
// the user factory and having this try-catch block here
// in the code. (06/06/03) -H2
                try {
                    user = UserFactory.getUser(userName);
                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR,"User with the username " 
                        + userName + " is not currently found in the system.");
                    continue;
                }
                /* end */
                
                score     = rs.getInt("score");
                origScore = rs.getInt("original_score");

                /* Building a GradebookScore object */
                gbScore = new GradebookScoreImpl (
                                    gradebookScoreID,
                                    gradebookItemID,
                                    userName,
                                    score,
                                    origScore,
                                    user);

                if (gradebookItemID != prevGBitemID) {
                    /* need to make a bunch of new lists */
                    gbScoreList = new ArrayList();

                    gbItem = new GradebookItemImpl(
                                    gradebookItemID,
                                    offeringID,
                                    position,
                                    weight,
                                    type,
                                    maxScore,
                                    minScore,
                                    mean,
                                    median,
                                    name,
                                    feedback,
                                    association,
                                    gbScoreList);
                    /* Adding a gradebook item for the gradebook */
                    gradebookEntries.add(gbItem);
                }

                prevGBitemID = gradebookItemID;
                gbScoreList.add(gbScore);
            } // end loop

            // Perform sorting and paging on gradebook scores
            __sortAndPageGradebookItems(gradebookEntries, page);
            
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return gradebookEntries;
    } // end __buildGradebookObjects
    
    private static ISortMode[] __getGradebookEntrySorts() {
        List sortModes = new ArrayList();

        // Sort by pos.
        sortModes.add(new FDbSortMode("GI.POS ASC"));

        // Sort by last name.
        //sortModes.add(new FDbSortMode("UPPER(UPD.LAST_NAME) ASC"));
        //sortModes.add(new FDbSortMode("UPPER(UPD.FIRST_NAME) ASC"));

        // Sort by gradebook score id.
        sortModes.add(new FDbSortMode("GS.GRADEBOOK_SCORE_ID ASC"));

        return (ISortMode[]) sortModes.toArray(new ISortMode[0]);
    } // end getGradebookEntrySorts
    
    private static final String insertGradebookItemSQL =
    "INSERT into gradebook_item (max_score, min_score, weight, name, pos, association, feedback, offering_id, type) values (?,?,?,?,?,?,?,?,?)";
    
    private static final String updateGradebookItemSQL =
    "UPDATE gradebook_item set max_score = ?, min_score = ?, weight = ?, name = ?, pos = ?, association = ?, feedback = ? where gradebook_item_id = ?";

    public static int save(GradebookItem gbItem, int newPosition, Connection conn) {
        return __dosaveGradebookItem(
                        gbItem.getId(),
                        gbItem.getOfferingId(),
                        gbItem.getType(),
                        gbItem.getWeight(),
                        newPosition,
                        gbItem.getPosition(),
                        gbItem.getName(),
                        gbItem.maximumScore(),
                        gbItem.minimumScore(),
                        gbItem.displayFeedback(),
                        gbItem.getAssociation(),
                        conn);
    }

    /**
     * Inserts a new gradebook item entry to the database
     * @param conn - a database Connection.
     * @return <code>in</code> - returns a status code of success.
     */
    public static int create (
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
        // XXX should return a GradebookItem
        return __dosaveGradebookItem(
                        -1, 
                        (int) offering.getId(), 
                        type, 
                        weight, 
                        position,
                        -1, 
                        name,
                        maxScore, 
                        minScore, 
                        feedback, 
                        association, 
                        conn);
    }

    public static boolean save(
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

        int retVal = __dosaveGradebookItem(
                        gbItemID,
                        (int) offering.getId(),
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
        return (retVal >= 0);
    }

    /** Deletes a gradebookItem and its children data */
    public static boolean remove (
                        GradebookItem gbItem, 
                        Offering offering, 
                        Connection conn) {
        return __removeFromDB(
                        (int) offering.getId(), 
                        gbItem.getId(), 
                        gbItem.getPosition(), 
                        conn);
    }

    public static boolean remove (            
                        Offering offering, 
                        int gbItemId, 
                        int currentPosition, 
                        Connection conn) {
        return __removeFromDB((int) offering.getId(), gbItemId, currentPosition, conn);
    }
    
    private static final String getGBScoreIDFromGBItemIDSQL = 
        "SELECT GS.gradebook_score_id from gradebook_score GS where GS.gradebook_item_id = ?";
    
    private static final String getGBItemActivationsSQL =
        "SELECT GI.gradebook_item_id, GI.offering_id, GI.pos, GI.weight, GI.max_score, GI.min_score, GI.name, GI.type, GI.association, GI.mean, GI.median, GI.feedback, GIA.activation_id FROM gradebook_item GI, gbitem_activation GIA WHERE GIA.gradebook_item_id = ? AND GIA.gradebook_item_id = GI.gradebook_item_id";
    
    private static final String deleteGBItemSQL =
        "DELETE from gradebook_item where gradebook_item_id = ?";
   
    public static boolean __removeFromDB(
                         int offeringId,
                         int gbItemID,
                         int currentPosition,
                         Connection conn) {

        boolean success = false;

        PreparedStatement pstmt  = null;
        ResultSet rs = null;

        LogService.instance().log(LogService.INFO,
            "GradebookItemFactory.deleteGradebookItem:gbItemID= " + gbItemID);
        LogService.instance().log(LogService.INFO,
            "GradebookItemFactory.deleteGradebookItem:offeringID= " + offeringId);
        LogService.instance().log(LogService.INFO,
            "GradebookItemFactory.deleteGradebookItem:currentPosition= " + currentPosition);

        try {
            int i = 0;

            /* delete relationships to gradebook_item, We are deleting 
             * the students scores */
            pstmt = conn.prepareStatement(getGBScoreIDFromGBItemIDSQL);
            pstmt.setInt(++i, gbItemID);
            rs = pstmt.executeQuery();

            int gbScoreID = -1;

            while (rs.next()) {
                gbScoreID = rs.getInt("gradebook_score_id");
                // Deleting Gradebook Score items
                GradebookScoreFactory.remove(gbScoreID, conn);
            }

            if (rs != null) {
                rs.close();
                rs = null;
            }

            pstmt.close();
            pstmt = null;

            i = 0;

            pstmt = conn.prepareStatement(getGBItemActivationsSQL);
            pstmt.setInt(++i, gbItemID);
            rs = pstmt.executeQuery();

            // Deleting Activations
            int activationID = -1;
            while (rs.next()) {
                activationID = rs.getInt("activation_id");
                GradebookActivationFactory.remove(activationID, conn);
            }

	    rs.close();
	    rs = null;
            pstmt.close();
            pstmt = null;

            /* deleting the gradebook item */
            i = 0;
            pstmt = conn.prepareStatement(deleteGBItemSQL);
            pstmt.setInt(++i, gbItemID);
            pstmt.executeUpdate();

            /* replacing the position */
            success = __doSwitchGradebookEntityPositions(
                            currentPosition, /* old pos */
                            - 1,              /* new pos */
                            offeringId,
                            conn);
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

    public static boolean removeAll(Offering offering, Connection conn) {
        return removeAll((int) offering.getId(), conn);
    }

    private static final String getAllGBItemIdSQL =
        "select gradebook_item_id, pos from gradebook_item where offering_id = ?";
        
    public static boolean removeAll(int offeringId, Connection conn) {
        boolean success = false;

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LogService.instance().log(LogService.INFO,
            "GradebookItemFactory.deleteGradebookItems:offeringID= " + offeringId);

        try {
    
            int i = 0;

            pstmt = conn.prepareStatement(getAllGBItemIdSQL);
            pstmt.setInt(++i, offeringId);

            rs = pstmt.executeQuery();

            int gbItemId = -1;
            int position = -1;
            while(rs.next() ) {
                gbItemId = rs.getInt("gradebook_item_id");
                position = rs.getInt("pos");
                __removeFromDB(offeringId, gbItemId, position, conn);   
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
    
    private static final String selectGradebookItemSQL =
         "SELECT gradebook_item_id from gradebook_item where offering_id = ? and pos = ?";  
         
    /**
     * Saves a gradebook item entry to the database
     * @param conn - a database Connection.
     * @return <code>in</code> - returns a status code of success.
     */
    private static int __dosaveGradebookItem(
                                    int gbItemID,
                                    int offeringID,
                                    int type,
                                    int weight,
                                    int newPosition,
                                    int oldPosition,
                                    String name,
                                    int maxScore,
                                    int minScore,
                                    String feedback,
                                    String association,
                                    Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LogService.instance().log(LogService.INFO,
            "gradebookService.dosaveGradebookItem:NEW_POSITION" + newPosition);

        LogService.instance().log(LogService.INFO,
            "gradebookService.dosaveGradebookItem:OLD_POSITION" + oldPosition);

        try {
            __doSwitchGradebookEntityPositions(oldPosition, newPosition, offeringID, conn);

            boolean isNew = gbItemID == -1 ? true : false;

            int i = 0;
            
            if (isNew) {
                /* insert */
                pstmt = conn.prepareStatement(insertGradebookItemSQL);
            } else {
                /* update */
                pstmt = conn.prepareStatement(updateGradebookItemSQL);
            }

            if (type == 1) {
                /* Online Assessment */
                pstmt.setNull(++i, Types.INTEGER);

                /* max_score */
                pstmt.setNull(++i, Types.INTEGER);

                /* min_score */

            } else {
                /* Other Gradebook Item */
                pstmt.setInt   (++i, maxScore);
                pstmt.setInt   (++i, minScore);
            }
            pstmt.setInt(++i, weight);
            pstmt.setString(++i, name);
            pstmt.setInt(++i, newPosition);
            pstmt.setString(++i, association);
            pstmt.setString(++i, feedback);

            if (!isNew) {
                pstmt.setInt(++i, gbItemID);
            } else {
                pstmt.setInt(++i, offeringID);
                pstmt.setInt(++i, type);
            }

            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            
            if (isNew) {
                pstmt = conn.prepareStatement(selectGradebookItemSQL);
                i = 0;

                pstmt.setInt(++i, offeringID);
                pstmt.setInt(++i, newPosition);
            
                rs = pstmt.executeQuery();
                
                if (rs.next() ) {
                    gbItemID = rs.getInt("gradebook_item_id");
                }
                GradebookScoreFactory.createAllUsers(gbItemID, conn);
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
        return gbItemID;
    }

    /* insert a new gradebook item */
    private static final String incrementGBEPositionSQL =
    "UPDATE gradebook_item set pos = (pos+1) where pos >= ? and offering_id = ?";

    /* update a gradebook item */
    private static final String decrementGBEPositionSQL =
    "UPDATE gradebook_item set pos = (pos-1) where pos > ? and offering_id = ?";

    /* Moving a gbe position to a higher position */
    private static final String switchIncreaseGBEPostionSQL =
    "UPDATE gradebook_item set pos = (pos-1) where pos > ? and pos <= ? and offering_id = ?";

    private static final String switchDecreaseGBEPostionSQL =
    "UPDATE gradebook_item set pos = (pos+1) where pos < ? and pos >= ? and offering_id = ?";
    
    /**
     * Swicthes the order of the gradebook item entry in the database
     * @param conn - a database Connection.
     * @return <code>in</code> - returns a status code of success.
     */
    private static boolean __doSwitchGradebookEntityPositions(
                                int oldPosition,
                                int newPosition,
                                int offeringID,
                                Connection conn) {

        boolean success = false;
        PreparedStatement pstmt = null;

        try {
            int i = 0;
            if (oldPosition == -1 && newPosition != -1) {
                /* insert new column */
                pstmt = conn.prepareStatement(incrementGBEPositionSQL);
                pstmt.setInt(++i, newPosition);

            } else if (oldPosition != -1 && newPosition == -1) {
                /* deleting a column */
                pstmt = conn.prepareStatement(decrementGBEPositionSQL);
                pstmt.setInt(++i, oldPosition);

            } else if (oldPosition == newPosition) {
                /* do nothing, no change */
                return true;
            } else if (oldPosition > newPosition) {
                /* decrement decrement positions */
                pstmt = conn.prepareStatement(switchDecreaseGBEPostionSQL);
                pstmt.setInt(++i, oldPosition);
                pstmt.setInt(++i, newPosition);
            } else if (oldPosition < newPosition) {
                /* increment current positions */
                pstmt = conn.prepareStatement(switchIncreaseGBEPostionSQL);
                pstmt.setInt(++i, oldPosition);
                pstmt.setInt(++i, newPosition);
            }
            pstmt.setInt(++i, offeringID);
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
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return success;
    }

    private static void __sortAndPageGradebookItem(
                                            GradebookItem gbItem, 
                                            IPageMode page) {
        if (gbItem != null) {
            try {
                // Get scores.
                List gbScores = gbItem.getGradebookScores();

                // First sort gradebook scores.
                Collections.sort(gbScores, __fullNameAscComparator);

                if (page != null) {
                    // Now do the paging.
                    int pgSize = ((GbPageMode)page).getPageSize();
                    int pgNum = ((GbPageMode)page).getPageNumber();
                    int fstEntry = (pgSize * (pgNum - 1)) + 1;
                    int lstEntry = pgSize * pgNum;    // will be 0 if "View All"

                    List rslts = new ArrayList();
                    int allEntriesCount = gbScores.size();
                    int entryCount = 0;
                    for (int i = 0; i < allEntriesCount; i++) {
                        entryCount++;
                        if (entryCount < fstEntry) continue;
                        if (lstEntry != 0 && entryCount > lstEntry) continue;
                        rslts.add(gbScores.get(i));
                    }

                    // Reset gradebook scores.
                    gbItem.setGradebookScores(rslts);

                    // Set the pageCount.
                    if (page.getPageCount() < allEntriesCount) {
                        int pgCnt = 1;  // Default.
                        if (pgSize != 0) {
                            pgCnt = (allEntriesCount / pgSize)
                                + ((allEntriesCount % pgSize == 0) ? 0:1);
                        }
                        if (pgCnt == 0) pgCnt++; // Must always be a page, even if empty.
                        ((GbPageMode)page).setPageCount(pgCnt);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end outer if
    } // end __sortAndPageGradebookItem

    private static void __sortAndPageGradebookItems(
                                                List gbItems, 
                                                IPageMode page) {
        for (int i = 0; i < gbItems.size(); i++) {
            GradebookItem gbItem = (GradebookItem) gbItems.get(i);
            __sortAndPageGradebookItem(gbItem, page);
        }
    } // end __sortAndPageGradebookItems

    private static final int ENROLLED = EnrollmentStatus.ENROLLED.toInt();
    
    /* Getting base gradebook sort modes for gradebook entries */
    private static final ISortMode[] __entrySorts = __getGradebookEntrySorts();

    private static final FullnameAscComparator __fullNameAscComparator = 
        new FullnameAscComparator();

} // end GradebookItemFactory
