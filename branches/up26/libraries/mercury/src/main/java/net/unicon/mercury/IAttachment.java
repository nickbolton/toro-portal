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

import java.io.InputStream;
import java.io.IOException;

public interface IAttachment {

    /**
     * Get the attachment's identifier.
     * @return Attachment id as int.
     */
    public int getId();

    /**
     * Get the attachment's filename.
     * @return Filename as String.
     */
    public String getName();

    /**
     * Get the attachment's file extenstion.
     * @return The file extension of the attachment's filename, or an empty
     * string if none.
     */
    public String getExtension();

    /**
     * Get the attachment's short filename.
     * @return The filename without the extension.
     */
    public String getShortName();

    /**
     * Get the attachment's content type.
     * @return Content-type (MIME type) as String.
     */
    public String getContentType();

    /**
     * Get a copy of the attachment's InputStream.
     * @return A copy of the attachment's InputStream.
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Get the file size of the attachment.
     * @return File size of the attachment in bytes.
     */
    public long getSize();
}
