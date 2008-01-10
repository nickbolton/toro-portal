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

package net.unicon.academus.apps.useradmin;

import net.unicon.sdk.properties.UniconProperties;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.academus.apps.ApplicationDataManager;
import net.unicon.academus.commands.*;
import net.unicon.academus.common.AcademusException;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.common.properties.*;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.producer.ContentProducerException;
import net.unicon.academus.producer.IProducer;
import net.unicon.academus.producer.ProducerType;
import net.unicon.portal.permissions.Activity;
import net.unicon.sdk.util.ExceptionUtils;


import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Method;

import org.w3c.dom.*;

public final class UserAdminProducer implements IProducer {

    // value: String (username affected by last command)

    private static final String CLASS_NAME = UserAdminApp.class.getName();

    // use a set as we will never reference the same app more than once
    private Map __appIDMap = null;

    // A Producer is a Singleton
    private static UserAdminProducer __producer;

    // Singleton method
    public synchronized static IProducer getInstance() {
        if (__producer == null) {
            __producer = new UserAdminProducer();
        }
        return __producer;
    }

    /**
     * All producers must have a default constructor
     **/
    private UserAdminProducer() {
        __appIDMap = new HashMap();
    }

    /**
     * allocateAppConnection sets aside application resources (if needed) because a client
     * expects to use the app through this producer. It isn't actually required that the
     * application be instantiated, but a reference to it must be allocated.
     * @param <code>string[]</code> 0 is upID, 1 is username, 2 is context (if exists yet)
     **/
    public String allocateAppConnection(String[] initInfo)
    throws ContentProducerException {
        String id = initInfo[0];
        String username = initInfo[1];
        String contextId = null;

        if (initInfo.length == 3) {
            contextId = initInfo[2];
        }
        String appId = ApplicationDataManager.generateAppId(
            id, username, contextId, CLASS_NAME);

        // store the appId, even though the app object doesn't yet exist
        __appIDMap.put(appId, initInfo);
        return appId;
    }

    /**
     * Deallocate means to disconnect this client from this application via this
     * Producer, presumably because the client is being shutdown.
     * @param appId of the application to disconnect from
     **/
    public void deallocateAppConnection(String appId)
    throws ContentProducerException {
        try {
            if (__appIDMap != null && __appIDMap.remove(appId) != null) {
                ApplicationDataManager.removeApplication(appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ContentProducerException(e);
        }
    }

    /**
     * Return the set of connection applictions to this producer
     * @return Iterator application IDs
     **/
    public Set getConnectedApplicationIDs() {
        return __appIDMap.keySet();
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
                UserAdminApp app =
                    (UserAdminApp)ApplicationDataManager.
                        getApplication(appIDs[0]);
                return app.getActivities();
            }
            return new Activity[0];
        } catch (Exception e) {
e.printStackTrace();
            throw new ContentProducerException(e);
        }
    }

