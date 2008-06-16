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
import java.text.DateFormat;
import java.util.*;






import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;



import org.jasig.portal.MultipartDataSource;

import org.jasig.portal.IMimeResponse;



public class Export extends SurveyAuthorScreen implements IMimeResponse {

  int m_formId, m_surveyId;

  String m_type = EMPTY;
  String m_defaultFieldSeparator =
  	Channel.getRADProperty("survey.export.fieldSeparator");
  String m_defaultMultiAnswerSeparator =
  	Channel.getRADProperty("survey.export.multiAnswerSeparator");
  	
  String m_fieldSeparator = m_defaultFieldSeparator;
  String m_multiAnswerSeparator = m_defaultMultiAnswerSeparator;



  public Export() {

    m_idempotentURL = true;

  }



  public void init(Hashtable params) throws Exception {

    m_formId = Integer.parseInt((String)params.get("FormId"));

    m_surveyId = Integer.parseInt((String)params.get("SurveyId"));

    m_type = (String)params.get("Type");

    super.init(params);

  }



  public void reinit(Hashtable params) throws Exception {

    init(params);

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("type", m_type);
    params.put("fieldSeparator", m_fieldSeparator);
    params.put("multiAnswerSeparator", m_multiAnswerSeparator);
    return params;

  }



  public XMLData getData() {

    return m_data;

  }




  public String getName() {

    return (String)m_channel.getParameters().get("filename");

  }

  public String getContentType() {

    return "zip";

  }

  public void downloadData(OutputStream out) throws IOException {}

  public Map getHeaders() {

    HashMap h = new HashMap();

    Hashtable params = m_channel.getParameters();

    String type = (String)params.get("type");

    try {

      FormData fd = getBo().getForm(m_formId);

      SurveyData sd = getBo().getSurvey(m_surveyId);

      if(fd != null && sd != null) {

        java.text.SimpleDateFormat formater = new java.text.SimpleDateFormat("MM-dd-yy hh'h'mm a");

        String fn = fd.getTitle() + ", " + formater.format(sd.getSent()).toLowerCase();

        fn += " " + type + ".txt";

        fn = fn.replace('\\',' ').replace('/',' ').replace('*',' ').replace('?',' ');

        fn = fn.replace('"','\'').replace('>',' ').replace('<',' ').replace('|',' ');

        h.put("Content-Disposition", "attachment; filename=\"" + fn + "\"");

      }

    } catch( Exception e) {

      log(e);

      h.put("Content-Disposition", "attachment; filename=\"Results.txt\"");

    }



    return h;

  }



