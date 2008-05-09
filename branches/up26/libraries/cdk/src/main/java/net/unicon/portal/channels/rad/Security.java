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

import java.util.Iterator;
import java.util.Vector;
import java.util.HashSet;

import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;

import net.unicon.academus.apps.rad.IdentityData;

/**
 * The wrapper of uPortal permissions and is used for the channel.
 * The owner normally is the channel package name.
 */
public class Security {
  String m_owner = null;
  IAuthorizationPrincipal m_principal = null;
  AuthorizationService m_service = null;
  IUpdatingPermissionManager m_upm = null;
  IPermissionManager m_pm = null;

  /**
   * Constructor with the owner
   * @param owner the owner of security
   * @throws Exception if there is an error when use the uPortal authentication service.
   */
  public Security( String owner) throws Exception {
    m_owner = owner;
    m_service = AuthorizationService.instance();
    m_upm = m_service.newUpdatingPermissionManager(m_owner);
    m_pm = m_service.newPermissionManager(m_owner);
  }

  /**
   * Get principal for authentication.
   * @param principal The user to get principal
   * @return IAuthorizationPrincipal
   * @throws Exception from uPortal authentication service.
   */
  public IAuthorizationPrincipal principal(IdentityData principal) throws Exception {
    Class type = principal.getType().equals(IdentityData.GROUP)? GroupData.C_GROUP:
                 GroupData.lookupClass(principal.getEntityType());
    String key = GroupData.getPortalKey( principal, type);
    if( m_principal == null ||
        (!m_principal.getKey().equals(key) || !m_principal.getType().equals(type))) {
      m_principal = m_service.newPrincipal(key, type);
    }
    return m_principal;
  }

  /**
   * Check permission for given principal with the given activity on given target.
   * @param principal The principal to check
   * @param activity The activity of permission
   * @param target on which given principal has the permission.
   * @return true if given principal has the permission, else - false
   * @throws Exception from uPortal authentication service.
   */
  public boolean hasPermission( IdentityData principal, String activity, String target) throws Exception {
    return principal(principal).hasPermission( m_owner,activity,target);
  }

  //------------------------------------------------------------------------//

  /**
   * Add a permission (activity, target, type) for given principal.
   * @param principal who will have the permission.
   * @param activity The activity of permission to add
   * @param target The target of permission.
   * @param type The type of permission.
   * @throws Exception comes from uPortal authentication service.
   */
  public void addPermission( IdentityData principal, String activity, String target, String type) throws Exception {
    // esc special char '
    principal = new IdentityData(principal);
    principal.putAlias(net.unicon.academus.apps.rad.SQL.quot(principal.getAlias()));
    principal.putName(net.unicon.academus.apps.rad.SQL.quot(principal.getName()));
    // Use IUpdatingPermissionManager to add permission
    IAuthorizationPrincipal iap = principal(principal);
    IPermission perm = m_upm.newPermission(null);
    perm.setPrincipal(iap.getPrincipalString());
    perm.setActivity(activity);
    perm.setTarget(target);
    perm.setType(type);
    m_upm.addPermissions( new IPermission[] {perm});
  }

  /**
   * Remove a permission
   * @param principal from which the permission will remove
   * @param activity The activity of permissions to remove
   * @param target The target of permissions.
   * @throws Exception comes from uPortal authentication service.
   */
  public void removePermission( IdentityData principal, String activity, String target) throws Exception {
    //esc special char '
    principal = new IdentityData(principal);
    principal.putAlias(net.unicon.academus.apps.rad.SQL.quot(principal.getAlias()));
    principal.putName(net.unicon.academus.apps.rad.SQL.quot(principal.getName()));
    // Use IUpdatingPermissionManager to remove permission
    IPermission perm = m_upm.newPermission(null);
    perm.setPrincipal(principal(principal).getPrincipalString());
    perm.setActivity(activity);
    perm.setTarget(net.unicon.academus.apps.rad.SQL.quot(target));
    m_upm.removePermissions( new IPermission[] {perm});
  }

