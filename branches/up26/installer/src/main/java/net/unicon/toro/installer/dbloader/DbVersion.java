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
package net.unicon.toro.installer.dbloader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.util.Properties;

/**
 DbVersion will query the db and driver for
 their respective versions.
 **/
public class DbVersion {

  private DbVersion() {
  }
  
  public static void usage() {
      System.out.println("Usage: DbVersion <driver-class> <db-url> <db-user> <db-password>");
  }

  public static void main(String[] args) {
      if (args.length != 4) {
          usage();
          System.exit(-1);
      }
      String driverClass = args[0];
      String url = args[1];
      String user = args[2];
      String password = args[3];
      
      try {
          Driver driver = (Driver)Class.forName(driverClass).newInstance();
          Properties tempProperties = new Properties();
          tempProperties.put("user", user);
          tempProperties.put("password", password);
          Connection conn = driver.connect(url, tempProperties);
          
          if (conn == null) {
              System.out.println("Failed to get a connection to db: url");
              System.exit(-1);
          }
          DatabaseMetaData dbMetaData = conn.getMetaData();
          String dbName = dbMetaData.getDatabaseProductName();
          String dbVersion = dbMetaData.getDatabaseProductVersion();
          String driverName = dbMetaData.getDriverName();
          String driverVersion = dbMetaData.getDriverVersion();
          
          System.out.println("    <db-name>"+dbName+"</db-name>");
          System.out.println("    <db-version>"+dbVersion+"</db-version>");
          System.out.println("    <driver-name>"+driverName+"</driver-name>");
          System.out.println("    <driver-version>"+driverVersion+"</driver-version>");
          
      } catch (Exception e) {
          System.out.println(e.getMessage());
          usage();
          System.exit(-1);
      }
  }
}
