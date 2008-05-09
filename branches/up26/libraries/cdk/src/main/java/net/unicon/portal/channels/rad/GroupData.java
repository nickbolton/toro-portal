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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Map;
import java.util.Comparator;
import java.util.HashSet;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.EntityNameFinderService;

import net.unicon.academus.apps.rad.Filter;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.apps.rad.XMLData;

/**
 * The wrapper of uPortal's Group, currently supporting 4 types of entities:
 *  - Object entity
 *  - IPerson entity
 *  - IEntityGroup entity
 *  - ChannelDefinition entity
 *
 * You can use it to get the root and all nodes of the tree of one of 4 above types.
 * The usage is:
 *   GroupData gr = new GroupData( GroupData.C_USER); --> root of user tree.
 *   XMLData xml = gr.xml();
 */

public class GroupData extends IdentityData {
  // Flag for filter
  /**
   * The filter will take only group
   */
  public static final int FILTER_GROUP = 1;

  /**
   * The filter will take only entity
   */
  public static final int FILTER_ENTITY = 2;

  /**
   * The filter will take both group and entity
   */
  public static final int FILTER_ALL = 3;

  // Predefined entity types
  /**
   * Contant for Object type of entity
   */
  public static final Class C_OBJECT = java.lang.Object.class;

  /**
   * Contant for IPerson type of entity
   */
  public static final Class C_USER = org.jasig.portal.security.IPerson.class;

  /**
   * Contant for IEntityGroup type of entity
   */
  public static final Class C_GROUP = org.jasig.portal.groups.IEntityGroup.class;

  /**
   * Contant for ChannelDefinition type of entity
   */
  public static final Class C_CHANNEL = org.jasig.portal.ChannelDefinition.class;

  /**
   * String Contant for Object type of entity
   */
  public static final String S_OBJECT = "o";

  /**
   * Sring contant for IPerson type of entity
   */
  public static final String S_USER = "u";
  public static final String S_USER_CAMPUS = "p";
  public static final String S_USER_CONTACT = "a";

  /**
   * String contant for IEntityGroup type of entity
   */
  public static final String S_GROUP = "g";

  /**
   * Stirng contant for ChannelDefinition type of entity
   */
  public static final String S_CHANNEL = "c";

  /**
   * the map of class and string for entity types
   */
  public static Hashtable m_classStr = new Hashtable();
  static {
    m_classStr.put(C_OBJECT,S_OBJECT);
    m_classStr.put(C_USER,S_USER);
    m_classStr.put(C_GROUP,S_GROUP);
    m_classStr.put(C_CHANNEL,S_CHANNEL);
  }

  // Helper
  public static final String PORTAL_GROUP = GROUP + SEPARATOR + S_USER;
  public static final String PORTAL_USER = ENTITY + SEPARATOR + S_USER;

  //------- Base Attributes of group ----------------------------//
  /**
   * Get description of group
   * @return the description of group
   */
  public String getDesc() {
    return (String)getA("desc");
  }

  /**
   * set description of group
   * @param desc the description of group
   */
  public void putDesc(String desc) {
    putA("desc",desc);
  }

  /**
   * Get creator of group
   * @return the id of creator
   */
  public String getCreator() {
    return (String)getA("creator");
  }

  /**
   * Set creator of group
   * @param creator the id of creator of group
   */
  public void putCreator(String creator) {
    putA("creator",creator);
  }

  //--------- Constructors --------------------------------------//

  GroupData m_root = null;
  IEntityGroup m_ieg = null;
  boolean m_detail = false;

  /**
   * Constructor of group
   * @param type the entity type
   * @param detail if this parameter is true all the descendant nodes will be filled
   *   with the detail information, else they contain only the id.
   * @throws Exception if uPortal's group service throws a exception.
   */
  public GroupData( Class type, boolean detail) throws Exception {
    this(GroupService.getRootGroup(type),null, detail);
    initChildren();
  }

  /**
   * Wrapper for IdentityData
   * @param gr
   */
  protected GroupData( IEntityGroup gr) {
    super(GROUP,(String)m_classStr.get(gr.getEntityType()),gr.getKey(),gr.getName());
  }

