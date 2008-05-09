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

package net.unicon.academus.api;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.unicon.academus.service.FImportStatusResult;
import net.unicon.academus.service.GroupData;
import net.unicon.academus.service.MemberData;
import net.unicon.academus.service.OfferingData;
import net.unicon.academus.service.RIAcademusImportService;
import net.unicon.academus.service.TopicData;
import net.unicon.academus.service.UserData;
import net.unicon.academus.service.adapters.UCFImportAdapter;
import net.unicon.sdk.api.ApiException;
import net.unicon.sdk.api.IApiController;
import net.unicon.sdk.api.IImportResult;
import net.unicon.sdk.event.EventLogException;
import net.unicon.sdk.event.IEventLogService;
import net.unicon.sdk.event.stub.FStubEvent;
import net.unicon.sdk.event.stub.FStubEventLogService;
import net.unicon.sdk.guid.Guid;
import net.unicon.sdk.guid.GuidException;
import net.unicon.sdk.guid.IGuidService;
import net.unicon.sdk.guid.md5.MD5GuidService;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.log4j.Log4jLogService;
import net.unicon.sdk.tpm.ITransactionLog;
import net.unicon.sdk.tpm.ITransactionPayload;
import net.unicon.sdk.tpm.TransactionLogException;
import net.unicon.sdk.tpm.fs.FSTransactionLog;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;


/**
 * Encapsulates the algorithms for importing and exporting data <i>vis-a-vis</i>
 * Academus.  External systems use a facade (e.g. an implementation of
 * <code>ImportServace</code>) to interect with Academus.  These classes rely on
 * the <code>ApiController</code> to perform the various proceedural steps that
 * happen in an import or export.<p>
 * Instances of <code>ApiController</code> are immutable.  A single instance may
 * be used safely more than once or even concurrently by several threads.
 */
public final class AcademusApiController implements IApiController {

    private static final IImportResult RSLT_SUCCESS = new IImportResult() {
        public boolean isSuccessful() { return true; }
        public String getMessage() { return "Document imported successfully"; }
        public Throwable getCause() { return null; }
    };

    // Available Actions.
    private static final int ACTION_ADD = 1000;
    private static final int ACTION_DELETE = 1001;
    private static final int ACTION_IMPORT = 1002;
    private static final int ACTION_UPDATE = 1003;

    // Instance Members.
    private IGuidService guid;
    private static final ILogService logger = new Log4jLogService();
    private IEventLogService evnt;
    private ITransactionLog trans;
    private RIAcademusImportService imprt;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>AcademusApiController</code> instance.
     */
    public AcademusApiController() throws ApiException {

        /*
         * GUID SERVICE.
         */
        guid = new MD5GuidService();

        /*
         * EVENT SERVICE.
         */
        evnt = FStubEventLogService.getEventService();

        /*
         * TRANSACTION LOG SERVICE.
         */
        try {
            trans = new FSTransactionLog();
        } catch (TransactionLogException tle) {
            String msg = "Unable to create the transaction log.";
            throw new ApiException(msg, tle);
        }

        /*
         * ACADEMUS IMPORT SERVICE.
         */
        String url = null;
        try {
            Properties p = new Properties();
            p.load(AcademusApiController.class.getResourceAsStream("/properties/unicon-service.properties"));
            url = p.getProperty("Academus.ImportService.Url");
            System.setSecurityManager(new RMISecurityManager());
            imprt = (RIAcademusImportService) Naming.lookup(url);
        } catch (IOException fnfe) {
            String msg = "Unable to read from 'unicon-service.properties' "
                                                + "configuration file";
            throw new ApiException(msg, fnfe);
        } catch (NotBoundException nbe) {
            String msg = "Academus import service not bound to:  " + url;
            throw new ApiException(msg, nbe);
        }

    }

