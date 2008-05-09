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
package net.unicon.portal.channels.topicadmin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.ItemNotFoundException;
import net.unicon.academus.domain.lms.EnrollmentModel;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.OperationFailedException;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.sor.SorViolationException;
import net.unicon.academus.service.calendar.CalendarServiceFactory;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.domain.ChannelMode;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.servants.IResultsAdapter;
import net.unicon.portal.servants.ServantManager;
import net.unicon.portal.servants.ServantResults;
import net.unicon.portal.servants.ServantType;
import net.unicon.portal.util.db.FDbDataSource;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbPageMode;
import net.unicon.sdk.catalog.db.FDbSortMode;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.util.ExceptionUtils;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;

public class TopicAdminChannel extends BaseSubChannel {

    protected static Catalog baseCatalog = null;

    // Commands
    protected static final String mainCommand          = "main";
    protected static final String viewCommand          = "view";
    protected static final String cancelCommand        = "cancel";
    protected static final String selectParentCommand  = "selectParent";
    protected static final String addCommand           = "add";
    protected static final String subsequentAddCommand = "subsequentAdd";
    protected static final String editCommand          = "edit";
    protected static final String subsequentEditCommand = "subsequentEdit";
    protected static final String deleteCommand        = "delete";
    protected static final String confirmDeleteCommand = "confirmDelete";
    protected static final String addSubmitCommand     = "addSubmit";
    protected static final String editSubmitCommand    = "editSubmit";
    protected static final String searchCommand        = "search";
    protected static final String searchResultsCommand = "searchResults";
    protected static final String restartCommand       = "scRestart";

    // Parameters
    protected static final String topicNameParam          = "topicName";
    protected static final String topicDescParam          = "topicDescription";
    protected static final String deleteConfirmationParam =
        "deleteConfirmation";
    protected static final String savedIdParam = "savedID";
    protected static final String savedNameParam = "savedName";
    protected static final String savedDescParam = "savedDesc";
    protected static final String savedParentGroupIdParam =
        "savedParentGroupId";
    protected static final String savedAdjunctDataParam = "savedAdjunct";
    protected static final String parentGroupIdParam = "parentGroupId";

    private static IResultsAdapter resultsAdapter = null;

    private static final String NAVIGATION_CHANNEL = "NavigationChannel";
    private static final String OFFERING_ADMIN_CHANNEL = "OfferingAdminChannel";

    static {
        resultsAdapter = new IResultsAdapter() {
            public Object[] adapt(Object[] results)
            throws PortalException {
                if (results == null) {
                    return new IGroup[0];
                }
                try {
                    Object[] adaptedResults = new Object[results.length];
                    for (int i=0; i<adaptedResults.length; i++) {
                        adaptedResults[i] = GroupFactory.getGroup(
                            ((IdentityData)results[i]).getID());
                    }
                    return adaptedResults;
                } catch (Exception e) {
                    throw new PortalException(e);
                }
            }
        };
    }

    protected static final String addParentGroupIdKey = "addParentGroupId";

    public TopicAdminChannel() {
        super();
    }

    public void buildXML(String upId) throws Exception {

/*
// spill parameters (uncomment to see)...
ChannelRuntimeData params = getRuntimeData(upId);
System.out.println("#### SPILLING PARAMETERS...");
Iterator it = params.keySet().iterator();
while (it.hasNext()) {
    String key = (String) it.next();
    String value = params.getParameter(key);
    System.out.println("\t"+key.toString()+"="+value);
}
*/

        // BEWARE!!! Incorporation of groups servant for selecting
        // parent group in add and edit commands a TOTAL HACK!!!
        // Have a nice day.  :)

        try {
            StringWriter sw = new StringWriter();
            PrintWriter servantOut = new PrintWriter(sw);
            String command = manageGroupServantCommunication(upId, servantOut);

            setServantContent(upId, sw.toString());

            setupParameters(upId);
            String xmlBody = performCommand(upId, command);
            StringBuffer xmlSB = new StringBuffer();
            xmlSB.append("<topicAdmin>");
            xmlSB.append(xmlBody);
            xmlSB.append("</topicAdmin>");
            setXML(upId, xmlSB.toString());
/*
System.out.println("");
System.out.println(xmlSB.toString());
System.out.println("");
*/
        } catch (Exception e) {
            e.printStackTrace();
            cleanSavedParams(upId);
            throw e;
        }
    }

