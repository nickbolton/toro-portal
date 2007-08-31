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

package net.unicon.academus.apps.content;

import org.dom4j.Element;

public final class UrlRewritingRule {

    // Instance Members.
    private final String markup;

    /*
     * Public API.
     */

    public static UrlRewritingRule parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("pattern")) {
            String msg = "Argument 'e [Element]' must be a <pattern> element.";
            throw new IllegalArgumentException(msg);
        }

        // Markup.
        String markup = e.asXML();

        return new UrlRewritingRule(markup);

    }

    public void toXml(StringBuffer otpt) {

        // Assertions.
        if (otpt == null) {
            String msg = "Argument 'otpt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        otpt.append(markup);

    }

    /*
     * Implementation.
     */

    private UrlRewritingRule(String markup) {

        // Assertions.
        if (markup == null) {
            String msg = "Argument 'markup' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.markup = markup;

    }

}