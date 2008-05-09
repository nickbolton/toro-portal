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
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class ComposeNavigateAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;
    private IScreen screenError;
    private final String nextScreen;

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
        if (p == null)
            throw new XmlFormatException(
                    "ComposeNavigateAction requires attribute 'inpt'.");
        choices = p.getValue().split(",");

        p = e.attribute("to-screen");
        if (p == null)
            throw new XmlFormatException(
                    "ComposeNavigateAction requires attribute 'to-screen'.");
        String nextScreen = p.getValue().toLowerCase();

        return new ComposeNavigateAction(owner, handle, choices, nextScreen);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create(nextScreen));
        screenError = m.getScreen(Handle.create("message_compose"));
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

        MessagingUserContext muc = (MessagingUserContext)ctx;
        ErrorMessage error = null;

        if (decisions.length > 0) {
            // Save the Subject, Body, and Priority from decision
            // cRecipients will contain recipients to remove, if 'action' is remove recips. Ignore otherwise.
            // cAttachments will contain attachments to remove, if action is remove attachment. Ignore otherwise.
            IDecisionCollection d = decisions[0];
            error = ComposeMessageAction.handleComposeDecisions(muc, d);
        }

        IActionResponse rslt = null;
        if (error != null) {
            ErrorMessage[] errors = new ErrorMessage[] { error };
            rslt = new SimpleActionResponse(screenError, new ComposeMessageQuery(muc, errors));
        } else {
            if (screen.getHandle().getValue().equals("message_compose"))
                rslt = new SimpleActionResponse(screen, new ComposeMessageQuery(muc));
            else
                rslt = new SimpleActionResponse(screen, new InitialQuery(muc));
        }
        return rslt;
    }

    /*
     * Package API.
     */

    private ComposeNavigateAction(IWarlockFactory owner, Handle handle, String[] choices, String nextScreen) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.app = null;
        this.nextScreen = nextScreen;
    }

}
