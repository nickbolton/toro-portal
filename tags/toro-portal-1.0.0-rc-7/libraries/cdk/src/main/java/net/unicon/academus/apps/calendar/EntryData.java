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

import java.util.*;

import net.unicon.academus.apps.rad.XMLData;

/**
 * This class describes entries that are events or todos.
 */
public class EntryData extends XMLData {
  // <entry ceid='ceid' share='share' created='created' last-modified='last-modified' revision='revision'>
  //   <duration/>
  //   <location>...</location>
  //   <organizer>...</organizer>
  //   <attendee>...</attendee>
  //   <event|todo>...</event|todo>
  //   <alarm>...</alarm>
  //   <recurrence/>
  // </entry>

  boolean m_isInvitation = false;
  boolean m_isDeclined = false;

  /**
   * Check whether this entry is an event or not.
   * @return
   */
  public boolean isEvent() {
    return (getEvent() != null && !isDeclined() && !isInvitation());
  }

  /**
   * Check whether this entry is a todo or not.
   * @return
   */
  public boolean isTodo() {
    return (getTodo() != null);
  }

  /**
   * Get list of recurrent duration.
   * @return
   */
  public boolean isRecurrent() {
    RecurrenceData[] recc = getRecurrence();
    return (recc != null && recc.length > 0);
  }

  /**
   * Check whether this entry is recurrent or not.
   * @return
   */
  public boolean isRecurrences() {
    if (getCeid().indexOf(".") != -1)
      return true;
    else
      return false;
  }

  /**
   * Check whether this entry is an invitation or not.
   * @return
   */
  public boolean isInvitation() {
    return m_isInvitation;
  }

  /**
   * Check whether this entry is declined or not.
   * @return
   */
  public boolean isDeclined() {
    return m_isDeclined;
  }

  /**
   * Check this entry is an accepted invitation or not.
   * @param cal. Input
   * @param user. Input
   * An entry maybe is both "event" and is "accepted invitation"
   */
  public boolean isAcceptedInvitation(CalendarData cal, String user) {
    if (isTodo())
      return false;

    AttendeeData att = findAttendee(user);
    if (cal.isOwner(user) && att != null && att.getStatus() != null &&
        att.getStatus().equals("ACCEPTED") &&
        getOrganizer().getCuid() != null &&
        !getOrganizer().getCuid().equals(user))
      return true;

    return false;
  }

  /**
   * Check this entry is an accepted invitation or not.
   * @param cal
   * @param id
   * @param user
   * @return
   */
  public boolean isAcceptedInvitation(CalendarData cal, String id, String user) {
    if (isTodo())
      return false;

    AttendeeData att = findAttendeeById(id);
    if (cal.isOwner(user) && att != null && att.getStatus() != null &&
        att.getStatus().equals("ACCEPTED") &&
        getOrganizer().getCuid() != null &&
        !getOrganizer().getCuid().equals(user))
      return true;

    return false;
  }

  /**
   * Set this entry is an opening invitation.
   * @param isInvitation
   */
  public void setIsInvitation(boolean isInvitation) {
    m_isInvitation = isInvitation;
  }

  /**
   * Set this entry is a declined invitation.
   * @param isDeclined
   */
  public void setIsDeclined(boolean isDeclined) {
    m_isDeclined = isDeclined;
  }

  /**
   * Get calendar entry identifier.
   * @return
   */
  public String getCeid() {
    return (String)getA("ceid");
  }

  /**
   * Set calendar entry identifier for entry.
   * @param ceid
   */
  public void putCeid(String ceid) {
    putA("ceid", ceid);
  }

  public String getShare() {
    return (String)getA("share");
  }
  public void putShare(String share) {
    putA("share", share);
  }

  /**
   * Get created date  of entry.
   * @return
   */
  public Date getCreated() {
    return (Date)getA("created");
  }

  /**
   * Get lastest modified date of entry.
   * @return
   */
  public Date getLastModified() {
    return (Date)getA("last-modified");
  }
  public Integer getRevision() {
    return (Integer)getA("revision");
  }

  /**
   * Get duration of entry.
   * @return
   */
  public DurationData getDuration() {
    return (DurationData)getE("duration");
  }

  /**
   * Set duration of entry.
   * @param duration
   */
  public void putDuration(DurationData duration) {
    putE("duration", duration);
  }

  /**
   * Get location of entry.
   * @return
   */
  public String getLocation() {
    return (String)getE("location");
  }

  /**
   * Set location for entry.
   * @param location
   */
  public void putLocation(String location) {
    putE("location", location);
  }

  /**
   * Get organizer of entry.
   * @return
   */
  public OrganizerData getOrganizer() {
    return (OrganizerData)getE("organizer");
  }

