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

package net.unicon.mercury;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Encapsulation of Mercury component features.
 */
public final class Features implements IMercuryEntity
{
    /** Subfolder support. */
    public static final int SUBFOLDERS      =   1;
    /** Attachment support. */
    public static final int ATTACHMENTS     =   2;
    /** Recipients implement IRecipientDetail. */
    public static final int RECIPIENTDETAIL =   4;
    /** Supports a view of all messages in the system. */
    public static final int SYSTEMVIEW =   8;

    private final IMessageFactory owner;
    private final int flags;

    public Features(IMessageFactory owner, int flags) {
        this.owner = owner;
        this.flags = flags;
    }

    public boolean hasFeature(int flag) {
        return (flags & flag) != 0;
    }

    public boolean allowSubfolders() {
        return hasFeature(SUBFOLDERS);
    }

    public boolean allowAttachments() {
        return hasFeature(ATTACHMENTS);
    }

    public IMessageFactory getOwner() {
        return this.owner;
    }

    public String toXml() throws MercuryException {
        StringBuffer buf = new StringBuffer();

        buf.append("<features ");

        // List the current flags for this instance. Use reflection so no one
        // forgets to keep it up to date.
        try {
            Field[] fields = this.getClass().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (Modifier.isStatic(fields[i].getModifiers()) &&
                        this.hasFeature(fields[i].getInt(null)))
                    buf.append(fields[i].getName().toLowerCase())
                       .append("=\"true\" ");
            }
        } catch (Exception e) {}

        buf.append("/>");

        return buf.toString();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        // List the current flags for this instance. Use reflection so no one
        // forgets to keep it up to date.
        try {
            Field[] fields = this.getClass().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (Modifier.isStatic(fields[i].getModifiers())) {
                    buf.append(fields[i].getName()).append("? ")
                       .append(this.hasFeature(fields[i].getInt(null)))
                       .append(" ");;
                }
            }
        } catch (Exception e) {}

        return buf.toString();
    }
}
