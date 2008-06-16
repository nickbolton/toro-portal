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
package net.unicon.portal.common;
import java.util.Hashtable;
import java.util.StringTokenizer;

import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.domain.ChannelClass;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.MultithreadedCacheableChannelAdapter;
import org.jasig.portal.MultithreadedCacheableCharacterChannelAdapter;
import org.jasig.portal.MultithreadedCacheableMimeResponseChannelAdapter;
import org.jasig.portal.MultithreadedCacheableMimeResponseCharacterChannelAdapter;
import org.jasig.portal.MultithreadedChannelAdapter;
import org.jasig.portal.MultithreadedCharacterChannelAdapter;
import org.jasig.portal.MultithreadedMimeResponseChannelAdapter;
import org.jasig.portal.MultithreadedMimeResponseCharacterChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedCacheableChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedCacheableCharacterChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedCharacterChannelAdapter;
import org.jasig.portal.MultithreadedPrivilegedMimeResponseCharacterChannelAdapter;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
public class SubChannelFactory {
    public static final String subChannelIdSeparator = "_";
    protected static final Hashtable userChannelTables = new Hashtable();
    protected static final Hashtable multithreadedChannels = new Hashtable();
    protected SubChannelFactory() { }
    public static Hashtable getUserChannelTables() {
        return userChannelTables;
    }
    protected static Hashtable getChannelTable(String upId) {
        Hashtable channelTable = (Hashtable)userChannelTables.get(upId);
        if (channelTable == null) {
            channelTable = new Hashtable();
            userChannelTables.put(upId, channelTable);
        }
        return channelTable;
    }
    public static IChannel getChannelInstance(String upId, ChannelClass cc,
    ChannelStaticData staticData, IPerson person,
    IAuthorizationPrincipal principal, IUserPreferencesManager upm) throws Exception {
        Hashtable channelTable = getChannelTable(upId);
        IChannel ch = (IChannel)channelTable.get(getSubChannelUID(upId, cc));
        if (ch == null) {
            try {
                ch = instantiateChannel(upId, cc, staticData, person,principal, upm);
                channelTable.put(getSubChannelUID(upId, cc), ch);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return ch;
    }
    protected static IChannel instantiateChannel(String upId, ChannelClass cc,
    ChannelStaticData staticData, IPerson person,
    IAuthorizationPrincipal principal, IUserPreferencesManager upm) throws Exception {
        IChannel ch = null;
        boolean exists = false;
        String subId = getSubChannelSubscribeId(upId, cc);
        String className = cc.getClassName();
        Object cobj = multithreadedChannels.get(cc);
        if (cobj != null) {
            exists = true;
        } else {
            try {
                cobj =  Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                LogService.instance().log(
                LogService.ERROR,
                "SuperChannel::getChannelInstance() : " +
                "Failed to instantiate channel: " + className);
                return null;
            }
        }
        String uid = getSubChannelUID(upId, cc);
        // determine what kind of a channel it is.
        if (cobj instanceof IMultithreadedCharacterChannel) {
            if (cobj instanceof IMultithreadedCacheable) {
                if (cobj instanceof IMultithreadedPrivileged) {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedPrivilegedMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    } else {
                        // both cacheable and privileged
                        ch = new MultithreadedPrivilegedCacheableCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    }
                } else {
                    if (cobj instanceof IMultithreadedMimeResponse) {
                        ch = new MultithreadedCacheableMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    } else {
                        // just cacheable
                        ch = new MultithreadedCacheableCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                    }
                }
            } else if (cobj instanceof IMultithreadedPrivileged) {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedPrivilegedMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                } else {
                    ch = new MultithreadedPrivilegedCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                }
            } else {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    ch = new MultithreadedMimeResponseCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                } else {
                    // plain multithreaded
                    ch = new MultithreadedCharacterChannelAdapter((IMultithreadedCharacterChannel)cobj, uid);
                }
            }
            // see if we need to add the instance to the multithreaded table
            if (!exists) {
                multithreadedChannels.put(cc, cobj);
            }
        } else if (cobj instanceof IMultithreadedChannel) {
            if (cobj instanceof IMultithreadedCacheable) {
                if (cobj instanceof IMultithreadedPrivileged) {
                    // both cacheable and privileged
                    ch = new MultithreadedPrivilegedCacheableChannelAdapter(
                        (IMultithreadedChannel)cobj, uid);
                } else if (cobj instanceof IMultithreadedMimeResponse) {
                    // MimeResponse and cacheable
                    ch = new MultithreadedCacheableMimeResponseChannelAdapter(
                    (IMultithreadedChannel)cobj, uid);
                } else {
                    // just cacheable
                    ch = new MultithreadedCacheableChannelAdapter(
                    (IMultithreadedChannel)cobj, uid);
                }
            } else if (cobj instanceof IMultithreadedPrivileged) {
                ch = new MultithreadedPrivilegedChannelAdapter(
                (IMultithreadedChannel)cobj, uid);
            } else {
                if (cobj instanceof IMultithreadedMimeResponse) {
                    // MimeResponse and multithreaded
                    ch = new MultithreadedMimeResponseChannelAdapter(
                    (IMultithreadedChannel)cobj, uid);
                } else {
                    // plain multithreaded
                    ch = new MultithreadedChannelAdapter(
                    (IMultithreadedChannel)cobj, uid);
                }
            }
            // see if we need to add the instance to the multithreaded table
            if (!exists) {
                multithreadedChannels.put(cc, cobj);
            }
        } else {
            // vanilla IChannel
            ch = (IChannel)cobj;
        }
        // construct a ChannelStaticData object
        ChannelStaticData sd = new ChannelStaticData(null, upm.getUserLayoutManager());
        sd.setTimeout(staticData.getTimeout());
        // Set the Ids of the channel
        sd.setChannelSubscribeId(subId);
        sd.setChannelPublishId("");
        sd.setParameters(staticData);
        sd.setPerson(person);
        sd.setJNDIContext(staticData.getJNDIContext());
        sd.setParameter("subchannel", "true");
        sd.setSerializerName(staticData.getSerializerName());

