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

package net.unicon.academus.apps.announcement;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Hashtable;
import java.lang.reflect.Method;


import net.unicon.academus.apps.ApplicationDataManager;
import net.unicon.academus.commands.*;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.producer.ContentProducerException;
import net.unicon.academus.producer.IProducer;
import net.unicon.academus.producer.ProducerType;
import net.unicon.portal.permissions.Activity;
import net.unicon.sdk.properties.*;

import org.w3c.dom.*;
import org.apache.xerces.dom.DocumentImpl;

public final class AnnouncementProducer implements IProducer {

    // value: String (announcement ID affected by last command)

    private static final String CLASS_NAME = AnnouncementApp.class.getName();

    // use a set as we will never reference the same app more than once
    private Hashtable __appIDSet = null;

    // A Producer is a Singleton
    private static AnnouncementProducer __producer;

    /**
     * All producers must have a default constructor
     **/
    private AnnouncementProducer() {
    __appIDSet = new Hashtable();
    }

    // Singleton method
    public static IProducer getInstance() {
    if (__producer == null) {
        __producer = new AnnouncementProducer();
    }
    return __producer;
    }

    /**
     * Return the set of connection applictions to this producer
     * @return Set application IDs
     **/
    public Set getConnectedApplicationIDs() {
    return __appIDSet.keySet();
    }

    /**
     * Deallocate means to disconnect this client from this application via this
     * Producer, presumably because the client is being shutdown.
     **/
    public void deallocateAppConnection(String appId) throws ContentProducerException {
    try {
        if (appId != null && __appIDSet.remove(appId) != null) {
        ApplicationDataManager.removeApplication(appId);
        }
    }
    catch (Exception e) {
        e.printStackTrace();
        throw new ContentProducerException(e);
    }
    }

    /**
     * Gets the permissible activities. This method delegates to
     * the underlying application. The application keeps a static list
     * of activities for all applications instances, so it suffices to
     * just return the activities of the first application.
     */
    public Activity[] getActivities()
    throws ContentProducerException {
        try {
            String[] appIDs = (String[])getConnectedApplicationIDs().
                toArray(new String[0]);
        
            if (appIDs.length > 0) {
                AnnouncementApp app =
                    (AnnouncementApp)ApplicationDataManager.
                        getApplication(appIDs[0]);
                return app.getActivities();
            }
            return new Activity[0];
        } catch (Exception e) {
            throw new ContentProducerException(e);
        }
    }

    /**
     * Sets up what application is invoked
     */
    private AcademusAppCommand __setupCommand(final Map appData) throws ContentProducerException {
    // first get the command factory object
    // YYY This reflection on every request is going to be too expensive. Either store a
    // ref to this object here or don't use reflection
    Class factoryClass = null;
    UniconProperties props = UniconPropertiesFactory.getManager(AcademusPropertiesType.FACTORY);
    if (props != null) {
        try {
        factoryClass = Class.forName((String)props.get(CLASS_NAME + ".CommandFactory"));
        }
        catch (Exception exc) {
        exc.printStackTrace();
        throw new ContentProducerException("Cannot get command factory for " + 
                           CLASS_NAME + ".CommandFactory");
        }
    }
    else {
        throw new ContentProducerException("Cannot get command factory for " + 
                           CLASS_NAME + ".CommandFactory");
    }

    AcademusAppCommandFactory cfactory = null;
    AcademusAppCommand command = null;
    String[] cmdStr = null;
    try {
        Method m = factoryClass.getMethod("getInstance", null);
        // should be a static method
        cfactory = (AcademusAppCommandFactory)m.invoke(null, null);

        // read the command from the appData
        cmdStr = (String[])appData.get(IProducer.COMMAND);

        // instantiate a Command object
        command = cfactory.createCommand(cmdStr[0]);
    }
    catch (Exception exc) {
        exc.printStackTrace();
        throw new ContentProducerException(exc);
    }

    // Map the request onto an application object
    return command;
    }

    // get the app we're working on. Make sure it is initialized
    private AnnouncementApp __setupApp(final Map appData) throws ContentProducerException
    {
    try {
        String appId = ((String[])appData.get(IProducer.APP_ID))[0];
        AnnouncementApp app = (AnnouncementApp)ApplicationDataManager.getApplication(appId);
        if (app == null) {
        String[] initInfo = (String[])__appIDSet.get(appId);
        if (initInfo == null || initInfo.length < 2) {
            throw new ContentProducerException("Error initializing application " + appId);
        }
        String id = initInfo[0];
        String username = initInfo[1];
        String contextId = null;
        if (initInfo.length == 3) {
            contextId = initInfo[2];
        }
        app = (AnnouncementApp)ApplicationDataManager.initializeApplication(id, 
                                            username,
                                            contextId,
                                            CLASS_NAME,
                                            null);
        }
        return app;
    }
    catch (Exception exc) {
        exc.printStackTrace();
        throw new ContentProducerException(exc);
    }
    }

    /**
     * allocateAppConnection sets aside application resources (if needed) because a client
     * expects to use the app through this producer. It isn't actually required that the
     * application be instantiated, but a reference to it must be allocated.
     **/
    public String allocateAppConnection(String[] initInfo) throws ContentProducerException {
    String id = initInfo[0];
    String username = initInfo[1];
    String contextId = null;

    if (initInfo.length == 3) {
        contextId = initInfo[2];
    }
    String appId = ApplicationDataManager.generateAppId(id, username, contextId,
                                CLASS_NAME);
    // store the appId, even though the app object doesn't yet exist
    __appIDSet.put(appId, initInfo);
    return appId;
    }

