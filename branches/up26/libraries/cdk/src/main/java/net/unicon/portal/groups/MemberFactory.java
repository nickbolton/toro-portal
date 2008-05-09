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
package net.unicon.portal.groups;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.*;

import org.jasig.portal.groups.IGroupMember;

import java.lang.reflect.Constructor;

/**
 * This factory is responsible for creating objects of type
 * <code>IGroup</code>.
 */

public class MemberFactory {

    /**
     * Instantiates a <code>IMember</code> object from the given key
     * with an implementation that is specified by the
     * <code>CommonPropertiesType</code>.FACTORY property file.
     * @param id The id of the group to retrieve.
     * @return The desired <code>IGroup</code> object.
     * @throws FactoryCreateException if the group object cannot be
     * instantiated.
     */
    public static IMember getMember(String key) throws FactoryCreateException {
        if (key == null) {
            throw new FactoryCreateException(
                "MemberFactory::getMember(String) : key is null!");
        }
        Object[] params = new Object[1];
        Class[] paramClasses = new Class[1];
        params[0] = key;
        paramClasses[0] = String.class;
        return instatiateMember(params, paramClasses);
    }

    static IMember getMember(IGroupMember groupMember)
    throws FactoryCreateException {
        if (groupMember == null) {
            throw new FactoryCreateException(
               "MemberFactory::getMember(IGroupMember) : groupMember is null!");
        }
        if (groupMember.isGroup()) {
            StringBuffer sb = new StringBuffer();
            sb.append("MemberFactory::getMember() groupMember not a group!");
            throw new FactoryCreateException(sb.toString());
        }
        Object[] params = new Object[1];
        Class[] paramClasses = new Class[1];
        params[0] = groupMember;
        paramClasses[0] = IGroupMember.class;
        return instatiateMember(params, paramClasses);
    }

    private static IMember instatiateMember(Object[] params,
        Class[] paramClasses)
    throws FactoryCreateException {
        String propName =
            "net.unicon.portal.groups.MemberFactory.implementation";

        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.FACTORY).getProperty(propName);

        if (className == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("MemberFactory::getMember() could ");
            sb.append("not find a value for '");
            sb.append(propName);
            sb.append("'");
            throw new FactoryCreateException(sb.toString());
        }

        IMember member = null;

        try {
            Constructor constructor =
                Class.forName(className).getDeclaredConstructor(paramClasses);
            member = (IMember)constructor.newInstance(params);
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("MemberFactory::getMember() could not instantiate ");
            sb.append(className);
            sb.append(": ").append(e.getMessage());
            throw new FactoryCreateException(sb.toString(), e);
        }

        return member;
    }
}
