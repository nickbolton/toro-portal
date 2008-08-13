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

package net.unicon.academus.apps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import net.unicon.academus.api.AcademusDataSource;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.alchemist.rdbms.Sequencer;

import org.dom4j.Element;

public class SsoMultiAuthentication extends SsoAuthentication {

    private static final String systemKey = "system-key";
    private static final String usernameKey = "username-key";
    private static final String encryptNameKey = "encrypt-ref-key";
    private static final String jndiNameKey = "jndi-ref";

    private EncryptionService encryptionService;
    private String systemName;
    private String jndiName;
    private DataSource dataSource;
    private Sequencer seq;

    /*
     * Public API.
     */
    
	public static SsoAuthentication parse(Element e) {
		
		// Assertions.
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (!e.getName().equals("authentication")) {
			String msg = "Argument 'e [Element]' must be an <authentication> element.";
			throw new IllegalArgumentException(msg);
		}
		
        Map authParams = new HashMap();
        for (Iterator it = e.selectNodes("parameter").iterator(); it.hasNext();) {
            Element p = (Element) it.next();
            String key = p.selectSingleNode("@name").getText();
            String val = p.selectSingleNode("value").getText();
            authParams.put(key, val);
        }
		
		return new SsoMultiAuthentication(authParams);
		
	}
    
    public Map resolve(Map userAttribs) throws NeedsAuthException {

        String userKey =
            (String)userAttribs.get(super.params.get(usernameKey));
        String username = null;
        String password = null;

        Connection conn = null;
        try {
            conn = getDataSource().getConnection();

            String[] credentials = this.getCredentials(userKey, conn);
            username = credentials[0];
            password = credentials[1];
        } catch (Exception sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }

        Map result = new HashMap(userAttribs);
        result.put(ATTRIB_PREFIX+"username", username);
        result.put(ATTRIB_PREFIX+"password", password);

        return result;
    }

    public void createAuthentication(Map userAttribs, String username,
        String password)
    {

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getDataSource().getConnection();

            // Get logged in username.
            String userKey =
                (String)userAttribs.get(super.params.get(usernameKey));

            // Attempt to get existing credentials.
            String[] credentials = null;
            try {
                credentials = this.getCredentials(userKey, conn);
            } catch (NeedsAuthException nae) {
                // Nothing needs to be done here.
            }

            // Encrypt the new external system password.
            String encryptedPWD = this.encryptionService.encrypt(password);

            if (credentials != null) {
                String sql = "UPDATE SSO_CREDENTIALS SET USERNAME = ?, PASSWORD = ? WHERE USER_KEY = ? AND SYSTEM = ?";

                pstmt = conn.prepareStatement(sql);

                int i = 0;
                pstmt.setString(++i, username);
                pstmt.setString(++i, encryptedPWD);
                pstmt.setString(++i, userKey);
                pstmt.setString(++i, this.systemName);
            } else {
                long credentId = getSequencer().next();

                String sql = "INSERT INTO SSO_CREDENTIALS (CREDENT_ID, USER_KEY, SYSTEM, USERNAME, PASSWORD) VALUES (?, ?, ?, ?, ?)";

                pstmt = conn.prepareStatement(sql);

                int i = 0;
                pstmt.setLong(++i, credentId);
                pstmt.setString(++i, userKey);
                pstmt.setString(++i, this.systemName);
                pstmt.setString(++i, username);
                pstmt.setString(++i, encryptedPWD);
            }

            // Now try to update credentials.
            pstmt.executeUpdate();

        } catch (Exception sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }

    } // end createAuthentication

    public boolean supportsAuthenticate() { return true; }
    
    public String[] getCredentials(String userKey) throws NeedsAuthException {
        Connection conn = null;
        
        try {
            conn = getDataSource().getConnection();
            return getCredentials(userKey, conn);
        } catch (Exception sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }
    }

    /*
     * Implementation.
     */
    
    private SsoMultiAuthentication(Map params) {
        super(params);

        String encryptName = (String)params.get(encryptNameKey);
        this.encryptionService = EncryptionService.getInstance(encryptName);
        if (this.encryptionService == null) {
            throw new RuntimeException(
                "Unable to acquire EncryptionService for encryptNameKey: "+
                encryptNameKey);
        }

        this.jndiName = (String)params.get(jndiNameKey);
        if (this.jndiName == null) {
            throw new RuntimeException(
                "Unable to acquire jndi-ref for DataSource");
        }

        try {
            this.dataSource = this.getDataSource();
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }

        this.systemName = (String)params.get(systemKey);
    }

    private Sequencer getSequencer() throws NamingException {
        if (seq == null) {
            seq = new Sequencer(getDataSource(), "SSO_CREDENTIALS", 1);
        }
        return seq;
    }

    private String[] getCredentials(String userKey, Connection conn)
    throws NeedsAuthException {

        String[] credentials = new String[2];
        String sql = "SELECT USERNAME, PASSWORD FROM SSO_CREDENTIALS WHERE USER_KEY = ? AND SYSTEM = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userKey);
            pstmt.setString(2, this.systemName);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                credentials[0] = rs.getString(1);
                credentials[1] =
                    this.encryptionService.decrypt(rs.getString(2));
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("Credentials does not exist for user: ");
                msg.append(userKey);
                msg.append(" for system: ");
                msg.append(this.systemName);

                throw new NeedsAuthException(msg.toString());
            }

            return credentials;
        } catch (SQLException sqle) {
            StringBuffer msg = new StringBuffer();
            msg.append("Error occurred accessing credentials for user: ");
            msg.append(userKey);
            msg.append(" for system: ");
            msg.append(this.systemName);

            throw new NeedsAuthException(msg.toString(), sqle);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
        }

    } // end getCredentials

    private DataSource getDataSource() throws NamingException {
    	try {
            if (dataSource == null) {
                dataSource = new AcademusDataSource();
            }
            return dataSource;
    	} catch (Throwable t) {
    		throw new RuntimeException(t);
    	}
    }
    
} // end SsoMultiAuthentication class
