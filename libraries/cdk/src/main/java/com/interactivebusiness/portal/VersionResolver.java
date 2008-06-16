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

package com.interactivebusiness.portal;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.services.LogService;
import org.jasig.portal.groups.IGroupMember;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.sql.DataSource;
import java.util.Properties;
import java.io.InputStream;
/**
 *
 * @author  jchen
 */
public class VersionResolver implements IVersionResolver
{
    private static Properties versionProperties = new Properties();
    private static VersionResolver resolver;    
    private static String portal_version;
    private static boolean bInitialized = false;

    /** Creates a new instance of VersionResolver */
    private VersionResolver()
    {
        initialize();
    }
    
    static
    {
        if (!bInitialized)
        {
            try
            {
                //portal_version = PropertiesManager.getProperty ("com.interactivebusiness.portal.version");            
                //this is the better way to get the property, however, uPortal 2.0 does not have
                //this method.
                //versionProperties = ResourceLoader.getResourceAsProperties (VersionResolver.class, "/properties/version.properties");
                InputStream inputStream = ResourceLoader.getResourceAsStream (VersionResolver.class, "/properties/version.properties");
                versionProperties.load(inputStream);
                inputStream.close();
                LogService.instance().log(LogService.INFO, "loadProps() :: version.properties file loaded. ");
                portal_version = versionProperties.getProperty("com.interactivebusiness.portal.version", "2.1");
                bInitialized = true;
            }
            catch (Exception e)
            {
                LogService.instance().log (LogService.ERROR, "failed to load version.properties "+e);
            }
        }
    }
    private void initialize ()
    {
    }
    
    public static VersionResolver getInstance()
    {
        if (resolver == null)
        {
            resolver = new VersionResolver ();
        }
        return resolver;
    }
    
    public IAuthorizationPrincipal getPrincipalByPortalVersions(IPerson person)
    {
        try {
            EntityIdentifier ei = person.getEntityIdentifier();
            return AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        } catch (AuthorizationException ae) {
            LogService.log(LogService.ERROR, "Failed to get authorization principal for userID: " + person.getID(), ae);
        }
        return null;
    }
    
    public static Connection getConnectionByPortalVersions(String dbName)
    {
        Connection conn = null;
        DataSource ds = null;
        try 
        {
          Context initCtx = new InitialContext();
          if (portal_version.startsWith ("2.0"))
            ds = (DataSource)initCtx.lookup (dbName);
          else if (portal_version.startsWith("2.1"))
          {              
              //if use poolamn     
              try{
                ds = (DataSource) initCtx.lookup(dbName);
              }
              catch (Exception e)
              {
                  try
                  {
                      Context envCtx = (Context) initCtx.lookup("java:comp/env");          
                      ds = (DataSource)envCtx.lookup("jdbc/" + dbName);  
                  }
                  catch (Exception ex){}
              }
          }
          if (ds != null) 
          {
            conn = ds.getConnection();
            // Make sure autocommit is set to true
            if (conn != null && !conn.getAutoCommit()) 
            {
              conn.rollback();
              conn.setAutoCommit(true);
            }
          } 
          else 
          {
            LogService.instance().log(LogService.ERROR, "The database '" + dbName + "' could not be found.");
          }
        } 
        catch (javax.naming.NamingException ne) 
        {
          LogService.instance().log(LogService.ERROR, ne);
        } 
        catch (java.sql.SQLException sqle) 
        {
          LogService.instance().log(LogService.ERROR, sqle);
        }
        return conn;
    }
    
    public String getUserKeyColumnByPortalVersions()
    {
        if (portal_version.startsWith("2.0"))
            return "USER_ID";
        else if (portal_version.startsWith("2.1"))
            return "USER_NAME";
        else
            return "USER_NAME";
    }
    
    public String getUserKeyColumnByPortalVersions(IPerson person)
    {
        if (portal_version.startsWith("2.0"))
            return Integer.toString(person.getID());
        else if (portal_version.startsWith("2.1"))
            return (String) person.getAttribute ("username");
        else
            return null;
    }
    
    public static String getPortalVersion()
    {
        return portal_version;
    }
    
    public boolean checkIfPersonInGroupByPortalVersions(IPerson person, IGroupMember member)
    {
        try
        {
            if (portal_version.startsWith("2.0"))
            {
                if (member.getKey().equals (Integer.toString(person.getID())))
                    return true;                
            }
            else if (portal_version.startsWith("2.1"))
            {
                if (member.getKey().equals (person.getAttribute("username")))
                    return true;
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }
    
}
