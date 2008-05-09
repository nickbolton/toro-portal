/*
 *******************************************************************************
 *
 * File:       CampusAnnouncementChannel.java
 *
 * Copyright:  �2002 Unicon, Inc. All Rights Reserved
 *
 * This source code is the confidential and proprietary information of Unicon.
 * No part of this work may be modified or used without the prior written
 * consent of Unicon.
 *
 *******************************************************************************
 */

package net.unicon.portal.channels.campusannouncement;

import com.interactivebusiness.portal.VersionResolver;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.campusannouncement.common.UPortalChannel;
import net.unicon.portal.channels.campusannouncement.domain.GroupsSearch;
import net.unicon.portal.channels.campusannouncement.domain.Universe;
import net.unicon.portal.channels.campusannouncement.types.Announcement;
import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.common.PermissibleFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.groups.GroupFactory;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.groups.UniconGroupServiceFactory;
import net.unicon.portal.permissions.Activity;
import net.unicon.portal.permissions.ActivityFactory;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;
import net.unicon.portal.servants.ServantManager;
import net.unicon.portal.servants.ServantType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IPermissible;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.jasig.portal.security.provider.PermissionManagerImpl;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.ContentHandler;

public class CampusAnnouncementChannel extends UPortalChannel
implements ICacheable, IPermissible {

    //added by Jing
    private static VersionResolver vr = VersionResolver.getInstance();
    private String toolbarString;
    private HashMap announcerMap;
    private HashMap groupsMap;
    private static HashMap activities = null;
    private static HashMap targets = null;

    private boolean makeSelection = false;
    private String returnPage;
    //private boolean notAB;
    private String announcementContent;
    public static final String CHANNEL = "net.unicon.portal.channels.campusannouncement.CampusAnnouncementChannel";
    private boolean KILL_CACHE = false;

    // permission activities
    public static final String ASSIGN_ANNOUNCER = "assignAnnouncer";
    public static final String ADD_ANNOUNCEMENT = "addAnnouncement";
    public static final String EDIT_ALL = "editAll";
    
    static {
        activities = new HashMap();
        activities.put(ASSIGN_ANNOUNCER, "User may assign announcers.");
        activities.put(ADD_ANNOUNCEMENT, "User may add an announcement.");
        activities.put(EDIT_ALL, "User may edit all annoucements.");

        targets = new HashMap();
        targets.put(CHANNEL, "CampusAnnouncementChannel");
    }    

    public CampusAnnouncementChannel() {
        super();
    }
    
    public void receiveEvent (PortalEvent event) {
        super.receiveEvent(event);
        ServantManager.sendPortalEvent(this, event);
    }

    public void buildXML(ContentHandler out) throws Exception {
		/*// spill parameters (uncomment to see)...
			  System.out.println("#### SPILLING PARAMETERS...");
			  Iterator it = runtimeData.keySet().iterator();
			  while (it.hasNext()) {
				  String key = (String) it.next();
				  String value = runtimeData.getParameter(key);
				  System.out.println("\t"+key.toString()+"="+value);
			}*/
        sslLocation = "CampusAnnouncementChannel.ssl";
        String command = runtimeData.getParameter("command");
        Servant ab_servant = (Servant) getStaticData().get("AddressbookServant");
        if (command == null && ab_servant !=null)
        {
		
          KILL_CACHE = true;          
               servantRenderXML(out);
               ab_servant = (Servant) getStaticData().get("AddressbookServant");
               if (ab_servant != null)
                 servant_display = true;
               else if (!makeSelection && returnPage.equalsIgnoreCase("edit-announcement")) {
       			 sheetName = "view-groups";
                 buildViewGroupsPage (getUniverse(), getPerson());
               }
               else if (makeSelection && returnPage.equalsIgnoreCase("edit-announcement")) {
       			sheetName = "assign-groups";
                 buildAssignGroupsPage (getUniverse(), getPerson(), groupsMap);
               }

                return;
        }
        else if (command == null && ServantManager.hasServant(this,ServantType.PERMISSION_ADMIN))
          {			
  			KILL_CACHE = true;
            ServantManager.renderServant(this, out, getStaticData(), getRuntimeData(),ServantType.PERMISSION_ADMIN);
            if (ServantManager.hasServant(this,ServantType.PERMISSION_ADMIN))
            {
    		servant_display = true;
              return;
            }
            else
            {
    		servant_display=false;
              //don't want to use the cache content because it need to be
              //refreshed to show the toolbar
              sheetName = "view-announcer";
              buildViewAnnouncerPage (getUniverse(), getPerson());
              return;
            }
          }

        else if (command == null || command.length() == 0 | command.equalsIgnoreCase("main")) {
			KILL_CACHE = true;
            sheetName = "main";
            buildNormalPage(getUniverse(), getPerson());
        } else if (command.equalsIgnoreCase("delete-announcement")) {
			sheetName = "delete-announcement";
            buildDeleteAnnouncementPage(getUniverse(), getPerson());
        } else if (command.equalsIgnoreCase("delete-announcements")){
		 buildDeleteSelectedPage(getUniverse(),getPerson()); 
        
        } else if (command.equalsIgnoreCase("edit-announcement"))
        {
		String subCommand = runtimeData.getParameter("sub-command");
          String submit;
          if (subCommand == null)
          {
  			sheetName = "edit-announcement";
            //notAB = true;
            announcementContent = null;
            buildEditAnnouncementPage(getUniverse(), getPerson());
            return;
          }
          else if (subCommand.equalsIgnoreCase("select-groups"))
          {
  		
            submit = runtimeData.getParameter("Submit");
            if (submit.equalsIgnoreCase("Next"))
            {
    		 returnPage = "edit-announcement";
              //save announcement content
              String message = runtimeData.getParameter("message");
              if (xml != null && xml.startsWith("<announcement-edit>"))
              {
      		 StringBuffer sBuf = new StringBuffer (xml.substring(0, xml.indexOf("<announcement-body>")));
                sBuf.append("<announcement-body>");
                // handle returns in body 
                String[] lines = message.split("\\r");
                for (int i = 0; i < lines.length; i++) {
                    sBuf.append("<![CDATA[").append(lines[i]).append("]]>").append("<br/>");
                }
                sBuf.append(xml.substring(xml.indexOf("</announcement-body>")));
                announcementContent = sBuf.toString();
              }
  			 sheetName = "view-groups";
              buildViewGroupsPage (getUniverse(), getPerson());

              // go straight to group selection if new announcement
              String id = runtimeData.getParameter("announcement-id");
              if (id.equals("-1"))
              {
      		  // Call Addressbook Servant
                getAddressBook(runtimeData);
                servant_display = true;
                ab_servant = (Servant) getStaticData().get("AddressbookServant");
                if (ab_servant != null)
                {
        		  servantRenderXML(out);
                }
              }
  			return;
            }
            else if (submit.equalsIgnoreCase("Change"))
            {
    		
              returnPage = "edit-announcement";
              // Call Addressbook Servant
              getAddressBook(runtimeData);
              servant_display = true;
              ab_servant = (Servant) getStaticData().get("AddressbookServant");
              if (ab_servant != null)
              {
      		 servantRenderXML(out);
                return;
              }
            }
            else if (submit.equalsIgnoreCase("Continue")) {
    		sheetName = "assign-groups";
             buildAssignGroupsPage (getUniverse(), getPerson(), groupsMap);
            }
          }
          else if (subCommand.equalsIgnoreCase("save-announcement"))
          {
  			//System.out.println("Message " + java.net.URLDecoder.decode(runtimeData.getParameter("message")));
            submit = runtimeData.getParameter("Submit");
            if (submit.equalsIgnoreCase("Submit"))
            {
    		  Universe universe = getUniverse();
              IPerson person = getPerson ();
              String announcement_id = runtimeData.getParameter("announcement-id");
              String date = runtimeData.getParameter("date");
              String message = runtimeData.getParameter("message");
              String [] groupsKey = runtimeData.getParameterValues("group_key");
              String [] groupsType = runtimeData.getParameterValues("group_type");
              universe.saveAnnouncement(announcement_id, date, message, groupsKey, groupsType, person);
              sheetName = "main";
              buildNormalPage(universe, person);
            }
          }
        }
        else if (command.equalsIgnoreCase("owned-announcements"))
        {
		sheetName = "owned-announcements";
          buildOwnedAnnouncementsPage (getUniverse(), getPerson());
          xslParameters.put("current-error","null");
        }
        else if (command.equalsIgnoreCase("edit-preferences")) {
		  sheetName = "edit-preferences";
            buildEditPreferencesPage(getUniverse(), getPerson());
        }
        else if (command.equalsIgnoreCase("admin-announcer"))
        {
		String subCommand = runtimeData.getParameter("sub-command");
          String submit;
          if (subCommand == null) {
  		  sheetName = "view-announcer";
            buildViewAnnouncerPage(getUniverse(), getPerson());
            return;
          }
          else if (subCommand != null &&
                   subCommand.equalsIgnoreCase("submit-announcer"))
          {
  		 submit = runtimeData.getParameter("Submit");
            if (submit.equalsIgnoreCase("change"))
            {
    	   if (ServantManager.hasServant(this, ServantType.PERMISSION_ADMIN)) {
                ServantManager.renderServant(this, out, getStaticData(), getRuntimeData(),
                                             ServantType.PERMISSION_ADMIN);
                servant_display = true;
                if (ServantManager.hasServant(this, ServantType.PERMISSION_ADMIN))
                  return;
              }

              //construct an activity DOM Element
              String activity_string = "<permissions><activity handle=\"" + ADD_ANNOUNCEMENT + "\"><label>Add Announcements</label><description>User may add an announcement.</description></activity></permissions>";
              DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
              DocumentBuilder builder = factory.newDocumentBuilder();
              Document doc = builder.parse(new java.io.ByteArrayInputStream(
                  activity_string.getBytes()));
              Element activityElement = doc.getDocumentElement();

              Activity[] activities = ActivityFactory.getActivities(activityElement);

              //generate IPermission Object
              int upId = Integer.parseInt(getStaticData().getChannelPublishId());
              ChannelDefinition cd = ChannelRegistryStoreFactory.
                  getChannelRegistryStoreImpl().getChannelDefinition(upId);
              //ChannelDataManager.registerChannelUser(getPerson(), cd, upId+"");
              String ownerName = null;
              String ownerToken = null;
              IPermissions p = null;
              IAuthorizationPrincipal principal = VersionResolver.getInstance().
                getPrincipalByPortalVersions(staticData.getPerson());
              if (cd != null) {
                ownerToken = cd.getJavaClass();
                ownerName = cd.getTitle();
                p = PermissionsService.instance().getPermissions(principal, cd);
              }
              else {

              }

  	       String[] targets = {ownerToken};//{"ADD_ICON"};
              IPermissible owner = PermissibleFactory.getPermissible(ownerToken,
                  ownerName, activities, targets);
              // create the servant .. the ServantManager will manage it for us.
              ServantManager.createPermissionAdminServant(this, getStaticData(),
                  owner, activities, null, "", null, false, false);
              // now render it
              
              ChannelRuntimeData temp_crd = getRuntimeData();
              temp_crd.put("sources", UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.channels.campusannouncement.CampusAnnouncementChannel.sources"));
              
              ServantManager.renderServant(this, out, getStaticData(), temp_crd,
                                           ServantType.PERMISSION_ADMIN);
              servant_display = true;
              return;
            }
          }
        }

        else {
          
	   KILL_CACHE = true;
          errorMessage = "CampusAnnouncementChannel received unsupported command: [" + command + "]";

        }

    }


    protected void buildNormalPage(Universe universe, IPerson person) throws Exception {

        CampusAnnouncementPreferences preferences = null;
        String subCommand = runtimeData.getParameter("sub-command");
        String submit = runtimeData.getParameter("Submit");

        if (subCommand != null && submit != null && submit.equalsIgnoreCase("submit")) {

            if (subCommand.equalsIgnoreCase("submit-preferences")) {
                preferences = submitPreferences(universe, person);
            } /*else if (subCommand.equalsIgnoreCase("submit-announcement")) {
                submitAnnouncement(universe, person);
            }*/
            else if (subCommand.equalsIgnoreCase("submit-delete")) {
                submitDelete(universe, person);
	        sheetName = "owned-announcements";
          	buildOwnedAnnouncementsPage (getUniverse(), getPerson());
                return;
 
            }else if (subCommand.equalsIgnoreCase("submit-deletes")) {
                submitDeletes(universe, person);
                sheetName= "owned-announcements";
                buildOwnedAnnouncementsPage (getUniverse(), getPerson());
                return;
            }
        }

        if (preferences == null) {

          Map rawPreferences = universe.getChannelPreferences(this, staticData,
              person);
		  GroupsSearch gs = new GroupsSearch ();
		  String userKey = vr.getUserKeyColumnByPortalVersions(person);
		  List myGroups = gs.getMyGroups(userKey);	
          preferences = new CampusAnnouncementPreferences(rawPreferences, myGroups);

        }

        //get all my groups

        List announcements = universe.getAnnouncements(person, preferences.getAgeLimit(), preferences.getOfferingIDs());


        Collections.sort(announcements);
        //it = announcements.iterator();
        int pageNumber = getCurrentPageNumber();
        int pageSize = preferences.getPageSize();
        int numberOfPages = -1;
        int start = 0;
        int end = announcements.size();

        if (pageSize > 0) {
            numberOfPages = ((announcements.size() - 1) / pageSize) + 1;

            if (subCommand != null && subCommand.equalsIgnoreCase("submit-page")) {
                if (submit.equalsIgnoreCase("first")) {
                    pageNumber = 1;
                } else if (submit.equalsIgnoreCase("last")) {
                    pageNumber = numberOfPages;
                } else if (submit.equalsIgnoreCase("previous")) {
                    pageNumber = Math.max(1, pageNumber - 1);
                } else if (submit.equalsIgnoreCase("next")) {
                    pageNumber = Math.min(numberOfPages, pageNumber + 1);
                } else if (submit.equalsIgnoreCase("goto")) {
                    pageNumber = Integer.parseInt(runtimeData.getParameter("selectedPageNumber"));
                }
            }

            start = (pageNumber - 1) * pageSize;
            end = Math.min(start + pageSize, announcements.size());
        }

        StringBuffer xmlBuffer = new StringBuffer("<campus-announcements>");
        xmlBuffer.append(getToolbar());

        for (int i = start; i < end; i++) {
            Announcement announcement = (Announcement) announcements.get(i);
            announcement.toXML(xmlBuffer);
        }

        xmlBuffer.append("</campus-announcements>");

        if (announcements.size() == 0) {
            sheetName = "empty";
        }

        xml = xmlBuffer.toString();

        // if(DEBUG)
    	//System.out.println (debugHeader+"xml = "+xml);

        xslParameters.put("channel_admin", "" + universe.isAdmin(person));
        xslParameters.put("current_command", "main");
        xslParameters.put("currentPage", "" + pageNumber);
        xslParameters.put("lastPage", "" + numberOfPages);
        xslParameters.put("pageSize", "" + preferences.getPageSize());
    }

    protected void buildEditPreferencesPage(Universe universe, IPerson person) throws Exception {

        Map rawPreferences = universe.getChannelPreferences(this, staticData, person);
        //CampusAnnouncementPreferences preferences = new CampusAnnouncementPreferences(rawPreferences);

        StringBuffer xmlBuffer = new StringBuffer("<campus-announcement-preferences><categories>");

        //added by Jing
        GroupsSearch gs = new GroupsSearch ();
        String userKey = vr.getUserKeyColumnByPortalVersions(person);
        List myGroups = gs.getMyGroups(userKey);
		CampusAnnouncementPreferences preferences = new CampusAnnouncementPreferences(rawPreferences, myGroups);
        List selectedGroups = preferences.getOfferingIDs();
        String group_id, group_name;
        IGroup group;
        for (int i = 0; i < myGroups.size(); i++)
        {
          group_id = (String) myGroups.get(i);		 
		  group = GroupFactory.getGroup(group_id);
		  //group_name = EntityNameFinderService.instance().getNameFinder(Class.
           //    forName("net.unicon.portal.groups.IGroup")).getName(group_id);
              
          formatTopicChoice(xmlBuffer, group_id, group.getPathsAsStrings(null, false)[0], selectedGroups.contains(group_id));
          
        }

        xmlBuffer.append("</categories>");
        xmlBuffer.append("</campus-announcement-preferences>");
        xml = xmlBuffer.toString();
		
		//System.out.println("XML " + xml); 
		
        xslParameters.put("current_command", "edit-preferences");
        xslParameters.put("maximumAge", "" + preferences.getAgeLimit());
        xslParameters.put("pageSize", "" + preferences.getPageSize());
    }

    protected void formatTopicChoice(StringBuffer buffer, Object value, Object label, boolean selected) {

        buffer.append("<category value=\"" + value + "\" selected=\"" + selected + "\">" + label + "</category>");

    }

    protected void buildDeleteAnnouncementPage(Universe universe, IPerson person) throws Exception {

        int announcementID = Integer.parseInt(runtimeData.getParameter("announcement-id"));
        Announcement announcement = universe.getCampusAnnouncement(announcementID);
        xml = announcement.toXML();
        xslParameters.put("current_command", "delete-announcement");
    }

    protected void buildDeleteSelectedPage(Universe universe, IPerson person) throws Exception {
        
       try{
         String[] announcementIDs = runtimeData.getParameterValues("announcementIDs");
         Announcement announcement = null;
         int announcementID = 0;  

         if(announcementIDs != null && announcementIDs.length > 0){

           sheetName = "delete-announcements";
           StringBuffer sBuff = new StringBuffer("<owned-announcements>");
           for (int i = 0; i < announcementIDs.length; i++){
             System.err.println("An announcementID: " + announcementIDs[i] + "\n");
             announcementID = Integer.parseInt(announcementIDs[i]);  
             announcement = universe.getCampusAnnouncement(announcementID);
             sBuff.append(announcement.toXML());
           }
           sBuff.append("</owned-announcements>");
           xml = sBuff.toString();
           xslParameters.put("current_command", "delete-announcements");
          }else{
            sheetName = "owned-announcements";
            buildOwnedAnnouncementsPage(universe ,person);
            xslParameters.put("current-error", "Please select at least one announcement to delete before selecting \'Delete-Selected\'");
          }
        }catch(Exception e){
            System.err.println(e.toString() + "\n");
            throw new Exception("Error in buildDeleteSelectedPage",e);
        }
    }

    protected void buildEditAnnouncementPage(Universe universe, IPerson person) throws Exception {
    	Announcement announcement = null;
        

        int announcementID = Integer.parseInt(runtimeData.getParameter("announcement-id"));

        if (announcementID >= 0) {
            announcement = universe.getCampusAnnouncement(announcementID);
        } else {
            announcement = universe.createCampusAnnouncement();
        }

        StringBuffer xmlBuffer = new StringBuffer("<announcement-edit>");
        announcement.toXML(xmlBuffer);

        xmlBuffer.append("</announcement-edit>");
        xml = xmlBuffer.toString();

        xslParameters.put("current_command", "edit-announcement");
    }

    protected CampusAnnouncementPreferences submitPreferences(Universe universe, IPerson person) throws Exception {

        String pageSizeChoice = runtimeData.getParameter("selectPageSize");
        String ageLimitChoice = runtimeData.getParameter("selectMaximumAge");
        String[] categoryChoices = runtimeData.getParameterValues("selectedCategories");

        //modified by Jing
        List offeringIDs;
        if (categoryChoices == null)
          offeringIDs = new ArrayList();
        else
          offeringIDs = new ArrayList(Arrays.asList(categoryChoices));

        //boolean getCampusAnnouncements = offeringIDs.remove("CAMPUS");

        int pageSize = pageSizeChoice.equalsIgnoreCase("all") ? 0 : Integer.parseInt(pageSizeChoice);
        int ageLimit = Integer.parseInt(ageLimitChoice);

        CampusAnnouncementPreferences preferences = new CampusAnnouncementPreferences(true, offeringIDs, pageSize, ageLimit);
        universe.setChannelPreferences(this, staticData, person, preferences.asMap());

        return preferences;
    }

    protected void submitDelete(Universe universe, IPerson person) throws Exception {

        String announcementIDString = runtimeData.getParameter("announcement-id");

        if (announcementIDString != null) {
            int announcementID = Integer.parseInt(announcementIDString);

            if (announcementID > 0) {
                universe.deleteAnnouncement(announcementID);
            }
        }
    }

    protected void submitDeletes(Universe universe, IPerson person) throws Exception {

        String[] announcementIDStrings = runtimeData.getParameterValues("announcementIDs");

        if (announcementIDStrings.length > 0){

          int announcementID = 0;
          for(int i=0; i < announcementIDStrings.length; i++){
              announcementID = Integer.parseInt(announcementIDStrings[i]);

              if (announcementID > 0) {
                  universe.deleteAnnouncement(announcementID);
              }
          }
        }
    }



    /**
     * added by Jing, 3/14/03
     * showing toolbar with certain previlege
     */
    protected String getToolbar ()
    {
        StringBuffer toolbarXML = new StringBuffer ("<toolbar>");
        try
        {
            //check if the user has permission to assign accouncer
            IAuthorizationPrincipal ap = vr.getPrincipalByPortalVersions (getPerson());
            if (ap.hasPermission(CHANNEL, ASSIGN_ANNOUNCER, CHANNEL))
                toolbarXML.append("<icon>admin</icon>");
            else
                toolbarXML.append("<icon>no-admin</icon>");
            //check if the user can publish announcement
            if (ap.hasPermission(
                CHANNEL,
                ADD_ANNOUNCEMENT,
                CHANNEL))
                toolbarXML.append("<icon>announcer</icon>");
            else
                toolbarXML.append("<icon>no-announcer</icon>");

            //everybody has option of 'preference'
            toolbarXML.append("<icon>preference</icon>");
            toolbarXML.append("</toolbar>");

            toolbarString = toolbarXML.toString();
        }
        catch (Exception e)
        {
        }
        return toolbarString;
    }

    protected void processAddressBookSelection (Object[] m_results)
    {
        try
        {
            String EntityID, EntityType, EntityName;
            IdentityData select;
            if (announcerMap != null && !announcerMap.isEmpty())
              announcerMap.clear();
            if (groupsMap != null && !groupsMap.isEmpty())
              groupsMap.clear();


            for (int i = 0; i < m_results.length; i++)
            {
                // addressbook returns these variables
                //G|u|4|Developers|Developers  = Group
                //E|u|2|demo|demo  = Portal User
                //E|p|flopez|flopez|Freddy Lopez  == Ldap user
                select = (IdentityData)m_results[i];
                //added by Jing
                EntityType = select.getType().equals("E") ? "2" : "3" ;
                EntityID   = select.getID();
                EntityName = select.getName();

                if (announcerMap != null)
                 announcerMap.put(EntityID+"_"+EntityType, EntityName);
                if (groupsMap != null)
                 groupsMap.put(EntityID+"_"+EntityType, EntityName);
                // if (DEBUG)
        //    System.out.println (debugHeader+"Entity="+select.toString());
            }
        }
        catch (Exception e)
        {
        }
    }

    protected void servantRenderXML(ContentHandler documentHandler) throws PortalException
    {
        ChannelRuntimeData rd = getRuntimeData();//this.runtimeData;
        Servant ab_servant = (Servant) getStaticData().get("AddressbookServant");
        if (!ab_servant.isFinished())
        {
            ab_servant.renderXML(documentHandler);
            if (!ab_servant.isFinished())
                return;
            //addressbook is finished
            else
            {
                Object[] results = ab_servant.getResults();
                if (results != null && results.length > 0)
                {
                    staticData.put("AddressbookResults", results);
                    // must remove AddressbookServant from staticData object
                    staticData.remove("AddressbookServant");

                    // parse results object
                    processAddressBookSelection (results);
                    makeSelection = true;
                }
                else
                {
                    // results came back empty, user clicked on cancel button or ok button without any selections
                    // return to admin screen
                    // must remove AddressbookServant from staticData object
                    staticData.remove("AddressbookServant");
                    makeSelection = false;
                }

            }
        }
        return;
    }

    protected void buildViewAnnouncerPage(Universe universe, IPerson person) throws Exception
    {
      PermissionManagerImpl pm = new PermissionManagerImpl (CHANNEL, (AuthorizationImpl)AuthorizationImpl.singleton());
      IAuthorizationPrincipal[] ap = pm.getAuthorizedPrincipals(ADD_ANNOUNCEMENT, CHANNEL);
      StringBuffer sBuf = new StringBuffer ("<announcers>");
      String nameFound;
      for (int i = 0; i < ap.length; i++)
      {
        //System.out.println(ap[i].getKey()+" - "+ap[i].getPrincipalString());
        //if person
        if (ap[i].getPrincipalString().startsWith("2."))
        {
          try {
             nameFound = EntityNameFinderService.instance().getNameFinder(Class.
                 forName("org.jasig.portal.security.IPerson")).getName(ap[i].getKey());
           }
           catch (SQLException sqle)
           {
             nameFound = ap[i].getKey();
           }
           if (ap[i].hasPermission(CHANNEL, ADD_ANNOUNCEMENT, CHANNEL))
              sBuf.append("<announcer key=\"").append(ap[i].getKey()).append("\" type=\"").
             append("2").append("\" name=\"").append(nameFound).append("\" permissionType=\"GRANT\"/>");
           else
             sBuf.append("<announcer key=\"").append(ap[i].getKey()).append("\" type=\"").
             append("2").append("\" name=\"").append(nameFound).append("\" permissionType=\"DENY\"/>");

        }
        //if group
        if (ap[i].getPrincipalString().startsWith("3."))
        {
          try
           {
             nameFound = EntityNameFinderService.instance().getNameFinder(Class.
                 forName("org.jasig.portal.groups.IEntityGroup")).getName(
                 ap[i].getKey());
           }
           catch (SQLException sqle)
           {
             nameFound = ap[i].getKey();
           }
           if (ap[i].hasPermission(CHANNEL, ADD_ANNOUNCEMENT, CHANNEL))
              sBuf.append("<announcer key=\"").append(ap[i].getKey()).append("\" type=\"").
             append("3").append("\" name=\"").append(nameFound).append("\" permissionType=\"GRANT\"/>");
           else
             sBuf.append("<announcer key=\"").append(ap[i].getKey()).append("\" type=\"").
             append("3").append("\" name=\"").append(nameFound).append("\" permissionType=\"DENY\"/>");

        }
      }
      sBuf.append("</announcers>");

      xml = sBuf.toString();
      xslParameters.put("current_command", "admin-announcer");
  }

    protected void buildViewGroupsPage(Universe universe, IPerson person) throws Exception
    {
      StringBuffer sBuf = new StringBuffer ("<announcementGroups>");
      if (groupsMap == null)
        groupsMap = new HashMap ();
      else
        groupsMap.clear();
      int announcementID = -1;
     try
      {
        announcementID = Integer.parseInt(runtimeData.getParameter(
            "announcement-id"));
      }
      catch (NumberFormatException nfe)
      {
        String temp = announcementContent;
        int index = temp.indexOf("announcement-id=\"")+17;
        int index1 = temp.indexOf("\"", index);
        String temp1 = temp.substring(index, index1);
        announcementID = Integer.parseInt(temp1);
      }
      if (announcementID < 0) //add new announcement
      {
      }
      else //editing existing announcement
      {
        //get groups from tables
        List groupLists = universe.getAnnouncementGroups(announcementID);
        //construct groupsMap
        //contruct xml
        String group_id, group_name, group_type;
        List sublist;
        for (int i = 0; i < groupLists.size(); i++)
        {
          sublist = (List) groupLists.get(i);
          group_id = (String) sublist.get(0);
          group_type = (String) sublist.get(1);
          if (group_type.equals("2"))
          {
            group_name = org.jasig.portal.services.EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.security.IPerson")).getName(group_id);
          }
          else
          {
            group_name = org.jasig.portal.services.EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.groups.IEntityGroup")).getName(group_id);
          }

          sBuf.append("<group key=\"").append(group_id).append("\" name=\"").append(group_name).append("\" type=\"").append(group_type).append("\"/>");
          groupsMap.put(group_id+"_"+group_type, group_name);
        }
      }
      sBuf.append("</announcementGroups>");
      xml = sBuf.toString();
      xslParameters.put("current_command", "edit-announcement");
  }

  protected void buildAssignGroupsPage(Universe universe, IPerson person, Map map) 
  throws Exception {
      StringBuffer sBuf = new StringBuffer ("<results><groups>");
      if(map != null) {
        // Recurse subgroups of Everyone, but do not recurse if
        // it containts everyone.
        boolean containsEveryoneGroup = false;
        IGroup rootGrp = UniconGroupServiceFactory.getService().getRootGroup();
        if (!map.containsKey(rootGrp.getKey() + "_" + rootGrp.getType()))
            recurseAssignedGroups(map);
        
        Set set = map.keySet();
        Iterator it = set.iterator();
        String userKey, entityType, mapKey, name;
       
        while (it.hasNext()) {
          mapKey = (String) it.next();
          userKey = mapKey.substring(0, mapKey.indexOf("_"));
          entityType = mapKey.substring(mapKey.indexOf("_")+1);
          name = (String) map.get(mapKey);
          sBuf.append("<group key=\"").append(userKey).append("\" type=\"").
              append(entityType).append("\" name=\"").append(name).append(
              "\"/>");
        }
      }
     sBuf.append("</groups>");
     if (announcementContent != null)
       sBuf.append(announcementContent);
    sBuf.append("</results>");
     xml = sBuf.toString();
     xslParameters.put("current_command", "edit-announcement");
  }


  protected void recurseAssignedGroups(Map map) 
  throws Exception {
      if (map != null) {
          Set set = new HashSet(map.keySet());
          Iterator it = set.iterator();
          String userKey, entityType, mapKey, name;

          Map toAddKey = new HashMap();

          while (it.hasNext()) {
              mapKey = (String) it.next();
              userKey = mapKey.substring(0, mapKey.indexOf("_"));
              try {
                  IGroup group = GroupFactory.getGroup(userKey);
                  
                  List desc = group.getAllDescendantGroups();
                  if (desc != null) { 
                      for (int ix=0; ix < desc.size(); ++ix) {
                          IGroup tmpGroup = (IGroup) desc.get(ix);
                          toAddKey.put(
                                      tmpGroup.getKey() + "_" + tmpGroup.getType(),
                                      tmpGroup.getName());
                      }
                      map.putAll(toAddKey);
                  }
              } catch (Exception e) { e.printStackTrace();}
          }
      }
  }
  
  
  protected void buildOwnedAnnouncementsPage (Universe universe, IPerson person) 
  throws Exception {
    StringBuffer sBuf = new StringBuffer ("<owned-announcements>");
    List announcements = universe.getOwnedAnnouncements (person);
    for (int i = 0; i < announcements.size(); i++) {
      Announcement announcement = (Announcement) announcements.get(i);
      announcement.toXML(sBuf);
    }
    sBuf.append("</owned-announcements>");
    xml = sBuf.toString();
    xslParameters.put("current_command", "edit-announcement");
  }

  public ChannelCacheKey generateKey() {
    ChannelRuntimeData runtimeData = this.runtimeData;

    ChannelCacheKey k = new ChannelCacheKey();

    StringBuffer sbKey = new StringBuffer(1024);
    k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    sbKey.append(CHANNEL);
    sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
    sbKey.append("stylesheetURI:");

    try {
      String sslURI = ResourceLoader.getResourceAsURLString(this.getClass(),
          sslLocation);
      sbKey.append(XSLT.getStylesheetURI(sslURI,runtimeData.getBrowserInfo()));
    } catch (Exception e) {
      sbKey.append("not defined");
    }

    k.setKey(sbKey.toString());

    return k;
  }

  /**
   * put your documentation comment here
   * @param validity
   * @return
   */
  public boolean isCacheValid(Object validity) {
    boolean cacheValid = true;

    if (KILL_CACHE) {
        KILL_CACHE = false;
        cacheValid = false;
    } else {
        String target = this.runtimeData.getParameter("targetSubscribeId");
        if (runtimeData.isTargeted()) {
            cacheValid = false;
        }
    }

    return cacheValid;
  }

  public void setKillCache (boolean b) {
    KILL_CACHE = b;
  }


  // IPermissible methods

  public String getOwnerName() {
    return "Academus Campus Announcements";
  }

  public String[] getActivityTokens () {
    return  (String[])activities.keySet().toArray(new String[0]);
  }

  public String getOwnerToken () {
    return CHANNEL;
  }

  public String getActivityName (String token) {
    return  (String)activities.get(token);
  }

  public String[] getTargetTokens () {
    return  (String[])targets.keySet().toArray(new String[0]);
  }

  public String getTargetName (String token) {
    String r = (String) targets.get(token);
    return r != null ? r : "";
  }

}
