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

import org.xml.sax.ContentHandler;
import org.w3c.dom.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;

import org.jasig.portal.*;
import org.jasig.portal.channels.BaseMultithreadedChannel;
import org.jasig.portal.utils.*;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;

public class BaseMultithreadedCachableChannel extends BaseMultithreadedChannel
implements IMultithreadedCacheable, IMultithreadedPrivileged  {

    private String getKey(String upId) {
        return upId;
    }

    private static final String CHANNEL_DATA_KEY = "ChannelData";


    public void receiveEvent (PortalEvent event, String upId) {
        super.receiveEvent(event, upId);
    }

    public void setPortalControlStructures(PortalControlStructures pcs,
        String upId)
    throws PortalException {
        getChannelData(upId).setPortalControlStructures(pcs);
    }

    public PortalControlStructures getPortalControlStructures(String upId) {
        return getChannelData(upId).getPortalControlStructures();
    }

    // this method of determining the upId is taken directly
    // from uPortal's ChannelManager class. If that happens
    // to change, this will have to mimic it.
    public String convertUpId(String upId, String channelId) {
        return getPortalControlStructures(upId).getHttpServletRequest().
            getSession(false).getId()+ "/" + channelId;
    }

    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        super.setStaticData(sd, upId);
        ChannelData dataState = new ChannelData();
        dataState.setUPortalUser(sd.getPerson());
        getChannelDataMap(upId).put(CHANNEL_DATA_KEY, dataState);
    }

    protected String getClassHandle() {
        return getClass().toString().substring(6);
    }

    public void setRuntimeData (ChannelRuntimeData rd, String upId)
    throws PortalException {
        super.setRuntimeData(rd, upId);
    }    

    public ChannelRuntimeData getRuntimeData(String upId) {
        ChannelState channelState = (ChannelState)channelStateMap.get(upId);
        return channelState.getRuntimeData();
    }

    public ChannelStaticData getStaticData(String upId) {
        ChannelState channelState =
            (ChannelState)channelStateMap.get(upId);
        return channelState.getStaticData();
    }

    public String getChannelSubscribeId(String upId) {
        return getStaticData(upId).getChannelSubscribeId();
    }

    public ChannelCacheKey generateKey(String upId) {
        ChannelCacheKey key = new ChannelCacheKey();
        key.setKey(getKey(upId));
        key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);

        key.setKeyValidity(null);
