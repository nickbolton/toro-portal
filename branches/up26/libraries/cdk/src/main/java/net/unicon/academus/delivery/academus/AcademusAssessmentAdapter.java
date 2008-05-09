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
package net.unicon.academus.delivery.academus;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;





import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.SearchCriteria;
import net.unicon.academus.delivery.Assessment;
import net.unicon.academus.delivery.AssessmentForm;
import net.unicon.academus.delivery.AssessmentFormImpl;
import net.unicon.academus.delivery.AssessmentImpl;
import net.unicon.academus.delivery.DeliveryAdapter;
import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.delivery.Question;
import net.unicon.academus.delivery.QuestionImpl;
import net.unicon.academus.delivery.ReferenceObject;
import net.unicon.academus.delivery.ReferenceObjectImpl;
import net.unicon.academus.delivery.Results;
import net.unicon.academus.delivery.ResultsImpl;
import net.unicon.academus.domain.assessment.*;
import net.unicon.academus.domain.lms.*;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.portal.common.service.activation.Activation;
import net.unicon.portal.channels.curriculum.Curriculum;
import net.unicon.portal.channels.gradebook.IAdjunctAgent;

public class AcademusAssessmentAdapter implements DeliveryAdapter {

    public AcademusAssessmentAdapter() {}

    /**
     * Gets a list of All the courses available on the
     * Delivery System
     *
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>Curriculum</code>
     * @see net.unicon.academus.domain.lms.User;
     * @see Curriculum;
     */
    public List getAllCourses(String[] contentGroups, User user) {
        return new ArrayList();
    }

    /**
     * Gets a list of All the courses available on the
     * Delivery System
     *
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>Curriculum</code>
     * @see net.unicon.academus.domain.lms.User;
     * @see Curriculum;
     */

    public List getAllCourses(String[] contentGroups, String username) {
        return new ArrayList();
    }

    /**
     * Gets a list of the courses available on the Delivery
     * System with a particular id.
     *
     * @return <code>Curriculum</code> - curriculum object
     * @param  <code>User</code> Academus domain user user
     * @return <code>List</code> of <code>Curriculum</code>
     * @see net.unicon.academus.domain.lms.User;
     * @see Curriculum;
     */

    public List getCourses(Curriculum curriculum, User user) {
        return new ArrayList();
    }

    /**
     * Gets a list of the courses available on the Delivery
     * System with a particular id.
     *
     * @return <code>Curriculum</code> - curriculum object
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>Curriculum</code>
     * @see Curriculum;
     */

    public List getCourses(Curriculum curriculum, String username) {
        return new ArrayList();
    }

    /**
     * Get a list of assessments based on a list of courses or
     * curriculum informat passed in as parameters.
     *
     * @param  <code>List</code> of <code>Curriculum</code> objects
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>Assessment</code>
     * @see <{Assessment}>
     * @see Curriculum;
     * @see User;
     */

    public List getAssessmentList(List curriculumObjectList, User user) {

        return getAssessmentList(curriculumObjectList, user.getUsername());

    }

    /**
     * Get a list of assessments based on a list of courses or
     * curriculum informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Curriculum</code> objects
     * @param  <code>String</code> username
     * @return <code>List</code> of <code>Assessment</code>
     * @see <{Assessment}>
     * @see Curriculum;
     */

    public List getAssessmentList(List curriculumObjectList, String username) {
        // We currently do not use a curriculum list to get the
        // appropiate assessment objects.

        List assessmentList = AssessmentFactory.getAssessments();
        List rtnList = new ArrayList();

        net.unicon.academus.domain.assessment.Assessment tempAss = null;
        for (int ix = 0; ix < assessmentList.size(); ++ix) {
            tempAss = (net.unicon.academus.domain.assessment.Assessment) assessmentList.get(ix);
            rtnList.add(convertAssessment(tempAss));
        }

        return rtnList;
    }

    /**
     * Get a list of assessments based on a list of assessment
     * objects informat passed in as parameters
     *
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>boolean</code> include sub versions of the assessment
     * @param  <code>User</code> user domain object
     * @return <code>List</code> of <code>Assessment</code>
     * @see <{Assessment}>
     * @see User;
     */
    public Assessment getAssessment(String id, boolean withForms, User user) {
        return getAssessment(id, withForms, user.getUsername());
    }

