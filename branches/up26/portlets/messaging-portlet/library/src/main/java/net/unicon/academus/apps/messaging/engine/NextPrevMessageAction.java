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

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.AttachmentsHelper;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class NextPrevMessageAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;
    private IScreen errScreen;
    private String move;
    
    /*
     * Public API.
     */

    public static IAction parse(Element e, IWarlockFactory owner)
                                    throws WarlockException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("action")) {
            String msg = "Argument 'e [Element]' must be an <action> element.";
            throw new IllegalArgumentException(msg);
        }
        if (owner == null) {
            String msg = "Argument 'owner+' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'handle'.";
            throw new XmlFormatException(msg);
        }
        Handle handle = Handle.create(h.getValue());

        // Choices.
        String[] choices = new String[0];
        Attribute p = e.attribute("inpt");
        if (p != null) {
            choices = p.getValue().split(",");
        }
        
        // Move.
        Attribute m = e.attribute("move");
        if (m == null) {
            String msg = "Element <action> is missing required attribute "
                                                            + "'mode'.";
            throw new XmlFormatException(msg);
        }
        
       String move = m.getValue();
        
       return new NextPrevMessageAction(owner, handle, choices, move);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("message_detail_view"));       
        errScreen = m.getScreen(Handle.create("message_list"));

    }

    public IActionResponse invoke(IDecisionCollection[] decisions,
                    IUserContext ctx) throws WarlockException {

        // Assertions.
        if (decisions == null) {
            String msg = "Argument 'decisions' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Identify the destination folder.
        MessagingUserContext muc = (MessagingUserContext) ctx;
        IMessage msg = null;
        
        try{
            // Clean up attachments from the last viewed
            AttachmentsHelper.cleanupAttachments(muc);

	        IMessage[] messages = muc.getMessageList();
	        int curIndex = muc.getDetailPref().getCurrentMessageIndex();

	        if (move.equals("next")){
	            msg = messages[curIndex + 1];
	            muc.getDetailPref().setCurrentMessageIndex(curIndex + 1);
	        }else {
	            msg = messages[curIndex - 1];
	            muc.getDetailPref().setCurrentMessageIndex(curIndex - 1);
	        }

	        // set the message to read
	        if(msg.isUnread()){	            
	            msg.setRead(true);	            
	        }

	        // Prepare the attachments for download
            AttachmentsHelper.prepareAttachments(muc, msg);

        } catch(Throwable t) {
            ErrorMessage[] errors = new ErrorMessage[1];
            String problem = "Unable to go to the next/prev the message.  "+t.getMessage();
            errors[0] = new ErrorMessage("other", problem, null);
            return new SimpleActionResponse(errScreen, new MessageListQuery(muc, errors));
        }

        return new SimpleActionResponse(screen, new ReadMessageQuery(muc));

    }

    /*
     * Implementation.
     */

    private NextPrevMessageAction(IWarlockFactory owner, Handle handle, String[] choices, String move) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.errScreen = null;
        this.move = move;

    }

}
