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
package net.unicon.portal.cscr;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrBrowserRegistry;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.services.LogService;

import com.oreilly.servlet.multipart.Part;

/**
 * @author nbolton
 *
 * This is a wrapper for the uPortal <code>ChannelRuntimeData</code> object that
 * integrates CSCR semantics for action url determination.
 */
public final class CscrChannelRuntimeData extends ChannelRuntimeData implements ICscrConstants {

    private ChannelRuntimeData channelRuntimeData;
    private String channelPublishId;
    private static final boolean useCSCR =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
        getPropertyAsBoolean("net.unicon.portal.cscr.useCSCR");;
    
    public CscrChannelRuntimeData(String channelPublishId, ChannelRuntimeData crd) {
        this.channelRuntimeData = (ChannelRuntimeData)crd.clone();
        this.channelPublishId = channelPublishId;
        if (checkCSCRStatus(channelPublishId)) {
            try {
                UPFileSpec up = crd.getUPFile(); 
                up.setMethod(UPFileSpec.WORKER_METHOD);
                up.setMethodNodeId(CSCR_METHOD_ID);
                channelRuntimeData.setUPFile(up);
            } catch (PortalException pe) {
                LogService.log(LogService.ERROR, pe);
            }    
        }
    }
    
    private boolean checkCSCRStatus(String publishId) {
        boolean enabled = false;
        
        // obey the global property setting and/or cscr browser registry
        if (!useCSCR || !CscrBrowserRegistry.instance().isEnabled(getBrowserInfo())) {
            return false;
        }
        
        // next see if the channel has a "cscrEnabled" attribute published
        try {
            ChannelDefinition channelDef = ChannelRegistryStoreFactory.
                getChannelRegistryStoreImpl().getChannelDefinition(
                    Integer.parseInt(publishId));
            ChannelParameter[] params = channelDef.getParameters();
            String name = null;
            String value = null;
            for (int i=0; i<params.length; i++) {
                name = params[i].getName();
                value = params[i].getValue();
                if ("cscrEnabled".equalsIgnoreCase(name)) {
                    enabled = new Boolean(value).booleanValue();
                }
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
        return enabled;
    }
    
    public String getChannelPublishId() {
        return channelPublishId;
    }

    public Object clone() {
        ChannelRuntimeData crd = (ChannelRuntimeData)channelRuntimeData.clone();
        return (Object)new CscrChannelRuntimeData(new String(channelPublishId), crd);
    }

    /**
     * Set a UPFileSpec which will be used to produce
     * baseActionURL and workerActionURL.
     * @param upfs the UPFileSpec
     */
    public void setUPFile(UPFileSpec upfs) {
        channelRuntimeData.setUPFile(upfs);
    }

    /**
     * Get the UPFileSpec
     * @return channelUPFile the UPFileSpec
     */
    public UPFileSpec getUPFile() {
        return channelRuntimeData.getUPFile();
    }

    /**
     * Set the HTTP Reqeust method.
     *
     * @param method a <code>String</code> value
     */
    public void setHttpRequestMethod(String method) {
        channelRuntimeData.setHttpRequestMethod(method);
    }

    /**
     * Get HTTP request method (i.e. GET, POST)
     *
     * @return a <code>String</code> value
     */
    public String getHttpRequestMethod() {
        return channelRuntimeData.getHttpRequestMethod();
    }

    /**
     * Sets the base action URL.  This was added back in for the benefit
     * of web services.  Not sure if it is going to stay this way.
     * @param baseActionURL the base action URL
     */
    public void setBaseActionURL(String baseActionURL) {
        channelRuntimeData.setBaseActionURL(baseActionURL);
    }

    /**
     * Sets whether or not the channel is rendering as the root of the layout.
     * @param rar <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public void setRenderingAsRoot(boolean rar) {
        channelRuntimeData.setRenderingAsRoot(rar);
    }

    /**
     * Sets whether or not the channel is currently targeted.  A channel is targeted
     * if an incoming request specifies the channel's subscribe ID as the targeted node ID.
     * @param targeted <code>true</code> if channel is targeted, otherwise <code>false</code>
     */
    public void setTargeted(boolean targeted) {
        channelRuntimeData.setTargeted(targeted);
    }

    /**
     * Setter method for browser info object.
     *
     * @param bi a browser info associated with the current request
     */
    public void setBrowserInfo(BrowserInfo bi) {
        channelRuntimeData.setBrowserInfo(bi);
    }

    /**
     * Provides information about a user-agent associated with the current request/response.
     *
     * @return a <code>BrowserInfo</code> object ecapsulating various user-agent information.
     */
    public BrowserInfo getBrowserInfo() {
        return channelRuntimeData.getBrowserInfo();
    }

    /**
     * Setter method for array of locales. A channel should
     * make an effort to render itself according to the
     * order of the locales in this array.
     * @param locales an ordered list of locales
     */
    public void setLocales(Locale[] locales) {
        channelRuntimeData.setLocales(locales);
    }

    /**
     * Accessor method for ordered set of locales.
     * @return locales an ordered list of locales
     */
    public Locale[] getLocales() {
        return channelRuntimeData.getLocales();
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The values in the Map must be object arrays. If (name, value[]) is in
     * the Map, then a future call to getParameter(name) will return value[0].
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParameters(Map params) {
        channelRuntimeData.setParameters(params);
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The Map should contain name-value pairs.  The name should be a String
     * and the value should be either a String or a Part.
     * If (name, value) is in the Map then a future call to getParameter(name)
     * will return value.
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParametersSingleValued(Map params) {
        channelRuntimeData.setParametersSingleValued(params);
    }

    /**
     * Sets multi-valued parameter.
     *
     * @param pName parameter name
     * @param values an array of parameter values
     * @return an array of parameter values
     */
    public String[] setParameterValues(String pName, String[] values) {
        return channelRuntimeData.setParameterValues(pName, values);
    }

    /**
     * Establish a parameter name-value pair.
     *
     * @param pName parameter name
     * @param value parameter value
     */
    public void setParameter(String pName, String value) {
        channelRuntimeData.setParameter(pName, value);
    }

    public com.oreilly.servlet.multipart.Part[] setParameterValues(
            String pName, com.oreilly.servlet.multipart.Part[] values) {
        return channelRuntimeData.setParameterValues(pName, values);
    }

    public void setParameter(String key, Part value) {
        channelRuntimeData.setParameter(key, value);
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL() {
        return channelRuntimeData.getBaseActionURL();
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @param idempotent a <code>boolean</code> value specifying if a given URL should be idepotent.
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL(boolean idempotent) {
        return channelRuntimeData.getBaseActionURL(idempotent);
    }

    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @return URL to invoke the worker.
     */
    public String getBaseWorkerURL(String worker) {
        return channelRuntimeData.getBaseWorkerURL(worker);
    }

    /**
     Returns a media base appropriate for web-visible resources used by and
     deployed with the passed in object. If the class of the passed in
     object was loaded from a CAR then a URL appropriate for accessing
     images in CARs is returned. Otherwise, a URL to the base media
     in the web application's document root is returned.
     */
    public String getBaseMediaURL(Object aChannelObject) throws PortalException {
        return channelRuntimeData.getBaseMediaURL(aChannelObject);
    }

    /**
     Returns a media base appropriate for web-visible resources used by and
     deployed with the passed in class. If the class of the passed in
     object was loaded from a CAR then a URL appropriate for accessing
     images in CARs is returned. Otherwise, a URL to the base media
     in the web application's document root is returned.
     */
    public String getBaseMediaURL(Class aChannelClass) throws PortalException {
        return channelRuntimeData.getBaseMediaURL(aChannelClass);
    }

    /**
     Returns a media base appropriate for the resource path passed in. The
     resource path is the path to the resource within its channel archive.
     (See org.jasig.portal.car.CarResources class for more information.)
     If the passed in resourcePath matches that of a resource loaded from
     CARs then this method returns a URL appropriate to obtain CAR
     deployed, web-visible resources. Otherwise it returns a URL to the
     traditional media path under the uPortal web application's document
     root.
     */
    public String getBaseMediaURL(String resourcePath) throws PortalException {
        return channelRuntimeData.getBaseMediaURL(resourcePath);
    }

    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @param idempotent a <code>boolean</code> value sepcifying if a URL should be idempotent
     * @return URL to invoke the worker.
     * @exception PortalException if an error occurs
     */
    public String getBaseWorkerURL(String worker, boolean idempotent)
            throws PortalException {
        return channelRuntimeData.getBaseWorkerURL(worker, idempotent);
    }

    /**
     * Tells whether or not the channel is rendering as the root of the layout.
     * @return <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public boolean isRenderingAsRoot() {
        return channelRuntimeData.isRenderingAsRoot();
    }

    /**
     * Tells whether or not the channel is currently targeted.  A channel is targeted
     * if an incoming request specifies the channel's subscribe ID as the targeted node ID.
     * @return <code>true</code> if channel is targeted, otherwise <code>false</code>
     */
    public boolean isTargeted() {
        return channelRuntimeData.isTargeted();
    }

    /**
     * Get a parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public String getParameter(String pName) {
        return channelRuntimeData.getParameter(pName);
    }

    /**
     * Obtain an <code>Object</code> parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public Object getObjectParameter(String pName) {
        return channelRuntimeData.getObjectParameter(pName);
    }

    /**
     * Obtain all values for a given parameter.
     *
     * @param pName parameter name
     * @return an array of parameter string values
     */
    public String[] getParameterValues(String pName) {
        return channelRuntimeData.getParameterValues(pName);
    }

    /**
     * Obtain all values for a given parameter as <code>Object</code>s.
     *
     * @param pName parameter name
     * @return a vector of parameter <code>Object[]</code> values
     */
    public Object[] getObjectParameterValues(String pName) {
        return channelRuntimeData.getObjectParameterValues(pName);
    }

    /**
     * Get an enumeration of parameter names.
     *
     * @return an <code>Enumeration</code> of parameter names.
     */
    public Enumeration getParameterNames() {
        return channelRuntimeData.getParameterNames();
    }

    /**
     * Get the parameters as a Map
     * @return a Map of parameter name-value pairs
     */
    public Map getParameters() {
        return channelRuntimeData.getParameters();
    }

    /**
     * Sets the keywords
     * @param keywords a String of keywords
     */
    public void setKeywords(String keywords) {
        channelRuntimeData.setKeywords(keywords);
    }

    /**
     * Returns the keywords
     * @return a String of keywords, null if there were none
     */
    public String getKeywords() {
        return channelRuntimeData.getKeywords();
    }
    
    
    /** Hashtable method wrappers **/
    
    public int size() {
        return channelRuntimeData.size();
    }

    public boolean isEmpty() {
        return channelRuntimeData.isEmpty();
    }

    public Enumeration<String> keys() {
        return channelRuntimeData.keys();
    }

    public Enumeration<Object> elements() {
        return channelRuntimeData.elements();
    }

    public boolean contains(Object value) {
        return channelRuntimeData.contains(value);
    }

    public boolean containsValue(Object value) {
        return channelRuntimeData.containsValue(value);
    }
    
    public boolean containsKey(String key) {
        return channelRuntimeData.containsKey(key);
    }
    
    public Object get(Object key) {
        return channelRuntimeData.get((String)key);
    }

    public Object get(String key) {
        return channelRuntimeData.get(key);
    }

    public Object put(String key, Object value) {
        return channelRuntimeData.put(key, value);
    }

    public Object remove(String key) {
        return channelRuntimeData.remove(key);
    }

    public void putAll(Map t) {
        channelRuntimeData.putAll(t);
    }

    public void clear() {
        channelRuntimeData.clear();
    }

    public String toString() {
        return channelRuntimeData.toString();
    }
    
    public Set<String> keySet() {
        return channelRuntimeData.keySet();
    }

    public Set entrySet() {
        return channelRuntimeData.entrySet();
    }

    public Collection<Object> values() {
        return channelRuntimeData.values();
    }

    public boolean equals(Object o) {
        if (o instanceof CscrChannelRuntimeData) {
            CscrChannelRuntimeData ccrd = (CscrChannelRuntimeData)o;
            return this.channelPublishId.equals(ccrd.getChannelPublishId()) &&
            	channelRuntimeData.equals(o);
        }
        return false;
    }

    public int hashCode() {
        return channelRuntimeData.hashCode();
    }
}
