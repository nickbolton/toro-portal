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
package net.unicon.portal.groups.framework.testers;

import junit.framework.TestCase;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;

/**
 * 
 */
public class FunctionalGroupTest extends TestCase {

    private IEntityGroup top = null;
    
    public FunctionalGroupTest(String entityType) throws GroupsException {
        this.top = GroupService.getDistinguishedGroup(entityType);
    }
    
    /**
     *
     */

    protected void runTest() throws Throwable {
        IEntityGroup[] subs = new IEntityGroup[100];
        /*
        // create new group1
        IEntityGroup newGroup1 = createGroup(top, "NewGroup1", "NewGroupDescription1");
        
        // create new group2
        IEntityGroup newGroup2 = createGroup(newGroup1, "NewGroup2", "NewGroupDescription2");
        
        // create 100 sub-groups to group2
        for (int i=0; i<100; i++) {
            subs[i] = createGroup(newGroup2, "NewSubGroup"+(i+1), "NewSubGroupDescription"+(i+1));
        }
        
        // try to create a new group named the same as group1 and try and add it to top
        // if it succeeds, it should throw an exception :)
        try {
            IEntityGroup duplicate = createGroup(top, "NewGroup1", "NewGroupDescription1");
            fail("Allowed group membership with duplicate name.");
        } catch (GroupsException ofe) {
        }
        
        // delete all the new groups
        for (int i=0; i<subs.length; i++) {
            subs[i].delete();
        }
        
        newGroup2.delete();
        newGroup1.delete();
        */
        
        // now do it all again using lockable groups
        
        // create new group1
        ILockableEntityGroup topl = GroupService.findLockableGroup(top.getKey(), "admin");
        ILockableEntityGroup newGroupL1 = createLockableGroup(topl, "NewGroup1", "NewGroupDescription1");
        
        // create new group2
        ILockableEntityGroup newGroupL2 = createLockableGroup(newGroupL1, "NewGroup2", "NewGroupDescription2");
        
        // create 100 sub-groups to group2
        for (int i=0; i<subs.length; i++) {
            subs[i] = createGroup(newGroupL2, "NewSubGroup"+(i+1), "NewSubGroupDescription"+(i+1));
        }
        
        // delete all the new groups
        for (int i=0; i<subs.length; i++) {
            subs[i].delete();
        }
        
        newGroupL2.delete();
        newGroupL1.delete();
        
    }
    
    private void addMember(IEntityGroup parent, IGroupMember member) throws Throwable {
        // assign member to parent
        
        parent.addMember(member);
        
        if (parent instanceof ILockableEntityGroup) {
            ((ILockableEntityGroup)parent).updateMembersAndRenewLock();
        } else {
            parent.updateMembers();
        }
        assertEquals(parent + " does not contain added member: " + member, parent.contains(member), true);
    }
    
    private void removeMember(IEntityGroup parent, IGroupMember member) throws Throwable {
        // assign member to parent
        parent.removeMember(member);
        
        if (parent instanceof ILockableEntityGroup) {
            ((ILockableEntityGroup)parent).updateMembersAndRenewLock();
        } else {
            parent.updateMembers();
        }
        assertEquals(parent + " contains removed member: " + member, parent.contains(member), false);
    }
    
    private void addGroupMember(IEntityGroup parent, IEntityGroup member) throws Throwable {
        addMember(parent, member);
        
        // test that parent added member
        assertNotNull(parent.getMemberGroupNamed(member.getName()));
    }
    
    private ILockableEntityGroup createLockableGroup(IEntityGroup parent, String name, String desc) throws Throwable {
        IEntityGroup newGroup = createGroup(parent, name, desc);
        
        ILockableEntityGroup lGroup = GroupService.findLockableGroup(newGroup.getKey(), "admin");
        assertNotNull(lGroup);
        return lGroup;
    }
    
    private IEntityGroup createGroup(IEntityGroup parent, String name, String desc) throws Throwable {
        // create new group
        IEntityGroup newGroup = GroupService.newGroup(top.getEntityType());
        assertNotNull(newGroup);
        IEntityGroup group = GroupService.findGroup(newGroup.getKey());
        group.setCreatorID("11");
        group.setName(name);
        group.setDescription(desc);
        group.update();
        
        addGroupMember(parent, group);
        
        return group;
    }
}
