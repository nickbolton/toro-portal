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

package net.unicon.academus.apps.messaging;

import java.util.ArrayList;
import java.util.List;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.mercury.IMessageFactory;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class MessageCenter {
    private String id;
    private String label;
    private String description;
    private AccessBroker broker;

    public MessageCenter(Element e) {
        Attribute attrId = e.attribute("id");
        if (attrId == null)
            throw new IllegalArgumentException(
                    "Element <message-center> must have an 'id' attribute");
        this.id = attrId.getValue();

        Element e2 = (Element)e.selectSingleNode("label");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <message-center> must contain a <label> element.");
        } else {
            this.label = e2.getText();
        }

        e2 = (Element)e.selectSingleNode("description");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <message-center> must contain a <description> element.");
        } else {
            this.description = e2.getText();
        }

        e2 = (Element)e.selectSingleNode("access-broker");
        if (e2 == null) {
            throw new IllegalArgumentException(
                    "Element <message-center> must contain a <access-broker> element.");
        } else {
            this.broker = AccessBroker.parse(e2);
        }
    }

    public AccessBroker getAccessBroker() { return this.broker; }
    public FactoryInfo createFactory(Principal p) {
        FactoryInfo rslt = null;

        IAccessEntry[] entries = broker.getEntries(p);

        Identity userId = null;
        Identity[] idents = p.getIdentities();
        for (int i = 0; userId == null && i < idents.length; i++) {
        	if (idents[i].getType() == IdentityType.USER)
	        	userId = idents[i];
        }
        
        if (entries != null && entries.length > 0) {

            List atypes = new ArrayList();
            AccessRule[] arules = entries[0].getAccessRules();
            for (int i = 0; i < arules.length; i++)
                if (arules[i].getStatus())
                    atypes.add(arules[i].getAccessType());

            rslt = new FactoryInfo(id, label, description,
                    (AccessType[])atypes.toArray(new AccessType[0]),
                    new CacheableMessageFactory((IMessageFactory)entries[0].getTarget(),
                    		userId.getId(), false));
        }

        return rslt;
    }
    public String getId() { return this.id; }
    public String getLabel() { return this.label; }
    public String getDescription() { return this.description; }
}

