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
 * The screen display a text message.
 */
public class Info extends Screen {
  static public final String INFO = "Information";
  static public final String ERROR = "Error";
  static public final String EXCEPTION = "Exception";

  static public final String SID = "INFO";

  /**
   * get screen identifier
   * @return screen id
   */
  public String sid() {
    return SID;
  }

  /**
   * Override the base method. Info screen can render together with other channels
   * @return true
   */
  public boolean canRenderAsPeephole() {
    return true;
  }

  public boolean isCacheValid( long oldMilisTime) {
    return false;
  }

  /**
   * The version of Info screen. It is the RAD version.
   * @return the RAD's version
   */
  public String getVersion() {
    return Channel.VERSION;
  }

  public Hashtable getXSLTParams() {
    String s = (String)getParameter("back");
    Hashtable params = new Hashtable();
    if( s != null) {
      Screen back = getScreen(s);
      if( back != null && back.lastRenderRoot())
        params.put("backRoot", "true");
    }
    return params;
  }

  /**
   * Called when user clicks ok button on Info screen.
   * @param params the parameters when user clicks ok
   * @return the next screen after Info.
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception {
    String s = (String)getParameter("back");
    if( s == null)
      return this;
    Screen back = getScreen(s);
    if( back == null)
      return this;

    String refresh = (String)getParameter("refresh");
    if( refresh != null && refresh.equals("yes")) {
      back.reinit(params);
      removeParameter("refresh");
    }

    if( m_channel.isRenderingAsPeephole() && back.canRenderAsPeephole() == false)
      return m_channel.m_lastPeephole;
    else
      return back;
  }

  /**
   * Called when Info screen is being active (second time).
   * @param params the parameters passed when enter to
   * @throws Exception
   */
public void reinit(Hashtable params) throws Exception {}

  /**
   * Generate XML string for Info screen
   * @return xml string
   * @throws Exception
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\"?>");
    xml.append("<info>");
    xml.append("  <icon>"+getParameter("icon")+"</icon>");
    xml.append("  <text>"+XML.esc((String)getParameter("text"))+"</text>");
    xml.append("</info>");
    return xml.toString();
  }
}
