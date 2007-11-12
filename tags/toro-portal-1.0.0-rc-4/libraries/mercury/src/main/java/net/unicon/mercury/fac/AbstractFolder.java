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

package net.unicon.mercury.fac;

import java.util.List;

import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
import net.unicon.alchemist.EntityEncoder;

public abstract class AbstractFolder extends AbstractMercuryEntity
                                     implements IFolder {
    
    // Instance Members
    private long id;
    private String label;
    private IFolder parent;

    /*
     * Public API.
     */

    public AbstractFolder(IMessageFactory owner, long id, String label) {
        this(owner, id, label, null);
    }

    public AbstractFolder(IMessageFactory owner, long id, String label,
                            IFolder parent) {
        super(owner);

        // Assertions.
        if (id < 0) {
            String msg = "Argument 'id' cannot be negative.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null || label.trim().length() == 0) {
            String msg = "Argument 'label' cannot be null or empty.";
            throw new IllegalArgumentException(msg);
        }

        this.id = id;
        this.label = label;
        this.parent = parent;
    }

    public long getId() {
        return this.id;
    }

    public String getIdString() {
        return String.valueOf(this.id);
    }

    public String getLabel() {
        return this.label;
    }

    public abstract void addMessage(IMessage msg) throws MercuryException;

    public abstract boolean removeMessage(IMessage msg) throws MercuryException;

    public abstract void expunge() throws MercuryException;

    public abstract IMessage[] getMessages() throws MercuryException;
 
    public abstract IFolder[] getSubfolders() throws MercuryException;

    public abstract int getUnreadCount() throws MercuryException;

    public IFolder getParent() throws MercuryException {
        return parent;
    }

    public StringBuffer toXml(StringBuffer out) throws MercuryException {
        // Begin.
        out.append("<folder ");

        // Id.
        out.append("id=\"")
           .append(this.getIdString())
           .append("\">");

        // Folder label.
        out.append("<label>")
           .append(EntityEncoder.encodeEntities(this.getLabel()))
           .append("</label>");

        // Subfolders.
        IFolder[] subFolders = this.getSubfolders();
        for (int i = 0; i < subFolders.length; i++) {
            ((AbstractMercuryEntity)subFolders[i]).toXml(out);
        }

        // End
        out.append("</folder>");

        return out;
    }

    /*
     * Protected API.
     */

    /**
     * Set the parent reference of the folder.
     * @param parent Parent reference or null for root folders
     */
    protected void setParent(IFolder parent) {
        this.parent = parent;
    }

}
