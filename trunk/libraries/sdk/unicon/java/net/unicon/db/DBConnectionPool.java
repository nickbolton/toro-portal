//**********************************************************************
//
//  File:            DBConnectionPool.java
//
//  Copyright:       (c) 2001 UNICON, Inc. All Rights Reserved
//
//  This source code is the confidential and proprietary information
//  of UNICON, Inc.  No part of this work may be modified or used
//  without the prior written consent of UNICON, Inc.
//
//**********************************************************************

/*
 * @(#)DBConnectionPool.java 11-29-2000	
 * @author UNICON, Inc
 * @version 1.0
 *
 */

package net.unicon.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.unicon.util.RootException;
import net.unicon.util.BaseSystemInfo;
import net.unicon.util.Debug;
import net.unicon.util.*;

/**
 * A {@link net.unicon.util.ResourcePool}
 * implementation that maintains a pool of database
 * connections. Applications using this class must ensure that the proper
 * database driver has been loaded.<p>
 *
 * The SimpleSQL should be a fast-executing DB hit that returns very little
 * data and is used to ensure the Connection being returned is valid.
 */
public class DBConnectionPool extends net.unicon.util.ResourcePool
{
    /**
	 * A quick database query used to verify a connection */
	public String simpleSQL;

    /**
	 * The URL of the database */
	public String DBURL;

    /**
	 * The username for the database */
	public String DBUser;

    /**
	 * The database password for {@link #DBUser} */
	public String DBPassword;

    /**
	 * Creates the singleton <code>DBConnectionPool</code>, initializing
	 * constants and populating the connection pool.
	 * @param system string
	 * @param simpleSQL string
	 * @param dbURL connection string
	 * @param dbUser database user name
	 * @param dbPassword database passoword
	 * @return DBConnectionPool a connection to the databade
	 */
	protected DBConnectionPool(String system, String simpleSQL,
							   String dbURL, String dbUser, String dbPassword)
    {
        super(system);

		this.simpleSQL = simpleSQL;
		this.DBURL = dbURL;
		this.DBUser = dbUser;
		this.DBPassword = dbPassword;
		
		if (simpleSQL == null || simpleSQL.length() == 0)
			debug.println(1, "db", "***DBConnectionPool: SimpleSQL is null");
		if (DBURL == null || DBURL.length() == 0)
			debug.println(1, "db", "***DBConnectionPool: DatabaseURL is null");
		if (DBUser == null || DBUser.length() == 0)
			debug.println(1, "db", "***DBConnectionPool: DatabaseUser is null");
		if (DBPassword == null || DBPassword.length() == 0)
			debug.println(1, "db", "***DBConnectionPool: DatabasePassword is null");

		debug.println(1,"db","DBConnectionPool URL: " + DBURL +
							 "&" + DBUser + "&" + DBPassword);
    }

	/** 
	 * check the connection to see if it is okay.
	 * @param connection object
	 * @return boolean true if the connection is okay
	 */
    protected boolean checkResource(Object o) throws RootException
    {
		try {

            Connection conn = (Connection)o;
            Statement dummy_stmt = conn.createStatement();

            // If simpleSQL is not set, then don't do a real check.
            if ((simpleSQL == null) || (simpleSQL.length() == 0)) {
                dummy_stmt.close();
                return true;
            }

            ResultSet rs = dummy_stmt.executeQuery(simpleSQL);
            rs.next();
            rs.close();
            dummy_stmt.close();

		} catch(SQLException se) {
			debug.printStackTrace("db",se);
			return false;
		}

		return true;
    }

	/**
	 * Release the connection resources.
	 * @param object connection
	 */
    protected void closeResource(Object o) throws RootException
    {
		try {
            Connection conn = (Connection)o;
            try {
			    conn.rollback();
			} catch (Exception e) {
		        /* For some reason the rollback did not work.  We
			     * want to catch the error and release the connection
			     * back to the resource pool.  If we did not catch 
			     * this error, the pool will never release the 
			     * connection. */
				Debug.out.println(2, "db", "Unable to rollback connetion");
			    Debug.out.printStackTrace(2, "db", e);
			}
            conn.close();

		} catch(SQLException se) {
			throw new RootException(se);
		}
	}

	/**
	 * Create a connection and set the auto-commit to false.
	 * @return x connection object.
	 */
	protected Object setResource() throws RootException
    {

		BaseSystemInfo sysInfo;
		Connection x;

		sysInfo = BaseSystemInfo.getMe();
		try {
			if (sysInfo.featureSupported("JdbcWrapper")) {
				Debug.out.println(2, "db", "Using JDBC wrapper");
				x = new ConnectionWrapper(DriverManager.getConnection(DBURL, DBUser, DBPassword),
					DBURL, DBUser, DBPassword);
			}
			else {
				Debug.out.println(7, "db", "Using STANDARD connection.");
				x = DriverManager.getConnection(DBURL, DBUser, DBPassword);
			}
			x.setAutoCommit(false);

			return x;

		} catch(SQLException se) {
			throw new RootException(se);
		} 
    }

	/**
	 * Allocate a connection.
	 * @param requestor 
	 * @return connection 
	 */
	public Connection allocateConnection(String requestor) 
		throws RootException
	{
		return (Connection)super.allocate(new StringBuffer(requestor));
	}

	/**
	 * Allocate a connection.
	 * @param request
	 * @return connection
	 */
	public Connection allocateConnection(StringBuffer requestor)
		throws RootException
	{
		return (Connection)super.allocate(requestor);
	}

	/**
	 * Release a connection from the pool.
	 * @param conn connection to be released
	 */
	public void releaseConnection(Connection conn)
		throws RootException
	{
		try {
            conn.rollback();
			super.release(conn);
		} catch(SQLException se) {
		    /* For some reason the rollback did not work.  We
			 * want to catch the error and release the connection
			 * back to the resource pool.  If we did not catch 
			 * this error, the pool will never release the 
			 * connection. */
			Debug.out.println(2, "db", "Unable to Rollback Connection, invalidating Resource");
			Debug.out.printStackTrace(2, "db", se);
			super.invalidateResource(conn);
		} 
	}

	public void release(Object obj) throws RootException
	{
		releaseConnection((Connection)obj);
	}
}

// End of DBConnectionPool.java

