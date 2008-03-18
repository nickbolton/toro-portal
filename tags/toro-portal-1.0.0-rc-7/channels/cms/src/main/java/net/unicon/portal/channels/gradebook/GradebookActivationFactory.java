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

package net.unicon.portal.channels.gradebook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.unicon.portal.common.service.activation.*;
import net.unicon.sdk.FactoryCreateException;

public final class GradebookActivationFactory {

    public GradebookActivationFactory() {}
    
    public static boolean remove (Activation activation, Connection conn) {
        return __removeFromDB(activation.getActivationID(), conn);
    }

    public static boolean remove (int activationID, Connection conn) {
        return __removeFromDB(activationID, conn);
    }

    private static final String deleteGradebookActivationSQL = 
        "DELETE from gbitem_activation where activation_id = ?";
        
    private static boolean __removeFromDB(int activationId, Connection conn) {
        boolean success = false;
        PreparedStatement pstmt = null;
        try {
            
            pstmt = conn.prepareStatement(deleteGradebookActivationSQL);
            pstmt.setInt(1, activationId);
            pstmt.executeUpdate();
            
            ActivationService actService = ActivationServiceFactory.getService();
            actService.deleteActivation(activationId, conn);

            success = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) { 
            fce.printStackTrace();
        }finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return success;
    }
}
