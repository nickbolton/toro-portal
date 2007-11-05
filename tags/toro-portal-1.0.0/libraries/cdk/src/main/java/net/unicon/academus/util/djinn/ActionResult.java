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

package net.unicon.academus.util.djinn;

/**
 * Represents the results of performing an action.  An <code>ActionResult</code>
 * contains both the new mode that results from that action and the state
 * information needed to express the mode.
 */
public final class ActionResult {

    // Instance Members.
    private Mode mode;
    private ModeStateBag state;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>ActionResult</code> with the specified mode and
     * state.
     *
     * @param mode the mode that results from the performed action.
     * @param state the state information that results from this action.
     */
    public ActionResult(Mode mode, ModeStateBag state) {

        // Assertions.
        if (mode == null) {
            String msg = "Argument 'mode' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.mode = mode;
        this.state = state;

    }

    /**
     * Returns the mode that results from the action performed.
     *
     * @return the mode that results from the action performed.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the state information that results from the action performed.
     *
     * @return the state information that results from the action performed.
     */
    public ModeStateBag getState() {
        return state;
    }

}
