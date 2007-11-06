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

public final class TypeNumber extends AbstractType {

    /*
     * Public API.
     */

    public static final TypeNumber INSTANCE = new TypeNumber();

    public IComplement parse(Object o) {
        Long i = null;

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (o instanceof String) {
            try {
                i = new Long((String)o);
            } catch (NumberFormatException e) {
                String msg = "Argument 'o [Object]' must be a String "
                           + "containing only numeric digits.";
                throw new IllegalArgumentException(msg);
            }
        } else if (o instanceof Long) {
            i = (Long)o;
        } else {
            String msg = "Argument 'o [Object]' must be a String or Long instance.";
            throw new IllegalArgumentException(msg);
        }

        return new ComplementNumber(i, this);
    }

    public IComplement fromByteArray(byte[] b) {
        Long i = null;

        try {
            i = new Long(new String(b));
        } catch (NumberFormatException e) {
            String msg = "Argument 'b [byte[]]' must be represent a String "
                       + "containing only numeric digits.";
            throw new IllegalArgumentException(msg);
        }

        return new ComplementNumber(i, this);
    }

    /*
     * Implementation.
     */

    private TypeNumber() {}

    protected class ComplementNumber extends AbstractComplement {
        public ComplementNumber(Long o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            return ((Long)getValue()).toString().getBytes();
        }
    }

}
