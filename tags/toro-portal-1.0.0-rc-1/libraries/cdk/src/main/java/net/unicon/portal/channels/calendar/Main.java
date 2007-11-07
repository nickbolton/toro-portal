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

import java.io.*;
import java.util.*;
import java.text.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;

/**
 * This class represents main screen of Calendar
 */
public class Main extends CalendarScreen
{
  XMLData m_data = new XMLData();
  XMLData m_view= new XMLData();

  CalendarView m_mini = new CalendarView("monthly");
  CalendarView m_detail = new CalendarView("monthly");
  EntryView m_entry = new EntryView();

  String m_calid = null;
  String m_window = "event";
  String m_main = "monthly";
  int m_week = 1;

  /**
   * Get identifier of the screen
   * @return
   */
  // truyen 03/21/2002
  public String sid()
  {
    return "Main";
  }

  /**
   * Get parameter of XSL
   * @return
   */
  public Hashtable getXSLTParams()
  {
    Hashtable params = super.getXSLTParams();
    params.put("window",m_window);
    params.put("main", m_detail.getMode());
    return params;
  }
  //---------end-----------

  public void reinit(Hashtable params) throws Exception
  {
    m_data.putE("calendar",null);
    init(params);
  }

  /**
   * Get xml-based data for the main screen
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception
  {
    //truyen 6/6/02
    m_data.putE("view", m_view);
    m_view.putE("calendar", new CalendarData());

    // Calendars
    //truyen 6/6/02
    CalendarData[] cals = getAllCalendars();
    Date[] range = m_detail.getRange();
    if (m_calid == null) m_calid = validateCalid(m_calid);
    if (m_calid.equals("all-calendars"))
    {
      if( m_window.equals("event"))
        cals = getServer().fetchEvents(cals, range[0], range[1]);
      else if( m_window.equals("todo"))
        cals = getServer().fetchTodos(cals, range[0], range[1]);
      else
        cals = getServer().fetchEntries(cals, range[0], range[1]);
      EntryData.removeDup(cals, getServer().getUser());  //truong 20/5/02
    }

    // Composite view
    else if (m_calid.startsWith("composite"))
      cals = getCompositeCalendars(cals, m_calid, range);
    else {
      if( m_window.equals("event")){
        cals = getServer().fetchEvents(new CalendarData[] {CalendarData.
                                       findCalendar(cals, m_calid)}
                                       , range[0], range[1]);
        //if (cals != null) System.out.println("cals length"+ cals.length + "calid 0 " + cals[0].getCalid());

      }
      else if( m_window.equals("todo"))
        cals = getServer().fetchTodos(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, range[0], range[1]);
      else
        cals = getServer().fetchEntries(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, range[0], range[1]);
      if (cals != null && cals.length != 0)
        cals = CalendarData.replaceCal(getAllCalendars(), cals[0]);
    }

    m_data.putE("calendar", cals);

    // view calendar id
    ((CalendarData)m_view.getE("calendar")).putCalid(m_calid);

    //for concurent delete
    //if (CalendarData.findCalendar(cals, m_calid) == null || CalendarData.findCalendar(cals, m_calid).getACE() == null)
    //  m_calid = getServer().getUser();
    //

    // Daily view mode
    String mode = m_detail.getMode();
    if (mode.equals("daily"))
    {
      Vector calv = new Vector();
      Timetable layout = new Timetable();
      if (m_calid.equals("all-calendars") || m_calid.startsWith("composite"))
        layout.layout(m_detail.getTime(), cals);
      else
      {
        CalendarData cal = CalendarData.findCalendar(cals, m_calid);
        EntryData[] ent = cal != null ? cal.getEntry() : null;
        layout.layout(m_detail.getTime(), ent);
      }

      layout.build(m_detail);
    }

    // Detail-monthly, Detail-daily, detail-weekly
    m_data.putE("mini-entry", m_entry);
    m_mini.setTime(m_detail.getTime());
    m_data.putE("mini-monthly", m_mini);
    m_data.putE("detail-monthly",null);
    m_data.putE("detail-daily",null);
    m_data.putE("detail-weekly",null);
    m_data.putE("detail-"+mode, m_detail);

    return m_data;
  }

  /**
   * Initialize
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception
  {
    super.init(params);
    // Get parameters : calendar id and op
    String calid = params.containsKey("calid")?(String)params.get("calid"):m_calid;
    // If init from toolbar, don't care about calid
    if (m_calid == null)
      m_calid = validateCalid(calid);
    String op = (String)params.get("op");
    m_window = params.get("window")!= null?(String)params.get("window"): m_window;

    // View mode: Monthly or Daily
    if (op == null);
    else if (op.startsWith("m~"))
    {
      m_detail.setMode("monthly");
      m_detail.setTime(op.substring(2));
    }
      // Truyen 03/21/2002
      //Weekly view
    else if (op.startsWith("w~"))
    {
      m_detail.setMode("weekly");
      m_detail.setTime(op.substring(2));
      m_week = m_detail.weekOfDate(m_detail.getTime());
      m_detail.putA("week", Integer.toString(m_week));
    }
    // Daily view
    else if (op.startsWith("d~"))
    {
      m_detail.setMode("daily");
      m_detail.setTime(op.substring(2));
    }
//    fetchData(from, to);
  }

  /**
   * Get name of xsl file.
   * @return
   */
  public String getXSL()
  {
    if (m_detail.getMode().equals("monthly"))
      return "Main-Monthly";
    if (m_detail.getMode().equals("weekly"))
      return "Main-Weekly";
    if (m_detail.getMode().equals("daily"))
      return "Main-Daily";
    return null;
  }
  //Truyen 03/25/2002
  /**
   * change view Event or Todo
   * @param params
   * @return
   * @throws Exception
   */
  public Screen changeView(Hashtable params) throws Exception
  {
    String window = (String)params.get("window");
    String op = (String)params.get("op");

    if(!m_window.equals(window))
      m_window = window;

    if (op == null) ;
    // View mode (v:monthly,v:weekly,v:daily)
    else if (op.startsWith("v~"))
      if (!m_detail.getMode().equals(op.substring(2)))
        m_detail.setMode(op.substring(2));

    return this;
  }

