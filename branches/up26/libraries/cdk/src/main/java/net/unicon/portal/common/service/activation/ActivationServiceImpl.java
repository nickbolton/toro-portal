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

package net.unicon.portal.common.service.activation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.SQLException;
import java.sql.Types;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import net.unicon.academus.domain.lms.*;
import net.unicon.portal.common.service.file.FileServiceImpl;
import net.unicon.portal.common.properties.*;
import net.unicon.sdk.properties.*;
import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.activation.ActivationService;
import net.unicon.sdk.time.*;
import net.unicon.sdk.FactoryCreateException;


public class ActivationServiceImpl implements ActivationService {

//  the old way of getting dates
//    private static final String DB_CURRENT_TIME = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.db.DBcurrDate");

    protected final static int   ACTIVE = 1;
    protected final static int DEACTIVE = 0;

    protected static final File uploadedActivationFileDir = new File(UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.gradebook.uploadedActivationFileDir"));

    protected final static String getActivationByIDSQL =
    (new StringBuffer().append("SELECT activation_id, offering_id, type, ")
    .append("status, start_date, end_date, duration, activation_date ")
    .append("from activation where activation_id = ? AND end_date > ?")
    .append(" AND status = 1")).toString();

    protected final static String getActivationIdSQL =
    (new StringBuffer().append("SELECT activation_id, offering_id, type, ")
    .append("status, start_date, end_date, duration, activation_date ")
    .append("from activation where activation_id = ")
    .append("(SELECT MAX(activation_id) from activation where ")
    .append("offering_id = ? and type= ? and status =? and ")
    .append("start_date = ? and end_date = ?)")).toString();


    //    "SELECT MAX(activation_id) AS activation_id, offering_id, type,
    //    status, start_date, start_time, end_date, end_time, duration,
    //    activation_date from activation where offering_id = ? and type= ?
    //    and status =? and start_date =? and start_time = ? and end_date =?
    //    and end_time = ? GROUP BY activation_id, offering_id, type, status,
    //    start_date, start_time, end_date, end_time, duration, activation_date";


    protected final static String getActivationParamsSQL =
    "SELECT activation_param_name, activation_param_value from activation_param where activation_id = ?";

    protected final static String getUserActivationsSQL =
    (new StringBuffer().append("SELECT A.activation_id, A.offering_id, ")
    .append("A.type, A.status, A.start_date, A.end_date, A.duration, ")
    .append("A.activation_date, UA.user_name from activation A, ")
    .append("user_activation UA where UA.user_name = ? and ")
    .append("A.offering_id = ? and UA.activation_id = A.activation_id ")
    .append("AND A.end_date > ? ")
    .append(" AND A.status = 1")).toString();

    protected final static String getUserTypeActivationsSQL =
    (new StringBuffer().append("SELECT A.activation_id, A.offering_id, ")
    .append("A.type, A.status, A.start_date, A.end_date, A.duration, ")
    .append("A.activation_date, UA.user_name from activation A, ")
    .append("user_activation UA where A.type = ? and UA.user_name = ? and ")
    .append("A.offering_id = ? and UA.activation_id = A.activation_id ")
    .append("AND A.end_date > ? ")
    .append(" AND A.status = 1")).toString();


    protected final static String getActivationByUserSQL =
    (new StringBuffer().append("SELECT A.activation_id, A.offering_id, ")
    .append("A.type, A.status, A.start_date, A.end_date, A.duration, ")
    .append("A.activation_date, UA.user_name from activation A, ")
    .append("user_activation UA where A.activation_id = ? AND ")
    .append("A.activation_id = UA.activation_id and UA.user_name = ? ")
    .append("AND A.end_date > ?")
    .append(" AND A.status = 1")).toString();

    protected final static String getActivationForUsers =
    "SELECT UA.user_name from user_activation UA where activation_id = ?";

    protected final static String getOfferingActivationsSQL =
    (new StringBuffer().append("SELECT activation_id, offering_id, type, ")
    .append("status, start_date, end_date, duration, activation_date ")
    .append("from activation where offering_id = ? and end_date > ?")
    .append(" and status = 1")).toString();

    protected final static String getOfferingActivationTypeSQL =
    (new StringBuffer().append("SELECT activation_id, offering_id, type, ")
    .append("status, start_date, end_date, duration, activation_date ")
    .append("from activation where offering_id = ? AND type = ? AND ")
    .append("end_date > ?")
    .append(" AND status = 1")).toString();

    protected final static String deactivateActivationSQL =
    "UPDATE activation set status = ? where activation_id = ?";

    protected final static String insertActivationSQL =
    (new StringBuffer()
    .append("INSERT into activation (offering_id, type, status, ")
    .append("start_date, end_date, duration, activation_date) ")
    .append("values (?,?,?,?,?,?,?)")).toString();

    protected final static String insertActivationAttSQL =
    "INSERT into activation_param (activation_id, activation_param_name, activation_param_value) values (?,?,?)";

    protected final static String insertUserActivationSQL =
    "INSERT into user_activation (activation_id, user_name) values (?,?)";

    protected final static String deleteActivationSQL =
    "DELETE from activation where activation_id = ?";

    protected final static String deleteUserActivationSQL =
    "DELETE from user_activation where activation_id = ?";

    protected final static String deleteActivationParamSQL =
    "DELETE from activation_param where activation_id = ?";


    private static final String INSERT_EVENTS_FOR_ACTIVATION =
    "INSERT INTO activation_param (activation_id, activation_param_name, activation_param_value) VALUES(?,?,?)";
                                                                                                 
    private static final String DELETE_EVENTS_FOR_ACTIVATION =
    "DELETE FROM activation_param WHERE activation_id = ? and (activation_param_name like \'%Event%\' or activation_param_name=\'numEvents\')";
 


    /** Empty Constructor */
    public ActivationServiceImpl() { }

    
    /**
     * Get an Activation object based on the activation ID
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     * @return <code>Activation</code> - activation object based on activation ID
     */
    public Activation getActivation(int activationID, Connection conn)
    		throws SQLException {

        Activation activation   = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(getActivationByIDSQL);
            int i = 0;

            pstmt.setInt(++i, activationID);
            pstmt.setTimestamp(++i, ts.getTimestamp());
            rs = pstmt.executeQuery();

            /* building activation object */
            if (rs.next()) {
                activation = buildActivation(rs, conn);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
            if (activation == null) {
                return null;
            }

            i = 0;
            pstmt = conn.prepareStatement(getActivationForUsers);
            pstmt.setInt(++i, activationID);
            rs = pstmt.executeQuery();

            /* building user list */
            activation.setUsernames(buildUserList(rs));
            Offering offering = OfferingFactory.getOffering(
            		activation.getOfferingID());

            if (activation.getUsernames().size() <
            		Memberships.getMembers(offering).size()) {
                activation.setForAllUsers(false);
            } else {
                activation.setForAllUsers(true);
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activation;
    }


    /**
     * Get an Activation object based on the activation ID and if its activate for a particuluar user.
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     * @param <code>String</code> - a user name
     * @return <code>Activation</code> - activation object based on activation ID
     */
    public Activation getActivation(int activationID, String username, Connection conn)
		throws SQLException {

        Activation activation   = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(getActivationByUserSQL);
            int i = 0;

            pstmt.setInt(++i, activationID);
            pstmt.setString(++i, username);
            pstmt.setTimestamp(++i, ts.getTimestamp());

            rs = pstmt.executeQuery();

            /* building activation object */
            if (rs.next()) {
                activation = buildActivation(rs, conn);
            }
            
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

            if (activation == null) {
                return null;
            }

            i = 0;
            pstmt = conn.prepareStatement(getActivationForUsers);
            pstmt.setInt(++i, activationID);

            rs = pstmt.executeQuery();

            /* building user list */
            activation.setUsernames(buildUserList(rs));
            Offering offering = OfferingFactory.getOffering(
                                              activation.getOfferingID());

            
            if (activation.getUsernames().size() <
            		Memberships.getMembers(offering).size()) {
                activation.setForAllUsers(false);
            } else {
                activation.setForAllUsers(true);
            }

            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activation;

    }

    
    /**
     * Returns all the Activations for an Offering in a List.
     * @param <code>int</code> offeringID
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the offering ID
     */
    public List getActivations(int offeringID, Connection conn)
		throws SQLException {

        List activations = new ArrayList();
        PreparedStatement pstmt = null;

        ResultSet rs = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(getOfferingActivationsSQL);
            int i = 0;

            pstmt.setInt(++i, offeringID);
            pstmt.setTimestamp(++i, ts.getTimestamp());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                activations.add(buildActivation(rs, conn));
            }

            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activations;

    }

    
    public List getActivations(int offeringID, String type, Connection conn)
		throws SQLException {

        List activations = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(getOfferingActivationTypeSQL);
            int i = 0;

            pstmt.setInt(++i, offeringID);
            pstmt.setString(++i, type);
            pstmt.setTimestamp(++i, ts.getTimestamp());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                activations.add(buildActivation(rs, conn));
            }

            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activations;

    }

    
    /**
     * Returns all the Activations for an Offering in a List.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the offering
     */
    public List getActivations(
		    Offering offering,
		    String type,
		    Connection conn)
		    throws SQLException {
        return getActivations((int) offering.getId(), type, conn);
    }

    
    /**
     * Returns all the Activations for an Offering in a List.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the offering
     */
    public List getActivations(Offering offering, Connection conn)
			throws SQLException {
        return getActivations((int) offering.getId(), conn);
    }

    
    /**
     * Returns all the Activations for an user in an offering in a List.
     * @param <code>int</code> offeringID
     * @param <code>String</code> - username
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering ID
     */
    public List getUserActivations(int offeringID, String username, String type, Connection conn)
			throws SQLException {
        return _getUserActivation(offeringID, username, type, conn);
    }


    public List getUserActivations(Offering offering, String username, String type, Connection conn) 
			throws SQLException {
    	return _getUserActivation((int)offering.getId(), username, type, conn);
    }


    /**
     * Returns all the Activations for an user in an offering in a List.
     * @param <code>int</code> offeringID
     * @param <code>String</code> - username
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering ID
     */
    public List getUserActivations(int offeringID, String username, Connection conn) 
			throws SQLException {
        return _getUserActivation(offeringID, username, null, conn);
    }


    protected List _getUserActivation(int offeringID, String username, String type, Connection conn) 
			throws SQLException {

    	List activations = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            TimeService ts = TimeServiceFactory.getService();

            int i = 0;
            if (type != null) {
                pstmt = conn.prepareStatement(getUserTypeActivationsSQL);
                pstmt.setString(++i, type);
            } else {
                pstmt = conn.prepareStatement(getUserActivationsSQL);
            }

            pstmt.setString(++i, username);
            pstmt.setInt(++i, offeringID);
            pstmt.setTimestamp(++i, ts.getTimestamp());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                activations.add(buildActivation(rs, conn));
            }

            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activations;

    }


