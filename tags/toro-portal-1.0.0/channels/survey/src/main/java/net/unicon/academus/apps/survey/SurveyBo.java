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
package net.unicon.academus.apps.survey;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import net.unicon.academus.apps.form.DataDetailData;
import net.unicon.academus.apps.form.FormData;
import net.unicon.academus.apps.form.FormDataData;
import net.unicon.academus.apps.rad.DBService;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.SQL;
import net.unicon.academus.apps.rad.StdBo;
import net.unicon.portal.util.db.AcademusDBUtil;


public class SurveyBo extends StdBo {



  static public final String ERROR_RDBM = "Database error.";

  static public final String DS = "Survey";



  private FormData[] m_fdr = new FormData[0];
  private SurveyData[] m_sdr = new  SurveyData[0];
  private String m_path = null;



  public SurveyBo() {
    super("SurveyBo");
  }

  public void setFilePath(String path) throws Exception {
    m_path = path;
  }

  public FormData[] listFormSurveyforAuthor(boolean refresh, IdentityData id)
  throws Exception {
    return listFormSurveyforAuthor(refresh, id, false);
  }


  public FormData[] listFormSurveyforAuthor(boolean refresh, IdentityData id,
    boolean getAll) throws Exception {

    if(m_fdr != null && !refresh)

      return m_fdr;

    Vector fdv = new Vector(), tdv = new Vector(), sdv = new Vector(), v = new Vector();
    
    String userId = id.getIdentifier();

    int i,j;

    DBService db = null;
    
    StringBuffer qry = new StringBuffer();

    try {
    db = getDBService(DS);

    ResultSet rs1 = null, rs2 = null, rs3 = null;

    try {
      if (getAll) {
		rs1 = db.select("SELECT form_id, title, type, xml, xsl, created, modified, user_id, user_name FROM upc_form_form");
      } else {
        qry.append("SELECT form_id, title, type, xml, xsl, created, modified, user_id, user_name FROM upc_form_form WHERE type='Public Survey' OR (type='Private Survey' AND user_id=");
        qry.append(SQL.esc(userId));
        qry.append(")");
        rs1 = db.select(qry.toString());
      }

      while (rs1.next()) {

        FormData fd = FormData.parse(rs1);

        fdv.addElement(fd);

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      try {
        if (rs1 != null) rs1.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();
    }

    try {

      if (getAll) {
          qry.setLength(0); // Clear the StringBuffer
          qry.append("SELECT ss.*,(SELECT COUNT(sd.SURVEY_ID) FROM ");
          qry.append("UPC_SURVEY_DATA sd WHERE ss.SURVEY_ID=sd.SURVEY_ID) ");
          qry.append("as NO FROM UPC_SURVEY_SURVEY ss");          
          rs2 = db.select(qry.toString());
      } else {
          /* TT 04774 - Number of records in SQL IN clause is limited to 1000 in some RDBMS's
          Fix: Changed to JOIN statement. */    
          qry.setLength(0); // Clear the StringBuffer
          qry.append("SELECT ss.*,(SELECT COUNT(sd.SURVEY_ID) FROM ");
          qry.append("UPC_SURVEY_DATA sd WHERE ss.SURVEY_ID=sd.SURVEY_ID) ");
          qry.append("as NO FROM UPC_FORM_FORM ff, UPC_SURVEY_SURVEY ss ");
          qry.append("WHERE (ff.USER_ID =");
          qry.append(SQL.esc(userId));
          qry.append(" AND ff.FORM_ID=ss.FORM_ID)");
          rs2 = db.select(qry.toString());
      }
      
      while (rs2.next()) {

        SurveyData sd = SurveyData.parse(rs2);

        sdv.addElement(sd);

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      try {
        if (rs2 != null) rs2.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();
    }

    try {

      if (getAll) {
          rs3 = db.select("SELECT survey_id, object FROM UPC_SURVEY_TARGET");
      } else {
          /* TT 04774 - Number of records in SQL IN clause is limited to 1000 in some RDBMS's
          Fix: Changed to JOIN statement. */ 
          qry.setLength(0); // Clear the StringBuffer
          qry.append("SELECT st.survey_id, st.object FROM UPC_FORM_FORM ff, ");
          qry.append("UPC_SURVEY_SURVEY ss, UPC_SURVEY_TARGET st ");
          qry.append("WHERE (ff.USER_ID=");
          qry.append(SQL.esc(userId));
          qry.append(" AND ff.FORM_ID=ss.FORM_ID ");
          qry.append("AND st.SURVEY_ID=ss.SURVEY_ID)");
          rs3 = db.select(qry.toString());
      }
      
      while(rs3.next()) {
          tdv.addElement(TargetData.parse(rs3));
      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM, e);

    } finally {

      try {
        if (rs3 != null) rs3.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();
    }

    } finally {
      if (db != null) db.release();
    }


    m_fdr = (FormData[])fdv.toArray(new FormData[0]);

    m_sdr = (SurveyData[])sdv.toArray(new SurveyData[0]);



    for(i=0; i<m_sdr.length; i++) {

      v = new Vector();

      for(j=0; j<tdv.size(); j++)

        if(m_sdr[i].getSurveyId().equals(((TargetData)tdv.elementAt(j)).getSurveyId()))

          v.addElement(((TargetData)tdv.elementAt(j)).getObject());

      m_sdr[i].putTarget((IdentityData[])v.toArray(new IdentityData[0]));

    }



    for(i=0; i<m_fdr.length; i++) {

      v = new Vector();

      for(j=0; j<m_sdr.length; j++)

        if(m_fdr[i].getFormId().equals(m_sdr[j].getFormId()))

          v.addElement(m_sdr[j]);

      m_fdr[i].putSurvey((SurveyData[])v.toArray(new SurveyData[0]));

    }

    return m_fdr;

  }



  public FormData[] listFormSurveyforSurvey(IdentityData id, String group) throws Exception {

    Vector fdv = new Vector(), sdv = new Vector(), fddv = new Vector(), v = new Vector();

    ResultSet rs1 = null, rs2 = null, rs3 = null;

    DBService db = null;
    
    StringBuffer qry = new StringBuffer();

    try {
    db = getDBService(DS);

    try {

      qry.append("SELECT DISTINCT ss.SURVEY_ID, ss.FORM_ID, ss.TYPE, ");
      qry.append("ss.SENT, ss.USER_ID, ss.USER_NAME, ss.distribution_title ");
      qry.append("FROM UPC_SURVEY_SURVEY ss, UPC_SURVEY_TARGET t ");
      qry.append("WHERE ss.SURVEY_ID=t.SURVEY_ID AND t.OBJECT in ");
      qry.append(group);
      qry.append(" AND ss.CLOSED is null AND ss.TYPE <> 'Poll'");
      
      rs1 = db.select(qry.toString());

      while (rs1.next()) {

        SurveyData sd = new SurveyData();

        sd.putSurveyId(new Integer(rs1.getInt("SURVEY_ID")));

        sd.putFormId(new Integer(rs1.getInt("FORM_ID")));

        sd.putType(rs1.getString("TYPE"));

        sd.putSent(rs1.getTimestamp("SENT"));

        sd.putUserId(rs1.getString("USER_ID"));

        sd.putUserName(rs1.getString("USER_NAME"));
        
        sd.putDistributionTitle(rs1.getString("DISTRIBUTION_TITLE"));

        sdv.addElement(sd);
      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      try {
        if (rs1 != null) rs1.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();
    }

    m_sdr = (SurveyData[])sdv.toArray(new SurveyData[0]);

    try {
      /* TT 04774 - Number of records in SQL IN clause is limited to 1000 in some RDBMS's
         Fix: Changed to JOIN statement. */
      qry.setLength(0); // Clear the StringBuffer
      qry.append("SELECT DISTINCT sd.SURVEY_ID, fd.CREATED, fd.MODIFIED ");
      qry.append("FROM UPC_SURVEY_SURVEY ss, UPC_SURVEY_TARGET t, ");
      qry.append("UPC_SURVEY_DATA sd, UPC_FORM_DATA fd ");
      qry.append("WHERE ((ss.SURVEY_ID=t.SURVEY_ID AND t.OBJECT IN ");
      qry.append(group);
      qry.append(" AND ss.CLOSED is null AND ss.TYPE <> 'Poll')");
      qry.append("AND fd.USER_ID=");
      qry.append(SQL.esc(id.getIdentifier()));
      qry.append(" AND sd.DATA_ID=fd.DATA_ID)");
      
      rs2 = db.select(qry.toString());      
      
      while(rs2.next()) {

        FormDataData fdd = new FormDataData();

        fdd.putSurveyId(new Integer(rs2.getInt("SURVEY_ID")));

        fdd.putCreated(rs2.getTimestamp("CREATED"));

        fdd.putModified(rs2.getTimestamp("MODIFIED"));

        fddv.addElement(fdd);

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs2 != null) rs2.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();

    }



    for(int i=0; i<m_sdr.length; i++) {

      for(int j=0; j<fddv.size(); j++) {

        FormDataData fdd = (FormDataData)fddv.elementAt(j);

        if(m_sdr[i].getSurveyId().equals(fdd.getSurveyId()))

          m_sdr[i].putLastReply((fdd.getModified() != null) ? fdd.getModified() : fdd.getCreated());

      }

    }

    try {
      /* TT 04774 - Number of records in SQL IN clause is limited to 1000 in some RDBMS's
         Fix: Changed to JOIN statement. */
      qry.setLength(0);  // Clear the StringBuffer
      qry.append("SELECT DISTINCT ff.* ");
      qry.append("FROM UPC_SURVEY_SURVEY ss, UPC_SURVEY_TARGET t, UPC_FORM_FORM ff ");
      qry.append("WHERE ((ss.SURVEY_ID=t.SURVEY_ID AND t.OBJECT IN ");
      qry.append(group);
      qry.append(" AND ss.CLOSED is null AND ss.TYPE <> 'Poll') ");
      qry.append("AND ff.FORM_ID=ss.FORM_ID)");
      
      rs3 = db.select(qry.toString());
      
      while(rs3.next()) {

        FormData fd = FormData.parse(rs3);

        v = new Vector();

        for(int k=0; k<m_sdr.length; k++)

          if(fd.getFormId().equals(m_sdr[k].getFormId()))

            v.addElement(m_sdr[k]);

        fd.putSurvey((SurveyData[])v.toArray(new SurveyData[0]));

        fdv.addElement(fd);

      }

    } catch(Exception e) {

          throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs3 != null) rs3.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
      db.closeStatement();


    }

    } finally {
      if (db != null) db.release();
    }



    m_fdr = (FormData[])fdv.toArray(new FormData[0]);

    return m_fdr;

  }



  public FormData[] listFormSurveyforPoll(boolean refresh) throws Exception {

    if(m_fdr != null && !refresh)

      return m_fdr;



    Vector fdv = new Vector(), sdv = new Vector();

    DBService db = null;

    ResultSet rs = null;



    try {

      db = getDBService(DS);
      
      // TT 04855 -- BSS -->
      // Removed constraint ss.SENT=(most recent distribution for a survey) so 
      // that all open distributions of each survey with type 'poll' are 
      // returned. 
      rs = db.select("select ff.FORM_ID as FORM_ID1, ff.TITLE, ff.XML, ff.XSL" +
              ", ss.SURVEY_ID, ss.FORM_ID as FORM_ID2, ss.SENT" +
              ", (select count(sd.SURVEY_ID) from UPC_SURVEY_DATA sd" +
              " where ss.SURVEY_ID=sd.SURVEY_ID) as NO" +
              ", ss.distribution_title from UPC_FORM_FORM ff" +
              ", UPC_SURVEY_SURVEY ss " +
              "where ff.FORM_ID=ss.FORM_ID and ss.TYPE='Poll'" +
              " and ss.CLOSED is null ");
      
      while (rs.next()) {

        FormData fd = new FormData();

        SurveyData sd = new SurveyData();



        fd.putFormId(new Integer(rs.getInt("FORM_ID1")));

        fd.putTitle(rs.getString("TITLE"));

        fd.putXMLForm(TextFileData.parseFilePath(rs.getString("XML").replace('/',File.separatorChar)));

        fd.putXSLForm(TextFileData.parseFilePath(rs.getString("XSL").replace('/',File.separatorChar)));

        sd.putSurveyId(new Integer(rs.getInt("SURVEY_ID")));

        sd.putFormId(new Integer(rs.getInt("FORM_ID2")));

        sd.putSent(rs.getTimestamp("SENT"));
        
        sd.putDistributionTitle(rs.getString("DISTRIBUTION_TITLE"));

        sd.putReplied(new Integer(rs.getInt("NO")));

        fd.putSurvey(new SurveyData[]{sd});

        sdv.addElement(sd);

        fdv.addElement(fd);

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      if (db != null) {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }



    m_sdr = (SurveyData[])sdv.toArray(new SurveyData[0]);

    m_fdr = (FormData[])fdv.toArray(new FormData[0]);

    return m_fdr;

  }
  
  /**
   * Retrieves the form based on the distribution.
   * Added due to TT 04855 to allow Poll to retrieve and
   * display named distributions.
   * 
   * @param surveyID   identifier of the distribution (unique survey id)
   * @return FormData  
   * @throws Exception
   */
  public FormData getFormBySurveyId(int surveyID) throws Exception {

      FormData fd = null;
      
      if(m_fdr != null && m_fdr.length > 0) {

        for(int i=0; i < m_fdr.length; i++)

            if(m_fdr[i].getSurvey()[0].getSurveyId().intValue() == surveyID) {
            fd = m_fdr[i];

            break;

          }

      } 

      if(fd == null)

        return null;



      TextFileData xml = new TextFileData();

      TextFileData xmlTmp = fd.getXMLForm();

      xml.putFileName(xmlTmp.getFileName());

      xml.putFileShow(xmlTmp.getFileShow());

      xml.putVersion(xmlTmp.getVersion());

      xml.parse(new FileInputStream(m_path + xml.getFileName()));

      fd.putXMLForm(xml);

      return fd;

    }


  public FormData getForm(int formID) throws Exception {

    FormData fd = null;
    boolean found = false;
    
    if(m_fdr != null && m_fdr.length > 0) {

      for(int i=0; i < m_fdr.length; i++)

        if(m_fdr[i].getFormId().intValue() == formID) {

          fd = m_fdr[i];
          
          found = true;

          break;

        }

    } 

    if(!found){

      fd = selectFormByID(formID);
      
      // add the form to the m_fdr      
      ArrayList forms = new ArrayList(Arrays.asList(m_fdr));
      forms.add(fd);
      m_fdr = (FormData[])(forms.toArray(new FormData[0]));
    
    }

    if(fd == null)

      return null;



    TextFileData xml = new TextFileData();

    TextFileData xmlTmp = fd.getXMLForm();

    xml.putFileName(xmlTmp.getFileName());

    xml.putFileShow(xmlTmp.getFileShow());

    xml.putVersion(xmlTmp.getVersion());

    xml.parse(new FileInputStream(m_path + xml.getFileName()));

    fd.putXMLForm(xml);

    return fd;

  }



  private FormData selectFormByID(int formID) throws Exception {

    FormData fd = null;

    DBService db = null;

    ResultSet rs=null;

    try {
      db = getDBService(DS);

      rs = db.select("SELECT form_id, title, type, xml, xsl, created, modified, user_id, user_name FROM upc_form_form WHERE form_id = "+ SQL.esc(formID));

      if(rs.next())

        fd = FormData.parse(rs);

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      if (db != null) {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }

    return fd;

  }



  public boolean isClosedSurvey(int surveyID) throws Exception {

    DBService db = null;
    ResultSet rs = null;

    try {

      db = getDBService(DS);
      rs = db.select("SELECT count(*) AS count_survey_survey FROM upc_survey_survey WHERE CLOSED IS NULL AND survey_id = "+ SQL.esc(surveyID));

      if (rs.next()) {
          int count = rs.getInt("count_survey_survey");
          if(count > 0) { return false; }
      }
      
    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      if (db != null) {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }

    return true;

  }



  public SurveyData getSurvey(int surveyID) throws Exception {

    SurveyData sd = null;
    boolean found = false;

    if(m_sdr != null && m_sdr.length > 0) {

      for(int i=0; i < m_sdr.length; i++){
 
        if(m_sdr[i].getSurveyId().intValue() == surveyID) {

          sd = m_sdr[i];
          
          found = true;

          break;

        }
      }

    } 
    if (!found){
    	sd = selectSurveyByID(surveyID);
    	
    	// add the new survey to the m_sdr
    	ArrayList surveys = new ArrayList(Arrays.asList(m_sdr));
    	surveys.add(sd);
    	m_sdr = (SurveyData[])(surveys.toArray(new SurveyData[0]));
    }
    
    return sd;

  }



  private SurveyData selectSurveyByID(int surveyID) throws Exception {

    DBService db = null;

    ResultSet rs = null;

    SurveyData sd = null;

    try {
      db = getDBService(DS);

      rs = db.select("select ss.*, (select count(sd.SURVEY_ID) from UPC_SURVEY_DATA sd where sd.SURVEY_ID=ss.SURVEY_ID) as NO from UPC_SURVEY_SURVEY ss where ss.SURVEY_ID = "+ SQL.esc(surveyID));

      if(rs.next()) {

        sd = SurveyData.parse(rs);

        sd.putReplied(new Integer(rs.getInt("NO")));

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      if (db != null) {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }

    return sd;

  }



  public FormDataData getResponse(int surveyID, IdentityData identity) throws Exception {

    FormDataData fdd = new FormDataData();

    DBService db = null;

    ResultSet rs = null;

    try {

      db = getDBService(DS);
      rs = db.select("select fd.* from UPC_FORM_DATA fd, UPC_SURVEY_DATA sd where fd.DATA_ID=sd.DATA_ID and fd.USER_ID="+ SQL.esc(identity.getIdentifier()) +" and sd.SURVEY_ID="+ SQL.esc(surveyID));

      if(rs.next()) {

        fdd = FormDataData.parse(rs);

        fdd.putResponse((DataDetailData[])getResponseDetail(fdd.getDataId().intValue()).toArray(new DataDetailData[0]));

      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      if (db != null) {
        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }

    return fdd;

  }



  public void createForm(FormData fd) throws Exception {

    DBService db = null;

    try {

    db = getDBService(DS);

    ResultSet rs = null;

    int id = 0;

    try {

      rs = db.select("select max(FORM_ID) as FORM_ID from UPC_FORM_FORM");

      if(rs.next())

        id = rs.getInt("FORM_ID");

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        db.closeStatement();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {

      db.begin();

      db.update("insert into UPC_FORM_FORM(FORM_ID, TITLE, TYPE, XML, XSL, CREATED, USER_ID, USER_NAME) values ("+ SQL.esc(id < 0 ? 1 : id+1) +","+ SQL.esc(fd.getTitle()) +","+ SQL.esc(fd.getType()) +","+ SQL.esc(fd.getXMLForm().getFileName().replace(File.separatorChar,'/')) +","+ SQL.esc(fd.getXSLForm().getFileName().replace(File.separatorChar,'/')) +","+ SQL.esc(new Timestamp(fd.getCreated().getTime())) +","+ SQL.esc(fd.getUserId()) +","+ SQL.esc(fd.getUserName()) +")");

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);
    } finally {
      db.closeStatement();
    }

    } finally {

      if (db != null) db.release();

    }

  }



  public void updateForm(FormData fd) throws Exception {

    DBService db = null;

    try {

      db = getDBService(DS);
      db.begin();

      db.update("update UPC_FORM_FORM set TITLE = "+ SQL.esc(fd.getTitle()) +", TYPE = "+ SQL.esc(fd.getType()) +", XML = "+ SQL.esc(fd.getXMLForm().getFileName().replace(File.separatorChar,'/')) +", XSL = "+ SQL.esc(fd.getXSLForm().getFileName().replace(File.separatorChar,'/')) +", MODIFIED = "+ SQL.esc(new Timestamp(fd.getModified().getTime())) +", USER_ID = "+ SQL.esc(fd.getUserId()) +", USER_NAME = "+ SQL.esc(fd.getUserName()) +" where FORM_ID = "+ SQL.esc(fd.getFormId().intValue()));

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

    } finally {
      if (db != null) {
        try {
          db.closeStatement();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.release();
      }
    }
  }



  public void createResponse(FormDataData fdd, int surveyId) throws Exception {

    DBService db = null;

    try {
    db = getDBService(DS);

    ResultSet rs0 = null, rs =null;

    DataDetailData[] fddr = null;

    String sql = null;

    int id = 0;

    boolean flag = false;

    if(fdd.getUserId() != null) {//Check existent response

      try {

        rs0 = db.select("select fd.DATA_ID as DATA_ID from UPC_FORM_DATA fd, UPC_SURVEY_DATA sd where fd.DATA_ID = sd.DATA_ID and fd.USER_ID = " + SQL.esc(fdd.getUserId()) + " and sd.SURVEY_ID = " + SQL.esc(surveyId));

        if(rs0.next()) {

          id = rs0.getInt("DATA_ID");

          flag = true;

        }

      } catch(Exception e) {

        throw new Exception(ERROR_RDBM);

      } finally {

        try {
          if (rs0 != null) rs0.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.closeStatement();

      }

    }

    if(!flag) {//Not exist response of User

      try {

        rs = db.select("select max(DATA_ID) as DATA_ID from UPC_FORM_DATA");

        if(rs.next())

          id = rs.getInt("DATA_ID");

      } catch(Exception e) {

        throw new Exception(ERROR_RDBM);

      } finally {

        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.closeStatement();

      }

      id = id < 0 ? 1 : id+1;

    }

    try {

      db.begin();

      if(!flag) {//Not exist response of User

        if(fdd.getUserId() == null)//Type response is Anonymous or Named

          db.update("insert into UPC_FORM_DATA(DATA_ID, FORM_ID, CREATED) values ("+ SQL.esc(id) +","+ SQL.esc(fdd.getFormId().intValue()) +","+ SQL.esc(new Timestamp(fdd.getCreated().getTime())) +")");

        else

          db.update("insert into UPC_FORM_DATA(DATA_ID, FORM_ID, CREATED, USER_ID, USER_NAME) values ("+ SQL.esc(id) +","+ SQL.esc(fdd.getFormId().intValue()) +","+ SQL.esc(new Timestamp(fdd.getCreated().getTime())) +","+ SQL.esc(fdd.getUserId()) +","+ SQL.esc(fdd.getUserName()) +")");

        db.update("insert into UPC_SURVEY_DATA(SURVEY_ID, DATA_ID) values ("+ SQL.esc(surveyId) +","+ SQL.esc(id) +")");

      } else {

        db.update("update UPC_FORM_DATA set MODIFIED="+ SQL.esc(new Timestamp(fdd.getCreated().getTime())) +" where DATA_ID="+ SQL.esc(id));

        db.update("delete from UPC_FORM_DATA_DETAIL where DATA_ID="+ SQL.esc(id));

      }

      fddr = fdd.getResponse();

      for(int i=0; i<fddr.length; i++)

        db.update("insert into UPC_FORM_DATA_DETAIL(DATA_ID, PAGE_ID, QUESTION_ID, INPUT_ID, DATA_CHOICES, DATA_TEXT) values ("+ SQL.esc(id) +","+ SQL.esc(fddr[i].getPageId().intValue()) +","+ SQL.esc(fddr[i].getQuestionId().intValue()) +","+ SQL.esc(fddr[i].getInputId().intValue()) +","+ SQL.esc(fddr[i].getDataChoices()) +","+ SQL.esc(fddr[i].getDataText()) +")");

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

    } finally {
      db.closeStatement();
    }


    } finally {

      db.release();

    }

  }



  public int createSurvey(SurveyData sd) throws Exception {

    DBService db = null;
    int id = 0;

    try {
    db = getDBService(DS);

    ResultSet rs = null;

    try {

      rs = db.select("select max(SURVEY_ID) as SURVEY_ID from UPC_SURVEY_SURVEY");

      if(rs.next())

        id = rs.getInt("SURVEY_ID");

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      db.closeStatement();

    }

    id = id < 0 ? 1 : id+1;

    try {

      db.begin();

      db.update("insert into UPC_SURVEY_SURVEY(SURVEY_ID, FORM_ID, TYPE, SENT, USER_ID, USER_NAME, DISTRIBUTION_TITLE) values ("+ SQL.esc(id) +","+ SQL.esc(sd.getFormId().intValue()) +","+ SQL.esc(sd.getType()) +","+ SQL.esc(new Timestamp(sd.getSent().getTime())) +","+ SQL.esc(sd.getUserId()) +","+ SQL.esc(sd.getUserName()) +","+ SQL.esc(sd.getDistributionTitle()) +")");

      if(!sd.getType().equals("Poll")) {

        IdentityData[] idr = sd.getTarget();

        for(int i=0; i<idr.length; i++)

          db.update("insert into UPC_SURVEY_TARGET(SURVEY_ID, OBJECT) values ("+ SQL.esc(id) +","+ SQL.esc(idr[i].getIdentifier()) +")");

      }

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

      return -1;

    } finally {
      db.closeStatement();
    }


    } finally {

      db.release();

    }

    return id;

  }



  public void updateSurvey(int id) throws Exception {

    DBService db = null;


    try {
      db = getDBService(DS);

      Calendar date = Calendar.getInstance();

      date.setTimeZone(TimeZone.getTimeZone("GMT+0"));

      db.begin();

      db.update("update UPC_SURVEY_SURVEY set CLOSED = "+ SQL.esc(new Timestamp(date.getTime().getTime())) +" where SURVEY_ID = "+ SQL.esc(id));

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

    } finally {

      if (db != null) db.release();

    }

  }



  public void deleteSurvey(int surveyID) throws Exception {

    DBService db = null;

    ResultSet rs = null;

    String dataIDString = "(";

    try {
      db = getDBService(DS);

      try {

        rs = db.select("SELECT survey_id, data_id FROM upc_survey_data WHERE survey_id = "+ SQL.esc(surveyID));

        while(rs.next())

          dataIDString += String.valueOf(rs.getInt("DATA_ID")) +",";

        dataIDString = dataIDString.substring(0,dataIDString.length()-1) + ")";

        dataIDString = (dataIDString.length()<3)?"(null)":dataIDString;

      } catch(Exception e) {

        throw new Exception(ERROR_RDBM);

      } finally {

        try {
          if (rs != null) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        db.closeStatement();

      }

      db.begin();

      db.update("delete from UPC_FORM_DATA_DETAIL where DATA_ID in "+ dataIDString);

      db.update("delete from UPC_SURVEY_DATA where SURVEY_ID = "+ SQL.esc(surveyID));

      db.update("delete from UPC_FORM_DATA where DATA_ID in "+ dataIDString);

      db.update("delete from UPC_SURVEY_TARGET where SURVEY_ID = "+ SQL.esc(surveyID));

      db.update("delete from UPC_SURVEY_SURVEY where SURVEY_ID = "+ SQL.esc(surveyID));

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

    } finally {

      if (db != null) db.release();

    }

  }



  public void deleteForm(int formID) throws Exception {

    DBService db = null;

    try {

      db = getDBService(DS);
      db.begin();

      db.update("delete from UPC_FORM_DATA_DETAIL where DATA_ID in (select DATA_ID from UPC_FORM_DATA where FORM_ID ="+ SQL.esc(formID) +")");

      db.update("delete from UPC_SURVEY_TARGET where SURVEY_ID in (select SURVEY_ID from UPC_SURVEY_SURVEY where FORM_ID ="+ SQL.esc(formID) +")");

      db.update("delete from UPC_SURVEY_DATA where SURVEY_ID in (select SURVEY_ID from UPC_SURVEY_SURVEY where FORM_ID ="+ SQL.esc(formID) +")");

      db.update("delete from UPC_FORM_DATA where FORM_ID ="+ SQL.esc(formID));

      db.update("delete from UPC_SURVEY_SURVEY where FORM_ID ="+ SQL.esc(formID));

      db.update("delete from UPC_FORM_FORM where FORM_ID ="+ SQL.esc(formID));

      db.commit();

    } catch(Exception e) {

      db.rollback(ERROR_RDBM);

    } finally {

      if (db != null) db.release();

    }

  }



  public boolean existResponse(IdentityData user, int surveyID) throws Exception {

    DBService db = null;
    ResultSet rs = null;

    try {
      db = getDBService(DS);

      rs = db.select("select UPC_FORM_DATA.DATA_ID as DATA_ID from UPC_FORM_DATA, UPC_SURVEY_DATA where UPC_FORM_DATA.DATA_ID = UPC_SURVEY_DATA.DATA_ID and UPC_FORM_DATA.USER_ID = "+ SQL.esc(user.getIdentifier()) +" and UPC_SURVEY_DATA.SURVEY_ID = "+ SQL.esc(surveyID));

      if(rs.next())

        return true;

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }

    return false;

  }



  public Vector getAllResponse(int surveyID) throws Exception {

    Vector v = new Vector();

    DBService db = null;

    ResultSet rs = null;

    try {
      db = getDBService(DS);

      rs = db.select("select fd.* from UPC_FORM_DATA fd, UPC_SURVEY_DATA sd where fd.DATA_ID = sd.DATA_ID and sd.SURVEY_ID = "+ SQL.esc(surveyID));

      while(rs.next())

        v.addElement(FormDataData.parse(rs));

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }

    return v;

  }

    private List getDataDetails(int surveyID) throws Exception {

        // SQL
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT fdd.data_id, fdd.page_id, fdd.question_id, ")
                    .append("fdd.input_id, fdd.data_text, fdd.data_date, ")
                    .append("fdd.data_number, fdd.data_choices ")
                    .append("FROM upc_survey_data sd, upc_form_data_detail fdd ")
                    .append("WHERE sd.data_id = fdd.data_id and sd.survey_id = ?");

        // Database objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Obtain a connection.
        try {
            conn = AcademusDBUtil.getDBConnection();
        } catch (Exception e) {
            String msg = "SurveyBo was unable to obtain a Connection";
            throw new Exception(msg, e);
        }

        // Lookup the details.
        List rslt = new ArrayList();
        try {

            // Access the record.
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, surveyID);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                rslt.addAll(DataDetailData.parseToVector(rs));
            }

        } catch (SQLException sqle) {
            String msg = "Error reading the database while attepting "
                                    + "to lookup survey details.";
            throw new Exception(msg, sqle);
        } finally {

            List msgs = new ArrayList();

            // Close the ResultSet
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                msgs.add("SurveyBo was unable to close a ResultSet");
            }

            // Close the PreparedStatement
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException sqle) {
                msgs.add("SurveyBo was unable to close a PreparedStatement");
            }

            // Release the connection.
            try {
                AcademusDBUtil.releaseDBConnection(conn);
            } catch (Exception e) {
                msgs.add("SurveyBo failed to release its Connection");
            }

            // Throw an exception if there were problems.
            if (msgs.size() > 0) {
                StringBuffer msg = new StringBuffer();
                msg.append("SurveyBo encountered the following ")
                        .append("problem(s) while cleaning up ")
                        .append("database resources:");
                Iterator it = msgs.iterator();
                while (it.hasNext()) {
                    msg.append("\n\t");
                    msg.append(it.next().toString());
                }
                throw new Exception(msg.toString());
            }

        }

        return rslt;

    }

/*
  private Vector getDataIDfromFormData(int surveyID) throws Exception {

    Vector v = new Vector();

    DBService db = null;

    ResultSet rs = null;

    try {
      db = getDBService(DS);

      rs = db.select("select fd.DATA_ID from UPC_FORM_DATA fd, UPC_SURVEY_DATA sd where fd.DATA_ID = sd.DATA_ID and sd.SURVEY_ID = "+ SQL.esc(surveyID));

      while(rs.next())

        v.addElement(new Integer(rs.getInt("DATA_ID")));

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {

      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();

    }

    return v;

  }
*/

  public Vector getResponseDetail(int dataID) throws Exception {

    Vector v = new Vector();

    ResultSet rs = null;

    DBService db = null;

    try {
      db = getDBService(DS);

      rs = db.select("SELECT data_id, page_id, question_id, input_id, data_text, data_date, data_number, data_choices FROM upc_form_data_detail WHERE data_id = "+ SQL.esc(dataID));

      while(rs.next()) {
        v.addAll(DataDetailData.parseToVector(rs));
      }

    } catch(Exception e) {

      throw new Exception(ERROR_RDBM);

    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (db != null) db.release();
    }

    return v;

  }


    private List evaluateSummary(List dataDetails) {

        // Assertions.
        if (dataDetails == null) {
            String msg = "Argument 'dataDetails' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        List rslt = new ArrayList();

        Iterator it = dataDetails.iterator();
        while (it.hasNext()) {

            DataDetailData ddd = (DataDetailData) it.next();
            if (ddd.getDataChoiceId() == null) {
                continue;
            }

            DataDetailData target = null;
            for (int i=0; i < rslt.size(); i++) {

                DataDetailData compare = (DataDetailData) rslt.get(i);
                if (compare.getDataChoiceId() == null) {
                    continue;
                }

                if (compare.getDataChoiceId().equals(ddd.getDataChoiceId())
                                && compare.getPageId().equals(ddd.getPageId())
                                && compare.getQuestionId().equals(ddd.getQuestionId())
                                && compare.getInputId().equals(ddd.getInputId())) {
                    target = compare;
                    break;
                }

            }

            if (target == null) {
                ddd.putNumResp(new Integer(1));
                rslt.add(ddd);
            } else {
                target.putNumResp(new Integer(target.getNumResp().intValue() + 1));
            }

        }

        return rslt;

    }

/*
  private Vector getSummary(Vector dddOldv, Vector dddNewv) throws Exception {

    for(int i=0; i<dddNewv.size(); i++) {

      DataDetailData dddNew = (DataDetailData)dddNewv.elementAt(i);

      if(dddNew.getDataChoiceId() != null) {

        dddNew.putNumResp(new Integer(1));

        for(int j=0; j<dddOldv.size(); j++) {

          DataDetailData dddOld = (DataDetailData)dddOldv.elementAt(j);

          if(dddOld.getDataChoiceId() != null)

            if(dddOld.getDataChoiceId().equals(dddNew.getDataChoiceId()) && dddOld.getPageId().equals(dddNew.getPageId()) && dddOld.getQuestionId().equals(dddNew.getQuestionId()) && dddOld.getInputId().equals(dddNew.getInputId())) {

              dddNew.putNumResp(new Integer(dddOld.getNumResp().intValue()+1));

              dddOldv.removeElementAt(j--);

            }

        }

      }

    }

    dddNewv.addAll(dddOldv);

    return dddNewv;

  }
*/


  private int getTotalResponse(int surveyID) throws Exception {

    int totalResponse = 0;
    SurveyData sd = selectSurveyByID(surveyID);
    if (sd != null) {
      totalResponse = sd.getReplied().intValue();
    }

    return totalResponse;

  }



  public Vector getExport(int surveyID) throws Exception {
/*
    Vector dddv = new Vector();

    Vector fddIDv = getDataIDfromFormData(surveyID);

    Vector dddtmpv = null;

    for(int i=0; i<fddIDv.size(); i++) {

      dddtmpv = getResponseDetail(((Integer)fddIDv.elementAt(i)).intValue());

      dddv = getSummary(dddv, dddtmpv);

    }
*/
    List dddv = evaluateSummary(getDataDetails(surveyID));

    int totalResponse = getTotalResponse(surveyID);

    for(int i=0; i<dddv.size(); i++) {

      DataDetailData ddd = (DataDetailData) dddv.get(i);

      if(ddd.getDataChoiceId() != null)

        ddd.putPercent(NumberFormat.getPercentInstance().format(new Float(ddd.getNumResp().floatValue()/totalResponse)));

    }

    return new Vector(dddv);

  }



  public SummaryData summarize(int surveyID) throws Exception {

    SummaryData smd = new SummaryData();
/*
    Vector dddv = new Vector();

    Vector fddIDv = getDataIDfromFormData(surveyID);

    Vector dddtmpv = null;

    for(int i=0; i<fddIDv.size(); i++) {

      dddtmpv = getResponseDetail(((Integer)fddIDv.elementAt(i)).intValue());

      dddv = getSummary(dddv, dddtmpv);

    }
*/
    List dddv = evaluateSummary(getDataDetails(surveyID));

    int totalResponse = getTotalResponse(surveyID);

    for(int i=0; i<dddv.size(); i++) {

      DataDetailData ddd = (DataDetailData) dddv.get(i);

      if(ddd.getDataChoiceId() != null)

        ddd.putPercent(NumberFormat.getPercentInstance().format(new Float(ddd.getNumResp().floatValue()/totalResponse)));

    }

    smd.putStatistics((DataDetailData[]) dddv.toArray(new DataDetailData[0]));

    return smd;

  }

}

