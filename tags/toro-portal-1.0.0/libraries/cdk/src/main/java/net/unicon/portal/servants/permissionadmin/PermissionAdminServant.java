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
package net.unicon.portal.servants.permissionadmin;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.Security;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.servants.IResultsAdapter;
import net.unicon.portal.servants.ServantEvent;
import net.unicon.portal.servants.ServantManager;
import net.unicon.portal.servants.ServantResults;
import net.unicon.portal.servants.ServantType;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.provider.AuthorizationServiceFactoryImpl;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * PermissionAdminServant is an IServant.
 * This will allow other channels to delegate to Permissions Administration
 * at runtime.
 *
 * Master channels should instantiate this channel with the following
 * staticData parameter preset:
 */
public final class PermissionAdminServant
implements IServant, IChannel {

    private boolean started = false;

    private Boolean submitted = new Boolean(false);
    private boolean finished;
    private Boolean groupsOnly = new Boolean(false);
    private Boolean singleSelection = new Boolean(false);
    private ChannelStaticData staticData;
    private ChannelRuntimeData runtimeData;
    private Document permissionsXML;
    private ChannelRuntimeProperties runtimeProperties =
        new ChannelRuntimeProperties();
    private IResultsAdapter resultsAdapter;
    private static IAuthorizationService authorizationService;
    private static IPermissionStore permissionStore;
 
    static {
        try {
            String className = PropertiesManager.getProperty(
                "org.jasig.portal.security.IPermissionStore.implementation");
            permissionStore = (IPermissionStore)
                Class.forName(className).newInstance();
            authorizationService =
                new AuthorizationServiceFactoryImpl().getAuthorization();
        } catch (Exception ae) {
            LogService.instance().log(LogService.ERROR, ae);
        }
    }

    /** Creates new PermissionAdminServant */
    PermissionAdminServant () {
    }

    public boolean isFinished() {
        return finished;
    }

    public Object[] getResults () {
        Object[] results = {submitted};
        return results;
    }
    
    public void setStaticData(ChannelStaticData staticData) {
        this.staticData = staticData;
        groupsOnly = (Boolean)staticData.get("groupsOnly");
        if (groupsOnly == null) {
            groupsOnly = new Boolean(false);
        }
        singleSelection = (Boolean)staticData.get("singleSelection");
        if (singleSelection == null) {
            singleSelection = new Boolean(false);
        }
    }

    public void setRuntimeData(ChannelRuntimeData runtimeData) {
        this.runtimeData = runtimeData;
    }

    public void receiveEvent(PortalEvent event) {
        switch (event.getEventNumber()) {
            case PortalEvent.SESSION_DONE:
            case ServantEvent.RENDERING_DONE:
                staticData = null;
                runtimeData = null;
                permissionsXML = null;
                runtimeProperties = null;
                resultsAdapter = null;
            break;
        }
        // this will clean up the groups selector servant
        ServantManager.sendPortalEvent(this, event);
    }

    public ChannelRuntimeProperties getRuntimeProperties() {
        return runtimeProperties;
    }

    public void renderXML(ContentHandler out)
    throws PortalException {

        try {

            final IPermissible owner = (IPermissible)staticData.get("owner");
            resultsAdapter = new IResultsAdapter() {
                public Object[] adapt(Object[] results)
                throws PortalException {
                    if (results == null) {
                        return new IAuthorizationPrincipal[0];
                    }
                    try {
                        Security security = new Security(owner.getOwnerToken());
                        Object[] adaptedResults = new Object[results.length];
                        for (int i=0; i<adaptedResults.length; i++) {
                            adaptedResults[i] =
                                security.principal((IdentityData)results[i]);
                        }
                        return adaptedResults;
                    } catch (Exception e) {
                        throw new PortalException(e);
                    }
                }
            };

            String command = runtimeData.getParameter("servant_command");

            if ("cancel".equalsIgnoreCase(command)) {
                // cancel the groups servant if it exists
                ServantManager.removeServant(this,
                    ServantType.IDENTITY_SELECTOR);
                finished = true;
                return;
            }

            // first look for a running groups manager servant
            if (ServantManager.hasServant(this,ServantType.IDENTITY_SELECTOR)) {
                setSources();
                ServantResults results = ServantManager.renderServant(this, out,
                    staticData, runtimeData, ServantType.IDENTITY_SELECTOR);

                if (ServantManager.hasServant(this,
                    ServantType.IDENTITY_SELECTOR)) {
                    // the groups manager servant isn't finished yet, return
                    return;
                }

                if (results == null || results.getResults() == null ||
                    results.getResults().length == 0) {
                    // user cancelled out of the group servant
                    finished = true;
                    return;
                }

                IAuthorizationPrincipal[] principals =
                    new IAuthorizationPrincipal[results.getResults().length];
                for (int i=0; i<principals.length; i++) {
                    principals[i] =
                        (IAuthorizationPrincipal)results.getResults()[i];
                }
                staticData.put("principals", principals);
            }

            if (!started) {
                started=true;
                if (staticData.get("principals") == null) {
                    // render groups selection servant

                    ServantManager.createIdentitySelectorServant(this,
                        staticData, null, null, resultsAdapter);

                    // this shouldn't return any results on the first
                    // invocation
                    setSources();
                    ServantManager.renderServant(this, out,
                        staticData, runtimeData, ServantType.IDENTITY_SELECTOR);
                    return;
                }
            }

            if ("update".equalsIgnoreCase(command)) {
                submitted = new Boolean(true);
                finished = true;
                updatePermissions();
                return;
            }

            Document doc = getDocument();
            Hashtable xslParams = new Hashtable(1);
            xslParams.put("baseActionURL", runtimeData.getBaseActionURL());
            xslParams.put("servantId", staticData.getChannelSubscribeId());
            RenderingUtil.renderDocument(this, out, doc,
                "PermissionAdminServant.ssl", "edit",
                runtimeData.getBrowserInfo(), xslParams);
        } catch (Exception e) {
e.printStackTrace();
            LogService.instance().log(LogService.ERROR,
                "PermissionAdminServant::renderXML() failed " +
                "rendering channel.", e);
            throw new PortalException(e);
        }
    }

    private void setSources() {
        runtimeData.put("singleSelection",
            singleSelection.toString().toLowerCase());
        if (groupsOnly.booleanValue()) {
            runtimeData.put("sources", "groupsOnly");
        } else {
        	if(runtimeData.get("sources") == null) { 
        		runtimeData.put("sources", "portal,campus,contact");
        	}
        }
    }

    private void updatePermissions()
    throws AuthorizationException {
        String value;
        String[] targets;
        Activity[] activities = (Activity[])staticData.get("activities");
        IPermissions permissions;
        IGroup group;
        IAuthorizationPrincipal[] principals =
            (IAuthorizationPrincipal[])staticData.get("principals");
        boolean willDo;

	/* DEBUG ONLY 
	   //net.unicon.common.util.debug.DebugUtil.spillParameters(runtimeData,
	   "PermissionAdminServant");
	*/

        IPermissible owner = (IPermissible)staticData.get("owner");
        targets = owner.getTargetTokens();
        for (int tIndex=0; tIndex<targets.length; tIndex++) {
            for (int aIndex=0; aIndex<activities.length; aIndex++) {
                value = (String)runtimeData.getParameter(
                    targets[tIndex] + "-" + activities[aIndex].getHandle());
                if (value != null) {
                    willDo = true;
                } else {
                    willDo = false;
                }
                    
                for (int pIndex=0; pIndex<principals.length; pIndex++) {
                    try {
                        permissions = PermissionsService.instance().
                            getPermissions(targets[tIndex], principals[pIndex]);
                        permissions.setActivity(
                            activities[aIndex].getHandle(), willDo);
                    } catch (Exception e) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("PermissionAdminServant::");
                        sb.append("updatePermissions() ");
                        sb.append("failed rendering channel.");
                        LogService.instance().log(LogService.ERROR,
                            sb.toString());
                        throw new AuthorizationException(sb.toString(), e);
                    }
                }
            }
        }
    }

    private Document getDocument() throws Exception {
        if (permissionsXML == null) {
            permissionsXML = buildDocument();
        }
        return permissionsXML;
    }

    private Document buildDocument()
    throws Exception {
        Map attributes = new HashMap();

        Document doc = DocumentFactory.getNewDocument();

        Node activityNode = null;
        Node targetNode = null;
        Node rootNode = XmlUtils.addNewNode(
            doc, doc, "manifest", attributes, null); 

        boolean value;
        IGroup group;
        IPermissions permissions;
        String label;
        String description;
        String[] targets = null;
        Activity[] activities = (Activity[])staticData.get("activities");
        IAuthorizationPrincipal[] principals =
            (IAuthorizationPrincipal[])staticData.get("principals");
        if (principals == null || principals.length == 0) return doc;

        IPermissible owner = (IPermissible)staticData.get("owner");
        targets = owner.getTargetTokens();

        for (int tIndex=0; tIndex<targets.length; tIndex++) {
            attributes.clear();
            attributes.put("handle", targets[tIndex]);
            attributes.put("label", owner.getTargetName(targets[tIndex]));
            targetNode = XmlUtils.addNewNode(
                doc, rootNode, "target", attributes, null);

            attributes.clear();
            XmlUtils.addNewNode(
                doc, targetNode, "label", attributes, targets[tIndex]);

            for (int aIndex=0; aIndex<activities.length; aIndex++) {
                // set the activity value to 
                // the value of the first principal
                value = principals[0].hasPermission(owner.getOwnerToken(),
                    activities[aIndex].getHandle(), targets[tIndex]);

                attributes.clear();
                attributes.put("handle", activities[aIndex].getHandle());
                attributes.put("allowed", value ? "Y" : "N");

                label = getActivityLabel(owner, activities[aIndex].getHandle());
                description = getActivityDescription(owner,
                    activities[aIndex].getHandle());

                activityNode = XmlUtils.addNewNode(
                    doc, targetNode, "activity", attributes, null);

                attributes.clear();
                XmlUtils.addNewNode(
                    doc, activityNode, "label", attributes, label);
                XmlUtils.addNewNode(
                    doc, activityNode, "description", attributes, description);
            }
        }

        return doc;
    }

    private String getActivityLabel(IPermissible owner, String handle) {
        String activityName = owner.getActivityName(handle);
        int pos = activityName.indexOf(Activity.activityNameSeparator);
        if (pos >= 0) {
            return activityName.substring(0, pos);
        }
        return activityName;
    }

    private String getActivityDescription(IPermissible owner, String handle) {
        String activityName = owner.getActivityName(handle);
        int pos = activityName.indexOf(Activity.activityNameSeparator);
        if (pos >= 0) {
            return activityName.substring(
                pos+Activity.activityNameSeparator.length());
        }
        return activityName;
    }

}
