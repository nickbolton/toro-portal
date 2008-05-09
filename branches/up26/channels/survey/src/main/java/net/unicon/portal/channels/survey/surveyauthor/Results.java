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
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;



import org.jasig.portal.utils.XSLT;

import org.jasig.portal.UPFileSpec;

import org.xml.sax.ContentHandler;



public class Results extends SurveyAuthorScreen {



  static int STEP = 30;

  FormData[] m_fdr;

  FormData m_fd = null;

  SurveyData m_sd = null;

  SummaryData m_smd = null;

  String m_mode, m_replierId;

  int m_formID, m_surveyID;

  int m_page, m_reply, m_replier, m_maxReplier, m_replierStep;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_page = 1;

    m_replierStep = 1;

    m_replier = 0;

    m_mode = "summary";



    SurveyData[] sdr = null;

    m_fdr = getBo().listFormSurveyforAuthor(true, m_user);

    for(int i=0; i<m_fdr.length; i++) {

      sdr = m_fdr[i].getSurvey();

      for(int j=0; j<sdr.length; j++)

        sdr[j].putTargetSize(new Integer(GroupData.getEntitySize(sdr[j].getTarget())));

    }

    m_formID = Integer.parseInt((String)params.get("FormId"));

    m_surveyID = Integer.parseInt((String)params.get("SurveyId"));



    m_fd = getBo().getForm(m_formID);

    m_sd = getBo().getSurvey(m_surveyID);

    processMode();

