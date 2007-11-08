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
package net.unicon.portal.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;


import net.unicon.academus.domain.lms.User;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.collection.FColFilterMode;
import net.unicon.sdk.catalog.collection.FColSortMode;
import net.unicon.sdk.catalog.collection.IColEntryConvertor;

public final class FColUserEntryDataSource implements IDataSource {
    private List entries;
    private IColEntryConvertor ec;

    /**
     * Creates a new <code>FColUserEntryDataSource</code> from the specified
     * <code>entries</code> and <code>IColEntryConvertor</code>.  The
     * <code>entries</code> represents a collection of objects 
     * before any filtering or sorting have been applied.
     *
     * @param entries a collection or <code>List</code> of objects
     * @param ec a <code>IColEntryConvertor</code> to use in converting
     * <code>List</code> entries into catalog entries.
     */
    public FColUserEntryDataSource(List entries, IColEntryConvertor ec) {
        // Assertions.
        if (entries == null) {
            String msg = "Argument entries cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ec == null) {
            String msg = "Argument ec (IColEntryConvertor) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Members.
        this.entries = entries;
        this.ec = ec;
    }

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
     * The <code>PageMode</code> also allows the <code>FColUserEntryDataSource</code> to
     * let its client know how many pages of entries are available given the
     * specified page size.
     *
     * @param sorts objects that specify how the resulting <code>Catalog</code>
     * should be ordered.
     * @param filters objects that specify which entries should be excluded from
     * the resulting <code>Catalog</code>.
     * @param page specifies how many entires should appear on each page and
     * which page(s) should be included in the resulting <code>Catalog</code>.
     * @return the collection of entries that conforms to the specified sorts,
     * filters, and page.
     */
    public List fetchData(ISortMode[] sorts, IFilterMode[] filters,
                                IPageMode page) throws CatalogException {
        // Assertions.
        if (sorts == null) {
            String msg = "Argument sorts cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (filters == null) {
            String msg = "Argument filters cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (page == null) {
            String msg = "Argument page cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Results.
        List rslts = new ArrayList();
        try {
            // Apply sorts.
            for (int i = 0; i < sorts.length; i++) {
                Comparator sort = ((FColSortMode)sorts[i]).getSort();
                Collections.sort(this.entries, sort); 
            }
            // Page constraints.
            int pgSize = page.getPageSize();
            int pgNum = page.getPageNumber();
            int fstEntry = (pgSize * (pgNum - 1)) + 1;
            int lstEntry = pgSize * pgNum;    // will be 0 if "View All"
            // Apply filters.
            List filterKeys = __getFilterKeys(filters);
            List filteredEntries = __filterByKeys(entries, filterKeys);
            int entryCount = 0;
            // Apply paging.
            for (int i = 0; i < filteredEntries.size(); i++) {
                entryCount++;
                if (entryCount < fstEntry) continue;
                if (lstEntry != 0 && entryCount > lstEntry) continue;
                rslts.add(ec.convertEntry(filteredEntries.get(i)));
            }
            // Set the pageCount.
            int pgCnt = 1;  // Default.
            if (pgSize != 0) {
                pgCnt = (entryCount / pgSize)
                    + ((entryCount % pgSize == 0) ? 0 : 1);
            }
            if (pgCnt == 0) pgCnt++;    // Must always be a page, even if empty.
            page.setPageCount(pgCnt);
        } catch (Exception e) {
            String msg = "FColUserEntryDataSource.fetchData failed.";
            throw new CatalogException(msg, e);
        }

        return rslts;
    }

    private List __getFilterKeys(IFilterMode[] filters) {
        List keys = new ArrayList();
        List filterList = null;
        // Add filter keys (username) to list.
        for (int i = 0; i < filters.length; i++) {
            filterList = ((FColFilterMode)filters[i]).getFilterList();
            for (int j = 0; j < filterList.size(); j++) {
                User user = (User)filterList.get(j);
                String username = user.getUsername();
                keys.add(username);
            }
        }

        return keys;
    }

    private List __filterByKeys(List entries, List keys) {
        List rslts = new ArrayList();
        // Create filtered list from keys list.
        for (int i = 0; i < entries.size(); i++) {
            User user = (User)entries.get(i);
            String username = user.getUsername();
            if (!keys.contains(username)) rslts.add(user);
        }

        return rslts;
    }

} // end FColUserEntryDataSource class
