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

import java.util.HashMap;
import java.util.Map;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;

public final class SimpleActionResponse implements IActionResponse {

    // Instance Members.
    private final IScreen screen;
    private final IStateQuery query;

    /*
     * Public API.
     */

    public SimpleActionResponse(IScreen screen, IStateQuery query) {

        // Assertions.
        if (screen == null) {
            String msg = "Argument 'screen' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (query == null) {
            String msg = "Argument 'query' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.screen = screen;
        this.query = query;

    }

    public IScreen getScreen() {
        return screen;
    }

    public IStateQuery getStateQuery() {
        return query;
    }

}
