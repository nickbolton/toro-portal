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

/**
 * This class provides some general utility functions for the catalog-based
 * usability channel pattern.
 */
public class UsabilityUtils {
    /**
     * Default number of entries to show on each page.
     */
    public static final int DEFAULT_PAGE_SIZE    = 10;
    /**
     * Default page number to display.
     */
    public static final int DEFAULT_PAGE_NUM     = 1;
    /**
     *Default search mode (no search).
     */
    public static final int DEFAULT_AND_OR_VALUE = 0;
    /**
     * Search for a match on any field.
     */
    public static final int SEARCH_OR            = 1;
    /**
     * Search for a match on all fields.
     */
    public static final int SEARCH_AND           = 2;
    /**
     * Show all entries on one page.
     */
    public static final String ALL_RESULTS_LABEL = "All";
    /**
     * Chooses a page size (number of entries per page) given the specified
     * <code>String</code>.  This <code>String</code> generally comes from the
     * request parameters.  The page size will evaluate to one of the following
     * three things:
     * <ul>
     *   <li>0 if the input equals the <code>ALL_RESULTS_LABEL</code></li>
     *   <li>The specified number if the input can be parsed to an integer</li>
     *   <li>The <code>DEFAULT_PAGE_SIZE</code> if neither of the above</li>
     * </ul>
     *
     * @param strPageSize ideally either an integer or
     * <code>ALL_RESULTS_LABEL</code>.
     * @return the number of entries to be included on each page.
     */
    public static int evaluatePageSize(String strPageSize) {
        int pgSize = DEFAULT_PAGE_SIZE; // Default.
        // Try to evaluate the page size from the input.
        if (strPageSize != null && !strPageSize.trim().equals("")) {
            if (strPageSize.equalsIgnoreCase(ALL_RESULTS_LABEL)) {
                pgSize = 0;
            } else {
                try {
                    pgSize = Integer.parseInt(strPageSize);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();  // pgSize remains at default
                }
            }
        }
        return pgSize;  // Return.
    }
    /**
     * Chooses a page number given the specified <code>String</code>.  This
     * <code>String</code> generally comes from the request parameters.  The
     * page number will evaluate to either the specified integer (if the input
     * could be parsed), or the <code>DEFAULT_PAGE_NUM</code> (if not).
     *
     * @param strPageNum ideally an integer specifying a page within a
     * <code>Catalog</code>.
     * @return the page number to display.
     */
    public static int evaluateCurrentPage(String strPageNum) {
        int pgNum = DEFAULT_PAGE_NUM;      // Default.
        if (strPageNum != null && !strPageNum.equals("")) {
            try {
                pgNum = Integer.parseInt(strPageNum);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();  // pgNum remains at default
            }
        }
        return pgNum;
    }
    /**
     * Chooses a search mode given the specified <code>String</code>.  This
     * <code>String</code> generally comes from the request parameters.  The
     * search mode will evaluate to one of the following three things:
     * <ul>
     *   <li><code>SEARCH_OR</code> if the input parses to that value</li>
     *   <li><code>SEARCH_AND</code> if the input parses to that value</li>
     *   <li><code>DEFAULT_AND_OR_VALUE</code> if neither of the above</li>
     * </ul>
     *
     * @param andOrValue ideally an integer specifying a search mode
     * @return the appropriate search mode
     */
    public static int evaluateSearchAndOr(String andOrValue) {
        int rslt = DEFAULT_AND_OR_VALUE;  // Default -- means no search.
        // Get out if there's no fName or lName.
        try {
            rslt = Integer.parseInt(andOrValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();    // rslt stays at default
        }
        return rslt;
    }
}
