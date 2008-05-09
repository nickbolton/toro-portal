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
 * Provides <code>Catalog</code> implementations access to their data.
 */
public interface IDataSource {
    /**
     * Returns the collection of entries that conforms to the specified sorts,
     * filters, and page inputs.  Use <code>ISortMode</code> objects to specify
     * the order of entries in the resultant collection.  Use
     * <code>IFilterMode</code> objects to exclude some entries from the results.
     * Where appropriate, use the <code>PageMode</code> to specify how many
     * entries should appear on each page and which page(s) should be included
     * in the results.
     * <p>
     * Note that this method signature is identical to <code>subCatalog</code>
     * on the <code>Catalog</code> itself.  It is up to individual
     * <code>Catalog</code> implementations to decide when and how to access
     * their data.  For example, one <code>Catalog</code> might call
     * <code>fetchData</code> at creation time to store the entire entry set,
     * while another might only call <code>fetchData</code> at the last second
     * (when it needs to hand out its entries).
     * <p>
     * The <code>IPageMode</code> also allows the <code>IDataSource</code> to let
     * its client know how many pages of entries are available given the
     * specified page size.
     *
     * @param sorts objects that specify how the resulting <code>Catalog</code>
     * should be ordered.
     * @param filters objects that specify which entries should be excluded from
     * the resulting <code>ICatalog</code>.
     * @param page specifies how many entires should appear on each page and
     * which page(s) should be included in the resulting <code>Catalog</code>.
     * @return the collection of entries that conforms to the specified sorts,
     * filters, and page.
     */
    public List fetchData(ISortMode[] sorts, IFilterMode[] filters,
                                IPageMode page) throws CatalogException;
}
