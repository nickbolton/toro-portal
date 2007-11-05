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
package net.unicon.portal.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.unicon.academus.domain.DomainException;
import net.unicon.sdk.util.ResourceLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides static methods to access the channel classes that are available
 * within the portal system.  <code>ChannelClassFactory</code> is not instantiable.
 */
public final class ChannelClassFactory {
    
    private static Map channelClasses = new HashMap();
    private static Map channelClassesByMode = new HashMap();
    private static Map orderedChannelClassesByMode = new HashMap();
    private ChannelClassFactory() { }

    /**
     * Provides the set of channel classes available within the portal system.
     * @return the complete set of channel classes.
     */
    public static Map getChannelClassMap()
    throws DomainException {
        if (channelClasses.size() == 0) {
            loadClasses();
        }
        return new HashMap(channelClasses);
    }

    public static Map getChannelClassMap(ChannelMode channelMode)
    throws DomainException {
        if (channelClassesByMode.size() == 0) {
            loadClasses();
        }
        Map classes = (Map)channelClassesByMode.get(channelMode);
        if (classes == null) {
            String msg = "Could not find Channel Classes of Mode: " +
            channelMode.toString();
            throw new DomainException(msg);
        }
        return new HashMap(classes);
    }
    public static Map getOrderedChannelClassMap(ChannelMode channelMode)
    throws DomainException {
        if (channelClassesByMode.size() == 0) {
            loadClasses();
        }
        Map classes = (Map)orderedChannelClassesByMode.get(channelMode);
        if (classes == null) {
            String msg = "Could not find Channel Classes of Mode: " +
            channelMode.toString();
            throw new DomainException(msg);
        }
        return new TreeMap(classes);
    }

    public static List getChannelClasses()
    throws DomainException {
        return new ArrayList(getChannelClassMap().values());
    }

    public static List getChannelClasses(ChannelMode channelMode)
    throws DomainException {
        return new ArrayList(getChannelClassMap(channelMode).values());
    }

    public static List getChannelClasses(ChannelType channelType,
        boolean includeGlobalModeChannels)
    throws DomainException {
        List allChannelClasses = getChannelClasses();
        List retList = new ArrayList(allChannelClasses.size());
        ChannelClass cc = null;
        Iterator itr = allChannelClasses.iterator();
        while (itr.hasNext()) {
            cc = (ChannelClass)itr.next();
            if (ChannelMode.GLOBAL.equals(cc.getChannelMode())) {
                // if including global channels, add it
                // otherwise skip it regardless of it's type
                if (includeGlobalModeChannels) {
                    retList.add(cc);
                } else {
                    continue;
                }
            }

            if (channelType.equals(cc.getChannelType())) {
                retList.add(cc);
            }
        }
        return retList;
    }

    public static boolean exists(String handle)
    throws DomainException {
        return __getChannelClass(handle) != null;
    }

    public static ChannelClass getChannelClass(String handle)
    throws DomainException {
        ChannelClass cc = __getChannelClass(handle);
        if ( cc == null) {
            String msg = "Channel class not found with handle: " + handle;
            throw new DomainException(msg);
        }
        return cc;
    }

    private static ChannelClass __getChannelClass(String handle)
    throws DomainException {
        ChannelClass cc = (ChannelClass) getChannelClassMap().get(handle);
        if (cc == null) {
            loadClasses();
            cc = (ChannelClass) getChannelClassMap().get(handle);
        }
        return cc;
    }

    public static Iterator getOrderedIterator(ChannelMode channelMode)
    throws DomainException {
        return getOrderedChannelClassMap(channelMode).values().iterator();
    }

    private static synchronized void loadClasses()
    throws DomainException {
        try {
            Map classes = null;
            Map orderedClasses = null;
            Document dom =
            ResourceLoader.getResourceAsDocument(ChannelClassFactory.class,
            "/properties/Channels.xml");
            // Utilities.
            Element cnl = null;
            ChannelClass obj = null;
            String handle = null;
            Integer orderedKey = null;
            NodeList nl = dom.getElementsByTagName("channel");
            for (int i = 0; i < nl.getLength(); i++) {
                cnl = (Element) nl.item(i);
                obj = new ChannelClass(cnl);
                handle = obj.getHandle();

                classes = (Map)channelClassesByMode.get(obj.getChannelMode());
                if (classes == null) {
                    classes = new HashMap();
                    channelClassesByMode.put(obj.getChannelMode(), classes);
                }
                orderedClasses =
                (Map)orderedChannelClassesByMode.get(obj.getChannelMode());
                if (orderedClasses == null) {
                    orderedClasses = new TreeMap();
                    orderedChannelClassesByMode.put(obj.getChannelMode(),
                    orderedClasses);
                }
                if (!classes.containsKey(handle)) {
                    classes.put(handle, obj);
                    orderedKey = new Integer(cnl.getAttribute("order"));
                    orderedClasses.put(orderedKey, obj);
                }
                channelClasses.put(handle, obj);
            }
        } catch (Exception e) {
        e.printStackTrace();
            StringBuffer msg  = new StringBuffer();
            msg.append("Method ChannelClassFactory.loadClasses ");
            msg.append("failed with the following message:\n");
            msg.append(e.getMessage());
            throw new DomainException(msg.toString());
        }
    }
}
