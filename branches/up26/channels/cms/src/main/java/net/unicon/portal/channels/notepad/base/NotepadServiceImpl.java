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

package net.unicon.portal.channels.notepad.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.notepad.*;
import net.unicon.sdk.properties.*;

import net.unicon.sdk.time.*;
import net.unicon.sdk.FactoryCreateException;

/** This class handles all the transactions required by the notepad channel. */

public class NotepadServiceImpl implements NotepadService {

//  old way of getting date
//    private static final String DB_CURRENT_TIME = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.db.DBcurrDate");

    // Offering Notes SQL

    private static final String insertOffNoteSQL =
    (new StringBuffer()
    .append("insert into notepad(offering_id, user_name, update_date, ")
    .append("title, body) values (?, ?, ?, ?, ?)")).toString();

    private static final String updateOffNoteSQL =
    (new StringBuffer()
    .append("update notepad set update_date=?, ")
    .append("title=?, body=? where notepad_id=?")).toString();

    private static final String deleteOffNoteSQL = "delete from notepad where notepad_id=?";

    private static final String deleteAllOffNoteSQL = "delete from notepad where offering_id = ?";

    private static final String getOffNotesSQL =
    "select notepad_id, offering_id, user_name, update_date, title, body from notepad where user_name=? and offering_id=? order by update_date asc";

    private static final String getOffNoteSQL =  "select notepad_id, offering_id, user_name, update_date, title, body from notepad where notepad_id=?";

    /** Constructor */

    public NotepadServiceImpl() { }

    /**
     * Delete the offering note entry from the database.
     * @param noteId the unique notepad id
     * @param conn database connection
     * @return <code>boolean</code> true if the offering note was deleted
     */

    public boolean deleteOfferingNote(int noteId, Connection conn)

    throws SQLException {

        boolean success = false;

        PreparedStatement pstmt = null;

        try {

            pstmt = conn.prepareStatement(deleteOffNoteSQL);

            int i = 0;

            pstmt.setInt(++i, noteId);

            pstmt.executeUpdate();

            success = true;

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            if (pstmt != null) {

                pstmt.close();

                pstmt = null;

            }

        }

        return success;

    }

    public boolean deleteAllOfferingNotes(Offering offering, Connection conn)

    throws SQLException {

        boolean success = false;

        PreparedStatement pstmt = null;

        if (offering == null) return false;

        try {

            pstmt = conn.prepareStatement(deleteAllOffNoteSQL);

            int i = 0;

            pstmt.setLong(++i, offering.getId());

            pstmt.executeUpdate();

            success = true;

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            if (pstmt != null) {

                pstmt.close();

                pstmt = null;

            }

        }

        return success;

    }

    /**
     * Add a new offering note to the database
     * @param note offering note
     * @param conn database connection
     * @return <code>boolean</code> true if the offering note was deleted
     */

    public boolean addOfferingNote(OfferingNote note, Connection conn)
    throws SQLException {
        boolean success = false;
        PreparedStatement pstmt = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(insertOffNoteSQL);

            int i = 0;
            pstmt.setInt(++i, note.getOfferingId());
            pstmt.setString(++i, note.getUsername());
            pstmt.setTimestamp(++i, ts.getTimestamp());
            pstmt.setString(++i, note.getTitle());
            pstmt.setString(++i, note.getMessageBody());

            pstmt.executeUpdate();
            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }

        return success;

    }

    /**
     * Update the offering note entry in the database.
     * @param note offering note
     * @param conn database connection
     * @return <code>boolean</code> true if the offering note was deleted
     */

    public boolean updateOfferingNote(OfferingNote note, Connection conn)
    throws SQLException {

        boolean success = false;
        PreparedStatement pstmt = null;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(updateOffNoteSQL);
            int i = 0;

            pstmt.setTimestamp(++i, ts.getTimestamp());
            pstmt.setString(++i, note.getTitle());
            pstmt.setString(++i, note.getMessageBody());
            pstmt.setInt(++i, note.getId());

            pstmt.executeUpdate();
            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } finally {
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }
        }

        return success;
    }

    /**
     * Get the offering notes from the database.
     * @param user name
     * @param id offering id
     * @param conn database connection
     * @return <code>List</code> list of offering notes
     */

    public List getOfferingNotes(String user, int offeringId , Connection conn)

    throws SQLException {

        List notes = new ArrayList();

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        try {

            pstmt = conn.prepareStatement(getOffNotesSQL);

            int i = 0;

            pstmt.setString(++i, user);

            pstmt.setInt(++i, offeringId);

            rs = pstmt.executeQuery();

            int notepadId = -1;

            int offerId = -1;

            String userName = "";

            Date updateDate = null;

            String title = "";

            String body = "";

            OfferingNote note = null;

            while (rs.next()) {

                notepadId = rs.getInt("notepad_id");

                offerId = rs.getInt("offering_id");

                userName = rs.getString("user_name");

                updateDate = rs.getDate("update_date");

                title = rs.getString("title");

                body = rs.getString("body");

                note = new OfferingNoteImpl(notepadId, title, body,

                updateDate.toString(), userName, offerId);

                notes.add(note);

            }

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            try {
            if (rs != null) {

                rs.close();

                rs = null;

            }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
            if (pstmt != null) {

                pstmt.close();

                pstmt = null;

            }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return notes;

    }

    /**
     * Get the offering note from the database.
     * @param notepad id
     * @param conn database connection
     * @return <code>OfferingNote</code> offering note with the notepad id
     */

    public OfferingNote getOfferingNote(int notepadId , Connection conn)

    throws SQLException {

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        OfferingNote note = null;

        try {

            pstmt = conn.prepareStatement(getOffNoteSQL);

            int i = 0;

            pstmt.setInt(++i, notepadId);

            rs = pstmt.executeQuery();

            int noteId = -1;

            int offeringId = -1;

            String userName = "";

            Date updateDate = null;

            String title = "";

            String body = "";

            if (rs.next() && rs != null) {

                noteId = rs.getInt("notepad_id");

                offeringId = rs.getInt("offering_id");

                userName = rs.getString("user_name");

                updateDate = rs.getDate("update_date");

                title = rs.getString("title");

                body = rs.getString("body");

                note = new OfferingNoteImpl(noteId, title, body,

                updateDate.toString(), userName, offeringId);

            }

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            try {
            if (rs != null) {

                rs.close();

                rs = null;

            }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
            if (pstmt != null) {

                pstmt.close();

                pstmt = null;

            }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return note;

    }

}

