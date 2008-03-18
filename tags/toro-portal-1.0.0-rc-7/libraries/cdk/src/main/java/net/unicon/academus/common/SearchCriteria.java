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

package net.unicon.academus.common;

/**
 * <p>
 * 
 * </p>
 */
public class SearchCriteria {
    private String name;
    private String description;
    private String id;
    private String keyword;
    
    private int currentPage;
    private int resultsPerPage;

    boolean matchAllCriteria;

    public SearchCriteria () {
        name        = null;
        description = null;
        id          = null;
        keyword     = null;
    }
    
    /**
     * <p>
     * Set the name to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setName(String searchName) {
        this.name = searchName;
    }

    /**
     * <p>
     * Returns the name to search on.
     * </p><p>
     * 
     * @return a String with the name to search on.
     * </p>
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * Set the description to search for.
     * </p><p>
     * 
     * @param a String with the description to search for.
     * </p>
     */
    public void setDescription(String searchDesc) {
        this.description = searchDesc;
    }

    /**
     * <p>
     * Returns the description to search on.
     * </p><p>
     * 
     * @return a String with the description to search on.
     * </p>
     */
    public String getDescription() {
        return this.description;
    }

    
    /**
     * <p>
     * Set the keyword to search for.
     * </p><p>
     * 
     * @param a String with the name to search for.
     * </p>
     */
    public void setKeyword(String searchkeyword) {
        this.keyword = searchkeyword;
    }

    /**
     * <p>
     * Returns the keyword to search on.
     * </p><p>
     * 
     * @return a String with the keyword to search on.
     * </p>
     */
    public String getKeyword() {
        return this.keyword;
    }

    /**
     * <p>
     * Set the description to search for.
     * </p><p>
     * 
     * @param a String with the description to search for.
     * </p>
     */
    public void setId(String searchId) {
        this.id = searchId;
    }

    /**
     * <p>
     * Returns the id to search on.
     * </p><p>
     * 
     * @return a String with the id to search on.
     * </p>
     */
    public String getId() {
        return this.id;
    }

    /**
     * <p>
     * Returns a true of false if the search criteria needs
     * to match all values set, or any value that is set.
     * </p><p>
     * 
     * @return a boolean true or false..
     * </p>
     */
    public void matchAllCriteria(boolean searchValue) {
        this.matchAllCriteria = searchValue;
    }

    /**
     * <p>
     * Set a true of false if the search criteria needs
     * to match all values set, or any value that is set.
     * </p><p>
     * 
     * @param a boolean true or false..
     * </p>
     */
    public boolean matchAllCriteria() {
        return this.matchAllCriteria;
    }

    /**
     * <p>
     * Get the current page view of the search results.
     * </p><p>
     * 
     * @param a boolean true or false..
     * </p>
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * <p>
     * Get number of results to view for the criteria.
     * </p><p>
     * 
     * @param a boolean true or false..
     * </p>
     */
    public int getResultsPerPage() {
        return resultsPerPage;
    }
}
