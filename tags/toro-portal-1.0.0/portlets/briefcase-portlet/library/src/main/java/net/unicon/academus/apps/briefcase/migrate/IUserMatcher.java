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

package net.unicon.academus.apps.briefcase.migrate;

import java.io.File;

import net.unicon.alchemist.access.Identity;

public interface IUserMatcher {

    void init();

    void destroy();

    /**
     * Finds and returns the <code>Identity</code> that owns the specified
     * resource factory.  Returns <code>null</code> if the owner cannot be
     * found.
     *
     * @param fac A resource factory that needs to be migrated.
     * @return The owner of the specified factory or <code>null</code>.
     */
    Identity matchIdentity(File f);

}