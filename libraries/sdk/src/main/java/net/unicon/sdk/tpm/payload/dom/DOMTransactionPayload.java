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
package net.unicon.sdk.tpm.payload.dom;

import java.io.BufferedWriter;
import java.io.StringWriter;

import net.unicon.sdk.tpm.ITransactionPayload;
import net.unicon.sdk.tpm.TransactionLogException;

import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;

/** 
 * DOMTransactionPayload contains a DOM associated with transaction data. 
 *
 * Content may or may not have been persisted yet. 
 */
public class DOMTransactionPayload implements ITransactionPayload {
    protected Document xmlDOM = null;

    /** constructor */
    public DOMTransactionPayload(Document xmlDOM) { 

        /* ASSERTIONS */
        // check xmlDOM 
        if (xmlDOM == null) {
            throw new IllegalArgumentException (
                "Transaction Document must be provided");
        }

        this.xmlDOM = xmlDOM;

    } // end constructor

    /** 
     * Provides access to the transaction XML content. 
     * 
     * @return Transaction data as a string. 
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     */
    public String getContent() throws TransactionLogException {
        StringWriter sout         = null;
        BufferedWriter buffWriter = null;
        XMLWriter xmlWriter       = null;
        SAXWriter saxWriter       = null;
        DOMReader domReader       = null; 

        try {
            // convert xmlDOM to a string
            sout = new StringWriter();
            buffWriter = new BufferedWriter(sout);

            xmlWriter = new XMLWriter(buffWriter); 
            saxWriter = new SAXWriter(xmlWriter);

            domReader = new DOMReader();
            saxWriter.write(domReader.read(xmlDOM));

            xmlWriter.flush();

            return sout.toString(); 
        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);

        } finally {

            if (buffWriter != null) {
                try {
                    buffWriter.close();
                } catch (Exception e) {}
            }

        } // end try block

    } // end getContent 

} // end DOMTransactionPayload class
