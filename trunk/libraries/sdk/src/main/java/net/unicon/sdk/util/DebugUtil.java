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

import java.util.Map;
import java.util.Set;

public class DebugUtil {
    private DebugUtil() {}

    // for debugging only!
    public static String spillParameters(Map inData, String msg) {
        StringBuffer rslt = new StringBuffer(msg);
	if (inData == null) {
	    rslt.append(" NO PARAMS");
	    return rslt.toString();
	}

	// To avoid ConcurrentModificationException in case our
	// Map is really a Hashtable or some other synchronized structure
	Set keySet = inData.keySet();
	Object[] keys = keySet.toArray();

	for (int i = 0; i < keys.length; i++) {
	    Object key = keys[i];
	    rslt.append('\t' + "#" + key + "# = ");
	    Object value = inData.get(key);
	    if (value == null) {
		rslt.append("NULL!\n");
	    }
	    else if (value instanceof java.util.Map) {
		Map valMap = (Map)value;
		Set valKeySet = valMap.keySet();
		Object[] valKeys = valKeySet.toArray();

		Object nextKey = null;
		rslt.append("<MAP>");
		for (int j = 0; j < valKeys.length; j++) {
		    nextKey = valKeys[j];
		    rslt.append("\t\t" + nextKey + " = " + valMap.get(nextKey));
		}
	    }
	    else if (value instanceof java.util.Collection) {
		rslt.append("<Collection>");
		Object[] valArray = ((java.util.Collection)value).toArray();
		for (int i2 = 0; i2 < valArray.length; i2++) {
		    rslt.append(valArray[i2] + "\n");
		}
	    }
	    else if (value.getClass().isArray()) {
		rslt.append("<Array>");
		Object[] valArray = (Object[]) value;
		for (int k = 0; k < valArray.length; k++) {
		    rslt.append(valArray[k] + "\n");
		}
	    }
	    else {
		rslt.append(value + "\n");
	    }
        }
	return rslt.toString();
    }
}
