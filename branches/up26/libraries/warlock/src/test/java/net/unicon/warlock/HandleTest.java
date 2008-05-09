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

package net.unicon.warlock;

import net.unicon.penelope.Label;
import junit.framework.TestCase;

public class HandleTest extends TestCase {
	/*
	 * Test method for 'net.unicon.warlock.Handle.create(String)'
	 */
	public void testCreateTooLong() {
		String longString = "12345678901234567890123456789012345678901234567890123456789012345";
		
		try {
			Handle.create(longString);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("create() argument is too long and should have thrown an IllegalArgumentException.");
	}

	public void testCreateWithNull() {
		try {
			Handle.create(null);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("create() argument is null and should have thrown an IllegalArgumentException.");
	}
	
	/*
	 * Test method for 'net.unicon.warlock.Handle.getValue()'
	 */
	public void testGetValue() {
		String shortString = "123457890";

		Handle h = Handle.create(shortString);
		
		assertTrue(shortString.equals((String)h.getValue()));
	}

	/*
	 * Test method for 'net.unicon.warlock.Handle.equals(Object)'
	 */
	public void testEqualsObject() {
		Handle h1 = Handle.create("string");
		Handle h2 = Handle.create("string");
		Handle h3 = Handle.create("string3");

		assertTrue(h1.equals(h2));
		assertFalse(h2.equals(h3));
	}
}
