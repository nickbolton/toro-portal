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

// Java API
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unicon.academus.apps.DownloadURLUtil;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.fac.AbstractResourceFactory;

public class DownloadServlet extends HttpServlet {

    public void doGet (HttpServletRequest req, HttpServletResponse res) {

        doPost(req, res);
    }

    public void doPost (HttpServletRequest req, HttpServletResponse res)  {

        String appName = req.getParameter(DownloadURLUtil.APPNAME_PARAMETER);
        String resourceKey = req.getParameter(DownloadURLUtil.RESOURCE_PARAMETER);

        try {

            if (appName == null || appName.trim().equals("") ||
                resourceKey == null || resourceKey.trim().equals(""))
                throw new IllegalArgumentException("Unable to locate required parameter.");

            // Resource key must be decrypted before use
            EncryptionService service = EncryptionService.getInstance(appName);

            if (service == null)
                throw new IllegalStateException("Unable to locate EncryptionService for appname: "+appName);

            resourceKey = service.decrypt(resourceKey);

            // IResource resource = ResourceFactoryBroker.getResource(decResourceKey);
            IResource resource = AbstractResourceFactory.resourceFromUrl(resourceKey);

            if (resource != null) {

                if (resource instanceof IFile) {

                    // Get the actual resource representation
                    IFile targetFile = (IFile) resource;

                    // Add the necessary meta data of the file to the response
                    prepareResponse(res, targetFile);

                    // Get inputstream from resource and output stream from response
                    InputStream is = targetFile.getInputStream();
                    OutputStream os = res.getOutputStream();

                    // Serve targeted file
                    downloadFile(is, os);

                } else {

                    StringBuffer errorMsg = new StringBuffer(128);

                    errorMsg.append("Resource with resource url '");
                    errorMsg.append(resourceKey);
                    errorMsg.append("' is not a file.");

                    throw new Exception(errorMsg.toString());
                }

            } else {

                StringBuffer errorMsg = new StringBuffer(128);

                errorMsg.append("File with resource url '");
                errorMsg.append(resourceKey);
                errorMsg.append("' was not found.");

                throw new Exception(errorMsg.toString());
            }

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("DownloadServlet: doPost():");
            errorMsg.append("An error occured while trying ");
            errorMsg.append("to download a file.");

            System.out.println(errorMsg.toString());
            e.printStackTrace();

            renderErrorMsg(res, errorMsg.toString());
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
                System.out.println("DownloadServlet:renderErrorMsg(): Failed to render error page.");
                ioe.printStackTrace();
            }
        }
    }

    private void prepareResponse (HttpServletResponse res, IFile file) {

        StringBuffer filenameHeader = new StringBuffer();

        filenameHeader.append("attachment; filename=\"");
        filenameHeader.append(file.getName());
        filenameHeader.append("\"");

        // Set file name and size in the header of the response
        res.setHeader("Content-Disposition",filenameHeader.toString());

        Long lSize = new Long(file.getSize());
        Integer iSize = new Integer(lSize.intValue());
        res.setHeader("Content-Length", iSize.toString());


        // Set the content type in the response
        // TO DO: Mime type is hard-coded
        res.setContentType(file.getContentType());
    }

    private void downloadFile (InputStream source, OutputStream target) {

        byte[] buffer = new byte[8096];
        int size = 0;

        boolean complete = false;

        try {

            // Read bytes from source and write them to target stream
            while (!complete) {

                size = source.read(buffer);

                if (size != -1) {
                    target.write(buffer, 0, size);
                } else {
                    complete = true;
                }
            }

            target.flush();

        } catch (IOException ioe) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("DownloadServlet: downloadFile():");
            errorMsg.append("An error occured while downloading a file.");

            System.out.println(errorMsg.toString());
            ioe.printStackTrace();

        } finally {

            // Close both streams
            try {
                if (source != null)
                     source.close();

                if (target != null)
                     target.close();

            } catch (IOException ioe) {

                StringBuffer errorMsg = new StringBuffer(128);

                errorMsg.append("DownloadServlet: downloadFile():");
                errorMsg.append("An error occured while cleaning up resources.");

                System.out.println(errorMsg.toString());
                ioe.printStackTrace();
            }
        }
    }
}
