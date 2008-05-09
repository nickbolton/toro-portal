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
package net.unicon.sdk.cache.jms;

import java.io.IOException;
import java.util.Properties;

public class JMSPropertiesManager {
    private static final String PROPERTIES_FILE_NAME =
    "/properties/jms_cache.properties";
    private static final Properties props = new Properties();
    static {
        loadProps();
    }
    /** Load the properties. */
    protected static void loadProps () {
        try {
            System.out.println("Loading JMSPropertiesManager\n");
            props.load(JMSPropertiesManager.class.getResourceAsStream(
            PROPERTIES_FILE_NAME));
        } catch (IOException ioe) {
            //LogService.instance().log(LogService.ERROR,
            //"Unable to read portal.properties file.");
            //LogService.instance().log(LogService.ERROR, ioe);
	    ioe.printStackTrace();
        }
    }
    /**
     * Returns the value of a property for a given name. A runtime exception is
     * thrown in the case the property cannot be found.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     */
    public static String getProperty(String name) {
        String val = props.getProperty(name);
        if (val == null)
            throw new RuntimeException("Property " + name + " not found!");
        return val;
    }
    /**
     * Returns the value of a property for a given name. This method can be used if the property is boolean in
     * nature and you want to make sure that <code>true</code> is
     * returned if the property is set to "true", "yes", "y", or "on" (regardless of case),
     * and <code>false</code> is returned in all other cases.
     * @param name the name of the requested property
     * @return value <code>true</code> if property is set to "true",
     * "yes", "y", or "on" regardless of case, otherwise <code>false</code>
     */
    public static boolean getPropertyAsBoolean(String name) {
        boolean retValue = false;
        String value = getProperty(name);
        if (value != null) {
            retValue = value.equalsIgnoreCase("true") ||
            value.equalsIgnoreCase("yes") ||
            value.equalsIgnoreCase("y") ||
            value.equalsIgnoreCase("on");
        }
        return retValue;
    }
    /**
     * Returns the value of a property for a given name as a <code>byte</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>byte</code>
     */
    public static byte getPropertyAsByte(String name) {
        return Byte.parseByte(getProperty(name));
    }
    /**
     * Returns the value of a property for a given name as a <code>short</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>short</code>
     */
    public static short getPropertyAsShort(String name) {
        return Short.parseShort(getProperty(name));
    }
    /**
     * Returns the value of a property for a given name as an <code>int</code>
     * @param name the name of the requested property
     * @return value the property's value as an <code>int</code>
     */
    public static int getPropertyAsInt(String name) {
        return Integer.parseInt(getProperty(name));
    }
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     */
    public static long getPropertyAsLong(String name) {
        return Long.parseLong(getProperty(name));
    }
    /**
     * Returns the value of a property for a given name as a <code>float</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>float</code>
     */
    public static float getPropertyAsFloat(String name) {
        return Float.parseFloat(getProperty(name));
    }
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     */
    public static double getPropertyAsDouble(String name) {
        return Double.parseDouble(getProperty(name));
    }
}
