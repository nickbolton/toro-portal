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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.AttachmentsHelper;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.SpecialFolder;
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

public final class SaveMessageAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screenDetail;
    private IScreen screenList;
    private String toScreen;
    
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
        
        // to-screen.
        Attribute t = e.attribute("to-screen");
        if (t == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'to-screen'.";
            throw new XmlFormatException(msg);
        }
        String toScreen = t.getValue();

        // Choices.
        String[] choices = new String[0];
        Attribute p = e.attribute("inpt");
        if (p != null) {
            choices = p.getValue().split(",");
        }

        return new SaveMessageAction(owner, handle, choices, toScreen);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screenDetail = m.getScreen(Handle.create("message_detail_view")); 
        screenList = m.getScreen(Handle.create("message_list"));
        
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

        MessagingUserContext muc = (MessagingUserContext) ctx;
        IDecision d = decisions[0].getDecision("selectedItems");
        ISelection[] selections = d.getSelections();

        // For error handling
        String msg = null;
        String solution = null;

        // Identify the source and destination folders.
        IFolder srcFold = muc.getFolderSelection();
        IFolder destFold = null;
        
        if (selections.length == 0) {
            msg = "No messages selected for saving.";
            solution = "Please select the messages you would like to save, then press the Save button.";
        } else {
            try {
                destFold = muc.getFactorySelection().getFactory()
                              .getSpecialFolder(SpecialFolder.SAVE); 
            } catch (MercuryException me) {
                /* TODO: Log exception! */
                me.printStackTrace(System.err);
                msg = "Problem retrieving save folder. "+me.getMessage();
                solution = "Please contact a Portal administrator for assistance, or try again.";
            }

            if (msg == null) {
                if (destFold == null) {
                    msg = "Unable to locate a Save folder.";
                    solution = "Please contact a Portal administrator for assistance; this should not occur.";
                } else {
                    // save each message.
                    try {
                        // TODO: When folder selection is added (for folder
                        // heirarchy support), this code will have to be
                        // modified to support an intermediate selection store
                        // ala ConfirmDeleteAction.
                        for (int i = 0; i < selections.length; i++) {
                            IMessage m = srcFold.getMessage(
                                                 selections[i].getOption()
                                                    .getHandle().getValue());
                            /*// Add it to the destination...
                            destFold.addMessage(m);
                            // ...and remove it from the source.
                            srcFold.removeMessage(m);*/
                            
                            // move the message
                            srcFold.getOwner().move(m, srcFold, destFold);
                        }

                        // Required to purge all removes, as per the Mercury contract.
                        //srcFold.expunge();
                    } catch(MercuryException e) {
                        /* TODO: Log exception! */
                        e.printStackTrace(System.err);
                        msg = e.getMessage();
                        solution = "Please contact a Portal administrator for assistance, or try again.";
                    }
                }
            }
        }

        ErrorMessage[] errors = null;
        if (msg != null) {
            errors = new ErrorMessage[1];
            errors[0] = new ErrorMessage("other", "Unable to save message(s). "+msg, solution);
        }

        // Clean any prepared attachments
        AttachmentsHelper.cleanupAttachments(muc);

        if(toScreen.equals("message_detail_view")){
            
            // check if we have not reached the last message
            int curIndex = muc.getDetailPref().getCurrentMessageIndex();
            if(curIndex < muc.getMessageList().length - 1){
                // remove the message at currentIndex
                List messages = new ArrayList(Arrays.asList(muc.getMessageList()));
                messages.remove(curIndex);
                muc.setMessageList((IMessage[])messages.toArray(new IMessage[0]));

                // Prepare the attachments for download
                AttachmentsHelper.prepareAttachments(muc,
                        (IMessage)messages.get(curIndex));

                return new SimpleActionResponse(screenDetail, new ReadMessageQuery(muc, errors));
            }
            
        }
        // the message list and index of current selected message will be reset in MessageListQuery. 
        return new SimpleActionResponse(screenList, new MessageListQuery(muc, errors));
        
        
    }

    /*
     * Implementation.
     */

    private SaveMessageAction(IWarlockFactory owner, Handle handle, String[] choices
            , String toScreen) {

        super(owner, handle, choices);

        // Instance Members.
        this.screenDetail = null;
        this.screenList = null;
        this.toScreen = toScreen;
    }

}
