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

import net.unicon.academus.apps.rad.XMLData;

/**
 * This class reprents "organizer" of event/todo/invitation.
 */
public class OrganizerData extends XMLData {
  // <organizer cuid='cuid' sent-by='sent-by'\>

  /**
   * Get calendar user id.
   * @return
   */
  public String getCuid() {
    return (String)getA("cuid");
  }

  /**
   * Set calendar user id.
   * @param cuid
   */
  public void putCuid(String cuid) {
    putA("cuid", cuid);
  }

  /**
   * Get sender of event/todo/invitation.
   * @return
   */
  public String getSentby() {
    return (String)getA("sent-by");
  }

  /**
   * Set sender for event/todo/invitation.
   * @param sentby
   */
  public void putSentby(String sentby) {
    putA("sent-by", sentby);
  }
}
