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
 * Thrown to indicate a problem in the UNICON Workflow Toolkit.
 */
public final class WorkflowException extends Exception {

    /*
     * Public API.
     */

    /**
     * Creates a new <code>WorkflowException</code>.
     */
    public WorkflowException() {
        super();
    }

    /**
     * Creates a new <code>WorkflowException</code> instance with the specified
     * error message.
     *
     * @param msg A human-readable message that describes the nature of the
     * exception.
     */
    public WorkflowException(String msg) {
        super(msg);
    }

    /**
     * Creates a new <code>WorkflowException</code> instance with the specified
     * underlying cause.
     *
     * @param cause An error that was thrown within the UNICON Workflow Toolkit
     * and that is the underlying cause of this exception.
     */
    public WorkflowException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new <code>WorkflowException</code> instance with the specified
     * error message and underlying cause.
     *
     * @param msg A human-readable message that describes the nature of this
     * exception.
     * @param cause An error that was thrown within the UNICON Workflow Toolkit
     * and that is the underlying cause of this exception.
     */
    public WorkflowException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