    private String manageGroupServantCommunication(String upId, PrintWriter out)
    throws Exception {
        String command = getRuntimeData(upId).getParameter("command");
        String nextCommand =getRuntimeData(upId).getParameter("next_command");

        if (nextCommand != null) {
            putChannelAttribute(upId, "nextCommand", nextCommand);
        }
        String servantCommand =
            getRuntimeData(upId).getParameter("servant_command");

        if ("cancel".equals(servantCommand)) {
            // clear out a previous running permissions servant
            ServantManager.removeServant(upId, ServantType.IDENTITY_SELECTOR);

            // clean up the saved params from any privious edit command
            cleanSavedParams(upId);
        }

        // first look for a running groups manager servant
        if (ServantManager.hasServant(upId ,ServantType.IDENTITY_SELECTOR)) {
            getRuntimeData(upId).put("sources", "groupsOnly");
            getRuntimeData(upId).put("singleSelection", "true");
            ServantResults results = ServantManager.renderServant(upId, out,
                getStaticData(upId), getRuntimeData(upId), ServantType.IDENTITY_SELECTOR);

            if (!ServantManager.hasServant(upId,
                ServantType.IDENTITY_SELECTOR)) {

                // the servant is finished, get the results

                if (results == null || results.getResults() == null ||
                    results.getResults().length == 0) {
                    String storedNextCommand =
                        (String)getChannelAttribute(upId, "nextCommand");
                    if ("Cancel".equals(
                        getRuntimeData(upId).getParameter("do=cancel"))) {
                        // user cancelled out of the groups manager
                        cleanSavedParams(upId);
                        command = cancelCommand;
                    } else {
                        // User selected submit from groups manager
                        // but didn't have a group selected.
                        // set the parent group to the current one
                        command = storedNextCommand;
                    }
                } else {
                    command = (String)getChannelAttribute(upId, "nextCommand");
                    // can only select one group, so only save the
                    // first element.
                    putChannelAttribute(upId, savedParentGroupIdParam,
                        ""+((IGroup)results.getResults()[0]).getGroupId());
                }
            } else {
                // servant running, default to the main
                // command to get the nav bar
                command = null;
            }
        }

        if (selectParentCommand.equals(command)) {
            // save the edit info if we're editing the parent group
            saveRuntimeParam(upId, "ID", savedIdParam);
            saveRuntimeParam(upId, topicNameParam, savedNameParam);
            saveRuntimeParam(upId, topicDescParam, savedDescParam);
            saveRuntimeParam(upId, parentGroupIdParam, savedParentGroupIdParam);

            // Save all the adjunct(s).
            IDecisionCollection[] dcs = evaluateAdjunctData(getRuntimeData(upId));
            putChannelAttribute(upId, savedAdjunctDataParam, dcs);


            // set the command to the default so the nav bar will display
            command = null;
            // NB:  We also need to remove the name search to avoid scrambling
            // the servant and the search results together.  This is not an
            // ideal fix for this problem -- this channel (and most channels)
            // really need to be redesigned!
            getRuntimeData(upId).remove("searchTopicName");

            // render groups selection servant
            ServantManager.createIdentitySelectorServant(upId,
                getStaticData(upId), null, null, resultsAdapter);

            // this shouldn't return any results on the first
            // invocation
            getRuntimeData(upId).put("sources", "groupsOnly");
            getRuntimeData(upId).put("singleSelection", "true");
            ServantManager.renderServant(upId, out, getStaticData(upId),
                getRuntimeData(upId), ServantType.IDENTITY_SELECTOR);
        }

        return command;
    }

    private void saveRuntimeParam(String upId, String name, String storedName) {
        String value = getRuntimeData(upId).getParameter(name);
        if (value == null || "".equals(value)) return;
        putChannelAttribute(upId, storedName, value);
    }

    private void cleanSavedParams(String upId) {
        putChannelAttribute(upId, savedIdParam, null);
        putChannelAttribute(upId, savedNameParam, null);
        putChannelAttribute(upId, savedDescParam, null);
        putChannelAttribute(upId, savedParentGroupIdParam, null);
        putChannelAttribute(upId, savedAdjunctDataParam, null);
        putChannelAttribute(upId, "nextCommand", null);
    }

