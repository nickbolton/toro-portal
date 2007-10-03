/*
 *******************************************************************************
 *
 * File:       PersonDirectoryServiceImpl.java
 *
 * Copyright:  Â©2002 Unicon, Inc. All Rights Reserved
 *
 * This source code is the confidential and proprietary information of Unicon.
 * No part of this work may be modified or used without the prior written
 * consent of Unicon.
 *
 *******************************************************************************
 */

package net.unicon.portal.common;

import net.unicon.academus.common.PersonDirectoryService;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.util.db.AcademusDBUtil;
import net.unicon.sdk.properties.*;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;

import java.util.Vector;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class PersonDirectoryServiceImpl implements PersonDirectoryService {

    static Vector sources = null; // List of PersonDirInfo objects
    static Hashtable drivers = new Hashtable(); // Registered JDBC drivers
    private PersonDirectory personDirService = null;
    
    static String uidKey   = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.uidKey");
    static String emailKey = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.emailKey");
    static String firstnameKey = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.firstnameKey");
    static String lastnameKey = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.lastnameKey");
    static String templateNameKey = UniconPropertiesFactory.getManager(
        PortalPropertiesType.LMS).getProperty(
            "org.jasig.portal.security.templateNameKey");

    public PersonDirectoryServiceImpl() {
        personDirService = PersonDirectory.instance();
        getParameters();
    }

    public User getPerson(String username, boolean createData) throws Exception {
    	
    	if (username == null || "".equals(username.trim())) {
    		throw new IllegalArgumentException("Argument username must be specified: " + username);
    	}
    	
        IPerson person = new PersonImpl();
        personDirService.getUserDirectoryInformation(username, person);
        
        String firstName = normalize((String)person.getAttribute(firstnameKey));
        String lastName  = normalize((String)person.getAttribute(lastnameKey));
        String password  = null;
        String email     = normalize((String)person.getAttribute(emailKey));
        String templateName =
            normalize((String)person.getAttribute(templateNameKey));
        
        Map attrMap = new HashMap();
        attrMap.put(IPerson.USERNAME, username);
        
        /* Building attribute map */
        Enumeration attrNames = person.getAttributeNames();
        
	if (attrNames != null) {
	    while (attrNames.hasMoreElements() ) {
		// Get the attribute Name
		String attName = (String) attrNames.nextElement();
            
		// Get the IPerson attribute value for this eduPerson attribute name
		String value = (String)person.getAttribute(attName);
		if (value != null) {
		    attrMap.put(attName, value);
		}
	    }
	}

        long userID = UserFactory.getUserId(username, templateName, createData);
        return UserFactory.getUser(
                            userID,
                            username, 
                            password, 
                            firstName, 
                            lastName, 
                            email, 
                            attrMap);
    }

    /**
     * This versin maintained for backward compatibility. Requests that
     * user data be written to the database if available.
     */
    public User getPerson(String uid) throws Exception {
        return getPerson(uid, true);
    }

    public List find(String username) throws Exception {
        PersonDirInfo pdi = (PersonDirInfo) sources.elementAt(0);
        Object[] args = {username};
        return createPeople(processLdapSearch(pdi.uidquery, args, pdi));
    }

    public List find(String username, String firstname,
        String lastname, String email, boolean matchAll) 
    throws Exception {
        return find(null, username, firstname, lastname, email, matchAll);
    }

    public List find(String[] usernames, String username, String firstname,
        String lastname, String email, boolean matchAll)
    throws Exception {

        final int numArgs = 4;

        StringBuffer debugMsg = new StringBuffer();
        debugMsg.append("PersonDirectoryServiceImpl::find() ");
        debugMsg.append("username: ").append(username);
        debugMsg.append(" - firstname: ").append(firstname);
        debugMsg.append(" - lastname: ").append(lastname);
        debugMsg.append(" - email: ").append(email);
        LogService.log(LogService.DEBUG, debugMsg.toString());

        // normalize the search criteria (make non-null)
        username = normalize(username);
        firstname = normalize(firstname);
        lastname = normalize(lastname);
        email = normalize(email);

        boolean openQuery = "".equals(username) && "".equals(firstname) &&
            "".equals(lastname) && "".equals(email);

        List results = new ArrayList();
        List queryResults = null;
        for (int i=0; i<sources.size(); i++) {
            queryResults = null;
            PersonDirInfo pdi = (PersonDirInfo) sources.elementAt(i);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::find() : finding using source " + i + " pdi" + (pdi.disabled ? "(disabled)" : "" ) + ": " + pdi.url);

            if (pdi.disabled) continue;

          
            /************************************************************/
            /* Changed by Mike Marquiz 10/06/2003
               Previous Code: (did not handle secure LDAP {ldaps})

                    if (pdi.url.startsWith("ldap:")) {

               New Code: (handles both secure LDAP {ldaps} and non-secure 
                          LDAP {ldap})

                    if (pdi.url.startsWith("ldap:") ||
                        pdi.url.startsWith("ldaps:")) {
            */
            /************************************************************/
            
            if (pdi.url.startsWith("ldap:") ||
                pdi.url.startsWith("ldaps:")) {

                String filter = null;
                Object[] ldapArgs = null;

                if (usernames != null) {
                    filter = createLdapUserFilter(pdi, matchAll,
                        username, firstname, lastname, email, usernames);
                    ldapArgs = new Object[usernames.length+numArgs];
                    ldapArgs[0] = username;
                    ldapArgs[1] = firstname;
                    ldapArgs[2] = lastname;
                    ldapArgs[3] = email;
                    System.arraycopy(usernames, 0, ldapArgs, numArgs,
                        usernames.length);
                } else {
                    filter = pdi.searchquery.replaceAll("~MATCH_ALL_OP~",
                        matchAll ? pdi.intersectionOperator:pdi.unionOperator);
                    ldapArgs = new Object[numArgs];
                    ldapArgs[0] = username;
                    ldapArgs[1] = firstname;
                    ldapArgs[2] = lastname;
                    ldapArgs[3] = email;
                }

                for (int j=0; j<numArgs; j++) {
                    String wildcardValue = "";
                    if (!"".equals((String)ldapArgs[j]) || matchAll ||
                        (!matchAll && openQuery)) {
                        wildcardValue = pdi.wildcard;
                    }
                    filter = filter.replaceAll("~WILDCARD_"+j+"~",
                        wildcardValue);
                }
                
                queryResults = processLdapSearch(filter, ldapArgs, pdi);
                //results.addAll(createPeople(queryResults));
            }
            if (pdi.url.startsWith("jdbc:")) {
                queryResults = processJdbcSearch(usernames, username, firstname,
                    lastname, email, matchAll, pdi);
                //results.addAll(createPeople(queryResults));
            }
            
            if(queryResults != null){
                queryResults = createPeople(queryResults);
                Iterator it = queryResults.iterator();
                User p;
                String key = null;
                String value = null;
                while(it.hasNext()){
                    p = (User)it.next();
                    if(results.contains(p)){
                        User dup = (User)results.get(results.indexOf(p));
                        Iterator keys = p.getAttributes().keySet().iterator();                        
                        while (keys.hasNext()) {
                            key = (String)keys.next();
                            value = (String)p.getAttribute(key);
                            dup.setAttribute(key, value);
                        }
                    }else{
                        results.add(p);
                    }
                    
                }
            }
        }
        return results;
    }

    // make null values empty strings
    private String normalize(String param) {
        return param == null ? "" : param;
    }

    private String createLdapUserFilter(PersonDirInfo pdi, boolean matchAll,
        String username, String firstname, String lastname,
        String email, String[] usernames) {

        StringBuffer userFilter = new StringBuffer(
            usernames.length*pdi.uidquery.length());

        for (int i=0; usernames != null && i<usernames.length; i++) {
            userFilter.append(pdi.uidSelect.replaceAll("~ARG~", ""+(i+4)));
        }

        String matchAllOp = matchAll ? pdi.intersectionOperator :
            pdi.unionOperator;

        return pdi.searchquery2.replaceAll("~SEARCH_QUERY~",
            pdi.searchquery).replaceAll("~USER_FILTER~",
                userFilter.toString()).replaceAll("~MATCH_ALL_OP~", matchAllOp);
    }

    private List createPeople(List paramsList) throws Exception {
        List people = new ArrayList();
        Hashtable attribs = null;
        String key        = null;
        String value      = null;
        IPerson person    = null;
        User    user      = null;
        if (paramsList != null) {
            Iterator itr = paramsList.iterator();
            while (itr.hasNext()) {
                attribs = (Hashtable)itr.next();
                person = new PersonImpl();
                Enumeration keys = attribs.keys();
                while (keys.hasMoreElements()) {
                    key = (String)keys.nextElement();
                    value = (String)attribs.get(key);
                    person.setAttribute(key, value);
                }
                user = buildUser(person);
                if (user != null)
                    people.add(user);
            }
        }

        return people;
    }

    private List processLdapSearch(String searchQuery, Object[] args,
        PersonDirInfo pdi) {

        List results = new ArrayList();
        Hashtable attribs = null;
        String value = null;
        String attName = null;
        Attribute tattrib = null;
        SearchResult result = null;
        Attributes ldapattribs = null;

        LogService.log(LogService.DEBUG,
            "PersonDirectoryServiceImpl::processLdapSearch() filter: " +
            searchQuery);

        //JNDI boilerplate to connect to an initial context
        Hashtable jndienv = new Hashtable();
        DirContext context = null;
        jndienv.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        jndienv.put(Context.SECURITY_AUTHENTICATION,"simple");
        
	/********************************************************************/
        /* Changed by Mike Marquiz 10/08/03
           The security protocol needs to be changed to ssl if secure LDAP
           {ldaps} is being utilized.  Further, the PROVIDER_URL also needs
           to be changed from ldaps:// to ldap://.  There is no such thing as
           a ldaps protocol.  All LDAP connectivity is done using the ldap://
           construct.  The only difference between the two is in the setting 
           of the SECURITY_PROTOCOL attribute.
           

           Old Code:
                     if (pdi.url.startsWith("ldaps:")) {
                         jndienv.put(Context.SECURITY_PROTOCOL,"ssl");
                     }

                     jndienv.put(Context.PROVIDER_URL,pdi.url);

           New Code:
                     if (pdi.url.startsWith("ldaps:")) {
                              String newurl = pdi.url.substring(0,4) +
                                              pdi.url.substring(5);
                              jndienv.put(Context.PROVIDER_URL, newurl);
                              jndienv.put(Context.SECURITY_PROTOCOL,"ssl");
                     } else {
                              jndienv.put(Context.PROVIDER_URL,pdi.url);
                     }
        */
        /********************************************************************/

        if (pdi.url.startsWith("ldaps:")) {
            String newurl = pdi.url.substring(0,4) +
                            pdi.url.substring(5);
            jndienv.put(Context.PROVIDER_URL, newurl);
            jndienv.put(Context.SECURITY_PROTOCOL,"ssl");
        } else {
            jndienv.put(Context.PROVIDER_URL,pdi.url);
        }
	
	if (pdi.logonid!=null) {
            jndienv.put(Context.SECURITY_PRINCIPAL,pdi.logonid);
        }
        if (pdi.logonpassword!=null) {
            jndienv.put(Context.SECURITY_CREDENTIALS,pdi.logonpassword);
        }

        try {
            context = new InitialDirContext(jndienv);
        } catch (NamingException nex) {
            LogService.instance().log(LogService.ERROR,
                "PersonDirectoryServiceImpl::processLdapSearch(): "+
                "Error getting InitialDirContext: " + nex, nex);
            return results;
        }

        // Search for the userid in the usercontext subtree of the directory
        // Use the uidquery substituting username for {0}
        NamingEnumeration userlist = null;
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {

            for (int i=0; args != null && i<args.length; i++) {
                LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processLdapSearch() : arg " + i + ": " + args[i]);
            }
            userlist = context.search(pdi.usercontext,searchQuery,args,sc);
        } catch (NamingException nex) {
            LogService.instance().log(LogService.ERROR,
                "PersonDirectoryServiceImpl::processLdapSearch(): "+
                "Error during search: " + nex, nex);
            try {
                context.close();
                context = null;
            } catch (NamingException ne) {
                LogService.instance().log(LogService.ERROR,
                    "PersonDirectoryServiceImpl::processLdapSearch(): "+
                    "Error cleaning up naming context: " + ne, ne);
            }
            return results;
        }

        // If one object matched, extract properties from the attribute list
        try {
            while (userlist.hasMoreElements()) {
                attribs = new Hashtable();
                result = (SearchResult) userlist.next();
                ldapattribs = result.getAttributes();
                for (int i=0;i<pdi.attributenames.length;i++) {
                    tattrib = null;
                    if (pdi.attributenames[i] != null) {
                        tattrib = ldapattribs.get(pdi.attributenames[i]);
                    }
                    if (tattrib!=null) {
                        value = tattrib.get().toString();
                        attribs.put(pdi.attributealiases[i],value);
                    }
                }
                results.add(attribs);
            }
        } catch (NamingException nex) {
            LogService.instance().log(LogService.ERROR,
                "PersonDirectoryServiceImpl::processLdapSearch(): "+
                "Error during search: " + nex, nex);
            return results;
        } finally {
            try {
                userlist.close();
                userlist = null;
            } catch (NamingException ne) {
                LogService.instance().log(LogService.ERROR,
                    "PersonDirectoryServiceImpl::processLdapSearch(): "+
                    "Error cleaning up naming enumeration: " + ne, ne);
            }
            try {
                context.close();
                context = null;
            } catch (NamingException ne) {
                LogService.instance().log(LogService.ERROR,
                    "PersonDirectoryServiceImpl::processLdapSearch(): "+
                    "Error cleaning up naming context: " + ne, ne);
            }
        }

        return results;
    }

    private List processJdbcSearch(String[] usernames, String username,
        String firstname, String lastname, String email,
        boolean matchAll, PersonDirInfo pdi) {

        List results = new ArrayList();
        Hashtable attribs = null;

        String value = null;
        String attName = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String matchAllOp = matchAll ? pdi.intersectionOperator :
                pdi.unionOperator;
            String sql = null;

            if (matchAll) {
                sql = pdi.searchquery;
            } else {
                sql = pdi.searchquery2;
            }
            if (usernames != null) {
                StringBuffer sb = new StringBuffer(usernames.length*
                    (pdi.uidSelect.length()+pdi.unionOperator.length()+2));
                for (int i=0; i<usernames.length; i++) {
                    if (i>0) {
                        sb.append(' ').append(pdi.unionOperator).append(' ');
                    }
                    sb.append(pdi.uidSelect);
                }
                sql = sql.replaceAll("~USER_FILTER~", sb.toString());
            } else {
                sql = sql.replaceAll("~USER_FILTER~", pdi.trueClause);
            }

            // Get a connection with URL, userid, password
            if (pdi.ResRefName!=null && pdi.ResRefName.length()>0) {
                conn = RDBMServices.getConnection(pdi.ResRefName);
                LogService.log(LogService.DEBUG,"PersonDirectoryServiceImpl::processJdbcSearch(): Looking in "+pdi.ResRefName+ " for person searching");
            } else {
              conn = RDBMServices.getConnection();
            }

            // Execute query substituting Username for parameter
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : PDI JDBC search query: " + sql);
            stmt = conn.prepareStatement(sql);

            int i=1;
            for (int j=0; usernames != null && j<usernames.length; j++) {
                LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : arg " + i + ": " + usernames[j]);
                stmt.setString(i++, usernames[j]);
            }
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : arg " + i + ": " + username);
            stmt.setString(i++, username);
            stmt.setString(i++, username);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : arg " + i + ": " + firstname);
            stmt.setString(i++, firstname);
            stmt.setString(i++, firstname);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : arg " + i + ": " + lastname);
            stmt.setString(i++, lastname);
            stmt.setString(i++, lastname);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl::processJdbcSearch() : arg " + i + ": " + email);
            stmt.setString(i++, email);
            stmt.setString(i++, email);
            rs = stmt.executeQuery();
            while (rs.next()) {

                attribs = new Hashtable(pdi.attributenames.length);

                // Foreach attribute, put its value and alias in the hashtable
                for (i=0;i<pdi.attributenames.length;i++) {
                    try {
                        value = null;
                        attName = pdi.attributenames[i];
                        if (attName != null && attName.length() != 0) {
                            value = rs.getString(attName);
                        }
                        if (value!=null) {
                            attribs.put(pdi.attributealiases[i],value);
                        }
                    } catch (SQLException sqle) {
                        // Don't let error in a field prevent processing of
                        // others.
                        LogService.log(LogService.DEBUG,"PersonDirectoryServiceImpl::processJdbcSearch(): Error accessing JDBC field "+pdi.attributenames[i]+" "+sqle, sqle);
                    }
                }
                results.add(attribs);
            }
        } catch (Exception e) {
            // If database down or can't logon, ignore this data source
            // It is not clear that we want to disable the source, since the
            // database may be temporarily down.
            LogService.log(LogService.DEBUG,
                "PersonDirectoryServiceImpl::processJdbcSearch(): Error "+e, e);
        } finally {
            try {
                if (rs!=null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                LogService.log(LogService.ERROR,
                    "PersonDirectoryServiceImpl::processJdbcSearch(): " +
                    "Error cleaning up jdbc resources: "+e, e);
            }

            try {
                if (stmt!=null) {
                    stmt.close();
                    stmt = null;
                }
/** HEK - use RDBMServices
                if (conn!=null) {
                    conn.close();
                    conn = null;
                }
*/
            } catch (Exception e) {
                LogService.log(LogService.ERROR,
                    "PersonDirectoryServiceImpl::processJdbcSearch(): " +
                    "Error cleaning up jdbc resources: "+e, e);
            }
            RDBMServices.releaseConnection(conn);
        }

        return results;
    }

    /**
     * Parse XML file and create PersonDirInfo objects
     *
     */
    private synchronized boolean getParameters() {
        if (sources!=null)
          return true;
        sources= new Vector();
        try  {

            // Build a DOM tree out of uPortal/properties/ToroPersonDirs.xml
            Document doc = ResourceLoader.getResourceAsDocument(
                this.getClass(), "/properties/ToroPersonDirs.xml");

            // Each directory source is a <PersonDirInfo> (and its contents)
            NodeList list = doc.getElementsByTagName("PersonDirInfo");
            for (int i=0;i<list.getLength();i++) { // foreach PersonDirInfo
                Element dirinfo = (Element) list.item(i);
                PersonDirInfo pdi = new PersonDirInfo(); //object holding params
                for (Node param = dirinfo.getFirstChild();
                    param!=null; // foreach tag under the <PersonDirInfo>
                    param=param.getNextSibling()) {
                    if (!(param instanceof Element)) {
                        continue; // whitespace (typically \n) between tags
                    }
                    Element pele = (Element) param;
                    String tagname = pele.getTagName();
                    String value = getTextUnderElement(pele);

                    // each tagname corresponds to an object data field
                    if (tagname.equals("url")) {
                        pdi.url=value;
                    } else if (tagname.equals("res-ref-name")) {
                        pdi.ResRefName=value;
                    } else if (tagname.equals("logonid")) {
                        pdi.logonid=value;
                    } else if (tagname.equals("driver")) {
                        pdi.driver=value;
                    } else if (tagname.equals("logonpassword")) {
                        pdi.logonpassword=value;
                    } else if (tagname.equals("uidquery")) {
                        pdi.uidquery=value;
                    } else if (tagname.equals("fullnamequery")) {
                        pdi.fullnamequery=value;
                    } else if (tagname.equals("usercontext")) {
                        pdi.usercontext=value;
                    } else if (tagname.equals("searchquery")) {
                        pdi.searchquery=value;
                    } else if (tagname.equals("searchquery2")) {
                        pdi.searchquery2=value;
                    } else if (tagname.equals("unionOperator")) {
                        pdi.unionOperator=value;
                    } else if (tagname.equals("intersectionOperator")) {
                        pdi.intersectionOperator=value;
                    } else if (tagname.equals("wildcard")) {
                        pdi.wildcard=value;
                    } else if (tagname.equals("trueClause")) {
                        pdi.trueClause=value;
                    } else if (tagname.equals("uidSelect")) {
                        pdi.uidSelect=value;
                    } else if (tagname.equals("attributes")) {
                        NodeList anodes=pele.getElementsByTagName("attribute");
                        int anodecount = anodes.getLength();
                        if (anodecount!=0) {
                            pdi.attributenames = new String[anodecount];
                            pdi.attributealiases = new String[anodecount];
                            for (int j =0; j<anodecount;j++) {
                                Element anode = (Element) anodes.item(j);
                                NodeList namenodes =
                                    anode.getElementsByTagName("name");
                                String aname = "$$$";
                                if (namenodes.getLength()!=0) {
                                    aname=getTextUnderElement(
                                        namenodes.item(0));
                                }
                                pdi.attributenames[j]=aname;
                                NodeList aliasnodes =
                                    anode.getElementsByTagName("alias");
                                if (aliasnodes.getLength()==0) {
                                    pdi.attributealiases[j]=aname;
                                } else {
                                    pdi.attributealiases[j]=
                                        getTextUnderElement(aliasnodes.item(0));
                                }
                            }
                        } else {
                            // The <attributes> tag contains a list of names
                            // and optionally aliases each in the form
                            // name[:alias]
                            // The name is an LDAP property or database column
                            // name. The alias, if it exists, is an eduPerson
                            // property that corresponds to the previous LDAP
                            // or DBMS name. If no alias is specified,
                            // the eduPerson name is also the LDAP or
                            // DBMS column name.
                            StringTokenizer st = new StringTokenizer(value);
                            int n = st.countTokens();
                            pdi.attributenames = new String[n];
                            pdi.attributealiases = new String[n];
                            for (int k=0;k<n;k++) {
                                String tk = st.nextToken();
                                int pos =tk.indexOf(':');
                                if (pos>0) { // There is an alias
                                    pdi.attributenames[k]=tk.substring(0,pos);
                                    pdi.attributealiases[k]=tk.substring(pos+1);
                                } else { // There is no alias
                                    pdi.attributenames[k]=tk;
                                    pdi.attributealiases[k]=tk;
                                }
                            }
                        }
                    } else {
                        LogService.instance().log(LogService.ERROR,"PersonDirectory::getParameters(): Unrecognized tag "+tagname+" in ToroPersonDirs.xml");
                    }
                }
                if (pdi.ResRefName != null && !"".equals(pdi.ResRefName)) {
                    pdi.url = AcademusDBUtil.getJdbcUrl(pdi.ResRefName);
                    pdi.driver = AcademusDBUtil.getJdbcDriver(pdi.ResRefName);
                    pdi.logonid = AcademusDBUtil.getJdbcUser(pdi.ResRefName);
                }
                pdi.dump();
                sources.addElement(pdi);//Add one LDAP or JDBC source to the list
            }
        } catch(Exception e) {
            LogService.instance().log(LogService.WARN,"PersonDirectory::getParameters(): properties/ToroPersonDirs.xml is not available, directory searching disabled.");
            return false;
        }
        return true;
    }

    private String getTextUnderElement(Node nele) {
        if (!(nele instanceof Element)) {
            return null;
        }
        Element pele = (Element) nele;
        StringBuffer vb = new StringBuffer();
        NodeList vnodes = pele.getChildNodes();
        for (int j =0; j<vnodes.getLength();j++) {
            Node vnode = vnodes.item(j);
            if (vnode instanceof Text) {
                vb.append(((Text)vnode).getData());
            }
        }
        return vb.toString();
    }

    private class PersonDirInfo {
        String ResRefName; // Resource Reference name for a J2EE style DataSource
        String url; // protocol, server, and initial connection parameters
        String driver; // JDBC java class to register
        String logonid; // database userid or LDAP user DN (if needed)
        String logonpassword; // password
        String usercontext; // where are users? "OU=people" or "CN=Users"
        String uidquery; // SELECT or JNDI query for userid
        String fullnamequery; // SELECT or JNDI query using fullname
        String searchquery; // SELECT or JNDI query using first, last, email
        String searchquery2; // searchquery filting certain users
                                          // by username
        String unionOperator; // the union operator for the particular filter
        String intersectionOperator; // the intersection operator
        String trueClause;
        String wildcard;
        String uidSelect;
        String[] attributenames;
        String[] attributealiases;
        boolean disabled = false;
        boolean logged = false;

        public void dump() {
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo ResRefName          : " + ResRefName);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo url                 : " + url);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo driver              : " +driver);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo logonid             : " + logonid);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo logonpassword       : " + logonpassword);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo usercontext         : " + usercontext);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo uidquery            : " + uidquery);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo fullnamequery       : " + fullnamequery);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo searchquery         : " + searchquery);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo searchquery2        : " + searchquery2);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo unionOperator       : " + unionOperator);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo intersectionOperator: " + intersectionOperator);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo trueClause          : " + trueClause);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo wildcard            : " + wildcard);
            LogService.log(LogService.DEBUG, "PersonDirectoryServiceImpl:PersonDirInfo uidSelect           : " + uidSelect);
        }
    }

    private User buildUser(IPerson person) throws Exception {

        String username  = normalize((String)person.getAttribute(uidKey));
        String firstName = normalize((String)person.getAttribute(firstnameKey));
        String lastName  = normalize((String)person.getAttribute(lastnameKey));
        String password  = null;
        String email     = normalize((String)person.getAttribute(emailKey));
        
        User rtnUser = null;
        if (username != null) {
            rtnUser = UserFactory.getUser(
                            -1,
                            username, 
                            password, 
                            firstName, 
                            lastName, 
                            email, 
                            new HashMap());
        }
        return rtnUser;
    }
}

