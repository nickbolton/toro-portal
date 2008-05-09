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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.academus.apps.campus.CampusData;
import net.unicon.academus.apps.campus.Ldap;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XML;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.IdentityDisplayNameComparator;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.channels.rad.Channel;
import net.unicon.portal.channels.rad.Finder;


import net.unicon.portal.groups.IGroupPermissionsManager;
import net.unicon.portal.groups.permissions.UniconGroupPermissionsManager;

import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogServiceFactory;
/*
  * href="{$goURL}=net.unicon.portal.channels.rad.Select&amp;opt=all&amp;methodSID=12223&amp;methodName=selectUsers"
  * @param params contains:
  *  "back"      <-- back screen
  *  "opt"    <-- all, group
  *  "methodSID", "methodName" <-- method to invoke if ok
  *  "selected"  <-- initial selected components
  *  others for ok params  <-- pass to above method if ok

 Notes: in your Channel.ssl  must add
 <?xml-stylesheet title="SelectGroups" href="/net/unicon/portal/channels/rad/groups.xsl" type="text/xsl" media="explorer"?>
 <?xml-stylesheet title="SelectAll" href="/net/unicon/portal/channels/rad/entities.xsl" type="text/xsl" media="explorer"?>

*/
public class Select extends AddressBookScreen {

  static public final String SID = "Select";
  static public final String ALL_FOLDER = "-1";
  public static final String RADIO_PERSONAL = "Personal";
  public static final String RADIO_PORTAL = "Portal";
  public static final String RADIO_SEARCH = "Search";

  private static ILogService logService = LogServiceFactory.instance();

  public static final int ROW_RADIO_PER_PAGE = 4;
  ///////////////////////////////////////////////////
  //For Common
  private String m_radio = null;
  ///////////////////////////////////////////////////
  // Sources to select from
  boolean m_portal = true;
  boolean m_campus = true;
  boolean m_contact = true;

  // Groups only source
  boolean m_singleSelection = false; // this implies groups only
  boolean m_groupsOnly = false;

  boolean m_isShowCheckbox = true;//for group entries
  ///////////////////////////////////////////////////
  //Start For Portal
  // group tree parameters
  // Action when user click OK
  String m_okSID = null;
  String m_okMethod = null;
  Hashtable m_okParams = null;
  // Action when user click Cancel
  String m_back = null;
  // Selected objects
  IdentityData[] m_selected = null;
  Hashtable m_selgroup = null;
  Hashtable m_selentity = null;
  // others
  String m_openGroup = null;//entities of openGroup are displayed
  int m_nPages = 0;
  int m_curPage = -1;
  int m_rpp = CAddressBook.ROW_PER_PAGE;// rows per page
  // Group tree
  XMLData m_xml = null;
  GroupData m_root = null;
  XMLData m_entities = new XMLData();
  //End For Portal
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  //Start For Search
  String m_name = "";
  String m_title = "";
  String m_department = "";
  String m_email = "";

