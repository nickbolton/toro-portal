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

import java.util.ArrayList;

import java.util.List;

import java.util.Comparator;

import java.util.Collections;

import java.sql.Connection;

import java.sql.SQLException;

import java.io.InputStream;

import java.io.ByteArrayInputStream;

import java.io.StringWriter;

import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.xml.serialize.BaseMarkupSerializer;

import org.apache.xml.serialize.XHTMLSerializer;

import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.BaseOfferingSubChannel;

import net.unicon.portal.channels.notepad.base.OfferingNoteImpl;





import net.unicon.portal.common.PortalObject;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.channels.notepad.OfferingNote;

import net.unicon.portal.channels.notepad.NotepadService;

import net.unicon.portal.channels.notepad.NotepadServiceFactory;

import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.ChannelRuntimeData;

/**
 * Notepad channel where the user can enter notes for the offering.  This is
 * offering specific.  User's can enter notes per offering basis.
 */

public class NotepadChannel extends BaseOfferingSubChannel {

    public NotepadChannel() {

        super();

    }

    /**
     * Build the XML for the offering notes that the user has for an offering.
     * @param upId channel information
     */

    public void buildXML(String upId) throws Exception {

        net.unicon.portal.util.Debug.instance().markTime();

        net.unicon.portal.util.Debug.instance().timeElapsed(

        "BEGIN BUILDING NotepadChannel", true);

        //super.setSSLLocation(upId, "NotepadChannel.ssl");
	ChannelDataManager.setSSLLocation(upId, 
	      ChannelDataManager.getChannelClass(upId).getSSLLocation());

        int guestID = getUPortalUser(upId).getID();

        NotepadService notepadService = NotepadServiceFactory.getService();

        List notes = null;

        super.setSheetName(upId, "main");

        ChannelRuntimeData runtimeData = getRuntimeData(upId);

        User user = super.getDomainUser(upId);

        String userName = user.getUsername();

        Context context = user.getContext();

        Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);

        String command = runtimeData.getParameter("command");

        OfferingNote offeringNote = null;

        /* Getting Database Connection */

        Connection conn = null;

