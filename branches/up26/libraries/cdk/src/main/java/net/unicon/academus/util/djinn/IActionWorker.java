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

package net.unicon.academus.util.djinn;

import java.util.Map;

import net.unicon.academus.common.AcademusException;

/**
 * Encapsulates the implementation of an action.  Each <code>Action</code>
 * leverages an <code>IActionWorker</code> to handle it's instance-specific
 * implementation.
 */
public interface IActionWorker {

    /**
     * Controller method for the behavior associated with an action.
     * Application developers must implement <code>doAction</code> once for each
     * action available within their app.  The <code>contextId</code> can be
     * anything on which the <code>IActionWorker</code> and the application
     * agree, and allows the <code>IActionWorker</code> implementation to
     * identify the relevent context.
     *
     * @param applicationId the unique identifier for the relevant application.
     * @param args the collection of inputs from the UI.
     * @return the result of this action (new mode and state).
     */
    ActionResult doAction(String applicationId, Map args)
                            throws AcademusException;

}
