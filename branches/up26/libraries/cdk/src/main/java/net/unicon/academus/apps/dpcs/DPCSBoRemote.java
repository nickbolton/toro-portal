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

package net.unicon.academus.apps.dpcs;

public interface DPCSBoRemote extends javax.ejb.EJBObject, java.io.Serializable {
  public java.lang.Object getProperty(java.lang.String arg0) throws java.rmi.RemoteException;
  public void putProperty(java.lang.String arg0, java.lang.Object arg1) throws java.rmi.RemoteException;
  public void ejbActivate() throws java.rmi.RemoteException;
  public void ejbPassivate() throws java.rmi.RemoteException;
  public void setSessionContext(javax.ejb.SessionContext arg0) throws java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData getEntry(java.lang.String arg0, java.lang.String arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public java.lang.String[] login(net.unicon.academus.apps.rad.IdentityData arg0, java.lang.String[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public void logout() throws java.lang.Exception, java.rmi.RemoteException;
  public void changePassword(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData createCalendar(net.unicon.academus.apps.calendar.CalendarData arg0) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData updateCalendar(net.unicon.academus.apps.calendar.CalendarData arg0) throws java.lang.Exception, java.rmi.RemoteException;
  public void deleteCalendar(java.lang.String arg0) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData[] getCalendars(java.lang.String[] arg0) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData[] fetchEntries(net.unicon.academus.apps.calendar.CalendarData[] arg0, java.util.Date arg1, java.util.Date arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData[] fetchEvents(net.unicon.academus.apps.calendar.CalendarData[] arg0, java.util.Date arg1, java.util.Date arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData[] fetchTodos(net.unicon.academus.apps.calendar.CalendarData[] arg0, java.util.Date arg1, java.util.Date arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData[] fetchEntriesByIds(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData[] fetchEventsByIds(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData[] fetchTodosByIds(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.CalendarData[] searchEvent(net.unicon.academus.apps.calendar.CalendarData[] arg0, java.lang.String arg1, java.lang.String arg2, int arg3, java.util.Date arg4, java.util.Date arg5) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData[] fetchInvitations() throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData[] fetchInvitations(java.util.Date arg0, java.util.Date arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public void replyInvitation(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange arg1, java.lang.String arg2, java.lang.String arg3) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData createEntry(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryData arg1, int arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public net.unicon.academus.apps.calendar.EntryData updateEntry(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange arg1, net.unicon.academus.apps.calendar.EntryData arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public void completeTodo(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange arg1, net.unicon.academus.apps.calendar.CompletionData arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public void deleteEvents(java.lang.String[] arg0, java.util.Date arg1, java.util.Date arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public void deleteEvents(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public void deleteTodos(java.lang.String[] arg0, java.util.Date arg1, java.util.Date arg2) throws java.lang.Exception, java.rmi.RemoteException;
  public void deleteTodos(java.lang.String arg0, net.unicon.academus.apps.calendar.EntryRange[] arg1) throws java.lang.Exception, java.rmi.RemoteException;
  public void setCalr(net.unicon.academus.apps.calendar.CalendarData[] arg0) throws java.lang.Exception, java.rmi.RemoteException;
  public java.lang.String importEntries(java.io.InputStream is, java.lang.String format, java.lang.String calid, java.util.Hashtable map) throws java.lang.Exception, java.rmi.RemoteException;
  public java.io.InputStream exportEntries(java.lang.String format, java.lang.String[] calids, java.util.Date from, java.util.Date to) throws java.lang.Exception, java.rmi.RemoteException;

}
