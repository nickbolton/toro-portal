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
package net.unicon.sdk.guid.vm;

import java.rmi.dgc.VMID;

import net.unicon.sdk.guid.Context;
import net.unicon.sdk.guid.Guid;
import net.unicon.sdk.guid.GuidException;
import net.unicon.sdk.guid.IGuidService;

/**
 * Provides Globally Unique Identifiers from the java VM.
 */
public class VMGuidService implements IGuidService {
    
    /** constructor */
    public VMGuidService() {}

    /**
     * Provides a Globally Unique Identifier (GUID) from the java VM.
     *
     * @return Guid object that represents a globally unique identifier. 
     * @throws <code>GuidException</code>
     * @see <{Guid}>
     */
    public synchronized Guid generate() throws GuidException {

        try {
            VMID vmID = new VMID();
            Guid guid = new Guid(vmID.toString());

            return guid;
        } catch (Throwable t) {
            throw new GuidException(t.getMessage(), t);
        }

    } // end generate 

    /**
     * Provides a Globally Unique Identifier (GUID) from the java VM.
     *
     * @param ctx Context that the GUID is to be created for.
     * @return Guid object that represents a globally unique identifier. 
     * @throws <code>GuidException</code>
     * @see <{Guid}>
     * @see <{Context}>
     */
    public synchronized Guid generate(Context ctx) throws GuidException {
        Guid guid = this.generate();

        return guid;
    } // end generate(ctx)

} // end VMGuidService
