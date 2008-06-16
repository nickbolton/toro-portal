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

package net.unicon.academus.api.uportal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.ReferenceGroupService;
import org.jasig.portal.security.IPerson;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;

public class AcademusGroupImpl implements IAcademusGroup {
	
	// Instance Members.
	private final IEntityGroup group;
	private final IAcademusFacade owner;

	/*
	 * Public API.
	 */

	public AcademusGroupImpl(IEntityGroup group, IAcademusFacade owner) {
		
		// Assertions.
		if (group == null) {
			String msg = "Argument 'group' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (owner == null) {
			String msg = "Argument 'owner' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.group = group; 
		this.owner = owner; 
		
	}
	
	public String getName() throws AcademusFacadeException {
		return group.getName();
	}

	public String getKey() throws AcademusFacadeException {
		return group.getKey();
	}
	
	public void setName(String name) throws AcademusFacadeException {
		try {
			group.setName(name);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public IAcademusGroup[] getDescendantGroups() throws AcademusFacadeException {

		try {
			
	    	List rslt = new LinkedList();
	    	for (Iterator it = group.getMembers(); it.hasNext();) {
	    		Object child = it.next();
	    		if (child instanceof IEntityGroup) {
	    			rslt.add(new AcademusGroupImpl((IEntityGroup) child, owner));
	    		}
	    	}
	    	
	    	return (IAcademusGroup[]) rslt.toArray(new IAcademusGroup[0]);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

	}

	public IAcademusGroup[] getAllDescendantGroups() throws AcademusFacadeException {

		try {
			
	    	List rslt = new LinkedList();
	    	for (Iterator it = group.getAllMembers(); it.hasNext();) {
	    		Object child = it.next();
	    		if (child instanceof IEntityGroup) {
	    			rslt.add(new AcademusGroupImpl((IEntityGroup) child, owner));
	    		}
	    	}
	    	
	    	return (IAcademusGroup[]) rslt.toArray(new IAcademusGroup[0]);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

	}

	public IAcademusUser[] getContainedUsers() throws AcademusFacadeException {

		try {
			
	    	List rslt = new LinkedList();
	    	for (Iterator it = group.getMembers(); it.hasNext();) {
	    		Object child = it.next();
	    		if (child instanceof IEntity) {
	    			IEntity y = (IEntity) child;
	    			rslt.add(owner.getUser(y.getKey()));
	    		}
	    	}
	    	
	    	return (IAcademusUser[]) rslt.toArray(new IAcademusUser[0]);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

	}

	public IAcademusUser[] getAllContainedUsers() throws AcademusFacadeException {

		try {
			
	    	List rslt = new LinkedList();
	    	for (Iterator it = group.getAllMembers(); it.hasNext();) {
	    		IEntity child = (IEntity) it.next();
	    		if (child instanceof IEntity) {
	    			IEntity y = (IEntity) child;
	    			rslt.add(owner.getUser(y.getKey()));
	    		}
	    	}
	    	
	    	return (IAcademusUser[]) rslt.toArray(new IAcademusUser[0]);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

	}

    public boolean containsUser (String username) throws AcademusFacadeException {
    	
    	try {
    		IGroupService gs = ReferenceGroupService.singleton();
    		IGroupMember member = gs.getGroupMember(username, IPerson.class);
    		return group.contains(member);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

    }

    public String[] getGroupPaths (String delimiter, boolean excludeThisNode) throws AcademusFacadeException {

    	List<String> rslt = new LinkedList<String>();
    	try {

            if (delimiter == null) {
                delimiter = IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR;
            }

            List<List> chains = null;
            if (!excludeThisNode) {
            	chains = walkUp(group, new LinkedList<IEntityGroup>());
            } else {
            	chains = new LinkedList<List>();
            	for (Iterator it = group.getContainingGroups(); it.hasNext();) {
            		IEntityGroup parent = (IEntityGroup) it.next();
            		chains.addAll(walkUp(parent, new LinkedList<IEntityGroup>()));
            	}
            }
            
            for (List<IEntityGroup> list : chains) {
            	StringBuffer buffer = new StringBuffer();
            	for (IEntityGroup grp : list) {
            		buffer.append(grp.getName()).append(delimiter);
            	}
            	buffer.setLength(buffer.length() - delimiter.length());
            	rslt.add(buffer.toString());
            }


        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return rslt.toArray(new String[0]);

    }
    
    private static List<List> walkUp(IEntityGroup grp, List<IEntityGroup> chain) {
    	
    	List<IEntityGroup> newChain = new LinkedList<IEntityGroup>(chain);
    	newChain.add(0, grp);

    	List<List> rslt = new LinkedList<List>();
        try {

        	int cnt = 0;
        	for (Iterator it = grp.getContainingGroups(); it.hasNext();) {
        		IEntityGroup parent = (IEntityGroup) it.next();
        		rslt.addAll(walkUp(parent, newChain));
        		++cnt;
        	}
        	if (cnt == 0) {
        		// This is the end of the line... add this chain...
        		rslt.add(newChain);
        	}
        	
        	
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    	
    	return rslt;

    }

}