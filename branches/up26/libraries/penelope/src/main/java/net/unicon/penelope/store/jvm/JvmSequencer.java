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

package net.unicon.penelope.store.jvm;

import net.unicon.penelope.store.ISequencer;

public final class JvmSequencer implements ISequencer {

    // Instance Members.
    private long seed;

    /*
     * Public API.
     */

    public JvmSequencer(long seed) {

        if (seed < 0) {
            String msg = "Argument 'seed' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.seed = seed;

    }

    public synchronized long next() {
        return ++seed;
    }

}
