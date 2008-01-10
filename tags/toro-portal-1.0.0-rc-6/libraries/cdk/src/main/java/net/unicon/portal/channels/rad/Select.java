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


package net.unicon.portal.channels.rad;

import java.util.*;
import java.text.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;

/**
 * It is a Screen used to select uPortal users, including the groups.
   * href="{$goURL}=net.unicon.portal.channels.rad.Select&amp;opt=all&amp;methodSID=12223&amp;methodName=selectUsers"
   * @param params contains:
   *  "back"      <-- back screen
   *  "opt"    <-- all, group
   *  "methodSID", "methodName" <-- method to invoke if ok
   *  "selected"  <-- initial selected components
   *  others for ok params  <-- pass to above method if ok
 */
public class Select extends Screen {

  static public final String SID = "Select";

  // portal group tree parameters
  boolean m_groupsOnly = true;

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
  int m_rpp = 5;                            // rows per page

  // Group tree
  XMLData m_xml = null;
  GroupData m_root = null;
  XMLData m_entities = new XMLData();

  /**
   * The Select screen can be "stateless", i.e, the sid can be a constant.
   * @return the id of Select screen.
   */
  public String sid() {
    return SID;
  }

  /**
   * The version of Select screen. It is the RAD version.
   * @return the RAD's version
   */
  public String getVersion() {
    return Channel.VERSION;
  }

  /**
   * Called when first entering to Select screen
   * @param params the parameters passed when enter to. They must at least contain the keys:
   *  methodSID - together with the methodName key specified what method to be called when finish Select screen.
   *  methodName - see above.
   *  back - The sid of back screen.
   *  sources - The user sources to select. Normally it is "campus,portal,contact".
   *  All other pairs key/values will pass to the method (methodSID, methodName) after user finishes with this screen.
   * @throws Exception if invalid input parameters.
   */
  public void init(Hashtable params) throws Exception {
    m_back = (String)params.remove("back");
    m_okSID = (String)params.remove("methodSID");
    m_okMethod = (String)params.remove("methodName");
    if( m_okSID == null)
      m_okSID = m_back;
    String opt = (String)params.remove("sources");
    m_groupsOnly = ( opt != null && opt.indexOf("group") != -1);
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
    m_root = new GroupData(GroupData.C_USER, false);//true Tien1011
    m_xml = m_root.xml();

    // selected, expanded
    m_selgroup = new Hashtable();
    m_selentity = new Hashtable();
    if(m_selected!=null&&m_selected.length>0)
      for(int i = 0; i < m_selected.length ; i++) {
        String type = m_selected[i].getType();
        if(type.equals("G"))
          m_selgroup.put(m_selected[i].getID(),m_selected[i]);
        else if(type.equals("E"))
          m_selentity.put(m_selected[i].getID(),m_selected[i]);
      }

    // open root group
    if( m_groupsOnly == false) {
      // Count row per page
      m_rpp = countExpanded( m_root);

      // Open root
      m_xml.putE("entities", m_entities);
      open(m_root.getID());
    }
  }

  /**
   * Called when user wants to expand a group.
   * @param params
   * @return this screen with the expanded given group.
   * @throws Exception
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
   * This screen is intended in the full screen mode.
   * @return false
   */
  public boolean canRenderAsPeephole() // NLe 0504
  {
    return false;
  }

  /**
   * Called when user collapses the group.
   * @param params
   * @return this screen.
   * @throws Exception
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
   * Called when user wants to see the content of a group.
   * @param params the parameters of current html form
   * @return this screen.
   * @throws Exception
   */
  public Screen open(Hashtable params) throws Exception {
    getSelected(params);
    //<a class='uportal-navigation-channel' href="{$doURL}=open&amp;key={@iid}">
    String key = (String)params.get("key");
    if( key != null)
      open(key);
    return this;
  }

  /**
   * Go to previous page
   * @param params
   * @return this
   * @throws Exception
   */
  public Screen prev(Hashtable params) throws Exception {
    getSelected(params);
    GroupData gr = m_root.getGroup( m_openGroup);
    if( gr != null)
      setCurPage( gr, m_curPage - 1);

    return this;
  }

  /**
   * Go to next page
   * @param params
   * @return this screen.
   * @throws Exception
   */
  public Screen next(Hashtable params) throws Exception {
    getSelected(params);

    GroupData gr = m_root.getGroup( m_openGroup);
    if( gr != null)
      setCurPage( gr, m_curPage + 1);

    return this;
  }