  /**
   * Constructor from IEntityGroup
   * @param gr uPortal's IEntityGroup
   * @param root the root of this GroupData
   * @param detail flag for fill node information
   * @throws Exception
   */
  public GroupData( IEntityGroup gr, GroupData root, boolean detail) throws Exception {
    this(gr);
    m_detail = detail;
    m_ieg = gr;
    m_root = root==null?this:root;

    putDesc(gr.getDescription());
    putCreator(gr.getCreatorID());

    // Save key <-> GroupData object as hidden field
    m_root.putH(gr.getKey(),this);
  }

  //-------------------------------------------------------------------------//

  /**
   * Get the root of this GroupData node
   * @return the root of tree contains this node
   */
  public GroupData getRoot() {
    return m_root;
  }

  /**
   * Get a group within the tree containing this group node
   * @param id the key of group to get
   * @return the GroupData of group with the given id
   * @throws Exception
   */
  public GroupData getGroup( String id) throws Exception {
    if( m_root != null)
      return (GroupData)m_root.getH(id);
    else
      return null;
  }

  /**
   * Check wether this group contains given group/entity, presented via given IdentityData
   * @param id the IdentityData to check
   * @return true if this group contains the input IdentityData, else - false
   * @throws Exception
   */
  public boolean contains( IdentityData id) throws Exception {
    IGroupMember member = createGroupMember(id);
    if( member == null)
      return true; // ???
    else
      return m_ieg.deepContains(member);
  }

  /**
   * Get total count of distinct entities (not group) for a list of group/entity
   * @param ids a array of IdentityData specifying the group/entity to count
   * @return the number of distinct entities in the given list
   * @throws Exception
   */
  public static int getEntitySize( IdentityData[] ids) throws Exception {
  	IdentityData[] temp = ids; 
    // Neu element sau chua trong element truoc thi set null.
    for ( int i = 0; i < ids.length; i++) {
      if( ids[i] != null && isPortalGroup(ids[i])) {
        GroupData gr = GroupData.group(ids[i], false);
        if( gr != null)
          for( int j = 0; j < ids.length; j++)
            if( j != i && ids[j] != null && isPortalIdentity(ids[i]) && gr.contains(ids[j]))
              ids[j] = null;
      }
    }

    // Get distinct users
    Hashtable hs = new Hashtable();
    for ( int i = 0; i < ids.length; i++) {
    	if( ids[i] != null){
    		if(isPortalGroup(ids[i])){
    	// Get all entities and put to hs
    	GroupData gr = GroupData.group(ids[i],false);
    			if( gr != null)
    				{	
    					IdentityData[] all = gr.getAllEntities();
    					if(all != null)
    					{
    						for( int j=0; j<all.length; j++){
    							hs.put(all[j].getIdentifier(), all[j]);
    						}
    					}
    				}
    		}
    		else
			{
			hs.put(ids[i].getIdentifier(), ids[i]);
			}
    	}
    }
    return hs.size();
  }

  /**
   * Exclude all duplicated elements in the array of groups/entities.
   * The result array contains only distinct elements from input array.
   * @param ids a array of IdentityData specifying the group/entity to exclude duplicate from
   * @return a array from input array that contains only distinct elements.
   * @throws Exception
   */
  public static IdentityData[] normalize( IdentityData[] ids) throws Exception {
    // Neu element sau chua trong element truoc thi set null.
    for ( int i = 0; i < ids.length; i++) {
      if( ids[i] != null && isPortalGroup(ids[i])) {
        GroupData gr = GroupData.group(ids[i], false);
        if( gr != null)
          for( int j = 0; j < ids.length; j++)
            if( j != i && ids[j] != null && isPortalIdentity(ids[i]) && gr.contains(ids[j]))
              ids[j] = null;
      }
    }

    // Get distinct...
    Vector v = new Vector();
    for ( int i = 0; i < ids.length; i++)
      if( ids[i] != null)
        v.addElement(ids[i]);
    return (IdentityData[])v.toArray( new IdentityData[0]);
  }

