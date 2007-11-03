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
 * This class represents events. It is xml-based object.
 */
public class EventData extends XMLData {
  // <event status='status' priority='priority'>summary
  //   <description>...</description>
  //   <category>...</category>
  // </event>

  /**
   * Get status of event.
   * @return
   */
  public String getStatus() {
    return (String)getA("status");
  }

  /**
   * Set status for event.
   * @param status
   */
  public void putStatus(String status) {
    putA("status", status);
  }

  /**
   * Get priority of event.
   * @return
   */
  public Integer getPriority() {
    return (Integer)getA("priority");
  }

  /**
   * Set priority of event.
   * @param priority
   */
  public void putPriority(Integer priority) {
    putA("priority", priority);
  }

  /**
   * Get description of event.
   * @return
   */
  public String getDescription() {
    return (String)getE("description");
  }

  /**
   * Set description of event.
   * @param description
   */
  public void putDescription(String description) {
    putE("description", description);
  }

  /**
   * Get list of categories that event belong to.
   * @return
   */
  public String[] getCategory() {
    return (String[])getE("category");
  }

  /**
   * Set list of catogories for event.
   * @param category
   */
  public void putCategory(String[] category) {
    putE("category", category);
  }
}
