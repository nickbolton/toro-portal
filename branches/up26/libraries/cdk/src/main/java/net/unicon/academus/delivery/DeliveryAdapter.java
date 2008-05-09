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

package net.unicon.academus.delivery;

import java.util.List;


import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.SearchCriteria;
import net.unicon.academus.delivery.Results;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.portal.channels.curriculum.Curriculum;
import net.unicon.portal.channels.gradebook.IAdjunctAgent;
import net.unicon.portal.common.service.activation.Activation;


public interface DeliveryAdapter {

    /* Different Query Types */
    public static final String ITEM_INFORMATION_PAGE = "IIP";
    public static final String PERSONALIZED_FEEDBACK = "PersonalizedFeedback";
    public static final String CONTENT               = "Content";
    public static final String ASSESSMENT_LIST       = "AssessmentList";
    public static final String PROFICIENCY_REPORT    = "ProficiencyReport";

    /**
     * Gets a list of All the courses available on the Delivery System
     *
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>net.unicon.portal.channels.curriculum.Curriculum</code>
     *
     * @see net.unicon.academus.domain.lms.User
     * @see net.unicon.portal.channels.curriculum.Curriculum
     */

    public List getAllCourses(String[] contentGroups, User user);

    /**
     * Gets a list of All the courses available on the Delivery System
     *
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>net.unicon.portal.channels.curriculum.Curriculum</code>
     *
     * @see net.unicon.academus.domain.lms.User
     * @see net.unicon.portal.channels.curriculum.Curriculum
     */

    public List getAllCourses(String[] contentGroups, String username);

    /**
     * Gets a list of the courses available on the Delivery System with a particular id.
     * @return <code>Curriculum</code> - curriculum object
     * @param  <code>User</code> Academus domain user user
     * @return <code>List</code> of <code>net.unicon.portal.channels.curriculum.Curriculum</code>
     * @see net.unicon.academus.domain.lms.User;
     * @see net.unicon.portal.channels.curriculum.Curriculum;
     */

    public List getCourses(Curriculum curriculum, User user);

    /**
     * Gets a list of the courses available on the Delivery System with a particular id.
     * @return <code>Curriculum</code> - curriculum object
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>net.unicon.portal.channels.curriculum.Curriculum</code>
     * @see net.unicon.portal.channels.curriculum.Curriculum;
     */

    public List getCourses(Curriculum curriculum, String username);

    /**
     * Get a list of assessments based on a list of courses or curriculum
     * informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Curriculum</code> objects
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>net.unicon.academus.delivery.Assessment</code>
     *
     * @see net.unicon.academus.delivery.Assessment
     * @see net.unicon.portal.channels.curriculum.Curriculum
     * @see net.unicon.academus.domain.lms.User
     */
    public List getAssessmentList(List curriculumObjectList, User user);

    /**
     * Get a list of assessments based on a list of courses or curriculum
     * informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Curriculum</code> objects
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>net.unicon.academus.delivery.Assessment</code>
     *
     * @see net.unicon.academus.delivery.Assessment
     * @see net.unicon.portal.channels.curriculum.Curriculum
     */
    public List getAssessmentList(List curriculumObjectList, String username);

    /**
     * Get a list of assessments based on a list of assessment objects
     * informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>boolean</code> include sub versions of the assessment
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>net.unicon.academus.delivery.Assessment</code>
     *
     * @see net.unicon.academus.delivery.Assessment
     * @see net.unicon.academus.domain.lms.User
     */
    public Assessment getAssessment(String assessmentId, boolean withForms, User user);

    /**
     * Get a list of assessments based on a list of assessment objects
     * informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>boolean</code> include sub versions of the assessment
     * @param  <code>User</code> user domain object
     * @param  <code>String</code> username
     *
     * @see net.unicon.academus.delivery.Assessment
     * @see net.unicon.academus.domain.lms.User
     */

    public Assessment getAssessment(String assessmentId, boolean withForms, String username);

    public Assessment getAssessment(String id, String username, boolean includeForms, boolean includeSMVs, boolean includeFTItems, boolean includeDisabledSMVs);

