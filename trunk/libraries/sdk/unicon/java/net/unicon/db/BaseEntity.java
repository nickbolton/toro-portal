package net.unicon.db;

import java.sql.*;
import java.sql.Date;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayList;

import net.unicon.util.*;
import net.unicon.web.FormData;

import oracle.jdbc.driver.*;

/**
 * Abstracts the basic insert, update, delete, and query methods to obtain
 * information for the database. Eases the gap between database rows and the
 * Java objects representing them.<p>
 *
 * This class has not really worked out to be useful, so it is only referenced
 * by old code that will be phased out soon.<p>
 *
 * <code>BaseEntity</code> is responsible for allocating and closing
 * <code>CallableStatements</code> and <code>ResultSets</code> for the
 * standard queries: insert, update, delete, and get.  Entities will likely
 * write static methods which return partially filled objects.  Those methods
 * are responsible for closing and Statement and ResultSet objects.<p>
 *
 * Subclasses must implement the following abstract methods:<ul>
 *
 * <li><code>getSQL(kind)</code>: 4 queries to insert/update/delete/query the
 * database
 *
 * <li><code>setKeyParameters(CallableStatement)</code>: sets the keys for the
 * query and delete SQL
 *
 * <li><code>fromDB(ResultSet)</code>: initializes <code>this</code> from a
 * row in the database
 *
 * <li><code>toDB(CallableStatement)</code>: initializes a
 * <code>Statement</code> from data in <code>this</code>
 *
 * <li><code>setFormData(FormData)</code>: initializes <code>this</code> from
 * values in a <code>FormData</code> object
 *
 * <li><code>setKey(int)</code>: sets the key returned by the database on a
 * call to <code>create()</code>.  If it is not a single int, override
 * <code>create()</code>.
 *
 * <li><code>init(Connection, key)</code>: initializes <code>this</code> by
 * calling <code>BaseEntity.init(Connection)</code> and setting the key values
 * </ul><p>
 *
 * In addition, Entities should also provide constructors with the following
 * signatures:<ul>
 *
 * <li><b>default constructor (no parameters)</b> - creates an empty entity
 * whose <code>initialized</code> and <code>updateable</code> properties are
 * false. User must call <code>init()</code> to use the new object.
 *
 * <li><b>DBConnection, ???keys</b> - creates a new entity from values in the
 * database by calling <code>get</code>, which in turn calls
 * <code>fromDB</code> in the subclass. The <code>updateable</code> property
 * is true for the new object.
 *
 * <li><b>DBConnection, FormData</b> - same as above but values (including
 * keys) are obtained from a <code>FormData</code> object.  The
 * <code>updateable</code> property is true for the new object.
 *
 * <li><b>??all attributes</b> - creates a partially filled entity with
 * nonspecified values set to <code>null</code> or <code>BADINT</code>. The
 * <code>updateable</code> property is false for the new object.
 * </ul><p>
 *
 * Subclasses should also provide the following methods:<ul>
 *
 * <li><code>static newInstance(DBConnection, ???keys)</code> - returns a
 * <code>BaseEntity</code> with the keys filled which may then be
 * <code>create()</code>-ed.  Keys will not include any automatically
 * generated IDs, only additional keys required to construct the
 * object.
 *
 * <li><code>getX()/setX()</code> - accessor methods for each attribute. The
 * <code>setX()</code> methods for key values will likely be private.
 *
 * <li><code>static Collection getX()</code> - methods which query the
 * database and return <code>Collections</code> of partially filled
 * <code>BaseEntities</code>.
 * </ul><p>
 *
 * <h3><code>BaseEntity</code> manipulation</h3>
 * <ul>
 * <li>General Manipulation:<ul>
 *	   <li>The no-argument constructor creates a non-initialized,
 *  	   non-updateable entity
 *     <li><code>init()</code> sets the connection and key values
 *         (<code>initialized</code> is now <code>true</code> but
 *         <code>updateable</code> is <code>false</code>)
 *     <li>all methods available except <code>update()</code>, which requires
 *         a <code>get()</code> to set the <code>updateable</code>
 *         property</ul>
 * <li>Creation:<ul>
 *	   <li>default constructor
 *     <li><code>init()</code>
 *     <li>optionally set any meaningful values, but not the
 *         database-generated keys
 *     <li><code>create()</code> adds the item to the database, creating any
 *         ID key values (<code>updateable</code> is still <code>false</code>,
 *         as we have no key value)</ul>
 * <li>Edit/Update/Delete:<ul>
 *     <li>The constructor creates the objects using key value(s) using
 *         <code>BaseEntity.get</code> and creates an updateable object
 *     <li>Other static methods also create partial objects which are not
 *         updateable (cannot call <code>update()</code>)
 *     <li><code>getX()/setX()</code> - attribute methods that read/write the
 *         entity
 *     <li><code>setFormData</code> also modifies attribute values
 *     <li><code>update()</code> and <code>delete()</code> modifies or removes
 *         the database row </ul>
 * </ul>
 *
 * @author Unicon, Inc.
 * @version 3.0 */
