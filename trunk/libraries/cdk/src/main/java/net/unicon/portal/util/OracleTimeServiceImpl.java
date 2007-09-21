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
 
package net.unicon.portal.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import net.unicon.sdk.time.ADBTimeService;
import org.jasig.portal.RDBMServices;
/*  This allows user to obtain the current date and time from the database.  We assume you have oracle setup.
 *
*/
public class OracleTimeServiceImpl extends ADBTimeService {
    
    private static final String sysDateSQL = "SELECT sysdate from dual";
    
    /** Constructor */
    public OracleTimeServiceImpl() { }
    
    /* This method returns the current time.
     * @return <code>Timestamp</code> the current time off of the system.
     */
    public Timestamp getTimestamp() {
        Timestamp timestamp = null;
        Connection conn = RDBMServices.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sysDateSQL);
            
            rs = pstmt.executeQuery();
            
            if (rs.next() ) {
                timestamp = rs.getTimestamp("sysdate");
            }
        } catch (SQLException se) {
            se.printStackTrace();
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
            RDBMServices.releaseConnection(conn);
        }
        return timestamp;
    }
    
    /* This method returns the current time.
     * @return <code>String</code> that represents the current date in SimpleDateFormat("MM/dd/yyyy").
    */
    public String getCurrentDate() {
        return super.getCurrentDate();
    }
    /* This method returns the current time.
     * @return <code>String</code> that represents the current time in SimpleDateformat("H:mm").
    */
    public String getCurrentTime() {
        return super.getCurrentTime(); 
    }
    
    /* This method returns the current date and time.
     * @return <code>String</code> that represents the current date and time in SimpleDateFormat("MM/dd/yyyy HH:mm"). 
    */ 
    public String getCurrentDateTime() {
        return super.getCurrentDateTime();
    }
    
    
    
}
