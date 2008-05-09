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
 * This class represents Alarm.
 * It is xml-based object.
 */
public class AlarmData extends XMLData {
  // <alarm action='action' trigger='trigger'>
  //   <recipient>cuid<\recipient>
  // <\alarm>

  /**
   * Get name of action.
   * @return
   */
  public String getAction() {
    return (String)getA("action");
  }

  /**
   * Set name of action.
   * @param action
   */
  public void putAction(String action) {
    putE("action", action);
  }

  /**
   * Get date of trigger.
   * @return
   */
  public Date getTrigger() {
    return (Date)getA("trigger");
  }

  /**
   * Set date for trigger.
   * @param trigger
   */
  public void putTrigger(Date trigger) {
    putA("trigger", trigger);
  }

  /**
   * Get list of recipients of the alarm.
   * @return array of recipients.
   */
  public String[] getRecipient() {
    return (String[])getE("recipient");
  }

  /**
   * Set list of recipients of the alarm.
   * @param recipient array of recipients.
   */
  public void putRecipient(String[] recipient) {
    putE("recipient", recipient);
  }
}
