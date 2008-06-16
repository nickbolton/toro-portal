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
package net.unicon.sdk.util;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateUtils {

	private DateUtils()
	{
	}
	
	private static Log log = LogFactory.getLog(DateUtils.class);

	public static long oneDayMillis = 1000 * 60 * 60 * 24;

	public static SimpleDateFormat normalFormat =
					new SimpleDateFormat("MM/dd/yyyy HH:mm");

	public static SimpleDateFormat normalFormat2 =
					new SimpleDateFormat ("d MMM ''yy HH:mm");

	public static SimpleDateFormat dateFormat =
					new SimpleDateFormat("MM/dd/yyyy");

	public static SimpleDateFormat timeFormat =
					new SimpleDateFormat("H:mm");

	public static SimpleDateFormat yearFormat =
					new SimpleDateFormat("yyyy");

	public static SimpleDateFormat monthFormat =
					new SimpleDateFormat("MMMMM");

	public static SimpleDateFormat dayFormat =
					new SimpleDateFormat("d");

	public static SimpleDateFormat hourFormat =
					new SimpleDateFormat("H");

	public static SimpleDateFormat minuteFormat =
					new SimpleDateFormat("mm");

	public static String showDateTime(Date ts)
	{
		if (ts == null) return "";
		return normalFormat.format(ts);
	}

	public static String showDateTime2(Date ts)
	{
		if (ts == null) return "";
		return normalFormat2.format(ts);
	}

	public static String showDate(Date ts)
	{
		if (ts == null) return "";
		return dateFormat.format(ts);
	}

	public static String showTime(Date ts)
	{
		if (ts == null) return "";
		return timeFormat.format(ts);
	}

	public static String showYear(Date ts)
	{
		if (ts == null) return "";
		return yearFormat.format(ts);
	}

	public static String showMonth(Date ts)
	{
		if (ts == null) return "";
		return monthFormat.format(ts);
	}

	public static String showDay(Date ts)
	{
		if (ts == null) return "";
		return dayFormat.format(ts);
	}

	public static String showHour(Date ts)
	{
		if (ts == null) return "";
		return hourFormat.format(ts);
	}

	public static String showMinute(Date ts)
	{
		if (ts == null) return "";
		return minuteFormat.format(ts);
	}

	public static String formatOracleDate(String date)
	{
	   if (date == null || date.length() < 12)
	   return "";

	   String year   = date.substring(2,4);
	   String month = date.substring(5,7);
	   String day  = date.substring(8,10);

	   return month + '/' + day + '/' + year;
	}

	public static String formatOracleDateY2K(String date)
	{
	   if (date == null || date.length() < 12)
	   return "";

	   String year   = date.substring(0,4);
	   String month = date.substring(5,7);
	   String day  = date.substring(8,10);

	   return month + '/' + day + '/' + year;
	}

	public static String formatOracleDateY2K(Date date)
	{
	   if (date == null)
	     return " ";

	   String sDate = date.toString();
	   if (sDate == null || sDate.length() < 10)
	     return " ";

	   String year   = sDate.substring(0,4);
	   String month = sDate.substring(5,7);
	   String day  = sDate.substring(8,10);

	   return month + '/' + day + '/' + year;
	}

	public static java.sql.Date setDateVal(String d) {
      if ((d == null) || (d.equals(""))) return null;
      String date = d;
      if (d.indexOf("/") != -1) date = reFormatDate(d);
      return java.sql.Date.valueOf(date);
	}

	public static Timestamp convertToTimestampVal(String t) {
	  if ((t == null) || (t.equals(""))) return null;
	  String datetime = t;
	  if (t.indexOf("/") != -1) datetime = reFormatDateAndTime(t);
	  return Timestamp.valueOf(datetime);
	}

	public static String reFormatDate(String simpleDate) {
      try {

      StreamTokenizer s = new StreamTokenizer(new StringReader(simpleDate));
      s.whitespaceChars('/','/');
      s.whitespaceChars(',',',');

      s.nextToken();  int mo = (int) s.nval;
      s.nextToken();  int da = (int) s.nval;
      s.nextToken();  int yr = (int) s.nval;

      String ret =  yr + "-";
      if (mo < 10) ret += "0";
      ret = ret + mo + "-";
      if (da < 10) ret += "0";
      ret = ret + da;
      return ret;

      } catch (Exception e) { return ""; }
	}

	public static String reFormatDateAndTime(String simpleDate) {
	try {

	  StreamTokenizer s = new StreamTokenizer(new StringReader(simpleDate));
	  s.whitespaceChars('/','/');
	  s.whitespaceChars(',',',');
	  s.whitespaceChars(':',':');

	  s.nextToken();  int mo = (int) s.nval;
	  s.nextToken();  int da = (int) s.nval;
	  s.nextToken();  int yr = (int) s.nval;
	  s.nextToken();  int hr = (int) s.nval;
	  s.nextToken();  int mi = (int) s.nval;

	  String ret =  yr + "-";
	  if (mo < 10) ret += "0";
	  ret = ret + mo + "-";
	  if (da < 10) ret += "0";
	  ret = ret + da + " ";
	  if (hr < 10) ret += "0";
	  ret = ret + hr + ":";
	  if (mi < 10) ret += "0";
	  ret = ret + mi + ":00.000000000";
      
	  return ret;

	  } catch (Exception e) { return ""; }
	}

	public static java.sql.Date convertToDate(String simpleDate) {
      if (simpleDate == null) return null;
      try {

      StreamTokenizer s = new StreamTokenizer(new StringReader(simpleDate));
      s.whitespaceChars('/','/');
      s.whitespaceChars(',',',');

      s.nextToken();
      if (s.ttype != s.TT_NUMBER) return null;
      int mo = (int) s.nval;
      if ((mo > 12) || (mo < 1)) return null;

      s.nextToken();
      if (s.ttype != s.TT_NUMBER) return null;
      int da = (int) s.nval;
      if ((da > 31) || (da < 1)) return null;

      s.nextToken();
      if (s.ttype != s.TT_NUMBER) return null;
      int yr = (int) s.nval;
      if ((yr > 3000) || (yr < 1800)) return null;

      String dStr =  yr + "-";
      if (mo < 10) dStr += "0";
      dStr = dStr + mo + "-";
      if (da < 10) dStr += "0";
      dStr = dStr + da;

      return java.sql.Date.valueOf(dStr);

      } catch (Exception e) { return null; }
	}

	public static String getCurrentDate() {
		java.sql.Date sys = new java.sql.Date(System.currentTimeMillis());
		return showDate(sys);
	}

	public static String getMonthName(int month)
   	{
		switch(month)
		{
			case 0:
				return "January";
			case 1:
				return "Feburary";
			case 2:
				return "March";
			case 3:
				return "April";
			case 4:
				return "May";
			case 5:
				return "June";
			case 6:
				return "July";
			case 7:
				return "August";
			case 8:
				return "September";
			case 9:
				return "October";
			case 10:
				return "November";
			case 11:
				return "December";
			default:
				return null;
		}
	}

	public static String getMonthPrefix(int month)
   	{
		switch(month)
		{
			case 0:
				return "Jan";
			case 1:
				return "Feb";
			case 2:
				return "Mar";
			case 3:
				return "Apr";
			case 4:
				return "May";
			case 5:
				return "Jun";
			case 6:
				return "Jul";
			case 7:
				return "Aug";
			case 8:
				return "Sept";
			case 9:
				return "Oct";
			case 10:
				return "Nov";
			case 11:
				return "Dec";
			default:
				return null;
		}
	}

	/**
	 * <P>This method returns the date in the following format:</P>
	 * <CODE>
	 * <3-letter prefix of month> '. ' <dd> ' ' <yyyy> ' ' {<HH>':'<MM>':'<SS>}	
	 * </CODE>
	 * <P>If show time is true, the time component is displayed else it is not.</P>
	 *
	 * @param date Date to be displayed.
	 * @param showTime boolean that determines whether the time component should be displayed.
	 * @return formatted date string. 
	 */
	public static String showMonthPrefixDate(java.util.Date date, boolean showTime)
	{
		String displaytime;
		
		if(showTime)
			displaytime = (date.getHours() < 10 ? "0" + date.getHours() : "" + date.getHours()) + ":" +
			              (date.getMinutes() < 10 ? "0" + date.getMinutes() : "" + date.getMinutes()) + ":" +
						  (date.getSeconds() < 10 ? "0" + date.getSeconds() : "" + date.getSeconds());
		else
			displaytime = "";
			
		return getMonthPrefix(date.getMonth()) + ". " + date.getDate() + " " + 
		       (date.getYear() + 1900) + " " + displaytime;
	}


  	/*** Simple date validation for now - dates must be separated by
        '/' and have legal values for m, d, y.  In the future, this
	method should user Java's Date class to make sure the m,d,y
	combinations a 1) legal and 2) greater than today for the
	class start date.
  	*/
  	public static GregorianCalendar verifyDate(String date)
  	{
    	boolean ok = true;
    	int mo=0, day=0, yr=0;
    	try {
			if (date.indexOf("/") == -1) ok = false;
			StreamTokenizer s = new StreamTokenizer(new StringReader(date));
			s.whitespaceChars('/', '/');
			s.nextToken(); mo  = (int) s.nval;
			s.nextToken(); day = (int) s.nval;
			s.nextToken(); yr  = (int) s.nval;
			if ( (mo<1)||(mo>12) || (yr<1900)||(yr>2050) || (day<0)||(day>31) )
	  			ok = false;
    	}
    	catch (Exception e) {ok = false;}

    	if (ok) return new GregorianCalendar(yr, mo-1, day);
    	else    return null;
  	}

   /**
    * The formatUSDateString method concatenates a zero to the day and/or month when the month and/or day
	* are single digits. 
	*
	* The parameter, date, is in the form of m{m}/d{d}/yyyy
	*/
   public static String formatUSDateString(String date)
   {  
      if(date.charAt(1) == '/')
	     date = "0" + date;
	  
      if(date.charAt(4) == '/')
	     date = date.substring(0, 3) + "0" + date.substring(3, 9);
	 
      return date.substring(0, 10);
   }

   private static String tabByZero(int number)
   {
      return number < 10 ? "0" + number : "" + number;
   }

   public static String convertToTimeZone(String currentTime, TimeZone currentTimeZone, TimeZone newTimeZone) throws Exception
   {
	  ParsePosition pp = new ParsePosition(0);   
   	  SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      Date dCurrentTime = sdf.parse(currentTime, pp);
	  String newTime = sdf.format(convertToTimeZone(dCurrentTime, currentTimeZone, newTimeZone));
	  return newTime;
   }

   public static Timestamp convertToTimeZone(Timestamp currentTime, TimeZone currentTimeZone, TimeZone newTimeZone) throws Exception
   {
      return new Timestamp(convertToTimeZone((Date)currentTime, currentTimeZone, newTimeZone).getTime()); 
   }
   
   public static Date convertToTimeZone(Date currentTime, TimeZone currentTimeZone, TimeZone newTimeZone) throws Exception
   {
	  GregorianCalendar gcCurrentTime = new GregorianCalendar(currentTimeZone);
	  GregorianCalendar gcNewTime = new GregorianCalendar(newTimeZone);
	  int currentTimeOffset;
	  int newTimeOffset;

	  gcCurrentTime.setTime(currentTime);
	  currentTimeOffset = gcCurrentTime.get(Calendar.ZONE_OFFSET) + gcCurrentTime.get(Calendar.DST_OFFSET);	  
	  gcNewTime.setTime(new Date(gcCurrentTime.getTime().getTime() + -1 * currentTimeOffset)); // Convert to GMT first
	  newTimeOffset = gcNewTime.get(Calendar.ZONE_OFFSET) + gcNewTime.get(Calendar.DST_OFFSET);
	  gcNewTime.setTime(new Date(gcNewTime.getTime().getTime() + newTimeOffset)); // Convert to new time zone
	  if (log.isDebugEnabled()) {
	      log.debug("Date-time in " + currentTimeZone.getID() + ": " + gcCurrentTime.getTime().toString() + " " + 
	          "Date-time in " + newTimeZone.getID() + ": " + gcNewTime.getTime().toString());
	  }
	  
      return gcNewTime.getTime(); 
   }
}

// end of DateUtils.java

