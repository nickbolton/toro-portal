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
import net.unicon.portal.util.db.AcademusDBUtil;

public final class QuestionInstanceFactory {
    
    private static Map instanceCache = new HashMap();

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public QuestionInstanceFactory() {}

    /**
     * <p>
     * Returns an cloned <code>QuestionInstance</code>(s) based
     * on passed in question id.  The question instance is
     * a manipulatable version of the question.  The reason its
     * cloned is because there is already built Question Instances
     * in memory. So instead of recreating one, we just clone the one
     * in memory
     * </p><p>
     *
     * @param a String with the question Id.
     *
     * @return a clone of this QuestionInstance object.
     * </p>
     */
    public static QuestionInstance getQuestionInstance(String id) {
        QuestionInstance rtnInstance = null;
        QuestionInstance temp = null;
        
        temp = __getCache(id);

        if (temp == null) {
            Connection conn = null;
            try {
                conn = AcademusDBUtil.getDBConnection();
                temp = __getInstanceFromDB(id, conn);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                AcademusDBUtil.safeReleaseDBConnection(conn);
            }
        }
        if (temp != null) {
            try {
                rtnInstance = (QuestionInstance) temp.clone();
            } catch (Exception e) {
                System.out.println("Unable to clone Question Instance");
                e.printStackTrace();
            }
        } 
        return rtnInstance;
    }
    
    /**
     * <p>
     * Returns an Question Instance from the database
     * </p><p>
     *
     * @param a String with the question Id.
     * @param a Connections with a database connection.
     *
     * @return an QuestionInstance object.
     * </p>
     */
    private static QuestionInstance __getInstanceFromDB(String id, Connection conn) {
       QuestionInstance rtnQuestInstance  = null;
       Question question = QuestionFactory.getQuestion(id, conn);
       
       if (question != null) {
           rtnQuestInstance = new QuestionInstance(question);
       } else {
           System.out.println("Cannot find Question with id" + id);
       }
       return rtnQuestInstance;
    }

    /**
     * <p>
     * Returns an Question Instance from cache
     * </p><p>
     *
     * @param a String with the question Id.
     *
     * @return an QuestionInstance object.
     * </p>
     */
    private static QuestionInstance __getCache(String id) {
        return ((QuestionInstance) instanceCache.get(id));
    }

    private static final String INITIALIZE_QUESTION_SQL =
        "insert into question_result (question_id, result_id, order_number, data) values (?,?,?,?)";

    /**
     * <p>
     * Initiailizes an Question Instance of the user to the database
     * </p><p>
     *
     * @param a Question Instance to save.
     * @param a Connections with a database connection.
     * </p>
     */
    protected static void initialize(QuestionInstance question, Connection conn) {
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(INITIALIZE_QUESTION_SQL);
            
            int i = 0;
            pstmt.setString(++i, question.getId());
            pstmt.setInt   (++i, question.getResultID());
            pstmt.setInt   (++i, question.getPositionNumber());
            pstmt.setString(++i, question.getResult().toXML());
            pstmt.executeUpdate();

            if (pstmt != null) {
                pstmt.close();
                pstmt = null;
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

    private static final String UPDATE_QUESTION_SQL =
        "update question_result set data = ? where result_id = ? and order_number = ?";

    /**
     * <p>
     * Saves an Question Instance of the user to the database
     * </p><p>
     *
     * @param a Question Instance to save.
     * </p>
     */
    public static void save (QuestionInstance question) {
        if (question != null) {
            Connection conn = null;
            try {
                conn = AcademusDBUtil.getDBConnection();
                save(question, conn);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                AcademusDBUtil.safeReleaseDBConnection(conn);
            }
        }
    }
    
    /**
     * <p>
     * Saves an Question Instance of the user to the database
     * </p><p>
     *
     * @param a Question Instance to save.
     * @param a Connections with a database connection.
     * </p>
     */
    public static void save (QuestionInstance question, Connection conn) {
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(UPDATE_QUESTION_SQL);
            
            int i = 0;
            pstmt.setString(++i, question.getResult().toXML());
            pstmt.setInt   (++i, question.getResultID());
            pstmt.setInt   (++i, question.getPositionNumber());
            pstmt.executeUpdate();
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

    private static final String USER_RESULTS_SQL =
        "select question_id, order_number, data from question_result where result_id = ? order by order_number";

    /**
     * <p>
     * Retreives the question results for the question.  This returns
     * the list of QuestionInstance with the QuestionResults inside.
     * </p><p>
     *
     * @param an int with the user's result id
     * @param a Connection with a database connection.
     *
     * @return a List with the Question instances with the users 
     * questions results inside.
     * </p>
     */
    public static List getQuestionResults(int resultId, Connection conn) {
        List rtnQuestion = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(USER_RESULTS_SQL);

            pstmt.setInt(1, resultId);

            rs = pstmt.executeQuery();
            
            QuestionInstance qI = null;
            while (rs.next() ) {
                qI = getQuestionInstance(rs.getString("question_id"));
                qI.setResult(new QuestionResults(rs.getString("data")));
                qI.setPositionNumber(rs.getInt("order_number"));
                // Adding question to the list.
                rtnQuestion.add(qI);
            }
            
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (rs != null) {rs.close();}
                rs    = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (pstmt != null) { pstmt.close();}
                pstmt = null;

            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return rtnQuestion;
    }
}
