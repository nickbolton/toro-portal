package net.unicon.portal.channels.ldapchangepw;

import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

/*  CLdapChangePW - LDAP/Active Directory password change channel
 *
 *  This channel can be configured to change the password of LDAP or Active Directory users.  SHA-1
 *  and MD5 hash types are supported for LDAP servers, as well as no hash type ('none') if desired.
 *  SSL-enabled connection types are supported as well.  An SSL-enabled connection is required to
 *  change a user's password in Active Directory.  The domain controller's SSL certificate must be
 *  added to Java's keystore (using the 'keytool' command).
 */

public class CLdapChangePW implements IChannel {
    ChannelStaticData staticData = null;
    ChannelRuntimeData runtimeData = null;
    private static final String sslLocation = "CLdapChangePW.ssl";
    private boolean passwordSet = false;
    private boolean passwordConfirm = false;
    private boolean userNotFound = false;
    private boolean wrongCurrentPassword = false;
    private String LDAPHost;		// The URL to the LDAP server, ex: ldap://ldap.example.edu:389
    private String LDAPUser;		// An LDAP user that can modify the password attribute,
					// ex: CN=Manager, dc=example, dc=edu
    private String LDAPPass;		// The password for the LDAP user, ex: secret
    private String LDAPBaseDN;		// The base DN where users are stored, ex: dc=example,dc=edu
    private String hashType;		// LDAP only : "MD5", "SHA-1", or "none"
    private String isSSL;		// Connect to LDAP with SSL? "yes" or "no"
    private String isActiveDirectory;	// Is this an Active Directory? "yes" or "no"
    private String askForCurrent;	// Force the user to verify their current password? "yes" or "no"

    private String username;		// The user accessing this channel
    private String newPass;		// The user's new password
    private String confirmPass;		// The confirmed new password
    private String curPass;		// The user's current password
    
    private Log log = LogFactory.getLog(CLdapChangePW.class);


    /*  Constructs a CLdapChangePW.
     */

    public CLdapChangePW() {
        this.staticData = new ChannelStaticData();
        this.runtimeData = new ChannelRuntimeData();
    }

    /*  Returns channel runtime properties
     *  @return handle to runtime properties
     */

    public ChannelRuntimeProperties getRuntimeProperties() {
        // Channel will always render, so the default values are ok
        return new ChannelRuntimeProperties();
    }

    /*  Processes layout-level events coming from the portal
     *  @param ev a portal layout event
     */

    public void receiveEvent(PortalEvent ev) {
        // no events for this channel
    }

    /*  Receive static channel data from the portal
     *  @param sd static channel data
     */

    public void setStaticData(ChannelStaticData sd) {
        this.staticData = sd;
        LDAPHost		= sd.getParameter("LDAPHost");
        LDAPUser		= sd.getParameter("LDAPUser");
        LDAPPass		= sd.getParameter("LDAPPass");
        LDAPBaseDN		= sd.getParameter("LDAPBaseDN");
        hashType		= sd.getParameter("hashType"); // SHA-1 or none
        isSSL			= sd.getParameter("isSSL");
        isActiveDirectory 	= sd.getParameter("isActiveDirectory");
        askForCurrent		= sd.getParameter("askForCurrent");

	// guess default values if none provided
	if (hashType == null) {
	    hashType = "SHA-1";
	}
	if (isSSL == null) {
	    isSSL = "no";
	}
	if (isActiveDirectory == null) {
	    isActiveDirectory = "no";
	}
	if (askForCurrent == null) {
	    askForCurrent = "no";
	}
    }

    /*  Receives channel runtime data from the portal and processes actions
     *  passed to it.  The names of these parameters are entirely up to the channel.
     *  @param rd handle to channel runtime data
     */

    public void setRuntimeData(ChannelRuntimeData rd) {
        this.runtimeData = rd;
        // retrieve user input and persondir attribute
        curPass		= runtimeData.getParameter("curPass");
        newPass		= runtimeData.getParameter("newPass");
        confirmPass	= runtimeData.getParameter("confirmPass");
        username	= (String)staticData.getPerson().getAttribute("username");
        // if a password was entered into the first field
        if (newPass != null)
        {
            passwordSet = true;
		    // if it matches what was entered into the confirm field
	   	    if (newPass.equals(confirmPass)) {
		        passwordConfirm = true;
				// if the channel is published to use the active directory method
				if (isActiveDirectory.equals("true") || isActiveDirectory.equals("yes")) {
				    setActiveDirectoryPassword();
				// else use the ldap method
				} else {
				    setLDAPPassword();
				}
	   	    }
        }
    }

