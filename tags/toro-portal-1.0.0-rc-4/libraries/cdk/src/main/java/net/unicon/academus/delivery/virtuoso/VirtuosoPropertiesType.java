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
package net.unicon.academus.delivery.virtuoso;

import net.unicon.sdk.properties.PropertiesType;

/**
 * Represents the set of properties classifications in use in the system.  This
 * class follows the type-safe enumeration pattern (see Effective Java).
 * Consequently, all possible instances of <code>VirtuosoPropertiesType</code> are
 * static members of the class itself.
 */
public class VirtuosoPropertiesType extends PropertiesType {
    /* Static Members */
    public static final PropertiesType JMS =
        new VirtuosoPropertiesType("/properties/virtuoso-jms.properties");

    public static final PropertiesType RMI =
        new VirtuosoPropertiesType("/properties/virtuoso-rmi.properties");
    
    private VirtuosoPropertiesType(String fileName) {
        super.init(fileName);
    }
}