  /**
   * Finish with the selection of users.
   * @param params
   * @return the new screen depent on invoked method (specified on input).
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception {
    // put result ( selected users) to m_okParams
    getSelected(params);

    IdentityData[] selected = null;
    int i = 0;
    if(m_groupsOnly)
      selected = new IdentityData[m_selgroup.size()];
    else
      selected = new IdentityData[m_selgroup.size() + m_selentity.size()];

    for(Enumeration enum1 = m_selgroup.keys();enum1.hasMoreElements();) {
      Object key = enum1.nextElement();
      selected[i++] = (IdentityData)m_selgroup.get(key);
    }

    if(!m_groupsOnly)
      for(Enumeration enum2 = m_selentity.keys();enum2.hasMoreElements();) {
        Object key = enum2.nextElement();
        selected[i++] = (IdentityData)m_selentity.get(key);
      }

    // Finally
    Finder.findRefferences(m_channel.logonUser(), selected);
    m_okParams.put("selected" , selected);

    //-----------------------------
    //--- Log for testing only ----
    log("**** Log selected users ********");
    for( i = 0; i < selected.length; i++) {
      IdentityData sel = selected[i];
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
   * User canceled the Select screen, will go back
   * @param params
   * @return back screen.
   * @throws Exception
   */
  public Screen cancel(Hashtable params) throws Exception {
    // NLe 0504
    if (m_channel instanceof Servant) {
      ((Servant)m_channel).finish(null);
      return null;
    }

    return getScreen(m_back);
  }

  public Hashtable getXSLTParams() {
    Hashtable params = new Hashtable();
    params.put("groupsOnly", m_groupsOnly?"true":"false");
    if( m_groupsOnly == false) {
      params.put("openGroup", m_openGroup);
      params.put("nPages", Integer.toString(m_nPages));
      params.put("curPage", Integer.toString(m_curPage));
    }
    return params;
  }

  /**
   * Generate the xml data for Select screen.
   * @return xml string
   * @throws Exception
   */
  public String buildXML() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("<?xml version=\"1.0\"?>");
    ps.println("<groups-system>");
    m_xml.print(ps, 1);
    this.printSelectedTag(ps);
    ps.println("</groups-system>");
    return baos.toString();
  }

  //----------------------------------------------------------------------//

  void updatePage() throws Exception {
    m_rpp = countExpanded( m_root);
    if( m_openGroup != null)
      open( m_openGroup);
  }

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

  void setCurPage( GroupData gr, int curPage) throws Exception {
    m_curPage = curPage;
    IdentityData[] entities = null;

    // extract from all entities
    if( curPage >= 0 && curPage < m_nPages) {
      // start index
      IdentityData[] children = gr.getChildEntities();
      int start = m_curPage * m_rpp;
      int len = (start + m_rpp >= children.length)? children.length - start: m_rpp;
      if( len > 0) {
        entities = new IdentityData[len];
        for( int i = 0; i < len; i++)
          entities[i] = children[start+i];

        //Find details Tien 1011
        Finder.findDetails(entities,null);
      }
    }

    m_entities.putE("entity", entities);
  }

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

  boolean isExpanded( String key) {
    String[] expanded = (String[])m_xml.getE("expanded");
    if( expanded == null)
      return false;
    for( int i = 0; i < expanded.length; i++)
      if( expanded[i].equals( key))
        return true;
    return false;
  }

  void getSelected(Hashtable params) throws Exception {
    // Clear selected groups belong to the submitted form
    clearSelectedGroup( m_root);

    // Clear selected entities in the submitted form
    IdentityData[] entities = (IdentityData[])m_entities.getE("entity");
    if( entities != null)
      for( int i = 0; i < entities.length; i++)
        m_selentity.remove(entities[i].getID());

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
      else if(!m_groupsOnly && idStr.startsWith("entity")) {
        String key = idStr.substring(6);
        IdentityData id = new IdentityData(GroupData.ENTITY,m_root.getEntityType(),key, null);
        Finder.findDetail( id, null);
        m_selentity.put(key, id);
      }
    }
  }

  String toXML(IdentityData id) {
    return "<selected iid='"+id.getID()+"' iname='"+id.getName()+"' itype='"+id.getType()+"' ientity='"+ id.getEntityType()+ "'/>";
  }

  void printSelectedTag(PrintStream ps) {
    IdentityData idGroupEntity = null;
    for(Enumeration enum1 = m_selgroup.keys();enum1.hasMoreElements();) {
      Object key = enum1.nextElement();
      idGroupEntity = (IdentityData)m_selgroup.get(key);
      ps.println(toXML(idGroupEntity));
    }

    if(!m_groupsOnly)
      for(Enumeration enum2 = m_selentity.keys();enum2.hasMoreElements();) {
        Object key = enum2.nextElement();
        idGroupEntity = (IdentityData)m_selentity.get(key);
        ps.println(toXML(idGroupEntity));
      }
  }

  //-----------------------------------------------------------------------//
}
