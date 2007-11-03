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

import net.unicon.sdk.catalog.IFilterMode;

/**
 * Allows catalog clients using a <code>FDbDataSource</code> to exclude some
 * entries from the collection.
 */
public final class FDbFilterMode extends ADbMode implements IFilterMode {
    /*
     * PUBLIC API
     */
    /**
     * Creates a new <code>FDbFilterMode</code> from the specified sql fragment
     * and parameters.  The number of parameters in the sql (?'s to be
     * substituted by the <code>PreparedStatement</code>) should be eqal to the
     * number of objects in the parameters array.  Also the objects in the
     * parameters array should be organized in the same order in which they
     * appear in the sql.
     *
     * @param sql a fragment of sql designed to exclude some results from a
     * 'SELECT' query.
     * @param params an array of objects to be used as sql parameters.
     */
    public FDbFilterMode(String sql, Object[] params) {
        super(sql, params);
    }
}
