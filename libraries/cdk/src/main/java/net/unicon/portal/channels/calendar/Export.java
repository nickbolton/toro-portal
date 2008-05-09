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
import java.io.*;
import java.text.SimpleDateFormat;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.services.LogService;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.portal.channels.rad.Screen;


public class Export extends CalendarScreen implements IMimeResponse {

  public Export() {
    this.m_idempotentURL = true;
  }

  public String sid() {
    return "Export";
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
    params.put("date", curDate());
    return params;
  }

  public void init(Hashtable params) throws Exception {
    super.init(params);
    this.m_idempotentURL = true;
    m_data = new XMLData();
    m_data.putE("calendar", getAllCalendars());

  }

  public void reinit(Hashtable params) throws Exception {
    init(params);
    log("reinit");
  }

  public Map getHeaders() {
    Hashtable params = m_channel.getParameters();
    log ("Export params" + params);
    m_format = (String)params.get("format");
    HashMap attachmentHeaders = new HashMap();
    attachmentHeaders.put("Content-Disposition", "attachment; filename=\""+ (m_format.equals("ical") ? "Event.vcs" : m_format +".txt") +"\"");

    return attachmentHeaders;
  }

  String m_format = "";
  //String m_formatFile = "";

  public InputStream getInputStream() throws IOException {
    Hashtable params = m_channel.getParameters();
    log ("Export params" + params);
    m_format = (String)params.get("format");
    //m_formatFile = (String)params.get("formatfile");
    String calid = (String)params.get("calid");
    String[] calids = null;
    if (calid != null && !calid.startsWith("composite")) {
      calids = new String[]{calid};
    } else if (calid != null && calid.startsWith("composite")) {
      String s = m_channel.getUserData(CCalendar.USERDATA_KEY);
      if (s != null) {
        XMLData data = new XMLData();
        try {
          data.parse( new ByteArrayInputStream(s.getBytes()));
        } catch (Exception e) {}
        //get group data
        // Get id of composite
        Object[] gcr = data.rgetE("calendar-group");
        XMLData caldata = null;
        if (gcr != null && gcr.length > 0) {
          Vector v = new Vector();
          for (int i = 0; i < gcr.length; i++) {
            String id = (String) ( (XMLData) gcr[i]).getA("id");
            log("composite. id ********** *******" + id + " " +
                calid.substring(10));
            if (calid.substring(10).equals(id))
              caldata = (XMLData) gcr[i];
          }
        }


        //get cal data
        //Object o = data.getE("calendar");
        Object o = caldata != null ? caldata.getE("calendar") : null;
        if (o!= null && o instanceof Object[]) {
          Object[] cr = (Object[])o;
          if (cr != null) {
            calids = new  String[cr.length];
            for (int i = 0; i < cr.length; i++) {
              XMLData xml = new XMLData();
              xml =  (XMLData)cr[i];
              String calid1 = (String)xml.getA("calid");
              log("calid ********** *******" + calid);
              calids[i] = calid1;
            }
          }
        }
      }
    }

    /*
    if (calid != null && !calid.equals("composite-cal")){
      calids = new String[]{calid};
    }
    else if (calid != null && calid.equals("composite-cal")){
      String s = m_channel.getUserData(CCalendar.USERDATA_KEY);
      if (s != null){
        XMLData data = new XMLData();
        try{
        data.parse( new ByteArrayInputStream(s.getBytes()));
        }
        catch (Exception e){}
        Object o = data.getE("calendar");
        if (o instanceof Object[]){
          Object[] cr = (Object[])o;
          if (cr != null){
            calids = new  String[cr.length];
            for (int i = 0; i < cr.length; i++){
              XMLData xml = new XMLData();
              xml =  (XMLData)cr[i];
              String calid1 = (String)xml.getA("calid");
              log("calid ********** *******" + calid);
              calids[i] = calid1;
            }
          }
        }
      }
    }
    */

    String mo =  (String)params.get("month");
    String day =  (String)params.get("day");
    String year =  (String)params.get("year");
    String endmo =  (String)params.get("endmonth");
    String endday =  (String)params.get("endday");
    String endyear =  (String)params.get("endyear");
    Date to = new Date();
    Date from = new Date( to.getTime() - 1*24*60*60*1000);
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm");
    try {
      from = df.parse(mo+"/"+day+"/"+year + " 00:00");
    } catch (Exception e) {}
    try {
      to = df.parse(endmo+"/"+endday+"/"+endyear + " 23:59");
    } catch (Exception e) {}
    log ("from + to " + from + " " + to);
    try {
      if (!m_format.equals("ical"))
        return getServer().exportEntries(m_format, calids , from , to);
      else if (!calid.equals("composite-cal")) {
        CalendarData[] cals = getAllCalendars();
        cals = new CalendarData[]{CalendarData.findCalendar(cals, calid)};
        cals = getServer().fetchEvents(cals, from, to);
        if (cals == null)
          return null;
        else {
          ICal ical = new ICal();
          return ical.init(cals[0]);
        }
      } else
        return null;
    } catch( Exception e) {
      e.printStackTrace(System.err);
      throw new IOException(e.getMessage());
    }
  }

  public Screen ok(Hashtable params) throws Exception {
    return getScreen("Main");
  }

  /**
   * Let the channel know that there were problems with the download
   * @param e
   */
  public void reportDownloadError(Exception e) {
    LogService.log(LogService.ERROR, "Export::reportDownloadError(): " + e.getMessage());
  }
}
