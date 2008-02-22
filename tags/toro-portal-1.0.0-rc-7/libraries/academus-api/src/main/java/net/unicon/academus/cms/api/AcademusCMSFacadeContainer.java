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

package net.unicon.academus.cms.api;

/**
 * Serves only as a container for the Academus CMS Facade
 * API implementation. The Academus codebase can register a CMS facade 
 * implementation in this container so that other applications that have 
 * access to the container can re-use functionality implemented in the 
 * Academus codebase.
 */
public class AcademusCMSFacadeContainer {

    // Placeholder for the Academus CMS Facade implementation
    private static IAcademusCMSFacade academusCMSFacade = null;

    // Not used at the moment
    private AcademusCMSFacadeContainer() {

    }

    /**
     * Registers the specified Academus CMS facade implementation into the * container.
     * @param facade The Academus CMS facade implementation to be used.
     */
    public synchronized static void registerFacade (IAcademusCMSFacade facade) {

        // Assertions
        if (facade == null) {

            StringBuffer error = new StringBuffer(128);
            error.append("AcademusCMSFacadeContainer:");
            error.append("An error occured while registering the "); error.append("Academus CMS Facade.");
            error.append("Facade reference was empty  or null.");
            
			throw new IllegalArgumentException(error.toString());
        }

        // Set the facade in the container
        academusCMSFacade = facade;
    }

    /**
     * Returns the Academus CMS facade implementation registered with the 
	 * container.
     * @return The Academus CMS facade implementation registered in this 
	 * container.
     */
    public synchronized static IAcademusCMSFacade retrieveFacade() {

        // Assertions
        if (academusCMSFacade == null) {

            StringBuffer error = new StringBuffer(128);
            error.append("AcademusCMSFacadeContainer:");
            error.append("An error occured while retrieving the "); error.append("Academus CMS Facade.");
            error.append("No facade has been registed yet.");

            throw new IllegalStateException(error.toString());
        }

        // Return the registered Academus facade
        return academusCMSFacade;
    }
}