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

package net.unicon.academus.domain.lms.ensemble;

// JDK API
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;

// Academus SDK
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogServiceFactory;


import net.unicon.portal.util.db.AcademusDBUtil;

/**
 * Responsible for manipulating additional offering data
 * specific to Ensemble.
 */
public class EnsembleService {

    private static ILogService logService = LogServiceFactory.instance();

    // Ensemble Service SQL
    private static final String GET_OFFERING_DATA =
            "SELECT PUBLISHED, HAS_BUY_NOW FROM " +
                "ADJUNCT_OFFERING_DATA WHERE OFFERING_ID = ?";
    private static final String GET_TOPIC_OFFERING_DATA =
            "SELECT TOPIC_OFFERING.OFFERING_ID, PUBLISHED, HAS_BUY_NOW FROM " +
                "ADJUNCT_OFFERING_DATA WHERE TOPIC_OFFERING.TOPIC_ID = ? AND " +
                    "TOPIC_OFFERING.OFFERING_ID=ADJUNCT_OFFERING_DATA.OFFERING_ID";
    private static final String CREATE_OFFERING_DATA =
            "INSERT INTO ADJUNCT_OFFERING_DATA (OFFERING_ID, PUBLISHED, " +
                "HAS_BUY_NOW) VALUES (?, ?, ?)";
    private static final String UPDATE_OFFERING_DATA =
            "UPDATE ADJUNCT_OFFERING_DATA SET PUBLISHED = ?, " +
                "HAS_BUY_NOW = ? WHERE OFFERING_ID = ?";
    private static final String DELETE_OFFERING_DATA =
            "DELETE FROM ADJUNCT_OFFERING_DATA WHERE OFFERING_ID = ?";

    /**
     * Creates an adjunct offering data entry for the
     * specified offering.
     * @param offeringId Specifies the offering for which
     * the adjunct data entry will be created.
     * @param offeringData The adjunct offering data to be
     * created.
     * @throws Exception
     */
    public static void createAdjunctOfferingData (
            long offeringId, AdjunctOfferingData offeringData)
                    throws Exception{

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(CREATE_OFFERING_DATA);

            ps.setLong(1, offeringId);
            ps.setBoolean(2, offeringData.isPublished());
            ps.setBoolean(3, offeringData.hasBuyNowEnabled());
            ps.executeUpdate();

        } catch (SQLException sqle) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while creating ");
            errorMsg.append("an adjuct offering data entry with id \"");
            errorMsg.append(offeringId);
            errorMsg.append("\".");
            logService.log(ILogService.ERROR,errorMsg.toString(), sqle);