  /**
   * Initialize all children nodes (child group and entities)
   * @throws Excepion if GroupService throws it
   */
  public void initChildren() throws Exception {
    // check
    GroupData[] objs = (GroupData[])getE("group");
    IdentityData[] es = (IdentityData[])getH("entities");
    if( objs != null && es != null)
      return;

    // Using GroupService to get
    Vector vGroups = new Vector();
    Vector vEntities = new Vector();
    if( m_ieg.hasMembers()) {
      // Use GroupService to get children
      Iterator iter = m_ieg.getMembers();
      while (iter.hasNext()) {
        IGroupMember member = (IGroupMember)iter.next();
        if (member.isGroup() && objs == null)
          vGroups.addElement(new GroupData((IEntityGroup)member, m_root, m_detail));
        else if( member.isEntity() && es == null)
          vEntities.addElement(createEntity(member));
      }
    }

    // save groups
    if( objs == null) {
      if( vGroups.size() > 0) {
        objs = new GroupData[vGroups.size()];
        for( int i = 0; i < vGroups.size(); i++)
          objs[i] = (GroupData)vGroups.elementAt(i);
        putE("group",objs);
      }
      putA("groups",Integer.toString(vGroups.size()));
    }

    // save entities as hidden field
    if( es == null) {
      if( vEntities.size() > 0) {
        es = new IdentityData[vEntities.size()];
        for( int i = 0; i < vEntities.size(); i++)
          es[i] = (IdentityData)vEntities.elementAt(i);

        if( m_detail)
          Finder.findDetails(es,null);

        Arrays.sort(es, new IdentityComparator());
        putH("entities",es);
      }
      putA("entities",Integer.toString(vEntities.size()));
    }
  }

  /**
   * Get child groups with checking in the cache.
  * @return all child groups.
  * @throws Excepion if GroupService throws it
   */
  public GroupData[] getChildGroups() throws Exception {
    // Check cache
    GroupData[] objs = (GroupData[])getE("group");
    if( objs == null)
      objs = childGroups();
    return objs;
  }

  /**
   * Get child entities with checking in the cache.
  * @return all child entities.
  * @throws Excepion if GroupService throws it
   */
  public IdentityData[] getChildEntities() throws Exception {
    // Check cache  	
    IdentityData[] es = (IdentityData[])getH("entities");
    if( es == null)
      es = childEntities();
    return es;
  }

  /**
   * Get all descendant entities with checking in the cache.
  * @return all descendant entities
  * @throws Excepion if GroupService throws it
   */
  public IdentityData[] getAllEntities() throws Exception {
    // Check cache
    IdentityData[] es = (IdentityData[])getH("allentities");
    if( es == null)
      es = allEntities();
    return es;
  }


  /**
    * Get all ancestors (those are groups).
   * @return all GroupData ancestors of this node
   * @throws Excepion if GroupService throws it
    */
  public GroupData[] getAncestors() throws Exception {
    if( m_ieg == null)
      return getAncestors(this, m_detail);

    Vector grps = new Vector();
    Iterator iter = m_ieg.getAllContainingGroups();
    while (iter.hasNext())
      grps.addElement(new GroupData((IEntityGroup)iter.next(),m_root,m_detail));

    GroupData[] g = new GroupData[grps.size()];
    for( int i = 0; i < grps.size(); i++)
      g[i] = (GroupData)grps.elementAt(i);
    return g;
  }

  /**
   * Get the xml presentation of this group
   * @return the XMLData of this node.
   */
  public XMLData xml() {
    XMLData r = new XMLData();
    r.putE("group",m_root);
    return r;
  }

  /**
   * Check empty of this node
   * @return true if it is not empty, else - false
   */
  public boolean hasMembers() throws Exception {
    return m_ieg.hasMembers();
  }

  // alias as other identifier
  /**
   * Override base method.
   */
  public String getAlias() {
    return getName();
  }
  /**
   * Override base method.
   */
  public void putAlias(String alias) {
    putName(alias);
  }

  //----------- for identity data ----------------------------------------//