        // register the user in the ChannelDataManager.
        // It gets cleaned up in the receiveEvent() method of the sub channel
        // NOTE: This needs to be called prior to setting the static data.
        // This is because some channels will register themselves
        // if they are not yet registered. (i.e. ones that can exist both
        // inside and outside the super channel)
        ChannelDataManager.registerChannelUser(person, principal, cc, uid);

        ch.setStaticData(sd);

        // if static data is not set in the CDM, set it now
        // non AMTC type channels may not do this.
        if (ChannelDataManager.getStaticData(uid) == null) {
            LogService.instance().log(LogService.INFO,
				"Setting static data for channel: " + cc.getClassName());
            ChannelDataManager.setStaticData(uid, sd);
        }

        return ch;
    }
    public static String convertUIDToSubscribeId(String upId) {
        // upId is a concatenation  of the session id, '/' character, and
        // the subscribeId. So strip off the session id
        return upId.substring(upId.indexOf("/") + 1);
    }
    public static String convertChannelHandleToSubUIDFromParent(
        String parentUID, String channelHandle) {
        return new StringBuffer(parentUID).
            append(subChannelIdSeparator).append(channelHandle).toString();
    }

    public static String convertChannelHandleToSubUID(String upId, String channelHandle) {
        // This takes the parent UID of the upId and
        // prepends it to the given channelHandle
        return convertChannelHandleToSubUIDFromParent(getParentUID(upId),
            channelHandle);
    }
    public static String getParentSubscribeId(String upId) {
        String parentId = getParentUID(upId);
        if (parentId == null) return null;
        return convertUIDToSubscribeId(parentId);
    }
    public static String getParentUID(String upId) {
        if (upId.indexOf(subChannelIdSeparator) < 0) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(upId, subChannelIdSeparator);
        StringBuffer parentSubId = new StringBuffer();
        String subIdComponent = null;
        while (st.hasMoreTokens()) {
            subIdComponent = st.nextToken();
            if (st.hasMoreTokens()) {
                if (parentSubId.length() > 0) {
                    parentSubId.append(subChannelIdSeparator);
                }
                parentSubId.append(subIdComponent);
            }
        }
        return parentSubId.toString();
    }
    public static String getSubChannelSubscribeId(String upId,
    ChannelClass cc) {
        return getSubChannelSubscribeId(upId, cc.getHandle());
    }
    public static String getSubChannelSubscribeId(String upId,
    String handle) {
        StringBuffer sb = new StringBuffer(convertUIDToSubscribeId(upId));
        sb.append(subChannelIdSeparator);
        sb.append(handle);
        return sb.toString();
    }

    // This method will return a unique identifier that we use
    // to uniquely identify a user with a given channel.
    public static String getSubChannelUID(String parentUID, ChannelClass cc) {
        return getSubChannelUID(parentUID,
        getSubChannelSubscribeId(parentUID, cc));
    }
    public static String getSubChannelUID(String parentUID,
    String subscribeId) {
        return new StringBuffer(100).append(getSessionId(parentUID)).
        append("/").append(subscribeId).toString();
    }
    public static String getSessionId(String upId) {
        return upId.substring(0, upId.indexOf("/"));
    }
}
