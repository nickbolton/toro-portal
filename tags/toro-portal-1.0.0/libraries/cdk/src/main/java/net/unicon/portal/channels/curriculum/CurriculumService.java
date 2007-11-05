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

package net.unicon.portal.channels.curriculum;

import java.sql.*;

import java.util.List;



import net.unicon.academus.delivery.DeliveryCurriculum;
import net.unicon.academus.delivery.virtuoso.content.IContentGroup;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;

public interface CurriculumService {

    public List getAvailableCurriculum(IContentGroup[] cGroups, User user);

    public List getCurriculum (Offering offering, User user, Connection conn);

    public List getCurriculum (String type, Offering offering, User user, Connection conn);

    public List getCurriculum (Offering offering, User user, boolean convertReference,
            													Connection conn);
    
    public List getCurriculum (Offering offering, User user, boolean convertReference,
                                                                Connection conn, boolean viewInstructorNotes);

    public List getCurriculum (String type, Offering offering, User user,
                            boolean convertReference, Connection conn);

    public DeliveryCurriculum getDeliveryCurriculum(String deliverySystemID,
                            String deliveryCurriculumID, Connection conn);

    public void saveCurriculum(String name, String description, String type,
                                String reference, String contentType,
                                Offering offering, String theme,
                                String style, Connection conn);

    public void deleteCurriculum(int curriculm_id, Offering offering, Connection conn);

    public void deleteOfferingCurriculum(Offering offering, Connection conn);

    public String getCurriculumLink(String reference, User user, Offering o, Connection conn, boolean viewInstructorNotes);

    /**
     *     This method adds course information to the table tracking the set of delivery courses.
     *     @return the number of successfully added courses
     */
    public int addDeliveryCourses(List contentList);

    /**
     *     This method updates course information to the table tracking the set of delivery courses.
     *     @return the number of successfully updated courses
     */
    public int updateDeliveryCourses(List contentList);

    /**
     *     This method deletes course information in the table tracking the set of delivery courses.
     *     @return the number of successfully deleted courses
     */
    public int deleteDeliveryCourses(List contentList);

    /**
     *     This method deletes course information in the table tracking
     * the set of delivery courses from a particular delivery system.
     *     @return the number of successfully deleted courses
     */

    public int deleteDeliveryCourses(String deliverySystem);

    /**
     *     This method deletes all course information in the table tracking the set of delivery courses.
     *     @return the number of successfully deleted courses
     */
    public int deleteDeliveryCourses();

}

