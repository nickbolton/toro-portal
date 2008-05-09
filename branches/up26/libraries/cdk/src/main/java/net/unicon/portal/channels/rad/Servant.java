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
import java.io.*;

import net.unicon.portal.servants.ServantEvent;

import org.jasig.portal.IServant;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.utils.*;
import org.xml.sax.*;

/**
 * The uPortal servant for RAD screen
 */
public class Servant extends Channel implements IServant {
  String m_screen = null;

  public void start(String screen) {
    m_screen = screen;
  }

  public void finish(Object[] results) {
    m_results = results;
    m_screen = null;
  }

  public String getMain() {
    return m_screen;
  }

  public boolean isRenderingAsPeephole() {
    boolean ret = super.isRenderingAsPeephole();
    if (ret && m_lastScreen != null && !m_lastScreen.canRenderAsPeephole())
      finish(null);
    return ret;
  }

  //- IServant ---------------------------------------------------------------//

  Object[] m_results = null;

  public boolean isFinished() {
    return m_screen == null;
  }

  public Object[] getResults() {
    return m_results;
  }

  public void receiveEvent(PortalEvent ev) {
    super.receiveEvent(ev);
    switch( ev.getEventNumber()) {
    case ServantEvent.RENDERING_DONE:
      m_screens = null;
      m_lastScreen = null;
      m_lastPeephole = null;
      m_subscribeId = null;
      m_publishId = null;
      m_peParams = null;
    break;
    default:
    break;
    }
  }

}
