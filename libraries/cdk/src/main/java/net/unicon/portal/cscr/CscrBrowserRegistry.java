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
package net.unicon.portal.cscr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.Cookie;

import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.util.db.AcademusDBUtil;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogServiceFactory;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.BrowserInfo;

/**
 * @author nbolton
 *
 * This is a registry for cscr enabled browser mappings. It keeps a list
 * registered agents by
 *     browser name ("MSIE", "Netscape", "Firefox", etc...)
 *     platform ("Macintosh", "Windows",  etc...)
 *     start_version ("6.0",...)
 *     end_version ("6.2", "any", ...) any means "or higher"
 *     enabled ("true", "false")
 */
public final class CscrBrowserRegistry {

    private static final boolean useCSCR =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
        getPropertyAsBoolean("net.unicon.portal.cscr.useCSCR");

    private static CscrBrowserRegistry __instance = null;
    private static ILogService logService;
    private static final String BROWSER_ENABLED = "true";
    private static final String BROWSER_DISABLED = "false";
    private static final String selectSql =
        "select browser, platform, start_version, end_version, enabled from cscr_browser_registry";
    private static final String insertSql =
        "insert into cscr_browser_registry (browser, platform, start_version, end_version, enabled) values (?,?,?,?,?)";
    private static final String deleteSql =
        "delete from cscr_browser_registry where browser=? and platform=?";
    private static long workerCycle;

    private Set registry = Collections.synchronizedSet(new TreeSet());
    private PollingWorker pollingWorker;

    private Map cache = Collections.synchronizedMap(new HashMap());

    public static CscrBrowserRegistry instance() {
        if (__instance == null) {
            logService = LogServiceFactory.instance();
            // value given should be in seconds, so convert to milliseconds
            workerCycle = 1000 * UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
                getPropertyAsInt("net.unicon.portal.cscr.CscrBrowserRegistry.workerCycle");
            __instance = new CscrBrowserRegistry();
        }
        return __instance;
    }

    // check to see if CSCR should be used
    public boolean isEnabled(BrowserInfo bInfo) {
        if (bInfo == null || !useCSCR) return false;

        // Check for the cookie 'javascriptEnabled', which is set by cscr.jsp
        // after the browser has been checked.
        boolean jsEnabled = false;
        Cookie[] cookies = bInfo.getCookies();
        for (int i = 0; !jsEnabled && i < cookies.length; i++) {
            if (cookies[i].getName().equals("javascriptEnabled"))
                jsEnabled = true;
        }

        return jsEnabled;
    }

    // used for the initial check. All subsequent checks are to use isEnabled(BrowserInfo).
    public boolean isEnabled(String agent) {
        // return a status of true if there is an
        // entry that matches this agent and that entry has
        // an enabled value.

        // guard against bogus agent queries
        if (!useCSCR || agent == null || "".equals(agent.trim())) return false;


        // first check the cache
        Boolean cacheValue = (Boolean)cache.get(agent);
        if (cacheValue != null) return cacheValue.booleanValue();

        // now go through the set of registered agents
        // and take the first one that meets the criteria of the
        // given agent.  The registry should be ordered such that
        // entries with higher start versions will be first.

        boolean cscrEnabled = false;
        boolean found = false;
        CscrBrowserAgent cscrBrowserAgent;
        Iterator itr = registry.iterator();
        while (!found && itr.hasNext()) {
            cscrBrowserAgent = (CscrBrowserAgent)itr.next();
            logService.log(ILogService.DEBUG, "CscrBrowserRegistry::isEnabled : checking agent (" +
                agent + ") against " + cscrBrowserAgent);
            if (agent.indexOf(cscrBrowserAgent.getBrowser()) < 0) continue;
            if (agent.indexOf(cscrBrowserAgent.getPlatform()) < 0) continue;

            logService.log(ILogService.DEBUG, "CscrBrowserRegistry::isEnabled :  agent (" +
                agent + ") matched browser/platform with " + cscrBrowserAgent);
          

            // parse out the given agent's version
            int browserPos = agent.indexOf(cscrBrowserAgent.getBrowser());
            int startPos = browserPos+cscrBrowserAgent.getBrowser().length()+1;

            String browserVersion = agent.substring(startPos);
            Matcher m = Pattern.compile("([0-9]+(.[0-9]+)?)").matcher(browserVersion);
            if (m.find()) {
                browserVersion = m.group(1);
            } else {
                logService.log(ILogService.ERROR,
                        "CscrBrowserRegistry::isEnabled : Failed to find version for agent: "+agent);
            }
            logService.log(ILogService.DEBUG, "CscrBrowserRegistry::isEnabled :  agent browser version: " +
                cscrBrowserAgent.getBrowser() + " " + browserVersion);

            Float browserVersionValue;
            try {
                browserVersionValue = new Float(browserVersion);

                if (browserVersionValue.compareTo(cscrBrowserAgent.getStartVersionValue()) < 0) continue;

                if (CscrBrowserAgent.ANY_VERSION.equals(cscrBrowserAgent.getEndVersion())) {
                    cscrEnabled = cscrBrowserAgent.getCscrEnabled();
                    found = true;
                } else {
                    Float endVersionValue = new Float(cscrBrowserAgent.getEndVersion());
                    if (browserVersionValue.compareTo(endVersionValue) < 0 ||
                        browserVersionValue.compareTo(endVersionValue) == 0) {
                        cscrEnabled = cscrBrowserAgent.getCscrEnabled();
                        found = true;
                    }
                }
            } catch (NumberFormatException nfe) {
                logService.log(ILogService.ERROR, "CscrBrowserRegistry::isEnabled :  invalid agent browser version: " +
                    cscrBrowserAgent.getBrowser() + " " + browserVersion);
                continue;
            }
        }

        logService.log(ILogService.DEBUG,
            "CscrBrowserRegistry::isEnabled : agent/status: " +
            agent + "/" + cscrEnabled);

        cache.put(agent, new Boolean(cscrEnabled));
        return cscrEnabled;
    }