    protected String performCommand(String upId, String command)
    throws Exception {

        String xmlBody = "";
        setSheetName(upId, command);
        ChannelRuntimeData runtimeData = getRuntimeData(upId);

        String searchTopicName = runtimeData.getParameter("searchTopicName");
        if (searchTopicName == null) {
            searchTopicName = "";
        }

/* -->  uncomment to debug this channel...
// Spill parameters.
System.out.println("*****> Spilling Parameters...");
Enumeration keys = runtimeData.getParameterNames();
while (keys.hasMoreElements()) {
    String key = null;
    try {
        key = (String) keys.nextElement();
        Object value = runtimeData.getParameter(key);
        String rslt = null;
        if (value instanceof String) {
            rslt = (String) value;
        } else {
            rslt = "[" + value.getClass().getName() + "]";
        }
        System.out.println("\t" + key + "=" + rslt);
    } catch (Throwable t) {
        System.out.println("\tEXCEPTION..." + key);
    }
}
*/

        Map xslParameters = getXSLParameters(upId);
        xslParameters.put("catPageSize", "" + evaluatePageSize(upId));
        xslParameters.put("searchTopicName", searchTopicName);
        xslParameters.put("catCurrentCommand", searchResultsCommand);
        xslParameters.put("pageCommand", searchResultsCommand);

        if (addCommand.equals(command)) {
            cleanSavedParams(upId);
            xmlBody = addCommand(upId);
        } else if (subsequentAddCommand.equals(command)) {
            xmlBody = addCommand(upId);
        } else if (searchCommand.equals(command)) {
            cleanSavedParams(upId);
            xmlBody = searchCommand(upId);
        } else if (searchResultsCommand.equals(command) &&
                   searchTopicName != null) {
            xmlBody = searchResultsCommand(upId);
        } else if (addSubmitCommand.equals(command)) {
            xmlBody = addSubmitCommand(upId);
            cleanSavedParams(upId);
            if (! "".equals(xmlBody)) return xmlBody;
        } else if (editSubmitCommand.equals(command)) {
            xmlBody = editSubmitCommand(upId);
            cleanSavedParams(upId);
            if (! "".equals(xmlBody)) return xmlBody;
        } else if (confirmDeleteCommand.equals(command)) {
            xmlBody = confirmDeleteCommand(upId);
            if (! "".equals(xmlBody)) return xmlBody;
        } else if (editCommand.equals(command)) {
            cleanSavedParams(upId);
            xmlBody = singularActionCommand(upId);
        } else if (subsequentEditCommand.equals(command)) {
            xmlBody = singularActionCommand(upId);
        } else if (viewCommand.equals(command)) {
            xmlBody = singularActionCommand(upId);
        } else if (deleteCommand.equals(command)) {
            xmlBody = deleteCommand(upId);
        } else if (restartCommand.equals(command)) {
            cleanSavedParams(upId);
        } else if (cancelCommand.equals(command)) {
            cleanSavedParams(upId);
        }

        if (xmlBody == null || xmlBody.equals("")) {
            if (searchTopicName == null ||
                searchTopicName.trim().length() == 0) {

                // default goes to an empty sheet, prompting user
                // to search for topics
                xmlBody = "";
                setSheetName(upId, mainCommand);
                xslParameters.put("catCurrentPage", "0");
                xslParameters.put("catLastPage", "0");
                xslParameters.put("catCurrentCommand", mainCommand);
            } else {
                xmlBody = searchResultsCommand(upId);
            }
        }
        return xmlBody;
    }

    protected String toXML(String upId, List topics)
    throws Exception {

        StringBuffer xmlSB = new StringBuffer();

        for (int ix = 0; topics != null && ix < topics.size(); ++ix) {

            Topic topic = (Topic)topics.get(ix);

            xmlSB.append("<topic id=\"" + topic.getId() + "\">\n");
            xmlSB.append("    <topicType>" + topic.getTopicType() +
                            "</topicType>\n");
            xmlSB.append("    <name>")
				.append("<![CDATA[")
				.append(topic.getName())
				.append("]]>")
				.append("</name>\n");
            xmlSB.append("    <description>")
				.append("<![CDATA[")
				.append(topic.getDescription())
				.append("]]>")
				.append("</description>\n");
            xmlSB.append("    <parentGroup id=\"");
            xmlSB.append(topic.getParentGroup().getGroupId());
            xmlSB.append("\">");

            String[] paths = topic.getParentGroup().getPathsAsStrings(" -> ", false);

            for (int index = 0; index < paths.length; index++) {
                xmlSB.append("<path>");
                xmlSB.append("<![CDATA[");
                xmlSB.append(paths[index]);
                xmlSB.append("]]>");
                xmlSB.append("</path>");
            }

            xmlSB.append("</parentGroup>\n");

            // Include the adjunct data.
            AdjunctTopicData atd = AdjunctTopicData.getInstance();
            Iterator it = Arrays.asList(atd.getData()).iterator();
            while (it.hasNext()) {
                IChoiceCollection c = (IChoiceCollection) it.next();
                xmlSB.append("<adjunct>");
                xmlSB.append(c.toXml());
                IDecisionCollection d = atd.getAgent(c).getDecisions(topic);
                xmlSB.append(d.toXml());
                xmlSB.append("</adjunct>");
            }

            xmlSB.append("</topic>");
        }

        return xmlSB.toString();

    }

