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



import java.sql.*;

import java.util.*;

import java.io.*;




import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;



public class FormData extends XMLData {



  public void putFormId(Integer formId) {

    putA("FormId", formId);

  }

  public void setFormId(Integer formId) {
      putFormId(formId);
  }

  public Integer getFormId() {

    return (Integer)getA("FormId");

  }


  public void setTitle(String title) {
      putTitle(title);
  }

  public void putTitle(String title) {

    putA("Title", title);

  }

  public String getTitle() {

    return (String)getA("Title");

  }


  public void setType(String type) {
      putType(type);
  }

  public void putType(String type) {

    putA("Type", type);

  }

  public String getType() {

    return (String)getA("Type");

  }


  public void setCreated(java.util.Date created) {
      putCreated(created);
  }

  public void putCreated(java.util.Date created) {

    putA("Created", created);

  }

  public java.util.Date getCreated() {

    return (java.util.Date)getA("Created");

  }


  public void setModified(java.util.Date modified) {
      putModified(modified);
  }


  public void putModified(java.util.Date modified) {

    putA("Modified", modified);

  }

  public java.util.Date getModified() {

    return (getA("Modified")==null) ? null : (java.util.Date)getA("Modified");

  }

  public void setUserId(String userId) {
      putUserId(userId);
  }

  public void putUserId(String userId) {

    putA("UserId", userId);

  }

  public String getUserId() {

    return (String)getA("UserId");

  }

  public void setUserName(String userName) {
      putUserName(userName);
  }

  public void putUserName(String userName) {

    putA("UserName", userName);

  }

  public String getUserName() {

    return (String)getA("UserName");

  }

  public String getXslForm() {
      return null;
  }

  public String getXmlForm() {
      return null;
  }

  public void setXmlForm(String form) {
  }

  public void setXslForm(String form) {
  }

  public void putXMLForm(TextFileData xmlForm) {

    putE("XMLForm", xmlForm);

  }

  public TextFileData getXMLForm() {

    return (TextFileData)getE("XMLForm");

  }



  public void putXSLForm(TextFileData xslForm) {

    putE("XSLForm", xslForm);

  }

  public TextFileData getXSLForm() {

    return (TextFileData)getE("XSLForm");

  }



  public void putSurvey(SurveyData[] survey) {

    putE("Survey", survey);

  }

  public SurveyData[] getSurvey() {

    return (SurveyData[])getE("Survey");

  }



  public static FormData parse(ResultSet rs) throws Exception {

    FormData fd = new FormData();

    fd.putFormId(new Integer(rs.getInt("FORM_ID")));

    fd.putTitle(rs.getString("TITLE"));

    fd.putType(rs.getString("TYPE"));

    fd.putXMLForm(TextFileData.parseFilePath(rs.getString("XML")));

    fd.putXSLForm(TextFileData.parseFilePath(rs.getString("XSL")));

    fd.putCreated(rs.getTimestamp("CREATED"));

    fd.putModified(rs.getTimestamp("MODIFIED"));

    fd.putUserId(rs.getString("USER_ID"));

    fd.putUserName(rs.getString("USER_NAME"));

    return fd;

  }

}

