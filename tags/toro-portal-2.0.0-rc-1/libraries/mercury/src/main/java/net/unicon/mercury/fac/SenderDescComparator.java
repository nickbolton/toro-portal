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

package net.unicon.mercury.fac;

import java.util.Comparator;

import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;

/**
 * Descending comparator for Message sender.
 * @author ibiswas
 *
 */
public class SenderDescComparator implements Comparator {
    /** 
	 * @param o1 - the first object to be compared.
	 * @param o2 - the second object to be compared
	 * @return a negative integer, zero, or a positive integer as the first argument is greater than, equal to, or less than the second. 
	 */
	public int compare(Object o1, Object o2) {
		int compareValue;
        try {
            compareValue = ((IMessage)o1).getSender().getLabel().compareToIgnoreCase(((IMessage)o2).getSender().getLabel());
        
	        if(compareValue < 0)
				return 1;
			if(compareValue > 0)
				return -1;
			return 0;
			
        } catch (MercuryException e) {
            throw new RuntimeException("SenderDescComparator : Error in getting message sender. ", e);
        }
	}
		
}
