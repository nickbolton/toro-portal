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
import java.io.InputStream;
/** <p>a data object that holds information about a notepad note.</p>
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */
public class ItemInfo implements java.io.Serializable
{
  String itemID;
  public String getItemID() { return itemID; }
  public void setItemID(String itemid) { itemID = itemid;}
  String topicID;
  public String getTopicID() { return topicID; }
  public void setTopicID(String topicid) { topicID = topicid;}
  String authorID;
  public String getAuthorID() { return authorID; }
  public void   setAuthorID(String userid) { authorID = userid; }
  String content;
  public String getItemContent() { return content; }
  public void   setItemContent(String msg) { content = msg; }
  String email;
  public String getEmail() { return email; }
  public void   setEmail(String Email) { email=Email; }
  String phone;
  public String getPhone() { return phone; }
  public void   setPhone(String Phone) { phone=Phone; }
  String cost;
  public String getCost() { return cost; }
  public void   setCost(String price) { cost=price; }
  String message;
  public String getMessageToAuth() { return message; }
  public void   setMessageToAuth(String msg) { message=msg; }
  java.sql.Timestamp createDate;
  public java.sql.Timestamp getCreateDate() { return createDate; }
  public void setCreateDate(java.sql.Timestamp date) {createDate=date; }
  java.sql.Timestamp expireDate;
  public java.sql.Timestamp getExpireDate() { return expireDate; }
  public void setExpireDate(java.sql.Timestamp date) {expireDate=date; }
  String approved;
  public String getApproved() { return approved; }
  public void   setApproved(String app) { approved=app; }
  String approvedBy;
  public String getApprovedBy() { return approvedBy; }
  public void   setApprovedBy(String id) { approvedBy=id; }
  java.sql.Timestamp approvedDate;
  public java.sql.Timestamp getApprovedDate() { return approvedDate; }
  public void setApprovedDate(java.sql.Timestamp date) {approvedDate=date; }
  // Freddy added
  String ContactName;
  public String getContactName () { return ContactName; }
  public void   setContactName (String cn) { ContactName = cn; }
  String itemImage;
  public String getItemImage () { return itemImage; }
  public void   setItemImage (String image) { itemImage = image; }
  String imageMimeType;
  public String getImageMimeType () { return imageMimeType; }
  public void   setImageMimeType (String mimeType) { imageMimeType = mimeType; }
  InputStream stream;
  public InputStream getImageInputStream () { return stream; }
  public void   setImageInputStream (InputStream imageStream) { stream = imageStream; }
}
