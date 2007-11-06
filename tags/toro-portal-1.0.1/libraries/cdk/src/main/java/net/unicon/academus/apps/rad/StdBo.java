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
import java.util.*;
import java.text.*;

import javax.naming.*;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

/**
 * The base class for all EJB classes used in RAD framework. It is a session bean.
 * It implements the most of methods in the SessionBean interface and provides
 * a way to access database through the DBService interface.
 */
public class StdBo implements SessionBean {
  /**
   * Debug option: 0:none, 1: console
   */
  public static int m_debug = 0;

  /**
   * The JNDI context
   */
  protected static Context m_ctxEnv = null;

  /**
   * data source cache
   */
  protected static Hashtable m_dsCache = new Hashtable();

  /**
   * The name of EJB. This nam must be unique in RAD framework.
   */
  protected String m_ejbName = null;

  /**
   * Session bean context
   */
  protected SessionContext m_sessionCtx;

  /**
   * EJB properties are stored in this variable.
   */
  protected Hashtable m_props = new Hashtable();

  public StdBo( String name) {
    super();
    m_ejbName = name;
  }

  //----Interface SessionBean ----------------------------------------------//

  /**
   * Implements the method ejbCreate() in SessionBean interface
   */
  public void ejbCreate() {
    trace("==== EJB " + m_ejbName + " created ===");
  }

  /**
   * Implements the method ejbRemove() in SessionBean interface
   */
  public void ejbRemove() {
    trace("#### EJB " + m_ejbName + " removed ####");
  }

  /**
   * Implements the method ejbActivate() in SessionBean interface.
   */
  public void ejbActivate() {}

  /**
   * Implements the method ejbPassivate() in SessionBean interface.
   */
  public void ejbPassivate() {}

  /**
   * Implements the method setSessionContext() in SessionBean interface.
   */
  public void setSessionContext(SessionContext ctx) {
    m_sessionCtx = ctx;
  }

  //----------------------------------------------------------------------//

  /**
   * Change EJB property
   * @param name The name of property to be change its value.
   * @param value The value to change to.
   */
  public void putProperty( String name, Object value) {
    if( value != null)
      m_props.put(name,value);
    else
      m_props.remove(name);
  }

  /**
   * Get the value of EJB property. The EJB properties include those properties
   * set by method putProperty() and those looked up in the JNDI environment context.
   * @param name The name of property to be get its value.
   * @return The value of EJB property.
   */
  public Object getProperty( String name) {
    Object ob = m_props.get(name);
    if( ob == null)
      ob = getEnvProperty( name);
    return ob;
  }

  public static java.util.Date getCurrentDate() {
    return new java.util.Date();
  }

  //----------------------------------------------------------------------//

  /**
   * Look up a value of EJB property in the JNDI environment context.
   * @param name The name of EJB property to be get its value.
   * @return The value of property or null if given property not found in the environment context.
   */
  protected Object getEnvProperty( String name) {
    // Cache environment context
    if( m_ctxEnv == null)
      try {
        m_ctxEnv = (Context) (new InitialContext()).lookup("java:comp/env");
      } catch (NamingException ne) {
        trace( ne);
        return null;
      }

    if( m_ctxEnv != null)
      try {
        return m_ctxEnv.lookup(name);
      } catch (NamingException ne) {
        trace( ne);
      }

    return null;
  }

  /**
   * Look up a data source by its name in the JNDI context.
   * @param ds The name of data source to be looked up.
   * @return The required data source or null if not found.
   */
  protected static DataSource getDS( String ds) {
    DataSource ob = (DataSource)m_dsCache.get(ds);
    if( ob == null) {
      Context ctx = null;
      try {
        ctx = new InitialContext();
      } catch (NamingException ne) {
        return null;
      }

      try {
        ob = (DataSource) ctx.lookup(ds);
      } catch (NamingException ne) {}
      if( ob == null)
        try {
          ob = (DataSource) ctx.lookup("java:/"+ds);
        } catch (NamingException ne) {}

      if( ob == null)
        try {
          ob = (DataSource) ctx.lookup("java:comp/env/"+ds);
        } catch (NamingException ne) {}

      if( ob == null)
        try {
          ob = (DataSource) ctx.lookup("java:comp/env/jdbc/"+ds);
        } catch (NamingException ne) {}

      if( ob != null)
        m_dsCache.put(ds,ob);
    }

    return ob;
  }

  /**
   * Provides a way to access to database through the DBService interface.
   * @param ds The name of data source. The actual data source name will be
   * taken from the map when deploying the EJB.
   * @return The object implementing the interface DBService.
   * @throws Exception if there is not found the data source or
   * a SQLException occurs when getting a database connection.
   */
  protected DBService getDBService(String ds) throws Exception {
    // map from code's ds to external DS
    String jndi = (String)getProperty(ds);
    return new StdDBService( jndi!=null?jndi:ds);
  }

