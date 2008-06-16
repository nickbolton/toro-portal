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

package net.unicon.portal.channels.gradebook;

import java.util.List;

import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.PortalObject;
import net.unicon.portal.channels.gradebook.GradebookSubmission;
import net.unicon.portal.channels.gradebook.GradebookFeedback;

public interface GradebookScore extends PortalObject {

    /** Get id of the gradebook score */
    public int getID();

    /** Get id of the associated gradebook item */
    public int getGradebookItemID();

    /** Get the username of the user who owns this score */
    public String getUsername();

    /** Get the score of gradebook item */
    public int getScore();

    /** Get the user associated with this score */
    public User getUser();

    /** Set the user associated with this score */
    public void setUser(User user);

    /** Set the if the user's score is hidden or not to be displayed in the gradebook */
    public void setHidden(boolean isHidden);

    /** Set the Submission information */
    public void setGradebookSubmission(GradebookSubmission s);

    /** Get the Submission information */
    public GradebookSubmission getGradebookSubmission();

    /** Set the the Feedback based on this score */
    public void setFeedback(GradebookFeedback f);

    /** Get the the Feedback based on this score */
    public GradebookFeedback getFeedback();

    /** Get the orginal score **/
    public int getOriginalScore();
}

