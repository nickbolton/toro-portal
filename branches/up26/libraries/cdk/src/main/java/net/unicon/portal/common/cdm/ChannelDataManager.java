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
package net.unicon.portal.common.cdm;

import java.lang.reflect.Method;

import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.*;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.domain.ChannelClass;
import org.jasig.portal.IServant;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.services.LogService;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Document;
import net.unicon.portal.common.ChannelState;
import net.unicon.sdk.properties.*;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.util.ChannelDefinitionUtil;

/**
 * This class manages the storage and retrieval of <code>ChannelData</code>.
 * The rendering object (SuperChannel) registers channel instances with
 * a unique id. This id is then passed to the sub channels via runtime data
 * with the SuperChannel.channelUidKey which the sub channels can then use as the upId parameter to retrieve channel data.
 */
public class ChannelDataManager {

    // This maps users to UIDs
    protected static final Hashtable uidTable = new Hashtable();
    // Maps UIDs to app ids so the proper app is invoked by a producer
    protected static final Hashtable uidAppIdTable = new Hashtable();

    protected static final Hashtable channelData = new Hashtable();
    protected static final List uidList = new ArrayList();
    protected static final String uidKey = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("org.jasig.portal.security.uidKey");
    /**
     * Returns a table of all the UIDs in the system
     * @return <{Hashtable}>
     */
    public static Hashtable getUIDTable() {
        return uidTable;
    }
    /**
     * Returns a map of channel UIDs for the user
     * @param user
     * @return <{Map}>
     */
    public static Map getChannelUIDs(User user) {
        return getChannelUIDs(user.getUsername());
    }
    /**
     * Returns a map of channel UIDs for the user
     * @param username
     * @return <{Map}>
     */
    public static Map getChannelUIDs(String username) {
        Map channelUIDs = (Map)uidTable.get(username);
        if (channelUIDs == null) return new HashMap();
        return channelUIDs;
    }
    /**
     * Checks for the existence of UID.
     * @param upId the channel's unique ID
     * @return boolean
     */
    public static boolean uidExists(String upId) {
        return uidList.contains(upId);
    }
    public static void removeData(String upId) {
//System.out.println("JONI-LOVES-CHACHI : Remove Channel: " + upId);
    // uh, let's blow away this before we remove channel data --KG
        try {
            IProducer producer = getProducer(upId);
            if (producer != null) {
                producer.deallocateAppConnection(getApplicationId(upId));
            }
        } catch (ContentProducerException cpe) {
            LogService.instance().log(LogService.ERROR, cpe);
        }

        removeChannelUser(upId);
        channelData.remove(upId);
    }
    /**
     * Retrieves a channels generic attribute. This can be any Object type.
     * @param upId the channel's unique ID
     * @param key the key used to retrieve the attribute.
     * @return <{Object}>
     */
    public static Object getAttribute(String upId, Object key) {
        return getChannelData(upId).getAttribute(key);
    }
    /**
     * Stores a channels generic attribute. This can be any Object type.
     * @param upId the channel's unique ID
     * @param key the key used to retrieve the attribute.
     * @param value the generic value to store.
     */
    public static void putAttribute(String upId, Object key, Object value) {
        getChannelData(upId).putAttribute(key, value);
    }
    /**
     * Removes a channels generic attribute. This can be any Object type.
     * @param upId the channel's unique ID
     * @param key the key used to retrieve the attribute.
     */
    public static void removeAttribute(String upId, Object key) {
        getChannelData(upId).removeAttribute(key);
    }
    /**
     * Queries if the channel identified by upId is dirty.
     * @param upId the channel's unique ID
     * @return boolean
     */
    public static boolean isDirty(String upId) {
        return getChannelData(upId).isDirty();
    }
    /**
     * Sets the dirty state of the channel identified by the upId
     * @param upId the channel's unique ID
     * @param dirty the channel's dirty value
     */
    public static void setDirty(String upId, boolean dirty) {
        getChannelData(upId).setDirty(dirty);
	LogService.instance().log(LogService.INFO, "In CDM.setDirty for upId " + upId);

        // Only set the parent dirty if setting this channel dirty
        // Clearing a dirty state might conflict with another channel
        // if they are trying to set the parent dirty.
        if (dirty) {
            setParentDirty(upId, dirty);
        }
    }
    /**
     * Sets the dirty state of the channel's parent channel
     * @param upId the channel's unique ID
     * @param dirty the parent's channel's dirty value
     */
    public static void setParentDirty(String upId, boolean dirty) {
        String parentId = SubChannelFactory.getParentUID(upId);
        if (parentId == null) return;
        setDirty(parentId, dirty);
    }
    /**
     * Gets the xml for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static String getXML(String upId) {
        return getChannelData(upId).getXML();
    }
    /**
     * Sets the xml for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param xml the channel's xml
     */
    public static void setXML(String upId, String xml) {
        getChannelData(upId).setXML(xml);
    }
    /**
     * Gets the error message for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static String getErrorMsg(String upId) {
        return getChannelData(upId).getErrorMsg();
    }
    /**
     * Sets the error message for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param errorMsg the channel's error message
     */
    public static void setErrorMsg(String upId, String errorMsg) {
        getChannelData(upId).setErrorMsg(errorMsg);
    }
    /**
     * Gets the <code>Document</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{Document}>
     */
    public static Document getDocument(String upId) {
        return getChannelData(upId).getDocument();
    }
    /**
     * Sets the <code>Document</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param document the channel's Document
     */
    public static void setDocument(String upId, Document document) {
        getChannelData(upId).setDocument(document);
    }
    /**
     * Gets the character buffer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static String getCharacterBuffer(String upId) {
        return getChannelData(upId).getCharacterBuffer();
    }
    /**
     * Sets the character buffer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param charBuffer the channel's character buffer
     */
    public static void setCharacterBuffer(String upId,
    String charBuffer) {
        getChannelData(upId).setCharacterBuffer(charBuffer);
    }
    /**
     * Gets the sax buffer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{SAX2BufferImpl}>
     */
    public static SAX2BufferImpl getSAXBuffer(String upId) {
        return getChannelData(upId).getSAXBuffer();
    }
    /**
     * Sets the sax buffer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param saxBuffer the channel's sax buffer
     */
    public static void setSAXBuffer(String upId, SAX2BufferImpl saxBuffer) {
        getChannelData(upId).setSAXBuffer(saxBuffer);
    }
    /**
     * Gets the ssl location for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static String getSSLLocation(String upId) {
        return getChannelData(upId).getSSLLocation();
    }
    /**
     * Sets the ssl location for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param sslLocation the channel's ssl location
     */
    public static void setSSLLocation(String upId, String sslLocation) {
        getChannelData(upId).setSSLLocation(sslLocation);
    }
    /**
     * Gets the sheet name for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static String getSheetName(String upId) {
        return getChannelData(upId).getSheetName();
    }
    /**
     * Sets the sheet name for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param sheetName the channel's sheet name
     */
    public static void setSheetName(String upId, String sheetName) {
        getChannelData(upId).setSheetName(sheetName);
        if (sheetName != null) {
            getXSLParameters(upId).put("current_command", sheetName);
        }
    }
    /**
     * Gets the xsl parameters table for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{Hashtable}>
     */
    public static Hashtable getXSLParameters(String upId) {
        return getChannelData(upId).getXSLParameters();
    }
    /**
     * Sets the xsl parameters table for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param params the channel's xsl parameters table
     */
    public static void setXSLParameters(String upId, Hashtable params) {
        getChannelData(upId).setXSLParameters(params);
    }
    /**
     * Gets the <code>IPerson</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{IPerson}>
     */
    public static IPerson getUPortalUser(String upId) {
        return getChannelData(upId).getUPortalUser();
    }
    /**
     * Sets the <code>IPerson</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param uportalUser the channel's <code>IPerson</code>
     */
    public static void setUPortalUser(String upId, IPerson uportalUser) {
        getChannelData(upId).setUPortalUser(uportalUser);
    }
    /**
     * Gets the domain <code>User</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{User}>
     */
    public static User getDomainUser(String upId) {
        return getChannelData(upId).getDomainUser();
    }
    /**
     * Sets the domain <code>User</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param domainUser the channel's domain <code>User</code>
     */
    public static void setDomainUser(String upId, User domainUser) {
        getChannelData(upId).setDomainUser(domainUser);
    }
    /**
     * Gets the ChannelClass for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static ChannelClass getChannelClass(String upId) {
        return getChannelData(upId).getChannelClass();
    }
    /**
     * Sets the ChannelClass for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param channelClass the channel's ChannelClass object
     */
    public static void setChannelClass(String upId,
        ChannelClass channelClass ) {
        getChannelData(upId).setChannelClass(channelClass);
    }
    /**
     * Gets the ChannelDefinition for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{String}>
     */
    public static ChannelDefinition getChannelDefinition(String upId) {
        return getChannelData(upId).getChannelDefinition();
    }
    /**
     * Sets the ChannelDefinition for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param channelClass the channel's ChannelDefinition object
     */
    public static void setChannelDefinition(String upId,
        ChannelDefinition channelDefinition ) {
        getChannelData(upId).setChannelDefinition(channelDefinition);
    }
    /**
     * Gets the <code>ChannelStaticData</code> for the channel identified by the upId.
     * @param upId the channel's unique ID
     * @return <{ChannelStaticData}>
     */
    public static ChannelStaticData getStaticData(String upId) {
        return getChannelData(upId).getStaticData();
    }
    /**
     * Sets the <code>ChannelStaticData</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param sd the channel's static data object
     */
    public static void setStaticData(String upId, ChannelStaticData sd) {
        getChannelData(upId).setStaticData(sd);
    }
    /**
     * Gets the <code>ChannelRuntimeData</code> for the channel identified by the upId.
     * @param upId the channel's unique ID
     * @return <{ChannelRuntimeData}>
     */
    public static ChannelRuntimeData getRuntimeData(String upId) {
        return getChannelData(upId).getRuntimeData();
    }
    /**
     * Sets the <code>ChannelRuntimeData</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param sd the channel's runtime data object
     */
    public static void setRuntimeData(String upId, ChannelRuntimeData rd) {
        getChannelData(upId).setRuntimeData(rd);
    }
    /**
     * Gets an <code>IPermissions</code> for the channel identified by the upId.
     * @param upId the channel's unique ID
     * @return <{IPermissions}>
     */
    public static IPermissions getPermissions(String upId) {
        return getChannelData(upId).getPermissions();
    }
    /**
     * Sets the <code>IPermissions</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param p the channel's permissions object
     */
    public static void setPermissions(String upId, IPermissions p) {
        getChannelData(upId).setPermissions(p);
    }
    /**
     * Returns whether the user's context has changed since the last rendering.
     * @param upId the channel's unique ID
     * @return boolean
     */
    public static boolean isContextChanged(String upId) {
        return getChannelData(upId).isContextChanged();
    }
    /**
     * Sets whether the user's context has changed.
     * @param upId the channel's unique ID
     * @param b the context changed state
     */
    public static void setContextChanged(String upId, boolean b) {
        getChannelData(upId).setContextChanged(b);
    }
    /**
     * Gets the <code>ChannelState</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{net.unicon.portal.common.ChannelState}>
     */
    public static ChannelState getCurrentState(String upId) {
        return getChannelData(upId).getCurrentState();
    }
    /**
     * Sets the <code>ChannelState</code> for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param currentState the channel's current state
     */
    public static void setCurrentState(String upId, ChannelState currentState) {
        getChannelData(upId).setCurrentState(currentState);
    }

