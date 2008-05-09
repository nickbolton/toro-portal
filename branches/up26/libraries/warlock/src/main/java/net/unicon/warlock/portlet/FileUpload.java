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

package net.unicon.warlock.portlet;

import java.io.File;
import java.io.InputStream;

public final class FileUpload {

    // Instance Members.
    private String name;
    private String path;
    private long size;
    private InputStream contents;
    private String contentType;

    /*
     * Public API.
     */

    public FileUpload(String path, long size, InputStream contents, String contentType) {

        // Assertions.
        if (path == null) {
            String msg = "Argument 'path' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (size < 0) {
            String msg = "Argument 'size' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }
        if (contents == null) {
            String msg = "Argument 'contents' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Instance Members.
        this.name = new File(path).getName();
        this.path = path;
        this.size = size;
        this.contents = contents;
        this.contentType = contentType;

    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public InputStream getContents() {
        return contents;
    }

    public String getContentType() {
        return contentType;
    }

}
