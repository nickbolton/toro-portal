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

package net.unicon.sdk.task.jms;

import javax.jms.TopicConnection;

import net.unicon.sdk.task.WorkflowException;

/**
 * Defines the contract through which the <code>JmsTaskFactory</code> obtains
 * JMS <code>Connection</code> instances.
 */
interface IConnectionBroker {

    /**
     * Provides a new JMS <code>Connection</code> instance.
     *
     * @return A new <code>Connection</code>.
     * @throws WorkflowException If the connection broker failed to create a
     * <code>Connection</code>.
     */
    TopicConnection newConnection() throws WorkflowException;

}