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

import java.util.*;
import java.text.*;

import net.unicon.academus.apps.addressbook.ContactData;
import net.unicon.portal.channels.rad.Screen;
import net.unicon.portal.channels.rad.Channel;

/**
 *  Eg: use link in xsl file for confirmation such as <a href="
 *  {$goURL}=net.unicon.portal.channels.rad.Confirm
 *  &amp;methodName=delete&amp;text=Normal Text &lt;b&gt;Bold Text&lt;/b&gt;
 *  &amp;calid={../@calid}"> <img src="{$baseImagePath}/rad/trash.gif"
 *  border="0" title="Delete"/></a> <a href='{$goURL}=net.unicon.portal.channels.rad.Confirm&amp;methodName=delete&amp;
 *  text=2&amp;distribution-id={@distribution-id}&amp;survey-id={../@survey-id}'>
 *  <img src="{$baseImagePath}/rad/trash.gif" title="Delete Distribution"
 *  border='0'/> </a> Notes: in your Channel.ssl must add (absolete)
 *  <?xml-stylesheet title="Confirm" href="/net/unicon/portal/channels/rad/confirm.xsl"
 *  type="text/xsl" media="explorer"?>
 *
 *@author     nvvu
 *@created    April 11, 2003
 */
/**
 *  The screen allows user to confirm something.
 *
 *@author     nvvu
 *@created    April 11, 2003
 */
public class Confirm extends AddressBookScreen {

  /**
   *  Description of the Field
   */
  String m_methodSID = null;
  String m_methodName = null;
  String m_back = null;
  String m_text = null;
  Hashtable m_okParams = null;
  Hashtable contact_duplicate_map = null;
  Hashtable contact_map = null;

  // Tien 0521
  ContactData[] m_contacts = null;

  /**
   *  The Confirm screen can be "stateless", i.e, the sid can be a constant.
   *
   *@return    the id of Confirm screen.
   */
  public String sid() {
    return "Confirm";
  }

  /**
   *  Gets the xSLTParams attribute of the Confirm object
   *
   *@return    The xSLTParams value
   */
  public Hashtable getXSLTParams() {
    String s = m_back;
    Hashtable params = new Hashtable();
    if (s != null) {
      Screen back = getScreen(s);
      if (back != null && back.lastRenderRoot()) {
        params.put("backRoot", "true");
      }
    }
    return params;
  }

  /**
   *  Called when Confirm screen is being active (second time).
   *
   *@param  params      the parameters passed when enter to
   *@throws  Exception
   */
  public void reinit(Hashtable params) throws Exception {}

  /**
   *  Override the base method. Confirm screen can render together with other
   *  channels
   *
   *@return    true
   */
  public boolean canRenderAsPeephole() {
    return true;
  }

  /**
   *  Called when first entering to Confirm screen
   *
   *@param  params      the parameters passed when enter to. They must at
   *      least contain the keys: methodSID - together with the methodName key
   *      specified what method to be called when finish Confirm screen.
   *      methodName - see above. back - The sid of back screen. text - the
   *      confirm text (or its id in the local.xml) to be displayed in the
   *      Confirm screen. mfpXX - The parameters of the confirm text All other
   *      pairs key/values will pass to the method (methodSID, methodName)
   *      after user selects the confirm action.
   *@throws  Exception  if invalid input parameters.
   */
  public void init(Hashtable params) throws Exception {
    m_contacts = (ContactData[])params.get("contacts");
    m_back = (String) params.get("back");
    m_methodName = (String) params.remove("methodName");
  }

  /**
   *  Called when user click cancel button on Confirm screen.
   *
   *@param  params      the parameters when user clicks.
   *@return             the next screen after Confirm.
   *@throws  Exception
   */
  public Screen cancel(Hashtable params) throws Exception {
    return getScreen(m_back);
  }

  public Screen replace(Hashtable params) throws Exception {
    params.put("contacts", m_contacts);
    params.put("option","replace");
    Screen back = getScreen(m_back);
    return back.invoke(m_methodName, params);
  }

  public Screen add
    (Hashtable params) throws Exception {
      params.put("contacts", m_contacts);
      params.put("option","add");
      Screen back = getScreen(m_back);
      return back.invoke(m_methodName, params);
    }

  public Screen skip(Hashtable params) throws Exception {
    params.put("contacts", m_contacts);
    params.put("option","skip");
    Screen back = getScreen(m_back);
    return back.invoke(m_methodName, params);
  }

  /**
   *  Generate XML string for Confirm screen
   *
   *@return             xml string
   *@throws  Exception
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    xml.append("<confirm>");
    printXML(xml);
    xml.append("</confirm>");
    return xml.toString();
  }

  /**
   *  Description of the Method
   *
   *@param  xml                      Description of the Parameter
   *@exception  java.lang.Exception  Description of the Exception
   */
  public void printXML(StringBuffer xml) throws java.lang.Exception {}
}

