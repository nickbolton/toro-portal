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
package net.unicon.academus.apps.form;



import java.util.*;

import java.sql.*;




import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;



public class FormDataData extends XMLData {


  /* hibernate stubs */
  public void setDataId(Integer i) {
  }
  public void setFormId(Integer i) {
  }
  public void setType(String s) {
  }
  public void setUserId(String s) {
  }
  public void setUserName(String s) {
  }
  public void setCreated(java.util.Date d) {
  }
  public void setModified(java.util.Date d) {
  }

  public void putDataId(Integer dataId) {

    putA("DataId", dataId);

  }

  public Integer getDataId() {

    return (Integer)getA("DataId");

  }



  public void putSurveyId(Integer surveyId) {

    putA("SurveyId", surveyId);

  }

  public Integer getSurveyId() {

    return (Integer)getA("SurveyId");

  }



  public void putFormId(Integer formId) {

    putA("FormId", formId);

  }

  public Integer getFormId() {

    return (Integer)getA("FormId");

  }



  public void putType(String type) {

    putA("Type", type);

  }

  public String getType() {

    return (String)getA("Type");

  }



  public void putCreated(java.util.Date created) {

    putA("Created", created);

  }

  public java.util.Date getCreated() {

    return (java.util.Date)getA("Created");

  }



  public void putModified(java.util.Date modified) {

    putA("Modified", modified);

  }

  public java.util.Date getModified() {

    return (java.util.Date)getA("Modified");

  }



  public void putUserId(String userId) {

    putA("UserId", userId);

  }

  public String getUserId() {

    return (String)getA("UserId");

  }



  public void putUserName(String userName) {

    putA("UserName", userName);

  }

  public String getUserName() {

    return (String)getA("UserName");

  }



  public void putResponse(DataDetailData[] dddr) {

    putE("response", dddr);

  }

  public DataDetailData[] getResponse() {

    return (DataDetailData[])getE("response");

  }



  public static FormDataData parse(ResultSet rs) throws Exception {

    FormDataData fdd = new FormDataData();

    fdd.putDataId(new Integer(rs.getInt("DATA_ID")));

    fdd.putFormId(new Integer(rs.getInt("FORM_ID")));

    fdd.putType(rs.getString("TYPE"));

    fdd.putCreated(rs.getTimestamp("CREATED"));

    fdd.putModified(rs.getTimestamp("MODIFIED"));

    fdd.putUserId(rs.getString("USER_ID"));

    fdd.putUserName(rs.getString("USER_NAME"));

    return fdd;

  }

}

