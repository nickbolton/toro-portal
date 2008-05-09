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
package net.unicon.portal.channels.gradebook.base;

import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IPageMode;

public final class GbPageMode implements IPageMode {

    private static final int PAGE_COUNT_NOT_SET = -1;
    private int pageSize;
    private int pageNumber;
    private int pageCount;

    public GbPageMode(int pageSize, int pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.pageCount = PAGE_COUNT_NOT_SET;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageCount() throws CatalogException {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public boolean isUsed() {
        if (pageCount == PAGE_COUNT_NOT_SET) return false;
        return true;
    }

}

