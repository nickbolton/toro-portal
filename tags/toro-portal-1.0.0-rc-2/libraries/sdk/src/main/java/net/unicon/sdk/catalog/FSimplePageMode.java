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


public final class FSimplePageMode implements IPageMode {
    private static final int PAGE_COUNT_NOT_SET = -1;
    private int pageSize;
    private int pageNumber;
    private int pageCount;
    /*
     * PUBLIC API
     */
    /**
     * Creates a new <code>FSimplePageMode</code> from the specified
     * <code>pageSize</code> and <code>pageNumber</code>.
     *
     * @param pageSize the number of entries contained on each page.
     * @param pageNumber the page that should be displayed.
     */
    public FSimplePageMode(int pageSize, int pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.pageCount = PAGE_COUNT_NOT_SET;
    }
    /**
     * Tells the catalog toolkit how many entries should appear on each page.
     *
     * @return the number of entries on each page.
     */
    public int getPageSize() {
        return pageSize;
    }
    /**
     * Tells the catalog toolkit which page within the collection should be
     * displayed.
     *
     * @return the number of the page to display.
     */
    public int getPageNumber() {
        return pageNumber;
    }
    /**
     * Gives catalog clients access to the count of available pages given the
     * specified page size.  Call this method only after you have arrived at
     * your final <code>Catalog</code> instance (the one that contains only the
     * results you intend to display).  This value will be calculated and set by
     * the the  <code>Catalog</code> or perhaps the <code>IDataSource</code>
     * implementation as they perform their task(s).
     * <p>
     * A <code>CatalogException</code> generally signifies that this
     * <code>PageMode</code> instance has not yet been used and therefore the
     * number of pages is not yet known.
     *
     * @return the number of pages in the parent collection
     * (<code>Catalog</code>) given the specified page size.
     * @throws net.unicon.common.util.catalog.CatalogException
     * if the value has not been set.
     */
    public int getPageCount() throws CatalogException {
        if (pageCount == -1) {
            String msg = "Page count not yet set.";
            throw new CatalogException(msg);
        }
        return pageCount;
    }
    /**
     * Allows the catalog toolkit to tell catalog clients how many pages are
     * available given the size of the collection and the specified number of
     * entries on each page.  Catalog clients should not call this method;  this
     * method should only be called by toolkit objects such as
     * <code>Catalog</code> or <code>IDataSource</code> implementations.
     * <p>
     * The page count may only be set once in the lifetime of each object.  This
     * method throws <code>CatalogException</code> if the page count has already
     * been set.
     *
     * @param count the number of pages in the collection.
     * @throws net.unicon.common.util.catalog.CatalogException if the
     * page count has already been set.
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
    /**
     * Allows the catalog toolkit to check whether the <code>PageMode</code>
     * object has already been used.  Each <code>PageMode</code> object may be
     * used only once.  It's harmless for catalog clients to call this method,
     * but there should be no reason to do so.
     *
     * @return <code>true</code> if the <code>PageMode</code> has been used,
     * otherwise <code>false</code>.
     */
    public boolean isUsed() {
        if (pageCount == PAGE_COUNT_NOT_SET) return false;
        return true;
    }
}