  /**
   * Extract elements from this group by given filter.
   * @param v all output elements will go here
   * @param f the filter to accept element from this group
   * @param limit max number of elements can take
   * @param flag indicate the output element is group or entities or both.
   * @throws Exception
   */
  public void filter( Vector v, Filter f, int limit, int flag) throws Exception {
    // This: GroupData
    if( v.size() < limit) {
      Finder.findDetail(this,null);
      Object out = f.convert(this);
      if( out != null && (flag == FILTER_GROUP || flag == FILTER_ALL))
        v.add(out);
    }

    // Child entities
    if( v.size() < limit && (flag == FILTER_ENTITY || flag == FILTER_ALL)) {
      IdentityData[] entities = getChildEntities();
      if( entities != null)
        for( int i = 0; i < entities.length; i++) {
          Finder.findDetail(entities[i],null);
          Object out = f.convert(entities[i]);
          if( out != null) {
            v.add(out);
            if( v.size() >= limit)
              return;
          }
        }
    }

    // Child group, recursively
    GroupData[] childGroups = getChildGroups();
    if( childGroups != null)
      for( int i = 0; i < childGroups.length && v.size() < limit; i++)
        childGroups[i].filter( v, f, limit, flag);
  }

  //---------------------------------------------------------------------//

  /**
   * Get root of group tree of given type
   * @param etype type of entity to get, should be:
   *  GroupData.S_OBJECT  ("o")
   *  GroupData.S_USER    ("u")
   *  GroupData.S_GROUP   ("g")
   *  GroupData.S_CHANNEL ("c")
   */
  static GroupData root( String etype, boolean detail) throws Exception {
    return new GroupData(lookupClass( etype), detail);
  }

  /**
   * Get group identified by given IdentityData, not for entity
   * @param gr identify the group to get
   * @param detail flag for getting detail information within output GroupData
   * @return GroupData for givne IdentityData
   * @throws Exception re-throw from GroupService.
   */
  public static GroupData group( IdentityData gr, boolean detail) throws Exception {
    // Check and get from Cache
    GroupData r = root(gr.getEntityType(), detail);

    // Find recursively if not found in the cache
    String id = gr.getID();
    GroupData ret = (GroupData)r.getH(id);
    if( ret == null) {
      ret = r.findGroup( id);
      if( ret != null)
        r.putH( id, ret);
    }
    return ret;
  }

  /**
   * Get ancestors for given entity
   * @param entity the entity for which all ancestors will be retrieved.
   * @param detail flag for getting detail information within output GroupData
   * @return all groups those are ancestors of given entity
   * @throws Exception rethrows from GroupService
   */
  public static GroupData[] getAncestors(IdentityData entity, boolean detail) throws Exception {
    IGroupMember member = createGroupMember( entity);
    if( member == null)
      return null;

    Vector grps = new Vector();
    Iterator groups = member.getAllContainingGroups();
    while (groups.hasNext())
      grps.addElement(new GroupData((IEntityGroup)groups.next(),null, detail));

    GroupData[] g = new GroupData[grps.size()];
    for( int i = 0; i < grps.size(); i++)
      g[i] = (GroupData)grps.elementAt(i);
    return g;
  }

  /**
   * Replace the groups by theirs entities.
   * @param ids contains the groups to be expanded to theirs entities
   * @param detail flag for filling detail information
   * @return all entities for given input groups/entities array.
   * @throws Exception if GroupService throws an exception.
   */
  public static IdentityData[] expandGroups( IdentityData[] ids, boolean detail) throws Exception {
    HashSet hs = new HashSet();
    for (int i=0; i < ids.length; i++) {
      // Group: get all entities in this group
      if( ids[i].getType().equals(IdentityData.GROUP) &&
          ids[i].getEntityType().equals(GroupData.S_USER)) {
        GroupData gr = group( ids[i], detail);
        IdentityData[] entities = gr.getAllEntities();
        if( entities != null)
          for( int j = 0; j < entities.length; j++)
            hs.add(entities[j]);
      }

      // entity
      else
        hs.add(ids[i]);
    }

    return (IdentityData[])hs.toArray(new IdentityData[0]);
  }

