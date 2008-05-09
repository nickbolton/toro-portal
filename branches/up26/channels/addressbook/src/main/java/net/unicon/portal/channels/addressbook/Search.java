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

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.addressbook.FolderData;
import net.unicon.academus.apps.campus.CampusData;
import net.unicon.academus.apps.campus.Ldap;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.XML;
import net.unicon.academus.domain.lms.MemberSearchCriteria;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserComparator;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Finder;
import net.unicon.portal.channels.rad.IdentityComparator;
import net.unicon.portal.channels.rad.IdentityDataSearcher;


import net.unicon.sdk.catalog.collection.IColEntryConvertor;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.portal.domain.FColUserEntryDataSource;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.collection.FColSortMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.FSimplePageMode;


public class Search extends AddressBookScreen {

  private static boolean useIdentityDataSearcher = true;

  static {
    useIdentityDataSearcher = "true".equalsIgnoreCase(Channel.getRADProperty("addressbook.useIdentityDataSearcher"));
  }

  static public final String SID = "Search";
  //Other
  int m_nPages = 0;
  int m_curPage = -1;
  int m_start = 0;
  int m_end = 0;
  int m_sum = 0;
  int m_rpp = CAddressBook.ROW_PER_PAGE;// rows per page
  //Info for Search
  String m_name = "";
  String m_title = "";
  String m_department = "";
  String m_email = "";
  //Content of checkbox
  boolean m_isPortal = true;
  boolean m_isPersonal = true;
  boolean m_isCampus = true;
  
  //No longer checkboxes and was hardcoded to always search LDAP. If no ldap host is set or is null in rad.properties, do not search
  final String sLdapHost = Channel.getRADProperty("campus.ldap.host");
  boolean ldapConfigured = (sLdapHost==null || sLdapHost.equals("")) ? false : true;

  Vector m_vcontact = null;
  Vector m_vcontactGroup = null;
  Vector m_vnonPortal = null;
  Vector m_vPortal = null;//including group

  String m_result = "";
  Sorted m_vresult = null;

