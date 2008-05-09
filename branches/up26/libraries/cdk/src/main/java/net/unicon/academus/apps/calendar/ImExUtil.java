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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import net.unicon.academus.apps.rad.MSVLine;

/*
import java.io.FileInputStream;
import java.io.File;
*/

public final class ImExUtil {
  private static final String TAB = "\t";
  private static final String NEWLINE = "\r";

  public static final int SUBJECT = 0;
  public static final int START_DATE = 1;
  public static final int START_TIME = 2;
  public static final int END_DATE = 3;
  public static final int END_TIME = 4;
  public static final int ALL_DAY = 5;
  public static final int ORGANIZER = 6;
  public static final int ATTENDEES = 7;
  public static final int CATEGORIES = 8;
  public static final int DESCRIPTION = 9;
  public static final int LOCATION = 10;
  public static final int PRIORITY = 11;
  public static final int DUE_DATE = 12;
  public static final int DATE_COMPLETE = 13;
  public static final int PERCENT_COMPLETE = 14;
  public static final int NOTES = 15;
  //added for priority bug. DG
  private static final int VERY_LOW = 0;
  private static final int LOW = 3;
  private static final int NORMAL = 5;
  private static final int HIGH = 7;
  private static final int VERY_HIGH = 9;
  private static final String sVERY_LOW = "Very Low";
  private static final String sLOW = "Low";
  private static final String sNORMAL = "Normal";
  private static final String sHIGH = "High";
  private static final String sVERY_HIGH = "Very High";

  /**Number of element of m_fields have to equal with n as above example.*/
  private static String[] m_fields = null;
  /**Save header of tab delimited file.*/
  private static String m_title = null;
  /**flag say status not init or init.*/
  private static boolean m_flag= false;

  private static void init() {
    //init Fields
    m_fields = new String[] {
                 "Subject","Start Date","Start Time","End Date","End Time",
                 "All day event","Meeting Organizer","Optional Attendees",
                 "Categories","Description","Location","Priority", "Due Date",
                 "Date Complete", "% Complete", "Notes"
               };
    StringBuffer sbRet = new StringBuffer();
    //init Title
    if(m_fields!=null && m_fields.length>0) {
      for(int i = 0; i < m_fields.length - 1; i++) {
        sbRet.append(m_fields[i]);
        sbRet.append(TAB);
      }
      sbRet.append(m_fields[m_fields.length-1]);
    }
    m_title = sbRet.toString();
    m_flag = true;
  }