  /**
   * Set organizer for entry.
   * @param organizer
   */
  public void putOrganizer(OrganizerData organizer) {
    putE("organizer", organizer);
  }

  /**
   * Get list of attendees of entry.
   * @return
   */
  public AttendeeData[] getAttendee() {
    return (AttendeeData[])getE("attendee");
  }

  /**
   * Set list of attendees for entry.
   * @param attendee
   */
  public void putAttendee(AttendeeData[] attendee) {
    putE("attendee", attendee);
  }

  /**
   * Get information of entry as an event.
   * @return
   */
  public EventData getEvent() {
    return (EventData)getE("event");
  }

  /**
   * Set information of entry as an event.
   * @param event
   */
  public void putEvent(EventData event) {
    putE("event", event);
    putE("todo",null);
  }

  /**
   * Get information of entry as a todo.
   * @return
   */
  public TodoData getTodo() {
    return (TodoData)getE("todo");
  }

  /**
   * Set information of entry as a todo.
   * @param todo
   */
  public void putTodo(TodoData todo) {
    putE("todo", todo);
    putE("event",null);
  }

  /**
   * Get title of entry.
   * @return
   */
  public String getTitle() {
    return (getEvent() != null) ? getEvent().get() : ((getTodo() != null) ? getTodo().get() : null);
  }
  public String[] getRelatedTos() {
    return (String[]) getE("relatedTos");
  }
  public String[] getResources() {
    return (String[]) getE("resources");
  }


  /**
   * Get list of alarms of entry.
   * @return
   */
  public AlarmData[] getAlarm() {
    return (AlarmData[])getE("alarm");
  }

  /**
   * Set list of alarms of entry.
   * @param alarm
   */
  public void putAlarm(AlarmData[] alarm) {
    putE("alarm", alarm);
  }

  /**
   * Get list of recurrences of entry.
   * @return
   */
  public RecurrenceData[] getRecurrence() {
    return (RecurrenceData[])getE("recurrence");
  }

  /**
   * Set list for recurrences of entry.
   * @param recurrence
   */
  public void putRecurrence(RecurrenceData[] recurrence) {
    putE("recurrence", recurrence);
  }

  public void putRelatedTos(String relatedTos[]) {
    putE("relatedTos",relatedTos);
  }
  public void putResources(String resources[]) {
    putE("resources",resources);
  }


  public static int indexOf( EntryData[] entries, String ceid) {
    if (entries != null)
      for (int i = 0; i < entries.length; i++)
        if (ceid.equals(entries[i].getCeid()))
          return i;
    return -1;
  }

  /**
   * Find an entry by id from array of entries.
   * @param entries
   * @param ceid
   * @return
   */

  public static EntryData findEntry( EntryData[] entries, String ceid) {
    if (entries != null)
      for (int i = 0; i < entries.length; i++)
        if (ceid.equals(entries[i].getCeid()))
          return entries[i];
    return null;
  }

  /**
   * Find an attendde by name of user.
   * @param user
   * @return
   */
  public AttendeeData findAttendee(String user) {
    AttendeeData[] attr = getAttendee();
    if (attr != null)
      for (int i=0; i<attr.length; i++) {
        if (attr[i].get().equals(user))
          return attr[i];
      }
    return null;
  }

  /**
   * Find an attendee by identifer.
   * @param iid
   * @return
   */
  public AttendeeData findAttendeeById(String iid) {
    AttendeeData[] attr = getAttendee();
    if (attr != null)
      for (int i=0; i<attr.length; i++) {
        if (attr[i].getID().equals(iid))
          return attr[i];
      }
    return null;
  }

  public static void removeDup(CalendarData[] cals, String user) {
    if (cals != null) {

      HashMap map = new HashMap();
      for (int i = 0; i < cals.length; i++) {
        if (cals[i] != null && cals[i].getOwner().equals(user)) {
          EntryData[] entr = cals[i].getEntry();
          if (entr != null) {
            for (int j = 0; j < entr.length; j++) {
              if (entr[j] != null)
                map.put(entr[j].getCeid(), "yes");
            }
          }
        }
      }
      for (int i = 0; i < cals.length; i++) {
        if (cals[i] != null && !cals[i].getOwner().equals(user)) {
          EntryData[] entr = cals[i].getEntry();
          if (entr != null) {
            for (int j = 0; j < entr.length; j++) {
              String oldEnt = (String)map.get(entr[j].getCeid());
              if (oldEnt != null) {
                entr[j] = null;
              } else if (oldEnt == null) {
                map.put(entr[j].getCeid(), "yes");
              }
            }
          }
        }
      }

    }
  }
}