        try {

            conn = super.getDBConnection();
            if (command != null) {

                if (command.equals("add")) {

                /* add code */

                    super.setSheetName(upId, "add");

                } else if (command.equals("edit")) {

                /* edit code */

                    super.setSheetName(upId, "edit");

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        notes = new ArrayList();

                        notes.add(notepadService.getOfferingNote(noteID, conn));

                    }

                } else if (command.equals("view")) {

                /* edit code */

                    super.setSheetName(upId, "view");

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        notes = new ArrayList();

                        notes.add(notepadService.getOfferingNote(noteID, conn));

                    }

                } else if (command.equals("delete")) {

                /* delete code */

                    super.setSheetName(upId, "delete");

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        //notepadService.deleteOfferingNote(noteID, conn);

                        notes = new ArrayList();

                        notes.add(notepadService.getOfferingNote(noteID, conn));

                    }

                } else if (command.equals("confirm")) {

                /* confirm delete code */

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        notepadService.deleteOfferingNote(noteID, conn);

                    }

                } else if (command.equals("insert")) {

                /* insert code */

                    String message = runtimeData.getParameter("message");

                    message = stripScripts(message);

                    String title = runtimeData.getParameter("title");

                    String date = ""; // sql will set the date

                    offeringNote = new OfferingNoteImpl(-1, title, message, date,

                    userName, (int)offering.getId());

                    notepadService.addOfferingNote(offeringNote, conn);

                } else if (command.equals("submit")) {

                /* submit code */

                    String noteIDString = runtimeData.getParameter("ID");

                    String message = runtimeData.getParameter("message");

                    message = stripScripts(message);

                    String title = runtimeData.getParameter("title");

                    String date = ""; // sql will set the date

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        offeringNote = new OfferingNoteImpl(noteID, title, message,

                        date, userName, (int)offering.getId());

                        notepadService.updateOfferingNote(offeringNote, conn);

                    }

                }

                if (!command.equals("edit") && !command.equals("view") &&

                !command.equals("delete")) {

                    notes = notepadService.getOfferingNotes(userName,

                    (int)offering.getId(), conn);

                }

            } else {

                notes = notepadService.getOfferingNotes(userName,

                (int)offering.getId(), conn);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        } finally {
            // Releasing DB Connection
            super.releaseDBConnection(conn);
        }

        String cmdSort = runtimeData.getParameter("sortby");

        String order = runtimeData.getParameter("order");

        // The list is sorted by date naturally by SQL

        if ("title".equals(cmdSort)) {

            getXSLParameters(upId).put("sortby", "title");

            if ("asc".equals(order)) {

                Collections.sort(notes, new TitleAscComparator());

                getXSLParameters(upId).put("order", "asc");

            } else {

                Collections.sort(notes, new TitleDescComparator());

                getXSLParameters(upId).put("order", "desc");

            }

        } else if ("date".equals(cmdSort)) {

            getXSLParameters(upId).put("sortby", "date");

            if ("asc".equals(order)) {

                Collections.sort(notes, new DateAscComparator());

                getXSLParameters(upId).put("order", "asc");

            } else {

                Collections.sort(notes, new DateDescComparator());

                getXSLParameters(upId).put("order", "desc");

            }

        } else {

            Collections.sort(notes, new DateAscComparator());

            getXSLParameters(upId).put("sortby", "date");

            getXSLParameters(upId).put("order", "asc");

        }

        setXML(upId, toXML(notes));

        net.unicon.portal.util.Debug.instance().timeElapsed(

        "DONE BUILDING NotepadChannel", true);


    }

    /**
     * Create the XML for the offering notes.
     * @param offeringNotes list of offering notes
     * @return <code>String</code> string representation of the XML
     */

    protected static String toXML(List offeringNotes) {

        StringBuffer xmlSB = new StringBuffer();

        xmlSB.append("<user-notes>\n");

        if (offeringNotes != null) {

            for (int i = 0; i < offeringNotes.size(); i++) {

                xmlSB.append(((PortalObject)offeringNotes.get(i)).toXML());

            }

        }

        xmlSB.append("</user-notes>\n");

        return xmlSB.toString();

    }

    /**
     * Converts a document back to a string.
     * @param doc Document to be converted
     * @return <code>String</code> string representation of the document
     */

    protected String docToString(Document doc) {

        BaseMarkupSerializer serializer = new XHTMLSerializer();

        StringWriter writer = new StringWriter();

        serializer.setOutputCharStream(writer);

        try {

            serializer.serialize(doc);

            writer.close();

        } catch (IOException ioe) {

            ioe.printStackTrace();

        }

        return getBody(writer.toString());

    }

    /** Strip only the body out of the document.  We do not want anything outside of the body because it will bomb the page. */

    protected String getBody(String body) {

        int start = body.indexOf("<body>") + 6;

        int end = body.lastIndexOf("</body>");

        if (end == -1) {

            return ""; // nothing in body

        }

        return body.substring(start, end);

    }

    /** Strip all the scripts out of the html string.  We don't allow it because of security issues. */

    protected String stripScripts(String s) {

        String regex = "(<script)" +

        "(.*\n.*|.*)" +

        "(</script>)";

        return s.replaceAll(regex, " ");

    }

    /* ------------ Inner classes used to sort the notes ----------------- */

    /** Inner class used for sorting by the title of the offering note. */

    class TitleAscComparator implements Comparator {

        public int compare(Object o1, Object o2) {

            OfferingNote note1 = (OfferingNote)o1;

            OfferingNote note2 = (OfferingNote)o2;

            return (note1.getTitle().compareTo(note2.getTitle()));

        }

        public boolean equals(Object obj) {

            return obj.equals(this);

        }

    }

    class TitleDescComparator implements Comparator {

        public int compare(Object o1, Object o2) {

            OfferingNote note1 = (OfferingNote)o1;

            OfferingNote note2 = (OfferingNote)o2;

            return (note2.getTitle().compareTo(note1.getTitle()));

        }

        public boolean equals(Object obj) {

            return obj.equals(this);

        }

    }

    /** Inner class used for sorting by the date of the offering note. */

    class DateAscComparator implements Comparator {

        public int compare(Object o1, Object o2) {

            OfferingNote note1 = (OfferingNote)o1;

            OfferingNote note2 = (OfferingNote)o2;

            int result = (note1.getDate().compareTo(note2.getDate()));

            if (result == 0) {

                result = note1.getTitle().compareTo(note2.getTitle());

            }

            return result;

        }

        public boolean equals(Object obj) {

            return obj.equals(this);

        }

    }

    class DateDescComparator implements Comparator {

        public int compare(Object o1, Object o2) {

            OfferingNote note1 = (OfferingNote)o1;

            OfferingNote note2 = (OfferingNote)o2;

            int result = (note2.getDate().compareTo(note1.getDate()));

            if (result == 0) {

                result = note2.getTitle().compareTo(note1.getTitle());

            }

            return result;

        }

        public boolean equals(Object obj) {

            return obj.equals(this);

        }

    }

}