  private static String getValue(EntryData ent, int key) {
    String ret = null;
    if (ent == null)
      return null;
    switch (key) {
    case SUBJECT : {
        ret = ent.getTitle();
        break;
      }
    case START_DATE : {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        //System.out.println("&&&& import &&&&&& start date " + ent.getDuration().getStart());
        ret = sdf.format(ent.getDuration().getStart());
        //ret = ent.getDuration().getStart().toString();
        //System.out.println("&&&& import &&&&&& ret " + ret);
        break;
      }
    case START_TIME : {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        ret = sdf.format(ent.getDuration().getStart());
        break;
      }
    case END_DATE : {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        if (ent.getDuration() != null && ent.getDuration().getEnd() != null)
          ret = sdf.format(ent.getDuration().getEnd());
        break;
      }
    case END_TIME : {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        if (ent.getDuration() != null && ent.getDuration().getEnd() != null)
          ret = sdf.format(ent.getDuration().getEnd());
        break;
      }
    case ALL_DAY : {
        boolean allday = ent.getDuration().getLength() != null ? ent.getDuration().getLength().getAllDay() : false;
        ret = (new Boolean(allday)).toString();
        break;
      }
    case ORGANIZER :
      ret = ent.getOrganizer().getCuid();
      break;
    case ATTENDEES : {
        AttendeeData[] atts = ent.getAttendee();
        if (atts != null && atts.length != 0) {
          for (int i = 0; i < atts.length; i++) {
            if (i == 0 )
              ret = atts[i].getCuid();
            else
              ret += ";" + atts[i].getCuid();
          }
        }
        break;
      }
    case CATEGORIES: {
        ret = ent.getEvent() != null ? ent.getEvent().getCategory() != null
              && ent.getEvent().getCategory().length != 0
              ? ent.getEvent().getCategory()[0]:
              ent.getTodo()!= null && ent.getTodo().getCategory() != null && ent.getTodo().getCategory().length != 0
      ? ent.getTodo().getCategory()[0] : null : null;
        break;
      }
    case DESCRIPTION : {
        ret = ent.getEvent() != null ? ent.getEvent().getDescription()
              : ent.getTodo().getDescription();
        break;
      }
    case NOTES :
      ret = ent.getEvent() != null ? ent.getEvent().getDescription()
            : ent.getTodo().getDescription();
      break;
    case LOCATION :
      ret = ent.getLocation();
      break;
    case PRIORITY : {
        Integer prior = ent.getEvent() != null ? ent.getEvent().getPriority() :
                        ent.getTodo() != null ? ent.getTodo().getPriority() : null;
        
        switch(prior.intValue()){
        case VERY_LOW :
            ret = sVERY_LOW;
            break;
        case LOW :
            ret = sLOW;
            break;
        case NORMAL :
            ret = sNORMAL;
            break;
        case HIGH :
            ret = sHIGH;
            break;
        case VERY_HIGH :
            ret = sVERY_HIGH;
            break;
        }
            
//        ret = prior.intValue() == 1 || prior.intValue() == 3 ? sLOW :
//              prior.intValue() == 7 || prior.intValue() == 9 ? "High" : "Normal";
        break;
      }
    case DUE_DATE : {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        if (ent.getTodo() != null)
          ret = sdf.format(ent.getTodo().getDue());
        break;
      }
    case DATE_COMPLETE : {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        if (ent.getTodo() != null)
          if (ent.getTodo().getCompletion() != null)
            ret = sdf.format(ent.getTodo().getCompletion().getCompleted());
        break;
      }
    case PERCENT_COMPLETE : {
        if (ent.getTodo() != null && ent.getTodo().getCompletion() != null && ent.getTodo().getCompletion().getPercent() != null)
          ret = ent.getTodo().getCompletion().getPercent().toString();
        break;
      }
    }
    return ret==null?"":MSVLine.encodeOutlook(ret);
  }

  private static String exportEntryData(EntryData ent) {
    StringBuffer sbData = new StringBuffer();
    if(m_flag == false)
      init();
    if(m_fields!=null && m_fields.length > 0) {
      for(int i = 0; i < m_fields.length - 1; i++) {
        sbData.append( getValue(ent , i ));
        sbData.append(TAB);
      }
      sbData.append( getValue(ent, m_fields.length - 1));
      sbData.append(TAB);
    }
    return sbData.toString();
  }

  /**
   * @param EntryData Array
   * @return Byte Array
   */

  public static byte[] exportEntryByte(EntryData[] ents) {
    StringBuffer sbData = new StringBuffer();
    if(m_flag == false)
      init();
    //add title
    sbData.append(m_title);
    //add content
    if(ents != null && ents.length > 0) {
      sbData.append(NEWLINE);
      for(int i = 0; i < ents.length - 1 ; i++) {
        sbData.append(exportEntryData(ents[i]));
        sbData.append(NEWLINE);
      }
      sbData.append(exportEntryData(ents[ents.length - 1]));
      sbData.append(NEWLINE);
    }
    return sbData.toString().getBytes();
  }

  /**
   * @param EntryData array
   * @return <code>InputStream</code>
   * @throws IOException
   */

  public static InputStream exportEntryStream(EntryData[] ents) throws IOException {
    return new ByteArrayInputStream(exportEntryByte(ents));
  }

  /**
   * @param EntryData
   * @param key int
   * @param value <code>String</code>
   * @param format <code>String</code> "TODO" or "EVENT"
   * @param error <code>String</code> Error message return "" if no error
   * @throws Exception
   */

