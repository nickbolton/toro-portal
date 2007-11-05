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

import java.sql.*;

import java.util.*;





import net.unicon.academus.apps.form.FormDataData;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;



public class SurveyData extends XMLData {

  // hibernate stubs
  public void setSurveyId(int i) {}
  public void setDataId(int i) {}
  public void setFormId(int i) {}
  public void setType(String s) {}
  public void setUserId(String s) {}
  public void setUserName(String s) {}
  public void setDistributionTitle(String s) {}
  public void setSent(java.util.Date d) {}
  public void setClosed(java.util.Date d) {}
  public int getDataId() { return 0; }

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



  public void putTargetSize(Integer targetSize) {

    putA("TargetSize", targetSize);

  }

  public Integer getTargetSize() {

    return (Integer)getA("TargetSize");

  }



  public void putSent(java.util.Date sent) {

    putA("Sent", sent);

  }

  public java.util.Date getSent() {

    return (java.util.Date)getA("Sent");

  }



  public void putClosed(java.util.Date closed) {

    putA("Closed", closed);

  }

  public java.util.Date getClosed() {

    return (java.util.Date)getA("Closed");

  }



  public void putReplied(Integer replied) {

    putA("Replied", replied);

  }

  public Integer getReplied() {

    return (Integer)getA("Replied");

  }



  public void putLastReply(java.util.Date lastReply) {

    putA("LastReply", lastReply);

  }

  public java.util.Date getLastReply() {

    return (java.util.Date)getA("LastReply");

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



  public void putTarget(IdentityData[] idr) {

    putE("Target",idr);

  }

  public IdentityData[] getTarget() {

    return (IdentityData[])getE("Target");

  }



  public void putRecipient(FormDataData[] fdd) {

    putE("Recipient",fdd);

  }

  public FormDataData[] getRecipient() {

    return (FormDataData[])getE("Recipient");

  }



  public void putSummary(SummaryData sd) {

    putE("Summary",sd);

  }

  public SummaryData getSummary() {

    return (SummaryData)getE("Summary");

  }
  
  public void putDistributionTitle (String distributionTitle)
  {
  	putA("DistributionTitle", distributionTitle);
  }

  public String getDistributionTitle ()
  {
  	return (String)getA("DistributionTitle");
  }


  public static SurveyData parse(ResultSet rs) throws Exception {

    SurveyData sd = new SurveyData();

    sd.putSurveyId(new Integer(rs.getInt("SURVEY_ID")));

    sd.putFormId(new Integer(rs.getInt("FORM_ID")));

    sd.putType(rs.getString("TYPE"));

    sd.putSent(rs.getTimestamp("SENT"));

    sd.putClosed(rs.getTimestamp("CLOSED"));

    sd.putUserId(rs.getString("USER_ID"));

    sd.putUserName(rs.getString("USER_NAME"));

    sd.putReplied(new Integer(rs.getInt("NO")));
   
    sd.putDistributionTitle(rs.getString("DISTRIBUTION_TITLE"));

    return sd;

  }

}

