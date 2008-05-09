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

package net.unicon.academus.apps.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unicon.academus.apps.download.IDownloadService.NameResourcePair;
import net.unicon.alchemist.MimeTypeMap;

public class DownloadServiceServlet extends HttpServlet {
   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(DownloadServiceServlet.class);
   
   private static final String RESOURCE_PARAMETER = "id";
   private IDownloadService service;
    
    public DownloadServiceServlet() {
       this.service = DownloadServiceFactory.getInstance();
    }

    public static String getResourceURL(String id) {
        StringBuffer url = new StringBuffer();

        try {
            // TODO: Use a configuration of some sort in the future.
            url.append("/toro-portlets-common/downloadService?id=")
                .append(URLEncoder.encode(id, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode resource url", e);
        }

        return url.toString();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        String resourceId = req.getParameter(RESOURCE_PARAMETER);

        try {
            if (resourceId == null || resourceId.trim().equals(""))
                throw new IllegalArgumentException("Unable to locate required parameter.");

            NameResourcePair nrp = service.getResource(resourceId);
            if (nrp != null) {

                // Add the necessary meta data of the file to the response
                prepareResponse(res, nrp);

                // Get inputstream from resource and output stream from response
                InputStream is = nrp.getResource();
                OutputStream os = res.getOutputStream();

                // Serve targeted file
                downloadFile(is, os);

            } else {
                throw new IllegalArgumentException(
                        "The requested resource was not found: "+resourceId);
            }

        } catch (Exception e) {
            log.error("Error downloading resource: "+resourceId, e);
            final String errorMsg =
                    "DownloadServiceServlet: An error occured while trying to "
                  + "download the requested file. This may because an expired "
                  + "resource was requested.";
            renderErrorMsg(res, errorMsg);
        }
    }

    private void renderErrorMsg (HttpServletResponse res, String errorMsg) {

        // Check if response is not already committed before
        // writing out the error page

        if (!res.isCommitted()) {

            StringBuffer errorPage = new StringBuffer(256);

            errorPage.append("<html>");
            errorPage.append("<head><title>");
            errorPage.append("Download failure");
            errorPage.append("</title></head>");
            errorPage.append("<body>");
            errorPage.append(errorMsg);
            errorPage.append("</body>");
            errorPage.append("</html>");

            try {
                res.getWriter().write(errorPage.toString());
            } catch (IOException ioe) {
               log.error("Failed to render error page", ioe);
            }
        }
    }

    private void prepareResponse (HttpServletResponse res, NameResourcePair nrp) {

        StringBuffer filenameHeader = new StringBuffer();

        filenameHeader.append("attachment; filename=\"");
        filenameHeader.append(nrp.getName());
        filenameHeader.append("\"");

        // Set file name and size in the header of the response
        res.setHeader("Content-Disposition",filenameHeader.toString());
        res.setHeader("Content-Length", Long.toString(nrp.getSize()));

        res.setContentType(MimeTypeMap.getContentType(nrp.getName()));
    }

    private void downloadFile (InputStream source, OutputStream target) {

        byte[] buffer = new byte[8096];
        int r = 0;

        try {

            // Read bytes from source and write them to target stream
            while ((r = source.read(buffer)) != -1) {
                target.write(buffer, 0, r);
            }
            target.flush();

        } catch (IOException ioe) {
            log.error("DownloadServiceServlet: downloadFile(): An error occured "
                  + "while transferring the file.", ioe);
        } finally {

            // Close both streams
            try {
                if (source != null)
                     source.close();

                if (target != null)
                     target.close();

            } catch (IOException ioe) {
                log.error("DownloadServiceServlet: downloadFile(): An error occured "
                      + "while cleaning up resources.", ioe);

            }
        }
    }
}
