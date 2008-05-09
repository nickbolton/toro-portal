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
package com.interactivebusiness.classifieds.data;
import java.util.*;
import java.sql.Timestamp;
/** <p>a data object that holds informaiton about a list of notes.
 * The next() and the getxxx() methods return the topics as elements of
 * an ordered list.
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */
public class TopicList implements java.io.Serializable
{
    private java.util.ArrayList  topicNameList;
    private java.util.ArrayList  topicIDList;
    private java.util.ArrayList  totalEntryList;
    private java.util.ArrayList  descList;
    private int count = 0;
    public int getCount () { return count; }
    private int currentIndex = -1;
    public void begin() { currentIndex = -1; }
    /**
     * Checks for the existence of the next file in the list.
     * @return true - another file exists, false - no more files.
     */
    public boolean next()
    {
      return ++currentIndex < count;
    }
    public String getTopicName()
    {
      return (hasMore() ? (String) topicNameList.get(currentIndex) : null);
    }
    public String getTopicDescription()
    {
      return (hasMore() ? (String)descList.get(currentIndex) : null);
    }
    public String getTopicID()
    {
      return (hasMore() ? (String) topicIDList.get(currentIndex) : null);
    }
    public String getTotalEntry()
    {
      return (hasMore() ? (String) totalEntryList.get(currentIndex) : null);
    }
    public boolean hasMore()
    {
      return (count > 0 && currentIndex < count);
    }
    public TopicList()
    {
      topicNameList      = new ArrayList();
      topicIDList        = new ArrayList();
      totalEntryList     = new ArrayList();
      descList     = new ArrayList();
    }
    public void addTopic (String topicName, String topicID, String total)
    {
      topicNameList.add(topicName);
      topicIDList.add(topicID);
      totalEntryList.add(total);
      count++;
    }
    public void addTopic (String topicID, String topicName, String topicDescription, boolean flag)
    {
      topicNameList.add(topicName);
      topicIDList.add(topicID);
      descList.add(topicDescription);
      count++;
    }
    public String getTopicName(String topicID)
    {
      int index=getIndex(topicID);
      String name=null;
      if(index!=-1)
      {
        name=(String)topicNameList.get(index);
      }
      return name;
    }
    public String getTotalEntry(String topicID)
    {
      int index=getIndex(topicID);
      String total=null;
      if(index!=-1)
      {
        total=(String)totalEntryList.get(index);
      }
      return total;
    }
    private int getIndex(String topicID)
    {
      int index=-1;
      for (int i=0; i<topicIDList.size(); i++)
      {
        if (topicIDList.get(i).equals(topicID))
          index=i;
      }
      return index;
    }
}
