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
 
package net.unicon.sdk.catalog;

import java.util.List;

/**
 * A collection of entries that supports paging.  A <code>Catalog</code> object
 * can support entries of any type.  The <code>Catalog</code> provides methods
 * that sort, filter, and break a collection into individual pages.
 */
public interface Catalog {
    /**
     * Creates and returns a new <code>Catalog</code> that is a subset of this
     * one.  Use <code>ISortMode</code> objects to specify the order of entries
     * in the resultant collection.  Use <code>IFilterMode</code> objects to
     * exclude some entries from the results.  The <code>PageMode</code> allows
     * the <code>Catalog</code> to organize the entries into pages.  It
     * specifies how many entries should appear on each page, as well as which
     * page(s) should be included in the results (returned collection).
     * <p>
     * The <code>PageMode</code> also serves as the mechanism whereby this
     * <code>Catalog</code> can let clients know how many pages of entries are
     * available given the specified page size.
     * <p>
     * Note that this method accepts 0, 1, or many sorts and/or filters, but
     * always exactly 1 page specification (<code>PageMode</code>).
     *
     * @param sorts objects that specify how the resulting <code>Catalog</code>
     * should be ordered.
     * @param filters objects that specify which entries should be excluded from
     * the resulting <code>Catalog</code>.
     * @param page specifies how many entires should appear on each page and
     * which page(s) should be included in the resulting <code>Catalog</code>.
     * @return the subcatalog that conforms to the specified sorts, filters, and
     * page.
     */
    public Catalog subCatalog(ISortMode[] sorts,
                IFilterMode[] filters, IPageMode page);
    /**
     * Returns all the entries contained in this <code>Catalog</code>.  This
     * method will throw a <code>CatalogException</code> if it wasn't able to
     * perform its responsibilities.
     *
     * @return a collection of all the entries contained in this
     * <code>Catalog</code>.
     * @throws net.unicon.common.util.catalog.CatalogException if this
     * <code>Catalog</code> is unable to gather the specified elements.
     */
    public List elements() throws CatalogException;
}
