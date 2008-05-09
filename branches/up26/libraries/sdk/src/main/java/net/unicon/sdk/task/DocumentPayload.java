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

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;

/**
 * Implements <code>IPayload</code> to cary an XML document.
 */
public final class DocumentPayload implements IPayload {

    // Instance Members.
    private Document doc;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>DocumentPayload</code> instance with the specified
     * w3c DOM document.
     *
     * @param d A w3c XML document.
     */
    public DocumentPayload(org.w3c.dom.Document d) {

        // Assertions.
        if (d == null) {
            String msg = "Argument 'd [Document]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        DOMReader r = new DOMReader();
        this.doc = r.read(d);

    }

    /**
     * Creates a new <code>DocumentPayload</code> instance with the specified
     * dom4j document.
     *
     * @param d A dom4j XML document.
     */
    public DocumentPayload(Document d) {

        // Assertions.
        if (d == null) {
            String msg = "Argument 'd [Document]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.doc = (Document) d.clone();

    }

    /**
     * Provides access to this payload in XML format as a dom4j node.
     *
     * @return A dom4j XML node representing this payload.
     */
    public Node toXml() {
        return (Node) doc.clone();
    }

}
