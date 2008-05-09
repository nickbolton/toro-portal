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

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.guid.GuidException;
import net.unicon.sdk.guid.GuidServiceFactory;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.util.StringUtils;

/**
 * This implementation of SquidMap allows a client to treat it as a
 * distinct instance with all the keys belonging to a unique class.
 * Aunique global identifier (guid) will be created for each instance and all
 * keys used will have it prepended prior to accessing squid.
 */

public class InstanceSquidMap extends SquidMap {

    private String guid;
    
    private static int keyLength = -1;
    private static String classDelimiter;

    static {
        try {
            // get the class delimiter
            classDelimiter = UniconPropertiesFactory.getManager(
                CommonPropertiesType.UTILS).getProperty(
                "net.unicon.sdk.cache.squid.InstanceSquidMap.classDelimiter");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Constructs an empty InstanceSquidMap
     */    
    public InstanceSquidMap() {
        guid = generateGuid();
            
        if (keyLength < 0) {
            // Determine what the key length will be so it doesn't have
            // to be calculated each time -- 32 is the size of the
            // StringUtils.md5String result.
            keyLength = 32 + classDelimiter.length() + guid.length();
        }
    }

    /**
     * Constructs an empty InstanceSquidMap. The initial capacity has no
     * meaning with this implementation of Map, but we must support it anyway.
     * @param initCapacity Not supported
     */    
    public InstanceSquidMap(int initCapacity) {
        this();
    }
    
    
    /**
     * Removes all mappings from this map.
     */    
    public void clear() {
        // This has the effect of clearing out the Map since all new
        // key generations will be in a new class.  Squid will have
        // to be responsible for clearing out the old class.
        guid = generateGuid();
    }

    /**
     * Override the constructKey method to generate a key that belongs
     * to a class of keys.
     * @param key the key whose associated value is to be returned.
     * @return A key based on this object instance
     */
    protected String constructKey(Object key) {
        StringBuffer sb = new StringBuffer(keyLength);
        sb.append(guid).append(classDelimiter);
        sb.append(StringUtils.md5String(""+key));
        return sb.toString();
    }
    
    private String generateGuid() {
        try {
            // generate the guid that will uniquely identify this instance and
            // be used to classify the keys that belong to this instance.
            return GuidServiceFactory.getService().generate().getValue();
        } catch (GuidException ge) {
            log(ILogService.ERROR, "Failed generating GUID.", ge);
            throw new RuntimeException(ge);
        } catch (FactoryCreateException  fce) {
            log(ILogService.ERROR, "Failed to create GUID service.", fce);
            throw new RuntimeException(fce);
        } catch (RuntimeException re) {
            log(ILogService.ERROR, "Runtime exceotion generating GUID.", re);
            throw new RuntimeException(re);
        } catch (Throwable t) {
            log(ILogService.ERROR, "Error generating GUID.", t);
            throw new RuntimeException(t);
        }
    }
}
