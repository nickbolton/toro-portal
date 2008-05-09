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

package net.unicon.portal.channels.classfolder;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.io.File;

import java.io.IOException;

import java.io.InputStream;

import java.util.Date;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.Node;

import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;

import org.jasig.portal.ChannelRuntimeData;

import org.jasig.portal.MultipartDataSource;

import org.jasig.portal.PropertiesManager;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.portal.common.service.file.FileServiceFactory;

import net.unicon.portal.common.service.file.FileService;


import org.jasig.portal.services.LogService;

// Collection of static helper functions

public final class ClassFolderUtil {

    private static final String deleteClassFolderEntriesSQL =

    "DELETE from class_folders where offering_id=?";

    protected static DateFormat df = new SimpleDateFormat("dd-MMM-yy.HH:mm:ss z");

    protected static final int maxFileSize = PropertiesManager.getPropertyAsInt(

    "org.jasig.portal.RequestParamWrapper.file_upload_max_size");

    public static String createXML(String oid, String type, String name, String desc, String href_or_file, String filetype) {

        StringBuffer xmlSB = new StringBuffer();

        String tagtype = null;

        if (type == null) { // should never happen

            return null;

        }

        else if (type.equalsIgnoreCase("folder")) {

            tagtype = "folder";

        }

        else if (type.equalsIgnoreCase("file")) {

            tagtype = "file";

        }

        else {

            tagtype = "url-element";

        }

        xmlSB.append("<");

        xmlSB.append(tagtype);

        xmlSB.append(" folded=\"{");

        xmlSB.append(oid);

        xmlSB.append("}\" oid=\"");

        xmlSB.append(oid);

        xmlSB.append("\" creation-date=\"");

        xmlSB.append(ClassFolderUtil.df.format(new Date()));

        xmlSB.append("\" last-modified=\"");

        xmlSB.append(ClassFolderUtil.df.format(new Date()));

        xmlSB.append("\"><name>");

        xmlSB.append(name);

        xmlSB.append("</name><description>");

        xmlSB.append(desc);

        xmlSB.append("</description>");

        if (tagtype.equals("url-element")) {

            xmlSB.append("<hyperlink>");

            xmlSB.append(href_or_file);

            xmlSB.append("</hyperlink>");

        }

        else if (tagtype.equals("file")) {

            xmlSB.append("<filename>");

            xmlSB.append(href_or_file);

            xmlSB.append("</filename>");

            xmlSB.append("<filetype>");

            xmlSB.append(filetype);

            xmlSB.append("</filetype>");

        }

        xmlSB.append("</");

        xmlSB.append(tagtype);

        xmlSB.append(">");

        return xmlSB.toString();

    }

    public static boolean deleteClassFolders(Offering offering, Connection conn) {

        boolean success = false;

        PreparedStatement pstmt = null;

        int offeringId = (int) offering.getId();

        LogService.instance().log(LogService.INFO,

        "classFolderUtil.deleteClassFolders:offeringID= " + offeringId);

        try {

            int i = 0;

            pstmt = conn.prepareStatement(deleteClassFolderEntriesSQL);

            pstmt.setInt(++i, offeringId);

            pstmt.executeUpdate();

            success = true;

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return success;

    }

    public static String genOID(String prefix, String suffix) {

        StringBuffer newOID = new StringBuffer();

        if (prefix != null) {

            newOID.append(prefix);

        }

        newOID.append("" + System.currentTimeMillis());

        if (suffix != null) {

            newOID.append(suffix);

        }

        // just use millseconds now - not the best but what is the chance

        // of a local collision?

        return newOID.toString();

    }

    /*

    // below is legacy code I was working on to try and gen an oid based

    // on position in the tree but it got too complex and sucked up too

    // much time.

    if (folderElement == null) {

        newOID = newOID + "1"; // all done with oid

    }

    else { // 1

        // get last child and add 1

        Element lastNode = (Element)folderElement.getLastChild();

        // if the Node is null, we are parent_oid-1, else we are +1 of the last sibling

        if (lastNode == null) {

        newOID = newOID + "1";

        }

        else { // 2

        String  lastOID  = lastNode.getAttribute("oid");

        if (lastOID == null) {

            // shouldn't happen

            newOID = newOID + "1";

        }

        else { // 3

            int index = lastOID.indexOf('-');

            if (index < 0) {

            // shouldn't happen

            newOID = newOID + "1";

            }

            else { // 4

            String lastNum = lastOID.substring(index+1);

            if (lastNum == null || lastNum.length() == 0) {

                // shouldn't happen

                newOID = newOID + "1";

            }

            else { // 5

                try {

                int lastValue = parseInt(lastNum);

                newOID = newOID + lastValue+1;

                }

                catch (Exception e1) {

                // error parsing int, punt

                newOID = newOID + "1";

                } // catch

            } // else 5

            } // else 4

        } // else 3

        } // else 2

    } // else 1

    return newOID;

    }

    */

    public static void deleteUploadedFile(String dir, String fname) {

        try {

            // attemt to delete an uploaded file. If we fail, just ignore

            StringBuffer fileSB = new StringBuffer();

            fileSB.append(dir);

            fileSB.append(File.separator);

            fileSB.append(fname);

            File dfile = new File(fileSB.toString());

            dfile.delete();

        }

        catch (Exception exc) {

            exc.printStackTrace();

        }

    }

    // taken from GradebookChannel, should be refactored out

    public static MultipartDataSource uploadFile(String dir,

    String prefix,

    Object uFile) {

        InputStream is = null;

        String filename = null;

        MultipartDataSource content = null;

        try {

        /* store any uploaded files */

            if (uFile == null) {

                return null;

            }

            content = (MultipartDataSource)uFile;

            is = content.getInputStream();

        /* if too many bytes, then get out of here */

            if (is.available() > maxFileSize) {

                return null;

            }

            filename = content.getName();

            String contentType = content.getContentType();

            if (filename == null || "".equals(filename)) {

                return null;

            }

            if (contentType == null || "".equals(contentType)) {

                return null;

            }

            if (is == null) {

                return null;

            }

            // dir needs to be a relative dir

            FileService us = FileServiceFactory.getService();

            us.uploadFile(dir, prefix + '_' + filename, is);

            is.close();

            is = null;

        }

        catch (Exception e) {

            e.printStackTrace();

            return null;

        }

        finally {

            try {

                if (is != null) {

                    is.close();

                }

            }

            catch (IOException ie) { }

        }

        // assume all went well

        return content;

    }

}

