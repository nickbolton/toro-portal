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
package net.unicon.portal.servants;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.servants.permissionadmin.PermissionAdminServantFactory;
import net.unicon.sdk.FactoryCreateException;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerServantFactory;
import org.jasig.portal.channels.permissionsmanager.CPermissionsManagerServantFactory;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.xml.sax.ContentHandler;

/**
 * This class manages servant rendering. It's very important that all
 * ServantManager invocations must contain the identical servant resource
 * identifiers. This is especially critical on calls to sendPortalEvent().
 */
public final class ServantManager {

    private static final Hashtable servants = new Hashtable();
    private static final Hashtable adapters = new Hashtable();
    private static final Hashtable nextCommands = new Hashtable();
    private static final Hashtable nextCommandArgs = new Hashtable();

    /**
     * This will clean up all the resources allocated to the given
     * resource identifier.
     */
    public static void sendPortalEvent(Object resId, ServantType servantType,
        PortalEvent event) {
        String servantKey = getKey(resId, servantType);
        IServant servant = (IServant)servants.get(servantKey);

        // pass the event to the servant
        if (servant != null && servant instanceof IChannel) {
            ((IChannel)servant).receiveEvent(event);
        } 

        // if the session is finished, clean up the servant's resources
        switch (event.getEventNumber()) {
            case PortalEvent.SESSION_DONE:
            case ServantEvent.RENDERING_DONE:
                removeServant(resId, servantType);
            break;
        }
    }

    public static void sendPortalEvent(Object resId, PortalEvent event) {
        ServantType[] servantTypes = ServantType.getServantTypes();
        for (int i=0; servantTypes != null && i<servantTypes.length; i++) {
            sendPortalEvent(resId, servantTypes[i], event);
        }
    }

    /**
     * This will create a permission admin servant. Once the servant is
     * created, this will hold onto it to allow for future invocations
     * via the renderServant method until the isFinshed() method of
     * the servant returns true.
     */
    public static void createPermissionAdminServant(Object resId,
        ChannelStaticData staticData, IPermissible owner, Activity[] activities,
        IAuthorizationPrincipal[] principals, String nextCommand,
        Map nextArgs, boolean groupsOnly, boolean singleSelection)
    throws PortalException {
        createPermissionAdminServant(resId, staticData, owner,
            activities, principals, null, nextCommand, nextArgs, groupsOnly,
            singleSelection);
    }

    public static void createPermissionAdminServant(Object resId,
        ChannelStaticData staticData, IPermissible owner, Activity[] activities,
        IAuthorizationPrincipal[] principals, IResultsAdapter resultsAdapter,
        String nextCommand, Map nextArgs, boolean groupsOnly,
        boolean singleSelection)
    throws PortalException {

        try {
            String servantKey = getKey(resId,
                ServantType.PERMISSION_ADMIN);

            // clean up any previously running servant
            if (hasServant(resId, ServantType.PERMISSION_ADMIN)) {
                removeServant(resId, ServantType.PERMISSION_ADMIN);
            }

            IServant servant = PermissionAdminServantFactory.
                getPermissionsServant(owner, staticData, activities, principals,
                    groupsOnly, singleSelection);
            servants.put(servantKey, servant);
            if (resultsAdapter != null) {
                adapters.put(servantKey, resultsAdapter);
            }
            nextCommands.put(servantKey,
                nextCommand != null ? nextCommand : "");
            nextCommandArgs.put(servantKey,
                nextArgs != null ? nextArgs : new Hashtable());
        } catch (FactoryCreateException fce) {
            throw new PortalException(fce);
        }
    }

    /**
     * This will create a permissions manager servant. Once the servant is
     * created, this will hold onto it to allow for future invocations
     * via the renderServant method until the isFinshed() method of
     * the servant returns true.
     */
    public static void createGroupsManagerServant(Object resId,
        ChannelStaticData staticData, String message, String type,
        String nextCommand, Map nextArgs)
    throws PortalException {
        createGroupsManagerServant(resId, staticData,
            message, type, nextCommand, nextArgs, null);
    }

