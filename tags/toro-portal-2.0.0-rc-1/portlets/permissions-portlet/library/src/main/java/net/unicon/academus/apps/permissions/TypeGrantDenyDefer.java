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

package net.unicon.academus.apps.permissions;

import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.complement.AbstractType;

public final class TypeGrantDenyDefer extends AbstractType {

    /*
     * Public API.
     */

    public static final TypeGrantDenyDefer INSTANCE = new TypeGrantDenyDefer();

    public IComplement parse(Object o) {
        String s = null;

        // Assertions.
        if(o == null){
            throw new IllegalArgumentException("Argument 'o' cannot be null.");
        }
        if (o instanceof String) {
            s = (String)o;
            if(s.equalsIgnoreCase("grant") || 
                    s.equalsIgnoreCase("deny") ||
                    s.equalsIgnoreCase("defer")){                  
            }else{    
    	        String msg = "Argument 'o' must be a String Object "
	                   + "containing only values 'grant', 'deny' or 'defer'.";
    	        throw new IllegalArgumentException(msg);
            }
        } else{
            throw new IllegalArgumentException("Argument 'o' should be of type String.");
        }
        
        return new ComplementGrantDenyDefer(s, this);
    }

    public IComplement fromByteArray(byte[] b) {
        String o = new String(b);

        if (o.equals("grant") || 
                o.equals("deny") ||
                o.equals("defer")){            
        }else{    
	        String msg = "Argument 'b [byte[]]' must be represent a String "
	                   + "containing only values 'grant', 'deny' or 'defer'";
	        throw new IllegalArgumentException(msg);
        }

        return new ComplementGrantDenyDefer(o, this);
    }

    /*
     * Implementation.
     */

    private TypeGrantDenyDefer() {}

    protected class ComplementGrantDenyDefer extends AbstractComplement {
        public ComplementGrantDenyDefer(String o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            return ((String)getValue()).getBytes();
        }
    }

}
