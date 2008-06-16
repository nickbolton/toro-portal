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
package net.unicon.portal.channels.survey.poll;



import java.io.*;

import java.util.*;




import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;



abstract public class PollScreen extends net.unicon.portal.channels.rad.Screen {



  XMLData m_data = null;



  public String getVersion() {

    return CPoll.VERSION;

  }

  // Return always true. Allows result screen to appear in peephole view
  public boolean canRenderAsPeephole() {
    return true;
  }

  public void init(Hashtable params) throws Exception {

    m_data = new XMLData();

  }



  abstract XMLData getData() throws Exception;



  SurveyBoRemote getBo() throws Exception {

    SurveyBoRemote ejb = (SurveyBoRemote)m_channel.getEjb("SurveyBo");

    if( ejb == null) {

      SurveyBoHome home = (SurveyBoHome)m_channel.ejbHome("SurveyBo");

      ejb = home.create();

      m_channel.putEjb("SurveyBo",ejb);

      ejb.setFilePath(CPoll.REPOSITORY_PATH);

    }

    return ejb;

  }



  public String buildXML() throws Exception {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PrintStream ps = new PrintStream(baos);



    ps.println("<?xml version='1.0' encoding='UTF-8'?>");

    ps.println("<survey-system>");

    XMLData data = getData();

    data.print(ps,1);

    ps.println("</survey-system>");

    return baos.toString();

  }



  public String sid() {

    String className = getClass().getName();

    int index = className.lastIndexOf(".");

    return (index > -1)?className.substring(index+1):className;

  }



  public static String fixURI (String str) {

    if (str==null)

      return null;
    
    //str = "file:///" + str.replaceFirst("[/]*", "");
    str = "file:" + str;



    return str;

  }

}

