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

package net.unicon.portal.channels.curriculum.base;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

/*
    ALTER TABLE curriculum ADD COLUMN content_type character varying (35);
*/


import net.unicon.academus.delivery.DeliveryAdapter;
import net.unicon.academus.delivery.DeliveryAdapterFactory;
import net.unicon.academus.delivery.DeliveryCurriculum;
import net.unicon.academus.delivery.DeliveryCurriculumImpl;
import net.unicon.academus.delivery.ReferenceObject;
import net.unicon.academus.delivery.virtuoso.content.IContentGroup;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.curriculum.Curriculum;
import net.unicon.portal.channels.curriculum.CurriculumService;
import net.unicon.portal.common.*;
import net.unicon.sdk.properties.*;
import net.unicon.portal.common.properties.*;

import org.jasig.portal.RDBMServices;

public class CurriculumServiceImpl implements CurriculumService {

    protected static final String INSERT_DELIVERY_COURSE_SQL =

    "INSERT INTO acad_delivery_curriculum (delivery_system_id, delivery_curriculum_id, " +

    " curriculum_title, curriculum_description) VALUES (?,?,?,?)";

    protected static final String UPDATE_DELIVERY_COURSE_SQL =

    "UPDATE acad_delivery_curriculum SET curriculum_title=?, curriculum_description=? " +

    " WHERE delivery_system_id=? AND delivery_curriculum_id=?";

    protected static final String DELETE_DELIVERY_COURSE_SQL =

    "DELETE FROM acad_delivery_curriculum WHERE delivery_system_id=? AND delivery_curriculum_id=?";

    protected static final String DELETE_DELIVERY_COURSES_FROM_SYSTEM_SQL =

    "DELETE FROM acad_delivery_curriculum WHERE delivery_system_id=";

    protected static final String DELETE_ALL_DELIVERY_COURSES_SQL =

    "DELETE FROM acad_delivery_curriculum";

    protected static final String SELECT_DELIVERY_CURRICULUM_SQL =

    "SELECT delivery_system_id, delivery_curriculum_id, curriculum_title, curriculum_description FROM " +

    "acad_delivery_curriculum WHERE delivery_system_id=? AND delivery_curriculum_id=?";

    protected static final String insertSQL =

    "INSERT INTO curriculum (offering_id, name, description, type, reference, content_type, theme, style) values (?,?,?,?,?,?,?,?)";

    protected static final String deleteSQL =

    "DELETE FROM curriculum where offering_id = ? and curriculum_id = ?";

    protected static final String deleteALLSQL =

    "DELETE FROM curriculum where offering_id = ?";

    protected static final String selectSQL =

    "SELECT curriculum_id, offering_id, name, description, type, reference, content_type, theme, style FROM curriculum WHERE offering_id = ?";

    protected static final String selectTypeSQL =

    "SELECT curriculum_id, offering_id, name, description, type, reference, content_type, theme, style FROM curriculum WHERE offering_id = ? and type = ?";

    public CurriculumServiceImpl () { }

