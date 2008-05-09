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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.MultipartStream;

import net.unicon.warlock.WarlockException;

public final class RequestReader {

    private static final FileItemFactory fac = new DefaultFileItemFactory();

    /*
     * Public API.
     */

    public static Map readActionRequest(ActionRequest req)
                            throws WarlockException {

        // Assertions.
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Parameters
        Map rslt = new HashMap(req.getParameterMap());

        // Multipart Content.
        String cType = req.getContentType();
        if (cType != null && cType.startsWith("multipart/")) {
            rslt.putAll(readMultipartContent(req));
        }

        return rslt;

    }

    private static Map readMultipartContent(ActionRequest req)
                                throws WarlockException {

        // Assertions.
        if (req == null) {
            String msg = "Argument 'req' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Map rslt = new HashMap();

        try {

            // Read the boundry marker.
            int index = req.getContentType().indexOf("boundary=");
            if (index < 0) {
                String msg = "Unable to locate multipart boundry.";
                throw new WarlockException(msg);
            }
            byte[] boundary = req.getContentType().substring(index + 9).getBytes();

            // Read the InputStream.
            InputStream input = req.getPortletInputStream();
            MultipartStream multi = new MultipartStream(input, boundary);
            multi.setHeaderEncoding(req.getCharacterEncoding());  // ...necessary?
            boolean hasMoreParts = multi.skipPreamble();
            while (hasMoreParts) {
                Map headers = parseHeaders(multi.readHeaders());
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    String subContentType = (String) headers.get("Content-type".toLowerCase());
                    if (subContentType != null && subContentType.startsWith("multipart/mixed")) {

                        throw new UnsupportedOperationException("Multiple-file request fields not supported.");

/* let's see if we need this...
                        // Multiple files.
                        byte[] subBoundary = subContentType.substring(subContentType.indexOf("boundary=") + 9).getBytes();
                        multi.setBoundary(subBoundary);
                        boolean nextSubPart = multi.skipPreamble();
                        while (nextSubPart) {
                            headers = parseHeaders(multi.readHeaders());
                            if (getFileName(headers) != null) {
                                FileItem item = createItem(headers, false);
                                OutputStream os = item.getOutputStream();
                                try {
                                    multi.readBodyData(os);
                                } finally {
                                    os.close();
                                }
                                rslt.add(item.getFieldName(), item.getInputStream());
                            } else {
                                // Ignore anything but files inside
                                // multipart/mixed.
                                multi.discardBodyData();
                            }
                            nextSubPart = multi.readBoundary();
                        }
                        multi.setBoundary(boundary);
*/
                    } else {
                        if (getFileName(headers) != null) {
                            // A single file.
                            FileItem item = fac.createItem(getFieldName(headers),
                                            (String) headers.get("Content-type".toLowerCase()),
                                            false,
                                            getFileName(headers));
                            OutputStream os = item.getOutputStream();
                            try {
                                multi.readBodyData(os);
                            } finally {
                                os.close();
                            }
                            String path = item.getName().replace('\\', '/');
                            String[] tokens = path.split("/");
                            FileUpload fu = new FileUpload(tokens[tokens.length - 1],
                                            item.getSize(), item.getInputStream(),
                                            item.getContentType());
                            rslt.put(item.getFieldName(), new FileUpload[] { fu });
                        } else {
                            // A form field.
                            FileItem item = fac.createItem(getFieldName(headers),
                                            (String) headers.get("Content-type".toLowerCase()),
                                            true,
                                            null);
                            OutputStream os = item.getOutputStream();
                            try {
                                multi.readBodyData(os);
                            } finally {
                                os.close();
                            }
                            List newEntry = new ArrayList();
                            if (rslt.get(item.getFieldName()) != null) {
                                String[] oldEntry = (String[]) rslt.get(item.getFieldName());
                                newEntry.addAll(Arrays.asList(oldEntry));
                            }
                            newEntry.add(item.getString());
                            rslt.put(item.getFieldName(), newEntry.toArray(new String[0]));
                        }
                    }
                } else {
                    // Skip this part.
                    multi.discardBodyData();
                }
                hasMoreParts = multi.readBoundary();
            }
        } catch (Throwable t) {
            String msg = "Unable to process multipart form data.";
            throw new WarlockException(msg, t);
        }

        return rslt;

    }

    private static Map parseHeaders(String raw) {

        // Assertions.
        if (raw == null) {
            String msg = "Argument 'raw' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Map rslt = new HashMap();
        char buffer[] = new char[1024];
        boolean done = false;
        int j = 0;
        int i;
        String header, headerName, headerValue;
        try {
            while (!done) {
                i = 0;
                // Copy a single line of characters into the buffer,
                // omitting trailing CRLF.
                while (i < 2 || buffer[i - 2] != '\r' || buffer[i - 1] != '\n') {
                    buffer[i++] = raw.charAt(j++);
                }
                header = new String(buffer, 0, i - 2);
                if (header.equals("")) {
                    done = true;
                } else {
                    if (header.indexOf(':') == -1) {
                        // This header line is malformed, skip it.
                        continue;
                    }
                    headerName = header.substring(0, header.indexOf(':'))
                        .trim().toLowerCase();
                    headerValue =
                        header.substring(header.indexOf(':') + 1).trim();
                    if (rslt.get(headerName) != null) {
                        // More that one heder of that name exists,
                        // append to the list.
                        rslt.put(headerName, ((String) rslt.get(headerName)) + ',' + headerValue);
                    } else {
                        rslt.put(headerName, headerValue);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Headers were malformed. continue with all that was
            // parsed.
        }

        return rslt;

    }

    private static String getFieldName(Map headers) {
        String rslt = null;
        String cd = (String) headers.get("Content-disposition".toLowerCase());
        if (cd != null && cd.startsWith("form-data")) {
            int start = cd.indexOf("name=\"");
            int end = cd.indexOf('"', start + 6);
            if (start != -1 && end != -1) {
                rslt = cd.substring(start + 6, end);
            }
        }
        return rslt;
    }

    private static String getFileName(Map headers) {
        String rslt = null;
        String cd = (String) headers.get("Content-disposition".toLowerCase());
        if (cd.startsWith("form-data") || cd.startsWith("attachment")) {
            int start = cd.indexOf("filename=\"");
            int end = cd.indexOf('"', start + 10);
            if (start != -1 && end != -1) {
                rslt = cd.substring(start + 10, end).trim();
            }
        }
        return rslt;
    }

}
