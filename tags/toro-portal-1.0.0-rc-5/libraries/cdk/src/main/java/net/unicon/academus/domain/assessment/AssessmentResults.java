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

import net.unicon.academus.common.XMLAbleEntity;

import java.sql.Timestamp;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import java.io.StringReader;
import org.apache.xpath.XPathAPI;
/**
 * <p>
 * 
 * </p>
 */
public class AssessmentResults implements Cloneable, XMLAbleEntity {
    private String username; 
    private float totalScore; 
    private Timestamp startTime; 
    private Timestamp finishTime; 
    private boolean incrementRetry;
    private int resultID;
    private String assessmentID;
    private String activationID;
    
    public AssessmentResults () {}

    public AssessmentResults (String xml) {
        try {
            DocumentBuilder builder = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            XPathAPI xpath = new XPathAPI();
            Node node = xpath.selectSingleNode(document, "summary_result");
                        
            if (node != null) {
                String resultStr = ((Element) node).getAttribute("result_ref");
                this.username    = ((Element) node).getAttribute("username");
                // Getting result ID
                if (resultStr != null) {
                    try {
                        this.resultID = Integer.parseInt(resultStr);
                    } catch (NumberFormatException nfe) {
                        this.resultID = 0;
                    }
                }
                
                Node scoreNode = xpath.selectSingleNode(node, "score/score_value");
                if (scoreNode != null) {
                    String scoreStr = scoreNode.getFirstChild().getNodeValue();

                    if (scoreStr != null) {
                        try {
                            this.totalScore = Float.parseFloat(scoreStr);
                        } catch (NumberFormatException nfe) {
                            this.totalScore = -1L;
                        }
                    }
                } else {
                    this.totalScore = -1L;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Returns a clone of this object.
     * </p><p>
     *
     * @return a Object with a representation of this object.
     * </p>
     */
    public Object clone() throws CloneNotSupportedException {
        AssessmentResults cloneCopy = null;
        try {
            cloneCopy = (AssessmentResults) super.clone();
            cloneCopy.startTime  = null;
            cloneCopy.finishTime = null;
            cloneCopy.totalScore = -1L;
        } catch (CloneNotSupportedException cnse) {
            String message = "Unable to clone the Assessment Results";
            throw new CloneNotSupportedException(message);
        }
        return cloneCopy;
    }
    
    /**
     * <p>
     * Returns the username for the user's Assessment Results.
     * </p><p>
     * 
     * @return a String with username of the user.
     * </p>
     */
    public String getUsername() {
        return this.username;
    } // end getUsername 
    
    /**
     * <p>
     * Sets the username for the user's Assessment Results.
     * </p><p>
     * 
     * @param a String with username of the user.
     * </p>
     */
    public void setUsername(String username) {
        this.username = username;
    } // end setUsername    

    /**
     * <p>
     * Return the total score of the user's assessment.
     * </p><p>
     * 
     * @return a float with the user's total assessment score.
     * </p>
     */
    public float getTotalScore() {
        return this.totalScore;
    } // end getTotalScore 
    
    /**
     * <p>
     * Set the total score of the user's assessment.
     * </p><p>
     * 
     * @param a float with the user's total assessment score.
     * </p>
     */
    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    } // end getTotalScore 

    /**
     * <p>
     * Returns the Start Timestamp and Time of when the user
     * took the assessment.
     * </p><p>
     * 
     * @return a Timestamp with the start time and day when the
     * user started the assessment.
     * </p>
     */
    public Timestamp getStartTime() {
        return this.startTime;
    } // end getStartTime    
    
    /**
     * <p>
     * Sets the Start Timestamp and Time of when the user
     * took the assessment.
     * </p><p>
     * 
     * @param a Timestamp with the start time and day when the
     * user started the assessment.
     * </p>
     */
    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    } // end setStartTime    

    /**
     * <p>
     * Returns the End Timestamp and Time of when the user
     * finished the assessment.
     * </p><p>
     * 
     * @return a Timestamp with the start time and day when the
     * user finished the assessment.
     * </p>
     */
    public Timestamp getEndTime() {        
        return this.finishTime;
    } // end getEndTime
    
    /**
     * <p>
     * Sets the End Timestamp and Time of when the user
     * finished the assessment.
     * </p><p>
     * 
     * @param a Timestamp with the start time and day when the
     * user finished the assessment.
     * </p>
     */
    public void setEndTime(Timestamp endTime) {        
        this.finishTime = endTime;
    } // end getEndTime    

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
        this.incrementRetry = increment;
    }
    
    /**
     * <p>
     * Return true if you want to increment the retry
     * counter.
     * </p><p>
     * 
     * @param true or false if this users attempt counts
     * toward the retry.
     * </p>
     */
    public boolean countAsRetry() {
        return this.incrementRetry;
    }
    
    /**
     * <p>
     * Set the result id of the assessment result
     * </p><p>
     * 
     * @param a int with the result id
     * </p>
     */
    public void setResultID(int resultID) {
        this.resultID = resultID;
    }
    
    /**
     * <p>
     * Return the result id of the assessment result
     * </p><p>
     * 
     * @return a int with the result id
     * </p>
     */
    public int getResultID() {
        return this.resultID;
    }
    
    /**
     * <p>
     * Return the assessment id of the assessment
     * </p><p>
     * 
     * @return a String with the assessment id
     * </p>
     */
    public String getAssessmentID() {
        return this.assessmentID;
    }
    
    /**
     * <p>
     * Return the assessment id of the assessment
     * </p><p>
     * 
     * @return a String with the assessment id
     * </p>
     */
    public void setAssessmentID(String id) {
        this.assessmentID = id;
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
        return this.activationID;
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
        this.activationID = id;
    }

    /**
     * <p>
     *  Returns an xml representation of the assessment
     *  result.
     * </p><p>
     *
     * @return a String with the xml representation of the data.
     * </p>
     */
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<summary_result");
        
        if (resultID > 0) {
            xmlBuff.append(" result_ref=\"");
            xmlBuff.append(""+resultID);
            xmlBuff.append("\"");
        }
        xmlBuff.append(" username=\"");
        xmlBuff.append(username);
        xmlBuff.append("\"");
        xmlBuff.append(">");
        
        // Start Date
        xmlBuff.append("<date>");
        xmlBuff.append("<type_label>");
        xmlBuff.append("Start Date/Time");
        xmlBuff.append("</type_label>");
        xmlBuff.append("<datetime>");
        if (startTime != null) {
            xmlBuff.append(startTime.toString());
        }
        xmlBuff.append("</datetime>");
        xmlBuff.append("</date>");
        
        // End Date
        xmlBuff.append("<date>");
        xmlBuff.append("<type_label>");
        xmlBuff.append("Finished Date/Time");
        xmlBuff.append("</type_label>");
        xmlBuff.append("<datetime>");
        if (finishTime != null) {
            xmlBuff.append(finishTime.toString());
        }
        xmlBuff.append("</datetime>");
        xmlBuff.append("</date>");
        

        //Score
        xmlBuff.append("<score>");
        if (totalScore >= 0L ){
            xmlBuff.append("<score_value>");
            xmlBuff.append(""+totalScore);
            xmlBuff.append("</score_value>");
        }
        xmlBuff.append("</score>");
        
        xmlBuff.append("</summary_result>");
        return xmlBuff.toString();
    }
    
} // end AssessmentResults
