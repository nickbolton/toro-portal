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

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;

import java.lang.reflect.Constructor;

/**
 * This factory is responsible for creating objects of type
 * <code>IGroup</code>.
 */

public class GroupFactory {

    /**
     * Retrieves an <code>IGroup</code> object with the given id
     * with an implementation that is specified by the
     * <code>CommonPropertiesType</code>.FACTORY property file.
     * @param id The id of the group to retrieve.
     * @return The desired <code>IGroup</code> object.
     * @throws FactoryCreateException if the group object cannot be
     * instantiated.
     */
    public static IGroup getGroup(long id) throws FactoryCreateException {
        Object[] params = new Object[1];
        Class[] paramClasses = new Class[1];
        params[0] = new Long(id);
        paramClasses[0] = Long.class;
        return instatiateGroup(params, paramClasses);
    }

    /**
     * Retrieves an <code>IGroup</code> object with the given key
     * with an implementation that is specified by the
     * <code>CommonPropertiesType</code>.FACTORY property file.
     * @param key The key of the group to retrieve.
     * @return The desired <code>IGroup</code> object.
     * @throws FactoryCreateException if the group object cannot be
     * instantiated.
     */
    public static IGroup getGroup(String key) throws FactoryCreateException {
        Object[] params = new Object[1];
        Class[] paramClasses = new Class[1];
        params[0] = key;
        paramClasses[0] = String.class;
        return instatiateGroup(params, paramClasses);
    }

    /**
     * Retrieves an <code>IGroup</code> object with the given
     * <code>IEntityGroup</code> association with an implementation
     * that is specified by the <code>CommonPropertiesType</code>.FACTORY
     * property file.
     * @param entityGroup The associated uPortal <code>IEntityGroup</code>
     * @return The desired <code>IGroup</code> object.
     * @throws FactoryCreateException if the group object cannot be
     * instantiated.
     */
    public static IGroup getGroup(IEntityGroup entityGroup)
    throws FactoryCreateException {
        Object[] params = new Object[1];
        Class[] paramClasses = new Class[1];
        params[0] = entityGroup;
        paramClasses[0] = IEntityGroup.class;
        return instatiateGroup(params, paramClasses);
    }

    static IGroup getGroup(IGroupMember groupMember)
    throws FactoryCreateException {
        if (!groupMember.isGroup() || !(groupMember instanceof IEntityGroup)) {
            StringBuffer sb = new StringBuffer();
            sb.append("GroupFactory::getGroup() groupMember is not a group!");
            throw new FactoryCreateException(sb.toString());
        }
        return getGroup((IEntityGroup)groupMember);
    }

    private static IGroup instatiateGroup(Object[] params, Class[] paramClasses)
    throws FactoryCreateException {
        String propName =
            "net.unicon.portal.groups.GroupFactory.implementation";

        String className = UniconPropertiesFactory.getManager(
            CommonPropertiesType.FACTORY).getProperty(propName);

        if (className == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("GroupFactory::getGroup() could not find a value for '");
            sb.append(propName);
            sb.append("'");
            throw new FactoryCreateException(sb.toString());
        }

        IGroup group = null;

        try {
            Constructor constructor =
                Class.forName(className).getDeclaredConstructor(paramClasses);
            group = (IGroup)constructor.newInstance(params);
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("GroupFactory::getGroup() could not instantiate ");
            sb.append(className);
            throw new FactoryCreateException(sb.toString(), e);
        }

        return group;
    }
}
