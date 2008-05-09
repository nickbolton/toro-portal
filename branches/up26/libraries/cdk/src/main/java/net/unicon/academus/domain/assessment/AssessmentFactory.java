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

public final class AssessmentFactory {
    
    private static Map assessmentCache = new HashMap();

    private AssessmentFactory () {}
    
    public static Assessment getAssessment(String id) {
        Assessment rtnAssessment = null;

        rtnAssessment = (Assessment) assessmentCache.get(id);

        if (rtnAssessment != null) {
            return rtnAssessment;
        }
        Connection conn = null;
        try { 
            conn = AcademusDBUtil.getDBConnection();
            rtnAssessment =  __getAssessmentFromDB(id, conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
        return rtnAssessment;
    }
    
    public static Assessment getAssessment(String id, Connection conn) {
        Assessment rtnAssessment = null;

        rtnAssessment = (Assessment) assessmentCache.get(id);

        if (rtnAssessment != null) {
            return rtnAssessment;
        }

        return __getAssessmentFromDB(id, conn);
    }

    private static final String GET_ASSESSMENT_SQL = 
        "select name, data from assessment where id = ? order by UPPER(name) ASC";
        
    private static Assessment __getAssessmentFromDB(String id, Connection conn) {
        Assessment rtnAssessment = null;

        if (id != null) {
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement(GET_ASSESSMENT_SQL);
                pstmt.setString(1, id);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    rtnAssessment = new Assessment(rs.getString("data"));
                }
            
                // Caching question objects for quicker reference
                assessmentCache.put(id, rtnAssessment);
            } catch (SQLException e) {
                e.printStackTrace();
            } // end try-catch
            finally {
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
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        } // end if
        return rtnAssessment;
    }

    
    private static final String FIND_ASSESSMENT_SQL =
        "select id, name, data from assessment";
    private static final String TITLE_SEG_SQL =
        " UPPER(NAME) LIKE '%' || UPPER(?) || '%'";
    private static final String DESCRIPTION_SEG_SQL =
        " UPPER(DESCRIPTION) LIKE '%' || UPPER(?) || '%'";

    private static final String ORDER_BY_NAME_SQL =
        " ORDER BY UPPER(name) ASC";
    public static List findAssessment(SearchCriteria parms) {
        // We are only going to search on first name for now
        List rtnList = new ArrayList();
        Connection conn = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            // XXX Need SQL Code
            rtnList = __findAssessments(parms,conn);
        
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
        return rtnList;
    }

    private static List __findAssessments(SearchCriteria parms, Connection conn) {
        List rtnList = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String title = parms.getName();
            String description  = parms.getDescription();

            StringBuffer sql = new StringBuffer(FIND_ASSESSMENT_SQL);
            if (title != null && description != null) {
                sql.append(" WHERE");
                sql.append(TITLE_SEG_SQL);
                
                if (parms.matchAllCriteria()) {
                    sql.append(" AND ");
                } else {
                    sql.append(" OR ");
                }
                sql.append(DESCRIPTION_SEG_SQL);
                sql.append(ORDER_BY_NAME_SQL);
                 
                pstmt = conn.prepareStatement(sql.toString());
                int i = 0;
                pstmt.setString(++i, title);
                pstmt.setString(++i, description);
            } else if (title != null) {
                sql.append(" WHERE");
                sql.append(TITLE_SEG_SQL);
                sql.append(ORDER_BY_NAME_SQL);
                
                pstmt = conn.prepareStatement(sql.toString());
                int i = 0;
                pstmt.setString(++i, title);
            } else if (description != null) {
                sql.append(" WHERE");
                sql.append(DESCRIPTION_SEG_SQL);
                sql.append(ORDER_BY_NAME_SQL);
                
                pstmt = conn.prepareStatement(sql.toString());
                int i = 0;
                pstmt.setString(++i, description);
            }
            else {
                sql.append(ORDER_BY_NAME_SQL);
                pstmt = conn.prepareStatement(sql.toString());
            }
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String xml = rs.getString("data");
                rtnList.add(new Assessment(xml));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rtnList;
    }
    
    public static List getAssessments() {
        List rtnList = new ArrayList();
        Connection conn = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            rtnList = getAssessments(conn);
        
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
        return rtnList;
    }

    private static final String GET_ALL_ASSESSMENTS =
        "select data from assessment";

    public static List getAssessments(Connection conn) {
        List rtnList = new ArrayList();
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(GET_ALL_ASSESSMENTS);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                rtnList.add(new Assessment(rs.getString(1)));
            }
            
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
        return rtnList;
    }
    
    private static final String QUERY_ASSESSMENT_SQL =
        "select id from assessment where id = ?";
    
    public static void importAssessment(String xml) 
    throws AcademusException {
        Connection conn = null;
        try { 
            conn = AcademusDBUtil.getDBConnection();
            importAssessment(xml, conn);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AcademusException(e);
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
    } 

    private static final String INSERT_ASSESSMENT_SQL = 
        "insert into assessment (id, name, description, data) values (?,?,?,?)";
        
    public static void importAssessment(String xml, Connection conn) 
    throws AcademusException {
        if (xml != null) {
            
            try {
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                /* Getting XML data */
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(xml)));

                XPathAPI xpath = new XPathAPI();
                Node node = xpath.selectSingleNode(document, "assessment");
            
                String id          = null;
                String title       = null;
                String description = null;
                if (node != null) {
                    title = ((Element) node).getAttribute("title");
                    id    = ((Element) node).getAttribute("ident");
                }
            
                node = xpath.selectSingleNode(document, "description");
            
                if (node != null) {
                    description = node.getFirstChild().getNodeValue();
                }    
            
                
                try {
                    pstmt = conn.prepareStatement(QUERY_ASSESSMENT_SQL);
                    pstmt.setString(1, id);
                    rs = pstmt.executeQuery();

                    if (rs.next()) {
                        throw new AcademusException (
                        "Assessment already exists");
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
                    pstmt = conn.prepareStatement(INSERT_ASSESSMENT_SQL);
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

                // Caching assessment objects for quicker reference
                assessmentCache.put(id, new Assessment(xml));
            } catch (SQLException se) {
                se.printStackTrace();
                throw new AcademusException (se);
            } catch (Exception e) {
                throw new AcademusException(e);
            } // end try-catch
        }
    }
    
    public static void loadFakeAssessment() {
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
             importAssessment(xmlString);
         } catch (Exception e) {
             e.printStackTrace();
         }
         // Caching question objects for quicker reference
         //assessmentCache.put(rtnAssessment.getId(), rtnAssessment);
    }
}