    protected void setupParameters(String upId) {
        ChannelDataManager.setSSLLocation(upId,
            ChannelDataManager.getChannelClass(upId).getSSLLocation());
        Hashtable topicAdminParams = getXSLParameters(upId);

        // Add the stylesheet parameters
        topicAdminParams.put("viewCommand",          viewCommand);
        topicAdminParams.put("cancelCommand",        cancelCommand);
        topicAdminParams.put("addCommand",           addCommand);
        topicAdminParams.put("subsequentAddCommand", subsequentAddCommand);
        topicAdminParams.put("selectParentCommand",  selectParentCommand);
        topicAdminParams.put("editCommand",          editCommand);
        topicAdminParams.put("subsequentEditCommand", subsequentEditCommand);
        topicAdminParams.put("deleteCommand",        deleteCommand);
        topicAdminParams.put("searchCommand",        searchCommand);
        topicAdminParams.put("searchResultsCommand", searchResultsCommand);
        topicAdminParams.put("confirmDeleteCommand", confirmDeleteCommand);
        topicAdminParams.put("addSubmitCommand",     addSubmitCommand);
        topicAdminParams.put("editSubmitCommand",    editSubmitCommand);
        topicAdminParams.put("topicNameParam",       topicNameParam);
        topicAdminParams.put("topicDescParam",       topicDescParam);
        topicAdminParams.put("deleteConfirmationParam",
            deleteConfirmationParam);
    }

    protected String singularActionCommand(String upId)
    throws Exception {

        putChannelAttribute(upId, "nextCommand", null);
        List topics = new ArrayList();

        if (getChannelAttribute(upId, savedIdParam) != null) {

            // retrieve the saved entries .. parent group will come
            // from runtimedata.
            String id = (String)getChannelAttribute(upId, savedIdParam);
            Topic topic = TopicFactory.getTopic(Long.parseLong(id));
            String name = (String)getChannelAttribute(upId, savedNameParam);
            String desc = (String)getChannelAttribute(upId, savedDescParam);
            String parentId = (String)getChannelAttribute(upId,
                savedParentGroupIdParam);
            IGroup group = GroupFactory.getGroup(Long.parseLong(parentId));

            // clear the saved entries
            cleanSavedParams(upId);

            StringBuffer xmlSB = new StringBuffer();

            xmlSB.append("<topic id=\"").append(id).append("\">\n");
            xmlSB.append("    <topicType>").append(topic.getTopicType());
            xmlSB.append("</topicType>\n");
            xmlSB.append("    <name>").append("<![CDATA[")
			.append(name).append("]]>").append("</name>\n");
            xmlSB.append("    <description>").append("<![CDATA[");
            xmlSB.append(desc).append("]]>").append("</description>\n");
            xmlSB.append("    <parentGroup id=\"");
            xmlSB.append(parentId);
            xmlSB.append("\">");

            String[] paths = group.getPathsAsStrings(" -> ", false);

            for (int index = 0; index < paths.length; index++) {
                xmlSB.append("<path>");
                xmlSB.append("<![CDATA[");
                xmlSB.append(paths[index]);
                xmlSB.append("]]>");
                xmlSB.append("</path>");
            }

            xmlSB.append("</parentGroup>\n");
            xmlSB.append("</topic>");
            
            return xmlSB.toString();
        } else {
            topics.add(getTopicFromParam(upId));
            return toXML(upId, topics);
        }
    }

    protected Topic getTopicFromParam(String upId) throws Exception {
        String topicIDString = getRuntimeData(upId).getParameter("ID");
        Topic topic = null;

        if (topicIDString != null) {
            long topicID = Long.parseLong(topicIDString);
            topic = TopicFactory.getTopic(topicID);
        }
        return topic;
    }

    /*
     * XXX These roles should be retrieved from a XML file
     * or some other source such as the db.  Hard coding
     * these defeats the purpose of having an configurable
     * external file -H2 (12/31/02)
     */