//        setLastContext(upId, getDomainUser(upId).getContext());
        return key;
    }

    public String fetchChannelTarget(String upId) {
        String servletPath = getPortalControlStructures(upId).
            getHttpServletRequest().getServletPath();
        StringTokenizer tokenizer =
            new StringTokenizer(servletPath, UPFileSpec.PORTAL_URL_SEPARATOR);

        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            if (UPFileSpec.TARGET_URL_ELEMENT.equals(tok) &&
                tokenizer.hasMoreTokens()) {
                return tokenizer.nextToken();
            }
        }
        return "";
    }

    public boolean isCacheValid(Object validity, String upId) {
        // Are they working in this Channel?
        // If so, invalidate the cache
        if (getChannelId(upId).equals(fetchChannelTarget(upId))) return false;
        return true;
    }

    // This method will display the default error page for channels.
    // It handles setting the SSL and populates the xml. So this should
    // be the last action on errors. (i.e. you shouldn't set the xml or
    // stylesheet location after calling this method.
    private void showErrorPage(String upId, String errorMsg) {
        setSSLLocation(upId, "/net/unicon/portal/channels/Error/error.ssl");
        setSheetName(upId, "error");

        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<errorPage><message>"+errorMsg+"</message></errorPage>");
        setXML(upId, xmlSB.toString());
    }

    public void renderXML (ContentHandler out, String upId)
    throws PortalException {

        // Clear out the xsl parameters
        Hashtable ht = getXSLParameters(upId);
        ht.clear();

        // ------------------------------------------------------------------
        // You can use a different sheetname to specify that you want to use
        // a different stylesheet.  This name must match up with the 'title'
        // attribute of one of the processing instructions in your stylesheet
        // list.
        // ------------------------------------------------------------------

        // First reset the errorMsg
        setErrorMsg(upId, "");

        try {
            buildXML(upId);
        } catch (Exception e) {
            throw new PortalException(e);
        }

        // If channel set an error msg, redirect to the global error page.
        String msg = getErrorMsg(upId);
        if (msg != null && !"".equals(msg)) {
            showErrorPage(upId, msg);
        }

        // ---------------------------------------------
        // Get the BrowserInfo associated with the user.
        // This is used to determine which stylesheet
        // will be used for rendering the XML.
        // ---------------------------------------------
        XSLT xsl = new XSLT(this);
        xsl.setTarget(out);

        if (getXML(upId) != null) {
            xsl.setXML(getXML(upId));
        } 
        xsl.setXSL(getSSLLocation(upId), getSheetName(upId),
            getRuntimeData(upId).getBrowserInfo());

        setupXSLParameters(upId);
        xsl.setStylesheetParameters(getXSLParameters(upId));
        xsl.transform();
    }

    public void setupXSLParameters(String upId) throws PortalException {
        Hashtable ht = getXSLParameters(upId);
        ht.put("baseActionURL", getRuntimeData(upId).getBaseActionURL());
    }

    public void buildXML(String upId) throws Exception {}

    public String getChannelId(String upId) {
        return getStaticData(upId).getChannelSubscribeId();
    }

    protected Connection getDBConnection() {
        Connection conn = null;
        try {
            RDBMServices rdbmServices = new RDBMServices();
            return  (rdbmServices.getConnection());
        } catch (Exception e) {
              return  (null);
        }
    }

    protected void releaseDBConnection(Connection connection) {
        try {
          RDBMServices rdbmServices = new RDBMServices();
          rdbmServices.releaseConnection(connection);
        } catch (Exception e) {
        }
    }

    public String getXML(String upId) {
        return getChannelData(upId).getXML();
    }

    public void setXML(String upId, String xml) {
        getChannelData(upId).setXML(xml);
    }

    public String getErrorMsg(String upId) {
        return getChannelData(upId).getErrorMsg();
    }

    public void setErrorMsg(String upId, String errorMsg) {
        getChannelData(upId).setErrorMsg(errorMsg);
    }

    public String getMessage(String upId) {
        return getChannelData(upId).getMessage();
    }

    public void setMessage(String upId, String message) {
        getChannelData(upId).setMessage(message);
    }

    public String getSSLLocation(String upId) {
        return getChannelData(upId).getSSLLocation();
    }

    public void setSSLLocation(String upId, String sslLocation) {
        getChannelData(upId).setSSLLocation(sslLocation);
    }

    public String getSheetName(String upId) {
        return getChannelData(upId).getSheetName();
    }

    public void setSheetName(String upId, String sheetName) {
        getChannelData(upId).setSheetName(sheetName);
        if (sheetName != null) {
            getXSLParameters(upId).put("current_command", sheetName);
        }
    }

    public Hashtable getXSLParameters(String upId) {
        return getChannelData(upId).getXSLParameters();
    }

    public void setXSLParameters(String upId, Hashtable params) {
        getChannelData(upId).setXSLParameters(params);
    }

    public IPerson getUPortalUser(String upId) {
        return getChannelData(upId).getUPortalUser();
    }

    public void setUPortalUser(String upId, IPerson uportalUser) {
        getChannelData(upId).setUPortalUser(uportalUser);
    }

    public Object getChannelAttribute(String upId, Object key) {
        return getChannelDataMap(upId).get(key);
    }

    public void setChannelAttribute(String upId, Object key, Object value) {
        getChannelDataMap(upId).put(key, value);
    }

    private Map getChannelDataMap(String upId) {
        return ((ChannelState)channelStateMap.get(upId)).getChannelData();
    }

    public boolean isInstantiated(String upId) {
        return channelStateMap.get(upId) != null;

    }

    private ChannelData getChannelData(String upId) {
        return (ChannelData)getChannelDataMap(upId).get(CHANNEL_DATA_KEY);
    }

    // Note: Channel data will persist between channel invocations.
    // So for example, if you set an XSL parameter in one invocation and
    // fail to clear it or reset it the next time through, it will remain
    // and be passed to the XSL. For the most part this is ok (i.e.
    // baseActionURL, channelId) because each channel should set the xsl
    // parameters accordingly.
    
    protected class ChannelData {
        private String xml;
        private String errorMsg;
        private String message;
        private String sslLocation;
        private String sheetName;
        private Hashtable xslParams;
        private IPerson uportalUser;
        private PortalControlStructures pcs;

        public String getXML() {
            return xml;
        }

        public void setXML(String xml) {
            this.xml = xml;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSSLLocation() {
            return sslLocation;
        }

        public void setSSLLocation(String sslLocation) {
            this.sslLocation = sslLocation;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public Hashtable getXSLParameters() {
            if (xslParams == null) {
                xslParams = new Hashtable();
            }
            return xslParams;
        }

        public void setXSLParameters(Hashtable xslParams) {
            this.xslParams = xslParams;
        }

        public IPerson getUPortalUser() {
            return uportalUser;
        }

        public void setUPortalUser(IPerson uportalUser) {
            this.uportalUser = uportalUser;
        }

        public PortalControlStructures getPortalControlStructures() { 
            return pcs; 
        } 
    
        public void setPortalControlStructures(PortalControlStructures pcs) { 
            this.pcs = pcs; 
        }
    }
}
