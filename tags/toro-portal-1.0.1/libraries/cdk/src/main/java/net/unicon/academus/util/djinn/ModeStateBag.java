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

/**
 * Collection of state data for a <code>Mode</code>.
 */
public final class ModeStateBag {

    // Instance Members.
    private Map entries;
    private List objects;
    private Map interfaceBags;

    /*
     * Public API.
     */

    /**
     * Constructs a new, empty <code>ModeStateBag</code>.
     */
    public ModeStateBag() {

        //Instance Members.
        this.entries = new HashMap();
        this.objects = new ArrayList();
        this.interfaceBags = new HashMap();

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
     * Associates a state object with the relevant <code>Mode</code>.  This
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
     * ToDo:  javadoc.
     */
    public void putInterfaceBag(Interface i, InterfaceStateBag bag) {

        // Assertions.
        if (i == null) {
            String msg = "Argument 'i' [Interface] cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (bag == null) {
            String msg = "Argument 'bag' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Access the list.
        List l = (List) interfaceBags.get(i);
        if (l == null) {
            l = new ArrayList();
            interfaceBags.put(i, l);
        }

        // Add the new bag.
        l.add(bag);

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

    InterfaceStateBag[] getInterfaceBags(Interface i) {

        // Assertions.
        if (i == null) {
            String msg = "Argument 'i' [Interface] cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Access the list.
        List l = (List) interfaceBags.get(i);
        if (l == null) return new InterfaceStateBag[0];

        // Copy to array.
        InterfaceStateBag[] rslt = new InterfaceStateBag[l.size()];
        l.toArray(rslt);

        return rslt;

    }

}