    public List getAvailableCurriculum(IContentGroup[] cGroups, User user) {

        // Convert to strings...
        List str = new ArrayList();
        Iterator it = Arrays.asList(cGroups).iterator();
        while (it.hasNext()) {
            IContentGroup g = (IContentGroup) it.next();
            str.add(g.getHandle());
        }

        List rslt = new ArrayList();
    //Systemout.println("IN CSI::getAvailableCurr");

        if ( ((String) UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.academus.delivery.DeliveryAdapter")).equals("true")) {
            try {

                DeliveryAdapter delService = (DeliveryAdapter) DeliveryAdapterFactory.getAdapter();
                rslt = delService.getAllCourses((String[]) str.toArray(new String[0]), user);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rslt;

    }

    public List getCurriculum (Offering offering,
                    User user,
                    Connection conn) {
        return this.getCurriculum (offering, user, false, conn);
    }

    public List getCurriculum (Offering offering, User user, boolean convertReference, Connection conn) {

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        List content = new ArrayList();

        try {

            pstmt = conn.prepareStatement(selectSQL);

            int i = 0;

            pstmt.setInt(++i, (int) offering.getId());

            rs = pstmt.executeQuery();

            Curriculum curr = null;

            while (rs.next()) {

                curr =  (Curriculum) new CurriculumImpl(
                            "" + rs.getInt("curriculum_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            (int) offering.getId(),
                            rs.getString("type"),
                            rs.getString("reference"),
                            rs.getString("content_type"),
                            rs.getString("theme"),
                            rs.getString("style")
                        );

                if (Curriculum.ONLINE.equals(rs.getString("type")) && convertReference) {

                    String url =

                    this.getCurriculumLink(rs.getString("reference"),
                                        user, offering, conn, false);

                    curr.setReference(url);

                }

                content.add(curr);

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
        }

        return content;
    }
    
    /**
     * incorporates flag for viewing Instructor Notes
     */
    public List getCurriculum (Offering offering, User user, boolean convertReference, Connection conn, boolean viewInstructorNotes) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List content = new ArrayList();

        try {

            pstmt = conn.prepareStatement(selectSQL);
            int i = 0;
            pstmt.setInt(++i, (int) offering.getId());
            rs = pstmt.executeQuery();
            Curriculum curr = null;

            while (rs.next()) {

                curr =  (Curriculum) new CurriculumImpl(
                            "" + rs.getInt("curriculum_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            (int) offering.getId(),
                            rs.getString("type"),
                            rs.getString("reference"),
                            rs.getString("content_type"),
                            rs.getString("theme"),
                            rs.getString("style")
                        );

                if (Curriculum.ONLINE.equals(rs.getString("type")) && convertReference) {

                    String url =
                    this.getCurriculumLink(rs.getString("reference"),
                                        user, offering, conn, viewInstructorNotes);
                    curr.setReference(url);
                }

                content.add(curr);
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
        }
        return content;
    }
    
    public List getCurriculum (

    String type,

    Offering offering,

    User user,

    Connection conn) {

        return this.getCurriculum (type, offering, user, false, conn);

    }

    public List getCurriculum (

    String type,

    Offering offering,

    User user,

    boolean convertReference,

    Connection conn) {

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        List content = new ArrayList();

        try {

            pstmt = conn.prepareStatement(selectTypeSQL);

            int i = 0;

            pstmt.setInt(++i, (int) offering.getId());

            pstmt.setString(++i, type);

            rs = pstmt.executeQuery();

            Curriculum curr = null;

            while (rs.next()) {

                curr = (Curriculum) new CurriculumImpl(
                                "" + rs.getInt("curriculum_id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                (int) offering.getId(),
                                rs.getString("type"),
                                rs.getString("reference"),
                                rs.getString("content_type"),
                                rs.getString("theme"),
                                rs.getString("style")
                            );

                if (Curriculum.ONLINE.equals(rs.getString("type")) && convertReference) {

                    String url = this.getCurriculumLink(rs.getString("reference"), user, offering, conn, false);

                    curr.setReference(url);

                }

                content.add(curr);

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
        }

        return content;

    }

    public DeliveryCurriculum getDeliveryCurriculum(String deliverySystemID,

    String deliveryCurriculumID,

    Connection conn) {

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        DeliveryCurriculum rval = null;

        try {

            pstmt = conn.prepareStatement(SELECT_DELIVERY_CURRICULUM_SQL);

            pstmt.setString(1, deliverySystemID);

            pstmt.setString(2, deliveryCurriculumID);

            rs = pstmt.executeQuery();

            if (rs.next()) {

                rval = new DeliveryCurriculumImpl(rs.getString(1),

                rs.getString(2),

                rs.getString(3),

                rs.getString(4));

            }

        }

        catch (Exception exc) {

            exc.printStackTrace();

        }

        finally {

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

        return rval;

    }

    public void saveCurriculum(String name, String description, String type,
                                    String reference, String contentType,
                                        Offering offering, String theme,
                                        String style, Connection conn) {


        PreparedStatement pstmt = null;

        try {

            pstmt = conn.prepareStatement(insertSQL);

            int i = 0;

            pstmt.setInt(++i, (int) offering.getId());

            pstmt.setString(++i, name);

            pstmt.setString(++i, description);

            pstmt.setString(++i, type);

            pstmt.setString(++i, reference);

            pstmt.setString(++i, contentType);

            pstmt.setString(++i, theme);

            pstmt.setString(++i, style);

            pstmt.executeUpdate();

        } catch (SQLException se) {

            se.printStackTrace();

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

    }

    public void deleteCurriculum(

    int curriculum_id,

    Offering offering,

    Connection conn) {

        PreparedStatement pstmt = null;

        try {

            pstmt = conn.prepareStatement(deleteSQL);

            int i = 0;

            pstmt.setInt(++i, (int) offering.getId());

            pstmt.setInt(++i, curriculum_id);

            pstmt.executeUpdate();

        } catch (SQLException se) {

            se.printStackTrace();

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

    }

    public void deleteOfferingCurriculum(

    Offering offering,

    Connection conn) {

        PreparedStatement pstmt = null;

        try {

            pstmt = conn.prepareStatement(deleteALLSQL);

            int i = 0;

            pstmt.setInt(++i, (int) offering.getId());

            pstmt.executeUpdate();

        } catch (SQLException se) {

            se.printStackTrace();

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

    }

    public String getCurriculumLink(String reference, User user, Offering o, Connection conn, boolean viewInstructorNotes) {
        return null;
    }

    public int addDeliveryCourses(List contentList) {

        Connection conn = null;

        PreparedStatement pstmt = null;

        int rval = 0;

        DeliveryCurriculum curr = null;
        CurriculumImpl curr2 = null;

        try {

            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(INSERT_DELIVERY_COURSE_SQL);

            for (Iterator it = contentList.iterator(); it.hasNext(); ) {

                Object o = it.next();

// System.out.println("\tclass="+o.getClass().getName());

                if (o instanceof DeliveryCurriculum)	{
                	
                	// The curriculum is a delivery type curriculum
                	curr = (DeliveryCurriculum) o;

                	pstmt.clearParameters();
                    pstmt.setString(1, "Virtuoso");
                    pstmt.setString(2, curr.getCurriculumId());
                    pstmt.setString(3, curr.getTitle());
                    pstmt.setString(4, curr.getDescription());
                    
                } else {
                	
                	curr2 = (CurriculumImpl) o;
               
                	pstmt.clearParameters();
                    pstmt.setString(1, "Virtuoso");
                    pstmt.setString(2, curr2.getId());
                    pstmt.setString(3, curr2.getName());
                    pstmt.setString(4, curr2.getDescription());
                    
                }

                try {

                    rval += pstmt.executeUpdate();

                }

                catch (SQLException se) {

                    se.printStackTrace();

                }

            }

        }

        catch (SQLException se2) {

            se2.printStackTrace();

            return 0;

        }

        finally {

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

        return rval;

    }

    public int updateDeliveryCourses(List contentList) {

        Connection conn = null;

        PreparedStatement pstmt = null;

        int rval = 0;

        Curriculum curr = null;

        try {

            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(UPDATE_DELIVERY_COURSE_SQL);

            for (Iterator it = contentList.iterator(); it.hasNext(); ) {

                curr = (Curriculum)it.next();

                pstmt.clearParameters();

                pstmt.setString(1, curr.getName());

                pstmt.setString(2, curr.getDescription());

                pstmt.setString(3, "Virtuoso");

                pstmt.setString(4, curr.getReference());

                try {

                    rval += pstmt.executeUpdate();

                }

                catch (SQLException se) {

                    se.printStackTrace();

                }

            }

        }

        catch (SQLException se2) {

            se2.printStackTrace();

        }

        finally {
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

        return rval;

    }

    public int deleteDeliveryCourses(List contentList) {

        Connection conn = null;

        PreparedStatement pstmt = null;

        int rval = 0;

        Curriculum curr = null;

        try {

            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(DELETE_DELIVERY_COURSE_SQL);

            for (Iterator it = contentList.iterator(); it.hasNext(); ) {

                curr = (Curriculum)it.next();

                pstmt.clearParameters();

                pstmt.setString(1, "Virtuoso");

                pstmt.setString(2, curr.getReference());

                try {

                    rval += pstmt.executeUpdate();

                }

                catch (SQLException se) {

                    se.printStackTrace();

                }

            }
        }
        catch (SQLException se2) {
            se2.printStackTrace();
        }
        finally {
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
        return rval;
    }

    // when called with a String, remove all courses from a given delivery system
    public int deleteDeliveryCourses(String systemID) {

        Connection conn = null;

        Statement stmt = null;

        int rval = 0;

        Curriculum curr = null;

        try {

            conn = RDBMServices.getConnection();
            // Using a Statement instead of a PreparedStatement on purpose. This query
            // does not happen often enough and with a high enough performance
            // constraint to tie up database-side resources with a PS

            stmt = conn.createStatement();

            StringBuffer queryBuf = new StringBuffer(20);

            queryBuf.append(DELETE_DELIVERY_COURSES_FROM_SYSTEM_SQL);
            queryBuf.append("'");
            queryBuf.append(systemID);
            queryBuf.append("'");

            rval = stmt.executeUpdate(queryBuf.toString());
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
            RDBMServices.releaseConnection(conn);
        }
        return rval;
    }

    // when called with no params, delete _everything_

    public int deleteDeliveryCourses() {

        Connection conn = null;

        Statement stmt = null;

        int rval = 0;

        Curriculum curr = null;

        try {
            conn = RDBMServices.getConnection();

            // Using a Statement instead of a PreparedStatement on purpose. This query

            // does not happen often enough and with a high enough performance

            // constraint to tie up database-side resources with a PS

            stmt = conn.createStatement();

            rval = stmt.executeUpdate(DELETE_ALL_DELIVERY_COURSES_SQL);
        }

        catch (SQLException se) {

            se.printStackTrace();

        }

        finally {

            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se2) {

                se2.printStackTrace();

            }
            RDBMServices.releaseConnection(conn);
        }

        return rval;

    }

}

