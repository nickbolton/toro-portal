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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.unicon.academus.apps.download.DownloadServiceFactory;
import net.unicon.academus.apps.download.DownloadServiceServlet;
import net.unicon.academus.apps.download.IDownloadService;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;

public class AttachmentsHelper {

    /**
     * Prepare attachments from a Message for downloading.
     * This method prepares attachments by registering them with
     * DownloadService and adding the corresponding DownloadService resource id
     * to the user context.
     *
     * @param muc User context
     * @param msg Message to prepare attachments for
     */
    public static void prepareAttachments(MessagingUserContext muc, IMessage msg) {
        try {
            IAttachment[] att = msg.getAttachments();
            String res = null;

            for (int i = 0; i < att.length; i++) {
                String fname = att[i].getName();
                if (fname != null && !fname.trim().equals("")) {
                    // Register for download
                    res = DownloadServiceFactory.getInstance()
                             .registerResource(fname, att[i].getInputStream());

                    // Hold onto the provided resource id
                    muc.addDownloadResource(res, msg, att[i]);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to prepare attachments for message", t);
        }
    }

    /**
     * Clean up prepared attachments. This method unregisters any registered
     * attachments from DownloadService.
     *
     * @param muc User context
     */
    public static void cleanupAttachments(MessagingUserContext muc) {
        String[] res = muc.getDownloadResources();

        IDownloadService ds = DownloadServiceFactory.getInstance();
        for (int i = 0; i < res.length; i++) {
           try {
              ds.unregisterResource(res[i]);
           } catch (IOException ex) {
              ex.printStackTrace();
           }
           muc.removeDownloadResource(res[i]);
        }
    }

    /**
     * Prepare Export XML for download.
     */
    public static void prepareExport(MessagingUserContext muc, String exportXML) {
        final String fname = "messages.xml";

        InputStream is = new ByteArrayInputStream(exportXML.getBytes());

        try {
	        String res = DownloadServiceFactory.getInstance().registerResource(fname, is);
	        muc.setExportResource(res);
        } catch (IOException ex) {
           throw new RuntimeException(
                 "Unable to register resource for download: "+fname, ex);
        }
    }

    /**
     * Get the URL to the export XML.
     */
    public static String getExportURL(MessagingUserContext muc) {
        return DownloadServiceServlet.getResourceURL(muc.getExportResource());
    }

    /**
     * Unregister any registered Export XML.
     */
    public static void cleanupExport(MessagingUserContext muc) {
        String res = muc.getExportResource();
        if (res != null) {
           try {
              DownloadServiceFactory.getInstance().unregisterResource(res);
              muc.setExportResource(null);
           } catch (IOException ex) {
              ex.printStackTrace();
           }
        }
    }
}

