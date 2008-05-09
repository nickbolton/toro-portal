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

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.Sorted;
import net.unicon.academus.common.PersonDirectoryService;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.domain.lms.MemberSearchCriteria;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserComparator;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupServiceFactory;

import org.jasig.portal.services.GroupService;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;


public class IdentityDataSearcher {

  private static PersonDirectoryService personDirService = null;

  static {
    try {
      personDirService = PersonDirectoryServiceFactory.getService();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Vector personSearch(String name, String email, int countLimit)
  throws Exception {
    boolean openSearch =
      "".equals(name.trim()) &&
      "".equals(email.trim());

    Vector results = personSearch(name, "", "", email, countLimit, openSearch);

    if (results.size() > countLimit) {
      results.setSize(countLimit);
    }

    return results;
  }

  public static Vector portalSearch(String name, String title,
    String department, String email, int countLimit)
  throws Exception {

    boolean openSearch =
      "".equals(name.trim()) &&
      "".equals(title.trim()) &&
      "".equals(department.trim()) &&
      "".equals(email.trim());

    Vector results = groupSearch(name, openSearch);

    results.addAll(personSearch(name, title, department, email, countLimit, openSearch));

    if (results.size() > countLimit) {
      results.setSize(countLimit);
    }
    return results;
  }

  private static Sorted groupSearch(String name, boolean openSearch)
  throws Exception {

    Sorted results = new Sorted(new IdentityComparator());

    // open need to search if it's an open search or
    // there is a group name to search for
    if (!openSearch && "".equals(name.trim())) return results;

    String lowerCaseName = name.toLowerCase();

    IGroup[] groups = UniconGroupServiceFactory.getService().
      fetchAllGroupsArray();

    for (int i=0; i<groups.length; i++) {
      if (openSearch || groups[i].getName().toLowerCase().indexOf(lowerCaseName) >= 0) {
        results.add(new IdentityData(GroupData.GROUP, GroupData.S_USER,
        groups[i].getKey(), groups[i].getName(), groups[i].getName()));
      }
    }

    return results;
  }


  // person search using the domain user
  private static Sorted personSearch(String name, String title,
    String department, String email, int countLimit, boolean openSearch)
  throws Exception {

    MemberSearchCriteria criteria = new MemberSearchCriteria();

    // match all if it's an open search
    criteria.matchAllCriteria(openSearch);
    criteria.setFirstName(name);
    criteria.setLastName(name);
    criteria.setEmail(email);

    Sorted results = new Sorted(new IdentityComparator());
    User[] users = (User[])UserFactory.findUsers(criteria).toArray(new User[0]);

    // filter out non-portal users
    results.addAll(convertToIdentityData(
      UserFactory.validatePortalUsers(users)));

    return results;
  }

  // converts domain User objects to IdentityData
  private static List convertToIdentityData(List users) {
    List ret = new ArrayList();

    if (users == null) return ret;

    User user;
    IdentityData id;
    Iterator itr = users.iterator();
    while (itr.hasNext()) {
      user = (User)itr.next();
      id = new IdentityData(GroupData.ENTITY, GroupData.S_USER,
        ""+user.getId(), user.getUsername(),
        user.getFirstName() + " " + user.getLastName());
      id.putEmail(user.getEmail());
      ret.add(id);
    }
    return ret;
  }
}