  /**
   * Change view mode and time range to view
   */
  public Screen update(Hashtable params) throws Exception
  {
    log("****************** " + params);
    // Get input
    String calid = (String)params.get("calid");
    log("****************** " + calid);
    // return if choose -----
    if (calid != null && calid.length() == 0) return this;

    String op = (String)params.get("op");

    //--------- Make changes based on "op" ------------
    if (calid != null)
      m_calid = validateCalid(calid);

    // View mode (v:monthly,v:weekly,v:daily)
    if (op == null) ;
    else if (op.startsWith("v~"))
    {
      m_detail.setMode(op.substring(2));
      Calendar cal = Calendar.getInstance();
      cal.setTime(m_detail.getTime());
      m_week = m_detail.weekOfDate(m_detail.dayOfWeek(cal.get(Calendar.WEEK_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)));
      m_detail.putA("week", Integer.toString(m_week));
    }

    // Detail of specific month
    else if (op.startsWith("m~"))
    {
      m_detail.setMode("monthly");
      m_detail.setTime(op.substring(2));
    }

    // Detail of specific day
    else if (op.startsWith("d~"))
    {
      m_detail.setMode("daily");
      m_detail.setTime(op.substring(2));
    }

    return this;
  }
    //Truyen 03/24/2002
    /**
     * Change month/year
     */
  public Screen changeDate(Hashtable params)throws Exception
  {
    String month = (String)params.get("month");
    String day = (String)params.get("day");
    String year = (String)params.get("year");
    String week = (String)params.get("week");
    
    System.out.println("Month: " + month + "Day: " + day + "Year: "  + year + "Week: " + week );

    if (m_detail.getMode().equals("monthly"))
      m_detail.setTime(month + "/1/" + year + "_00:00");
    else if(m_detail.getMode().equals("weekly"))
    {
      m_detail.setTime(m_detail.dayOfWeek(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year)));
      m_week = m_detail.weekOfDate(m_detail.getTime());
      m_detail.putA("week", Integer.toString(m_week));
    }
    else
      m_detail.setTime(month + "/"+ day +"/" + year + "_00:00");

