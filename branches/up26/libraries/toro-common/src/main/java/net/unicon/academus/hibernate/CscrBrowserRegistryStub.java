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
package net.unicon.academus.hibernate;

/**
 * Stub for hibernate schema loading
 */
public class CscrBrowserRegistryStub {
    private String browser;
    private String platform;
    private String startVersion;
    private String endVersion;
    private String enabled;
    public String getBrowser() { return browser; }
    public void setBrowser(String s) { this.browser = s; }
    public String getPlatform() { return platform; }
    public void setPlatform(String s) { this.platform = s; }
    public String getStartVersion() { return startVersion; }
    public void setStartVersion(String s) { this.startVersion = s; }
    public String getEndVersion() { return endVersion; }
    public void setEndVersion(String s) { this.endVersion = s; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String s) { this.enabled = s; }
}