    /**
     * Imports the specified XML document into Academus.  The input document
     * must be well-formed XML and adhere to the native Academus XML
     * specification.
     *
     * @param d An XML document that conforms to the native Academus XML
     * specification.
     * @return An object detailing the result(s) of importation.
     * @throws ApiException If the document could not be imported due to
     * improper formatting or server error.
     */
    public IImportResult doImport(org.w3c.dom.Document d)
                                        throws ApiException {

        // Assertions.
        if (d == null) {
            String msg = "Argument 'd [org.w3c.dom.Document]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Create an ID for the transaction.
        Guid id = null;
        try {
            id = guid.generate();
        } catch (GuidException ge) {
            String msg = "Unable to obtain a GUId for the transaction.";
            throw new ApiException(msg, ge);
        }

        // Log the transaction.
        try {
            evnt.logEvent(new FStubEvent("Document Received")); // ToDo:  Fix!
        } catch (EventLogException ele) {
            String msg = "Unable to log a transaction event.";
            throw new ApiException(msg, ele);
        }

        // Convert to dom4j.
        DOMReader dr = new DOMReader();
        Document doc = dr.read(d);

        // Persist the transaction.
        try {
            final org.w3c.dom.Document fd = d;
            ITransactionPayload pld = new ITransactionPayload() {
                public String getContent() throws TransactionLogException {
                    try {
                        java.io.StringWriter sw = new java.io.StringWriter();
                        SAXWriter sax = new SAXWriter(new XMLWriter(sw));
                        sax.write(new org.dom4j.io.DOMReader().read(fd));
                        return sw.toString();
                    } catch (Throwable t) {
                        throw new TransactionLogException("blah", t);
                    }
                }
            };
            trans.persist(id.getValue(), pld);
        } catch (TransactionLogException tle) {
            String msg = "Unable to log the requested transaction.";
            throw new ApiException(msg, tle);
        }

        List resp = new ArrayList();

        // Process the detail(s).
        Iterator it = doc.selectNodes("//batch/detail").iterator();
        while (it.hasNext()) {
            Element dtl = (Element) it.next();
            resp.addAll(Arrays.asList(processDetail(dtl)));
        }

        // Analyse the response(s) from Academus.
        boolean success = true; // At least unltil we learn otherwise...
        StringBuffer warn = new StringBuffer();
        StringBuffer error = new StringBuffer();
        it = resp.iterator();
        StringBuffer err = null;
        while(it.hasNext()) {
            FImportStatusResult s = (FImportStatusResult) it.next();
            switch (s.getStatusCode()) {
                case FImportStatusResult.SUCCESSFUL:
                    // Do nothing...
                    break;
                case FImportStatusResult.SUCCESSFUL_WARNINGS:
                    err = new StringBuffer();
                    err.append("\t* ERROR CODE:  ");
                    if (s.getErrorCode() != null) {
                        err.append(s.getErrorCode().getId());
                    } else {
                        err.append("[Not Provided]");
                    }
                    err.append(" - ");
                    err.append(s.getMessage());
                    err.append(" - Description: ");
                    err.append(s.getErrorCode().getDescription());
                    logger.log(ILogService.WARN, s.getMessage());
                    if (s.getObject() instanceof String) {  // We'll assume this is a stack trace...
                        String logIt = (String) s.getObject();
                        logger.log(ILogService.ERROR, logIt);
                        err.append(" <STACKTRACE>");
                        err.append(logIt);
                        err.append(" </STACKTRACE>");
                    }
                    warn.append(err.toString()).append("\n");
                    break;
                case FImportStatusResult.FAILED:
                    success = false;    // No longer considered a success...
                    err = new StringBuffer();
                    err.append("\t* ERROR CODE:  ");
                    if (s.getErrorCode() != null) {
                        err.append(s.getErrorCode().getId());
                    } else {
                        err.append("[Not Provided]");
                    }
                    err.append(" - ");
                    err.append(s.getMessage());
                    err.append(" - Description: ");
                    err.append(s.getErrorCode().getDescription());
                    logger.log(ILogService.ERROR, s.getMessage());
                    if (s.getObject() instanceof String) {  // We'll assume this is a stack trace...
                        String logIt = (String) s.getObject();
                        logger.log(ILogService.ERROR, logIt);
                        err.append(" <STACKTRACE>");
                        err.append(logIt);
                        err.append(" </STACKTRACE>");
                    }
                    warn.append(err.toString()).append("\n");
                    break;
                case FImportStatusResult.FAILED_WARNINGS:
                    success = false;    // No longer considered a success...
                    err = new StringBuffer();
                    err.append("\t* ERROR CODE:  ");
                    if (s.getErrorCode() != null) {
                        err.append(s.getErrorCode().getId());
                    } else {
                        err.append("[Not Provided]");
                    }
                    err.append(" - ");
                    err.append(s.getMessage());
                    err.append(" - Description: ");
                    err.append(s.getErrorCode().getDescription());
                    logger.log(ILogService.ERROR, s.getMessage());
                    if (s.getObject() instanceof String) {  // We'll assume this is a stack trace...
                        String logIt = (String) s.getObject();
                        logger.log(ILogService.ERROR, logIt);
                        err.append(" <STACKTRACE>");
                        err.append(logIt);
                        err.append(" </STACKTRACE>");
                    }
                    warn.append(err.toString()).append("\n");
                    break;
                case FImportStatusResult.FAILED_EXCEPTION:
                    success = false;    // No longer considered a success...
                    err = new StringBuffer();
                    err.append("\t* ERROR CODE:  ");
                    if (s.getErrorCode() != null) {
                        err.append(s.getErrorCode().getId());
                    } else {
                        err.append("[Not Provided]");
                    }
                    err.append(" - ");
                    err.append(s.getMessage());
                    err.append(" - Description: ");
                    err.append(s.getErrorCode().getDescription());
                    logger.log(ILogService.ERROR, s.getMessage());
                    if (s.getObject() instanceof String) {  // We'll assume this is a stack trace...
                        String logIt = (String) s.getObject();
                        logger.log(ILogService.ERROR, logIt);
                        err.append(" <STACKTRACE>");
                        err.append(logIt);
                        err.append(" </STACKTRACE>");
                    }
                    error.append(err.toString()).append("\n");
                    break;
                default:
                    String msg = "Unrecognized status code for "
                                            + "FImportStatusResult:  "
                                            + s.getStatusCode();
                    throw new ApiException(msg);
            }

        }

        // Prepare the response for IPAQ.
        String ipaq_msg = null;
        if (error.length() > 0) {
            error.insert(0, "Academus reported the following error(s):\n\n");
            error.append("\n");
        }
        if (warn.length() > 0) {
            warn.insert(0, "Academus reported the following warning(s):\n\n");
            warn.append("\n");
        }
        if (error.length() > 0 || warn.length() > 0) {
            StringBuffer newMsg = new StringBuffer();
            newMsg.append(error.toString());
            newMsg.append(warn.toString());
            ipaq_msg = newMsg.toString();
        }
        if (success == true && ipaq_msg == null) {  // Clean success...
            return RSLT_SUCCESS;
        } else {                                    // There were problems...
            return new ImportResultImpl(success, ipaq_msg, null);
        }

    }

    /*
     * Implementation.
     */

    private FImportStatusResult[] processDetail(Element e) throws ApiException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // Evaluate the action.
        int action = 0;
        Element a = (Element) e.selectSingleNode("action/*");
        if (a.getName().equals("add")) {
            action = ACTION_ADD;
        } else if (a.getName().equals("delete")) {
            action = ACTION_DELETE;
        } else if (a.getName().equals("import")) {
            action = ACTION_IMPORT;
        } else if (a.getName().equals("update")) {
            action = ACTION_UPDATE;
        } else {
            String msg = "Unsupported detail action:  " + a.getName();
            throw new ApiException(msg);
        }

        List resp = new ArrayList();

        /*
         * Handle marshalling types individually.
         */

        String xpath = null;

        // Users.
        Iterator usrs = e.selectNodes("payload/person").iterator();
        while (usrs.hasNext()) {
            UserData u = null;
            try {
                u = UCFImportAdapter.parseUser((Element) usrs.next());
            } catch (Throwable t) {
                String msg = "Unable to parse specified UserData.";
                throw new ApiException(msg, t);
            }
            try {
                switch (action) {
                    case ACTION_ADD:
                        resp.add(imprt.importUser(u));
                        break;
                    case ACTION_DELETE:
                        resp.add(imprt.deleteUser(u));
                        //u.getUserName(),
                        //u.getSource()));
                        break;
                    case ACTION_IMPORT:
                        resp.add(imprt.syncUser(u));
                        break;
                    case ACTION_UPDATE:
                        resp.add(imprt.updateUser(u));
                        break;
                }
            } catch (RemoteException re) {
                String msg = "Academus Importer transaction failed.";
                throw new ApiException(msg, re);
            }
        }

        // Topics.
        xpath = "payload/group[child::*[name() = 'topic']]";
        Iterator tpx = e.selectNodes(xpath).iterator();
        while (tpx.hasNext()) {
            TopicData p = null;
            try {
                p = UCFImportAdapter.parseTopic((Element) tpx.next());
            } catch (Throwable t) {
                String msg = "Unable to parse specified TopicData.";
                throw new ApiException(msg, t);
            }
            try {
                switch (action) {
                    case ACTION_ADD:
                        resp.add(imprt.importTopic(p));
                        break;
                    case ACTION_DELETE:
                        resp.add(imprt.deleteTopic(p));
                        break;
                    case ACTION_IMPORT:
                        resp.add(imprt.syncTopic(p));
                        break;
                    case ACTION_UPDATE:
                        resp.add(imprt.updateTopic(p));
                        break;
                }
            } catch (RemoteException re) {
                String msg = "Academus Importer transaction failed.";
                throw new ApiException(msg, re);
            }
        }

        // Offerings.
        xpath = "payload/group[child::*[name() = 'offering']]";
        Iterator offs = e.selectNodes(xpath).iterator();
        while (offs.hasNext()) {
            OfferingData o = null;
            try {
                o = UCFImportAdapter.parseOffering((Element) offs.next());
            } catch (Throwable t) {
                String msg = "Unable to parse specified OfferingData.";
                throw new ApiException(msg, t);
            }
            try {
                switch (action) {
                    case ACTION_ADD:
                        resp.add(imprt.importOffering(o));
                        break;
                    case ACTION_DELETE:
                        resp.add(imprt.inactivateOffering(o));
                        break;
                    case ACTION_IMPORT:
                        resp.add(imprt.syncOffering(o));
                        break;
                    case ACTION_UPDATE:
                        resp.add(imprt.updateOffering(o));
                        break;
                }
            } catch (RemoteException re) {
                String msg = "Academus Importer transaction failed.";
                throw new ApiException(msg, re);
            }
        }

        // Memberships.
        Iterator mbrs = e.selectNodes("payload/member").iterator();
        while (mbrs.hasNext()) {
            MemberData m = null;
            Element n = (Element) mbrs.next();
            try {
                m = UCFImportAdapter.parseMember(n);
            } catch (Throwable t) {
                String msg = "Unable to parse specified MemberData.";
                throw new ApiException(msg, t);
            }
            try {
                switch (action) {
                    case ACTION_ADD:
                        resp.add(imprt.importMembership(m));
                        break;
                    case ACTION_DELETE:
                        resp.add(imprt.deleteMembership(m));
                        break;
                    case ACTION_IMPORT:
                        if (n.selectSingleNode("status").getText().equals("inactive")) {
                            // NB:  This is a drop modification -- we don't
                            // support 'drop' so we need to handle as delete.
                            resp.add(imprt.deleteMembership(m));
                        } else {
                            resp.add(imprt.syncMembership(m));
                        }
                        break;
                    case ACTION_UPDATE:
                        if (n.selectSingleNode("status").getText().equals("inactive")) {
                            // NB:  This is a drop modification -- we don't
                            // support 'drop' so we need to handle as delete.
                            resp.add(imprt.deleteMembership(m));
                        } else {
                            resp.add(imprt.updateMembership(m));
                        }
                        break;
                }
            } catch (RemoteException re) {
                String msg = "Academus Importer transaction failed.";
                throw new ApiException(msg, re);
            }
        }

        // Groups.
        xpath = "payload/group[child::*[name() = 'club' "
                                    + "or name() = 'generic-group' "
                                    + "or name() = 'department' "
                                    + "or name() = 'division']]";
        Iterator grps = e.selectNodes(xpath).iterator();
        while (grps.hasNext()) {
            GroupData g = null;
            try {
                g = UCFImportAdapter.parseGroup((Element) grps.next());
            } catch (Throwable t) {
                String msg = "Unable to parse specified GroupData.";
                throw new ApiException(msg, t);
            }
            try {
                switch (action) {
                    case ACTION_ADD:
                        resp.add(imprt.importGroup(g));
                        break;
                    case ACTION_DELETE:
                        resp.add(imprt.deleteGroup(g));
                        break;
                    case ACTION_IMPORT:
                        resp.add(imprt.syncGroup(g));
                        break;
                    case ACTION_UPDATE:
                        resp.add(imprt.updateGroup(g));
                        break;
                }
            } catch (RemoteException re) {
                String msg = "Academus Importer transaction failed.";
                throw new ApiException(msg, re);
            }
        }

        // Send back the status object(s).
        FImportStatusResult[] rslt = new FImportStatusResult[resp.size()];
        resp.toArray(rslt);
        return rslt;

    }

    private static class ImportResultImpl implements IImportResult {

        // Instance Members.
        private boolean success;
        private String message;
        private Throwable cause;

        /*
         * Public API.
         */

        public ImportResultImpl(boolean success, String message,
                                            Throwable cause) {

            // Assertions.
            if (message == null) {
                String msg = "Argument 'message' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.success = success;
            this.message = message;
            this.cause = cause;

        }

        public boolean isSuccessful() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

    }

}
