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

import net.unicon.academus.domain.ItemNotFoundException;

public final class TopicType {
    /* Static Members */
    /** Topics of this type are academic courses. */
    public static final TopicType ACADEMICS = new TopicType("Academics");
    /** Topics of this type are communities of users with a common interest. */
    public static final TopicType COMMUNITIES_OF_INTEREST =
    new TopicType("CommunitiesOfInterest");
    /* Instance Members */
    private String label = null;
    private TopicType(String label) {
        this.label = label;
    }
    public static TopicType getTopicType(String topicType)
    throws ItemNotFoundException {
        if (ACADEMICS.toString().equalsIgnoreCase(topicType)) {
            return ACADEMICS;
        }
        if (COMMUNITIES_OF_INTEREST.toString().equalsIgnoreCase(topicType)) {
            return COMMUNITIES_OF_INTEREST;
        }
        throw new ItemNotFoundException(
        "Could not find Topic Type: " + topicType);
    }
    /**
     * Provides the label for this topic.  This information is useful for display purposes.
     * @return the topic type in string form.
     */
    public String toString() {
        return label;
    }
}
