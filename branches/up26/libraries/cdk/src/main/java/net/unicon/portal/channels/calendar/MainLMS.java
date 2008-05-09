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



import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.unicon.academus.apps.calendar.CalendarData;
import net.unicon.academus.apps.calendar.CompletionData;
import net.unicon.academus.apps.calendar.DurationData;
import net.unicon.academus.apps.calendar.EntryData;
import net.unicon.academus.apps.calendar.EntryRange;
import net.unicon.academus.apps.calendar.EventData;
import net.unicon.academus.apps.calendar.TimeLength;
import net.unicon.academus.apps.calendar.TodoData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;



/**

 * This class represents main screen of Learning Unicon

 */



public class MainLMS extends CalendarScreen
{

  XMLData m_data = new XMLData();

  XMLData m_view= new XMLData();



  //CalendarView m_mini = new CalendarView("daily");

  CalendarView m_monthly = new CalendarView("monthly");

  CalendarView m_detail = new CalendarView("daily");

  CalendarView m_daily = new CalendarView("daily");

  EntryView m_entry = new EntryView();



  String m_calid = null;

  String m_window = "event";

  String m_main = "daily";

  int m_week = 1;



  // Truyen 03/21/2002

  /**

   * Get identifer of the screen

   * @return

   */

  public String sid()
  {

    return "MainLMS";

  }



  public boolean canRenderAsPeephole()
  {

    return true;

  }



  /**

   * Get parameters of XSL

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



  /**

   * Re-initialize for the screen

   * @param params

   * @throws Exception

   */

  public void reinit(Hashtable params) throws Exception
  {

    //m_data.putE("calendar",null);

    update(params);

  }



  /**

   * Get xml-based data of the screen

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
    else {

      if( m_window.equals("event"))

        cals = getServer().fetchEvents(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, range[0], range[1]);

      else if( m_window.equals("todo"))

        cals = getServer().fetchTodos(new CalendarData[]{CalendarData.findCalendar(cals,m_calid)}, range[0], range[1]);

      else

        cals = getServer().fetchEntries(new CalendarData[] {
                                          CalendarData.findCalendar(cals,m_calid)
                                        }
                                        , range[0], range[1]);

      cals = CalendarData.replaceCal(getAllCalendars(), cals[0]);

    }



    m_data.putE("calendar", cals);

    // view calendar id

    ((CalendarData)m_view.getE("calendar")).putCalid(m_calid);



    // Daily view mode

    String mode = m_detail.getMode();

    if (mode.equals("daily"))
    {

      //Truyen 04/22/2002

      Vector calv = new Vector();

      Timetable layout = new Timetable();

      if (m_calid.equals("all-calendars"))

        layout.layout(m_detail.getTime(), cals);

      else
      {

        EntryData[] ent = CalendarData.findCalendar(cals, m_calid).getEntry();

        layout.layout(m_detail.getTime(), ent);

      }

      layout.build(m_detail);

    }



    // Detail-monthly, Detail-daily, detail-weekly

    m_data.putE("mini-entry", m_entry);

    m_data.putE("mini-monthly", m_monthly);

    m_data.putE("detail-monthly",null);

    m_data.putE("detail-daily",null);

    m_data.putE("detail-weekly",null);

    m_data.putE("detail-"+mode, m_detail);

    m_data.putE("mini-daily", m_daily);



    return m_data;

  }



  /**

   * Initialize the screen

   * @param params

   * @throws Exception

   */

  public void init(Hashtable params) throws Exception
  {

    super.init(params);
    // unicon specific logic

    //if ((String)params.get("focusedChannel") != null && m_detail.getMode() == null);

    //m_detail.setMode("daily");

    // end unicon specific



    // Get parameters : calendar id and op

    String calid = params.containsKey("calid")?(String)params.get("calid"):m_calid;

    // If init from toolbar, don't care about calid

    if (m_calid == null)

      m_calid = validateCalid(calid);

    String op = (String)params.get("op");

    m_window = params.get("window")!= null?(String)params.get("window"): m_window;

    // View mode: Monthly or Daily

    if (op == null)
      ;

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

  }



  /**

   * Get name of XSL file

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

   * Process "change view Event or Todo" action

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



    if (op == null)
      ;

    // View mode (v:monthly,v:weekly,v:daily)

    else if (op.startsWith("v~"))

      if (!m_detail.getMode().equals(op.substring(2)))

        m_detail.setMode(op.substring(2));



    return this;

  }



  /**

   * Process "Change view mode and time range" action

   * @param params

   * @return

   * @throws Exception

   */

  public Screen update(Hashtable params) throws Exception
  {

    // Get input

    log(" param " + params);

    String calid = (String)params.get("calid");

    // return if choose -----

    if (calid != null && calid.length() == 0)
      return this;



    String op = (String)params.get("op");


    //--------- Make changes based on "op" ------------

    if (calid != null){

      m_calid = validateCalid(calid);

      getData();
    }

    // View mode (v:monthly,v:weekly,v:daily)

    if (op == null)
      ;



    else if (op.startsWith("v~"))
    {

      m_detail.setMode(op.substring(2));

      Calendar cal = Calendar.getInstance();

      cal.setTime(m_detail.getTime());

      m_week = m_detail.weekOfDate(m_detail.dayOfWeek(1, cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)));

      m_detail.putA("week", Integer.toString(m_week));

    }


    else if (op.startsWith("w~"))
    {

      m_detail.setMode("weekly");

      m_detail.setTime(op.substring(2));

      m_daily.setTime(op.substring(2));

    }



