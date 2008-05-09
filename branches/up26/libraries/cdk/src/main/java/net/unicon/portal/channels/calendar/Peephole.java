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

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import net.unicon.academus.apps.calendar.CalendarData;
import net.unicon.academus.apps.calendar.CalendarServer;
import net.unicon.academus.apps.calendar.CompletionData;
import net.unicon.academus.apps.calendar.DurationData;
import net.unicon.academus.apps.calendar.EntryData;
import net.unicon.academus.apps.calendar.EntryRange;
import net.unicon.academus.apps.calendar.EventData;
import net.unicon.academus.apps.calendar.TimeLength;
import net.unicon.academus.apps.calendar.TodoData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;

public class Peephole extends CalendarScreen {
  // Peephole mode
  public static final int PVM_CALENDAR = 0;
  public static final int PVM_EVENTS = 1;
  public static final int PVM_TODOS = 2;

  // Members
  int m_pvm = PVM_CALENDAR;
  String m_calid = "";
  String m_window = "event";

  XMLData m_data = new XMLData();
  CalendarView m_monthly = new CalendarView("monthly");
  CalendarView m_daily = new CalendarView("daily");
  XMLData m_invitationView = new XMLData();
  // truyen 03/21/002
  XMLData m_view= new XMLData();

  public Peephole() {
    m_data.putE("mini-monthly", m_monthly);
    m_data.putE("mini-daily", m_daily);
    m_data.putE("invitations",m_invitationView);
  }

  // truyen 03/21/2002
  public String sid() {
    return "Peephole";
  }

  public boolean canRenderAsPeephole() {
    return true;
  }

  public void refresh() throws Exception {
    // get calendars
    CalendarData[] cals = null;
    CalendarData[] calsview = null;
    CalendarServer server = getServer();

    switch( m_pvm) {
    case PVM_TODOS:
      Date[] range0 = m_daily.getRange();
      CalendarData[] allCals0 = cals = getAllCalendars();
      if (m_calid.startsWith("composite"))
        cals = getCompositeCalendars(cals, m_calid, range0);
      else if (m_calid != null && !m_calid.equals("") && !m_calid.equals("all-calendars"))
        cals = new CalendarData[]{CalendarData.findCalendar(cals, m_calid)};
      //calsview = CalendarData.cloneCalProps(cals);
      //m_data.putE("calendar", calsview);
      m_data.putE("calendar", allCals0);
      // for view cur calendar
      XMLData xcalid0 = new XMLData();
      xcalid0.putA("calid", m_calid);
      m_view.putE("calendar", xcalid0);
      //
      cals = server.fetchTodos(cals, null, null);
      //m_data.putE("view", filter(cals, server.getUser()));
      m_entry = filter(cals, server.getUser());
      m_data.putE("view", new XMLData[]{m_entry, m_view});
      break;
    case PVM_EVENTS:
      Date[] range = m_daily.getRange();
      CalendarData[] allCals = cals = getAllCalendars();
      if (m_calid.startsWith("composite"))
        cals = getCompositeCalendars(cals, m_calid, range);
      else if (m_calid != null && !m_calid.equals("") && !m_calid.equals("all-calendars"))
        cals = new CalendarData[]{CalendarData.findCalendar(cals, m_calid)};
      m_data.putE("calendar", allCals);
      cals = server.fetchEvents(cals,range[0],range[1]);
      // for view cur calendar
      XMLData xcalid = new XMLData();
      xcalid.putA("calid", m_calid);
      m_view.putE("calendar", xcalid);
      //
      m_entry = filter(cals, server.getUser());
      m_data.putE("view", new XMLData[]{m_entry, m_view});
      range = getRange(0, CCalendar.EVENT_AFTER);
      m_invitationView.putA("count",new Integer(server.countInvitations(range[0], range[1])));
      m_invitationView.putA("days", new Integer(CCalendar.EVENT_AFTER +1));
      break;
    case PVM_CALENDAR:
      m_data.putE("calendar", null);
      break;
    }
  }

