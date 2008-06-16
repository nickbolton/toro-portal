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
package net.unicon.sdk.tpm.payload;

import net.unicon.sdk.tpm.ITransactionPayload;
import net.unicon.sdk.tpm.TransactionLogException;

/** 
 * TextTransactionPayload contains the content associated with a transaction. 
 *
 * Content may or may not have been persisted yet. 
 */
public class TextTransactionPayload implements ITransactionPayload {
    protected String content = null;

    /** constructor */
    public TextTransactionPayload(String content) { 

        /* ASSERTIONS */
        // check content 
        if (!(content != null && content.length() > 0)) {
            throw new IllegalArgumentException (
                "Transaction content must be provided");
        }

        this.content = content;

    } // end constructor

    /** 
     * Provides access to the transaction XML content. 
     * 
     * @return Transaction data as a string. 
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     */
    public String getContent() throws TransactionLogException {
        return content;
    } // end getContent 

} // end TextTransactionPayload class
