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
package net.unicon.portal.common;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Role;
/**
 * This is a class that contains state information about a channel. Channels will be able to cache certain states across users
 * and this class will be used as a key to index that cache. A state will be defined as
 * the <code>Offering</code>, <code>Role</code> and command for a channel.
 */
public class ChannelState {
    private Offering offering;
    private Role role;
    private String command;
    /**
     * Create a ChannelState object.
     * @param offering the offering
     * @param role the role
     * @param command the command
     */
    public ChannelState(Offering offering, Role role, String command) {
        this.offering = offering;
        this.role = role;
        this.command = command;
    }
    /** Create a ChannelState object. */
    public ChannelState() {
        this.offering = null;
        this.role = null;
        this.command = null;
    }
    /**
     * Set the <code>Offering</code>
     * @param offering the offering
     */
    public void setOffering(Offering offering) {
        this.offering = offering;
    }
    /**
     * Get the <code>Offering</code>
     * @return <code>Offering</code>
     */
    public Offering getOffering() {
        return this.offering;
    }
    /**
     * Set the <code>Role</code>
     * @param role the role
     */
    public void setRole(Role role) {
        this.role = role;
    }
    /**
     * Get the <code>Role</code>
     * @return <code>Role</code>
     */
    public Role getRole() {
        return this.role;
    }
    /**
     * Set the command
     * @param command the command
     */
    public void setCommand(String command) {
        this.command = command;
    }
    /**
     * Get the command
     * @return <code>String</code>
     */
    public String getCommand() {
        return this.command;
    }
    /**
     * Test the equality of ChannelState objects.
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (obj instanceof ChannelState) {
            ChannelState cs = (ChannelState)obj;
            return offeringsMatch(cs) && rolesMatch(cs) && commandsMatch(cs);
        }
        return false;
    }
    private boolean offeringsMatch(ChannelState cs) {
        Offering off1 = cs.getOffering();
        Offering off2 = this.getOffering();
        // either they are both null or it's the same object, they match
        if (off1 == off2) return true;
        // One object is null and the other isn't, they don't match
        if (off1 == null || off2 == null) return false;
        // return true if their ids match
        return off1.getId() == off2.getId();
    }
    private boolean rolesMatch(ChannelState cs) {
        Role role1 = cs.getRole();
        Role role2 = this.getRole();
        // either they are both null or it's the same object, they match
        if (role1 == role2) return true;
        // One object is null and the other isn't, they don't match
        if (role1 == null || role2 == null) return false;
        // return true if their ids match
        return role1.getId() == role2.getId();
    }
    private boolean commandsMatch(ChannelState cs) {
        String cmd1 = cs.getCommand();
        String cmd2 = this.getCommand();
        // either they are both null or it's the same object, they match
        if (cmd1 == cmd2) return true;
        // One object is null and the other isn't, they don't match
        if (cmd1 == null || cmd2 == null) return false;
        // return true if they're equal
        return cmd1.equals(cmd2);
    }
}
