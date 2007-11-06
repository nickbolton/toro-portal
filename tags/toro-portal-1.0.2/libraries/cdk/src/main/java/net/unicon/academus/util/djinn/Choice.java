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
import java.util.Arrays;
import java.util.List;

/**
 * Represents a choice that may be made by a client.
 */
public final class Choice implements IInterfaceComponent {

    // Instance Members.
    private String handle;
    private String label;
    private Option[] options;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Choice</code> with unspecified options.
     * The options for this choice are dynamic and come from the
     * <code>InterfaceStateBag</code> at the time of rendering.  Throws an
     * <code>IllegalArgumentException</code> if the handle is null.
     *
     * @param handle the handle (short code name) for this <code>Choice</code>.
     * @param label the label for this <code>Choice</code>.
     */
    public Choice(String handle, String label) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'label' may be null.

        // Instance Members.
        this.handle = handle;
        this.label = label;
        this.options = null;

    }

    /**
     * Constructs a new <code>Choice</code> with a fixed (static) set of
     * options.  Throws an <code>IllegalArgumentException</code> if either
     * the handle or the options is null, or if the options array is
     * zero-length.
     *
     * @param handle the handle (short code name) for this <code>Choice</code>.
     * @param label the label for this <code>Choice</code>.
     * @param options the set of available options for this choice.
     */
    public Choice(String handle, String label, Option[] options) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'label' may be null.
        if (options == null) {
            String msg = "Argument 'options' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (options.length == 0) {
            String msg = "Argument 'options' must contain at least 1 element.";
            throw new IllegalArgumentException(msg);
        }
        for (int i=0; i < options.length; i++) {
            if (options[i] == null) {
                StringBuffer msg = new StringBuffer();
                msg.append("The 'options' array ");
                msg.append("may not contain null references.");
                throw new IllegalArgumentException(msg.toString());
            }
        }

        // Instance Members.
        this.handle = handle;
        this.label = label;
        this.options = options;

    }

    /**
     * Creates and returns an XML representation of this <code>Choice</code>.
     * The resulting XML conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render the screen
     * (template) of which this choice is a part.
     * @return an XML representation of this choice.
     */
    public String toXml(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<choice ");

        // Handle.
        rslt.append("handle=\"");
        rslt.append(handle);
        rslt.append("\" ");

        // Multiplicity.
        rslt.append("multiplicity=\"");
        rslt.append("single");  // ToDo:  Implement!
        rslt.append("\">");

        // Label.
        if (label != null) {
            rslt.append("<label>");
            rslt.append(label);
            rslt.append("</label>");
        }

        // Options.
        Option[] optns = (options != null)?options:state.getOptions(this);
        for (int i=0; i < optns.length; i++) {
            rslt.append(optns[i].toXml(state));
        }

        // End.
        rslt.append("</choice>");
        return rslt.toString();

    }

    /**
     * Prepares and returns an array referencing the <code>StateKey</code>
     * objects employed by this <code>Choice</code> and any components contained
     * within it.
     *
     * @return the <code>StateKey</code>s in use by this <code>Choice</code>.
     */
    public StateKey[] getStateKeys() {

        // Get out if we use dynamic options.
        if (options == null) return new StateKey[0];

        // Find the StateKey(s) of our options.
        List keys = new ArrayList();
        for (int i=0; i < options.length; i++) {
            List l = Arrays.asList(options[i].getStateKeys());
            keys.addAll(l);
        }

        // Return the aggregate.
        StateKey[] rslt = new StateKey[keys.size()];
        keys.toArray(rslt);
        return rslt;

    }

    /*
     * Package API.
     */

    String getHandle() {
        return handle;
    }

}
