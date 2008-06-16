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

import net.unicon.academus.apps.rad.XML;

import java.util.Hashtable;

/**
 * The About screen display the channel version, description and copyright.
 */
public class About extends Screen {
  static public final String SID = "ABOUT";

  String m_back = null;
  String m_version = null;
  String m_copyright = null;
  String m_desc = null;
  String m_logo = null;

  /**
   * The About screen can be "stateless", i.e, the sid can be a constant.
   * @return the id of About screen.
   */
  public String sid() {
    return SID;
  }

  /**
   * The version of About screen. It is the RAD version.
   * @return the RAD's version
   */
  public String getVersion() {
    return Channel.VERSION;
  }

  /**
   * Override the base method. About screen can render together with other channels
   * @return true
   */
  public boolean canRenderAsPeephole() // Tien 0405
  {
    return true;
  }

  /**
   * Called when first entering to About screen
   * @param params the parameters passed when enter to
   * @throws Exception if invalid input parameters.
   */
  public void init(Hashtable params) throws Exception {
    super.init( params);
    m_back = (String)params.get("back");
    m_version = (String)params.get("version");
    m_copyright = (String)params.get("copyright");
    m_desc = (String)params.get("desc");
    m_logo = (String)params.get("logo");
  }

  /**
   * Called when About screen is being active (second time).
   * @param params the parameters passed when enter to
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception {}

  /**
   * Called when user clicks ok button on About screen.
   * @param params the parameters when user clicks ok
   * @return the next screen after About.
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception {
    Screen back = getScreen(m_back);
    String refresh = (String)getParameter("refresh");
    if( refresh != null && refresh.equals("yes"))
      back.reinit(params);

    if( m_channel.isRenderingAsPeephole() && back.canRenderAsPeephole() == false)
      return m_channel.m_lastPeephole;
    else
      return back;
  }

  /**
   * Generate XML string for About screen
   * @return xml string
   * @throws Exception
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\"?>");
    xml.append("<about>");
    xml.append("  <logo>"+m_logo+"</logo>");
    xml.append("  <version>"+m_version+"</version>");
    xml.append("  <copyright>"+m_copyright+"</copyright>");
    if( m_desc != null && m_desc.trim().length() > 0)
      xml.append("  <product-desc>"+m_desc+"</product-desc>");
    xml.append("  <back>"+m_back+"</back>");
    xml.append("</about>");
    return xml.toString();
  }

  /**
   * Response to cache mechanism.
   * @param oldMilisTime
   * @return always false.
   */
  public boolean isCacheValid( long oldMilisTime) {
    return false;
  }
}

