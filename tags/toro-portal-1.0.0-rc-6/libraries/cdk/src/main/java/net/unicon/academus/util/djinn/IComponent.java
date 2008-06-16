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
 * A modality component within the Djinn Toolkit.
 */
public interface IComponent {

    /**
     * Creates and returns an XML representation of this component.  In the case
     * of a container, calling <code>toXml</code> on this component will cause
     * it to call and include its children recursively.  The resulting XML
     * conforms to the Djinn DTD.
     *
     * @param state the collection of state values needed to render the mode
     * (template) of which this component is a part.
     * @return an XML representation of the component.
     */
    String toXml(InterfaceStateBag state);

    /**
     * Prepares and returns an array referencing all <code>StateKey</code>
     * objects employed by this component.
     *
     * @return the <code>StateKey</code>s in use by this component.
     */
    StateKey[] getStateKeys();

}
