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

package net.unicon.sdk.task;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

/**
 * Encapsulates the algorithm for processing within the UNICON Workflow Toolkit.
 * Clients of the UNICON Workflow Toolkit interact with the workflow subsystem
 * via this class.<p>
 * <code>WorkflowController</code> instances receive their instructions in the
 * form of an XML specification document.  This document defines one or more
 * task factories (i.e. <code>ITaskFactory</code> implementations) and one or
 * more workflow plans.<p>
 * The allowable format of this XML document is as follows:
 *
 * <blockquote><code>
 * &lt;!ELEMENT workflow (factory*, plan*)&gt;<br>
 * <br>
 * &lt;!ELEMENT factory ANY&gt;<br>
 * &lt;!ATTLIST factory handle CDATA #REQUIRED<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;impl CDATA #REQUIRED&gt;<br>
 * <br>
 * &lt;!ELEMENT plan (when, do)&gt;<br>
 * &lt;!ATTLIST plan name CDATA #REQUIRED&gt;<br>
 * <br>
 * &lt;!ELEMENT when (filter*)&gt;<br>
 * <br>
 * &lt;!ELEMENT filter (#PCDATA)&gt;<br>
 * <br>
 * &lt;!ELEMENT do (task*)&gt;<br>
 * <br>
 * &lt;!ELEMENT task ANY&gt;<br>
 * &lt;!ATTLIST task name CDATA #REQUIRED<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;factory CDATA #REQUIRED&gt;<br>
 * </code></blockquote>
 *
 * Each <i>&lt;factory&gt;</i> element defines a task factory to be leveraged by
 * workflow plans.  The <i>&lt;impl&gt;</i> attribute must specify a
 * fully-qualified <code>ITaskFactory</code> implementation.  The allowable
 * content model of the <i>&lt;factory&gt;</i> element is implementation
 * specific.<p>
 * The inner text of each <i>&lt;filter&gt;</i> element must be an XPath
 * expression.  The <i>&lt;factory&gt;</i> attribute of each <i>&lt;task&gt;</i>
 * element must match the handle of an <code>ITaskFactory</code> instance
 * defined in this document.  The allowable content model for the
 * <i>&lt;task&gt;</i> element depends on the <code>ITaskFactory</code>
 * implementation.<p>
 * The UNICON Workflow Toolkit can be extended through the creation of new
 * <code>ITaskFactory</code> implementations.
 */
public final class WorkflowController {