  private static void setValue(EntryData ent, int key, String value, String format, String error) throws Exception {
    //if (value != null && value.trim().length() != 0){
    //System.out.println("^^^^  value ^^^^^ "+value);
    switch (key) {
    case SUBJECT : {
        if (format != null && format.equals(CalendarServer.EVENT)) {
          EventData event = ent.getEvent() != null ? ent.getEvent() : new EventData();
          event.put(value != null && value.trim().length() != 0 ? value : "Untitled");
          ent.putEvent(event);
        } else if (format != null && format.equals(CalendarServer.TODO)) {
          TodoData todo = ent.getTodo() != null ? ent.getTodo() : new TodoData();
          todo.put(value != null && value.trim().length() != 0 ? value : "Untitled");
          ent.putTodo(todo);
        }
        break;
      }
    case START_DATE : {
        //System.out.println("^^^^  value start date "+value);
        try {
          //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
          //Date date = Screen.parseDate(value);
          Date date = parseDate(value);
          if (date != null) {
            DurationData dur = ent.getDuration();
            if (dur != null)
              dur.putStart(date);
            else {
              dur = new DurationData();
              dur.putStart(date);
              ent.putDuration(dur);
              //System.out.println( "Start date " + dur.getStart());
            }
          }
        } catch (Exception e) {
          error = "Invalid Start Date" ;
          throw new Exception("Invalid Start Date");
        }
        break;
      }
    case START_TIME : {
        //System.out.println("^^^^  value start time "+value);
          //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
          //Date date = sdf.parse(value);
          Date date = parseDate(value);
          Calendar cal0 = Calendar.getInstance();
          cal0.setTime(date);
          if (date != null) {
            DurationData dur = ent.getDuration() != null ? ent.getDuration() : new DurationData();
            Date start = dur.getStart();
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            cal.set(Calendar.HOUR, cal0.get(Calendar.HOUR));
            cal.set(Calendar.MINUTE, cal0.get(Calendar.MINUTE));
            cal.set(Calendar.AM_PM, cal0.get(Calendar.AM_PM));
            dur.putStart(cal.getTime());
            ent.putDuration(dur);
          } else {
            error = "Invalid Start Time" ;
            throw new Exception("Invalid Start Time");
          }
          break;
    }
      
    case END_DATE : {
        //System.out.println("^^^^  value end date "+value);
        if (format != null && format.equals(CalendarServer.EVENT)
            && value != null && !value.equals("") ) {
          //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
          //Date date = Screen.parseDate(value);
          Date date = parseDate(value);
          if (date != null) {
            DurationData dur = ent.getDuration();
            if (dur != null)
              dur.putEnd(date);
            else {
              dur = new DurationData();
              dur.putEnd(date);
              ent.putDuration(dur);
              //System.out.println( "end  " + dur.getEnd());
            }
          } else {
            error = "Invalid End Date" ;
            throw new Exception("Invalid End Date");
          }
          break;
        }
      }
    case END_TIME : {
        //System.out.println("^^^^  value end time "+value);
        if (format != null && format.equals(CalendarServer.EVENT)
            && value != null && !value.equals("") ) {
          //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
          Date date = parseDate(value);//sdf.parse(value);
          Calendar cal0 = Calendar.getInstance();
          cal0.setTime(date);
          if (date != null) {
            DurationData dur = ent.getDuration() != null ? ent.getDuration() : new DurationData();
            Date start = dur.getEnd();
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            cal.set(Calendar.HOUR, cal0.get(Calendar.HOUR));
            cal.set(Calendar.MINUTE, cal0.get(Calendar.MINUTE));
            cal.set(Calendar.AM_PM, cal0.get(Calendar.AM_PM));
            dur.putEnd(cal.getTime());
            ent.putDuration(dur);
          } else {
            error = "Invalid End Time" ;
            throw new Exception("Invalid End Time");
          }
          break;
        }
      }
    case ALL_DAY : {
        //System.out.println("^^^^  value all day "+value);
        if ( format.equals("EVENT") && value != null && value.equals("True")) {
          DurationData dur = ent.getDuration();
          if (dur == null)
            dur = new DurationData();
          TimeLength len = dur.getLength();
          if (len == null)
            len = new TimeLength();
          len.setAllDay();
          dur.putLength(len);
          //System.out.println("length "+ dur.getLength());
        }
        break;
      }
    case ORGANIZER : {
        OrganizerData od = new OrganizerData();
        od.putCuid(value != null && value.trim().length() != 0 ? value : "admin");
        ent.putOrganizer(od);
        break;
      }
    case ATTENDEES : {
        StringTokenizer token = new StringTokenizer(value, ";");
        Vector v = new Vector();
        while (token.hasMoreElements()) {
          String val = token.nextToken();
          if (val != null && val.trim().length() > 0) {
            AttendeeData att = new AttendeeData();
            att.putCuid(val);
            att.putName(val);
            att.putStatus("NEED-ACTION");
            v.addElement(att);
          }
        }
        if (v.size() > 0)
          ent.putAttendee((AttendeeData[])v.toArray(new AttendeeData[]{new AttendeeData()}));
        break;
      }
    case CATEGORIES : {
        StringTokenizer token = new StringTokenizer(value, ";");
        Vector v = new Vector();
        while (token.hasMoreElements()) {
          String val = token.nextToken();
          v.addElement(val);
        }
        if (format.equals(CalendarServer.EVENT)) {
          EventData event = ent.getEvent();
          if (event != null) {
            event.putCategory((String[])v.toArray(new String[]{new String()}));
          } else {
            event = new EventData();
            event.putCategory((String[])v.toArray(new String[]{new String()}));
          }
          ent.putEvent(event);
        } else if (format.equals(CalendarServer.TODO)) {
          TodoData todo = ent.getTodo();
          if (todo != null) {
            todo.putCategory((String[])v.toArray(new String[]{new String()}));
          } else {
            todo = new TodoData();
            todo.putCategory((String[])v.toArray(new String[]{new String()}));
          }
          ent.putTodo(todo);
        }
        break;
      }
    case DESCRIPTION : {
        //System.out.println("^^^^  value notes "+value);
        if (format.equals(CalendarServer.EVENT)) {
          EventData event = ent.getEvent();
          if (event != null) {
            event.putDescription(value);
          } else {
            event = new EventData();
            event.putDescription(value);
          }
          ent.putEvent(event);
        } else if (format.equals(CalendarServer.TODO)) {
          TodoData todo = ent.getTodo();
          if (todo != null) {
            todo.putDescription(value);
          } else {
            todo = new TodoData();
            todo.putDescription(value);
          }
          ent.putTodo(todo);
        }
        break;
      }
    case NOTES : {
        //System.out.println("^^^^  value notes "+value);
        if (format.equals(CalendarServer.EVENT)) {
          EventData event = ent.getEvent();
          if (event != null) {
            event.putDescription(value);
          } else {
            event = new EventData();
            event.putDescription(value);
          }
          ent.putEvent(event);
        } else if (format.equals(CalendarServer.TODO)) {
          TodoData todo = ent.getTodo();
          if (todo != null) {
            todo.putDescription(value);
          } else {
            todo = new TodoData();
            todo.putDescription(value);
          }
          ent.putTodo(todo);
        }
        break;
      }
    case LOCATION : {
        ent.putLocation(value != null ? value : "");
        break;
      }
    case PRIORITY : {
        //System.out.println("^^^^  value priority "+value);
//        int prior = value.equals("Low") ? 2 : value.equals("High") ? 7 : 5;
        int prior;
        if(value.equals(sVERY_LOW))
            prior = VERY_LOW;
        else if (value.equals(sLOW))
            prior = LOW;
        else if (value.equals(sHIGH))
            prior = HIGH;
        else if (value.equals(sVERY_HIGH))
            prior = VERY_HIGH;
        //If none of the above default to normal priority
        else 
            prior = NORMAL;
        if (format.equals(CalendarServer.TODO)) {
          TodoData todo = ent.getTodo() != null ? ent.getTodo() : new TodoData();
          todo.putPriority(new Integer(prior));
        }
        if (format.equals(CalendarServer.EVENT)) {
          EventData event = ent.getEvent() != null ? ent.getEvent() : new EventData();
          event.putPriority(new Integer(prior));
        }
        break;
      }
    case DUE_DATE : {
        if (format.equals(CalendarServer.TODO) && value != null && value.length()!=0 ) {
          //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
          //Date date = Screen.parseDate(value);
          Date date = parseDate(value);
          if (date != null) {
            TodoData todo = ent.getTodo();
            if (todo != null) {
              todo.putDue(date);
            } else {
              todo = new TodoData();
              todo.putDue(date);
            }
            ent.putTodo(todo);
          } else {
            error = "Invalid Due Date" ;
            throw new Exception("Invalid Due Date");
          }
        }
        break;
      }
    case DATE_COMPLETE : {
        if (format.equals(CalendarServer.TODO) && value != null && value.length()!=0 ) {
          Date date = null;
          try {
            //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            //Date date = Screen.parseDate(value);
            date = parseDate(value);
          } catch (Exception e) {
            e.printStackTrace();
            error = "Invalid Date Completed" ;
            throw new Exception("Invalid Date Completed");
          }
          if (date != null) {
            TodoData todo = ent.getTodo();
            if (todo == null)
              todo = new TodoData();
            CompletionData comp = todo.getCompletion();
            if (comp == null)
              comp = new CompletionData(date, new Integer(0));
            comp.putCompleted(date);
            todo.putCompletion(comp);
            ent.putTodo(todo);
          }
        }
      }
    case PERCENT_COMPLETE : {
        if (format.equals(CalendarServer.TODO)) {
          try {
            //System.out.println("^^^^  value ^^^^^ percent "+value);
            int per = 0;
            if (value != null && value.length() != 0 && value.startsWith("1.0"))
              per = 100;
            TodoData todo = ent.getTodo();
            if (todo == null)
              todo = new TodoData();
            CompletionData comp = todo.getCompletion();
            if (comp == null)
              comp = new CompletionData(null,new Integer(per));
            comp.putPercent(new Integer(per));
            todo.putCompletion(comp);
            ent.putTodo(todo);
          } catch (Exception e) {
            error = "Invalid Percent Completed";
            //e.printStackTrace(System.err);
            //throw e;
          }
        }
      }
    }
    //}
  }
  /**
   * Checking file is valid or non valid.
   * @param in contains of file.
   * @return true is valid.
   *         false is not valid.
   */
  private static boolean validateTabDelimited(InputStream in) {
    //Not yet
    return true;
  }

