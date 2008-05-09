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



import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.services.LogService;

import org.jasig.portal.IWorkerRequestProcessor;

import org.jasig.portal.UPFileSpec;

import org.jasig.portal.PortalSessionManager;

import org.jasig.portal.IChannel;

import org.jasig.portal.PortalControlStructures;

import org.jasig.portal.PortalException;

import org.jasig.portal.IPrivilegedChannel;

import org.jasig.portal.ChannelRuntimeData;

import org.jasig.portal.BrowserInfo;

import org.jasig.portal.UserInstance;






import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;




public class PrintReadyWorker implements IWorkerRequestProcessor {

  int m_formID, m_surveyID;

  public void processWorkerDispatch(PortalControlStructures pcs) throws PortalException {

    HttpServletRequest req = pcs.getHttpServletRequest();

    HttpServletResponse res = pcs.getHttpServletResponse();

    m_formID = Integer.parseInt(req.getParameter("FormId"));

    m_surveyID = Integer.parseInt(req.getParameter("SurveyId"));

    String channelTarget = (String)req.getParameter("tarChannel");

    String content = "", htmlout = "";

    if(channelTarget == null){

      htmlout += "Error: Not found target channel!";

      res.setContentType("text/plain");

    }

    else{

      IChannel ch = pcs.getChannelManager().getChannelInstance(channelTarget);

      if(ch == null){

        htmlout += "Error: Not found target channel " + channelTarget;

        res.setContentType("text/plain");

      } else{

        if(ch instanceof CSurveyAuthor){

          CSurveyAuthor export = (CSurveyAuthor)ch;

          try {

            content = getSummaryReport(export);

          } catch(Exception e) {

            e.printStackTrace();

          }

          if(content.equals("")){

            htmlout += "Error: Not found Data Description file";

            res.setContentType("text/plain");

          }else{

            htmlout += "<html><body><head><title>Report</title></head><body>";

            htmlout += "<table cellspacing='2' cellpadding='2' border='0' width='100%'>";

            htmlout += "<tr><td align='center' bgcolor='silver' align='right'>Content</td>";

            htmlout += "<td bgcolor='silver' align='right'>Number of Responses</td>";

            htmlout += "<td bgcolor='silver' align='right'>Percent of Responses</td></tr>";

            htmlout += content;

            htmlout += "</table></body></html>";

            res.setContentType("text/html");

          }

        }

        else{

          htmlout += "Error: Channel " + ch.getClass().getName();

          res.setContentType("text/plain");

        }

      }

    }

    try {

      res.setContentLength(-1);

      javax.servlet.ServletOutputStream out = res.getOutputStream();

      out.println(htmlout);

    } catch (Exception ioe) {

      throw new PortalException(ioe);

    }

  }

  public String getSummaryReport(CSurveyAuthor ch) throws Exception {

    String pageID, questionID, inputID, dataID;

    String content = "";

    int h,i,j,k,l,m,y,z;

    FormData fd = null;

    FormData[] fdr = null;

    Vector dddv = null;

    DataDetailData[] sumr = null;

    try {

      fdr = getBo(ch).listFormSurveyforAuthor(true, ch.logonUser());

      fd = getBo(ch).getForm(m_formID);

      dddv = getBo(ch).getExport(m_surveyID);

      for(z=0; z<dddv.size(); z++) {

        if(((DataDetailData)dddv.elementAt(z)).getDataText() != null)

          dddv.removeElementAt(z--);

      }

      sumr = (DataDetailData[])dddv.toArray(new DataDetailData[0]);

    } catch(Exception e) {

    }



    if(fd != null) {

      Object[] exchPage = null;

      exchPage = fd.getXMLForm().rgetE("page");

      if(exchPage != null)

        for(i=0; i<exchPage.length; i++) {

          pageID = (String)((XMLData)exchPage[i]).getA("page-id");

          content += "<tr><td colspan='3'>" + (String)((XMLData)exchPage[i]).getA("title") + "</td></tr>";



          Object[] exchQuestion = null;

          exchQuestion = ((XMLData)exchPage[i]).rgetE("question");

          if(exchQuestion != null)

            for(j=0; j<exchQuestion.length; j++) {

              questionID = (String)((XMLData)exchQuestion[j]).getA("question-id");

              content += "<tr><td colspan='3'>" + (String)((XMLData)exchQuestion[j]).get() + "</td></tr>";



              Object[] exchInput = null;

              exchInput = ((XMLData)exchQuestion[j]).rgetE("input");

              if(exchInput != null)

                for(k=0; k<exchInput.length; k++) {

                  inputID = (String)((XMLData)exchInput[k]).getA("input-id");



                  Object[] exchData = null;

                  exchData = ((XMLData)exchInput[k]).rgetE("data");

                  if(exchData != null) {



                    Object[] exchEntry = null;

                    exchEntry = ((XMLData)exchData[0]).rgetE("entry");

                    if(exchEntry != null)

                      for(l=0; l<exchEntry.length; l++) {

                        dataID = (String)((XMLData)exchEntry[l]).getA("data-id");

                        boolean found = false;



                        if(sumr != null)

                          for(m=0; m<sumr.length; m++)

                            if(pageID.equals(sumr[m].getPageId().toString()) && questionID.equals(sumr[m].getQuestionId().toString()) && inputID.equals(sumr[m].getInputId().toString()) && dataID.equals(sumr[m].getDataChoiceId().toString())) {

                              content += "<tr><td>" + (String)((XMLData)exchEntry[l]).get() + "</td><td bgcolor='silver' align='right'>" + sumr[m].getNumResp().toString() + "</td><td bgcolor='silver' align='right'>" + sumr[m].getPercent().toString() + "</td></tr>";

                              found = true;

                              break;

                            }

                        if(!found)

                          content += "<tr><td>" + (String)((XMLData)exchEntry[l]).get() + "</td><td bgcolor='silver' align='right'>0</td><td bgcolor='silver' align='right'>0%</td></tr>";

                      }

                  }

                }

            }

        }

    }

    return content;

  }



  SurveyBoRemote getBo(CSurveyAuthor ch) throws Exception {

    SurveyBoRemote ejb = (SurveyBoRemote)ch.getEjb("SurveyBo");

    if( ejb == null) {

      SurveyBoHome home = (SurveyBoHome)ch.ejbHome("SurveyBo");

      ejb = home.create();

      ch.putEjb("SurveyBo", ejb);

      ejb.setFilePath(CSurveyAuthor.REPOSITORY_PATH);

    }

    return ejb;

  }

}
