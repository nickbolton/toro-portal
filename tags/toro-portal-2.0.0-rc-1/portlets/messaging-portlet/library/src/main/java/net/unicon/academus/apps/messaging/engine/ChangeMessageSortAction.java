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

import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.SortMethod;
import net.unicon.penelope.IDecisionCollection;
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

public final class ChangeMessageSortAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;
    private String mode;

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

        // Mode.
        Attribute m = e.attribute("mode");
        if (m == null) {
            String msg = "Element <action> is missing required attribute "
                                                            + "'mode'.";
            throw new XmlFormatException(msg);
        }
        String mode = m.getValue();

        return new ChangeMessageSortAction(owner, handle, choices, mode);

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

        // Change/toggle the sorting and respond.
        MessagingUserContext muc = (MessagingUserContext) ctx;
        SortMethod change = null;
        SortMethod current = muc.getMessageSortMethod();
        if (current.getMode().equals(mode)) {
            // Toggle the current mode in the other direction.
            change = SortMethod.reverse(current);
        } else {
            // Switch to a different mode.
            change = SortMethod.getInstance(mode + "-asc");
        }
        muc.setMessageSortMethod(change);
        return new SimpleActionResponse(screen, new MessageListQuery(muc));

    }

    /*
     * Package API.
     */

    private ChangeMessageSortAction(IWarlockFactory owner, Handle handle,
                                String[] choices, String mode) {

        super(owner, handle, choices);

        // Assertions.
        if (mode == null) {
            String msg = "Argument 'mode' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = null;
        this.screen = null;
        this.mode = mode;

    }

}
