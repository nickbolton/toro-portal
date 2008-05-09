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
package net.unicon.academus.domain.lms;

import java.util.Iterator;
import java.util.List;

import org.jasig.portal.services.LogService;

import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/**
 * Information about a logical group of offerings including name, type, and
 * parent topic.  <code>TopicType</code> follows the type-safe enumeration pattern.
 */
public final class Topic implements IDomainEntity {

    static String groupPrefix = null;

    static {
        try {
            groupPrefix = UniconPropertiesFactory.getManager(
                AcademusPropertiesType.LMS).getProperty(
                    "net.unicon.academus.domain.lms.Topic.groupPrefix");
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }

    /* Instance Members */
    private long Id       = 0;
    private IGroup parentGroup = null;
    private IGroup group  = null;
    private String name        = null;
    private String description = null;
    private TopicType type     = null;
    Topic(long Id, IGroup group, String name, String description,
    TopicType type, IGroup parentGroup) {
        this.Id = Id;
        this.name = name;
        this.group = group;
        this.parentGroup = parentGroup;
        this.description = description;
        this.type = type;
    }
    /**
     * Provides the unique identifier for this topic.
     * @return the Id for this topic.
     */
    public long getId() {
        return Id;
    }
    /**
     * Provides the associated group for this topic.
     * @return the Id for this topic.
     */
    public IGroup getGroup() {
        return group;
    }
    /**
     * Provides the associated parent group for this topic.
     * @return the Id for this topic.
     */
    public IGroup getParentGroup() {
        return parentGroup;
    }
    /**
     * Provides the associated parent group for this topic.
     * @return the Id for this topic.
     */
    public void setParentGroup(IGroup newParentGroup)
    throws OperationFailedException {

        try {
            // first remove the topic's group from it's current
            // parent group
            if (parentGroup != null) {
                parentGroup.removeGroup(group);
            }

            // now make this group a member of the new parentGroup
            newParentGroup.addGroup(group);

            parentGroup = newParentGroup;
        } catch (UniconGroupsException uge) {
            throw new OperationFailedException(uge);
        }
    }
    /**
     * Provides the label (name) for this topic.  This information is useful for display purposes.
     * @return the name of this topic.
     */
    public String getName() {
        return name;
    }

    /**
     * Modifies the label (name) associated with this topic.
     * @param newName the new name to be used for this topic.
     * @throws IllegalArgumentException if the newName is zero length or
     * contains only whitespace characters or already exists (for another topic) within the same parent topic.
     */
    public void setName(String newName) throws IllegalArgumentException,
                            OperationFailedException, ItemNotFoundException,
                            DomainException {

        // Assertions.
        if (newName == null || newName.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Topic name can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }
        if (!name.equals(newName) &&
        TopicFactory.topicExistsInContext(newName, parentGroup)) {
            StringBuffer msg = new StringBuffer();
            msg.append("The specified topic name already exists ");
            msg.append("within context parent group.");
            throw new IllegalArgumentException(msg.toString());
        }

        this.name = newName;

        // Update the group.
        try {
            group.setName(groupPrefix+newName);
        } catch (UniconGroupsException ge) {
            StringBuffer msg = new StringBuffer();
            msg.append("Failed to update the associated group: ");
            msg.append(ge.getMessage());
            throw new OperationFailedException(msg.toString());
        }

    }

    /**
     * Provides the label (description) for this topic. This information is useful for display purposes.
     * @return the description of this topic.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifies the label (description) associated with this topic.
     * @param newDesc the new description to be used for this topic.
     * @throws IllegalArgumentException if the newDesc is zero length or contains only whitespace characters.
     */
    public void setDescription(String newDesc) throws IllegalArgumentException,
                            ItemNotFoundException, OperationFailedException,
                            DomainException {

        // Assertions.
        if (newDesc == null || newDesc.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Topic description can't be zero-length ");
            msg.append("or contain only whitespace.");
            throw new IllegalArgumentException(msg.toString());
        }

        this.description = newDesc;

        // Update the group.
        try {
            group.setDescription(newDesc);
        } catch (UniconGroupsException ge) {
            StringBuffer msg = new StringBuffer();
            msg.append("Failed to update the associated group: ");
            msg.append(ge.getMessage());
            throw new OperationFailedException(msg.toString());
        }

    }

    /**
     * Provides the type for this topic.  <code>TopicType</code> is a type-safe
     * enumeration (see Effective Java).  Examples of topic types include Academics and Communities of Interest.
     * @return the type for this topic.
     */
    public TopicType getTopicType() {
        return type;
    }
    
    public boolean hasChildren() throws IllegalArgumentException,
        OperationFailedException {
            return (OfferingFactory.getOfferings(this).size() +  TopicFactory.getTopics(this.getGroup()).size()) > 0;
    }
    
    /**
     * Returns true if an Offering name exists in this Topic. This method was 
     * added for the fix of &lsquo;TeamTrack 04651 &ndash; Import: Users are 
     * able to import the same offering multiple times &rsquo;.
     * 
     * @param offeringName the name of the Offering being checked.
     * @return true if the specified Offering name exists in the this Topic, 
     *         false otherwise.
     * @throws IllegalArgumentException if an argument is null.
     * @throws OperationFailedException if a referenced domain method attempts
     *         to read from or update the database but fails to do so.
     */
    public boolean hasOffering(String offeringName) 
            throws OperationFailedException {
        
        // Assertions
        if (offeringName == null) {
            String msg = "The argument 'offeringName' cannot be null.";
            throw new IllegalArgumentException(msg);            
        }
        
        boolean rslt = false;
        
        List offerings = OfferingFactory.getOfferings(this);        
        Iterator it = offerings.listIterator();
        
        while (it.hasNext()) {
            
            if (offeringName.equals(((Offering)it.next()).getName())) {
                rslt = true;
                break;
            }
            
        }
        
        return rslt;
    }
}
