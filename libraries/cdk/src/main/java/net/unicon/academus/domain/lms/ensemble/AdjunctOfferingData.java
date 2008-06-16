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

package net.unicon.academus.domain.lms.ensemble;

/**
 * An abstraction for adjunct offering data required by
 * Ensemble.
 */
public class AdjunctOfferingData {

    private long offeringId;
    private boolean published;
    private boolean hasBuyNowEnabled;

    /**
     * Default constructor for AdjunctOfferingData.
     * @param published Determines if offering is published
     * in the course catalog.
     * @param hasBuyNowEnabled Determines if the offering
     * will be available for purchase via a "Buy Now"
     * feature.
     */
    public AdjunctOfferingData (long offeringId,
            boolean published, boolean hasBuyNowEnabled) {

        if (offeringId < 1) {
                throw new IllegalArgumentException(
                        "Offering id cannot be less than 1.");
        }

        this.offeringId = offeringId;
        this.published = published;
        this.hasBuyNowEnabled = hasBuyNowEnabled;
    }

    /**
     * Returns the offering of the offering this
     * AdjunctOfferingData instance is associated with.
     * @return The offering id of this instance.
     */
    public long getOfferingId() {

        return this.offeringId;
    }

    /**
     * Determines if offering is published in the course catalog.
     * @return Returns true if offering is published, false
     * otherwise.
     */
    public boolean isPublished() {

        return this.published;
    }

    /**
     * Determines if offering has "Buy Now" feature enabled.
     * @return Returns true if offering has "Buy Now" enabled,
     * false otherwise.
     */
    public boolean hasBuyNowEnabled() {

        return this.hasBuyNowEnabled;
    }
}
