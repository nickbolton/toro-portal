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
import java.sql.Timestamp;
import java.util.ArrayList;
/** <p>a data object that holds information about a notepad note.</p>
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */
public class TopicInfo implements java.io.Serializable
{
  String topicID;
  public String getTopicID() { return topicID; }
  public void setTopicID(String topicid) { topicID = topicid;}
  String topicName;
  public String getName() { return topicName; }
  public void   setName(String topicname) { topicName = topicname; }
  String description;
  public String getDescription() { return description; }
  public void   setDescription(String desc) { description = desc; }
  java.sql.Timestamp createDate;
  public java.sql.Timestamp getCreateDate() { return createDate; }
  public void setCreateDate(java.sql.Timestamp date) {createDate=date; }
  String imageName;
  public String getImageName() { return imageName; }
  public void   setImageName(String image) { imageName = image; }
  String mimeType;
  public String getImageType() { return mimeType; }
  public void   setImageType(String type) { mimeType = type; }
  String total;
  public String getTotalEntry() { return total; }
  public void   setTotalEntry(String totalEntry) { total=totalEntry; }
  // freddy testing
  public String currentState;
  public String editTopicID;
  public ArrayList selectedGroupsList;
  public ArrayList getSelectedGroups () { return selectedGroupsList; }
  public void setSelectedGroups (ArrayList selection) { selectedGroupsList = selection;}
  public String SearchString;
  public String getSearchString () { return SearchString;}
  public void setSearchString (String search) { SearchString = search;}
  public TopicList SearchList;
  public TopicList getSearchList () { return SearchList;}
  public void setSearchList (TopicList listFound) { SearchList = listFound;}
  public ArrayList DeleteList;
  public ArrayList getDeleteList () { return DeleteList;}
  public void setDeleteList (ArrayList deletelist) { DeleteList = deletelist;}
}
