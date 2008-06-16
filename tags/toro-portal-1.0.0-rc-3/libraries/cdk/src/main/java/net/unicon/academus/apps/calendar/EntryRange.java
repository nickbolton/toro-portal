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

/**
 * This represents a range of entry.
 */
public class EntryRange {
  public static final int PAST = 1;
  public static final int FUTURE = 2;
  public static final int ALL = 3;

  /**
   * Entry identifier.
   */
  public String m_ceid = null;

  /**
   * Mode
   */
  public int m_mod = 0;

  /**
   * Constructor with given entry identifier.
   * @param ceid
   */
  public EntryRange(String ceid) {
    m_ceid = ceid;
  }

  /**
   * Constructor with given entry id and mode.
   * @param ceid
   * @param mod
   */
  public EntryRange(String ceid, int mod) {
    m_ceid = ceid;
    m_mod = mod;
  }
}
