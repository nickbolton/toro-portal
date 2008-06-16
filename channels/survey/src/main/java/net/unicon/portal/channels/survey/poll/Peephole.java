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

import org.jasig.portal.UPFileSpec;

import org.jasig.portal.utils.ResourceLoader;

import org.xml.sax.ContentHandler;



public class Peephole extends PollScreen {



  FormData m_fd;

  FormData[] m_fdr;

  FormDataData m_fdd;

  Vector m_dddv;

  int m_page;

  String m_formTitle, m_flagForm, m_oldForm;

  public SurveyData getSurvey() { 
    if (m_fd == null) return null; 
    return m_fd.getSurvey()[0]; 
  } 

  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_fdd = new FormDataData();

    m_dddv = new Vector();

    m_fd = null;

    m_formTitle = null;

    Date latest = null;

    m_page = 1;

    m_flagForm = "False";

    m_fdr = getBo().listFormSurveyforPoll(true);

    for(int i=0; i<m_fdr.length; i++) {

        // TT 04855 - BSS - Removed code to filter out surveys 
        // having the same name even though the named distributions may vary.
        
        SurveyData sd = m_fdr[i].getSurvey()[0];

        //Begin: Get latest survey/distribution

        if((latest != null && latest.compareTo(sd.getSent()) < 0) 
                || latest == null) {

          latest = sd.getSent();
        
          m_fd = getBo().getFormBySurveyId(
                  m_fdr[i].getSurvey()[0].getSurveyId().intValue());
        
        }
        
        // End: Get latest survey/distribution

    }

    boolean flag = false;

    m_formTitle = m_channel.m_csd.getParameter("Form");

    if(m_formTitle == null)

      m_formTitle = (String)params.get("Form");

