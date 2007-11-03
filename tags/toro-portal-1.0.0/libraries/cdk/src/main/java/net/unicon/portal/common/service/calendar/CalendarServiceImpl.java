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
package net.unicon.portal.common.service.calendar;


import java.util.Properties;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.lang.Integer;

import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.apps.calendar.ACEData;
import net.unicon.academus.apps.calendar.AttendeeData;
import net.unicon.academus.apps.calendar.CalendarData;
import net.unicon.academus.apps.calendar.CalendarServer;
import net.unicon.academus.apps.calendar.DurationData;
import net.unicon.academus.apps.calendar.EntryData;
import net.unicon.academus.apps.calendar.EntryRange;
import net.unicon.academus.apps.calendar.EventData;
import net.unicon.academus.apps.calendar.OrganizerData;
import net.unicon.academus.apps.calendar.RecurrenceData;
import net.unicon.academus.apps.calendar.TimeLength;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.service.calendar.CalendarServerInfo;
import net.unicon.academus.service.calendar.CalendarService;
import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.common.service.activation.ActivationService;
import net.unicon.portal.channels.rad.PortalFinder;
import net.unicon.portal.channels.rad.GroupData;


public class CalendarServiceImpl implements CalendarService {

    private static final Properties props = new Properties();
    //everything but the last 5 seconds really...this has to do with activations..
    private static long milliseconds_in_day = 1000*60*60*24 - (1000*60*5);

    private static PortalFinder pFinder = new PortalFinder();

    static {

        try {

            System.out.println("Loading Calendar Service properties");

            props.load(CalendarServiceImpl.class.getResourceAsStream(

            "/properties/rad.properties"));

        } catch (IOException ioe) {

            LogService.instance().log(LogService.ERROR,

            "Unable to read rad.properties file.");

            LogService.instance().log(LogService.ERROR, ioe);

        }

    }

    public String getCalendarId(Offering offering) throws Exception {
        if (offering == null)
          throw new Exception(
            "CalendarServiceImpl::getCalendarId : null offering");
        return "Offering - "+offering.getId();
    }

    public void deleteCalendar(Offering offering)
    throws Exception {
        if (offering == null)
          throw new Exception(
            "CalendarServiceImpl::deleteCalendar : null offering");

        String calid = getCalendarId(offering);
        IdentityData logonOffering = new IdentityData(
            IdentityData.ENTITY, GroupData.S_OBJECT,
            "" + offering.getId(), calid, calid);
        CalendarServer server = CalendarServer.getInstance(props);
        CalendarData cal = CalendarData.findCalendar(
            server.login(logonOffering, null), calid);
        
        if (cal == null) {
            LogService.log(LogService.WARN, "CalendarServiceImpl::deleteCalendar : unable to find calendar: " + calid);
            return;
        }

        server.deleteCalendar(calid);
    }

    public CalendarServerInfo createCalendar(Offering offering)
    throws Exception {
        if (offering == null)
          throw new Exception(
            "CalendarServiceImpl::createCalendar : null offering");

        String calid = getCalendarId(offering);
        IdentityData logonOffering = new IdentityData(
            IdentityData.ENTITY, GroupData.S_OBJECT,
            "" + offering.getId(), calid, calid);
        CalendarServer server = CalendarServer.getInstance(props);
        CalendarData cal = CalendarData.findCalendar(
            server.login(logonOffering, null), calid);
        String calname =
            new StringBuffer().append(offering.getTopic().getName()).
                append(" - ").append(offering.getName()).toString();
        java.util.Date now = new java.util.Date();

        if (cal != null) {

            if (cal.getCalid().equals(cal.getCalname())) {
                // The calendar server created a calendar when it
                // logged in. Update the calname and owner
                cal.putCalname(calname);
                cal.putOwner(calname);
                cal.putA("last-modified", now);
                server.updateCalendar(cal);
                return new CalendarServerInfo(server,
                                              server.updateCalendar(cal));
            
            }
            return new CalendarServerInfo(server, cal);
        }

        cal = new CalendarData();
        cal.putCalid(calid);
        cal.putCalname(calname);
        cal.putOwner(calname);
        cal.putA("created", now);
        cal.putA("last-modified", now);
        server.createCalendar(cal);
        return new CalendarServerInfo(server, cal);
    }

