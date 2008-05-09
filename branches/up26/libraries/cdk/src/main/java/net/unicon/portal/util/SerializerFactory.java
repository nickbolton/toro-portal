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
package net.unicon.portal.util;

import java.io.Writer;

import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.serialize.BaseMarkupSerializer;

public class SerializerFactory {
	
    private static SerializerFactory __instance = null;
    private ContentMediaManager mediaManager = null;
    
    public synchronized static SerializerFactory instance() {
        if (__instance == null) {
            __instance = new SerializerFactory();
        }
        return __instance;
    }
    
    public BaseMarkupSerializer getSerializer(String serializerName, Writer out)
    throws PortalException { 
        if (serializerName == null) {
            throw new PortalException("No serializer name given.");
        }
        BaseMarkupSerializer serializer = mediaManager.getSerializerByName(serializerName, out);
        
        if (serializer == null) {
            throw new PortalException("No serializer available for name: " + serializerName);
        }
        return serializer;
    }
    
    private SerializerFactory() {
        mediaManager = new ContentMediaManager();
    }
}
