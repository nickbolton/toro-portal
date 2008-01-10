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

public class SurveyBoSkel extends net.unicon.academus.apps.rad.BaseSkel implements SurveyBoHome, SurveyBoRemote {

  private SurveyBo m_bo = null;



  public void remove

    () {

    if (m_bo != null) {

      m_bo.ejbRemove();

      m_bo = null;

    }

  }



  public SurveyBoRemote create() throws java.rmi.RemoteException, javax.ejb.CreateException {

    SurveyBoSkel remote = new SurveyBoSkel();

    remote.m_bo = new SurveyBo();

    populateProperty(remote.m_bo);

    remote.m_bo.ejbCreate();

    return remote;

  }



  public java.lang.Object getProperty(java.lang.String arg0) throws java.rmi.RemoteException {

    return m_bo.getProperty(arg0);

  }



  public void putProperty(java.lang.String arg0, java.lang.Object arg1) throws java.rmi.RemoteException {

    m_bo.putProperty(arg0, arg1);

  }



  public void ejbActivate() throws java.rmi.RemoteException {

    m_bo.ejbActivate();

  }



  public void ejbPassivate() throws java.rmi.RemoteException {

    m_bo.ejbPassivate();

  }



  public void setSessionContext(javax.ejb.SessionContext arg0) throws java.rmi.RemoteException {

    m_bo.setSessionContext(arg0);

  }



  public void setFilePath(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.setFilePath(arg0);

  }



  public net.unicon.academus.apps.form.FormData[] listFormSurveyforAuthor(boolean arg0, net.unicon.academus.apps.rad.IdentityData arg1) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.listFormSurveyforAuthor(arg0, arg1);

  }

  public net.unicon.academus.apps.form.FormData[] listFormSurveyforAuthor(boolean arg0, net.unicon.academus.apps.rad.IdentityData arg1, boolean arg2) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.listFormSurveyforAuthor(arg0, arg1, arg2);

  }



  public net.unicon.academus.apps.form.FormData[] listFormSurveyforPoll(boolean arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.listFormSurveyforPoll(arg0);

  }



  public net.unicon.academus.apps.form.FormData[] listFormSurveyforSurvey(net.unicon.academus.apps.rad.IdentityData arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.listFormSurveyforSurvey(arg0, arg1);

  }



  public net.unicon.academus.apps.form.FormDataData getResponse(int arg0, net.unicon.academus.apps.rad.IdentityData arg1) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getResponse(arg0, arg1);

  }


  public net.unicon.academus.apps.form.FormData getFormBySurveyId(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

      return m_bo.getFormBySurveyId(arg0);

    }
  

  public net.unicon.academus.apps.form.FormData getForm(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getForm(arg0);

  }



  public boolean isClosedSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.isClosedSurvey(arg0);

  }



  public net.unicon.academus.apps.survey.SurveyData getSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getSurvey(arg0);

  }



  public net.unicon.academus.apps.survey.SummaryData summarize(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.summarize(arg0);

  }



  public void createForm(net.unicon.academus.apps.form.FormData arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.createForm(arg0);

  }



  public void updateForm(net.unicon.academus.apps.form.FormData arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.updateForm(arg0);

  }



  public void createResponse(net.unicon.academus.apps.form.FormDataData arg0, int arg1) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.createResponse(arg0,arg1);

  }



  public int createSurvey(net.unicon.academus.apps.survey.SurveyData arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.createSurvey(arg0);

  }



  public void updateSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.updateSurvey(arg0);

  }



  public void deleteForm(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.deleteForm(arg0);

  }



  public void deleteSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    m_bo.deleteSurvey(arg0);

  }



  public boolean existResponse(net.unicon.academus.apps.rad.IdentityData user, int surveyId) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.existResponse(user, surveyId);

  }



  public java.util.Vector getAllResponse(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getAllResponse(arg0);

  }




  public java.util.Vector getResponseDetail(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getResponseDetail(arg0);

  }



  public java.util.Vector getExport(int arg0) throws java.lang.Exception, java.rmi.RemoteException {

    return m_bo.getExport(arg0);

  }

}