    return this;
  }
  
  public Screen nextDate(Hashtable params)throws Exception
  {
  	int tmpMonth;
  	int tmpDay;
  	int tmpYear;
  	int tmpWeek;
  	
  	String week = null;
  	String month = null;
  	String year= null;
  	String day = null;
  	    
    System.out.println("Month: " + month + "Day: " + day + "Year: "  + year + "Week: " + week );

    if (m_detail.getMode().equals("monthly"))
    {
    	if (params.get("month") != null)
      	{
      	tmpMonth = Integer.parseInt((String)params.get("month")) + 1;
      	month = String.valueOf(tmpMonth);
      	}
    	day = (String)params.get("day");
        year = (String)params.get("year");
        week = (String)params.get("week");	
        m_detail.setTime(month + "/1/" + year + "_00:00");
    }
    else if(m_detail.getMode().equals("weekly"))
    {
    	if (params.get("week") != null)
      	{	
      	tmpWeek = Integer.parseInt((String)params.get("week")) + 1;
      	week = String.valueOf(tmpWeek);
      	}
    	day = (String)params.get("day");
        year = (String)params.get("year");
        month = (String)params.get("month"); 	
      m_detail.setTime(m_detail.dayOfWeek(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year)));
      m_week = m_detail.weekOfDate(m_detail.getTime());
      m_detail.putA("week", Integer.toString(m_week));
    }
    else
    {
    	if (params.get("day") != null)
      	{
      	tmpDay = Integer.parseInt((String)params.get("day")) + 1;
      	day = String.valueOf(tmpDay);
      	}	
    	week = (String)params.get("week");
        year = (String)params.get("year");
        month = (String)params.get("month");
      m_detail.setTime(month + "/"+ day +"/" + year + "_00:00");
    }
    return this;
  }
  
  public Screen previousDate(Hashtable params)throws Exception
  {
  	int tmpMonth;
  	int tmpDay;
  	int tmpYear;
  	int tmpWeek;
  	
  	String week = null;
  	String month = null;
  	String year= null;
  	String day = null;
  	
    System.out.println("Month: " + month + "Day: " + day + "Year: "  + year + "Week: " + week );

    if (m_detail.getMode().equals("monthly"))
    {
    	if (params.get("month") != null)
      	{
      	tmpMonth = Integer.parseInt((String)params.get("month")) - 1;
      	month = String.valueOf(tmpMonth);
      	}
    	day = (String)params.get("day");
        year = (String)params.get("year");
        week = (String)params.get("week");	
        m_detail.setTime(month + "/1/" + year + "_00:00");
    }
    else if(m_detail.getMode().equals("weekly"))
    {
    	if (params.get("week") != null)
      	{	
      	tmpWeek = Integer.parseInt((String)params.get("week")) - 1;
      	week = String.valueOf(tmpWeek);
      	}
    	day = (String)params.get("day");
        year = (String)params.get("year");
        month = (String)params.get("month"); 	
      m_detail.setTime(m_detail.dayOfWeek(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year)));
      m_week = m_detail.weekOfDate(m_detail.getTime());
      m_detail.putA("week", Integer.toString(m_week));
    }
    else
    {
    	if (params.get("day") != null)
      	{
      	tmpDay = Integer.parseInt((String)params.get("day")) - 1;
      	day = String.valueOf(tmpDay);
      	}	
    	week = (String)params.get("week");
        year = (String)params.get("year");
        month = (String)params.get("month");
      m_detail.setTime(month + "/"+ day +"/" + year + "_00:00");
    }
    return this;
  }
  

  /**
   * Process "Quick Add Event" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen addEvent(Hashtable params) throws Exception
  {
    // Get input
    String calid = validateCalid((String)params.get("calid"));
    String sMonth = (String)params.get("month");
    String sDay = (String)params.get("day");
    String sYear = (String)params.get("year");
    String sHour = (String)params.get("hour");
    String sMinute = (String)params.get("minute");
    String sHours = (String)params.get("hours");
    String sMinutes = (String)params.get("minutes");
    String title = ((String)params.get("event")).trim();

    // Check valid input
    if (title.length() == 0 )
      return error(CCalendar.ERROR_NO_EVENT_TITLE);
    if (calid.equals("all-calendars") || calid.startsWith("composite"))
      calid = CalendarData.findPersonal((CalendarData[])m_data.getE("calendar"),getServer().getUser()).getCalid();

    // Prepare data
    EntryData ent = new EntryData();
    EventData ev = new EventData();
    DurationData dur = new DurationData();
    Calendar start = Timetable.getCalendar();
    start.set(Calendar.MONTH, Integer.parseInt(sMonth)-1);
    start.set(Calendar.DATE, Integer.parseInt(sDay));
    start.set(Calendar.YEAR, Integer.parseInt(sYear));
    start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sHour));
    start.set(Calendar.MINUTE, Integer.parseInt(sMinute));
    TimeLength length = new TimeLength((Integer.parseInt(sHours)*3600+Integer.parseInt(sMinutes)*60)*1000);
    dur.putStart(start.getTime());
    dur.putLength(length);
    ent.putDuration(dur);
    ev.put(title);
    ev.putPriority(new Integer(5));
    ent.putEvent(ev);

    // Check length of event
    if (length.getLength() == 0)
    {
      m_view.putE("entry",ent);
      return error(CCalendar.ERROR_INPUT_LENGTH);
    }

    // Create event
    try
    {
      getServer().createEntry(calid, ent);
    }
    catch (Exception e)
    {
      m_channel.log(e);
      return error(CCalendar.ERROR_FAILED_TO_CREATE, new Object[]{getServer().getError()});
    }

    return info(CCalendar.MSG_EVENT_ADDED, new Object[]{ev.get()}, true);
  }

  /**
   * Process "Quick add todo" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen addTodo(Hashtable params) throws Exception
  {
    // Get input
    String calid = validateCalid((String)params.get("calid"));
    String sMonth = (String)params.get("month");
    String sDay = (String)params.get("day");
    String sYear = (String)params.get("year");
    String sHour = (String)params.get("hour");
    String sMinute = (String)params.get("minute");
    String title = ((String)params.get("todo")).trim();

    // Check valid input
    if (title.length() == 0 )
      return error(CCalendar.ERROR_NO_TODO_TITLE);
    if (calid.equals("all-calendars") || calid.startsWith("composite"))
      calid = CalendarData.findPersonal((CalendarData[])m_data.getE("calendar"),getServer().getUser()).getCalid();

    // Prepare data
    TodoData td = new TodoData();
    EntryData ent = new EntryData();
    DurationData dur = new DurationData();
    Calendar due = Timetable.getCalendar();
    due.set(Calendar.MONTH, Integer.parseInt(sMonth)-1);
    due.set(Calendar.DATE, Integer.parseInt(sDay));
    due.set(Calendar.YEAR, Integer.parseInt(sYear));
    due.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sHour));
    due.set(Calendar.MINUTE, Integer.parseInt(sMinute));
    td.putDue(due.getTime());
    td.put(title);
    td.putPriority(new Integer(5));
    ent.putTodo(td);

    // Create todo
    try
    {
      getServer().createEntry(calid, ent);
    }
    catch (Exception e)
    {
      m_channel.log(e);
      return error(CCalendar.ERROR_TODO_FAILED_TO_CREATE, new Object[]
                   {getServer().getError()});

    }

    return info(CCalendar.MSG_TODO_ADDED, new Object[]{td.get()}, true);
  }

  /**
   * Process "complete todo" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen complete(Hashtable params) throws Exception
  {
    log("** Main.complete:"+ params);
    // Get input
    String calid = (String)params.get("calid");
    String ceid = (String)params.get("ceid");
    boolean complete = (params.get("complete") != null);

    EntryData data = getServer().fetchTodosByIds(calid,new EntryRange[]{new EntryRange(ceid)})[0];
    if (complete)
      getServer().completeTodo(calid, new EntryRange(ceid), new CompletionData(data.getDuration().getStart(), new Integer(100)));
    else
      getServer().completeTodo(calid, new EntryRange(ceid), new CompletionData(data.getDuration().getStart(), new Integer(0)));
    return this;
  }

  CalendarData[] getAllCalendars() throws Exception{
    CalendarData[] cals = CalendarData.cloneCalProps(getServer().getCalendars(null));
    XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (data != null){
      Object o = data.getE("calendar-hidden");
      if (o != null){
        if (o instanceof Object[]){
          Object[] os = (Object[])o;
          for (int i = 0; i <  os.length; i++){
            String calid = (String)((XMLData)os[i]).getA("calid");
            if (calid != null) cals = CalendarData.removeCalendar(cals, calid);
          }
        }
        else{
          String calid = (String)((XMLData)o).getA("calid");
            if (calid != null) cals = CalendarData.removeCalendar(cals, calid);
        }
      }
    }
    return cals;
  }



  String validateCalid(String calid) throws Exception{

    if (calid == null || CalendarData.findCalendar((CalendarData[])m_data.getE("calendar"),calid) == null)
      calid = calid != null && (calid.equals("all-calendars") || calid.startsWith("composite"))? calid : getServer().getUser();

    return calid;
  }
  //--------------------------------------------------------------------------//

  CalendarData[] getCompositeCalendars(CalendarData[] cals, String groupid, Date[] range) throws Exception
  {
    CalendarData[] compCals = null;
    String s = m_channel.getUserData(CCalendar.USERDATA_KEY);
    if (s != null){
      // Parse xml
      XMLData data = new XMLData();
      data.parse( new ByteArrayInputStream(s.getBytes()));

      // Get id of composite
      Object[] gcr = data.rgetE("calendar-group");
      XMLData caldata = null;
      if( gcr != null && gcr.length > 0) {
        Vector v = new Vector();
        for (int i = 0; i < gcr.length; i++){
          String id = (String)((XMLData)gcr[i]).getA("id");
          log("composite. id ********** *******" + id +" "+ groupid.substring(10));
          if (groupid.substring(10).equals(id)) caldata = (XMLData)gcr[i];
        }
      }

      // Get calid of composite
      Object[] cr = caldata != null ? caldata.rgetE("calendar") : null;
      if( cr != null && cr.length > 0) {
        Vector v = new Vector();
        for (int i = 0; i < cr.length; i++){
          String calid = (String)((XMLData)cr[i]).getA("calid");
          log("composite.calid ********** *******" + calid);
          CalendarData c = CalendarData.findCalendar(cals,calid);
          if( c != null) v.addElement(c);
          else log("Composite: not found calendar:"+calid);
        }
        compCals = (CalendarData[])v.toArray(new CalendarData[0]);
      }
    }

    // Default when not specifying preferences
    if( compCals == null)
      compCals = getAllCalendars();

    // Get all events
    if( m_window.equals("event"))
      compCals = getServer().fetchEvents(compCals, range[0], range[1]);
    else if( m_window.equals("todo"))
      compCals = getServer().fetchTodos(compCals, range[0], range[1]);
    else
      compCals = getServer().fetchEntries(compCals, range[0], range[1]);

    EntryData.removeDup(cals, getServer().getUser());

    if (compCals!= null && cals != null){
      for (int i = 0; i < compCals.length; i++){
        CalendarData.replaceCal(cals, compCals[i]);
      }
    }

    // filter for hidden
    /*
    if (cals != null && cals.length != 0){
      XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
      if (data != null) {
        Object o = data.getE("calendar-hidden");
        if (o != null) {
          if (o instanceof Object[]) {
            Object[] os = (Object[]) o;
            for (int i = 0; i < os.length; i++) {
              String calid = (String) ( (XMLData) os[i]).getA("calid");
              if (calid != null)
                cals = CalendarData.removeCalendar(cals, calid);
            }
          }
          else {
            String calid = (String) ( (XMLData) o).getA("calid");
            if (calid != null)
              cals = CalendarData.removeCalendar(cals, calid);
          }
        }
      }
    }
    */
    //


    //return compCals;
    return cals;
  }

/*  void fecthData(Date from, Date to)
  {
    // Fetch data
    if (m_calid.equals("all-calendars"))
    {
      if( m_window.equals("event"))
        getServer().fetchEvents(cals, from, to);
      else if( m_window.equals("todo"))
        getServer().fetchTodos(cals, from, to);
      else
        getServer().fetchEntries(cals, from, to);
      EntryData.removeDup(cals, getServer().getUser());  //truong 20/5/02
    }
    else {
      if( m_window.equals("event"))
        getServer().fetchEvents(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, from, to);
      else if( m_window.equals("todo"))
        getServer().fetchTodos(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, from, to);
      else
        getServer().fetchEntries(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, from, to);
    }
  }
*/
}