    public void setActiveDirectoryPassword() {
	// setup our environment
	Hashtable env = new Hashtable();
	env.put(Context.SECURITY_AUTHENTICATION,"simple");
	env.put(Context.SECURITY_PRINCIPAL,LDAPUser);
	env.put(Context.SECURITY_CREDENTIALS,LDAPPass);
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL,LDAPHost);
	env.put(Context.SECURITY_PROTOCOL, "ssl"); // Active Directory always requires SSL to change passwords
	try {
	    // try to connect to Active Directory
        LdapContext ctx = new InitialLdapContext(env, null);
	    // setup our search
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration answer = ctx.search(LDAPBaseDN, "(sAMAccountName="+username+")", ctls);
	    // retrieve our result
        String targetUser = ((SearchResult)answer.next()).getName();
        String targetUserDN = targetUser + "," + LDAPBaseDN;
	    // should be logged properly
        if (log.isInfoEnabled()) {
		    log.info("Changing password for user: " + targetUserDN);
        }
	    // user was found
	    if (targetUserDN != null) {
	        userNotFound = false;
			if (askForCurrent.equals("yes") || askForCurrent.equals("true")) {
	    	    Hashtable userenv = new Hashtable();
		        userenv.put(Context.SECURITY_AUTHENTICATION,"simple");
	            userenv.put(Context.SECURITY_PRINCIPAL,targetUserDN);
	            userenv.put(Context.SECURITY_CREDENTIALS,curPass);
	            userenv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	            userenv.put(Context.PROVIDER_URL,LDAPHost);
		 	    userenv.put(Context.SECURITY_PROTOCOL, "ssl");
			    // attempt to bind with user's account and supplied password
		 	    try {
		 	    	LdapContext userctx = new InitialLdapContext(userenv, null);
			    } catch (NamingException e) {
                    if (log.isInfoEnabled()) {
	                    log.info("Wrong current password", e);
                    }
			    	wrongCurrentPassword = true;
			    }
			}
			if (wrongCurrentPassword == false) {
	            // convert password to Active Directory format
			    String quotedPassword = "\"" + newPass + "\"";
			    char unicodePwd[] = quotedPassword.toCharArray();
			    byte pwdArray[] = new byte[unicodePwd.length * 2];
			    for (int i=0; i<unicodePwd.length; i++) {
			        pwdArray[i*2 + 1] = (byte) (unicodePwd[i] >>> 8);
			        pwdArray[i*2 + 0] = (byte) (unicodePwd[i] & 0xff);
			    }
			    // modify the password attribute
	            ModificationItem[] mods = new ModificationItem[1];
	            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					           new BasicAttribute("unicodePwd", pwdArray));
	            ctx.modifyAttributes(targetUserDN, mods);
			}
	    // user was not found
	    } else {
	        userNotFound = true;
	    }
        } catch (NamingException e) {
            log.error("Naming Exception: " + e.getMessage(), e);
            throw new RuntimeException("NamingException: Ldap Connection failed to initialize\n" , e);
        }
    }

    public void setLDAPPassword() {
        String encPass = null;
        // setup our environment
        Hashtable env = new Hashtable();
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL,LDAPUser);
        env.put(Context.SECURITY_CREDENTIALS,LDAPPass);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,LDAPHost);
		if (isSSL.equals("yes") || isSSL.equals("true")) {
		    env.put(Context.SECURITY_PROTOCOL, "ssl");
		}

        try {
        	// connect to LDAP
            LdapContext ctx = new InitialLdapContext(env, null);
            // setup our search
            SearchControls ctls = new SearchControls();
            ctls.setReturningObjFlag (true);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration answer = ctx.search(LDAPBaseDN, "(uid=" + username + ")", ctls);
		    // retrieve results
		    String targetUser = ((SearchResult)answer.next()).getName();
		    String targetUserDN = targetUser + "," + LDAPBaseDN;
            if (log.isInfoEnabled()) {
			    log.info("Changing password for user: targetuserdn = " + targetUserDN);
            }
		    // a user was found
		    if (targetUserDN != null) {
		        userNotFound = false;
				// confirm the user's current password
				if (askForCurrent.equals("yes") || askForCurrent.equals("true")) {
	        	    Hashtable userenv = new Hashtable();
	    	        userenv.put(Context.SECURITY_AUTHENTICATION,"simple");
		            userenv.put(Context.SECURITY_PRINCIPAL,targetUserDN);
		            userenv.put(Context.SECURITY_CREDENTIALS,curPass);
		            userenv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		            userenv.put(Context.PROVIDER_URL,LDAPHost);
		    	    if (isSSL.equals("yes") || isSSL.equals("true")) {
		    	    	userenv.put(Context.SECURITY_PROTOCOL, "ssl");
		    	    }

		    	    // attempt to bind with user's account and supplied password
		            try {
		            	LdapContext userctx = new InitialLdapContext(userenv, null);
				    } catch (NamingException e) {
                        if (log.isInfoEnabled()) {
                            log.info("Wrong current password", e);
                        }
				    	wrongCurrentPassword = true;
				    }
				}
				if (wrongCurrentPassword == false) {
				    // set the encryption type
				    encPass = LDAPPassword(newPass);
				    // change the password
		            ModificationItem[] mod = new ModificationItem[1];
	                mod[0] = new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,
			   			          new BasicAttribute("userPassword", encPass));
		            ctx.modifyAttributes(targetUserDN, mod);
				}
		    // user was not found
		    } else {
		        userNotFound = true;
		    }
        } catch (NamingException e) {
            log.error("NamingException: Ldap Connection failed to initialize", e);
            throw new RuntimeException("NamingException: Ldap Connection failed to initialize\n" , e);
        } /*catch (NoSuchAlgorithmException e) {
	    System.out.println("Invalid hashType, try SHA-1 or none: " + e);
	}*/
    }

    /*  Output channel content to the portal
     *  @param out a sax document handler
     */
    public void renderXML(ContentHandler out) throws PortalException {
        StringWriter w = new StringWriter();
        w.write("<?xml version='1.0'?>\n");
        w.write("<content>\n");

        if (passwordSet && passwordConfirm) {
       	    w.write("<Confirm>1</Confirm>\n");
        } else if (passwordSet && !passwordConfirm) {
            w.write("<noConfirm>1</noConfirm>\n");
        }

        if (userNotFound) {
        	w.write("<userNotFound>1</userNotFound>\n");
        }

        if (wrongCurrentPassword) {
        	w.write("<wrongCurrentPassword>1</wrongCurrentPassword>\n");
        }

		if (askForCurrent.equals("yes") || askForCurrent.equals("true")) {
		    w.write("<askForCurrent>1</askForCurrent>\n");
		}

        w.write("</content>\n");

        XSLT xslt = XSLT.getTransformer(this);
        xslt.setXML(w.toString());
        xslt.setXSL(sslLocation, "main", runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
    }

    public String LDAPPassword(String newPass) {
		String encPass = null;
		if (!hashType.equals("none")) {
		    try {
	            MessageDigest md = MessageDigest.getInstance(hashType);
	            md.reset();
	            md.update(newPass.getBytes());
	  	        if (hashType.equals("SHA-1")) {
	                encPass = "{SHA}" + encode(md.digest());
	  	        }
		    } catch (NoSuchAlgorithmException e) {
		        log.error("Invalid hashType, try SHA-1 or none", e);
		        throw new RuntimeException("Invalid hashType, try SHA-1 or none: \n" , e);
		    }
		} else {
		    encPass = newPass;
		}
		return encPass;
    }


    /*  encode - encodes a string to base64 for LDAP password storage
     */


    public static String encode(String raw) {
    	return encode(raw.getBytes());
    }

    public static String encode(byte[] raw) {
        StringBuffer encoded = new StringBuffer();
        for (int i = 0; i < raw.length; i += 3) {
        	encoded.append(encodeBlock(raw, i));
        }
        return encoded.toString();
    }

    public static char[] encodeBlock(byte[] raw, int offset) {
        int block = 0;
        int slack = raw.length - offset - 1;
        int end = (slack >= 2) ? 2 : slack;
        for (int i = 0; i <= end; i++) {
	  	    byte b = raw[offset + i];
		    int neuter = (b < 0) ? b + 256 : b;
		    block += neuter << (8 * (2 - i));
        }
        char[] base64 = new char[4];
        for (int i = 0; i < 4; i++) {
		    int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
		    base64[i] = getChar_encode(sixbit);
        }
        if (slack < 1) base64[2] = '=';
        if (slack < 2) base64[3] = '=';
        return base64;
    }

    public static char getChar_encode(int sixBit) {
        if (sixBit >= 0 && sixBit <= 25)
            return (char)('A' + sixBit);
        if (sixBit >= 26 && sixBit <= 51)
            return (char)('a' + (sixBit - 26));
        if (sixBit >= 52 && sixBit <= 61)
            return (char)('0' + (sixBit - 52));
        if (sixBit == 62) return '+';
        if (sixBit == 63) return '/';
        return '?';
    }
}