    /**
     * Gets the appId for a given upId
     * @param upId the channel's unique ID
     * @return String appId
     */
    public static String getApplicationId(String upId) {
    return (String)uidAppIdTable.get(upId);
    }

    /**
     * Sets the applicationId for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param appId the channel's appId it is connected to
     */
    public static void setApplicationId(String upId, String appId) {
        uidAppIdTable.put(upId, appId);
    }

    /**
     * Gets the producer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @return <{net.unicon.portal.common.ChannelState}>
     */
    public static IProducer getProducer(String upId) {
        return getChannelData(upId).getProducer();
    }

    /**
     * Sets the producer for the channel identified by the upId
     * @param upId the channel's unique ID
     * @param currentState the channel's current state
     */
    public static void setProducer(String upId, IProducer producer) {
        getChannelData(upId).setProducer(producer);
    }

    /**
     * Assigns a servant for this channel.
     * @param upId the channel's unique ID
     * @param servant an <code>ISevant</code> to be assigned to this channel.
     */
    public static void setServant(String upId, IServant servant) {
        getChannelData(upId).setServant(servant);
    }

    /**
     * Gets the servant that's currently assigned to this channel.
     * @param upId the channel's unique ID
     * @return <{IServant}>
     */
    public static IServant getServant(String upId) {
        return getChannelData(upId).getServant();
    }

