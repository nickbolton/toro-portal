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

import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.unicon.academus.cache.DomainCacheRequestHandler;
import net.unicon.academus.cache.DomainCacheRequestHandlerFactory;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.IdentityStoreFactory;
import net.unicon.academus.common.PersonDirectoryService;
import net.unicon.academus.common.PersonDirectoryServiceFactory;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.ExpiringDomainCache;
import net.unicon.academus.domain.IDomainCache;
import net.unicon.academus.domain.IDomainEventHandler;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.sor.AccessType;
import net.unicon.academus.domain.sor.IEntityRecordInfo;
import net.unicon.academus.domain.sor.ISystemOfRecord;
import net.unicon.academus.domain.sor.SorViolationException;
import net.unicon.academus.domain.sor.SystemOfRecordBroker;
import net.unicon.academus.service.ImportServicePropertiesType;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.IMember;
import net.unicon.portal.groups.MemberFactory;
import net.unicon.portal.util.db.AcademusDBUtil;
import net.unicon.sdk.authentication.PasswordUtil;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.*;


import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;


/** Provides static methods to create and access users.  <code>UserFactory</code> is not instantiable. */
public final class UserFactory {
    // some well-defined attribute keys
    public static final String TEMPLATE_KEY = "template_name";
    private static int MAX_WHERE_IN_DEPTH = 999;
    private static IDomainCache cache = new ExpiringDomainCache();
    private static final boolean isCaching = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.domain.lms.isCaching");
    private static final String groupsMemberKeyType = UniconPropertiesFactory.getManager(AcademusPropertiesType.APPS).getProperty("net.unicon.academus.domain.lms.User.groupsMemberKeyType");

    private static final boolean isMultiBox = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getPropertyAsBoolean("net.unicon.portal.Academus.multipleBoxConfig");
    private static DomainCacheRequestHandler handler = null;
    private static PersonDirectoryService personDirService = null;


    private static final List deleteHandlers = new ArrayList();

