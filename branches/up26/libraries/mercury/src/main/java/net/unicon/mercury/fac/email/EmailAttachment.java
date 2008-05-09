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

package net.unicon.mercury.fac.email;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;

import net.unicon.mercury.fac.AttachmentImpl;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.MercuryException;

/**
 * Attachment adapter for JavaMail.
 *
 * Implements JavaBean Activation Framework's DataSource interface, to allow
 * interoperability between Mercury and JavaMail attachment types.
 *
 * @author eandresen
 * @see AttachmentImpl
 */
public class EmailAttachment extends AttachmentImpl
                             implements DataSource
{
    public EmailAttachment(int id, IAttachment attachment) throws MercuryException {
        super(id, attachment);
    }

    public EmailAttachment(int id, String filename,
                           String mimetype, byte[] contents)
                    throws MercuryException {
        super(id, filename, mimetype, contents);
    }

    public EmailAttachment(int id, String filename,
                           String mimetype, InputStream stream)
                    throws MercuryException {
        super(id, filename, mimetype, stream);
    }

    /* For jaf DataSource */
    /**
     * Unsupported: Return the OutputStream associated with this DataSource.
     * <code>EmailAttachment</code>s are read-only.
     * @throws UnsupportedOperationException always; read-only object.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException(
                    "getOutputStream() not supported on EmailAttachment");
    }

    /**
     * Convienience method to return a String of the data contents.
     * Calls <code>new String(byte[])</code>.
     */
    public String getText() throws MercuryException {
        return new String(super.getBytes());
    }
}

