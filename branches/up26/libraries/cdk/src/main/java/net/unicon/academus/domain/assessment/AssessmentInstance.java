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
package net.unicon.academus.domain.assessment;

import java.util.Collections;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

import net.unicon.academus.common.XMLAbleEntity;

/**
 * <p>
 *
 * </p>
 */
public class AssessmentInstance implements Cloneable, XMLAbleEntity {

    private AssessmentResults results;
    private Assessment assessment;
    private List questionInstances; // of type QuestionInstance

    public AssessmentInstance(String assessmentId) {
        this(AssessmentFactory.getAssessment(assessmentId));

    }

    public AssessmentInstance(Assessment assessment) {
        this.assessment = assessment;
        List questions  = assessment.getQuestions();
        questionInstances = new ArrayList();

        for(int ix = 0; ix < questions.size(); ++ix) {
            String questId = ((Question) questions.get(ix)).getId();
            questionInstances.add(QuestionInstanceFactory.getQuestionInstance(questId));
        }
        this.results = new AssessmentResults();
    }

    /**
     * <p>
     * Make an exact copy of this Assessment Instance Object.
     * </p><p>
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public Object clone() throws CloneNotSupportedException {
        AssessmentInstance cloneCopy = null;
        try {
            cloneCopy = (AssessmentInstance) super.clone();
            //Cloning Assessment Results
            cloneCopy.results = (AssessmentResults) results.clone();

            //Cloning QuestionInstances
            if (questionInstances != null) {
                List cloneQuestInstances = new ArrayList();
                
                for (int ix = 0; ix < questionInstances.size(); ++ix) {
                    cloneQuestInstances.add(
                        ((QuestionInstance)questionInstances.get(ix)).clone());
                }
                cloneCopy.questionInstances = cloneQuestInstances;
            }

            // Assessment object will NOT be cloned because
            // its a singleton.  With the inherited clone
            // method, the new cloned object will have a
            // reference to it.
        } catch (CloneNotSupportedException cnse) {
            String message = "Unable to close the Assessment Instance";
            throw new CloneNotSupportedException(message);
        }
        return cloneCopy;
    } // end clone

    /**
     * <p>
     * Returns a Question Instance based on its question id.
     * </p><p>
     *
     * @return a QuestionInstance based on the question id.
     * </p><p>
     * @param questionId of the Question Instance to retreive.
     * </p>
     */
    public QuestionInstance getQuestionInstance(String questionId) {
        QuestionInstance rtnQuestion = null;

        if (questionInstances != null) {
            for (int ix=0; ix < questionInstances.size(); ++ix) {
                if ((((QuestionInstance) questionInstances).getId()).equals(questionId)) {
                    rtnQuestion = (QuestionInstance) questionInstances.get(ix);
                    // Setting the value of ix to end gracefully
                    // out of the for loop.  One thing to remember
                    // in the future is if the assessment has the
                    // same question twice.  In this instance, we
                    // will return the first occurance of the
                    // QuestionInstance object.
                    ix = questionInstances.size();
                }
            }
        }
        return rtnQuestion;
    } // end getQuestionInstance

    /**
     * <p>
     * Returns all Question Instances.
     * </p><p>
     *
     * @return a List with all of the Questions Instances contained.
     * </p>
     */
    public List getQuestionInstances() throws Exception {
        return (questionInstances != null)
                            ?new ArrayList(questionInstances)
                            :new ArrayList();
    } // end getQuestionInstances
    
    /**
     * <p>
     * Sets all Question Instances.
     * </p><p>
     *
     * @param a List with all of the Questions Instances contained.
     * </p>
     */
    void setQuestionInstances(List questions) throws Exception {
        this.questionInstances = questions;
    } // end setQuestionInstances

    /**
     * <p>
     * Returns all unanswered Question Instances.
     * </p><p>
     *
     * @return a List with all of the unanswered Questions Instances.
     * </p><p>
     *
     * @see net.unicon.academus.assessment.domain.QuestionInstance
     * </p>
     */
    public List getUnansweredQuestions() throws Exception {
        List rtnList = new ArrayList();
        if (questionInstances != null) {
            QuestionInstance tempQI = null;
            for (int ix = 0; ix < questionInstances.size(); ++ix) {
                tempQI = (QuestionInstance) questionInstances.get(ix);
                if (!tempQI.hasAnswered() ) {
                    rtnList.add(tempQI);
                }
            }
        }
        return rtnList;
    }

