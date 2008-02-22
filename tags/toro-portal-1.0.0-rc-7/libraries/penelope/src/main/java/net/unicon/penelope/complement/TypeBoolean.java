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

public final class TypeBoolean extends AbstractType {

    /*
     * Public API.
     */

    public static final TypeBoolean INSTANCE = new TypeBoolean();

    public IComplement parse(Object o) {
        Boolean b = null;

        // Assertions.
        if(o == null){
            throw new IllegalArgumentException("Argument 'o' cannot be null.");
        }
        if (o instanceof String) {
            if(((String)o).equalsIgnoreCase("true") || 
                    ((String)o).equalsIgnoreCase("false")){
                b = Boolean.valueOf((String)o);   
            }else{    
    	        String msg = "Argument 'o' must be a String Object "
	                   + "containing only boolean values 'true' or 'false'.";
    	        throw new IllegalArgumentException(msg);
            }
        } else{
            throw new IllegalArgumentException("Argument 'o' should be of type String.");
        }
        
        return new ComplementBoolean(b, this);
    }

    public IComplement fromByteArray(byte[] b) {
        Boolean o = null;

        if (new String(b).equals("true") || new String(b).equals("false")){
            o = Boolean.valueOf(new String(b));
        }else{    
	        String msg = "Argument 'b [byte[]]' must be represent a String "
	                   + "containing only boolean values 'true' or 'false'.";
	        throw new IllegalArgumentException(msg);
        }

        return new ComplementBoolean(o, this);
    }

    /*
     * Implementation.
     */

    private TypeBoolean() {}

    protected class ComplementBoolean extends AbstractComplement {
        public ComplementBoolean(Boolean o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            return ((Boolean)getValue()).toString().getBytes();
        }
    }

}
