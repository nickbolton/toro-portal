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

package net.unicon.academus.apps.briefcase.engine;

import java.util.StringTokenizer;

import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.paging.PageChange;
import net.unicon.alchemist.paging.PagingState;
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

public final class ChangeFolderPageAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private BriefcaseApplicationContext app;
    private IScreen screen;
    private PageChange change;

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

        // Move.
        Attribute m = e.attribute("move");
        if (m == null) {
            String msg = "Element <action> is missing required attribute "
                                                            + "'mode'.";
            throw new XmlFormatException(msg);
        }
        PageChange change = PageChange.getInstance(m.getValue());

        return new ChangeFolderPageAction(owner, handle, choices, change);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("folderview"));

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
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking Briefcase Action: " + getClass().getName());
        }

        // Adjust the history and respond.
        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        PagingState ps = user.getFolderPaging();
        switch (change.toInt()) {
            case PageChange.FIRST_SWITCHABLE:
                ps.setCurrentPage(PagingState.FIRST_PAGE);
                break;
            case PageChange.PREVIOUS_SWITCHABLE:
                ps.setCurrentPage(ps.getCurrentPage() - 1);
                break;
            case PageChange.NEXT_SWITCHABLE:
                ps.setCurrentPage(ps.getCurrentPage() + 1);
                break;
            case PageChange.LAST_SWITCHABLE:
                ps.setCurrentPage(PagingState.LAST_PAGE);
                break;
        }
        return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user));

    }

    /*
     * Package API.
     */

    private ChangeFolderPageAction(IWarlockFactory owner, Handle handle,
                                String[] choices, PageChange change) {

        super(owner, handle, choices);

        // Assertions.
        if (change == null) {
            String msg = "Argument 'change' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = null;
        this.screen = null;
        this.change = change;

    }

}