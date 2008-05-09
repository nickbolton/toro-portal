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
package net.unicon.academus.apps.calendar;


import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

/**
 * xml to VCalendar format
 * @version 1.0
 * @author nntruong@ibs-dp.com
 */

//public class ICal extends Properties{
public class ICal extends Vector{

  // - keys
  static final String BEGIN = "BEGIN";
  static final String VCAL = "VCALENDAR";
  static final String PRODID = "PRODID";
  static final String PRODIDVAL = "-//iPlanet/Calendar Hosting Server//EN";
  static final String METHOD = "METHOD";
  static final String METHODVAL = "PUBLISH";
  static final String VERSION = "VERSION";
  static final String VERSIONVAL = "2.0";
  static final String VEVENT = "VEVENT";
  static final String UID = "UID";
  static final String REC_ID = "RECURRENCE-ID";
  static final String DTSTAMP = "DTSTAMP";
  static final String SUMMARY = "SUMMARY";
  static final String DTSTART = "DTSTART";
  static final String DTEND = "DTEND";
  static final String CREATED = "CREATED";
  static final String LASTMOD = "LAST-MODIFIED";
  static final String PRIORITY = "PRIORITY";
  static final String SEQ = "SEQUENCE";
  static final String DES = "DESCRIPTION";
  static final String LOC = "LOCATION";
  static final String ORG = "ORGANIZER";
  static final String X_NSCP_ORG = "X-NSCP-ORGANIZER-UID";
  static final String X_NSCP_ORG_SENT = "X-NSCP-ORGANIZER-SENT-BY-UID";
  static final String STATUS = "STATUS";
  static final String CONFIRM = "CONFIRMED";
  static final String TRANSP = "TRANSP";
  static final String TRANSPVAL = "OPAQUE";
  static final String ATTENDEE = "ATTENDEE";
  static final String ROLE = "ROLE";
  static final String ROLEVAL = "REQ-PARTICIPANT";
  static final String CUTYPE = "CUTYPE";
  static final String CUTYPEVAL = "INDIVIDUAL";
  static final String PARTSTAT = "PARTSTAT";
  static final String PARTSTATVAL = "ACCEPTED";
  static final String NEED_ACTION = "NEEDS-ACTION";
  static final String RSVP = "RSVP";
  static final String TRUE = "TRUE";
  static final String X_NSCP_ATT = "X-NSCP-ATTENDEE-GSE-STATUS";
  static final String X_NSCP_ORIGINAL = "X-NSCP-ORIGINAL-DTSTART";
  static final String X_NSCP_LAN = "X-NSCP-LANGUAGE";
  static final String X_NSCP_DTSTART = "X-NSCP-DTSTART-TZID";
  static final String X_NSCP_TOMBSTONE = "X-NSCP-TOMBSTONE";
  static final String X_NSCP_ONGOING = "X-NSCP-ONGOING";
  static final String X_NSCP_GSE_COMPO = "X-NSCP-GSE-COMPONENT-STATE";
  static final String X_NSCP_GSE_COMMENT = "X-NSCP-GSE-COMMENT";
  static final String REQWAITFORREP = "REQUEST-WAITFORREPLY";
  static final String REQCOMP = "REQUEST-COMPLETED";
  static final String X_NSCP_WCAP = "X-NSCP-WCAP-ERRNO";
  static final String NONE = "<NONE>";
  static final String END = "END";

  public ICal(){

  }

  public InputStream init(CalendarData cal) throws Exception{
    if (cal == null) return null;
    part1();
    EntryData[] ents = cal.getEntry();
    if (ents == null || ents.length == 0) return null;
    //System.out.println("Entry size " + ents.length);
    for (int i = 0; i < ents.length; i++){
      if (ents[i] != null) buildEvent(ents[i], cal);
    }
    part2();
    return export();
  }

  public static EntryData[] importVcal(InputStream in) throws Exception{
    if (in == null) return null;
    LineNumberReader line = new LineNumberReader(new InputStreamReader(in));
    return generateEvent(line);
  }

