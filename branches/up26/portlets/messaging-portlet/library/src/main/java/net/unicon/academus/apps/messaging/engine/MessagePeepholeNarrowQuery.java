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

package net.unicon.academus.apps.messaging.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.unicon.academus.apps.messaging.FactoryInfo;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.MercuryException;
import net.unicon.warlock.WarlockException;

/**
 * State Query for the message_peephole_narrow screen. 
 */
public class MessagePeepholeNarrowQuery extends InitialQuery
{
    private static final DateFormat formatter = new SimpleDateFormat();

    public MessagePeepholeNarrowQuery(MessagingUserContext muc) {
        super(muc);
    }    

    public String query() throws WarlockException {
    	StringBuffer rslt = new StringBuffer();
        FactoryInfo[] facts = muc.listFactories();

        rslt.append("<state>");
        super.commonQueries(rslt);

        IFolder f = muc.getFolderSelection();
        
        try {
            rslt.append("<messages total-unread=\"")
            	.append(f.getUnreadCount())
            	.append("\" />");
        } catch (MercuryException e) {
            throw new RuntimeException("MessagePeepholeNarrowQuery : Error in " +
            		"getting the unread message count for folder with id = " + 
            		  f.getIdString(), e);
        }
        
        rslt.append("</state>");

        return rslt.toString();    
    }
    
    protected void queryNavigation(StringBuffer rslt) throws WarlockException {
        
    }
    
    protected void querySelections(StringBuffer rslt) throws WarlockException {
        // Add current selections (factory, viewmode, folder and message as applicable)

        // Account.
        if (muc.hasFactorySelection()) {
            FactoryInfo fInfo = muc.getFactorySelection();
            rslt.append(fInfo.toXml());

            try {
                // Folder. (Inbox, Sent, Saved?)
                if (!muc.hasFolderSelection()) {
                    // Default to root (Inbox)
                    muc.setFolderSelection(fInfo.getFactory().getRoot());
                }
                rslt.append("<folder id=\"")
                    .append(EntityEncoder.encodeEntities(muc.getFolderSelection().getIdString()))
                    .append("\"><label>")
                    .append(EntityEncoder.encodeEntities(muc.getFolderSelection().getLabel()))
                    .append("</label></folder>");

                rslt.append("<time>")
                	.append(formatter.format(new Date(System.currentTimeMillis())))
                	.append("</time>");
                
            } catch (MercuryException me) {
                throw new WarlockException(
                        "Failed to query IMessageFactory: "+fInfo.getId(), me);
            }            
        }
    }


}