  public void init(Hashtable params) throws Exception {
    super.init( params);

    // Peephole view mode
    Integer mode = (Integer)getShared("CCalendar.PeepholeMode");
    if( mode == null) {
      String view = (String)m_channel.m_csd.getParameter("view");
      view = (view == null)?"Calendar":view.trim();
      mode = view.equals("Events")?new Integer( PVM_EVENTS):
             view.equals("Todos")?new Integer( PVM_TODOS):new Integer( PVM_CALENDAR);
      putShared("CCalendar.PeepholeMode", mode);
    }
    m_pvm = mode.intValue();
    if (m_pvm == PVM_TODOS)
      m_window = "todo";
    else
      m_window = "event";

    // Get personal use info
    XMLData udata = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (udata != null) {
      Object o = udata.getE("peephole-calid");
      if (o != null) {
        String calid = ((XMLData)o).get();
        if (calid != null)
          m_calid = calid;
      }
    }
    //
  }

  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("view", (m_pvm == PVM_EVENTS)?"Events":(m_pvm == PVM_TODOS)?"Todos":"Calendar");
    params.put("focusedChannel","Y");
    return params;
  }


  public XMLData getData() throws Exception {
    // get calendars
    refresh();
    return m_data;
  }

  public Screen update(Hashtable params) throws Exception {
    String calid = (String)params.get("calid");
    if (calid != null)
      m_calid = calid;
    //save calid for personal use
    if (m_calid != null && !m_calid.equals(""))
      savePerInfo(m_calid);
    //
    String op = (String)params.get("op");
    //Truyen 03/21/2001
    if (op != null && (op.startsWith("m~") || op.startsWith("d~")|| op.startsWith("w~"))) {
      Main main = (Main)makeScreen("Main");
      main.m_mini = m_monthly;
      main.init(params);
      return main;
    }

    return this;
  }
  //Truyen March 21-2002
  /**
   * Change month/year
   */
  public Screen changeDate(Hashtable params)throws Exception {
    // get calendars
    //truyen 6/6/02
    CalendarData[] cals = getAllCalendars();

    String month = (String)params.get("month");
    String day = (String)params.get("day");
    String year = (String)params.get("year");
    Date[] range = null;
    if (day != null) {
      m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");
      range = m_daily.getRange();
    } else {
      m_monthly.setTime(month + "/1/" + year + "_00:00");
      range = m_monthly.getRange();
    }
    return this;
  }
  
  public Screen nextDate(Hashtable params)throws Exception
  {
  	int tmpMonth;
  	int tmpDay;
  	int tmpYear;
  	
  	
  	String month = null;
  	String year= null;
  	String day = null;
  	Date[] range = null;
  		
  	if (params.get("day") != null)
  	{
  	tmpDay = Integer.parseInt((String)params.get("day")) + 1;
  	day = String.valueOf(tmpDay);	
    year = (String)params.get("year");
    month = (String)params.get("month");
    m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");
    range = m_daily.getRange();
  	}
    else
    {
  	tmpMonth = Integer.parseInt((String)params.get("month")) + 1;
    month = String.valueOf(tmpMonth);
    day = (String)params.get("day");
    year = (String)params.get("year");	
    m_monthly.setTime(month + "/1/" + year + "_00:00");
    range = m_monthly.getRange();
    }
    return this;
  }
  
  public Screen previousDate(Hashtable params)throws Exception
  {
  	int tmpMonth;
  	int tmpDay;
  	int tmpYear;
  	
  	String month = null;
  	String year= null;
  	String day = null;
  	Date[] range = null;
  	
  	if (params.get("day") != null)
  	{
  	tmpDay = Integer.parseInt((String)params.get("day")) - 1;
  	day = String.valueOf(tmpDay);	
    year = (String)params.get("year");
    month = (String)params.get("month");
    m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");
    range = m_daily.getRange();
  	}
    else
    {
  	tmpMonth = Integer.parseInt((String)params.get("month")) - 1;
    month = String.valueOf(tmpMonth);
    day = (String)params.get("day");
    year = (String)params.get("year");	
    m_monthly.setTime(month + "/1/" + year + "_00:00");
    range = m_monthly.getRange();
    }    
  	
  	return this;
  }
  

  /**
   * Quick Add event to
   */
  public Screen addEvent(Hashtable params) throws Exception {
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
    if (calid.equals("all-calendars"))
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
    if (length.getLength() == 0) {
      m_view.putE("entry",ent);
      return error(CCalendar.ERROR_INPUT_LENGTH);
    }

    // Create event
    try {
      getServer().createEntry(calid, ent);
      /*    //truyen 6/6/02
            // Fetch data
            Date[] range = m_daily.getRange();
            getServer().fetchEntries((CalendarData[])m_data.getE("calendar"),range[0],range[1]);
      */
    } catch (Exception e) {
      m_channel.log(e);
      return error(CCalendar.ERROR_FAILED_TO_CREATE, new Object[]{getServer().getError()});
    }

    return info(CCalendar.MSG_EVENT_ADDED, new Object[]{ev.get()}, true);
  }
  /**
   * Quick Add todo to
   */
  public Screen addTodo(Hashtable params) throws Exception {
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
    if (calid.equals("all-calendars"))
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
    try {
      getServer().createEntry(calid, ent);
      /*    //truyen 6/6/02
            // Fetch data
            Date[] range = m_daily.getRange();
            getServer().fetchEntries((CalendarData[])m_data.getE("calendar"),range[0],range[1]);
      */
    } catch (Exception e) {
      m_channel.log(e);
      return error(CCalendar.ERROR_TODO_FAILED_TO_CREATE, new Object[]
                   {getServer().getError()});

    }

    return info(CCalendar.MSG_TODO_ADDED, new Object[]{td.get()}, true);
  }

  String validateCalid(String calid) throws Exception {
    if (calid == null || CalendarData.findCalendar((CalendarData[])m_data.getE("calendar"),calid) == null)
      calid = calid != null && calid.equals("all-calendars") ? calid : getServer().getUser();

    return calid;
  }

  /**
   * Todo completion
   */
  public Screen complete(Hashtable params) throws Exception {
    //XMLData data = (XMLData)m_data.getE("view");
    XMLData data = m_entry;
    if( data == null)
      return this;
    XMLData[] todos = (XMLData[])data.getE("todo");
    if( todos == null)
      return this;

    for(Enumeration en = params.keys();en.hasMoreElements();) {
      String idStr = (String)en.nextElement();
      // Selected check box
      if(idStr.startsWith("chk:")) {
        String key = idStr.substring(4);
        String ceid = key.substring(0,key.indexOf("@")) ;
        String calid = key.substring(key.indexOf("@")+1);

        for( int i = 0; i < todos.length; i++)
          if( todos[i].getA("calid").equals( calid) && todos[i].getA("ceid").equals( ceid)) {
            getServer().completeTodo(calid, new EntryRange(ceid), new CompletionData((Date)todos[i].getA("start"), new Integer(100)));
            break;
          }
      }
    }

    return this;
  }
  //-------------end-----------------
  XMLData m_entry = new XMLData();

  public XMLData filter(CalendarData[] cals, String user) {
    if (cals != null) {
      HashMap map = new HashMap();
      for (int i = 0; i < cals.length; i++) {
        EntryData[] entr = cals[i].getEntry();
        if (entr != null) {
          for (int j = 0; j < entr.length; j++) {
            EntryData oldEnt = (EntryData)map.get(entr[j].getCeid());
            if ( m_pvm == PVM_EVENTS && entr[j].getEvent() != null) {
              if ( oldEnt == null || (oldEnt!=null && !oldEnt.getOrganizer().getCuid().equals(user) && entr[j].getOrganizer().getCuid().equals(user) ) ) {
                entr[j].putA("calid", cals[i].getCalid());
                map.put(entr[j].getCeid(), entr[j]);
              }
            }
            if (m_pvm == PVM_TODOS && entr[j].getTodo() != null && (entr[j].getTodo().getCompletion() == null || (entr[j].getTodo().getCompletion() != null &&  entr[j].getTodo().getCompletion().getCompleted() == null))) {
              entr[j].putA("calid", cals[i].getCalid());
              map.put(entr[j].getCeid(), entr[j]);
            }
          }
        }
      }
      Vector entv = new Vector();
      entv.addAll(map.values());
      CalendarServer.sort(entv);
      XMLData[] viewr = new XMLData[entv.size()];
      for (int i = 0; i < viewr.length; i++) {
        viewr[i] = new XMLData();
        EntryData ent = (EntryData)entv.elementAt(i);
        viewr[i].putA("ceid", ent.getCeid());
        viewr[i].putA("title", ent.getTitle());
        viewr[i].putA("priority", ent.getEvent()!= null ? ent.getEvent().getPriority().toString() : (ent.getTodo()!= null ? ent.getTodo().getPriority().toString() : ""));
        viewr[i].putA("start", ent.getDuration().getStart());
        if (!ent.getDuration().getLength().getAllDay()) {
          if (ent.getDuration().getStart().before(ent.getDuration().getEnd()))
            viewr[i].putA("end", ent.getDuration().getEnd());
        } else
          viewr[i].putA("length", "all-day");

        viewr[i].putA("calid", ent.getA("calid"));
      }
      XMLData view = new XMLData();

      if (m_pvm == PVM_EVENTS)
        view.putE("event", viewr);
      else if (m_pvm == PVM_TODOS)
        view.putE("todo", viewr);

      return view;
    } else
      return null;
  }

  public Date[] getRange(int before, int after) {
    Date[] range = new Date[2];
    Calendar cal = Timetable.getCalendar();
    Calendar pre = (Calendar)cal.clone();
    pre.add(Calendar.DATE, - before);
    range[0] = pre.getTime();
    Calendar next = (Calendar)cal.clone();
    next.add(Calendar.DATE, after);
    range[1] = next.getTime();
    return range;
  }

  CalendarData[] getCompositeCalendars(CalendarData[] cals, String groupid, Date[] range) throws Exception {
    CalendarData[] compCals = null;
    String s = m_channel.getUserData(CCalendar.USERDATA_KEY);
    if (s != null) {
      // Parse xml
      XMLData data = new XMLData();
      data.parse( new ByteArrayInputStream(s.getBytes()));

      // Get id of composite
      Object[] gcr = data.rgetE("calendar-group");
      XMLData caldata = null;
      if( gcr != null && gcr.length > 0) {
        Vector v = new Vector();
        for (int i = 0; i < gcr.length; i++) {
          String id = (String)((XMLData)gcr[i]).getA("id");
          log("composite. id ********** *******" + id);
          if (groupid.substring(10).equals(id))
            caldata = (XMLData)gcr[i];
        }
      }

      // Get calid of composite
      Object[] cr = caldata != null ? caldata.rgetE("calendar") : null;
      if( cr != null && cr.length > 0) {
        Vector v = new Vector();
        for (int i = 0; i < cr.length; i++) {
          String calid = (String)((XMLData)cr[i]).getA("calid");
          log("composite.calid ********** *******" + calid);
          CalendarData c = CalendarData.findCalendar(cals,calid);
          if( c != null)
            v.addElement(c);
          else
            log("Composite: not found calendar:"+calid);
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

    if (compCals!= null && cals != null) {
      for (int i = 0; i < compCals.length; i++) {
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


    return compCals;
    //return cals;
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

  /*
  CalendarData[] getCompositeCalendars(CalendarData[] cals, Date[] range) throws Exception {
    CalendarData[] compCals = null;
    String s = m_channel.getUserData(CCalendar.USERDATA_KEY);
    if (s != null) {
      // Parse xml
      XMLData data = new XMLData();
      data.parse( new ByteArrayInputStream(s.getBytes()));

      // Get calid of composite
      Object[] cr = data.rgetE("calendar");
      if( cr != null && cr.length > 0) {
        Vector v = new Vector();
        for (int i = 0; i < cr.length; i++) {
          String calid = (String)((XMLData)cr[i]).getA("calid");
          //log("composite.calid ********** *******" + calid);
          CalendarData c = CalendarData.findCalendar(cals,calid);
          if( c != null)
            v.addElement(c);
          //else log("Composite: not found calendar:"+calid);
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
    return compCals;
  }
  */

  void savePerInfo(String calid) throws Exception {
    XMLData udata = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
    if (udata != null) {
      udata.putE("peephole-calid", calid);
      m_channel.setUserProfile(udata, CCalendar.USERDATA_KEY);
    }
  }

}
