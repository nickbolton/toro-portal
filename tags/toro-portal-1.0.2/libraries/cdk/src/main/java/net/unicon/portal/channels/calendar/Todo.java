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
import java.text.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.*;

/**
 * This class represents todo screen.
 */
public class Todo extends CalendarScreen
{
  EntryView m_entryView = new EntryView();
  XMLData m_view= new XMLData();
  String m_calid = null;
  XMLData m_calview = new CalendarData();
  XMLData m_data = new XMLData();
  String m_back = null;

  // For display pages
  EntryData[] m_entries = null;//array entry repeat
  int m_nPages = -1; // number of pages
  int m_curPage = -1;
  final int MAX_NUM = 10;

  /**
   * Make a copy of the screen
   * @return
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException
  {
    Todo clone = (Todo)super.clone();
    clone.m_entryView = (EntryView)m_entryView.clone();
    return clone;
  }

  // truyen 03/21/2002
  /**
   * Get identifier of the screen
   * @return
   */
  public String sid(){
    return "Todo";
  }

  /**
   * Get parameters of XSL
   * @return
   */
  public Hashtable getXSLTParams()
  {
    Hashtable params = super.getXSLTParams();
    params.put("back", m_back);
    return params;
  }
  //---------end-----------

  /**
   * Get xml-based data
   * @return
   * @throws Exception
   */
  public XMLData getData() throws Exception
  {
    return m_data;
  }

  //Truyen 03/26/2002
  /**
   * Re-initialize the screen
   * @param params
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception
  {
      init(params);
  }

  void initTodo( EntryData[] all, String ceid) throws Exception
  {
    // find this
    EntryData ent = null;
    for( int i = 0; i < all.length && ent==null; i++) {
      if( all[i].getCeid().equals(ceid))
        ent = all[i];
    }

    if( ent != null) {
      // Alarm
      AlarmData[] alrmr = ent.getAlarm();
      if (alrmr != null && alrmr.length > 0)
        m_entryView.putA("alarm-trigger", new Long((ent.getDuration().getStart().getTime()-alrmr[0].getTrigger().getTime())/1000));
      m_entryView.putCeid(ceid);

      // Prepare display page
      if(ent.isRecurrences())
      {
        m_entries = all;
        m_nPages = m_entries.length / MAX_NUM;
        if (m_entries.length % MAX_NUM > 0) m_nPages++;
        // Truong 0123
        if (m_curPage > 0 && m_curPage < m_nPages) setPage(m_curPage);
        else if (m_curPage > 0 && m_curPage > m_nPages) setPage(m_nPages);
        else setPage(m_nPages > 0? 0: -1);
        //
      }
    }
  }

  /**
   * Initialize the screen
   * @param params
   * @throws Exception
   */
  public void init(Hashtable params) throws Exception
  {

    super.init(params);
    String ceid = (String)params.get("ceid");
    m_back = params.containsKey("back")?(String)params.get("back"): m_back;
    if (m_back == null)
      m_back = "Peephole";

    m_calid = (String)params.get("calid");
    m_view.putA("back",m_back);
    //Truong 14_8
    m_calview.putA("calid",m_calid);
    // get calendars
    CalendarData[] cals = getServer().getCalendars(null);
    m_data.putE("view",m_view);
    m_view.putE("entry",m_entryView);
    m_view.putE("calendar",m_calview);

    // Edit
    if (ceid != null)
    {
      CalendarData one = (CalendarData)CalendarData.findCalendar(cals,m_calid);
      m_data.putE("calendar", new CalendarData[] {one});
      EntryData[] all = getServer().fetchTodosByIds(m_calid,
                        new EntryRange[] {new EntryRange( ceid, EntryRange.ALL)});
      if (all != null)
      {
        one.putEntry(all);
        initTodo(all, ceid);
      }
      else m_view.removeE("entry");
    }

    // Create new
    else {
      m_data.putE("calendar", cals);
      m_entryView.removeA("ceid");
    }

    String date = (String)getParameter("date");
    if (date != null)
      m_entryView.putDate(date);
    String hour = (String)getParameter("hour");
    if (hour != null)
      m_entryView.putHour(hour);
  }

