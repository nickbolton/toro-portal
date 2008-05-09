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

package net.unicon.penelope;

/**
 * The <code>IDecision</code> interface abstracts the idea of a decision
 * contaning 0 or more selections.
 */
public interface IDecision extends IPenelopeEntity {

    /**
     * Retrieve the corresponding choice.
     * @return the corresponding choice
     */
    IChoice getChoice();

    /**
     * Gets the selection for the specified option, or <code>null</code> if the
     * option was not selected.
     *
     * @param o An option belonging to the associated choice.
     * @return The relevant selection or <code>null</code>.
     * @throws IllegalArgumentException If argument 'o' does not belong to the
     * choice referenced by this decision.
     */
    ISelection getSelection(IOption o);

    /**
     * Retrieve the selection that corresponds to a named option.
     * @return the selection associated with the named option, or null
     */
    ISelection getSelection(String optionHandle);

    /**
     * Retrieve the selections in this decision.
     * @return the selections of this decision
     */
    ISelection[] getSelections();

    /**
     * Convienence method to acquire the handle of the first Selection's
     * associated Option.
     * @return handle of the first selection's associated option, or null
     */
    String getFirstSelectionHandle();

    /**
     * Convienence method to acquire the Complement Value of the first
     * Selection.
     * @return complement value of the first selection, or null
     */
    Object getFirstSelectionValue();

}
