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

package net.unicon.demetrius.fac;

import junit.framework.TestCase;

import java.util.Date;

import net.unicon.demetrius.IResource;
import net.unicon.demetrius.MockResource;

public class NameAscComparatorTest extends TestCase {
	/*
	 * Test method for 'net.unicon.demetrius.fac.NameAscComparator.compare(Object, Object)'
	 */
	public void testCompare() {
		Date d1 = new Date();
		Date d2 = new Date(d1.getTime() + 1000);
		
		IResource r1 = new MockResource("filename.txt", 0, d1);

		NameAscComparator nac = new NameAscComparator();
		
		// Test when the name is the same, and the object is the same
		assertTrue(nac.compare(r1, r1) == 0);

		// Test when the name is the same, but the object is different
		assertTrue(nac.compare(new MockResource("filename.txt", 0, d1), new MockResource("filename.txt", 0, d2)) == 0);

		// Test when the name is different
		assertFalse(nac.compare(new MockResource("filename1.txt", 0, d1), new MockResource("filename2.txt", 0, d1)) == 0);

		// Test when obj A sorts first
		assertTrue(nac.compare(new MockResource("objA.txt", 0, d1), new MockResource("objB.txt", 0, d2)) == -1);

		// Test when obj B sorts first
		assertTrue(nac.compare(new MockResource("objB.txt", 0, d1), new MockResource("objA.txt", 0, d2)) == 1);

	}
}
