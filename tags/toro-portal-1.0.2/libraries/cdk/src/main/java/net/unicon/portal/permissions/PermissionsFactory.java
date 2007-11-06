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
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.*;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;

import java.lang.reflect.Constructor;

/**
 * This factory is responsible for creating objects of type
 * <code>IPermission</code>.
 */

public class PermissionsFactory {

    /**
     * Retrieves a <code>IPermissions</code> with principcal <code>IGroup</code>
     * and target <code>ChannelClass</code> with an implementation that is
     * specified by the <code>CommonPropertiesType</code>.FACTORY property file.
     * @param principal The principal group of the permissions.
     * @param target The target channel of the permissions.
     * @return The desired <code>IPermissions</code> object.
     * @throws FactoryCreateException if the object could not be instantiated.
     */
    public static IPermissions getPermissions(IGroup principal,
        ChannelClass target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IGroup.class;
        params[1] = PermissionsService.instance().getTarget(target);
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiateChannelClassPermissions(params, paramClasses, target);
    }

    public static IPermissions getPermissions(IMember principal,
        ChannelClass target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IMember.class;
        params[1] = PermissionsService.instance().getTarget(target);
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiateChannelClassPermissions(params, paramClasses, target);
    }

    public static IPermissions getPermissions(IAuthorizationPrincipal principal,
		ChannelClass target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IAuthorizationPrincipal.class;
        params[1] = PermissionsService.instance().getTarget(target);
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiateChannelClassPermissions(params, paramClasses, target);
    }

    /**
     * Retrieves a <code>IPermissions</code> with principcal <code>IGroup</code>
     * and target <code>ChannelDefinition</code> with an implementation that is
     * specified by the <code>CommonPropertiesType</code>.FACTORY property file.
     * @param principal The principal group of the permissions.
     * @param target The target channel of the permissions.
     * @return The desired <code>IPermissions</code> object.
     * @throws FactoryCreateException if the object could not be instantiated.
     */
    public static IPermissions getPermissions(IGroup principal,
		ChannelDefinition target, Activity[] activities)
    throws FactoryCreateException {
        try {
            Object[] params = new Object[3];
            Class[] paramClasses = new Class[3];
            params[0] = principal;
            paramClasses[0] = IGroup.class;
            params[1] = PermissionsService.instance().getTarget(target);
            paramClasses[1] = String.class;
            params[2] = activities;
            paramClasses[2] = Activity[].class;
            return instantiateChannelDefinitionPermissions(params,
                paramClasses, target);
        } catch (FactoryCreateException fce) {
            throw fce;
        } catch (Exception e) {
            throw new FactoryCreateException(e);
        }
    }

    public static IPermissions getPermissions(IMember principal,
		ChannelDefinition target, Activity[] activities)
    throws FactoryCreateException {
        try {
            Object[] params = new Object[3];
            Class[] paramClasses = new Class[3];
            params[0] = principal;
            paramClasses[0] = IMember.class;
            params[1] = PermissionsService.instance().getTarget(target);
            paramClasses[1] = String.class;
            params[2] = activities;
            paramClasses[2] = Activity[].class;
            return instantiateChannelDefinitionPermissions(params,
                paramClasses, target);
        } catch (FactoryCreateException fce) {
            throw fce;
        } catch (Exception e) {
            throw new FactoryCreateException(e);
        }
    }

    public static IPermissions getPermissions(IAuthorizationPrincipal principal,
		ChannelDefinition target, Activity[] activities)
    throws FactoryCreateException {
        try {
            Object[] params = new Object[3];
            Class[] paramClasses = new Class[3];
            params[0] = principal;
            paramClasses[0] = IAuthorizationPrincipal.class;
            params[1] = PermissionsService.instance().getTarget(target);
            paramClasses[1] = String.class;
            params[2] = activities;
            paramClasses[2] = Activity[].class;
            return instantiateChannelDefinitionPermissions(params,
                paramClasses, target);
        } catch (FactoryCreateException fce) {
            throw fce;
        } catch (Exception e) {
            throw new FactoryCreateException(e);
        }
    }

    public static IPermissions getPermissions(IGroup principal,
		String target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IGroup.class;
        params[1] = target;
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiate(params, paramClasses);
    }

    public static IPermissions getPermissions(IAuthorizationPrincipal principal,
		String target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IAuthorizationPrincipal.class;
        params[1] = target;
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiate(params, paramClasses);
    }

    public static IPermissions getPermissions(IMember principal,
		String target, Activity[] activities)
    throws FactoryCreateException {
        Object[] params = new Object[3];
        Class[] paramClasses = new Class[3];
        params[0] = principal;
        paramClasses[0] = IMember.class;
        params[1] = target;
        paramClasses[1] = String.class;
        params[2] = activities;
        paramClasses[2] = Activity[].class;
        return instantiate(params, paramClasses);
    }

    private static IPermissions instantiateChannelClassPermissions(
        Object[] params, Class[] paramClasses, ChannelClass channelClass)
    throws FactoryCreateException {
        IPermissions permissions = instantiate(params, paramClasses);
        return new ChannelClassPermissions(permissions, channelClass);
    }

    private static IPermissions instantiateChannelDefinitionPermissions(
        Object[] params, Class[] paramClasses, ChannelDefinition channelDef)
    throws FactoryCreateException {
        IPermissions permissions = instantiate(params, paramClasses);
        return new ChannelDefinitionPermissions(permissions, channelDef);
    }

    private static IPermissions instantiate(Object[] params,
        Class[] paramClasses)
    throws FactoryCreateException {
        String propName =
            "net.unicon.portal.permissions.PermissionsFactory.implementation";
        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.FACTORY).getProperty(propName);

        if (className == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionsFactory::getGroup() could ");
			sb.append("not find a value for '");
            sb.append(propName);
            sb.append("'");
            throw new FactoryCreateException(sb.toString());
        }

        IPermissions permissions = null;

        try {
            Constructor constructor =
                Class.forName(className).getDeclaredConstructor(paramClasses);
            permissions = (IPermissions)constructor.newInstance(params);
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("PermissionsFactory::getPermissions() ");
			sb.append("could not instantiate ");
            sb.append(className);
            throw new FactoryCreateException(sb.toString(), e);
        }

        return permissions;
    }
}
