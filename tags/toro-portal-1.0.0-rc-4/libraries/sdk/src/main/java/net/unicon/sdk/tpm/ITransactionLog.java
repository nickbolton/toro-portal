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
 * ITransactionLog provides the ability to persist and retrieve
 * transaction data.
 */
public interface ITransactionLog {

    /** 
     * Persists a copy of the content provided in the transaction payload,
     * that is associated with the given transaction ID.
     *
     * @param transID Unique transaction ID. The toString() method with be 
     * called on this parameter.
     * @param transPayload Represents the transaction content to be persisted.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     */
    public void persist(Object transID, ITransactionPayload transPayload) 
    throws TransactionLogException;

    /** 
     * Retrieves persisted transaction data.
     *
     * @param transID Unique transaction ID. The toString() method with be 
     * called on this parameter get the value.
     * @return TransactionLogEntry that contains the transaction data and
     * archive details.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     * @see <{TransactionLogEntry}>
     */
    public TransactionLogEntry retrieve(Object transID) 
    throws TransactionLogException;

    /** 
     * Provides an Array containing transaction archive details. 
     *
     * @return ArchiveDetail objects that represent the latest 
     * archive information of all persisted transaction data.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     * @see <{ArchiveDetail}>
     */
    public ArchiveDetail[] listArchiveDetails() 
    throws TransactionLogException;

    /** 
     * Removes persisted transaction data that associated with
     * the given transaction ID.
     *
     * @param transID Unique transaction ID. The toString() method with be 
     * called on this parameter.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     */
    public void remove(Object transID) throws TransactionLogException;

} // end ITransactionLog 
