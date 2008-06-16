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



import java.io.*;

import java.util.*;






import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;


import org.jasig.portal.utils.XSLT;

import org.jasig.portal.UPFileSpec;

import org.xml.sax.ContentHandler;



public class Survey extends SurveyScreen {

  FormData m_fd;

  SurveyData m_sd;

  FormDataData m_fdd;

  Vector m_dddViewv;

  boolean m_fromNotification = false;

  int m_page;



  public void init(Hashtable params) throws Exception {

    super.init(params);



    //Begin: Check from notification

    String noti = (String)params.get("notification");

    m_fromNotification = (noti != null && noti.equals("yes"));

    int surveyId = Integer.parseInt((String)params.get("SurveyId"));

    //End: Check from notification



    m_fdd = new FormDataData();

    m_dddViewv = new Vector();

    m_page = 1;



    m_fd = getBo().getForm(Integer.parseInt((String)params.get("FormId")));

    m_sd = getBo().getSurvey(surveyId);

    if(m_fd == null || m_sd == null) {
      redirectToInfoScreen(CSurvey.ERROR_FORM_REMOVED);
      return;
    }

    // Check if survey is closed 
    if (getBo().isClosedSurvey(surveyId)) { 
      Screen info = getScreen(Info.SID); 
    
      if (info == null) 
        info = makeScreen("net.unicon.portal.channels.rad.Info"); 
            
      info.putParameter("icon", Info.INFO); 
      info.putParameter("text", getLocalText(CSurvey.ERROR_FORM_CLOSED)); 
      makeScreen("Peephole"); 
      info.putParameter("back", "Peephole"); 
      info.putParameter("refresh", "yes"); 
            
      redirect( info.sid(), params);         
      return; 
    } 

    // Check election mode

    if( m_sd.getType().equals(CSurvey.ELECTION) && getBo().existResponse( m_channel.logonUser(), surveyId)) {

      Screen info = getScreen(Info.SID);

      if (info == null)

        info = makeScreen("net.unicon.portal.channels.rad.Info");

      info.putParameter("icon", Info.INFO);

      info.putParameter("text", getLocalText(CSurvey.MSG_ELECTION_SUBMITTED));

      info.putParameter("back", sid());

      redirect( info.sid(), params);

      return;

    }



    // Randomize the questions...

    Shuffling(m_fd.getXMLForm());



    m_fd.putSurvey(new SurveyData[]{m_sd});



    if(m_sd.getType().equals(CSurvey.NAMED)) {

      m_fdd = getBo().getResponse(m_sd.getSurveyId().intValue(), m_user);

      DataDetailData[] dddr = m_fdd.getResponse();

      if(dddr != null)

        for(int i = 0; i < dddr.length; i++)

          m_dddViewv.addElement(dddr[i]);

    }

  }



  public void reinit(Hashtable params) throws Exception {

    if ((String)params.get("Init") != null && ((String)params.get("Init")).equals("Yes"))

      init(params);

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("CurPage", new Integer(m_page));

    params.put("LogonUserName", m_user.getName());

    return params;

  }



  public XMLData getData() throws Exception {

    XMLData m_view = new XMLData();

    m_view.putE("response", (DataDetailData[])m_dddViewv.toArray(new DataDetailData[0]));

    m_data.putE("view", m_view);

    m_data.putE("Form",m_fd);

    return m_data;

  }



  public Screen Close(Hashtable params) throws Exception {

    Screen scr = getScreen("Peephole");

    scr.init(params);

    return scr;

  }



  public Screen Next(Hashtable params) throws Exception {

    Screen redirect = verifySurveyState(); 
    if (redirect != null) return redirect; 

    saveInput(params);

    int maxPage = Integer.parseInt((String)params.get("MaxPage"));

    m_page = m_page < maxPage ? ++m_page : maxPage;

    return this;

  }



  public Screen Previous(Hashtable params) throws Exception {

    Screen redirect = verifySurveyState(); 
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

    for(int i = 0; i < m_dddViewv.size(); i++)

      if(((DataDetailData)m_dddViewv.elementAt(i)).getPageId().intValue() == pageNo)

        m_dddViewv.removeElementAt(i--);

    //End: Delete old page

    m_dddViewv.addAll(dddv);

    //End: Update DataDetailData vector

  }



  public Screen Cancel(Hashtable params) throws Exception {

    if (m_fromNotification)

      return this;

    Screen scr =  getScreen("Peephole");

    scr.init(params);

    return scr;

  }



  public Screen Submit(Hashtable params) throws Exception {

    Screen redirect = verifySurveyState(); 
    if (redirect != null) return redirect; 

    saveInput(params);

    //Begin: Transfer DataChoiceId to DataChoices

    int len, dataChoiceId;

    String dataChoices;

    for(int i=0; i<m_dddViewv.size(); i++) {

      DataDetailData dddI = (DataDetailData)m_dddViewv.elementAt(i);

      dataChoices = "";

      for(int j=i; j<m_dddViewv.size(); j++) {

        DataDetailData dddJ = (DataDetailData)m_dddViewv.elementAt(j);

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

            m_dddViewv.removeElementAt(j--);

        }

      }

      ((DataDetailData)m_dddViewv.elementAt(i)).putDataChoices(dataChoices);

    }

