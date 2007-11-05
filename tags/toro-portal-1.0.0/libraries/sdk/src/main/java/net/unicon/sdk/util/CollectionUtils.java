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
package net.unicon.sdk.util;

import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Kevin Gary
 *
 * Static utility methods for operating on Collections or similar such data
 * structures in java.util
 */
public class CollectionUtils {
    public static final int INTERSECTION = 1;
    public static final int UNION        = 2;
    public static final int DIFFERENCE   = 3;
    
    /**
     * This method constructs a Map from a Set and a Collection using either
     * Intersection, Union, or Difference
     * @param operand1
     * @param operand2
     * @param masterValues 
     * @param isInt
     * @return
     */
    public static Map constructMapFromSet(Set operand1, Collection operand2,
                                          Map masterValues, int operation) {
        if (operation == 1) {  // INTERSECTION
            operand1.retainAll(operand2);
        }
        else if (operation == 2) {  // UNION
            operand1.addAll(operand2);
        }
        else if (operation == 3) {  // DIFFERENCE
            operand1.removeAll(operand2);
        }

        // now put all of what is left in the Map
        Map attributes = new HashMap();
        String nextKey = null;
        for (Iterator iter = operand1.iterator(); iter.hasNext(); ) {
            nextKey = (String)iter.next();
            attributes.put(nextKey, (String)masterValues.get(nextKey));
        }
        return attributes;
    }    

}
