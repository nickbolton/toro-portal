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

package net.unicon.portal.channels.calendar;

import java.util.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.portal.channels.rad.Screen;

import java.io.*;

public class Preference extends CalendarScreen {

    private String m_prefType = null;
    private String m_prefSaved = null;
    
  public String sid() {
    return "Preference";
  }

  XMLData m_data = new XMLData();

  public XMLData getData() throws Exception {
    return m_data;
  }

  /**
   * Get parameter of XSL
   * @return
   */
  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("cur-date", curDate());
    return params;
  }

  public void init(Hashtable params) throws Exception {
    super.init(params);
    //log("Preference.init");
    m_data = new XMLData();
    m_data.putE("calendar", getAllCalendars());
    getConfig();
  }

  public void reinit(Hashtable params) throws Exception {
    init(params);
    //log("Preference.reinit");
  }

  public Screen addGroup(Hashtable params) throws Exception {
    GroupEdit ge = (GroupEdit)makeScreen("GroupEdit");

    return ge;
  }

  public Screen editGroup(Hashtable params) throws Exception {
    String id = (String)params.get("id");
    String name = (String)params.get("name");
    GroupEdit ge = (GroupEdit)makeScreen("GroupEdit");
    ge.set(id, name);

    return ge;
  }

  public Screen deleteGroup(Hashtable params) throws Exception {
    //System.out.println("params " + params);
    String id = (String)params.get("id");
    XMLData udata = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (udata != null) {
      Object o = udata.getE("calendar-group");
      if (o instanceof Object[]) {
        Object[] os = (Object[])o;
        XMLData[] newdata = new XMLData[os.length-1];
        int j = 0;
        for (int i = 0; i < os.length; i++) {
          XMLData gr = (XMLData)os[i];
          if (!gr.getA("id").equals(id)) {
            newdata[j] = gr;
            j++;
          }
        }
        udata.putE("calendar-group", newdata);
      } else
        udata.removeE("calendar-group");
    }
    m_channel.setUserProfile(udata, CCalendar.USERDATA_KEY);

    return this;
  }

  public Screen save(Hashtable params) throws Exception {
    log("save " + params);
    String id = (String)params.get("id");
    Vector v = new Vector();
    for(Enumeration e = params.keys(); e.hasMoreElements();) {
      String key = (String)e.nextElement();
      if (key.startsWith("calid_") && key.indexOf("hidden_") == -1) {
        XMLData xml = new XMLData();
        xml.putA("calid", key.substring(key.indexOf("_")+1));
        //log(key.substring(key.indexOf("_")+1));
        v.addElement(xml);
      }
    }
    XMLData[] datar = (XMLData[])v.toArray(new XMLData[]{new XMLData()});
    XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (data == null)
      data = new XMLData();
    if (data != null) {
      Object o = data.getE("calendar-group");
      if (o instanceof Object[]) {
        Object[] os = (Object[])o;
        for (int i = 0; i < os.length; i++) {
          XMLData gr = (XMLData)os[i];
          if (gr.getA("id").equals(id)) {
            gr.putE("calendar", datar);
          }
        }
      } else
        ((XMLData)o).putE("calendar", datar);
    }
    
    //data.putE("calendar", datar);
    
    m_prefType = "composite";
    if(m_channel.setUserProfile(data, CCalendar.USERDATA_KEY))
        m_prefSaved = "true";
    else 
        m_prefSaved = "false";
    
    getConfig();
    return this;
  }

  public Screen saveHidden(Hashtable params) throws Exception {
    log("saveHidden " + params);
    Vector v = new Vector();
    for(Enumeration e = params.keys(); e.hasMoreElements();) {
      String key = (String)e.nextElement();
      if (key.startsWith("calid_hidden_")) {
        XMLData xml = new XMLData();
        xml.putA("calid", key.substring(key.indexOf("hidden_")+7));
        log("calid hidden " + key.substring(key.indexOf("hidden_")+7));
        v.addElement(xml);
      }
    }
    XMLData[] datar = (XMLData[])v.toArray(new XMLData[]{new XMLData()});
    XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (data == null)
      data = new XMLData();
    data.putE("calendar-hidden", datar);
    
    m_prefType = "hidden";
    if(m_channel.setUserProfile(data, CCalendar.USERDATA_KEY))
        m_prefSaved = "true";
    else 
        m_prefSaved = "false";
    getConfig();
    return this;
  }

  /**
   * The buildXML overrides the method in CalendarScreen. This return well-formed xml-data.
   * @return String of xml-based data.
   * @throws Exception
   */
  public String buildXML() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("<?xml version=\"1.0\"?>");
    ps.println("<calendar-system>");
    XMLData setup = new XMLData();
    setup.putA("setup", CCalendar.SETUP);
    XMLData config = new XMLData();
    config.putE("config",setup);
    config.print(ps,1);
    XMLData udata = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    XMLData pref = new XMLData();
    if (udata != null) {      
      if(m_prefSaved != null) {
        udata.putA("type", m_prefType);
        udata.putA("saved", m_prefSaved);
        m_prefSaved = null;
      }
      pref.putE("preference", udata);      
    }
    pref.print(ps,1);
    XMLData data = getData();
    data.print(ps, 1);
    XMLData logonData = getServer().getLogonData();
    logonData.print(ps, 1);
    ps.println("</calendar-system>");
    return baos.toString();
  }
  
  void getConfig() throws Exception {
    //XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    //if (data != null) m_data.putE("preference", data);
  }

}







