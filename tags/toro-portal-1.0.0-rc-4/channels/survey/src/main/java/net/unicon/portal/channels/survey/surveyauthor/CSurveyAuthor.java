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
package net.unicon.portal.channels.survey.surveyauthor;



import java.io.File;
import java.util.HashMap;

import net.unicon.portal.channels.rad.MimeResponseChannel;

import org.jasig.portal.IPermissible;
import org.jasig.portal.utils.ResourceLoader;



public class CSurveyAuthor extends MimeResponseChannel implements IPermissible {

  static final String VERSION = "1.2.f";

  public static String REPOSITORY_PATH = getRADProperty("survey.repository") + "/";

  public static String STYLES_PATH;

  public static final String ERROR_CLOSE_DISTRIBUTION_FAIL = "ERROR_CLOSE_DISTRIBUTION_FAIL";

  public static final String ERROR_DELETE_DISTRIBUTION_FAIL = "ERROR_DELETE_DISTRIBUTION_FAIL";

  public static final String ERROR_DELETE_SURVEY_FAIL = "ERROR_DELETE_SURVEY_FAIL";

  public static final String ERROR_DISTRIBUTION_FAIL = "ERROR_DISTRIBUTION_FAIL";

  public static final String ERROR_CREATE_FORM_FAIL = "ERROR_CREATE_FORM_FAIL";

  public static final String ERROR_UPDATE_FORM_FAIL = "ERROR_UPDATE_FORM_FAIL";

  public static final String ERROR_NOT_HAVE_RECIPIENT = "ERROR_NOT_HAVE_RECIPIENT";

  public static final String ERROR_EMPTY_FORM_XML = "ERROR_EMPTY_FORM_XML";

  public static final String ERROR_FILE_NOT_FOUND = "ERROR_FILE_NOT_FOUND";

  public static final String ERROR_XSL = "ERROR_XSL";

  public static final String ERROR_XML = "ERROR_XML";

  public static final String ERROR_NO_TITLE = "ERROR_NO_TITLE";

  public static final String ERROR_INVALID_NUMBER = "ERROR_INVALID_NUMBER";

  public static final String CONFIRM_DELETE_DISTRIBUTION = "CONFIRM_DELETE_DISTRIBUTION";

  public static final String CONFIRM_CLOSE_DISTRIBUTION = "CONFIRM_CLOSE_DISTRIBUTION";

  public static final String CONFIRM_DELETE_FORM = "CONFIRM_DELETE_FORM";

  public static final String NONE_EMAIL_SENDER = "NONE_EMAIL_SENDER";
  
  public static final String NONE_RECIPIENTS = "NONE_RECIPIENTS";

  public static final String NONE_EMAIL_SELECTION = "NONE_EMAIL_SELECTION";

  public static final String ERROR_INVALID_CHOICE_NUMBER = "ERROR_INVALID_CHOICE_NUMBER";

  public static final String ERROR_INVALID_CHECK_NUMBER = "ERROR_INVALID_CHECK_NUMBER";

  public static final String ERROR_NOT_FOUND_XSL = "ERROR_NOT_FOUND_XSL";



  public static final String EDIT_ALL = "editAll";
  public static final String CHANNEL = "Academus Survey Author";
  private static HashMap activities = null;
  private static HashMap targets = null;

  static {
    activities = new HashMap();
    activities.put(EDIT_ALL, "User may edit/delete all surveys.");

    targets = new HashMap();
    targets.put(CHANNEL, "CSurveyAuthor");
  }


  public String getMain() {

    try{

      File f = ResourceLoader.getResourceAsFile(CSurveyAuthor.class, "/net/unicon/portal/channels/survey/surveyauthor/peephole.xsl");

      STYLES_PATH = f.getPath().replace(File.separatorChar,'/').replaceAll("/surveyauthor/peephole.xsl", "") + "/";

    }

    catch(Exception e){

      e.printStackTrace();

    }

    return "net.unicon.portal.channels.survey.surveyauthor.Peephole";

  }

  protected void execute() throws Exception {
    super.execute();
    
    java.util.Hashtable params = getParameters();

	// go to peephole view if the not in focus view
	if (m_lastScreen != null && !isFocus()) {
	    m_lastScreen.init(params);
	}
	
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
  
  private boolean isFocus() {
      java.util.Hashtable params = getParameters();
      if (params != null) {
        return !"root".equalsIgnoreCase(
          (String)params.get("uP_root"));
      }
      return true;
    }

  // IPermissible methods
  public String getOwnerName() {
    return "Academus Survey Author";
  }

  public String[] getActivityTokens () {
    return  (String[])activities.keySet().toArray(new String[0]);
  }

  public String getOwnerToken () {
    return CHANNEL;
  }

  public String getActivityName (String token) {
    return  (String)activities.get(token);
  }

  public String[] getTargetTokens () {
    return  (String[])targets.keySet().toArray(new String[0]);
  }

  public String getTargetName (String token) {
    String r = (String) targets.get(token);
    return r != null ? r : "";
  }
}
