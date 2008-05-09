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

package net.unicon.warlock.portlet;

import net.unicon.penelope.complement.AbstractType;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;

public final class TypeFileUpload extends AbstractType {

    /*
     * Public API.
     */

    public static final TypeFileUpload INSTANCE = new TypeFileUpload();

    public IComplement parse(Object o) {

        // Assertions.
        if (o == null) {
            String msg = "Argument 'o [Object]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!(o instanceof FileUpload)) {
            String msg = "Argument 'o [Object]' must be a FileUpload "
                                                    + "instance.";
            throw new IllegalArgumentException(msg);
        }

        return new ComplementFileUpload(o, this);

    }

    public IComplement fromByteArray(byte[] b) {
        throw new UnsupportedOperationException(
                "TypeFileUpload does not support byte array conversion.");
    }

    /*
     * Implementation.
     */

    private TypeFileUpload() {}

    protected class ComplementFileUpload extends AbstractType.AbstractComplement {
        public ComplementFileUpload(Object o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            throw new UnsupportedOperationException(
                        "TypeFileUpload does not support byte array conversion.");
        }
    }


}
