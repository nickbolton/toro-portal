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
package net.unicon.portal.util;

import java.util.Hashtable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.NumberFormat;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;

import org.jasig.portal.services.LogService;
import org.apache.log4j.*;

public class Debug {

    // copied from uPortal LogService
    public final static Priority NONE = Priority.DEBUG;
    public final static Priority SEVERE = Priority.FATAL;
    public final static Priority ERROR = Priority.ERROR;
    public final static Priority WARN = Priority.WARN;
    public final static Priority INFO = Priority.INFO;
    public final static Priority DEBUG = Priority.DEBUG;

    protected static final boolean debugEnabled = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.util.Debug.debugEnabled");
    protected static final Hashtable threadTime = new Hashtable();
    protected static final NumberFormat nf = NumberFormat.getInstance();
    protected static final SimpleDateFormat df =
    new SimpleDateFormat("MM-dd-yyyy HH:mm:ss:SSS");
    protected static Debug instance = new Debug();
    static {
        nf.setMinimumIntegerDigits(5);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
    }
    protected Debug() { }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static Debug instance() {
        return instance;
    }

    public void debug() {
        debug(Priority.DEBUG, "", null);
    }

    public void debug(Throwable t) {
        debug(Priority.DEBUG, "", t);
    }

    public void debug(StringBuffer sb) {
        debug(Priority.DEBUG, sb, null);
    }
    
    public void debug(String msg) {
        debug(Priority.DEBUG, msg, null);
    }
    
    public void debug(StringBuffer sb, Throwable t) {
        debug(Priority.DEBUG, sb, t);
    }
    
    public void debug(String msg, Throwable t) {
        debug(Priority.DEBUG, msg, t);
    }
    
    public void debug(Priority pr, StringBuffer sb, Throwable throwable) {
        if (!debugEnabled) return;
        Thread t = new Thread(new Worker(pr, sb.toString(), throwable));
        // give it a low priority
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    public void debug(Priority pr, String msg, Throwable throwable) {
        if (!debugEnabled) return;
        Thread t = new Thread(new Worker(pr, msg, throwable));
        // give it a low priority
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    public void timeElapsed() {
        timeElapsed(Thread.currentThread(), "", false);
    }
    public void timeElapsed(Object key) {
        timeElapsed(key, "", false);
    }
    public long getTimeElapsed(Object key) {
        Date time = (Date)threadTime.get(key);
        if (time == null) return 0L;
        return (new Date().getTime() - time.getTime());
    }
    public long getTimeElapsed() {
        return getTimeElapsed(Thread.currentThread());
    }
    public void timeElapsed(String msg, boolean markTime) {
        timeElapsed(Thread.currentThread(), msg, markTime);
    }
    public void timeElapsed(Object key, String msg, boolean markTime) {
        if (!debugEnabled) return;
        Date time = (Date)threadTime.get(key);
        StringBuffer sb = new StringBuffer();
        if (msg != null) {
            sb.append(msg);
        }
        if (time != null) {
            if (msg != null) {
                sb.append(" ");
            }
            sb.append("Time Elapsed: ");
            sb.append((new Date().getTime() - time.getTime()));
            sb.append(" ms");
        }
        debug(sb);
        if (markTime) {
            markTime(key);
        }
    }
    public void markTime() {
        threadTime.put(Thread.currentThread(), new Date());
    }
    public void markTime(Object key) {
        threadTime.put(key, new Date());
    }
    protected class Worker implements Runnable {
        String msg;
        Priority priority;
        Throwable throwable;
        
        public Worker (Priority priority, String msg, Throwable throwable) {
            this.priority = priority;
            this.msg = msg;
            this.throwable = throwable;
        }
        public void run() {
            if (throwable != null) {
                LogService.instance().log(priority, msg, throwable);
            } else {
                LogService.instance().log(priority, msg);
            }
        }
    }
}
