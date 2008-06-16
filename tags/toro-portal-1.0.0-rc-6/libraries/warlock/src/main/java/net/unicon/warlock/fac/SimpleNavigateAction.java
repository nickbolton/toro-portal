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

package net.unicon.warlock.fac;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;

public final class SimpleNavigateAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private final Handle toScreen;
    private final IStateQuery query;
    private IActionResponse response;

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
            StringTokenizer tokens = new StringTokenizer(p.getValue(), ",");
            choices = new String[tokens.countTokens()];
            for (int i=0; tokens.hasMoreTokens(); i++) {
                choices[i] = tokens.nextToken();
            }
        }

        // ToScreen.
        Attribute s = e.attribute("to-screen");
        if (s == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'to-screen'.";
            throw new XmlFormatException(msg);
        }
        String toScreen = s.getValue();

        // Query.
        String sqXml = "<state />"; // default...
        Element state = (Element) e.selectSingleNode("state");
        if (state != null) {
            sqXml = state.asXML();
        }
        IStateQuery query = new StateQueryImpl(sqXml);

        return new SimpleNavigateAction(owner, handle, choices,
                                            toScreen, query);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        response = new SimpleActionResponse(m.getScreen(toScreen), query);

    }

    public IActionResponse invoke(IDecisionCollection[] decisions,
                    IUserContext ctx) throws WarlockException {
        return response;
    }

    /*
     * Implementation.
     */

    private SimpleNavigateAction(IWarlockFactory owner, Handle handle,
                                String[] choices, String toScreen,
                                IStateQuery query) {

        super(owner, handle, choices);

        // Assertions.
        if (toScreen == null) {
            String msg = "Argument 'toScreen' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (query == null) {
            String msg = "Argument 'query' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.toScreen = Handle.create(toScreen);
        this.query = query;
        this.response = null;

    }

    /*
     * Nested Types.
     */

    private static final class StateQueryImpl implements IStateQuery {

        // Instance Members.
        private String state;

        /*
         * Public API.
         */

        public StateQueryImpl(String state) {

            // Assertions.
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            this.state = state;

        }

        public String query() {
            return state;
        }

        public IDecisionCollection[] getDecisions() {
            return new IDecisionCollection[0];
        }

    }

}