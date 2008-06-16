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
import java.util.List;
import java.util.Map;

import net.unicon.academus.common.AcademusException;

/**
 * Represents an available action that may be taken by a client.  This action
 * may or may not result in changes of mode or state.
 */
public final class Action implements IInterfaceComponent {

    // Instance Members.
    private String handle;
    private IActionWorker worker;
    private String labelString;
    private StateKey label;
    private String statusCode;
    private StateKey status;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Action</code> with the specified handle, worker,
     * label, and status.
     *
     * @param handle the handle (short code name) for this <code>Action</code>.
     * @param worker the <code>IActionWorker</code> that encapsulates the
     * implementation of this action..
     * @param label the label shown to the user for this <code>Action</code>.
     * @param status a code that represents the status of this action.
     */
    public Action(String handle, IActionWorker worker, String label,
                                            String status) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (worker == null) {
            String msg = "Argument 'worker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (status == null) {
            String msg = "Argument 'status' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.worker = worker;
        this.labelString = label;
        this.label = null;
        this.statusCode = status;
        this.status = null;

    }

    /**
     * Constructs a new <code>Action</code> with static handle, worker, and
     * status but a dynamic label.  Clients of this action will use the label
     * <code>StateKey</code> to assign a label to this action on a per-click
     * basis.
     *
     * @param handle the handle (short code name) for this <code>Action</code>.
     * @param worker the <code>IActionWorker</code> that encapsulates the
     * implementation of this action..
     * @param label a <code>StateKey</code> that corresponds to the label for
     * this action.
     * @param status a code that represents the status of this action.
     */
    public Action(String handle, IActionWorker worker, StateKey label,
                                            String status) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (worker == null) {
            String msg = "Argument 'worker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (status == null) {
            String msg = "Argument 'status' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.worker = worker;
        this.labelString = null;
        this.label = label;
        this.statusCode = status;
        this.status = null;

    }

    /**
     * Constructs a new <code>Action</code> with static handle, worker, and
     * label but dynamic status.  Clients of this action will use the status
     * <code>StateKey</code> to assign a <code>StateCode</code> to this action
     * on a per-click basis.
     *
     * @param handle the handle (short code name) for this <code>Action</code>.
     * @param worker the <code>IActionWorker</code> that encapsulates the
     * implementation of this action..
     * @param label the label shown to the user for this <code>Action</code>.
     * @param status a <code>StateKey</code> that corresponds to the status of
     * this action.
     */
    public Action(String handle, IActionWorker worker, String label,
                                            StateKey status) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (worker == null) {
            String msg = "Argument 'worker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (status == null) {
            String msg = "Argument 'status' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.worker = worker;
        this.labelString = label;
        this.label = null;
        this.statusCode = null;
        this.status = status;

    }

    /**
     * Constructs a new <code>Action</code> with static handle and worker, but
     * dynamic label and status.  Clients of this action will use the label
     * <code>StateKey</code> to assign a label to this action on a per-click
     * basis.  Similarly, clients of this action will use the status
     * <code>StateKey</code> to assign a <code>StateCode</code> to this action
     * on a per-click basis.
     *
     * @param handle the handle (short code name) for this <code>Action</code>.
     * @param worker the <code>IActionWorker</code> that encapsulates the
     * implementation of this action..
     * @param label a <code>StateKey</code> that corresponds to the label for
     * this action.
     * @param status a <code>StateKey</code> that corresponds to the status of
     * this action.
     */
    public Action(String handle, IActionWorker worker, StateKey label,
                                            StateKey status) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (worker == null) {
            String msg = "Argument 'worker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (status == null) {
            String msg = "Argument 'status' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.worker = worker;
        this.labelString = null;
        this.label = label;
        this.statusCode = null;
        this.status = status;

    }

    /**
     * Performs the action represented by this object.  The
     * <code>contextId</code> can be anything on which the
     * <code>IActionWorker</code> and the application agree, and allows the
     * <code>IActionWorker</code> implementation to identify the relevent
     * context.
     *
     * @param applicationId the unique identifier for the relevant application.
     * @param args the collection of inputs from the UI.
     * @return the result of this action (new mode and state).
     */
    public ActionResult execute(String applicationId, Map args)
                                            throws AcademusException {

        // Assertions.
        if (applicationId == null) {
            String msg = "Argument 'applicationId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return worker.doAction(applicationId, args);

    }

    /**
     * Creates and returns an XML representation of this <code>Action</code>.
     * The resulting XML conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render the screen
     * (template) of which this action is a part.
     * @return an XML representation of this action.
     */
    public String toXml(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<action ");

        // Handle.
        rslt.append("handle=\"");
        rslt.append(handle);
        rslt.append("\" ");

        // Status.
        rslt.append("status=\"");
        if (statusCode != null) {
            rslt.append(statusCode);
        } else {
            String s = state.getEntry(status);
            if (s == null) {
                String msg = "StateKey for '" + status.getHandle()
                                        + "' was not defined";
                throw new IllegalStateException(msg);
            }
            rslt.append(s);
        }
        rslt.append("\">");

        // Label.
        rslt.append("<label>");
        if (labelString != null) {
            rslt.append(labelString);
        } else {
            String s = state.getEntry(label);
            if (s == null) {
                String msg = "StateKey for '" + label.getHandle()
                                        + "' was not defined";
                throw new IllegalStateException(msg);
            }
            rslt.append(s);
        }
        rslt.append("</label>");

        // End.
        rslt.append("</action>");
        return rslt.toString();

    }

    /**
     * Prepares and returns an array referencing the <code>StateKey</code>
     * objects employed by this <code>Action</code>.
     *
     * @return the <code>StateKey</code>s in use by this <code>Action</code>.
     */
    public StateKey[] getStateKeys() {

        List keys = new ArrayList();
        if (status != null) keys.add(status);
        if (label != null) keys.add(label);

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
