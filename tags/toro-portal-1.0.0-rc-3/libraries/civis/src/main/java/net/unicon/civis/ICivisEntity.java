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

package net.unicon.civis;

import net.unicon.penelope.IDecisionCollection;

/**
 * Base type for Civis entities.
 */
public interface ICivisEntity extends Comparable {

    /**
     * Obtains the unique id for this Civis entity.
     */
    String getId();

    /**
     * Obtains the factory instance that 'owns' this Civis entity.
     */
    ICivisFactory getOwner();

    /**
     * Obtains the name for this Civis entity.  Names are guaranteed to be
     * unique in context.  In other words, there can be only one person with a
     * specified name per factory instance, and no two groups with the same name
     * may have a common parent.
     */
    String getName();

    /**
     * Obtains the set of attributes for the Civis entity.  Attributes are
     * open-ended in the Civis Subsystem.  Each factory implementation must
     * define the attributes and allowable values for each entity type.
     */
    IDecisionCollection getAttributes();

    /**
     * Obtains an XML representation of this Civis entity.
     */
    String toXml();
    
    /**
     * Obtains an URL representation of this Civis entity.
     * The URL contains information of the factory that created it
     * as well the unique identifier for the entity
     */
    String getUrl();
}