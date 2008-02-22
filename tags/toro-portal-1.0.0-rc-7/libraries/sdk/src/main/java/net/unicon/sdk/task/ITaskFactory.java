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

import org.dom4j.Element;

/**
 * Defines the contract for a task factory within the UNICON Workflow Toolkit.
 * Task factories follow the Abstract Factory pattern.  Each
 * <code>ITaskFactory</code> implementation knows how to create appropriate task
 * instances.
 */
public interface ITaskFactory {

    /*
     * Public API.
     */

    /**
     * Parses an import task from the specified XML element.  The required
     * format of the XML is dependent upon the <code>ITaskFactory</code>
     * implementation.
     *
     * @param e An XML element containing the specifics of an import task.
     * @return An import task based upon the specified XML.
     * @throws WorkflowException If the task factory cannot create an import
     * task from the specified instructions.
     */
    ITask parseImportTask(Element e) throws WorkflowException;

    /**
     * Creates and returns a new transaction context.  <code>ITaskFactory</code>
     * implementations <b>must</b> provide import tasks that support the ability
     * to roll back all changes associated with a specific transaction context.
     *
     * @return A new transaction context.
     * @throws WorkflowException If the task factory cannot initiate a
     * transaction context.
     */
    ITransactionContext createTransactionContext() throws WorkflowException;

}
