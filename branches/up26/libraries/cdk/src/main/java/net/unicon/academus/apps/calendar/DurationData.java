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
 * This class represents durations.
 */
public class DurationData extends XMLData {
  // <duration dow='Sunday/Monday/.../Saturday' start='M/d/yy h:mm a' end='M/d/yy h:mm a' length='all-day'|'h:mm' blocked='true|false'/>

  /**
   * Get day of week.
   * @return
   */
  public String getDOW() {
    return (String)getA("dow");
  }

  /**
   * Set day of week.
   * @param dow
   */
  public void putDOW(String dow) {
    putA("dow", dow);
  }

  /**
   * Get start date of duration.
   * @return
   */
  public Date getStart() {
    return (Date)getA("start");
  }

  /**
   * Set start date for duration.
   * @param start
   */
  public void putStart(Date start) {
    putA("start", start);
  }

  /**
   * Get length of time of duration.
   * @return
   */
  public TimeLength getLength() {
    return (TimeLength)getA("length");
  }

  /**
   * Set length of time of duration.
   * @param length
   */
  public void putLength(TimeLength length) {
    putA("length", length);
    if (length.getAllDay())
      removeA("end");
    else
      putA("end", new Date(getStart().getTime()+length.getLength()));
  }

  /**
   * Get end time of duration.
   * @return
   */
  public Date getEnd() {
    return (Date)getA("end");
  }

  /**
   * Set end time of duration.
   * @param end
   */
  public void putEnd(Date end) {
    putA("end", end);
    putLength(new TimeLength(end.getTime()-getStart().getTime()));
  }

  /**
   * Set length of time is full of day.
   */
  public void putAllDay() {
    putLength(new TimeLength());
    removeA("end");
  }
  public Boolean getBlocked() {
    return (Boolean)getA("blocked");
  }
  public void putBlocked(Boolean blocked) {
    putA("blocked", blocked);
  }
}
