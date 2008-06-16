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

import java.io.*;
import java.util.*;
import java.text.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * It is the another wrapper of XML data, rather than standard org.w3c.dom.* notation.
 * It can contain an entire tree represented in the standard XML format. It provides basic methods for:
 *  - read XML stream
 *  - output in the XML format
 *  - get/put any attributes/elements in all nodes in the tree.
 */
public class XMLData {
  /**
   * The text of this node.
   */
  public String m_text = null;

  /**
   * All node's attributes.
   */
  public Hashtable m_attrs = new Hashtable();

  /**
   * All node's child elements
   */
  public Hashtable m_elems = new Hashtable();

  /**
   * Some hiden attributes of this node. Those attribute will not printed in the xml ouput tree.
   */
  public Hashtable m_hiddens = new Hashtable();

  //- accessors and mutators -------------------------------------------------//

  /**
   * Get the text content of this node.
   * @return The text this node contains.
   */
  public String get
    () {
    return m_text;
  }

  /**
   * Change the text of node.
   * @param text new text of node
   */
  public void put(String text) {
    m_text = text;
  }

  /**
   * Get hidden attribute value
   * @param key The name of hidden attribute.
   * @return The Object associated with given key.
   */
  public Object getH(String key) {
    return m_hiddens.get(key);
  }

  /**
   * Change the value of hidden attribute.
   * @param key The name of hidden attribute.
   * @param value The new value of key
   */
  public void putH(String key, Object value) {
    if (value == null)
      m_hiddens.remove(key);
    else
      m_hiddens.put(key, value);
  }

  /**
   * Remove a hidden attribute
   * @param key The name of hidden attribute to remove
   */
  public void removeH(String key) {
    m_hiddens.remove(key);
  }

  /**
   * Get the attribute value of this node.
   * @param key The name of attribute to get.
   * @return The value of given attribute key
   */
  public Object getA(String key) {
    return m_attrs.get(key);
  }

  /**
   * Change the attribute value of this node.
   * @param key The name of attribute to be changed.
   * @param value The new value of given attribute.
   */
  public void putA(String key, Object value) {
    if (value == null)
      m_attrs.remove(key);
    else
      m_attrs.put(key, value);
  }

  /**
   * Remove an attribute from this node.
   * @param key The name of attribute to remove.
   */
  public void removeA(String key) {
    m_attrs.remove(key);
  }

  /**
   * Get an child element(s) with the given name. If this node contains
   * more than one child elements with the same name, an array will return.
   * @param key The name of element.
   * @return The element(s) of given key.
   */
  public Object getE(String key) {
    return m_elems.get(key);
  }

  /**
   * Change the element(s) with given key.
   * @param key The name of element.
   * @value The new element(s) for this key.
   */
  public void putE(String key, Object value) {
    if (value == null)
      m_elems.remove(key);
    else
      m_elems.put(key, value);
  }

  /**
   * Get all child elements with the same given name.
   * @param key The name of element to get.
   * @return an array of elements with the given name.
   */
  public Object[] rgetE(String key) {
    Object value = getE(key);
    if (value == null)
      return null;
    if (value instanceof Object[])
      return (Object[])value;
    Object[] valuer = new Object[1];
    valuer[0] = value;
    return valuer;
  }

  /**
   * Add a child element with the given name.
   * @param key The name of element to add.
   * @param valud The child element to add.
   */
  public void rputE(String key, Object value) {
    Object old = getE(key);
    if (old == null)
      putE(key, value);
    else if (old instanceof Object[]) {
      Object[] arr = new Object[((Object[])old).length+1];
      for (int i = 0; i < arr.length-1; i++)
        arr[i] = ((Object[])old)[i];
      arr[arr.length-1] = value;
      putE(key, arr);
    } else {
      Object[] arr = new Object[2];
      arr[0] = old;
      arr[1] = value;
      putE(key, arr);
    }
  }

  /**
   * Remove all child elements with the given name.
   * @param key The name of child elements to remove.
   */
  public void removeE(String key) {
    m_elems.remove(key);
  }

  /**
   * Clear all child elements and attributes.
   */
  public void clear() {
    m_elems.clear();
    m_attrs.clear();
  }

  //- helpers ----------------------------------------------------------------//

