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

package net.unicon.portal.common.service.channel;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.classfolder.ClassFolders;

import net.unicon.portal.channels.notes.Note;



import java.util.List;

import java.util.Map;

import java.sql.*;

import org.w3c.dom.Document;

public interface ChannelService {
    public List getAllNotes(

    int userID,

    Connection conn) throws SQLException;

    public Note getNote(

    int noteID,

    Connection conn) throws SQLException;

    public boolean addNote(

    int userID,

    String body,

    Connection conn) throws SQLException;

    public boolean deleteNote(

    int noteID,

    Connection conn) throws SQLException;

    public boolean updateNote(

    int noteID,

    String body,

    Connection conn) throws SQLException;

    // Added by KG to support RelatedResources/ClassFolders

    /** Adds a folder/document/url to the class folder for this offering by adding the item at the end of the specified path. */

    public String addClassFolder(

    Offering offering,

    String oid,

    String type,

    String name,

    String desc,

    String href_or_file,

    Map args,

    Object uFile,

    Connection conn);

    /**
     * Deletes a folder/document/url from the class folder. If the object
     * is a folder, ensures all items underneath it's subtree are removed.
     */

    public String deleteClassFolder(

    Offering offering,

    String oid,

    Connection conn);

    /** Update the item at the end of the specified path with the new item */

    public String updateClassFolder(

    Offering offering,

    String oid,

    String name,

    String desc,

    String url,

    Connection conn);

    /** toggle the folded = Y/N flag on the folder and all subelements */

    public String toggleClassFolder(

    Offering offering,

    Map args,

    String oid,

    Connection conn);

    /** Get the class folders associated with an offering */

    public ClassFolders getClassFolder(

    Offering offering,

    Connection conn);

    /** Get the filename associcated with the given oid. */

    public String getFileNameByOID(Offering offering,

    String oid,

    Connection conn) throws Exception;

}