    /**
     * Assigns an authorization principal for this channel.
     * @param upId the channel's unique ID
     * @param principal an <code>IAuthorizationPrincipal</code> to be
     * assigned to this channel.
     */
    public static void setAuthorizationPrincipal(String upId,
        IAuthorizationPrincipal principal) {
        getChannelData(upId).setAuthorizationPrincipal(principal);
    }

    /**
     * Gets the authorization principal that's currently assigned
     * to this channel.
     * @param upId the channel's unique ID
     * @return <{IAuthorizationPrincipal}>
     */
    public static IAuthorizationPrincipal getAuthorizationPrincipal(
        String upId) {
        return getChannelData(upId).getAuthorizationPrincipal();
    }

    /** 
     * Assigns a portal control strcutures object for this channel. 
     * @param upId the channel's unique ID 
     * @param pcs a <code>PortalControlStructures</code> object to be 
     * assigned to this channel. 
     */ 
    public static void setPortalControlStructures(String upId, 
        PortalControlStructures pcs) { 
        getChannelData(upId).setPortalControlStructures(pcs); 
    } 
    
    /** 
     * Gets the portal control structures object that's currently assigned 
     * to this channel. 
     * @param upId the channel's unique ID 
     * @return <{PortalControlStructures}> 
     */ 
    public static PortalControlStructures getPortalControlStructures( 
        String upId) { 
        return getChannelData(upId).getPortalControlStructures(); 
    } 

