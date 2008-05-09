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

/**
 * @deprecated As of Toro release, replaced by 
 * {@link net.unicon.penelope.complement.TypeTextConfigurableLimit}
 */
@Deprecated
public final class TypeText1024 extends AbstractType {

    /*
     * Public API.
     */

    public static final TypeText1024 INSTANCE = new TypeText1024();

    public IComplement parse(Object o) {

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!(o instanceof String)) {
            String msg = "Argument 'o [Object]' must be a String instance.";
            throw new IllegalArgumentException(msg);
        }
        String s = (String) o;
        if (s.length() > 1024) {
            String msg = "Argument 's [String]' cannot be longer than 1024 "
                                                        + "characters.";
            throw new IllegalArgumentException(msg);
        }

        return new ComplementText1024(s, this);

    }

    public IComplement fromByteArray(byte[] b) {
        return new ComplementText1024(new String(b), this);
    }

    /*
     * Implementation.
     */

    private TypeText1024() {}

    protected class ComplementText1024 extends AbstractComplement {
        public ComplementText1024(Object o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            return ((String)getValue()).getBytes();
        }
    }
}
