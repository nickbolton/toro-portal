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

package net.unicon.academus.domain.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.IDomainEventHandler;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.util.db.AcademusDBUtil;

/**
 * Implements a delete event handler responsible to delete any user-associated
 * data that does no belong to any specific application. The data mostly
 * consists of user-related uPortal tables.
 *
 */
public class UserDeleteEventHandler implements IDomainEventHandler {

    public UserDeleteEventHandler() {}

    public void handleEvent(IDomainEntity event) throws DomainException {

		  // Assertions.
		  if (event == null) {

			  String msg = "Argument 'event [IDomainEntity]' cannot be null.";
				  throw new IllegalArgumentException(msg);
		  }

		  // Delete all framework channels
		  User user = null;
          String username = null;
          int user_id = -1;
          PreparedStatement pstmt = null;
          Connection conn = null;
          ResultSet rs = null;
          String sql = null;
          boolean loggedIn = false;

		  try {

			  user = (User) event;
              username = user.getUsername();

              conn = AcademusDBUtil.getDBConnection();

              // Detect if the user has logged in yet
			  sql = "SELECT user_id FROM up_user WHERE user_name = ?";
			  pstmt = conn.prepareStatement(sql);
			  pstmt.setString(1, username);
			  rs  = pstmt.executeQuery();

			  if (rs.next()) {
                  loggedIn = true;
                  user_id = rs.getInt("user_id");
			  }

			  rs.close();
			  rs = null;
			  pstmt.close();
			  pstmt = null;

			  // Deleting user's personal information
			  sql = "DELETE FROM person_dir_attr where user_name = ?";
			  pstmt = conn.prepareStatement(sql);
			  pstmt.setString(1, username);
			  pstmt.executeUpdate();
			  pstmt.close();
			  pstmt = null;

			  sql = "DELETE FROM person_dir_metadata where user_name = ?";
			  pstmt = conn.prepareStatement(sql);
			  pstmt.setString(1, username);
			  pstmt.executeUpdate();
			  pstmt.close();
			  pstmt = null;

			  sql = "DELETE FROM up_person_dir where user_name = ?";
			  pstmt = conn.prepareStatement(sql);
			  pstmt.setString(1, username);
			  pstmt.executeUpdate();
			  pstmt.close();
			  pstmt = null;

			  if (!loggedIn) {
                  // Deleteing is complete, exit
				  return;
			  }
		} catch (Exception sqle) {
			sqle.printStackTrace();
		} finally {
            try {
				if (rs != null) { rs.close(); }
				if (pstmt != null) { pstmt.close(); }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_ss_user_atts WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_ss_user_parm WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_user_param WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_user_profile WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_user_ua_map WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int simpleLayoutDeletes = 0;

        try {
            sql = "DELETE FROM up_user_layout WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_layout_param WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_layout_struct WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_user_layout_aggr WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_layout_struct_aggr WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            sql = "SELECT DISTINCT owner.fragment_id FROM up_fragments fr, up_owner_fragment owner " +
            	"WHERE fr.fragment_id= owner.fragment_id and owner.owner_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            rs = pstmt.executeQuery();

            String sql1 = "DELETE FROM up_fragments WHERE fragment_id = ?";
            String sql2 = "DELETE FROM up_fragment_restrictions WHERE fragment_id = ?";

            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);

			// Delete fragment nodes and associated restrictions of fragments
			// owned by the user being deleted
            while (rs.next()) {

				int fragment_id = rs.getInt(1);

				pstmt1.setInt(1, fragment_id);
				pstmt1.executeUpdate();
				pstmt2.setInt(1, fragment_id);
				pstmt2.executeUpdate();
			}

			pstmt1.close();
			pstmt2.close();

			rs.close();
			rs = null;

			pstmt.close();

			// Delete fragment owner entry
			sql = "DELETE FROM up_owner_fragment WHERE owner_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, user_id);
            pstmt.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                rs = null;
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM up_user where up_user.user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
            pstmt = null;
        }
	}
}
