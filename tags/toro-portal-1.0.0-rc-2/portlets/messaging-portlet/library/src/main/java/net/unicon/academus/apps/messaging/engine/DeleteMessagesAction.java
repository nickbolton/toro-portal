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
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.AttachmentsHelper;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IChoiceCollection;
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

public final class DeleteMessagesAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screenDetail;
    private IScreen screenList;

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
            String msg = "Argument 'owner' cannot be null.";
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

        return new DeleteMessagesAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screenList = m.getScreen(Handle.create("message_list"));
        screenDetail = m.getScreen(Handle.create("message_detail_view"));

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

        MessagingUserContext user = (MessagingUserContext) ctx;
        IFolder f = user.getFolderSelection();

        // Make sure the user clicked 'yes' to confirm.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("deleteConfirmation")));

        boolean deleted = false;
        String problem = null;
        String solution = null;

        boolean confirmed = "yes".equalsIgnoreCase(d.getFirstSelectionHandle());
        
        if (confirmed) {
            // Mark the items for deletion.
            try {
                IMessage[] msgSel = user.getMessageSelection();
                for (int i = 0; i < msgSel.length; i++) {
                    f.removeMessage(msgSel[i]);
                }
            } catch(MercuryException e) {
                /* TODO: Log exception! */
                e.printStackTrace(System.err);
                problem = "Unable to delete message: " + e.getMessage();
                solution = "Please contact a Portal administrator for assistance, or try again.";
            }

            if (problem == null) {
                // Expunge the deleted items.
                try {
                    f.expunge();
                } catch(MercuryException e) {
                    /* TODO: Log exception! */
                    e.printStackTrace(System.err);
                    problem = "Unable to expunge folder '"+f.getLabel()+"': " + e.getMessage();
                    solution = "Please contact a Portal administrator for assistance, or try again.";
                }
            }
        }

        ErrorMessage[] errors = null;
        if (problem != null) {
            errors = new ErrorMessage[1];
            errors[0] = new ErrorMessage("other", "Unable to delete messages. "+problem, solution);           
        } else {
            errors = new ErrorMessage[0];
        }

        // Clear the selected messages.
        user.clearMessageSelection();

        IActionResponse rslt = null;
        if (user.getDetailPref().getCurrentMessageIndex() != -1) {
            // Go back to detail view
            rslt = new SimpleActionResponse(screenDetail, new ReadMessageQuery(user, errors));

            if (!confirmed) {
                // Prepare the attachments for download
                AttachmentsHelper.prepareAttachments(user,
                        user.getMessageList()[user.getDetailPref().getCurrentMessageIndex()]);
            } else {
                int curIndex = user.getDetailPref().getCurrentMessageIndex();
                // check if we have not reached the last message
	            if (curIndex < user.getMessageList().length - 1) {
                    // remove the message at currentIndex
                    List messages = new ArrayList(Arrays.asList(user.getMessageList()));
                    messages.remove(curIndex);

                    try {
                        // set the next message to read
                        if (((IMessage)messages.get(curIndex)).isUnread()){	            
                            ((IMessage)messages.get(curIndex)).setRead(true);	            
                        }
                    } catch (MercuryException me) {
                        rslt = new SimpleActionResponse(screenDetail,
                                new ReadMessageQuery(user,
                                    new ErrorMessage[] {new ErrorMessage("other", "Unable to set message as read", "Unknown")}));
                    }

                    user.setMessageList((IMessage[])messages.toArray(new IMessage[0]));

                    // Prepare the attachments for download
                    AttachmentsHelper.prepareAttachments(user,
                            (IMessage)messages.get(curIndex));
                } else {
                    // Need to go to MessageList
                    rslt = new SimpleActionResponse(screenList, new MessageListQuery(user, errors));
                }
            }
        } else {
            // Go to message list
            rslt = new SimpleActionResponse(screenList, new MessageListQuery(user, errors));
        }

        return rslt;
    }

    /*
     * Package API.
     */

    private DeleteMessagesAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screenList = null;
        this.screenDetail = null;

    }

}