  //-----------------------------------------------------------------------//
  static Hashtable m_hkey = new Hashtable();

  /**
   * Inner class implements the DBService interface.
   * @see DBService
   */
  class StdDBService implements DBService {
    java.sql.Connection m_conn = null;
    Statement m_stmt = null;
    boolean m_oldAutoCommit;

    protected void finalize() throws Throwable {
      if( m_stmt != null) {
        m_stmt.close();
        m_stmt = null;
      }

      if( m_conn != null) {
        m_conn.close();
        m_conn = null;
      }
    }

    public StdDBService(String ds) throws Exception {
      // Get DataSource
      DataSource ods = getDS(ds);
      if( ods == null)
        throw new Exception("Could not lookup data source for "+ds);
      m_conn = ods.getConnection();
    }

    public synchronized ResultSet select(String sql)
    throws Exception {
      trace("***SQL***");
      trace(sql);
      trace("*********");
      if( m_stmt != null)
        m_stmt.close();
      m_stmt = m_conn.createStatement();
      m_stmt.execute(sql);
      return m_stmt.getResultSet();
    }

    public int getNextValue(String table, String field) throws Exception // Thach Mar-13
    {
      return getNextValue(table,field,null);
    }

    public int getNextValue(String table, String field, String cond) throws Exception // Thach Mar-13
    {
      int max = 0;
      try {
        String sql = (cond==null)? "SELECT MAX("+field+") FROM "+table:
                     "SELECT MAX ("+field+") FROM "+table+" WHERE "+cond;
        ResultSet rs = null;

        try {
          rs = select(sql);
          if (rs.next())
            max = rs.getInt(1);
        } finally {
          try {
            if (rs != null) rs.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          closeStatement();
        }
        if( max < 0)
          max = 0;
      } catch(Exception e) {
        throw e;
      } finally {
        //release(); //it is release on function used it
      }


      // Check in Hash key
      String key = table + "." + field + (cond != null?"." + cond:"");
      synchronized (m_hkey) {
        Integer value = (Integer)m_hkey.get(key);
        if (value != null) {
          int iv = value.intValue();
          // max of iv and ret
          max = (iv > max?iv:max);
        }
        max++;
      }

      m_hkey.put(key, new Integer(max));
      return max;
    }

    public void closeStatement() throws Exception {
      if( m_stmt != null) {
        m_stmt.close();
        m_stmt = null;
      }
    }

    public synchronized void release()
    throws Exception {

      try {
        closeStatement();
      } catch (Exception e) {
        e.printStackTrace();
      }

      if( m_conn != null) {
        m_conn.close();
        m_conn = null;
      }
    }

    public synchronized int update( String sql)
    throws Exception {
      trace("***SQL***");
      trace(sql);
      trace("*********");
      if( m_stmt != null)
        m_stmt.close();
      m_stmt = m_conn.createStatement();
      int iRet = m_stmt.executeUpdate(sql);
      m_stmt.close();
      m_stmt = null;
      return iRet;
    }

    public synchronized PreparedStatement prepareStatement(String sql)
    throws Exception {
      trace("***SQL***");
      trace(sql);
      trace("*********");
      return m_conn.prepareStatement(sql);
    }

    public synchronized int executePreparedStatement(PreparedStatement ps)
    throws Exception {
      return ps.executeUpdate();
    }

    public void begin() throws Exception {
      m_oldAutoCommit = m_conn.getAutoCommit();
      m_conn.setAutoCommit(false);
    }

    public void commit() throws Exception {
      m_conn.commit();
      m_conn.setAutoCommit(m_oldAutoCommit);
    }

    public void rollback(String msg) throws Exception {
      trace(msg);
      m_conn.rollback();
      m_conn.setAutoCommit(m_oldAutoCommit);
      throw new Exception(msg);
    }
  }

  //-------------------------------------------------------------//

  /**
   * Logs a message. The output is dependent of debug option.
   * To turn off the log, set debug option to 0.
   * @param trace String to be loged.
   */
  static public void trace(String trace) {
    if( m_debug != 0)
      System.out.println(trace);
  }

  /**
   * Logs Throwable object. The output is dependent of debug option.
   * To turn off the log, set debug option to 0.
   * @param trace Throwable object to log. Currently this method will dump
   * stack trace on standard output if debug option is on.
   */
  static public void trace(Throwable trace) {
    if( m_debug != 0)
      trace.printStackTrace();
  }
}
