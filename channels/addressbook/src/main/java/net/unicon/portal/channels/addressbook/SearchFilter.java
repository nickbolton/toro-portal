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

package net.unicon.portal.channels.addressbook;

import net.unicon.academus.apps.rad.Filter;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.GroupData;

public class SearchFilter implements Filter {
  String m_fullname = null;
  String m_email = null;
  String m_title = null;
  String m_department = null;

  public SearchFilter(String name, String title, String department, String email) {
    m_fullname = (name==null) ? "" : name;
    m_title = (title==null) ? "" : title;
    m_department = (department==null) ? "" : department;
    m_email = (email==null) ? "" : email;
  }

  /**
   * From interface Filter. must be initialized.
   */
  public Object convert( Object obj) {
    if( obj instanceof IdentityData) {
      IdentityData fd = (IdentityData)obj;
      boolean flag = true;
      String name = fd.getName() == null ? "": fd.getName().toUpperCase();
      String email = fd.getEmail() == null ? "": fd.getEmail().toUpperCase();
      //Not yet for title and department
      if(m_department.length()>0||m_title.length()>0)
        return null;
      //Name
      if(name.indexOf(m_fullname.toUpperCase())==-1)
        flag = false;
      //Email
      if(GroupData.isPortalGroup(fd)) {
        if(m_email.length()>0)
          flag = false;
      } else
        if(email.indexOf(m_email.toUpperCase())==-1)
          flag = false;

      if(flag)
        return obj;
      else
        return null;
    } else
      return null;
  }
}
