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
package net.unicon.sdk.catalog.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.unicon.sdk.catalog.CatalogException;

/**
 * Converts rows from a <code>ResultSet</code> to catalog entries.  Note that
 * the runtime type for catalog entries is not defined;  catalog clients are
 * free to implement their entries in any fashion.
 */
public interface IDbEntryConvertor {
    /**
     * Converts a individual row from a <code>ResultSet</code> to a catalog
     * entry.  The <code>IDbEntryConvertor</code> will convert the row at the
     * current cursor position.
     *
     * @param rs a <code>ResultSet</code> that has its cursor set to the desired
     * row.
     * @return a catalog entry based on the choosen row.
     * @throws java.sql.SQLException if for any reason the
     * <code>ResultSet</code> throws one.
     * @throws net.unicon.common.util.catalog.CatalogException if the
     * <code>IDbEntryConvertor</code> can't convert the row.
     */
    public Object convertRow(ResultSet rs) throws SQLException,
                                                CatalogException;
}
