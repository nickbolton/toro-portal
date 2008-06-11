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

package net.unicon.civis.grouprestrictor;

import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;

public interface IGroupRestrictor {

    public String SELECT_GROUP_ACTIVITY = "SELECT";
    
    /**
	 * Determines if the given user can perform the specified activity on the given group.
	 * @param user An <code>IPerson</code> instance that uniquely identifies the user.
	 * @param group An <code>IGroup</code> that uniquely identifies the target group.
	 * @param activity The name of the activity being tested.
	 * @param inherited defines whether inherited permissions should be taken into consideration.
	 *
	 * @return True if the activity is allowed, false otherwise.
	 */
	public boolean checkUsersGroupPermission(IPerson user, IGroup group,
			String activity, boolean inherited);
					
}