  /*
    private static String scanLine(String str, LineNumberReader readLine) throws java.io.IOException{
      System.out.println("str " + str);
      if (str.indexOf('"') != -1){
          String s1 = str.substring(str.indexOf("\""));
          if (s1 != null && !s1.startsWith("\"")){
            while (true){
              String s2 = readLine.readLine();
              //str = str + scanLine(s2, readLine);
              str = str + s2;
              if (s2 == null) break;
              if (s2 != null && s2.indexOf("\"") != -1 && s2.substring(s2.indexOf("\"")) != null
                  && !s2.substring(s2.indexOf("\"")).startsWith("\""))
                break;
            }
          }
          else if (s1 != null && s1.startsWith("\"")) {scanLine(str, readLine);}
          else return str;
      }
      return str;
    }
  */

  /**
   * @param <code>InputStream</code>
   * @return <code>Hashtable</code>
   * @throws Exception
   */

  public static Hashtable[] loadDataFromTabDelimitedFile(InputStream in) throws Exception {

    String str = null;
    Vector vret = new Vector();
    Vector vheader = new Vector();
    int i = 0 ;
    if(!validateTabDelimited(in))
      return null;

    StringBuffer sb = new StringBuffer();
    int b;
    while ((b = in.read()) != -1) {
        sb.append((char)b);
    }

    String msvLine = sb.toString();
    char separator = '\n';
    if (msvLine.indexOf("\r") != -1) separator = '\r';
    MSVLine lines = new MSVLine(msvLine, separator, true);
    if (lines != null && lines.size() > 1) {
      //read file header.
      str = (String)lines.elementAt(0);
      //System.out.println("%%%%%%%% header "  +str);
      StringTokenizer tk = new StringTokenizer(str,"\t");
      while(tk.hasMoreTokens()) {
        i++;
        String temp = (String)tk.nextToken();
        if(temp.equals(""))
          vheader.addElement("FIELD" + i);
        else
          vheader.addElement(temp);
      }
      //System.out.println("%%%%%%%% vheader size "  +vheader.size());
      //read content
      for (int j = 1; j < lines.size(); j++) {
        str = (String)lines.elementAt(j);
        //System.out.println("%%%%%%%% line "+j+" "+str);
        MSVLine lineFields = new MSVLine(str, '\t', false);
        //System.out.println("%%%%%%%% lineFields size "  + lineFields.size());
        i = 0;
        Hashtable hs = new Hashtable();
        for (int k = 0; k < lineFields.size(); k++) {
          String fieldContent = (String)lineFields.elementAt(k);
          //System.out.println("%%%%%%%% fieldContent k " + k + " " + fieldContent);
          hs.put((String)vheader.elementAt(i),fieldContent);
          i++;
        }

        vret.addElement(hs);
      }
    }


    /*
    //read file header.
    if((str = readLine.readLine()) != null){
      StringTokenizer tk = new StringTokenizer(str,"\t");
      while(tk.hasMoreTokens()){
        i++;
        String temp = (String)tk.nextToken();
        if(temp.equals("")) vheader.addElement("FIELD" + i);
        else vheader.addElement(temp);
      }
    }
    //read content
    while((str = readLine.readLine()) != null) {

    System.out.println("%%%%%%%% " +str);
      StringTokenizer tk = new StringTokenizer(str,"\t",true);
      i = 0;
      Hashtable hs = new Hashtable();
      while(tk.hasMoreTokens()){
        String temp = (String)tk.nextToken();
        if(!temp.equals("\t")){
          if(tk.hasMoreTokens()){
            tk.nextToken();
          }
        }
        else
          temp = "";

        hs.put((String)vheader.elementAt(i),temp);
        i++;
      }
      vret.addElement(hs);
    }
    */

    return (Hashtable[])vret.toArray(new Hashtable[0]);
  }
  /**
   * Import data from list of input and map.
   */
  public static EntryData[] importEntry(Hashtable[] inputs, Hashtable map, String format, String error) throws Exception {
    EntryData[] entr = null;
    if(m_flag == false)
      init();
    if(inputs != null && inputs.length > 0) {
      //System.out.println("importEntry 2 ");
      entr = new EntryData[inputs.length];
      for(int i = 0; i < inputs.length; i++) {
        entr[i] = new EntryData();
        for(int j = 0; j < m_fields.length; j++) {
          if(map.get(new Integer(j).toString())!=null) {
            if(inputs[i].get((String)map.get(new Integer(j).toString()))!=null)
              setValue(entr[i],j,(String)inputs[i].get((String)map.get(new Integer(j).toString())), format, error);
            else
              setValue(entr[i],j,"",format, error);
          }
        }
      }
    }
    return entr;
  }

  //--------------
  protected static SimpleDateFormat[] m_dateFormats = new SimpleDateFormat[] {
        new java.text.SimpleDateFormat("MM/dd/yy hh:mm a"), // default format
        new java.text.SimpleDateFormat("MM/dd/yy"),
        new java.text.SimpleDateFormat("MM/dd/yyyy"),
        new java.text.SimpleDateFormat("hh:mm:ss a"),
        new java.text.SimpleDateFormat("hh:mm:ss"),
        new java.text.SimpleDateFormat("hh:mm a"),
        new java.text.SimpleDateFormat("hh:mm")
      };

  public static Date parseDate(String str) {
    if (str == null || str.equals("") )
      return null;
    Date d = null;
    for( int i = 0; i < m_dateFormats.length && d == null; i++)
      d = m_dateFormats[i].parse( str ,new ParsePosition(0));
    return d;
  }
}
