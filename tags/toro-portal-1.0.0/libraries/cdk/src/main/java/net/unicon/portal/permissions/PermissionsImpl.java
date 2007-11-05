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

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.academus.common.properties.AcademusPropertiesType;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;

import net.unicon.portal.common.properties.PortalPropertiesType;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.provider.AuthorizationServiceFactoryImpl;

import org.jasig.portal.services.EntityLockService;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This Implementation of IPermissions is a wrapper around the uPortal
 * <code>IAuthorizationPrincipal</code> object.
 */

public class PermissionsImpl implements IPermissions {

    private String owner;
    private IGroup groupPrincipal;
    private IMember memberPrincipal;
    private String target;
    private IAuthorizationPrincipal authPrincipal;
    private static IAuthorizationService authorizationService;
    private static List superUsers;
    private IUpdatingPermissionManager permissionManager;
    private PrincipalType principalType;
    private Map activities;
    private static int permissionLockTimeout;

    // Default permission lock timeout is 60 seconds
    private static int DEFAULT_TIMEOUT = 60;

    static {
        try {
            authorizationService =
                new AuthorizationServiceFactoryImpl().getAuthorization();

            superUsers = new ArrayList();

            StringTokenizer st = new StringTokenizer(
                UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).
                getProperty(
                "net.unicon.academus.domain.lms.Permissions.superUsers"), ",");

            while (st.hasMoreTokens()) {
                superUsers.add(st.nextToken());
            }

        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }

    static {
        try{
            String timeout = UniconPropertiesFactory.
                                getManager(PortalPropertiesType.PORTAL).
                                    getProperty("net.unicon.portal.permissions.timeout");

            if (timeout == null)
                throw new Exception();

            permissionLockTimeout = Integer.parseInt(timeout);

        } catch (Exception e) {

            permissionLockTimeout = DEFAULT_TIMEOUT;

            StringBuffer sb = new StringBuffer();
            sb.append("PermissionsImpl:");
            sb.append("Error retrieving value for \"net.unicon.portal.permissions.timeout\".\n");
            sb.append("Timeout set to default:" + DEFAULT_TIMEOUT + " seconds.");

           LogService.log(LogService.ERROR,sb.toString(), e);
        }
    }

    PermissionsImpl(IGroup principal, String target, Activity[] activities) {
        try {
            this.owner = target;
            setPrincipal(principal);
            this.target = target;
            this.permissionManager = AuthorizationService.instance().
                newUpdatingPermissionManager(owner);
            __setupActivities(activities);
        } catch (AuthorizationException ae) {
            LogService.log(LogService.ERROR, ae);
        } catch (PermissionsException pe) {
            LogService.log(LogService.ERROR, pe);
        }
    }

    PermissionsImpl(IMember principal, String target, Activity[] activities) {
        try {
            this.owner = target;
            setPrincipal(principal);
            this.target = target;
            this.permissionManager = AuthorizationService.instance().
                newUpdatingPermissionManager(owner);
            __setupActivities(activities);
        } catch (AuthorizationException ae) {
            LogService.log(LogService.ERROR, ae);
        } catch (PermissionsException pe) {
            LogService.log(LogService.ERROR, pe);
        }
    }

    PermissionsImpl(IAuthorizationPrincipal principal, String target,
        Activity[] activities) {
        try {
            this.owner = target;
            this.authPrincipal = principal;
            this.target = target;
            this.permissionManager = AuthorizationService.instance().
                newUpdatingPermissionManager(owner);
            __setupActivities(activities);
        } catch (AuthorizationException ae) {
            LogService.log(LogService.ERROR, ae);
        }
    }

    private void __setupActivities(Activity[] activities) {
        this.activities = new HashMap();

        if (activities == null) return;

        for (int i=0; i<activities.length; i++) {
            this.activities.put(activities[i].getHandle(), activities[i]);
        }
    }

    // Target - Channel

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    // Principal - Group

    /**
     * Retrieve the principal (<code>IGroup</code>) object for this permission.
     * @return The <code>IGroup</code> object for this permission.
     */
    public IGroup getGroupPrincipal() {
        return groupPrincipal;
    }

    public IMember getMemberPrincipal() {
        return memberPrincipal;
    }

    public IAuthorizationPrincipal getPrincipal() {
        return authPrincipal;
    }

    public void setPrincipal(IAuthorizationPrincipal newPrincipal) {
        this.authPrincipal = newPrincipal;
    }

    public PrincipalType getPrincipalType() {
        return principalType;
    }

