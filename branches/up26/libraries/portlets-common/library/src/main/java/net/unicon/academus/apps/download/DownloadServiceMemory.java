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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.unicon.alchemist.encrypt.Digest;

/**
 * Generic resource download service.
 * <p>
 * Client applications can register resources together with a name to allow the
 * downloading of a file from a servlet.
 * <p>
 * The registered resource can be given a Time-to-Live to specify how long the
 * service should make the file accessible. This value can be 0 to specify that
 * it should be available indefinitely.
 * <p>
 * The registered inputstreams are held until first use, at which time they are
 * buffered and the InputStream released. This reduces memory consumption at
 * the cost of potential file handles.
 * <p>
 * Client applications can explicitly unregister a resource by passing the
 * resource id as provided by the register method. This will remove all
 * knowledge of the resource and cause any attempts to download that resource
 * to fail.
 *
 * @author eandresen
 */
public class DownloadServiceMemory implements IDownloadService {

    /** Default Time-to-Live for resources. */
    public static final int DEFAULT_TTL = 
       DownloadServiceFactory.getPropertyAsInt(DownloadServiceMemory.class, "default_ttl");

    /*
     * Implementation.
     */

    private final Map resourceMap;
    private long id;

    /**
     * Public constructor. Initializes the resource map.
     */
    public DownloadServiceMemory() {
        this.resourceMap = Collections.synchronizedMap(new HashMap());
        this.id = 0;
    }

    /**
     * Register a resource for downloading availability with the default TTL.
     *
     * This method calls {@link #registerResource(String,InputStream,long)}.
     *
     * @param name the resource's name
     * @param resource the resource contents
     * @return the resource identifier assigned to the registered resource
     *
     * @throws IllegalArgumentException if name or resource is null
     * @see #registerResource(String,InputStream,long)
     * @see #DEFAULT_TTL
     */
    public String registerResource(String name, InputStream resource) {
        return registerResource(name, resource, DEFAULT_TTL);
    }

    /**
     * Register a resource for downloading availability.
     *
     * @param name the resource's name
     * @param resource the resource contents
     * @param ttl the Time-to-Live of the resource, a positive integer
     * represents the time to live in seconds, and a 0 means the resource is
     * eternal.
     * @return the resource identifier assigned to the registered resource
     *
     * @throws IllegalArgumentException if name or resource is null, or if ttl is negative.
     */
    public String registerResource(String name, InputStream resource, int ttl) {
        performExpirations();

        if (name == null || name.trim().equals(""))
            throw new IllegalArgumentException("Argument 'name' cannot be null, empty, or contain only whitespace.");
        if (resource == null)
            throw new IllegalArgumentException("Argument 'resource' cannot be null.");

        NameResourcePair nrp = new NameResourcePair(name, resource, ttl);
        String id = Digest.digest((this.id++)+name);

        resourceMap.put(id, nrp);

        return id;
    }

    /**
     * Unregister a resource, disabling its availability for download.
     *
     * @param id Resource identifier to unregister
     * @return true if the resource was not expired and was removed by this
     * call.
     */
    public boolean unregisterResource(String id) {
        performExpirations();

        return resourceMap.remove(id) != null;
    }

    /**
     * Retrieve the resource associated with the given identifier.
     *
     * @param id Resource identifier to resolve
     * @return NameResourcePair of the requested resource, or null if unknown or expired.
     */
    public IDownloadService.NameResourcePair getResource(String id) {
        performExpirations();

        return (NameResourcePair)resourceMap.get(id);
    }

    /**
     * Perform expirations on resources.
     * Iterates through all known resources and checks to see if their TTL is
     * up. Any resources beyond their TTL will be removed from the resource
     * map.
     */
    private void performExpirations() {
       synchronized(resourceMap) {
          Iterator it = resourceMap.values().iterator();
          while (it.hasNext()) {
             NameResourcePair nrp = (NameResourcePair)it.next();

             if (nrp.isExpired())
                it.remove();
          }
       }
    }

    /*
     * Static Inner Classes.
     */

    /**
     * Mapping of a name to a resource.
     */
    public static class NameResourcePair implements IDownloadService.NameResourcePair {
        private String name;
        private InputStream resource;
        private byte[] resourceCache;
        private long creationTime;
        private int ttl;

        private NameResourcePair(String name, InputStream resource, int ttl) {
            if (resource == null)
                throw new IllegalArgumentException("Argument 'resource' cannot be null.");

            this.name = name;
            this.resource = resource;
            this.resourceCache = null;
            this.creationTime = System.currentTimeMillis();
            this.ttl = ttl;
        }

        /**
         * Get the resource's name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Get the resource contents.
         */
        public InputStream getResource() {
            if (this.resourceCache == null) {
                this.resourceCache = buffer(this.resource);
                this.resource = null;
            }

            return new ByteArrayInputStream(this.resourceCache);
        }

        /**
         * Get the resource size in bytes.
         */
        public long getSize() {
            if (this.resourceCache == null) {
                this.resourceCache = buffer(this.resource);
                this.resource = null;
            }

            return this.resourceCache.length;
        }

        // Clean up open file handles
        protected void finalize() {
            try {
                if (this.resource != null) {
                    this.resource.close();
                    this.resource = null;
                }
            } catch (Throwable t) {}
        }

        private int getTTL() {
            return this.ttl;
        }

        private boolean isExpired() {
            return (ttl > 0 && (creationTime+(ttl*1000)) <= System.currentTimeMillis());
        }

        private byte[] buffer(InputStream stream) {
            if (stream == null)
                return new byte[0];

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int r = 0;

            try {
                while ((r = stream.read(b)) >= 0) {
                    bos.write(b, 0, r);
                }
            } catch (IOException ioe) {
                throw new RuntimeException("Error reading resource content", ioe);
            } finally {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    throw new RuntimeException("Error reading resource content", ioe);
                }
            }

            return bos.toByteArray();
        }
    }
}
