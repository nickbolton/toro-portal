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


package net.unicon.academus.apps.rad;

import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

/**
 * This class identifies a node in the tree by a string. A node is either leaf or
 * composite, which can contains other nodes. We will call a leaf node is an "entity"
 * and composite node - "group". Thus a node has the following properties:
 * - Type: group or entity
 * - EntityType: type of entity, or type of entities a group node contains.
 * - ID: unique in (Type,EntityType) context.
 * - Name is human-readable name.
 *
 * Below the words "entity of this node" refers to this node if it is ENTITY or to child nodes if it is GROUP.
 */

public class IdentityData extends XMLData {
  /**
   * Leaf type of node
   */
  public static final String ENTITY = "E";

  /**
   * Composite type of node
   */
  public static final String GROUP = "G";

  /**
   * The old separator used to format this IdentityData object
   */
  public static final String OLD_SEPARATOR = "@";

  /**
   * The current separator used to format this IdentityData object
   */
  public static final String SEPARATOR = "|";

  /**
   * Unknown user
   */
  public static final String ID_UNKNOWN = "-1";

  public IdentityData() {}

  /**
   * Clonnable constructor
   * @param other The other IdentityData to clone.
   */
  public IdentityData(IdentityData other) {
    putType(other.getType());
    putEntityType(other.getEntityType());
    putOID(other.getID());
    putAlias( other.getAlias());
    putName(other.getName());
    putEmail( other.getEmail());
  }

  /**
   * Constructor with the most of parameters
   * @param type The node type, should be ENTITY or GROUP
   * @param entityType The one-character String indicates the type of
   * this node ( if type is ENTITY) or child node ( if type is GROUP)
   * @param id The identifier string of this node.
   * @param alias The alias name of this node, i.e., the short human-readable name of this node instead of id.
   * @param name The full name of this node.
   */
  public IdentityData( String type, String entityType, String id, String alias, String name) {
    putType(type);
    putEntityType(entityType);
    putOID(id);
    putAlias( alias);
putName(name == null? (alias!=null?alias:id): name);
  }

  /**
   * Constructor with the most of parameters except the alias attribute
   * @param type The node type, should be ENTITY or GROUP
   * @param entityType The one-character String indicates the type of
   * this node ( if type is ENTITY) or child node ( if type is GROUP)
   * @param id The identifier string of this node.
   * @param name The full name of this node.
   */
  public IdentityData( String type, String entityType, String id, String name) {
    putType(type);
    putEntityType(entityType);
    putOID(id);
    putName(name == null? id: name);
  }

  /**
   * Other constructor from id string and full name
   * @param id4 is in format: "type@etype@id@alias" or "type|etype|id|alias"
   * @param name The full name of this node.
   */
  public IdentityData( String id4, String name) {
    StringTokenizer st = new StringTokenizer(id4,SEPARATOR+OLD_SEPARATOR);
    if (st.hasMoreTokens())
      putType( st.nextToken());
    if (st.hasMoreTokens())
      putEntityType( st.nextToken());
    if (st.hasMoreTokens())
      putOID( st.nextToken());
    if (st.hasMoreTokens())
      putAlias( st.nextToken());

    putName(name == null? getID(): name);
  }

  /**
   * Constructor from String that is the result of toString() method.
   * @param desc is in format: "type@etype@id@alias@fullname"
   */
  public IdentityData( String desc) {
    StringTokenizer st = new StringTokenizer(desc,SEPARATOR+OLD_SEPARATOR);
    if (st.hasMoreTokens())
      putType( st.nextToken());
    if (st.hasMoreTokens())
      putEntityType( st.nextToken());
    if (st.hasMoreTokens())
      putOID( st.nextToken());
    if (st.hasMoreTokens())
      putAlias( st.nextToken());
    if (st.hasMoreTokens())
      putName( st.nextToken());
  }

  /**
   * Override the base toString() method.
   * @return The String identified this IdentityData object
   */
  public String toString() {
    return getType() + SEPARATOR + getEntityType() + SEPARATOR + getID() +
           SEPARATOR + getAlias() + SEPARATOR + getName();
  }

  /**
   * The same as toString method but here it uses other separator than standard.
   * @param separator The separator to be used in the resulting String.
   * @return The String identified this IdentityData object with given separator.
   */
  public String toString(char separator) {
    return getType() + separator + getEntityType() + separator + getID() +
           separator + getAlias() + separator + getName();
  }

  /**
   * Override the base equals method.
   */
  public boolean equals(Object obj) {
    if ( obj == null )
      return false;
    if ( obj == this )
      return true;
    if ( ! ( obj instanceof IdentityData))
      return false;

    IdentityData other = (IdentityData) obj;
    return getType().equals(other.getType()) &&
           getEntityType().equals( other.getEntityType()) &&
           getID().equals( other.getID());
  }

  /**
   * Get the container type: 'E' - entity or 'G' - group ( can contains others)
   * @return String either ENTITY or GROUP
   */
  public String getType() {
    return (String)getA("itype");
  }

  /**
   * Change the container type.
   * @param type is either ENTITY or GROUP.
   */
  public void putType(String type) {
    putA("itype",type);
  }

