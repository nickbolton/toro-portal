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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an application's 'modality' (state and available state
 * transitions) at a point in time (click).
 */
public final class Mode {

    // Instance Members.
    private String handle;
    private Interface[] interfaces;
    private Map actions;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Mode</code> that exposes the specidied interfaces.
     *
     * @param interfaces the set of interfaces that are a part of this
     * <code>Mode</code>
     */
    public Mode(String handle, Interface[] interfaces) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (interfaces == null) {
            String msg = "Argument 'interfaces' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (interfaces.length == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Argument 'interfaces' must contain ");
            msg.append("at least 1 element.");
            throw new IllegalArgumentException(msg.toString());
        }
        for (int i=0; i < interfaces.length; i++) {
            if (interfaces[i] == null) {
                StringBuffer msg = new StringBuffer();
                msg.append("The 'interfaces' array ");
                msg.append("may not contain null references.");
                throw new IllegalArgumentException(msg.toString());
            }
        }

        // Instance Members.
        this.handle = handle;
        this.interfaces = interfaces;

        // Actions.
        this.actions = new HashMap();
        for (int i=0; i < interfaces.length; i++) {
            Action[] acts = interfaces[i].getActions();
            for (int j=0; j < acts.length; j++) {
                String hdl = acts[j].getHandle();
                if (actions.containsKey(hdl)) {
                    if (acts[j] != actions.get(hdl)) {
                        StringBuffer msg = new StringBuffer();
                        msg.append("Within a single mode, there may be only ");
                        msg.append("one Action instance with a given ");
                        msg.append("handle.  This instance may be referenced ");
                        msg.append("multiple times.");
                        throw new IllegalArgumentException(msg.toString());
                    }
                } else {
                    actions.put(hdl, acts[j]);
                }
            }
        }

    }

    /**
     * Returns the handle for this <code>Mode</code>.
     *
     * @return the handle for this <code>Mode</code>.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Obtains a reference to an interface contained within this mode.  Throws
     * <code>IllegalArgumentException</code> if the handle does not match an
     * available interface.
     *
     * @param handle the handle to the desired interface.
     * @return the interface that corresponds to the specified handle.
     */
    public Interface getInterface(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Lookup the Interface.
        Interface rslt = null;
        for (int i=0; i < interfaces.length; i++) {
            if (interfaces[i].getHandle().equals(handle)) {
                rslt = interfaces[i];
                break;
            }
        }

        // Make sure we have one.
        if (rslt == null) {
            String msg = "No interface defined for handle:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return rslt;

    }

    /**
     * Obtains a reference to an available action with the specified handle.
     * Throws <code>IllegalArgumentException</code> if the handle does not match
     * an allowable action from this mode.
     *
     * @param handle the handle to the desired action.
     * @return the action that corresponds to the specified handle.
     */
    public Action getAction(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!actions.containsKey(handle)) {
            String msg = "No action defined for handle:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (Action) actions.get(handle);

    }

    /**
     * Creates and returns an XML representation of this mode.  Calling
     * <code>toXml</code> on this component will cause it to call the same
     * method upon and include its children recursively.  The resulting XML
     * conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render this mode.
     * @return an XML representation of this mode and its children.
     */
    public String toXml(ModeStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<mode ");

        // Handle.
        rslt.append("handle=\"");
        rslt.append(handle);
        rslt.append("\">");

        // State Object(s).
        IStateObject[] objs = state.getObjects();
        for (int i=0; i < objs.length; i++) {
            rslt.append("<state-object>");
            rslt.append(objs[i].toXml());
            rslt.append("</state-object>");
        }

        // Interface(s).
        for (int i=0; i < interfaces.length; i++) {
            InterfaceStateBag[] bags = state.getInterfaceBags(interfaces[i]);
            for (int j=0; j < bags.length; j++) {
                rslt.append(interfaces[i].toXml(bags[j]));
            }
        }

        // End.
        rslt.append("</mode>");
        return rslt.toString();

    }

}
