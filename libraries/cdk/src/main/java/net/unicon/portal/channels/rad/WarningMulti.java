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
 * The warning screen with the multiline support.
 */
public class WarningMulti extends Screen {

  static public final String SID = "WarningMulti";

  String m_back = null;
  boolean m_refresh = false;
  String[] m_texts = null;

  /**
   * Get Screen Identifier
   * @return screen identifier
   */
  public String sid() {
    return SID;
  }

  /**
   * The version of this screen. It is the RAD version.
   * @return the RAD's version
   */
  public String getVersion() {
    return Channel.VERSION;
  }

  /**
   * Called when user clicks ok button on this screen.
   * @param params the parameters when user clicks ok
   * @return the next screen after this screen.
   * @throws Exception
   */
  public Screen ok(Hashtable params) throws Exception {
    Screen scr = getScreen(m_back);
    if( m_refresh)
      scr.reinit(params);
    return scr;
  }

  /**
   * Generate XML string for this screen
   * @return xml string
   * @throws Exception
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\"?>");
    xml.append("<warning-multi>");
    if( m_texts != null)
      for( int i = 0; i < m_texts.length; i++)
        xml.append("  <text>"+XML.esc(m_texts[i])+"</text>");
    xml.append("</warning-multi>");
    return xml.toString();
  }
}
