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

import java.util.HashMap;
import java.util.Map;

import net.unicon.academus.domain.DomainException;

/**
 * Defines the categories of access that can be requested for a domain entity.
 * <code>AccessType</code> follows the type-safe enumeration pattern.
 */
public final class AccessType implements Comparable {

    private static Map instances = new HashMap();

    // Instance Members.
    private int lvl;
    private String label;

    /*
     * Public API.
     */

    /**
     * Represents read access to a domain entity.  A principal who has read
     * access may view the contents of an entity.
     */
    public static final AccessType READ = new AccessType(1, "READ");

    /**
     * Represents modify access to a domain entity.  A principal who has modify
     * access may change the contents of an entity.
     */
    public static final AccessType MODIFY = new AccessType(2, "MODIFY");

    /**
     * Represents delete access to a domain entity.  A principal who has delete
     * access may remove an entity from the system.
     */
    public static final AccessType DELETE = new AccessType(3, "DELETE");

    /**
     * Represents complete and total access to a domain entity.  A principal who
     * has this level of access may do anything to an entity.
     */
    public static final AccessType ANY = new AccessType(7, "ANY");

    /**
     * Obtains the <code>AccessType</code> instance that corresponds to the
     * specified integer level.
     *
     * @param An integer that maps to an access level.
     * @return An <code>AccessType</code> instance.
     * @throws DomainException If the requested <code>AccessType</code> doesn't
     * exist.
     */
    public static AccessType getInstance(int lvl) throws DomainException {

        Integer key = new Integer(lvl);
        if (!instances.containsKey(key)) {
            String msg = "AccessType not found for level:  " + lvl;
            throw new DomainException(msg);
        }
        return (AccessType) instances.get(key);

    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The Object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!(o instanceof AccessType)) {
            String msg = "Argument 'o [Object]' is not an instance of "
                                                    + "AccessType.";
            throw new ClassCastException(msg);
        }

        // NB:  Normally it's a good idea to override equals() & hashCode()
        // when implementing Comparable.  We don't need to in this case b/c
        // AccessType is a type-safe enumeration (only contructor is private).

        AccessType a = (AccessType) o;
        return this.lvl - a.lvl;

    }

    /**
     * Returns a label that describes this instance.
     *
     * @return The label for this <code>AccessType</code>.
     */
    public String toString() {
        return label;
    }

    /*
     * Implementation.
     */

    private AccessType(int lvl, String label) {

        // Instance Members.
        this.lvl = lvl;
        this.label = label;

        // Add to the collection.
        instances.put(new Integer(lvl), this);

    }

}
