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
package net.unicon.portal.common;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.util.StringUtils;

import java.util.Map;
import java.util.Enumeration;
import java.util.Iterator;
import org.jasig.portal.*;
import org.jasig.portal.services.LogService;

public class RuntimeDataFilter extends ChannelRuntimeData {
    protected ChannelRuntimeData crd = null;
    public RuntimeDataFilter(ChannelRuntimeData crd) {
        boolean isClone = (this.crd != null);
        this.crd = crd;
        if (!isClone) filterParameters();
    } // end RuntimeDataFilter(ChannelRuntimeData) constructor
    public Object clone() {
        return new RuntimeDataFilter((ChannelRuntimeData)crd.clone());
    } // end clone
    private void filterParameters() {
        Enumeration e = crd.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Object[] values = crd.getObjectParameterValues(key);
            if ((values != null) && (values.length > 0)) {
                if (values instanceof String[]) {
                    this.setParameterValues(key, (String[]) values);
                } else if (values instanceof
                com.oreilly.servlet.multipart.Part[]) {
                    crd.setParameterValues(key,
                    (com.oreilly.servlet.multipart.Part[]) values);
                } else {
                    crd.put(key, values);
                }
            }
        }
    } // end escapeParameters
    public void setParameter(String key, String value) {
        crd.setParameter(
        key, StringUtils.replaceSpecialCharsOfString(value));
    } // end setParameter
    public String getParameter(String key) {
        return crd.getParameter(key);
    } // end Object getParameter
    public String[] getParameterValues(String key) {
        return crd.getParameterValues(key);
    } // end getObjectParameterValues
    public Object getObjectParameter(String key) {
        return crd.getObjectParameter(key);
    } // end getObjectParameter
    public Object[] getObjectParameterValues(String key) {
        return crd.getObjectParameterValues(key);
    } // end getObjectParameterValues
    public void setParameters(Map params) {
        Iterator it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Object[] values = (Object[]) params.get(key);
            if ((values != null) && (values.length > 0)) {
                if (values instanceof String[]) {
                    this.setParameterValues(key, (String[]) values);
                } else if (values instanceof
                com.oreilly.servlet.multipart.Part[]) {
                    crd.setParameterValues(key,
                    (com.oreilly.servlet.multipart.Part[]) values);
                } else {
                    crd.put(key, values);
                }
            }
        } // end loop
    } // end setParameters(Map)
    public String[] setParameterValues(String key, String[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = StringUtils.replaceSpecialCharsOfString(
            values[i]);
        }
        return crd.setParameterValues(key, values);
    } // end setParameterValues(String, String[])
    public com.oreilly.servlet.multipart.Part[] setParameterValues(
    String key, com.oreilly.servlet.multipart.Part[] values) {
        return crd.setParameterValues(key, values);
    } // end serParameterValues(String, Part[])
    public synchronized void setParameter(
    String key, com.oreilly.servlet.multipart.Part value) {
        crd.put(key, value);
    } // end setParameter(String, Part[])
    public String getBaseActionURL() {
        return crd.getBaseActionURL(false);
    }
    public String getBaseActionURL(boolean idempotent) {
        return crd.getBaseActionURL(idempotent);
    }
    public String getWorkerActionURL(String worker) {
        // this is not a typo .. it should be calling getBaseWorkerURL()
        return crd.getBaseWorkerURL(worker);
    }
    public String getBaseWorkerURL(String worker) {
        return crd.getBaseWorkerURL(worker);
    }
    public String getBaseWorkerURL(String worker, boolean idempotent) throws PortalException {
        return crd.getBaseWorkerURL(worker, idempotent);
    }
    public void setUPFile(UPFileSpec upfs) {
        crd.setUPFile(upfs);
    } // end setUPFile
    public void setRenderingAsRoot(boolean rar) {
        crd.setRenderingAsRoot(rar);
    } // end setRenderingAsRoot
    public void setBrowserInfo(BrowserInfo bi) {
        crd.setBrowserInfo(bi);
    } // end setBrowserInfo
    public BrowserInfo getBrowserInfo() {
        return crd.getBrowserInfo();
    } // end getBrowserInfo
    public Enumeration getParameterNames() {
        return crd.getParameterNames();
    } // end getParameterNames
} // end RuntimeDataFilter class
