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

package net.unicon.academus.civisselector;

import java.util.Comparator;

import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.penelope.IDecisionCollection;

public class EntityAscComparator implements Comparator {

	/** 
	 * @param sharee1
	 * 	The first object to be compared.
	 * @param sharee2
	 * 	The second object to be compared
	 * @return 
	 * 	a negative integer, zero, or a positive integer as the first argument
	 *  is less than, equal to, or greater than the second. 
	 */
	public int compare(Object sharee1, Object sharee2) {
		int rslt = 0;
		IEntity s1 = (IEntity)sharee1;
		IEntity s2 = (IEntity)sharee2;
		
		// Testing if sharee types are equal
		if (s1.getType().equals(s2.getType())) {
			// Testing the first sharee to see which type it is.
			if (s1.getType().equals(EntityType.GROUP)){
				// the type is ShareeType.GROUP
				rslt = ((IGroup)s1.getObject()).getName()
					.toLowerCase().compareTo(((IGroup)s2.getObject())
					.getName().toLowerCase());
			 
			} else {
				// the type is ShareeType.MEMBER
			    IDecisionCollection dColl1 = ((IPerson)s1.getObject()).getAttributes();
			    IDecisionCollection dColl2 = ((IPerson)s2.getObject()).getAttributes();
			    rslt = ((String)dColl1.getDecision("lName").getFirstSelectionValue())
					.toLowerCase().compareTo(((String)dColl2
					.getDecision("lName").getFirstSelectionValue()).toLowerCase());
				if (rslt == 0) {
					rslt = ((String)dColl1.getDecision("fName").getFirstSelectionValue()).toLowerCase()
					.compareTo(((String)dColl2.getDecision("fName").getFirstSelectionValue())
					.toLowerCase());
				}
				 
			}
		} else if (s1.getType().equals(EntityType.GROUP)){
			rslt = -1;
		} else if (s1.getType().equals(EntityType.MEMBER)){
			rslt = 1;
		}

		return rslt;
	}
}
