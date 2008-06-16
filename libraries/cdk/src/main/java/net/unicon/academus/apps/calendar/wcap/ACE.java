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

package net.unicon.academus.apps.calendar.wcap;

import java.util.HashSet;
import java.util.Iterator;

import net.unicon.academus.apps.calendar.*;

/**
 * This class represents Access Control Entries (ACE string) that determines
 * access control for calendars.</br>
 * Here is an example of an ACE string:</br>
 * <b>jdoe^c^wd^g</b></br>
 * The string has four elements separated by three "^" characters. The four elements
 * are:</p>
 * 1. The first elements of ACE tell who applies to.</br>
 *    This could be an individual user, a domain, or a class-type of user. There
 *    are four types of classes for users:
 *    <ul>
 *     <li>All users, represented by the string "@".</li>
 *     <li>Primary owners of a calendar represented by the string "@@p".</li>
 *     <li>Owners of a calendar represented by the string "@@o".</li>
 *     <li>Non-owners of a calendar represented by the string "@@n".</li>
 *    </ul>
 *
 * 2. The second element of an ACE indicates what the ACE applies to.
 *    The ACE can be applied to:
 *    <ul>
 *     <li>The entire calendar. To indicate the entire calendar, pass in the value a.</li>
 *     <li>The components of the calendar only. To indicate just the components,
 *       pass in the value c.</li>
 *     <li>The calendar properties of the calendar only. To indicate calendar properties
 *       only, pass in the value p.</li>
 *    </ul>
 * 3. The third element of an ACE indicates what access values the ACE applies to.
 *    Multiple values may be specified at the same time. This element contains a
 *    string with one or more of the Access Control characters.<p/>
 *    <table border="1" width="100%" cellspacing="0" cellpadding="0">
 *     <tr><th width="10%" nowrap> <p align="left">Acesss Control<br/>Characters</th><th>Description</th></tr>
 *     <tr><td width="10%">c</td><td>Grants the user act-on-behalf-of cancel access.
 *     </td></tr>
 *     <tr><td width="10%">d</td><td>Grants the user delete access.
 *     </td></tr>
 *     <tr><td width="10%">e</td><td>Grants the user act-on-behalf-of reply access.
 *     </td></tr>
 *     <tr><td width="10%">f</td><td>Grants the user free-bysy access.
 *     </td></tr>
 *     <tr><td width="10%">i</td><td>Grants the user act-on-behalf-of invite access.
 *     </td></tr>
 *     <tr><td width="10%">r</td><td>Grants the user act-on-behalf-of read access.
 *     </td></tr>
 *     <tr><td width="10%">s</td><td>Grants the user act-on-behalf-of schedule access.
 *     </td></tr>
 *     <tr><td width="10%">w</td><td>Grants the user act-on-behalf-of write access.
 *     </td></tr>
 *    </table>
 * <p/>
 * 4. The fourth element of an ACE indicated whether to grant or deny access.
 *    <ul>
 *     <li>To grant access, set the value to g.</li>
 *     <li>To deny access, set the value to d.</li>
 *    </ul>
 */
public class ACE {

  /**
   * Username of ACE.
   */
  public String m_user = null;
  HashSet m_ag = new HashSet();
  HashSet m_cg = new HashSet();
  HashSet m_pg = new HashSet();
  HashSet m_ad = new HashSet();
  HashSet m_cd = new HashSet();
  HashSet m_pd = new HashSet();

  static final Character FREEBUSY = new Character('f');
  static final Character SCHEDULE = new Character('s');
  static final Character READ = new Character('r');
  static final Character WRITE = new Character('w');
  static final Character DELETE = new Character('d');
  static final Character REPLY = new Character('e');
  static final Character CANCEL = new Character('c');
  static final Character INVITE = new Character('i');

  void setAccess(HashSet hsg, HashSet hsd, String how, boolean grant) {
    if (hsg == null || hsd == null)
      return;

    for (int i = 0; i < how.length(); i++) {
      Character c = new Character(how.charAt(i));
      if (grant) {
        hsg.add(c);
        hsd.remove(c);
      } else {
        hsg.remove(c);
        hsd.add(c);
      }
    }
  }

  /**
   * Set raw string of ACE.
   * @param tokens
   */
  public void setRawACE(String[] tokens) {
    // User
    m_user = tokens[0];

    // Access flags
    String w = tokens[1];
    String h = tokens[2];
    boolean g = tokens[3].equalsIgnoreCase("g");
    if (w.equals("a"))
      setAccess(m_ag, m_ad, h, g);
    else if (w.equals("c"))
      setAccess(m_cg, m_cd, h, g);
    else if (w.equals("p"))
      setAccess(m_pg, m_pd, h, g);
  }

  String applyPattern(HashSet hs, String p) {
    String str = "";
    Iterator it = hs.iterator();
    while(it.hasNext()) {
      char c = ((Character)it.next()).charValue();
      if (p.indexOf(c) >= 0) // valid to output
        str += c;
    }
    return str;
  }

