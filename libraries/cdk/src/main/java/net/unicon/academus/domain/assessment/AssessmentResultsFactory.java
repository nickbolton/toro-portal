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

import net.unicon.academus.domain.assessment.AssessmentResults;

public final class AssessmentResultsFactory {

    public AssessmentResultsFactory () {}

    private static final String USER_RESULTS_SQL = 
        "select assessment_id, activation_id, result_id, data, user_name from assessment_result where result_id = ? ";
        
    public static AssessmentResults getAssessmentResult(int resultId, Connection conn) {
        AssessmentResults rtnResult = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(USER_RESULTS_SQL);

            pstmt.setInt(1, resultId);

            rs = pstmt.executeQuery();

            if (rs.next() ) {
                rtnResult = new AssessmentResults(rs.getString("data"));
                rtnResult.setResultID(resultId);
                rtnResult.setAssessmentID(rs.getString("assessment_id"));
                rtnResult.setActivationID(rs.getString("activation_id"));
            }
        } catch (SQLException se) {
            se.printStackTrace();    
        } finally {
            try {
                if (rs != null) { rs.close(); }
                rs = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }

            try {
                if (pstmt != null) { pstmt.close(); }
                pstmt = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return rtnResult;
    }
}
