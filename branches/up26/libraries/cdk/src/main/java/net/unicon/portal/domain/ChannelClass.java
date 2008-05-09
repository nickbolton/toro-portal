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
package net.unicon.portal.domain;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.jasig.portal.services.LogService;

import net.unicon.academus.domain.DomainException;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ActivityFactory;
import net.unicon.sdk.util.XmlUtils;

/** A class of channels within the portal-offering ecosystem. */
public final class ChannelClass {
    /* Instance Members */
    private String handle           = null;
    private String className        = null;
    private String producer         = null;
    private ChannelType channelType = null;
    private String sslLocation      = null;
    private long timeout            = 0L;
    private String label            = null;
    private String description      = null;
    private boolean cscrEnabled     = false;
    private List activities         = null;
    private Element permissionsElement = null;
    private ChannelMode channelMode = null;

    ChannelClass(Element manifest)
    throws DomainException {

        try {
            // Getting attribute
            this.label = getLabel(manifest);
            this.handle = manifest.getAttribute("handle");
            this.className = getClassName(manifest);
            this.producer = getProducer(manifest);
            this.sslLocation = getSSLLocation(manifest);
            this.timeout = Long.parseLong(getTimeout(manifest));
            this.channelType = getChannelType(manifest);
            this.description = getDescription(manifest);
            this.cscrEnabled = getCscrEnabled(manifest);
            this.channelMode =
            ChannelMode.getChannelMode(manifest.getAttribute("mode"));
            /* Building activity List */
            if (producer == null || "".equals(producer)) {
                permissionsElement = manifest;
                activities = new ArrayList(Arrays.asList(ActivityFactory.getActivities(manifest)));
            } else {
                String permissionsFilePath = Class.forName(producer).
                    getResource("permissions.xml").toString();
                permissionsElement = XmlUtils.parse(permissionsFilePath, false).
                    getDocumentElement();
                activities = new ArrayList(Arrays.asList(ActivityFactory.getActivities(permissionsElement)));
            }
        } catch (Exception e) {
            throw new DomainException(e);
        }
    }
    /**
     * Provides the handle for this channel class.
     * @return the channel class name.
     */
    public String getHandle() {
        return handle;
    }
    public String getPermissionsOwner() {
        if (producer == null || "".equals(producer)) {
            return className;
        } else {
            return producer;
        }
    }
    /**
     * Provides the class name for this channel class.
     * @return the channel class name.
     */
    public String getClassName() {
        return className;
    }
    /**
     * Provides the Producer name for this channel class.
     * @return the channel class producer.
     */
    public String getProducer() {
        return producer;
    }
    /**
     * Provides the ssl location for this channel class.
     * @return the channel class ssl location.
     */
    public String getSSLLocation() {
        return sslLocation;
    }
    /**
     * Provides the ssl location for this channel class.
     * @return the channel class ssl location.
     */
    public long getTimeout() {
        return timeout;
    }
    /**
     * Provides the ChannelType for this channel class.
     * @return the ChannelType object.
     */
    public ChannelType getChannelType() {
        return channelType;
    }
    /**
     * Provides the label for this channel class.
     * @return the channel class label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Provides the description for this channel class.
     * @return the channel class description.
     */
    public String getDescription() {
        return description;
    }

    public boolean isCscrEnabled() {
        return cscrEnabled;
    }

    public int hashCode() {
        return handle.hashCode();
    }

    /**
     * Provides the set of activities exposed by this channel class.
     * These correspond directly to the activities one can have
     * within any channel. They also contain the default settings used when a new <code>Permissions</code> object is created.
     * Channel Activities are defined in the manifest.xml file.
     * @return the set of activities for this channel.
     */
    public List getActivities() {
        return new ArrayList(activities);
    }

    public void addActivity(Activity a) {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a [Activity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Add to activities collection.
        if (!activities.contains(a)) {
            activities.add(a);
        }

        // Add to DOM as well.
        Document doc = permissionsElement.getOwnerDocument();
        Element e = (Element) permissionsElement.getElementsByTagName("permissions").item(0);

        Element lbl = doc.createElement("label");
        lbl.appendChild(doc.createTextNode(a.getLabel()));

        Element desc = doc.createElement("description");
        desc.appendChild(doc.createTextNode(a.getDescription()));

        Element act = doc.createElement("activity");
        act.setAttribute("handle", a.getHandle());
        act.appendChild(lbl);
        act.appendChild(desc);

        Map def = a.getDefaults();
        Iterator it = (def.keySet().iterator());
        while (it.hasNext()) {
            String role = (String) it.next();
            Element set = doc.createElement("setting");
            set.setAttribute("role", role);
            set.setAttribute("value", def.get(role).toString());
            act.appendChild(set);
        }

        e.appendChild(act);

    }

    /**
     * Provides an XML <code>Element</code> that represents a <code>ChannelClass</code>.
     * @return the <code>Element</code> that represents this <code>ChannelClass</code>.
     */
    public Element getElement() {
        return permissionsElement;
    }
    /**
     * Provides a <code>Map</code> that contains all the default activities for this <code>ChannelClass</code>.
     * @return the <code>Map</code> that contains the default activities for this <code>ChannelClass</code>.
     */
    public ChannelMode getChannelMode() {
        return channelMode;
    }
    private boolean getCscrEnabled(Element node) {
        return "true".equalsIgnoreCase(
            getNodeValueFromElement(node, "cscrEnabled"));
    }

    private ChannelType getChannelType(Element node) {
        ChannelType channelType = null;
        try {
           channelType = ChannelType.getChannelType(
                getNodeValueFromElement(node, "type"));
        } catch (DomainException e) {
            LogService.log(LogService.ERROR,
                "ChannelClass::getChannelType : Failed to get channel type");

        }
        return channelType;
    }
    private String getLabel(Element node) {
        return getNodeValueFromElement(node, "label");
    }
    private String getProducer(Element node) {
        return getNodeValueFromElement(node, "producer");
    }
    private String getSSLLocation(Element node) {
        return getNodeValueFromElement(node, "sslLocation");
    }
    private String getTimeout(Element node) {
        return getNodeValueFromElement(node, "timeout");
    }
    private String getClassName(Element node) {
        return getNodeValueFromElement(node, "className");
    }
    private String getDescription(Element node) {
        return getNodeValueFromElement(node, "description");
    }
    private String getNodeValueFromElement(Element element, String nodeName) {
        Node lblNode = element.getElementsByTagName(nodeName).item(0);
        if (lblNode == null ||
            lblNode.getFirstChild() == null ||
            lblNode.getFirstChild().getNodeValue() == null) return "";
        return lblNode.getFirstChild().getNodeValue();
    }
}
