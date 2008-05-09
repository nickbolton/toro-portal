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

import java.io.StringReader;

import net.unicon.sdk.tpm.payload.dom.DOMTransactionPayload;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TransactionLogDriver {

    public TransactionLogDriver() {}

    public static void main(String[] args) {

        try {
            ITransactionLog tpm = 
                TransactionLogFactory.getService();

            net.unicon.sdk.guid.IGuidService guidService = 
                net.unicon.sdk.guid.GuidServiceFactory.getService();

            String guid = guidService.generate().getValue();

            StringBuffer xml = new StringBuffer("");
            xml.append("<?xml version=\"1.0\"?>\n");
            xml.append("<transaction>\n");
            xml.append("    <guid>").append(guid).append("</guid>\n");
            xml.append("</transaction>\n");

            System.out.println("\nStart TPM");

            /* TEST EXPECTED CASES */
            // persit it
            System.out.println("\t::persist()");

            DOMParser parser = new DOMParser();
            StringReader reader = new StringReader(xml.toString());
            InputSource is = new InputSource(reader);
            parser.parse(is);
            Document doc = parser.getDocument();

            ITransactionPayload payload = 
                new DOMTransactionPayload(doc);

            tpm.persist(guid,payload);

            // retrieve it
            TransactionLogEntry entry = tpm.retrieve(guid);

            System.out.println("\t::retrieve()");
            System.out.println(entry.getPayload().getContent());

            // list archive details
            System.out.println("\t::listArchiveDetails()");

            ArchiveDetail[] details = tpm.listArchiveDetails();

            for (int i = 0; i < details.length; i++) {
                System.out.println("\t\tArchiveDetails["+i+"]:");
                ArchiveDetail detail = details[i];
                System.out.println("\t\t\t--> transID: "+detail.getID());
                System.out.println(
                    "\t\t\t--> modified: "+detail.lastModified());
            }

            // remove it
            //System.out.println("\t::remove()");
            //tpm.remove(guid);

            System.out.println("End TMP\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end main

} // end TransactionLogDriver class
