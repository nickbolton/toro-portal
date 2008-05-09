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

package net.unicon.warlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class StateMachine {

    // Instance Members.
    private final IApplicationContext ctx;
    private final Map screens;
    private final IScreen peephole;

    /*
     * Public API.
     */

    public StateMachine(IApplicationContext ctx, IScreen[] screens) {
        this(ctx, screens, null);
    }

    public StateMachine(IApplicationContext ctx, IScreen[] screens,
                                            IScreen peephole) {

        // Assertions.
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (screens == null) {
            String msg = "Argument 'screens' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'peephole' may be null.

        // Instance Members.
        this.ctx = ctx;
        this.screens = new HashMap();
        Iterator it = Arrays.asList(screens).iterator();
        while (it.hasNext()) {
            IScreen s = (IScreen) it.next();
            // Make sure there aren't 2 screens w/ the same handle.
            if (this.screens.containsKey(s.getHandle())) {
                String msg = "Handle must be unique in context.  Duplicate "
                                + "handle:  " + s.getHandle().getValue();
                throw new IllegalArgumentException(msg);
            }
            this.screens.put(s.getHandle(), s);
        }
        if (peephole != null && !this.screens.containsValue(peephole)) {
            String msg = "The specified peephole screen is not contained in "
                                            + "the screens collection.";
            throw new IllegalArgumentException(msg);
        }
        this.peephole = peephole;

        // Initialize Actions.
        Iterator ss = Arrays.asList(screens).iterator();
        while (ss.hasNext()) {
            IScreen s = (IScreen) ss.next();
            Iterator aa = Arrays.asList(s.getActions()).iterator();
            while (aa.hasNext()) {
                IAction a = (IAction) aa.next();
                a.init(this);
            }
        }

    }

    public IApplicationContext getContext() {
        return ctx;
    }

    public IScreen getPeephole() {
        return peephole;
    }

    /**
     * Provided so that <code>IAction</code> implementations can obtain
     * references to the screen(s) they navigate to.  These references should be
     * established in the <code>init</code> method to provide a bootstrap-time
     * check for expected screens.  Throws <code>IllegalArgumentException</code>
     * if there is no screen defined for the specified handle.
     *
     * @param handle The handle to a screen defined for the application.
     * @return The specified screen.
     */
    public IScreen getScreen(Handle h) {

        // Assertions.
        if (!screens.containsKey(h)) {
            String msg = "Unrecognized screen.  Application does not "
                    + "contain a screen with the specified handle:  "
                    + h.getValue();
            throw new IllegalArgumentException(msg);
        }

        return (IScreen) screens.get(h);

    }

}
