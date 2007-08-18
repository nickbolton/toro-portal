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
package net.unicon.portal.groups.framework;

import java.util.LinkedList;
import java.util.List;


public class ActionCountQueue {
	private int queueSize;
	
	private List queue = new LinkedList();
	
	public ActionCountQueue(int queueSize) {
		this.queueSize = queueSize;
	}
	
	public int size() {
		return queue.size();
	}
	
	public void enqueue(long count) {
		queue.add(new Long(count));
		if (queue.size() > queueSize) {
			queue.remove(0);
		}
	}
	
	public long getTotalActions() {
		long sum = 0;
		for (int i=0; i<queue.size(); i++) {
			sum += ((Long)queue.get(i)).longValue();
		}
		return sum;
	}
}