  /**
   * Remove all permissions for given activity on given target.
   * @param activity The activity of permissions to remove
   * @param target The target of permissions.
   * @throws Exception comes from uPortal authentication service.
   */
  public void removePermissions( String activity, String target) throws Exception {
    //esc '
    //target = net.unicon.academus.apps.rad.SQL.quot(target);
    //
    IPermission[] perms = m_pm.getPermissions(activity,target);
    if (perms != null)
      for (int i = 0; i < perms.length; i++) {
        perms[i].setPrincipal(net.unicon.academus.apps.rad.SQL.quot(perms[i].getPrincipal()));
        perms[i].setTarget(net.unicon.academus.apps.rad.SQL.quot(perms[i].getTarget()));
      }
    m_upm.removePermissions( perms);
  }

  //-------------------------------------------------------------------------//

  /**
   * Get all the targets of given principal with the given activity.
   * @param principal is portal principal
   * @param activity The activity for which all targets will be collected.
   * @throws Exception comes from uPortal authentication service.
   */
  public String[] getPermissionTargets( IdentityData principal, String activity) throws Exception {
    // esc special char ' apostrophe
    principal = new IdentityData(principal);
    principal.putAlias(net.unicon.academus.apps.rad.SQL.quot(principal.getAlias()));
    principal.putName(net.unicon.academus.apps.rad.SQL.quot(principal.getName()));
    // Main pricipal
    IAuthorizationPrincipal prin = principal(principal);
    IPermission[] perms = prin.getAllPermissions(m_owner,activity,null);

    HashSet s = new HashSet();
    if( perms != null && perms.length > 0)
      for( int i = 0; i < perms.length; i++)
        s.add(perms[i].getTarget());

    // Campus principal
    IdentityData campus = principal.getRef("campus");
    if( campus != null) {
      perms = principal(campus).getAllPermissions(m_owner,activity,null);
      if( perms != null && perms.length > 0)
        for( int i = 0; i < perms.length; i++)
          s.add(perms[i].getTarget());
    }

    // Contact principal
    IdentityData contact = principal.getRef("contact");
    if( contact != null) {
      perms = principal(contact).getAllPermissions(m_owner,activity,null);
      if( perms != null && perms.length > 0)
        for( int i = 0; i < perms.length; i++)
          s.add(perms[i].getTarget());
    }

    // Combine all
    int i = 0;
    Iterator it = s.iterator();
    String[] targets = new String[s.size()];
    while( it.hasNext() && i < targets.length)
      targets[i++] = (String)it.next();

    // Restore principal
    m_principal = prin;
    return targets;
  }

  /**
   * Get all the principals that has the permissions with the given (activity, target).
   * @param activity The activity of permissions to get.
   * @param target The target of permissions.
   * @param detail indicating the return IdentityData objects will be filled with the detail informations.
   * @return The array of IdentityData objects that has given permission.
   * @throws Exception comes from uPortal authentication service.
   */
  public IdentityData[] getPermissionPrincipals( String activity, String target, boolean detail) throws Exception {
    IAuthorizationPrincipal[] aps = m_pm.getAuthorizedPrincipals(activity, target);
    if( aps == null || aps.length == 0)
      return null;

    // Convert to IdentityData
    IdentityData[] sels = new IdentityData[aps.length];
    for( int i = 0; i < aps.length; i++)
      sels[i] = GroupData.createIdentityData( aps[i].getKey(), aps[i].getType());

    if (detail)
      Finder.findDetails( sels, null);

    return sels;
  }

  /**
   * Make the call to:
   * public IdentityData[] getPermissionPrincipals( activity, target, null);
   */
  public IdentityData[] getPermissionPrincipals( String activity, String target) throws Exception {
    return getPermissionPrincipals( activity,target, true);
  }
}
