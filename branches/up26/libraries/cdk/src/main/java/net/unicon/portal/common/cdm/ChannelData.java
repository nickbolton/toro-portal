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
package net.unicon.portal.common.cdm;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.IProducer;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.permissions.IPermissions;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.services.LogService;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Document;
import net.unicon.portal.common.ChannelState;

public class ChannelData {
    private String charBuffer;
    private SAX2BufferImpl saxBuffer;
    private String xml;
    private ChannelClass channelClass;
    private ChannelDefinition channelDefinition;
    private String errorMsg;
    private Document document;
    private String sslLocation;
    private String sheetName;
    private Hashtable xslParams;
    private IPerson uportalUser;
    private String domainUsername;
    private boolean dirty;
    private Map attributes = new HashMap();
    private ChannelStaticData staticData;
    private ChannelRuntimeData runtimeData;
    private IPermissions permissions;
    private boolean contextChanged;
    private ChannelState currentState;
    private IProducer producer;
    private IServant servant;
    private IAuthorizationPrincipal authorizationPrincipal;
    private PortalControlStructures pcs;

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }
    public void putAttribute(Object key, Object value) {
        attributes.put(key, value);
    }
    public void removeAttribute(Object key) {
        attributes.remove(key);
    }
    public String getXML() {
        return xml;
    }
    public void setXML(String xml) {
        this.xml = xml;
    }
    public ChannelClass getChannelClass() {
        return channelClass;
    }
    public void setChannelClass(ChannelClass channelClass) {
        this.channelClass = channelClass;
    }
    public ChannelDefinition getChannelDefinition() {
        return channelDefinition;
    }
    public void setChannelDefinition(ChannelDefinition channelDefinition) {
        this.channelDefinition = channelDefinition;
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getCharacterBuffer() {
        return charBuffer;
    }
    public void setCharacterBuffer(String charBuffer) {
        this.charBuffer = charBuffer;
    }
    public SAX2BufferImpl getSAXBuffer() {
        return saxBuffer;
    }
    public void setSAXBuffer(SAX2BufferImpl saxBuffer) {
        this.saxBuffer = saxBuffer;
    }
    public Document getDocument() {
        return document;
    }
    public void setDocument(Document document) {
        this.document = document;
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
    public User getDomainUser() {
        User domainUser = null;
        try {
            domainUser = UserFactory.getUser(domainUsername);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR,
            "Unable to retrieve user from factory with username = : " +
            domainUsername);
        }
        return domainUser;
    }
    public void setDomainUser(User domainUser) {
        this.domainUsername = domainUser.getUsername();
    }
    public boolean isDirty() {
        return dirty;
    }
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    public ChannelStaticData getStaticData() {
        return staticData;
    }
    public void setStaticData(ChannelStaticData staticData) {
        this.staticData = staticData;
    }
    public ChannelRuntimeData getRuntimeData() {
        return runtimeData;
    }
    public void setRuntimeData(ChannelRuntimeData runtimeData) {
        this.runtimeData = runtimeData;
    }
    public IPermissions getPermissions() {
        return permissions;
    }
    public void setPermissions(IPermissions p) {
        this.permissions = p;
    }
    public boolean isContextChanged() {
        return contextChanged;
    }
    public void setContextChanged(boolean b) {
        this.contextChanged = b;
    }
    public ChannelState getCurrentState() {
        return currentState;
    }
    public void setCurrentState(ChannelState currentState) {
        this.currentState = currentState;
    }
    public IProducer getProducer() {
        return producer;
    }
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }
    public IServant getServant() {
        return servant;
    }
    public void setServant(IServant servant) {
        this.servant = servant;
    }
    public IAuthorizationPrincipal getAuthorizationPrincipal() {
        return authorizationPrincipal;
    }
    public void setAuthorizationPrincipal(IAuthorizationPrincipal principal) {
        this.authorizationPrincipal = principal;
    }
    public PortalControlStructures getPortalControlStructures() {  
        return pcs;  
    }  
    public void setPortalControlStructures(PortalControlStructures pcs) {  
        this.pcs = pcs;  
    }  
}
