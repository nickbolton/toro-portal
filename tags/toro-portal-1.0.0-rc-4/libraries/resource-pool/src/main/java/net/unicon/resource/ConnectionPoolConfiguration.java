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

import java.util.Map;

class ConnectionPoolConfiguration extends ResourcePoolConfiguration {
    protected String driver;
    protected String url;
    protected String user;
    protected String password;
    protected String simpleSQL;

    ConnectionPoolConfiguration(String name, Map m) {
        initialize(name, m);
    }

    public String getDriver() {
        return driver;
    }

    public String getURL() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getSimpleSQL() {
        return simpleSQL;
    }

    @Override
    public String getDescriptor() {
        return ConnectionPoolConfiguration.asDescriptor(url, user, password);
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSimpleSQL(String simpleSQL) {
        this.simpleSQL = simpleSQL;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return super.toString() + "\t"
            + "driver = " + driver + "\n\t"
            + "url = " + url + "\n\t"
            + "user = " + user + "\n\t"
            + "password = " + password + "\n\t"
            + "simpleSQL = " + simpleSQL + "\n";
    }

    protected void initialize(String nameArg, Map m) {
        setDefaults();

        this.url = (String)m.get("url");
        this.user = (String)m.get("user");
        this.password = (String)m.get("password");
        this.driver = (String)m.get("driver");
        this.simpleSQL = (String)m.get("simpleSQL");
        setName(nameArg);

        if (m.get("size") != null) {
            setSize(Integer.valueOf((String)m.get("size")));
        }
        if (m.get("refreshSleepTime") != null) {
            setRefreshSleepTime(Integer.valueOf((String)m.get("refreshSleepTime")));
        }
        if (m.get("refreshPeriod") != null) {
            setRefreshPeriod(Integer.valueOf((String)m.get("refreshPeriod")));
        }
        if (m.get("sizeSleepTime") != null) {
            setSizeSleepTime(Integer.valueOf((String)m.get("sizeSleepTime")));
        }
        if (m.get("timeoutSleepTime") != null) {
            setTimeoutSleepTime(Integer.valueOf((String)m.get("timeoutSleepTime")));
        }
        if (m.get("timeoutPeriod") != null) {
            setTimeoutPeriod(Integer.valueOf((String)m.get("timeoutPeriod")));
        }
        if (m.get("createLimit") != null) {
            setCreateLimit(Integer.valueOf((String)m.get("createLimit")));
        }
        if (m.get("destroyLimit") != null) {
            setDestroyLimit(Integer.valueOf((String)m.get("destroyLimit")));
        }
        if (m.get("recycleLimit") != null) {
            setRecycleLimit(Integer.valueOf((String)m.get("recycleLimit")));
        }
        if (m.get("verifyLimit") != null) {
            setVerifyLimit(Integer.valueOf((String)m.get("verifyLimit")));
        }
        if (m.get("allocationWarnTime") != null) {
            setAllocationWarnTime(Integer.valueOf((String)m.get("allocationWarnTime")));
        }
        if (m.get("allocationWaitWarnTime") != null) {
            setAllocationWaitWarnTime(Integer.valueOf((String)m.get("allocationWaitWarnTime")));
        }
        if (m.get("creationWarnTime") != null) {
            setCreationWarnTime(Integer.valueOf((String)m.get("creationWarnTime")));
        }
        if (m.get("recycleWarnTime") != null) {
            setRecycleWarnTime(Integer.valueOf((String)m.get("recycleWarnTime")));
        }
        if (m.get("traceCallers") != null) {
            setTraceCallers(Integer.valueOf((String)m.get("traceCallers")));
        }
        if (m.get("useGuaranteedActions") != null) {
            setUseGuaranteedActions(Boolean.valueOf((String)m.get("useGuaranteedActions")));
        }
        if (m.get("logMicroTimes") != null) {
            setLogMicroTimes(Boolean.valueOf((String)m.get("logMicroTimes")));
        }
        if (m.get("numberOfRecycleThreads") != null) {
            setNumberOfRecycleThreads(Integer.valueOf((String)m.get("numberOfRecycleThreads")));
        }
        if (m.get("logLevel") != null) {
            setLogLevel(Integer.valueOf((String)m.get("logLevel")));
        }
    }

    protected void setDefaults() {
        size = 10;
        refreshSleepTime = 30000;
        refreshPeriod = 120000;
        sizeSleepTime = 15000;
        timeoutSleepTime = 20000;
        timeoutPeriod = 60000;
        createLimit = 10000;
        destroyLimit = 10000;
        recycleLimit = 5000;
        verifyLimit = 5000;
        allocationWarnTime = 10000;
        allocationWaitWarnTime = 10000;
        creationWarnTime = 5000;
        recycleWarnTime = 5000;
        traceCallers = 0;
        useGuaranteedActions = true;
        logMicroTimes = false;
        numberOfRecycleThreads = 50;
        logLevel = 0;
    }

    protected static String asDescriptor(String url, String user, String password) {
        return url + "#" + user + "#" + password;
    }
}
