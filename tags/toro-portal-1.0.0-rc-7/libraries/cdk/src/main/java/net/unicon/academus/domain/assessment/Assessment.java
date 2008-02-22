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

import java.io.StringReader;

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
public final class Assessment implements XMLAbleEntity {

    private String id; 
    private String title; 
    private String description;
    private String keyword;
    private String version;
    private String xml;
    private int maxScore; 
    private int minScore; 
    private boolean randomize; 
    private List questions;

    /**
     * <p>
     * Constructor ...
     * </p><p>
     * 
     * </p>
     */
    public Assessment(String xml) {
        try {
            this.xml = xml;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            XPathAPI xpath = new XPathAPI();
            Node node = xpath.selectSingleNode(document, "assessment");
            
            if (node != null) {
                title = ((Element) node).getAttribute("title");
                id    = ((Element) node).getAttribute("ident");
                String random = ((Element) node).getAttribute("sequence");
                if ("random".equals(random)) {
                    randomize = true;
                } else {
                    randomize = false;
                }

            }
            Node descNode = xpath.selectSingleNode(node, "description");
            Node verNode  = xpath.selectSingleNode(node, "version");
            Node kwNode   = xpath.selectSingleNode(node, "keyword");
            
            if (descNode != null) {
                description = descNode.getFirstChild().getNodeValue();
            }    

            if (verNode != null) {
                version = verNode.getFirstChild().getNodeValue();
            }

            if (kwNode != null) {
                keyword = verNode.getFirstChild().getNodeValue();
            }
            
            // Creating questions.
            NodeList nodeList = document.getElementsByTagName("itemref");
            questions = new ArrayList();
            Question tempQuestion = null;

            // max score 
            maxScore = 0;
            for (int ix = 0; ix < nodeList.getLength(); ++ix) {
                String id = ((Element) nodeList.item(ix)).getAttribute("ident");
                tempQuestion = QuestionFactory.getQuestion(id);
                
                if (tempQuestion != null) {
                    maxScore += tempQuestion.getMaxScore();

                    // Adding the question to the question list
                    questions.add(tempQuestion);
                } else {
                    // probably should throw a nasty error
                    // if we cannot find the correspoding 
                    // question.
                }
            }
           
            // min score
            minScore = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * <p>
     * Constructor ...
     * </p><p>
     * 
     * </p>
     */
    public Assessment(String xml, String id) {
        this(xml);
        this.id = id;
    }
    
    /**
     * <p>
     * Return the assessment id
     * </p><p>
     * 
     * @return a String with the id of the assessment.
     * </p>
     */
    public String getId() {        
        return this.id;
    } // end getId        

    /**
     * <p>
     * Return the assessment title.
     * </p><p>
     * 
     * @return a String with the title of the assessment.
     * </p>
     */
    public String getTitle() {
        return this.title;
    } // end getTitle        

    /**
     * <p>
     * Return the assessment description.
     * </p><p>
     * 
     * @return a String with the description of the assessment.
     * </p>
     */
    public String getDescription() {        
        return this.description;
    } // end getDescription 
    
    /**
     * <p>
     * Get version.
     * </p><p>
     * 
     * @return a String with the version of the assessment.
     * </p>
     */
    public String getVersion() {        
        return this.version;
    } // end getVersion
    
    /**
     * <p>
     * Get keyword set.
     * </p><p>
     * 
     * @return a String with the keywords of the assessment.
     * </p>
     */
    public String getKeyword() {        
        return this.keyword;
    } // end getkeyword     


    /**
     * <p>
     * Return the max score of the assessment.
     * </p><p>
     * 
     * @return a int with the maxium score for the assesment.
     * </p>
     */
    public int getMaxScore() {        
        return this.maxScore;
    } // end getMaxScore   

    /**
     * <p>
     * Return the minimum score of the assessment.
     * </p><p>
     * 
     * @return a int with the minimum score for the assessment.
     * </p>
     */
    public int getMinScore() {
        // Min Score is zero until its authored
        return this.minScore;
    } // end getMinScore   

    /**    
     * <p>
     * Does the assessment have the option of being randomized.
     * </p><p>
     * 
     * @return a boolean with if the assessment can be randomized.
     * </p>
     */
    public boolean hasRandomizedQuestions() {        
        return this.randomize;
    } // end hasRandomizedQuestions 

    /**
     * <p>
     * Returns a question from the assessment based on its
     * question id.
     * </p><p>
     * 
     * @return a Question from the assessment based on an id.
     * </p><p>
     * @param questionId of the Question
     * </p><p>
     * @see net.unicon.academus.assessment.domain.Question
     * </p>
     */
    public Question getQuestion(String questionId) { 
        Question question = null;
        if (questionId != null && questions != null) {
            for (int ix = 0; ix < questions.size(); ++ix ) {
                if (questionId.equals(((Question) questions.get(ix)).getId()) ) {
                    question = (Question) questions.get(ix);
                }
            }
        }
        return question;
    } 

    /**
     * <p>
     * Returns all questions associated with the assessment.
     * </p><p>
     * 
     * @return a List with all the assessment questions.
     * </p>
     */
    public List getQuestions() {        
        List rtnList = null;

        if (questions != null) {
            rtnList = questions;
        } else {
            rtnList = new ArrayList();
        }
        return rtnList;
    } // end getQuestions

    /**
     * <p>
     * Return the number of questions on the assessment.
     * </p><p>
     * 
     * @return a int with number of questions in the assessment.
     * </p>
     */
    public int getNumberOfQuestions() {
        int rtnValue = 0;

        if (questions != null) {
            rtnValue = questions.size();    
        }
        return rtnValue;
    } // end getNumberOfQuestions

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
        sb.append("Assessment    :").append("\n");
        sb.append("\tid          : ").append(id).append("\n");
        sb.append("\ttitle       : ").append(title).append("\n");
        sb.append("\tdescription : ").append(description).append("\n");
        sb.append("\tmax Score   : ").append(""+maxScore).append("\n");
        sb.append("\tmin Score   : ").append(""+minScore).append("\n");

        sb.append("\tQuestions [\n");
        for(int ix=0; ix < questions.size(); ++ix) {
            sb.append(((Question) questions.get(ix)).toString());
        }
        sb.append("\t]\n");
        return sb.toString();
    }

    public static void main (String [] args) throws Exception {
         // start hack test code
         StringBuffer xmlBuffer = new StringBuffer();
         xmlBuffer.append("<assessment ident=\"IMS_ASS_01\" title=\"Basic Assessment Example\">");
         xmlBuffer.append("    <description>This is a kick butt Assessment Example</description>");
         xmlBuffer.append("    <presentation-material>Blah</presentation-material>");
         xmlBuffer.append("    <itemref ident=\"IMS_V01\"/>");
         xmlBuffer.append("    <itemref ident=\"IMS_V02\"/>");
         xmlBuffer.append("    <itemref ident=\"IMS_V03\"/>");
         xmlBuffer.append("</assessment>");
         String xmlString = xmlBuffer.toString();
         // end hack test code
         try {
             Assessment assessment = new Assessment(xmlString);
             System.out.println("DESCRIPTION : " + assessment.getDescription());
         } catch (Exception e) {
             e.printStackTrace();
         }
        System.exit(1);
    }
} // end Assessment

