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
import net.unicon.academus.apps.messaging.ImportExportHelper;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.portlet.FileUpload;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class ImportMessageAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;

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

        return new ImportMessageAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("message_list"));
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

        // Add each item.
        IDecision d = decisions[0].getDecision("fileUploads");
        FileUpload fu = (FileUpload)d.getFirstSelectionValue();
        String msg = null;
        String solution = null;
        boolean done = true;

        // Check to be sure we don't exceed the size limit.
        long maxSize = muc.getAppContext().getUploadLimit();
        if (maxSize != 0 && fu.getSize() > maxSize) {
            msg = "File exceeds the size limitation for file uploads:  "
                  + fu.getName();
            solution = "Please double-check the file size of the uploaded file"
                     + " to ensure that it falls within the specified limit.";
            done = false;
        } else {
            DraftMessage[] drafts = null;

            // Attempt to parse the file.
            try {
                drafts = ImportExportHelper.parseInputStream(fu.getContents(), muc.getCivisFactories());
            } catch (MercuryException me) {
                me.printStackTrace(System.err);
                msg = "There was an error parsing the supplied document: "+me.getMessage();
                solution = "Check the syntax of the submitted document and try again.";
                done = false;
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                msg = "There was an error parsing the supplied document: "+t.getMessage();
                solution = "Please verify that the document contains the correct information.";
                done = false;
            }

            if (drafts != null && drafts.length > 0) {
                // Now send the drafts.
                IMessageFactory fact = muc.getFactorySelection().getFactory();

                try {
                    for (int i = 0; i < drafts.length; i++)
                        fact.sendMessage(drafts[i]);
                } catch (Throwable t) {
                    done = false;
                    msg = "An error occured while sending the imported message(s): "+t.getMessage();
                    t.printStackTrace(System.err);
                }

            } else if (drafts != null) {
                msg = "No messages found in the submitted document.";
                solution = "Please check the document for correct syntax and try again.";
                done = false;
            }
        }

        if (done) {
            return new SimpleActionResponse(screen, new MessageListQuery(muc));
        } else {
            ErrorMessage[] errors = new ErrorMessage[1];
            String problem = "Unable to import message(s).  " + (msg != null ? msg : "");
            errors[0] = new ErrorMessage("other", problem, solution);
            return new SimpleActionResponse(screen, new MessageListQuery(muc, errors));
        }
    }

    /*
     * Package API.
     */

    private ImportMessageAction(IWarlockFactory owner, Handle handle, String[] choices) {
        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
    }

}
