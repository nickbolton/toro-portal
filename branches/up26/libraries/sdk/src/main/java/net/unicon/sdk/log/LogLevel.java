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
 * Represents the set of priority level classifications 
 * in use in the log system.
 */
public final class LogLevel implements Comparable {
    private int level = 0;

    public final static LogLevel SEVERE = new LogLevel(1);
    public final static LogLevel ERROR  = new LogLevel(2);
    public final static LogLevel WARN   = new LogLevel(3);
    public final static LogLevel INFO   = new LogLevel(4);
    public final static LogLevel DEBUG  = new LogLevel(5);

    private LogLevel(int level) {
        this.level = level;
    }

    /**
     * Implementation of Comparable interface. 
     * 
     * @return a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object. 
     */
    public int compareTo(Object obj) {
        LogLevel compareLevel = (LogLevel)obj;
        int compareInt = compareLevel.toInt();
        int result = 0;

        if (level == compareInt) {
            result = 0;
        }
        if (level < compareInt) {
            result = -1;
        }
        if (level > compareInt) {
            result = 1;
        }

        return result;
    } // end compareTo

    /**
     * Is true if the objects are of the same log level.
     * 
     * @return true if objects are of the same log level. 
     */
    public boolean equals(Object obj) {
        LogLevel compareLevel = (LogLevel)obj;
        int compareInt = compareLevel.toInt();

        return (level == compareInt);
    }

    /**
     * Provides access to the level value of this object. 
     * 
     * @return level value of this object as an int. 
     */
    public int toInt() {
        return level;
    }

} // end LogLevel class
