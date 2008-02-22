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

package net.unicon.academus.apps.permissions.engine;

import net.unicon.academus.apps.permissions.PermissionsApplicationContext;
import net.unicon.academus.apps.permissions.PermissionsUserContext;
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
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class ChangePermissionsPageAction extends PermissionsAbstractAction {

    // Instance Members.
    private PermissionsApplicationContext app;
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
            choices = p.getValue().split(",");
        }

        // Move.
        Attribute m = e.attribute("mode");
        if (m == null) {
            String msg = "Element <action> is missing required attribute "
                                                            + "'mode'.";
            throw new XmlFormatException(msg);
        }
        PageChange change = PageChange.getInstance(m.getValue());

        return new ChangePermissionsPageAction(owner, handle, choices, change);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (PermissionsApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("permissions_list"));
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
        if (decisions.length == 0) {
            String msg = "Argument 'decisions' cannot be empty.";
            throw new IllegalArgumentException(msg);
        }

        PermissionsUserContext puc = (PermissionsUserContext) ctx;
        
        // update the entity permissions  
        updatePermissions(decisions[0], puc);
        
        PagingState ps = puc.getPermissionsPaging();
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
        return new SimpleActionResponse(screen, new PortletPermissionsListQuery(puc));

    }

    /*
     * Package API.
     */

    private ChangePermissionsPageAction(IWarlockFactory owner, Handle handle,
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