    public InputStream getInputStream() throws IOException {

        StringBuffer rslt = new StringBuffer();

        Hashtable params = m_channel.getParameters();
/*
// spill parameters (uncomment to see)...
System.out.println("#### SPILLING PARAMETERS...");
Enumeration n = params.keys();
while (n.hasMoreElements()) {
    Object key = n.nextElement();
    Object value = params.get(key);
    System.out.println("\t"+key.toString()+"="+value.toString());
}
*/
        FormData fd = null;
        Vector dddv = null;
        DataDetailData[] sumr = null;
        String type = (String)params.get("type");
        String formFieldSep = (String)params.get("fieldSeparator");
        String formMultiAnswerSep = (String)params.get("multiAnswerSeparator");
        IExportFormat exportFormat;
        
        if ("delimited_details".equals(type)) {
            if (formFieldSep == null || "".equals(formFieldSep.trim())) {
        	    m_fieldSeparator = m_defaultFieldSeparator;
            } else {
                m_fieldSeparator = formFieldSep;
            }
            if (formMultiAnswerSep == null || "".equals(formMultiAnswerSep.trim())) {
        	    m_multiAnswerSeparator = m_defaultMultiAnswerSeparator;
            } else {
        	    m_multiAnswerSeparator = formMultiAnswerSep;
            }
            exportFormat = new ExportableFormat(m_fieldSeparator, m_multiAnswerSeparator);
        } else {
            exportFormat = new HumanReadableFormat();
        }

        try {

            fd = getBo().getForm(m_formId);
            dddv = getBo().getExport(m_surveyId);
            for (int z=0; z<dddv.size(); z++) {
                if (((DataDetailData)dddv.elementAt(z)).getDataText() != null) {
                    dddv.removeElementAt(z--);
                }
            }
            sumr = (DataDetailData[])dddv.toArray(new DataDetailData[0]);

        } catch(Exception e) {
            e.printStackTrace(System.out);
        }

        if (fd != null) {

            exportFormat.summaryTitle(rslt);

            // ## PAGE ##
            Object[] exchPage = fd.getXMLForm().rgetE("page");
            if (exchPage != null) {

                for (int i=0; i<exchPage.length; i++) {

                    String pageID = (String)((XMLData)exchPage[i]).getA("page-id");

                    exportFormat.pageTitle(rslt, (XMLData)exchPage[i]);

                    // ## QUESTION ##
                    Object[] exchQuestion = ((XMLData)exchPage[i]).rgetE("question");
                    if (exchQuestion != null) {

                        for (int j=0; j<exchQuestion.length; j++) {

                            String questionID = (String)((XMLData)exchQuestion[j]).getA("question-id");

                            Object[] exchInput = ((XMLData)exchQuestion[j]).rgetE("input");
                            boolean hasSubQuestion = exchInput != null && exchInput.length > 0 &&
                                ((XMLData)exchInput[0]).get() != null &&
                                (((XMLData)exchInput[0]).get()).trim().length() != 0;
                            exportFormat.question(rslt, (XMLData)exchQuestion[j], hasSubQuestion);

                            // ## INPUT ##
                            if (exchInput != null) {

                                for (int k=0; k<exchInput.length; k++) {

                                    String inputID = (String)((XMLData)exchInput[k]).getA("input-id");

                                    if (((XMLData)exchInput[k]).get() != null && (((XMLData)exchInput[k]).get()).trim().length() != 0) {
                                        exportFormat.subQuestion(rslt, (XMLData)exchInput[k]);
                                    }

                                    Object[] exchData = ((XMLData)exchInput[k]).rgetE("data");

                                    if (exchData != null) {

                                        Object[] exchEntry = ((XMLData)exchData[0]).rgetE("entry");
                                        if (exchEntry != null) {

                                            for (int l=0; l<exchEntry.length; l++) {

                                                String dataID = (String)((XMLData)exchEntry[l]).getA("data-id");

                                                boolean found = false;

                                                if (sumr != null) {

                                                    for (int m=0; m<sumr.length; m++) {

                                                        if (pageID.equals(sumr[m].getPageId().toString()) && questionID.equals(sumr[m].getQuestionId().toString()) && inputID.equals(sumr[m].getInputId().toString()) && dataID.equals(sumr[m].getDataChoiceId().toString())) {

                                                            exportFormat.response(rslt, (XMLData)exchEntry[l]);
                                                            exportFormat.numResponses(rslt, sumr[m]);
                                                            exportFormat.percent(rslt, sumr[m]);

                                                            found = true;

                                                            break;

                                                        }

                                                    }

                                                }

                                                if (!found) {
                                                    exportFormat.notFoundPercent(rslt, (XMLData)exchEntry[l]);
                                                }

                                            }

                                        } else {

                                            // Textual question...
                                            exportFormat.textSummary(rslt);

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

            }

        }

        // Handle either 'respondants' or 'details' where appropriate...
        if(type.equals("respondants")) {

            exportFormat.respondentsTitle(rslt);


            Vector v = null;
            try {
                v = getBo().getAllResponse(m_surveyId);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IOException("Survey Export failed to obtain responses "
                                                + "for the specified survey.");
            }

            if (fd != null) {

                Enumeration resp = v.elements();
                while (resp.hasMoreElements()) {

                    FormDataData response = (FormDataData) resp.nextElement();
                    exportFormat.respondentsUser(rslt, response);

                }

            }

        } else if("details".equals(type) || "delimited_details".equals(type)) {

            SurveyData sData = null;
            try {
                sData = getBo().getSurvey(m_surveyId);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IOException("Survey Export failed to obtain the specified survey.");
            }

            Vector v = null;
            try {
                v = getBo().getAllResponse(m_surveyId);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IOException("Survey Export failed to obtain responses "
                                                + "for the specified survey.");
            }

/*
System.out.println("#### SPILLING DATA ID'S...");
Enumeration n = v.elements();
while (n.hasMoreElements()) {
    System.out.println("\t"+n.nextElement().toString());
}
*/

            if (fd != null) {

                Enumeration resp = v.elements();
                while (resp.hasMoreElements()) {

                    // Next response, username, & timestamp.
                    FormDataData response = (FormDataData) resp.nextElement();
                    exportFormat.detailsUser(rslt, sData, response);

                    // Obtain the details & map them to questions.
                    Vector details = null;
                    try {
                        details = getBo().getResponseDetail(response.getDataId().intValue());
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        throw new IOException("Unable to get response details.");
                    }
                    Map answers = new HashMap();
                    Enumeration dtl = details.elements();
                    while (dtl.hasMoreElements()) {
                        DataDetailData d = (DataDetailData) dtl.nextElement();
                        AnswerKey key = new AnswerKey(d.getPageId(), d.getQuestionId(), d.getInputId());
                        if (d.getDataChoiceId() != null) {      // multiple choice...
                            List l = (List) answers.get(key);
                            if (l == null) {
                                l = new ArrayList();
                            }
                            l.add(new McResponse(d.getDataChoiceId(), d.getDataText()));
                            answers.put(key, l);
                        } else if (d.getDataText() != null) {   // textual...
                            answers.put(key, d.getDataText());
                        }
                    }

/*
System.out.println("#### Choices...");
Iterator it = answers.keySet().iterator();
while (it.hasNext()) {
    Object key = it.next();
    Object value = answers.get(key);
    System.out.println("\t"+key+"="+value);
}
*/

                    // ## PAGE ##
                    Object[] exchPage = fd.getXMLForm().rgetE("page");
                    if (exchPage != null) {

                        for (int i=0; i<exchPage.length; i++) {

// System.out.println(XMLData.xml((XMLData) exchPage[i], "page"));

                            String pageID = (String)((XMLData)exchPage[i]).getA("page-id");
                            Integer pId = new Integer(pageID);

                            exportFormat.pageTitle(rslt, (XMLData)exchPage[i]);

                            // ## QUESTION ##
                            Object[] exchQuestion = ((XMLData)exchPage[i]).rgetE("question");
                            if (exchQuestion != null) {

                                for (int j=0; j<exchQuestion.length; j++) {
                                    String questionID = (String)((XMLData)exchQuestion[j]).getA("question-id");
                                    Integer qId = new Integer(questionID);

                                    exportFormat.questionWithAnswers(rslt, (XMLData)exchQuestion[j]);

                                    // ## INPUT ##
                                    Object[] exchInput = ((XMLData)exchQuestion[j]).rgetE("input");
                                    if (exchInput != null) {

                                        for (int k=0; k<exchInput.length; k++) {

                                            String inputID = (String)((XMLData)exchInput[k]).getA("input-id");
                                            Integer iId = new Integer(inputID);
                                            if (((XMLData)exchInput[k]).get() != null && (((XMLData)exchInput[k]).get()).trim().length() != 0) {
                                                exportFormat.subQuestionWithAnswers(rslt, (XMLData)exchInput[k]);
                                            }

                                            AnswerKey key = new AnswerKey(pId, qId, iId);
                                            exportFormat.answer(rslt, key, answers,
                                                ((XMLData)((XMLData)exchInput[k]).rgetE("data")[0]).rgetE("entry"));
                                        }

                                    }

                                }

                            }

                        }

                    }

                }

            }

        }

        return new ByteArrayInputStream(rslt.toString().getBytes());

    }

    public void reportDownloadError(Exception e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Exception]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // NB:  This method was added to comply with
        // the uPortal 2.2 contract for IMimeResponse.

        // ToDo:  anything???

    }

  /* End: IMimeResponse */

}

