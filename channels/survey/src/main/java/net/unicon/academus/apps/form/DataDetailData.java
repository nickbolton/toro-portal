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



import net.unicon.academus.apps.rad.XMLData;



public class DataDetailData extends XMLData {


  // hibernate stubs
  public void setDataId(int i) {
  }
  public void setPageId(int i) {
  }
  public void setQuestionId(int i) {
  }
  public void setInputId(int i) {
  }
  public void setDataNumber(int i) {
  }
  public int getDataNumber() {
      return 0;
  }
  public void setDataText(String s) {
  }
  public void setDataDate(java.util.Date d) {
  }
  public java.util.Date getDataDate() {
      return null;
  }
  public void setDataChoices(String s) {
  }


  public void putDataId(Integer dataId) {

    putA("DataId", dataId);

  }

  public Integer getDataId() {

    return (Integer)getA("DataId");

  }



  public void putPageId(Integer pageId) {

    putA("page-id", pageId);

  }

  public Integer getPageId() {

    return (Integer)getA("page-id");

  }



  public void putQuestionId(Integer questionId) {

    putA("question-id", questionId);

  }

  public Integer getQuestionId() {

    return (Integer)getA("question-id");

  }



  public void putInputId(Integer inputId) {

    putA("input-id", inputId);

  }

  public Integer getInputId() {

    return (Integer)getA("input-id");

  }



  public void putDataChoices(String dataChoices) {

    putA("DataChoices", dataChoices);

  }

  public String getDataChoices() {

    return (String)getA("DataChoices");

  }



  public void putDataChoiceId(Integer dataChoiceId) {

    putA("data-id", dataChoiceId);

  }

  public Integer getDataChoiceId() {

    return (Integer)getA("data-id");

  }



  public void putDataText(String dataText) {

    putA("data-text", dataText);

  }

  public String getDataText() {

    return (String)getA("data-text");

  }



  public void putNumResp(Integer numResp) {

    putA("number-responses", numResp);

  }

  public Integer getNumResp() {

    return (Integer)getA("number-responses");

  }



  public void putPercent(String percent) {

    putA("percent", percent);

  }

  public String getPercent() {

    return (String)getA("percent");

  }



  public static DataDetailData parse(ResultSet rs) throws Exception {

    DataDetailData ddd = new DataDetailData();

    int dataId = rs.getInt("DATA_ID");

    int pageId = rs.getInt("PAGE_ID");

    int questionId = rs.getInt("QUESTION_ID");

    int inputId = rs.getInt("INPUT_ID");

    String dataText = rs.getString("DATA_TEXT");

    Timestamp dataDate = rs.getTimestamp("DATA_DATE");

    int dataNumber = rs.getInt("DATA_NUMBER");

    String dataChoices = rs.getString("DATA_CHOICES");

    ddd.putDataId(new Integer(dataId));

    ddd.putPageId(new Integer(pageId));

    ddd.putQuestionId(new Integer(questionId));

    ddd.putInputId(new Integer(inputId));

    ddd.putDataText(dataText);

    ddd.putDataChoices(dataChoices);

    return ddd;

  }



  public static Vector parseToVector(ResultSet rs) throws Exception {

    Vector v = new Vector();



    int dataId = rs.getInt("DATA_ID");

    int pageId = rs.getInt("PAGE_ID");

    int questionId = rs.getInt("QUESTION_ID");

    int inputId = rs.getInt("INPUT_ID");

    String dataText = rs.getString("DATA_TEXT");

    String dataChoices = rs.getString("DATA_CHOICES");



    if(dataChoices != null) {

      for(int i=0; i<dataChoices.length(); i++)

        if(dataChoices.charAt(i)=='1') {

          DataDetailData ddd = new DataDetailData();

          ddd.putDataId(new Integer(dataId));

          ddd.putPageId(new Integer(pageId));

          ddd.putQuestionId(new Integer(questionId));

          ddd.putInputId(new Integer(inputId));

          ddd.putDataChoiceId(new Integer(i+1));

          v.addElement(ddd);

        }

    } else if(dataText != null) {

      DataDetailData ddd = new DataDetailData();

      ddd.putDataId(new Integer(dataId));

      ddd.putPageId(new Integer(pageId));

      ddd.putQuestionId(new Integer(questionId));

      ddd.putInputId(new Integer(inputId));

      ddd.putDataText(dataText);

      v.addElement(ddd);

    }

    return v;

  }

}

