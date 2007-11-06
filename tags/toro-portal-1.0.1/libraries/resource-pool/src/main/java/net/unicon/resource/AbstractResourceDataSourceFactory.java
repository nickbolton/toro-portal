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

package net.unicon.resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class AbstractResourceDataSourceFactory implements ObjectFactory {

    private Log log = LogFactory.getLog(AbstractResourceDataSourceFactory.class);

    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
    throws Exception {
        if (obj instanceof Reference) {
            Reference reference = (Reference)obj;

            Map m = new HashMap();
            for (Enumeration en = reference.getAll(); en.hasMoreElements();) {
                RefAddr ref = (RefAddr)en.nextElement();
                m.put(ref.getType(), ref.getContent());
            }

            RefAddr configFileRefAddr = reference.get("configPath");
            if (configFileRefAddr != null) {
	            File configFile = new File((String)reference.get("config").getContent());

	            if (configFile.exists()) {
                    if (hasObjectFactoryConfigurations(m)) {
                        throw new RuntimeException("ResourceDataSourceFactory can only accept one configuration source. " +
                            "Please either provide a path to a properties file with 'configPath' or all the required " +
                            "connection pool parameters ('driver', 'url', 'user', 'password').");
		            }

		            Properties props = new Properties();
		            props.load(new FileInputStream(configFile));
		            m = props;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("ResourceDataSourceFactory : creating ResourcePoolBasic with attributes: " + m);
            }

            String poolName = name.get(0);
            ConnectionPoolConfiguration config = new ConnectionPoolConfiguration(poolName, m);

            Resources.addConnectionPool(newResourcePool(config));

            return new ResourceDataSource(poolName);
        } else {
            throw new RuntimeException("Object needs to be of type: " + Reference.class.getName());
        }
    }

    private boolean hasObjectFactoryConfigurations(Map m) {
    	return m.get("url") != null || m.get("driver") != null ||
    		m.get("user") != null || m.get("password") != null;
    }

    protected abstract ResourcePool newResourcePool(ConnectionPoolConfiguration config);
}