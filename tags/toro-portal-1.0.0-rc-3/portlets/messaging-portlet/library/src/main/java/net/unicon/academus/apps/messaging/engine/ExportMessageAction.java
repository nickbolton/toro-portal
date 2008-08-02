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
import net.unicon.mercury.MercuryException;
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

public final class ExportMessageAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screenList;
    private IScreen screenDetail;
    private IScreen screenExport;

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

        return new ExportMessageAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screenExport = m.getScreen(Handle.create("message_export"));
        screenList   = m.getScreen(Handle.create("message_list"));
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

        MessagingUserContext muc = (MessagingUserContext) ctx;
        IDecision d = decisions[0].getDecision("selectedItems");
        ISelection[] selections = d.getSelections();
        IFolder f = muc.getFolderSelection(); 

        // For error handling
        String msg = null;
        String solution = null;

        if (selections.length == 0) {
            msg = "No messages selected for exporting.";
            solution = "Please select the messages you would like to export, then press the Export button.";
        } else {
            // the user selected targets...
            try {
                IMessage[] targets = new IMessage[selections.length];

                for (int i=0; i < selections.length; i++) {
                    String msgId = selections[i].getOption().getHandle().getValue();
                    targets[i] = f.getMessage(msgId);
                }

                muc.setMessageSelection(targets);
            } catch (MercuryException e) {
                /* TODO: Log exception! */
                e.printStackTrace(System.err);
                msg = "Error during message selection.";
                solution = "Please contact a Portal administrator for assistance, or try again.";
            }
        }

        IActionResponse rslt = null;
        if (msg == null) {
            // Move to the export screen
            rslt = new SimpleActionResponse(screenExport, new ExportMessageQuery(muc));

        } else {
            ErrorMessage[] errors = new ErrorMessage[1];
            errors[0] = new ErrorMessage("other", "Unable to export message(s). "+msg, solution);

            if (muc.getDetailPref().getCurrentMessageIndex() != -1) {
                // Go back to detail view
                rslt = new SimpleActionResponse(screenDetail, new ReadMessageQuery(muc, errors));
            } else {
                // Cleanup any prepared attachments
                AttachmentsHelper.cleanupAttachments(muc);

                // Go to message list
                rslt = new SimpleActionResponse(screenList, new MessageListQuery(muc, errors));
            }
        }

        return rslt;
    }

    /*
     * Implementation.
     */

    private ExportMessageAction(IWarlockFactory owner, Handle handle,
                                String[] choices) {
        super(owner, handle, choices);
    }

}