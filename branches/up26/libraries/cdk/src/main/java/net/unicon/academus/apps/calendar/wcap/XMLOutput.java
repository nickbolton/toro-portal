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

package net.unicon.academus.apps.calendar.wcap;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;

/**
 * The object stores data is returned from iPlanet Calendar Server.
 */
public class XMLOutput {
  // Output flags
  public static final int OUT_NONE = 0;
  public static final int OUT_AUTHENTICATE = 1; // Session and error
  public static final int OUT_LOGIN = 2;

  public static final int OUT_CALENDAR_UPDATE = 10;
  public static final int OUT_CALENDAR_DELETE = 11;
  public static final int OUT_CALENDAR_CREATE = 12;
  public static final int OUT_CALENDAR_GETPROPS = 13;

  public static final int OUT_ENTRY_FETCH = 20;

  public static final int OUT_INVITATION_FETCH = 30;
  public static final int OUT_INVITATION_UPDATE = 31;
  public static final int OUT_INVITATION_REPLY = 32;
  public static final int OUT_INVITATION_COUNT = 33;

  public static final int OUT_EVENT_FETCH = 40;
  public static final int OUT_EVENT_DELETE = 41;
  public static final int OUT_EVENT_UPDATE = 42;

  public static final int OUT_TODO_UPDATE = 50;
  public static final int OUT_TODO_DELETE = 51;
  public static final int OUT_TODO_FETCH = 52;

  Node m_root = null;
  String m_session = null;
  String m_error;
  String m_user;
  int m_outFlags = 0;

  /**
   * Constructor with an inputstream, user and outputFlags
   * @param is
   * @param user
   * @param outputFlags
   * @throws Exception
   */
  public XMLOutput(InputStream is, String user, int outputFlags) throws Exception {
    m_root = parseToNode(is);
    m_user = user;
    m_outFlags = outputFlags;
  }

  /**
   * Get session
   * @return
   */
  public String getSession() {
    return m_session;
  }

  /**
   * Get string error.
   * @return
   */
  public String getError() {
    return m_error;
  }

  /**
   * Get a globally unique identifier
   * @return
   * @throws Exception
   */
  public String getGUID() throws Exception {
    NodeList nodes = m_root.getChildNodes();
    if (nodes.item(1).getChildNodes().item(1).getNodeName().equals("X-NSCP-GUID0"))
      return readText(nodes.item(1).getChildNodes().item(1) );
    else
      return null;
  }