    //End: Transfer DataChoiceId to DataChoices

    m_fdd.putFormId(m_fd.getFormId());

    if(m_sd.getType().equals(CSurvey.NAMED) || m_sd.getType().equals(CSurvey.ELECTION)) {

      m_fdd.putUserId(m_user.getIdentifier());

      m_fdd.putUserName(m_user.getName());

    }

    Calendar date = Calendar.getInstance();

    date.setTimeZone(TimeZone.getTimeZone("GMT+0"));

    m_fdd.putCreated(date.getTime());

    if (m_dddViewv.size() == 0)

      return error(CSurvey.ERROR_NO_RESPONSE);

    m_fdd.putResponse((DataDetailData[])m_dddViewv.toArray(new DataDetailData[0]));

    try {

      getBo().createResponse(m_fdd, m_sd.getSurveyId().intValue());

    } catch(Exception e) {

      return error(e.getMessage());

    }



    // Notification

    if (m_fromNotification)

      return this;

    else {

      Screen scr =  makeScreen("Peephole");

      scr.init(params);

      return scr;

    }

  }



  public String getXSL() {

    return super.fixURI(CSurvey.REPOSITORY_PATH + m_fd.getXSLForm().getFileName() + "_survey_survey.xsl");

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



  public void Shuffling(TextFileData td) throws Exception {

    int i, j, k, l, m, n;

    Object[] exchPage = null;

    exchPage = td.rgetE("page");

    if(exchPage != null)

      for(i=0; i<exchPage.length; i++) {

        Object[] exchQuestion = null;

        exchQuestion = ((XMLData)exchPage[i]).rgetE("question");

        if(exchQuestion != null)

          for(j=0; j<exchQuestion.length; j++) {

            Object[] exchInput = null;

            exchInput = ((XMLData)exchQuestion[j]).rgetE("input");

            if(exchInput != null)

              for(k=0; k<exchInput.length; k++) {

                Object[] exchData = null;

                exchData = ((XMLData)exchInput[k]).rgetE("data");

                if(exchData != null)

                  for(l=0; l<exchData.length; l++) {

                    Object[] exchEntry = null;

                    exchEntry = ((XMLData)exchData[l]).rgetE("entry");

                    if(exchEntry != null) {

                      Vector orderShuffv = new Vector();

                      Vector shuffv = new Vector();

                      Vector srcv = new Vector();

                      for(m=0; m<exchEntry.length; m++)

                        if(((XMLData)exchEntry[m]).getA("shuffle") == null)

                          srcv.addElement(exchEntry[m]);

                        else{

                          srcv.addElement(exchEntry[m]);

                          shuffv.addElement(exchEntry[m]);

                          orderShuffv.addElement(new Integer(m));

                        }

                      shuffv = randomVector(shuffv);

                      for(n=0; n<orderShuffv.size(); n++)

                        srcv.setElementAt(shuffv.elementAt(n), ((Integer)orderShuffv.elementAt(n)).intValue());

                      exchEntry = srcv.toArray();

                    }

                    ((XMLData)exchData[l]).putE("entry", exchEntry);

                  }

              }

          }

      }

  }



  public Vector randomVector(Vector vsrc) throws Exception {

    Vector vdes = new Vector();

    Random r = new Random();

    int index;

    if(vsrc.size() > 0) {

      for(int j=vsrc.size(); j>0; j--) {

        index = r.nextInt(vsrc.size());

        vdes.addElement(vsrc.elementAt(index));

        vsrc.removeElementAt(index);

      }

    }

    return vdes;

  }


  // verify that the user is not trying to access an invalid survey 
  // returns Screen if redirected 
  private Screen verifySurveyState() throws Exception { 
    if (m_sd == null) return null; 
    
    Integer surveyId = m_sd.getSurveyId(); 
    if (surveyId == null) return null; 
    
    // Check if survey has been deleted 
    SurveyData surveyData = getBo().getSurvey(surveyId.intValue()); 
    if (surveyData == null) { 
      return redirectToInfoScreen(CSurvey.ERROR_FORM_REMOVED); 
    } 
    
    // Check if survey is closed 
    if (getBo().isClosedSurvey(surveyId.intValue())) { 
      return redirectToInfoScreen(CSurvey.ERROR_FORM_CLOSED); 
    } 
    
    // Check election mode, user can only submit one response per election 
    
    if (m_sd.getType().equals(CSurvey.ELECTION) && 
      getBo().existResponse(m_channel.logonUser(), surveyId.intValue())) { 
          
      return redirectToInfoScreen(CSurvey.MSG_ELECTION_SUBMITTED); 
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

