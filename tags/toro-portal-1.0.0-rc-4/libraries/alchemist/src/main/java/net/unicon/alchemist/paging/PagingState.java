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

package net.unicon.alchemist.paging;

public class PagingState {

    // Instance Members.
    private int itemsPerPage;
    private int currentPage;

    /*
     * Public API.
     */

    public static final int SHOW_ALL_ITEMS = 0;
    public static final int FIRST_PAGE = 1; // duh...;-)
    public static final int LAST_PAGE = 0;

    public PagingState() {

        // Instance Members.
        this.itemsPerPage = 10;
        this.currentPage = 1;

    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int value) {

        // Assertions.
        if (value < 0) {
            String msg = "Argument 'value' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }

        itemsPerPage = value;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int value) {

        // Assertions.
        if (value < LAST_PAGE) {
            String msg = "Argument 'value' cannot be less than zero.";
            throw new IllegalArgumentException(msg);
        }

        currentPage = value;
    }

}