    /**
     * Checks for the existence of a <code>ChannelData</code> registered
     * with the given ID.
     * @param upId the channel's unique ID
     * @return boolean
     */
    public static boolean hasChannelData(String upId) {
        return channelData.get(upId) != null;
    }

    /**
     * Gets the <code>ChannelData</code> object for the channel identified
     * by the upId
     * @param upId the channel's unique ID
     * @return <{net.unicon.portal.common.cdm.ChannelData}>
     */
    public static ChannelData getChannelData(String upId) {
        synchronized (upId) {
            ChannelData cd = (ChannelData)channelData.get(upId);
            if (cd == null) {
                cd = new ChannelData();
                channelData.put(upId, cd);
            }
            return cd;
        }
    }
    /**
     * Registers the channel with given  and <code>IPerson</code> with the given upId.
     * @param person the user the channel belongs to
     * @param channelClass the channel's ChannelClass object
     * @param upId the channel unique identifier
     */
    public static void registerChannelUser(IPerson person,
        IAuthorizationPrincipal principal, ChannelDefinition channelDefinition,
        String upId)
    throws PortalException {
        setChannelDefinition(upId, channelDefinition);
//System.out.println("JONI-LOVES-CHACHI : Register Channel: " + upId);
        registerChannelUser(person, principal, null, upId,
             getProducerInstance(channelDefinition));
    }

