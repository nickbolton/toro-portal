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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
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

public final class ExternalCallbackAction extends AbstractWarlockFactory
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

        return new ExternalCallbackAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("message_detail_view")); 
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
        
        String extAction = decisions[0].getDecision("externActions")
                                       .getFirstSelectionHandle();

        // For error messages
        String msg = null;
        String solution = null;

        try {

//System.out.println("Do external callback for: "+extAction);
            URL url = (URL)muc.getCallbackActions().get(extAction);
            if (url == null) {
                throw new IllegalArgumentException(
                        "The specified external action is unknown.");
            }

            // make the request, but ignore the results.
            String ret = bufferStream(url.openStream());
//System.out.println("Response: "+ret);


        } catch (Exception me) {
            /* TODO: Log exception! */
            me.printStackTrace(System.err);
            msg = "Error handling external callback: "+me.getMessage();
            solution = "Please contact a Portal administrator for assistance, or try again.";
        }

        ErrorMessage[] errors = new ErrorMessage[0];
        if (msg != null) {
            errors = new ErrorMessage[1];
            errors[0] = new ErrorMessage("other", "Unable to read message. "+msg, solution);
        }
        return new SimpleActionResponse(screen, new ReadMessageQuery(muc, errors));

    }

    /*
     * Package API.
     */

    private ExternalCallbackAction(IWarlockFactory owner, Handle handle, String[] choices) {
        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
    }

    private String bufferStream(InputStream is) throws IOException {
        ByteArrayOutputStream rslt = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];
        int r = 0;
        while ((r = is.read(buf)) != -1) {
            rslt.write(buf, 0, r);
        }

        return rslt.toString();
    }
}