    /**
     * <p>
     * Returns the start Timestamp and time of when the assessment was taken.
     * </p><p>
     *
     * @return a Timestamp with the start time and day of when the assesment was
     * taken.
     * </p>
     */
    public Timestamp getStartTime() {
        return results != null ? results.getStartTime() : null;
    } // end getStartTime

    /**
     * <p>
     * Returns the completion Timestamp and time of when the assessment was taken.
     * </p><p>
     *
     * @return a Timestamp with the completion time and day of when the assesment was
     * taken.
     * </p>
     */
    public Timestamp getEndTime() {
        return results != null ? results.getEndTime() : null;
    } // end getEndTime

    /**
     * <p>
     * Sets the start Timestamp and time of when the assessment was taken.
     * </p><p>
     *
     * @param a Timestamp with the start time and day of when the assesment was
     * taken.
     * </p>
     */
    public void setStartTime(Timestamp startTime) {
        if (results != null) {
            results.setStartTime(startTime);
        }
    } // end getStartTime

    /**
     * <p>
     * Sets the completion Timestamp and time of when the assessment was taken.
     * </p><p>
     *
     * @param a Timestamp with the completion time and day of when the assesment was
     * taken.
     * </p>
     */
    public void setEndTime(Timestamp endTime) {
        if (results != null) {
            results.setEndTime(endTime);
        }
    } // end getEndTime

    /**
     * <p>
     * Returns the username of the user taking the assessment.
     * </p><p>
     *
     * @return a String with username of the user.
     * </p>
     */
    public String getUsername() {
        return results != null ? results.getUsername() : null;
    } // end getUsername
    
    /**
     * <p>
     * Set the username of the user taking the assessment.
     * </p><p>
     *
     * @param a String with username of the user.
     * </p>
     */
    public void setUsername(String username) {
        if (results != null) {
            results.setUsername(username);
        }
    } // end getUsername

    /**
     * <p>
     * Return the total score of the user's assessment.
     * </p><p>
     *
     * @return a float with the user's total assessment score.
     * </p>
     */
    public float getTotalScore() {
        return results != null ? results.getTotalScore() : 0;
    } // end getTotalScore


    /**
     * <p>
     * Return the total score of the user's assessment. This
     * method actually does the accumulation process of the
     * Question Instance objects if they are available.
     * </p><p>
     *
     * @return a float with the user's total assessment score.
     * </p>
     */
    public void accumulateScore() {
        float rtnScore = assessment.getMinScore();
        if (questionInstances != null) {
            for (int ix=0; ix < questionInstances.size(); ++ix) {
                rtnScore += ((QuestionInstance)
                                questionInstances.get(ix)).getScore();
            }
        }
        if (results != null) {
            results.setTotalScore(rtnScore);
        }
    }
    
    /**
     * <p>
     * Return the activation id of the assessment
     * </p><p>
     * 
     * @return a String with the activation id
     * </p>
     */
    public String getActivationID() {
        return results != null ? results.getActivationID() : null;
    }
    
    /**
     * <p>
     * Sets the activation id of the assessment
     * </p><p>
     * 
     * @param a String with the activation id
     * </p>
     */
    public void setActivationID(String id) {
        if (results != null) {
            results.setActivationID(id);
        }
    }

    /**
     * <p>
     * Set to true if you want to increment the retry
     * counter.
     * </p><p>
     *
     * @param true or false if this users attempt counts
     * toward the retrydd.
     * </p>
     */
    public void incrementRetry(boolean increment) {
       if (results != null) {
           results.incrementRetry(increment);
       }
    }

     /**
     * <p>
     * Return  true if you want to increment the retry
     * counter.
     * </p><p>
     *
     * @param true or false if this users attempt counts
     * toward the retrydd.
     * </p>
     */
    public boolean countAsRetry() {
       return results != null ? results.countAsRetry() : false;
    }

    /**
     * <p>
     * Returns the id of the Assessment.
     * </p><p>
     *
     * @return a String with the Assessment id.
     * </p>
     */
    public String getId() {
        return assessment != null ? assessment.getId() : null;
    } // end getId
    
    /**
     * <p>
     * Sets the result id of the Assessment.
     * </p><p>
     *
     * @param a int with the Assessment result id.
     * </p>
     */
    public void setResultId(int resultID) {
        if (results != null) {
            results.setResultID(resultID);
        }
    } // end setResultId
    