  static EntryData[] generateEvent(LineNumberReader line) throws Exception{
	  
    // TT05517 - Removed 'Z' because ISO-8601 UTC time zone identifier is not 
    // required. No identifier signifies local time zone. 
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    
    Vector v = new Vector();
    EntryData ent = null;
    EventData env = null;
    DurationData dur = null;
    OrganizerData org = null;
    Vector attv = new Vector();
    String ceid = null;
    boolean endEvent = false;
    String s = null;
    while ((s = line.readLine()) != null){
      if (s== null) continue;
      else if (s.startsWith(BEGIN+":"+VEVENT)){
        ent = new EntryData();
        env = new EventData();
        dur = new DurationData();
        org = new OrganizerData();
        endEvent = false;
      }
      else if (s != null && s.startsWith(END+":"+VEVENT) && ent != null){
        endEvent = true;
        ent.putEvent(env);
        ent.putOrganizer(org);
        ent.putDuration(dur);
        if (attv.size() != 0) {
            ent.putAttendee((AttendeeData[])attv.toArray(new AttendeeData[0]));
        }
        v.addElement(ent);
      }
      //else if (s.startsWith(UID) && (ceid = s.substring(4)) != null){
      //  ent.putCeid(ceid);
//System.out.println("ceid = " + ceid);
      //}
      //else if (s.startsWith(REC_ID))
      //  ent.putCeid(ent.getCeid()+"."+s.substring(REC_ID.length()));
      else if (s.startsWith(SUMMARY)){
          env.put(s.substring(8));
      }
      else if (s.startsWith(DTSTART)){
          dur.putStart(parseIso8601DateFormat(s.substring(8), sdf));
      }      
      else if (s.startsWith(DTEND)) {
          dur.putEnd(parseIso8601DateFormat(s.substring(6), sdf));
      } 
      else if (s.startsWith(CREATED) && s.substring(8) != null) {
          ent.putA("created", parseIso8601DateFormat(s.substring(8), sdf));
      }
      else if (s.startsWith(LASTMOD) && s.substring(14) != null) {
          ent.putA("last-modified", parseIso8601DateFormat(s.substring(14), 
                  sdf));
      }
      else if (s.startsWith(PRIORITY) && s.substring(9) != null) {
        env.putPriority(new Integer(s.substring(9)));
      }
      else if (s.startsWith(DES)) {
        env.putDescription(s.substring(12));
      }
      else if (s.startsWith(LOC)) {
        ent.putLocation(s.substring(9));
      }
      else if (s.startsWith(ORG)){
        //String sid = s.substring(32);
        //if (sid != null) org.putCuid(sid.substring(sid.indexOf("=")));
        StringTokenizer token = new StringTokenizer(s,";:=");
        int count = 0;
        while (token.hasMoreTokens()){
          ++count;
          String e = (String)token.nextElement();
          if (count == 3){org.putCuid(e); break;}
        }
      }
      else if (s.startsWith(STATUS)) {
        env.putStatus(s.substring(7));
      }
      else if (s.startsWith(ATTENDEE)){
        AttendeeData att = new AttendeeData();
        StringTokenizer token = new StringTokenizer(s,";=:");
        int count = 0;
        while (token.hasMoreTokens()){
          count++;
          String e = (String)token.nextElement();
          if (count == 3) att.putRole(e);
        }
        s = line.readLine();
        token = new StringTokenizer(s,";=");
        count = 0;
        while (token.hasMoreTokens()){
          count++;
          String e = (String)token.nextElement();
          if (count == 5)  att.putRSVP(new Boolean(e));
          if (count == 3)  att.putStatus(e);
        }
        s = line.readLine();
        s = line.readLine();
        att.putCuid(s.substring(s.indexOf(":")+1));
        att.put(att.getCuid());
        attv.addElement(att);
      }
    }
    //System.out.println("v size" + v.size());
    return (EntryData[])v.toArray(new EntryData[]{new EntryData()});
  }

  InputStream export() throws Exception{
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    list(out);
    //System.out.println("iCAL file " + baos.toString());
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

    return bais;
  }

