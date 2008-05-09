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
package net.unicon.portal.util;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;

public final class ChannelCacheTable extends WeakHashMap
implements HttpSessionBindingListener {

  public ChannelCacheTable() {}

  /**
   * This notifies ChannelCacheTable that it has been unbound from the session.
   *
   * @param bindingEvent an <code>HttpSessionBindingEvent</code> value
   */
  public void valueUnbound(HttpSessionBindingEvent bindingEvent) {
    clear();
  }

  /**
   * Notifies ChannelCacheTable that it has been bound to a session.
   *
   * @param bindingEvent a <code>HttpSessionBindingEvent</code> value
   */
  public void valueBound (HttpSessionBindingEvent bindingEvent) {}
}
