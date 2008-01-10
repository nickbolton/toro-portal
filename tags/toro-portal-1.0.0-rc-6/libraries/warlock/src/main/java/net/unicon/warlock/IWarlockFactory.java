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

package net.unicon.warlock;

import org.dom4j.Element;

public interface IWarlockFactory {

    IRenderingEngine getRenderingEngine();

    /**
     * Creates a new screen based on the specified XML fragment.
     *
     * @param e An XML fragment that defines a screen.
     * @throws WarlockException If a screen could not be created from the
     * specified XML.
     */
    IScreen parseScreen(Element e) throws WarlockException;

    /**
     * Creates a new screen based on the specified XML fragment.  Callers may
     * provide and include reusable sub-fragments by passing them in the
     * <code>reference</code> argument.
     *
     * @param e An XML fragment that defines a screen.
     * @param reference Zero or more &lt;reference&gt; XML fragments.
     * @throws WarlockException If a screen could not be created from the
     * specified XML.
     */
    IScreen parseScreen(Element e, Element[] reference) throws WarlockException;

}