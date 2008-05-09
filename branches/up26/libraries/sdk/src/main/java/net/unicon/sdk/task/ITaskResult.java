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
 * Defines the contract for the information returned by a task when it performs
 * its function.
 */
public interface ITaskResult {

    /**
     * Returns the name of the task that generated this result.
     *
     * @return The name of a workflow task.
     */
    String getName();

    /**
     * Indicates whether the task failed.
     *
     * @return <code>true</code> if the task failed, otherwise
     * <code>false</code>.
     */
    boolean isFailure();

    /**
     * Returns a message describing the result of this task, if there is one.
     * This method will commonly return <code>null</code> if the task was a
     * success, but should provide helpful information if the task failed.
     *
     * @return A description of this result.
     */
    String getMessage();

    /**
     * If the reporting task failed, this method will provide access to the
     * error that caused the failure where applicable.  Otherwise, this method
     * returns <code>null</code>.
     *
     * @return An error that cased task failure.
     */
    Throwable cause();

    /**
     * Utility implementation of <code>ITaskResult</code> to indicate success.
     */
    static final class SuccessfulTaskResult implements ITaskResult {

        private String name;

        /*
         * Public API.
         */

        /**
         * Creates a new <code>SuccessfulTaskResult</code> with the specified
         * task name.
         *
         * @param name The name of the task that will report this result.
         */
        public SuccessfulTaskResult(String name) {

            // Assertions.
            if (name == null) {
                String msg = "Argument 'name' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            this.name = name;

        }

        /**
         * Returns the name of the task that is reporting success.
         *
         * @return The name of a workflow task.
         */
        public String getName() { return name; }

        /**
         * Returns <code>false</code>.
         *
         * @return <code>false</code>.
         */
        public boolean isFailure() { return false; }

        /**
         * Returns <code>null</code>.
         *
         * @return <code>null</code>.
         */
        public String getMessage() { return null; }

        /**
         * Returns <code>null</code>.
         *
         * @return <code>null</code>.
         */
        public Throwable cause() { return null; }

    }

}
