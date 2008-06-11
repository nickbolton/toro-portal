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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserIdentityStore;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;

public class UPortalAcademusFacade implements IAcademusFacade {
	
	/*
	 * Public API.
	 */

	public static void register() {
		AcademusFacadeContainer.registerFacade(new UPortalAcademusFacade());
	}
	
    public IAcademusGroup getRootGroup() throws AcademusFacadeException {

    	IAcademusGroup rslt = null;
    	try {
    		ICompositeGroupService gs = GroupService.getCompositeGroupService();    		
//        	IGroupService gs = ReferenceGroupService.singleton();
        	IEntityGroup root = gs.findGroup("local.0"); 
        	rslt = new AcademusGroupImpl(root, this);
    	} catch (Throwable t) {
    		String msg = "Unable to obtain a reference to the root group 'local.0'.";
    		throw new AcademusFacadeException(msg, t);
    	}

    	return rslt;

    }

    public IAcademusUser getUser(String username) throws AcademusFacadeException {

    	IAcademusUser rslt = null;
    	try {
    		IPerson user = PersonFactory.createPerson();
    		user.setAttribute(IPerson.USERNAME, username);
    		
    		user.setAttributes(convertToMapOfLists(PersonDirectory.getPersonAttributeDao().getUserAttributes(username)));
        	rslt = new AcademusUserImpl(user, this);
    	} catch (Throwable t) {
    		String msg = "Unable to obtain a reference to the specified user:  " + username;
    		throw new AcademusFacadeException(msg, t);
    	}

    	return rslt;

    }
    
    private Map<String, List<Object>> convertToMapOfLists(Map<String, Object> m) {
        Map<String, List<Object>> expandedMap = new HashMap<String, List<Object>>(m.size());
        
        for (String key : m.keySet()) {
            List<Object> l = new LinkedList<Object>();
            l.add(m.get(key));
            expandedMap.put(key, l);
        }
        return expandedMap;
    }

