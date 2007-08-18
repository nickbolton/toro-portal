package net.unicon.db;

/**
 * Thrown when an error occurs accessing the database
 *
 * @author Unicon, Inc.
 * @version 3.0 */
public class DBException extends net.unicon.util.RootException
{
	/**
	 * Creates a new <code>DBException</code> with a specified message
	 *
	 * @param s the message associated with this exception */
	public DBException(String s)
	{ super(s); }

	/**
	 * Creates a new <code>DBException</code> with a specified message and
	 * severity level
	 *
	 * @param s the message associated with this exception
	 * @param sev the severity of the new exception (0 - 3)*/
	public DBException(String s, int sev)
	{ super(s, sev); }
}