    /**
     * Get a list of assessments based on a list of assessment
     * objects informat passed in as parameters
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>boolean</code> include sub versions of the assessment
     * @param  <code>User</code> user domain object
     * @param  <code>String</code> username
     * @see <{Assessment}>
     * @see User;
     */
    public Assessment getAssessment(String id, boolean withForms, String username) {

        return convertAssessment(AssessmentFactory.getAssessment(id));
    }

    public Assessment getAssessment(String id, String username, boolean includeForms, boolean includeSMVs, boolean includeFTItems, boolean includeDisabledSMVs) {
        return convertAssessment(AssessmentFactory.getAssessment(id));
    }

    /**
     * Getting the reference to a link
     * @param  <code>User</code> user domain object
     * @see User;
     */

    public ReferenceObject getReferenceLink(EntityObject portalObj, Curriculum curriculum,
                String type, User user, Offering o) throws DeliveryException {
        return getReferenceLink( portalObj, curriculum.getReference(), type, user, o);
    }

    /**
     * Getting the reference to a link
     * @param  <code>User</code> user domain object
     * @see User;
     */
    public ReferenceObject getReferenceLink(
                                EntityObject portalObj,
                                String referenceStr,
                                String type,
                                User user, Offering o) throws DeliveryException {
        ReferenceObject referenceLink = null;

        if (type.equals(ASSESSMENT_LIST) && referenceStr != null) {
            referenceLink = (ReferenceObject) new ReferenceObjectImpl(
                                    ReferenceObject.DEFAULT ,
                                    "To Assessment",
                                    ReferenceObject.SELF);

            Map params = new HashMap();
            params.put("uP_root", "me");
            params.put("focusedChannel", "");

            referenceLink.setParameters(params);
        } else {
            referenceLink = (ReferenceObject) new ReferenceObjectImpl(
                                    null,
                                    "Personalized Feedback",
                                    ReferenceObject.SELF);
        }

        return referenceLink;
    }

    public ReferenceObject getReferenceLink(
            EntityObject obj,
            String referenceStr,
            String type,
            User user,
            Offering o,
            boolean viewInstructorNotes) throws DeliveryException {
        return getReferenceLink( obj, referenceStr, type, user, o);
    }
/*
    public ReferenceObject getReferenceLink(
                                EntityObject portalObj,
                                String referenceStr,
                                String type,
                                User user, String useTheme, String useStyle) {
        return getReferenceLink(portalObj, referenceStr, type, user, useTheme, useStyle);
    }
*/

    public IAdjunctAgent getActivationAgent() {
throw new UnsupportedOperationException("Needs implementation...");
    }

    /**
     * Creates on online activation.
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>User</code> user domain object
     * @return <code>boolean</code> true or false success
     * @see <{Activation}>
     * @see User;
     */

    public boolean createActivation(Activation activation, IDecisionCollection dc, User user) throws DeliveryException {
        return createActivation(activation, dc, user.getUsername());
    }

    /**
     * Creates on online activation.
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>String</code> username
     * @return <code>boolean</code> true or false success
     * @see <{Activation}>
     * @see User;
     */

    public boolean createActivation(Activation activation, IDecisionCollection dc, String username) throws DeliveryException {
        return true;
    }

    public List getAssessmentResults(List assessmentResultQuery, User user, String useTheme, String useStyle) {
        return getAssessmentResults(assessmentResultQuery, user.getUsername(), useTheme, useStyle);
    }

    /**
     * Deactivate on online activation.
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>User</code> user domain object
     * @return <code>boolean</code> true or false success
     * @see <{Activation}>
     * @see <{User}>
     */

    public boolean deactivateAssessments(Activation activation, User user) {
        return true;
    }

    /**
     * Deactivate on online activation.
     * @param  <code>List</code> of <code>Assessment</code> objects
     * @param  <code>String</code> username
     * @return <code>boolean</code> true or false success
     * @see <{Activation}>
     * @see <{User}>
     */

    public boolean deactivateAssessments(Activation activation, String username) {
        return true;
    }