  /**
   * Get the type of entity.
   * @return The string indicates the type of entity ( this node or child node if this node is GROUP)
   */
  public String getEntityType() {
    return (String)getA("ientity");
  }

  /**
   * Change entity type
   * @param type The new type of entity
   */
  public void putEntityType(String type) {
    putA("ientity",type);
  }

  /**
   * Get the identifier string (key of entity) of this IdentityData
   * @return identifier String
   */
  public String getID() {
    return (String)getA("iid");
  }

  /**
   * Change the identifier string (key of entity) of this IdentityData
   * @param id The new identifier String
   */
  public void putID(String id) {
    putA("iid",id);
    putA("ialias", id);
  }

  public void putOID(String id) {
    putA("iid",id);
  }

  /**
   * Alias as other identifier. This method allow to get the alias name.
   * @return The alias of this.
   */
  public String getAlias() {
    return (String)getA("ialias");
  }

  /**
   * Change alias.
   * @param alias The new alias name.
   */
  public void putAlias(String alias) {
    putA("ialias", alias);
  }

  //
  /**
   * The name here means the full name of entity. This method return the name of this.
   * @return The name of IdentityData object.
   */
  public String getName() {
    return (String)getA("iname");
  }

  /**
   * Change the name of IdentityData.
   * @param name the new name of this IdentityData object.
   */
  public void putName(String name) {
    putA("iname",name);
  }

  /**
   * Get e-mail address, set by method putEmail() before.
   * @return the email address stored in this IdentityData object.
   */
  public String getEmail() {
    return (String)getA("email");
  }

  /**
   * Change the email address of this IdentityData object.
   * @param email the new email to be stored in this IdentityData object.
   */
  public void putEmail(String email) {
    putA("email",email);
  }

  /**
   * The full identifier String of this IdentityData. It is in the format:
   * <ContainerType> SEPARATOR <EntityType> SEPARATOR <ID>
   * That String is fully identified this IdentityData and so can be used to store it to database.
   * @return The identifier string
   */
  public String getIdentifier() {
    return getType() + SEPARATOR + getEntityType() + SEPARATOR + getID();
  }

  /**
   * The same as the method getIdentifier without parameters but the SEPARATOR
   * now is the given separator instead standard SEPARATOR.
   * @param separator The separator used in resulting identifier string.
   * @return The identifier string
   */
  public String getIdentifier(char separator) {
    return getType() + separator + getEntityType() + separator + getID();
  }

  /**
   * In the real world it offten happens that one user may have more than one identifier.
   * In office 1 he/she has the identifier <Id_Office1>, in other office 2 - <Id_Office2>,...
   * The entity is one but it may have many identifiers in the different contexts.
   * We call the other identifiers of the same entity as refferences. This method returns
   * all refferent identifier string.
   * @return an array of all refferent identifiers
   */
  public String[] getIdentifiers() {
    IdentityData[] arr = getAllRefs();
    String[] ret = new String[arr.length];
    for( int i = 0; i < arr.length; i++)
      ret[i] = arr[i].getIdentifier();
    return ret;
  }

  /**
   * The same as method getIdentifiers() but with the given separator rather than standard separator.
   * @return an array of all refferent identifiers
   */
  public String[] getIdentifiers( char separator) {
    IdentityData[] arr = getAllRefs();
    String[] ret = new String[arr.length];
    for( int i = 0; i < arr.length; i++)
      ret[i] = arr[i].getIdentifier(separator);
    return ret;
  }

  Hashtable m_refs = new Hashtable();

  /**
   * Refferences are IdentityData from different sources and all of them reffer to the same one entity.
   * This method get the refference with the given source name
   * @param name The name of refference source.
   * @return The IdentityData object in the source with given name.
   */
  public IdentityData getRef(String name) {
    return (IdentityData)m_refs.get(name);
  }

  /**
   * Change the refference.
   * @param name The name of refference source.
   * @param ref The new IdentityData.
   */
  public void putRef(String name, IdentityData ref) {
    if( ref != null)
      m_refs.put(name, ref);
    else
      m_refs.remove(name);
  }

  /**
   * Get the id field of a refference
   * @param name The name of the refference source.
   * @return the id field in the IdentityData
   */
  public String getRefID(String name) {
    return (String)getA(name);
  }

  /**
   * Change the Id field of a refference.
   * @param name the name of the refference source
   * @param refID the new Id of the field Id in the IdentityData.
   */
  public void putRefID(String name, String refID) {
    putA(name, refID);
  }

  /**
   * Get the full email address in the format: "name" <email>
   * @return The email address in full format.
   */
  public String getFullEmail() {
    String name = getName();
    String email = getEmail();
    if( name == null || name.length() == 0)
      return email;
    else
      return "\"" + name + "\" <" + email + ">";
  }

  //------------------//

  IdentityData[] getAllRefs() {
    HashSet hs = new HashSet();
    Collection c = m_refs.values();
    hs.addAll(c);
    hs.add(this);
    return (IdentityData[])hs.toArray(new IdentityData[0]);
  }

  /**
   * This method is used in the SQL query with the LIKE clause.
   */
  public static String like( String id) {
    return id.replace('@','_').replace('|','_');
  }
}