    public IAcademusGroup[] getContainingGroups(String username) throws AcademusFacadeException {

        try {
        	
    		ICompositeGroupService gs = GroupService.getCompositeGroupService();
//        	IGroupService gs = ReferenceGroupService.singleton();
        	IEntity user = gs.getEntity(username, IPerson.class);

        	List rslt = new LinkedList();
        	for (Iterator it = user.getContainingGroups(); it.hasNext();) {
        		IEntityGroup group = (IEntityGroup) it.next();
        		rslt.add(new AcademusGroupImpl(group, this));
        	}
        	
        	return (IAcademusGroup[]) rslt.toArray(new IAcademusGroup[0]);
        	
        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("UPortalAcademusFacade:getContainingGroups(): ");
            errorMsg.append("An error occured while retrieving containing groups ");
            errorMsg.append("of the user with username:");
            errorMsg.append(username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }    	

    }

    public IAcademusGroup[] getAllContainingGroups(String username) throws AcademusFacadeException {

        try {
        	        	
    		ICompositeGroupService gs = GroupService.getCompositeGroupService();
//        	IGroupService gs = ReferenceGroupService.singleton();
        	IEntity user = gs.getEntity(username, IPerson.class);

        	List rslt = new LinkedList();
        	for (Iterator it = user.getAllContainingGroups(); it.hasNext();) {
        		IEntityGroup group = (IEntityGroup) it.next();
        		rslt.add(new AcademusGroupImpl(group, this));
        	}
        	
        	return (IAcademusGroup[]) rslt.toArray(new IAcademusGroup[0]);
        	
        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);

            errorMsg.append("UPortalAcademusFacade:getAllContainingGroups(): ");
            errorMsg.append("An error occured while retrieving containing groups ");
            errorMsg.append("of the user with username:");
            errorMsg.append(username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }    	
    	
    }

    public DataSource getAcademusDataSource() throws AcademusFacadeException {
    	return RDBMServices.getDataSource();
    }

    public boolean authenticate(String username, String password) throws AcademusFacadeException {
        boolean result = false;
        try {
            result = PortalAuthentication.authenticate(username, password);
        } catch (Exception e) {
            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("AcademusFacadeImpl:authenticate(): ");
            errorMsg.append("An error while authenticating user: " + username);

            throw new AcademusFacadeException(errorMsg.toString(), e);
        }

        return result;
    }

    public Timestamp getTimeStamp () throws AcademusFacadeException {
    	return new Timestamp(System.currentTimeMillis());
    }

    public IAcademusGroup getGroup(long groupId) throws AcademusFacadeException {
    	throw new UnsupportedOperationException();
    }
    
    public IAcademusGroup getGroup(String key) throws AcademusFacadeException {

    	IAcademusGroup rslt = null;
    	try {
    		ICompositeGroupService gs = GroupService.getCompositeGroupService();
//        	IGroupService gs = ReferenceGroupService.singleton();
        	IEntityGroup root = gs.findGroup(key); 
        	rslt = new AcademusGroupImpl(root, this);
    	} catch (Throwable t) {
    		String msg = "Unable to obtain a reference to the specified group:  " + key;
    		throw new AcademusFacadeException(msg, t);
    	}

    	return rslt;

    }

    public IAcademusGroup getGroupByPath(String[] path) throws AcademusFacadeException {

    	// Assertions.
    	if (path == null) {
    		String msg = "Argument 'path' cannot be null.";
    		throw new IllegalArgumentException(msg);
    	}
    	if (path.length == 0) {
    		String msg = "Argument 'path' must contain at least one element.";
    		throw new IllegalArgumentException(msg);
    	}


    	IEntityGroup walker = null;
    	try {

    		ICompositeGroupService gs = GroupService.getCompositeGroupService();

        	EntityIdentifier ei = gs.searchForGroups(path[0], IGroupConstants.IS, IPerson.class)[0];
        	walker = gs.findGroup(ei.getKey());
        	
        	if (walker == null) {
        		String msg = "Entry point group not found:  " + path[0] + " [ei=" + ei.getKey() + "]";
        		throw new AcademusFacadeException(msg);
        	}

        	for (int i=1; i < path.length; i++) {
        		IEntityGroup next = null;
        		for (Iterator it = walker.getMembers(); it.hasNext();) {
        			IGroupMember m = (IGroupMember) it.next();
        			if (!m.isGroup()) {
        				continue;
        			}
        			IEntityGroup g = (IEntityGroup) m;
        			if (g.getName().equals(path[i])) {
        				next = g;
        				break;
        			}
        		}
        		if (next != null) {
        			walker = next;
        		} else {
        			StringBuffer msg = new StringBuffer("The specified group does not exist:  ");
        	    	for (int k=0; k < path.length; k++) {
        	    		msg.append(path[k]).append(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR);
        	    	}
        	    	msg.setLength(msg.length() - IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR.length());
        	    	throw new AcademusFacadeException(msg.toString());
        		}
        	}

    	} catch (Throwable t) {
			StringBuffer msg = new StringBuffer("Unable to resolve the specified group path:  ");
	    	for (int k=0; k < path.length; k++) {
	    		msg.append(path[k]).append(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR);
	    	}
	    	msg.setLength(msg.length() - IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR.length());
    		throw new AcademusFacadeException(msg.toString(), t);
    	}

    	return new AcademusGroupImpl(walker, this);
    	
    }

    public boolean checkUsersGroupPermission(IAcademusUser user, IAcademusGroup group,
            	String activity, boolean inherited)throws AcademusFacadeException {
    	// ToDo:  Implement me!
    	return true;
    }

    public IAcademusGroup getGroupByPath(String path) throws AcademusFacadeException {
    	return getGroupByPath(path.split(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR));
    }
    
    public IAcademusUser[] getAcademusUsers() throws AcademusFacadeException {
    	throw new UnsupportedOperationException();
    }

    public IAcademusUser[] findAcademusUsers(String username, String firstName, String lastName, 
    				String email, boolean matchAll) throws AcademusFacadeException {
    	
    	// NB:  Ignoring firstName, lastName, email b/c it's not exactly obvious how to find on them in uPortal...
    	
    	List rslt = new LinkedList();
    	try {
    		IPerson user = PersonFactory.createPerson();
    		user.setAttribute(IPerson.USERNAME, username);

    		// Verify the user exists...
    		RDBMUserIdentityStore uis = new RDBMUserIdentityStore();
    		uis.getPortalUID(user, false);
    		
    		// Fill w/ attributes...
    		user.setAttributes(PersonDirectory.getPersonAttributeDao().getUserAttributes(username));
        	rslt.add(new AcademusUserImpl(user, this));
    	} catch (Throwable t) {
    		// t.printStackTrace(System.out);
    		// RDBMUserIdentityStore probably threw it b/c the user doesn't exist.  We have no information to add...
    	}

    	return (IAcademusUser[]) rslt.toArray(new IAcademusUser[0]);
    	
    }
    
    public IAcademusGroup[] findAcademusGroups(String keyword) throws AcademusFacadeException {

    	List rslt = new LinkedList();
    	
    	try {
    		
        	ICompositeGroupService gs = GroupService.getCompositeGroupService();

        	EntityIdentifier[] eis = gs.searchForGroups(keyword, IGroupConstants.CONTAINS, IPerson.class);
        	for (int i=0; i < eis.length; i++) {
            	IEntityGroup g = gs.findGroup(eis[i].getKey());
            	rslt.add(new AcademusGroupImpl(g, this));
        	}

    	} catch (Throwable t) {
    		String msg = "Operation findAcaemusGroups() failed.  keyword=" + keyword;
    		throw new AcademusFacadeException(msg, t);
    	}

    	return (IAcademusGroup[]) rslt.toArray(new IAcademusGroup[0]);
    	
    }

}
