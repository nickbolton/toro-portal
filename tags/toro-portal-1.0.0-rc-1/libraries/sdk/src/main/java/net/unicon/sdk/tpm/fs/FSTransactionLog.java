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
package net.unicon.sdk.tpm.fs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.tpm.ArchiveDetail;
import net.unicon.sdk.tpm.ITransactionLog;
import net.unicon.sdk.tpm.ITransactionPayload;
import net.unicon.sdk.tpm.TransactionLogEntry;
import net.unicon.sdk.tpm.TransactionLogException;
import net.unicon.sdk.tpm.payload.TextTransactionPayload;

/**  
 * FSTransactionLog provides the ability to persist and retrieve
 * transaction data to and from the file system.
 */
public class FSTransactionLog implements ITransactionLog {

    private File repository = null; 
    private static final String repositoryBaseDir = UniconPropertiesFactory.
        getManager(CommonPropertiesType.SERVICE).
            getProperty("net.unicon.sdk.tpm.ITransactionLog.repositoryBaseDir");
    
    /** default constructor */
    public FSTransactionLog() throws TransactionLogException {
        try {
            repository = new File(repositoryBaseDir, "");
            repository.mkdirs();
        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);
        }
    } // end constructor

    /** 
     * Persists a copy of the transaction data to the file system and 
     * names the resulting file from the provided transID.
     *
     * @param transID Unique transaction ID.  The toString() is called on 
     * this parameter to retrieve the value.
     * @param transPayload Represents transaction payload to be persisted
     * to the file system for later retrieval.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     * @see <{ITransactionPayload}>
     */
    public void persist(Object transID, ITransactionPayload transPayload) 
    throws TransactionLogException {

        /* ASSERTIONS */
        String transIDString = transID.toString();
        // check transIDString 
        if (!(transIDString != null && transIDString.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "transaction ID must be provided");
        }
        // check transPayload 
        if (transPayload == null) {
            throw new IllegalArgumentException(
                "Transaction payload must be provided");
        }
        
        try {
            String transContent = transPayload.getContent(); 
            // check transContent 
            if (!(transContent != null && transContent.trim().length() > 0)) {
                throw new Exception("Transaction content must be provided");
            }

            // write the transaction content to the file system
            writeFile(transIDString, transContent);

        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);
        }

    } // end persist(transID,transPayload) 

    /** 
     * Retrieves persisted transaction data from the file system.
     *
     * @param transID Unique transaction ID. The toString() method with be 
     * called on this parameter get the value.
     * @return TransactionLogEntry that contains the transaction data and
     * archive details.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     * @see <{TransactionLogEntry}>
     */
    public TransactionLogEntry retrieve(Object transID) 
    throws TransactionLogException {

        /* ASSERTIONS */
        String transIDString = transID.toString();
        // check transIDString 
        if (!(transIDString != null && transIDString.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "transaction ID must be provided");
        }

        try {
            // check if transaction file exists first
            File file = new File(repositoryBaseDir, transIDString);
            if (!file.exists()) {
                StringBuffer errorMsg = new StringBuffer("");
                errorMsg.append("transaction data file \"");
                errorMsg.append(transIDString);
                errorMsg.append("\" does not exist.");
                throw new Exception(errorMsg.toString());
            }

            // get transaction log entry 
            TransactionLogEntry transLogEntry = createLogEntry(file);

            return transLogEntry;
        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);
        }

    } // end retrieve(transID)

    /** 
     * Provides an Array containing archive details for transaction data
     * stored on the file system. 
     *
     * @return ArchiveDetail objects that represent the latest 
     * archive information of all persisted transaction data on the file system.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     * @see <{ArchiveDetail}>
     */
    public ArchiveDetail[] listArchiveDetails() 
    throws TransactionLogException {

        try {
            // get the list of transaction XML files
            File[] files = repository.listFiles();
            ArchiveDetail[] archiveDetails = new ArchiveDetail[files.length];

            // create list of archive details from file list
            for (int i = 0; i < files.length; i++) {
                File file      = files[i];            
                String transID = file.getName();
                long modified  = file.lastModified();
                
                ArchiveDetail archiveDetail = 
                    new ArchiveDetail(transID, modified);
                archiveDetails[i] = archiveDetail;
            }

            return archiveDetails;
        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);
        }

    } // end listArchiveDetails

    /** 
     * Removes persisted transaction data files on the file system
     * that are associated with the given transaction ID.
     *
     * @param transID Unique transaction ID. The toString() method with be 
     * called on this parameter.
     * @throws <code>net.unicon.sdk.tpm.TransactionLogException</code>
     */
    public void remove(Object transID) throws TransactionLogException {

        /* ASSERTIONS */
        String transIDString = transID.toString();
        // check transIDString 
        if (!(transIDString != null && transIDString.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Transaction ID must be provided");
        }

        try {
            // delete the transaction file 
            File file = new File(repositoryBaseDir, transIDString);
            file.delete();
        } catch (Throwable t) {
            throw new TransactionLogException(t.getMessage(), t);
        }
    } // end remove(transID)

    /* Creates a TransactionLogEntry from a persisted transaction data file */
    private TransactionLogEntry createLogEntry(File file)
    throws Exception {
        // create a transaction entry from the file
        String transContent = readFile(file);
        String transID      = file.getName();
        long modified       = file.lastModified(); 

        ArchiveDetail detail = new ArchiveDetail(transID, modified);
        ITransactionPayload payload = 
            new TextTransactionPayload(transContent);
        TransactionLogEntry transLogEntry = 
            new TransactionLogEntry(payload, detail);

        return transLogEntry;
    } // end createLogEntry

    /* Reads in the contents from a persisted transaction data file */
    private String readFile(File file) throws IOException {
        BufferedReader reader = null;
        
        try {
            // read in the transaction file contents
            int numRead;
            char[] b = new char[4096];
            StringBuffer buff = new StringBuffer("");
            reader = new BufferedReader(new FileReader(file));

            while ((numRead = reader.read(b, 0, 4096)) != -1) {
                buff.append(b, 0, numRead);
            }

            return buff.toString();
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {}
            }

        } // end try block

    } // end readFile

    /* Persists transaction data to the file system */
    private void writeFile(String transID, String transContent) 
    throws IOException {
        ByteArrayInputStream stream = null; 
        FileOutputStream fout       = null; 

        try {
            // write out the transaction content file 
            File file = new File(repositoryBaseDir, transID);
            file.delete();

            int numRead = 0;
            byte[] b = new byte[4096];
            fout = new FileOutputStream(file);
            stream = new ByteArrayInputStream(transContent.getBytes());

            while ((numRead = stream.read(b, 0, 4096)) != -1) {
                fout.write(b, 0, numRead);
            }

        } finally {

            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e) {}
            }

        } // end try block

    } // end writeFile

} // end FSTransactionLog class
