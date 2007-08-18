/*
 * @(#)DbParameter.java 12-5-2000	
 * @author Unicon, Inc
 * @version 1.0
 *
 * Parameter class for storing the in and out parameters of a statement.
 * This class will make it easier to store the parameters and regenerate
 * a statement if an error occurs.
 */

package net.unicon.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import net.unicon.util.Debug;

public class DbParameter
{
	private Object obj = null;
	private Calendar calTemplate = null;
	private int functionType = 0;
	private int length = 0;
	private int pIndex = 0;
	private int targetType = 0;
	private String typeName;

	/**************************************************************************
	 * Constructor
	 * 
	 * @param index - parameter index
	 * @param o - object reference
	 * @param len - length or scale of parameter
	 * @param target - sql type of the target
	 * @param fun - set function 
	 * @param cal - calendar template for setting locale and timezone
	 * @param name - user define type name
	 */
	public DbParameter (int index, Object o, int len, int target, int fun, 
		Calendar cal, String name) {

		pIndex = index;
		obj = o;
		length = len;
		targetType = target;
		functionType = fun;
		calTemplate = cal;
		typeName = name;
	}

	/**************************************************************************
	 * Gets the parameter index value.
	 *
	 * @return the parameter index.
	 */
	public int getIndex() {
		return pIndex;
	}

	/**************************************************************************
	 * Gets the object of the parameter.
	 *
	 * @return the object of the parameter.
	 */
	public Object getObject() {
		return obj;
	}

	/**************************************************************************
	 * Gets the length or scale of the parameter.
	 *
	 * @return the length or scale of the parameter.
	 */
	public int getLength() {
		return length;
	}

	/**************************************************************************
	 * Gets the function number that this parameter is used for.
	 *
	 * @return the function number of the parameter.
	 */
	public int getFunction() {
		return functionType;
	}

	/**************************************************************************
	 * Gets the target sql type of the parameter.
	 * 
	 * @return the target sql type
	 */
	public int getTargetSqlType() {
		return targetType;
	}

	/**************************************************************************
	 * Gets the calendar template of the parameter.
	 *
	 * @return the calendar
	 */
	public Calendar getCalendar() {
		return calTemplate;
	}

	/**************************************************************************
	 * Gets the type name of the parameter.  This is only used in the setNull 
	 * function.
	 *
	 * @return the SQL user-named type
	 */
	public String getTypeName() {
		return typeName;
	}
}
