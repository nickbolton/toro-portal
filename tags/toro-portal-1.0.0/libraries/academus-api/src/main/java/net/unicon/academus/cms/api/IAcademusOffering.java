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

package net.unicon.academus.cms.api;

import java.util.Set;

/**
 *   An interface describing an Academus Offering instance.
 */
public interface IAcademusOffering {

    /**
     * The offering id of this offering.
     * @return The offering id.
     */
    public long getOfferingId ();

    /**
     * The offering name of this offering.
     * @return The offering name.
     */
    public String getOfferingName();

    /**
     * The offering description of this offering.
     * @return The offering description.
     */
    public String getOfferingDescription();

    /**
     * The offering location of this offering.
     * @return The offering location.
     */
    public String getLocation ();

    /**
     * The end time of this offering in the form of HH:MM AM/PM.
     * @return The offering end time.
     */
    public String getOfferingEndTime();

    /**
     * The start time of this offering in the form of HH:MM AM/PM.
     * @return The offering start time.
     */
    public String getOfferingStartTime();

    /**
     * The days during which the offering is offered in the
     * following format:
     * M,T,W,Th,F,Sa,Su
     *
     * @return The days this offering is offered.
     */
    public String getOfferingWeekDays();

    /**
     * The start date of this offering.
     * @return The offering start date.
     */
    public String getOfferingStartDate();

    /**
     * The end date of this offering.
     * @return The offering end date.
     */
    public String getOfferingEndDate();

    /**
     * Determines if offering is published in the
     * course catalog.
     * @return Returns true if offering is published,
     * false otherwise.
     */
    public boolean isPublished();

    /**
     * Determines if offering has "Buy Now" enabled.
     * @return Returns true if offering has "Buy Now"
     * enabled, false otherwise.
     */
    public boolean isBuyNowEnabled();

    /**
     * Returns an id set of topics this offering belongs to.
     * @return A set of topic ids.
     */
    public Set getTopicIds();


}