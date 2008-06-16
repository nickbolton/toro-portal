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

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;

import java.util.*;
import java.text.*;

/**
 * This class represents search screen
 */
public class Search extends CalendarScreen
{
  // Current search values
  XMLData m_input = new XMLData();
  String m_title = "";

  // Root data for xml
  XMLData m_data = new XMLData();
  XMLData m_view = new XMLData();

  // Search result is placed here
  CalendarData[] m_result = null;

  // For display pages
  EntryPos[] m_entries = null;
  int m_nPages = -1; // number of pages
  int m_curPage = -1;
  final int MAX_NUM = 10;
  class EntryPos
  {
    public int m_cal;
    public int m_entry;
    public EntryPos(int cal, int entry)
    {
      m_cal = cal;
      m_entry = entry;
    }
  }

  void reset() throws Exception
  {

    for (int i = 0; i < m_result.length; i++)
      m_result[i].putEntry(null);

    m_entries = null;
    ((XMLData)m_view.getE("calendar")).putE("entry",null);
    m_nPages = -1;
    setPage(-1);
  }

  /**
   * Get screen identifer of the screen
   * @return
   */
  static final String SID = "Search";

  public String sid(){
    return SID;
  }



  /**
   * Set page for results.
   * @param p
   * @throws Exception
   */
  public void setPage(int p) throws Exception
  {
    // Create empty page
    //truyen 6/6/02 CalendarData[] page = cloneCalProps(m_result);
    CalendarData[] page = CalendarData.cloneCalProps(m_result);
    m_data.putE("calendar", page);

    // Save current page number
    m_curPage = p;
    m_view.putA("prev",new Boolean(m_curPage > 0));
    m_view.putA("next",new Boolean(m_curPage < m_nPages - 1));
    m_view.putA("pages",new Integer(m_nPages));
    m_view.putA("page",new Integer(m_curPage));
    m_view.putE("page",null);
    if (m_curPage < 0 || m_curPage >= m_nPages || m_nPages < 0) return;

    // Start index of event and len of page
    int start = p * MAX_NUM;
    int len = (start + MAX_NUM > m_entries.length)? m_entries.length - start: MAX_NUM;

    // Extract entries to calendar page
    XMLData[] viewData = new XMLData[len];
    for (int i = 0; i < len; i++)
    {
      EntryPos pos = m_entries[start+i];
      EntryData entry = (m_result[pos.m_cal].getEntry())[pos.m_entry];
      addEntry(page[pos.m_cal], entry);
      viewData[i] = new XMLData();
      // put ceid to "view" once if there are many
      boolean existed = false;
      for (int j=0; j < i; j++)
        if (entry.getCeid().equals(viewData[j].getA("ceid"))){
          existed = true; break;
        }
      if (!existed)
        viewData[i].putA("ceid", entry.getCeid());
    }
    ((XMLData)m_view.getE("calendar")).putE("entry",viewData);
  }

  //------------------------------------------------------------------------//

  /**
   * Initialize the screen
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception
  {
    super.init(params);
    m_input.clear();

    String calid = (String)params.get("calid");

    if (calid==null)
      calid = "all-calendars";

    XMLData viewCal = new XMLData();
    viewCal.putA("calid",calid);
    m_view.putE("calendar",viewCal);

    // added to specify which channel  the search screen was being called from
    XMLData back = new XMLData();
	back.putA("value",(String)params.get("back"));
	m_data.putE("back",back);
	
    m_data.putE("view",m_view);
    m_input.putA("title", new Boolean(true));
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
    Date start = new Date(Channel.getCurrentDate().getTime());
    Date end = new Date(Channel.getCurrentDate().getTime());
    m_input.putA("start",sdf.format(start));
    m_input.putA("end",sdf.format(end));
    m_view.putE("search-event",m_input);

    // Reset all calendars
    CalendarData[] cals = getServer().getCalendars(null);

    m_result = new CalendarData[cals.length];
    for (int i=0; i < cals.length; i++)
      m_result[i] = (CalendarData)cals[i];
    reset();
  }

  /**
   * Re-initialize the screen
   * @param params
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception
  {
    String ret = (String)params.get("ret");
    String init = (String)params.get("init");
    if (init != null)
    {
      String calid = (String)params.get("calid");
      if (calid != null)
        ((XMLData)m_view.getE("calendar")).putA("calid",calid);
      reset();
      m_title = "";
      m_input.putA("search-str",m_title);

      // Truong 011102 - reset input form
      m_input.clear();
      SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
      Date start = new Date(Channel.getCurrentDate().getTime());
      Date end = new Date(Channel.getCurrentDate().getTime());
      m_input.putA("start",sdf.format(start));
      m_input.putA("end",sdf.format(end));
      m_input.putA("title","true");
      
      // Added so that the calendars would be updated in the CMS channel
      // when changing the offerings
      if(((String)params.get("back")).equals("MainLMS")){
	      // Reset all calendars
	      CalendarData[] cals = getServer().getCalendars(null);
	
	      m_result = new CalendarData[cals.length];
	      for (int i=0; i < cals.length; i++)
	        m_result[i] = (CalendarData)cals[i];
	      reset();
      }

    }
    else if ((ret == null || !ret.equals("cancel")) && init == null)
      find(null);
  }


  /**
   * Make a copy of the screen
   * @return
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException
  {
    Search clone = (Search)super.clone();
    clone.m_data = m_data;
    clone.m_view = m_view;
    clone.m_result = m_result;
    clone.m_entries = m_entries;
    clone.m_nPages = m_nPages;
    clone.m_curPage = m_curPage;
    return clone;
  }

  /**
   * Get xml-based data of the screen
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception
  {
    return m_data;
  }


  /**
   * Get parameter of XSL
   * @return
   */
  public Hashtable getXSLTParams()
  {
    Hashtable params = super.getXSLTParams();
    params.put("cur-date", curDate());
    return params;
  }