    /*
     *The set of allowable characters in usernames.
     *
     * This collection is meant to be...
     *  -> a through z
     *  -> A through Z
     *  -> 1234567890
     *  -> ~!@#$%^*`\()_-=
     * ...but has been reduced for emergency fix.
     */
    private static final char[] nameChars = new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '_'
        };  // specials are:  ~!@#$%^*`\()_-=

    static {
        try {
            if (isMultiBox) {
                handler = DomainCacheRequestHandlerFactory.getHandler();
            }
            personDirService = PersonDirectoryServiceFactory.getService();

            // The user factory owns this user delete event handler
            // so it is the factories responsibility to register it
            IDomainEventHandler deleteHandler = new UserDeleteEventHandler();
            registerDeleteUserEventHandler(deleteHandler);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Allows registering delete user event handles with the UserFactory
    public synchronized static void registerDeleteUserEventHandler (IDomainEventHandler handler) {

        // Assertions.
        if (handler == null) {
            String msg = "Argument 'handler [IDomainEventHandler]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (!deleteHandlers.contains(handler)) {
            deleteHandlers.add(handler);
        }
    }

    public static char[] getAllowableUsernameCharacters() {

        // defensive copy.
        char[] rslt = new char[nameChars.length];
        System.arraycopy(nameChars, 0, rslt, 0, rslt.length);
        return rslt;

    }

    // list of users that are considered Super-Users
    private static List superUsers = null;
    static {
        superUsers = new ArrayList();
        StringTokenizer st = new StringTokenizer(UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getProperty("net.unicon.academus.domain.lms.Permissions.superUsers"), ",");
        while (st.hasMoreTokens()) {
            superUsers.add(st.nextToken().trim());
        }
    }

    private UserFactory() { }

    private static void __checkCreateUserArgs(String username, String password,
                                                 String firstName, String lastName,
                                                 String email)
        throws IllegalArgumentException {
            // Assertions.
            StringBuffer msg = new StringBuffer();
            if (username == null) {
                msg.append("The username can't be null.");
                throw new IllegalArgumentException(msg.toString());
            }
            username = username.trim();
            if (username == null || username.length() == 0) {
                msg.append("E-mail can't be zero-length ");
                msg.append("or contain only whitespace.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (password == null) {
                msg.append("The password can't be null.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (firstName == null) {
                msg.append("The first name can't be null.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (firstName.trim().length() == 0) {
                msg.append("FirstName can't be zero-length ");
                msg.append("or contain only whitespace.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (lastName == null) {
                msg.append("The last name can't be null.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (lastName.trim().length() == 0) {
                msg.append("LastName can't be zero-length ");
                msg.append("or contain only whitespace.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (email == null) {
                msg.append("The E-mail can't be null.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (email.trim().length() == 0) {
                msg.append("Email can't be zero-length ");
                msg.append("or contain only whitespace.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (username.indexOf(' ') > 0) {
                msg.append("Username cannot contain any ");
                msg.append("whitespace.");
                throw new IllegalArgumentException(msg.toString());
            }
            if (username.indexOf('"') > 0) {
                msg.append("Username cannot contain any ");
                msg.append("quotes.");
                throw new IllegalArgumentException(msg.toString());
            }
        }

    /**
     * Creates a new user with the specified parameters.  Throws
     * <code>IllegalArgumentException</code> if the any of the following is true: <ul> <li>any parameter is null</li>
     * <li>username, password, firstName, lastName, or email is zero-length or contains only whitespace</li>
     * <li>the specified username already exists within the portal system</li>
     * <li>email is not a properly formatted e-mail address</li> </ul>
     * @param username the new user's handle within the portal system.
     * @param password the new user's (non-encrypted) password to access the portal system.
     * @param firstName the new user's first name.
     * @param lastName the new user's last name.
     * @param email the new user's e-mail address.
     * @return the newly created user.
     * @throws IllegalArgumentException if <code>createUser</code> cannot create a new user with the specified parameters.
     */
    public static User createUser(String username, String password, String firstName,
                                  String lastName, String email, Map attributes)
       throws IllegalArgumentException, OperationFailedException
    {
        __checkCreateUserArgs(username, password, firstName, lastName, email);
        return _addUser(username, password, firstName, lastName, email, null, null,
                        null, null, null, null, null, null, null, attributes);
    }

    public static User createUser(String username, String password, String firstName,
                                  String lastName, String email,
                                  String prefix, String suffix, String addr1,
                                  String addr2, String city, String state, String zip,
                                  String phone, String role, Map attributes)
        throws IllegalArgumentException, OperationFailedException {

        __checkCreateUserArgs(username, password, firstName, lastName, email);
        return _addUser(username, password, firstName, lastName, email, prefix, suffix,
                        addr1, addr2, city, state, zip, phone, role, attributes);
    }

    /**
     * Creates a new user with the specified parameters.  Throws
     * <code>IllegalArgumentException</code> if the any of the following is true: <ul> <li>any parameter is null</li>
     * <li>username, password, firstName, lastName, or email is zero-length or contains only whitespace</li>
     * <li>the specified username already exists within the portal system</li>
     * <li>email is not a properly formatted e-mail address</li> </ul>
     * @param username the new user's handle within the portal system.
     * @param password the new user's (non-encrypted) password to access the portal system.
     * @param firstName the new user's first name.
     * @param lastName the new user's last name.
     * @param email the new user's e-mail address.
     * @return the newly created user.
     * @throws IllegalArgumentException if <code>createUser</code> cannot create a new user with the specified parameters.
     */
    protected static User _addUser(String username, String password,
                                   String firstName, String lastName,
                                   String email, String prefix,
                                   String suffix, String addr1,
                                   String addr2, String city,
                                   String state, String zip,
                                   String phone, String role, Map attributes)
    throws OperationFailedException {
        User rtn = null;
        long id = 0;
        String encodedPasswd = encodePassword(password);
        boolean autocommit = false;
        // SQL.
        final String sql =
             "Insert into up_person_dir (user_name, encrptd_pswd, first_name,"
           + "last_name, email) values(?, ?, ?, ?, ?)";
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // Create a user.

            // This call initiates a very very bad side-effect of
            // creating the user if none exists. The 2nd param
            // is a template name, which we grab from a property if
            // it is null. Modified so this can be passed in the
            // Map if needed.
            String templateName = null;
            if (attributes != null) {
                templateName = (String)attributes.remove(TEMPLATE_KEY);
            }
            id = getUserId(username, templateName, true);

            // note the User person data is not persisted by constructing this obj
            rtn = new User(id, getGroupMember(id,username),username,
                           encodedPasswd, firstName, lastName, email,
                           prefix, suffix, addr1, addr2, city, state,
                           zip, phone, role, attributes);

            // now write their metadata
            conn = AcademusDBUtil.getDBConnection();
            // make sure autocommit is off
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            int i = 1;
            pstmt.setString(i++, username);
            pstmt.setString(i++, encodedPasswd);
            pstmt.setString(i++, firstName);
            pstmt.setString(i++, lastName);
            pstmt.setString(i++, email);
            pstmt.execute();

            // second query: insert/update person_dir_attr and person_dir_metadata
             __updatePersonDirAttr(rtn, conn);

             // now commit
             conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("Operation createUser failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                     conn.rollback();
                     // turn autocommit back on if it was off
                     conn.setAutoCommit(autocommit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            pstmt = null;
            conn = null;
        }
        if (isCaching) {
            cache.put(username, rtn);
            if (handler != null) {
                try {
                    handler.broadcastRefreshUser(username);
                } catch (AcademusException pe) {
                    pe.printStackTrace();
                }
            }
        }
        return rtn;

    }

    /**
     * <p>
     * Gets a new user with the specified parameters.  Throws
     * <code>IllegalArgumentException</code> if the any of the following is true: <ul>
     * <li>any parameter (except password) is null</li>
     * <li>username, firstName, lastName, or email is zero-length or contains only whitespace</li>
     * <li>email is not a properly formatted e-mail address</li> </ul>
     * </p><p>
     *
     * @param username the  user's handle within the portal system.
     * @param password the  user's (non-encrypted) password to access the portal system.
     * @param firstName the  user's first name.
     * @param lastName the  user's last name.
     * @param email the  user's e-mail address.
     * @param attr the attribute sof the user.
     * </p><p>
     * @return the a non usuable user.
     * </p><p>
     * @throws IllegalArgumentException if <code>createUser</code> cannot create a new user with the specified parameters.
     */
    public static User getUser(long userID, String username, String password,
                               String firstName, String lastName, String email, Map attr)
    throws IllegalArgumentException, OperationFailedException {
        if (username == null) {
            String msg = "The username can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (firstName == null) {
            String msg = "The first name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (lastName == null) {
            String msg = "The last name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (email == null) {
            String msg = "The E-mail can't be null.";
            throw new IllegalArgumentException(msg);
        }
        IMember groupMember = null;

        if (userID > 0) {
            groupMember = getGroupMember(userID, username);
        }
        return new User(userID, groupMember, username,
                        password, firstName, lastName, email, attr);
    }

    public static User getUser(long userID, String username, String password,
                               String firstName, String lastName,
                               String email, String prefix,
                               String suffix, String addr1,
                               String addr2, String city,
                               String state, String zip,
                               String phone, String role, Map attributes)
    throws IllegalArgumentException, OperationFailedException {
        if (username == null) {
            String msg = "The username can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (firstName == null) {
            String msg = "The first name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (lastName == null) {
            String msg = "The last name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (email == null) {
            String msg = "The E-mail can't be null.";
            throw new IllegalArgumentException(msg);
        }
        IMember groupMember = null;

        if (userID > 0) {
            groupMember = getGroupMember(userID, username);
        }
        return new User(userID, groupMember, username,
                        password, firstName, lastName, email,
                        prefix, suffix, addr1, addr2, city, state,
                        zip, phone, role, attributes);
    }

    /**
     * Provides the user with the specified userId. This method uses Lazy
     * Initialization of User objects, and as such is not able to report
     * invalid users immediately. The User object will be populated on the
     * first request of its attributes.
     * @param username the desired user's user name.
     * @return a user in the portal system.
     */
    public static User getUser(String username)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        return _getUser(username);
    }

    static User _getUser(String username)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        if (username == null) {
            String msg = "The user name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        User rtn = null;
        if (isCaching) {
            rtn = (User) cache.get(username);
            if (rtn != null) return rtn;
        }
        try {
            rtn = personDirService.getPerson(username);
        } catch (Throwable t) {
            if (t instanceof PortalException) {
                // uPortal nests exceptions in a non-standard way;  information
                // will be lost unless you capture it here.
                Throwable nest = ((PortalException) t).getRecordedException();
                System.out.println("** The Following Exception Was Swallowed by uPortal **");
                System.out.println(nest.getMessage());
                nest.printStackTrace(System.out);
            }
            String msg = "Unable to retrieve the specified user:  " + username;
            throw new OperationFailedException(msg, t);
        }

        if (rtn == null) {
            StringBuffer msg = new StringBuffer();
            msg.append("Can't find user with user_name=");
            msg.append(username);
            throw new ItemNotFoundException(msg.toString());
        }

        if (isCaching) {
            // Handle the race condition that exists when two calls to
            // _getUser() for the same user block on a database lookup. Only
            // allow the cached object to escape.
            synchronized(cache) {
                User tmp = (User)cache.get(username);
                if (tmp == null) {
                    cache.put(username, rtn);
                    // pre-cache the user's offerings
                    Memberships.getAllOfferings(rtn);
                } else
                    rtn = tmp;
            }
        }
        return rtn;
    }

    /**
     * Provides the set of users (members) for the specified offering.  For
     * example, if the offering is an academic course, this method will provide
     * all the sudents, TAs, and Instructor(s) for that course.
     * @param forOffering an offering in the portal system.
     * @return all the users associated with the specified offering.
     * @throws IllegalArgumentException if the offering is null.
     */
    public static List getUsers(Offering forOffering)
    throws IllegalArgumentException,
    OperationFailedException {
        if (forOffering == null) {
            String msg = "The offering can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        List rtn = null;
        List uNames = new ArrayList();
        // SQL.
        final String sql = "Select user_name from Membership where Offering_Id = ?";
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the user.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, forOffering.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                uNames.add(rs.getString("user_name"));
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getUsers(forOffering) failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        rtn = new ArrayList(uNames.size());
        Iterator it = uNames.iterator();
        String uName = null;
        try {
            while (it.hasNext()) {
                uName = (String) it.next();
                rtn.add(getUser(uName));
            }
        } catch (ItemNotFoundException infe) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation getUsers(forOffering) failed ");
            msg.append("with the following message:\n");
            msg.append(infe.getMessage());
            throw new OperationFailedException(msg.toString());
        }
        return rtn;
    }

    public static boolean isSuperUser(String username) {
        return superUsers.contains(username);
    }

    public static boolean isDefaultUser(String username) {
        List defaultUsers = UniconPropertiesFactory.getManager(AcademusPropertiesType.LMS).getArrayList("net.unicon.academus.domain.lms.Permissions.defaultUsers");
        return defaultUsers.contains(username);
    }

    /**
     * Checks to see if the user exists in the system
     * before trying to create the user with the same
     * username
     *
     * @param username a String with the username.
     * @param createData requests that user data be created if it doesn't exist
     * @return a boolean 'true' if a user exist with the same passed
     * in parameter.
    public static boolean usernameExists(String username, boolean createData)
    throws OperationFailedException {
        if (username == null) {
            String msg = "The user name can't be null.";
            throw new IllegalArgumentException(msg);
        }
        // The rtn.
        boolean rtn = false;

        try {
            User user = personDirService.getPerson(username, createData);
            if (user != null)
                rtn = true;
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer msg = new StringBuffer();
            msg.append("UserFactory.usernameExists() : ");
            msg.append("Operation failed will finding username : ");
            msg.append(username);
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        }
        return rtn;
    }
     */

    /**
     * Maintained for backward compatibility.
     * @param a String with the username.
     *
     * @return a boolean 'true' if a user exist with the same passed
     * in parameter.
     */
    public static boolean usernameExists(String username)
        throws OperationFailedException
    {
        try {
	        return IdentityStoreFactory.getStore().userExists(username);
        } catch (Exception e) {
            throw new OperationFailedException(e);
        }
    }

    public static void persist(User obj) throws ItemNotFoundException,
                    OperationFailedException, DomainException {
        if (obj == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        persistUser(obj, SystemOfRecordBroker.ACADEMUS);
    }

    public static void persist(User obj, ISystemOfRecord principal) throws ItemNotFoundException,
                    OperationFailedException, DomainException {
        if (obj == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "The principal object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        persistUser(obj, principal);
    }


    /**
     * Return valid portal users from the given users populating the
     * portal ID. This method will chunk up the querries so it only
     * retrieves at most MAX_WHERE_IN_DEPTH users.  We do this because
     * where in clause depth can only be so deep. The least common
     * denominator for sql server, oracle and postgres is 1000 users.
     * @throw OperationFailedException
     * @return list of users with valid portal IDs.
     */
    public static List validatePortalUsers(User[] users)
    throws OperationFailedException {
        List validUsers = new ArrayList();
        List userList = Arrays.asList(users);

        int start=0;
        int end=Math.min(MAX_WHERE_IN_DEPTH, userList.size());
        while (start<userList.size()) {
            validUsers.addAll(__validatePortalUsers(
                (User[])userList.subList(start, end).toArray(new User[0])));
            start=end;
            end=Math.min(start+MAX_WHERE_IN_DEPTH, userList.size());
        }
        return validUsers;
    }

    private static List __validatePortalUsers(User[] users)
    throws OperationFailedException {
        // The rtn.
        List validUsers = new ArrayList();
        if (users == null || users.length == 0) return validUsers;

        Map userMap = new HashMap();
        User user;
        String username;
        long userId;

        // create a username->User mapping
        for (int i=0; i<users.length; i++) {
            userMap.put(users[i].getUsername(), users[i]);
        }

        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append(
            "Select user_id, user_name from up_user where user_name in (");
        sql.ensureCapacity(sql.length()+users.length*2);
        sql.append("?");
        for (int i=1; i<users.length; i++) {
            sql.append(",?");
        }
        sql.append(")");

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // Try to find the user.
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i=0; i<users.length; i++) {
                pstmt.setString(i+1, users[i].getUsername());
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                username = rs.getString("user_name");
                user = (User)userMap.get(username);
                if(user!=null){
                    user.setId(rs.getLong("user_id"));
                    validUsers.add(user);
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation validatePortalUsers() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }

        return validUsers;
    }


    private static void persistUser(User obj) throws ItemNotFoundException,
                        OperationFailedException, DomainException {
        persistUser(obj, SystemOfRecordBroker.ACADEMUS);
    }

    private static void persistUser(User obj, ISystemOfRecord principal)
                    throws ItemNotFoundException, OperationFailedException,
                    DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, principal, AccessType.MODIFY);

        // Monitor & react to the # of rows that are changed.
        int numRowsChanged = -1;

        // SQL.
        // Make sure autocommit is off
        // check to see if a person_dir_attr record exists
        // check to see if any other attributes written

        final String sql = "update up_person_dir set first_name = ?, "
                         + "last_name = ?, email = ? where user_name = ?";

        // db objects.
        Connection conn = null;
        PreparedStatement pstmt  = null;

        boolean autoCommit = false;
        try {
            conn = AcademusDBUtil.getDBConnection();

            // make sure autocommit is off
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // first query: update up_person_dir
            pstmt = conn.prepareStatement(sql);
            int i = 1;
            pstmt.setString(i++, obj.getFirstName());
            pstmt.setString(i++, obj.getLastName());
            pstmt.setString(i++, obj.getEmail());
            pstmt.setString(i++, obj.getUsername());

            numRowsChanged = pstmt.executeUpdate();

            // second query: insert/update person_dir_attr and person_dir_metadata
            __updatePersonDirAttr(obj, conn);

            // now commit
            conn.commit();

            // refresh the cache still
            if (isCaching && handler != null) {
                try {
                    handler.broadcastRefreshUser(obj.getUsername());
                } catch (AcademusException pe) {
                    pe.printStackTrace();
                }
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation persistUser() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.rollback();
                    // turn autocommit back on if it was off
                    conn.setAutoCommit(autoCommit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
            pstmt = null;
        }

        String msg = null;
        switch (numRowsChanged) {
            case 0:
                // Nothing happened...
                cache.remove(obj.getUsername());    // Clear cache so the values will be reset...
                String createAcademusUser = UniconPropertiesFactory.getManager(
                                            ImportServicePropertiesType.IMPORT).getProperty(
                                            "net.unicon.academus.service.ImportService.createAcademusUser");
		if (!createAcademusUser.equals("false")) {
	                msg = "Unable to update user information.  This user may "
	                                + "belong to an external system, "
	                                + "such as a directory server.";
	                throw new OperationFailedException(msg);
		}
            case 1:
                // Success scenario... do nothing.
                break;
            default:
                // Something is messed up...
                // Not sure what to do here... possibly throw an exception.
                break;
        }

    }

    private static void __updatePersonDirAttr(User user, Connection conn) throws SQLException {

        final String ISQL = "INSERT INTO person_dir_attr (user_name, prefix, suffix, address1, " +
                            "address2, city, state, zip, phone) VALUES (?,?,?,?,?,?,?,?,?)";
        final String DSQL = "DELETE FROM person_dir_attr WHERE user_name = ?";

        // make sure there is something to insert/update for person_dir_attr
        boolean writeRec =  (user.getPrefix() != null) || (user.getSuffix() != null) ||
                            (user.getAddress1() != null) || (user.getState() != null) ||
                            (user.getAddress2() != null) || (user.getCity() != null) ||
                            (user.getZip() != null) || (user.getPhone() != null);
        // if writeRec is true, do the insert, but you still have to delete either way

        PreparedStatement pstmt  = null;
        PreparedStatement ps2 = null;

        try {
            // always delete a record in person_dir_attr if it exists
            pstmt = conn.prepareStatement(DSQL);
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            // if we have attributes to write then write them
            if (writeRec) {
                pstmt = conn.prepareStatement(ISQL);
                int i = 1;
                pstmt.setString(i++, user.getUsername());
                pstmt.setString(i++, user.getPrefix());
                pstmt.setString(i++, user.getSuffix());
                pstmt.setString(i++, user.getAddress1());
                pstmt.setString(i++, user.getAddress2());
                pstmt.setString(i++, user.getCity());
                pstmt.setString(i++, user.getState());
                pstmt.setString(i++, user.getZip());
                pstmt.setString(i++, user.getPhone());
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }

            // Now take care of person_dir_metadata
            // remove any metadata if needed
            ps2 = conn.prepareStatement("DELETE FROM person_dir_metadata WHERE user_name=?");
            ps2.setString(1, user.getUsername());
            ps2.executeUpdate();
            ps2.close();

            // now add all the new stuff
            ps2 = conn.prepareStatement("INSERT INTO person_dir_metadata VALUES (?,?,?)");
            Map attrs = user.getAttributes();
            if (attrs != null) {
                Set keys  = attrs.keySet();
                String keyname = null;
                String value   = null;
                for (Iterator keyIter = keys.iterator(); keyIter.hasNext(); ) {
                    keyname = (String)keyIter.next();
                    value   = (String)attrs.get(keyname);
                    if (value != null && value.length() > 0) {
                        ps2.clearParameters();
                        ps2.setString(1, user.getUsername());
                        ps2.setString(2, keyname);
                        ps2.setString(3, value);
                        ps2.executeUpdate();
                    }
                }
            }
            ps2.close(); ps2 = null;
        }
        catch (SQLException exc) {
            exc.printStackTrace();
            throw exc;
        }
        finally {
            try {
                if (pstmt != null) { pstmt.close(); }
                if (ps2 != null)    { ps2.close(); }
            }
            catch (Exception e) {
                   e.printStackTrace();
                   // don't rethrow
            }
        }
    }

    public static void changePassword(User obj, String password)
    throws ItemNotFoundException, OperationFailedException, DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (password == null) {
            String msg = "Argument 'password' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, SystemOfRecordBroker.ACADEMUS, AccessType.MODIFY);

        // SQL.
        StringBuffer sql = new StringBuffer();
        sql.append("update up_person_dir set encrptd_pswd = ? ");
        sql.append("where user_name = ?");
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 1;
            pstmt.setString(i++, encodePassword(password));
            pstmt.setString(i++, obj.getUsername());
            pstmt.executeUpdate();
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Operation chagePassword() failed ");
            msg.append("with the following message:\n");
            msg.append(e.getMessage());
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
            pstmt = null;
        }
    }

    public static void delete(User obj) throws IllegalArgumentException,
    ItemNotFoundException, OperationFailedException, DomainException {
        delete(obj, SystemOfRecordBroker.ACADEMUS);
    }

    public static void delete(User obj, ISystemOfRecord principal) throws IllegalArgumentException,
    ItemNotFoundException, OperationFailedException, DomainException {
        if (obj == null) {
            String msg = "The user object can't be null.";
            throw new IllegalArgumentException(msg);
        } else if (isSuperUser(obj.getUsername())) {
            String msg = "Cannot delete Super user.";
            throw new IllegalArgumentException(msg);
        }
        __deleteUser(obj, principal);
    }

    public static void delete(String username) throws IllegalArgumentException,
    ItemNotFoundException, OperationFailedException, DomainException {
        delete(username, SystemOfRecordBroker.ACADEMUS);
    }

    public static void delete(String username, ISystemOfRecord principal) throws IllegalArgumentException,
    ItemNotFoundException, OperationFailedException, DomainException {
        if (username == null) {
             String msg = "The username String can't be null.";
             throw new IllegalArgumentException(msg);
        } else if (isSuperUser(username) || isDefaultUser(username)) {
             String msg = "You can't delete guest, admin, demo and super users.";
             throw new IllegalArgumentException(msg);
        }
        delete(getUser(username), principal);
    }

    private static void __deleteUser(User obj)
    throws ItemNotFoundException, OperationFailedException, DomainException {
        __deleteUser(obj, SystemOfRecordBroker.ACADEMUS);
    }

    private static void __deleteUser(User obj, ISystemOfRecord principal)
    throws ItemNotFoundException, OperationFailedException, DomainException {

        // Assertions.
        if (obj == null) {
            String msg = "Argument 'obj' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Make sure we don't violate an SOR.
        ensureSorAccess(obj, principal, AccessType.DELETE);

        String username = obj.getUsername();
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        int user_id = -1;
        try {
        // Get an IMember from the User object
        IMember member = obj.getGroupMember();

        // Get all containting groups of that member
        IGroup[] containingGroups = member.getContainingGroups();
        IGroup currentGroup = null;

        // Loop through all groups and remove the member from all the groups
        for (int index = 0; index < containingGroups.length; index++)
        {
            currentGroup = containingGroups[index];

            try
            {
             currentGroup.removeMember(member);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                LogService.log(LogService.ERROR,
                    "UserFactory::__deleteUser(): Removing user " + username +
                    " from group " + currentGroup.getName() +
                    ":" + e);
            }
        }

        // remove memberships
        Iterator itr = Memberships.getAllOfferings(obj).iterator();
        Offering off;
        EnrollmentStatus status;
        while (itr.hasNext()) {
            off = (Offering)itr.next();
            status = Memberships.getEnrollmentStatus(obj, off);
            Memberships.remove(obj, off, status);

            if (status.toInt() == EnrollmentStatus.ENROLLED.toInt()) {
                // remove the user from the offering's calendar
                CalendarServiceFactory.getService().removeUser(off, obj);

            }
        }

        // Invoke all registered delete user event handlers
        Iterator it = deleteHandlers.iterator();
        while (it.hasNext()) {
            IDomainEventHandler e = (IDomainEventHandler) it.next();
            e.handleEvent(obj);
        }

    /* ================== DELETE USER FROM CHANNEL-SPECIFIC TABLES ================== */

        conn = AcademusDBUtil.getDBConnection();

        /* ------ GRADEBOOK CHANNEL ------ */
        String sql = "DELETE FROM gradebook_submission WHERE gradebook_score_id in (SELECT gradebook_score_id FROM gradebook_score WHERE user_name = ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            sql = "DELETE FROM gradebook_feedback WHERE gradebook_score_id in (SELECT gradebook_score_id FROM gradebook_score WHERE user_name = ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        sql = "DELETE FROM gradebook_score WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        sql = "DELETE FROM user_activation WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        /* ------ NOTEPAD CHANNEL ------ */
        sql = "DELETE FROM notepad WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
        /* ------ CHAT ADMIN AND GROUP CHAT CHANNELS ------ */
        sql = "DELETE FROM chat_users WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ResultSet rs  = null;
        PreparedStatement pstmt2 = null;
        try {
        /* ------ ASSESSMENT CHANNEL  ------ */
        sql = "SELECT result_id FROM assessment_result WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs  = pstmt.executeQuery();

        sql = "DELETE FROM question_result WHERE result_id = ?";
        String result_id;

        pstmt2 = conn.prepareStatement(sql);

        while (rs.next())
        {
        result_id = rs.getString("result_id");

        pstmt2.setString(1, result_id);
        pstmt2.executeUpdate();
        }
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt2 != null) pstmt2.close();
                pstmt2 = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        sql = "DELETE FROM assessment_result WHERE user_name = ?";
        pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        /* ------ CALENDAR CHANNEL  ------ */
        sql = "SELECT calendar_id FROM calendar WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs  = pstmt.executeQuery();

        sql = "DELETE FROM event WHERE calendar_id = ?";
        String calendar_id;

        pstmt2 = conn.prepareStatement(sql);

        while (rs.next())
        {
        calendar_id = rs.getString("calendar_id");

        pstmt2.setString(1, calendar_id);
        pstmt2.executeUpdate();

        }
        } finally {
            try {
                if (rs != null) rs.close();
                rs = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (pstmt2 != null) pstmt2.close();
                pstmt2 = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        sql = "DELETE FROM calendar WHERE user_name = ?";
        pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
        /* ------ REMOVE USER'S CHANNEL PREFERENCES  ------ */
        sql = "DELETE FROM channel_preference WHERE user_name = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    /* =============================================================================== */

        // Now attempt to delete the user from the up_user table
        try {
            sql = "DELETE FROM up_user WHERE user_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            int deleteCount = pstmt.executeUpdate();

            // Remove the user from cache
            removeUserFromCache(username);

        } finally {
            try {
                if (pstmt != null) pstmt.close();
                pstmt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

            if (isCaching) {
                cache.remove(username);
                if (handler != null) {
                    try {
                        handler.broadcastRemoveUser(username);
                    } catch (AcademusException ae) {
                        ae.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            String msg = "__deleteUser(user) failed, with the following mesage:\n"
                + e.getMessage();
            throw new OperationFailedException(msg.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
            pstmt = null;
        }
    }

    /**
     * Removes the user object from memory/cache.
     * @param usrname - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void removeUserFromCache(String username)
    throws ItemNotFoundException, OperationFailedException  {
        if (isCaching) {
            cache.remove(username);
        }
    }

    /**
     * Refreshed the user object from memory/cache.
     * @param username - the username to remove from cache
     * @see net.unicon.portal.domain.IllegalArgumentException
     * @see net.unicon.portal.domain.ItemNotFoundException
     * @see net.unicon.portal.domain.OperationFailedException
     */
    public static void refreshUserCache(String username)
    throws IllegalArgumentException, OperationFailedException, ItemNotFoundException  {
        if (isCaching) {
            cache.remove(username);
            getUser(username);
        }
    }

    static String encodePassword(String password)
    throws OperationFailedException {
        String encodedPassword = null;
        try {
            encodedPassword = PasswordUtil.createPassword(password);
        } catch (NoSuchAlgorithmException nsae) {
            String msg = "Can't encode specified password.";
            throw new OperationFailedException(msg);
        }
        return encodedPassword;
    }

    private static IMember getGroupMember(long id, String username)
    throws OperationFailedException {
        IMember member = null;
        try {
            member = MemberFactory.getMember(getMemberKey(username, id));
        } catch (FactoryCreateException fce) {
            throw new OperationFailedException(fce);
        }

        if (member == null) {
            throw new OperationFailedException(
                "UserFactory::getGroupMember() : IMember object could not " +
                "be instantiated for (" + id + ", " + username + ")");
        }
        return member;
    }

    public static String getMemberKey(String username, long id) {
        String key = username;
        if ("userid".equalsIgnoreCase(groupsMemberKeyType)) {
            key = ""+id;
        }
        return key;
    }

    public static String getMemberKey(User user) {
        return user.getGroupMember().getKey();
    }

    public static String getMemberKey(String username)
    throws IllegalArgumentException,
    OperationFailedException, ItemNotFoundException {
        // Assertions.
        if (username == null) {
            String msg = "The username can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return getMemberKey(getUser(username));
    }

    public static long getUserId(String username, String templateName, boolean createData)
    throws IllegalArgumentException, Exception {
        if (username == null) {
            String msg = "The username String can't be null.";
            throw new IllegalArgumentException(msg);
        }
        return IdentityStoreFactory.getStore().getUserId(username,
                                                         templateName, createData);
    }

    public static User getUser(long id)
    throws OperationFailedException,
    ItemNotFoundException {
        User user               = null;
        PreparedStatement pstmt = null;
        ResultSet rs            = null;
        Connection conn         = null;
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT user_name from up_user where user_id = ?");
        try {
            conn = AcademusDBUtil.getDBConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int i = 0;
            pstmt.setLong(++i, id);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new ItemNotFoundException(
                    "Could not find user with id: " + id);
            }


            user = getUser(rs.getString("user_name"));
        } catch (Exception sqle) {
            throw new OperationFailedException(sqle);
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
            AcademusDBUtil.safeReleaseDBConnection(conn);
            conn = null;
        }
        return user;
    }

    public static User getUserByKey(String key)
    throws Exception {
        if ("userid".equalsIgnoreCase(groupsMemberKeyType)) {
            return getUser(Long.parseLong(key)); /* user_id */
        }
        return getUser(key); /* username */
    }

    public static List findUsers(MemberSearchCriteria criteria)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException {
        return _findUsers(criteria);
    }

    protected static List _findUsers(MemberSearchCriteria criteria)
    throws IllegalArgumentException, ItemNotFoundException,
    OperationFailedException  {
        if (criteria == null) {
            String msg = "The search criteria object can't be null.";
            throw new IllegalArgumentException(msg);
        }
        List rtnList = null;
        try {
            rtnList = personDirService.find(
                            criteria.getUsername(),
                            criteria.getFirstName(),
                            criteria.getLastName(),
                            criteria.getEmail(),
                            criteria.matchAllCriteria());
        } catch (Exception e) {
            throw new OperationFailedException(e);
        }
        return rtnList;
    }

    private static void ensureSorAccess(User u, ISystemOfRecord principal,
                                AccessType t) throws DomainException {

        // Assertions.
        if (u == null) {
            String msg = "Argument 'u [User]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (t == null) {
            String msg = "Argument 't [AccessType]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IEntityRecordInfo rec = SystemOfRecordBroker.getRecordInfo(u);

        // Get out if the sor is the principal.
        if (rec.getSystemOfRecord() == principal) {
            return;
        }

        // Otherwise, see if the requested access is allowable.
        if (rec.getSystemOfRecord().getEntityAccessLevel().compareTo(t) < 0) {
            String msg = "Access of type " + t.toString() + " is not allowed "
                                    + "on entities from the "
                                    + rec.getSystemOfRecord().getSourceName()
                                    + " system.";
            throw new SorViolationException(msg);
        }

    }

}