  //----------------------------------------------------------------------//

  /**
   * Convert entity type from String to Class
   * @param s the String identifies the entity type
   * @return the Class of entity type
   */
  public static Class lookupClass( String s) {
    Enumeration e = m_classStr.keys();
    while( e.hasMoreElements()) {
      Class cls = (Class)e.nextElement();
      String val = (String)m_classStr.get(cls);
      if( val.equals(s))
        return cls;
    }
    if (S_USER_CAMPUS.equals(s) || S_USER_CONTACT.equals(s))
        return C_USER;

    return C_OBJECT;
  }

  /**
   * Check either given IdentityData is portal user
   * @param id IdentityData to check
   * @return true if it is portal user
   */
  public static boolean isPortalUser( IdentityData id) {
    return id.getIdentifier().startsWith(PORTAL_USER);
  }

  /**
   * Check either given IdentityData is portal group
   * @param id IdentityData to check
   * @return true if it is portal group
   */
  public static boolean isPortalGroup( IdentityData id) {
    return id.getIdentifier().startsWith(PORTAL_GROUP);
  }

  /**
   * Check either given IdentityData is portal group or user
   * @param id IdentityData to check
   * @return true if it is portal user or group
   */
  public static boolean isPortalIdentity( IdentityData id) {
    return id.getEntityType().equals(S_USER);
  }

  //----------------------------------------------------------------------//

  /**
  * Search and sort on all elements of specified tree of entity type.
   * @param etype type of entity to search, should be:
   *  GroupData.S_OBJECT  ("o")
   *  GroupData.S_USER    ("u")
   *  GroupData.S_GROUP   ("g")
   *  GroupData.S_CHANNEL ("c")
   * @param f indicates how to search.
  * @param flag indicates group or user to take to search
  * @param c used to sort the result
  * @param limit max number of elements to search
  * @param detail flag for filling detail information (name, fullname,...)
  * @return a Vector containing sorted result of search.
   */
  public static Vector search( String etype, Filter f, int flag, Comparator c, int limit, boolean detail) throws Exception {
    Sorted v = new Sorted( c);
    GroupData gr = root( etype, false);//true); Tien 1016
    gr.filter(v,f,limit,flag);
    if(v.size() > 0) {
      IdentityData model = (IdentityData)v.elementAt(0);
      for(int i = 1; i < v.size() ; ) {
        if(((IdentityData)v.elementAt(i)).getIdentifier().compareTo(model.getIdentifier())==0) {
          v.remove(i);
        } else {
          model = (IdentityData)v.elementAt(i);
          i++;
        }
      }
    }
    /*
    if( v.size() > limit) {
      v.setSize(limit);
      if( detail)
        Finder.findDetails((IdentityData[])v.toArray(new IdentityData[0]), null);
    }
    */
    return v;
  }

  //----------------------------------------------------------------------//
  /**
   * Find descendent group with given id
   * @param id group id to find
   * @return null if not found, else - the GroupData with given id
   * @throws Exception
   */
  GroupData findGroup( String id) throws Exception {
    // Check this
    if( getID().equals(id))
      return this;

    // Find in child groups
    GroupData[] children = getChildGroups();
    if( children != null)
      for( int i = 0; i < children.length; i++) {
        GroupData gr = children[i].findGroup(id);
        if( gr != null)
          return gr;
      }

    // not found
    return null;
  }