    // Instance Members.
    private Map factories;
    private List plans;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>WorkflowController</code> instance that supports
     * workflow plans defined in an XML document at the specified path.  The
     * allowable XML format is defined in the documentation for this class.
     *
     * @param path The location of an XML-based workflow plan definition file.
     * @throws WorkflowException If the controller isn't able to bootstrap
     * itself from the specified parameters.
     */
    public WorkflowController(String path) throws WorkflowException {

        // Assertions.
        if (path == null) {
            String msg = "Argument 'path' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        File f = new File(path);
        if (!f.exists()) {
            String msg = "The specified file does not exist.";
            throw new IllegalArgumentException(msg);
        }

        // ...
        SAXReader sax = new SAXReader();
        Document doc = null;
        try {
            doc = sax.read(f);
        } catch (Throwable t) {
            String msg = "Unable to read the XML document at the specified "
                                                    + "path:  " + path;
            throw new WorkflowException(msg, t);
        }

        // Iterator.
        Iterator it = null;

        // Factories.
        this.factories = new HashMap();
        it = doc.selectNodes("/workflow/factory").iterator();
        while (it.hasNext()) {
            Element fac = (Element) it.next();
            String s = fac.attributeValue("impl");
            String handle = fac.attributeValue("handle");
            try {
                Class impl = Class.forName(s);
                Class[]  args = new Class[] { org.dom4j.Element.class };
                Constructor c = impl.getConstructor(args);
                factories.put(handle, c.newInstance(new Object[] { fac }));
            } catch (Throwable t) {
                String msg = "Unable to create the following task factory:  "
                                                            + handle;
                throw new WorkflowException(msg, t);
            }
        }

        // Plans.
        plans = new ArrayList();
        it = doc.selectNodes("/workflow/plan").iterator();
        while (it.hasNext()) {
            Element pln = (Element) it.next();
            plans.add(new ImportPlan(pln));
        }

    }

    /**
     * Creates a new <code>ITransaction</code> instance and returns it.
     *
     * @return A new transaction.
     */
    public ITransaction createTransaction() {
        return new Transaction();
    }

    /**
     * Executes the specified workflow operation.  This overload of
     * <code>execute</code> manages a workflow transaction around the specified
     * operation.  In other words, it will create a transaction, execute the
     * operation, and either commit or rollback the transaction depending on the
     * success of the operation.
     *
     * @param p A workflow payload.
     * @return An object encapsulating the result(s) of the workflow operation.
     * @throws WorkflowException If the controller was unable to process the
     * specified payload.
     */
    public IWorkflowResult execute(IPayload p) throws WorkflowException {

        // Assertions.
        if (p == null) {
            String msg = "Argument 'p [IPayload]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Create a transaction.
        ITransaction trans = createTransaction();

        // Pass controll to the other overload.
        IWorkflowResult rslt = this.execute(p, trans);

        // Commit the transaction if execution reaches this point -- it
        // will have been rolled back already if the transaction failed.
        trans.commit();

        return rslt;

    }

    /**
     * Executes the specified workflow operation within the context of the
     * specified workflow transaction.
     *
     * @param p A workflow payload.
     * @param t A workflow transaction.
     * @return An object encapsulating the result(s) of the workflow operation.
     * @throws WorkflowException If the controller was unable to process the
     * specified payload.
     */
    public IWorkflowResult execute(IPayload p, ITransaction t)
                                throws WorkflowException {

        // Assertions.
        if (p == null) {
            String msg = "Argument 'p [IPayload]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (t == null) {
            String msg = "Argument 't [ITransaction]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Downcast the transaction.
        Transaction trans = (Transaction) t;
        if (trans.isFinished()) {
            String msg = "The specified transaction has already been commited "
                                                        + "or rolled back.";
            throw new IllegalArgumentException(msg);
        }

        // Select the proper plan.
        ImportPlan target = null;
        Iterator it = plans.iterator();
        while (it.hasNext()) {
            ImportPlan pln = (ImportPlan) it.next();
            if (pln.appliesTo(p.toXml())) {
                target = pln;
                break;
            }
        }

        // Make sure we have a plan.
        if (target == null) {
            String msg = "No plan exists for the specified payload.";
            throw new WorkflowException(msg);
        }

        // Follow the plan.
        return target.execute(p, trans);

    }

    /*
     * Implementation.
     */

    private final class Transaction implements ITransaction {

        private Map contexts;
        private boolean finished;

        /*
         * Public API.
         */

        public Transaction() {
            this.contexts = new HashMap();
            this.finished = false;
        }

        public boolean isFinished() {
            return finished;
        }

        public boolean contains(ITaskFactory f) {

            // Assertions.
            if (finished) {
                String msg = "This transaction has already been commited or "
                                                        + "rolled back.";
                throw new IllegalStateException(msg);
            }
            if (f == null) {
                String msg = "Argument 'f [ITaskFactory]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            return contexts.containsKey(f);

        }

        public void put(ITaskFactory f, ITransactionContext ctx) {

            // Assertions.
            if (finished) {
                String msg = "This transaction has already been commited or "
                                                        + "rolled back.";
                throw new IllegalStateException(msg);
            }
            if (f == null) {
                String msg = "Argument 'f [ITaskFactory]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (ctx == null) {
                String msg = "Argument 'ctx' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (contexts.containsKey(f)) {
                String msg = "This transaction already contains a transaction "
                                    + "context for the specified factory.";
                throw new IllegalArgumentException(msg);
            }

            contexts.put(f, ctx);

        }

        public ITransactionContext get(ITaskFactory f)
                        throws WorkflowException {

            // Assertions.
            if (finished) {
                String msg = "This transaction has already been commited or "
                                                        + "rolled back.";
                throw new IllegalStateException(msg);
            }
            if (f == null) {
                String msg = "Argument 'f [ITaskFactory]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            ITransactionContext rslt = (ITransactionContext) contexts.get(f);

            if (rslt == null) {
                String msg = "No transaction context has been created for the "
                                                    + "specified factory.";
                throw new WorkflowException(msg);
            }

            return rslt;

        }

        public void commit() throws WorkflowException {

            // NB:  Subsequent calls to commit() do nothing...
            if (finished) return;

            try {

                // Commit each context.
                Iterator it = contexts.values().iterator();
                while (it.hasNext()) {
                    ITransactionContext ctx = (ITransactionContext) it.next();
                    ctx.commit();
                }

                // We're done.
                finished = true;

            } catch (Throwable t) {
                String msg = "Import Transaction failed to commit.";
                throw new WorkflowException(msg, t);
            } finally {
                if (!finished) rollback();
            }

        }

        public void rollback() throws WorkflowException {

            // Assertions.
            if (finished) {
                String msg = "This transaction has already been commited or "
                                                        + "rolled back.";
                throw new IllegalStateException(msg);
            }

            List errors = new ArrayList();

            // Rollback each context.
            Iterator it = contexts.values().iterator();
            while (it.hasNext()) {
                ITransactionContext ctx = (ITransactionContext) it.next();
                try {
                    ctx.rollback();
                } catch (Throwable t) {
                    errors.add(t);
                }
            }

            finished = true;

            if (errors.size() != 0) {
                String msg = "Import transaction encountered one or more "
                                    + "errors attempting to rollback:  ";
                Iterator r = errors.iterator();
                while (r.hasNext()) {
                    Throwable t = (Throwable) r.next();
                    msg += "\n\t\t" + t.getClass().getName() + ":  "
                                                    + t.getMessage();
                }
                msg += "\n";
                throw new WorkflowException(msg);
            }

        }

    }

    private final class ImportPlan {

        private final String name;
        private List filters;
        private List tasks;

        /*
         * Public API.
         */

        public ImportPlan(Element e) throws WorkflowException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Iterator.
            Iterator it = null;

            // name.
            this.name = e.attributeValue("name");

            // Filters.
            filters = new ArrayList();
            it = e.selectNodes("when/filter").iterator();
            while (it.hasNext()) {
                Element fltr = (Element) it.next();
                filters.add(fltr.getStringValue());
            }

            // Tasks.
            tasks = new ArrayList();
            it = e.selectNodes("do/task").iterator();
            while (it.hasNext()) {
                Element tsk = (Element) it.next();
                String handle = tsk.attributeValue("factory");
                ITaskFactory f = (ITaskFactory) factories.get(handle);
                tasks.add(f.parseImportTask(tsk));
            }

        }

        public boolean appliesTo(Node n) {

            // Assertions.
            if (n == null) {
                String msg = "Argument 'n [Node]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            boolean rslt = true;    // default.

            // Test against the filters.
            Iterator it = filters.iterator();
            while (it.hasNext()) {
                String fltr = (String) it.next();
                XPath p = new DefaultXPath(fltr);
                if (!p.matches(n)) {
                    rslt = false;
                    break;
                }
            }

            return rslt;

        }

        public IWorkflowResult execute(IPayload p, Transaction t)
                                    throws WorkflowException {

            // Assertions.
            if (p == null) {
                String msg = "Argument 'p [IPayload]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (t == null) {
                String msg = "Argument 't [Transaction]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            try {

                // Process the tasks.
                Iterator it = tasks.iterator();
                while (it.hasNext()) {
                    ITask k = (ITask) it.next();
                    ITaskFactory f = k.factory();
                    if (!t.contains(f)) {
                        t.put(f, f.createTransactionContext());
                    }
                    ITransactionContext ctx = t.get(f);
                    final ITaskResult r = k.performTask(p, ctx);
                    if (r.isFailure()) {
                        t.rollback();
                        return new IWorkflowResult() {
                            public String getName() { return name; }
                            public boolean isFailure() { return true; }
                            public ITaskResult[] failedTasks() {
                                return new ITaskResult[] { r };
                            }
                            public String getMessage() {
                                return "A required task failed";
                            }
                        };
                    }
                }

            } catch (Throwable w) {
                t.rollback();
                String msg = "A workflow task failed to execute.  The "
                                + "transaction has been rolled back.";
                throw new WorkflowException(msg, w);
            }

            // Return success if we make it this far.
            return new IWorkflowResult() {
                public String getName() { return name; }
                public boolean isFailure() { return false; }
                public ITaskResult[] failedTasks() {
                    return new ITaskResult[0];
                }
                public String getMessage() { return null; }
            };

        }

    }

}