  /**
   * Apply an ACE from elements.
   * @param pA a string indicates what the ACE applies.
   * @param pC a string indicates what access values the ACE applies.
   * @param pP a string indicates who the ACE applies.
   * @return
   */
  public String ace(String  pA, String pC, String pP) {
    //if (m_user.equals(CUID_SHARE))
    if (WCAP.getCuidType(m_user) == CalendarServer.CUID_SHARE)
      return "@@o^c^wdeic^g;@@o^a^rfs^g;";

    String ag = applyPattern(m_ag, pA);
    String ad = applyPattern(m_ad, pA);
    String cg = applyPattern(m_cg, pC);
    String cd = applyPattern(m_cd, pC);
    String pg = applyPattern(m_pg, pP);
    String pd = applyPattern(m_pd, pP);

    // Grant
    String grant = "";
    if (ag.length() > 0)
      grant += m_user + "^a^" + ag + "^g;";
    if (cg.length() > 0)
      grant += m_user + "^c^" + cg + "^g;";
    if (pg.length() > 0)
      grant += m_user + "^p^" + pg + "^g;";

    // Deny
    String deny = "";
    //if (m_user.equals(CUID_ALL) == false)
    if (WCAP.getCuidType(m_user) != CalendarServer.CUID_ALL) {
      if (ad.length() > 0)
        deny += m_user + "^a^" + ad + "^d;";
      if (cd.length() > 0)
        deny += m_user + "^c^" + cd + "^d;";
      if (pd.length() > 0)
        deny += m_user + "^p^" + pd + "^d;";
    }

    return (grant + deny);
  }

  /**
   * Check whether the ACE is "freebusy" or not.
   * @return
   */
  public boolean getFreebusy() {
    return m_ag.contains(FREEBUSY) && !m_ad.contains(FREEBUSY);
  }

  /**
   * Set "freebusy" for the ACE.
   * @param f
   */
  public void setFreebusy(boolean f) {
    if (f) {
      m_ag.add(FREEBUSY);
      m_ad.remove(FREEBUSY);
    } else {
      m_ad.add(FREEBUSY);
      m_ag.remove(FREEBUSY);
    }
  }

  /**
   * Check whether the ACE is schedule or not.
   * @return
   */
  public boolean getSchedule() {
    return m_ag.contains(SCHEDULE) && !m_ad.contains(SCHEDULE);
  }

  /**
   * Set "schedule" for the ACE.
   * @param s
   */
  public void setSchedule(boolean s) {
    if (s) {
      m_ag.add(SCHEDULE);
      m_ad.remove(SCHEDULE);
    } else {
      m_ad.add(SCHEDULE);
      m_ag.remove(SCHEDULE);
    }
  }

  /**
   * Check "read" of the ACE.
   * @return
   */
  public boolean getRead() {
    return m_ag.contains(FREEBUSY) && m_ag.contains(SCHEDULE) && m_ag.contains(READ) &&
           !m_ad.contains(FREEBUSY) && !m_ad.contains(SCHEDULE) && !m_ad.contains(READ);
  }

  /**
   * Check "write" of the ACE.
   * @return
   */
  public boolean getWrite() {
    return getRead() && m_cg.contains(DELETE) && m_cg.contains(WRITE) && m_cg.contains(INVITE) &&
           m_cg.contains(CANCEL) && m_cg.contains(REPLY);// &&

  }

  /**
   * Set "read" for the ACE.
   * @param r
   */
  public void setRead(boolean r) {
    if (r) {
      m_ag.add(FREEBUSY);
      m_ag.add(SCHEDULE);
      m_ag.add(READ);
      m_ad.remove(FREEBUSY);
      m_ad.remove(SCHEDULE);
      m_ad.remove(READ);
    } else {
      m_ag.remove(READ);
      m_ad.add(READ);
    }
  }

  /**
   * Set "write" for the ACE.
   * @param w
   */
  public void setWrite(boolean w) {
    if (w) {
      setRead(w);
      m_cg.add(DELETE);
      m_cg.add(WRITE);
      m_cg.add(INVITE);
      m_cg.add(CANCEL);
      m_cg.add(REPLY);
    } else {
      m_cg.remove(DELETE);
      m_cg.remove(WRITE);
      m_cg.remove(INVITE);
      m_cg.remove(CANCEL);
      m_cg.remove(REPLY);
      m_cd.add(DELETE);
      m_cd.add(WRITE); //m_cd.add(INVITE);
      //m_cd.add(CANCEL); m_cd.add(REPLY);
    }
  }

  /**
   * Create a access control list from array of ACEs
   * @param aces
   * @return
   */
  static public String acl(ACEData[] aces) {
    String acl = "";
    boolean everyone = false;
    if (aces != null) {
      for (int i=0; i < aces.length; i++) {
        //if (aces[i].getCuid().equals(ACE.CUID_ALL))
        if (WCAP.getCuidType(aces[i].getCuid()) == CalendarServer.CUID_ALL)
          everyone = true;
        acl += iCSFormat(aces[i]);
      }
    }

    // Add everyone
    if (everyone == false)
      acl = "@^a^fs^g;" + acl;

    return ACE.normalize(acl);
  }


  static String iCSFormat(ACEData ace) {
    ACE wcap = new ACE();
    wcap.m_user = ace.getCuid();
    Boolean b = ace.getFreebusy();
    if (b != null)
      wcap.setFreebusy(b.booleanValue());
    b = ace.getSchedule();
    if (b != null)
      wcap.setSchedule(b.booleanValue());
    b = ace.getRead();
    if (b != null)
      wcap.setRead(b.booleanValue());
    b = ace.getWrite();
    if (b != null)
      wcap.setWrite(b.booleanValue());

    return wcap.ace("rfs","wdeic","rwfs");
  }

  static String normalize(String acl) {
    int idx = acl.lastIndexOf(';');
    if (idx >= 0)
      acl = acl.substring(0,idx);
    return acl;
  }
}
