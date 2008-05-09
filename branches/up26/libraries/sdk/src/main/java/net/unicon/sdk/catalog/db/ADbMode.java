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

/**
 * Provides some common implementation for mode objects in the
 * <code>net.unicon.common.util.catalog.db</code> package.
 */
public abstract class ADbMode {
    private String sql;
    private Object[] params;
    /*
     * PUBLIC API
     */
    /**
     * Provides access to the sql fragment for this mode object.
     *
     * @return the sql fragment for this mode object.
     */
    public String toSql() {
        return sql;
    }
    /**
     * Provides access to the array of parameters for this mode object.
     *
     * @return the array of parameters for this mode object.
     */
    public Object[] getParameters() {
        return params;
    }
    /*
     * PROTECTED API
     */
    protected ADbMode(String sql, Object[] params) {
        // Assertions.
        if (sql == null) {
            String msg = "Argument sql cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (params == null) {
            String msg = "Argument params cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Members.
        this.sql = sql.trim();
        this.params = params;
    }
}
