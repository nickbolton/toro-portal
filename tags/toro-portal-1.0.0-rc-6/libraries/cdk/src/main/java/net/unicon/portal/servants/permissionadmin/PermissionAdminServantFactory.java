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
package net.unicon.portal.servants.permissionadmin;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.permissions.Activity;
import net.unicon.sdk.FactoryCreateException;

import org.jasig.portal.IServant;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.IPermissible;
import org.jasig.portal.services.LogService;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;


/**
 * PermissionAdminServantFactory
 *
 * calling getPermissionsServant will return an instance of the default
 * PermissionAdminServant implementation
 */
public final class PermissionAdminServantFactory {

    private static final String propName =
        PermissionAdminServantFactory.class.getName() + ".implementation";

    /**
     * @param owner
     * @param principals
     * @param activities
     * @param targets
     * @param staticData
     * @return <code>IServant</code>
     * @exception org.jasig.portal.PortalException
     */
    public static IServant getPermissionsServant (IPermissible owner,
        ChannelStaticData staticData, Activity[] activities,
        IAuthorizationPrincipal[] principals, boolean groupsOnly,
        boolean singleSelection)
    throws FactoryCreateException {

        if (owner == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionAdminServantFactory.");
            sb.append("getPermissionsServant():: owner ");
            sb.append("parameter must be specified.");
            LogService.instance().log(LogService.ERROR, sb.toString());
            throw new FactoryCreateException(sb.toString());
        }

        if (staticData == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionAdminServantFactory.");
            sb.append("getPermissionsServant():: static data ");
            sb.append("parameter must be specified.");
            LogService.instance().log(LogService.ERROR, sb.toString());
            throw new FactoryCreateException(sb.toString());
        }

        if (activities == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionAdminServantFactory.");
            sb.append("getPermissionsServant():: ");
            sb.append("activities parameter must be specified.");
            LogService.instance().log(LogService.ERROR, sb.toString());
            throw new FactoryCreateException(sb.toString());
        }

        IServant servant = null;
        try {
            boolean isOK = true;
            servant = getServant();
            ChannelStaticData slaveSD =
                (ChannelStaticData)staticData.clone();
            Enumeration srd = slaveSD.keys();
            while (srd.hasMoreElements()) {
                slaveSD.remove(srd.nextElement());
            }
            if (principals != null) {
                slaveSD.put("principals", principals);
            }
            slaveSD.put("owner", owner);
            slaveSD.put("activities", activities);
            slaveSD.put("groupsOnly", new Boolean(groupsOnly));
            slaveSD.put("singleSelection", new Boolean(singleSelection));
            ((IChannel)servant).setStaticData(slaveSD);
        } catch (PortalException pe) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionAdminServantFactory.");
            sb.append("getPermissionsServant():: unable to ");
            sb.append("properly initialize servant, check ");
            sb.append("that mast staticData is being properly ");
            sb.append("passed to this method");
            LogService.instance().log(LogService.ERROR, sb.toString(), pe);
            throw new FactoryCreateException(sb.toString());
        }
        return  servant;
    }

    /**
     * @param name
     * @return <code>IServant</code>
     */
    private static IServant getServant ()
    throws FactoryCreateException {
        IServant servant = null;
        String className = UniconPropertiesFactory.getManager(
            PortalPropertiesType.FACTORY).getProperty(propName);

        if (className == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionsServantFactory::getServant() ");
            sb.append("could not find a value for '");
            sb.append(propName);
            sb.append("'");
            throw new FactoryCreateException(sb.toString());
        }

        try {
            servant = (IServant)Class.forName(className).newInstance();
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionsServantFactory::getServant() ");
            sb.append("could not instantiate ");
            sb.append(className);
            LogService.instance().log(LogService.ERROR, sb.toString(), e);
            throw new FactoryCreateException(sb.toString(), e);
        }
        return servant;
    }
}