    /**
     * Sets the principal (<code>IGroup</code>) object for this permission.
     * This will retrive a uPortal <code>IAuthorizationPrincipal</code> object
     * from the uPortal <code>AuthorizationService</code> that is based
     * on the given group and set it as its authorization principal.
     * @param newPrincipal the new <code>IGroup</code> object.
     * @throws PermissionsException if the underlying uPortal
     * <code>IAuthorizationPrincipal</code> object does not exist.
     */
    public void setPrincipal (IGroup newPrincipal)
    throws PermissionsException {
        try {
            this.principalType = PrincipalType.GROUP;
            this.groupPrincipal = newPrincipal;
            this.authPrincipal = AuthorizationService.instance().
                newPrincipal(newPrincipal.getEntityGroup());
            if (authPrincipal == null) {
                StringBuffer sb = new StringBuffer();
                sb.append("PermissionsImpl::setPrincipal() : ");
                sb.append("Could not find authorization principal for group: ");
                sb.append(newPrincipal.getGroupId()).append(" - ");
                sb.append(newPrincipal.getName());
                throw new PermissionsException(sb.toString());
            }
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public void setPrincipal (IMember newPrincipal)
    throws PermissionsException {
        try {
            this.principalType = PrincipalType.PERSON;
            this.memberPrincipal = newPrincipal;
            this.authPrincipal = AuthorizationService.instance().
                newPrincipal(newPrincipal.getKey(),
                org.jasig.portal.security.IPerson.class);
            if (authPrincipal == null) {
                StringBuffer sb = new StringBuffer();
                sb.append("PermissionsImpl::setPrincipal() : ");
                sb.append("Could not find authorization ");
                sb.append("principal for member: ");
                sb.append(newPrincipal.getKey());
                throw new PermissionsException(sb.toString());
            }
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    // Activities

    /**
     * Retrieve the activities for this permission. A permission object
     * consists of a collection of activities. This particular mapping
     * contains names mapped to an enumeration of permission values "Y" and "N".
     * @return A <code>Map</code> that contains the <code>IGroup</code> objects
     * for a permissions object.
     * @throws PermissionsException if it cannot retrieve the underlying 
     * uPortal permission objects.
     */
    public Map getEnumeratedActivities(String username)
    throws PermissionsException {
        if (activities == null) return new HashMap();

        Map enumeratedActivities = new HashMap(activities.size());

        String handle;
        Iterator itr = activities.keySet().iterator();
        while (itr.hasNext()) {
            handle = (String)itr.next();
            enumeratedActivities.put(handle, canDo(username, handle)
                ? IPermissions.YES : IPermissions.NO);
        }
        return enumeratedActivities;
    }

    public Map getCompleteActivities() {
        return activities;
    }
    
    /**
     * Retrieve the activities for this permission. A permission object
     * consists of a collection of activities. This particular mapping
     * should contain names mapped to <code>Boolean</code> values.
     * @return A <code>Map</code> that contains the <code>Boolean</code> objects
     * for a permissions object.
     * @throws PermissionsException if it cannot retrieve the underlying 
     * uPortal permission objects.
     */
    public Map getActivities()
    throws PermissionsException {
        IPermission[] uportalPermissions = getUPortalPermissions();

        Map activities = new HashMap(uportalPermissions.length);

        for (int i=0; i<uportalPermissions.length; i++) {
            activities.put(uportalPermissions[i].getActivity(),
                new Boolean(IPermission.PERMISSION_TYPE_GRANT.equals(
                    uportalPermissions[i].getType())));
        }

        return activities;
    }

    /**
     * Tells if a permissions object has the given activity.
     * @return The existence value of the given activity.
     * @throws PermissionsException if it cannot retrieve the underlying 
     * uPortal permission objects.
     */
    public boolean hasActivity(String activity)
    throws PermissionsException {
        return findPermission(activity) != null;
    }

    
    /**
     * This associates a value with an activity of a permissions object. If
     * the activity doesn't exist, it should add it.
     * @param name the name of the activty.
     * @param value the value of the activity.
     * @throws PermissionsException if it cannot retrieve the underlying
     * uPortal permission objects or if its persistence fails.
     */
    public void setActivity(String name, boolean value)
    throws PermissionsException {
        try {
            // create a new uPortal IPermission object
            IPermission newPermission =
                permissionManager.newPermission(authPrincipal);
            newPermission.setActivity(name);
            newPermission.setType(getActivityType(value));
            newPermission.setTarget(getTargetToken());

            // now save it
            doAssignment(newPermission, false);
        } catch (AuthorizationException ae) {
            throw new PermissionsException(ae);
        }
    }

    /**
     * This adds a name/value activity with a permissions object.
     * @param name the name of the new activty.
     * @param value the initial value of the new activity.
     * @throws PermissionsException if the underlying uPortal permission object
     * creation fails.
     */
    public void addActivity(String name, boolean value)
    throws PermissionsException {
        setActivity(name, value);
    }

    /**
     * This removes the association with the permissions object and 
     * the activity with the given name.
     * @param name the name of the activty to be removed.
     * @throws PermissionsException if the underlying uPortal permission object
     * removal fails.
     */
    public void removeActivity(String name)
    throws PermissionsException {
        IPermission permission = findPermission(name);

        if (permission != null) {
            doAssignment(permission, true);
        }
    }

    //
    //  Lock Key is formed by the following:
    //  Key = Owner + Target + Principal + Activity
    //
    private String generateLockKey (IPermission permission) {

         String owner  = permission.getOwner();
         String target = permission.getTarget();
         String principal = permission.getPrincipal();
         String activity  = permission.getActivity();

        String lockKey = owner + target + principal + activity;

        return lockKey;
    }

    private void doAssignment(IPermission permission, boolean remove)
    throws PermissionsException {
        try {
            IPermission[] permissions = {permission};

            // Generate lock key based on permission values
            String lockKey = generateLockKey(permission);

            EntityLockService lockService =  EntityLockService.instance();

            // Lock permission for a specific target, owner, principal and activity name
            IEntityLock lock = lockService.newWriteLock(this.getClass(), lockKey,
                                        this.getClass().getName(), permissionLockTimeout);

            // Check if the lock is valid
            if (lock.isValid()) {

                permissionManager.removePermissions(permissions);

                if (!remove) {
                    permissionManager.addPermissions(permissions);
                }

                lock.release();
            } else {

                throw new LockingException();                
            }
        } catch (AuthorizationException ae) {
                               
            throw new PermissionsException(ae);

        } catch (LockingException le) {

            String error = logPermissionsError("doAssignment",
                            "Failed to obtain lock on permission", permission);

            throw new PermissionsException(error, le);                         
        }
    }

    private String logPermissionsError(String method, String message, IPermission permission) {

        StringBuffer sb = new StringBuffer();
        sb.append("PermissionsImpl:");
        sb.append(method + "()");
        sb.append(message + ":\n");
        sb.append("Owner:" + permission.getOwner() + "\n");
        sb.append("Target:" + permission.getTarget() + "\n");
        sb.append("Principal:" + permission.getPrincipal() + "\n");
        sb.append("Activity:" + permission.getActivity() + "\n");

        LogService.log(LogService.ERROR, sb.toString());

        return sb.toString();
    }

    /**
     * Tells if the principal can perform the given activity.
     * @return The ability to perform the given activity.
     */
/*
    public boolean canDo(String name) {
        boolean retValue = false;
        try {
            Boolean value = (Boolean)getActivities().get(name);
            if (value != null) {
                retValue = value.booleanValue();
            }
        } catch (PermissionsException pe) {
            retValue = false;
            LogService.log(LogService.ERROR, pe);
        }
        return retValue;
    }
*/

    public boolean canDo(String username, String activity) {
        boolean retValue = false;

        if (username != null && superUsers.contains(username)) {
            return true;
        }

        try {
//            retValue = authorizationService.doesPrincipalHavePermission(
//                authPrincipal, owner, activity, getTargetToken());

            retValue = authPrincipal.hasPermission(owner, activity,
                getTargetToken());
        } catch (AuthorizationException ae) {
            retValue = false;
            LogService.log(LogService.ERROR, ae);
        }
        return retValue;
    }

    /**
     * Prints out a listing of the contents of a permissions object. This 
     * is used for debugging.
     */
    public void dump() {
        try {
            System.out.println("Permissions Object:");

            System.out.println("\tAuth Principal: " + authPrincipal.getKey() +
                " - " + authPrincipal.getPrincipalString());
            System.out.println("\tPrincipal: " + (groupPrincipal != null ? groupPrincipal.getName() : "null"));

            System.out.println("\tTarget: " + target);

            System.out.println("\tActivities:");

            Iterator itr = this.activities.keySet().iterator();
            String name = null;
            String value = null;
            while (itr.hasNext()) {
                name = (String)itr.next();
                value = authorizationService.doesPrincipalHavePermission(
                    authPrincipal, owner, name,
                        getTargetToken()) ? IPermissions.YES : IPermissions.NO;
                System.out.println("\t\t" + name + ": " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogService.log(LogService.ERROR, e);
        }
    }

    private IPermission[] getUPortalPermissions()
    throws PermissionsException {
        // retrieve the uPortal permissions.
        // This permissions object is the owner.

        try {
            return authPrincipal.getPermissions(
                owner, null, getTargetToken());
        } catch (Exception e) {
            throw new PermissionsException(e);
        }
    }

    public String getTargetToken() {
        return target;
    }

    private IPermission findPermission(String activity)
    throws PermissionsException {

        IPermission retValue = null;
        IPermission[] permissions = getUPortalPermissions();
        
        for (int i=0; i<permissions.length; i++) {
            if (permissions[i].getActivity().equals(activity)) {
                return permissions[i];
            }
        }
        return retValue;
    }

    private String getActivityType(boolean value) {
        return value ? IPermission.PERMISSION_TYPE_GRANT :
            IPermission.PERMISSION_TYPE_DENY;
    }

}