  /**
   * Read calendars
   * @return Array of CalendarData object
   * @throws Exception
   * This method reads data from m_root
   */
  public CalendarData[] readCalendars() throws Exception {
    Vector calv = new Vector();
    NodeList nodes = m_root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeName().equals("iCal"))
        calv.addElement(readCalendar(node));
    }

    // to array
    CalendarData[] calr = new CalendarData[calv.size()];
    for (int i = 0; i < calr.length; i++) {
      calr[i] = (CalendarData) calv.elementAt(i);
      //System.out.println("calr[i].getCalid() reacalendar "+ calr[i].getCalid());
    }


    return calr;
  }

  /**
   * Read a calendar from give node
   * @param node is calendar node (child of root)
   * @return
   * @throws Exception
   */
  public CalendarData readCalendar(Node node) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    CalendarData cal = new CalendarData();
    Vector acev = new Vector();
    Vector entv = new Vector();

    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      //System.out.println("node name " + name);
      //System.out.println("node text " + readText(node));
      //if( filter( name) == false) continue;

      if (name.equals("X-NSCP-WCAP-SESSION-ID"))
        m_session = readText(node);
      //else if (name.equals("X-NSCP-WCAP-CALENDAR-ID") || name.equals("X-NSCP-CALPROPS-RELATIVE-CALID")){
      else if (name.indexOf ("X-NSCP-WCAP-CALENDAR-ID") != -1 || name.equals("X-NSCP-CALPROPS-RELATIVE-CALID")) {
        if (cal.getCalid() == null)
          cal.putA("calid", readText(node));
        //System.out.println("readText(node) calid " + readText(node) );
      } else if (name.equals("X-NSCP-WCAP-CALENDAR-NAME") || name.equals("X-NSCP-CALPROPS-NAME"))
        cal.putA("calname", readText(node));
      else if (name.equals("X-NSCP-CALPROPS-PRIMARY-OWNER"))
        cal.putA("owner", readText(node));
      else if (name.equals("X-NSCP-CALPROPS-ACCESS-CONTROL-ENTRY"))
        acev.addElement(readText(node));
      else if (name.equals("EVENT")) {
        EntryData et = readEntry(cal, node,"event");
        if (et.isEvent())
          entv.addElement(et);
      } else if (name.equals("TODO"))
        entv.addElement(readEntry(cal, node, "todo"));
      else if (name.equals("X-NSCP-CALPROPS-CREATED"))
        cal.putA("created", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("X-NSCP-CALPROPS-LAST-MODIFIED"))
        cal.putA("last-modified", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("X-NSCP-WCAP-GUID0") || name.equals("X-NSCP-GUID0"))
        cal.putA("GUID0", readText(node));
    }

    //System.out.println("calname "+ cal.getCalname() + " calid " + cal.getCalid());


    //-----------------------------
    //----- Validate our format ---
    //-----------------------------
    //-- Calendar name
    if (cal.getCalname() == null)
      cal.putCalname(cal.getCalid()!=null?cal.getCalid():"");

    //System.out.println("calname "+ cal.getCalname() + " calid " + cal.getCalid());
    //-- ACE list
    if (acev !=null && acev.size() > 0) {
      //-- Here ACE is in iCS 5.0 format: user^what^how^grant
      //-- We must transform to our format
      Vector acl = new Vector();
      for (int i = 0; i < acev.size(); i++) {
        String aceStr = (String)acev.elementAt(i);
        aceStr = aceStr.toLowerCase();

        String[] tokens = getTokens(aceStr);//Tien 0731
        if (tokens == null)
          continue;

        String user = tokens[0];
        ACE value = findACE(acl, user);
        if (value == null) {
          value = new ACE();
          acl.addElement(value);
        }
        value.setRawACE(tokens);
      }

      // to array of ACEData
      int i = 0;
      ACEData[] acer = new ACEData[acl.size()];
      for (i = 0; i < acl.size(); i++) {
        ACE ace = (ACE)acl.elementAt(i);
        ACEData data = new ACEData();
        data.putEntityType("p");//Campus data
        data.putOID(ace.m_user);
        data.putCuid(ace.m_user);
        data.putName(ace.m_user); //truyen 15/05/02
        data.putFreebusy(new Boolean(ace.getFreebusy()));
        data.putSchedule(new Boolean(ace.getSchedule()));
        data.putRead(new Boolean(ace.getRead()));
        data.putWrite(new Boolean(ace.getWrite()));
        acer[i] = data;
      }
      cal.putE("ace", acer);
    } else
      cal.putE("ace",null);

    ///////////
    CalendarServer.sort(entv);
    EntryData[] entr = new EntryData[entv.size()];
    for (int i = 0; i < entr.length; i++)
      entr[i] = (EntryData)entv.elementAt(i);

    cal.putE("entry", entr);

    return cal;
  }

  boolean filterEntry( String name) {
    return true;
  }

  boolean filter( String name) {
    switch( m_outFlags) {
    case OUT_NONE:
      return true;
    case OUT_AUTHENTICATE:
      return (name.equals("X-NSCP-WCAP-SESSION-ID") ||
              name.equals("RSTATUS") || name.equals("X-NSCP-WCAP-ERRNO"));

    case OUT_LOGIN: // No entries here ( in the xml from iCS)
    case OUT_CALENDAR_UPDATE:
    case OUT_CALENDAR_DELETE:
    case OUT_CALENDAR_CREATE:
    case OUT_CALENDAR_GETPROPS:
      return true;

    case OUT_ENTRY_FETCH:
      return true;

    case OUT_INVITATION_FETCH:
    case OUT_INVITATION_UPDATE:
    case OUT_INVITATION_REPLY:
      return !name.equals("TODO");

    case OUT_EVENT_FETCH:
    case OUT_EVENT_DELETE:
    case OUT_EVENT_UPDATE:
      return true;

    case OUT_TODO_UPDATE:
    case OUT_TODO_DELETE:
    case OUT_TODO_FETCH:
      return true;
    default:
      return true;
    }
  }

  /**
   * Search event
   * @param cals
   * @param text
   * @param category
   * @param lookin
   * @throws Exception
   */
  public void searchEvent(CalendarData[] cals, String text, String category, int lookin) throws Exception {
    // Travel each calendar node looking for search conditions
    NodeList list = m_root.getChildNodes();
    for (int i=0; i < list.getLength(); i++) {
      Node calNode = list.item(i);
      if (calNode.getNodeName().equals("iCal")) {
        // Find event on this calendar node
        CalendarData cal = searchEvent(calNode,text,category,lookin);

        // Update to output
        CalendarData output = CalendarData.findCalendar(cals, cal.getCalid());
        if (output != null)
          output.putEntry(cal.getEntry());
      }
    }
  }

  /**
   * @param node is calendar node
   * @return CalendarData with calid and event entries
   */
  CalendarData searchEvent(Node node, String text, String category, int lookin) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    CalendarData cal = new CalendarData();
    Vector entv = new Vector();

    String calid = "";
    String owner = "";

    EntryData ent = null;
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      if( filter( name) == false)
        continue;

      if (name.equals("X-NSCP-WCAP-SESSION-ID"))
        m_session = readText(node);
      else if (name.equals("X-NSCP-WCAP-CALENDAR-ID") || name.equals("X-NSCP-CALPROPS-RELATIVE-CALID")) {
        cal.putA("calid", readText(node));
        calid = readText(node);
      } else if (name.equals("X-NSCP-CALPROPS-PRIMARY-OWNER"))
        cal.putA("owner", readText(node));

      else if (name.equals("EVENT") && ((ent = readEntry(cal, node,"event")) != null) && ent.isEvent()) {
        String title = (ent.getTitle()==null)?"":ent.getTitle().toLowerCase();
        String desc = (ent.getEvent().getDescription()==null)?"":ent.getEvent().getDescription().toLowerCase();
        String loca = (ent.getLocation()==null)?"":ent.getLocation().toLowerCase();
        String textlower = text==null?"":text.toLowerCase();
        boolean match1 = false;

        if ((lookin & CalendarServer.LOOK_IN_TITLE) > 0 && title.indexOf(textlower) >= 0)
          match1 = true;
        if ((lookin & CalendarServer.LOOK_IN_NOTES) > 0 && desc.indexOf(textlower) >= 0)
          match1 = true;
        if ((lookin & CalendarServer.LOOK_IN_PLACE) > 0 && loca.indexOf(textlower) >= 0)
          match1 = true;

        if (match1 && (category == null || (ent.getEvent().getCategory() != null &&
                                            ent.getEvent().getCategory().length > 0 &&
                                            union(ent.getEvent().getCategory(),category))))
          entv.addElement(ent);
      }
    }

    // Sort event and convert to array
    CalendarServer.sort(entv);
    EntryData[] entr = new EntryData[entv.size()];
    for (int i = 0; i < entr.length; i++)
      entr[i] = (EntryData)entv.elementAt(i);

    // Sava on output
    cal.putE("entry", entr);
    return cal;
  }

  /**
   * Read invitations
   * @return Array of object EntryData
   * @throws Exception
   */
  public EntryData[] readInvitations() throws Exception {
    Vector inviv = new Vector();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    CalendarData cal = new CalendarData();
    NodeList nodes = m_root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node1 = nodes.item(i);
      if (node1.getNodeName().equals("iCal")) {
        NodeList nodes1 = node1.getChildNodes();
        for (int j = 0; j < nodes1.getLength(); j++) {
          Node node2 = nodes1.item(j);
          String name = node2.getNodeName();
          if( filter( name) == false)
            continue;

          if (name.equals("X-NSCP-WCAP-SESSION-ID"))
            m_session = readText(node2);
          else if (name.equals("X-NSCP-WCAP-CALENDAR-ID") || name.equals("X-NSCP-CALPROPS-RELATIVE-CALID"))
            cal.putA("calid", readText(node2));
          else if (name.equals("X-NSCP-CALPROPS-PRIMARY-OWNER"))
            cal.putA("owner", readText(node2));
          else if (name.equals("EVENT")) {
            EntryData et = readInvitation(node2);
            if (et != null)
              inviv.insertElementAt(et,binary(inviv,et,0,inviv.size()));
          }
        }
      }
    }
    EntryData[] edr = new EntryData[inviv.size()];
    for (int i=0; i < inviv.size(); i++)
      edr[i] = (EntryData)inviv.elementAt(i);
    return edr;
  }

  /**
   * Count number of invitations
   * @return
   * @throws Exception
   */
  public int countInvitations() throws Exception {
    int todo = 0;
    int total = 0;
    int count = 0;
    CalendarData cal = new CalendarData();
    NodeList nodes = m_root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node1 = nodes.item(i);
      if (node1.getNodeName().equals("iCal")) {
        NodeList nodes1 = node1.getChildNodes();
        for (int j = 0; j < nodes1.getLength(); j++) {
          Node node2 = nodes1.item(j);
          String name = node2.getNodeName();
          if (name.equals("X-NSCP-WCAP-SESSION-ID"))
            m_session = readText(node2);
          else if (name.equals("EVENT")) {
            total++;
            if( isInvitationEntry(node2))
              count++;
          } else if (name.equals("TODO")) {
            todo++;
          }
        }
      }
    }

    //System.out.println("*** inv="+count+"/"+total+", todo="+todo);

    return count;
  }

  boolean isInvitationEntry(Node node) throws Exception {
    // Organizer()!= null and attendee
    // Scan children
    OrganizerData org = null;
    AttendeeData att = null;
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength() && (org==null || att==null); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      if (name.equals("ORGANIZER")) {
        OrganizerData org1 = readOrganizer(node);
        if( org1 != null && org1.get() != null)
          org = org1;
      } else if (name.equals("ATTENDEE")) {
        AttendeeData attend = readAttendee(node);
        if (attend.get().equals(m_user))
          att = attend;
      }
    }

    // Verify invitation
    if (org != null && att != null) {
      String status = att.getStatus();
      return ( status != null && status.equals("NEEDS-ACTION"));
    }

    return false;
  }

  boolean isInvitation( EntryData ent, Vector attdv) {
    if ( ent.getOrganizer()!=null && ent.getOrganizer().get() != null) {
      for (int i = 0; i < attdv.size(); i++) {
        AttendeeData att = (AttendeeData)attdv.elementAt(i);
        if (att.get().equals(m_user)) {
          String status = att.getStatus();
          if (status != null && status.equals("NEEDS-ACTION"))
            return true;
        }
      }
    }
    return false;
  }


  EntryData readInvitation(Node node) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    EntryData ent = new EntryData();
    Vector attdv = new Vector();
    DurationData dur = new DurationData();
    Vector alv = new Vector();
    Vector recv = new Vector();
    Vector resv = new Vector();
    Vector relv = new Vector();
    CompletionData comp = new CompletionData(null,null);
    XMLData desc = null;
    desc = new EventData();

    // Scan children
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      if (name.equals("UID"))
        ent.putA("ceid", readText(node));
      else if (name.equals("RECURID"))
        ent.putA("ceid", ent.getA("ceid")+"."+readText(node));
      else if (name.equals("CLASS"))
        ent.putA("share", readText(node));
      else if (name.equals("START")) {
        String text = readText(node);
        try {
          dur.putA("start", sdf.parse(text+"GMT"));
          dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
        } catch (ParseException e) {
          dur.putA("start", (new SimpleDateFormat("yyyyMMdd")).parse(text));
          dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
          dur.putAllDay();
        }
      } else if (name.equals("END")) {
        String text = readText(node);
        try {
          dur.putEnd(sdf.parse(text+"GMT"));
        } catch (ParseException e) {
          dur.putEnd((new SimpleDateFormat("yyyyMMdd")).parse(text));
        }
        if (dur.getStart().equals(dur.getEnd()))
          dur.putAllDay();
      } else if (name.equals("DUE")) {
        String s = readText(node);
        dur.putStart(sdf.parse(s+"GMT"));
        dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
        dur.putEnd(sdf.parse(s+"GMT"));
      } else if (name.equals("TRANSP"))
        dur.putA("blocked", new Boolean(readText(node).equals("OPAQUE")));
      else if (name.equals("LOCATION")) {
        String loc = readText(node);
        if (loc != null && loc.equals("<NONE>"))
          loc = null;
        ent.putE("location", loc);
      } else if (name.equals("SUMMARY"))
        desc.put(readText(node));
      else if (name.equals("COMPLETED")) {
        String complete = readText(node);
        complete = complete.trim();
        if (complete != null && !complete.equals("0")) {
          comp.putCompleted(sdf.parse(readText(node)+"GMT"));
          desc.putE("completion",comp);
        }
      } else if (name.equals("PERCENT")) {
        comp.putE("percent", new Integer(readText(node)));
        desc.putE("completion",comp);
      } else if (name.equals("STATUS"))
        desc.putA("status", readText(node));
      else if (name.equals("PRIORITY"))
        desc.putA("priority", new Integer(readText(node)));
      else if (name.equals("DESC")) {
        String notes = readText(node);
        if (notes != null && notes.equals("<NONE>"))
          notes = null;
        desc.putE("description", notes);
      } else if (name.equals("CATEGORIES"))
        desc.putE("category", split(readText(node), ','));
      else if (name.equals("ORGANIZER"))
        ent.putE("organizer", readOrganizer(node));
      else if (name.equals("ATTENDEE"))
        attdv.addElement(readAttendee(node));
      else if (name.equals("CREATED"))
        ent.putA("created", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("LAST-MOD"))
        ent.putA("last-modified", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("SEQUENCE"))
        ent.putA("revision", new Integer(readText(node)));
      else if (name.equals("ALARM")) {
        AlarmData ala = readAlarm(node);
        if (ala.getTrigger().getTime() > 0)
          alv.addElement(ala);
      } else if (name.equals("RRULE") || name.equals("RDATE") || name.equals("EXRULE") || name.equals("EXDATE")) {
        RecurrenceData rec = new RecurrenceData();
        if (name.startsWith("EX"))
          rec.putA("exclude", new Boolean(true));
        if (name.endsWith("DATE"))
          rec.putA("date", sdf.parse(readText(node)+"GMT"));
        if (name.endsWith("RULE")) {
          String[] rule = split(readText(node), ';');
          for (int j = 0; j < rule.length; j++) {
            if (rule[j].startsWith("FREQ"))
              rec.putA("frequency", rule[j].substring(5));
            if (rule[j].startsWith("INTERVAL"))
              rec.putA("interval", new Integer(rule[j].substring(9)));
            if (rule[j].startsWith("COUNT"))
              rec.putA("count", new Integer(rule[j].substring(6)));
            if (rule[j].startsWith("UNTIL"))
              rec.putA("until", sdf.parse(rule[j].substring(6)+"GMT"));
          }
        }
        recv.addElement(rec);
      } else if (name.equals("RSTATUS"))
        m_error = readText(node);
      else if (name.equals("RELATED"))
        relv.addElement(readText(node));
      else if (name.equals("RESOURCES"))
        resv.addElement(readText(node));
    }

    // Verify invitation
    if( isInvitation( ent, attdv)) {
      ent.setIsInvitation(true);

      // Title of entry (event of todo)
      ent.putE("event", desc);

      // Duration
      ent.putE("duration", dur);

      // Attendees
      AttendeeData[] attdr = new AttendeeData[attdv.size()];
      for (int i = 0; i < attdr.length; i++)
        attdr[i] = (AttendeeData)attdv.elementAt(i);
      ent.putE("attendee", attdr);

      // RelatedTo and Resources
      if( relv.size() > 0) {
        String[] relr = new String[relv.size()];
        for (int i=0; i < relv.size(); i++)
          relr[i] = (String)relv.elementAt(i);
        ent.putRelatedTos(relr);
        String[] resr = new String[resv.size()];
        for (int i=0; i < resv.size(); i++)
          resr[i] = (String)resv.elementAt(i);
        ent.putResources(resr);
      }

      // Alarms
      if( alv.size() > 0) {
        AlarmData[] alr = new AlarmData[alv.size()];
        for (int i = 0; i < alr.length; i++)
          alr[i] = (AlarmData)alv.elementAt(i);
        ent.putE("alarm", alr);
      }

      // Recurrences
      RecurrenceData[] recr = new RecurrenceData[recv.size()];
      for (int i = 0; i < recr.length; i++)
        recr[i] = (RecurrenceData)recv.elementAt(i);
      ent.putE("recurrence", recr);

      // OK to return
      return ent;
    }

    return null;
  }

  EntryData readEntry(CalendarData cal, Node node, String type) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    EntryData ent = new EntryData();
    Vector attdv = new Vector();
    DurationData dur = new DurationData();
    Vector alv = new Vector();
    Vector recv = new Vector();
    Vector resv = new Vector();
    Vector relv = new Vector();
    CompletionData comp = new CompletionData(null,null);
    XMLData desc = null;

    // Initial type of entry
    if (type.equals("event")) {
      desc = new EventData();
    } else {
      desc = new TodoData();
    }

    // Scan children
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      if( filterEntry( name) == false)
        continue;

      if (name.equals("UID"))
        ent.putA("ceid", readText(node));
      else if (name.equals("RECURID"))
        ent.putA("ceid", ent.getA("ceid")+"."+readText(node));
      else if (name.equals("CLASS"))
        ent.putA("share", readText(node));
      else if (name.equals("START")) {
        String text = readText(node);
        try {
          dur.putA("start", sdf.parse(text+"GMT"));
          dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
        } catch (ParseException e) {
          dur.putA("start", (new SimpleDateFormat("yyyyMMdd")).parse(text));
          dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
          dur.putAllDay();
        }
      } else if (name.equals("END")) {
        String text = readText(node);
        try {
          dur.putEnd(sdf.parse(text+"GMT"));
        } catch (ParseException e) {
          dur.putEnd((new SimpleDateFormat("yyyyMMdd")).parse(text));
        }
        if (dur.getStart().equals(dur.getEnd()))
          dur.putAllDay();
      } else if (name.equals("DUE")) {
        String s = readText(node);
        dur.putStart(sdf.parse(s+"GMT"));
        dur.putDOW(net.unicon.portal.channels.calendar.CalendarView.dayOfWeek(dur.getStart()));
        dur.putEnd(sdf.parse(s+"GMT"));
      } else if (name.equals("TRANSP"))
        dur.putA("blocked", new Boolean(readText(node).equals("OPAQUE")));
      else if (name.equals("LOCATION")) {
        String loc = readText(node);
        if (loc != null && loc.equals("<NONE>"))
          loc = null;
        ent.putE("location", loc);
      } else if (name.equals("SUMMARY"))
        desc.put(readText(node));
      else if (name.equals("COMPLETED")) {
        String complete = readText(node);
        complete = complete.trim();
        if (complete != null && !complete.equals("0")) {
          comp.putCompleted(sdf.parse(readText(node)+"GMT"));
          desc.putE("completion",comp);
        }
      } else if (name.equals("PERCENT")) {
        comp.putE("percent", new Integer(readText(node)));
        desc.putE("completion",comp);
      } else if (name.equals("STATUS"))
        desc.putA("status", readText(node));
      else if (name.equals("PRIORITY"))
        desc.putA("priority", new Integer(readText(node)));
      else if (name.equals("DESC")) {
        String notes = readText(node);
        if (notes != null && notes.equals("<NONE>"))
          notes = null;
        desc.putE("description", notes);
      } else if (name.equals("CATEGORIES"))
        desc.putE("category", split(readText(node), ','));
      else if (name.equals("ORGANIZER"))
        ent.putE("organizer", readOrganizer(node));
      else if (name.equals("ATTENDEE"))
        attdv.addElement(readAttendee(node));
      else if (name.equals("CREATED"))
        ent.putA("created", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("LAST-MOD"))
        ent.putA("last-modified", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("SEQUENCE"))
        ent.putA("revision", new Integer(readText(node)));
      else if (name.equals("ALARM")) {
        AlarmData ala = readAlarm(node);
        if (ala.getTrigger().getTime() > 0)
          alv.addElement(ala);
      } else if (name.equals("RRULE") || name.equals("RDATE") || name.equals("EXRULE") || name.equals("EXDATE")) {
        RecurrenceData rec = new RecurrenceData();
        if (name.startsWith("EX"))
          rec.putA("exclude", new Boolean(true));
        if (name.endsWith("DATE"))
          rec.putA("date", sdf.parse(readText(node)+"GMT"));
        if (name.endsWith("RULE")) {
          String[] rule = split(readText(node), ';');
          for (int j = 0; j < rule.length; j++) {
            if (rule[j].startsWith("FREQ"))
              rec.putA("frequency", rule[j].substring(5));
            if (rule[j].startsWith("INTERVAL"))
              rec.putA("interval", new Integer(rule[j].substring(9)));
            if (rule[j].startsWith("COUNT"))
              rec.putA("count", new Integer(rule[j].substring(6)));
            if (rule[j].startsWith("UNTIL"))
              rec.putA("until", sdf.parse(rule[j].substring(6)+"GMT"));
          }
        }
        recv.addElement(rec);
      } else if (name.equals("RSTATUS"))
        m_error = readText(node);
      else if (name.equals("RELATED"))
        relv.addElement(readText(node));
      else if (name.equals("RESOURCES"))
        resv.addElement(readText(node));
    }

    // Title of entry (event of todo)
    ent.putE(type, desc);

    // Duration
    ent.putE("duration", dur);

    // Attendees
    if (attdv.size() > 0) {
      // Convert to array
      AttendeeData[] attdr = new AttendeeData[attdv.size()];
      for (int i = 0; i < attdr.length; i++)
        attdr[i] = (AttendeeData)attdv.elementAt(i);
      ent.putE("attendee", attdr);
    }

    // Verify invitation
    if (cal.isPersonal() &&
        ent.getOrganizer()!=null && ent.getOrganizer().get() != null) {
      for (int i = 0; i < attdv.size(); i++) {
        AttendeeData att = (AttendeeData)attdv.elementAt(i);
        if (att.get().equals(cal.getOwner())) {
          String status = att.getStatus();
          if (status.equals("NEEDS-ACTION"))
            ent.setIsInvitation(true);
          else if (status.equals("DECLINED"))
            ent.setIsDeclined(true);
          break;
        }
      }
    }

    // RelatedTo and Resources
    if( relv.size() > 0) {
      String[] relr = new String[relv.size()];
      for (int i=0; i < relv.size(); i++)
        relr[i] = (String)relv.elementAt(i);
      ent.putRelatedTos(relr);
      String[] resr = new String[resv.size()];
      for (int i=0; i < resv.size(); i++)
        resr[i] = (String)resv.elementAt(i);
      ent.putResources(resr);
    }

    // Alarms
    if( alv.size() > 0) {
      AlarmData[] alr = new AlarmData[alv.size()];
      for (int i = 0; i < alr.length; i++)
        alr[i] = (AlarmData)alv.elementAt(i);
      ent.putE("alarm", alr);
    }

    // Recurrences
    RecurrenceData[] recr = new RecurrenceData[recv.size()];
    for (int i = 0; i < recr.length; i++)
      recr[i] = (RecurrenceData)recv.elementAt(i);
    ent.putE("recurrence", recr);

    return ent;
  }

  OrganizerData readOrganizer(Node node) throws Exception {
    OrganizerData orgr = new OrganizerData();
    orgr.put(readText(node));
    NamedNodeMap attrs = node.getAttributes();
    Node attr = attrs.getNamedItem("X-NSCP-ORGANIZER-SENT-BY-UID");
    if (attr != null)
      orgr.putA("sent-by", readText(attr));

    attr = attrs.getNamedItem("X-NSCP-ORGANIZER-UID");
    if (attr != null)
      orgr.putA("cuid", readText(attr));
    return orgr;
  }

  AttendeeData readAttendee(Node node) throws Exception {
    AttendeeData attd = new AttendeeData();
    attd.putA("cuid", readText(node));
    attd.putEntityType("p"); //campus data
    attd.putOID(attd.getCuid());
    attd.put(readText(node));
    NamedNodeMap attrs = node.getAttributes();
    Node attr = null;
    if ((attr = attrs.getNamedItem("ROLE")) != null)
      attd.putA("role", readText(attr));
    if ((attr = attrs.getNamedItem("PARTSTAT")) != null)
      attd.putA("status", readText(attr));
    if ((attr = attrs.getNamedItem("RSVP")) != null)
      attd.putA("rsvp", new Boolean(readText(attr).equals("TRUE")));

    return attd;
  }

  AlarmData readAlarm(Node node) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'zzz");
    AlarmData alrm = new AlarmData();
    Vector recv = new Vector();
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      node = nodes.item(i);
      String name = node.getNodeName();
      if (name.equals("ACTION"))
        alrm.putA("action", readText(node));
      else if (name.equals("TRIGGER"))
        alrm.putA("trigger", sdf.parse(readText(node)+"GMT"));
      else if (name.equals("ATTENDEE"))
        recv.addElement(readText(node).substring(7));
    }
    String[] recr = new String[recv.size()];
    for (int i = 0; i < recr.length; i++)
      recr[i] = (String)recv.elementAt(i);
    alrm.putE("recipient", recr);
    return alrm;
  }
  String readText(Node node) throws Exception {
    Node text = node.getFirstChild();
    if (text == null)
      return null;
    if (text.getNodeType() != Node.TEXT_NODE)
      throw new Exception("[WCAP] Unexpected XML format!");
    return text.getNodeValue();
  }

  //========================================================================//
  static ACE findACE(Vector acl, String cuid) {
    for (int i = 0; i < acl.size(); i++) {
      ACE data = (ACE)acl.elementAt(i);
      if (data != null && data.m_user.equals(cuid))
        return data;
    }
    return null;
  }

  /**
   * Get array of tokens from ACEs
   * @param ace
   * @return [0]: user, [1]:what, [2]: how, [3]: grant or deny
   */
  public static String[] getTokens(String ace) {
    //format: user^what^access^grant
    StringTokenizer token = new StringTokenizer(ace, "^", false);
    if (token.countTokens() != 4)
      return null;
    else
      return new String[] {
               token.nextToken(),token.nextToken(),
               token.nextToken(),token.nextToken()
             };
  }

  /**
   * Parse inputstream to Node object
   * @param stream
   * @return
   * @throws Exception
   */
  public static Node parseToNode(InputStream stream) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    DocumentBuilder parser = dbf.newDocumentBuilder();
    Document dom = parser.parse(new InputSource(stream));

    Node node = dom.getFirstChild();
    if (!node.getNodeName().equals("iCalendar"))
      throw new Exception("[WCAP] Unexpected XML format!");
    return node;
  }

  static boolean union(String[] strr, String str)throws Exception {
      for (int i=0; i<strr.length; i++) {
        if (str.equals(strr[i]))
          return true;
      }
      return false;
    }

  static String[] split(String sz, char sep) {
    Vector szv = new Vector();
    for (;;) {
      int i = sz.indexOf(sep);
      if (i == -1) {
        szv.addElement(sz);
        break;
      }
      szv.addElement(sz.substring(0, i));
      sz = sz.substring(i+1);
    }
    String[] szr = new String[szv.size()];
    for (int i = 0; i < szr.length; i++)
      szr[i] = (String)szv.elementAt(i);
    return szr;
  }

  static int binary(Vector inviv, EntryData et , int from, int to) {
    if (to-from <= 5) {
      for (int i = from; i < to; i++) {
        EntryData ent = (EntryData)inviv.elementAt(i);

        if (et.getDuration().getStart().after(ent.getDuration().getStart()))
          return i;
      }
      return to;
    }
    int mid = (from+to)/2;
    EntryData ent = (EntryData)inviv.elementAt(mid);

    if (et.getDuration().getStart().after(ent.getDuration().getStart()))
      return binary(inviv,et, from, mid+1);
    if (et.getDuration().getStart().before(ent.getDuration().getStart()))
      return binary(inviv,et, mid, to);
    return mid;
  }

}