    /**
     * Passes back content through the return Map.
     */
    public Map getContent(final Map appData) throws ContentProducerException {

    Map rval = null;

    // DEBUG ONLY, change to log service later
    // net.unicon.common.util.debug.DebugUtil.spillParameters(appData, 
    //                               "Starting AnnouncementProducer.getContent()");

    AcademusAppCommand command = __setupCommand(appData);

    // invoke the Command
    AnnouncementApp app = __setupApp(appData);
    try {
        // app is a reference to the application object. Get its state
        // and marshal it into a Map. synchronize on the app so nobody
        // else goes in and changes it state while I mess with it.
        // (Usually I'm the only one who would mess with it anyway, the
        // only time this is really needed is when the same app is shared
        // among producers/clients, a rare feat)
        synchronized (app) {
        rval = command.invokeOnApp(app.getApplicationID(), appData);

	/* DEBUG ONLY
        // net.unicon.common.util.debug.DebugUtil.spillParameters(appData, 
                  "Starting AnnouncementProducer.getContent() for (appID, username, contextID) (" +
                  app.getApplicationID() + "," + app.getUsername() + "," + app.getContextID() + " INPUT:\n");
	*/

        String xml = null;
        try {
            List announcementsList = app.getAnnouncements();
            xml = AnnouncementProducer.toXML(announcementsList);
            rval.put(IProducer.STATUS, IProducer.OK);
            rval.put(IProducer.STATUS_MSG, "");
            rval.put(IProducer.XML, xml);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            rval.put(IProducer.STATUS, IProducer.EXCEPTION);
            rval.put(IProducer.STATUS_MSG, exc.getMessage());
            rval.put(IProducer.XML, "<class-announcements></class-announcements>");
        }
        } // end synchronized on app

	/* DEBUG ONLY
        //net.unicon.common.util.debug.DebugUtil.spillParameters(rval, 
                                   "Ending AnnouncementProducer.getContent(), OUTPUT:\n");
	*/

        return rval;
    }
    catch (AcademusException ae) {
        ae.printStackTrace();
        throw new ContentProducerException(ae);
    }
    }

    /**
     * Builds a XML String of annoucement data.
     * @param <code>List</code> - list of annoucments
     * @return <code>String</code> - a XML String
     * @exception <{Exception}>
     */
    protected static String toXML(List announcements) throws Exception {
        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<class-announcements>\n");
        if (announcements != null) {
            for (int ix = 0; ix < announcements.size(); ++ix) {
                xmlSB.append(((XMLAbleEntity) announcements.get(ix)).toXML());
            }
        }
        xmlSB.append("</class-announcements>\n");
        return xmlSB.toString();
    }

    /**
     * Builds a DOM document of annoucement data.
     * @param <code>List</code> - list of annoucments
     * @return <code>org.w3c.dom.Document</code> - a dom document
     * @exception <{Exception}>
     * @see <{Document}>
     */
    protected static Document toDocument(List announcements) throws Exception {
        Document doc = new DocumentImpl();
        Element root = doc.createElement("class-announcements");
        if (announcements != null) {
            for (int ix = 0; ix < announcements.size(); ++ix) {
                Node node = ((net.unicon.academus.apps.announcement.Announcement) announcements.get(ix)).toNode(doc);
                root.appendChild(node);
            }
        }
        doc.appendChild(root);
        return doc;
    }


    /**
     * Gets the type of producer
     */
    public ProducerType getType() {
    return ProducerType.XML;
    }

    // Driver program
    /*
    public static void main(String[] args) {
    if (args.length < 2) {
        System.out.println("USAGE: java net.unicon.academus.apps.announcement.AnnouncementProducer <username> <command> [ <command>...]");
        System.exit(-1);
    }

    try {
        AnnouncementProducer producer = new AnnouncementProducer();

        String[] initArgs = {"1", args[0]};
        String appID = producer.initialize(initArgs);
    
        int index = 1;
        HashMap inputParams = new HashMap();
        Map outParams = null;
        String key = null;
        String val = null;

        String[] cmd = new String[1];

        String[] appIDarr = new String[1];
        appIDarr[0] = appID;

        inputParams.put(IProducer.APP_ID, appIDarr);
        inputParams.put(IProducer.COMMAND, cmd);

        while (index < args.length) {
        cmd[0] = args[index++];

        // call getContent
        outParams = producer.getContent(inputParams);

        // display the output params:
        System.out.println("------------------");
        System.out.println("APPLICATION OUTPUT");
        System.out.println("------------------");
        System.out.println("Application ID:\t" + appIDarr[0]);
        System.out.println("Command:\t\t" + cmd[0]);
        for (Iterator it = outParams.keySet().iterator(); it.hasNext();) {
            key = (String)it.next();
            val = (String)outParams.get(key);
            System.out.println("\t" + key + "=" + val);
        }
        }
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println("Exiting...");
    System.exit(0);
    }    
    */
    
    protected void addResultMapping(Map m, String name, Object value) {
        if (name != null && value != null) {
            m.put(name, value);
        }
    }
}