  private int m_countLimit = 50;
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String sid() {
    return SID;
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void init(Hashtable params) throws Exception {
    m_vcontact = new Vector();
    m_vcontactGroup = new Vector();
    m_vnonPortal = new Vector();
    m_vPortal = new Vector();
    String from = (String)params.get("from");
    if(from!=null&&from.equals(Peephole.SID))
      return;
    String where = (String)params.get("where");
    if(where!=null) {
      if(where.equals(Portal.SID)||where.equals(Personal.SID)) {
        m_isCampus = false;
        m_isPersonal = true;
        m_isPortal = false;
      }
    } else
      search();
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void reinit(Hashtable params) throws Exception {
    String where = (String)params.get("where");
    if(where==null) {
      init(params);
    } else if(where.equals(Portal.SID)||where.equals(Personal.SID)) {
      m_vcontact = new Vector();
      m_vcontactGroup = new Vector();
      m_vnonPortal = new Vector();
      m_vPortal = new Vector();
      m_isCampus = false;
      m_isPersonal = true;
      m_isPortal = false;
      m_result = "";
      m_nPages = 0;
      m_curPage = -1;
      m_start = 0;
      m_end = 0;
      m_sum = 0;
      m_name = "";
      m_title = "";
      m_department = "";
      m_email = "";
    }
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("nPages", new Integer(m_nPages));
    params.put("curPage", new Integer(m_curPage));
    params.put("maxResult",new Integer(m_countLimit));
    params.put("start",new Integer(m_start));
    params.put("end",new Integer(m_end));
    params.put("total",new Integer(m_sum));
    params.put("name",m_name);
    params.put("title",m_title);
    params.put("department",m_department);
    params.put("email",m_email);
    if(m_isCampus)
      params.put("is-campus-check","true");
    if(m_isPortal)
      params.put("is-portal-check","true");
    if(m_isPersonal)
      params.put("is-personal-check","true");
    return params;
  }
  /**
   *  Override from <code>AddressBookScreen</code>.
   *  @see net.unicon.portal.channels.addressbook.AddressBookScreen.
   */
  public void printXML(StringBuffer xml) throws Exception {
    xml.append(m_result);
  }
  /**
   * Search contacts.
   * @param params Noop at here. It is used by framework.
   */
  public Screen search(Hashtable params) throws Exception {
    //recieve data from form
    getForm(params);
    search();
    return this;
  }
  /**
   * Copy contact from a non portal contact data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen copyContact(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));
    CampusData campus = (CampusData)m_vresult.get(ord);
    ContactData ct = null;
    boolean flag = false;
    String ref = "p:" + campus.getID();
    ct = getBo().getContactByRef(ref,getUserLogon());
    if(ct==null) {
      Finder.findRefferences(m_channel.logonUser(),campus);
      ref = "u:" + campus.getRefID("portal");
      ct = getBo().getContactByRef(ref,getUserLogon());
    }

    Contact screen = (Contact)m_channel.makeScreen("Contact");
    screen.init(params);
    if(ct == null) {
      ct = new ContactData();
      ct.putName(campus.getName());
      ct.putTitle(campus.getTitle());
      ct.putDepartment(campus.getDepartment());
      ct.putBusinessPhone(campus.getTelephoneNumber());
      ct.putEmail(campus.getEmail());
      ct.putRef("campus", campus);
      ct.putRefID("campus", campus.getID());
      flag = true;
    } else {
      screen.getFolderOfContact(ct.getID());
    }
    screen.m_ct = ct;
    screen.m_back = sid();
    screen.m_methodName = null;
    screen.m_isNew = flag;

    return screen;
  }
  /**
   * Copy contact from a portal contact data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen copyContactPortal(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));
    IdentityData data = (IdentityData)m_vresult.get(ord);

    ContactData ct = null;
    boolean flag = false;
    String ref = "u:" + data.getAlias();
    ct = getBo().getContactByRef(ref,getUserLogon());
    if(ct==null) {
      Finder.findRefferences(m_channel.logonUser(),data);
      ref = "p:" + data.getRefID("campus");
      ct = getBo().getContactByRef(ref,getUserLogon());
    }

    Contact screen = (Contact)m_channel.makeScreen("Contact");
    screen.init(params);
    if(ct==null) {
      ct = new ContactData();
      ct.putName(data.getName());
      ct.putEmail(data.getEmail());
      ct.putBusinessPhone((String)data.getA("telephoneNumber"));
      ct.putRef("portal", data);
      ct.putRefID("portal", data.getAlias());
      flag = true;
    } else {
      screen.getFolderOfContact(ct.getID());
    }
    screen.m_ct = ct;
    screen.m_back = sid();
    screen.m_isNew = flag;
    screen.m_methodName = null;

    return screen;
  }

  /**
   * Edit Contact.
   * @param params Noop at here. It is used by framework.
   */
  public Screen editContact(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));
    ContactData ct = (ContactData)m_vresult.get(ord);
    if(ct!=null) {
      Contact screen = (Contact)m_channel.makeScreen("Contact");
      screen.m_isNew = false;
      screen.m_back = sid();
      screen.m_ct = ct;
      screen.init(params);
      screen.m_methodName = "updateScreen";
      screen.getFolderOfContact(ct.getID());

      return screen;
    } else
      return info(getLocalText(CAddressBook.INFO_CONTACT_NOT_EXISTED,CAddressBook.class), true);
  }
  /**
   * Comfirm before deleting a contact data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen confirmDelContact(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));
    String contactid = ((IdentityData)m_vresult.get(ord)).getID();
    String[] mfps = new String[1];
    mfps[0] = ((IdentityData)m_vresult.get(ord)).getName();
    params.put("methodName","deleteContact");
    params.put("contactid",contactid);

    // VU - begin (5/15/2003)
    params.put("methodSID", sid());
    params.put("back", sid());

    DeleteConfirm confirm = (DeleteConfirm) super.makeScreen("DeleteConfirm");
    confirm.init(params);
    return confirm;

//    return confirm(CAddressBook.CONFIRM_DELETE_CONTACT,mfps,params,sid());
  }
  /**
   * Delete a contact data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen deleteContact(Hashtable params) throws Exception {
    boolean ret = getBo().deleteContact(null,(String)params.get("contactid"));

    //search();
    ContactData ct = new ContactData((String)params.get("contactid"),null);
    m_vresult.remove(ct);
    updateCursor();
    m_result = setCurPage(m_curPage);

    return this;
  }
  /**
   * Comfirm before deleting a folder data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen confirmDelFolder(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));
    String folderid = ((IdentityData)m_vresult.get(ord)).getID();
    String[] mfps = new String[1];
    mfps[0] = ((IdentityData)m_vresult.get(ord)).getName();
    params.put("methodName","deleteFolder");
    params.put("folderid",folderid);

     // VU - begin (5/15/2003)
    params.put("methodSID", sid());
    params.put("back", sid());

    DeleteConfirm confirm = (DeleteConfirm) super.makeScreen("DeleteConfirm");
    confirm.init(params);
    return confirm;

//    return confirm(CAddressBook.CONFIRM_DELETE_FOLDER,mfps,params,sid());
  }
  /**
   * Delete a folder data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen deleteFolder(Hashtable params) throws Exception {
    boolean ret = getBo().deleteFolder((String)params.get("folderid"));
    search();
    updateCursor();
    return this;
  }
  /**
   * Edit a folder data.
   * @param params Noop at here. It is used by framework.
   */
  public Screen editFolder(Hashtable params) throws Exception {
    int ord = Integer.parseInt((String)params.remove("ord-id"));

    Folder screen = (Folder)m_channel.makeScreen("Folder");
    screen.m_isNew = false;
    screen.m_back = sid();
    screen.m_folder = (FolderData)m_vresult.get(ord);
    screen.m_methodName = "updateScreen";
    return screen;
  }
  /**
   * Go to Next Page.
   * @param params Noop at here. It is used by framework.
   */
  public Screen next(Hashtable params) throws Exception {
    m_result = setCurPage( m_curPage + 1);
    return this;
  }
  /**
   * Go to previous page.
   * @param params Noop at here. It is used by framework.
   */
  public Screen prev(Hashtable params) throws Exception {
    m_result = setCurPage( m_curPage - 1);
    return this;
  }
  /**
   * Update content of contact data and folder data on screen.
   * @param params Noop at here. It is used by framework.
   */
  public Screen updateScreen(Hashtable params) throws Exception {
    int i;
    Object data = params.get("data");
    if(data!=null) {
      if(data instanceof ContactData) {
        i = Contact.find(m_vresult,((ContactData)data).getID());
        m_vresult.remove(i);
        m_vresult.add(data);
      } else if(data instanceof FolderData) {
        i = Contact.find(m_vresult,((FolderData)data).getID());
        m_vresult.remove(i);
        m_vresult.add(data);
      }
      m_result = setCurPage(m_curPage);
      params.remove("data");
    }
    return this;
  }
  /**
   * Get contact data from user.
   * @param params Noop at here. It is used by framework.
   */
  private void getForm(Hashtable params) {
    if(params.get("max-results")!=null)
      m_countLimit = Integer.parseInt((String)params.get("max-results"));
    if(params.get("name")!=null)
      m_name = ((String)params.get("name")).trim();
    if(params.get("title")!=null)
      m_title = ((String)params.get("title")).trim();
    if(params.get("department")!=null)
      m_department = ((String)params.get("department")).trim();
    if(params.get("email")!=null)
      m_email = ((String)params.get("email")).trim();
    m_isPortal = (params.get("Portal")==null)?false:true;
    m_isCampus = (params.get("Campus")==null)?false:true;
    m_isPersonal = (params.get("Personal")==null)?false:true;
  }
  /**
   * Cal and get contact datas from the specified page.
   * @param curPage The specified page.
   */
  private String setCurPage( int curPage) throws Exception {
    StringBuffer xml = new StringBuffer();
    m_curPage = curPage;
    if( curPage >= 0 && curPage < m_nPages) {
      int start = m_curPage * m_rpp;
      int end = start + m_rpp;
      if( end > m_vresult.size())
        end = m_vresult.size();
      m_start = start;
      m_end = end;
      for(int i = start ; i < end; i++) {
        Object idDataObj = m_vresult.get(i);
        if(idDataObj instanceof FolderData) {
          ((FolderData)idDataObj).printXML(xml);
        } else if(idDataObj instanceof CampusData) {
          ((CampusData)idDataObj).printXML(xml);
        } else if(idDataObj instanceof ContactData) {
          ((ContactData)idDataObj).printXML(xml);
        } else if(idDataObj instanceof IdentityData) {
          xml.append(printPortalXML((IdentityData)idDataObj));
        }
      }
    }
    return xml.toString();
  }
  /**
   * Convert IdentityData data to XML data.
   * @param data data is needed to convert.
   * @return Xml data.
   */
  public static String printPortalXML(IdentityData data) {
    StringBuffer xml = new StringBuffer();
    if(data.getType().equals(IdentityData.GROUP))
      xml.append("<group iid='" + data.getID() + "' iname='" + XML.esc(data.getName()) + "'/>");
    else if (data.getAlias() != null)
      xml.append("<entity iid='"+ data.getID() +"' iname='" + XML.esc(data.getName()) +"' email='" + XML.esc(data.getEmail()) + "' />");
    return xml.toString();
  }
  /**
   * Search contact data in database.
   * Init list of contacts find out.
   */
  private void search() throws Exception {
    m_vcontact.clear();
    m_vcontactGroup.clear();
    m_vnonPortal.clear();
    m_vPortal.clear();

    //search
    if(m_isPersonal) {
      m_vcontact = getBo().searchContacts(getUserLogon(),m_name,m_email,m_title,m_department,m_countLimit);
      if(!((m_email!=null&&m_email.length()>0)||(m_title!=null&&m_title.length()>0)||(m_department!=null&&m_department.length()>0)))
        m_vcontactGroup = getBo().searchFolders(getUserLogon(),m_name,m_countLimit);
    }
    if(m_isCampus && ldapConfigured)
      m_vnonPortal = Ldap.searchCampusContacts(m_name,m_email,m_title,m_department,m_countLimit);
    if(m_isPortal) {
      if (useIdentityDataSearcher) {
        m_vPortal = IdentityDataSearcher.portalSearch(m_name, m_title, m_department, m_email, m_countLimit);
      } else {
        m_vPortal = GroupData.search(GroupData.S_USER,new SearchFilter(m_name,m_title,m_department,m_email),GroupData.FILTER_ALL,new IdentityComparator(),m_countLimit, true);
      }
    }

    doFilter(m_vcontact,m_vnonPortal,m_vPortal);

    m_vresult = new Sorted(new IdentityComparator());

//    m_vresult.addAll(m_vcontact);
//    m_vresult.addAll(m_vcontactGroup);
//    m_vresult.addAll(m_vnonPortal);
//    m_vresult.addAll(m_vPortal);

    IdentityData person = null;
    Iterator it = null;
    HashMap contacts = new HashMap();

    // Filter campus
    it = m_vcontact.iterator();
    while (it.hasNext()) {
      person = (IdentityData)it.next();
      String id = person.getID();
      if (!contacts.containsKey(id)) {
//System.out.println("ContactId [ " + id + " ]");
        contacts.put(id, person);
      }
    }

    // Filter campus-group
    it = m_vcontactGroup.iterator();
    while (it.hasNext()) {
      person = (IdentityData)it.next();
      String id = person.getID();
      if (!contacts.containsKey(id)) {
// System.out.println("ContactId [ " + id + " ]");
        contacts.put(id, person);
      }
    }

    // Filter non-portal
    it = m_vnonPortal.iterator();
    while (it.hasNext()) {
      person = (IdentityData)it.next();
      String id = person.getID();
      if (!contacts.containsKey(id)) {
//  System.out.println("ContactId [ " + id + " ]");
        contacts.put(id, person);
      }
    }

    // Filter portal
    it = m_vPortal.iterator();
    while (it.hasNext()) {
      person = (IdentityData)it.next();
      String id = person.getID();
      if (!contacts.containsKey(id)) {
//   System.out.println("ContactId [ " + id + " ]");
        contacts.put(id, person);
      }
    }

    // Adding results
    m_vresult.addAll(contacts.values());

    //update page
    if(m_vresult.size()>m_countLimit) {
      m_vresult.setSize(m_countLimit);
      m_sum = m_countLimit;
    } else
      m_sum = m_vresult.size();

    m_nPages = 0;
    if( m_sum >0 ) {
      m_nPages = m_sum / m_rpp;
      if( (m_sum % m_rpp) != 0)
        m_nPages++;
    }
    m_result = setCurPage((m_nPages > 0)?0:-1);
  }
  /**
   * Update currently page.
   */
  private void updateCursor() {
    m_sum = m_vresult.size();
    if( m_sum > 0 ) {
      m_nPages = m_sum / m_rpp;
      if( (m_sum % m_rpp) != 0)
        m_nPages++;
    }
    if(m_curPage > m_nPages)
      m_curPage = m_nPages - 1;
  }
  /**
   * Filter contact data from 3 source ( contact , campus , portal ).
   * Following order such as Contact Campus Portal.
   */
  public static void doFilter(Vector vcontact, Vector vcampus, Vector vportal) {
    int i , j ;
    //Campus with portal
    for( i = 0 ; i < vcampus.size() ; i++) {
      CampusData campus = (CampusData)vcampus.elementAt(i);
      String tmp = campus.getRefID("portal");
      for(j = 0 ; j < vportal.size() ; ) {
        IdentityData portal = (IdentityData)vportal.elementAt(j);
        if(tmp.equals(portal.getAlias())) {
          campus.putRef("portal", portal);
          vportal.remove(j);
        } else
          j++;
      }
    }

    //Personal with Campus and LDap
    for(i = 0 ; i < vcontact.size() ; i++) {
      ContactData contact = (ContactData)vcontact.elementAt(i);
      String campusID = contact.getRefID("campus"); // uid of campus data
      String portalID = contact.getRefID("portal"); // uid of portal data

      //Campus Filter
      if( campusID != null) {
        for(j = 0 ; j < vcampus.size() ;) {
          CampusData campus = (CampusData)vcampus.elementAt(j);
          if(campusID.equals(campus.getID())) {
            contact.putRef("campus",campus);
            vcampus.remove(j);
          } else
            j++;
        }
      }

      if( portalID != null) {
        //Portal Filter
        for(j = 0 ; j < vportal.size();) {
          IdentityData portal = (IdentityData)vportal.elementAt(j);
          if(portalID.equals(portal.getAlias())) {
            contact.putRef("portal",portal);
            vportal.remove(j);
          } else
            j++;
        }
        //Campus Filter
        for(j = 0 ; j < vcampus.size() ; ) {
          CampusData campus = (CampusData)vcampus.elementAt(j);
          if(campus.getRef("portal")!=null) {
            if(portalID.equals(((IdentityData)campus.getRef("portal")).getAlias())) {
              contact.putRef("campus",campus);
              vcampus.remove(j);
            } else
              j++;
          } else
            j++;
        }
      }
    }
  }
}
