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
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Timestamp;

import net.unicon.academus.apps.rad.SQL;

/**
 * This class is intended for db tools such as converting data format.
 * It implements the DBService to provide the database access.
 */
public class Rdbm implements DBService {
  public static int RETRY_COUNT = 5;
  private String m_sJdbcDriver = null;
  private String m_sJdbcUrl = null;
  private String m_sJdbcUser = null;
  private String m_sJdbcPassword = null;

  // member
  Connection m_con = null;
  Statement m_stmt = null;
  boolean m_oldAutoCommit = false;

  public Rdbm( Properties rdbmProps) {
    m_sJdbcDriver = rdbmProps.getProperty("jdbcDriver");
    m_sJdbcUrl = rdbmProps.getProperty("jdbcUrl");
    m_sJdbcUser = rdbmProps.getProperty("jdbcUser");
    m_sJdbcPassword = rdbmProps.getProperty("jdbcPassword");
  }

  protected void finalize() throws Throwable {
    if( m_con != null)
      release();
  }

  //-------- DBService ------------------//
  public int getNextValue(String table, String field) throws Exception {
    return 0;
  }
  public int getNextValue(String table, String field, String cond) throws Exception {
    return 0;
  }

  public synchronized ResultSet select(String sql)
  throws Exception {
    m_stmt = m_con.createStatement();
    m_stmt.execute(sql);
    return m_stmt.getResultSet();
  }

  public synchronized int update( String sql)
  throws Exception {
    try {
      m_stmt = m_con.createStatement();
      int iRet = m_stmt.executeUpdate(sql);
      return iRet;
    } finally {
      closeStatement();
    }
  }

  public synchronized PreparedStatement prepareStatement(String sql)
  throws Exception {
    return m_con.prepareStatement(sql);
  }

  public synchronized int executePreparedStatement(PreparedStatement ps)
  throws Exception {
    return ps.executeUpdate();
  }

  public void closeStatement() throws Exception {
    if (m_stmt != null) m_stmt.close();
    m_stmt = null;
  }

  public void begin() throws Exception {
    if( m_con != null)
      release();

    m_con = getConnection();
    m_oldAutoCommit = m_con.getAutoCommit();
    m_con.setAutoCommit(false);
  }

  public void commit() throws Exception {
    m_con.commit();
  }

  public void rollback(String msg) throws Exception {
    m_con.rollback();
    m_con.setAutoCommit(m_oldAutoCommit);
    throw new Exception(msg);
  }

  public void release() throws Exception {
    try {
      closeStatement();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (m_con != null)
      m_con.close();
    m_con = null;
  }

  //=====================================================================//
  /**
   * Gets a database connection
   * @return a database Connection object
   */
  public Connection getConnection () {
    Connection conn = null;
    for (int i = 0; i < RETRY_COUNT && conn == null; ++i) {
      try {
        Class.forName(m_sJdbcDriver).newInstance();
        conn = DriverManager.getConnection(m_sJdbcUrl, m_sJdbcUser, m_sJdbcPassword);
      } catch (ClassNotFoundException cnfe) {
        System.out.println("The driver " + m_sJdbcDriver + " was not found, please check the rdbm.properties file and your classpath.");
        return null;
      } catch (InstantiationException ie) {
        System.out.println("The driver " + m_sJdbcDriver + " could not be instantiated, please check the rdbm.properties file.");
        return null;
      } catch (IllegalAccessException iae) {
        System.out.println("The driver " + m_sJdbcDriver + " could not be instantiated, please check the rdbm.properties file.");
        return null;
      } catch (SQLException SQLe) {
        SQLe.printStackTrace();
      }
    }
    return  conn;
  }

  /**
   * Releases database connection
   * @param a database Connection object
   */
  public static void releaseConnection (Connection con) {
    try {
      if (con != null)
        con.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