    public static void createGroupsManagerServant(Object resId,
        ChannelStaticData staticData, String message, String type,
        String nextCommand, Map nextArgs, IResultsAdapter resultsAdapter)
    throws PortalException {
        String servantKey = getKey(resId, ServantType.GROUPS_MANAGER);

        // clean up any previously running servant
        if (hasServant(resId, ServantType.GROUPS_MANAGER)) {
            removeServant(resId, ServantType.GROUPS_MANAGER);
        }

        IServant servant = CGroupsManagerServantFactory.
            getGroupsServantforSelection(staticData, message, type);
        servants.put(servantKey, servant);
        nextCommands.put(servantKey,
                nextCommand != null ? nextCommand : "");
        nextCommandArgs.put(servantKey,
            nextArgs != null ? nextArgs : new Hashtable());
        if (resultsAdapter != null) {
            adapters.put(servantKey, resultsAdapter);
        }
    }
    
    /**
     * This will create a permissions manager servant. Once the servant is
     * created, this will hold onto it to allow for future invocations
     * via the renderServant method until the isFinshed() method of
     * the servant returns true.
     */
    public static void createPermissionsManagerServant(Object resId,
        ChannelStaticData staticData, IPermissible owner, String[] targets,
        IAuthorizationPrincipal[] principals, String nextCommand,
        Map nextArgs)
    throws PortalException {
        createPermissionsManagerServant(resId, staticData, owner,
            targets, principals, nextCommand, nextArgs, null);
    }

    public static void createPermissionsManagerServant(Object resId,
        ChannelStaticData staticData, IPermissible owner, String[] targets,
        IAuthorizationPrincipal[] principals, String nextCommand,
        Map nextArgs, IResultsAdapter resultsAdapter)
    throws PortalException {
        String servantKey = getKey(resId,
            ServantType.PERMISSIONS_MANAGER);

        // clean up any previously running servant
        if (hasServant(resId, ServantType.PERMISSIONS_MANAGER)) {
            removeServant(resId, ServantType.PERMISSIONS_MANAGER);
        }

        IServant servant = CPermissionsManagerServantFactory.
            getPermissionsServant(owner, staticData, principals, null, targets);
        servants.put(servantKey, servant);
        nextCommands.put(servantKey,
                nextCommand != null ? nextCommand : "");
        nextCommandArgs.put(servantKey,
            nextArgs != null ? nextArgs : new Hashtable());
        if (resultsAdapter != null) {
            adapters.put(servantKey, resultsAdapter);
        }
    }

    /**
     * This will create an Address book entity selector servant.
     * Once the servant is created, this will hold onto it to
     * allow for future invocations via the renderServant method
     * until the isFinshed() method of the servant returns true.
     */
    public static void createIdentitySelectorServant(Object resId,
        ChannelStaticData staticData, String nextCommand, Map nextArgs)
    throws PortalException {
        createIdentitySelectorServant(resId, staticData,
            nextCommand, nextArgs, null);
    }

    public static void createIdentitySelectorServant(Object resId,
        ChannelStaticData staticData, String nextCommand, Map nextArgs,
        IResultsAdapter resultsAdapter)
    throws PortalException {
        String servantKey = getKey(resId,
            ServantType.IDENTITY_SELECTOR);

        // clean up any previously running servant
        if (hasServant(resId, ServantType.IDENTITY_SELECTOR)) {
            removeServant(resId, ServantType.IDENTITY_SELECTOR);
        }

        Servant servant = new Servant();
        servant.setStaticData(staticData);
        servant.start("net.unicon.portal.channels.addressbook.Select");
        servants.put(servantKey, servant);
        nextCommands.put(servantKey,
            nextCommand != null ? nextCommand : "");
        nextCommandArgs.put(servantKey,
            nextArgs != null ? nextArgs : new Hashtable());
        if (resultsAdapter != null) {
            adapters.put(servantKey, resultsAdapter);
        }
    }