            throw new Exception(errorMsg.toString(), sqle);

        } finally {
            try {
                if(ps != null) ps.close();
                if(conn!=null) conn.close();
            } catch (Exception e) {
                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                logService.log(ILogService.ERROR,errorMsg.toString(), e);
            }
        }
    }

    /**
     * Updates an adjunct offering data entry for the
     * specified offering.
     * @param offeringId Specifies the offering for which
     * the adjunct data entry will be updated.
     * @param offeringData The adjunct offering data to be
     * updated.
     * @throws Exception
     */
    public static void updateAdjunctOfferingData (
            long offeringId, AdjunctOfferingData offeringData)
                    throws Exception{

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(UPDATE_OFFERING_DATA);

            ps.setBoolean(1, offeringData.isPublished());
            ps.setBoolean(2, offeringData.hasBuyNowEnabled());
            ps.setLong(3, offeringId);
            ps.executeUpdate();

        } catch (SQLException sqle) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while updating ");
            errorMsg.append("adjunct offering data with id \"");
            errorMsg.append(offeringId);
            errorMsg.append("\".");
            logService.log(ILogService.ERROR,errorMsg.toString(), sqle);

            throw new Exception(errorMsg.toString(), sqle);

        } finally {
            try {
                if(ps != null) ps.close();
                if(conn!=null) conn.close();
            } catch (Exception e) {
                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                logService.log(ILogService.ERROR,errorMsg.toString(), e);
            }
        }
    }


    /**
     * Returns the adjunct offering data entry for the
     * specified offering.
     * @param offeringId Specifies the offering for which
     * the adjunct data entry will be retrieved.
     * @return An adjunct offering data entry.
     * @throws Exception
     */
    public static AdjunctOfferingData getAdjunctOfferingData (
            long offeringId) throws Exception {

        AdjunctOfferingData offeringData = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(GET_OFFERING_DATA);
            ps.setLong(1, offeringId);
            rs = ps.executeQuery();

            // Determine if any course details were found
            if (rs.next()) {

                // Build course details instance
                boolean published = rs.getBoolean("published");
                boolean hasBuyNowEnabled = rs.getBoolean("has_buy_now");

                offeringData =
                    new AdjunctOfferingData(
                            offeringId, published, hasBuyNowEnabled);
            } else {

                offeringData = new AdjunctOfferingData(
                        offeringId, false, false);
            }
        } catch (SQLException sqle) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while retrieving ");
            errorMsg.append("adjunct offering data with id \"");
            errorMsg.append(offeringId);
            errorMsg.append("\".");
            logService.log(ILogService.ERROR,errorMsg.toString(), sqle);

            throw new Exception(errorMsg.toString(), sqle);

        } finally {
            try {
                if(rs !=null) rs.close();
                if(ps != null) ps.close();
                if(conn!=null) conn.close();
            } catch (Exception e) {
                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                logService.log(ILogService.ERROR,errorMsg.toString(), e);
            }
        }

        return offeringData;
    }

    /**
     * Deletes the adjunct offering data entry for the
     * specified offering.
     * @param offeringId Specifies the offering for which
     * the adjunct data entry will be deleted.
     * @throws Exception
     */
    public static void deleteAdjunctOfferingData (long offeringId)
    throws Exception {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(DELETE_OFFERING_DATA);
            ps.setLong(1, offeringId);
            ps.executeUpdate();

        } catch (SQLException sqle) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while ");
            errorMsg.append("deleting adjuct offering data with id \"");
            errorMsg.append(offeringId);
            errorMsg.append("\".");
            logService.log(ILogService.ERROR,errorMsg.toString(), sqle);

            throw new Exception(errorMsg.toString(), sqle);

        } finally {
            try {
                if(ps != null) ps.close();
                if(conn!=null) conn.close();
            } catch (Exception e) {
                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                logService.log(ILogService.ERROR,errorMsg.toString(), e);
            }
        }
    }

    /**
     * Retrieves adjuct offering data for all the offerings
     * under the specified topic.
     * @param topicId Only offering data of offerings under
     * this topic will be retrieved.
     * @return All offering data of all the offerings under
     * the specified topic.
     * @throws Exception
     */
    public static AdjunctOfferingData[] getOfferingData(long topicId)
    throws Exception {

        AdjunctOfferingData[] offeringDataSet = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List offeringDataList = new LinkedList();

        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(GET_TOPIC_OFFERING_DATA);
            ps.setLong(1, topicId);
            rs = ps.executeQuery();

            // Retrieve offering data
            while (rs.next()) {

                long offeringId = rs.getLong("offering_id");
                boolean published = rs.getBoolean("published");
                boolean hasBuyNowEnabled = rs.getBoolean("has_buy_now");

                AdjunctOfferingData offeringData =
                        new AdjunctOfferingData(
                                offeringId, published, hasBuyNowEnabled);

                offeringDataList.add(offeringData);
            }
        } catch (SQLException sqle) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while ");
            errorMsg.append("retrieving adjunct offering data ");
            errorMsg.append("for all offerings under topic with id \"");
            errorMsg.append(topicId);
            errorMsg.append("\".");
            logService.log(ILogService.ERROR,errorMsg.toString(), sqle);

            throw new Exception(errorMsg.toString(), sqle);

        } finally {
            try {
                if(rs !=null) rs.close();

                if(conn!=null) conn.close();
            } catch (Exception e) {

                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                logService.log(ILogService.ERROR,errorMsg.toString(), e);
            }
        }

        offeringDataSet =
                (AdjunctOfferingData[]) offeringDataList.toArray(
                        new AdjunctOfferingData[0]);

        return offeringDataSet;
    }
}
