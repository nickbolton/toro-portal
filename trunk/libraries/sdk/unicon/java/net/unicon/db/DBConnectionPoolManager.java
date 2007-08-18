/*
 * @(#)DBConnectionPoolManager.java 11-29-2000	
 * @author Unicon, Inc
 * @version 1.0
 *
 * Connection pool manager for managaing the connections to a database.
 */

package net.unicon.db;

import java.util.HashMap;
import java.util.Map;

import net.unicon.util.BaseSystemInfo;
import net.unicon.util.Debug;
import net.unicon.util.*;

public class DBConnectionPoolManager
{
	private Map pools = null;

	/**
	 * Private constructor because this class is a singleton please use 
	 * getDBConnectionPoolManager() instead.
	 * @return connection pool manager
	 */
	private DBConnectionPoolManager()
	{
		pools = new HashMap(10);
	}

	/**
	 * Create a connection pool.
	 * @param poolName
	 * @param simpleSSQL for testing the resource
	 * @param url database location
	 * @param user name
	 * @param password of the user
	 * @return connection pool
	 */
	public synchronized DBConnectionPool getDBConnectionPool(String poolName,
								String simpleSQL, String url, String user,
								String password)
	{
		PoolKey key = new PoolKey(url,user,password);
		DBConnectionPool pool = (DBConnectionPool)pools.get(key);
		if (pool == null)
		{
			pool = new DBConnectionPool(poolName,simpleSQL,url,user,password);
			pools.put(key,pool);
		}

		return pool;
	}

	/** 
	 * Create a connection pool using the system.
	 * @param system information
	 * @return connection pool
	 */
	public synchronized DBConnectionPool getDBConnectionPool(String system)
	{
		BaseSystemInfo si = BaseSystemInfo.getMe();
		Debug.out.println(6,"db", "Gathering Information on DB Pool: ->" + system + "<-");
		String url = si.getProperty(system + "DBURL");
		String user = si.getProperty(system + "DBUser");
		String password = si.getProperty(system + "DBPassword");
		String simpleSQL = si.getProperty(system + "DBSimpleSQL");
		String driver = si.getProperty(system + "DBDriver");

		StringBuffer error = new StringBuffer();
		if (url == null)		error.append("\n**ERROR: Missing " + system + "DBURL property");
		if (user == null)		error.append("\n**ERROR: Missing " + system + "DBUser property");
		if (password == null)	error.append("\n**ERROR: Missing " + system + "DBPassword property");
		if (simpleSQL == null)	error.append("\n**ERROR: Missing " + system + "DBSimpleSQL property");
		if (driver == null)		error.append("\n**ERROR: Missing " + system + "DBDriver property");

		if (error.length() != 0) {
			Debug.out.error("db", error.toString());
			return null;
		}

		url			= url.trim();
		user		= user.trim();
		password	= password.trim();
		simpleSQL	= simpleSQL.trim();
		driver		= driver.trim();

		try {
		Debug.out.println(6,"db", "Trying to load jdbc class: ->" + driver + "<-");
		Class.forName(driver);
		} catch (NullPointerException ne) {
			Debug.out.error("db", "****DBConnectionPoolManager cannot load database driver: " + driver);
			Debug.out.error("db", "    Check the " + system + "DBDriver property");
			Debug.out.printStackTrace("db",ne);
		} catch (ClassNotFoundException ce) {
			Debug.out.error("db", "****DBConnectionPoolManager cannot load database driver: " + driver);
			Debug.out.error("db", "    Check the " + system + "DBDriver property");
			Debug.out.printStackTrace("db",ce);
		}

		PoolKey key = new PoolKey(url,user,password);
		DBConnectionPool pool = (DBConnectionPool)pools.get(key);
		if (pool == null)
		{
			pool = new DBConnectionPool(system,simpleSQL,url,user,password);
			pools.put(key,pool);
		}

		return pool;
	}

	// ** singleton stuff BELOW here **

	private static DBConnectionPoolManager theMgr = null;

	// simply used on the **RARE** chance two threads try to create
	// the singleton at the same time
	private static final Boolean lock = new Boolean(true);

	/**
	 * Create a new pool manager if it has not yet been created.
	 * @return connection pool manager
	 */
	public static DBConnectionPoolManager getDBConnectionPoolManager()
	{
		synchronized (lock) {
			if (theMgr == null) {
				theMgr = new DBConnectionPoolManager();
			}
		}

		return theMgr;
	}

	// ** singleton stuff ABOVE here **

	/** 
	 * Create the pool key using the url, name and password.
	 */
	private static class PoolKey
	{
		public String url;
		public String user;
		public String password;

		/** 
		 * Constructor
		 */
		public PoolKey(String in_URL, String in_user, String in_password)
		{
			url = in_URL;
			user = in_user;
			password = in_password;
		}

		/**
		 * Generate the hash code using the url, user name and passoword.
		 * @return hash code
		 */
		public int hashCode()
		{
			return url.hashCode() + user.hashCode() + password.hashCode();
		}

		/**
		 * Check to see if an object is the same, hash key wise.
		 * @param object to be checked against
		 * @return true if the object has the same url, user and password
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof PoolKey))
				return false;

			PoolKey key = (PoolKey)obj;

			if ( ((url == null) && (key.url == null)) ||
				 (url.equals(key.url)) )
				if ( ((user == null) && (key.user == null)) ||
					 (user.equals(key.user)) )
					if ( ((password == null) && (key.password == null)) ||
						 (password.equals(key.password)) )
						return true;

			return false;
		}
	}
}

// End of DBConnectionPoolManager.java

