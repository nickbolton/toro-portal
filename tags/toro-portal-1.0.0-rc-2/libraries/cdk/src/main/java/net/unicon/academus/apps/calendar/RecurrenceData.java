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
 * This class represents time recurrence of event/todo/invitation.
 */
public class RecurrenceData extends XMLData {
  // <recurrence exclude='true|false' date='date' frequency='frequency' interval='interval' count='count' until='until'\>

  /**
   * Check whether the recurrent is be excluded or included.
   * @return true if it's excluded.
   */
  public Boolean getExclude() {
    return (Boolean)getA("exclude");
  }

  /**
   * Set "exclude" for the recurrent.
   * @param exclude
   */
  public void putExclude(Boolean exclude) {
    putA("exclude", exclude);
  }

  /**
   * Get date of the recurrence.
   * @return
   */
  public Date getDate() {
    return (Date)getA("date");
  }

  /**
   * Get "frequency" of recurrence.
   * @return
   */
  public String getFrequency() {
    return (String)getA("frequency");
  }

  /**
   * Get "interval" of recurrence.
   * @return
   */
  public Integer getInterval() {
    return (Integer)getA("interval");
  }

  /**
   * Get number of repeats.
   * @return
   */
  public Integer getCount() {
    return (Integer)getA("count");
  }

  /**
   * Get the end time of the recurrence.
   * @return
   */
  public Date getUntil() {
    return (Date)getA("until");
  }

  public String getByDay() {
    return (String)getA("byday");
  }
  public void putByDay(String days) {
    putA("byday", days);
  }


  /**
   * Set date for the recurrence.
   * @param date
   */
  public void putDate(Date date) {
    putA("date", date);
    removeA("frequency");
    removeA("interval");
    removeA("count");
    removeA("until");
  }

  /**
   * Set rule for the recurrence.
   * @param frequency
   * @param interval
   * @param count
   */
  public void putRule(String frequency, Integer interval, Integer count) {
    putA("frequency", frequency);
    putA("interval", interval);
    putA("count", count);
    removeA("until");
    removeA("date");
  }

  /**
   * Set rule for the recurrence.
   * @param frequency
   * @param interval
   * @param until
   */
  public void putRule(String frequency, Integer interval, Date until) {
    putA("frequency", frequency);
    putA("interval", interval);
    putA("until", until);
    removeA("count");
    removeA("date");
  }
  /*
    public void putRule(String frequency, Integer interval)
    {
      putA("frequency", frequency);
      putA("interval", interval);
   
      removeA("count");
      removeA("date");
      removeA("until");
    }
  */
}
