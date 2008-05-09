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





import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;


import org.jasig.portal.utils.XSLT;

import org.xml.sax.ContentHandler;



public class Results extends PollScreen {


  Integer formId = null;
  
  Integer surveyId = null;
  
  FormData[] m_fdr;

  FormData m_fd;

  int m_page;

  String m_flagForm;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_fd = null;

    m_page = 1;

    m_flagForm = 
        (m_channel.m_csd.getParameter("Form") != null) ? "True" : "False";

    m_fdr = getBo().listFormSurveyforPoll(true);

    // Check if the result screen has already been initialized
    if (formId == null) {
	    formId = (Integer) params.get("FormId");
    }
    if (surveyId == null) {
        surveyId = (Integer) params.get("SurveyId");
    }
   
    m_fd = getBo().getFormBySurveyId(surveyId.intValue());
    
    m_fd.getSurvey()[0].putSummary(getBo().summarize(surveyId.intValue()));

  }



  public void reinit(Hashtable params) throws Exception {

    init(params);

  }



  public XMLData getData() throws Exception {

    m_data.putE("Form", m_fdr);

    return m_data;

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    if(m_fd != null) {

      params.put("FormId", m_fd.getFormId());

      // TT 04855 - Added to pass Survey Id (Distribution identifier)
      params.put("SurveyId", m_fd.getSurvey()[0].getSurveyId());

      params.put("CurPage", String.valueOf(m_page));

      params.put("mode","summary");

      params.put("FlagForm", m_flagForm);

    }

    return params;

  }



  public Screen next(Hashtable params) throws Exception {

    int maxPage = Integer.parseInt((String)params.get("MaxPage"));
    
    int curPage = Integer.parseInt((String)params.get("CurPage"));

    m_page = curPage < maxPage ? ++curPage : maxPage;

    return this;

  }



  public Screen previous(Hashtable params) throws Exception {

  	int curPage = Integer.parseInt((String)params.get("CurPage"));
  	
    m_page = curPage > 1 ? --curPage : 1;

    return this;

  }



  public Screen Close(Hashtable params) throws Exception {

    Screen scr = getScreen("Peephole");

    params.put("Init","Yes");

    scr.reinit(params);

    return scr;

  }



  public Screen selectPoll(Hashtable params) throws Exception {

    m_page = 1;
    
    // TT 04855 - 'Polls' is Survey Id (Distribution identifier)
    m_fd = getBo().getFormBySurveyId(Integer.parseInt(
            (String)params.get("Polls")));

    SurveyData sd = getBo().getSurvey(
            m_fd.getSurvey()[0].getSurveyId().intValue());

    sd.putSummary(getBo().summarize(sd.getSurveyId().intValue()));

    return this;

  }



  public String getXSL() {

    if(m_fd != null)

      return super.fixURI(CPoll.REPOSITORY_PATH + 
              m_fd.getXSLForm().getFileName() + "_poll_results.xsl");

    return super.getXSL();

  }



  public void processXML(ContentHandler out1, OutputStream out2) 
      throws Exception {

    if(m_fd != null) {

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

    } else

      super.processXML(out1, out2);

  }

}

