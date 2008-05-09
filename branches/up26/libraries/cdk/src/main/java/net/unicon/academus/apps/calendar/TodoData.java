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
 * This class represents "todo".
 */
public class TodoData extends XMLData {
  // <todo status='status' priority='priority'>summary
  //   <description>...</description>
  //   <category>...</category>
  //   <completion/>
  // </todo>

  /**
   * Get status of todo.
   * @return
   */
  public String getStatus() {
    return (String)getA("status");
  }

  /**
   * Set status for todo.
   * @param status
   */
  public void putStatus(String status) {
    putA("status", status);
  }

  /**
   * Get priority of todo.
   * @return
   */
  public Integer getPriority() {
    return (Integer)getA("priority");
  }

  /**
   * Set priority of todo.
   * @param priority
   */
  public void putPriority(Integer priority) {
    putA("priority", priority);
  }

  /**
   * Get description of todo.
   * @return
   */
  public String getDescription() {
    return (String)getE("description");
  }

  /**
   * Set description for todo.
   * @param description
   */
  public void putDescription(String description) {
    putE("description", description);
  }

  /**
   * Get list of categories that the todo belongs to.
   * @return
   */
  public String[] getCategory() {
    return (String[])getE("category");
  }

  /**
   * Set list of categories that the todo belongs to.
   * @param category
   */
  public void putCategory(String[] category) {
    putE("category", category);
  }

  public Date getDue() {
    return (Date)getE("due");
  }
  public void putDue(Date due) {
    putE("due", due);
  }

  /**
   * Get completion of todo.
   * @return
   */
  public CompletionData getCompletion() {
    return (CompletionData)getE("completion");
  }

  /**
   * Set comletion for todo.
   * @param completion
   */
  public void putCompletion(CompletionData completion) {
    putE("completion", completion);
  }

}
