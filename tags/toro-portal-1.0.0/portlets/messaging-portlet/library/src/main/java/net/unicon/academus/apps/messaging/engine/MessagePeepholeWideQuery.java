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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.academus.apps.messaging.FactoryInfo;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.academus.apps.messaging.ViewMode;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
import net.unicon.warlock.WarlockException;

/**
 * State Query for the message_peephole_wide screen. 
 */
public class MessagePeepholeWideQuery extends InitialQuery
{
	private static Log log = LogFactory.getLog(MessagePeepholeWideQuery.class); 
    private static final DateFormat formatter = new SimpleDateFormat();

    public MessagePeepholeWideQuery(MessagingUserContext muc) {
        super(muc);
    }    

    public String query() throws WarlockException {
    	StringBuffer rslt = new StringBuffer();
        FactoryInfo[] facts = muc.listFactories();

        rslt.append("<state>");
        super.commonQueries(rslt);

        IFolder f = muc.getFolderSelection();
        
        try {
            IMessage[] msgs;
//          Synchronize on muc for message list cache purposes.
    		synchronized (muc) {
    			msgs = muc.getMessageList();
    			if (msgs == null || msgs.length == 0 || muc.isMessageListDirty()) {
    				if (log.isDebugEnabled())
    					log.debug("["+muc.getUsername()+"] Refreshing message list in peephole.");
 
					msgs = f.getMessages();

					// Narrow the results based on view mode.
					if (msgs.length > 0) {
						List mlist = new ArrayList();
		                for (int i = 0; i < msgs.length; i++) {
		                    if (msgs[i].isUnread()){
		                        mlist.add(msgs[i]);
		                    }
		                }
						msgs = (IMessage[])mlist.toArray(new IMessage[0]);
					}
    			}

    			// Sort the list.
    			Arrays.sort(msgs, muc.getMessageSortMethod().getComparator());

    			// Save the list (for Detail View navigation).
    			muc.setMessageList(msgs);
    		} 
 
            rslt.append("<messages total-unread=\"")
            	.append(msgs.length)
            	.append("\" >");
            	
            //add the messages based on the config information
            for(int i = 0; i < muc.getAppContext().getMsgLimit() && i < msgs.length; i++){
                rslt.append("<message ");

                // Message identifier.
                rslt.append("id=\"")
                    .append(EntityEncoder.encodeEntities(msgs[i].getId()))
                    .append("\"> ");

                // Received Date.
                rslt.append("<received>")
                    .append(formatter.format(msgs[i].getDate()))
                    .append("</received>");
                
                // Read status.
                rslt.append("<status>unread</status>");

                // Priority.
                rslt.append("<priority>")
                    .append(msgs[i].getPriority().toInt())
                    .append("</priority>");

                // Sender.
                rslt.append("<sender>");
                rslt.append(EntityEncoder.encodeEntities(msgs[i].getSender().getLabel()));
                rslt.append("</sender>");

                // Subject
                rslt.append("<subject>")
                    .append(EntityEncoder.encodeEntities(msgs[i].getSubject()))
                    .append("</subject>");

                // Attachments.
                rslt.append("<attachments total=\"")
                	.append(msgs[i].getAttachments().length)
                	.append("\"></attachments>");

                rslt.append("</message>");
            }
            	
            rslt.append("</messages>");            
    // System.out.println("Peephole wide " + rslt.toString());       
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