  void buildEvent(EntryData ent, CalendarData cal) throws Exception{
    setProperty(BEGIN, VEVENT);
    setProperty(UID, ent.getCeid());
    //setProperty(REC_ID, "");
    setProperty(DTSTAMP, sdf(cal.getCreated()));
    setProperty(SUMMARY, ent.getTitle());
    setProperty(DTSTART, sdf(ent.getDuration().getStart()));
    //System.out.println("ent.getDuration().getEnd() " + ent.getDuration().getEnd());
    if (ent.getDuration().getEnd() == null)
       setProperty(DTEND, sdf(new Date(ent.getDuration().getStart().getTime()+ent.getDuration().getLength().getLength())));
    else
     setProperty(DTEND, ent.getDuration().getEnd() != null ? sdf(ent.getDuration().getEnd()) : "");
    setProperty(CREATED, sdf(ent.getCreated()));
    setProperty(LASTMOD, sdf(ent.getLastModified()));
    setProperty(PRIORITY, ent.getEvent().getPriority() != null ? ent.getEvent().getPriority().toString() : "0");
    setProperty(SEQ, ent.getRevision() != null ? ent.getRevision().toString() : "0");
    setProperty(ORG+";"+X_NSCP_ORG+"="+ent.getOrganizer().getCuid()
                +";"+X_NSCP_ORG_SENT+"="+ent.getOrganizer().getSentby()
                ,ent.getOrganizer().getCuid());
    setProperty(STATUS, ent.getEvent().getStatus());
    setProperty(TRANSP, TRANSPVAL);
    buildAttendees(ent.getAttendee());
    setProperty(X_NSCP_ORIGINAL, sdf(ent.getDuration().getStart()));
    setProperty(X_NSCP_LAN, "en");
    Calendar calendar = Calendar.getInstance();
    setProperty(X_NSCP_DTSTART, calendar.getTimeZone().getID());
    setProperty(X_NSCP_TOMBSTONE, "0");
    setProperty(X_NSCP_ONGOING, "0");
    setProperty(X_NSCP_GSE_COMPO + ";" + X_NSCP_GSE_COMMENT + "=" + REQWAITFORREP, "131074");
    setProperty(END, VEVENT);
  }

  void buildAttendees(AttendeeData[] atts) throws Exception{
    if (atts != null && atts.length != 0){
      for (int i = 0; i < atts.length; i++){
        if (atts[i] != null){
          setProperty(ATTENDEE + ";" + ROLE + "=" + atts[i].getRole() + ";" + CUTYPE +
                      "=" + CUTYPEVAL + "\n"
                      + " ;" + PARTSTAT + "=" + atts[i].getStatus()
                      + ";" + RSVP + "=" + atts[i].getRSVP() + "\n"
                      + " ;" + X_NSCP_ATT + "=" + "2" + "\n" + " "
                      , atts[i].getCuid());
        }
      }
    }

  }

  void part1(){
    setProperty(BEGIN, VCAL);
    setProperty(PRODID, PRODIDVAL);
    setProperty(METHOD, METHODVAL);
    setProperty(VERSION, VERSIONVAL);
  }

  void part2(){
    setProperty(X_NSCP_WCAP, "0");
    setProperty(END, VCAL);
  }

  void setProperty(String s1, String s2){
     add(s1+":"+s2);
  }


  void list(PrintStream out) {
     for (int i = 0; i < size(); i++){
       out.println(elementAt(i));
     }
   }

   private String sdf(Date date){
     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
     Calendar calendar = Calendar.getInstance();
     //sdf.setTimeZone(calendar.getTimeZone());
     sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
     return sdf.format(date);
   }
   
    
    /*
     * Determines whether a Time Zone has been specified in the ISO 8601 
     * date/time format. 'Z' indicates UTC. No identifier indicates local time.
     * For example:
     *     yyyymmddThhmmssZ - UTC
     *     yyyymmddThhmmss  - Local
     * Sets the SimpleDateFormat parser's time zone appropriately.
     */
    private static Date parseIso8601DateFormat(String dateTime, 
            SimpleDateFormat sdf) throws ParseException {

        // Assertions
        if (dateTime == null) {
            final String msg = "Argument 'dateTime' [String] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (sdf == null) {
            final String msg = 
                "Argument 'sdf' [SimpleDateFormat] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        if (dateTime.length() == 16
                && "Z".equalsIgnoreCase(dateTime.substring(15))) {
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        } else {
            sdf.setTimeZone(TimeZone.getDefault()); // Local time zone 
        }

        return sdf.parse(dateTime);
    }

}
