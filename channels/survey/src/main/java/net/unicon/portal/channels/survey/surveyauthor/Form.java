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
import net.unicon.portal.channels.rad.Screen;



import org.jasig.portal.utils.ResourceLoader;

import org.xml.sax.ContentHandler;



import javax.xml.transform.*;

import javax.xml.transform.stream.*;



public class Form extends SurveyAuthorScreen {

  FormData m_fd;

  Vector m_xslFileNamev;

  TextFileData m_tdXML, m_tdXSL;

  String m_formID, m_oldXMLFile, m_oldXSLFile;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_fd = new FormData();

    m_tdXML = new TextFileData();

    m_tdXSL = new TextFileData();

    m_xslFileNamev = new Vector();



    m_formID = (String)params.get("FormId");



    File dir = new File(CSurveyAuthor.STYLES_PATH);

    File[] list = dir.listFiles();

    if(dir.list() != null) {

      for(int i=0; i<list.length; i++) {

        if(list[i].isFile()){

          TextFileData td = new TextFileData();

          td.putFileName(list[i].getName());

          td.putFileShow(list[i].getName());

          m_xslFileNamev.addElement(td);
        }

      }

    }

    if(m_formID == null)

      m_fd.putType((String)params.get("Type"));

    else {

      m_fd = getBo().getForm(Integer.parseInt(m_formID));

      m_tdXML = m_fd.getXMLForm();

      m_tdXSL = m_fd.getXSLForm();

      m_tdXML.putContent(TextFileData.getContentByFilename(CSurveyAuthor.REPOSITORY_PATH + m_tdXML.getFileName()));

      m_oldXMLFile = m_tdXML.getFileName();

      m_oldXSLFile = m_tdXSL.getFileName();

      for(int i=0; i<m_xslFileNamev.size(); i++)

        if(m_tdXSL.getFileShow().equals(((TextFileData)m_xslFileNamev.elementAt(i)).getFileShow())) {

          m_xslFileNamev.removeElementAt(i);

          m_xslFileNamev.addElement(m_tdXSL);

          break;

        }

    }

  }



  public void reinit(Hashtable params) throws Exception {

    if ((String)params.get("Init") != null && ((String)params.get("Init")).equals("Yes"))

      init(params);

  }



  public XMLData getData() throws Exception {

    m_data.putE("File", (TextFileData[])m_xslFileNamev.toArray(new TextFileData[0]));

    m_data.putE("Form", m_fd);

    return m_data;

  }



  public Hashtable getXSLTParams() {
	  Hashtable params = new Hashtable();
	  if (m_fd.getTitle() != null) {
		  params.put("Title", m_fd.getTitle());
	  }
	  if (m_formID != null) {
		  params.put("FormID", m_formID);
	  }
	  return params;
  }



  public Screen editXML(Hashtable params) throws Exception {

    if ((String)params.get("Title") != null)

      m_fd.putTitle((String)params.get("Title"));
    
    if((String)params.get("xsl") == null) {

      return error(CSurveyAuthor.ERROR_NOT_FOUND_XSL);

    } else

      m_tdXSL = TextFileData.parseFilePath((String)params.get("xsl"));

    if(m_formID != null){

      if(m_oldXSLFile.equals(m_tdXSL.getFileName()))

        m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName()));

      else

        m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));
    }

    else

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));

    m_tdXSL.putFileName(m_user.getID() + "/" + m_tdXSL.getFileShow() + "_v" + String.valueOf(findMaxVersion(getBo().listFormSurveyforAuthor(false, m_user), "XSL") + 1));

    m_fd.putXSLForm(m_tdXSL);

    m_fd.putXMLForm(m_tdXML);



    Editor editor = (Editor)makeScreen("Editor");

    params.put("title", m_fd.getTitle());

    params.put("xslfile", m_tdXSL);

    params.put("xmlString", XMLData.xml(m_tdXML, "xmlString"));

    editor.init(params);

    return editor;

  }



  public Screen uploadXML(Hashtable params) throws Exception {

    if ((String)params.get("Title") != null)

      m_fd.putTitle((String)params.get("Title"));

    if((String)params.get("xsl") == null) {
      return error(CSurveyAuthor.ERROR_NOT_FOUND_XSL);
    }
    else

      m_tdXSL = TextFileData.parseFilePath((String)params.get("xsl"));

    m_fd.putXSLForm(m_tdXSL);

    Upload source = (Upload)makeScreen("Upload");

    source.init(params);

    return source;

  }

  public Screen textXSL(Hashtable params) throws Exception {

    if ((String)params.get("Title") != null)

      m_fd.putTitle((String)params.get("Title"));

    if((String)params.get("xsl") == null){
      return error(CSurveyAuthor.ERROR_NOT_FOUND_XSL);
    }

    else

      m_tdXSL = TextFileData.parseFilePath((String)params.get("xsl"));

    if(m_formID == null || m_tdXSL.getVersion().equals("0")) {

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));

    } else {

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName()));

    }

    m_fd.putXSLForm(m_tdXSL);

    Source source = (Source)makeScreen("Source");

    params.put("Source",m_tdXSL);

    source.init(params);

    return source;

  }



  public Screen preview(Hashtable params) throws Exception {

    m_fd.putTitle(((String)params.get("Title")).trim());

    if((String)params.get("xsl") == null) {
      return error(CSurveyAuthor.ERROR_NOT_FOUND_XSL);
    }

    else

      m_tdXSL = TextFileData.parseFilePath((String)params.get("xsl"));

    if(m_formID != null){

      if(m_oldXSLFile.equals(m_tdXSL.getFileName()))

        m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName()));

      else

        m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));
    }

    else

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));

    m_tdXSL.putFileName(m_user.getID() + "/" + m_tdXSL.getFileShow() + "_v" + String.valueOf(findMaxVersion(getBo().listFormSurveyforAuthor(false, m_user), "XSL") + 1));

    m_fd.putXSLForm(m_tdXSL);



    if (m_tdXML.getContent() == null)

      return error(CSurveyAuthor.ERROR_EMPTY_FORM_XML);



    if (tryProcessXSLT(m_tdXML.getContent(), m_tdXSL.getContent()) == false)

      return error(CSurveyAuthor.ERROR_XSL);



    // Begin: Save XML, XSL, template file

    FileOutputStream file;

    File dir = new File(CSurveyAuthor.REPOSITORY_PATH + m_user.getID());

    if(!dir.isDirectory())

      dir.mkdir();



    file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName());

    file.write(m_tdXSL.getContent().getBytes());

    file.close();



    saveTemplate("author_sample.xsl");

    // End: Save XML, XSL,  file



    Sample sample = (Sample)makeScreen("Sample");

    params.put("back", "Form");

    params.put("Form", m_fd);

    sample.init(params);

    return sample;

  }



  public Screen Cancel(Hashtable params) throws Exception {
      
      Screen scr = null;
      
      if (m_tdXML.getContent() == null || m_formID != null) {
      
          scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");
          
          scr.reinit(params);
      
      } else {

	    scr = makeScreen("Confirm");
	
	    params.put("title", m_fd.getTitle());
	
	    params.put("xslfile", m_tdXSL);
	
	    params.put("xmlString", XMLData.xml(m_tdXML, "xmlString"));
	    
	    scr.init(params);
      }
    
    return scr;

  }



  public Screen OK(Hashtable params) throws Exception {

    String title = (String)params.get("Title");

    if (title == null || title.trim().length() == 0)

      return error(CSurveyAuthor.ERROR_NO_TITLE);

    m_fd.putTitle(title);



    if(m_formID == null) {

      title = title.replace('\\',' ').replace('/',' ').replace('*',' ').replace('?',' ');

      title = title.replace('"','\'').replace('>',' ').replace('<',' ').replace('|',' ');

      m_tdXML.putFileShow(title);

      m_tdXML.putFileName(m_user.getID() + "/" + m_tdXML.getFileShow() + "_v" + String.valueOf(findMaxVersion(getBo().listFormSurveyforAuthor(false, m_user), "XML") + 1));

    }

    m_fd.putXMLForm(m_tdXML);

    if((String)params.get("xsl") == null) {
      return error(CSurveyAuthor.ERROR_NOT_FOUND_XSL);
    }

    else

      m_tdXSL = TextFileData.parseFilePath((String)params.get("xsl"));

    if(m_formID == null || !m_oldXSLFile.equals(m_tdXSL.getFileName()))

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.STYLES_PATH + m_tdXSL.getFileName()));

    else

      m_tdXSL.putContent(TextFileData.getContentByFilename(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName()));

    m_tdXSL.putFileName(m_user.getID() + "/" + m_tdXSL.getFileShow() + "_v" + String.valueOf(findMaxVersion(getBo().listFormSurveyforAuthor(false, m_user), "XSL") + 1));

    m_fd.putXSLForm(m_tdXSL);



    if (m_tdXML.getContent() == null)

      return error(CSurveyAuthor.ERROR_EMPTY_FORM_XML);



    if (tryProcessXSLT(m_tdXML.getContent(), m_tdXSL.getContent()) == false)

      return error(CSurveyAuthor.ERROR_XSL);



    // don't overwrite the form owner
    if (m_fd.getUserId() == null) {
      m_fd.putUserId(m_user.getIdentifier());
      m_fd.putUserName(m_user.getName());
    }



    Calendar date = Calendar.getInstance();

    date.setTimeZone(TimeZone.getTimeZone("GMT+0"));



    if(m_formID == null) {

      m_fd.putCreated(date.getTime());

      try {

        getBo().createForm(m_fd);

      } catch(Exception e) {

        return error(e.getMessage());

      }

    } else {

      m_fd.putModified(date.getTime());

      try {

        getBo().updateForm(m_fd);

      } catch(Exception e) {

        return error(e.getMessage());

      }

    }

    //Begin: Delete old file

    if(m_formID != null) {

      File f;

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXMLFile);

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile);

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile + "_author_sample.xsl");

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile + "_author_results.xsl");

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile + "_survey_survey.xsl");

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile + "_poll_peephole.xsl");

      f.delete();

      f = new File(CSurveyAuthor.REPOSITORY_PATH + m_oldXSLFile + "_poll_results.xsl");

      f.delete();

    }

    //End: Delete old file



    // Begin: Save XML, XSL, template file

    FileOutputStream file;

    File dir = new File(CSurveyAuthor.REPOSITORY_PATH + m_user.getID());

    if(!dir.isDirectory())

      dir.mkdir();



    file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName());

    file.write(m_tdXSL.getContent().getBytes());

    file.close();



    file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXML.getFileName());

    file.write(m_tdXML.getContent().getBytes());

    file.close();



    saveTemplate("author_sample.xsl");

    saveTemplate("author_results.xsl");

    saveTemplate("survey_survey.xsl");

    saveTemplate("poll_peephole.xsl");

    saveTemplate("poll_results.xsl");

    // End: Save XML, XSL,  file



    Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");

    scr.reinit(params);

    return scr;

  }



  private boolean tryProcessXSLT(String xml, String xsl) throws Exception {

    StreamSource xmlSource = new StreamSource(new StringReader(xml));

    StreamSource xslSource = new StreamSource(new StringReader(xsl));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    Result result = new StreamResult(baos);

    try {

      Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);

      trans.transform(xmlSource, result);

    } catch (Exception e) {

      return false;

    }

    return true;

  }



  private void saveTemplate(String filename) throws Exception {

    StringBuffer strXSL = new StringBuffer(ResourceLoader.getResourceAsString(this.getClass(), filename));

    int pos = strXSL.toString().indexOf("insertinclude");

    strXSL.replace(pos, pos+13, "<xsl:include href=\""+ new File(m_tdXSL.getFileName()).getName() +"\"/>");

    FileOutputStream file = new FileOutputStream(CSurveyAuthor.REPOSITORY_PATH + m_tdXSL.getFileName() + "_" + filename);

    file.write(strXSL.toString().getBytes());

    file.close();

  }



  private int findMaxVersion(FormData[] fdr, String type) throws Exception {

    int version = 0;

    if(type.equals("XML")) {

      for(int i=0; i<fdr.length; i++)

        if(fdr[i].getUserId().equals(m_user.getIdentifier()) && m_tdXML.getFileShow().equals(fdr[i].getXMLForm().getFileShow()) && version < Integer.parseInt(fdr[i].getXMLForm().getVersion()))

          version = Integer.parseInt(fdr[i].getXMLForm().getVersion());

    } else {

      for(int i=0; i<fdr.length; i++)

        if(fdr[i].getUserId().equals(m_user.getIdentifier()) && m_tdXSL.getFileShow().equals(fdr[i].getXSLForm().getFileShow()) && version < Integer.parseInt(fdr[i].getXSLForm().getVersion()))

          version = Integer.parseInt(fdr[i].getXSLForm().getVersion());

    }

    return version;

    }

    public Screen returnFromUpload(Hashtable params) throws Exception {

        m_tdXML = (TextFileData) params.get("xmlfile");

        m_tdXML.putFileName(m_user.getID()
                + "/"
                + m_tdXML.getFileShow()
                + "_v"
                + String.valueOf(findMaxVersion(getBo()
                        .listFormSurveyforAuthor(false, m_user), "XML") + 1));

        m_fd.putXMLForm(m_tdXML);

        return this;

    }

    public Screen returnFromEditor(Hashtable params) throws Exception {

        TextFileData tmp = (TextFileData) params.get("xmlfile");

        if (tmp != null) {

            m_tdXML = tmp;

            m_fd.putXMLForm(m_tdXML);

        }

        return this;

    }

    public Screen returnFromCancel(Hashtable params) throws Exception {

        TextFileData tmp = (TextFileData) params.get("xmlfile");

        if (tmp != null) {

            m_tdXML = tmp;

            m_fd.putXMLForm(m_tdXML);

        }

        return this;

    }
    
}



