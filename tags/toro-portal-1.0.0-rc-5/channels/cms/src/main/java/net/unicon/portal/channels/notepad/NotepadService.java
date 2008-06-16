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

package net.unicon.portal.channels.notepad;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.notes.Note;

import net.unicon.portal.channels.notepad.OfferingNote;



import java.util.List;

import java.sql.*;

public interface NotepadService {

    /**
     * Get the list of offering note objects for the user per offering.
     * @param user_name the user name
     * @param offeringId the offering assoicated with the notes
     * @param conn the database connection
     * @return <code>List</code> list of offering objects
     */

    public List getOfferingNotes(String user_name, int offeringId,

    Connection conn) throws SQLException;

    /**
     * Get the offering note associated with the notepad id.
     * @param noteID notepad id for the note
     * @param conn database connection
     * @return <code>OfferingNote</code> offering note with the notepad id
     */

    public OfferingNote getOfferingNote(int noteID, Connection conn)

    throws SQLException;

    /**
     * Add a new offering note to the database.
     * @param offeringNote offering note to be added to the database
     * @param conn database connection
     * @return <code>boolean</code> true if the add was successful
     */

    public boolean addOfferingNote(OfferingNote offeringNote, Connection conn)

    throws SQLException;

    /**
     * Delete an offering note with the notepad id from the database.
     * @param noteID the notepad id
     * @param conn database connection
     * @return <code>boolean</code> true if the note was deleted succesfully
     */

    public boolean deleteOfferingNote(int noteID, Connection conn)

    throws SQLException;

    /**
     * Delete all offering note with the notepad id from the database.
     * @param offering - offering
     * @param conn database connection
     * @return <code>boolean</code> true if the note was deleted succesfully
     */

    public boolean deleteAllOfferingNotes(Offering offering, Connection conn)

    throws SQLException;

    /**
     * Update an offering note.
     * @param offeringNote note that is to be updated
     * @param conn database connection
     * @return <code>boolean</code> true if the update was successful
     */

    public boolean updateOfferingNote(OfferingNote offeringNote, Connection conn)

    throws SQLException;

}

