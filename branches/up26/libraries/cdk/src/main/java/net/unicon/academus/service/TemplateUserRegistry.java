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

package net.unicon.academus.service;

import java.util.HashMap;
import java.util.Map;

import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconProperties;
import net.unicon.sdk.properties.UniconPropertiesFactory;

public final class TemplateUserRegistry {

    private static Map registry = null;

    /*
     * Public API.
     */

    public static String getTemplateUserName(String systemRole) {

        // Assertions.
        if (systemRole == null) {
            String msg = "Argument 'systemRole' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure the registry is loaded.
        if (registry == null) {
            synchronized (TemplateUserRegistry.class) {
                // Double-check.
                if (registry == null) {
                    // Load the registry.
                    registry = loadRegistry();
                }
            }
        }

        // Make sure we have a mapping.
        if (!registry.containsKey(systemRole)) {
            String msg = "No template user exists for the specified system role:  " + systemRole;
            throw new IllegalArgumentException(msg);
        }

        return (String) registry.get(systemRole);

    }

    /*
     * Implementation.
     */

    private TemplateUserRegistry() {}

    private static Map loadRegistry() {

        // Read the configuration data.
        UniconProperties props = UniconPropertiesFactory.getManager(CommonPropertiesType.FACTORY);
        String mappings = props.getProperty("net.unicon.academus.service.IImportService.templateUser.mappings");

        Map rslt = new HashMap();

        // Convert to entry array.
        String[] tokens = mappings.split(",");
        for (int i=0; i < tokens.length; i++) {
            String[] tuple = tokens[i].split(":");
            rslt.put(tuple[0], tuple[1]);
        }

        return rslt;

    }

}
