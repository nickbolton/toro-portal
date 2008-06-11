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

package net.unicon.academus.apps.briefcase;

import junit.framework.TestCase;

public class SequencerTest extends TestCase {
	public void testSequencerInt() {
		Sequencer s = new Sequencer(1);
		return;
	}
	
	public void testSequencerIntWithNegativeSeed() {
		try {
			new Sequencer(-1);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("Constructor argument is negative and should have thrown an IllegalArgumentException.");
	}

	/*
	 * Test method for 'net.unicon.academus.apps.briefcase.Sequencer.Sequencer()'
	 */
	public void testSequencer() {
		Sequencer s = new Sequencer();
		return;
	}

	/*
	 * Test method for 'net.unicon.academus.apps.briefcase.Sequencer.next()'
	 */
	public void testNext() {
		Sequencer s = new Sequencer(100);

		assertEquals(s.next(), 100);
		return;
	}
}
