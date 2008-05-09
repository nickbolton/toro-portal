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

package net.unicon.alchemist;

public final class EntityEncoder {

    private static final String[][] tuples = new String[][] {
                new String[] { "&", "&amp;" },   // *must* be first!
                new String[] { ">", "&gt;" },
                new String[] { "<", "&lt;" },
                new String[] { "\"", "&quot;" },
                new String[] { "\'", "&apos;" }
            };

    /*
     * Public API.
     */

    public static String encodeEntities(String inpt) {

        // Assertions.
        if (inpt == null) {
            String msg = "Argument 'inpt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        String rslt = inpt;

        for (int i=0; i < tuples.length; i++) {
            String[] entry = tuples[i];
            rslt = rslt.replaceAll(entry[0], entry[1]);
        }

        return rslt;

    }

    /*
     * Implementation.
     */

    private EntityEncoder() {}

}
