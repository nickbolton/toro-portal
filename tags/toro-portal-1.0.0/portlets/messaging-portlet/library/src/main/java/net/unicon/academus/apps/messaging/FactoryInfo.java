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

import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessType;
import net.unicon.mercury.Features;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.SpecialFolder;
import net.unicon.warlock.WarlockException;

public class FactoryInfo {
    private final String id;
    private final String label;
    private final String description;
    private final AccessType[] atypes;
    private final IMessageFactory factory;

    public FactoryInfo(String id, String label, String desc,
                       AccessType[] atypes, IMessageFactory factory) {
        this.id = id;
        this.label = label;
        this.description = desc;
        this.atypes = atypes;
        this.factory = factory;
    }

    public IMessageFactory getFactory() { return this.factory; }
    public String getId() { return this.id; }
    public String getLabel() { return this.label; }
    public String getDescription() { return this.description; }
    public AccessType[] getAccessTypes() { return this.atypes; }

    public boolean hasAccessType(AccessType a) {
        boolean found = false;
        for (int i = 0; !found && i < atypes.length; i++)
            if (atypes[i].equals(a))
                found = true;
        return found;
    }

    public String toXml() throws WarlockException {
        return this.toXml(false);
    }

    public String toXml(boolean showFolders) throws WarlockException {
        StringBuffer rslt = new StringBuffer();

        rslt.append("<account id=\"")
            .append(getId())
            .append("\">");

        rslt.append("<label>")
            .append(getLabel())
            .append("</label>");

        rslt.append("<description>")
            .append(getDescription())
            .append("</description>");

        IFolder f = null;
        if (showFolders) {
            try {
                f = this.factory.getSpecialFolder(SpecialFolder.INBOX);
                if (f != null)
                    folderXml(f, rslt);

                f = this.factory.getSpecialFolder(SpecialFolder.OUTBOX);
                if (f != null)
                    folderXml(f, rslt);

                f = this.factory.getSpecialFolder(SpecialFolder.SAVE);
                if (f != null)
                    folderXml(f, rslt);
                
                // add the System view Folder if user has permission and also 
                // if the factory supports this feature
                if(getFactory().getFeatures().hasFeature(Features.SYSTEMVIEW)
                        && hasAccessType(MessagingAccessType.VIEW_ALL)){
	                rslt.append("<folder id=\"")
	                	.append(EntityEncoder.encodeEntities(SpecialFolder.SYSFOLDER.getLabel()))
	                	.append("\"><label>")
	                	.append(EntityEncoder.encodeEntities(SpecialFolder.SYSFOLDER.getLabel()))
	                	.append("</label></folder>");
                }
                
            } catch (MercuryException me) {
                throw new WarlockException("Failed to query MessageFactory folder structure.", me);
            }
        }

        rslt.append("</account>");

        return rslt.toString();
    }

    private void folderXml(IFolder f, StringBuffer rslt) throws MercuryException {
        rslt.append("<folder id=\"")
            .append(EntityEncoder.encodeEntities(f.getIdString()))
            .append("\"><label>")
            .append(EntityEncoder.encodeEntities(f.getLabel()))
            .append("</label></folder>");
    }
}

