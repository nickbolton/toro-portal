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

package net.unicon.sdk.task.fake;

import net.unicon.sdk.task.IPayload;
import net.unicon.sdk.task.ITask;
import net.unicon.sdk.task.ITaskFactory;
import net.unicon.sdk.task.ITaskResult;
import net.unicon.sdk.task.ITransactionContext;
import net.unicon.sdk.task.WorkflowException;

import org.dom4j.Element;

public final class FakeTaskFactory implements ITaskFactory {

    /*
     * Public API.
     */

    public FakeTaskFactory(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

    }

    public ITask parseImportTask(Element e) throws WorkflowException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        System.out.println("Executing FakeTaskFactory.parseImportTask()...");

        return new FakeTask(e, this);

    }

    public ITransactionContext createTransactionContext() throws WorkflowException {

        System.out.println("Executing FakeTaskFactory.createTransactionContext()...");

        return new FakeTransactionContext();

    }

    /*
     * Implementation.
     */

    private final class FakeTask implements ITask {

        // Instance Members.
        private String name;
        private ITaskFactory fac;

        /*
         * Public API.
         */

        public FakeTask(Element e, ITaskFactory f) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (f == null) {
                String msg = "Argument 'f [ITaskFactory]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.name = e.attributeValue("name");
            this.fac = f;

        }

        public String getName() {
            return name;
        }

        public ITaskFactory factory() {
            return fac;
        }

        public ITaskResult performTask(IPayload p, ITransactionContext ctx) throws WorkflowException {

            // Assertions.
            if (p == null) {
                String msg = "Argument 'p [IPaylod]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (ctx == null) {
                String msg = "Argument 'ctx' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            System.out.println("Executing FakeTask.performTask()...");

            return new ITaskResult.SuccessfulTaskResult(name);

        }

    }

    private final class FakeTransactionContext implements ITransactionContext {

        /*
         * Public API.
         */

        public FakeTransactionContext() {}

        public void commit() throws WorkflowException {

            System.out.println("Executing FakeTransactionContext.commit()...");

        }

        public void rollback() throws WorkflowException {

            System.out.println("Executing FakeTransactionContext.rollback()...");

        }

    }

}