public abstract class BaseEntity implements java.io.Serializable
{

	/**	
	 * The value which is illegal for any integer in the DB */
	public static int BADINT = -9;

	/** 
	 * @see #getSQL(int) */
	protected static int	INSERT		= 0;

	/** 
	 * @see #getSQL(int) */
	protected static int	UPDATE		= 1;

	/** 
	 * @see #getSQL(int) */
	protected static int	DELETE		= 2;

	/** 
	 * @see #getSQL(int) */
	protected static int	GET			= 3;

	/** 
	 * The connection which created this <code>BaseEntity</code> */
	/**
	 *  */
	protected transient Connection connection = null;

	/** 
	 * Initially <code>false</code>; set to <code>true</code> by
	 * <code>init()</code> */
	protected boolean initialized = false;

	/** Were we created correctly from the DB and can be stored?
	*/
	/**
	 * Indicates whether <code>this</code> can be stored to the database  */
	protected boolean updateable = false;

	/** Get the SQL for the <i>kind</i> of statement (INSERT, DELETE, etc.)
	*/
	/**
	 * Translates the <code>int</code> representation of a transaction type
	 * into a SQL String 
	 *
	 * @param kind indicates the transaction type (0 - 3)
	 *
	 * @return <code>"INSERT", "UPDATE", "DELETE,</code> or <code>GET</code>
	 *
	 * @see #INSERT
	 * @see #UPDATE
	 * @see #DELETE
	 * @see #GET */
	protected abstract String getSQL(int kind);

	/** Sets the out parameters for the key values 
		beginning at a specified index
		<i>startIndex</i>(e.g. startIndex of 2 becomes setInt(2)).
	*/
	/**
	 * Sets the out parameters for the key values beginning at a specified
	 * index
	 *
	 * @param stmt
	 * @param startIndex
	 *
	 * @exception SQLException */
	protected abstract void	setKeyParameters(CallableStatement stmt,
											 int startIndex)
		throws SQLException;

	/** Sets this entity's key value to that generated by the database.
		Called by create().  THIS SHOULD BE ABSTRACT.
	*/
	/**
	 * Sets this entity's key value to that generated by the database
	 *
	 * @param keyValue
	 *
	 * @exception SQLException */
	protected void setKey(int keyValue)
		throws SQLException {}

	/**	Construct the Entity.  The RS contains an value for each DB field
	*/
	/**
	 * Constructs <code>this</code> based on a given result set, which
	 * contains a value for each database field
	 *
	 * @param rs
	 *
	 * @exception SQLException */
	protected abstract void	fromDB(ResultSet rs) 
		throws SQLException;

	/**	Set the appropriate DB fields for the Entities attributes.  Called
		on UPDATE.
	*/
	/**
	 *
	 *
	 * @param cs
	 *
	 * @exception SQLException */
	protected abstract void	toDB(CallableStatement cs) 
		throws SQLException;

	/**	Set the Entities attributes from a FormData object.  All values must
		exist in the FormData.
	*/
	/**
	 *
	 *
	 * @param fd
	 *
	 * @exception DBException */
	public abstract void setFormData(FormData fd) 
		throws DBException;

	/** Are the key values for this Entity empty (probably 0)?  Keys must
		be empty to create (insert) and non-empty to update, get, delete.
	*/
	/**
	 * @return <code>true</code> if the key values for <code>this</code> are
	 * empty; <code>false</code> otherwise */
	protected abstract boolean emptyKeys();

