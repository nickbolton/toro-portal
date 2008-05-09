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

import java.io.*;
import java.util.*;

import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.notification.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;

import com.interactivebusiness.portal.VersionResolver;

public class Peephole extends SurveyAuthorScreen {

  FormData[] m_fdr;

  TargetData[] m_tdr;

  int m_publicExpand = 1;

  int m_privateExpand = 1;

  String m_mode;

  private static final VersionResolver vr = VersionResolver.getInstance();

  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_publicExpand = 1;

    m_privateExpand = 1;



    m_mode = m_channel.m_csd.getParameter("mode");

    m_mode = (m_mode == null)?"all":m_mode;

  }



  public void reinit(Hashtable params) throws Exception {

    super.init(params);

    refresh();

  }



  public void refresh() throws Exception {

    m_mode = m_channel.m_csd.getParameter("mode");

    m_mode = (m_mode == null)?"all":m_mode;

    initData();

    boolean emptyPb = true, emptyPv = true;

    for(int i=0; i<m_fdr.length; i++) {

      if(m_fdr[i].getType().equals("Public Survey"))

        emptyPb = false;

      if(m_fdr[i].getType().equals("Private Survey"))

        emptyPv = false;

    }

    m_publicExpand = (emptyPb) ? 1 : m_publicExpand;

    m_privateExpand = (emptyPv) ? 1 : m_privateExpand;

  }



  public XMLData getData() throws Exception {

    m_data.putE("Form", m_fdr);

    return m_data;

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("tarChannel", m_channel.getSubscribeId());

    params.put("mode", m_mode);

    params.put("publicExpand", new Integer(m_publicExpand));

    params.put("privateExpand", new Integer(m_privateExpand));

    return params;

  }



  public Screen closeDistribution(Hashtable params) throws Exception {

    int formId = Integer.parseInt((String)params.get("FormId"));

    int surveyId = Integer.parseInt((String)params.get("SurveyId"));

    try {

      getBo().updateSurvey(surveyId);

    } catch(Exception e) {

      return error(e.getMessage());

    }



    // Update notification

    updateNotification( formId, surveyId);

    reinit(params);

    return this;

  }



  void updateNotification(int formId, int surveyId) throws Exception {

    String formTitle = null;

    String surveyDesc = null;

    for(int i=0; i<m_fdr.length; i++)

      if(m_fdr[i].getFormId().intValue() == formId) {

        formTitle = m_fdr[i].getTitle();

        SurveyData[] surveys = m_fdr[i].getSurvey();

        for( int j = 0; j < surveys.length; j++) {

          if( surveys[j].getSurveyId().intValue() == surveyId) {

            Date d = surveys[j].getSent();

            java.text.SimpleDateFormat formater = new java.text.SimpleDateFormat("MM/dd/yy hh:mm a");

            surveyDesc = formater.format(d).toLowerCase();

            String newParams = "Survey " + formTitle + (surveyDesc!=null?", "+surveyDesc:"") + " has been closed.";

            Notification.delete("net.unicon.portal.channels.survey.survey.CSurvey", "Yes_" + formId + "_" + surveyId, "Survey Closed", newParams);

            break;

          }

        }

        break;

      }

  }



  void updateNotification(FormData form) throws Exception {

    String formTitle = form.getTitle();

    SurveyData[] surveys = form.getSurvey();

    java.text.SimpleDateFormat formater = new java.text.SimpleDateFormat("MM/dd/yy hh:mm a");

    for( int j = 0; j < surveys.length; j++) {

      Date d = surveys[j].getSent();

      String surveyDesc = formater.format(d).toLowerCase();

      String newParams = "Survey " + formTitle + (surveyDesc!=null?", "+surveyDesc:"") + " has been closed.";

      Notification.delete("net.unicon.portal.channels.survey.survey.CSurvey", "Yes_" + form.getFormId() + "_" + surveys[j].getSurveyId(), "Survey Closed", newParams);

    }

  }



  public Screen deleteDistribution(Hashtable params) throws Exception {

    int formId = Integer.parseInt((String)params.get("FormId"));

    int surveyId = Integer.parseInt((String)params.get("SurveyId"));

    try {

      getBo().deleteSurvey(surveyId);

    } catch(Exception e) {

      return error(e.getMessage());

    }



    updateNotification( formId, surveyId);

    reinit(params);

    return this;

  }



  public Screen deleteForm(Hashtable params) throws Exception {

    int i,j,formId = Integer.parseInt((String)params.get("FormId"));

    boolean fXML, fXSL;

    try {

      getBo().deleteForm(formId);

    } catch(Exception e) {

      return error(e.getMessage());

    }



    for(i=0; i<m_fdr.length; i++)

      if(m_fdr[i].getFormId().intValue() == formId) {

        fXML = false;

        fXSL = false;

        for(j=0; j<m_fdr.length; j++) {

          if(i!=j && m_fdr[j].getXMLForm().getFileName().equals(m_fdr[i].getXMLForm().getFileName()))

            fXML = true;

          if(i!=j && m_fdr[j].getXSLForm().getFileName().equals(m_fdr[i].getXSLForm().getFileName()))

            fXSL = true;

        }

        if(!fXML)

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXMLForm().getFileName()).delete();

        if(!fXSL) {

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName()).delete();

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName() + "_author_sample.xsl").delete();

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName() + "_author_results.xsl").delete();

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName() + "_survey_survey.xsl").delete();

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName() + "_poll_peephole.xsl").delete();

          new File(CSurveyAuthor.REPOSITORY_PATH + m_fdr[i].getXSLForm().getFileName() + "_poll_results.xsl").delete();

        }

        File dir = new File(CSurveyAuthor.REPOSITORY_PATH + m_user.getID());

        if(dir.list() != null && dir.list().length == 0)

          dir.delete();



        // Update notification

        updateNotification( m_fdr[i]);

      }



    reinit(params);

    return this;

  }



  public Screen publicExpand(Hashtable params) throws Exception {

    if(m_fdr == null)

      initData();

    m_publicExpand = 2;
    m_privateExpand = 1;
    
    return this;

  }



  public Screen privateExpand(Hashtable params) throws Exception {

    if(m_fdr == null)

      initData();

    m_privateExpand = 2;
    m_publicExpand = 1;
    
    return this;

  }



  public void initData() throws Exception {

    SurveyData[] sdr;

    boolean getAll = vr.getPrincipalByPortalVersions(
      m_channel.m_csd.getPerson()).hasPermission(
        CSurveyAuthor.CHANNEL, CSurveyAuthor.EDIT_ALL, CSurveyAuthor.CHANNEL);

    m_fdr = getBo().listFormSurveyforAuthor(true, m_user, getAll);

    for(int i=0; i<m_fdr.length; i++) {

      sdr = m_fdr[i].getSurvey();

      for(int j=0; j<sdr.length; j++)

        sdr[j].putTargetSize(new Integer(GroupData.getEntitySize(sdr[j].getTarget())));

    }

  }



  public Screen Distribution(Hashtable params) throws Exception {

    Screen scr = makeScreen("Distribution");

    String formId = (String)params.get("FormId");

    for(int i=0; i<m_fdr.length; i++)

      if(m_fdr[i].getFormId().intValue() == Integer.parseInt(formId)) {

        params.put("Title", m_fdr[i].getTitle());

        break;

      }

    scr.init(params);

    return scr;

  }



  public Screen confirmDeleteForm(Hashtable params) throws Exception {

    return confirm(CSurveyAuthor.CONFIRM_DELETE_FORM, null, params, sid());

  }



  public Screen confirmDeleteDistribution(Hashtable params) throws Exception {

    return confirm(CSurveyAuthor.CONFIRM_DELETE_DISTRIBUTION, null, params, sid());

  }



  public Screen confirmCloseDistribution(Hashtable params) throws Exception {

    return confirm(CSurveyAuthor.CONFIRM_CLOSE_DISTRIBUTION, null, params, sid());

  }

}

