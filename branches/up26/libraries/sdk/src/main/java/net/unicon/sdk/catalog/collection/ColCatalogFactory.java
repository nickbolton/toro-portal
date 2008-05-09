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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;

public final class ColCatalogFactory {

    /*
     * Public API.
     */

    public ColCatalogFactory() {}

    public IDataSource createDataSource(Object[] elements) {
        return new DataSourceImpl(elements);
    }

    public IPageMode createPageMode(int pgSize, int pgNumber) {
        return new PageModeImpl(pgSize, pgNumber);
    }

    /*
     * Nested Types.
     */

    public interface IColSortMode extends ISortMode {

        Object[] sort(Object[] entries);

    }

    public interface IColFilterMode extends IFilterMode {

        Object[] filter(Object[] entries);

    }

    private static final class DataSourceImpl implements IDataSource {

        // Instance Members.
        private final Object[] elements;

        /*
         * Public API.
         */

        public DataSourceImpl(Object[] elements) {

            // Assertions.
            if (elements == null) {
                String msg = "Argument 'elements' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.elements = new Object[elements.length];
            System.arraycopy(elements, 0, this.elements, 0, elements.length);

        }

        public List fetchData(ISortMode[] sorts, IFilterMode[] filters, IPageMode page) throws CatalogException {

            Iterator it = null;

            // Assertions.
            if (sorts == null) {
                String msg = "Argument 'sorts' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (sorts.length > 1) {
                String msg = "Argument 'sorts' cannot contain more than one element.";
                throw new IllegalArgumentException(msg);
            }
            it = Arrays.asList(sorts).iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o == null) {
                    String msg = "Argument 'sorts' cannot contain null elements.";
                    throw new IllegalArgumentException(msg);
                }
                if (!(o instanceof IColSortMode)) {
                    String msg = "Argument 'sorts' must contain only IColSortMode instances.";
                    throw new IllegalArgumentException(msg);
                }
            }
            if (filters == null) {
                String msg = "Argument 'filters' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            it = Arrays.asList(filters).iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o == null) {
                    String msg = "Argument 'filters' cannot contain null elements.";
                    throw new IllegalArgumentException(msg);
                }
                if (!(o instanceof IColFilterMode)) {
                    String msg = "Argument 'filters' must contain only IColFilterMode instances.";
                    throw new IllegalArgumentException(msg);
                }
            }
            if (page == null) {
                String msg = "Argument 'page' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!(page instanceof PageModeImpl)) {
                String msg = "Argument 'page' must be an PageModeImpl instance.";
                throw new IllegalArgumentException(msg);
            }

            // STEP ONE:  Filter.
            Object[] include = elements;
            it = Arrays.asList(filters).iterator();
            while (it.hasNext()) {
                IColFilterMode f = (IColFilterMode) it.next();
                include = f.filter(include);
            }

            // STEP TWO:  Sort.
            Object[] sorted = null;
            if (sorts.length == 1) {
                IColSortMode sm = (IColSortMode) sorts[0];
                sorted = sm.sort(include);
            } else {
                sorted = include;
            }

            // STEP THREE:  Page.
            int maxPage = 0;
            if (page.getPageSize() == 0) {
                // view all...
                maxPage = (sorted.length / page.getPageSize()) + (sorted.length % page.getPageSize() != 0 ? 1 : 0);
            } else {
                maxPage = 1;
            }
            int selPage = page.getPageNumber() <= maxPage ? page.getPageNumber() : maxPage;
            List rslt = new ArrayList();
            int firstIndex = (page.getPageSize() * (selPage - 1));
            for (int i=firstIndex; i < firstIndex + page.getPageSize(); i++) {
                // get out if we've reached the end of the buffer...
                if (i == sorted.length) {
                    break;
                }
                rslt.add(sorted[i]);
            }

            // Set the pg number and return...
            ((PageModeImpl) page).setPageNumber(selPage);
            return rslt;

        }

    }

    private static final class PageModeImpl implements IPageMode {

        private static final int PAGE_COUNT_NOT_SET = -1;

        // Instance Members.
        private int pgSize;
        private int pgNumber;
        private int pgCount;

        /*
         * Public API.
         */

        public PageModeImpl(int pgSize, int pgNumber) {

            // Instance Members.
            this.pgSize = pgSize;
            this.pgNumber = pgNumber;
            this.pgCount = PAGE_COUNT_NOT_SET;

        }

        public int getPageSize() {
            return pgSize;
        }

        public int getPageNumber() {
            return pgNumber;
        }

        public void setPageNumber(int number) {
            pgNumber = number;
        }

        public int getPageCount() throws CatalogException {
            if (pgCount == PAGE_COUNT_NOT_SET) {
                String msg = "Page count has not been established.";
                throw new IllegalStateException(msg);
            }
            return pgCount;
        }

        public void setPageCount(int count) throws CatalogException {
            if (pgCount != PAGE_COUNT_NOT_SET) {
                String msg = "Page count has already been established.";
                throw new IllegalStateException(msg);
            }
            pgCount = count;
        }

        public boolean isUsed() {
            return pgCount != PAGE_COUNT_NOT_SET ? true : false;
        }

    }

}
