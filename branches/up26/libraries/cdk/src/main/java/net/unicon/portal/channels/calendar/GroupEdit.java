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

/**
 * <p>Title: Add/Edit Composite view of Calendars</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author nntruong@ibs-dp.com
 * @version 1.0
 */

import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;

import java.util.Hashtable;

public class GroupEdit extends CalendarScreen {
  public GroupEdit() {}

  public String sid() {
    return "GroupEdit";
  }

  String m_id = "";
  String m_name = "";

  public void init(Hashtable params) throws Exception {
    super.init(params);
    m_id="";
    m_name="";
  }

  public Hashtable getXSLTParams() {
    Hashtable params = super.getXSLTParams();
    params.put("id", m_id);
    params.put("name", m_name);
    return params;
  }

  public void set
    (String m_id, String m_name) {
    this.m_id = m_id;
    this.m_name = m_name;
  }

  public XMLData getData() throws Exception {

    return new XMLData();
  }

  public Screen update(Hashtable params) throws Exception {
    //System.out.println("params " + params);
    String id = (String)params.get("id");
    String name = (String)params.get("groupname");
    if (id == null || id.equals(""))
      return addGroup(name);
    else if (name != null && !name.trim().equals("")) {
      XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
      if (data == null)
        data = new XMLData();
      Object o = (Object)data.getE("calendar-group");
      if (o != null && o instanceof Object[]) {
        Object[] grs = (Object[])o;
        for (int i = 0; i < grs.length; i++) {
          // JK - fix for TT03810 - move break inside if statement
          if (((XMLData)grs[i]).getA("id").equals(id)) {
            ((XMLData)grs[i]).putA("name", name);
            break;
          }
        }
      } else if (o != null) {
        XMLData gr = (XMLData)o;
        gr.putA("name", name);
      }
      m_channel.setUserProfile(data, CCalendar.USERDATA_KEY);
    }

    return getScreen("Preference");
  }

  private Screen addGroup(String name) throws Exception {
    if (name != null && !name.trim().equals("")) {
      XMLData data = m_channel.getUserProfile(CCalendar.USERDATA_KEY);
      if (data == null)
        data = new XMLData();
      Object o = data.getE("calendar-group");
      XMLData gr = new XMLData();
      gr.putA("name", name);
      gr.putA("id", "group_"+(new Long(System.currentTimeMillis())).toString());
      if (o != null) {
        if (o instanceof Object[]) {
          Object[] os = (Object[])o;
          XMLData[] newgrs = new XMLData[os.length + 1];
          for (int i = 0; i < (newgrs.length - 1); i++)
            newgrs[i] = (XMLData)os[i];
          newgrs[newgrs.length-1] = gr;
          data.putE("calendar-group", newgrs);
        } else if (o instanceof XMLData)
          data.putE("calendar-group", new XMLData[]{(XMLData)o, gr});
      } else
        data.putE("calendar-group", gr);
      m_channel.setUserProfile(data, CCalendar.USERDATA_KEY);
    }
    return getScreen("Preference");
  }

}
