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
package net.unicon.portal.channels.rad;

import java.util.Comparator;

import net.unicon.academus.apps.rad.IdentityData;
/**
 * It is used to compare two elements in sort algorithm.
 */
public class IdentityComparator implements Comparator {
  /**
   * Create a new instance with empty value.
   */
  public IdentityComparator() {}
  /**
   * Override in <code>Comparator</code>.
   */
  public int compare(Object obj1 , Object obj2) {
    IdentityData tmp1 = (IdentityData)obj1;
    IdentityData tmp2 = (IdentityData)obj2;
    if(tmp1.getName()==null&&tmp2.getName()==null)
      return 0;
    else {
      if(tmp1.getName()==null)
        return -1;
      if(tmp2.getName()==null)
        return 1;
    }
    return tmp1.getName().compareToIgnoreCase(tmp2.getName());
  }
}