    /**
     * Registers the channel with given  and <code>IPerson</code> with the given upId.
     * @param person the user the channel belongs to
     * @param channelClass the channel's ChannelClass object
     * @param upId the channel unique identifier
     */
    public static void registerChannelUser(IPerson person,
        IAuthorizationPrincipal principal, ChannelClass channelClass,
        String upId)
    throws PortalException {
        setChannelClass(upId, channelClass);
        registerChannelUser(person, principal, channelClass, upId,    
            getProducerInstance(channelClass));
    }

    /**
     * Registers the channel with given  and <code>IPerson</code> with the given upId.
     * @param person the user the channel belongs to
     * @param principal the authorization principal the channel belongs to
     * @param channelClass the channel's ChannelClass object
     * @param upId the channel unique identifier
     */
    private static void registerChannelUser(IPerson person,
        IAuthorizationPrincipal principal, ChannelClass channelClass,
        String upId, IProducer producer)
    throws PortalException {

        User user = null;
        try {
            String username = (String) person.getAttribute(uidKey);
            user = UserFactory.getUser(username);
        } catch (IllegalArgumentException e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new PortalException(e);
        } catch (ItemNotFoundException e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new PortalException(e);
        } catch (OperationFailedException e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new PortalException(e);
        }
        setUPortalUser(upId, person);
        setAuthorizationPrincipal(upId, principal);
        setDomainUser(upId, user);
        Map userChannelUIDs = (Map)uidTable.get(user.getUsername());
        if (userChannelUIDs == null) {
            userChannelUIDs = new HashMap();
            uidTable.put(user.getUsername(), userChannelUIDs);
        }
        // Only register the channel->uid mapping for sub channels.
        if (channelClass != null) {
            if (!userChannelUIDs.containsKey(channelClass)) {
                userChannelUIDs.put(channelClass, upId);
            }
        }

        // add the upId 
        if (!uidList.contains(upId)) {
            synchronized (uidList) {
                uidList.add(upId);
            }
        }

        // register the producer
        setProducer(upId, producer);
    }

    static void removeChannelUser(String upId) {
        User user = getDomainUser(upId);
        if (user == null) {
            LogService.instance().log(LogService.ERROR, 
                new StringBuffer("ChannelDataManager.removeChannelUser(upId) failed, unable to find user with upId : ").append(upId).append("Session may not be available or channel is already removed.").toString() );
        } else {
            // From the upId we were able to get a user name.        
            Map userChannelUIDs = (Map)uidTable.get(user.getUsername());
            if (userChannelUIDs != null) {
                userChannelUIDs.remove(getChannelClass(upId));
                if (userChannelUIDs.size() == 0) {
                    uidTable.remove(user.getUsername());
                }
            }
        }
        
        synchronized (uidList) {
            uidList.remove(upId);
        }
    }

    private static IProducer getProducerInstance(ChannelClass cc)
    throws PortalException {
        return getProducerInstance(cc.getProducer());
    }

    private static IProducer getProducerInstance(ChannelDefinition cd)
    throws PortalException {
        try {
            String producerClass =
                ChannelDefinitionUtil.getParameter(cd, "producer");
            if (producerClass == null) return null;
            return getProducerInstance(producerClass);
        } catch (PortalException pe) {
            LogService.instance().log(LogService.ERROR, pe);
            throw pe;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new PortalException(e);
        }
    }

    private static IProducer getProducerInstance(String className)
    throws PortalException {
        IProducer producer = null;
        try {
            if (className == null || "".equals(className.trim())) return null;

            // call the singleton getInstance method
            Class factoryClass = Class.forName(className);
            Method m = factoryClass.getMethod("getInstance", null);
            // should be a static method
            producer = (IProducer)m.invoke(null, null);
        } catch (Exception e) {
e.printStackTrace();
            throw new PortalException(e);
        } 
        return producer;
    }

    protected ChannelDataManager() { }
}