    if(m_formTitle != null) {

      for(int i=0; i<m_fdr.length; i++)

        if(m_fdr[i].getTitle().equals(m_formTitle)) {

        	m_fd = getBo().getFormBySurveyId(
                    m_fdr[i].getSurvey()[0].getSurveyId().intValue());

          flag = true;

        }

      if(!flag)

        m_fd = null;

      else

        m_flagForm = "True";

    }

  }



  public void reinit(Hashtable params) throws Exception {

    refresh();

  }



  public void refresh() throws Exception {

    Hashtable params = new Hashtable();

    if(m_fd != null) {

      int oldSurveyId = m_fd.getSurvey()[0].getSurveyId().intValue();

      super.init(params);

      m_fdd = new FormDataData();

      m_dddv = new Vector();

      m_fd = null;

      Date latest = null;

      m_page = 1;

      m_fdr = getBo().listFormSurveyforPoll(true);

      // TT 04855 - BSS - Removed code to filter out surveys 
      // having the same name even though the named distributions may vary.

      m_fd = getBo().getFormBySurveyId(oldSurveyId);

      if(m_fd == null) {

        if(m_formTitle != null)

          m_fd = null;

        else

          for(int i=0; i < m_fdr.length; i++) {

            SurveyData sd = m_fdr[i].getSurvey()[0];
            
            //Begin: Get most recent distribution

            if((latest != null && latest.compareTo(sd.getSent()) < 0)
                    || latest == null) {

              latest = sd.getSent();

              m_fd = getBo().getFormBySurveyId(
                      m_fdr[i].getSurvey()[0].getSurveyId().intValue());

            }

            //End: Get most recent distribution


          }

      }

    } else

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
      // for use in Poll selection 
      params.put("SurveyId", m_fd.getSurvey()[0].getSurveyId());

      params.put("CurPage", String.valueOf(m_page));

      params.put("FlagForm", m_flagForm);

    }

    return params;

  }



  public Screen Next(Hashtable params) throws Exception {

    Screen redirect = verifyPollState(); 
    if (redirect != null) return redirect; 

    saveInput(params);

    int maxPage = Integer.parseInt((String)params.get("MaxPage"));

    m_page = m_page < maxPage ? ++m_page : maxPage;

    return this;

  }



  public Screen Previous(Hashtable params) throws Exception {

    Screen redirect = verifyPollState(); 
    if (redirect != null) return redirect; 

    saveInput(params);

    m_page = m_page > 1 ? --m_page : 1;

    return this;

  }



  void saveInput(Hashtable params) throws Exception {

    Vector dddv = new  Vector();

    int pageNo = Integer.parseInt((String)params.get("page-id"));

    for(Enumeration e = params.keys(); e.hasMoreElements();) {

      String key = (String) e.nextElement();

      if (!key.equals("do.x") && !key.equals("do.y")

          && !key.equals("do=next.x") && !key.equals("do=next.y")

          && !key.equals("do=previous.x") && !key.equals("do=previous.y")

          && !key.equals("Polls")

          && !key.equals("do=selectPoll.x") && !key.equals("do=selectPoll.y")

          && !key.equals("sid")

          && !key.equals("do") && !key.equals("MaxPage") && !key.equals("page-id")) {

        StringTokenizer st = new StringTokenizer(key, ".");

        if (st.countTokens() == 4 && ((String)params.get(key)).trim().length() != 0) {

          DataDetailData ddd = new DataDetailData();

          ddd.putPageId(new Integer(st.nextToken()));

          ddd.putQuestionId(new Integer(st.nextToken()));

          ddd.putInputId(new Integer(st.nextToken()));

          ddd.putDataChoiceId(new Integer((String)params.get(key)));

          dddv.addElement(ddd);

        } else if (st.countTokens() == 3 && ((String)params.get(key)).trim().length() != 0) {

          DataDetailData ddd = new DataDetailData();

          ddd.putPageId(new Integer(st.nextToken()));

          ddd.putQuestionId(new Integer(st.nextToken()));

          ddd.putInputId(new Integer(st.nextToken()));

          ddd.putDataText((String)params.get(key));

          dddv.addElement(ddd);

        }

      }

    }

    //Begin: Update DataDetailData vector

    //Begin: Delete old page

    for (int i = 0; i < m_dddv.size(); i++)

      if (((DataDetailData)m_dddv.elementAt(i)).getPageId().intValue() == pageNo)

        m_dddv.removeElementAt(i--);

    //End: Delete old page

    m_dddv.addAll(dddv);

    //End: Update DataDetailData vector

    XMLData view = new XMLData();

    view.putE("response", (DataDetailData[])m_dddv.toArray(new DataDetailData[0]));

    m_data.putE("view", view);

  }


  public Screen Results(Hashtable params) throws Exception {

    Screen scr = makeScreen("Results");

    params.put("FormId", m_fd.getFormId());
    
    // TT 04855 - Added to pass Survey Id (Distribution identifier)
    params.put("SurveyId", m_fd.getSurvey()[0].getSurveyId());
    
    scr.init(params);

    return scr;

  }



  public Screen Submit(Hashtable params) throws Exception {

    Screen redirect = verifyPollState(); 
    if (redirect != null) return redirect; 

    saveInput(params);

    //Begin: Transfer DataChoiceId to DataChoices

    int len, dataChoiceId;

    String dataChoices;

    for(int i=0; i<m_dddv.size(); i++) {

      DataDetailData dddI = (DataDetailData)m_dddv.elementAt(i);

      dataChoices = "";

      for(int j=i; j<m_dddv.size(); j++) {

        DataDetailData dddJ = (DataDetailData)m_dddv.elementAt(j);

        if(dddJ.getDataChoiceId() != null

            && dddI.getPageId().intValue()==dddJ.getPageId().intValue()

            && dddI.getQuestionId().intValue() == dddJ.getQuestionId().intValue()

            && dddI.getInputId().intValue() == dddJ.getInputId().intValue()) {

          dataChoiceId = dddJ.getDataChoiceId().intValue();

          len = dataChoices.length();

          if(dataChoiceId > len)

            for(int k=0; k<(dataChoiceId-len); k++)

              dataChoices = dataChoices.concat("0");

          String tmp = dataChoices.substring(dataChoiceId);

          dataChoices = dataChoices.substring(0,dataChoiceId-1);

          dataChoices = dataChoices.concat("1");

          dataChoices = dataChoices.concat(tmp);

          if(i!=j)

            m_dddv.removeElementAt(j--);

        }

      }

      ((DataDetailData)m_dddv.elementAt(i)).putDataChoices(dataChoices);

    }

    //End: Transfer DataChoiceId to DataChoices

    m_fdd.putFormId(m_fd.getFormId());

    Calendar date = Calendar.getInstance();

    date.setTimeZone(TimeZone.getTimeZone("GMT+0"));

    m_fdd.putCreated(date.getTime());

    m_fdd.putResponse((DataDetailData[])m_dddv.toArray(new DataDetailData[0]));

    if (m_fdd == null || m_fdd.getResponse().length == 0)

      return error(CPoll.ERROR_NO_RESPONSE);

    try {

      getBo().createResponse(m_fdd, m_fd.getSurvey()[0].getSurveyId().intValue());

    } catch(Exception e) {

      return error(e.getMessage());

    }

    params.put("Polls", m_fd.getSurvey()[0].getSurveyId().toString());

    return selectPoll(params);

  }



  public Screen selectPoll(Hashtable params) throws Exception {

    super.init(params);

    m_fdd = new FormDataData();

    m_dddv = new Vector();

    m_fd = null;

    m_formTitle = null;

    m_page = 1;

    m_fdr = getBo().listFormSurveyforPoll(true);

    m_fd = getBo().getFormBySurveyId(
            Integer.parseInt((String)params.get("Polls")));
    
    // TT 04855 - BSS - Removed code to always retrieve the latest distribution
    // of a survey, regardless of specific Survey Id being requested.

    return this;

  }



  public String getXSL() {

    if(m_fd != null)

      return super.fixURI(CPoll.REPOSITORY_PATH + m_fd.getXSLForm().getFileName() + "_poll_peephole.xsl");

    return super.getXSL();

  }



  public void processXML(ContentHandler out1, OutputStream out2) throws Exception {

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

  // verify that the user is not trying to access an invalid poll 
  // returns info Screen if a redirected is needed. 
  private Screen verifyPollState() throws Exception { 
    SurveyData surveyData = getSurvey(); 
    if (surveyData == null) return null; 
    
    Integer surveyId = surveyData.getSurveyId(); 
    if (surveyId == null) return null; 
    
    // Check if survey has been deleted 
    if (getBo().getSurvey(surveyId.intValue()) == null) { 
      return redirectToInfoScreen(CPoll.ERROR_FORM_REMOVED); 
    } 
    
    // Check if survey is closed 
    if (getBo().isClosedSurvey(surveyId.intValue())) { 
      return redirectToInfoScreen(CPoll.ERROR_FORM_CLOSED); 
    } 
    
    return null; 
  } 
    
  private Screen redirectToInfoScreen(String error) throws Exception { 
    Screen info = getScreen(Info.SID); 
    
    if (info == null) 
      info = makeScreen("net.unicon.portal.channels.rad.Info"); 
            
    info.putParameter("icon", Info.INFO); 
    info.putParameter("text", getLocalText(error)); 
    makeScreen("Peephole"); 
    info.putParameter("back", "Peephole"); 
    info.putParameter("refresh", "yes"); 
    redirect(info.sid(), null);         
    return info; 
  } 
}

