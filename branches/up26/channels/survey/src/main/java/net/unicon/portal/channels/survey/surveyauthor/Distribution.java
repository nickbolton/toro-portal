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

import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.academus.apps.survey.SurveyData;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;

public class Distribution extends SurveyAuthorScreen {

  SurveyData m_sd;

  Vector m_target;

  String m_title, m_type, m_mail, m_noti, m_anonymous, m_distributionTitle;
  

  public void init(Hashtable params) throws Exception {

    super.init(params);

    m_target = new Vector();

    m_mail = null;

    m_type = "Poll";

    m_noti = "Noti";

    m_anonymous = "Anonymous";

    m_title = (String)params.get("Title");
    
    m_distributionTitle = (String)params.get("SDTitle");
    
    m_sd = new SurveyData();

    m_sd.putFormId(new Integer((String)params.get("FormId")));

  }



  public void reinit(Hashtable params) throws Exception {

    if ((String)params.get("Init") != null && ((String)params.get("Init")).equals("Yes"))

      init(params);

  }



  public Hashtable getXSLTParams() {

    Hashtable params = new Hashtable();

    params.put("Title", m_title);
    
    if (m_distributionTitle != null)
	{
    params.put("SDTitle", m_distributionTitle);
	}
    params.put("Type", m_type);

    params.put("Anonymous",m_anonymous);

    if(m_mail != null)

      params.put("Mail", m_mail);

    if(m_noti != null)

      params.put("Noti", m_noti);

    return params;

  }



  public XMLData getData() throws Exception {

    m_data.putE("ViewTarget", m_target.toArray());

    return m_data;

  }



  public Screen Cancel(Hashtable params) throws Exception {

    Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");

    scr.reinit(params);

    return scr;

  }



  public Screen deleteRecipient(Hashtable params) throws Exception {

    m_type = (String)params.get("Type");

    m_anonymous = (String)params.get("Anonymous");

    m_noti = (String)params.get("Noti");

    m_mail = (String)params.get("Mail");
    
    m_distributionTitle = (String)params.get("SDTitle");

    IdentityData id = new IdentityData((String)params.get("itype"),(String)params.get("ientity"),(String)params.get("iid"),(String)params.get("iname"));

    for(int i=0; i<m_target.size(); i++)

      if(id.equals(((IdentityData)m_target.elementAt(i))))

        m_target.removeElementAt(i);

    return this;

  }



  public Screen addSelected(Hashtable params) throws Exception {

    int i,j;

    boolean flagMail = false;

    IdentityData[] sels = (IdentityData[])params.get("selected");

    for(i=0; i<sels.length; i++) {

      if(!sels[i].getType().equals(IdentityData.GROUP) && sels[i].getEmail() == null)

        flagMail = true;

      for(j=0; j<m_target.size(); j++)

        if(sels[i].equals(((IdentityData)m_target.elementAt(j))))

          break;

      if(j == m_target.size())

        m_target.addElement(sels[i]);

    }

    if(flagMail)

      return warningMulti(new String[]{CSurveyAuthor.NONE_EMAIL_SELECTION}, sid(), false);

    return this;

  }



  public Screen addTarget(Hashtable params) throws Exception {

    m_type = (String)params.get("Type");

    m_anonymous = (String)params.get("Anonymous");

    m_noti = (String)params.get("Noti");

    m_mail = (String)params.get("Mail");
    
    m_distributionTitle = (String)params.get("SDTitle");

    return select(m_channel.getRADProperty("survey.sources"), (IdentityData[])m_target.toArray(new IdentityData[0]), "addSelected", params);

  }



  public Screen OK(Hashtable params) throws Exception {

    int surveyId;
    
    m_type = (String)params.get("Type");

    m_noti = (String)params.get("Noti");

    m_mail = (String)params.get("Mail");
    
    m_distributionTitle  = (String)params.get("SDTitle");
  

    if(m_type.equals("Poll"))

      m_sd.putType("Poll");

    else {

      m_sd.putType((String)params.get("Anonymous"));

      if(m_target.size() == 0)

        return error(CSurveyAuthor.ERROR_NOT_HAVE_RECIPIENT);

      m_sd.putTarget((IdentityData[])m_target.toArray(new IdentityData[0]));

    }



    Calendar date = Calendar.getInstance();

    date.setTimeZone(TimeZone.getTimeZone("GMT+0"));

    m_sd.putSent(date.getTime());

    m_sd.putUserId(m_user.getIdentifier());

    m_sd.putUserName(m_user.getName());
    
    m_sd.putDistributionTitle(m_distributionTitle);

    if((surveyId = getBo().createSurvey(m_sd))==-1)

      return error(CSurveyAuthor.ERROR_DISTRIBUTION_FAIL);

    // Begin: Send to Notification

    if (m_target.size() != 0 && (m_noti != null || m_mail != null)) {
        
      int sendFlag = 0;

      if (m_noti != null)
          sendFlag |= NotificationService.TYPE_NOTIFICATION;
      if (m_mail != null)
          sendFlag |= NotificationService.TYPE_EMAIL;

      NotificationService notificationService =
                NotificationServiceFactory.getService();

      String notifyClass = "net.unicon.portal.channels.survey.survey.CSurvey";
      String notifyParams = "Yes_" + m_sd.getFormId() + "_" + surveyId;
      String shortmsg = m_distributionTitle.trim().equals("") ? m_title : m_distributionTitle;
      String subject = "Survey: " + shortmsg;

      String body =
            new StringBuffer("You have been selected to participate in the \"")
                .append(shortmsg)
                .append("\" Survey. Navigate to the Survey Channel to participate in this survey.\n")
                .toString();

      IdentityData[] targets = m_sd.getTarget();
      // Expand targets in case of groups
      //targets = GroupData.expandGroups(targets, false);
      
      
      try {
          notificationService.sendNotifications(
                  targets, m_user, subject, body, shortmsg,
                  notifyClass, notifyParams, sendFlag);
      } catch(Exception ex) {
          log("***Failed to send notification/email: " + ex.toString());
          ex.printStackTrace(System.err);
          return warningMulti(new String[]{CSurveyAuthor.NONE_EMAIL_SENDER}, "Peephole", true);
      }

    }

    // End: Send to Notification

    Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");

    scr.reinit(params);

    return scr;

  }

}