    public CalendarServerInfo getCalendar(Offering offering, User user)

    throws Exception {

        if (user == null ) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::getCalendar() null user.");

            return null;

        }

        if (offering == null) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::getCalendar() offering is null.");

            return null;

        }

        Topic topic = offering.getTopic();

        String calid = getCalendarId(offering);

        CalendarServer server = CalendarServer.getInstance(props);

        // Offering calendar logon

        IdentityData logonOffering = new IdentityData(

        IdentityData.ENTITY, GroupData.S_OBJECT,

        "" + offering.getId(), calid, calid);


        // Logon to CalendarServer (DPCS)

        LogService.log(LogService.DEBUG,
           "//// login to Calendar:logon=" + logonOffering);

        CalendarData[] cals = server.login(logonOffering, null);

        if ( cals == null)

            LogService.log(LogService.DEBUG,
                "//// cals == null ");

        else {

            LogService.log(LogService.DEBUG,
                "//// logon OK: cals.length=" + cals.length);

            for ( int i = 0; i < cals.length; i++) {

                LogService.log(LogService.DEBUG,
                    "  //// cal(" + i + ")=" + cals[i].getCalid());

            }

        }

        CalendarData cal = CalendarData.findCalendar(cals, calid);

        if ( cal == null)

            LogService.log(LogService.DEBUG,
                "//// not found cal " + calid);

        return new CalendarServerInfo(server, cal);

    }

    public CalendarServerInfo updateCalendar(Offering offering)
    throws Exception {
        String calid = getCalendarId(offering);
        IdentityData logonOffering = new IdentityData(
            IdentityData.ENTITY, GroupData.S_OBJECT,
            "" + offering.getId(), calid, calid);
        CalendarServer server = CalendarServer.getInstance(props);
        CalendarData cal = CalendarData.findCalendar(
            server.login(logonOffering, null), calid);
        if (cal == null) {
            LogService.log(LogService.ERROR, "CalendarServiceImpl::updateCalendar : unable to find calendar: " + calid);
            throw new Exception("Calendar not found '" + calid + "'");
        }

        String calname =
            new StringBuffer().append(offering.getTopic().getName()).
                append(" - ").append(offering.getName()).toString();
        cal.putCalname(calname);
        cal.putOwner(calname);
        server.updateCalendar(cal);
        return new CalendarServerInfo(server, cal);
    }

    public void addUser(Offering offering, User user)

    throws Exception {

        if (user == null ) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::addUser() null useuser.");

            return;

        }

        if (offering == null) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::addUser() offering is null.");

            return;

        }

        CalendarServerInfo csi = getCalendar(offering, user);

        if (csi == null) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::addUser() " +

            "failed to retrieve calendar info");

            return;

        }

        CalendarServer server = csi.getServer();

        CalendarData cal = csi.getData();

        if (cal != null) {

            // Sharing user

            IdentityData shareUser = new IdentityData(

            IdentityData.ENTITY, GroupData.S_USER,

            null, user.getUsername(), user.getFullName());

            // Add ace to acl

            ACEData[] acl = cal.getACE();

            ACEData[] newAcl = new ACEData[(acl == null ? 0 : acl.length) + 1];

            if ( acl != null)
                for (int i = 0; i < acl.length; i++) {

                    newAcl[i] = acl[i];

                }

            newAcl[newAcl.length - 1] = new ACEData(shareUser, false);

            cal.putACE(newAcl);

            // Save changes

            server.updateCalendar(cal);

        }

        server.logout();

    }

    public void removeUser(Offering offering, User user) throws Exception {

        if (user == null ) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::removeUser() null user.");

            return;

        }

        if (offering == null) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::removeUser() offering is null.");

            return;

        }

        CalendarServerInfo csi = getCalendar(offering, user);

        if (csi == null) {

            LogService.instance().log(LogService.ERROR,

            "CalendarServiceImpl::removeUser() " +

            "failed to retrieve calendar info");

            return;

        }

        CalendarServer server = csi.getServer();

        CalendarData cal = csi.getData();

        if (cal != null) {

            ACEData[] acl = cal.getACE();

            if ( acl != null) {

                String username = user.getUsername();

                //-- Dump ---

                for (int i = 0; i < acl.length; i++) {

                    LogService.log(LogService.DEBUG,
                        "////-- acl(" + i + ")=" + IdentityData.xml(acl[i], "tien"));

                }

                // Remove user from acl

                Vector v = new Vector();

                for (int i = 0; i < acl.length; i++) {

                    String cuid = acl[i].getCuid();

                    if ( cuid == null || !cuid.equals(username))

                        v.addElement(acl[i]);

                }

                cal.putACE((ACEData[]) v.toArray(new ACEData[0]));

                // Save

                server.updateCalendar(cal);

            }

        }

        server.logout();

    }

    /**
    * <p>
    * A method that creates events in a calendar for an activation 
    * <p>
    * @param newact - a newly created activation object
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    */

    public Activation pushToCalendar(Activation newact,String calid, String title, String description, int priority, String location, String category, String organizer) throws Exception{
       Calendar start = Calendar.getInstance();
       Calendar end = Calendar.getInstance();
       start.setTime(new Date(newact.getStartTime()));
       end.setTime(new Date(newact.getEndTime()));

       if(title.length() > 512){
         throw new Exception("Title should not exceed 512 characters.");
       }else if(description.length() > 512){
         throw new Exception("Description should not exceed 512 characters.");
       }else if(location.length() > 512){
         throw new Exception("Location should not exceed 512 characters."); 
       }else if(category.length() > 512){
         throw new Exception("Category should not exceed 512 characters.");
       }

       int numDays = 0;
       Calendar secondDay = Calendar.getInstance();
       secondDay.setTime(end.getTime());
       secondDay.set(Calendar.MILLISECOND,0);
       secondDay.set(Calendar.SECOND,0);
       secondDay.set(Calendar.MINUTE,0);
       secondDay.set(Calendar.HOUR_OF_DAY,0);
       Date secondDayStart = secondDay.getTime();
       Date endDate = end.getTime(); 

       if(endDate.compareTo(secondDayStart) != 0){
         numDays = end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);      
       }

       try{
         if(numDays > 0){

           //System.err.println("Pushing the event to multiple days");
           //The activation spans at least over one day boundary..so we need to
           //create two or more events.
           newact = pushCalMultDay(newact,start,end,calid,numDays,title,description,priority,location,category,organizer);

         }else{
           //Activation occurs only within one day.

            //System.err.println("Pushing the event to a single day");
            newact = pushCalOneDay(newact,start,end,calid,title,description,priority,location,category,organizer);
         }
       }catch(Exception e){
          throw e;
       }

       return newact;

 
    }


    /**
    * <p>
    * Deletes events for an activation from the calendar.
    * <p>
    * @param oldact - the activity for which events should be removed
    * @param calid - identifier for the calendar which the events exist in
    * <p>
    * @throws Exception - if the calendar server cannot be accessed
    */
    public void delFromCalendar(Activation oldact, String calid) throws Exception{
      CalendarServer server = null;
      EntryRange[] entryRange = null;
      String label = "Event";

      try{ 
        server = CalendarServer.getInstance(props);
      }catch(Exception e){
         throw new Exception("Could not access the calendar server\n");
      }

      Map attributes = oldact.getAttributes();

      String numberOfEvents = (String)attributes.get("numEvents");

      if(numberOfEvents != null){

        int numEvents = Integer.parseInt(numberOfEvents);

        entryRange = new EntryRange[numEvents];

        for(int i = 0; i < numEvents; i++){
          
          EntryRange entRange = new EntryRange((String)attributes.get((label + i)),EntryRange.ALL );
          entryRange[i] = entRange;  
          attributes.remove((label + i));
        }

        attributes.remove("numEvents");
        oldact.setAttributes(attributes);

        if(calid != null){
          server.deleteEvents(calid,entryRange);
        }
      }
     
    }

    /**
    * <p>
    * Updates the events for an activation
    * <p>
    * @param update - an activation object
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    * <p>
    * @throws Exception if the calendar server cannot be accessed, or if the
    * if the events cannot be stored in the database due to a failure
    */

    public void pushEditToCalendar(Activation update, String calid,String title,String description, int priority, String location, String category,String organizer) throws Exception{
       try{
         delFromCalendar(update,calid);
         pushToCalendar(update,calid,title,description,priority,location,category,organizer);
       }catch(Exception e){
         throw new Exception("Update activation, failed to update events: ", e);
       }
    }

   /**
   * <p>
   * @param title - title for the event
   * @param category - category for the event
   * @param priority - the priority for the envent
   * @param description - description for the event
   * <p>
   * @return EventData - an EventData object that represents the event
   */

    private EventData createEvent(String title, String category, int priority, String description){
       EventData activationEvent = new EventData();
       activationEvent.put(XMLData.replace(title, "&#34;", "\""));
       //Add a category..none seems to be the most suitable existing choice.
       String[] categories = new String[1];
       categories[0] =  XMLData.replace(category, "&#34;", category);
       activationEvent.putCategory(categories);
       activationEvent.putPriority(new Integer(priority));
       activationEvent.putDescription(XMLData.replace(description, "&#34;", "\""));

       return activationEvent;
    }

    /**
    * <p>
    * Places an event on a calendar if the activation only occurs within one day.
    * <p>
    * @param newact - a new activation object
    * @param start - calendar instance representing the start time of the event to be created
    * @param end - calendar instance representing the end time of the event to be created
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    * <p>
    * @return Activation - an activation object with the event linked to it
    * <p>
    * @throws Exception - throws an Exception when the calendar server cannot be accessed or the 
    * event cannot be created in the database.
    */
    private Activation pushCalOneDay(Activation newact, Calendar start, Calendar end, String calid,String title, String description, int priority, String location, String category,String organizer) throws Exception{
   
       Map attributeMap = newact.getAttributes();
       EntryData entData = null;
       //System.err.println("Just before pushEntry\n");

       try{
         entData = pushEntry(newact, calid, start, end,title,description,priority,location,category,organizer); 
       }catch(Exception e){
         throw e;
       }

       attributeMap.put("numEvents",Integer.toString(1));
       attributeMap.put("Event0" ,entData.getCeid());      

       //System.err.println("Leaving pushCalOneDay\n");

       return newact;
 
    }

    /**
    * Pushes an event to a calendar when the activation spans multiple days.
    * <p>
    * @param newact - a new activation object
    * @param start - calendar instance representing the start time of the event to be created
    * @param end - calendar instance representing the end time of the event to be created
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    * <p>
    * @return Activation - an activation object with the event linked to it
    * <p>
    * @throws Exception - throws an Exception when the calendar server cannot be accessed or the 
    * event cannot be created in the database.
    */

    private Activation pushCalMultDay(Activation newact, Calendar start, Calendar end, String calid, int range,String title, String description, int priority, String location, String category, String organizer) throws Exception{
      int splitOverNDays = 0;
      Map attributeMap = newact.getAttributes();
      EntryData entData = null;
      String eventText = "Event";
      int iterationsNeeded = 0;

      if(range == 1){
        iterationsNeeded = 2;
      }else{
        iterationsNeeded = 3;
      }

      int lastIteration = iterationsNeeded - 1;
 

      //System.err.println("Start is: " + start.getTime() + "\n");
      //System.err.println("End is: " + end.getTime() + "\n");
      Calendar startDayEnd = Calendar.getInstance();
      Calendar lastDayStart = Calendar.getInstance(); 


      attributeMap.put("numEvents",Integer.toString(iterationsNeeded));
 
      for(int i = 0; i < iterationsNeeded; i++){
          
        try{
          if(i == 0){
                //Need to calculate the beginning of the second day.
                startDayEnd.setTime(start.getTime());
                startDayEnd.set(Calendar.MILLISECOND,0);
                startDayEnd.set(Calendar.SECOND,0);
                startDayEnd.set(Calendar.MINUTE,0);
                startDayEnd.set(Calendar.HOUR_OF_DAY,0);
                startDayEnd.roll(Calendar.DAY_OF_YEAR,1);
                //System.err.println("StartDayEnd is: " + startDayEnd.getTime() + "\n");
                entData = pushEntry(newact, calid, start, startDayEnd,title,description,priority,location,category,organizer);
                //Find the end of the second to last day
                //for the next pass

                //dateCalc is now equal to the end time
                lastDayStart.setTime(end.getTime());
                lastDayStart.set(Calendar.MILLISECOND,0);
                lastDayStart.set(Calendar.SECOND,0);
                lastDayStart.set(Calendar.MINUTE,0);
                lastDayStart.set(Calendar.HOUR_OF_DAY,0);
 
          }else if(i == lastIteration){

                //System.err.println("lastDayStart is: " + lastDayStart.getTime() + "\n");
                entData = pushEntry(newact, calid, lastDayStart, end,title,description,priority,location,category,organizer);
          }else{
                //System.err.println("lastDayStart is: " + lastDayStart.getTime() + "\n");
                entData = pushEntry(newact, calid, startDayEnd, lastDayStart,title,description,priority,location,category,organizer);
          }
        }catch(Exception e){
          throw e;
        }
        attributeMap.put("Event" + i,entData.getCeid());
 
      }


      return newact;
      
    }

    /**
    * <p>
    * Creates the recurrence data structure for an activation's event
    * <p>
    * @param newact - a newly created activation
    * @param start - Calendar instance representing the start time of the activation
    * @param end - Calendar instance representing the end time of the activation
    * @param range - the number of days for which the recurrence should be created
    * <p>
    * @return RecurrenceData[] - Array of reccurrence data for an event
    */

    private RecurrenceData[] setRecurrence(Activation newact,Calendar start,Calendar end,int range){
       //Assuming that the start date is before the end date.
       RecurrenceData recData = new RecurrenceData();
       //Activation is spread out over at least three days.
       if (range >= 2){

           //System.err.println("Getting the until calendar\n"); 
           Calendar until = Calendar.getInstance();
           until.setTime(end.getTime());
           until.set(Calendar.MILLISECOND,0);
           until.set(Calendar.SECOND,0);
           until.set(Calendar.MINUTE,0);
           until.set(Calendar.HOUR_OF_DAY,0);
           until.roll(Calendar.DAY_OF_YEAR,-1);
          
           //System.err.println("Setting up the recurrence data\n"); 
           //System.err.println("putting in the recurrence rule\n");

           //if(recData == null){
             //System.err.println("Recdata is null!\n");
           //}else{
             //System.err.println("Recdata is not null...\n");
           //}
           recData.putRule("DAILY",new Integer(1),until.getTime());
           //System.err.println("Leaving setRecurrence\n");        
       }


       RecurrenceData[] returnMe = {recData};
        
       return returnMe;
    }

    /**
    * <p>
    * Creates an entry data for an activation event
    * <p>
    * @param newact - a new activation object
    * @param start - calendar instance representing the start time of the event to be created
    * @param end - calendar instance representing the end time of the event to be created
    * @param calid - identifier for the calendar that events will be created on
    * @param title - title for the event
    * @param description - description for the event
    * @param priority - the priority for the event (see Event.java and event.xsl)
    * @param location - the location of the event
    * @param category - a category to place the event under
    * @param organizer - the individual whom organized the event
    * <p>
    * @return EntryData - An EntryData object for an activation event
    * <p>
    * @throws Exception when the calendar server cannot be accessed or a failure occurs when writing to the
    * database
    */
    private EntryData pushEntry(Activation newact, String calid, Calendar start, Calendar end,String title, String description, int priority, String location, String category, String organizer) throws Exception{
       EntryData entData = new EntryData();
       long length = end.getTimeInMillis() - start.getTimeInMillis();
       DurationData activationDur = new DurationData();
       Calendar beginningSecondDay = Calendar.getInstance();
        
       //if the end date is set to the 12:00:00:00 a.m. we want this to show up on only one day..
       beginningSecondDay.setTime(start.getTime());
       beginningSecondDay.set(Calendar.MILLISECOND,0);
       beginningSecondDay.set(Calendar.SECOND,0);
       beginningSecondDay.set(Calendar.MINUTE,0);
       beginningSecondDay.set(Calendar.HOUR_OF_DAY,0);
       beginningSecondDay.roll(Calendar.DAY_OF_YEAR,1);

       Date startDay = start.getTime();
       Date begSecDay = beginningSecondDay.getTime();       
       int range = 0;


       if(startDay.compareTo(begSecDay) != 0){
         range = end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
       }    

       CalendarServer server = null;

       //System.err.println("Getting ready to access the calendar server\n");

       try{ 
         server = CalendarServer.getInstance(props);
       }catch(Exception e){
          throw new Exception("Could not access the calendar server\n");
       }


       //System.err.println("Setting up the duration\n");

       //set up the time for the assessment/activation
       TimeLength durInMilli = new TimeLength();

       //System.err.println("created TimeLength\n");
       //System.err.println("Length is: " + length + "\n");
       //System.err.println("milliseconds_in_day is: " + milliseconds_in_day + "\n");
       if(milliseconds_in_day <= length){
         durInMilli.setAllDay(); 
       }else{
         durInMilli.setLength(length);
       }

       //System.err.println("Puttin start time\n");
       activationDur.putStart(start.getTime());
       //System.err.println("Putting the end time\n");
       activationDur.putEnd(end.getTime());
       //System.err.println("timelength set\n");
       activationDur.putLength(durInMilli);


       //System.err.println("Setting the day of week\n");

       int day_of_week = start.get(Calendar.DAY_OF_WEEK);
       String dow = "";

       switch (day_of_week){
          case Calendar.SUNDAY:
            dow = "Sunday";
            break;
          case Calendar.MONDAY:
            dow = "Monday";
            break;
          case Calendar.TUESDAY:
            dow = "Tuesday";
            break;
          case Calendar.WEDNESDAY:
            dow = "Wednesday";
            break;
          case Calendar.THURSDAY:
            dow = "Thursday";
            break;
          case Calendar.FRIDAY:
            dow = "Friday";
            break;
          case Calendar.SATURDAY:
            dow = "Saturday";
            break;

       } 
       

       activationDur.putDOW(dow);

       //System.err.println("Putting in the duration\n");
       entData.putDuration(activationDur);

       //System.err.println("Putting the location\n");
       entData.putLocation(location);

       //System.err.println("Setting the event up\n");

       //set up the event data
       try{
         entData.putEvent(createEvent(title,category,priority,description)); 
       }catch(Exception e){
          throw e;
       }

       //System.err.println("Setting the recurrence up\n");
       //System.err.println("Range is: " + range + "\n");
              //recurrence for activations longer than 2 days
       entData.putRecurrence(setRecurrence(newact,start,end,range));
       AttendeeData[] attend = null;

       if(newact.forAllUsers()){
         //Set it up at the calendar level to share with entire class.
         AttendeeData attData = new AttendeeData(new IdentityData(
              IdentityData.ENTITY, GroupData.S_OBJECT,
              "" + newact.getOfferingID(), calid, calid));
         attend = new AttendeeData[1];
         attend[0] = attData; 
       }else{
         List users = newact.getUsernames();
         int numUsers = users.size();
         attend = new AttendeeData[users.size()];

         for(int i=0; i < numUsers; i++){
            IdentityData user = new IdentityData();
            //System.err.println("Looking for: " + ((String)users.get(i)) + "\n");
            user.putAlias((String)users.get(i));
            user.putType(IdentityData.ENTITY);
            user.putEntityType(GroupData.S_USER);
            pFinder.fillDetail(user,null);
            //System.err.println("Printing out user: " + user.toString());
            AttendeeData attendent = new AttendeeData(user);
            attendent.putStatus("ACCEPTED");
            attend[i] = attendent;
         }

       }

       //System.err.println("Setting up the organizer\n");    

       if(organizer != null || !(organizer.equals(""))){
          OrganizerData organizerRad = new OrganizerData();
          organizerRad.putCuid(organizer);          
          entData.putOrganizer(organizerRad);
       }

       //System.err.println("Setting up the atendees\n");

       entData.putAttendee(attend);

       EntryData newEntry = null;

       //System.err.println("Creating the new entries in the database\n\n");

       IdentityData logon = new IdentityData(
              IdentityData.ENTITY, GroupData.S_OBJECT,
              "" + newact.getOfferingID(), calid, calid);
          
       try{
         server.login(logon,null);
         newEntry = server.createEntry(calid, entData);
       }catch(Exception e){
          throw new Exception("Could not create entries in the database: ",e);
       }


       return newEntry;

    }

}
