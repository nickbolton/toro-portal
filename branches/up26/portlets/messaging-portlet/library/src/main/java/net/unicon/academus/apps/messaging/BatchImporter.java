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

package net.unicon.academus.apps.messaging;

import java.io.FileInputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.unicon.academus.apps.ConfigHelper;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.alchemist.rdbms.SimpleDataSource;
import net.unicon.civis.ICivisFactory;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Command line Batch Message Importer.
 *
 * @see #usage()
 */
public class BatchImporter {
    private static final String DEFAULT_CONFIG = "/config/messaging-portlet.xml";
    
    /**
     * Parse command line options into a hashmap of name-value pairs.
     * Arguments with values are considered those provided as '-myarg=myvalue',
     * split at the '='.
     *
     * Non-value arguments will have {@link Boolean#TRUE Boolean.TRUE} as their
     * paired value.
     *
     * Any arguments that do not begin with a '-' character will be considered
     * non-arguments, and be stored in the special key 'nonarg' as a List.
     *
     * If no arguments are provided, or an argument named '-help' or '-?' is
     * provided, this method will call {@link #usage()} and exit.
     */
    public static Map getopt(String[] args) {
        Map rslt = new HashMap();
        rslt.put("nonarg", new ArrayList());

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].indexOf('=') != -1) {
                    String[] tmp = args[i].substring(1).split("=", 2);
                    rslt.put(tmp[0], tmp[1]);
                } else {
                    rslt.put(args[i].substring(1), Boolean.TRUE);
                }
            } else {
                ((List)rslt.get("nonarg")).add(args[i]);
            }
        }

        if (args.length == 0 || rslt.containsKey("help") || rslt.containsKey("?"))
            usage();

        return rslt;
    }

    public static void warn(String msg) {
        System.err.println(msg);
    }
    
    public static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    /**
     * Command usage.
     *
     * Usage for net.unicon.academus.apps.messaging.BatchImporter:<br>
     *         java -cp &lt;classpath&gt; net.unicon.academus.apps.messaging.BatchImporter [arguments] &lt;input files&gt;
     * 
     * <br>Arguments include:
     * <ul>
     *  <li>-config=&lt;configfile&gt; [Optional; Default
     *  "/config/messaging-portlet.xml" (from classpath)] Specify the location
     *  of the MessagingPortlet configuration file.
     * 
     *  <li>-messagecenter=&lt;messagecenter&gt; [Optional; Default
     *  "notifications"] Specify the message-center to be used.
     * 
     *  <li>-user=&lt;username&gt; [Optional; Default "admin"] Specify the
     *  username to use as the sender.
     * 
     *  <li>-groups=&lt;groups&gt; [Optional] Specify the groups that the user
     *  needs to be a part of. Comma delimited.
     * 
     *  <li>-altrecips=&lt;altrecips&gt;  [Optional] Specify a comma-delimited
     *  list of recipients to use in place of those specified in the import
     *  file.
     * </ul>
     * 
     * <p>In order to utilize database-oriented message centers, a DataSource must
     * be created. This can be done by providing the following properties (using the
     * -Dpropname=propvalue syntax for the 'java' command):</p>
     * <ul>
     *   <li>simpledatasource.driver - JDBC Driver class
     *   <li>simpledatasource.url    - Database URL
     *   <li>simpledatasource.user   - Database username
     *   <li>simpledatasource.pass   - Database password
     * </ul>
     *
     * <p>Alternatively, you can specify the property 'simpledatasource.propsfile'
     * to point to a file containing the above properties.</p>
     * 
     * At least one input file must be specified.
     */
    public static void usage() {
        System.out.println("Usage for "+BatchImporter.class.getName()+":");
        System.out.println("\tjava -cp <classpath> "+BatchImporter.class.getName()+" [arguments] <input files>");
        System.out.println();
        System.out.println("Arguments include: ");

        System.out.println("\t-config=<configfile>\t\t[Optional; Default \""+DEFAULT_CONFIG+"\"]");
        System.out.println("\t\t\t\t\tSpecify the location of the MessagingPortlet configuration file.");
        System.out.println();

        System.out.println("\t-messagecenter=<messagecenter>\t[Optional; Default \"notifications\"]");
        System.out.println("\t\t\t\t\tSpecify the message-center to be used.");
        System.out.println();

        System.out.println("\t-user=<username>\t\t[Optional; Default \"admin\"]");
        System.out.println("\t\t\t\t\tSpecify the username to use as the sender.");
        System.out.println();

        System.out.println("\t-groups=<groups>\t\t[Optional]");
        System.out.println("\t\t\t\t\tSpecify the groups that the user needs to be a part of. Comma delimited.");
        System.out.println();

        System.out.println("\t-altrecips=<altrecips>\t\t[Optional]");
        System.out.println("\t\t\t\t\tSpecify a comma-delimited list of recipients to use in place of those specified in the import file.");
        System.out.println();

        System.out.println();
        System.out.println("In order to utilize database-oriented message "
                + "centers, a DataSource must be created. This can be done by "
                + "providing the following properties (using the "
                + "-Dpropname=propvalue syntax for the 'java' command):");
        System.out.println("\tsimpledatasource.driver - JDBC Driver class");
        System.out.println("\tsimpledatasource.url    - Database URL");
        System.out.println("\tsimpledatasource.user   - Database username");
        System.out.println("\tsimpledatasource.pass   - Database password");
        System.out.println("Alternatively, you can specify the property "
                    + "'simpledatasource.propsfile' to point to a file containing "
                    + "the above properties.");
        System.out.println();

        System.out.println("At least one input file must be specified.");

        System.exit(2);
    }

    public static void main(String[] args) throws Exception {
        Map argmap = getopt(args);

        List largs = (List)argmap.get("nonarg");

        if (largs.isEmpty()) {
            warn("No input file(s) given.");
            usage();
        }

        Map mcenters = null;
        String configFile = (String)argmap.get("config");
        if (configFile == null) {
            URL u = BatchImporter.class.getResource(DEFAULT_CONFIG);
            if (u == null) {
                warn("No configuration file specified, and unable to load the default.\n"
                        + "Please specify one using the '-config=<myconfig>' argument.");
                usage();
            } else {
                configFile = u.toString();
            }
        }

        // Message Center lookup
        String mcenter = (String)argmap.get("messagecenter");
        if (mcenter == null) {
            mcenter = "notifications";
        }
        MessageCenter mc = configure(configFile, mcenter);
        if (mc == null)
            error("Unable to locate a message-center with the id: "+mcenter);
        System.out.println("Using message-center: "+mc.getLabel());


        // Civis factory parsing
        //ICivisFactory[] factories = configureCivis(configFile);
        ICivisFactory[] factories = null;
        
        // Access Broker related.
        List identities = new ArrayList();

        // What user do we need to be?
        String username = (String)argmap.get("user");
        if (username == null)
            username = "admin";
        identities.add(new Identity(username, IdentityType.USER));
        System.out.println("Using username: "+username);

        // Figure out what groups were requested to be a part of
        String tmp = (String)argmap.get("groups");
        if (tmp != null) {
            String[] groups = tmp.split("[,;:\t]");
            for (int i = 0; i < groups.length; i++) {
                identities.add(new Identity(groups[i], IdentityType.GROUP));
            }
        }
        // Always add Everyone
        identities.add(new Identity("Everyone", IdentityType.GROUP));

        // Look up the user's message factory
        Principal princ = new Principal((Identity[])identities.toArray(new Identity[0]));
        FactoryInfo finfo = mc.createFactory(princ);

        if (finfo == null)
            error("Unable to authorize user '"+username+"' for message-center '"+mc.getLabel()+"'.");

        IMessageFactory fact = finfo.getFactory();

        if (fact == null)
            error("Unable to authorize user '"+username+"' for message-center '"+mc.getLabel()+"'.");

        DraftMessage[] drafts = null;

        System.out.println();
        System.out.println("Parsing input files...");
        System.out.println();
        try {
            drafts = parseFiles(largs, factories);
        } catch (Exception e) {
            warn("Failed to parse input files. See stack trace for further information.");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        System.out.println("Done. "+drafts.length+" message(s) parsed.");

        tmp = (String)argmap.get("altrecips");
        if (tmp != null) {
            String[] altRecips = tmp.split("[,;:\t]");

            System.out.println();
            System.out.println("Altering recipients list...");

            alterRecipients(drafts, "To", altRecips);

            System.out.println("Done. "+drafts.length+" message(s) altered.");
        }

        System.out.println();
        System.out.println("Sending imported messages...");
        if (sendMessages(fact, drafts) == false)
            error("Failed to send imported messages. Please correct any errors ant try again.");
        System.out.println("Done. Batch import completed for "+drafts.length+" message(s).");

    }

    public static DraftMessage[] parseFiles(List largs, ICivisFactory[] factories) throws Exception {
        Iterator it = largs.iterator();
        List rslt = new ArrayList();
        boolean hasError = false;

        while (it.hasNext()) {
            String file = (String)it.next();

            System.out.print("Parsing file: "+file+" ");
            FileInputStream fin = new FileInputStream(file);
            
            DraftMessage[] drafts = null;
            
            try {
                drafts = ImportExportHelper.parseInputStream(fin, factories);
            } catch (XmlFormatException e) {
                System.out.println();
                warn("Unable to parse input file '"+file+"': "+e.getMessage());
                e.printStackTrace(System.err);
                hasError = true;
            }

            if (drafts == null || drafts.length == 0) {
                System.out.println();
                warn("No valid messages found in file: "+file);
                hasError = true;
            } else {
                rslt.addAll(Arrays.asList(drafts));
                System.out.println(" -- "+ drafts.length + " message(s)");
            }

        }

        System.out.println();
        if (hasError) {
            error(
                 "Unable to successfully parse at least one input file. Please "
               + "check the output and correct any errors before trying "
               + "again.");
        } else if (rslt.isEmpty()) {
            error("No valid messages found in any input file(s).\n"
                + "Please provide input files containing messages.");
        }

        return (DraftMessage[])rslt.toArray(new DraftMessage[0]);
    }

    public static void alterRecipients(DraftMessage[] drafts, String recipType, String[] altRecips) throws Exception {
        if (altRecips == null || altRecips.length == 0)
            error("No alternate recipients specified.");

        for (int i = 0; i < drafts.length; i++) {
            drafts[i].removeRecipients();
            
            for (int j = 0; j < altRecips.length; j++)
                drafts[i].addRecipient(recipType, altRecips[j]
                           , altRecips[j], EntityType.USER);
        }
    }

    public static boolean sendMessages(IMessageFactory fact, DraftMessage[] drafts) {
        List errors = new ArrayList();
        boolean hasError = false;

        // Validate the messages first.
        for (int i = 0; i < drafts.length; i++) {
            IRecipient[] recips = drafts[i].getRecipients();
            String subject = drafts[i].getSubject();
            String body = drafts[i].getBody();
            if (recips == null || recips.length == 0) {
                errors.add("No recipients provided.");
            }

            if (subject == null || subject.trim().equals("")) {
                errors.add("No subject provided.");
            }

            if (body == null || body.trim().equals("")) {
                errors.add("No message body provided.");
            }

            if (!errors.isEmpty()) {
                Iterator it = errors.iterator();
                warn("The following errors occurred while validating message "+i+":");
                while (it.hasNext()) {
                    warn("\t"+(String)it.next());
                }
                errors.clear();
                hasError = true;
            }
        }

        if (!hasError) {
            try {
                for (int i = 0; i < drafts.length; i++) {
                    System.out.println("["+i+"] Sending message: "+drafts[i].getSubject());
                    fact.sendMessage(drafts[i]);
                }
            } catch (MercuryException e) {
                warn("Unable to send message(s): "+e.getMessage());
                e.printStackTrace(System.err);
                hasError = true;
            }
        }

        return !hasError;
    }

    public static MessageCenter configure(String configFile, String mcenter) throws Exception {
        Element configElement = (Element)(new SAXReader())
                                    .read(configFile)
                                    .selectSingleNode("messaging");

        configElement = ConfigHelper.handle(configElement, false);

        // Bootstrap datasources with SimpleDataSource...
        List bsList = configElement.selectNodes("//*[@needsDataSource='true']");
        if (!bsList.isEmpty()) {
            try {
                DataSource ds = new SimpleDataSource();

                for (Iterator it = bsList.iterator(); it.hasNext();) {
                    Element e = (Element) it.next();

                    Attribute impl = e.attribute("impl");
                    if (impl == null)
                        throw new IllegalArgumentException(
                                "Elements with the 'needsDataSource' attribute "
                              + " must have an 'impl' attribute.");

                    Class.forName(impl.getValue())
                        .getDeclaredMethod("bootstrap",
                                new Class[] { DataSource.class })
                        .invoke(null, new Object[] { ds });
                }
            } catch (Throwable e) {
                e.printStackTrace(System.err);
                error("Failed the DataSource bootstrapping. Please correct any errors.");
            }
        }

        // Handle any global configuration for the message factories.
        List mList = configElement.selectNodes("message-factory");
        for (Iterator mIt = mList.iterator(); mIt.hasNext();) {
            Element e = (Element)mIt.next();
            Attribute impl = e.attribute("impl");
            if (impl == null)
                throw new IllegalArgumentException(
                        "Element <message-factory> must have an 'impl'"
                        + " attribute.");
            try {
                Class.forName(impl.getValue())
                    .getDeclaredMethod("parse",
                            new Class[] { Element.class })
                    .invoke(null, new Object[] { e });
            } catch (Throwable t) {
                throw new RuntimeException(
                        "Failed to parse the <message-factory> element.", t);
            }
        }

        Element e = (Element)configElement.selectSingleNode("message-center[@id='"+mcenter+"']");

        return new MessageCenter(e);
    }
    
    /* This is not used because the only civis implementation currently requires the full portal to operate.

    private static ICivisFactory[] configureCivis(String configFile) throws Exception{
        
        Element configElement = (Element)(new SAXReader())
        		.read(configFile)
        		.selectSingleNode("messaging");
        
        List facs = new ArrayList();
        
        Iterator it = configElement.selectNodes("civis").iterator();
        Element el = null;
        
        while(it.hasNext()){
            el = (Element)it.next();
	        if (el != null) {
	            Attribute impl = el.attribute("impl");
	            if (impl == null)
	                throw new IllegalArgumentException(
	                        "Element <civis> must have an 'impl'"
	                      + " attribute.");
	            try {
	                facs.add((ICivisFactory)Class.forName(impl.getValue())
	                     .getDeclaredMethod("parse",
	                             new Class[] { Element.class })
	                     .invoke(null, new Object[] { el }));
	            } catch (Throwable t) {
	                throw new RuntimeException(
	                    "Failed to parse the <civis> element.", t);
	            }
	        }
    	}
        return (ICivisFactory[])(facs.toArray(new ICivisFactory[0]));
    }
    */
}
