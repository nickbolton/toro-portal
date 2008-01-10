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
 * A single, exclusive possibility within a choice.  Coices contain one or more
 * available options.  A lone option within a choice implies the negative.
 */
public final class Option implements IComponent {

    // Instance Members.
    private String handle;
    private String label;
    private boolean selectedBool;
    private StateKey selectedKey;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Option</code> with the specified handle, label,
     * and selected value.
     *
     * @param handle the handle (short code name) for this <code>Option</code>.
     * @param label the label for this <code>Option</code>.
     * @param selected indicates whether the option is selected or not.
     */
    public Option(String handle, String label, boolean selected) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.label = label;
        this.selectedBool = selected;

    }

    /**
     * Constructs a new <code>Option</code> with the specified handle, label,
     * and <code>StateKey</code>.  The <code>StateKey</code> will be used at
     * marshalling to deturmise whether this option is currently selected or
     * not.  The corresponding state entry will be evaluated using
     * <code>Boolean.valueOf</code>.
     *
     * @param handle the handle (short code name) for this <code>Option</code>.
     * @param label the label for this <code>Option</code>.
     * @param selected a <code>StateKey</code> corresponding to this option's
     * state, either selected or not.
     */
    public Option(String handle, String label, StateKey selected) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (label == null) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (selected == null) {
            String msg = "Argument 'selected' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.label = label;
        this.selectedKey = selected;

    }

    /**
     * Creates and returns an XML representation of this <code>Option</code>.
     * The resulting XML conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render the screen
     * (template) of which this option is a part.
     * @return an XML representation of this option.
     */
    public String toXml(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<option ");

        // Handle.
        rslt.append("handle=\"");
        rslt.append(handle);
        rslt.append("\" ");

        // Selected.
        rslt.append("selected=\"");
        String entry = null;
        if (selectedKey != null) {
            entry = state.getEntry(selectedKey);
        } else {
            entry = (selectedBool)?"true":"false";
        }
        if (entry != null && Boolean.valueOf(entry).booleanValue()) {
            rslt.append("true");
        } else {
            rslt.append("false");
        }
        rslt.append("\">");

        // Label.
        rslt.append("<label>");
        rslt.append(label);
        rslt.append("</label>");

        // End.
        rslt.append("</option>");
        return rslt.toString();

    }

    /**
     * Prepares and returns an array referencing the <code>StateKey</code>
     * objects employed by this <code>Option</code>.
     *
     * @return the <code>StateKey</code>s in use by this <code>Option</code>.
     */
    public StateKey[] getStateKeys() {

        StateKey[] rslt = null;
        if (selectedKey != null) {
            rslt = new StateKey[] { selectedKey };
        } else {
            rslt = new StateKey[0];
        }
        return rslt;

    }

}
