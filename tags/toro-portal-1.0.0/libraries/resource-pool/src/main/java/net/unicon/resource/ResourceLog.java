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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @deprecated just use Commons Logging.  logging utilities previously in this
 * class have been moved elsewhere.
 */
@Deprecated
public class ResourceLog {

    // All logs are DEBUG level, this just gives a finer grandularity
    private int logLevel;
    private Log log = LogFactory.getLog(ResourceLog.class);

    private ResourceLog() {}

    public ResourceLog(int logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isEnabled(int logLevelArg) {
        return this.logLevel >= logLevelArg && log.isDebugEnabled();
    }

    public void error(ResourcePool logger, Throwable t) {
        myLog(0, logger, -1, "error", "ALERT", t, -1);
    }

    public void error(ResourcePool logger, Object o, Throwable t) {
        StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        myLog(0, logger, -1, "error", "ALERT", o + " --> " + t + "\n" + stackTrace, -1);
    }

    public void log(int logLevel, String message) {
        myLog(logLevel, message);
    }

    public void log(int logLevel, ResourcePool logger, Object details) {
        myLog(logLevel, logger, -1, null, null, details, -1);
    }

    public void log(int logLevel, ResourcePool logger, String action, String phase) {
        myLog(logLevel, logger, -1, action, phase, null, -1);
    }

    public void log(int logLevel, ResourcePool logger, String action, String phase, Object details) {
        myLog(logLevel, logger, -1, action, phase, details, -1);
    }

    public long time(int logLevel, ResourcePool logger, long startTime, String action, String phase) {
        return myLog(logLevel, logger, startTime, action, phase, null, -1);
    }

    public long time(int logLevel, ResourcePool logger, long startTime, String action, String phase, Object details) {
        return myLog(logLevel, logger, startTime, action, phase, details, -1);
    }

    public long time(int logLevel, ResourcePool logger, long startTime, String action, String phase, Object details, long maximumTime) {
        return myLog(logLevel, logger, startTime, action, phase, details, maximumTime);
    }

    protected long myLog(int logLevel, ResourcePool logger, long startTime, String action, String phase, Object details, long maximumTime) {
        if (!isEnabled(logLevel)) {
            return -1;
        }

        long timeDelta = -1;

        StringBuffer buffer = new StringBuffer(120);
        buffer.append(radjust(logger.getName(), 16));
        if (startTime > 0) {
            long endTime = System.currentTimeMillis();
            timeDelta = endTime - startTime;
            buffer.append(ladjust(timeDelta, 6));
        } else {
            buffer.append(ladjust("", 6));
        }
        buffer.append(radjust(phase, 8));
        buffer.append(radjust(action, 20));
        buffer.append((details == null) ? "" : details.toString());

        log.debug(buffer.toString());

        if (startTime > 0 && maximumTime > 0 && timeDelta > maximumTime) {
            myLog(1, logger, -1, combine(action, phase), "WARN", details + " -- " + timeDelta + " ms, exceeding max of " + maximumTime, -1);
        }

        return timeDelta;
    }

    protected void myLog(int logLevel, String message) {
        if (isEnabled(logLevel)) {
            log.debug(message);
        }
    }



    protected String combine(String action, String phase) {
        return (phase == null || phase.length() == 0) ? action : action + " / " + phase;
    }

    protected String ladjust(long l, int length) {
        return ladjust("" + l, length);
    }

    protected String radjust(long l, int length) {
        return radjust("" + l, length);
    }

    protected String ladjust(String s, int length) {
        String target = (s == null || s.length() == 0) ? "-" : s;
        while (target.length() < length) {
            target = " " + target;
        }
        if (target.length() > length) {
            target = target.substring(0, length);
        }

        return target + " ";
    }

    protected String radjust(String s, int length) {
        String target = (s == null || s.length() == 0) ? "-" : s;
        while (target.length() < length) {
            target = target + " ";
        }
        if (target.length() > length) {
            target = target.substring(0, length);
        }

        return target + " ";
    }
}
