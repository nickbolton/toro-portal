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

import net.unicon.sdk.catalog.CatalogException;

/**
 * Converts entries from a <code>Collection</code> to catalog entries.  Note that
 * the runtime type for catalog entries is not defined;  catalog clients are
 * free to implement their entries in any fashion.
 */
public interface IColEntryConvertor {
    /**
     * Converts a individual entry from a <code>Collection</code> to a catalog
     * entry.  The <code>IColEntryConvertor</code> will convert the entry at the
     * current index position.
     *
     * @param entry from the current index position of a <code>Collection</code>
     * @return a catalog entry based on the choosen collection entry.
     * @throws net.unicon.common.util.catalog.CatalogException if the
     * <code>IColEntryConvertor</code> can't convert the entry.
     */
    public Object convertEntry(Object entry) throws CatalogException;
}
