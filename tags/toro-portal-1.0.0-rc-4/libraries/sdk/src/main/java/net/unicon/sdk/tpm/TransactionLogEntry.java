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
 * TransactionLogEntry provides data pertaining to a transaction,
 * including the transaction payload and the associated archive details.
 */
public class TransactionLogEntry {
    protected ITransactionPayload payload = null;
    protected ArchiveDetail archiveDetail = null;

    /** constructor */
    public TransactionLogEntry(
                            ITransactionPayload payload, 
                            ArchiveDetail archiveDetail) {

        /* ASSERTIONS */
        // check payload
        if (payload == null) {
            throw new IllegalArgumentException (
                "Transaction Payload must be provided");
        }
        // check archiveDetail 
        if (archiveDetail == null) {
            throw new IllegalArgumentException (
                "Archive Detail must be provided");
        }

        this.payload = payload;
        this.archiveDetail = archiveDetail;

    } // end constructor

    /** 
     * Provides access to the payload or content of the associated transaction. 
     *
     * @return Payload of the transaction.
     * @see <{ITransactionPayload}>
     */
    public ITransactionPayload getPayload() {
        return payload;
    }

    /** 
     * Provides access to the archive details of the associated transaction. 
     *
     * @return Archive details of the transaction.
     * @see <{ArchiveDetail}>
     */
    public ArchiveDetail getArchiveDetail() {
        return archiveDetail;
    }

} // end TransactionLogEntry class
