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

import net.unicon.academus.apps.survey.SurveyData;
import net.unicon.portal.channels.rad.*;

public class CPoll extends Channel {

  static final String VERSION = "1.2.f";
  public static String REPOSITORY_PATH = getRADProperty("survey.repository") + "/";
  public static final String ERROR_NO_RESPONSE = "ERROR_NO_RESPONSE";
  public static final String ERROR_RESPONSE_FAIL = "ERROR_RESPONSE_FAIL";
  public static final String ERROR_FORM_CLOSED = "ERROR_FORM_CLOSED"; 
  public static final String ERROR_FORM_REMOVED = "ERROR_FORM_REMOVED"; 

  public String getMain() {

    return "net.unicon.portal.channels.survey.poll.Peephole";

  }

  protected void execute() throws Exception { 
    super.execute(); 
    if (m_lastScreen != null && needsRefresh()) { 
      m_lastScreen.refresh(); 
    } 
  } 
    
  private boolean needsRefresh() { 
    java.util.Hashtable params = getParameters(); 
    if (params != null) { 
      return "refresh".equalsIgnoreCase( 
        (String)params.get("channel_command")); 
    } 
    return false; 
  } 
}
