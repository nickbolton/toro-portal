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

import java.util.List;

import net.unicon.academus.common.XMLAbleEntity;

public class QuestionInstance implements Cloneable, XMLAbleEntity {
    private int orderPosition; 
    private boolean isAnswered;
    
    private Question question; 
    private QuestionResults results; 


    public QuestionInstance(Question question) {
        this.question = question;
        this.isAnswered = false;
        this.results = new QuestionResults();
        this.results.setQuestionID(question.getId());
    }
    
    /**
     * <p>
     * Make an exact copy of this Question Instance object.
     * </p><p>
     * 
     * @return a clone of this QuestionInstance Object.
     * </p>
     */
    public Object clone() throws CloneNotSupportedException {
        QuestionInstance cloneCopy = null;
        try {
            cloneCopy = (QuestionInstance) super.clone();
            // Cloning Question Results
            cloneCopy.results = (QuestionResults) results.clone();
            //cloneCopy.results.setQuestionId(question.getId());
            // Question object will NOT be cloned because
            // its a singleton.  With the inherited clone
            // method, the new cloned object will have a 
            // reference to it.
        } catch (CloneNotSupportedException cnse) {
            String message = "Unable to close the Question Instance";
            throw new CloneNotSupportedException(message);
        }
        // your code here
        return cloneCopy;
    } // end clone 

    /**
     * <p>
     * Returns the position number of the Question.
     * </p><p>
     * 
     * @return a int with the position number or order number.
     * </p>
     */
    public int getPositionNumber() {        
        return this.orderPosition;
    } // end getPositionNumber

    /**
     * <p>
     * Sets the position number for this question.  This is what
     * the order number this question falls in the assessment.
     * </p><p>
     * 
     * @param position - the order of this question in the assessment.
     * </p>
     */
    public void setPositionNumber(int position) {        
        this.orderPosition = position;
    } // end setPositionNumber        

    /**
     * <p>
     * Returns the id of the question.
     * </p><p>
     * 
     * @return a String with the quesitons id
     * </p>
     */
    public String getId() {
        String rtnId = null;
        if (question != null) {
            rtnId = question.getId();
        }
        return rtnId;
    } // end getId       

    /**
     * <p>
     * Return the score based on the question response.
     * </p><p>
     * 
     * @return a float of the question score.
     * </p>
     */
    public float getScore() {        
        float rtnScore = 0;
        if (results != null) {
            rtnScore = results.getScore();
        }
        return rtnScore;
    } // end getScore   

    /**
     * <p>
     * Return the maximum score value for the quesiton.
     * </p><p>
     * 
     * @return a int with max score value for the question.
     * </p>
     */
    public int getMaxScore() {       
        int rtnMaxScore = 1; // Making 1 default if not present
        
        if (question != null) {
            rtnMaxScore = question.getMaxScore();
        }
        return rtnMaxScore;
    } // end getMaxScore 

    /**
     * <p>
     * Return the minimum score value for the question.
     * </p><p>
     * 
     * @return a int with minimum score value for the quesiton.
     * </p>
     */
    public int getMinScore() { 
        int rtnMinScore = 0; // Making the default zero if not present

        if (question != null) {
            rtnMinScore = question.getMinScore();
        }
        return rtnMinScore;
    } // end getMinScore 

    /**
     * <p>
     * Return the title of the question.
     * </p><p>
     * 
     * @return a String with the of the question.
     * </p>
     */
    public String getTitle() {        
        String rtnTitle = null;
        if (question != null) {
            rtnTitle = question.getTitle();
        }
        return rtnTitle;
    } // end getTitle   


    /**
     * <p>
     * Returns a description of the question.
     * </p><p>
     * 
     * @return a String with description of the question.
     * </p>
     */
    public String getDescription() {        
        String rtnDescription = null;
        if (question != null) {
            rtnDescription = question.getDescription();
        }
        return rtnDescription;
    } // end getDescription        

    /**
     * <p>
     * Return the username of taking the question.
     * </p><p>
     * 
     * </p>
     */
    public String getUsername() {        
        String rtnUsername = null;
        if (results != null) {
            rtnUsername = results.getUsername();
        }
        return rtnUsername;
    } // end getUsername  
    
    /**
     * <p>
     * Set the username of taking the question.
     * </p><p>
     * 
     * </p>
     */
    public void setUsername(String username) {        
        if (results != null) {
            results.setUsername(username);
        }
    } // end getUsername       

    public void setResultID(int resultID) {
        if (results != null) {
            results.setResultID(resultID);
        }
    }

    public int getResultID() {
        return results != null ? results.getResultID() : 0;
    }
    /**
     * <p>
     * Sets the user's responses to the question. This
     * is the Raw format from the view.
     * </p><p>
     * 
     * @param String[] with the question responses. 
     * </p>
     */
    public void setResponse(List responses) {
        if (responses != null && responses.size() > 0 ) {
        for (int ix=0; ix < responses.size(); ++ix ) {
        System.out.println(((Response) responses.get(ix)).toXML());
        }
            // Setting that the person has answered
            // the question by supply a response
            isAnswered = true;
            // Setting the Response for the 
            // Question Instance
            results.setResponse(responses);
        }
    } // end setResponse
   
    QuestionResults getResult() {
        return results;
    }
    
    void setResult(QuestionResults results) {
        this.results = results;
    }
    
    /**
     * <p>
     * Returns true if the question 'has answered'
     * </p><p>
     *
     * @return a boolean true if answered, false if no.
     * </p>
     */
    public boolean hasAnswered() {
        return isAnswered;
    }
    
    /**
     * <p>
     * Set true if the question 'is answered'
     * </p><p>
     *
     * @return a boolean true if question is answered, false if not.
     * </p>
     */
    protected void hasAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
    }

    /**
     * <p>
     * Grades the question and reports the score to the user's
     * question results.
     * </p><p>
     * 
     * </p>
     */
    public void grade() {
        if (question != null) {
            results.setScore((float) question.grade(results.getResponse())); 
        }
    } // end grade

    public boolean equals(QuestionInstance quest) {
        return (this.getId() == quest.getId());
    }
    
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<question-instance");

        /*~~~~~~~~~~~~~~~~~~~~
         * ATTRIBUTES
         *~~~~~~~~~~~~~~~~~~~*/
        // Position Number Question Instance Attribute
        xmlBuff.append(" numbering=\"");
        xmlBuff.append(""+orderPosition);
        xmlBuff.append("\"");
        xmlBuff.append(">");
        
        // Question
        if (question != null) {
            xmlBuff.append(question.toXML());
        }

        // Question Results
        if (results != null) {
            xmlBuff.append(results.toXML());
        }
        xmlBuff.append("</question-instance>");
        return xmlBuff.toString();
    }
} // end QuestionInstance
