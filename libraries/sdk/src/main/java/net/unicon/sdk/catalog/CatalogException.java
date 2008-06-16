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
 * Thrown when the catalog toolkit encounters a fatal exception.
 */
public class CatalogException extends Exception {
    /**
     * Constructs a new exception.
     */
    public CatalogException() {
        super();
    }
        
    /**
     * Constructs a new exception with the specified message.
     * @param msg insightful feedback concerning this exception.
     */
    public CatalogException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs a new exception with the specified message and cause.
     * @param msg insightful feedback concerning this exception.
     * @param cause the exception that forced this exception.
     */
    public CatalogException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    /**
     * Constructs a new exception with the specified cause.
     * @param cause the exception that forced this exception.
     */
    public CatalogException(Throwable cause) {
        super(cause);
    }    
}