    /**
     * Passes back content through the return Map.
     */
    public Map getContent(Map appData) throws ContentProducerException {
    /* DEBUG ONLY
        //net.unicon.common.util.debug.DebugUtil.spillParameters(appData,
            "Starting UserAdminProducer.getContent()");
    */

        UserAdminApp app = __setupApp(appData);

        AcademusAppCommand command = __setupCommand(appData);


        // app is a reference to the application object. Get its state
        // and marshal it into a Map
        Map rval = new HashMap();

        try {
            // invoke the Command
            rval = command.invokeOnApp(app.getApplicationID(), appData);

        /* DEBUG ONLY
            //net.unicon.common.util.debug.DebugUtil.spillParameters(appData,
               "Starting2 UserAdminProducer.getContent() for " +
               "(appID, username, contextID) (" + app.getApplicationID() + "," +
               app.getUsername() + "," + app.getContextID() + " INPUT:\n");
        */

            // set xml
            setXmlXsl(app, appData, rval);
            rval.put(IProducer.STATUS, IProducer.OK);
            rval.put(IProducer.STATUS_MSG, "");

        /* DEBUG ONLY
            //net.unicon.common.util.debug.DebugUtil.spillParameters(rval,
                "Ending UserAdminProducer.getContent(), OUTPUT:\n");
        */
        } catch (AcademusException ae) {
            ae.printStackTrace();
            rval.put(IProducer.STATUS, IProducer.EXCEPTION);
            rval.put(IProducer.ABORT_SERVANT, new Boolean(true));
            rval.put(IProducer.STATUS_MSG,
                ExceptionUtils.getExceptionMessage(ae));
            rval.put(IProducer.XML, "<user-admin></user-admin>");
        }
        return rval;
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
    private UserAdminApp __setupApp(final Map appData)
    throws ContentProducerException {
        try {
            String appId = ((String[])appData.get(IProducer.APP_ID))[0];
            UserAdminApp app =
                (UserAdminApp)ApplicationDataManager.getApplication(appId);
            if (app == null) {
                String[] initInfo = (String[])__appIDMap.get(appId);
                if (initInfo == null || initInfo.length < 2) {
                    throw new ContentProducerException(
                        "Error initializing application " + appId);
                }
                String id = initInfo[0];
                String username = initInfo[1];
                String contextId = null;
                if (initInfo.length == 3) {
                    contextId = initInfo[2];
                }
                app = (UserAdminApp)ApplicationDataManager.
                    initializeApplication(id, username,
                        contextId, CLASS_NAME, null);
            }
            return app;
        } catch (Exception exc) {
            exc.printStackTrace();
            throw new ContentProducerException(exc);
        }
    }

    private void setXmlXsl(UserAdminApp app, Map appData, Map rval)
    throws ContentProducerException {

        try {
            Map xslParams = new HashMap();
            StringBuffer xml = new StringBuffer(512);
            xml.append("<user-admin>");

            xslParams.put("current_command", app.getCommand());

            addDefaultRuntimeParams(appData, xslParams);

            if (UserAdminAppCommandFactory.ADD_COMMAND.
                equals(app.getCommand())) {
                processAddCommand(app, appData, xml, xslParams);
            } else if (UserAdminAppCommandFactory.EDIT_COMMAND.
                       equals(app.getCommand())) {
                processEditCommand(app, appData, xml, xslParams);
                String selectedUsername = getParameter(appData, "user_name");
                xml.append(UserFactory.getUser(selectedUsername).toXML());
            } else if (UserAdminAppCommandFactory.INSERT_COMMAND.
                       equals(app.getCommand())) {
                processInsertCommand(app, appData, xml, xslParams);
            } else if (UserAdminAppCommandFactory.CHANGE_PASSWORD_COMMAND.
                       equals(app.getCommand())) {
                processChangeUserCommands(app, appData, xml, xslParams);
            } else if (UserAdminAppCommandFactory.CHANGE_EMAIL_COMMAND.
                       equals(app.getCommand())) {
                processChangeUserCommands(app, appData, xml, xslParams);
            }

            Iterator itr = app.getUsers().iterator();

            // We are not going to iterate through the catalog
            // on an edit.  The edit only returns one person with
            // additional information in the XML.  To avoid a join
            // and additional unneeded information we only get this
            // additional information for a user when specifically
            // request to do so - H2
            while (itr.hasNext() &&
                !UserAdminAppCommandFactory.EDIT_COMMAND.equals(app.getCommand())) {
                xml.append(itr.next().toString());
            }

            xml.append("</user-admin>");

            // Important! --> must call for pg count after getUsers().
            xslParams.put("catLastPage", String.valueOf(app.getPageCount()));

            rval.put(IProducer.XML, xml.toString());
            rval.putAll(xslParams);
        } catch (Exception e) {
            throw new ContentProducerException(e);
        }
    }

    private void addDefaultRuntimeParams(Map appData, Map xslParams) {
        addDefaultParam(appData, xslParams, "user_name");
        addDefaultParam(appData, xslParams, "first_name");
        addDefaultParam(appData, xslParams, "last_name");
        addDefaultParam(appData, xslParams, "email");
    }

    private void addDefaultParam(Map appData, Map xslParams, String key) {
        String value = getParameter(appData, key);
        if (value != null && !"".equals(value.trim())) {
            xslParams.put(key, value);
        }
    }

    private void processAddCommand(UserAdminApp app, Map appData,
        StringBuffer xml, Map xslParams) {
        xslParams.put("in_use", "false");
    }

    private void processEditCommand(UserAdminApp app, Map appData,
        StringBuffer xml, Map xslParams) {
        xslParams.put("in_use", "true");
    }

    private void processInsertCommand(UserAdminApp app, Map appData,
        StringBuffer xml, Map xslParams) {
        if (UserAdminMsgType.USER_EXISTS.equals(app.getMsg())) {
            // Error Handling
            // Calling the add with the prepopulated data but
            // telling the user that username has already in use
            xslParams.put("current_command",
                UserAdminAppCommandFactory.ADD_COMMAND);
            xslParams.put("in_use", "true");
        }
    }

    private void processChangeUserCommands(UserAdminApp app, Map appData,
        StringBuffer xml, Map xslParams) {

        User user = app.getCurrentUser();

        xslParams.put("first_name", user.getFirstName());
        xslParams.put("last_name",  user.getLastName());
        xslParams.put("email",      user.getEmail());
        xslParams.put("user_name",   user.getUsername());        
    }

    private String getParameter(Map params, String key) {
        if (params.get(key) instanceof String[]) {
            return ((String[])params.get(key))[0];
        } else if (params.get(key) instanceof String) {
            return (String)params.get(key);
        }
        return "";
    }

    /**
     * Builds a XML String of user data.
     * @param <code>List</code> - list of users
     * @return <code>String</code> - a XML String
     * @exception <{Exception}>
     */
    protected static String toXML(List users) throws Exception {
        StringBuffer xmlSB = new StringBuffer();
        xmlSB.append("<user-admin>\n");
        if (users != null) {
            for (int ix = 0; ix < users.size(); ++ix) {
                xmlSB.append(((XMLAbleEntity)users.get(ix)).toXML());
            }
        }
        xmlSB.append("</user-admin>\n");
        return xmlSB.toString();
    }

    /**
     * Builds a DOM document of user data.
     * @param <code>List</code> - list of users
     * @return <code>org.w3c.dom.Document</code> - a dom document
     * @exception <{Exception}>
     * @see <{Document}>
     */
    protected static Document toDocument(List users) throws Exception {
        throw new Exception("toDocument Not implemented!");
    }

    /**
     * Gets the type of producer
     */
    public ProducerType getType() {
        return ProducerType.XML;
    }
}
