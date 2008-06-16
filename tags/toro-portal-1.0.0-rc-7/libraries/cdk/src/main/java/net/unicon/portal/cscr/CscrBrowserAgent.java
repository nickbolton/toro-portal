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

/**
 * @author nbolton
 */
public final class CscrBrowserAgent implements Comparable {

    private String browser;
    private String platform;
    private String startVersion;
    private String endVersion;
    private boolean cscrEnabled;
    private Float startVersionValue;
    private Float endVersionValue;

    private int hashcode;
    private String key;

    public static final String ANY_VERSION = "any";

    public CscrBrowserAgent(String browser, String platform, String startVersion,
        String endVersion, boolean cscrEnabled) throws CscrRegistryException {

        if (browser == null || "".equals(browser.trim())) {
            throw new CscrRegistryException(
                "CscrBrowserRegistry : invalid browser: " + browser);
        }

        if (platform == null || "".equals(platform.trim())) {
            throw new CscrRegistryException(
                "CscrBrowserRegistry : invalid platform: " + platform);
        }

        this.browser = browser;
        this.platform = platform;
        setStartVersion(startVersion);
        setEndVersion(endVersion);
        this.cscrEnabled = cscrEnabled;
        this.key = browser+platform;
        this.startVersionValue = new Float(startVersion);

        this.hashcode = key.hashCode();
    }

    public String getBrowser() {
        return browser;
    }

    public String getPlatform() {
        return platform;
    }

    public String getKey() {
        return key;
    }

    public String getStartVersion() {
        return startVersion;
    }

    public Float getStartVersionValue() {
        return startVersionValue;
    }

    public void setStartVersion(String startVersion) throws CscrRegistryException {
        try {
            startVersionValue = new Float(startVersion);
        } catch (NumberFormatException nfe) {
            throw new CscrRegistryException(
                "CscrBrowserAgent::setStartVersion : invalid start_version: " +
                 startVersion);
        }
        this.startVersion = startVersion;
    }

    public String getEndVersion() {
        return endVersion;
    }

    public void setEndVersion(String endVersion) throws CscrRegistryException {
        try {
            if (!ANY_VERSION.equals(endVersion)) {
                endVersionValue = new Float(endVersion);
            }
        } catch (NumberFormatException nfe) {
            throw new CscrRegistryException(
                "CscrBrowserRegistry::setEndVersion : invalid end_version: " + endVersion);
        }
        this.endVersion = endVersion;
    }

    public boolean getCscrEnabled() {
        return cscrEnabled;
    }

    public void setCscrEnabled(boolean cscrEnabled) {
        this.cscrEnabled = cscrEnabled;
    }

    public int hashCode() {
        return hashcode;
    }

    public int compareTo(Object o) {
        if (!(o instanceof CscrBrowserAgent)) return 1;

        CscrBrowserAgent agent = (CscrBrowserAgent)o;

        // if the keys are equal, then drill down to the start version
        if (agent.getKey().equals(this.getKey())) {

            // reverse the order, so the values that are greater are listed first
            return agent.getStartVersionValue().compareTo(this.getStartVersionValue());
        }
        return this.getKey().compareTo(agent.getKey());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer('(');
        sb.append(browser).append(", ");
        sb.append(platform).append(", ");
        sb.append(startVersion).append(", ");
        sb.append(endVersion).append(", ");
        sb.append(cscrEnabled).append(", ");
        sb.append(')');
        return sb.toString();
    }
}
