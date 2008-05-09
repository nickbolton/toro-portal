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
 * A cluster of available interaction(s) within a mode.  An interface may
 * support one or more actions, and may include choices, inputs, and/or state
 * data.
 */
public final class Interface implements IComponent {

    // Instance Members.
    private String handle;
    private IInterfaceComponent[] components;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Interface</code> that contains the specidied
     * components.
     *
     * @param components the set of components that are a part of this
     * <code>Interface</code>
     */
    public Interface(String handle, IInterfaceComponent[] components) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (components == null) {
            String msg = "Argument 'components' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (components.length == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Argument 'components' must contain ");
            msg.append("at least 1 element.");
            throw new IllegalArgumentException(msg.toString());
        }
        for (int i=0; i < components.length; i++) {
            if (components[i] == null) {
                StringBuffer msg = new StringBuffer();
                msg.append("The 'components' array ");
                msg.append("may not contain null references.");
                throw new IllegalArgumentException(msg.toString());
            }
        }

        this.handle = handle;
        this.components = components;

    }

    /**
     * ToDo:  javadoc.
     */
    public Choice getChoice(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Loop & Seek.
        Choice rslt = null;
        for (int i=0; i < components.length; i++) {
            if (components[i] instanceof Choice) {
                Choice c = (Choice) components[i];
                if (c.getHandle().equals(handle)) {
                    rslt = c;
                    break;
                }
            }
        }

        // Complain if we didn't find it.
        if (rslt == null) {
            String msg = "No choice defined for handle:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return rslt;

    }

    /**
     * Creates and returns an XML representation of this interface.  Calling
     * <code>toXml</code> on this component will cause it to call the same
     * method upon and include its children recursively.  The resulting XML
     * conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render this
     * interface, its children, and the rest of the mode.
     * @return an XML representation of the interface and its children.
     */
    public String toXml(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Validate the inputs.
        validateState(state);

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<interface>");

        // State Object(s).
        IStateObject[] objs = state.getObjects();
        for (int i=0; i < objs.length; i++) {
            rslt.append("<state-object>");
            rslt.append(objs[i].toXml());
            rslt.append("</state-object>");
        }

        // Component(s).
        for (int i=0; i < components.length; i++) {
            rslt.append(components[i].toXml(state));
        }

        // End.
        rslt.append("</interface>");
        return rslt.toString();

    }

    /**
     * Prepares and returns an array referencing the <code>StateKey</code>
     * objects employed by this <code>Interface</code> and any components
     * contained within it.
     *
     * @return the <code>StateKey</code>s in use by this <code>Interface</code>.
     */
    public StateKey[] getStateKeys() {

        // Find the StateKey(s) of our options.
        List keys = new ArrayList();
        for (int i=0; i < components.length; i++) {
            List l = Arrays.asList(components[i].getStateKeys());
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

    Action[] getActions() {

        List acts = new ArrayList();
        for (int i=0; i < components.length; i++) {
            if (components[i] instanceof Action) acts.add(components[i]);
        }
        Action[] rslt = new Action[acts.size()];
        acts.toArray(rslt);
        return rslt;

    }

    /*
     * Implementation.
     */

    private void validateState(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure all defined keys are represented.
        StateKey[] defined = getStateKeys();
        for (int i=0; i < defined.length; i++) {
            if (state.getEntry(defined[i]) == null) {
                String msg = "Invalid State --> Missing StateKey:  "
                                        + defined[i].getHandle();
                throw new IllegalArgumentException(msg);
            }
        }

        // Make sure all supplied keys are defined.
        StateKey[] supplied = state.getStateKeys();
        for (int i=0; i < supplied.length; i++) {
            boolean isDefined = false;
            for (int j=0; j < defined.length; j++) {
                if (supplied[i].equals(defined[j])) {
                    isDefined = true;
                    break;
                }
            }
            if (!isDefined) {
                String msg = "Invalid State --> Unexpected StateKey:  "
                                        + supplied[i].getHandle();
                throw new IllegalArgumentException(msg);
            }
        }

    }

}
