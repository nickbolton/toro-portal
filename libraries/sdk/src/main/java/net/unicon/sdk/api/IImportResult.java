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

package net.unicon.sdk.api;

/**
 * Defines the contract through which an API controller reports on the result(s)
 * of an import operation.
 */
public interface IImportResult {

    /**
     * Indicaes whether the import completed successfully.  When this method
     * returns <code>false</code>, look to <code>getMessage</code> and
     * <code>getCause</code> for useful information concerning what went wrong.
     *
     * @return <code>true</code> if the import was a success.
     */
    boolean isSuccessful();

    /**
     * Provides a textual description of the result(s) of the import.
     *
     * @return A description of the result(s).
     */
    String getMessage();

    /**
     * Provides a reference to the error that caused import failure, where
     * applicable.  This method returns <code>null</code> if the import was a
     * success.  In some circumstances, an import operation may fail without a
     * <code>Throwable</code> to identify the cause.
     *
     * @return The excepotion that caused import failure, or <code>null</code>.
     */
    Throwable getCause();

}
