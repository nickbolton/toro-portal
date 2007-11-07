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
package net.unicon.sdk.log.log4j; 

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Priority;
import org.apache.log4j.Logger;

import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogLevel;
import net.unicon.sdk.util.ResourceLoader;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.CommonPropertiesType;

/**
 * Log4JService is used to output messages to a log file or other data source
 * using log4j.
 *
 * Log4j configuration can be overridden using the 
 * /properties/Logger.properties file.
 *
 * Read the Log4J documentation on how to configure this file. 
 */
public final class Log4jLogService implements ILogService {
    private final static Priority SEVERE = Priority.FATAL;
    private final static Priority ERROR  = Priority.ERROR;
    private final static Priority WARN   = Priority.WARN;
    private final static Priority INFO   = Priority.INFO;
    private final static Priority DEBUG  = Priority.DEBUG;

    private Logger logger = null;

    /** constructor */
    public Log4jLogService() {
        initialize();
    }

    private void initialize() {
        try {
            String loggerConfigFile = UniconPropertiesFactory.getManager(
                CommonPropertiesType.SERVICE).getProperty(
                    "net.unicon.sdk.log.Log4jLogService.configFile");
            String loggerPropsFileName = 
                ResourceLoader.getResourceAsFileString(
                    Log4jLogService.class, loggerConfigFile);
            PropertyConfigurator.configureAndWatch(loggerPropsFileName);
            logger = Logger.getRootLogger();
        } catch (Throwable t) {
            System.err.println("Problem initializing Log4jLogService.");
            t.printStackTrace();
        }
    } // end initialize

    private Priority logLevelToPriority(LogLevel logLevel) {
        // Map LogLevel to log4j Priority
        if (logLevel.equals(LogLevel.SEVERE)) { // SEVERE
            return SEVERE;
        } else if (logLevel.equals(LogLevel.ERROR)) { // ERROR
            return ERROR;
        } else if (logLevel.equals(LogLevel.WARN)) { // WARN
            return WARN;
        } else if (logLevel.equals(LogLevel.INFO)) { // INFO
            return INFO;
        } else if (logLevel.equals(LogLevel.DEBUG)) { // DEBUG
            return DEBUG;
        } else { // otherwise, return the default 
            return INFO;
        } // end if
    } // end logLevelToPriority

    /**
     * Logs a message for the given LogLevel using log4j. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, String message) {
        Priority priority = logLevelToPriority(logLevel);
        logger.log(priority, message);
    } // end log(logLevel,message)

    /**
     * Logs an exception for the given LogLevel using log4j. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, Throwable ex) {
        Priority priority = logLevelToPriority(logLevel);
        logger.log(priority, "EXCEPTION: " + ex, ex);
    } // end log(logLevel,message,ex)

    /**
     * Logs an exception and associated message for the given LogLevel
     * using log4j. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, String message, Throwable ex) {
        Priority priority = logLevelToPriority(logLevel);
        logger.log(priority, message, ex);
    } // end log(logLevel,message,ex)

} // end Log4jLogService class
