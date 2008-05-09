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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Vector;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jasig.portal.MultipartDataSource;

import net.unicon.academus.apps.calendar.EntryData;
import net.unicon.academus.apps.calendar.ICal;
import net.unicon.academus.apps.calendar.ImExUtil;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.XML;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;


public class Import extends CalendarScreen {
  /* Override from Screen in rad. */
  static public final String SID = "Import";
  /* Contain data from file. */
  private Hashtable[] m_data = null;
  /* Map between fields. */
  private Hashtable m_map = null;
  /* Content of cbo */
  private String m_subject = null;
  private String m_startDate = null;
  private String m_startTime = null;
  private String m_endDate = null;
  private String m_endTime = null;
  private String m_allDay = null;
  private String m_organizer = null;
  private String m_attendees = null;
  private String m_categories = null;
  private String m_description = null;
  private String m_location = null;
  private String m_priority = null;
  private String m_dueDate = null;
  private String m_dateComplete = null;
  private String m_percentComplete = null;

  private boolean m_isOpen = false;
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String sid() {
    return SID;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  String m_format = "";

  public void init(Hashtable params) throws Exception {
    m_map = new Hashtable();
    m_xdata.putE("calendar", getAllCalendars());
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void reinit(Hashtable params) throws Exception {
    m_isOpen = false;
    m_map.clear();
    //clear currently m_data.
    if(m_data!=null&&m_data.length>0) {
      for(int i = 0; i < m_data.length; i++) {
        m_data[i].clear();
        m_data[i] = null;
      }
    }
    m_data = null;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("isOpen",(new Boolean(m_isOpen)).toString());
    params.put("format", m_format);
    return params;
  }

  XMLData m_xdata = new XMLData();
  public XMLData getData() {
    Vector v = new Vector();
    if(m_data!=null&&m_data.length>0) {
      Sorted s = new Sorted(new Comparator() {
                              public int compare(Object obj1 , Object obj2) {
                                if(obj1==null&&obj2==null)
                                  return 0;
                                else {
                                  if(obj1==null)
                                    return -1;
                                  if(obj2==null)
                                    return 1;
                                }
                                return ((String)obj1).compareToIgnoreCase((String)obj2);
                              }
                            }
                           );
      for(Enumeration en = m_data[0].keys();en.hasMoreElements();) {
        s.add((String)en.nextElement());
      }
      for(int i = 0 ; i < s.size() ; i++) {
        XMLData data = new XMLData();
        data.putA("name", XML.esc((String)s.elementAt(i)));
        v.addElement(data);
      }
      XMLData field = new XMLData();
      field.putE("field", (XMLData[])v.toArray(new XMLData[]{new XMLData()}));
      m_xdata.putE("fields", field);
    }

    return m_xdata;
  }

  /**
   * Load data from file. init data in combo box.
   */

  InputStream m_stream = null;
  //String m_fileFormat = null;

  public Screen go(Hashtable params) throws Exception {
    m_format = (String)params.get("format");
    //m_fileFormat = (String)params.get("fileformat");
    MultipartDataSource[] fileParts = (MultipartDataSource[])params.get("file");
    if(fileParts != null) {
      //clear currently m_data.
      if(m_data!=null&&m_data.length>0) {
        for(int i = 0; i < m_data.length; i++) {
          m_data[i].clear();
          m_data[i] = null;
        }
      }
      m_data = null;
      //init again
      try {
        if (!m_format.equals("ical")) {
          m_stream =  fileParts[0].getInputStream();
          m_data = ImExUtil.loadDataFromTabDelimitedFile(m_stream);
          m_stream =  fileParts[0].getInputStream();
        } else {
          EntryData[] ents = ICal.importVcal(fileParts[0].getInputStream());
          if (ents != null && ents.length != 0) {
            //System.out.println("ents.length "+ ents.length);
            for (int i = 0; i < ents.length; i++) {
              getServer().createEntry(getServer().getUser(), ents[i]);
            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace(System.err);
        return error(CCalendar.ERROR_INVALID_FILE);
      }
      m_isOpen = true;
    }

    return !m_format.equals("ical") ? this : getScreen("Main");
  }

  public Screen ok(Hashtable params) throws Exception {
    log("import params " + " m_format " + m_format + params);
    String calid = (String)params.get("calid");
    getContactOnForm(params);
    log("ok import 0");
    //init map
    m_map.clear();
    m_map.put(new Integer(ImExUtil.SUBJECT).toString(),m_subject);
    m_map.put(new Integer(ImExUtil.START_DATE).toString(),m_startDate);
    m_map.put(new Integer(ImExUtil.END_DATE).toString(),m_endDate);
    m_map.put(new Integer(ImExUtil.START_TIME).toString(),m_startTime);
    m_map.put(new Integer(ImExUtil.END_TIME).toString(),m_endTime);
    m_map.put(new Integer(ImExUtil.ALL_DAY).toString(),m_allDay);
    m_map.put(new Integer(ImExUtil.ORGANIZER).toString(),m_organizer);
    m_map.put(new Integer(ImExUtil.ATTENDEES).toString(),m_attendees);
    m_map.put(new Integer(ImExUtil.CATEGORIES).toString(),m_categories);
    m_map.put(new Integer(ImExUtil.DESCRIPTION).toString(),m_description);
    m_map.put(new Integer(ImExUtil.LOCATION).toString(),m_location);
    m_map.put(new Integer(ImExUtil.PRIORITY).toString(), m_priority);
    m_map.put(new Integer(ImExUtil.DUE_DATE).toString(),m_dueDate);
    m_map.put(new Integer(ImExUtil.DATE_COMPLETE).toString(),m_dateComplete);
    m_map.put(new Integer(ImExUtil.PERCENT_COMPLETE).toString(),m_percentComplete);
    log("ok import 1");
    String errorMsg = "";
    try {
      errorMsg = getServer().importEntries(m_stream, m_format, calid, m_map);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      return error(!errorMsg.equals("") ? errorMsg : e.getMessage());
    }
    log("ok import 2");

    return getScreen("Main");
  }
  /**
   * Check if wrong, edit to right.
   * @param ct the specified contact.
   * @return  true  data is good.
   *          false data is too long.
   */
  /*
  private boolean checkAndSetValue(EntryData ct){
    boolean ret = true;
    String tmp = ct.getName();
    //Name
    if (tmp == null || tmp.length() == 0)
      ct.putName("No Name");
    if (tmp.length() > ContactData.MAX_NAME_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_NAME_LENGTH));
      ret = false;
    }
    //Email
    tmp = ct.getEmail();
    if(tmp.length() > ContactData.MAX_EMAIL_LENGTH){
      ct.putEmail("");
      ret = false;
    }
    if (!CAddressBook.isValidEmail(tmp)){
      ct.putEmail("");
    }
    //Mobile
    tmp = ct.getCellPhone();
    if(tmp.length() > ContactData.MAX_MOBILE_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_MOBILE_LENGTH));
      ret = false;
    }
    //Title
    tmp = ct.getTitle();
    if(tmp.length() > ContactData.MAX_TITLE_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_TITLE_LENGTH));
      ret = false;
    }
    //Company
    tmp = ct.getCompany();
    if(tmp.length() > ContactData.MAX_COMPANY_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_COMPANY_LENGTH));
      ret = false;
    }
    //Department
    tmp = ct.getDepartment();
    if(tmp.length() > ContactData.MAX_DEPARTMENT_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_DEPARTMENT_LENGTH));
      ret = false;
    }
    //Business Phone
    tmp = ct.getBusinessPhone();
    if(tmp.length() > ContactData.MAX_BUSINESS_PHONE_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_BUSINESS_PHONE_LENGTH));
      ret = false;
    }
    //Fax
    tmp = ct.getFax();
    if(tmp.length() > ContactData.MAX_FAX_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_FAX_LENGTH));
      ret = false;
    }
    //Office Address
    tmp = ct.getOfficeAddress();
    if(tmp.length() > ContactData.MAX_OFFICE_ADDRESS_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_OFFICE_ADDRESS_LENGTH));
      ret = false;
    }
    //Home Phone
    tmp = ct.getHomePhone();
    if(tmp.length() > ContactData.MAX_HOME_PHONE_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_HOME_PHONE_LENGTH));
      ret = false;
    }
    //Home Address
    tmp = ct.getHomeAddress();
    if(tmp.length() > ContactData.MAX_HOME_ADDRESS_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_HOME_ADDRESS_LENGTH));
      ret = false;
    }
    //Notes
    tmp = ct.getNotes();
    if(tmp.length() > ContactData.MAX_NOTES_LENGTH){
      ct.putName(tmp.substring(0,ContactData.MAX_NOTES_LENGTH));
      ret = false;
    }
    return ret;
  }
  */

  /**
   * Cancel, go to "Main" Screen.
   */
  public Screen cancel(Hashtable params) throws Exception {
    return getScreen("Main");
  }
  /**
   * Get data from form.
   */
  private void getContactOnForm(Hashtable params) throws Exception {
    m_subject = params.get("subject")==null ? "" : ((String)params.get("subject")).trim();
    m_startDate = params.get("start-date")==null ? "" : ((String)params.get("start-date")).trim();
    m_startTime = params.get("start-time")==null ? "" : ((String)params.get("start-time")).trim();
    m_endDate = params.get("end-date")==null ? "" : ((String)params.get("end-date")).trim();
    m_endTime = params.get("end-time")==null ? "" : ((String)params.get("end-time")).trim();
    m_allDay = params.get("all-day")==null ? "" : ((String)params.get("all-day")).trim();
    m_organizer = params.get("organizer")==null ? "" : ((String)params.get("organizer")).trim();
    m_attendees = params.get("attendees")==null ? "" : ((String)params.get("attendees")).trim();
    m_categories = params.get("categories")==null ? "" : ((String)params.get("categories")).trim();
    m_description = params.get("description")==null?"":((String)params.get("description")).trim();
    m_location = params.get("location")==null?"":((String)params.get("location")).trim();
    m_priority = params.get("priority")==null? "" : ((String)params.get("priority")).trim();
    m_dueDate = params.get("due-date")==null? "" : ((String)params.get("due-date")).trim();
    m_dateComplete = params.get("date-complete")==null? "" : ((String)params.get("date-complete")).trim();
    m_percentComplete = params.get("percent-complete")==null? "" :((String)params.get("percent-complete")).trim();
  }
}
