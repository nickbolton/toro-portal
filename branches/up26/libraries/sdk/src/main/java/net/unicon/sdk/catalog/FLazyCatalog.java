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

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Catalog</code> implementation that always relies on its
 * <code>IDataSource</code> for information about its entries.
 * <code>FLazyCatalog</code> does not cache information of any sort;  it merely
 * holds on to its sorts, filters, and page information until a client calls the
 * <code>elements()</code> method.  Subsequent calles to <code>elements()</code>
 * will cause the <code>FLazyCatalog</code> to go to its <code>IDataSource</code>
 * again.
 */
public final class FLazyCatalog implements Catalog {
    private IDataSource ds;
    private ISortMode[] sorts;
    private IFilterMode[] filters;
    private IPageMode page;
    /*
     * PUBLIC API
     */
    /**
     * Constructs a new <code>FLazyCatalog</code> with the specified
     * <code>IDataSource</code>.  This constructor will throw an
     * <code>IllegalArgumentException</code> if the <code>IDataSource</code>
     * reference is <code>null</code>.
     *
     * @param ds the <code>IDataSource</code> that this <code>FLazyCatalog</code>
     * should use to obtain its data.
     */
    public FLazyCatalog(IDataSource ds) {
        // Assertions.
        if (ds == null) {
            String msg = "Argument ds (IDataSource) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        this.ds = ds;
        this.sorts = new ISortMode[] { };
        this.filters = new IFilterMode[] { };
        this.page = null;
    }
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
                IFilterMode[] filters, IPageMode page) {
        // Assertions.
        if (sorts == null) {
            String msg = "Argument sorts cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (filters == null) {
            String msg = "Argument filters cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // sorts.
        ISortMode[] sAry = null;
        if (sorts.length > 0) {
            List s = new ArrayList();
            for (int i = 0; i < this.sorts.length; i++) {
                s.add(this.sorts[i]);
            }
            for (int i = 0; i < sorts.length; i++) {
                s.add(sorts[i]);
            }
            sAry = (ISortMode[]) s.toArray(new ISortMode[0]);
        } else {
            sAry = this.sorts;
        }
        // filters.
        IFilterMode[] fAry = null;
        if (filters.length > 0) {
            List f = new ArrayList();
            for (int i = 0; i < this.filters.length; i++) {
                f.add(this.filters[i]);
            }
            for (int i = 0; i < filters.length; i++) {
                f.add(filters[i]);
            }
            fAry = (IFilterMode[]) f.toArray(new IFilterMode[0]);
        } else {
            fAry = this.filters;
        }
        // return.
        return new FLazyCatalog(ds, sAry, fAry, page);
    }
    /**
     * Returns all the entries contained in this <code>Catalog</code>.  This
     * method will throw a <code>CatalogException</code> if it wasn't able to
     * perform its responsibilities.
     * <p>
     * Note that the <code>FLazyCatalog</code> defers all interaction with its
     * <code>IDataSource</code> until this method is called.  Subsequent calls to
     * this method will cause the <code>FLazyCatalog</code> to turn to the
     * <code>IDataSource</code> each time for fresh information.
     *
     * @return a collection of all the entries contained in this
     * <code>Catalog</code>.
     * @throws net.unicon.common.util.catalog.CatalogException when the
     * <code>IDataSource</code> cannot gather the collection of elements.
     */
    public List elements() throws CatalogException {
        return ds.fetchData(sorts, filters, page);
    }
    /*
     * IMPLEMENTATION
     */
    private FLazyCatalog(IDataSource ds, ISortMode[] sorts,
                        IFilterMode[] filters, IPageMode page) {
        // Assertions.
        if (ds == null) {
            String msg = "Argument ds (IDataSource) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (sorts == null) {
            String msg = "Argument sorts cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (filters == null) {
            String msg = "Argument filters cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        this.ds = ds;
        this.sorts = sorts;
        this.filters = filters;
        this.page = page;
    }
}