    locateReply(params);

  }



  public void reinit(Hashtable params) throws Exception {

    if ((String)params.get("Init") != null && ((String)params.get("Init")).equals("Yes"))

      init(params);

  }



  public XMLData getData() throws Exception {

    m_data.putE("Form",m_fd);

    return m_data;

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("Step", new Integer(STEP));

    params.put("FormId", m_fd.getFormId());

    params.put("SurveyId", m_sd.getSurveyId());

    params.put("CurPage", new Integer(m_page).toString());

    params.put("ReplierStep", new Integer(m_replierStep));

    params.put("Reply", new Integer(m_reply));

    params.put("mode", m_mode);

    if(m_replierId != null)

      params.put("ReplierId", m_replierId);

    if(m_replier > 0)

      params.put("Replier", new Integer(m_replier));

    return params;

  }



  public Screen Close(Hashtable params) throws Exception {

    Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");

    scr.reinit(params);

    return scr;

  }



  public Screen previous(Hashtable params) throws Exception {

  	int curPage = Integer.parseInt((String)params.get("CurPage"));
  	
    m_page = curPage > 1 ? --curPage : 1;

    return this;

  }



  public Screen next(Hashtable params) throws Exception {

  	int maxPage = Integer.parseInt((String)params.get("MaxPage"));
    
    int curPage = Integer.parseInt((String)params.get("CurPage"));

    m_page = curPage < maxPage ? ++curPage : maxPage;

    return this;

  }



  public Screen changeMode(Hashtable params) throws Exception {

    if(((String)params.get("mode")).equals("summary"))

      m_mode = "summary";

    else

      m_mode = "one-by-one";

    m_page = 1;

    processMode();

    return this;

  }



  public Screen previousReplier(Hashtable params) throws Exception {

    m_replier = m_replier > 1 ? m_replier - STEP : 1;

    m_replierStep = m_replierStep > 1 ? --m_replierStep : 1;

    return this;

  }



  public Screen nextReplier(Hashtable params) throws Exception {

    int maxReplier = Integer.parseInt((String)params.get("MaxReplier"));

    m_replier = m_replier < maxReplier ? m_replier + STEP : maxReplier;

    m_replierStep = m_replierStep < maxReplier ? ++m_replierStep : m_replierStep;

    return this;

  }
  

  public Screen prevReply(Hashtable params) throws Exception {

    m_reply = m_reply > 1 ? m_reply-1 : 1;

    params.put("Reply", String.valueOf(m_reply));

    locateReply(params);

    return this;

  }



  public Screen nextReply(Hashtable params) throws Exception {

    int maxReplier = Integer.parseInt((String)params.get("MaxReplier"));

    m_reply = m_reply < maxReplier ? m_reply+1 : maxReplier;

    params.put("Reply", String.valueOf(m_reply));

    locateReply(params);

    return this;

  }
  public Screen locateReplierByName(Hashtable params) throws Exception {

    //Show response by inputed username

    Vector recipientv = new Vector();

    String ReplierName = (String)params.get("ReplierName");

    FormDataData[] fddr = m_sd.getRecipient();

    if(ReplierName == null || ReplierName.trim().length() == 0) {

      for(int i=0; i<fddr.length; i++) {

        TargetData td = new TargetData();

        td.putUserId(fddr[i].getUserId());

        td.putUserName(fddr[i].getUserName());

        recipientv.addElement(td);

      }

    } else

      for(int i=0; i<fddr.length; i++)

        if(fddr[i].getUserName().toLowerCase().indexOf(ReplierName.trim().toLowerCase()) != -1) {

          TargetData td = new TargetData();

          td.putUserId(fddr[i].getUserId());

          td.putUserName(fddr[i].getUserName());

          recipientv.addElement(td);

        }

    super.init(params);

    if(recipientv.size() > 0) {

      m_replier = 1;

      XMLData view = new XMLData();

      view.putE("Identity", (TargetData[])recipientv.toArray(new TargetData[0]));

      m_data.putE("RecipientName", view);

    }

    return this;

  }



  public Screen locateReplierById(Hashtable params) throws Exception {

    //Show response by user list

    m_replierId = (String)params.get("ReplierId");

    getReponse(m_replierId);

    return this;

  }



  public Screen locateReply(Hashtable params) throws Exception {

    //Show response by inputed index

    int tmp = 1;

    try {

      if (params.get("Reply")!=null)

        tmp = Integer.parseInt((String)params.get("Reply"));

      if(tmp<1 || tmp>m_sd.getReplied().intValue())

        return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

    } catch(NumberFormatException e) {

      return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

    }

    m_reply = tmp;

    m_replierId = null;

    getReponse(m_reply);

    return this;

  }



  public void processMode() throws Exception {

    if(m_mode.equals("summary")) {

      if(m_sd.getSummary() == null)

        m_sd.putSummary(getBo().summarize(m_surveyID));

    } else {

      if(m_sd.getRecipient() == null)

        m_sd.putRecipient((FormDataData[])getBo().getAllResponse(m_surveyID).toArray(new FormDataData[0]));

        // Make the 1st response the current one.
        if (m_sd.getRecipient().length > 0) {
            getReponse(1);
        }

    }

    m_fd.putSurvey(new SurveyData[]{m_sd});

  }



  public void getReponse(int index) throws Exception {

    FormDataData[] fddr = m_sd.getRecipient();

    if(fddr != null) {

      for(int i=0; i<fddr.length; i++) {

        fddr[i].putResponse(null);

        fddr[i].putA("current", null);

      }

      index--;

      fddr[index].putResponse((DataDetailData[])getBo().getResponseDetail(fddr[index].getDataId().intValue()).toArray(new DataDetailData[0]));

      fddr[index].putA("current", "true");

    }

  }



  public void getReponse(String ident) throws Exception {

    FormDataData[] fddr = m_sd.getRecipient();

    if(fddr != null)

      for(int i=0; i<fddr.length; i++) {

        if(fddr[i].getUserId().equals(ident)) {

          fddr[i].putResponse((DataDetailData[])getBo().getResponseDetail(fddr[i].getDataId().intValue()).toArray(new DataDetailData[0]));

          fddr[i].putA("current", "true");

        } else {

          fddr[i].putResponse(null);

          fddr[i].putA("current", null);

        }

      }

  }



  /////////////////////////////////////////////////////////////////////////////////////////

  // Render

  /////////////////////////////////////////////////////////////////////////////////////////

  public String getXSL() {

    return super.fixURI(CSurveyAuthor.REPOSITORY_PATH + m_fd.getXSLForm().getFileName() + "_author_results.xsl");

  }



  public void processXML(ContentHandler out1, OutputStream out2) throws Exception {

    String xml = buildXML();

    String xslURI = getXSL();

    Hashtable params = getXSLTParams();

    if (params == null)

      params = new Hashtable();

    putSystemXSLTParams(params);

    XSLT xslt = new XSLT(this);

    xslt.setXSL(xslURI);

    xslt.setStylesheetParameters(params);

    xslt.setXML(xml);

    if (out1 != null)

      xslt.setTarget(out1);

    else

      xslt.setTarget(out2);

    xslt.transform();

  }

}

