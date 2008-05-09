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
 * An item of information included within an interface.  This item is static
 * within the mode.  It cannot be altered, only returned as part of the
 * interface.
 */
public final class Token implements IInterfaceComponent {

    // Instance Members.
    private String handle;
    private String valueString;
    private StateKey value;

    /*
     * Public API.
     */

    /**
     * Constructs a new <code>Token</code> with the specified handle and value.
     * The value of this <code>Token</code> will never change.
     *
     * @param handle the handle (short code name) for this <code>Token</code>.
     * @param value the value that this <code>Token</code> will have.
     */
    public Token(String handle, String value) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "Argument 'value' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.valueString = value;
        this.value = null;

    }

    /**
     * Constructs a new <code>Token</code> with the specified handle and value
     * key.  The <code>StateKey</code> allows this token to assume its value at
     * marshalling time.
     *
     * @param handle the handle (short code name) for this <code>Token</code>.
     * @param value a <code>StateKey</code> that represents this token's value.
     */
    public Token(String handle, StateKey value) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "Argument 'value' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.handle = handle;
        this.valueString = null;
        this.value = value;

    }

    /**
     * Creates and returns an XML representation of this <code>Token</code>.
     * The resulting XML conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render the screen
     * (template) of which this token is a part.
     * @return an XML representation of this token.
     */
    public String toXml(InterfaceStateBag state) {

        // Assertions.
        if (state == null) {
            String msg = "Argument 'state' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<token ");

        // Handle.
        rslt.append("handle=\"");
        rslt.append(handle);
        rslt.append("\" ");

        // Value.
        rslt.append("value=\"");
        if (valueString != null) {
            rslt.append(valueString);
        } else {
            String s = state.getEntry(value);
            if (s == null) {
                String msg = "StateKey for '" + value.getHandle()
                                        + "' was not defined";
                throw new IllegalStateException(msg);
            }
            rslt.append(s);
        }
        rslt.append("\" ");

        // End.
        rslt.append("/>");
        return rslt.toString();

    }

    /**
     * Prepares and returns an array referencing the <code>StateKey</code>
     * objects employed by this <code>Token</code>.
     *
     * @return the <code>StateKey</code>s in use by this <code>Token</code>.
     */
    public StateKey[] getStateKeys() {

        StateKey[] rslt = null;
        if (value != null) {
            rslt = new StateKey[] { value };
        } else {
            rslt = new StateKey[0];
        }
        return rslt;

    }

}
