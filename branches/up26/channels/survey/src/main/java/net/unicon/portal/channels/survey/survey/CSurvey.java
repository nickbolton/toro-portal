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
package net.unicon.portal.channels.survey.survey;



import java.util.*;

import java.io.*;



import org.xml.sax.ContentHandler;

import net.unicon.academus.apps.notification.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;



import org.jasig.portal.PortalException;

import org.jasig.portal.utils.ResourceLoader;

import org.jasig.portal.UPFileSpec;

import org.jasig.portal.IChannel;



public class CSurvey extends Channel implements INotified {

  static final String VERSION = "1.2.f";

  public static String REPOSITORY_PATH = getRADProperty("survey.repository") + "/";

  public static final String ERROR_NO_RESPONSE = "ERROR_NO_RESPONSE";

  public static final String ERROR_RESPONSE_FAIL = "ERROR_RESPONSE_FAIL";

  public static final String ERROR_FORM_REMOVED = "ERROR_FORM_REMOVED";

  public static final String ERROR_FORM_CLOSED = "ERROR_FORM_CLOSED";

  public static final String MSG_ELECTION_SUBMITTED = "MSG_ELECTION_SUBMITTED";



  public static final String NAMED = "Named";

  public static final String ANONYMOUS = "Anonymous";

  public static final String POLL = "Poll";

  public static final String ELECTION = "Election";



  public String getMain() {

    return "net.unicon.portal.channels.survey.survey.Peephole";

  }



  public INotifier m_notifier = null;



  public void openNotification( Notification notification, INotifier notifier, ContentHandler out) throws Exception {

    m_notifier = notifier;



    // Parse params

    StringTokenizer token = new StringTokenizer(notification.m_params,"_");

    String init = token.nextToken();

    String form_id = token.nextToken();

    String survey_id = token.nextToken();



    // Prepare to go to Survey screen

    m_crd.setParameter("go","Survey");

    m_crd.put("FormId",form_id);

    m_crd.put("SurveyId",survey_id);

    m_crd.put("init",init);

    m_crd.put("notification", "yes"); // call from notification



    try {

      execute();

      if( m_lastScreen != null)

        m_lastScreen.processXML(out, null);

    } catch( Exception e) {

      e.printStackTrace();

      throw e;

    }

  }



  public boolean isNotificationClosed() {

    String doAct = getCmdValue("do");

    if (doAct != null && (doAct.equals("Submit") || doAct.equals("Cancel") || doAct.equals("Close") || doAct.equals("ok")))

      return true;

    else

      return ( getCmdValue("go") == null && doAct == null);

  }



  public void renderXML(org.xml.sax.ContentHandler out) throws PortalException {

    try {

      if (m_lastScreen != null && needsRefresh()) {
        m_lastScreen.refresh();
      }
      execute();

      if (m_notifier != null && isNotificationClosed())

        m_notifier.goBack(out);

      else if(m_lastScreen != null) {
        m_lastScreen.processXML(out, null);
      }

    } catch (Exception e) {

      log(e);

      String msg = (e==null)?"Object Exception is null":e.getMessage();

      throw new PortalException(msg) {

        public int getExceptionCode() {

          return -1;

        }

      };

    }

  }

  private boolean needsRefresh() { 
    Hashtable params = getParameters(); 
    if (params != null) { 
      return "refresh".equalsIgnoreCase( 
        (String)params.get("channel_command")); 
    } 
    return false; 
  } 
}
