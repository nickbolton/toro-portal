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


/**
 * Defines the contract for an operation that takes place as a part of a
 * workflow plan.  Pursuant to the Abstract Factory pattern, clients of the
 * UNICON Workflow Toolkit must specify an <code>ITaskFactory</code>
 * implementation.  Task factories know how to create appropriate
 * <code>ITask</code> instances.
 */
public interface ITask {

    /*
     * Public API.
     */

    /**
     * Returns the name of this task.
     *
     * @return The task name.
     */
    String getName();

    /**
     * Provides a reference to the factory that created this task.  The Workflow
     * Toolkit uses this method to implement workflow transactions.
     *
     * @return A workflow task factroy.
     */
    ITaskFactory factory();

    /**
     * Causes this task object to do its job based on the specified XML
     * document.
     *
     * @param p A workflow payload.
     * @param ctx The transaction context in which to perform the task.
     * @return An object encapsulating the result(s) of the task.
     * @throws WorkflowException If the task was unable to perform its
     * operations successfully.
     */
    ITaskResult performTask(IPayload p, ITransactionContext ctx)
                                    throws WorkflowException;

}
