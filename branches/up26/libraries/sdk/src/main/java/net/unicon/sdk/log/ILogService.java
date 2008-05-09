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
package net.unicon.sdk.log;

/**
 * Provides logging of messages to a file or other type of data source. 
 */
public interface ILogService {

    /**
     * Logs a message for the given LogLevel. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, String message);

    /**
     * Logs an exception for the given LogLevel. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, Throwable ex);

    /**
     * Logs an exception and associated message for the given LogLevel. 
     * 
     * @see <{LogLevel}>
     */
    public void log(LogLevel logLevel, String message, Throwable ex);

    public final static LogLevel SEVERE = LogLevel.SEVERE;
    public final static LogLevel ERROR  = LogLevel.ERROR;
    public final static LogLevel WARN   = LogLevel.WARN;
    public final static LogLevel INFO   = LogLevel.INFO;
    public final static LogLevel DEBUG  = LogLevel.DEBUG;

} // end ILogService