  /**
   * Process "Find" action
   * @param params
   * @return
   * @throws Exception
   */

  public Screen find(Hashtable params) throws Exception
  {
    //--- Clear previous search results
    reset();

    //--- Get input from params
    if (params != null)
    {
      m_title = (String)params.get("search-string");
      //m_input.putA("search-str", XMLData.format(m_title));
      m_input.putA("search-str", m_title);
      m_input.putA("title", new Boolean(params.containsKey("titles")));
      m_input.putA("notes", new Boolean(params.containsKey("notes")));
      m_input.putA("places", new Boolean(params.containsKey("places")));
      String start = params.get("month1") + "/" + params.get("day1") + "/" + params.get("year1");
      m_input.putA("start", start );
      String end =  params.get("month2") + "/" + params.get("day2") + "/" + params.get("year2");
      m_input.putA("end", end);

      String category = (String)params.get("category");
      if (category == null || category.equals("all-categories"))
        m_input.removeA("category");
      else
        m_input.putA("category", category);
      String calid = (String)params.get("calid");
      if (calid == null || calid.equals("all-calendars"))
      {
        ((XMLData)m_view.getE("calendar")).removeA("calid");
        calid = null;
      }
      else
        ((XMLData)m_view.getE("calendar")).putA("calid", calid);
    }

    // Check valid input
    Date from = null;
    Date to = null;
    try{
      String sfrom = (String)m_input.getA("start");
      String sto = (String)m_input.getA("end");
      if (sfrom != null && sfrom.length() > 0) from = parseDate2(sfrom);
      if (sto != null && sto.length() > 0) to = parseDate2(sto);
    }catch(ParseException pe)
    {
      return info(pe.getMessage(), true);
    }

    Calendar cal = Calendar.getInstance();
    cal.setTime(to);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    to = cal.getTime();

    String calid = (String)((XMLData)m_view.getE("calendar")).getA("calid");
    if (calid != null && calid.equals("all-calendars"))
      calid = null;
    // Search...
    getServer().searchEvent((calid == null)?m_result:new CalendarData[]{CalendarData.findCalendar(m_result,calid)},
                            m_title,
                            (String)m_input.getA("category"),
                            getLookin(), from, to);

    // Prepare display page
    m_entries = sort();
    m_nPages = m_entries.length / MAX_NUM;
    if (m_entries.length % MAX_NUM > 0) m_nPages++;
    setPage(m_nPages > 0? 0: -1);
    return this;
  }

  /**
   * Process "Previous" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen previous(Hashtable params) throws Exception
  {
    String p = (String)params.get("p");
    if( p != null) try {
      setPage( Integer.parseInt(p) - 1);
    } catch( Exception e) {
      log(e);
    }
    return this;
  }

  /**
   * Process "Next" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen next(Hashtable params) throws Exception
  {
    String p = (String)params.get("p");
    if( p != null) try {
      setPage( Integer.parseInt(p) + 1);
    } catch( Exception e) {
      log(e);
    }
    return this;
  }

  int getLookin()
  {
    int n = 0;
    Boolean b = (Boolean)m_input.getA("title");
    if (b != null && b.booleanValue()) n += CalendarServer.LOOK_IN_TITLE;
    b = (Boolean)m_input.getA("notes");
    if (b != null && b.booleanValue()) n += CalendarServer.LOOK_IN_NOTES;
    b = (Boolean)m_input.getA("places");
    if (b != null && b.booleanValue()) n += CalendarServer.LOOK_IN_PLACE;
    return n;
  }

  Date parseDate2(String dateStr)throws Exception
  {
    try{
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      Date date = sdf.parse(dateStr);
      return date;
    }catch(ParseException pe)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
      return sdf.parse(dateStr);
    }
  }

  EntryPos[] sort()
  {
    // collect all entries
    Vector v = new Vector();
    int nEvents = 0;
    for (int iCal = 0; iCal < m_result.length; iCal++)
    {
      EntryData[] entries = m_result[iCal].getEntry();
      if (entries != null) for (int i = 0; i < entries.length; i++)
      {
        entries[i].putE("pos", new EntryPos(iCal,i));
        v.addElement(entries[i]);
      }
    }

    // sort by date-time
    CalendarServer.sort(v);

    // Convert to array of EntryPos
    EntryPos[] result = new EntryPos[v.size()];
    for (int i = 0; i < v.size(); i++)
    {
      result[v.size() - i -1] = (EntryPos)((EntryData)v.elementAt(i)).getE("pos");
      ((EntryData)v.elementAt(i)).putE("pos",null);
    }
    return result;
  }

  void addEntry(CalendarData cal, EntryData entry)
  {
    EntryData[] entries = cal.getEntry();
    int len = (entries == null)? 0: entries.length;
    EntryData[] newEntries = new EntryData[len + 1];
    for (int i = 0; i < len; i++)
      newEntries[i] = entries[i];
    newEntries[len] = entry;
    cal.putEntry(newEntries);
  }

  static String enc(String s)
  {
    s = XMLData.replace(s, "'", "&#");
    s = XMLData.replace(s, "\"", "&#34;");
    s = XMLData.replace(s, "<", "&#60;");
    s = XMLData.replace(s, ">", "&#62;");

    return s;
  }


}
