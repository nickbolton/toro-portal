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



import net.unicon.academus.apps.rad.XMLData;



public class StatisticsData extends XMLData {



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



  public void putDataChoiceId(Integer dataChoiceId) {

    putA("data-id", dataChoiceId);

  }

  public Integer getDataChoiceId() {

    return (Integer)getA("data-id");

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



  public static StatisticsData[] convert(Vector stdv) {

    if(stdv == null)

      return null;

    StatisticsData[] stdr = new StatisticsData[stdv.size()];

    for (int i=0; i < stdv.size(); i++)

      stdr[i] = (StatisticsData)stdv.elementAt(i);

    return stdr;

  }

}

