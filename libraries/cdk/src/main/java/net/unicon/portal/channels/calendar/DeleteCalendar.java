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
package net.unicon.portal.channels.calendar;
import java.io.*;
import java.util.*;

import net.unicon.academus.apps.calendar.*;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;
public class DeleteCalendar extends CalendarScreen {
  String m_calid = null;
  String m_calname = null;
  Setup m_back = null;
  public XMLData getData() throws Exception {
    XMLData data = new XMLData();
    XMLData delete = new XMLData();
    delete.putA("calid", m_calid);
    delete.putA("calname", m_calname);
    data.putE("delete", delete);
    return data;
  }
  public void init(Hashtable params) throws Exception {
    super.init(params);
    m_calid = (String)getParameter("calid");
    m_calname = (String)getParameter("calname");
    m_back = (Setup)getScreen((String)getParameter("back"));
  }
  public Screen ok(Hashtable params) throws Exception {
    params.put("calid", m_calid);
    return m_back.deleteCalendar(params);
  }
  public Screen cancel(Hashtable params) throws Exception {
    return m_back;
  }
}
