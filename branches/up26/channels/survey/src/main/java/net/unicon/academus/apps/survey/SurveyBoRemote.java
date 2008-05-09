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

public interface SurveyBoRemote extends javax.ejb.EJBObject, java.io.Serializable {

  public java.lang.Object getProperty(java.lang.String arg0) throws java.rmi.RemoteException;

  public void putProperty(java.lang.String arg0, java.lang.Object arg1) throws java.rmi.RemoteException;

  public void ejbActivate() throws java.rmi.RemoteException;

  public void ejbPassivate() throws java.rmi.RemoteException;

  public void setSessionContext(javax.ejb.SessionContext arg0) throws java.rmi.RemoteException;

  public void setFilePath(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData[] listFormSurveyforSurvey(net.unicon.academus.apps.rad.IdentityData arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData[] listFormSurveyforAuthor(boolean arg0, net.unicon.academus.apps.rad.IdentityData arg1) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData[] listFormSurveyforAuthor(boolean arg0, net.unicon.academus.apps.rad.IdentityData arg1, boolean arg2) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData[] listFormSurveyforPoll(boolean arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData getForm(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormData getFormBySurveyId(int arg0) throws java.lang.Exception, java.rmi.RemoteException;
  
  public boolean isClosedSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.survey.SurveyData getSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.form.FormDataData getResponse(int arg0, net.unicon.academus.apps.rad.IdentityData arg1) throws java.lang.Exception, java.rmi.RemoteException;

  public net.unicon.academus.apps.survey.SummaryData summarize(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void createForm(net.unicon.academus.apps.form.FormData arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void updateForm(net.unicon.academus.apps.form.FormData arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void createResponse(net.unicon.academus.apps.form.FormDataData arg0, int arg1) throws java.lang.Exception, java.rmi.RemoteException;

  public int createSurvey(net.unicon.academus.apps.survey.SurveyData arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void updateSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void deleteForm(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public void deleteSurvey(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public boolean existResponse(net.unicon.academus.apps.rad.IdentityData user, int surveyId) throws java.lang.Exception, java.rmi.RemoteException;

  public java.util.Vector getAllResponse(int arg0) throws java.lang.Exception, java.rmi.RemoteException;


  public java.util.Vector getResponseDetail(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

  public java.util.Vector getExport(int arg0) throws java.lang.Exception, java.rmi.RemoteException;

}

