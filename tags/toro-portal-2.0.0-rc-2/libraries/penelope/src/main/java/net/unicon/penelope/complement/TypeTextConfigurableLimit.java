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

import java.util.HashMap;
import java.util.Map;

import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;

/**
 * The <code>TypeTextConfigurableLimit</code> class is a configurable addition
 * to the TypeText IComplementType types. This class exposes static factory 
 * methods instead of a public static member field named INSTANCE. This allows 
 * for the text field character limit to be specified dynamically.
 * 
 * @author bszabo@unicon.net
 * 
 * @since Toro
 */
public final class TypeTextConfigurableLimit extends AbstractType {

    /*
     * Public API.
     */

    /**
     * Factory method.
     * 
     * @return instance of <code>TypeTextConfigurableLimit</code> that has 
     *         no character limit.
     */
    public static TypeTextConfigurableLimit createInstance() {
        return createInstance(null);
    }
    
    /**
     * Factory method.
     * 
     * @param characterLimit specifies the maximum number of characters allowed.
     *        Unlimited text length can be specified by passing null for this
     *        argument, or if the <code>Integer</code> int value is negative.
     * @return instance of <code>TypeTextConfigurableLimit</code> that is 
     *         constrained by the specified <code>characterLimit</code>.
     */
    public static TypeTextConfigurableLimit createInstance(
            Integer characterLimit) {
        
       TypeTextConfigurableLimit instance = null; 
      
       synchronized (TypeTextConfigurableLimit.class) {
           if (instances.containsKey(characterLimit)){
               instance = instances.get(characterLimit);
           }
           else {
               instance = new TypeTextConfigurableLimit(characterLimit);
               instances.put(characterLimit, instance);
           }
       }
       
       return instance;
    }

    /**
     * Validates the given string against the the character limit of this 
     * instance.
     * 
     * @param o specifies a string of characters
     * @return instance of <code>IComplement</code> that represents the text.
     * @throws IllegalArgumentException if <code>o</code> is null, not a 
     *         <code>String</code>, or exceeds the character limit for this 
     *         <code>IComplementType</code>.
     */
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
        if (this.characterLimit >= 0 && this.characterLimit < s.length()) {
            StringBuffer msg = new StringBuffer(62);
            msg.append("Argument (String)'Object o' cannot be longer than ");
            msg.append(this.characterLimit);
            msg.append(" characters.");
            throw new IllegalArgumentException(msg.toString());
        }

        return new ComplementTextConfigurableLimit(s, this);

    }

    /**
     * Returns an <code>IComplement</code> parsed from the givem byte array.
     * 
     * @param b is a byte array representation of an <code>IComplement</code>
     * @return instance of <code>IComplement</code> generated from the given 
     *         byte array.
     */
    public IComplement fromByteArray(byte[] b) {
        return new ComplementTextConfigurableLimit(new String(b), this);
    }

    /*
     * Implementation.
     */

    private static Map<Integer, TypeTextConfigurableLimit> instances = 
        new HashMap<Integer, TypeTextConfigurableLimit>();
    
    private int characterLimit;
    
    private TypeTextConfigurableLimit(Integer characterLimit) {
        int limit = -1; // Negative represents unlimited
        if (characterLimit != null) {
            limit = characterLimit.intValue();
        }
        this.characterLimit = limit;
    }

    protected class ComplementTextConfigurableLimit extends AbstractComplement {
        
        public ComplementTextConfigurableLimit(Object o, IComplementType t) {
            super(o, t);
        }

        public byte[] toByteArray() {
            return ((String)getValue()).getBytes();
        }
    }

}
