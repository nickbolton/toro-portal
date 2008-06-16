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
 * This class represents Completion that indicates whether a todo have been
 * completed or not.
 */
public class CompletionData extends XMLData {
  // <completion percent='percent' completed='completed'/>
  /**
   * Constructor with completed data.
   * @param completed
   * @param percent
   */
  public CompletionData(Date completed, Integer percent) {
    putCompleted(completed);
    putPercent(percent);
  }

  /**
   * Get completed date of todo.
   * @return
   */
  public Date getCompleted() {
    return (Date)getA("completed");
  }

  /**
   * Set completed data for todo.
   * @param completed
   */
  public void putCompleted(Date completed) {
    putA("completed", completed);
  }

  /**
   * Get percent of completion of todo.
   * @return
   */
  public Integer getPercent() {
    return (Integer)getA("percent");
  }

  /**
   * Set percent of completion for todo.
   * @param percent
   */
  public void putPercent(Integer percent) {
    putA("percent", percent);
  }
}
