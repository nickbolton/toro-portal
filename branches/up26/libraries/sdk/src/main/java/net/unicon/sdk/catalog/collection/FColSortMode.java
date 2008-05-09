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
package net.unicon.sdk.catalog.collection;

import java.util.Comparator;

import net.unicon.sdk.catalog.ISortMode;

/**
 * Allows catalog clients using a <code>FColUserEntryDataSource</code> to specify the order
 * in which entries should appear.
 */
public final class FColSortMode implements ISortMode {

    private Comparator sort = null;

    /**
     * Provides access to the sort comparator for this mode object.
     *
     * @return the sort comparator for this mode object.
     */
    public Comparator getSort() {
        return this.sort;
    }

    /**
     * Creates a new <code>FColSortMode</code> from the specified comparator.
     *
     * @param sort a comparator designed to sort the results of a collection 
     */
    public FColSortMode(Comparator sort) {
        if (sort == null) {
            String msg = "Argument sort cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        this.sort = sort;
    }
}
