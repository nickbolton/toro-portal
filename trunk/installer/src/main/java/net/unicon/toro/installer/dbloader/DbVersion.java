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
