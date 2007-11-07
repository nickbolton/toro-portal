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

import java.sql.Connection;
import java.util.List;

import net.unicon.academus.common.SearchCriteria;
import net.unicon.academus.delivery.Assessment;
import net.unicon.academus.delivery.AssessmentList;
import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.sdk.catalog.IFilterMode;

public interface GradebookAssessmentService {

    /**
     * <p>
     * Get a list of assessment based on the curriculum.
     * </p><p>
     *
     * @param user - a domain user object.
     * @param a List with all the curriculum
     * </p><p>
     *
     * @return List with the assessments.
     * </p>
     */
    public List getAvailableAssessments(
                                User user,
                                List curriculumList);

    /**
     * <p>
     * </p><p>
     *
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * </p><p>
     *
     * @return success - returns a boolean if successful.
     * </p><p>
     *
     * @see net.unicon.portal.domain.Offering
     * </p>
     */
    public List getGBItemQuestionDetails(
                                int gbItem,
                                String username,
                                User user,
                                Offering offering,
                                Connection conn) throws DeliveryException;

    /**
     * <p>
     * </p><p>
     *
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * </p><p>
     *
     * @return success - returns a boolean if successful.
     * </p><p>
     *
     * @see net.unicon.portal.domain.Offering
     * </p>
     */
    public List getGBItemQuestionDetails(
                                int gbItem,
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                Connection conn) throws DeliveryException;

    /**
     * <p>
     * </p><p>
     *
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * </p><p>
     *
     * @return success - returns a boolean if successful.
     * </p><p>
     *
     * @see net.unicon.portal.domain.Offering
     * </p>
     */
    public List getGBItemQuestionDetails(
                                int gbItem,
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn) throws DeliveryException;

    /**
     * <p>
     * </p><p>
     *
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * </p><p>
     *
     * @return success - returns a boolean if successful.
     * </p><p>
     *
     * @see net.unicon.portal.domain.Offering
     * </p>
     */
    public AssessmentList getAssessmentList(
                                User user,
                                Offering offering,
                                Connection conn);

    /**
     * <p>
     * </p><p>
     *
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * </p><p>
     *
     * @return success - returns a boolean if successful.
     * </p><p>
     *
     * @see net.unicon.portal.domain.Offering
     * </p>
     */
    public Assessment getAssessment (
                                User user,
                                Offering offering,
                                String assessmentID,
                                boolean withForms,
                                Connection conn);

    public void updateAssessmentSubmission(
                                String username,
                                String resultID);

    public AssessmentList findAssessment(
                                SearchCriteria criteria,
                                User user,
                                Offering offering) throws Exception;

    /**
     * <p>
     * Add the given list of assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int addDeliveryAssessments(List assessmentList, String systemID);

    /**
     * <p>
     * Update the given list of assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int updateDeliveryAssessments(List assessmentList, String systemID);

    /**
     * <p>
     * Delete the given list of assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a List with a list of assessment ids.
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteDeliveryAssessments(List contentList, String systemID);

    /**
     * <p>
     * Deletes all of the delivery assessments for a given
     * delivery system.
     * </p><p>
     *
     * @param a String with the system ID of the delivery system.
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteDeliveryAssessments(String systemID);

    /**
     * <p>
     * Deletes all of the delivery assessments in the
     * database regardless of the system ID
     * </p><p>
     *
     * @return a <code>int</code> with a status code.
     * </p>
     */
    public int deleteAllDeliveryAssessments();
} // end GradebookAssessmentService

