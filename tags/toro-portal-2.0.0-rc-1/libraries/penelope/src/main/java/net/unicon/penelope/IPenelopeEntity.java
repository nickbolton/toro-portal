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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface IPenelopeEntity {

    long getId();

    IEntityStore getOwner();

    /**
     * Selects a decendant object that matches the specified expression.  Valid
     * expressions use a "path-like" syntax, where elements in the path are
     * separated by a slash ("/") character.  Each element in the path must
     * begin with one of the following characters:<p>
     *
     * <ul>
     *   <li>. (signifies the context entity)</li>
     *   <li>@ (followed by a handle, signifies a handle)</li>
     *   <li># (signifies a complement)</li>
     *   <li>! (signifies the payload or "value" of a complement)</li>
     * </ul><p>
     *
     * The type of the returned object may depend on whether the context entity
     * (i.e. the one upon which <code>select()</code> is being invoked) is a
     * member of a choice tree or a decision tree.  For example, the expression
     * "@color/@other" would return an option if invoked upon an
     * <code>IChoiceCollection</code> instance, but a selection if invoked upon
     * an <code>IDecisonCollection</code> instance.  The path elements "#" and
     * "!" have no meaning in the context of a choice tree.
     *
     * @param expression A path-like expression that evaluates to a decendant
     * object.
     * @return An entity that matches the specified expression.
     */
    IPenelopeEntity select(String expression);

    String toXml();
    
    void sendXmlEvents(ContentHandler ch) throws SAXException;

}