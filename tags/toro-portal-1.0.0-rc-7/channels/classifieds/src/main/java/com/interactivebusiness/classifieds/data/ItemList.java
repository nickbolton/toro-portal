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
import java.io.InputStream;
/** <p>a data object that holds informaiton about a list of notes.
 * The next() and the getxxx() methods return the topics as elements of
 * an ordered list.
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */
public class ItemList implements java.io.Serializable
{
    public java.util.ArrayList  itemIDList;
    public java.util.ArrayList  itemList;
    public java.util.ArrayList  expireDateList;
    public java.util.ArrayList  topicNameList;
    public java.util.ArrayList  topicIDList;
    public java.util.ArrayList  emailList;
    public java.util.ArrayList  phoneList;
    public java.util.ArrayList  costList;
    public java.util.ArrayList  msgToAuthList;
    public java.util.ArrayList  approveList;
    public java.util.ArrayList  contactList;
    public java.util.ArrayList  imageList;
    public java.util.ArrayList  imageContentTypeList;
    public java.util.ArrayList  authorList;
    private int count = 0;
    public int getCount () { return count; }
    private int currentIndex = -1;
    public void begin() { currentIndex = -1; }
    public int getCurrentIndex () {return currentIndex;}
    public void setCurrentIndex (String itemID)
    {
      currentIndex = getIndex(itemID);
    }
    /**
     * Checks for the existence of the next file in the list.
     * @return true - another file exists, false - no more files.
     */
    public boolean next()
    {
      return ++currentIndex < count;
    }
    public boolean previous()
    {
      return --currentIndex < count;
    }
    public String getItemID()
    {
      return (hasMore() ? (String) itemIDList.get(currentIndex) : null);
    }
    public String getContent()
    {
      return (hasMore() ? (String) itemList.get(currentIndex) : null);
    }
    public String getTopicName()
    {
      return (hasMore() ? (String) topicNameList.get(currentIndex) : null);
    }
    public String getTopicID()
    {
      return (hasMore() ? (String) topicIDList.get(currentIndex) : null);
    }
    public String getExpireDate()
    {
      if (hasMore())
      {
        Timestamp expireDate = (Timestamp)expireDateList.get(currentIndex);
        return expireDate.toString();
      }
      return "";
    }
    public String getEmail()
    {
      return (hasMore() ? (String) emailList.get(currentIndex) : null);
    }
    public String getPhone()
    {
      return (hasMore() ? (String) phoneList.get(currentIndex) : null);
    }
    public String getCost()
    {
      return (hasMore() ? (String) costList.get(currentIndex) : null);
    }
    public String getMsgToAuth()
    {
      return (hasMore() ? (String) msgToAuthList.get(currentIndex) : null);
    }
    public String getApprove()
    {
      return (hasMore() ? (String) approveList.get(currentIndex) : null);
    }
    public String getContact()
    {
      return (hasMore() ? (String) contactList.get(currentIndex) : null);
    }
    public String getAuthor()
    {
      return (hasMore() ? (String) authorList.get(currentIndex) : null);
    }
    public String getImage()
    {
      return (hasMore() ? (String) imageList.get(currentIndex) : null);
    }
    public String getContentType()
    {
      return (hasMore() ? (String) imageContentTypeList.get(currentIndex) : null);
    }
    public boolean hasMore()
    {
      return (count > 0 && currentIndex < count);
    }
    public ItemList()
    {
      itemIDList         = new ArrayList();
      itemList           = new ArrayList();
      expireDateList     = new ArrayList();
      topicNameList      = new ArrayList();
      topicIDList        = new ArrayList();
      emailList          = new ArrayList();
      phoneList          = new ArrayList();
      costList           = new ArrayList();
      msgToAuthList      = new ArrayList();
      approveList        = new ArrayList();
      contactList        = new ArrayList();
      imageList        = new ArrayList();
      authorList        = new ArrayList();
      imageContentTypeList        = new ArrayList();
    }
    public void addItem (String itemID, String item, Timestamp expireDate,String topicName, String topicID,
                         String email,String phone, String cost, String msgToAuth,String approve, String contact, String image, String contentType, String author)
    {
      itemIDList.add(itemID);
      itemList.add(item);
      expireDateList.add(expireDate);
      topicNameList.add(topicName);
      topicIDList.add(topicID);
      emailList.add(email);
      phoneList.add(phone);
      costList.add(cost);
      msgToAuthList.add(msgToAuth);
      approveList.add(approve);
      contactList.add (contact);
      imageList.add (image);
      imageContentTypeList.add (contentType);
      authorList.add (author);
      count++;
    }
    public String getContent(String itemID)
    {
      int index=getIndex(itemID);
      String item=null;
      if (index!=-1)
      {
        item=(String)itemList.get(index);
      }
      return item;
    }
    public String getTopicName(String itemID)
    {
      int index=getIndex(itemID);
      String name=null;
      if(index!=-1)
      {
        name=(String)topicNameList.get(index);
      }
      return name;
    }
    public String getTopicID(String itemID)
    {
      int index=getIndex(itemID);
      String id=null;
      if(index!=-1)
      {
        id=(String)topicIDList.get(index);
      }
      return id;
    }
    public String getExpireDate(String itemID)
    {
      int index=getIndex(itemID);
      String expireDate=null;
      if(index!=-1)
      {
        expireDate=((Timestamp)expireDateList.get(index)).toString();
      }
      return expireDate;
    }
    public String getEmail(String itemID)
    {
      int index=getIndex(itemID);
      String email=null;
      if(index!=-1)
      {
        email=(String)emailList.get(index);
      }
      return email;
    }
    public String getPhone(String itemID)
    {
      int index=getIndex(itemID);
      String phone=null;
      if(index!=-1)
      {
        phone=(String)phoneList.get(index);
      }
      return phone;
    }
    public String getCost(String itemID)
    {
      int index=getIndex(itemID);
      String cost=null;
      if(index!=-1)
      {
        cost=(String)costList.get(index);
      }
      return cost;
    }
    public String getMsgToAuth(String itemID)
    {
      int index=getIndex(itemID);
      String msg=null;
      if(index!=-1)
      {
        msg=(String)msgToAuthList.get(index);
      }
      return msg;
    }
    public String getApprove(String itemID)
    {
      int index=getIndex(itemID);
      String approve=null;
      if(index!=-1)
      {
        approve=(String)approveList.get(index);
      }
      return approve;
    }
    private int getIndex(String itemID)
    {
      int index=-1;
      for (int i=0; i<itemIDList.size(); i++)
      {
        if (itemIDList.get(i).equals(itemID))
          index=i;
      }
      return index;
    }
    //Freddy added
    public String getContact(String itemID)
    {
      int index=getIndex(itemID);
      String contact=null;
      if(index!=-1)
      {
        contact=(String)contactList.get(index);
      }
      return contact;
    }
    public String getAuthor(String itemID)
    {
      int index=getIndex(itemID);
      String author=null;
      if(index!=-1)
      {
        author=(String)authorList.get(index);
      }
      return author;
    }
    public String getImage(String itemID)
    {
      int index=getIndex(itemID);
      String image=null;
      if(index!=-1)
      {
        image=(String)imageList.get(index);
      }
      return image;
    }
    public String getContentType(String itemID)
    {
      int index=getIndex(itemID);
      String contentType=null;
      if(index!=-1)
      {
        contentType=(String)imageContentTypeList.get(index);
      }
      return contentType;
    }
}