	/** Overriden by subclasses to provide a search method for Entities.
		The filled attributes of the BaseEntity define which values
		to query by.  In essence this method is a server solution
		to get the results of all the static methods provided by the
		Entity subclasses.  By default we return a List object of
		size 0.
	*/
	/**
	 * Overriden by subclasses to provide a search method for Entities.
	 *
	 * @param e
	 *
	 * @return */
	public List getList(BaseEntity e) {
		return new ArrayList();
	}

	/** Was the Entity initialized (connection and key values set).
		Subclasses should provide an init() method which sets init=true.
	*/
	/**
	 * @return <code>true</code> if this <code>BaseEntity</code> has been
	 * initialized; <code>false</code> otherwise */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Sets the value of {@link #initialized}
	 *
	 * @param init the new value for <code>initialized</code>*/
	public void setInitialized(boolean init) {
		initialized = init;
	}

	/**
	 * Sets this entity's database connection, and sets
	 * <code>initialized</code> to <code>true</code>
	 *
	 * @param conn the new database connection */
	public void init(Connection conn) {
		connection = conn;
		initialized = true;
	}

	/** Was the Entity created such that it can be written.
	*/
	/**
	 * @return <code>true</code> if <code>this</code> was created such that it
	 * can be written; <code>false</code> otherwise */
	public boolean isUpdateable() {
		return updateable;
	}

	/**
	 * Sets the updatable property for this entity
	 *
	 * @param update */
	public void setUpdateable(boolean update) {
		updateable = update;
	}

	/**
	 * Creates a new <code>BaseEntity</code> with a given connection to the
	 * database
	 *
	 * @param conn the database connection for the new <code>BaseEntity</code>
	 */
	protected BaseEntity(Connection conn) {
		super();
		connection = conn;
		initialized = false;
		updateable = false;
	}

	/**
	 * Creates a new <code>BaseEntity</code> which is neither
	 * <code>initialized</code> nor <code>updateable</code>. The database
	 * connection is <code>null</code>. */
	protected BaseEntity() {
		initialized = false;
		updateable = false;
	}

	/** Get the Entity from the database using the entity's current
		values for keys and connection.  We use getSQL(GET) to
		retrieve the values and fromDB(ResultSet) to set the values.
		This method calls the abstract method setKeyParameters()
		which sets the key values before performing the query.
	*/
	/**
	 * Gets this entity from the database using the entity's current values
	 * for keys and connection
	 *
	 * @exception DBException */
	protected void get() 
		throws DBException
	{
		if (emptyKeys())
			throw new DBException(getClass()+": keys are empty");

		try {
			String sql = getSQL(GET);
			CallableStatement cstmt = connection.prepareCall(sql);
			cstmt.registerOutParameter(1, OracleTypes.CURSOR);

			setKeyParameters(cstmt, 2);		// start at index 2
			cstmt.execute();
			ResultSet rs = (ResultSet)cstmt.getObject(1);
			if (rs.next() == false)
				throw new DBException(getClass()+".get(): Empty ResultSet");
			fromDB(rs);
			rs.close();
			cstmt.close();
			updateable = true;
		} catch(SQLException se) {
			updateable = false;
			Debug.out.printStackTrace("db",se);
			throw new DBException(getClass()+":"+se.getMessage());
		}
	}

	/**	Update the database with attributes from the Entity.  The Entity
		must have been constructed correctly so that all values are
		set (updateable == true).
	*/
	/**
	 * Updates the database with attributes from this entity
	 *
	 * @exception DBException */
	public void	update() 
		throws DBException
	{
		if (emptyKeys())
			throw new DBException("Update: "+getClass()+" keys are empty");

		if (!isUpdateable())
			throw new DBException("Update: " +getClass()+
								  "Entity's updateable property is false");
			
		try {
			String sql = getSQL(UPDATE);
			CallableStatement cstmt = connection.prepareCall(sql);
			toDB(cstmt);
			cstmt.execute();
			cstmt.close();
		} catch(SQLException se) {
			Debug.out.printStackTrace("db",se);
			throw new DBException(getClass()+":"+se.getMessage());
		}
	}

	/**	Delete the entry in the database.  The Entity must have correct
		key values (!emptyKeys()) but does not need to be updateable.
	*/
	/**
	 * Deletes the entry for <code>this</code> in the database
	 *
	 * @exception DBException */
	public void	delete() 
		throws DBException
	{
		if (emptyKeys())
			throw new DBException("Delete: "+getClass()+" keys are empty");

		try {
			String sql = getSQL(DELETE);
			CallableStatement cstmt = connection.prepareCall(sql);
			setKeyParameters(cstmt, 1);
			cstmt.execute();
			cstmt.close();
		} catch(SQLException se) {
			Debug.out.printStackTrace("db",se);
			throw new DBException(getClass()+":"+se.getMessage());
		}
	}

	/**	Insert a row in the database using attributes from the Entity.
		We check the Entity's key values first (emptyKeys()==true).
		This method assumes the stored procedure returns the generated
		key for the table and we call setKey(int) to assign it.
	*/
	/**
	 * Inserts a row in the database using attributes from <code>this</code>
	 *
	 * @exception DBException */
	public void	create() 
		throws DBException
	{
		if (!emptyKeys())
			throw new DBException(getClass()+": keys must be empty");

		try {
			String sql = getSQL(INSERT);
			CallableStatement cstmt = connection.prepareCall(sql);
			toDB(cstmt);
			cstmt.execute();
			setKey(cstmt.getInt(1));
			cstmt.close();
			updateable = true;
		} catch(SQLException se) {
			Debug.out.printStackTrace("db",se);
			throw new DBException(getClass()+":"+se.getMessage());
		}
	}

	/**
	 * 
	 *
	 * @param cstmt
	 *
	 * @return
	 *
	 * @exception SQLException */
	protected boolean fromDB(CallableStatement cstmt) throws SQLException {
		ResultSet rs = (ResultSet)cstmt.getObject(1);
		if (rs.next() == false) 
			return false;
		fromDB(rs);
		return true;
	}

	/**
	 * Returns this <code>BaseEntity</code>'s database connection
	 *
	 * @return the current database connection */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets the database connection 
	 *
	 * @param c the new database connection for this <code>BaseEntity</code> */
	public void	setConnection(Connection c) {
		connection = c;
	}

	//************** UTLITY METHODS *****************/
	
	/**
	 *
	 *
	 * @param cstmt
	 * @param i
	 * @param s
	 *
	 * @exception SQLException */
	protected void setString(CallableStatement cstmt, int i, String s)
		throws SQLException 
	{
		if (s != null)
			cstmt.setString(i,s);
		else
			cstmt.setString(i,"not set");
	}

	/**
	 *
	 *
	 * @param cstmt
	 * @param i
	 *
	 * @exception SQLException */
	protected void setUpdateDate(CallableStatement cstmt, int i)
		throws SQLException 
	{
		cstmt.setDate(i++, new java.sql.Date(System.currentTimeMillis()));
	}

	/**
	 *
	 *
	 * @param fd
	 * @param property
	 *
	 * @return */
	protected int getInt(FormData fd, String property) {
		return Integer.parseInt(fd.getProperty(property));
	}

	/**
	 * 
	 *
	 * @param s
	 *
	 * @return */
	public String getStringVal(String s) { 
		if (s == null) 
			return ""; 
		return s; 
	}

	/**
	 * 
	 *
	 * @param d
	 *
	 * @return */
	public String getDateVal(Date d) {
		if (d == null) 
			return "";
		int mo = d.getMonth()+1;
		int da = d.getDate();
		int yr = d.getYear() + 1900;
		String displayValue = "" + mo + "/" + da + "/" + yr;
		return displayValue;
	}

	/**
	 * 
	 *
	 * @param t
	 *
	 * @return */
	public String getTimestampVal(Timestamp t) {
		if (t == null) 
			return "";
		int mo = t.getMonth()+1;
		int da = t.getDate();
		int yr = t.getYear() + 1900;
		int hr = t.getHours();
		int mi = t.getMinutes();
		String displayValue = "" + mo + "/" + da + "/" + yr + " ";
		if (hr < 10) 
			displayValue += "0";
		displayValue = (displayValue + hr) + ":";
		if (mi < 10) 
			displayValue += "0";
		displayValue = displayValue + mi;
		return displayValue;
	}

	/**
	 * 
	 *
	 * @param s
	 *
	 * @return */
	public String setStringVal(String s) {
		if ((s == null) || (s.equals(""))) 
			return null;
		return s;
	}

	/**
	 * 
	 *
	 * @param d
	 *
	 * @return */
	public Date setDateVal(String d) {
		if ((d == null) || (d.equals(""))) 
			return null;
		String date = d;
		if (d.indexOf("/") != -1) 
			date = reFormatDate(d);
		return Date.valueOf(date);
	}

	/**
	 * 
	 *
	 * @param t
	 *
	 * @return */
	public Timestamp setTimestampVal(String t) {
		if ((t == null) || (t.equals(""))) 
			return null;
		String datetime = t;
		if (t.indexOf("/") != -1) 
			datetime = reFormatDateTime(t);
		return Timestamp.valueOf(datetime);
	}

	/**
	 * 
	 *
	 * @param simpleDate
	 *
	 * @return
	 *
	 * @deprecated dates should be formatted using <code>DateFormat</code>
	 * for internationalization */
	public String reFormatDate(String simpleDate) {
		try {
			StreamTokenizer s = new StreamTokenizer(new StringReader(simpleDate));
			s.whitespaceChars('/','/');
			s.whitespaceChars(',',',');

			s.nextToken();  int mo = (int) s.nval;
			s.nextToken();  int da = (int) s.nval;
			s.nextToken();  int yr = (int) s.nval;

			String ret =  yr + "-";
			if (mo < 10) 
				ret += "0";
			ret = ret + mo + "-";
			if (da < 10) 
				ret += "0";
			ret = ret + da;
			return ret;
		} catch (Exception e) { 
			return ""; 
		}
	}

	/**
	 * 
	 *
	 * @param simpleDate
	 *
	 * @return
	 *
	 * @deprecated dates should be formatted using DateFormat
	 * for internationalization */
	public String reFormatDateTime(String simpleDate) {
		try {
			StreamTokenizer s = new StreamTokenizer(new StringReader(simpleDate));
			s.whitespaceChars('/','/');
			s.whitespaceChars(',',',');
			s.whitespaceChars(':',':');

			s.nextToken();  int mo = (int) s.nval;
			s.nextToken();  int da = (int) s.nval;
			s.nextToken();  int yr = (int) s.nval;
			s.nextToken();  int hr = (int) s.nval;
			s.nextToken();  int mi = (int) s.nval;

			String ret =  yr + "-";
			if (mo < 10) 
				ret += "0";
			ret = ret + mo + "-";
			if (da < 10) 
				ret += "0";
			ret = ret + da + " ";
			if (hr < 10) 
				ret += "0";
			ret = ret + hr + ":";
			if (mi < 10) 
				ret += "0";
			ret = ret + mi + ":00.000000000";
		
			return ret;
		} catch (Exception e) { 
			return ""; 
		}
	}

	/** 
	 * Displays the values for all attributes of this <code>BaseEntity</code>
	 */
	public void dump() {
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		System.out.println("Dumping " + this.getClass().getName() + fields.length);
		for (int i=0; i < fields.length; i++) {
		  try {
			  System.out.println("  " + fields[i].getName() + "=\t" + fields[i].get(this));
		  } catch (IllegalAccessException e) {
			  System.out.println("Dump error: " + e.getMessage());
		  }
		}
	}

	/**
	 * By default, same behavior as {@link #toStringComplete} */
	public String toString() {
		return toStringComplete();
	}

	/**
	 * Returns a String representation of this object in the format:
	 * <code>{name1=value1, name2=value2, ..., nameX=valueX}</code> */
 	public String toStringComplete() {
		StringBuffer result = new StringBuffer();
		result.append("{");
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		for (int i=0; i < fields.length; i++) {
			try {
				result.append(fields[i].getName());
				result.append("=");
				result.append(fields[i].get(this));
				result.append(", ");
			} catch (IllegalAccessException e) { } 
		}

		result.append("}");
		return result.toString();
	}
}