    // Detail of specific month

    else if (op.startsWith("m~"))
    {

      m_detail.setMode("monthly");

      m_detail.setTime(op.substring(2));
      m_daily.setTime(op.substring(2));

    }



    // Detail of specific day

    else if (op.startsWith("d~"))
    {

      m_detail.setMode("daily");

      m_detail.setTime(op.substring(2));

      m_daily.setTime(op.substring(2));
      
      m_monthly.setTime(op.substring(2));

    }



    return this;

  }



  //Truyen 03/24/2002

  /**

   * Process "Change month/year" action

   * @param params

   * @return

   * @throws Exception

   */

  public Screen changeDate(Hashtable params)throws Exception
  {

    String month = (String)params.get("month");

    String day = (String)params.get("day");

    String year = (String)params.get("year");

    String week = (String)params.get("week");



    /*

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

    */





    if (day != null)
    {

      m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");

    }
    else
    {

      m_monthly.setTime(month + "/1/" + year + "_00:00");

      m_detail.setTime(month + "/1/" + year + "_00:00");

      m_daily.setTime(month + "/1/" + year + "_00:00");

    }



    return this;

  }


/*

    AW 8/24/05:  Removed in favor of the impl below.

  public Screen nextDate(Hashtable params)throws Exception
  {
    int tmpMonth;
    int tmpDay;
    int tmpYear;


    String month = null;
    String year= null;
    String day = null;

    if (params.get("day") != null)
    {
    tmpDay = Integer.parseInt((String)params.get("day")) + 1;
    day = String.valueOf(tmpDay);
    year = (String)params.get("year");
    month = (String)params.get("month");
    m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");
    }
    else
    {
    tmpMonth = Integer.parseInt((String)params.get("month")) + 1;
    month = String.valueOf(tmpMonth);
    day = (String)params.get("day");
    year = (String)params.get("year");
    m_monthly.setTime(month + "/1/" + year + "_00:00");

    m_detail.setTime(month + "/1/" + year + "_00:00");

    m_daily.setTime(month + "/1/" + year + "_00:00");

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

    if (params.get("day") != null)
    {
    tmpDay = Integer.parseInt((String)params.get("day")) - 1;
    day = String.valueOf(tmpDay);
    year = (String)params.get("year");
    month = (String)params.get("month");
    m_daily.setTime(month + "/"+ day +"/" + year + "_00:00");
    }
    else
    {
    tmpMonth = Integer.parseInt((String)params.get("month")) - 1;
    month = String.valueOf(tmpMonth);
    day = (String)params.get("day");
    year = (String)params.get("year");
    m_monthly.setTime(month + "/1/" + year + "_00:00");

    m_detail.setTime(month + "/1/" + year + "_00:00");

    m_daily.setTime(month + "/1/" + year + "_00:00");
    }

    return this;
  }
*/

  public Screen nextDate(Hashtable params)throws Exception
  {

    Calendar cndr = Calendar.getInstance();
    cndr.setTime(m_detail.getTime());

    if (m_detail.getMode().equals("weekly")) {
        cndr.add(Calendar.DAY_OF_YEAR, 7);
    } else {
        // NB:  Monthly & Daily views do the same thing...
        cndr.add(Calendar.MONTH, 1);
    }

    StringBuffer tme = new StringBuffer();
    tme.append(String.valueOf(cndr.get(Calendar.MONTH) + 1)).append("/")    // NB:  java.util.Calendar uses 0-11 for months...
            .append(String.valueOf(cndr.get(Calendar.DAY_OF_MONTH))).append("/")
            .append(String.valueOf(cndr.get(Calendar.YEAR))).append("_00:00");

// System.out.println("tme: " + tme.toString());

    m_detail.setTime(tme.toString());
    m_daily.setTime(tme.toString());
    m_monthly.setTime(tme.toString());
    m_detail.putA("week", String.valueOf(cndr.get(Calendar.WEEK_OF_YEAR))); // NB:  The need for this is positively insipid...

    return this;

  }

  public Screen previousDate(Hashtable params)throws Exception
  {

    Calendar cndr = Calendar.getInstance();
    cndr.setTime(m_detail.getTime());

    if (m_detail.getMode().equals("weekly")) {
        cndr.add(Calendar.DAY_OF_YEAR, -7);
    } else {
        // NB:  Monthly & Daily views do the same thing...
        cndr.add(Calendar.MONTH, -1);
    }

    StringBuffer tme = new StringBuffer();
    tme.append(String.valueOf(cndr.get(Calendar.MONTH) + 1)).append("/")    // NB:  java.util.Calendar uses 0-11 for months...
            .append(String.valueOf(cndr.get(Calendar.DAY_OF_MONTH))).append("/")
            .append(String.valueOf(cndr.get(Calendar.YEAR))).append("_00:00");

// System.out.println("tme: " + tme.toString());

    m_detail.setTime(tme.toString());
    m_daily.setTime(tme.toString());
    m_monthly.setTime(tme.toString());
    m_detail.putA("week", String.valueOf(cndr.get(Calendar.WEEK_OF_YEAR))); // NB:  The need for this is positively insipid...

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

   * Process "Complete todo" action

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



  String validateCalid(String calid) throws Exception
  {

    if (calid == null || CalendarData.findCalendar((CalendarData[])m_data.getE("calendar"),calid) == null)

      calid = calid != null && calid.equals("all-calendars") ? calid : getServer().getUser();



    return calid;

  }



  //----------------------------------------------------------------------------//







  //----------------------------------------------------------------------------//



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

