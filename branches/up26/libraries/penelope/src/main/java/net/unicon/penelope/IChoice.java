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
 * The <code>IChoice</code> interface abstracts the idea of a choice containing
 * multiple options.
 */
public interface IChoice extends IPenelopeEntity {

    /**
     * Retrieve the choice's identifier.
     * @return the choice's indentifying handle
     */
    Handle getHandle();

    /**
     * Get the human readable label associated with the choice.
     * @return human readable label, or null
     */
    Label getLabel();

    /**
     * Retrieve the named option from the choice.
     * @param h String handle of the option to retrieve.
     * @return named option
     * @throws IllegalArgumentException if the named option does not exist.
     */
    IOption getOption(String h);

    /**
     * Retrieve the named option from the choice.
     * @param h Handle of the option to retrieve.
     * @return named option
     * @throws IllegalArgumentException if the named option does not exist.
     */
    IOption getOption(Handle h);

    /**
     * Retrieve all options in this choice.
     * @return all options in the choice
     */
    IOption[] getOptions();

    /**
     * Get the minimum number of allowed selections for the choice.
     * @return minimum number of allowed selections
     */
    int getMinSelections();

    /**
     * Get the maximum number of allowed selections for the choice.
     * @return maximum number of allowed selections
     */
    int getMaxSelections();

}
