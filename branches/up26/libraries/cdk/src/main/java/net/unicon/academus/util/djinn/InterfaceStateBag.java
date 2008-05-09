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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of state data for an <code>Interface</code>.
 */
public final class InterfaceStateBag {

    // Instance Members.
    private Map entries;
    private List objects;
    private Map options;

    /*
     * Public API.
     */

    /**
     * Constructs a new, empty <code>InterfaceStateBag</code>.
     */
    public InterfaceStateBag() {

        //Instance Members.
        this.entries = new HashMap();
        this.objects = new ArrayList();
        this.options = new HashMap();

    }

    /**
     * Puts a new entry into the bag.  The entry data is a <code>String</code>.
     * This method throws an <code>IllegalArgumentException</code> if either the
     * key or the value is null.
     *
     * @param key the <code>StateKey</code> that represents this entry.
     * @param value the state-specific data for this entry.
     */
    public void putEntry(StateKey key, String value) {

        // Assertions.
        if (key == null) {
            String msg = "Argument 'key' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "Argument 'value' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (entries.containsKey(key)) {
            String msg = "Entry already defined for StateKey:  "
                                        + key.getHandle();
            throw new IllegalArgumentException(msg);
        }

        entries.put(key, value);

    }

    /**
     * Associates a state object with the relevant <code>Interface</code>.  This
     * method throws an <code>IllegalArgumentException</code> if the state
     * object is null.
     *
     * @param o the state object to associate with the specified mode.
     */
    public void putObject(IStateObject o) {

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o' [IStateObject] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        objects.add(o);

    }

    /**
     * Associates an <code>Option</code> with the specified <code>Choice</code>.
     * This method throws an <code>IllegalArgumentException</code> if either the
     * choice or the option is null.
     *
     * @param c the <code>Choice</code> with which to associate the specified
     * <code>Option</code>.
     * @param o the <code>Option</code> to associate with the specified
     * <code>Choice</code>.
     */
    public void putOption(Choice c, Option o) {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c' [Choice] cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (o == null) {
            String msg = "Argument 'o' [Option] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Access the list.
        List l = (List) options.get(c);
        if (l == null) {
            l = new ArrayList();
            options.put(c, l);
        }

        l.add(o);

    }


    /*
     * Package API.
     */

    String getEntry(StateKey key) {
        // NB:  Clients must check for undefined entries.
        return (String) entries.get(key);
    }

    IStateObject[] getObjects() {

        // Create the array.
        IStateObject[] rslt = new IStateObject[objects.size()];
        objects.toArray(rslt);

        return rslt;

    }

    Option[] getOptions(Choice c) {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c' [Choice] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Access the list.
        List l = (List) options.get(c);
        if (l == null) return new Option[0];

        // Copy to array.
        Option[] rslt = new Option[l.size()];
        l.toArray(rslt);

        return rslt;

    }

    StateKey[] getStateKeys() {

        Set keys = entries.keySet();
        StateKey[] rslt = new StateKey[keys.size()];
        keys.toArray(rslt);
        return rslt;

    }

}
