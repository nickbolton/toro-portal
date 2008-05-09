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
package net.unicon.sdk.tpm;

/** 
 * ArchiveDetail provides archive details pertaining to a transaction 
 *
 * details include a unique ID associated with a persisted transaction, 
 * which can be used for later retrieval of the transaction content.
 */
public class ArchiveDetail {
    protected String transID;
    protected long modified;

    /** constructor */
    public ArchiveDetail(String transID, long modified) { 

        /* ASSERTIONS */
        // check transID 
        if (!(transID != null && transID.trim().length() > 0)) {
            throw new IllegalArgumentException (
                "transaction ID must be provided");
        }
        // check modified date
        if (modified <= 0) {
            throw new IllegalArgumentException (
                "archive modification date must be provided");
        }

        this.transID  = transID;
        this.modified = modified;
        
    } // end ArchiveDetail(transID,modified) constructor

    /** 
     * Provides access to the ID of the associated transaction. 
     *
     * @return Unique ID of the associated transaction as a string.
     */
    public String getID() {
        return transID;
    }

    /** 
     * Provides access to the date, in milliseconds, of the associated 
     * transaction data. 
     *
     * @return Last modified date of transaction data in miliseconds.
     */
    public long lastModified() {
        return modified;
    }

} // end ArchiveDetail class
