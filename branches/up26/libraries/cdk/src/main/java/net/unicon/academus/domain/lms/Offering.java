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

import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupsException;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jasig.portal.services.LogService;

/**
 * A community of users in the portal system.  An offering might represent a
 * course of students lead by an instructor, or perhaps a student
 * organization lead by a student president.
 */
public final class Offering implements IDomainEntity {

    static String groupPrefix = null;

    static {
        try {
            groupPrefix = UniconPropertiesFactory.getManager(
                AcademusPropertiesType.LMS).getProperty(
                    "net.unicon.academus.domain.lms.Offering.groupPrefix");
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }
    public static final int DEFAULT_VALUE = 0;

    public static final int ACTIVE   = 1;
    public static final int INACTIVE = 2;
    /* Instance Members */
    private long Id = 0;
    private IGroup __group     = null;
    private String name        = null;
    private String description = null;
    private EnrollmentModel enrollModel = null;
    private List channels      = null;
    private Map channelMap     = null;
    private long [] topicId;
    private long defaultRoleId;
    private int status = 0;
    //*************************************************************
    // New instance members for Requirements OA 4.1-4.12
    private String optionalId   = null;
    private String optionalTerm = null;

    private int optionalMonthStart     = DEFAULT_VALUE;
    private int optionalDayStart       = DEFAULT_VALUE;
    private int optionalYearStart      = DEFAULT_VALUE;

    private int optionalMonthEnd       = DEFAULT_VALUE;
    private int optionalDayEnd         = DEFAULT_VALUE;
    private int optionalYearEnd        = DEFAULT_VALUE;

    // Days of Week
    private int __daysOfWeek           = DEFAULT_VALUE;

    private int optionalHourStart      = DEFAULT_VALUE;
    private int optionalMinuteStart    = DEFAULT_VALUE;
    private int optionalAmPmStart      = DEFAULT_VALUE;

    private int optionalHourEnd        = DEFAULT_VALUE;
    private int optionalMinuteEnd      = DEFAULT_VALUE;
    private int optionalAmPmEnd        = DEFAULT_VALUE;

    private String optionalLocation = null;

    Offering(long Id,
    IGroup group,
    String name,
    String description,
    int status,
    EnrollmentModel enrollModel,
    Topic [] topics,
    long defaultRoleId,
    List channelList)
    throws ItemNotFoundException, OperationFailedException {
        this.Id = Id;
        this.__group = group;
        this.name = name;
        this.description = description;
        this.enrollModel = enrollModel;
        this.defaultRoleId = defaultRoleId;
        this.channels = channelList;
        this.status = status;

        if (topics != null) {
            this.topicId = new long [topics.length];
            for(int ix=0; ix < topics.length; ++ix) {
                this.topicId[ix] = topics[ix].getId();
            }
        } else {
            throw new ItemNotFoundException(
                "Offering must have atleast one parent Topic");
        }
    }

    Offering(long Id,
    IGroup group,
    String name,
    String description,
    int status,
    EnrollmentModel enrollModel,
    Topic topic,
    long defaultRoleId,
    List channelList)
    throws ItemNotFoundException, OperationFailedException {
        this(
            Id,
            group,
            name,
            description,
            status,
            enrollModel,
            new Topic[] {topic},
            defaultRoleId,
            channelList);
    }

    public Set getTopicIds () {

        Set idSet = new HashSet();

        for (int index = 0; index < topicId.length; index++) {

            long id = topicId[index];
            String idStr = Long.toString(id);
            idSet.add(idStr);
        }

        return idSet;
    }

    public IGroup getGroup() {
        return __group;
    }

    /**
     * Returns the first topic in the list
     *
     * @return a the first Topic associated with the Offering.
     */
    public Topic getTopic() {
        Topic topic = null;
        try {
            topic = TopicFactory.getTopic(topicId[0]);
        } catch (OperationFailedException ofe) {
            LogService.log(LogService.ERROR,
            "getTopic Operation Failed : Offering.getTopic()");
        } catch (ItemNotFoundException infe) {
            LogService.log(LogService.ERROR,
            "Cannot Find Topic Object : Offering.getTopic() " + topicId);
        }
        return topic;
    }

    /**
     * Returns all topics in the list
     *
     * @return all the Topics associated with the Offering.
     */
    public Topic[] getTopics() {
        Topic [] rtnValue = new Topic[topicId.length];
        try {
            for (int ix=0; ix < topicId.length; ++ix) {
                rtnValue[ix] = TopicFactory.getTopic(topicId[ix]);
            }
        } catch (OperationFailedException ofe) {
            LogService.log(LogService.ERROR,
            "getTopic Operation Failed : Offering.getTopic()");
        } catch (ItemNotFoundException infe) {
            LogService.log(LogService.ERROR,
            "Cannot Find Topic Object : Offering.getTopic() " + topicId);
        }
        return rtnValue;
    }

    /**
     * This method will add topics to the offering.
     * No topics are removed when another is added.
     *
     * @param topic - add a topic to the offering.
     */
    public void addTopic(Topic topic)
    throws OperationFailedException, IllegalArgumentException,
    ItemNotFoundException, DomainException {

        // Checking to see if the there is already an association
        // with the current topic.
        try {
            long [] nTopicArray = new long[topicId.length+1];

            boolean contains = false;
            for (int ix=0; ix < this.topicId.length && !contains; ++ix) {
                nTopicArray[ix] = topicId[ix];
                contains = this.topicId[ix] == topic.getId();
            }
            if (contains) return;

            nTopicArray[topicId.length] = topic.getId();
            this.topicId = nTopicArray;

            // now add new parent group association
            Topic currentTopic = getTopic();
            if (currentTopic != null && currentTopic.getGroup() != null) {
                currentTopic.getGroup().addGroup(__group);
            }
        } catch (UniconGroupsException uge) {
            throw new OperationFailedException(uge);
        }
    }

    /**
     * This method will remove topics to the offering.
     * No topics are removed when another is added.
     *
     * @param topic - remove a topic to the offering.
     */
    public void removeTopic(Topic topic)
    throws OperationFailedException, IllegalArgumentException,
    ItemNotFoundException {
        // Checking to see if the there is already an association
        // with the current topic.
        try {
            long [] nTopicArray = null;
            if (topicId.length > 0 && topic != null) {
                nTopicArray = new long[topicId.length-1];

                boolean contains = false;
                for (int ix=0; ix < this.topicId.length; ++ix) {
                    if (this.topicId[ix] != topic.getId()) {
                        nTopicArray[ix] = topicId[ix];
                    } else {
                        contains = true;
                    }
                }
                if (!contains) return;

                if (topic.getGroup() != null) {
                    topic.getGroup().removeGroup(__group);
                }
                this.topicId = nTopicArray;
             }
        } catch (UniconGroupsException uge) {
            throw new OperationFailedException(uge);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status)
    throws OperationFailedException, IllegalArgumentException,
    ItemNotFoundException, DomainException {
        this.status = status;
    }

    public Role getDefaultRole() {
        Role role = null;
        try {
            role = RoleFactory.getRole(defaultRoleId);
        } catch (OperationFailedException ofe) {
            LogService.log(LogService.ERROR,
            "getRole Operation Failed : Offering,getDefaulttRole()");
        } catch (ItemNotFoundException infe) {
            LogService.log(LogService.ERROR,
            "Cannot Find Role Object : Offering.getDefaultRole() " + defaultRoleId);
        }
        return role;
    }

    public void setDefaultRole(long defaultRoleId)
    throws OperationFailedException, IllegalArgumentException,
    ItemNotFoundException, DomainException {
        this.defaultRoleId = defaultRoleId;
    }

    /**
     * Provides the offering's unique identifier within the portal system.
     * Offering Id(s) are used to query for other objects, <i>e.g.</i> all
     * the members of this offering or all the channels it supports.
     *
     * @return the Id for this offering.
     */
    public long getId() {
        return Id;
    }
    /**
     * Provides the name assigned to this offering by someone in authority.
     * An offering's name is used for display purposes.
     *
     * @return the name of this offering.
     */
    public String getName() {
        return name;
    }
    /**
     * Modifies the name given to this offering.  An offering's name is used
     * for display purposes.
     *
     * @param name the new name to be used for this offering.
     *
     * @throws IllegalArgumentException if the <code>name</code> is
     * <code>null</code>, is zero-length, or contains only whitespace, or if
     * the <code>name</code> already exists for another offering within the
     * parent topic.
     */
    public void setName(String name) throws IllegalArgumentException,
    OperationFailedException, DomainException {
        if (name == null) {
            throw new IllegalArgumentException("The name can't be null.");
        }
        if (name.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("The name can't be zero-length ");
            msg.append("or contain only whitespace");
            throw new IllegalArgumentException(msg.toString());
        }
        if (OfferingFactory.offeringExistsInContext(name, this)) {
            StringBuffer msg = new StringBuffer();
            msg.append("The specified offering name already exists ");
            msg.append("within this context (topic).");
            throw new IllegalArgumentException(msg.toString());
        }
        this.name = name;
        try {
            __group.setName(groupPrefix + name);
        } catch (UniconGroupsException ge) {
            StringBuffer msg = new StringBuffer();
            msg.append("Failed to update the associated group: ");
            msg.append(ge.getMessage());
            throw new OperationFailedException(msg.toString());
        }
    }

    /**
     * Provides the description assigned to this offering by someone
     * in authority.  An offering's description is used for display purposes.
     * @return the description of this offering.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifies the description given to this offering. An offering's description
     * is used for display purposes.
     * @param description the new description to be used for this offering.
     * @throws IllegalArgumentException if the <code>description</code> is
     * <code>null</code>, is zero-length, or contains only whitespace.
     */
    public void setDescription(String description)
    throws IllegalArgumentException, OperationFailedException, DomainException {
        if (description == null) {
            throw new IllegalArgumentException(
            "The description can't be null.");
        }
        if (description.trim().length() == 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("The description can't be zero-length ");
            msg.append("or contain only whitespace");
            throw new IllegalArgumentException(msg.toString());
        }
        this.description = description;
        try {
            __group.setDescription(description);
        } catch (UniconGroupsException ge) {
            StringBuffer msg = new StringBuffer();
            msg.append("Failed to update the associated group: ");
            msg.append(ge.getMessage());
            throw new OperationFailedException(msg.toString());
        }
    }

    /**
     * Indicates how new members are to be enrolled in this offering.
     * <code>EnrollmentModel</code> is a type-safe enumeration (see Effective Java).
     * Examples include: <ul>
     * <li>Self-enrolled</li> <li>Facilitator-enrolled</li> </ul>
     * @return the type of enrollment used by this offering.
     */
    public EnrollmentModel getEnrollmentModel() {
        return enrollModel;
    }
    /**
     * Modifies the way in which new members are to be added to the offering.
     * <code>EnrollmentModel</code> is a type-safe enumeration (see Effective Java).
     * Examples include: <ul>
     * <li>Self-enrolled</li> <li>Facilitator-enrolled</li> </ul>
     * @param model the new method of enrollment for this offering.
     * @throws IllegalArgumentException if <code>model</code> is <code>null</code>.
     */
    public void setEnrollmentModel(EnrollmentModel model)
    throws IllegalArgumentException, OperationFailedException, DomainException {
        if (model == null) {
            String msg = "The enrollment model can't be null.";
            throw new IllegalArgumentException(msg);
        }
        enrollModel = model;
    }

    /**
     * Provides the set of all channels this offering employs for and
     * given <code>ChannelMode</code>.  Use <code>addChannel</code> and
     * <code>removeChannel</code> to alter this collection, as this method
     * performs a defensive copy.
     *
     * @param <code>ChannelMode</code> the mode
     * @return all the channels this offering employs.
     */
    public List getChannels(ChannelMode mode) {
        // for now, all offerings will offer all channels
        // return new ArrayList(channels);
        try {
            return new ArrayList(ChannelClassFactory.
            getOrderedChannelClassMap(mode).values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }
    public List getChannels() {
        return getChannels(ChannelMode.OFFERING);
    }
    public Map getChannelMap() {
        // for now, all offerings will offer all channels
        // return new ArrayList(channels);
        // return channelMap;
        try {
            return ChannelClassFactory.
            getOrderedChannelClassMap(ChannelMode.OFFERING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap();
    }
    public Iterator getChannelIterator() {
        // for now, all offerings will offer all channels
        // return new ArrayList(channels);
        // return channelMap.values().iterator();
        try {
            return ChannelClassFactory.getOrderedIterator(
            ChannelMode.OFFERING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList().iterator();
    }
    protected void setupChannelMap()
    throws OperationFailedException, ItemNotFoundException {
        try {
            // Create a new TreeMap so we can keep them ordered
            channelMap = new TreeMap();
            Map fullSet =
            ChannelClassFactory.getOrderedChannelClassMap(ChannelMode.OFFERING);
            Iterator itr = fullSet.keySet().iterator();
            while (itr != null && itr.hasNext()) {
                Integer orderKey = (Integer)itr.next();
                ChannelClass cc = (ChannelClass)fullSet.get(orderKey);
                if (channels.contains(cc)) {
                    channelMap.put(orderKey, cc);
                }
            }
        } catch (DomainException de) {
            throw new OperationFailedException(de);
        }
    }
    /**
     * Adds the specified channel to the set of channels this offering employs.
     * @param channel the new channel to add.
     * @throws IllegalArgumentException if the <code>channel</code> is null or
     * is already a member of this offering's channels.
     */
    public void addChannel(ChannelClass channel)
    throws IllegalArgumentException {
        throw new UnsupportedOperationException("XXX:  Not Implemented!");
    }
    /**
     * Removes the specified channel from the set of channels this offering
     * employs.
     * @param channel the channel to remove.
     * @throws IllegalArgumentException if the <code>channel</code> is null
     * or is not a member of this offering's channels.
     */
    public void removeChannel(ChannelClass channel)
    throws IllegalArgumentException {
        throw new UnsupportedOperationException("XXX:  Not Implemented!");
    }
    public boolean equals(Object otherOffering) {
        return ( (otherOffering instanceof Offering) &&
        ((Offering)otherOffering).getId() == getId());
    }
    public int hashCode() {
        return (int)getId();
    }
    //*********************************************************************
    // New get and set methods for Requirements OA 4.1-4.12
    /**
     * Get method for the offering's optional Identification string
     * @return optionalId
     */
    public String getOptionalId() {
        return optionalId;
    }
    /**
     * Set method for the offering's optional Identification string
     * @param optionalIdString corresponds to the optional Id for the offering
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalId(String optionalIdString)
    throws OperationFailedException {
        this.optionalId = optionalIdString;
    }
    /**
     * Get method for the offering's optional Term string
     * @return optionalTerm
     */
    public String getOptionalTerm() {
        return optionalTerm;
    }
    /**
     * Set method for the offering's optional Term string
     * @param optionalTermString corresponds to the optional Term for the offering
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalTerm(String optionalTermString)
    throws OperationFailedException {
        this.optionalTerm = optionalTermString;
    }
    /**
     * Get method for the offering's optional month start
     * @return optionalMonthStart
     */
    public int getOptionalMonthStart() {
        return optionalMonthStart;
    }
    /**
     * Set method for the offering's optional month start
     * @param optionalMonthStartInt corresponds to the optional month
     * start for the offering (1-12)
     * @throws IllegalArgumentException if the month specified is not in [1..12]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalMonthStart(int optionalMonthStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalMonthStartInt != DEFAULT_VALUE &&
            (optionalMonthStartInt < 1 || optionalMonthStartInt > 12)) {
            String msg = "Unable to set the optional Month Start for "
            + "offeringId=" + Id + " - month out of range:  "
            + optionalMonthStartInt;
            throw new IllegalArgumentException(msg);
        }
        this.optionalMonthStart = optionalMonthStartInt;
    }
    /**
     * Get method for the offering's optional day start
     * @return optionalDayStart
     */
    public int getOptionalDayStart() {
        return optionalDayStart;
    }
    /**
     * Set method for the offering's optional day start
     * @param optionalDayStartInt corresponds to the optional day start
     * for the offering (1-31)
     * @throws IllegalArgumentException if the day specified is not in [1..31]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalDayStart(int optionalDayStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalDayStartInt != DEFAULT_VALUE &&
            (optionalDayStartInt < 1 || optionalDayStartInt > 31)) {
            String msg = "Unable to set the optional Day Start for " +
            "offeringId=" + Id + " - day out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalDayStart = optionalDayStartInt;
    }
    /**
     * Get method for the offering's optional year start
     * @return optionalYearStart
     */
    public int getOptionalYearStart() {
        return optionalYearStart;
    }
    /**
     * Set method for the offering's optional year start
     * @param optionalYearStartInt corresponds to the optional year start
     * for the offering (2002-2010)
     * @throws IllegalArgumentException if the year specified is not in [2002..2010]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalYearStart(int optionalYearStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalYearStartInt != DEFAULT_VALUE &&
            (optionalYearStartInt < 2002 || optionalYearStartInt > 2010)) {
            String msg = "Unable to set the optional Year Start for " +
            "offeringId=" + Id + " - year out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalYearStart = optionalYearStartInt;
    }
    /**
     * Get method for the offering's optional month end
     * @return optionalMonthEnd
     */
    public int getOptionalMonthEnd() {
        return optionalMonthEnd;
    }
    /**
     * Set method for the offering's optional month end
     * @param optionalMonthEndInt corresponds to the optional month end
     * for the offering (1-12)
     * @throws IllegalArgumentException if the month specified is not in [1..12]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalMonthEnd(int optionalMonthEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalMonthEndInt != DEFAULT_VALUE &&
            (optionalMonthEndInt < 1 || optionalMonthEndInt > 12)) {
            String msg = "Unable to set the optional Month End for " +
            "offeringId=" + Id + " - month out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalMonthEnd = optionalMonthEndInt;
    }
    /**
     * Get method for the offering's optional day end
     * @return optionalDayEnd
     */
    public int getOptionalDayEnd() {
        return optionalDayEnd;
    }
    /**
     * Set method for the offering's optional day end
     * @param optionalDayEndInt corresponds to the optional day
     * end for the offering (1-31)
     * @throws IllegalArgumentException if the day specified is not in [1..31]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalDayEnd(int optionalDayEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalDayEndInt != DEFAULT_VALUE &&
            (optionalDayEndInt < 1 || optionalDayEndInt > 31)) {
            String msg = "Unable to set the optional Day End for " +
            "offeringId=" + Id + " - day out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalDayEnd = optionalDayEndInt;
    }
    /**
     * Get method for the offering's optional year end
     * @return optionalYearEnd
     */
    public int getOptionalYearEnd() {
        return optionalYearEnd;
    }
    /**
     * Set method for the offering's optional year end
     * @param optionalYearEndInt corresponds to the optional year end
     * for the offering (2002-2010)
     * @throws IllegalArgumentException if the year specified is not in [2002..2010]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalYearEnd(int optionalYearEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalYearEndInt != DEFAULT_VALUE && (
            optionalYearEndInt < 2002 || optionalYearEndInt > 2010)) {
            String msg = "Unable to set the optional Year End for " +
            "offeringId=" + Id + " - year out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalYearEnd = optionalYearEndInt;
    }

    public void setDaysOfWeek(int days) {
        __daysOfWeek = days;
    }

    public boolean doesMeet(int days) {
        return (__daysOfWeek & days) != 0;
    }

    public int getDaysOfWeek() {
        return __daysOfWeek;
    }

    /**
     * Get method for the offering's optional hour start
     * @return optionalHourStart
     */
    public int getOptionalHourStart() {
        return optionalHourStart;
    }
    /**
     * Set method for the offering's optional hour start
     * @param optionalHourStartInt corresponds to the optional hour start
     * for the offering (1-12)
     * @throws IllegalArgumentException if the hour specified is not in [1..12]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalHourStart(int optionalHourStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalHourStartInt != DEFAULT_VALUE &&
            (optionalHourStartInt < 1 || optionalHourStartInt > 12)) {
            String msg = "Unable to set the optional Hour Start for " +
            "offeringId=" + Id + " - hour out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalHourStart = optionalHourStartInt;
    }
    /**
     * Get method for the offering's optional minute start
     * @return optionalMinuteStart
     */
    public int getOptionalMinuteStart() {
        return optionalMinuteStart;
    }
    /**
     * Set method for the offering's optional minute start
     * @param optionalMinuteStartInt corresponds to the optional
     * minute start for the offering to the nearest five
     * minute mark (i.e. 0, 5, 10, 15, ..., 55)
     * @throws IllegalArgumentException if the minute specified is not in [0..59]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalMinuteStart(int optionalMinuteStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalMinuteStartInt != DEFAULT_VALUE &&
            (optionalMinuteStartInt < 0 || optionalMinuteStartInt > 59)) {
            String msg = "Unable to set the optional Minute Start for " +
            "offeringId=" + Id + " - minute out of range!";
            throw new IllegalArgumentException(msg);
        }
        // If the optionalMinuteStartInt is not an even multiple of 5, then
        // we essentially round it down to the nearset multiple of 5 (a
        // modified floor function, so to speak)
        while ((optionalMinuteStartInt % 5) != 0) {
            optionalMinuteStartInt--;
        }
        this.optionalMinuteStart = optionalMinuteStartInt;
    }
    /**
     * Get method for the offering's optional AM/PM start
     * @return optionalAmPmStart (1 for AM, 2 for PM)
     */
    public int getOptionalAmPmStart() {
        return optionalAmPmStart;
    }
    /**
     * Set method for the offering's optional AM/PM start
     * @param optionalAmPmStartInt corresponds to the optional AM/PM
     * start for the offering (1 for AM, 2 for PM)
     * @throws IllegalArgumentException if the parameter specified is not in [1,2]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalAmPmStart(int optionalAmPmStartInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalAmPmStartInt != DEFAULT_VALUE &&
            (optionalAmPmStartInt < 1 || optionalAmPmStartInt > 2)) {
            String msg = "Unable to set the optional AM/PM Start for " +
            "offeringId=" + Id + " - AM/PM out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalAmPmStart = optionalAmPmStartInt;
    }
    /**
     * Get method for the offering's optional hour end
     * @return optionalHourEnd
     */
    public int getOptionalHourEnd() {
        return optionalHourEnd;
    }
    /**
     * Set method for the offering's optional hour end
     * @param optionalHourEndInt corresponds to the optional hour
     * end for the offering (1-12)
     * @throws IllegalArgumentException if the hour specified is not in [1..12]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalHourEnd(int optionalHourEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalHourEndInt != DEFAULT_VALUE &&
            (optionalHourEndInt < 1 || optionalHourEndInt > 12)) {
            String msg = "Unable to set the optional Hour End for " +
            "offeringId=" + Id + " - hour out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalHourEnd = optionalHourEndInt;
    }
    /**
     * Get method for the offering's optional minute end
     * @return optionalMinuteEnd
     */
    public int getOptionalMinuteEnd() {
        return optionalMinuteEnd;
    }
    /**
     * Set method for the offering's optional minute end
     * @param optionalMinuteEndInt corresponds to the optional minute
     * end for the offering to the nearest five
     * minute mark (i.e. 0, 5, 10, 15, ..., 55)
     * @throws IllegalArgumentException if the minute specified is not in [0..59]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalMinuteEnd(int optionalMinuteEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalMinuteEndInt != DEFAULT_VALUE &&
            (optionalMinuteEndInt < 0 || optionalMinuteEndInt > 59)) {
            String msg = "Unable to set the optional Minute End for " +
            "offeringId=" + Id + " - minute out of range!";
            throw new IllegalArgumentException(msg);
        }
        // If the optionalMinuteEndInt is not an even multiple of 5, then
        // we essentially round it down to the nearset multiple of 5 (a
        // modified floor function, so to speak)
        while ((optionalMinuteEndInt % 5) != 0) {
            optionalMinuteEndInt--;
        }
        this.optionalMinuteEnd = optionalMinuteEndInt;
    }
    /**
     * Get method for the offering's optional AM/PM end
     * @return optionalAmPmEnd (1 for AM, 2 for PM)
     */
    public int getOptionalAmPmEnd() {
        return optionalAmPmEnd;
    }
    /**
     * Set method for the offering's optional AM/PM end
     * @param optionalAmPmEndInt corresponds to the optional AM/PM
     * end for the offering (1 for AM, 2 for PM)
     * @throws IllegalArgumentException if the parameter specified is not in [1,2]
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalAmPmEnd(int optionalAmPmEndInt)
    throws OperationFailedException, IllegalArgumentException {
        if (optionalAmPmEndInt != DEFAULT_VALUE &&
            optionalAmPmEndInt != 1 && optionalAmPmEndInt != 2) {
            String msg = "Unable to set the optional AM/PM End for " +
            "offeringId=" + Id + " - AM/PM out of range!";
            throw new IllegalArgumentException(msg);
        }
        this.optionalAmPmEnd = optionalAmPmEndInt;
    }
    /**
     * Get method for the offering's optional Location string
     * @return optionalLocation
     */
    public String getOptionalLocation() {
        return optionalLocation;
    }
    /**
     * Set method for the offering's optional Location string
     * @param optionalLocationString corresponds to the optional
     * Location for the offering
     * @throws OperationFailedException if the update fails
     */
    public void setOptionalLocation(String optionalLocationString)
    throws OperationFailedException {
        this.optionalLocation = optionalLocationString;
    }
    //*********************************************************************
}
