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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.StringReader;

import net.unicon.sdk.time.*;
import net.unicon.portal.util.db.AcademusDBUtil;

/**
 * <p>
 *
 * </p>
 */
public final class AssessmentInstanceFactory {
    private static Map assInstanceCache = new HashMap();
    
    
    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    private AssessmentInstanceFactory() {}

    /**
     * <p>
     * Returns an cloned <code>AssessmentInstance</code>(s) based
     * on passed in assessment id.  The assessment instance is
     * a manipulatable version of the assessment.  The reason its
     * cloned is because there is already build Assessment Instances
     * in memory. So instead of recreating one, we just clone the one
     * in memory
     * </p><p>
     *
     * @param a String with the assessment Id.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static AssessmentInstance getAssessmentInstance(String id) {
        AssessmentInstance tempAssInstance = null;
        AssessmentInstance rtnAssInstance  = null;
        
        tempAssInstance = __getCache(id);

        if (tempAssInstance == null) {
            Connection conn = null;
            try {
                conn = AcademusDBUtil.getDBConnection();
                tempAssInstance = __getInstanceFromDB(id, conn);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                AcademusDBUtil.safeReleaseDBConnection(conn);
            }
        }
        
        // Cloning instance
        if (tempAssInstance != null) {
            try {
                rtnAssInstance = (AssessmentInstance) tempAssInstance.clone();
            } catch (Exception e) {
                System.out.println("Unable to clone Assessment Instance");
                e.printStackTrace();
            }
        } 
        return rtnAssInstance;
    }

    /**
     * <p>
     * Returns an cloned <code>AssessmentInstance</code>(s) based
     * on passed in assessment id.  The assessment instance is
     * a manipulatable version of the assessment.  The reason its
     * cloned is because there is already build Assessment Instances
     * in memory. So instead of recreating one, we just clone the one
     * in memory
     * </p><p>
     *
     * @param a String with the assessment Id.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static AssessmentInstance getAssessmentInstance(
                                        String id, 
                                        Connection conn) throws Exception {
        AssessmentInstance tempAssInstance = null;
        AssessmentInstance rtnAssInstance  = null;
        
        tempAssInstance = __getCache(id);

        if (tempAssInstance == null) {
            tempAssInstance = __getInstanceFromDB(id, conn);
        }

        if (tempAssInstance != null) {
            try {
                rtnAssInstance = (AssessmentInstance) tempAssInstance.clone();
            } catch (Exception e) {
                System.out.println("Unable to clone Assessment Instance");
                e.printStackTrace();
            }
            return rtnAssInstance;
        } 
        return rtnAssInstance;
    }

    /**
     * <p>
     * Returns an cloned <code>AssessmentInstance</code>(s) based
     * on passed in result id. This is the users complete copy of
     * there attempt on an assessment.  It contains assessment and
     * and question results in addition to responses the user 
     * may have entered while taking the assesment.
     * </p><p>
     *
     * @param a int with the result Id.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static AssessmentInstance getAssessmentResults(int resultId) {
        AssessmentInstance rtnAssessment = null;
        Connection conn = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            rtnAssessment = getAssessmentResults(resultId, conn); 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AcademusDBUtil.safeReleaseDBConnection(conn);
        }
        return rtnAssessment;
    }

    /**
     * <p>
     * Returns an cloned <code>AssessmentInstance</code>(s) based
     * on passed in result id. This is the users complete copy of
     * there attempt on an assessment.  It contains assessment and
     * and question results in addition to responses the user 
     * may have entered while taking the assesment.
     * </p><p>
     *
     * @param a int with the result Id.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static AssessmentInstance getAssessmentResults(int resultId, Connection conn) {
        AssessmentInstance rtnAssessment = null;
        try {
            AssessmentResults assResults = 
                AssessmentResultsFactory.getAssessmentResult(resultId, conn);
                
            if (assResults != null) {
                rtnAssessment = getAssessmentInstance(assResults.getAssessmentID());

                if (rtnAssessment != null) {
                    // Setting the assessment results
                    rtnAssessment.setResult(assResults);
                    
                    // Setting the Question Results
                    List qIs = QuestionInstanceFactory.getQuestionResults(resultId, conn);
                    rtnAssessment.setQuestionInstances(qIs);
                }
            }
        } catch (Exception e) {
        }
        return rtnAssessment;
    }
    
    /**
     * <p>
     * Returns an Assessment Instance from cache
     * </p><p>
     *
     * @param a String with the assessment Id.
     *
     * @return an AssessmentInstance object.
     * </p>
     */
    private static AssessmentInstance __getCache(String id) {
        return (AssessmentInstance) assInstanceCache.get(id);
    }

