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

package net.unicon.mercury;

/**
 * Representation of a message recipient with a scoping type.
 * <p>The actual types are left up to the implementing classes to determine,
 * but common types might include "To", "Cc", and "Bcc".</p>
 */
public interface IRecipient{

    /**
     * Retrieve the associated Address.
     * @return Address represented by this recipient object.
     * @see IAddress
     */
    IAddress getAddress();

    /**
     * Retrieve the associated ReceipientType.
     * @return Scoping type tied to this recipient object.
     * @see IRecipientType
     */
    IRecipientType getType();
    
    /**
     * Retrieve the associated EntityType.
     * @return Scoping type tied to this recipient object.
     * @see EntityType
     */
    EntityType getEntityType();
    
    /**
     * Retrive the XML representation of the given object.
     * @return String representation of the instance
     * @throws MercuryException
     */
    String toXml() throws MercuryException;

}
