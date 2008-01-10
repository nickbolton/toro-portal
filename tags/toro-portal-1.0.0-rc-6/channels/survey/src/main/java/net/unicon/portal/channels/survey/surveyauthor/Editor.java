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



import org.jasig.portal.utils.ResourceLoader;

import javax.xml.transform.*;

import javax.xml.transform.stream.*;



import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;








public class Editor extends SurveyAuthorScreen {



  TextFileData m_tdXML, m_tdXSL;

  Object[] m_pager;

  int m_curPage, m_curQuestion;

  String m_type, m_title, m_xmlBackup;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_tdXML = new TextFileData();

    m_curPage = 1;

    m_curQuestion = 1;

    m_title = (String)params.get("title");

    m_xmlBackup = (String)params.get("xmlString");

    m_tdXSL = (TextFileData)params.get("xslfile");

    m_tdXML.parse(new StringBufferInputStream( m_xmlBackup));

    m_pager = m_tdXML.rgetE("page");

    if(m_pager == null) {

      m_type = "new";

      Vector inputv = new Vector(), entryv = new Vector();

      for (int j=0; j<3; j++)

        entryv.addElement(newEntry(j+1));

      inputv.addElement(editInput(1, null, "Check", "Column", entryv));



      m_pager = new XMLData[1];

      m_pager[0] = editPage(1, "Type the title of the survey page here", editQuestion(1, "Type question text here", inputv));

    } else

      m_type = "edit";

  }



