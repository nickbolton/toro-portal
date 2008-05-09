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




import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;



public class TargetData extends XMLData {



  public void putSurveyId(Integer surveyId) {

    putA("SurveyId", surveyId);

  }

  public Integer getSurveyId() {

    return (Integer)getA("SurveyId");

  }



  public void putObject(IdentityData idr) {

    putE("Object",idr);

  }

  public IdentityData getObject() {

    return (IdentityData)getE("Object");

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



  public static TargetData parse(ResultSet rs) throws Exception {

    TargetData td = new TargetData();

    td.putSurveyId(new Integer(rs.getInt("SURVEY_ID")));

    IdentityData id = new IdentityData(rs.getString("OBJECT"));

    td.putObject(id);

    return td;

  }

}

