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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.io.StringReader;
import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.SearchCriteria;
import net.unicon.portal.util.db.AcademusDBUtil;

public final class QuestionFactory {
    
    private static Map questionCache = new HashMap();

    private QuestionFactory () {}
    
    public static Question getQuestion(String id) {
        Question rtnQuestion = null;

        rtnQuestion = (Question) questionCache.get(id);

        if (rtnQuestion != null) {
            return rtnQuestion;
        }
        
        Connection conn = null;
        try { 
            conn = AcademusDBUtil.getDBConnection();
            rtnQuestion =  __getQuestionFromDB(id, conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
        return rtnQuestion;
    }
    
    public static Question getQuestion(String id, Connection conn) {
        Question rtnQuestion = null;

        rtnQuestion = (Question) questionCache.get(id);

        if (rtnQuestion != null) {
            return rtnQuestion;
        }

        return __getQuestionFromDB(id, conn);
    }

    private static final String GET_QUESTION_SQL =
        "select data from question where id = ?";
        
    private static Question __getQuestionFromDB(String id, Connection conn) {
        Question rtnQuestion = null;
        
        if (id != null) {
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement(GET_QUESTION_SQL);
                pstmt.setString(1, id);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    rtnQuestion = new Question(rs.getString(1));
                }
            
                // Caching question objects for quicker reference
                questionCache.put(id, rtnQuestion);
            } catch (SQLException e) {
                e.printStackTrace();
            } // end try-catch
            finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                }
                try {
                    if (pstmt != null) {
                        pstmt.close();
                    }
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        } // end if
        return rtnQuestion;
    }
    
    public static List findQuestion(SearchCriteria parms) {
        List rtnList = new ArrayList();
        // XXX Need SQL Code
        return rtnList;
    }
    
    private static final String QUERY_QUESTION_SQL =
        "select id from question where id = ?";
    
