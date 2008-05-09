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


package net.unicon.academus.apps.rad;

import java.sql.*;

/**
 * An interface to provide relational database access through JDBC.
 * It simplifies the database access.
 */
public interface DBService {
  /**
   * Execute the select query and return its result.
   * @param sql the select query to execute.
   * @return The standard java.sql.ResultSet that is the result of given query.
   */
  ResultSet select(String sql) throws Exception;

  /**
   * This method is reserved for the future. It works as an "ID generator".
   * It will call the method:
   *  int getNextValue(String table, String field, String cond) throws Exception;
   * with the parameter cond is null.
   */
  int getNextValue(String table, String field) throws Exception;

  /**
   * This method is reserved for the future. It works as an "ID generator".
   * @param table The name of table for which the next id will generated.
   * @param field The field name of the above table, on which the next id will generated. This field must have a number type.
   * @param cond The "where" filter.
   * @return The next id of given field that can be used as the next id for given table.
   * @throws Exception if there is a SQLException.
   */
  int getNextValue(String table, String field, String cond) throws Exception;

  /**
   * Execute the update (UPDATE/INSERT/DELETE) query and return the number of records updated/inserted/deleted.
   * @param sql The update query to execute.
   * @return The number of updated/inserted/deleted records.
   * @throws Exception if there is a SQLException.
   */
  int update( String sql) throws Exception;

  /**
   * Prepare a query to be used as PreparedStatement.
   * @param sql The query to be prepared.
   * @return The standard PreparedStatement.
   * @throws Exception if there is a SQLException.
   */
  PreparedStatement prepareStatement(String sql) throws Exception;

  /**
   * Execute an update PreparedStatement.
   * @param ps The PreparedStatement to be executed.
   * @return The number of updated/inserted/deleted records.
   * @throws Exception if there is a SQLException.
   */
  int executePreparedStatement(PreparedStatement ps) throws Exception;

  /**
   * This method together with the methods commit(), rollback() provide an transactional database access.
   * The standard usage is:
   *   DBService db = <obtains the object implementing this interface>;
   *   db.begin();
   *   try {
   *     <Execute some update/select queries via this interface>
   *     ....
   *
   *     // Commit all changes
   *     db.commit();
   *   }
   *   catch( Exception e) {
   *     // rollback
   *     e.printStackTrace();
   *     db.rollback(e.getMessage());
   *   }
   *   finally {
   *    // Release any resouces used by this database connection.
   *     db.release();
   *   }
   *
   */
  void begin() throws Exception;

  /**
   * @see begin()
   */
  void commit() throws Exception;

  /**
   * @see begin()
   */
  void rollback(String msg) throws Exception;

  /**
   * Close statement
   */
  void closeStatement() throws Exception;

  /**
   * Release any resouces used by this database connection.
   */
  void release() throws Exception;
}
