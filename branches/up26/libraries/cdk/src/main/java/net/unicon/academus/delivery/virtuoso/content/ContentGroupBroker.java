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

package net.unicon.academus.delivery.virtuoso.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ContentGroupBroker {

    private static Map instances = new HashMap();

    /*
     * Public API.
     */

    public static IContentGroup getContentGroup(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (instances == null) {
            refresh();
        }

        IContentGroup rslt = (IContentGroup) instances.get(handle);
        if (!instances.containsKey(handle)) {
            String msg = "No content group found with specified handle:  "
                                                            + handle;
            throw new IllegalArgumentException(msg);
        }
        return rslt;

    }

    public static IContentGroup[] getAvailableContentGroups() {

        if (instances == null) {
            refresh();
        }

        return (IContentGroup[]) instances.values().toArray(
                                    new IContentGroup[0]);

    }

    public static synchronized void refresh() {

    }

    /*
     * Implementation.
     */

    private ContentGroupBroker() {}

    /*
     * Nested Types.
     */

    private static final class ContentGroupImpl implements IContentGroup {

        // Instance Members.
        private String handle;
        private String name;

        /*
         * Public API.
         */

        public ContentGroupImpl(String handle, String name) {

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (name == null) {
                String msg = "Argument 'name' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;
            this.name = name;

            // Add to collection.
            instances.put(handle, this);

        }

        public String getHandle() {
            return handle;
        }

        public String getName() {
            return name;
        }

        public boolean equals(Object o) {

            if (o == null) {
                return false;
            }

            boolean rslt = false;

            if (o instanceof ContentGroupImpl) {
                ContentGroupImpl g = (ContentGroupImpl) o;
                rslt = g.handle.equals(handle);
            }

            return rslt;

        }

        public int hashCode() {
            return handle.hashCode();
        }

    }

}