public void reinit(Hashtable params) throws Exception {}



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("typeEditor", m_type);

    params.put("currentPage", String.valueOf(m_curPage));

    params.put("currentQuestion", String.valueOf(m_curQuestion));

    return params;

  }



  public XMLData getData() {

    m_tdXML.putE("page", m_pager);

    m_data.putE("source", m_tdXML);

    return m_data;

  }



  public Screen prevPage(Hashtable params) throws Exception {

    saveSession(params);

    m_curPage--;

    m_curQuestion = 1;

    return this;

  }



  public Screen firstPage(Hashtable params) throws Exception {

    saveSession(params);

    m_curPage = 1;

    m_curQuestion = 1;

    return this;

  }



  public Screen nextPage(Hashtable params) throws Exception {

    saveSession(params);

    m_curPage++;

    m_curQuestion = 1;

    return this;

  }



  public Screen lastPage(Hashtable params) throws Exception {

    saveSession(params);

    m_curPage = m_pager.length;

    m_curQuestion = 1;

    return this;

  }



  public Screen prevQuestion(Hashtable params) throws Exception {

    saveSession(params);

    m_curQuestion--;

    return this;

  }



  public Screen firstQuestion(Hashtable params) throws Exception {

    saveSession(params);

    m_curQuestion = 1;

    return this;

  }



  public Screen nextQuestion(Hashtable params) throws Exception {

    saveSession(params);

    m_curQuestion++;

    return this;

  }



  public Screen lastQuestion(Hashtable params) throws Exception {

    saveSession(params);

    m_curQuestion = (((XMLData)m_pager[m_curPage-1]).rgetE("question")).length;

    return this;

  }



  public Screen gotoPage(Hashtable params) throws Exception {

    saveSession(params);

    m_curPage = Integer.parseInt((String)params.get("selectPage"));

    m_curQuestion = 1;

    return this;

  }



  public Screen gotoQuestion(Hashtable params) throws Exception {

    saveSession(params);

    m_curQuestion = Integer.parseInt((String)params.get("selectQuestion"));

    return this;

  }



  public Screen deletePage(Hashtable params) throws Exception {

    saveSession(params);

    Vector v = toVector(m_pager);

    v.removeElementAt(m_curPage-1);

    m_pager = (XMLData[])v.toArray(new XMLData[0]);

    for(int i=0; i<m_pager.length; i++)

      ((XMLData)m_pager[i]).putA("page-id", String.valueOf(i+1));

    if(m_curPage > m_pager.length)

      m_curPage = m_pager.length;

    m_curQuestion = 1;

    return this;

  }



  public Screen deleteQuestion(Hashtable params) throws Exception {

    saveSession(params);

    XMLData page = (XMLData)m_pager[m_curPage-1];

    Vector questionv = toVector(page.rgetE("question"));

    questionv.removeElementAt(m_curQuestion-1);

    for(int i=0; i<questionv.size(); i++)

      ((XMLData)questionv.elementAt(i)).putA("question-id", String.valueOf(i+1));

    page.putE("question", (XMLData[])questionv.toArray(new XMLData[0]));

    if(m_curQuestion > questionv.size())

      m_curQuestion = questionv.size();

    return this;

  }



  public Screen deleteSubQuestion(Hashtable params) throws Exception {

    saveSession(params);

    int inputID = Integer.parseInt((String)params.get("inputId"));

    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    Vector inputv = toVector(question.rgetE("input"));

    inputv.removeElementAt(inputID-1);

    for(int i=0; i<inputv.size(); i++)

      ((XMLData)inputv.elementAt(i)).putA("input-id", String.valueOf(i+1));

    question.putE("input", (XMLData[])inputv.toArray(new XMLData[0]));

    page.putE("question", questionr);

    return this;

  }



  public Screen deleteResponse(Hashtable params) throws Exception {

    saveSession(params);

    int inputID = Integer.parseInt((String)params.get("inputId"));

    int dataID = Integer.parseInt((String)params.get("dataId"));

    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    Object[] inputr = question.rgetE("input");

    XMLData input = (XMLData)inputr[inputID-1];

    XMLData data = (XMLData)input.getE("data");

    Vector entryv = toVector(data.rgetE("entry"));

    entryv.removeElementAt(dataID-1);

    for(int i=0; i<entryv.size(); i++)

      ((XMLData)entryv.elementAt(i)).putA("data-id", String.valueOf(i+1));

    data.putE("entry", (XMLData[])entryv.toArray(new XMLData[0]));

    page.putE("question", questionr);

    return this;

  }



  public Screen addPage(Hashtable params) throws Exception {

    saveSession(params);

    Vector inputv = new Vector(), entryv = new Vector();

    for (int j=0; j<3; j++)

      entryv.addElement(newEntry(j+1));

    inputv.addElement(editInput(1, null, "Check", "Column", entryv));



    Vector v = toVector(m_pager);

    v.addElement(editPage(m_pager.length+1, "Type the title of the survey page here", editQuestion(1, "Type question text here", inputv)));

    m_pager = (XMLData[])v.toArray(new XMLData[0]);

    m_curPage = m_pager.length;

    m_curQuestion = 1;

    return this;

  }



  public Screen addQuestion(Hashtable params) throws Exception {

    saveSession(params);

    Vector inputv = new Vector(), entryv = new Vector();

    for (int j=0; j<3; j++)

      entryv.addElement(newEntry(j+1));

    inputv.addElement(editInput(1, null, "Check", "Column", entryv));



    XMLData page = (XMLData)m_pager[m_curPage-1];

    int maxQuestion = (page.rgetE("question")).length;

    page.rputE("question", editQuestion(maxQuestion + 1, "Type question text here", inputv));

    m_curQuestion = maxQuestion + 1;

    return this;

  }



  public Screen addSubQuestion(Hashtable params) throws Exception {

    saveSession(params);

    Vector entryv = new Vector();

    for (int j=0; j<3; j++)

      entryv.addElement(newEntry(j+1));



    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    question.rputE("input", editInput(question.rgetE("input").length + 1, "Type sub-question text here", "Check", "Column", entryv));

    page.putE("question", questionr);

    return this;

  }



  public Screen preview(Hashtable params) throws Exception {

    saveSession(params);

    FormData fd = new FormData();

    fd.putTitle(m_title);

    fd.putXMLForm(m_tdXML);

    fd.putXSLForm(m_tdXSL);

    // Begin: Save XML, XSL, template file

    FileOutputStream file;

    File dir = new File(CSurveyAuthor.REPOSITORY_PATH + m_user.getID());

    if(!dir.isDirectory())

      dir.mkdir();



    file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName());

    file.write(m_tdXSL.getContent().getBytes());

    file.close();



    StringBuffer strXSL = new StringBuffer(ResourceLoader.getResourceAsString(this.getClass(), "author_sample.xsl"));

    int pos = strXSL.toString().indexOf("insertinclude");

    strXSL.replace(pos, pos+13, "<xsl:include href=\""+ new File(m_tdXSL.getFileName()).getName() +"\"/>");

    file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName() + "_author_sample.xsl");

    file.write(strXSL.toString().getBytes());

    file.close();

    // End: Save XML, XSL,  file



    Sample sample = (Sample)makeScreen("Sample");

    params.put("back", "Editor");

    params.put("Form", fd);

    sample.init(params);

    return sample;

  }



  public Screen reset(Hashtable params) throws Exception {

	  m_curPage = 1;
	  m_curQuestion = 1;
	  
	  if (m_type.equals("new")) {
		  Vector inputv = new Vector(), entryv = new Vector();
		  for (int j = 0; j < 3; j++) {
			  entryv.addElement(newEntry(j + 1));
		  }
		  inputv.addElement(editInput(1, null, "Check", "Column", entryv));
		  
		  m_pager[m_curPage - 1] = editPage(m_curPage, "Type the title of the survey page here", editQuestion(1, "Type question text here", inputv));
		  
	  } else {
		  m_tdXML = new TextFileData();
		  m_tdXML.parse(new StringBufferInputStream(m_xmlBackup));
		  
		  m_pager = m_tdXML.rgetE("page");		  
	  }
	  
	  return this;

  }



  public Screen save(Hashtable params) throws Exception {

    saveSession(params);

    m_tdXML.putE("page", m_pager);

    XMLData tmp = new XMLData();

    tmp.putE("page", m_pager);

    m_tdXML.putContent( XMLData.xml(tmp, "xml-form"));

    params.put("xmlfile", m_tdXML);

    Screen scr = getScreen("Form");

    return (scr == null) ? this : scr.invoke("returnFromEditor", params);

  }



  public Screen close(Hashtable params) throws Exception {

	Screen scr = getScreen("Peephole");
	scr.reinit(params);
	    
	return scr;

  }

  public Screen dataTypeChanged(Hashtable params) throws Exception {

    saveSession(params);

    int inputID = Integer.parseInt((String)params.get("inputId"));

    String type = (String)params.get("dataType" + String.valueOf(inputID));

    String form = (String)params.get("dataForm" + String.valueOf(inputID));



    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    Object[] inputr = question.rgetE("input");

    XMLData input = (XMLData)inputr[inputID-1];



    if(type.equals("Text"))

      inputr[inputID-1] = editInput(inputID, input.get(), type, form, null);

    else {

      XMLData data = (XMLData)input.getE("data");

      Vector entryv = toVector(data.rgetE("entry"));

      if(entryv.size() < 1){

        for (int j=0; j<3; j++)

          entryv.addElement(newEntry(j+1));

      }

      else{

        if(type.equals("Choice") && entryv.size() < 2)

          return error(CSurveyAuthor.ERROR_INVALID_CHOICE_NUMBER);

        if(type.equals("Check") && entryv.size() < 1)

          return error(CSurveyAuthor.ERROR_INVALID_CHECK_NUMBER);

      }

      inputr[inputID-1] = editInput(inputID, input.get(), type, form, entryv);

    }

    page.putE("question", questionr);

    return this;

  }

  public Screen updateQuestionType(Hashtable params) throws Exception {

    saveSession(params);

    String type = (String)params.get("dataType1");

    String form = (String)params.get("dataForm1");

    String subQuestionSizeSt = (String)params.get("subQuestionSize");

    int subQuestionSize = 0;

    try {

      if(subQuestionSizeSt != null)

        subQuestionSize = Integer.parseInt(subQuestionSizeSt);

      if(subQuestionSize < 0)

        return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

    } catch(NumberFormatException e) {

      return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

    }



    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];


    Vector inputv = new Vector();

    if(type.equals("Text")) {

      if(subQuestionSize == 0)

        inputv.addElement(editInput(1, null, type, form, null));

      else

        for(int i=0; i<subQuestionSize; i++)

          inputv.addElement(editInput(i+1, "Type sub-question text here", type, form, null));

    } else {

      String responseSizeSt = (String)params.get("responseSize");

      int responseSize = 0;

      try {

        if(responseSizeSt != null)

          responseSize = Integer.parseInt(responseSizeSt);

        if(type.equals("Choice") && responseSize < 2)

          return error(CSurveyAuthor.ERROR_INVALID_CHOICE_NUMBER);

        if(type.equals("Check") && responseSize < 1)

          return error(CSurveyAuthor.ERROR_INVALID_CHECK_NUMBER);

      } catch(NumberFormatException e) {

        return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

      }



      if(subQuestionSize == 0) {

        Object[] inputr = question.rgetE("input");

        XMLData input = (XMLData)inputr[0];

        XMLData data = (XMLData)input.getE("data");

        Vector entryv = toVector(data.rgetE("entry"));

        if(entryv.size() >= responseSize)

          entryv.setSize(responseSize);

        else

          for(int j=entryv.size(); j<responseSize; j++)

            entryv.addElement(newEntry(j+1));

        inputv.addElement(editInput(1, null, type, form, entryv));

      }

      else{

        for (int i=0; i<subQuestionSize; i++) {

          Vector entryv = new Vector();

          for (int j=0; j<responseSize; j++)

            entryv.addElement(newEntry(j+1));

          inputv.addElement(editInput(i+1, "Type sub-question text here", type, form, entryv));

        }

      }

    }



    question.putE("input", (XMLData[])inputv.toArray(new XMLData[0]));

    page.putE("question", questionr);

    return this;

  }



  public Screen updateSubQuestionType(Hashtable params) throws Exception {

    saveSession(params);

    int inputID = Integer.parseInt((String)params.get("inputId"));

    String type = (String)params.get("dataType" + String.valueOf(inputID));

    String form = (String)params.get("dataForm" + String.valueOf(inputID));



    XMLData page = (XMLData)m_pager[m_curPage-1];

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    Object[] inputr = question.rgetE("input");



    if(type.equals("Text"))

      inputr[inputID-1] = editInput(inputID, "Type sub-question text here", type, form, null);

    else {

      String responseSizeSt = (String)params.get("responseSize" + String.valueOf(inputID));

      int responseSize = 0;

      try {

        if(responseSizeSt != null)

          responseSize = Integer.parseInt(responseSizeSt);

        if(type.equals("Choice") && responseSize < 2)

          return error(CSurveyAuthor.ERROR_INVALID_CHOICE_NUMBER);

        if(type.equals("Check") && responseSize < 1)

          return error(CSurveyAuthor.ERROR_INVALID_CHECK_NUMBER);

      } catch(NumberFormatException e) {

        return error(CSurveyAuthor.ERROR_INVALID_NUMBER);

      }

      XMLData input = (XMLData)inputr[inputID-1];

      XMLData data = (XMLData)input.getE("data");

      Vector entryv = toVector(data.rgetE("entry"));

      if(entryv.size() >= responseSize)

        entryv.setSize(responseSize);

      else

        for(int j=entryv.size(); j<responseSize; j++)

          entryv.addElement(newEntry(j+1));

      inputr[inputID-1] = editInput(inputID, input.get(), type, form, entryv);
    }

    page.putE("question", questionr);

    return this;

  }



  private XMLData editPage(int id, String title, XMLData question) throws Exception {

    XMLData page = new XMLData();

    page.putA("page-id", String.valueOf(id));

    page.putA("title", title);

    page.putE("question", question);

    return page;

  }



  private XMLData editQuestion(int id, String title, Vector inputv) throws Exception {

    XMLData question = new XMLData();

    question.putA("question-id", String.valueOf(id));

    question.put(title);

    question.putE("input", (XMLData[])inputv.toArray(new XMLData[0]));

    return question;

  }



  private XMLData editInput(int id, String title, String type, String form, Vector entryv) throws Exception {

    XMLData input = new XMLData(), data = new XMLData();

    input.putA("input-id", String.valueOf(id));

    if(title != null)

      input.put(title);

    data.putA("type", type);

    data.putA("form", form);

    if(entryv != null)

      data.putE("entry", (XMLData[])entryv.toArray(new XMLData[0]));

    input.putE("data", data);

    return input;

  }



  private XMLData newEntry(int id) throws Exception {

    XMLData entry = new XMLData();

    entry.putA("data-id", String.valueOf(id));

    entry.put("Type response text here");

    entry.putA("shuffle", "n");

    return entry;

  }



  private Vector toVector(Object[] r) throws Exception {

    Vector v = new Vector();

    if(r != null)

      for(int i=0; i<r.length; i++)

        v.addElement(r[i]);

    return v;

  }



  private String processString(String str) throws Exception {

    if(str == null)

      return null;

    else {

      str = str.trim();

      if(str.length() == 0)

        return null;

      else

        return str;

    }

  }



  private void saveSession(Hashtable params) throws Exception {

    XMLData page = (XMLData)m_pager[m_curPage-1];

    page.putA("title", processString((String)params.get("pageText")));

    Object[] questionr = page.rgetE("question");

    XMLData question = (XMLData)questionr[m_curQuestion-1];

    question.put(processString((String)params.get("questionText")));

    Object[] inputr = question.rgetE("input");

    for(int i=0; i<inputr.length; i++) {

      XMLData input = (XMLData)inputr[i];

      input.put(processString((String)params.get("inputText" + String.valueOf(i+1))));

      XMLData data = (XMLData)input.getE("data");

      String type = (String)data.getA("type");

      String form = (String)params.get("dataForm" + String.valueOf(i+1));

      data.putA("form", form);

      if(!type.equals("Text")) {

        Object[] entryr = data.rgetE("entry");

        for(int j=0; j<entryr.length; j++) {

          XMLData entry = (XMLData)entryr[j];

          entry.put(processString((String)params.get("entryText" + String.valueOf(i+1) + String.valueOf(j+1))));

          entry.putA("href", processString((String)params.get("entryHref" + String.valueOf(i+1) + String.valueOf(j+1))));

          if((String)params.get("entryShuffle" + String.valueOf(i+1) + String.valueOf(j+1)) == null)

            entry.putA("shuffle", null);

          else

            entry.putA("shuffle", "y");

        }

      }

    }

    question.putE("input", inputr);

    page.putE("question", questionr);

  }

}