    /**
     * <p>
     * Returns the result id of the Assessment.
     * </p><p>
     *
     * @return a int with the Assessment result id.
     * </p>
     */
    public int getResultId() {
        return results != null ? results.getResultID() : 0;
    } // end getResultId

    /**
     * <p>
     * Returns the title of the Assessment.
     * </p><p>
     *
     * @return a String with the Assessment title.
     * </p>
     */
    public String getTitle() {
        return assessment != null ? assessment.getTitle() : null;
    } // end getTitle

    /**
     * <p>
     * Returns the description of the assessment.
     * </p><p>
     *
     * @return a String with the Assessment description.
     * </p>
     */
    public String getDescription() {
        return assessment != null ? assessment.getDescription() : null;
    } // end getDescription

    /**
     * <p>
     * Returns the max score of the assessment.
     * </p><p>
     *
     * @return a int with the Assessment maximum score.
     * </p>
     */
    public int getMaxScore() {
        return assessment != null ? assessment.getMaxScore() : 0;
    } // end getMaxScore

    /**
     * <p>
     * Returns the min score of the assessment.
     * </p><p>
     *
     * @return a int with the Assessment minimum score.
     * </p>
     */
    public int getMinScore() {
        return assessment != null ? assessment.getMinScore() : 0;
    } // end getMinScore

    /**
     * <p>
     * Does the assessment have the option of being randomized.
     * </p><p>
     *
     * @return a boolean with if the assessment can be randomized.
     * </p>
     */
    public boolean isRandomized() {
        return assessment != null ? assessment.hasRandomizedQuestions() : false;
    } // end isRandomized

    /**
     * <p>
     * Get the total number of questions the assessment has.
     * </p><p>
     *
     * @return a int total number of questions on the assessment.
     * </p>
     */
    public int getNumberOfQuestions() {
        return assessment != null ? assessment.getNumberOfQuestions() : 0;
    } // end getNumberOfQuestions

    /**
     * <p>
     * Randomizes the questions on the assessment.
     * </p><p>
     *
     * </p>
     */
    public void randomizeQuestions() {
        Collections.shuffle(questionInstances);
    } // end randomizeQuestions

    /**
     * <p>
     * Sets the position number for the question.  If this method is
     * not called, no position numbers will be applied to the
     * Question Instances.
     * </p><p>
     *
     * </p>
     */
    public void setPositionNumbers() {
        QuestionInstance tempInstance = null;

        if (questionInstances != null) {
            for (int ix = 0; ix < questionInstances.size(); ++ix) {
                tempInstance = (QuestionInstance) questionInstances.get(ix);
                tempInstance.setPositionNumber(ix + 1);
            }
        }
    }

    /**
     * <p>
     * Returns the Assessment Result object.
     * </p><p>
     *
     * @return AssessmentResults with the current assessment results.
     * </p>
     */
    AssessmentResults getResult() {
        return results;
    }
    
    /**
     * <p>
     * Sets the Assessment Result object.
     * </p><p>
     *
     * @param AssessmentResults with the current assessment results.
     * </p>
     */
    void setResult(AssessmentResults results) {
        this.results = results;
    }


    /**
     * <p>
     * Grades all the questions in the assessment.  Note, it
     * does not accumulate score
     * </p><p>
     *
     * </p>
     */
    public void gradeAllQuestions() {
        if (questionInstances != null) {
            for (int ix = 0; ix < questionInstances.size(); ++ix) {
                ((QuestionInstance) questionInstances.get(ix)).grade();
            }
        }
    }
    
    /**
     * <p>
     * Returns an XML representation of the assessment instance.
     * </p><p>
     *
     * @return a String with the XML representation of the assessment instance.
     * </p>
     */
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<assessment-instance");
        xmlBuff.append(">");

        /*~~~~~~~~~~~~~~~~~~~~
         * ATTRIBUTES
         *~~~~~~~~~~~~~~~~~~~*/
        // Question
        if (assessment != null) {
            xmlBuff.append(assessment.toXML());
        }

        // Assessment Results
        if (results != null) {
            xmlBuff.append(results.toXML());
        }
        xmlBuff.append("</assessment-instance>");
        return xmlBuff.toString();
   } // end toXML

} // end AssessmentInstance
