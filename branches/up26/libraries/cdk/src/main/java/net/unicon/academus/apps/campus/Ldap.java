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

import java.util.Hashtable;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.directory.SearchControls;
import javax.naming.directory.DirContext;

public class Ldap {
  private static String esc(String s) {
    if(s!=null&&s.length()>0) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0 ; i < s.length() ; i++) {
        char ch = s.charAt (i);
        switch (ch) {
        case '*':
          sb.append ("\\2a");
          break;
        case '(':
          sb.append ("\\28");
          break;
        case ')':
          sb.append ("\\29");
          break;
        case '\\':
          sb.append ("\\5c");
          break;
        default:
          sb.append (ch);
        }
      }
      return sb.toString();
    } else
      return null;
  }

  private static String wildCard(String s) {
    String ret = esc(s);
    if(ret==null)
      return null;
    else
      return "*" + ret + "*";
  }

  private static CampusData getResult(NamingEnumeration userlist,String[] m_outputAttrs) throws Exception {
    CampusData con = new CampusData();

    Hashtable params = new Hashtable();
    SearchResult result = (SearchResult) userlist.nextElement();
    Attributes attrs = result.getAttributes();

    for (int i = 0; i < m_outputAttrs.length; i ++) {
      String attValue = getAttributeValue(attrs,i,m_outputAttrs);
      switch (i) {
      case 0 :
        con.putOID(attValue);
        int k = 0;
        if ((k = con.getID().indexOf("@")) > 0)
          con.putOID(con.getID().substring(0,k));
        break;
      case 1 :
        con.putName(attValue);
        break;
      case 2 :
        con.putEmail(attValue);
        break;
      case 3 :
        con.putTelephoneNumber(attValue);
        break;
      case 4 :
        con.putDepartment(attValue);
        break;
      case 5 :
        con.putTitle(attValue);
        break;
      case 6 :
        con.putRefID("portal", attValue);
        break;
      }
    }
    return con;
  }

  public static CampusData getCampusContact(String uid) throws Exception {
    CampusData con = null;
    String[] m_outputAttrs = LdapServices.getOutputAttributes();
    DirContext conn = null;
    NamingEnumeration userlist = null;
    conn = LdapServices.getConnection();
    if( conn == null)
      return null;
    SearchControls sCtrls = new SearchControls();
    try {
      sCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      sCtrls.setReturningAttributes(m_outputAttrs);
      String filter = ("("+LdapServices.getUidAttribute()+"="+ esc(uid) +")");
      userlist = conn.search(LdapServices.getBaseDN(),filter,sCtrls);
      if (userlist != null && userlist.hasMoreElements())
        con = getResult(userlist,m_outputAttrs);
    } catch(NamingException e) {
      throw e;
    } finally {
      LdapServices.releaseConnection(conn);
    }
    return con;
  }

  public static CampusData getCampusFromPortal(String logon) throws Exception {
    CampusData con = null;
    String[] m_outputAttrs = LdapServices.getOutputAttributes();
    DirContext conn = null;
    NamingEnumeration userlist = null;
    conn = LdapServices.getConnection();
    if( conn == null)
      return null;
    SearchControls sCtrls = new SearchControls();
    try {
      sCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      sCtrls.setReturningAttributes(m_outputAttrs);
      String filter = ("("+LdapServices.getPortalAttribute()+"="+ esc(logon) +")");
      userlist = conn.search(LdapServices.getBaseDN(),filter,sCtrls);
      if (userlist != null && userlist.hasMoreElements())
        con = getResult(userlist,m_outputAttrs);
    } catch(NamingException e) {
      throw e;
    } finally {
      LdapServices.releaseConnection(conn);
    }
    return con;
  }

  public static Vector searchCampusContacts(String name, String email, String title, String dep, long countLimit) throws Exception {
    Vector campusContacts = new Vector();
    String[] m_outputAttrs = LdapServices.getOutputAttributes();

    DirContext conn = null;
    NamingEnumeration userlist = null;
    conn = LdapServices.getConnection();
    if( conn == null)
      return new Vector();
    SearchControls sCtrls = new SearchControls();
    sCtrls.setCountLimit(countLimit);
    try {
      sCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      sCtrls.setReturningAttributes(m_outputAttrs);
      String filter = "";
      if(wildCard(name)!=null||wildCard(email)!=null||wildCard(title)!=null||wildCard(dep)!=null)
        filter = "(&";
      else
        filter = ("("+LdapServices.getUidAttribute()+"=*)");
      String filtertmp = "";
      if(wildCard(name)!=null)
        filter = filter + "("+LdapServices.sName+"=" + wildCard(name) + ")";
      if(wildCard(email)!=null)
        filter = filter + "("+LdapServices.sEmail+"=" + wildCard(email) + ")";
      if(wildCard(title)!=null)
        filter = filter + "("+LdapServices.sTitle+"=" + wildCard(title) + ")";
      if(wildCard(dep)!=null)
        filter = filter + "("+LdapServices.sDepartment+"=" + wildCard(dep) + ")";
      if(wildCard(name)!=null||wildCard(email)!=null||wildCard(title)!=null||wildCard(dep)!=null)
        filter = filter + "("+LdapServices.getUidAttribute()+"=*))";

      userlist = conn.search(LdapServices.getBaseDN(),filter,sCtrls);

      while (userlist != null && userlist.hasMoreElements()) {
        CampusData campus = getResult(userlist,m_outputAttrs);
        campusContacts.addElement(campus);
      }
    } catch(NamingException e) {
      //Error at filter string when it find through ldap.
      throw e;
    } finally {
      LdapServices.releaseConnection(conn);
    }
    sort(campusContacts);
    return campusContacts;
  }

  private static void sort(Vector vec) {
    for (int i = 0; i < vec.size()-1; i++) {
      CampusData oi = (CampusData)vec.elementAt(i);
      String ni = oi.getName();
      if (ni == null)
        return;
      CampusData ov = oi;
      int v = i;
      for (int j = i+1; j < vec.size(); j++) {
        CampusData oj = (CampusData)vec.elementAt(j);
        String nj = oj.getName();
        if (nj.compareTo(ov.getName()) < 0) {
          ov = oj;
          v = j;
        }
      }
      if (v != i) {
        vec.setElementAt(ov,i);
        vec.setElementAt(oi,v);
      }
    }
  }

  /////////////////
  /**
   * <p>Return a single value of an attribute from possibly multiple values,
   * grossly ignoring anything else.  If there are no values, then
   * return an empty string.</p>
   *
   * @param results LDAP query results
   * @param attribute LDAP attribute we are interested in
   * @return a single value of the attribute
   */
  static private String getAttributeValue (Attributes attrs, int attribute,String[] m_outputAttrs) throws NamingException {
    NamingEnumeration values = null;
    String aValue = "";
    if (!isAttribute(attribute,m_outputAttrs))
      return  aValue;
    Attribute attrib = attrs.get(m_outputAttrs[attribute]);
    if (attrib != null) {
      for (values = attrib.getAll(); values.hasMoreElements();) {
        aValue = (String)values.nextElement();
        break;// take only the first attribute value
      }
    }
    return  aValue;
  }

  /**
   * Is this a value attribute that's been requested?
   *
   * @param attribute in question
   */
  static private boolean isAttribute (int attribute,String[] m_outputAttrs) {
    if (attribute < 0 || attribute > m_outputAttrs.length + 1) {
      return  false;
    }
    return  true;
  }
}