    /**
     * Returns all the Activations for an user in an offering in a List.
     * @param <code>net.unicon.portal.domain.Offering</code> offering
     * @param <code>String</code> - username
     * @param <code>Connection</code> - a database connection
     * @return <code>List</code> - list of activation object based on the username and offering
     */
    public List getUserActivations(Offering offering, String username, Connection conn)
			throws SQLException {
        return getUserActivations((int) offering.getId(), username, conn);
    }


    /**
     * Deactivate an Activation
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     */
    public void deactivateActivation(int activationID, Connection conn)
			throws SQLException  {

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(deactivateActivationSQL);

            int i = 0;
            pstmt.setInt(++i, DEACTIVE);
            pstmt.setInt(++i, activationID);
            pstmt.executeUpdate();

        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }
    }


    /**
     * Delete/Remove persistance of an Activation
     * @param <code>int</code> activationID
     * @param <code>Connection</code> - a database connection
     */
    public void deleteActivation(int activationID, Connection conn)
			throws SQLException {

        PreparedStatement pstmt = null;

        try {

            pstmt = conn.prepareStatement(deleteActivationParamSQL);

            int i = 0;
            pstmt.setInt(++i, activationID);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            i = 0;
            pstmt = conn.prepareStatement(deleteUserActivationSQL);
            pstmt.setInt(++i, activationID);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            i = 0;
            pstmt = conn.prepareStatement(deleteActivationSQL);
            pstmt.setInt(++i, activationID);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }
    }


    public Activation createActivation(
		    int offeringID,
		    String type,
		    Date startDate,
		    Date endDate,
		    long startTime,
		    long endTime,
		    Map attributes,
		    List userList,
		    boolean allUsers,
		    Connection conn) throws SQLException {

    	Activation activation = null;

    	try {
            activation = insertActivation(
            offeringID,
            type,
            startDate,
            endDate,
            startTime,
            endTime,
            conn );

            activation.setForAllUsers(allUsers);

            /* building username list */
            if (userList != null && userList.size() > 0) {
                insertUserActivation (
                activation.getActivationID(),
                userList,
                conn);
            }

            activation.setUsernames(userList);

            if (attributes != null) {
                insertAttributes (
                activation.getActivationID(),
                attributes,
                conn);
            }

            activation.setAttributes(attributes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return activation;
    }


    protected Activation insertActivation ( int offeringID,
                                            String type,
                                            Date startDate,
                                            Date endDate,
                                            long startTime,
                                            long endTime,
                                            Connection conn) 
			throws SQLException, Exception {

        Activation activation = null;
        int activationID = -1;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try	{

            pstmt = conn.prepareStatement(insertActivationSQL);

            TimeService ts = TimeServiceFactory.getService();

            Date newStart = new Date();
            newStart.setTime(startTime);
            newStart.setMonth(startDate.getMonth());
            newStart.setDate(startDate.getDate());
            newStart.setYear(startDate.getYear());
   
            Date newEnd = new Date();
            newEnd.setTime(endTime);
            newEnd.setMonth(endDate.getMonth());
            newEnd.setDate(endDate.getDate());
            newEnd.setYear(endDate.getYear());

            int i = 0;
            pstmt.setInt(++i, offeringID);
            pstmt.setString(++i, type);
            pstmt.setInt(++i, ACTIVE);
            pstmt.setTimestamp(++i, new java.sql.Timestamp(newStart.getTime()));
            pstmt.setTimestamp(++i, new java.sql.Timestamp(newEnd.getTime()));
            pstmt.setInt(++i, -1);
            pstmt.setTimestamp(++i, ts.getTimestamp());

            //System.out.println(insertActivationSQL);
  
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            pstmt = conn.prepareStatement(getActivationIdSQL);

            //(new StringBuffer()
            //    .append("SELECT activation_id,offering_id,type,")
            //    .append("status, start_date,end_date,duration,activation_date")
            //    .append("from activation where activation_id = ")
            //    .append("(SELECT MAX(activation_id) from activation where ")
            //    .append("offering_id = ? and type= ? and status =? and ")
            //    .append("start_date = ? and end_date = ?)");

            i = 0;
            pstmt.setInt(++i, offeringID);
            pstmt.setString(++i, type);
            pstmt.setInt(++i, ACTIVE);
            pstmt.setTimestamp(++i, new java.sql.Timestamp(newStart.getTime()));
            pstmt.setTimestamp(++i, new java.sql.Timestamp(newEnd.getTime()));

            //        pstmt.setTimestamp(++i, newStart);
            //        pstmt.setTimestamp(++i, newEnd);
            //        pstmt.setDate(++i, new java.sql.Date(startDate.getTime()));
            //        pstmt.setTime(++i, new java.sql.Time(startTime));
            //        pstmt.setDate(++i, new java.sql.Date(endDate.getTime()));
            //        pstmt.setTime(++i, new java.sql.Time(endTime));

            //System.out.println(getActivationIdSQL);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                activation = buildActivation(rs, null);
            }

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        return activation;

    }


    protected void insertUserActivation (
		    int activationID,
		    List usernames,
		    Connection conn) throws SQLException {

    	PreparedStatement pstmt = null;

    	try {

    		pstmt = conn.prepareStatement(insertUserActivationSQL);

        int i = 0;
        for (int ix = 0; ix < usernames.size(); ++ix ) {
            i = 0;
            pstmt.setInt(++i, activationID);
            pstmt.setString(++i, (String) usernames.get(ix));
            pstmt.executeUpdate();
        }

        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }
    }


    protected void insertAttributes (
		    int activationID,
		    Map attributes,
		    Connection conn) throws SQLException {

        PreparedStatement pstmt = null;

        try {

        pstmt = conn.prepareStatement(insertActivationAttSQL);

        Iterator iterator = attributes.keySet().iterator();
        String key   = null;
        String value = null;
        int i = 0;
        while (iterator.hasNext()) {
            i = 0;
            key   = (String) iterator.next();
            value = (String) attributes.get(key);
            pstmt.setInt(++i, activationID);
            pstmt.setString(++i, key);
            pstmt.setString(++i, value);
            pstmt.executeUpdate();
        }

        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }
    }


    /**
     * builds Activation object the result set
     * @param <code>ResultSet</code>
     * @return <code>Map</code> - activation params or attributes
     */
    protected Activation buildActivation(
		    ResultSet rs,
		    Connection conn) throws SQLException, Exception {

    	Activation activation =  (Activation) new ActivationImpl(
	        rs.getInt("activation_id"),
	        rs.getInt("offering_id"),
	        rs.getString("type"),
	        rs.getDate("start_date"),
	        rs.getDate("end_date"),
	        rs.getTime("start_date").getTime(),
	        rs.getTime("end_date").getTime()
	        );

        if (conn != null) {

            PreparedStatement  pstmt = null;
            ResultSet rsi = null;

            try {
	            pstmt =
	            conn.prepareStatement(getActivationParamsSQL);
	
	            int i = 0;
	            pstmt.setInt(++i, activation.getActivationID());
	            rsi = pstmt.executeQuery();
	
	            /* building attribute list */
	            activation.setAttributes(buildAttributes(rsi));

            } finally {

            	try {
                    if (rsi != null) {
                        rsi.close();
                        rsi = null;
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
        }

        // Find out if this activation has a file associated
        FileService us = new FileServiceImpl();
        File dir = new File(uploadedActivationFileDir,
        		"" + activation.getActivationID());
        activation.setFile(dir.exists() && dir.list().length > 0);

        return activation;
    }


    /**
     * builds the list of users that are a  It does not close the result set
     * @param <code>ResultSet</code>
     * @return <code>Map</code> - activation params or attributes
     */

    protected List buildUserList(ResultSet rs) throws SQLException, Exception {

        List userList = new ArrayList();
        while (rs.next()) {
            userList.add(UserFactory.getUser(rs.getString("user_name")));
        }

        return userList;
    }


    /**
     * builds the activation param map.  It does not close the result set
     * @param <code>ResultSet</code>
     * @return <code>Map</code> - activation params or attributes
     */

    protected Map buildAttributes(ResultSet rs) throws SQLException {

        Map params = new HashMap();
        while (rs.next()) {
            params.put(
            rs.getString("activation_param_name"),
            rs.getString("activation_param_value")
            );
        }

        return params;
    }

         /**
     * <p>
     * Links activations together with events that may exist in the offering 
     * calendar
     * </p><p>
     * @param newact - an activation object
     * @param conn - a JDBC database connection
     * @return a <code>boolean</code> which indicates if the action was successful.
     * </p>
     */
                                                                                                 
    public boolean addEventsToActivation(Activation newact, Connection conn){
                                                                                                 
       PreparedStatement pstmt = null;
       Map activationParams = newact.getAttributes();
       String eventID = "numEvents";
       int activationID = newact.getActivationID();
       boolean success = false;
                                                                                                 
       int numEventsToAdd = Integer.parseInt((String)activationParams.get("numEvents"));
                                                                                                 
       try {
         pstmt = conn.prepareStatement(INSERT_EVENTS_FOR_ACTIVATION);
                                                                                                 
         pstmt.setInt(1, activationID);
         pstmt.setString(2, "numEvents");
         pstmt.setString(3, "" + numEventsToAdd);
         pstmt.executeUpdate();
                                                                                                 
                                                                                                 
         eventID = "Event";
                                                                                                 
         for(int i = 0; i < numEventsToAdd; i++){
                                                                                                 
           String idValue = (String)activationParams.get(eventID + i);
           int j = 0;
           pstmt.setInt(++j, activationID);
           pstmt.setString(++j, eventID + i);
           pstmt.setString(++j, idValue);
           pstmt.executeUpdate();
                                                                                                 
         }
                                                                                                 
         success = true;
                                                                                                 
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

       return success;
                                                                                                 
                                                                                                 
    }


    /**
     * <p>
     * Removes the link between an activation and events.
     * </p><p>
     * @param activationID - A unique identifier for an activation
     * @param conn - A JDBC database connection
     * </p>
     */
                                                                                                 
    public void removeEventsFromActivation(int activationID, Connection conn){
                                                                                                 
      PreparedStatement pstmt = null;
                                                                                                 
       try {
         pstmt = conn.prepareStatement(DELETE_EVENTS_FOR_ACTIVATION);
                                                                                                 
         pstmt.setInt(1, activationID);
         pstmt.executeUpdate();
                                                                                                 
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


}
