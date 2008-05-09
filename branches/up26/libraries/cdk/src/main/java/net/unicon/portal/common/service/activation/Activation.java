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

package net.unicon.portal.common.service.activation;

import java.util.List;
import java.util.Date;
import java.util.Map;

import net.unicon.academus.delivery.ReferenceObject;
import net.unicon.portal.common.PortalObject;

public interface Activation extends PortalObject {

    /**
     * returns the activation id
     * @return <code>int</code> of the activation id
     */
    public int getActivationID();

    /**
     * returns the offering id
     * @return <code>int</code> of the offering id
     */
    public int getOfferingID();

    /**
     * returns the type of the activation
     * @return <code>String</code> of the type
     */
    public String getType();

    /**
     * returns the start date of the activation
     * @return <code>Date</code> of the start date
     */
    public Date getStartDate();

    /**
     * returns the start time of the activation in long
     * @return <code>long</code> of the start time
     */
    public long getStartTime();

    /**
     * returns the end date of the activation
     * @return <code>Date</code> of the end date
     */
    public Date getEndDate();

    /**
     * returns the end time of the activation in long
     * @return <code>long</code> of the end time
     */
    public long getEndTime();

    /**
     * returns the duration of  time of the activation in long
     * @return <code>long</code> of the time of the duration
     */
    public long getDuration();

    /**
     * returns the attributes of the activation
     * @return <code>Map<code> of the attributes of the activation
     */
    public Map getAttributes();

    public void addAttribute(String key, String value);

    /**
     * returns the List of usernames that are part of the activation
     * In the case that the activation is for all members, the list will be empty
     * @return <code>List</code> of the usernames
     */
    public List getUsernames();

    /**
     * return true of false if the activation effects all users
     * @return <code>boolean</code>
     */
    public boolean forAllUsers();

    /**
     * set the attributes of the activation
     * @param <code>Map<code> of the attributes of the activation
     */
    public void setAttributes(Map attributes);

    /**
     * sets the List of usernames that are part of the activation
     * In the case that the activation is for all members, the list will be empty
     * @param <code>List</code> of the usernames
     */
    public void setUsernames(List userList);

    /**
     * sets true of false if the activation effects all users
     * @param <code>boolean</code>
     */
    public void setForAllUsers(boolean forAllUsers);

    /**
     * sets the refrence object in which the activation has to 
     * link too
     * @param <code>ReferenceObject</code>
     */
    public void setReferenceObject(ReferenceObject ref);
    
    public boolean hasFile();

    public void setFile(boolean b);

}

