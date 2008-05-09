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
package net.unicon.academus.apps.survey;

import java.util.*;

import java.sql.*;

import java.io.FileInputStream;






import net.unicon.academus.apps.rad.DBService;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Rdbm;
import net.unicon.academus.apps.rad.SQL;



public class Survey {


  public static void main(String[] args) {

    if(args.length<3) {

      System.out.println("Usage: Survey [rdbm.properties file] [fromVersion] [toVersion]");

      return;

    }



    DBService db = null;

    try {

      // Get parameters

      Properties props = new Properties();

      props.load(new FileInputStream(args[0]));

      String from = args[1];

      String to = args[2];

      int count = 0, id;

      String sql, ident, newIdent;

      ResultSet rs;

      // Convert

      db = new Rdbm(props);

      db.begin();



      sql = "SELECT FORM_ID, USER_ID FROM UPC_FORM_FORM";

      rs = db.select(sql);

      while(rs.next()) {

        id = rs.getInt(1);

        ident = rs.getString(2);

        newIdent = (new IdentityData(ident)).getIdentifier();

        if(!newIdent.equals(ident)) {

          sql = "UPDATE UPC_FORM_FORM SET USER_ID="+SQL.esc(newIdent)+" WHERE FORM_ID="+id;

          db.update(sql);

          count++;

        }

      }



      sql = "SELECT DATA_ID, USER_ID FROM UPC_FORM_DATA";

      rs = db.select(sql);

      while(rs.next()) {

        id = rs.getInt(1);

        ident = rs.getString(2);

        newIdent = (new IdentityData(ident)).getIdentifier();

        if(!newIdent.equals(ident)) {

          sql = "UPDATE UPC_FORM_DATA SET USER_ID="+SQL.esc(newIdent)+" WHERE DATA_ID="+id;

          db.update(sql);

          count++;

        }

      }



      sql = "SELECT SURVEY_ID, USER_ID FROM UPC_SURVEY_SURVEY";

      rs = db.select(sql);

      while(rs.next()) {

        id = rs.getInt(1);

        ident = rs.getString(2);

        newIdent = (new IdentityData(ident)).getIdentifier();

        if(!newIdent.equals(ident)) {

          sql = "UPDATE UPC_SURVEY_SURVEY SET USER_ID="+SQL.esc(newIdent)+" WHERE SURVEY_ID="+id;

          db.update(sql);

          count++;

        }

      }



      sql = "SELECT SURVEY_ID, OBJECT FROM UPC_SURVEY_TARGET";

      rs = db.select(sql);

      while(rs.next()) {

        id = rs.getInt(1);

        ident = rs.getString(2);

        newIdent = (new IdentityData(ident)).getIdentifier();

        if(!newIdent.equals(ident)) {

          sql = "UPDATE UPC_SURVEY_TARGET SET OBJECT="+SQL.esc(newIdent)+" WHERE SURVEY_ID="+id+" AND OBJECT="+SQL.esc(ident);

          db.update(sql);

          count++;

        }

      }



      db.commit();

      System.out.println("Convert "+count+" records successfully.");

    } catch(Exception e) {

      e.printStackTrace();

      if(db!=null)

        try {

          db.rollback(e.getMessage());

        } catch(Exception ex) {}

      System.exit(-1);

    } finally {
        try {
          if (db != null) db.release();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

  }

}

