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
package net.unicon.academus.domain.assessment;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.common.service.activation.ActivationService;
import net.unicon.portal.common.service.activation.ActivationServiceFactory;

/**
 * <p>
 * 
 * </p>
 */
public final class ActivationInstanceFactory {
    
    private ActivationInstanceFactory () {}
    
    /**
     * <p>
     * Returns a list of <code>ActivationInstance</code>(s) based
     * on what assessment the user in an offering can taken with 
     * the given activation constraints. This list doesn not contain
     * activations that the user can take, suchas expired activations.
     * to many retries, etc.
     * </p><p>
     *
     * @param a String with the username to find activations for.
     * @param a int with the offering/groud id.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static List getUserActivations(String username, int offeringId, Connection conn) {
        List rtnList = new ArrayList();
        try {
            ActivationService actService = 
                                ActivationServiceFactory.getService();
            List activations = actService.getUserActivations(
                                                offeringId, 
                                                username, 
                                                actService.ONLINE_ASSESSMENT, 
                                                conn);

            Activation     tempAct          = null;
            UserActivation tempUserAct      = null;
            ActivationInstance tempActInst  = null;
            
            for (int ix=0; ix < activations.size(); ++ix) {
                tempAct = (Activation) activations.get(ix);
                tempUserAct = __getUserActivation(tempAct.getActivationID(), username, conn);

                // Checking to see the user has anymore attempts left.
                tempActInst = new ActivationInstance(tempAct, tempUserAct);
                if (tempActInst.canAttempt()) {
                    rtnList.add(tempActInst);
                }
            }
        } catch (SQLException se) {
           se.printStackTrace(); 
        } catch (Exception e) {
           e.printStackTrace();
        }
        return rtnList;
    }

    /**
     * <p>
     * Returns a list of <code>ActivationInstance</code>(s) based
     * on what assessment the user can taken with the given activation
     * constraints.
     * </p><p>
     *
     * @param a String with the username to find activations for.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static List getUserActivations(String username, Connection conn) {
        List rtnList = new ArrayList();
        // YYY Need to implement in the future, there are no requirements
        // that needs this method.
        return rtnList;
    }
    
    /**
     * <p>
     * Returns a list of <code>ActivationInstance</code>(s) based
     * on what assessment the user can taken with the given activation
     * constraints.
     * </p><p>
     *
     * @param a int with a specific activation id.
     * @param a String with the username of the user.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static ActivationInstance getActivationInstance(int activationId, String username, Connection conn) {
        ActivationInstance rtnActivationInstance = null;
        try {
            ActivationService actService = 
                                ActivationServiceFactory.getService();
            
            Activation activation =  actService.getActivation(activationId, username, conn);
            UserActivation userActivation = __getUserActivation(activationId, username, conn);

            rtnActivationInstance = new ActivationInstance(activation, userActivation);
        } catch (SQLException se) {
            System.out.println("AssessmentActivationServiceImpl.getActivationInstance ERROR: ");
            se.printStackTrace();
        } catch (Exception e) {
        }
        return rtnActivationInstance;
        
    }

    /**
     * <p>
     * Returns a list of <code>UserActivation</code>(s) based
     * on what assessment the user has taken with the given activation
     * constraints.
     * </p><p>
     *
     * @param a int with a specific activation id.
     * @param a String with the username of the user.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    protected static UserActivation __getUserActivation(int activationId, String username, Connection conn) {
        UserActivation rtnUserActivation =  null;
       
        int attemptCount = 0;

        if (activationId > 0 && username != null) {
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement(ATTEMPT_COUNT_SQL);

                int i = 0;
                pstmt.setInt(++i, activationId);
                pstmt.setString(++i, username);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    attemptCount = rs.getInt("num_count");        
                }

                if (rs != null) {
                    rs.close();
                    rs = null;
                }

                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }

                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (SQLException se2) {
                    se2.printStackTrace();
                }
            }
        }
        rtnUserActivation = new UserActivation(attemptCount, username);
        
        return rtnUserActivation;
    }

    protected static final String ATTEMPT_COUNT_SQL = "select count(count_attempt) as num_count from assessment_result where count_attempt = 1 and activation_Id = ? and user_name = ?";
}
