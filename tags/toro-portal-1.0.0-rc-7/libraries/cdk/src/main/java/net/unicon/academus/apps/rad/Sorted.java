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


package net.unicon.academus.apps.rad;

import java.util.*;

import net.unicon.academus.apps.rad.Filter;

import java.io.Serializable;

/**
 * General class with sort ability. It implements the insert sort algorithm.
 */
public class Sorted extends Vector
  implements Serializable {
  /**
   * The comparator used when sorting
   */
  Comparator m_comparator = null;

  /**
   * Constructor with the comparator
   * @param comp The comparator used in sort operation
   */
  public Sorted( Comparator comp) {
    m_comparator = comp;
  }

  //----Applied when search--------------------------------------------------//

  /**
   * Extract from this Sorted set those elements that meets the given filter.
   * @param f The filter to select element in this Sorted object. It implements the interface Filter
   * @return a subset of this Sorted object that meets the given filter.
   */
  public Sorted filter( Filter f) {
    Sorted other = new Sorted( m_comparator);
    for( int i = 0; i < size(); i++) {
      Object output = f.convert(elementAt(i));
      if( output != null)
        other.addElement(output);
    }
    return other;
  }

  /**
   * Extract from this Sorted set those elements that meets the given filter and
   * then sort the result by given comparator.
   * @param f The filter to select element in this Sorted object. It implements the interface Filter
   * @param comp The comparator applied to the result of filter.
   * @return a subset of this Sorted object that meets the given filter and is sorted by given comparator
   */
  public Sorted filter( Filter f, Comparator comp) {
    Sorted other = new Sorted( comp);
    for( int i = 0; i < size(); i++) {
      Object output = f.convert(elementAt(i));
      if( output != null)
        other.add(output);
    }
    return other;
  }

  //-----Add/Remove elements with sorting------------------------------------//

  /**
   * Find the position of a Object in this Sorted set.
   * @param obj The Object to find.
   * @return The position of given obj in this Sorted set or -1 if not found.
   */
  public int find(Object obj) {
    int index = binary( obj, 0, size());
    for (int i = index; i < size(); i++) {
      Object gd = elementAt(i);
      if( obj.equals(gd))
        return i;

      if( m_comparator.compare(obj, gd) > 0)
        break;
    }
    return -1;
  }

  /**
   * Add a new element to this Sorted set.
   * @param data The new element to add
   * @return true
   */
  public boolean add
    ( Object data) {
    add
      (binary( data, 0, size()), data);
    return true;
  }

  /**
   * Add entire a collection to this Sorted set.
   * @param c The collection to add
   * @return true
   */
  public boolean addAll(Collection c) {
    Iterator i = c.iterator();
    if(i==null)
      return false;
    while(i.hasNext()) {
      Object ob = i.next();
      this.add(ob);
    }
    return true;
  }

  /**
   * Remove a element from this Sorted set.
   * @param data The element to remove.
   * @return true if successfully remove.
   * If the given element is not found in this set, the false value will return.
   */
  public boolean remove
    ( Object data) {
    int index = find( data);
    if (index != -1) {
      remove
        (index);
      return true;
    } else
      return false;
  }

  //--------------------------------------------------------------------------//

  int binary( Object obj, int from, int to) {
    if (to-from <= 5) {
      for (int i = from; i < to; i++)
        if( m_comparator.compare(obj, elementAt(i)) <= 0)
          return i;
      return to;
    }

    int mid = (from+to)/2;
    int comp = m_comparator.compare(obj, elementAt(mid));
    if( comp < 0)
      return binary(obj, from, mid+1);
    else if( comp > 0)
      return binary(obj, mid, to);
    else
      return mid;
  }
}
