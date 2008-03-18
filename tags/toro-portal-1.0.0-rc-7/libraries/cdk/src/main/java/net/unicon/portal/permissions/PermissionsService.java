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
package net.unicon.portal.permissions;

import net.unicon.academus.domain.DomainException;
import net.unicon.portal.util.ChannelDefinitionUtil;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;
import net.unicon.sdk.FactoryCreateException;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.provider.AuthorizationServiceFactoryImpl;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This singleton provides <code>IPermissions</code> servicing from the uPortal
 * <code>AuthorizationService</code>
 */

public final class PermissionsService {

    private static PermissionsService _instance = null;

    private static IAuthorizationService authorizationService;

    static {
        try {
            authorizationService =
                new AuthorizationServiceFactoryImpl().getAuthorization();
        } catch (AuthorizationException ae) {
            LogService.log(LogService.ERROR, ae);
        }
    }

    /**
     * This provides access to the singleton object.
     * @return The singleton PermissionsService object.
     */
    public static synchronized PermissionsService instance() {
        if (_instance == null) {
            _instance = new PermissionsService();
        }
        return _instance;
    }

    /**
     * Retrieves an array of <code>IPermissions</code> objects that pertain
     * to the given principal group.
     * @param group The principal <code>IGroup</code> to which the permissions
     * belong.
     * @return an array of <code>IPermissions</code>  objects that belong
     * to the given principal group.
     * @throws PermissionsException if there is no uPortal group that
     * is associated with the given principal group or if the permissions
     * cannot be retrieved from the underlying uPortal AuthorizationService.
     */
    public IPermissions[] getPermissions(IGroup principal)
    throws PermissionsException {
        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                    newPrincipal(principal.getEntityGroup());
            return __getPermissions(authPrincipal, null, principal);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions getPermissions(IAuthorizationPrincipal authPrincipal,
        ChannelDefinition target)
    throws PermissionsException {
        try {
            Activity[] activities = __getActivities(target);
            return PermissionsFactory.getPermissions(authPrincipal,
                target, activities);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions getPermissions(String target,
        IAuthorizationPrincipal principal)
    throws PermissionsException {
        try {
            return PermissionsFactory.getPermissions(principal, target, null);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    private Activity[] __getActivities(ChannelDefinition cd)
    throws Exception {
        String permissionsFilePath;
        String producerName = ChannelDefinitionUtil.
            getParameter(cd,"producer");
        String permissions = ChannelDefinitionUtil.
            getParameter(cd,"permissions");

        if (permissions == null || "".equals(permissions)) {
            return null;
        }

        if (producerName != null && !"".equals(producerName)) {
            permissionsFilePath = Class.forName(producerName).
                getResource(permissions).toString();
        } else {
            permissionsFilePath = Class.forName(cd.getJavaClass()).
                getResource(permissions).toString();
        }
        return ActivityFactory.getActivities(permissionsFilePath);
    }

    private IPermissions[] getPermissions(IAuthorizationPrincipal authPrincipal,
        String owner)
    throws PermissionsException {

        try {
            List permissions = new ArrayList();
            IPermission[] uportalPermissions =
                authPrincipal.getPermissions(owner, null, null);

            // collect the channels (targets)
            String target;
            Set channels = new HashSet();

            for (int i=0; i<uportalPermissions.length; i++) {
                target = uportalPermissions[i].getTarget();
                if (!channels.contains(target)) {
                    channels.add(target);
                    permissions.add(getPermissions(authPrincipal, target));
                }
            }
            return (IPermissions[])permissions.toArray(new IPermissions[0]);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    private IPermissions[] __getPermissions(
        IAuthorizationPrincipal authPrincipal, String owner, IGroup principal)
    throws PermissionsException {

        try {
            List permissions = new ArrayList();
            IPermission[] uportalPermissions =
                authPrincipal.getPermissions(owner, null, null);

            // collect the channels (targets)
            String target;
            Set channels = new HashSet();

            for (int i=0; i<uportalPermissions.length; i++) {
                target = uportalPermissions[i].getTarget();
                if (!channels.contains(target)) {
                    channels.add(target);
                    permissions.add(getPermissions(target, principal));
                }
            }
            return (IPermissions[])permissions.toArray(new IPermissions[0]);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    private IPermissions[] __getPermissions(
        IAuthorizationPrincipal authPrincipal, String owner, IMember principal)
    throws PermissionsException  {

        try {
            List permissions = new ArrayList();
            IPermission[] uportalPermissions =
                authPrincipal.getPermissions(owner, null, null);

            // collect the channels (targets)
            String target;
            Set channels = new HashSet();

            for (int i=0; i<uportalPermissions.length; i++) {
                target = uportalPermissions[i].getTarget();
                if (!channels.contains(target)) {
                    channels.add(target);
                    permissions.add(getPermissions(target, principal));
                }
            }
            return (IPermissions[])permissions.toArray(new IPermissions[0]);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions[] getPermissions(IMember principal)
    throws PermissionsException {
        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                newPrincipal(principal.getKey(),
                org.jasig.portal.security.IPerson.class);
            return __getPermissions(authPrincipal, null, principal);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    /**
     * Retrieves an array of <code>IPermissions</code> objects that pertain
     * to the given principal group.
     * @param group The principal <code>IGroup</code> to which the permissions
     * @param owner The owner of the permissions object
     * @return an array of <code>IPermissions</code>  objects that belong
     * to the given principal group.
     * @throws PermissionsException if there is no uPortal group that
     * is associated with the given principal group or if the permissions
     * cannot be retrieved from the underlying uPortal AuthorizationService.
     */
    public IPermissions[] getPermissions(IGroup principal, String owner)
    throws PermissionsException {
        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                    newPrincipal(principal.getEntityGroup());

            return __getPermissions(authPrincipal, owner, principal);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions[] getPermissions(IMember principal, String owner)
    throws PermissionsException {
        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                newPrincipal(principal.getKey(),
                org.jasig.portal.security.IPerson.class);
            return __getPermissions(authPrincipal, owner, principal);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    /**
     * Retrieves an array of <code>IPermissions</code> objects that pertain
     * to the given principal group.
     * @param group The principal <code>IGroup</code> to which the permissions
     * @param owners The owners of the permissions object
     * belong.
     * @return an array of <code>IPermissions</code>  objects that belong
     * to the given principal group.
     * @throws PermissionsException if there is no uPortal group that
     * is associated with the given principal group or if the permissions
     * cannot be retrieved from the underlying uPortal AuthorizationService.
     */
    public IPermissions[] getPermissions(IGroup principal, String[] owners)
    throws PermissionsException {
        List retObj = new ArrayList();
        for (int i=0; i<owners.length; i++) {
            retObj.addAll(Arrays.asList(getPermissions(principal, owners[i])));
        }
        return (IPermissions[])retObj.toArray(new IPermissions[0]);
    }
   
    public IPermissions getPermissions(String target, IGroup principal)
    throws PermissionsException {
        try {
            return PermissionsFactory.getPermissions(principal, target, null);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    public IPermissions[] getPermissions(IMember principal, String[] owners)
    throws PermissionsException {
        List retObj = new ArrayList();
        for (int i=0; i<owners.length; i++) {
            retObj.addAll(Arrays.asList(getPermissions(principal, owners[i])));
        }
        return (IPermissions[])retObj.toArray(new IPermissions[0]);
    }
   
    public IPermissions getPermissions(String target, IMember principal)
    throws PermissionsException {
        try {
            return PermissionsFactory.getPermissions(principal, target, null);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    /**
     * This is a convenience wrapper for the <code>PermissionsFactory<code>
     * class getPermissions method.
     * @param target The target for the permissions.
     * @param principal The principal group for the permissions.
     * @return the permissions that belong to the given principal group and
     * target.
     * @throws PermissionsException if the <code>PermissionsFactory<code>
     * class getPermissions method fails.
     */
    public IPermissions getPermissions(ChannelClass ccTarget, IGroup principal)
    throws PermissionsException {
        try {
            Activity[] activities = (Activity[])ccTarget.getActivities().
                toArray(new Activity[0]);
            return PermissionsFactory.getPermissions(principal,
                ccTarget, activities);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    public IPermissions[] getPermissions(ChannelClass[] ccTargets,
        IGroup principal)
    throws PermissionsException {
        if (ccTargets == null) return new IPermissions[0];

        List permissionsList = new ArrayList(ccTargets.length);

        for (int i=0; i<ccTargets.length; i++) {
            permissionsList.add(getPermissions(ccTargets[i], principal));
        }
        return (IPermissions[])permissionsList.toArray(new IPermissions[0]);
    }

    public IPermissions getPermissions(ChannelClass ccTarget,
        IAuthorizationPrincipal principal)
    throws PermissionsException {
        try {
            Activity[] activities = (Activity[])ccTarget.getActivities().
                toArray(new Activity[0]);
            return PermissionsFactory.getPermissions(principal,
                ccTarget, activities);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    public IPermissions getPermissions(ChannelClass ccTarget, IMember principal)
    throws PermissionsException {
        try {
            Activity[] activities = (Activity[])ccTarget.getActivities().
                toArray(new Activity[0]);
            return PermissionsFactory.getPermissions(principal,
                ccTarget, activities);
        } catch (FactoryCreateException fce) {
            throw new PermissionsException(fce);
        }
    }

    /**
     * This is a convenience wrapper for the <code>PermissionsFactory<code>
     * class getPermissions method.
     * @param target The target for the permissions.
     * @param principal The principal group for the permissions.
     * @return the permissions that belong to the given principal group and
     * target.
     * @throws PermissionsException if the <code>PermissionsFactory<code>
     * class getPermissions method fails.
     */
    public IPermissions getPermissions(ChannelDefinition cdTarget,
        IGroup principal)
    throws PermissionsException {
        try {
            Activity[] activities = __getActivities(cdTarget);
            return PermissionsFactory.getPermissions(principal,
                cdTarget, activities);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions[] getPermissions(ChannelDefinition[] cdTargets,
        IGroup principal)
    throws PermissionsException {
        if (cdTargets == null) return new IPermissions[0];

        List permissionsList = new ArrayList(cdTargets.length);

        for (int i=0; i<cdTargets.length; i++) {
            permissionsList.add(getPermissions(cdTargets[i], principal));
        }
        return (IPermissions[])permissionsList.toArray(new IPermissions[0]);
    }

    public IPermissions getPermissions(ChannelDefinition cdTarget,
        IMember principal)
    throws PermissionsException {
        try {
            Activity[] activities = __getActivities(cdTarget);
            return PermissionsFactory.getPermissions(principal,
                cdTarget, activities);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    /**
     * Creates a new <code>IPermissions</code> object in the system.
     * @param target The target channel (<code>ChannelClass</code>)
     * for the permissions.
     * @param principal The principal group for the permissions.
     * @return The newly created <code>IPermissions</code> object.
     * @throws PermissionsException if there no
     * <code>IAuthorizationPrincipal</code> exists for the given group or the
     * underlying permission objects creation fails.
     */
    public IPermissions createPermissions(ChannelClass target, IGroup principal)
    throws PermissionsException {

        try {
            //IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                    newPrincipal(principal.getEntityGroup());

            Activity activity = null;
            String handle = null;
            boolean value = false;
            IPermissions newPerm = getPermissions(target, principal);

            Iterator itr = target.getActivities().iterator();
            while (itr.hasNext()) {
                activity = (Activity)itr.next();
                handle = activity.getHandle();
                value = activity.getDefaultPermissionsSetting(
                    principal.getName());
                newPerm.addActivity(handle, value);
            }
            return newPerm;
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public IPermissions[] createPermissions(IGroup principal)
    throws PermissionsException {
        try {
            List retObj = new ArrayList();
            ChannelClass cc = null;

            Iterator itr = ChannelClassFactory.getChannelClasses().iterator();
            while (itr.hasNext()) {
                cc = (ChannelClass)itr.next();
                retObj.add(createPermissions(cc, principal));
            }
            return (IPermissions[])retObj.toArray(new IPermissions[0]);
        } catch (DomainException de) {
            throw new PermissionsException(de);
        }
    }

    /**
     * Creates a new <code>IPermissions</code> object in the system.
     * @param target The target channel (<code>ChannelDefinition</code>
     * for the permissions.
     * @param group The principal group for the permissions.
     * @return The newly created <code>IPermissions</code> object.
     * @throws PermissionsException if there no
     * <code>IAuthorizationPrincipal</code> exists for the given group or the
     * underlying permission object creations fails.
     */
    public IPermissions createPermissions(ChannelDefinition target,
        IGroup principal)
    throws PermissionsException {
        // ZZZ not implemented yet, just return an empty permissions object.
        return getPermissions(target, principal);
    }

    public IPermissions createPermissions(ChannelDefinition target,
        IMember principal)
    throws PermissionsException {
        // ZZZ not implemented yet, just return an empty permissions object.
        return getPermissions(target, principal);
    }

    /**
     * Removes all the permissions for the given principal <code>IGroup</code>.
     * @param group The target principal group.
     * @throws PermissionsException if there no
     * <code>IAuthorizationPrincipal</code> exists for the given group or the
     * underlying permission object removals fail.
     */
    public void removePermissions(String owner, IGroup principal)
    throws PermissionsException {

        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                    newPrincipal(principal.getEntityGroup());

            IPermission[] uportalPermissions =
                authPrincipal.getPermissions(
                    owner, null, null);

            authorizationService.removePermissions(uportalPermissions);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public void removePermissions(IGroup principal)
    throws PermissionsException {
        try {
            ChannelClass cc = null;

            Iterator itr = ChannelClassFactory.getChannelClasses().iterator();
            while (itr.hasNext()) {
                cc = (ChannelClass)itr.next();
                removePermissions(cc.getClassName(), principal);
            }
        } catch (DomainException de) {
            throw new PermissionsException(de);
        }
    }

    public void removePermissions(String owner, IMember principal)
    throws PermissionsException {

        try {
            IAuthorizationPrincipal authPrincipal =
                AuthorizationService.instance().
                    newPrincipal(principal.getKey(),
                    org.jasig.portal.security.IPerson.class);

            IPermission[] uportalPermissions =
                authPrincipal.getPermissions(
                    owner, null, null);

            authorizationService.removePermissions(uportalPermissions);
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    String getTarget(ChannelClass cc) {
        String target;
        String producerName = cc.getProducer();
        if (producerName == null || "".equals(producerName)) {
            target = cc.getClassName();
        } else {
            target = producerName;
        }
        return target;
    }

    String getTarget(ChannelDefinition cd)
    throws Exception {
        String target;
        String producerName = ChannelDefinitionUtil.
            getParameter(cd,"producer");
        if (producerName == null || "".equals(producerName)) {
            target = cd.getJavaClass();
        } else {
            target = producerName;
        }
        return target;
    }

    private PermissionsService() {}
}
