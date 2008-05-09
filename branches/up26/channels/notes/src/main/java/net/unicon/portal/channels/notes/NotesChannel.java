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

package net.unicon.portal.channels.notes;

// Java API classes
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import net.unicon.portal.common.AcademusMultithreadedChannel;
import net.unicon.portal.common.PortalObject;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.common.service.channel.ChannelService;
import net.unicon.portal.common.service.channel.ChannelServiceFactory;
import net.unicon.portal.util.PeepholeManager;
import net.unicon.portal.util.RenderingUtil;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.LogService;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.UPFileSpec;

import com.interactivebusiness.portal.VersionResolver;

public class NotesChannel extends AcademusMultithreadedChannel
					implements IMultithreadedCharacterChannel {

    private static String InvalidNoteMsg = "Invalid NoteID - you do not have access to the specified note.";

    public NotesChannel() {

        super();

    }

    public void renderCharacters(PrintWriter out, String upId)
            throws PortalException {

        net.unicon.portal.util.Debug.instance().markTime();

        net.unicon.portal.util.Debug.instance().timeElapsed(
                "BEGIN BUILDING NotesChannel", true);

        /* Getting Database Connection */

        Connection conn = null;

        List notes = null;

        ChannelRuntimeData runtimeData = getRuntimeData(upId);

        ChannelStaticData staticData = getStaticData(upId);
        
/* // spill parameters (uncomment to see)...
System.out.println("#### SPILLING PARAMETERS...");
Iterator it = runtimeData.keySet().iterator();
while (it.hasNext()) {
    String key = (String) it.next();
    String value = runtimeData.getParameter(key);
    System.out.println("\t" + key.toString() + "=" + value);
}
*/       
        String command = runtimeData.getParameter("command");

	    boolean bInvalidNote = false;

        try {

            int guestID = staticData.getPerson().getID();

            ChannelService channelService = ChannelServiceFactory.getService();

            conn = super.getDBConnection();

            if (command != null) {

                // Assume we are going to the main screen view  

                super.setSheetName(upId, "general");

                super.setSSLLocation(upId, "NotesChannel.ssl");

                super.setupXSLParameters(upId);

                if (command.equals("general")) {

                    /* main view code */

                    super.setSheetName(upId, "general");

                } else if (command.equals("add")) {

                    /* add code */

                    super.setSheetName(upId, "add");

                } else if (command.equals("edit")) {
                
                    /* edit code */

                    super.setSheetName(upId, "edit");

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

			            Note thisNote = channelService.getNote(noteID, conn);
    
        		        if (thisNote.getUserID() == guestID) {
    
	    				    // user owns this note, proceed with edit action

                            notes = new ArrayList();

			    		    notes.add(thisNote);
				        }
			  	        else {

					        // user does NOT own thisNote, display error message

					        bInvalidNote = true;

			  	        }

                    }

                } else if (command.equals("delete")) {

                    /* delete code */

                    super.setSheetName(upId, "delete");

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

				        Note thisNote = channelService.getNote(noteID, conn);

    				    if (thisNote.getUserID() == guestID) {

    					    // user owns this note, proceed with delete action

    	                    getXSLParameters(upId).put("ID", noteIDString);
		    		    }
			    	    else {

				    	    // user does NOT own thisNote, display error message
    
	    				    bInvalidNote = true;

		    		    }

                    }

                } else if (command.equals("deleteConfirm")) {

                    /* delete confirmation code */

                    String noteIDString = runtimeData.getParameter("ID");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        channelService.deleteNote(noteID, conn);
                    }

                    super.setSheetName(upId, "general");

                } else if (command.equals("insert")) {

                    /* insert code */

                    String message = runtimeData.getParameter("message");

                    channelService.addNote(guestID, message, conn);

                    super.setSheetName(upId, "general");

                } else if (command.equals("submit")) {

                    /* submit code */

                    String noteIDString = runtimeData.getParameter("ID");

                    String message = runtimeData.getParameter("message");

                    if (noteIDString != null) {

                        int noteID = Integer.parseInt(noteIDString);

                        channelService.updateNote(noteID, message, conn);

                    }

                    super.setSheetName(upId, "general");

                } else if (command.equals("cancel")) {

                    /* cancel code - returning from error */

                    super.setSheetName(upId, "general");

                }

                // After editing or adding, display list of all notes

                if (!command.equals("edit") && !command.equals("add")) {

                    notes = channelService.getAllNotes(guestID, conn);

                }

		        if (bInvalidNote) {      

		    	    // Construct Error page

	    	    	showErrorPage(upId, InvalidNoteMsg);

		        }
		        else {

	                // Construct XML

        	        StringBuffer xmlSB = new StringBuffer();

                	xmlSB.append("<user-notes>");

	                if (notes != null) {
	
        	            for (int ix = 0; ix < notes.size(); ++ix) {

                	        xmlSB.append(((PortalObject) notes.get(ix)).toXML());

	                  }

	              }

        	      xmlSB.append("</user-notes>");

                  setXML(upId, xmlSB.toString());
		    }
	        // Render XML

       	    RenderingUtil.renderXML(this, getStaticData(upId).getSerializerName(),
       		    out, getXML(upId), getSSLLocation(upId), getSheetName(upId),
                getRuntimeData(upId).getBrowserInfo(), getXSLParameters(upId));

            } else {

                // Render peephole view if a sheetname has not been set.

                if (super.getSheetName(upId) == null) {

                    String baseActionURL = runtimeData.getBaseActionURL();
                    String peepholeView = PeepholeManager.getInstance()
                            .getPeephole(this.getClass(), baseActionURL);
                    out.print(peepholeView);

                } else {
                    
                    RenderingUtil.renderXML(this, getStaticData(upId).getSerializerName(),
                        out, getXML(upId), getSSLLocation(upId), getSheetName(upId),
                        getRuntimeData(upId).getBrowserInfo(),
                        getXSLParameters(upId));
                }
                
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            // Releasing DB Connection

            super.releaseDBConnection(conn);

        }

        net.unicon.portal.util.Debug.instance().timeElapsed(
                "DONE BUILDING NotesChannel", true);

    }
    
    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        super.setStaticData(sd, upId);
        try {
            int publishId = Integer.parseInt(sd.getChannelPublishId());
            ChannelDefinition cd = ChannelRegistryStoreFactory.
                getChannelRegistryStoreImpl().getChannelDefinition(publishId);
            ChannelDataManager.registerChannelUser(sd.getPerson(),
                null, cd, upId);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            throw new PortalException(e);
        }
        IAuthorizationPrincipal principal = VersionResolver.getInstance().
            getPrincipalByPortalVersions(sd.getPerson());
        ChannelDataManager.setAuthorizationPrincipal(upId, principal);
    }


    // This method will display the error page for this channel.
    // It handles setting the SSL and populates the xml. So this should
    // be the last action on errors. (i.e. you shouldn't set the xml or
    // stylesheet location after calling this method.

    private void showErrorPage(String upId, String errorMsg)
    throws PortalException {

        setSSLLocation(upId, "/net/unicon/portal/channels/notes/NotesChannel.ssl");
        setSheetName(upId, "error");
        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<errorPage><message>" + errorMsg + "</message></errorPage>");
        setXML(upId, xmlSB.toString());

        // bypass cscr
        String parentUID = SubChannelFactory.getParentUID(upId);
        if (parentUID != null) {
            BrowserInfo bi = getRuntimeData(upId).getBrowserInfo();
            UPFileSpec upfs = RenderingUtil.setupUPURL(parentUID,
                ChannelDataManager.getChannelClass(upId), bi, false);
            Hashtable ht = getXSLParameters(upId);
            ht.put("baseActionURL", upfs.getUPFile());
        }
    }

}