    /**
     * <p>
     * Sets an Assessment Instance in cache
     * </p><p>
     *
     * @param a String with the assessment Id.
     *
     * @param an AssessmentInstance object.
     * </p>
     */
    private static void __putCache(String id, AssessmentInstance instance) {
        assInstanceCache.put(id, instance);
    }

    
    /**
     * <p>
     * Returns an <code>AssessmentInstance</code>(s) based
     * on passed in assessment id from the database.
     * </p><p>
     *
     * @param a String with the assessment Id.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    private static AssessmentInstance __getInstanceFromDB(String id, Connection conn) {
       AssessmentInstance rtnAssInstance  = null;
       Assessment assessment = AssessmentFactory.getAssessment(id, conn);
       
       if (assessment != null) {
           rtnAssInstance = new AssessmentInstance(assessment);
       } else {
           System.out.println("Cannot find Assessment with id" + id);
       }
       return rtnAssInstance;
    }

    private static final String INITIALIZE_ASSESSMENT_SQL = 
        "insert into assessment_result (assessment_id, activation_id, user_name, start_time, data, count_attempt) values (?,?,?,?,?,?)";
        
    private static final String RESULT_ID_SELECT_SQL = 
        "select result_id from assessment_result where assessment_id = ? and activation_id = ? and user_name = ? and start_time = ?";

    /**
     * <p>
     * Intialized an <code>AssessmentInstance</code>(s) based
     * on passed in assessment for the database.  The initialzation
     * captures the users attempt for this assessment in the
     * database.  This saves information suchs as what order the
     * students questions were in, etc.
     * </p><p>
     *
     * @param a ActivationInstance with the users activation
     * @param an AssessmentInstance with the user assessment.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static void initialize(ActivationInstance activation, 
        AssessmentInstance assessment, Connection conn) 
    {
        assessment.setUsername(activation.getUsername());

        try {
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            TimeService ts = TimeServiceFactory.getService();
            java.sql.Timestamp startTime = ts.getTimestamp();
            assessment.setStartTime(startTime); 

            try {
            pstmt = conn.prepareStatement(INITIALIZE_ASSESSMENT_SQL);
            
            int i = 0;
            
            pstmt.setString(++i, assessment.getId());
            pstmt.setInt   (++i, activation.getActivationID());
            pstmt.setString(++i, assessment.getUsername());
            pstmt.setTimestamp(++i, startTime);
            String xml = assessment.getResult().toXML();
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
            
            assessment.incrementRetry(false);
            pstmt.setInt(++i, assessment.countAsRetry() ? 1 : 0);
            pstmt.executeUpdate();

            } finally {
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
            pstmt = conn.prepareStatement(RESULT_ID_SELECT_SQL);
            int i = 0;
            pstmt.setString(++i, assessment.getId());
            pstmt.setInt   (++i, activation.getActivationID());
            pstmt.setString(++i, assessment.getUsername());
            pstmt.setTimestamp(++i, startTime);
            rs = pstmt.executeQuery();
            
            int resultId = 0;
            if (rs.next() ) {
                resultId = rs.getInt("result_id");
                assessment.setResultId(resultId); 
                try {
                    List questions = assessment.getQuestionInstances();
                
                    for (int ix = 0; ix < questions.size(); ++ix) {
                        QuestionInstance question = (QuestionInstance) questions.get(ix);
                        question.setResultID(resultId);
                        question.setUsername(assessment.getUsername());
                        QuestionInstanceFactory.initialize(question, conn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String UPDATE_ASSESSMENT_SQL =
        "update assessment_result set data = ?, count_attempt = ? where result_id = ?";

    /**
     * <p>
     * Saves an <code>AssessmentInstance</code>(s) based
     * on passed in assessment for the database.  
     * </p><p>
     *
     * @param an AssessmentInstance with the user assessment.
     * @param a Connection with a database connection.
     *
     * @return a clone of this AssessmentInstance object.
     * </p>
     */
    public static void save(AssessmentInstance assessment, Connection conn) 
    {
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(UPDATE_ASSESSMENT_SQL);
            
            int i = 0;

            String xml = assessment.getResult().toXML();
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
            pstmt.setInt(++i,  assessment.countAsRetry() ? 1 : 0);
            pstmt.setInt(++i, assessment.getResultId());
            pstmt.executeUpdate();
            
            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
            }

            try {
                List questions = assessment.getQuestionInstances();
                 
                // Saving question Instance
                for (int ix = 0; ix < questions.size(); ++ix) {
                    QuestionInstance question = (QuestionInstance) questions.get(ix);
                    QuestionInstanceFactory.save(question, conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
    }

    public static void main (String [] args) throws Exception {
        AssessmentInstance ass = AssessmentInstanceFactory.getAssessmentResults(50);

                
        System.out.println(ass.getResult().toXML());
        
        List qi = ass.getQuestionInstances();
        for (int ix = 0; ix < qi.size(); ++ix) {
            System.out.println("\n");
            System.out.println(((QuestionInstance) qi.get(ix)).getResult().toXML() );
            System.out.println("\n");
        }
        
        System.exit(1);
    }
}
