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
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import java.io.StringReader;
import org.apache.xpath.XPathAPI;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.util.xml.NodeToString;
/**
 * <p>
 *
 * </p>
 */
public class QuestionResults implements Cloneable, XMLAbleEntity {

    private String username;
    private String questionId;
    private float score;
    private List responses;
    int resultId;
    private String xml;

    /**
     * <p>
     * Does ...
     * </p><p>
     *
     * @return a String with ...
     * </p>
     */
    public QuestionResults () {}

    public QuestionResults(String xml) {
        try {
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Node node = XPathAPI.selectSingleNode(document, "item_result");
            if (node != null) {
                String resultStr = ((Element) node).getAttribute("result_ref");
                this.username    = ((Element) node).getAttribute("username");
                this.questionId  = ((Element) node).getAttribute("ident_ref");
                // Getting result ID
                if (resultStr != null) {
                    try {
                        this.resultId = Integer.parseInt(resultStr);
                    } catch (NumberFormatException nfe) {
                        this.resultId = 0;
                    }
                }

                Node scoreNode = XPathAPI.selectSingleNode(node, "score/score_value");
                if (scoreNode != null) {
                    String scoreStr = scoreNode.getFirstChild().getNodeValue();

                    if (scoreStr != null) {
                        try {
                            this.score = Float.parseFloat(scoreStr);
                        } catch (NumberFormatException nfe) {
                            this.score = -1L;
                        }
                    }
                } else {
                    this.score = -1L;
                }
            }
            this.responses = new ArrayList();

            NodeList nodeList = document.getElementsByTagName("response");
            if (nodeList != null) {
                for (int ix=0; ix < nodeList.getLength(); ++ix ) {
                     responses.add(ResponseFactory.getResponse(
                                 new NodeToString(
                                     (Node) nodeList.item(ix)).toString()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Object clone() throws CloneNotSupportedException {
        QuestionResults cloneCopy = null;

        // Responses do not need to be cloned because it is possible
        // not to have a response to a question.
        try {
            cloneCopy = (QuestionResults) super.clone();
            cloneCopy.responses = new ArrayList();
        } catch (CloneNotSupportedException cnse) {
            String message = "Unable to clone Question Result Object";
            throw new CloneNotSupportedException(message);
        }
        return cloneCopy;
    }

    /**
     * <p>
     * Return the username for this quesiton results.
     * </p><p>
     *
     * @return a String with the username for this question's result.
     * </p>
     */
    public String getUsername() {
        return this.username;
    } // end getUsername

    /**
     * <p>
     * Sets the username for this quesiton results.
     * </p><p>
     *
     * @param a String with the username for this question's result.
     * </p>
     */
    public void setUsername(String username) {
        this.username = username;
    } // end getUsername

    /**
     * <p>
     * Returns the user's score for this question.
     * </p><p>
     *
     * @return a float with the user's score for this question.
     * </p>
     */
    public float getScore() {
        return this.score;
    } // end getScore

    /**
     * <p>
     * Set the user's score for this question.
     * </p><p>
     *
     * @parm a float with the user's score for this question.
     * </p>
     */
    public void setScore(float score) {
        this.score = score;
    } // end setScore


    /**
     * <p>
     * Returns the list of repsonses
     * </p><p>
     *
     * @return a List with the question responses.
     * </p>
     */
    public List getResponse() {
        List rtnList = null;

        if (responses != null) {
            rtnList = responses;
        } else {
            rtnList = new ArrayList();
        }
        return rtnList;
    } // end getResponse

    /**
     * <p>
     * Sets the responses to the question.  The question
     * are then formated to a Academus format.
     * </p><p>
     *
     * @param a String[] with the question responses.
     * </p>
     */
    public void setResponse(List responses) {
        this.responses = responses;
    }

    public void setResultID(int resultID) {
        this.resultId = resultID;
    }

    public int getResultID() {
        return this.resultId;
    }

    public void setQuestionID(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionID() {
        return this.questionId;
    }

    /**
     * <p>
     * Returns an XML representation of the assessment.
     * </p><p>
     *
     * @return a String with the XML representation of the assessment.
     * </p>
     */
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<item_result");

        // Question ID
        xmlBuff.append(" ident_ref=\"");
        xmlBuff.append(questionId);
        xmlBuff.append("\"");

        // Exam Result ID
        xmlBuff.append(" result_ref=\"");
        xmlBuff.append(""+resultId);
        xmlBuff.append("\"");

        // Username
        xmlBuff.append(" username=\"");
        xmlBuff.append(username);
        xmlBuff.append("\"");
        xmlBuff.append(">");
        if (responses != null) {
            for (int ix=0; ix < responses.size(); ++ix) {
                xmlBuff.append(((Response) responses.get(ix)).toXML());
            }
        }
        xmlBuff.append("<score>");

        // The actually score
        xmlBuff.append("<score_value>");
        xmlBuff.append(""+score);
        xmlBuff.append("</score_value>");

        xmlBuff.append("</score>");
        xmlBuff.append("</item_result>");
        return xmlBuff.toString();
    } // end toXML

} // end QuestionResults
