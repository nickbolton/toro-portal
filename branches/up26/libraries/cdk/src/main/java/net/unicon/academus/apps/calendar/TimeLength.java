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
import java.text.*;

/**
 * This class represents length of time.
 */
public class TimeLength {
  boolean m_allday = true;
  long m_length = 0;  // milliseconds

  /**
   * Default constructor. The length is all of day.
   */
  public TimeLength() {
    setAllDay();
  }

  /**
   * Constructor with given length of time. It is measured by miliseconds.
   * @param length
   */
  public TimeLength(long length) {
    setLength(length);
  }

  /**
   * Check whether length of time is full of day or not.
   * @return
   */
  public boolean getAllDay() {
    return m_allday;
  }

  /**
   * Set the length is full of day.
   */
  public void setAllDay() {
    m_allday = true;
    m_length = 0;
  }

  /**
   * Get length of time. It's measured by miliseconds.
   * @return
   */
  public long getLength() {
    return m_length;
  }

  /**
   * Set length of time. It's measured by miliseconds.
   * @param length
   */
  public void setLength(long length) {
    m_allday = false;
    m_length = length;
  }

  /**
   * Get string-format of length of time.
   * @return
   */
  public String toString() {
    if (m_allday)
      return "all-day";
    int hours = (int)(m_length/3600000);
    int minutes = (int)((m_length%3600000)/60000);
    int seconds = (int)(((m_length%3600000)%60000)/1000);
    return ""+hours+":"+(minutes < 10? "0"+minutes:""+minutes);
  }
}
