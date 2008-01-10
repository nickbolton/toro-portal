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
package net.unicon.academus.apps.campus;

import java.io.Serializable;
import java.lang.Cloneable;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XML;

/*
  type : "E"
  entityType : "p"
  id : userid
  name : name
*/
public class CampusData extends IdentityData implements Serializable,Cloneable {
  public static final String S_CAMPUS = "p";
  public CampusData() {
    putType(ENTITY);
    putEntityType(S_CAMPUS);
  }

  public CampusData(String userid,String name) {
    super(ENTITY,S_CAMPUS,userid,name);
  }

  public String getAlias() {
    return getID();
  }
  public void putAlias(String alias) {
    putOID(alias);
  }

  public String getTelephoneNumber() {
    return (String)getE("telephonenumber");
  }
  public void putTelephoneNumber(String tel) {
    putE("telephonenumber",tel);
  }

  public String getDepartment() {
    return (String)getE("department");
  }
  public void putDepartment(String department) {
    putE("department",department);
  }

  public String getTitle() {
    return (String)getE("title");
  }
  public void putTitle(String title) {
    putE("title",title);
  }

  public void printXML(StringBuffer xml) throws Exception {
    xml.append("<campus id= '" + (this.getID()!=null? XML.esc((String)this.getID()):"") + "'>");
    xml.append("<name>" + (this.getName()!= null? XML.esc((String)this.getName()): "") + "</name>");
    xml.append("<email>" + (this.getEmail()!= null? XML.esc((String)this.getEmail()): "") + "</email>");
    xml.append("<business-phone>" + (this.getTelephoneNumber()!= null? XML.esc((String)this.getTelephoneNumber()): "") + "</business-phone>");
    xml.append("<title>" + (this.getTitle()!= null? XML.esc((String)this.getTitle()): "") + "</title>");
    xml.append("<department>" + (this.getDepartment()!= null? XML.esc((String)this.getDepartment()): "") + "</department>");
    xml.append("</campus>");
  }

  public Object clone() throws CloneNotSupportedException {
    CampusData ct = new CampusData();

    ct.putType(getType());
    ct.putTitle(getTitle());
    ct.putName(getName());
    ct.putOID(getID());
    ct.putTelephoneNumber(getTelephoneNumber());
    ct.putEntityType(getEntityType());
    ct.putEmail(getEmail());
    ct.putDepartment(getDepartment());

    return ct;
  }

  //---------------------------------------------------------------------//

  static final String CAMPUS_ID = ENTITY + SEPARATOR + S_CAMPUS;

  public static boolean isCampusUser( IdentityData id) {
    return (id.getIdentifier().startsWith(CAMPUS_ID) && id.getAlias() != null);
  }
}
