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
package net.unicon.academus.domain.sor;

import net.unicon.academus.domain.DomainException;

/**
 * Thrown when a system attempts to access a domain object in a manner that
 * exceeds that allowable access level defined by the system of record for that
 * entity.
 */
public class SorViolationException extends DomainException {

    /**
     * Creates an empty <code>SorViolationException</code>.
     */
    public SorViolationException() {}

    /**
     * Creates an <code>SorViolationException</code> with the specified message.
     *
     * @param msg A detail message describing the circumstances surrounding this
     * exception.
     */
    public SorViolationException(String msg) {
        super(msg);
    }

    /**
     * Creates an <code>SorViolationException</code> with the specified cause.
     *
     * @param cause The exception that caused this exception.
     */
    public SorViolationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an <code>SorViolationException</code> with the specified message
     * and cause.
     *
     * @param msg A detail message describing the circumstances surrounding this
     * exception.
     * @param cause The exception that caused this exception.
     */
    public SorViolationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
