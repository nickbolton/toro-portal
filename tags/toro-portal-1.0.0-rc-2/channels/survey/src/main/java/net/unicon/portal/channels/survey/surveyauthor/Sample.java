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



import java.util.*;

import java.io.*;





import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.portal.channels.rad.*;



import org.jasig.portal.utils.XSLT;

import org.jasig.portal.UPFileSpec;

import org.xml.sax.ContentHandler;



public class Sample extends SurveyAuthorScreen {



  FormData m_fd = null;

  String m_formID = null, m_back = null;

  int m_page;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_fd = new FormData();

    m_back = (String)params.get("back");

    m_formID = (String)params.get("FormId");

    m_page = 1;

    if(m_formID != null)

      m_fd = getBo().getForm(Integer.parseInt(m_formID));

    else

      m_fd = (FormData)params.get("Form");

  }



  public Screen close(Hashtable params) throws Exception {

    return getScreen((m_back == null) ? "Peephole" : m_back);

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("CurPage", new Integer(m_page));

    params.put("Title", (m_fd.getTitle() == null) ? "No title" : m_fd.getTitle());

    return params;

  }



  public XMLData getData() throws Exception {

    m_data.putE("Form",m_fd);

    return m_data;

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



  public String getXSL() {

    return super.fixURI(CSurveyAuthor.REPOSITORY_PATH + m_fd.getXSLForm().getFileName() + "_author_sample.xsl");

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

