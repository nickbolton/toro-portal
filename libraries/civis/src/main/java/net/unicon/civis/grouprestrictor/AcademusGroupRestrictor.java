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

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;

public class AcademusGroupRestrictor implements IGroupRestrictor {

    public boolean checkUsersGroupPermission(IPerson user, IGroup group, String activity, boolean inherited) {
        try{
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade(true);
            return facade.checkUsersGroupPermission(facade.getUser(user.getName())
                    , facade.getGroup(group.getId()), activity, inherited);
        }catch(AcademusFacadeException e){
            throw new CivisRuntimeException("Error in AcademusGroupRestrictor : " +
            		"checkUsersGroupPermission for user. " + user.getName(), e);
        }        
    }

}