    public static void importQuestion(String xml) 
    throws AcademusException {
        Connection conn = null;
        try { 
            conn = AcademusDBUtil.getDBConnection();
            importQuestion(xml, conn);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AcademusException(e);
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
    }
    
    private static final String INSERT_QUESTION_SQL = 
        "insert into question (id, name, description, data) values (?,?,?,?)";
        
    public static void importQuestion (String xml, Connection conn) 
    throws AcademusException {
        if (xml != null) {
System.out.println(xml + "\n\n");           
            try {
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                /* Getting XML data */
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(xml)));

                XPathAPI xpath = new XPathAPI();
                Node node = xpath.selectSingleNode(document, "item");
            
                String id          = null;
                String title       = null;
                String description = null;
                if (node != null) {
                    id    = ((Element) node).getAttribute("ident");
                    title = ((Element) node).getAttribute("title");
                }

                node = xpath.selectSingleNode(document, "description");
            
                if (node != null) {
                    description = node.getFirstChild().getNodeValue();
                }    
            
                
                try {
                pstmt = conn.prepareStatement(QUERY_QUESTION_SQL);
                pstmt.setString(1, id);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    // This should be a message back to whoever
                    // published to let them know of a duplicate
                    // assessment question. - H2
                    System.out.print("\tid:     " + id);
                    System.out.print("\ttitle : " + title);
                    System.out.println(" Question already exists");
                    return;
                } 
 
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (pstmt != null) {
                            pstmt.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                

                try {
                    pstmt = conn.prepareStatement(INSERT_QUESTION_SQL);
                    int i = 0;
                    pstmt.setString(++i, id);
                    pstmt.setString(++i, title);
                    pstmt.setString(++i, description);
                    try {
                        pstmt.setCharacterStream(++i, 
                                new StringReader(xml), xml.length());
                    } catch (SQLException se) {
                        // If setCharacterStream is not support
                        // by the driver, we will try to execute
                        // under the normal setString method...
                        // Most support setString for data larger
                        // than 4k, except for Oracle.  I know 
                        // that not all of the jdbc support the
                        // setCharacterStream, so we'll go back
                        // to setString() - H2
                        pstmt.setString(i, xml);
                    }
                    pstmt.executeUpdate();
                } finally {
                    try {
                        if (pstmt != null) {
                            pstmt.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Caching question objects for quicker reference
                questionCache.put(id, new Question(xml));
            } catch (SQLException se) {
                se.printStackTrace();
                throw new AcademusException (se);
            } catch (Exception e) {
                throw new AcademusException(e);
            }
        }
    }

    public static void main (String [] args) throws Exception {
        QuestionFactory.loadFakeQuestions();    
    }
    
    public static void loadFakeQuestions() throws AcademusException{
        // start hack code
        StringBuffer xmBuffer = new StringBuffer();
        xmBuffer.append("<item ident=\"IMS_V01\" title=\"Basic MCSA Question\">");
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
            importQuestion(xmlString);
            
        xmBuffer.delete(0,xmBuffer.length());
        xmBuffer.append("<item title=\"Composite Item\" ident=\"IMS_V02\">");
        xmBuffer.append("<presentation label=\"CompExample001\">");
        xmBuffer.append("<flow>");
        xmBuffer.append("<material>");
        xmBuffer.append("<mattext>Which </mattext>"); 
        xmBuffer.append("<matemtext>city </matemtext> ");
        xmBuffer.append("<mattext>is the capital of </mattext>");
        xmBuffer.append("<matemtext>England </matemtext>");
        xmBuffer.append(" <mattext>and name another city in England ?</mattext>");
        xmBuffer.append("</material>");
        xmBuffer.append("<response_lid ident=\"Comp_MC01\" rcardinality=\"Single\" rtiming=\"No\">");

        xmBuffer.append("<render_choice shuffle=\"Yes\">");
        xmBuffer.append("<response_label ident=\"A\">"); 
        xmBuffer.append("<flow_mat class=\"List\">");
        xmBuffer.append("<material><mattext>Sheffield</mattext></material>");
        xmBuffer.append("</flow_mat>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("<response_label ident=\"B\">"); 
        xmBuffer.append("<flow_mat class=\"List\">");
        xmBuffer.append("<material><mattext>London</mattext></material>");
        xmBuffer.append("</flow_mat>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("<response_label ident=\"C\">"); 
        xmBuffer.append("<flow_mat class=\"List\">");
        xmBuffer.append("<material><mattext>Manchester</mattext></material>");
        xmBuffer.append("</flow_mat>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("<response_label ident=\"D\">"); 
        xmBuffer.append("<flow_mat class=\"List\">");
        xmBuffer.append("<material><mattext>Edinburgh</mattext></material>");
        xmBuffer.append("</flow_mat>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("</render_choice>");
        xmBuffer.append("</response_lid>");
        xmBuffer.append("</flow>");
        xmBuffer.append("</presentation>");
        xmBuffer.append("<resprocessing>");
        xmBuffer.append("<respcondition title=\"Correct\">");
        xmBuffer.append(" <conditionvar>");
        xmBuffer.append("<varequal respident=\"Comp_MC01\">B</varequal>");
        xmBuffer.append("</conditionvar>");
        xmBuffer.append("<setvar action=\"Set\" varname=\"MCSCORE\">1</setvar>");
        xmBuffer.append("</respcondition>");
        xmBuffer.append("</resprocessing>");
        xmBuffer.append("</item>");        
/*
        xmBuffer.delete(0,xmBuffer.length());
        xmBuffer.append("<item ident=\"IMS_V02\" title=\"Basic Example Question\">");
        xmBuffer.append("<presentation label=\"BasicExample002\">");
        xmBuffer.append("<material>");
        xmBuffer.append("<mattext>I am your daddy ?</mattext>");
        xmBuffer.append("</material>");
        xmBuffer.append("<response_lid ident=\"TF02\" rcardinality=\"Single\" rtiming=\"No\">");
        xmBuffer.append("<render_choice>");
        xmBuffer.append("<response_label ident=\"T\">");
        xmBuffer.append("<material><mattext> True </mattext></material>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("<response_label ident=\"F\">");
        xmBuffer.append("<material><mattext> False </mattext></material>");
        xmBuffer.append("</response_label>");
        xmBuffer.append("</render_choice>");
        xmBuffer.append("</response_lid>");
        xmBuffer.append("</presentation>");
        xmBuffer.append("</item>");
*/
        xmlString = xmBuffer.toString();
            importQuestion(xmlString);
        
        xmlString = xmBuffer.toString();
        xmBuffer.delete(0,xmBuffer.length());
        xmBuffer.append("<item title=\"Standard FIB string Item\" ident=\"IMS_V03\">");
        xmBuffer.append("<presentation label=\"BasicExample012b\">");
        xmBuffer.append("<flow>");
        xmBuffer.append("<material>");
        xmBuffer.append("<mattext>Complete the sequence: </mattext>");
        xmBuffer.append("</material>");
        xmBuffer.append("<flow>");
        xmBuffer.append("<material> ");
        xmBuffer.append("<mattext>Winter, Spring, Summer, </mattext>");
        xmBuffer.append("</material>");
        xmBuffer.append("<response_str ident=\"FIB01\" rcardinality=\"Single\" rtiming=\"No\">");
        xmBuffer.append("<render_fib fibtype=\"String\" prompt=\"Dashline\" maxchars=\"6\">");
        xmBuffer.append("<response_label ident=\"A\"/>");
        xmBuffer.append("<material>");
        xmBuffer.append("<mattext>.</mattext>");
        xmBuffer.append("</material>");
        xmBuffer.append("</render_fib>");
        xmBuffer.append("</response_str>");
        xmBuffer.append("</flow>");
        xmBuffer.append("</flow>");
        xmBuffer.append("</presentation>");
        xmBuffer.append("<resprocessing>");
        xmBuffer.append("<respcondition>");
        xmBuffer.append("<conditionvar>");
        xmBuffer.append("<varequal respident=\"FIB01\" case=\"Yes\">Autumn</varequal>");
        xmBuffer.append("</conditionvar>");
        xmBuffer.append("<setvar action=\"Add\" varname=\"FIBSCORE\">1</setvar>");
        xmBuffer.append("</respcondition>");
        xmBuffer.append("</resprocessing>");
        xmBuffer.append("</item>");
        /*
        xmBuffer.append("<item ident=\"IMS_V03\" title=\"Basic Example Question\">");
        xmBuffer.append("   <presentation label=\"BasicExample002\">");
        xmBuffer.append("      <material>");
        xmBuffer.append("              <mattext>Is there a dry heat in Arizona ?</mattext>");
        xmBuffer.append("      </material>");
        xmBuffer.append("      <response_lid ident=\"TF03\" rcardinality=\"Single\" rtiming=\"No\">");
        xmBuffer.append("          <render_choice>");
        xmBuffer.append("              <response_label ident=\"T\">");
        xmBuffer.append("                 <material><mattext>Yes</mattext></material>");
        xmBuffer.append("              </response_label>");
        xmBuffer.append("              <response_label ident=\"F\">");
        xmBuffer.append("                 <material><mattext>No</mattext></material>");
        xmBuffer.append("              </response_label>");
        xmBuffer.append("          </render_choice>");
        xmBuffer.append("      </response_lid>");
        xmBuffer.append("   </presentation>");
        xmBuffer.append("</item> ");
        */
        xmlString = xmBuffer.toString();
        importQuestion(xmlString);        
    }
}
