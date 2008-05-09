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
package net.unicon.sdk.cache.squid;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogLevel;
import net.unicon.sdk.log.LogServiceFactory;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.util.StringUtils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Squid based implementation of the Map interface. This implementation provides
 * all of the map operations excluding size, isEmpty, containsValue,
 * clear, keySet, and values, and permits null values but not the null
 * key.
 */
public class SquidMap implements Map {
    
    static String localUrl = null;
    static String originatingUrlHeaderName =
        "squid-map-originating-url";
    
    private static Hashtable tempMap = new Hashtable();
    private static final Map putHeaders = new HashMap();
    private static String squidUrl = null;
    private static boolean debug = false;
    private static boolean active = true;

    // this is a mapping of md5 keys to the actual key for debugging
    private static Map keyMap = null;

    private static ILogService logService = null;

    static {
        try {
            active = UniconPropertiesFactory.getManager(
                CommonPropertiesType.UTILS).getPropertyAsBoolean(
                    "net.unicon.sdk.cache.squid.SquidMap.active");
            debug = UniconPropertiesFactory.getManager(
                CommonPropertiesType.UTILS).getPropertyAsBoolean(
                    "net.unicon.sdk.cache.squid.SquidMap.debug");
            if (debug) {
                keyMap = new HashMap();
            }

            // The local url tells distributed servers how to get
            // back to the originating server.
            localUrl = UniconPropertiesFactory.getManager(
                CommonPropertiesType.UTILS).getProperty(
                    "net.unicon.sdk.cache.squid.SquidMap.localUrl");

            // initialize the put http headers
            putHeaders.put("cache-control","no-cache");

            squidUrl = UniconPropertiesFactory.getManager(
                CommonPropertiesType.UTILS).getProperty(
                    "net.unicon.sdk.cache.squid.SquidMap.squidUrl");
            logService = LogServiceFactory.instance();

            log(ILogService.DEBUG, "SquidMap : localUrl: " + localUrl);
            log(ILogService.DEBUG, "SquidMap : squidUrl: " + squidUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a SquidMap. The map may or may not be empty depending on the state of
     * the corresponding squid server.
     */    
    public SquidMap() {
    }

    /** Constructs an empty InstanceSquidMap. The initial capacity has no meaning with
     * this implementation of Map, but we must support it anyway.
     * @param initCapacity initCapacity Not supported
     */    
    public SquidMap(int initCapacity) {
        this();
    }

    /**
     * Method not supported.
     * @return
     */    
    public int size() {
        return -1;
    }

    /**
     * Method not supported.
     * @return
     */    
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     * @param key The key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key.
     */    
    public boolean containsKey(java.lang.Object key) {
        if (!active) return false;
        return getSquid(key) != null;
    }

    /**
     * Method not supported.
     * @param value
     * @return
     */    
    public boolean containsValue(java.lang.Object value) {
        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.
     * Returns null if the map contains no mapping for this key.
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or null
     * if the map contains no mapping for this key.
     */    
    public Object get(Object key) {
        if (!active) return null;
        return getSquid(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old value
     * is replaced.
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return null
     */    
    public Object put(Object key, Object value) {
        if (!active) return null;
        return putSquid(key, value, true);
    }

    /**
     * Removes the mapping for this key from this map if it is present.
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or null
     * if there was no mapping for key.
     */    
    public Object remove(Object key) {
        if (!active) return null;
        Object obj = getSquid(key);
        putSquid(key, null, false);
        return obj;
    }

    /**
     * Method not supported.
     * @param otherMap
     */    
    public void putAll(Map otherMap) {
    }

    /**
     * Method not supported.
     */
    public void clear() {
    }

    /**
     * Method not supported.
     * @return
     */    
    public Set keySet() {
        return new HashSet();
    }

    /**
     * Method not supported.
     * @return
     */    
    public Collection values() {
        return new ArrayList();
    }

    /**
     * Method not supported.
     * @return
     */    
    public Set entrySet() {
        return new HashSet();
    }

    private String constructUrl(Object key) {
        if (squidUrl == null) return null;
        return new StringBuffer().
            append(squidUrl).append('/').append(constructKey(key)).toString();
    }

    /**
     * Generate a proper key to be used to store/retrieve from squid.
     * @param key The clients key.
     * @return A well-formatted key to be used with squid.
     */    
    protected String constructKey(Object key) {
        return StringUtils.md5String(""+key);
    }

    private Object getSquid(Object key) {
        try {
            String url = constructUrl(key);
            log(ILogService.DEBUG, "GET", url, key);
            return getUrl(url, null);
        } catch (EOFException eofe) {
            // expected if a squid miss
            log(ILogService.DEBUG, "getSquid() : eof reached for", "", key);
        } catch (Exception e) {
            log(ILogService.ERROR, "getSquid() : exception occured, key: " + key, e);
        }

        return null;
    }

    private Object putSquid(Object key, Object value, boolean persist) {
        String sKey = constructKey(key);

        if (debug) {
            keyMap.put(sKey, key);
        }

        if (persist) {
            // store the value in the temporary map
            tempMap.put(sKey, value);
        }

        try {
            // call squid w/ the "no-cache" header
            // so that squid will call our servlet
            String url = constructUrl(key);
            log(ILogService.DEBUG, "PUT", url, key);
            getUrl(url, putHeaders);
        } catch (EOFException eofe) {
            // This is expected is we are not persisting
            log(ILogService.DEBUG, "putSquid() : eof reached for", "", key);
        } catch (Throwable e) {
            log(ILogService.ERROR, "putSquid() : exception occured, key: " + key, e);
        } finally {
            tempMap.remove(sKey);
        }
        return null;
    }

    private Object getUrl(String url, Map headers)
    throws Exception {
        if (url == null) {
            log(ILogService.ERROR, "No url specified!");
            return null;
        }

        try {
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(url);

            method.setRequestHeader(originatingUrlHeaderName, localUrl);
            applyHeaders(method, headers);
            client.executeMethod(method);
            Object responseBody = new ObjectInputStream(
                method.getResponseBodyAsStream()).readObject();
            method.releaseConnection();
            return responseBody;
        } catch (StreamCorruptedException sce) {
            // this usually means the backend servlet could not be accessed
            log(ILogService.ERROR, "StreamCorruptedException trying to access "+
                url);
        }
        return null;
    }

    private void applyHeaders(HttpMethod method, Map headers) {
        if (headers == null || method == null) return;

        String name;
        Iterator itr = headers.keySet().iterator();
        while (itr.hasNext()) {
            name = (String)itr.next();
            method.setRequestHeader(
                new Header(name, (String)headers.get(name)));
        }
    }

    Object getTemp(String key) {
        log(ILogService.DEBUG, "getTemp() : Map size: " + tempMap.size(),
            "", key);
        return tempMap.remove(key);
    }
    
    protected static void log(LogLevel level, String msg) {
        log(level, msg, "", "");
    }

    protected static void log(LogLevel level, String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        out.println(msg);
        t.printStackTrace(out);
        log(level, sw.toString(), "", "");
    }

    protected static void log(LogLevel level, String msg, String url, Object key) {
        if (logService == null) return;
        StringBuffer sb = new StringBuffer();
        sb.append("SquidMap ").append(msg).append(": ").append(url);
        sb.append("(key: ").append(key.toString()).append(")");
        if (debug) {
            sb.append(", ").append(keyMap.get(key) != null ?
                keyMap.get(key) : "no key mapping");
        }
        sb.append(")");
        logService.log(level, sb.toString());
    }
}
