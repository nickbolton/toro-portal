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
 * Defines the contract for the information returned by a workflow operation.
 */
public interface IWorkflowResult {

    /**
     * Returns the name of the operation that the Workflow Toolkit attempted to
     * perform.
     *
     * @return The name of a workflow operation.
     */
    String getName();

    /**
     * Indicates whether the operation failed.  Any failed operation has already
     * been rolled back.
     *
     * @return <code>true</code> if the operation failed, otherwise
     * <code>false</code>.
     */
    boolean isFailure();

    /**
     * Provides the array of task results from the tasks that failed, if any.
     * Note that, in some cases, there may be tasks that fail even if the
     * workflow operation is a success.
     *
     * @return An array of task results.
     */
    ITaskResult[] failedTasks();

    /**
     * Returns a message describing the result of this operation, if there is
     * one.  This method will commonly return <code>null</code> if the operation
     * was a success, but should provide helpful information if the operation
     * failed.
     *
     * @return A message describing the result of this operation.
     */
    String getMessage();

}
