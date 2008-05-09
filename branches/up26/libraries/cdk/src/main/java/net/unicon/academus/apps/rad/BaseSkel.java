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

import java.util.Hashtable;

/**
 * The class is RAD implementation of local EJB.
 */
public class BaseSkel implements javax.ejb.EJBHome, javax.ejb.EJBObject {
  // Properties
  public Hashtable m_props = null;

  //
  // Home
  //
  public javax.ejb.HomeHandle getHomeHandle() throws java.rmi.RemoteException {
    throw new java.rmi.RemoteException("Not implemented!");
  }

  public void remove
    (javax.ejb.Handle handle) throws java.rmi.RemoteException, javax.ejb.RemoveException {
      throw new java.rmi.RemoteException("Not implemented!");
    }

  public void remove
    (java.lang.Object primaryKey) throws java.rmi.RemoteException, javax.ejb.RemoveException {
      throw new java.rmi.RemoteException("Not implemented!");
    }

  public javax.ejb.EJBMetaData getEJBMetaData() throws java.rmi.RemoteException {
    throw new java.rmi.RemoteException("Not implemented!");
  }

  //
  // Remote
  //
  public javax.ejb.EJBHome getEJBHome() {
    return this;
  }

  public javax.ejb.Handle getHandle() {
    return null;
  }

  public java.lang.Object getPrimaryKey() {
    return null;
  }

  public boolean isIdentical(javax.ejb.EJBObject obj) {
    return false;
  }

  public void remove
  () {}

  //-----------------------------------------------------------------------//

  protected void populateProperty( StdBo bo) {
    if( m_props != null)
      bo.m_props = (Hashtable)m_props.clone();
  }
}
