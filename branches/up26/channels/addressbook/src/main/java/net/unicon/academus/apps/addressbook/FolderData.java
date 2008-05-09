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
package net.unicon.academus.apps.addressbook;

import java.io.Serializable;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XML;

/**
 * A <code>ContactData</code> object contains data of contact.
 *  type : "G"
 *  entityType : "a"
 *  id : folderID
 *  name : fullname
 * @version
 * @author
 */
public class FolderData extends IdentityData implements Serializable {
  public static final String S_CONTACT = "a";

  public static final int MAX_NAME_LENGTH = 64;
  /**
   * Create a new instance of <code>FolderData</code> with empty data.
   */
  public FolderData() {
    putType(GROUP);
    putEntityType(S_CONTACT);
  }
  /**
   * Create a new instance of <code>FolderData</code> with <code>IdentityData</code> data.
   * @param folderId    Id of folder.
   * @param fullname    Name of folder.
   */
  public FolderData(String folderId,String fullname) {
    super(GROUP,S_CONTACT,folderId,fullname);
  }
  /**
   * Get Owner from folder.
   */
  public String getOwnerId() {
    return (String)getE("ownerid");
  }
  /**
   * Set Owner from folder.
   */
  public void putOwnerId(String ownerId) {
    putE("ownerid",ownerId);
  }
  /**
   * Convert data of folder to xml structure.
   * @param xml contains data of folder.
   */
  public void printXML(StringBuffer xml) {
    xml.append("<folder id='" + getID() + "'>");
    xml.append((String)getName()!= null? XML.esc((String)getName()): "");
    xml.append("</folder>");
  }
}
