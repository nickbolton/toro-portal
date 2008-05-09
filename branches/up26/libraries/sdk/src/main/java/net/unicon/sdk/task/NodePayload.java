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

package net.unicon.sdk.task;

import org.dom4j.Node;

/**
 * Implements <code>IPayload</code> to cary an XML document.
 */
public final class NodePayload implements IPayload {

    // Instance Members.
    private Node data;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>NodePayload</code> instance with the specified dom4j
     * node.
     *
     * @param d A dom4j XML document.
     */
    public NodePayload(Node n) {

        // Assertions.
        if (n == null) {
            String msg = "Argument 'n [Node]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.data = (Node) n.clone();

    }

    /**
     * Provides access to this payload in XML format as a dom4j node.
     *
     * @return A dom4j XML node representing this payload.
     */
    public Node toXml() {
        return (Node) data.clone();
    }

}
