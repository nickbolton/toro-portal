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

package net.unicon.academus.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
	Serves only as a container for the Academus Facade API implementation.
	The Academus codebase can register a facade implementation in this container
	so that other applications that have access to it can re-use functionality
	implemented in the Academus codebase.
*/
public class AcademusFacadeContainer {

	// Placeholder for the Academus Facade implementation
    private static IAcademusFacade academusFacade = null;
    private static Object lock = new Object();
    private static Log log = LogFactory.getLog(AcademusFacadeContainer.class);

	// Not used at the moment
    private AcademusFacadeContainer() {

	}

	/**
     * Registers the specified Academus facade implementation into the container.
	 *
     * @param facade The Academus facade implementation to be used.
   	 *
	 */
    public synchronized static void registerFacade (IAcademusFacade facade) {

		// Assertions
		if (facade == null) {

			StringBuffer error = new StringBuffer(128);

			error.append("AcademusFacadeContainer:");
			error.append("An error occured while registering the Academus Facade.");
			error.append("Facade reference was empty/null.");

			throw new IllegalArgumentException(error.toString());
		}

		if (log.isDebugEnabled()) {
		    log.debug("registering facade: " + facade.getClass().getName());
		}
		// Set the facade in the container
		academusFacade = facade;
		lock.notifyAll();
	}

	/**
	 * Returns the Academus Facade implementation registered with the container.
	 *
	 * @return The Academus facade implementation registered in this container.
	 *
	 */
    public synchronized static IAcademusFacade retrieveFacade(boolean blocking) {

		if (log.isDebugEnabled()) {
		    log.debug("retrieving facade(blocking="+blocking+"): " + academusFacade);
		}
		
		// Assertions
		if (!blocking && academusFacade == null) {

			StringBuffer error = new StringBuffer(128);

			error.append("AcademusFacadeContainer:");
			error.append("An error occured while retrieving the Academus Facade.");
			error.append("No facade has been registed yet.");

			throw new IllegalStateException(error.toString());
		}

		while (blocking && academusFacade == null) {
		    try {
        		if (log.isDebugEnabled()) {
        		    log.debug("Waiting for facade...");
        		}
                lock.wait();
        		if (log.isDebugEnabled()) {
        		    log.debug("Woke up - facade: " + academusFacade);
        		}
            } catch (InterruptedException e) {
                return retrieveFacade(false);
            }
		}
		if (log.isDebugEnabled()) {
		    log.debug("returning facade: " +
		        (academusFacade != null ? academusFacade.getClass().getName() : "NULL"));
		}
		return academusFacade;
	}
}