  /**
   * Process "OK" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception
  {
    String err = null;
    EntryData ent = new EntryData();
    DurationData dur = new DurationData();
    Calendar due = Timetable.getCalendar();
    m_calid = params.get("calid") != null?(String)params.get("calid"): m_calid;
    m_calview.putA("calid",m_calid);
    m_view.putE("calendar",m_calview);

    due.set(Calendar.MONTH, Integer.parseInt((String)params.get("month"))-1);
    due.set(Calendar.DATE, Integer.parseInt((String)params.get("day")));
    due.set(Calendar.YEAR, Integer.parseInt((String)params.get("year")));

    due.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String)params.get("hour")));
    due.set(Calendar.MINUTE, Integer.parseInt((String)params.get("minute")));

    String loc = ((String)params.get("place"));
    if (loc != null && loc.length() > 0)
      ent.putLocation(loc);
    ent.putShare((String)params.get("share"));

    TodoData td = new TodoData();
    td.putDue(due.getTime());
    String title = ((String)params.get("todo")).trim();
    if (title.length() == 0)
      err = new String(CCalendar.ERROR_NO_TODO_TITLE);
    td.put(Event.enc(title));

    td.putPriority(new Integer((String)params.get("priority")));
    String desc = ((String)params.get("description")).trim();

    if (desc.length() > 0)
      td.putDescription(desc);

    ent.putTodo(td);

    //Truyen 04/05/2002
    EntryData oldEnt = null;
    if (m_entryView.getCeid() != null)
      oldEnt = getServer().fetchTodosByIds(m_calid,new EntryRange[]{new EntryRange(m_entryView.getCeid())})[0];
    //repeating
    try{
      if (params.get("freq")!=null && !((String)params.get("freq")).equals("0"))
      {

        if (m_entryView.getCeid() != null && oldEnt.getRecurrence() != null &&
            oldEnt.getRecurrence().length > 0)
          ent.putRecurrence(oldEnt.getRecurrence());
        else
        {
          String byDate = "";
          int intr = 1;
          int times = 1;
          String freq = null;
          switch(Integer.parseInt((String)params.get("freq")))
          {
            case 1: // repeat every .. days
              freq = "DAILY";
              intr = Integer.parseInt((String)params.get("days"));
              break;
            case 2: // repeat every .. weeks
              freq = "WEEKLY";
              intr = Integer.parseInt((String)params.get("weeks"));
              if (CCalendar.SERVER_NAME.equals(CCalendar.DPCS_SERVER))
              {
                if(params.containsKey("sun"))
                  byDate += "1";
                if(params.containsKey("mon"))
                  byDate += "2";
                if(params.containsKey("tue"))
                  byDate += "3";
                if(params.containsKey("wed"))
                  byDate += "4";
                if(params.containsKey("thu"))
                  byDate += "5";
                if(params.containsKey("fri"))
                  byDate += "6";
                if(params.containsKey("sat"))
                  byDate += "7";
              }
              else
              {
                if(params.containsKey("mon"))
                  byDate += "SU,";
                if(params.containsKey("tue"))
                  byDate += "MO,";
                if(params.containsKey("wed"))
                  byDate += "TU,";
                if(params.containsKey("thu"))
                  byDate += "WE,";
                if(params.containsKey("fri"))
                  byDate += "TH,";
                if(params.containsKey("sat"))
                  byDate += "FR,";
                if(params.containsKey("sun"))
                  byDate += "SA,";
                if (byDate.endsWith(","))
                  byDate = byDate.substring(0, byDate.length()-1) ;
              }
              break;
            case 3: // repeat every .. months
              freq = "MONTHLY";
              intr = Integer.parseInt((String)params.get("months"));
              break;
            case 4: // repeat every year
              freq = "YEARLY";
              intr = 1;
          }
          RecurrenceData rec = new RecurrenceData();
          if (params.get("until")!=null)
          {
            if(((String)params.get("until")).equals("0"))
              rec.putRule(freq, new Integer(intr), new Integer((String)params.get("times")));
            else
            {
              Calendar until = (Calendar)due.clone();
              until.set(Calendar.MONTH, Integer.parseInt((String)params.get("month2"))-1);
              until.set(Calendar.DATE, Integer.parseInt((String)params.get("day2")));
              until.set(Calendar.YEAR, Integer.parseInt((String)params.get("year2")));
              rec.putRule(freq, new Integer(intr), until.getTime());
            }
            if (freq.equals("WEEKLY") && byDate != null && byDate.length() > 0)
              rec.putByDay(byDate);
          }

          RecurrenceData[] recr = {rec};
          ent.putRecurrence(recr);
        }
      }
      else
        ent.putRecurrence(null);
    }catch(Exception ex){

      err = new String(CCalendar.ERROR_INPUT_REPEAT);
    }

    if (params.get("remind") != null)
    {
      String email = ((String)params.get("email")).trim();
      if (email.length() > 0)
      {
        AlarmData alrm = new AlarmData();
        Calendar trig = (Calendar)due.clone();
        trig.add(Calendar.SECOND, -1*Integer.parseInt((String)params.get("trigger")));
        alrm.putTrigger(trig.getTime());
        String[] reir = {email};
        alrm.putRecipient(reir);
        AlarmData[] alrmr = {alrm};
        ent.putAlarm(alrmr);
      }
    }else
    {
      if (m_entryView.getCeid() != null)
      {
//        EntryData oldEnt = getServer().getEntry(m_calid, m_entryView.getCeid());
        if (oldEnt.getAlarm() != null && oldEnt.getAlarm().length > 0)
          ent.putAlarm(new AlarmData[0]);
        else
          ent.putAlarm(null);
      }
    }

    CompletionData cd = null;
    if (params.containsKey("complete"))
      cd = new CompletionData(due.getTime(), new Integer(100));

    td.putCompletion(cd);

    m_back = (String)params.get("back"); // Thach July 17
    String ceid = m_entryView.getCeid();
    if (ent.getRecurrence()!=null && ent.getRecurrence()[0].getUntil() != null && ent.getRecurrence().length > 0 &&
      ent.getTodo().getDue().after(
        Timetable.lastDayTime(ent.getRecurrence()[0].getUntil()).getTime()))
      {
        err = new String(CCalendar.ERROR_INPUT_UNTILDATE);
      }

    if (err != null)
      return error(err);

    try
    {
      if (ceid == null)
        getServer().createEntry(m_calid, ent);
      else
      {
        //EntryData oldEnt = getServer().getEntry(m_calid, ceid);
        int mod = 0;
        if (oldEnt != null)
        {
//          oldEnt = getServer().getEntry(m_calid, ceid);
          if (oldEnt.getLocation() != null && oldEnt.getLocation().length() > 0 &&
            ent.getLocation() == null)
            ent.putLocation("");

          if (oldEnt.getTodo().getDescription() != null && oldEnt.getTodo().getDescription().length() > 0 &&
            ent.getTodo().getDescription() == null)
            ent.getTodo().putDescription("");


          if ((oldEnt.getRecurrence() == null || oldEnt.getRecurrence().length == 0)
            && ent.getRecurrence() != null && ent.getRecurrence().length > 0)
            mod = EntryRange.FUTURE;

          if (params.get("future") != null)
            mod = EntryRange.FUTURE;
        }
        else
          mod = params.get("future") == null? 0:EntryRange.FUTURE;

        getServer().updateEntry(m_calid, new EntryRange(ceid, mod), ent);
      }
    }
    catch (Exception e)
    {
      m_channel.log(e);
      return error(CCalendar.ERROR_TODO_FAILED_TO_CREATE, new Object[]
                   {getServer().getError()}, m_back != null? m_back:"Main");
    }
    
    if (ceid == null)
      return info(CCalendar.MSG_TODO_ADDED, new Object[]
                  {title},m_back != null? m_back:"Main", true);
    else
      return info(CCalendar.MSG_TODO_UPDATED, new Object[]
                  {title},m_back != null? m_back:"Main", true);
    /*
    Main main = (Main)getScreen("Main") != null ? (Main)getScreen("Main") : (Main)makeScreen("Main");
    main.m_window = "todo";

    return main;*/
  }

  /**
   * Process "View Repeat" action
   * @param params
   * @return
   * @throws Exception
   */
  public Screen viewRepeat(Hashtable params) throws Exception
  {
    // Input params
    String ceid = (String)params.get("ceid");

    // get calendar
    CalendarData[] cals = (CalendarData[])m_data.getE("calendar");
    EntryData[] all = cals[0].getEntry();
    initTodo(all, ceid);
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

  /**
   * Set page to show the results
   * @param p
   * @throws Exception
   */
  public void setPage(int p) throws Exception
  {
    m_curPage = p;
    m_view.putA("prev",new Boolean(m_curPage > 0));
    m_view.putA("next",new Boolean(m_curPage < m_nPages - 1));
    m_view.putA("pages",new Integer(m_nPages));
    m_view.putA("page",new Integer(m_curPage));
    if (m_curPage < 0 || m_curPage >= m_nPages || m_nPages < 0) return;
    // Start index of event and len of page display
    int start = p * MAX_NUM;
    int len = (start + MAX_NUM > m_entries.length)? m_entries.length - start: MAX_NUM;

    // Extract entries to calendar page
    XMLData[] viewData = new XMLData[len];
    for (int i = 0; i < len; i++)
    {
      EntryData viewEn = m_entries[start+i];
      viewData[i] = new XMLData();
      // put ceid to "view" once if there are many
      viewData[i].putA("ceid", viewEn.getCeid());
    }
    m_calview.putE("entry",viewData);

  }

}
