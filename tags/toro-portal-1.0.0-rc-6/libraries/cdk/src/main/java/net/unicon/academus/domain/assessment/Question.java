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

import java.io.StringReader;

import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;

import net.unicon.academus.common.XMLAbleEntity;

/**
 * <p>
 * 
 * </p>
 */
public final class Question implements XMLAbleEntity {

    private String id; 
    private int maxScore; 
    private int minScore; 
    private String title; 
    private String description; 
    private String xml;
    private List gradingConditions;

    /**
     * <p>
     * Constructor ...
     * </p><p>
     * 
     * @return a String with ...
     * </p>
     */
    public Question (String xml) {
        try {
            this.xml = xml;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            XPathAPI xpath = new XPathAPI();
            Node node = xpath.selectSingleNode(document, "item");
            
            if (node != null) {
                this.title = ((Element) node).getAttribute("title");
                this.id    = ((Element) node).getAttribute("ident");
            
                // Getting grading criteria
                Node resprocessing = xpath.selectSingleNode(node, "resprocessing");
            
                if (resprocessing != null) {
                    gradingConditions = GradeConditionFactory.getGradeConditions(resprocessing);
                } else {
                    gradingConditions = new ArrayList();
                }
                
                Node descNode = xpath.selectSingleNode(node, "description");
            
                if (descNode != null) {
                    this.description = descNode.getFirstChild().getNodeValue();
                }      
            }
            

            //min score
            this.minScore = 0;

            this.maxScore = Integer.parseInt(((Element) node).getAttribute("maxscore"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * <p>
     * Constructor ...
     * </p><p>
     * 
     * @return a String with ...
     * </p>
     */
    public Question (String xml, String id) {
        this(xml);
        this.id = id;
    }

    /**
     * <p>
     * Returns the id of the question
     * </p><p>
     * 
     * @return a String representation of id.
     * </p>
     */
    public String getId() {        
        return this.id;
    } // end getId        

    /**
     * <p>
     * Returns the maximum point value for this question.
     * </p><p>
     * 
     * @return a int with the maximum score for this question
     * </p>
     */
    public int getMaxScore() {        
        return this.maxScore;
    } // end getMaxScore        

    /**
     * <p>
     * Returns the minimun point value for this question.
     * </p><p>
     * 
     * @return a int with the minimum score for this question.
     * </p>
     */
    public int getMinScore() {        
        return this.minScore;
    } // end getMinScore       

    /**
     * <p>
     * Returns the title of the question
     * </p><p>
     * 
     * @return a String with the question title.
     * </p>
     */
    public String getTitle() {        
        return this.title;
    } // end getTitle        

    /**
     * <p>
     * Returns the description of the question
     * </p><p>
     * 
     * @return a String with the question description.
     * </p>
     */
    public String getDescription() {        
        return this.description;
    } // end getDescription    

    /**
     * <p>
     * Grades a question based on the passed in results
     * </p><p>
     * 
     * @param userResponse is the users response to the question.
     * </p><p>
     * @return a int with graded question score.
     * </p>
     */
    public int grade(List userResponse) {        
        int rtnScore = minScore;
        
        if (userResponse != null) {
            int tempScore = 0;
            for (int ix = 0 ; ix < gradingConditions.size(); ++ix) {
                tempScore += ((GradeCondition) 
                    gradingConditions.get(ix)).evaluate(userResponse);
            }
            rtnScore = tempScore;
        }
        
        return rtnScore; 
    } // end grade        

    /**
     * <p>
     * Returns an XML representation of the assessment.
     * </p><p>
     *
     * @return a String with the XML representation of the assessment.
     * </p>
     */
    public String toXML() {
        return xml;
    } // end toXML

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Question :").append("\n");
        sb.append("\tid        : ").append(id).append("\n");
        sb.append("\ttitle     : ").append(""+title).append("\n");
        sb.append("\tmax Score : ").append(""+maxScore).append("\n");
        sb.append("\tmin Score : ").append(""+minScore).append("\n");
        return sb.toString();
    }

    public static void main (String [] args) {
        // start hack code
        StringBuffer xmBuffer = new StringBuffer();
        xmBuffer.append("<item ident=\"IMS_V011\" title=\"Basic MCSA Question\" maxscore=\"15\">");
        xmBuffer.append("   <presentation label=\"BasicExample001\">");
        xmBuffer.append("      <material>");
        xmBuffer.append("              <mattext>Paris is the Capital of France ?</mattext>");
        xmBuffer.append("      </material>");
        xmBuffer.append("      <response_lid ident=\"TF01\" rcardinality=\"Single\" rtiming=\"No\">");
        xmBuffer.append("          <render_choice>");
        xmBuffer.append("              <response_label ident=\"T\">");
        xmBuffer.append("                 <material><mattext> True </mattext></material>");
        xmBuffer.append("              </response_label>");
        xmBuffer.append("              <response_label ident=\"F\">");
        xmBuffer.append("                 <material><mattext> False </mattext></material>");
        xmBuffer.append("              </response_label>");
        xmBuffer.append("          </render_choice>");
        xmBuffer.append("      </response_lid>");
        xmBuffer.append("   </presentation>");
        xmBuffer.append("   <resprocessing>");
        xmBuffer.append("      <respcondition title=\"Correct\">");
        xmBuffer.append("          <conditionvar>");
        xmBuffer.append("              <varequal respident=\"TF01\">T</varequal>");
        xmBuffer.append("          </conditionvar>");
        xmBuffer.append("          <setvar action=\"Set\" varname=\"MCSCORE\">1</setvar>");
        xmBuffer.append("      </respcondition>");
        xmBuffer.append("   </resprocessing>");
        xmBuffer.append("</item> ");
        String xmlString = xmBuffer.toString();

        Question question = new Question(xmlString);
        System.out.println("MAX_SCORE = " + question.getMaxScore());
    }
    
} // end Question
