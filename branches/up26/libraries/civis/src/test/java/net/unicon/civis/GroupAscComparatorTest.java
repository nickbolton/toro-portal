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

package net.unicon.civis;

import junit.framework.TestCase;

public class GroupAscComparatorTest extends TestCase {
	/*
	 * Test method for 'net.unicon.civis.GroupAscComparator.compare(Object, Object)'
	 */
	public void testCompare() {
		
		IGroup g1 = new MockGroup("filename.txt");

		GroupAscComparator c = new GroupAscComparator();
		
		// Test when the date is the same, and the object is the same
		assertTrue(c.compare(g1, g1) == 0);

		// Test when the name is the same, but the object is different
		assertTrue(c.compare(new MockGroup("filename.txt"), new MockGroup("filename.txt")) == 0);

		// Test when obj A sorts first
		assertTrue(c.compare(new MockGroup("filename1.txt"), new MockGroup("filename2.txt")) == -1);

		// Test when obj B sorts first
		assertTrue(c.compare(new MockGroup("filename2.txt"), new MockGroup("filename1.txt")) == 1);
	}
}