    /**
     * Getting the reference to a link
     * @param  <code>User</code> user domain object
     * @return <code>ReferenceObject</code> Reference object
     * @see net.unicon.academus.domain.lms.User
     */
    public ReferenceObject getReferenceLink(
    EntityObject obj,
    Curriculum curriculum,
    String type,
    User user, Offering o) throws DeliveryException;

    /**
     * Getting the reference to a link
     * @param  <code>User</code> user domain object
     * @return <code>ReferenceObject</code> Reference object
     * @see net.unicon.academus.domain.lms.User;
     */
/*    public ReferenceObject getReferenceLink(
    EntityObject obj,
    String referenceStr,
    String type,
    User user);*/

    /**
     * Getting the reference to a link
     * @param  <code>User</code> user domain object
     * @return <code>ReferenceObject</code> Reference object
     * @see net.unicon.academus.domain.lms.User;
     */
    public ReferenceObject getReferenceLink(
                        EntityObject obj,
                        String referenceStr,
                        String type,
                        User user,
                        Offering o) throws DeliveryException;

    /**
     * Getting the reference to a link for content with Instructor Notes
     * @param  <code>User</code> user domain object
     * @return <code>ReferenceObject</code> Reference object
     * @see net.unicon.academus.domain.lms.User;
     */
    public ReferenceObject getReferenceLink(
                        EntityObject obj,
                        String referenceStr,
                        String type,
                        User user,
                        Offering o,
                        boolean viewInstructorNotes) throws DeliveryException;

    /**
     * Obtains the activation agent for this assessment provider.
     *
     * @return A gradebook activation agent.
     */
    IAdjunctAgent getActivationAgent();

    /**
     * Creates on online activation.
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>User</code> user domain object
     *
     * @return <code>boolean</code> true or false success
     *
     * @see net.unicon.portal.common.service.activation.Activation
     * @see net.unicon.academus.domain.lms.User
     */
    public boolean createActivation(Activation activation, IDecisionCollection dc, User user ) throws DeliveryException;

    /**
     * Creates on online activation.
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>String</code> username
     *
     * @return <code>boolean</code> true or false success
     *
     * @see net.unicon.portal.common.service.activation.Activation
     * @see net.unicon.academus.domain.lms.User
     */
    public boolean createActivation(Activation activation, IDecisionCollection dc, String username ) throws DeliveryException;

    /**
     * Retrieves the assessment results
     *
     * @param  resultID ...
     * @param  user An Academus user.
     * @param  useTheme Name of the theme to use.
     * @param  useStyle Name of the style to use.
     *
     * @return <code>Results</code> - the assessment results
     *
     * @see net.unicon.academus.delivery.Results
     * @see net.unicon.academus.domain.lms.User
     */
    public Results getAssessmentResults(String resultID, User user, String useTheme, String useStyle);

    /**
     * Retrieves the assessment results
     *
     * @param  <code>String</code> username
     * @param  <code>String</code> username
     * @param  useTheme Name of the theme to use.
     * @param  useStyle Name of the style to use.
     *
     * @return <code>Results</code> - the assessment results
     *
     * @see net.unicon.academus.delivery.Results
     * @see net.unicon.academus.domain.lms.User
     */
    public Results getAssessmentResults(String resultID, String username, String useTheme, String useStyle);

    /**
     * Deactivate on online activation.
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>User</code> user domain object
     *
     * @return <code>boolean</code> true or false success
     *
     * @see net.unicon.portal.common.service.activation.Activation
     * @see net.unicon.academus.domain.lms.User
     */
    public boolean deactivateAssessments(Activation activation, User user);

    /**
     * Deactivate on online activation.
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>String</code> username
     *
     * @return <code>boolean</code> true or false success
     *
     * @see net.unicon.portal.common.service.activation.Activation
     * @see net.unicon.academus.domain.lms.User
     */
    public boolean deactivateAssessments(Activation activaiton, String username);

    /**
     * Find a set of curriculum based on a set of search criteria
     *
     * @param <code>SearchCriteria</code>
     *
     * @return a <code>List</code> with the curriculum that met the criteria
     */
    public List findCurriculum(SearchCriteria criteria);

    /**
     * Find a set of assessment based on a set of search criteria
     *
     * @param <code>SearchCriteria</code>
     *
     * @return a <code>List</code> with the assessment that met the criteria
     */
    public List findAssessments(SearchCriteria criteria, User user, int contextId);
}