    public void registerUserAgent(String browser, String platform, String startVersion,
        String endVersion, boolean enabled) throws Exception {

        CscrBrowserAgent agent = new CscrBrowserAgent(browser, platform, startVersion, endVersion, enabled);
        synchronized (registry) {
            removeAgent(agent);
            addAgent(agent);
        }
    }

    private void addAgent(CscrBrowserAgent agent) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(insertSql);
            int i=1;
            ps.setString(i++, agent.getBrowser());
            ps.setString(i++, agent.getPlatform());
            ps.setString(i++, agent.getStartVersion());
            ps.setString(i++, agent.getEndVersion());
            ps.setString(i++,
                (agent.getCscrEnabled() ? BROWSER_ENABLED : BROWSER_DISABLED));
            ps.executeUpdate();
            registry.add(agent);
            logService.log(ILogService.DEBUG,
                "CscrBrowserRegistry::addAgent : adding agent: " + agent);
        } catch (Exception e) {
            logService.log(ILogService.ERROR,
                "CscrBrowserRegistry::addAgent : Failed to add agent: " + agent);
        } finally {
            AcademusDBUtil.safeClosePreparedStatement(ps);
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
    }

    private void removeAgent(CscrBrowserAgent agent) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            ps = conn.prepareStatement(deleteSql);
            int i=1;
            ps.setString(i++, agent.getBrowser());
            ps.setString(i++, agent.getPlatform());
            int result = ps.executeUpdate();
            if (registry.remove(agent)) {
                logService.log(ILogService.DEBUG,
                    "CscrBrowserRegistry::removeAgent : removed agent: " + agent);
            }
        } catch (Exception e) {
            logService.log(ILogService.ERROR,
                "CscrBrowserRegistry::removeAgent : Failed to remove agent: " + agent, e);
        } finally {
            AcademusDBUtil.safeClosePreparedStatement(ps);
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
    }

    public void startService() {
        if (pollingWorker == null) {
            pollingWorker = new PollingWorker(registry, cache, workerCycle);
            Thread t = new Thread(pollingWorker);
            t.setDaemon(true);
            t.start();
        }
    }

    public void stopService() {
        pollingWorker.stop();
        pollingWorker = null;
    }

    private CscrBrowserRegistry() {
        startService();
    }

    public static final class PollingWorker implements Runnable {
        private Set registry;
        private Map cache;
        private long workerCycle;
        private boolean running = false;

        public PollingWorker(Set registry, Map cache, long workerCycle) {
            this.registry = registry;
            this.cache = cache;
            this.workerCycle = workerCycle;
            this.running = true;
        }

        public void stop() {
            running = false;
        }

        public void run() {
            try {
                while (running) {
                    synchronized (registry) {
                        refresh();
                    }
                    
                    logService.log(ILogService.DEBUG, "CscrBrowserRegistry : sleeping " + workerCycle + " ms");
                    Thread.sleep(workerCycle);
                }
            } catch (InterruptedException ie) {
                logService.log(ILogService.ERROR,
                    "CscrBrowserRegistry : polling interrupted, aborting service.", ie);
            }
        }

        private void refresh() {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            registry.clear();
            cache.clear();
            try {
                logService.log(ILogService.DEBUG, "CscrBrowserRegistry::refresh : polling browser registry");
                String browser;
                String platform;
                String startVersion;
                String endVersion;
                String enabled;
                CscrBrowserAgent agent;
                conn = AcademusDBUtil.getDBConnection();
                ps = conn.prepareStatement(selectSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    browser = rs.getString("browser");
                    platform = rs.getString("platform");
                    startVersion = rs.getString("start_version");
                    endVersion = rs.getString("end_version");
                    enabled = rs.getString("enabled");

                    agent = new CscrBrowserAgent(browser, platform, startVersion,
                        endVersion, new Boolean(enabled).booleanValue());
                    logService.log(ILogService.DEBUG,
                        "CscrBrowserRegistry::refresh : adding agent: " + agent);
                    registry.add(agent);
                }
            } catch (Exception e) {
                logService.log(ILogService.ERROR,
                    "CscrBrowserRegistry::refresh : Failed to read registry", e);
            } finally {
                AcademusDBUtil.safeCloseResultSet(rs);
                AcademusDBUtil.safeClosePreparedStatement(ps);
                AcademusDBUtil.safeReleaseDBConnection(conn);
            }
        }
    }
}
