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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import net.unicon.mercury.IAttachment;
import net.unicon.mercury.MercuryException;

public class AttachmentImpl implements IAttachment
{
    private final int id;
    private final String filename;
    private final String mimetype;
    private final byte[] buf;

    /**
     * Construct a new IAttachment from the contents of another IAttachment.
     *
     * @param id Identifier for the new attachment
     * @param attachment Mercury IAttachment object to clone
     *
     * @throws MercuryException if the operation is unsuccessful
     */
    public AttachmentImpl(int id, IAttachment attachment)
                          throws MercuryException {
        this.id = id;
        this.filename = attachment.getName();
        this.mimetype = attachment.getContentType().toLowerCase();
        try {
            this.buf = buffer(attachment.getInputStream());
        } catch (IOException e) {
            throw new MercuryException("Unable to clone attachment.", e);
        }
    }

    public AttachmentImpl(int id, String filename,
                          String mimetype, byte[] contents) {
        this.id = id;
        this.filename = filename;
        this.mimetype = mimetype.toLowerCase();
        this.buf = contents;
    }

    public AttachmentImpl(int id, String filename,
                          String mimetype, InputStream stream)
                          throws MercuryException {
        this.id = id;
        this.filename = filename;
        this.mimetype = mimetype.toLowerCase();
        this.buf = buffer(stream);
    }

    public int getId() { return this.id; }

    public String getShortName() {
        String rslt = "";
        int i = filename.lastIndexOf('.');

        if (i > 0)
            rslt = filename.substring(0, i);

        return rslt;
    }

    public String getExtension() {
        String rslt = "";
        int i = filename.lastIndexOf('.');

        if (i >= 0)
            rslt = filename.substring(i);

        return rslt;
    }

    public String getName() {
        return this.filename;
    }

    public String getContentType() {
        return this.mimetype;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.buf);
    }

    public long getSize() {
        return this.buf.length;
    }

    /*
     * Protected API.
     */

    protected byte[] getBytes() {
        return this.buf;
    }

    protected byte[] buffer(InputStream stream) throws MercuryException {
        if (stream == null)
            return new byte[0];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int r = 0;

        try {
            while ((r = stream.read(b)) >= 0) {
                bos.write(b, 0, r);
            }
        } catch (IOException ioe) {
            throw new MercuryException("Error reading attachment content", ioe);
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                throw new MercuryException("Error reading attachment content", ioe);
            }
        }

        return bos.toByteArray();
    }
}

