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

package net.unicon.academus.apps.briefcase;

import org.dom4j.Attribute;
import org.dom4j.Element;

import net.unicon.alchemist.access.AccessBroker;

public final class Drive {

    // Instance Members.
    private String handle;
    private long maxFileSize;
    private String largeIcon;
    private String openIcon;
    private String closedIcon;
    private String label;
    private String description;
    private String shareTarget;
    private AccessBroker broker;

    /*
     * Public API.
     */

    public static final long NO_MAXIMUM_FILESIZE = 0;

    public static Drive parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("drive")) {
            String msg = "Argument 'e [Element]' must be a <drive> element.";
            throw new IllegalArgumentException(msg);
        }

        // Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
            String msg = "Element <drive> is missing required attribute "
                                                        + "'handle'.";
            throw new IllegalArgumentException(msg);
        }
        String handle = h.getValue();

        // MaxFileSize.
        long maxFileSize = 0L;
        Attribute x = e.attribute("max-upload");
        // NB:  Attribute 'max-upload' is optional.
        if (x != null) {
            try {
                maxFileSize = Long.parseLong(x.getValue());
            } catch (Throwable t) {
                String msg = "Attribute 'max-upload' must be a valid integer.";
                throw new RuntimeException(msg, t);
            }
        }

        // Large Icon.
        Attribute lgi = e.attribute("large-icon");
        if (lgi == null) {
            String msg = "Element <drive> is missing required attribute "
                                                    + "'large-icon'.";
            throw new IllegalArgumentException(msg);
        }
        String largeIcon = lgi.getValue();

        // Open Icon.
        Attribute opi = e.attribute("open-icon");
        if (opi == null) {
            String msg = "Element <drive> is missing required attribute "
                                                    + "'open-icon'.";
            throw new IllegalArgumentException(msg);
        }
        String openIcon = opi.getValue();

        // Closed Icon.
        Attribute cli = e.attribute("closed-icon");
        if (cli == null) {
            String msg = "Element <drive> is missing required attribute "
                                                    + "'closed-icon'.";
            throw new IllegalArgumentException(msg);
        }
        String closedIcon = cli.getValue();

        // Label.
        Element l = (Element) e.selectSingleNode("label");
        if (l == null) {
            String msg = "Element <drive> is missing required child element "
                                                            + "<label>.";
            throw new IllegalArgumentException(msg);
        }
        String label = l.getText();

        // Description.
        Element d = (Element) e.selectSingleNode("description");
        if (d == null) {
            String msg = "Element <drive> is missing required child element "
                                                        + "<description>.";
            throw new IllegalArgumentException(msg);
        }
        String description = d.getText();

        // ShareTarget.
        Attribute s = e.attribute("share-target");
        String shareTarget = null;
        if (s != null) {
            // Not provided means don't share.
            shareTarget = s.getValue();
        }

        // Broker.
        Element b = (Element) e.selectSingleNode("access-broker");
        if (b == null) {
            String msg = "Element <drive> is missing required child element "
                                                    + "<access-broker>.";
            throw new IllegalArgumentException(msg);
        }
        AccessBroker broker = AccessBroker.parse(b);

        return new Drive(handle, maxFileSize, largeIcon, openIcon, closedIcon,
                                label, description, shareTarget, broker);

    }

    public String getHandle() {
        return handle;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public String getOpenIcon() {
        return openIcon;
    }

    public String getClosedIcon() {
        return closedIcon;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSharing() {
        return shareTarget != null;
    }

    public String getShareTarget() {
        return shareTarget;
    }

    public AccessBroker getBroker() {
        return broker;
    }
    
    public boolean equals(Object o){
        if(!(o instanceof Drive)){
            return false;
        }
        
        return ((Drive)o).getHandle().equals(this.getHandle());
    }
    
    public int hashCode(){
        return this.getHandle().hashCode();
    }

    /*
     * Implementation.
     */

    private Drive(String handle, long maxFileSize, String largeIcon,
                    String openIcon, String closedIcon, String label,
                    String description, String shareTarget,
                    AccessBroker broker) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (maxFileSize < 0) {
            String msg = "Argument 'maxFileSize' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }
        if (largeIcon == null) {
            String msg = "Argument 'largeIcon' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (openIcon == null) {
            String msg = "Argument 'openIcon' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (closedIcon == null) {
            String msg = "Argument 'closedIcon' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "Argument 'description' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  shareTarget may be null.
        if (broker == null) {
            String msg = "Argument 'broker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.maxFileSize = maxFileSize;
        this.largeIcon = largeIcon;
        this.openIcon = openIcon;
        this.closedIcon = closedIcon;
        this.label = label;
        this.description = description;
        this.shareTarget = shareTarget;
        this.broker = broker;

    }

}