    protected String getDefaultRolesXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<role default=\"false\">Sponsor</role>\n");
        sb.append("<role default=\"false\">Assistant</role>\n");
        sb.append("<role default=\"true\">Member</role>\n");
        sb.append("<role default=\"false\">Observer</role>\n");
        return sb.toString();
    }

    protected String addCommand(String upId) throws Exception {
        putChannelAttribute(upId, "nextCommand", null);
        StringBuffer xmlSB = new StringBuffer();
        List channelList = ChannelClassFactory.getChannelClasses(
        ChannelMode.OFFERING);

        String name = "";
        String desc = "";
        IGroup parentGroup = null;
        Map decisions = null;

        if (getChannelAttribute(upId, savedNameParam) != null) {
            name = (String)getChannelAttribute(upId, savedNameParam);
        }
        if (getChannelAttribute(upId, savedDescParam) != null) {
            desc = (String)getChannelAttribute(upId, savedDescParam);
        }
        if (getChannelAttribute(upId, savedParentGroupIdParam) != null) {
            String groupId =
                (String)getChannelAttribute(upId, savedParentGroupIdParam);
            parentGroup = GroupFactory.getGroup(Long.parseLong(groupId));
        }
        if (getChannelAttribute(upId, savedAdjunctDataParam) != null) {
            IDecisionCollection[] dcs = (IDecisionCollection[]) getChannelAttribute(upId, savedAdjunctDataParam);
            decisions = new HashMap();
            Iterator dd = Arrays.asList(dcs).iterator();
            while (dd.hasNext()) {
                IDecisionCollection dCol = (IDecisionCollection) dd.next();
                decisions.put(dCol.getChoiceCollection(), dCol);
            }
        }

        xmlSB.append("<topic id=\"\">\n");
        xmlSB.append("    <name>").append("<![CDATA[").append(name)
				.append("]]>").append("</name>\n");
        xmlSB.append("    <description>").append("<![CDATA[")
				.append(desc).append("]]>");
        xmlSB.append("</description>\n");
        if (parentGroup != null) {
            xmlSB.append("    <parentGroup id=\"");
            xmlSB.append(parentGroup.getGroupId());
            xmlSB.append("\">");

            String[] paths = parentGroup.getPathsAsStrings(" -> ", false);

            for (int index = 0; index < paths.length; index++) {
                xmlSB.append("<path>");
                xmlSB.append("<![CDATA[");
                xmlSB.append(paths[index]);
                xmlSB.append("]]>");
                xmlSB.append("</path>");
            }

            xmlSB.append("</parentGroup>\n");
        }

        // Include the adjunct data.
        AdjunctTopicData atd = AdjunctTopicData.getInstance();
        Iterator it = Arrays.asList(atd.getData()).iterator();
        while (it.hasNext()) {
            IChoiceCollection c = (IChoiceCollection) it.next();
            xmlSB.append("<adjunct>");
            xmlSB.append(c.toXml());
            if (decisions != null) {
                IDecisionCollection dCol = (IDecisionCollection) decisions.get(c);
                if (c != null) {
                    xmlSB.append(dCol.toXml());
                }
            }
            xmlSB.append("</adjunct>");
        }

        xmlSB.append("</topic>");
        xmlSB.append("    <enrollmentModel default=\"true\">" +
        EnrollmentModel.FACILITATOR + "</enrollmentModel>\n");
        xmlSB.append(getDefaultRolesXML());
        Iterator itr = channelList.iterator();

        while (itr.hasNext()) {
            ChannelClass channel = (ChannelClass)itr.next();
            xmlSB.append("    <channel id=\"" + channel.getHandle() +
                "\">" + channel.getLabel() + "</channel>\n");
        }
        
        return xmlSB.toString();
    }

    protected String deleteCommand(String upId) throws Exception {
        long id = Long.parseLong(getRuntimeData(upId).getParameter("ID"));
        Topic topic = TopicFactory.getTopic(id);

        // find out if this or any child topic has offerings
        Topic offendingTopic = offeringExistsUnderTopic(topic);

        if (offendingTopic != null) {
            StringBuffer msg = new StringBuffer();
            if (offendingTopic.getId() == id) {
                msg.append("The topic \"" + offendingTopic.getName() + "\" ");
                msg.append("currently has offerings associated with it.");
            } else {
                msg.append("The child topic \""+offendingTopic.getName()+"\" ");
                msg.append("currently has offerings associated with it.");
            }
            setErrorMsg(upId, msg.toString());
            return "error";
        }
        return singularActionCommand(upId);
    }

    protected Topic offeringExistsUnderTopic(Topic topic)
    throws Exception {
        // find out if this or any child topic has offerings
        List topicList = new ArrayList();
        topicList.add(topic);
        topicList.addAll(TopicFactory.getAllTopics(topic.getGroup()));

        Iterator itr = topicList.iterator();

        Topic offendingTopic = null;
        while (itr.hasNext()) {
            // reusing topic var
            topic = (Topic)itr.next();
            if (OfferingFactory.getOfferings(topic).size() > 0) {
                offendingTopic = topic;
                break;
            }
        }
        return offendingTopic;
    }

    protected String confirmDeleteCommand(String upId) throws Exception {
        String param =
            getRuntimeData(upId).getParameter(deleteConfirmationParam);

        if (!"yes".equals(param)) return "";

        long id = Long.parseLong(getRuntimeData(upId).getParameter("ID"));
        Topic topic = TopicFactory.getTopic(id);

        // Don't allow the delete if this topic has child topics.
        List subTopics = TopicFactory.getAllTopics(topic.getGroup());
        if (subTopics.size() > 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Unable to delete:  topic '" + topic.getName() + "' contains ");
            msg.append(Integer.toString(subTopics.size()));
            msg.append(" child topics.");
            setErrorMsg(upId, msg.toString());
            return "error";
        }

        // Don't allow the delete if this topic contains offerings.
        int offeringCount = OfferingFactory.getOfferings(topic).size();
        if (offeringCount > 0) {
            StringBuffer msg = new StringBuffer();
            msg.append("Unable to delete:  topic '" + topic.getName() + "' contains ");
            msg.append(Integer.toString(offeringCount));
            msg.append(" offerings.");
            setErrorMsg(upId, msg.toString());
            return "error";
        }

        // Attempt the delete.
        try {
            TopicFactory.deleteTopic(topic.getId());
        } catch (SorViolationException sve) {
            StringBuffer msg = new StringBuffer();
            msg.append("Unable to delete:  ");
            msg.append(sve.getMessage());
            setErrorMsg(upId, msg.toString());
            return "error";
        }

        // mark the offering admin channel dirty
        broadcastDirtyChannel(OFFERING_ADMIN_CHANNEL);
        return "";

    }

    protected String editSubmitCommand(String upId) throws Exception {

        long id = -1;
        String idStr = getRuntimeData(upId).getParameter("ID");
        String name = getRuntimeData(upId).getParameter(topicNameParam);
        String desc = getRuntimeData(upId).getParameter(topicDescParam);
        long parentGroupId = new Long(getRuntimeData(upId).
            getParameter(parentGroupIdParam)).longValue();

        Topic topic = null;
        try {
            id = Long.parseLong(idStr);
            topic = TopicFactory.getTopic(id);

            // Check if the parent group changed
            if (parentGroupId != topic.getParentGroup().getGroupId()) {

                IGroup topicGroup = topic.getGroup();

                if (parentGroupId == topicGroup.getGroupId())
                    throw new IllegalArgumentException("Topic cannot be its own parent group.");

                IGroup newParentGroup = GroupFactory.getGroup(parentGroupId);
                List newParentGroupParents = newParentGroup.getParents();
                if (newParentGroupParents != null) {
                    boolean found = false;
                    for (Iterator it = newParentGroupParents.iterator(); !found && it.hasNext();) {
                        IGroup p = (IGroup)it.next();
                        if (p.getKey().equals(topicGroup.getKey()))
                            found = true;
                    }

                    if (found)
                        throw new IllegalArgumentException("Topic cannot be placed in a child group.");
                }

                topic.setParentGroup(GroupFactory.getGroup(parentGroupId));
            }
            topic.setName(name);
            topic.setDescription(desc);
            TopicFactory.persist(topic);

            // set the action user's navigation channel dirty
            broadcastUserDirtyChannel(getDomainUser(upId), NAVIGATION_CHANNEL);

            // mark the offering admin channel dirty
            broadcastDirtyChannel(OFFERING_ADMIN_CHANNEL);

            // broadcast navigation dirty for all users enrolled in offering
            // whose parent topic is the modified topic
            // And update the offering calendars
            Offering off;
            Iterator itr = OfferingFactory.getOfferings(topic).iterator();
            while (itr.hasNext()) {
                off = (Offering)itr.next();
                CalendarServiceFactory.getService().updateCalendar(off);
                broadcastUserOfferingDirtyChannel(off, NAVIGATION_CHANNEL,
                    false);
            }
        } catch (NumberFormatException e) {
            String msg = "Failed to edit topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (IllegalArgumentException e) {
            String msg = "Failed to edit topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (OperationFailedException e) {
            String msg = "Failed to edit topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (ItemNotFoundException e) {
            String msg = "Failed to edit topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (SorViolationException e) {
            String msg = "Failed to edit topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } finally {
            cleanSavedParams(upId);
        }

        // Save the adjunct data.
        saveAdjunctData(topic, getRuntimeData(upId));

        return "";
    }

    protected String searchCommand(String upId) throws Exception {
        setSheetName(upId, searchCommand);
        String searchTopicName =
            getRuntimeData(upId).getParameter("searchTopicName");
        int pageSize = evaluatePageSize(upId);
        int currentPageNumber = evaluateCurrentPage(upId);
        IPageMode pageMode = new FDbPageMode(pageSize, currentPageNumber);
        Catalog catalog = createCatalog(searchTopicName, pageMode);
        List elements = catalog.elements();

        Map xslParameters = getXSLParameters(upId);
        xslParameters.put("catPageSize", "" + pageSize);
        xslParameters.put("catCurrentPage", "" + currentPageNumber);
        xslParameters.put("catLastPage", "" + pageMode.getPageCount());
        xslParameters.put("catCurrentCommand", searchCommand);
        xslParameters.put("searchTopicName", searchTopicName);
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < elements.size(); i++) {
            buffer.append(elements.get(i));
        }
        return buffer.toString();
    }

    protected String searchResultsCommand(String upId) throws Exception {
        setSheetName(upId, searchResultsCommand);
        String searchTopicName =
            getRuntimeData(upId).getParameter("searchTopicName");
        int pageSize = evaluatePageSize(upId);
        int currentPageNumber = evaluateCurrentPage(upId);
        IPageMode pageMode = new FDbPageMode(pageSize, currentPageNumber);
        Catalog catalog = createCatalog(searchTopicName, pageMode);
        List elements = catalog.elements();

        Map xslParameters = getXSLParameters(upId);
        xslParameters.put("catPageSize", "" + pageSize);
        xslParameters.put("catCurrentPage", "" + currentPageNumber);
        xslParameters.put("catLastPage", "" + pageMode.getPageCount());
        xslParameters.put("catCurrentCommand", searchResultsCommand);
        xslParameters.put("searchTopicName", searchTopicName);
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < elements.size(); i++) {
            buffer.append(elements.get(i));
        }
        return buffer.toString();
    }

    protected String addSubmitCommand(String upId) throws Exception {
        String name = getRuntimeData(upId).getParameter(topicNameParam);
        String desc = getRuntimeData(upId).getParameter(topicDescParam);
        IGroup parentGroup;
        long parentGroupId = new Long(getRuntimeData(upId).
            getParameter(parentGroupIdParam)).longValue();

        Topic topic = null;
        try {
            parentGroup = GroupFactory.getGroup(parentGroupId);

            // make sure the parent object is ok
            if (!parentGroup.ok()) {
                String msg = "Failed to add topic due to invalid parent group";
                setErrorMsg(upId, msg);
                return "";
            }

            topic = TopicFactory.createTopic(name,
                desc,
                TopicType.ACADEMICS,
                parentGroup,
                getDomainUser(upId));

            // mark the offering admin channel dirty
            broadcastDirtyChannel(OFFERING_ADMIN_CHANNEL);
        } catch (FactoryCreateException e) {
            String msg = "Failed to add topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (IllegalArgumentException e) {
            String msg = "Failed to add topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } catch (OperationFailedException e) {
            String msg = "Failed to add topic: " +
                ExceptionUtils.getExceptionMessage(e);
            setErrorMsg(upId, msg);
        } finally {
            cleanSavedParams(upId);
        }

        // Save the adjunct data.
        saveAdjunctData(topic, getRuntimeData(upId));

        return "";
    }

    protected Catalog createCatalog(String topicNamePrefix, IPageMode pageMode)
    throws Exception {
        List sortModes = new ArrayList(3);
        sortModes.add(new FDbSortMode("UPPER(NAME) ASC"));
        List filterModes = new ArrayList(3);
        Object[] typeFilterParameters = { TopicType.ACADEMICS.toString() };
        filterModes.add(new FDbFilterMode("TYPE = ?", typeFilterParameters));
        Object[] nameFilterParameters = { topicNamePrefix.toUpperCase() + "%" };
        filterModes.add(new FDbFilterMode("UPPER(NAME) LIKE ?",
            nameFilterParameters));
        ISortMode[] sortModeArray =
            (ISortMode[]) sortModes.toArray(new ISortMode[0]);

        IFilterMode[] filterModeArray =
            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]);
        return getBaseCatalog().subCatalog(sortModeArray,
            filterModeArray, pageMode);
    }

    protected static Catalog getBaseCatalog() {
        if (baseCatalog == null) {
            baseCatalog = new FLazyCatalog(createBaseDataSource());
        }
        return baseCatalog;
    }

    protected static IDataSource createBaseDataSource() {
        return new FDbDataSource(
            "select topic_id, name, description from topic",
            createBaseEntryConverter());
    }

    protected static IDbEntryConvertor createBaseEntryConverter() {
        return new IDbEntryConvertor() {
            public Object convertRow(ResultSet resultSet)
            throws SQLException, CatalogException {
                long topicID = resultSet.getLong("topic_id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                StringBuffer sb = new StringBuffer();
                sb.append("<topic id=\"").append(topicID);
                sb.append("\"><topicType>").append(TopicType.ACADEMICS);
                sb.append("</topicType><name>")
				.append("<![CDATA[").append(name).append("]]>");
                sb.append("</name><description>")
				.append("<![CDATA[").append(description).append("]]>");
                sb.append("</description></topic>");
                
                return sb.toString();
            }
        };
    }

    protected int evaluatePageSize(String upId) {
        int pgSize = 10;    // Default.
        String strPageSize = getRuntimeData(upId).getParameter("catPageSize");

        if (strPageSize != null) {
            if (strPageSize.equals("All")) {
                pgSize = 0;
            } else if (strPageSize.trim().equals("")) {
                // Fall through...go w/ default.
            } else {
                pgSize = Integer.parseInt(strPageSize);
            }
        }
        return pgSize;
    }

    protected int evaluateCurrentPage(String upId) {
        int pgNum = 1;      // Default.
        String strPageNum = getRuntimeData(upId).getParameter("catSelectPage");

        if (strPageNum != null) {
            pgNum = Integer.parseInt(strPageNum);
        }
        return pgNum;
    }

    private void saveAdjunctData(Topic t, ChannelRuntimeData crd) throws Exception {

        IDecisionCollection[] dcs = evaluateAdjunctData(crd);
        Iterator it = Arrays.asList(dcs).iterator();
        while (it.hasNext()) {
            IDecisionCollection col = (IDecisionCollection) it.next();
            IAdjunctAgent a = AdjunctTopicData.getInstance().getAgent(col.getChoiceCollection());
            a.setDecisions(t, col);
        }

    }

    private IDecisionCollection[] evaluateAdjunctData(ChannelRuntimeData crd) throws Exception {

        List rslt = new ArrayList();

        Iterator collections = Arrays.asList(AdjunctTopicData.getInstance().getData()).iterator();
        while (collections.hasNext()) {

            IChoiceCollection l = (IChoiceCollection) collections.next();
            String s1 = l.getHandle().getValue();

            IEntityStore store = l.getOwner();

            List dec = new ArrayList();

            Iterator choices = Arrays.asList(l.getChoices()).iterator();
            while (choices.hasNext()) {

                IChoice c = (IChoice) choices.next();
                String s2 = c.getHandle().getValue();

                List sel = new ArrayList();

                Iterator options = Arrays.asList(c.getOptions()).iterator();
                while (options.hasNext()) {

                    IOption o = (IOption) options.next();
                    String s3 = o.getHandle().getValue();

                    String key = s1.concat(s2).concat(s3);

                    String value = crd.getParameter(key);
                    if (value != null && !value.trim().equals("")) {
                        IComplement p = o.getComplementType().parse(value);
                        sel.add(store.createSelection(o, p));
                    }

                }

                ISelection[] selections = (ISelection[]) sel.toArray(new ISelection[0]);
                dec.add(store.createDecision(null, c, selections));

            }

            IDecision[] decisions = (IDecision[]) dec.toArray(new IDecision[0]);
            rslt.add(store.createDecisionCollection(l, decisions));

        }

        return (IDecisionCollection[]) rslt.toArray(new IDecisionCollection[0]);

    }

}