  /**
   * Get child groups from IEntityGroup and update related
   * (child elements "group" and attribute "groups")
   */
  GroupData[] childGroups() throws Exception {
    if( !m_ieg.hasMembers())
      return null;
    Vector v = new Vector();
    Iterator iter = m_ieg.getMembers();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup())
        v.addElement(new GroupData((IEntityGroup)member, m_root, m_detail));
    }

    GroupData[] objs = null;
    removeE("group");
    if( v.size() > 0) {
      objs = new GroupData[v.size()];
      for( int i = 0; i < v.size(); i++)
        objs[i] = (GroupData)v.elementAt(i);
      putE("group",objs);
    }
    putA("groups",Integer.toString(v.size()));
    return objs;
  }

  public static IdentityData createEmailEntity( String email) {
    IdentityData id = new IdentityData( ENTITY, S_USER, ID_UNKNOWN, email, email);
    id.putEmail(email);
    return id;
  }

  public static IdentityData createEntity( IGroupMember member) {
    if( Channel.m_upVersion.startsWith("2.0"))
      return new IdentityData( ENTITY, (String)m_classStr.get(member.getEntityType()), member.getKey(), null);
    else
      return new IdentityData( ENTITY, (String)m_classStr.get(member.getEntityType()),null, member.getKey(), null);
  }

  public static IdentityData createGroup( IEntityGroup gr) {
    return new GroupData( gr);
  }

  public static IdentityData createPortalMember( IGroupMember member) {
    return member.isGroup()? createGroup((IEntityGroup)member):createEntity(member);
  }

  public static IGroupMember createGroupMember( IdentityData id) throws Exception {
    Class cls = ( id.getType().equals(GROUP))?C_GROUP:lookupClass(id.getEntityType());
    String key = getPortalKey( id, cls);
    return key==null?null:GroupService.getGroupMember( key, cls);
  }

  public static String getPortalKey( IdentityData id, Class cls) throws Exception {
      if( cls == null)
          cls = ( id.getType().equals(GROUP))?C_GROUP:lookupClass(id.getEntityType());

      String key = null;
      if (id.getType().equals(GROUP) || id.getAlias() == null)
          key = id.getID();
      else
          key = id.getAlias();

      return (isPortalUser(id) || cls.equals(C_USER) ?
                  key
                : (isPortalGroup(id) ?
                    id.getID() 
                  : (id.toString().startsWith("G|g") ?
                      key
                    : id.toString())));
  }


  public static IdentityData createIdentityData( String key, Class cls) throws Exception {
    String et = (String)GroupData.m_classStr.get(cls);
    if (et.equals(S_GROUP))
      return new IdentityData( GROUP, S_USER, key, null);
    else if (et.equals(S_USER))
      return Channel.m_upVersion.startsWith("2.0")? new IdentityData( ENTITY, S_USER, key, null, null): new IdentityData( ENTITY, S_USER, null, key, null);
    else
      return new IdentityData(key);
  }

  public static IdentityData createPortalUser( IPerson p) {
    if( Channel.m_upVersion.startsWith("2.0"))
      return new IdentityData(ENTITY, S_USER, ""+p.getID(), (String)p.getAttribute("username"), p.getFullName());
    else
      return new IdentityData(ENTITY, S_USER, (String)p.getAttribute("username"), (String)p.getAttribute("username"), p.getFullName());
  }

  /**
   * Get child entities from IEntityGroup, updates related.
   */
  IdentityData[] childEntities() throws Exception {
    // Use GroupService to get
  	if( !m_ieg.hasMembers())
      return null;
    Vector v = new Vector();
    Iterator iter = m_ieg.getMembers();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if( member.isEntity())
        v.addElement(createEntity(member));
    }

    IdentityData[] es = null;
    removeH("entities");
    if( v.size() > 0) {
      es = new IdentityData[v.size()];
      for( int i = 0; i < v.size(); i++)
        es[i] = (IdentityData)v.elementAt(i);

      if( m_detail)
        Finder.findDetails(es,null);

      // sort the data
      Arrays.sort(es, new IdentityComparator());
      putH("entities",es);
    }
    putA("entities",Integer.toString(v.size()));
    return es;
  }

  /**
   * Get all entities without cache
   */
  IdentityData[] allEntities() throws Exception {
    Vector v = new Vector();
    Iterator iter = m_ieg.getAllEntities();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if( member.isEntity())
        v.addElement(createEntity(member));
    }

    // save as hidden field
    IdentityData[] es = null;
    removeH("allentities");
    if( v.size() > 0) {
      es = new IdentityData[v.size()];
      for( int i = 0; i < v.size(); i++)
        es[i] = (IdentityData)v.elementAt(i);

      if( m_detail)
        Finder.findDetails(es,null);

      putH("allentities", es);
    }
    return es;
  }
}