    /**
     * Retrieves the assessment results
     * @param  <code>String</code> result reference identification
     * @param  <code>User</code> user domain object
     * @return <code>Results</code> - the assessment results
     * @see <{Results}>
     * @see <{User}>
     */

    public Results getAssessmentResults(String resultID, User user, String useTheme, String useStyle) {

        return getAssessmentResults(resultID, user.getUsername(), useTheme, useStyle);

    }

    /**
     * Retrieves the assessment results
     * @param  <code>String</code> result reference identification
     * @param  <code>String</code> username
     * @return <code>Results</code> - the assessment results
     * @see <{Results}>
     * @see <{User}>
     */

    public Results getAssessmentResults(String resultID, String username, String useTheme, String useStyle) {

        Results results = null;
        if (resultID != null) {
            try {
                int rID = Integer.parseInt(resultID);

                AssessmentInstance assessment
                        = AssessmentInstanceFactory.getAssessmentResults(rID);

                List questionList = new ArrayList();
                List qIList = assessment.getQuestionInstances();

                QuestionInstance qi = null;
                for (int ix = 0; ix < qIList.size(); ++ix) {
                    qi = (QuestionInstance) qIList.get(ix);

                    questionList.add(new QuestionImpl(
                                            qi.getId(),
                                            qi.getTitle(),
                                            ix,
                                            qi.getMaxScore(),
                                            qi.getMinScore(),
                                            (int) qi.getScore())
                                    );
                }

                results = (Results) new ResultsImpl(
                                        assessment.getId(),
                                        assessment.getActivationID(),
                                        questionList);
                results.setScore((int)assessment.getTotalScore());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public List getAssessmentResults(List assessmentResultQuery, String username, String useTheme, String useStyle) {
        return new ArrayList();
    }

    private net.unicon.academus.delivery.Assessment convertAssessment(net.unicon.academus.domain.assessment.Assessment assessment) {
        net.unicon.academus.delivery.Assessment rtnAssessment = null;

        if (assessment != null) {

            // If the AssessmentImpl required the min and max
            // then it should make that part of its contructor
            // or factory method
            Map formAttr = new HashMap();
            formAttr.put("maxScore",""+assessment.getMaxScore());
            formAttr.put("minScore",""+assessment.getMinScore());

            List questionlist = new ArrayList();
            List qIList = assessment.getQuestions();

            net.unicon.academus.domain.assessment.Question qi = null;
            for (int ix = 0; ix < qIList.size(); ++ix) {
                qi = (net.unicon.academus.domain.assessment.Question) qIList.get(ix);

                questionlist.add(new QuestionImpl(
                                            qi.getId(),
                                            qi.getTitle(),
                                            ix,
                                            qi.getMaxScore(),
                                            qi.getMinScore())
                                    );
            }


            AssessmentForm form = (AssessmentForm) new AssessmentFormImpl(
                                    assessment.getId(),
                                    assessment.getTitle(),
                                    assessment.getDescription(),
                                    questionlist,
                                    formAttr
                                    );
            List formList = new ArrayList();
            formList.add(form);
            rtnAssessment = (net.unicon.academus.delivery.Assessment) new AssessmentImpl(
                                                assessment.getId(),
                                                assessment.getTitle(),
                                                "en",
                                                assessment.getDescription(),
                                                formList);
        }
        return rtnAssessment;
    }
    /**
     * Find a set of curriculum based on a set of search criteria
     *
     * @param <code>SearchCriteria</code>
     *
     * @return a <code>List</code> with the curriculum that met the criteria
     */
    public List findCurriculum(SearchCriteria criteria) {
        return null;
    }

    /**
     * Find a set of assessment based on a set of search criteria
     *
     * @param <code>SearchCriteria</code>
     *
     * @return a <code>List</code> with the assessment that met the criteria
     */
    public List findAssessments(SearchCriteria criteria, User user, int contextID) {

        List rtnAssessment = AssessmentFactory.findAssessment(criteria);

        for (int ix = 0; ix < rtnAssessment.size(); ++ix) {
            rtnAssessment.set(
                ix,
                this.convertAssessment(
                    (net.unicon.academus.domain.assessment.Assessment)
                        rtnAssessment.get(ix)));
        }
        return rtnAssessment;
    }

    public static void main (String [] args) {
        System.exit(0);
    }

}

