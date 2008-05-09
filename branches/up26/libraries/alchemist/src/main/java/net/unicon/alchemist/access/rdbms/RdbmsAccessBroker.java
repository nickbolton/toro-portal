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

package net.unicon.alchemist.access.rdbms;

import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.alchemist.rdbms.Sequencer;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class RdbmsAccessBroker extends AccessBroker {

    private static DataSource defaultDataSource;

    // Member Variables
    private DataSource dataSource = null;
    private int id;
    private Sequencer seq = null;
    private AccessType[] atypes;
    
    public RdbmsAccessBroker(DataSource dataSource, int id, AccessType[] atypes, Element e) {
        super(e);
        this.dataSource = dataSource;
        this.id = id;
        this.seq = new Sequencer(dataSource, "RdbmsAccessBroker", 1);
        this.atypes = atypes;
    }

    public static AccessBroker parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("access-broker")) {
            String msg = "Argument 'e [Element]' must be an <access-broker> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Call parse() in the specified implementation.
        Attribute impl = e.attribute("impl");

        Attribute handle = e.attribute("handle");
        String name = handle.getText();

        // AccessTypes lookup
        Element el = (Element)e.selectSingleNode("access");
        if (el == null)
            throw new IllegalArgumentException(
                    "RdbmsAccessBroker requires the <access> element.");
        impl = el.attribute("impl");
        if (impl == null)
            throw new IllegalArgumentException(
                    "The element <access> must contain an 'impl' attribute.");
        AccessType[] accessTypes = null;
        try {
            accessTypes = (AccessType[])
                Class.forName(impl.getValue())
                     .getMethod("getInstances", null)
                     .invoke(null, null);
        } catch (ClassNotFoundException ex1) {
            throw new RuntimeException("Could not find the class " + impl.getValue(), ex1);
        } catch (NoSuchMethodException ex2) {
            throw new RuntimeException("Could not find the Method 'getInstances' on class " + impl.getValue(), ex2);
        } catch (Exception ex3) {
            throw new RuntimeException("Unable to execute Method 'getInstances' on class " + impl.getValue(), ex3);
        }
        if (accessTypes == null || accessTypes.length == 0)
            throw new IllegalArgumentException(
                    "No AccessTypes found from <access> element declaration.");


        DataSource dataSource = getDataSource();

        return getInstance(dataSource, name, accessTypes, true, e);
        
    }
    
    public static RdbmsAccessBroker getInstance(DataSource dataSource
            , String name, AccessType[] atypes, boolean create, Element e){

        // assertions
        if(dataSource == null){
            throw new IllegalArgumentException("Argument 'dataSource' cannot be null");
        }
        if(name == null){
            throw new IllegalArgumentException("Argument 'name' cannot be null");
        }

        RdbmsAccessBroker rslt = null;
        String getAccessControllerSql = "SELECT * FROM " +
                "ACCESS_CONTROLLER WHERE UPPER(NAME) = UPPER(?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(getAccessControllerSql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();

            if(rs.next()){
                rslt = new RdbmsAccessBroker(dataSource, rs.getInt("Access_Controller_ID"), atypes, e);
            }
        }catch(SQLException sqle){
            StringBuffer msg = new StringBuffer("Rdbms Access Broker failed to find " +
                    "the instance of the accessController. \n").append(sqle.getMessage());
            throw new RuntimeException(msg.toString(), sqle);
        }finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        if(create && rslt == null){
            int id = getAccessBrokerUniqueId(dataSource);
            try{
                conn = dataSource.getConnection();
                pstmt = conn.prepareStatement("INSERT INTO ACCESS_CONTROLLER" +
                		"(ACCESS_CONTROLLER_ID, NAME)  VALUES( ?, ?)");
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.execute();
                rslt = new RdbmsAccessBroker(dataSource, id, atypes, e);
            }catch(SQLException sqle){
                String msg = "Rdbms Access Broker failed to create a new AccessController instance.";
                throw new RuntimeException(msg, sqle);
            }finally{
                if (pstmt != null) closeStatement(pstmt);
                if (conn != null) closeConnection(conn);
            }
        }
        return rslt;
    }

    public IAccessEntry setAccess(Identity principal, AccessRule[] rules,
            Object target) {

        // assertions
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // check if the principal already has access for the target for the given domain
        // if exists, update the permissions
        // else insert user with permissions
        StringBuffer sql = new StringBuffer();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long accessEntryId = -1;

        try{

            conn = dataSource.getConnection();
            // reflection
            Class c = target.getClass();
            Method mthd = c.getDeclaredMethod("getUrl", null);
            String tUrl = (String)mthd.invoke(target, null);

            // get the accessEntryId
            sql.append("SELECT * FROM ACCESS_ENTRY WHERE UPPER(IDENTITY_NAME) = UPPER(?) " +
            " AND UPPER(TARGET) = UPPER(?) AND ACCESS_CONTROLLER_ID = ? " );
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, principal.getId());
            pstmt.setString(2, tUrl);
            pstmt.setInt(3, id);

            rs = pstmt.executeQuery();
            if(rs.next()){
                accessEntryId = rs.getInt("access_Entry_Id");
            }
            sql.delete(0, sql.length());

            if(rules.length > 0){
                // if the entry exists, delete from the table
                if(accessEntryId != -1){
                    sql.append("DELETE FROM ACCESS_TYPES WHERE ACCESS_ENTRY_ID = ?");
                    pstmt = conn.prepareStatement(sql.toString());
                    pstmt.setLong(1, accessEntryId);
                    pstmt.executeUpdate();
                }
                else{
                    sql.delete(0, sql.length());
                    sql.append("INSERT INTO ACCESS_ENTRY (ACCESS_ENTRY_ID" +
                    		", IDENTITY_TYPE, IDENTITY_NAME, TARGET" +
                    		", ACCESS_CONTROLLER_ID) VALUES ( ?, ?, ?, ?, ?)");
                    pstmt = conn.prepareStatement(sql.toString());
                    //accessEntryId = getAccessEntryUniqueId();
                    accessEntryId = seq.next();
                    pstmt.setLong(1, accessEntryId);
                    pstmt.setInt(2, principal.getType().toInt());
                    pstmt.setString(3, principal.getId());
                    pstmt.setString(4, tUrl);
                    pstmt.setInt(5, id);
                    pstmt.executeUpdate();
                }
                if(pstmt != null) closeStatement(pstmt);

                for(int i = 0; i < rules.length; i++){
                	sql.delete(0, sql.length());
                    sql.append("INSERT INTO ACCESS_TYPES(ACCESS_ENTRY_ID" +
                    		", ACCESS_TYPE, STATUS) VALUES (?, ?, ?)");
                    pstmt = conn.prepareStatement(sql.toString());
                    pstmt.setLong(1, accessEntryId);
                    pstmt.setInt(2, rules[i].getAccessType().toInt());
                    pstmt.setString(3, rules[i].getStatus()? "T" : "F");
                    pstmt.executeUpdate();
                    if(pstmt != null) pstmt.close();
                }                
                pstmt = null;
            }
            // if the type array is empty, remove all the access from the
            // accessEntry and accessType table for the given principal and target
            else{
                sql.append("DELETE FROM ACCESS_TYPES WHERE ACCESS_ENTRY_ID = ?");                
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, accessEntryId);
                pstmt.setLong(2, accessEntryId);
                pstmt.execute();
                if (pstmt != null) closeStatement(pstmt);
                sql.delete(0, sql.length());
                sql.append("DELETE FROM ACCESS_ENTRY WHERE ACCESS_ENTRY_ID = ?");
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, accessEntryId);
                pstmt.setLong(2, accessEntryId);
                pstmt.execute();                
            }

        }catch(SQLException se){
            throw new RuntimeException("Access Controller was unsuccessful in tying to " +
                    "set access." ,se);
        }
        catch(Throwable t){
            throw new RuntimeException("Access Controller was unsuccessful in tying to " +
                    "set access." ,t);
        }
        finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return new AccessEntryImpl(principal, rules, target);
    }

    // TODO to be completed
    // this method will set access for all the identities chosen in the selection basket
   /* public synchronized IAccessEntry[] setAccess(Identity[] principal, List[] types,
            Object target) {

        IAccessEntry[] rslt = new IAccessEntry[principal.length];

        // assertions
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (types == null) {
            String msg = "Argument 'types' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal.length != types.length) {
            String msg = "Argument 'principal' and 'types' must have the same number of elements.";
            throw new IllegalArgumentException(msg);
        }

        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;


        // delete all the access_types for the given identities and target
        StringBuffer sql = new StringBuffer("delete from access_types where " +
        		"access_entry_id in (select access_entry_id from " +
        		"access_entry where identity_name in (");

        for(int i = 0; i < principal.length; i++){
            if(i == principal.length - 1){
                sql.append(" ? ");
            }else{
                sql.append(" ? , ");
            }
        }

        sql.append(") and target = ? and access_controller_id = ? ) ");
        
        try{
            //  reflection
            Class c = target.getClass();
            Method mthd = c.getDeclaredMethod("getUrl", null);
            String tUrl = (String)mthd.invoke(target, null);

            pstmt = conn.prepareStatement(sql.toString());

            int index = 1;
            // set the parameters for the delete all accessTypes
            for(int i = 0; i < principal.length; i++){
                pstmt.setString(index++, principal[i].getId());
            }

            pstmt.setString(index++, tUrl);
            pstmt.setInt(index++, this.id);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            sql.delete(0, sql.length());
            
            // select all the identities that are already in the database
            sql.append("select distinct identity_name from access_entry where " +
            		"upper(target) = upper(?) and access_controller_id = ? ; ");
            pstmt = conn.prepareStatement(sql.toString());
            
            // set parameters for select all identities from the DB for the given target
            pstmt.setString(1, tUrl);
            pstmt.setInt(2, this.id);
            rs = pstmt.executeQuery();

            List identities = new LinkedList();
            while(rs.next()){
                identities.add(rs.getString("identity_name"));
            }

            // insert identities in access_entry table and accesstypes in accessTypes table
            List accessTypes = null;
            pstmt = conn.prepareStatement("");
            PreparedStatement tempPstmt = null;
            for(int i = 0; i < principal.length; i++){
                accessTypes = types[i];
                if(accessTypes.size() > 0){
                    int accessEntryId = seq.next();
                    if( ! identities.contains(principal[i].getId())){
                        sql.delete(0, sql.length());
                        sql.append(" insert into access_entry (access_entry_id, identity_type")
                        .append(", identity_name, target, access_controller_id) select ?, ?")
                        .append(", ?, ?, ? ");

                        tempPstmt = conn.prepareStatement(sql.toString());
                        tempPstmt.setInt(1, accessEntryId);
                        tempPstmt.setInt(2, principal[i].getType().toInt());
                        tempPstmt.setString(3, principal[i].getId());
                        tempPstmt.setString(4, tUrl);
                        tempPstmt.setInt(5, this.id);
                        pstmt.addBatch(tempPstmt.toString());
                        //where  not exists(select * from access_entry " +
                        //"where upper(identity_name) = upper(?) and " +
                        //"upper(target) = upper(?) and access_controller_id = ?); ");
                    }
                    for(int j = 0; j < accessTypes.size(); j++){
                        sql.delete(0, sql.length());

                        sql.append("insert into access_types (access_entry_id, access_type)");
                        pstmt.addBatch(tempPstmt.toString());
                        
                        sql.delete(0, sql.length());
                        sql.append("select access_entry_id , ? from access_entry where " +
                        		"Upper(identity_name)")
                            .append(" = Upper(?) and upper(target) = upper(?) and " +
                            		"access_controller_id = ?");

                        tempPstmt = conn.prepareStatement(sql.toString());
                        tempPstmt.setInt(1, ((AccessType)accessTypes.get(j)).toInt());
                        tempPstmt.setString(2, principal[i].getId());
                        tempPstmt.setString(3, tUrl);
                        tempPstmt.setInt(4, this.id);
                        pstmt.addBatch(tempPstmt.toString());
                    }
                }else{
                    sql.delete(0, sql.length());

                    sql.append("delete from access_entry where upper(identity_name) = upper(?)")
                       .append("and upper(target) = upper(?) and access_controller_id = ?  ");

                    tempPstmt = conn.prepareStatement(sql.toString());
                    tempPstmt.setString(1, principal[i].getId());
                    tempPstmt.setString(2, tUrl);
                    tempPstmt.setInt(3, this.id);
                    pstmt.addBatch(tempPstmt.toString());
                }
            }

            pstmt.executeBatch();

        }catch(SQLException se){
            throw new RuntimeException("Access Broker was not able to set access. ", se);
        }catch(Throwable t){
            throw new RuntimeException("Access Broker was not able to get " +
            		"the url for the given target. ", t);
        }

        return rslt;
    }*/

    public void removeAccess(IAccessEntry[] entries) {
        // assertions
        if (entries == null) {
            String msg = "Argument 'entries' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        StringBuffer selectIdSql = new StringBuffer();
        StringBuffer deleteSql = new StringBuffer();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int accessEntryId = -1;

        if(entries.length == 0){
            return;
        }

        try{
            conn = dataSource.getConnection();
            // reflection
            Class c = entries[0].getTarget().getClass();
            Method mthd = c.getDeclaredMethod("getUrl", null);
            String tUrl = (String)mthd.invoke(entries[0].getTarget(), null);

            // get the accessEntryIds
            selectIdSql.append("SELECT * FROM ACCESS_ENTRY WHERE UPPER(IDENTITY_NAME) IN ( ");
            for (int i = 0; i < (entries.length - 1); i++) {
                selectIdSql.append(" ? , ");
            }
            selectIdSql.append(" ? ) ");
            selectIdSql.append(" AND UPPER(TARGET) = UPPER(?) AND ACCESS_CONTROLLER_ID = ? ");

            pstmt = conn.prepareStatement(selectIdSql.toString());
            int i = 0;
            for (i = 0; i < entries.length; i++) {
                pstmt.setString(i+1, entries[i].getIdentity().getId().toUpperCase());
            }
            pstmt.setString(++i, tUrl);
            pstmt.setInt(++i, id);
            rs = pstmt.executeQuery();

            List accessEntries = new LinkedList();
            while (rs.next()) {
                accessEntries.add(rs.getObject("access_Entry_Id"));
            }
            if (rs != null) closeResultSet(rs);

            if(accessEntries.size() == 0){
                return;
            }
            StringBuffer params = new StringBuffer();
            for(i = 0; i < (accessEntries.size() - 1); i++){
                params.append(" ? , ");
            }
            params.append(" ? ) ");

            deleteSql.append("DELETE FROM ACCESS_TYPES WHERE ACCESS_ENTRY_ID IN ( ");
            deleteSql.append(params);            

            if (pstmt != null) closeStatement(pstmt);
            pstmt = conn.prepareStatement(deleteSql.toString());

            int num = accessEntries.size();
            for(i = 0; i < accessEntries.size(); i++){
                pstmt.setObject(i+1, accessEntries.get(i));                
            }
            pstmt.execute();
            if(pstmt != null) pstmt.close();
            pstmt = null;
                        
            deleteSql.delete(0, deleteSql.length());
            deleteSql.append("DELETE FROM ACCESS_ENTRY WHERE ACCESS_ENTRY_ID IN ( ");
            deleteSql.append(params);
            pstmt = conn.prepareStatement(deleteSql.toString());
            for(i = 0; i < accessEntries.size(); i++){
                pstmt.setObject(i+1, accessEntries.get(i));             
            }
            pstmt.execute();
            

        }catch(SQLException se){
            throw new RuntimeException("Access Controller was unsuccessful in trying to " +
                    "remove access." ,se);
        }catch(Throwable t){
            throw new RuntimeException("Access Controller was unsuccessful in trying to " +
                    "remove access." ,t);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

    }

    public IAccessEntry getEntry(Identity principal, Object r) {

        // assertions
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (r == null) {
            String msg = "Argument 'r [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IAccessEntry rslt = null;
        ArrayList rules = new ArrayList();

        String getAccessSql = "SELECT ACCESS_TYPE, STATUS FROM ACCESS_ENTRY AC" +
        		", ACCESS_TYPES AT WHERE AT.ACCESS_ENTRY_ID = AC.ACCESS_ENTRY_ID " +
        		"AND AC.ACCESS_CONTROLLER_ID = ? AND UPPER(IDENTITY_NAME) " +
        		"= UPPER(?) AND UPPER(TARGET) = UPPER(?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{

            conn = dataSource.getConnection();
            // reflection
            Class c = r.getClass();
            Method mthd = c.getDeclaredMethod("getUrl", null);
            String tUrl = (String)mthd.invoke(r, null);

            pstmt = conn.prepareStatement(getAccessSql);
            pstmt.setInt(1, id);
            pstmt.setString(2, principal.getId());
            pstmt.setString(3, tUrl);
            rs = pstmt.executeQuery();

            while(rs.next()){
            	rules.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
            }
            if(rules.size() > 0){
            	rslt = new AccessEntryImpl(principal
            			, (AccessRule[])rules.toArray(new AccessRule[rules.size()]), r);
            }           

        }catch(SQLException se){
            throw new RuntimeException("Access Controller was unsuccessful in getting " +
                    "the AccessRules for the given principal.", se);
        }catch(Throwable t){
            throw new RuntimeException("Access Controller was unsuccessful in getting " +
                    "the AccessRules for the given principal.", t);
        }
        finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return rslt;

    }

    public IAccessEntry[] getEntries(Principal p, AccessRule[] rules) {

        // assertions
        if (p == null) {
            String msg = "Argument 'p [Principal]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IAccessEntry[] rslt = new IAccessEntry[0];
        List entries = new ArrayList();
        StringBuffer getTargetSql = new StringBuffer("SELECT * FROM " +
        		"ACCESS_ENTRY AE, ACCESS_TYPES AT WHERE " +
        		"AE.ACCESS_CONTROLLER_ID = ? AND UPPER(IDENTITY_NAME) IN ( ");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer params = new StringBuffer();
        Identity[] principals = p.getIdentities();
        int accessEntryId = -1;
        
        try {
            conn = dataSource.getConnection();
            
            for(int i = 0; i < principals.length; i++){
                if(i == principals.length - 1){
                    getTargetSql.append(" ? ");
                }else{
                    getTargetSql.append(" ? , ");
                }
            }
            getTargetSql.append(" )");

            getTargetSql.append(" AND AE.ACCESS_ENTRY_ID = AT.ACCESS_ENTRY_ID ");
            //  if types is empty, get all of them.
            if(rules.length > 0){
                getTargetSql.append(" AND ( ");
                for(int i = 0; i < rules.length; i++){
                    getTargetSql.append(" (ACCESS_TYPE = ? AND STATUS = ?) ");
                    if(i != rules.length - 1){
                        getTargetSql.append(" OR ");
                    }
                }
                getTargetSql.append(" ) ");
            }

            getTargetSql.append(" ORDER BY AE.ACCESS_ENTRY_ID ");
            pstmt = conn.prepareStatement(getTargetSql.toString());
            pstmt.setInt(1, id);
            for (int i = 0; i < principals.length; i++) {
                pstmt.setString(i + 2, principals[i].getId().toUpperCase());
            }

            int initialIndex = principals.length + 2;
            for(int i = 0; i < rules.length; i++){
                pstmt.setInt(initialIndex + (i * 2), rules[i].getAccessType().toInt());
                pstmt.setString(initialIndex + (i * 2) + 1, rules[i].getStatus()? "T" : "F");
            }
            
            rs = pstmt.executeQuery();
            List rule = new ArrayList();
            AccessRule tempRule = null;
            if(rs.next()){
            	String principal = rs.getString("identity_name");
                IdentityType idType = IdentityType.getInstance(rs.getInt("identity_type"));
                String target = rs.getString("target");
                accessEntryId = rs.getInt("access_Entry_id");
                Object targetObject = getTargetObject(target);
                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	            while (rs.next()) {	                
	                if(rs.getInt("access_entry_id") == accessEntryId){
	                    tempRule = new AccessRule(getAccessType(rs.getInt("Access_Type"))
	                	        , rs.getString("status").equalsIgnoreCase("T")? true:false);
	                    if(!rule.contains(tempRule)){
	                        rule.add(tempRule);
	                    }
	                }else{
		                // targets could be repeated in case of shared
		                // check if the entry already exists, if it does, update the access_types
		                entries.add(new AccessEntryImpl(new Identity(principal, idType)
		                		, (AccessRule[]) rule.toArray(new AccessRule[0])
								, targetObject));
		                rule.clear();
		                principal = rs.getString("identity_name");
		                idType = IdentityType.getInstance(rs.getInt("identity_type"));
		                target = rs.getString("target");
		                accessEntryId = rs.getInt("access_Entry_id");
		                targetObject = getTargetObject(target);
		                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
		            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	                }
	            }
	            if(rule.size() > 0){
		            entries.add(new AccessEntryImpl(new Identity(principal, idType)
		            		, (AccessRule[]) rule.toArray(new AccessRule[0])
							, targetObject));
		            rule.clear();
	            }
            }
            rslt = (IAccessEntry[])entries.toArray(new IAccessEntry[entries.size()]);

        }catch(IllegalArgumentException iae){
        	//TODO this needs to be addressed properly
        	// this is coming from the getTargetObject. The factory for the given URL could not be created.
        	// delete the entry from the database.
        	try{
        		if (rs != null) rs.close(); rs = null;
        		if (pstmt != null) closeStatement(pstmt);       		
        		pstmt = conn.prepareStatement("DELETE FROM ACCESS_TYPES WHERE ACCESS_ENTRY_ID = " + accessEntryId);
        		pstmt.execute();
        		closeStatement(pstmt);        		
        		pstmt = conn.prepareStatement("DELETE FROM ACCESS_ENTRY WHERE ACCESS_ENTRY_ID = " + accessEntryId);
        		pstmt.execute();        		
        		
        	}catch(SQLException s){
        		throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                        "targets for the given identity_name and Access_Type.", s);
        	}
        }
        catch (SQLException se) {
            throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                    "targets for the given identity_name and Access_Type.", se);
        }
        finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return rslt;

    }

    /* (non-Javadoc)
     * @see net.unicon.academus.access.AccessBroker#getEntries(net.unicon.academus.access.ITarget, net.unicon.academus.access.AccessType[])
     */
    public IAccessEntry[] getEntries(Object r, AccessRule[] rules) {

        //      assertions
        if (r == null) {
            String msg = "Argument 'r [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'types' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IAccessEntry[] rslt = new IAccessEntry[0];
        List entries = new ArrayList();
        StringBuffer getTargetSql = new StringBuffer("SELECT * FROM " +
        		"ACCESS_ENTRY AE, ACCESS_TYPES AT WHERE " +
        		"AE.ACCESS_CONTROLLER_ID = ? AND UPPER(TARGET) = UPPER(?) " +
        		"AND AE.ACCESS_ENTRY_ID = AT.ACCESS_ENTRY_ID ");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer params = new StringBuffer();

        try {

            conn = dataSource.getConnection();
            
            // reflection
            Class c = r.getClass();
            Method mthd = c.getDeclaredMethod("getUrl", null);
            String tUrl = (String)mthd.invoke(r, null);

            //  if types is empty, get all of them.
            if(rules.length > 0){
                getTargetSql.append(" AND ( ");
                for(int i = 0; i < rules.length; i++){
                    getTargetSql.append(" (ACCESS_TYPE = ? AND STATUS = ?) ");
                    if(i != rules.length - 1){
                        getTargetSql.append(" OR ");
                    }
                }
                getTargetSql.append(" ) ");
            }

            getTargetSql.append(" ORDER BY IDENTITY_NAME, TARGET");
            pstmt = conn.prepareStatement(getTargetSql.toString());
            pstmt.setInt(1, id);
            pstmt.setString(2, tUrl);

            int initialIndex = 3;
            for(int i = 0; i < rules.length; i++){
                pstmt.setInt(initialIndex + (i * 2), rules[i].getAccessType().toInt());
                pstmt.setString(initialIndex + (i * 2) + 1, rules[i].getStatus()? "T":"F");
            }
            
            rs = pstmt.executeQuery();

            List rule = new ArrayList();
            String principal = null;
            int accessEntryId = 0;
            int identityType = 0;
            if(rs.next()){
            	// get information for the first item
            	principal = rs.getString("identity_name");
                accessEntryId = rs.getInt("access_Entry_id");
                identityType = rs.getInt("identity_type");
                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	            while (rs.next()) {	                
	                if(rs.getInt("access_entry_id") == accessEntryId){
	                    rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
	                	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	                }
	                else{
		                entries.add(new AccessEntryImpl(new Identity(principal
		                        , IdentityType.getInstance(identityType))
		                        , (AccessRule[])rule.toArray(new AccessRule[0])
		                        , r));
		                rule.clear();
		                principal = rs.getString("identity_name");
		                accessEntryId = rs.getInt("access_Entry_id");
		                identityType = rs.getInt("identity_type");
		                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
		            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	                }
	            }
	            if(rule.size() > 0){
		            entries.add(new AccessEntryImpl(new Identity(principal
	                        , IdentityType.getInstance(identityType))
	                        , (AccessRule[])rule.toArray(new AccessRule[0])
	                        , r));
		            rule.clear();
	            }
            }
            rslt = (IAccessEntry[])entries.toArray(new IAccessEntry[entries.size()]);

        } catch (SQLException se) {
            throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                    "targets for the given identity_name and Access_Type.", se);
        }catch (Throwable t) {
            throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                    "targets for the given identity_name and Access_Type.", t);
        }
        finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return rslt;

    }
    
    public IAccessEntry[] getEntries() {

        IAccessEntry[] rslt = new IAccessEntry[0];
        List entries = new ArrayList();
        StringBuffer getTargetSql = new StringBuffer("Select * from Access_Entry ae" +
        		", Access_types at where ae.Access_Controller_ID = ? and " +
                " ae.access_entry_id = at.access_entry_id ");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer params = new StringBuffer();

        try {

            conn = dataSource.getConnection();
            
            getTargetSql.append(" order by identity_name, target");
            pstmt = conn.prepareStatement(getTargetSql.toString());
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();

            List rule = new ArrayList();
            String principal = null;
            String target = null;
            int accessEntryId = 0;
            int identityType = 0;
            Object targetObject = null;
            if(rs.next()){
            	// get information for the first item
            	principal = rs.getString("identity_name");
                accessEntryId = rs.getInt("access_Entry_id");
                identityType = rs.getInt("identity_type");
                target = rs.getString("target");
                targetObject = getTargetObject(target);
                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	            while (rs.next()) {	                
	                if(rs.getInt("access_entry_id") == accessEntryId){
	                    rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
	                	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	                }
	                else{
		                entries.add(new AccessEntryImpl(new Identity(principal
		                        , IdentityType.getInstance(identityType))
		                        , (AccessRule[])rule.toArray(new AccessRule[0])
		                        , targetObject));
		                rule.clear();
		                principal = rs.getString("identity_name");
		                accessEntryId = rs.getInt("access_Entry_id");
		                identityType = rs.getInt("identity_type");
		                rule.add(new AccessRule(getAccessType(rs.getInt("Access_Type"))
		            	        , rs.getString("status").equalsIgnoreCase("T")? true:false));
	                }
	            }
	            if(rule.size() > 0){
		            entries.add(new AccessEntryImpl(new Identity(principal
	                        , IdentityType.getInstance(identityType))
	                        , (AccessRule[])rule.toArray(new AccessRule[0])
	                        , targetObject));
		            rule.clear();
	            }
            }
            rslt = (IAccessEntry[])entries.toArray(new IAccessEntry[entries.size()]);

        } catch (SQLException se) {
            throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                    "targets for the given identity_name and Access_Type.", se);
        }catch (Throwable t) {
            throw new RuntimeException("Access Controller was unsuccessful in getting the " +
                    "targets for the given identity_name and Access_Type.", t);
        }
        finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return rslt;

    }
    
    private static void closeResultSet(ResultSet rs){
        if (rs == null) {
            return;
        }

        try {
            rs.close();
            rs = null;
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }
    }

    private static void closeStatement(Statement stmt){

        if (stmt == null) {
            return;
        }

        try {
            stmt.close();
            stmt = null;
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }
    }

    private static void closeConnection(Connection conn){

        if (conn == null) {
            return;
        }

        try {
            conn.close();
            conn = null;
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }
    }

    public static Object getTargetObject(String url) throws RuntimeException{

        // Assertions.
        if(url == null || url.trim().equals("")){
            throw new IllegalArgumentException("getTargetObject: The Factory"
                                            + " Url must not be null or empty.");
        }

        Object rf = null;

        // URL: protocol://classname/parameters
        // Tokens:
        // 0 = "protocol"
        // 1 = empty string
        // 2 = classname
        // 3+ = parameters
        String[] tokens = url.split("/", 4);
        if (tokens.length < 3)
            throw new IllegalArgumentException("The URL is not correctly formatted: "+url);

        try {
            Class c = Class.forName(tokens[2]);
            Method mthd = c.getDeclaredMethod("fromUrl", new Class[]{String.class});
            rf = mthd.invoke(null, new Object[]{url});
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Make sure a valid response was created.
        // TODO this needs to be handled differently.
        // the quick fix is to remove the entry that is causing the problem.
        if (rf == null) {
        	String msg = "Unable to construct the target object from the "
                                        + "specified url:  " + url;
            //throw new RuntimeException(msg);
        	throw new IllegalArgumentException(msg);
        }

        return rf;
    }

    public static void bootstrap(DataSource ds) {
        if (ds == null)
            throw new IllegalArgumentException("Argument 'ds' cannot be null.");

        defaultDataSource = ds;
    }

    private static DataSource getDataSource() {
        if (defaultDataSource == null) {
            try {
                defaultDataSource = AcademusFacadeContainer.retrieveFacade().getAcademusDataSource();
            } catch(AcademusFacadeException afe){
                afe.printStackTrace();
            }
        }

        return defaultDataSource;
    }

    //  returns a unique AccessObject for the given factory.
    private static synchronized int getAccessBrokerUniqueId(DataSource dataSource) {

        // Assertions.
        if (dataSource == null) {
            String msg = "Argument 'dataSource' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        int rslt = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{

            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement("select max(Access_Controller_ID) " +
                    "from Access_Controller");
            rs = pstmt.executeQuery();

            if (rs.next()){
                rslt = rs.getInt(1) + 1;
            } else {
                rslt = 1;
            }

        }catch(SQLException sqle){
            String msg = "Rdbms Access Broker failed to evaluate the next controller id.";
            throw new RuntimeException(msg, sqle);
        }finally{
            if (rs != null) closeResultSet(rs);
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

        return rslt;

    }

    private AccessType getAccessType(int id) {
        AccessType rslt = null;
        for (int i = 0; rslt == null && i < atypes.length; i++)
            if (atypes[i] != null && atypes[i].compareInt(id))
                rslt = atypes[i];

        if (rslt == null)
            throw new IllegalArgumentException(
                    "Unable to locate a matching AccessType for id: "+id);
        return rslt;
    }

}
