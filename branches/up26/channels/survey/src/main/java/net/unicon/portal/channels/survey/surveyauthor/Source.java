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
import java.util.*;

import net.unicon.academus.apps.form.FormData;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;



public class Source extends SurveyAuthorScreen {

  FormData m_fd;
  TextFileData m_tdXML = null;	
  TextFileData m_td = null;
  String return_value = null;
  String m_formID=null, m_oldXMLFile=null;

  public void init(Hashtable params) throws Exception {
	  super.init(params);

	  m_fd = new FormData();
	  m_tdXML = new TextFileData();

	  m_formID = (String) params.get("FormId");
	  if (m_formID == null) {
		  m_fd.putType((String) params.get("Type"));
	  } else {
		  m_fd = getBo().getForm(Integer.parseInt(m_formID));
		  m_tdXML = m_fd.getXMLForm();
		  m_tdXML.putContent(TextFileData
				  .getContentByFilename(CSurveyAuthor.REPOSITORY_PATH
						  + m_tdXML.getFileName()));
		  m_oldXMLFile = m_tdXML.getFileName();
	  }

	  String go_param = (String)params.get("go"); 
	  if((go_param != null) && (go_param.equals("Source"))) {
		  m_td = m_tdXML;
	  } else {
		  m_td = (TextFileData)params.get("Source");
	  }
	  return_value = (String) params.get("return");
  }
  
  public Hashtable getXSLTParams() {
	  Hashtable params = new Hashtable();
	  
	  if (return_value != null) {
		  params.put("return", return_value);
	  }
	  if (m_formID != null) {
		  params.put("FormID", m_formID);
	  }
	  return params;
  }

  public Screen Close(Hashtable params) throws Exception {

   	if((return_value != null) && (return_value.equals("Form"))) {
		return (getScreen("Form") == null) ? makeScreen("Form") : getScreen("Form");
	} else {
		return (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");
	}
        
  }

  public XMLData getData() {

    m_data.putE("Source", m_td.getContent());

    return m_data;

  }

}