    public static void removeServant(Object resId,
        ServantType servantType) {
        String servantKey = getKey(resId, servantType);
        IServant servant = (IServant)servants.get(servantKey);

        // clean up a previous running servant --
        // send the RENDERING_DONE event to the servant
        // so it will clean up
        if (servant != null && servant instanceof IChannel) {
            ((IChannel)servant).receiveEvent(
                new PortalEvent(ServantEvent.RENDERING_DONE));
        }
        adapters.remove(servantKey);
        nextCommands.remove(servantKey);
        nextCommandArgs.remove(servantKey);
        servants.remove(servantKey);
    }

    public static ServantResults renderServant(Object resId,
        PrintWriter out, ChannelStaticData staticData,
        ChannelRuntimeData runtimeData,
        ServantType servantType)
    throws PortalException {
        return __renderServant(resId, out, staticData, runtimeData, servantType);
    }

    public static ServantResults renderServant(Object resId,
        ContentHandler out, ChannelStaticData staticData,
        ChannelRuntimeData runtimeData,
        ServantType servantType)
    throws PortalException {
        return __renderServant(resId, out, staticData, runtimeData, servantType);
    }

    // this method is accessed though the public renderServant methods
    // only, so the out parameter is guarenteed to be a valid type.
    private static ServantResults __renderServant(Object resId,
        Object out, ChannelStaticData staticData, ChannelRuntimeData runtimeData,
        ServantType servantType)
    throws PortalException {

        String servantKey = getKey(resId, servantType);

        ServantResults results = new ServantResults(null, "", null);
        IServant servant = (IServant)servants.get(servantKey);
        String nextCommand = (String)nextCommands.get(servantKey);
        Map nextArgs = (Map)nextCommandArgs.get(servantKey);

        if (servant == null) {
            // no servant exists for this owner, just return
            return results;
        }

        if (out instanceof PrintWriter) {
            ServantUtil.renderServant((PrintWriter)out,servant,staticData,runtimeData);
        } else if (out instanceof ContentHandler) {
            ServantUtil.renderServant((ContentHandler)out,servant,runtimeData);
        } else {
            // should never get here, but throw an exception anyway
            throw new PortalException("ServantManager::__renderServant() " +
                "invalid out parameter: " + out.getClass());
        }

        if (servant.isFinished()) {
            // finished, clean up the servant and return the results
            IResultsAdapter adapter = (IResultsAdapter)adapters.get(servantKey);
            if (adapter == null) {
                results = new ServantResults(
                    servant.getResults(), nextCommand, nextArgs);
            } else {
                results = new ServantResults(
                    adapter.adapt(servant.getResults()), nextCommand, nextArgs);
            }
            removeServant(resId, servantType);
        } else {
            results = null;
        }

        String command = runtimeData.getParameter("command");
        if (command != null && command.toLowerCase().indexOf("cancel") >= 0) {
            results = null;
        }
        command = runtimeData.getParameter("servant_command");
        if (command != null && command.toLowerCase().indexOf("cancel") >= 0) {
            results = null;
        }
        command = runtimeData.getParameter("do~cancel");
        if (command != null && command.toLowerCase().indexOf("cancel") >= 0) {
            results = null;
        }

        // not finished
        return results;
    }

    public static boolean hasServant(Object resId,
        ServantType servantType) {
        return servants.containsKey(getKey(resId, servantType));
    }

    public static String getNextCommand(Object resId,
        ServantType servantType) {
        return (String)nextCommands.get(getKey(resId, servantType));
    }

    public static Map getNextCommandArgs(Object resId,
        ServantType servantType) {
        return (Map)nextCommandArgs.get(getKey(resId, servantType));
    }

    private static String getKey(Object resId, ServantType servantType) {
        StringBuffer servantKey = new StringBuffer();
        servantKey.append(resId.hashCode());
        servantKey.append(servantType.toString());
        return servantKey.toString();
    }

    private ServantManager() {}
}