  /**
   * Create String in the XML format for any XMLData with the given name of root node.
   * @param data The tree to format to xml string.
   * @param name The name of the root node in the resulting xml string.
   * @return The String in the xml format with the root having the given name.
   */
  public static String xml( XMLData data, String name) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    XMLData temp = new XMLData();
    temp.putE(name, data);
    temp.print(ps,1);
    return baos.toString();
  }

  /**
   * Make sure a String can valid in the xml string. Most of changes are for the special characters.
   * @param s The string to be in the xml string.
   * @return The string "escaped" for xml format.
   */
  static public String format(String s) {
    return XML.esc( s);
  }

  /**
   * Replace all occurences of one String by other String in the source String
   * @param s1 The Source String
   * @param p1 The String to be replaced.
   * @param p2 The String to replace by.
   * @return The new String that is result of replacing all occurences of p1 by p2 in the s1.
   */
  static public String replace(String s1, String p1, String p2) {
    String s2 = "";
    String s = s1;
    while (s != null) {
      int i = s.indexOf(p1);
      if (i == -1) {
        s2 += s;
        return s2;
      }
      s2 += s.substring(0, i)+p2;
      s = s.substring(i+p1.length());
    }
    return null;
  }

  //- print ------------------------------------------------------------------//

  /**
   * The format of date in the XML stream.
   */
  static public final SimpleDateFormat SDF = new SimpleDateFormat("M/d/yy_H:mm");

  /**
   * Craete the indent string at any level.
   * @param level The indent level.
   * @return The String with the indent at given level .
   */
  static public String indent(int level) {
    String indent = "";
    for (int i = 0; i < level; i++)
      indent += "  ";
    return indent;
  }

  /**
   * The helper method for
   * public void print(PrintStream out, int level)
   */
  static void print(PrintStream out, String val, int level, boolean newline) {
    //out.print(val);
    if (!newline)
      out.print(indent(level)+val);
    else
      out.println(indent(level)+val);
  }

  /**
   * The helper method for
   * public void print(PrintStream out, int level)
   */
  void print(PrintStream out, String key, Object val, int level) {
    if (val instanceof XMLData)
      ((XMLData)val).print(out, key, level);
    else if (val instanceof Date)
      print(out, "<"+key+">"+SDF.format((Date)val).toLowerCase()+"</"+key+">", level, true);
    else if (val instanceof String)
      print(out, "<"+key+">"+format((String)val)+"</"+key+">", level, true);
    else
      print(out, "<"+key+">"+val+"</"+key+">", level, true);
  }

  /**
   * Create a output stream in the xml format.
   * @param out The output stream
   * @param level It is the indent level.
   */
  public void print(PrintStream out, int level) {
    for (Enumeration e = m_elems.keys(); e.hasMoreElements();) {
      String elem = (String)e.nextElement();
      Object val = m_elems.get(elem);
      if (val instanceof Object[]) {
        for (int i = 0; i < ((Object[])val).length; i++)
          print(out, elem, ((Object[])val)[i], level);
      } else
        print(out, elem, val, level);
    }
  }

  /**
   * The helper method for
   * public void print(PrintStream out, int level)
   */
  public void print(PrintStream out, String key, int level) {
    print(out, "<"+key, level, false);
    for (Enumeration e = m_attrs.keys(); e.hasMoreElements();) {
      String attr = (String)e.nextElement();
      Object val = m_attrs.get(attr);
      if (val instanceof Date)
        print(out, " "+attr+"='"+SDF.format((Date)val).toLowerCase()+"'", 0, false);
      //- Truong 0211
      else if (val instanceof String)
        print(out, " "+attr+"='"+format((String)val)+"'", 0, false);
      else
        print(out, " "+attr+"='"+val+"'", 0, false);
    }
    String text = get
                    ();
    if (text == null && m_elems.isEmpty())
      print(out, "/>", 0, true);
    else {
      if (text == null)
        text = "";
      print(out, ">"+format(text), 0, false);
      if (m_elems.isEmpty())
        print(out, "</"+key+">", 0, true);
      else {
        print(out, "", 0, true);
        print(out, level+1);
        print(out, "</"+key+">", level, true);
      }
    }
  }

  //- parse ------------------------------------------------------------------//

  /**
   * Extract the text content for given node.
   */
  static String readText(Node node) throws Exception {
    Node text = node.getFirstChild();
    if (text == null || text.getNodeType() != Node.TEXT_NODE)
      return null;
    return text.getNodeValue();
  }

  /**
   * Transter all informations contained in the standard Node into this XMLData object.
   * @param node The standard org.w3c.dom.Node to extract informations from.
   */
  public void parse(Node node) throws Exception {
    NamedNodeMap attrs = node.getAttributes();
    if( attrs != null)
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        putA(attr.getNodeName(), readText(attr));
      }

    NodeList elems = node.getChildNodes();
    if( elems != null)
      for (int i = 0; i < elems.getLength(); i++) {
        Node elem = elems.item(i);
        short type = elem.getNodeType();
        if (type == Node.TEXT_NODE) {
          String text = elem.getNodeValue().trim();
          if (text.length() > 0)
            put(text);
        } else if (type == Node.ELEMENT_NODE) {
          XMLData child = new XMLData();
          child.parse(elem);
          rputE(elem.getNodeName(), child);
        }
      }
  }

  /**
   * Parse the xml stream into this XMLData object.
   * @param in The input stream to take from.
   * @return The name of root node.
   */
  public String parse(InputStream in) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    DocumentBuilder parser = dbf.newDocumentBuilder();
    Document dom = parser.parse(new InputSource(in));

    // Get first element node
    Node node = dom.getFirstChild();
    while( node != null && node.getNodeType() != Node.ELEMENT_NODE)
      node = node.getNextSibling();

    if (node == null)
      return null;
    parse(node);
    return node.getNodeName();
  }
}
