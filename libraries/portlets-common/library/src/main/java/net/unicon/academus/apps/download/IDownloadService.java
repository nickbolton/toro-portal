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

package net.unicon.academus.apps.download;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generic resource download service.
 * <p>
 * Client applications can register resources together with a name to allow the
 * downloading of a file from a servlet.
 * <p>
 * Client applications can explicitly unregister a resource by passing the
 * resource id as provided by the register method. This will remove all
 * knowledge of the resource and cause any attempts to download that resource
 * to fail.
 *
 * @author eandresen
 */
public interface IDownloadService {

   /**
    * Register a resource for downloading availability.
    *
    * @param name the resource's name
    * @param resource the resource contents
    * @return the resource identifier assigned to the registered resource
    *
    * @throws IllegalArgumentException if name or resource is null
    */
   String registerResource(String name, InputStream resource) throws IOException;

    /**
     * Unregister a resource, disabling its availability for download.
     *
     * @param id Resource identifier to unregister
     * @return true if the resource was not expired and was removed by this
     * call.
     */
   boolean unregisterResource(String id) throws IOException;

    /**
     * Retrieve the resource associated with the given identifier.
     *
     * @param id Resource identifier to resolve
     * @return NameResourcePair of the requested resource, or null if unknown or expired.
     */
   NameResourcePair getResource(String id) throws IOException;
   
    /**
     * Mapping of a name to a resource.
     */
   public interface NameResourcePair {
      
      /**
       * Get the resource contents.
       */
      public InputStream getResource() throws IOException;
      
      /**
       * Get the resource size in bytes.
       */
      public long getSize();
      
      /**
       * Get the resource's name.
       */
      public String getName();
   }

}