  //End For Search
  ///////////////////////////////////////////////////
  SSearch m_search = null;
  ///////////////////////////////////////////////////
  //Start For Personal
  SPersonal m_personal = null;
  //End For Personal
  ///////////////////////////////////////////////////
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
  public boolean canRenderAsPeephole() // NLe 0504
  {
    return super.canRenderAsPeephole();
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public void init(Hashtable params) throws Exception {
    // Use when goURL from xsl
    m_back = (String)params.remove("back");
    m_okSID = (String)params.remove("methodSID");
    m_okMethod = (String)params.remove("methodName");
    if( m_okSID == null)
      m_okSID = m_back;

    String singleSelectionOpt = (String)params.remove("singleSelection");
    if ("true".equalsIgnoreCase(singleSelectionOpt)) {
      m_singleSelection = true;
    }

    //sources: "portal.group,campus,contact"
    String opt = (String)params.remove("sources");
    if(opt!=null) {
      m_portal = (opt.indexOf("portal") != -1);
      m_campus = (opt.indexOf("campus") != -1);
      m_contact = (opt.indexOf("contact") != -1);
      m_isShowCheckbox = ( opt.indexOf(".disablegroup") == -1);
      m_groupsOnly = (opt.indexOf("groupsOnly") != -1);
    }

    // single selection implies groups only mode
    if (m_singleSelection) {
      m_groupsOnly = true;
    }

    if (m_groupsOnly) {
      m_radio = RADIO_PORTAL;
      m_portal = true;
      m_campus = false;
      m_contact = false;
    }

    if(m_portal)
      m_radio = RADIO_PORTAL;
    else if(m_campus)
      m_radio = RADIO_SEARCH;
    else if(m_contact)
      m_radio = RADIO_PERSONAL;
    else if (!m_groupsOnly) {
      m_campus = true;
      m_radio = RADIO_SEARCH;
    }

    // Initial selected identities
    m_selected = (IdentityData[])params.get("selected");

    // Save okParams
    params.remove("sid");
    params.remove("go");
    params.remove("do");
    m_okParams = new Hashtable();
    Enumeration e = params.keys();
    while( e.hasMoreElements()) {
      String key = (String)e.nextElement();
      m_okParams.put(key,params.get(key));
    }
    // Init group tree
    m_root = new GroupData(GroupData.C_USER, false);//true Tien1016;
    m_xml = m_root.xml();
    // selected, expanded
    m_selgroup = new Hashtable();
    m_selentity = new Hashtable();
    if(m_selected!=null&&m_selected.length>0)
      for(int i = 0; i < m_selected.length ; i++) {
        String type = m_selected[i].getType();
        if(type.equals(IdentityData.GROUP))
          m_selgroup.put(m_selected[i].getID(),m_selected[i]);
        else if(type.equals(IdentityData.ENTITY))
          m_selentity.put(m_selected[i].getID(),m_selected[i]);
      }
    m_personal = null;
    m_search = null;
    m_openGroup = m_root.getID();
    updateCheckXML(m_radio,null);
  }

  /**
   * Expand a node.
   */
  public Screen expand(Hashtable params) throws Exception {
    //<a href="{$doURL}=expand&amp;key={@iid}">
    //m_xml.putE("expanded", m_expanded);
    getSelected(params);

    String key = (String)params.get("key");
    if( key != null) {
      GroupData gr = m_root.getGroup( key);
      if( gr != null) {
        // expand group
        GroupData[] ch = gr.getChildGroups();
        if( ch != null)
          for( int i = 0; i < ch.length; i++)
            ch[i].initChildren();

        // save state
        String[] old = (String[])m_xml.getE("expanded");
        int len = (old==null)?0:old.length;
        String[] n = new String[len+1];
        if( old != null)
          for( int i = 0; i < old.length; i++)
            n[i] = old[i];
        n[len] = key;
        m_xml.putE("expanded", n);

        // Update page info
        updatePage();
      }
    }
    return this;
  }
  /**
   * Collapse a node on tree.
   */
  public Screen collapse(Hashtable params) throws Exception {
    getSelected(params);
    //<a href="{$doURL}=collapse&amp;key={@iid}">
    String key = (String)params.get("key");
    if( key != null) {
      String[] old = (String[])m_xml.getE("expanded");
      if( old != null && old.length > 0) {
        String[] n = new String[old.length-1];
        for( int i = 0, j = 0; i < old.length && j < old.length - 1; i++)
          if( !old[i].equals(key))
            n[j++] = old[i];
        m_xml.putE("expanded", n);
      }

      // Update page info
      updatePage();
    }
    return this;
  }
  /**
   *
   */
  public Screen open(Hashtable params) throws Exception {
    if(m_radio.equals(RADIO_PORTAL)) {
      getSelected(params);
      //<a class='uportal-navigation-channel' href="{$doURL}=open&amp;key={@iid}">
      String key = (String)params.get("key");
      if( key != null)
        open(key);
      return this;
    } else if(m_radio.equals(RADIO_PERSONAL)) {
      return m_personal.open(params);
    }
    return null;
  }
  /**
   * Go to previous page.
   */
  public Screen prev(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    if(m_radio.equals(RADIO_PORTAL)) {
      getSelected(params);
      GroupData gr = m_root.getGroup( m_openGroup);
      if( gr != null)
        setCurPage( gr, p - 1);
      return this;
    } else if(m_radio.equals(RADIO_PERSONAL)) {
      return m_personal.prev(params);
    } else if(m_radio.equals(RADIO_SEARCH)) {
      return m_search.prev(params);
    }
    return null;
  }
  /**
   * Go to next page.
   */
  public Screen next(Hashtable params) throws Exception {
    int p = Integer.parseInt((String)params.get("p"));
    if(m_radio.equals(RADIO_PORTAL)) {
      getSelected(params);
      GroupData gr = m_root.getGroup( m_openGroup);
      if( gr != null)
        setCurPage( gr, p + 1);
      return this;
    } else if(m_radio.equals(RADIO_PERSONAL)) {
      return m_personal.next(params);
    } else if(m_radio.equals(RADIO_SEARCH)) {
      return m_search.next(params);
    }
    return null;
  }
  /**
   * Go to search screen.
   */
  public Screen search(Hashtable params) throws Exception {
    return m_search.search(params);
  }

  /**
   *
   */
  public Screen selecting(Hashtable params) throws Exception {
    // Portal listing
    if( m_radio.equals(RADIO_PORTAL))
      getSelected(params);

    // Personal listing
    else if(m_radio.equals(RADIO_PERSONAL))
      m_personal.getSelected(params);

    // Search results
    else if(m_radio.equals(RADIO_SEARCH))
      m_search.getSelected(params);

    return this;
  }

  public Screen deselecting(Hashtable params) throws Exception {
    // Portal listing
    if( m_radio.equals(RADIO_PORTAL))
      deselect(params);

    // Personal listing
    else if(m_radio.equals(RADIO_PERSONAL))
      m_personal.deselect(params);

    // Search results
    else if(m_radio.equals(RADIO_SEARCH))
      m_search.deselect(params);

    return this;
  }

  public Screen ok(Hashtable params) throws Exception {
    // Get last selected items
    selecting(params);

    Vector vselected = new Vector();

    // Combine all...
    Hashtable hs = new Hashtable();

    //--- From Portal ---------------
    for(Enumeration enum1 = m_selgroup.keys();enum1.hasMoreElements();) {
      IdentityData sel = (IdentityData)m_selgroup.get(enum1.nextElement());
      hs.put(sel.getIdentifier(),sel);
    }

    for(Enumeration enum2 = m_selentity.keys();enum2.hasMoreElements();) {
      IdentityData sel = (IdentityData)m_selentity.get(enum2.nextElement());
      hs.put(sel.getIdentifier(),sel);
    }

    //--- Personal -------------
    if(m_personal!=null && m_personal.m_contactCheck.size() > 0) {
      for(Enumeration enum3 = m_personal.m_contactCheck.keys();enum3.hasMoreElements();) {
        // sel is ContactData
        IdentityData sel = (IdentityData)m_personal.m_contactCheck.get(enum3.nextElement());
        hs.put(sel.getIdentifier(),sel);
      }
    }

    //--- Search ---------------
    if(m_search!=null && m_search.m_contactCheck.size() > 0) {
      for(Enumeration enum4 = m_search.m_contactCheck.keys();enum4.hasMoreElements();) {
        IdentityData sel = (IdentityData)m_search.m_contactCheck.get(enum4.nextElement());
        hs.put(sel.getIdentifier(),sel);
      }
    }

    //--- Put final selected array ------------
    IdentityData selr[] = (IdentityData[])hs.values().toArray(new IdentityData[0]);
    Finder.findRefferences(m_channel.logonUser(), selr);
    m_okParams.put("selected" , selr);

    //-----------------------------
    //--- Log for testing only ----
    log("**** Log selected users ********");
    for( int i = 0; i < selr.length; i++) {
      IdentityData sel = selr[i];
      IdentityData portal = sel.getRef("portal");
      IdentityData campus = sel.getRef("campus");
      IdentityData contact = sel.getRef("contact");
      log("sel="+sel+
          (portal!=null?",portal="+portal:"")+
          (campus!=null?",campus="+campus:"")+
          (contact!=null?",contact="+contact:"")
         );
    }
    log("*******************************");
    //-----------------------------

    // NLe 0504
    if (m_channel instanceof Servant) {
      ((Servant)m_channel).finish((Object[])m_okParams.get("selected"));
      return null;
    }

    // return to method (m_okSID,m_okMethod)
    Screen scr = getScreen(m_okSID);
    if( scr != null)
      return scr.invoke(m_okMethod, m_okParams);
    else
      return this;
  }
  /**
   *
   */
  public Screen cancel(Hashtable params) throws Exception {
    // NLe 0504
    if (m_channel instanceof Servant) {
      ((Servant)m_channel).finish(null);
      return null;
    }

    return getScreen(m_back);
  }
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("isShowCheckbox", new Boolean(m_isShowCheckbox).toString());
    if(m_radio.equals(RADIO_PORTAL)) {
      params.put("openGroup", m_openGroup);
      params.put("nPages", new Integer(m_nPages));
      params.put("curPage", new Integer(m_curPage));
    } else if(m_radio.equals(RADIO_PERSONAL)) {
      params.put("openFolder",m_personal.m_openFolder);
      params.put("nPages", new Integer(m_personal.m_nPages));
      params.put("curPage", new Integer(m_personal.m_curPage));
    } else if(m_radio.equals(RADIO_SEARCH)) {
      params.put("nPages", new Integer(m_search.m_nPages));
      params.put("curPage", new Integer(m_search.m_curPage));
      params.put("maxResult",new Integer(m_search.m_countLimit));
      params.put("start",new Integer(m_search.m_start));
      params.put("end",new Integer(m_search.m_end));
      params.put("total",new Integer(m_search.m_sum));
      params.put("name",m_search.m_name);
      params.put("title",m_search.m_title);
      params.put("department",m_search.m_department);
      params.put("email",m_search.m_email);
    }
    params.put("choose-radio",m_radio);
    if(m_portal)
      params.put("portal","true");
    else
      params.put("portal","false");
    if(m_contact)
      params.put("contact","true");
    else
      params.put("contact","false");
    if(m_campus)
      params.put("search","true");
    else
      params.put("search","false");
    if (m_singleSelection) {
      params.put("singleSelection","true");
    } else {
      params.put("singleSelection","false");
    }
    if (m_groupsOnly) {
      params.put("groupsOnly","true");
    } else {
      params.put("groupsOnly","false");
    }
    return params;
  }
  /**
   *  Override from <code>AddressBookScreen</code>.
   *  @see net.unicon.portal.channels.addressbook.AddressBookScreen.
   */
  public void printXML(StringBuffer xml) throws Exception {}
  /**
   *  Override from <code>Screen</code> in rad.
   *  @see net.unicon.portal.channels.rad.Screen.
   */
  public String buildXML() throws Exception {
    if(m_radio.equals(RADIO_PORTAL)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      ps.println("<?xml version='1.0'?>");
      ps.println("<addressbook-system>");
      m_xml.print(ps, 1);
      this.printSelectedTag(ps);
      this.printPermitted(ps);
      ps.println("</addressbook-system>");

      return baos.toString();
    } else if (m_radio.equals(RADIO_PERSONAL)) {
      StringBuffer xml = new StringBuffer();
      xml.append("<?xml version='1.0'?>");
      xml.append("<addressbook-system>");
      m_personal.printXML(xml);
      xml.append("</addressbook-system>");
      return xml.toString();
    } else if (m_radio.equals(RADIO_SEARCH)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);

      StringBuffer xml = new StringBuffer();
      xml.append("<?xml version='1.0'?>");
      xml.append("<addressbook-system>");
      m_search.printXML(xml);
      this.printPermitted(xml);
      xml.append("</addressbook-system>");

      return xml.toString();
    }
    return null;
  }

  //----------------------------------------------------------------------//
  /**
   *
   */
  void updatePage() throws Exception {
    m_rpp = countExpanded( m_root);
    m_rpp = m_rpp + ROW_RADIO_PER_PAGE;
    if( m_openGroup != null)
      open( m_openGroup);
  }
  /**
   *
   */
  void open( String grID) throws Exception {
    // get group data associated with this id
    GroupData gr = m_root.getGroup( grID);
    m_openGroup = grID;
    m_entities.removeA("group");
    if( gr != null) {
      // Save open group id
      m_entities.putA("group", grID);

      // get all entities
      IdentityData[] children = gr.getChildEntities();

      // Count page
      m_nPages = 0;
      if( children != null) {
        m_nPages = children.length / m_rpp;
        if( (children.length % m_rpp) != 0)
          m_nPages++;
      }

      // current page
      setCurPage( gr, (m_nPages > 0)?0:-1);
    }
  }
  /**
   *
   */
  void setCurPage( GroupData gr, int curPage) throws Exception {
    m_curPage = curPage;
    IdentityData[] entities = null;

    // extract from all entities
    if( curPage >= 0 && curPage < m_nPages) {
      // start index
      IdentityData[] children = gr.getChildEntities();

      // Find details Tien 1011
      Finder.findDetails(children,null);
      Arrays.sort(children, new IdentityDisplayNameComparator());

      int start = m_curPage * m_rpp;
      int len = (start + m_rpp >= children.length)? children.length - start: m_rpp;
      if( len > 0) {
        entities = new IdentityData[len];
        for( int i = 0; i < len; i++)
          entities[i] = children[start+i];
      }
    }

    m_entities.putE("entity", entities);
  }
  /**
   *
   */
  int countExpanded( GroupData gr) throws Exception {
    // This
    int ret = 1;

    // check expanded
    if( isExpanded( gr.getID())) {
      GroupData[] ch = gr.getChildGroups();
      if( ch != null)
        for( int j = 0; j < ch.length; j++)
          ret += countExpanded( ch[j]);
    }

    return ret;
  }
  /**
   *
   */
  void clearSelectedGroup( GroupData gr) throws Exception {
    String key = gr.getID();
    m_selgroup.remove(key);

    // check expanded
    if( isExpanded( key)) {
      GroupData[] ch = gr.getChildGroups();
      if( ch != null)
        for( int j = 0; j < ch.length; j++)
          clearSelectedGroup( ch[j]);
    }
  }
  /**
   *
   */
  boolean isExpanded( String key) {
    String[] expanded = (String[])m_xml.getE("expanded");
    if( expanded == null)
      return false;
    for( int i = 0; i < expanded.length; i++)
      if( expanded[i].equals( key))
        return true;
    return false;
  }
  /**
   *
   */
  void getSelected(Hashtable params) throws Exception {
    // Clear selected groups belong to the submitted form
    //clearSelectedGroup( m_root);

    // Clear selected entities in the submitted form
    //IdentityData[] entities = (IdentityData[])m_entities.getE("entity");
    //if( entities != null)
    //  for( int i = 0; i < entities.length; i++)
    //    m_selentity.remove(entities[i].getID());

    // Single selection mode
    if (m_singleSelection) {
      m_selgroup.clear();
      String key = (String)params.get("selectedGroup");
      if (key != null) {
        GroupData gr = m_root.getGroup(key);
        if( gr != null) {
          m_selgroup.put(key, gr);
        }
      }
    }

    // Multiple mode
    else {
      // Set selected for ckecked items of submitted form
      for(Enumeration en = params.keys();en.hasMoreElements();) {
        String idStr = (String)en.nextElement();

        // Selected group
        if(idStr.startsWith("group")) {
          String key = idStr.substring(5);
          GroupData gr = m_root.getGroup( key);
          if( gr != null)
            m_selgroup.put(key, gr);
        }

        // entity
        else if(idStr.startsWith("entity")) {
          String key = idStr.substring(6);
          IdentityData data = new IdentityData(GroupData.ENTITY,
                                               m_root.getEntityType(),key, null);
          Finder.findDetail( data, null);
          m_selentity.put(key, data);
        }
      }
    }
  }

  void deselect(Hashtable params) throws Exception {
    for(Enumeration en = params.keys();en.hasMoreElements();) {
      String idStr = (String)en.nextElement();

      // Selected group
      if(idStr.startsWith("_group")) {
        String key = idStr.substring(6);
        m_selgroup.remove(key);
      }

      // entity
      else if(idStr.startsWith("_entity")) {
        String key = idStr.substring(7);
        m_selentity.remove(key);
      }
    }
  }

  /**
   *
   */
  String toXML(IdentityData id) {
    return "<selected iid='"+XML.esc(id.getID())+"' iname='"+XML.esc(id.getName())+"' itype='"+XML.esc(id.getType())+"' ientity='"+ XML.esc(id.getEntityType())+ "'/>";
  }


  /**
   *
   */

  String permittedXML(String id, String groupName) {

    String tempName = groupName.replaceAll("\'","&apos;");
    tempName = tempName.replaceAll("<","&lt;");
    tempName = tempName.replaceAll(">","&gt;");
    tempName = tempName.replaceAll("&","&lt;");
    tempName = tempName.replaceAll("\"","&lt;");

    return "<permitted iid='"+ id +"' iname='"+tempName+"'/>";

  }
  /**
   *
   */
  void printSelectedTag(PrintStream ps) {
    IdentityData idGroupEntity = null;
    for(Enumeration enum1 = m_selgroup.keys();enum1.hasMoreElements();) {
      Object key = enum1.nextElement();
      idGroupEntity = (IdentityData)m_selgroup.get(key);
      ps.println(toXML(idGroupEntity));
    }

    for(Enumeration enum2 = m_selentity.keys();enum2.hasMoreElements();) {
      Object key = enum2.nextElement();
      idGroupEntity = (IdentityData)m_selentity.get(key);
      ps.println(toXML(idGroupEntity));
    }
  }

  /**
   * This method prints out a set of XML to indicate to the XSLT that a group can
   * be selected by the end-useri, when using the "PORTAL" view.
   * @param ps An PrintStream for buffering XML
   */
  void printPermitted(PrintStream ps) {

    //Get a handle to the GroupPermissionsManager
    IGroupPermissionsManager gpm = UniconGroupPermissionsManager.getInstance();

    //I was here John Bodily
    Hashtable permitHash = new Hashtable();
    GroupData[] tempGroups = null;
    GroupData tempGroup = null;
    String userID = getUserLogon();
    boolean isPermitted = false;
    String[] groupIds = null;
    String rootID = m_root.getID();
    boolean isRootExpanded = false;

    //Make sure we selected the portal view (technically if you're in here you must have).
    if(m_radio.equals(RADIO_PORTAL)){

      //Get a list of groups that have been expanded on the screen
      groupIds = (String[])m_xml.getE("expanded");

      //Are there any expanded groups?
      if (groupIds != null && groupIds.length > 0){

        //Need to go through the expanded groups and their children to apply permissions to
        //the groups on screen.
        for(int i = 0; i < groupIds.length; i++){

           //check to see if the root has been expanded..it could be
           //that we have an expanded node that is covered up and not
           //visible because the root is not expanded. Thus we need
           //to make sure that we still check the root node for when
           //an expanded tag is being printed and the root is collapsed.
           if(!isRootExpanded && groupIds[i].equalsIgnoreCase(rootID)){
              isRootExpanded = true;
           }

           try{
             isPermitted = gpm.inheritsPermission(UniconGroupPermissionsManager.GROUPSMANAGERCHAN,userID,groupIds[i],"net.unicon.portal.groups.IMember","SELECT");
           }catch(Exception e){
           //An exception was thrown because no permissions(for anything) existed for that user.
           //Just consume it..as there is nothing more to do.
           }

           //get a handle to the GroupData for the expanded group...we need it to check the children.
           try{
             tempGroup = m_root.getGroup(groupIds[i]);

           }catch(Exception e){
              logService.log(ILogService.ERROR,e);
           }


             //this expanded group is selectable
           if(isPermitted){
             if(!permitHash.containsKey(tempGroup.getID())){
               permitHash.put(tempGroup.getID(),permittedXML(tempGroup.getID(),tempGroup.getName()));
               //ps.println(permittedXML(tempGroup.getID(), tempGroup.getName()));
             }
           }

           //now we need to print the children that are visible

           try{
             tempGroups = tempGroup.getChildGroups();
           }catch(Exception e){
              logService.log(ILogService.ERROR,e);
           }


           if(tempGroups != null){

             if(isPermitted){
                 //We know the root of this sub-tree is selectable so make the whole sub-tree selectable..this will give us some better performance
                 //for someone who has access to the whole group structure.
                 for(int l = 0; l < tempGroups.length; l++){
                   if(!permitHash.containsKey(tempGroups[l].getID())){
                     permitHash.put(tempGroups[l].getID(),permittedXML(tempGroups[l].getID(),tempGroups[l].getName()));
                     //ps.println(permittedXML(tempGroups[l].getID(),tempGroups[l].getName()));
                   }
                 }
             }else{
                 //The root of the subtree wasn't selectable..so we have to check permissions for the child nodes.
                 for(int j = 0; j < tempGroups.length; j++){


                   try{
                     isPermitted = gpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager",userID,tempGroups[j].getID(),"net.unicon.portal.groups.IMember","SELECT");
                   }catch(Exception e){
                   //An exception was thrown because no permissions(for anything) existed for that user.
                   //Just consume it..as there is nothing more to do.
                   }

                   if(isPermitted){
                     if(!permitHash.containsKey(tempGroups[j].getID())){
                       permitHash.put(tempGroups[j].getID(),permittedXML(tempGroups[j].getID(),tempGroups[j].getName()));

                         //ps.println(permittedXML(tempGroups[j].getID(),tempGroups[j].getName()));
                     }
                   }
                 }
               }
             }
         }
       }

       if(!isRootExpanded){

          try{
            isPermitted = gpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager",userID,m_root.getID(),"net.unicon.portal.groups.IMember","SELECT");
          }catch(Exception e){
           //An exception was thrown because no permissions(for anything) existed for that user.
           //Just consume it..as there is nothing more to do.

          }


          if(isPermitted){
            if(!permitHash.containsKey(m_root.getID())){
              permitHash.put(m_root.getID(),permittedXML(m_root.getID(),m_root.getName()));
              //ps.println(permittedXML(m_root.getID(),m_root.getName()));
            }
          }
        }

        //We've built up a hash of permitted groups...so let's print em out!
        if(permitHash.size() > 0){
          Enumeration en = permitHash.keys();

          while(en.hasMoreElements()){
            String groupKey = (String)en.nextElement();
            ps.println((String)permitHash.get(groupKey));
          }
        }

      }
  }

  /**
   * This method prints out a set of XML to indicate to the XSLT that a group can
   * be selected by the end-useri, when using the "SEARCH" view.
   * @param strBuff A StringBuffer for buffering XML
   */
  void printPermitted(StringBuffer strBuff){

    IGroupPermissionsManager gpm = UniconGroupPermissionsManager.getInstance();
    Pattern p = Pattern.compile("<group\\siid=\'([^\']+)\'\\siname=\'([^\']+)\'(\\s*)/>");
    Matcher m = p.matcher(strBuff.toString());
    String userID = getUserLogon();
    boolean found = false;
    boolean isPermitted = false;
    String gId = null;
    String groupName = null;

    if(m_radio.equals(RADIO_SEARCH)){

       found = m.find();


       while(found){

          gId = m.group(1);
          groupName = m.group(2);


          if(gId != null){

              try{
                isPermitted = gpm.inheritsPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager",userID,gId,"net.unicon.portal.groups.IMember","SELECT");
              }catch(Exception e){
           //An exception was thrown because no permissions(for anything) existed for that user.
           //Just consume it..as there is nothing more to do.

              }

              if(isPermitted){
                try{
                  strBuff.append(permittedXML(gId,groupName));

                }catch(Exception e){
                   logService.log(ILogService.ERROR,e);
                }
              }

            }

          found = m.find();

        }

      }
  }

  /**
   *
   */
  public Screen personalCheck(Hashtable params) throws Exception {
    updateCheckXML(this.RADIO_PERSONAL,params);
    return this;
  }
  /**
   *
   */
  public Screen portalCheck(Hashtable params) throws Exception {
    updateCheckXML(this.RADIO_PORTAL,params);
    return this;
  }
  /**
   *
   */
  public Screen searchCheck(Hashtable params) throws Exception {
    updateCheckXML(this.RADIO_SEARCH,params);
    return this;
  }
  /**
   *
   */
  private void updateCheckXML(String radioNew,Hashtable params) throws Exception {
    if( params != null) {
      if( m_radio.equals(RADIO_PORTAL))
        getSelected(params);
      else if(m_radio.equals(RADIO_PERSONAL)) {
        if(m_personal!=null)
          m_personal.getSelected(params);
      } else if(m_radio.equals(RADIO_SEARCH)) {
        if(m_search!=null)
          m_search.getSelected(params);
      }
    }
    m_radio = radioNew;

    // Initialize
    // Portal listing
    if( m_radio.equals(RADIO_PORTAL)) {
      // Open root
      m_xml.putE("entities", m_entities);
      open(m_openGroup);
    }

    // Personal contacts
    else if(m_radio.equals(RADIO_PERSONAL)) {
      if(m_personal==null) {
        m_personal = new SPersonal(this,null,getBo(),getUserLogon());
        m_personal.init(null);
      } else
        m_personal.open(m_personal.m_openFolder);
    }

    // Search
    else if(m_radio.equals(RADIO_SEARCH)) {
      if(m_search==null) {
        m_search = new SSearch(this,getBo(),getUserLogon());
        m_search.init(null);
//        m_search.m_campus = m_campus;
//        m_search.m_contact = m_contact;
//        m_search.m_portal = m_portal;
      }
    }
  }
}
