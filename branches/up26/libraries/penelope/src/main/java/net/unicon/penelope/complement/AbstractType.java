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

package net.unicon.penelope.complement;

import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.PenelopeException;

/**
 * Base class for all ComplementTypes.
 * All extending classes are required to implement a public static member field
 * named INSTANCE which should refer to an instance of the extending class.
 */
public abstract class AbstractType implements IComplementType {

    /*
     * Public API.
     */

    public abstract IComplement fromByteArray(byte[] b);

    /**
     * Return an IComplementType instance for the given classname.
     * All extending classes are required to implement a public static member field
     * named INSTANCE which should refer to an instance of the extending class.
     *
     * @throws PenelopeException On reflection failure.
     * @deprecated As of the Toro release, this method should no longer be 
     * used to obtain <code>IComplementType</code>'s. Subclasses should not 
     * implement a public static member field named INSTANCE, but instead
     * declare their own static factory methods to allow for configuration via 
     * an IOC container.
     */
    public static IComplementType getInstance(String classname)
                                    throws PenelopeException {
        IComplementType rslt = null;

        try {
            Class c = Class.forName(classname);

            rslt = (IComplementType)c.getField("INSTANCE").get(null);

        } catch (ClassNotFoundException e) {
            String msg = "Unable to load the requested IComplementType.";
            throw new PenelopeException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Unable to load the requested IComplementType.";
            throw new PenelopeException(msg, e);
        } catch (NoSuchFieldException e) {
            String msg = "Unable to load the requested IComplementType. The "
                       + "requested type does not implement the INSTANCE "
                       + "public field.";
            throw new PenelopeException(msg, e);
        }

        return rslt;
    }

    /*
     * Protected API.
     */

    protected AbstractType() {}

    /*
     * Nested Types.
     */

    public abstract class AbstractComplement implements IComplement {

        // Instance Members.
        private Object value;
        private IComplementType type;

        /*
         * Public API.
         */

        public AbstractComplement(Object value, IComplementType type) {

            // Assertions.
            // NB:  value may be null.
            if (type == null) {
                String msg = "Argument 'type' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.value = value;
            this.type = type;

        }

        public Object getValue() {
            return value;
        }

        public IComplementType getType() {
            return type;
        }

        public abstract byte[] toByteArray();

    }

}
