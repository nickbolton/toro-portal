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


package com.interactivebusiness.classifieds.channel;

// Alex added
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.IdentityDataSearcher;
import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrChannelRuntimeData;
import net.unicon.portal.util.GroupsSearch;
import net.unicon.portal.util.PeepholeManager;
import net.unicon.portal.util.PortalSAXUtils;
import net.unicon.portal.util.RenderingUtil;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.IPermissible;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.XMLEscaper;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.interactivebusiness.classifieds.data.ClassifiedsDb;
import com.interactivebusiness.classifieds.data.IconInfo;
import com.interactivebusiness.classifieds.data.ItemInfo;
import com.interactivebusiness.classifieds.data.ItemList;
import com.interactivebusiness.classifieds.data.TopicInfo;
import com.interactivebusiness.classifieds.data.TopicList;
import com.interactivebusiness.portal.VersionResolver;

/**
 * Title:            CClassifieds Channel
 * Description:      provides the portal user to post their classified info,
 *
 * Copyright:        Copyright (c) IBS
 * Company:          Interactive Business Solution
 * @author           Jing Chai
 * @version 1.0
 */

public class CClassifieds extends BaseChannel
implements IMimeResponse, ICacheable, ICharacterChannel, IPermissible
{

  private static final String sslLocation = "CClassifieds/CClassifieds.ssl";

  private final XSLT xslt;

  //Classifieds specific declarations
  private static ClassifiedsDb classifiedsDB;
  private static XMLEscaper xmlescaper=new XMLEscaper();
  private static String personID;

  //State variables
  private String       m_action;
  private String       m_currentTopicID;          // current folder id
  private String       m_currentTopicName;
  private String       totalEntry;
  private ItemList     m_currentNeedApvdItemList;
  private ItemList     m_currentUserPublishedItem;
  private ItemList     m_currentUserDeniedItem;
  private ItemList     m_currentUserPendingItem;
  private String       m_currentUserTopicID;
  private ItemList     m_currentUserDeleteList;
  private ItemList     m_currentMsgToAuth;
  private String       m_currentPageIndex;
  private ItemList     m_currentPubItemInATopic;
  private TopicList    m_currentExpireItemTopicList;
  private String       m_currentDeleteMode;

  private boolean      hasApprovalMsg;
  private boolean      hasStatus;
  private boolean      isAdminRole;
  private boolean      isApprovalRole;


  private static final String PENDING  ="P";   // for table CLASSIFIED_ITEM column APPROVED
  private static final String DENIED   ="D";
  private static final String APPROVED ="A";

  // Freddy added
  private ArrayList sError = new ArrayList ();
  private String xslFile = null;
  private ItemInfo newItemInfo = new ItemInfo();;
  private ArrayList selectedGroups;
  private ArrayList folderEditGroupIDs;
  private GroupsSearch gs = new GroupsSearch ();
  private TopicInfo myTopicInfo = new TopicInfo();
  private Vector namesFound = null;
  private Hashtable topicNames = new Hashtable();
  private ItemList il;
  private static final String fs = File.separator;
  private ArrayList myClassifiedsList = new ArrayList();
  private TopicInfo topicDisplayed = null;
  private static final String CHANNEL = "AcademusClassifieds";

  private static VersionResolver vr = VersionResolver.getInstance();

// *** JK - fix for TT03675, TT03908 - use address book for approver selection ala briefcase
  private Servant m_servant;

  private boolean locked = false;

// *** Alex - fix for TT03858
  private static String DATASOURCE;
  private static String datastore;


  // IPermissible attributes
  private static HashMap activities = null;
  private static HashMap targets = null;

  // permission activities
  public static final String VIEW = "VIEW";

private static final int FILE_SIZE = 102400;

  static
  {
      DATASOURCE = UniconPropertiesFactory.getManager(
                      PortalPropertiesType.RAD).getProperty(
                        "com.interactivebusiness.classifieds.channel.datasource"
                      );
      datastore = UniconPropertiesFactory.getManager(
                      PortalPropertiesType.RAD).getProperty(
                        "com.interactivebusiness.classifieds.channel.datastore"
                      );

      activities = new HashMap();
      activities.put(VIEW, "User may view.");

      targets = new HashMap();
      targets.put("TOPICS_ICON", "User may manage classified topics");
      targets.put("MY_CLASSIFIEDS_ICON", "User may delete any posted classified");
  }
// *** end fix for TT03858

  public CClassifieds ()
  {
    this.xslt = new XSLT(this);
  }
  
  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    // Reset serialized execution lock
    locked = false;

    this.staticData = sd;

    // *** Alex - fix for TT03858
    // String DATASOURCE = staticData.getParameter("datasource").trim();
    // Create an instance of the database access object.
    if(DATASOURCE != null)
    {
      try
      {
        classifiedsDB = ClassifiedsDb.getInstance(DATASOURCE);  // create the singleton object
      }
      catch (Exception e)
      {
        LogService.instance().log (LogService.ERROR, "Exception connecting to database with datasource name "+DATASOURCE+".\n" );
      }
    }
    listTopics();
  }

  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
      try {
          // Obtain object monitor
          synchronized (this) {
              // Attempt to obtain the serialized execution lock
              while (!testAndSetLock()) {
                  wait();
              }
          }

          // Proceed with execution
          CscrChannelRuntimeData ccrd = new CscrChannelRuntimeData( this.staticData.getChannelPublishId(), rd );
          super.setRuntimeData(ccrd);
          processRuntimeData(ccrd);

      } catch (InterruptedException ie) {

          // Release the serialized execution lock if an error occured
          releaseLock();
          throw new PortalException(ie);

      } catch (PortalException pe) {

          // Release the serialized execution lock if an error occured
          releaseLock();
          throw pe;
      }
  }

  /**
    Tests and sets the serialized execution lock
   */
  synchronized private boolean testAndSetLock () {

      boolean result = false;

      if (locked == true) {
          result = false;
      } else {
          locked = true;
          result = true;
      }

      return result;
  }

  /**
    Releases the serialized execution lock so that
    other threads can start executing
   */
  synchronized private void releaseLock () {

      // Set the serialized execution lock to false
      locked = false;

      // Notify all waiting threads that the lock has been released
      notifyAll();
  }

  private void processRuntimeData  (ChannelRuntimeData rd)
  throws PortalException {

    m_action = rd.getParameter("action");

    // check if Addressbook Servant is active
    if (staticData.get("AddressbookServant") != null)
    {
      getAddressBook (rd);
    }
    else if (m_action != null)
    {
      if (m_action.equals("main"))
      {
        personID=Integer.toString(staticData.getPerson().getID());
        hasApprovalMsg=classifiedsDB.hasApprovalMessages(personID);
        hasStatus=hasStatus();

        if (m_currentPageIndex !=null)
          m_currentPageIndex=null;
        if (m_currentTopicID !=null)
          m_currentTopicID=null;
        if (m_currentTopicName!=null)
          m_currentTopicName=null;
        if (m_currentPubItemInATopic!=null)
          m_currentPubItemInATopic=null;

      }
      else if  (m_action.equals("assignApprover") && checkInput (rd, "newtopic")) {

          ArrayList mySelection = myTopicInfo.getSelectedGroups();

          // if editing a topic, allow removal of current approvers and addition of new ones
          if (mySelection != null)
          {
               Document doc = DocumentFactory.getNewDocument();
               doc = getApproverScreen (doc, rd);
          }
          // otherwise this is a new topic, so force selection of approver
          else
          {
               if (rd.getParameter("cancel") == null)
                    getAddressBook (rd);
          }
      }
      else if (m_action.equals("PreviewTopic") && rd.getParameter("next") != null)
        getAddressBook (rd);
      else if (m_action.equals("PreviewTopic") && rd.getParameter("skip") != null)
        rd.setParameter("next", "next");
      else if (staticData.get("AddressbookServant") != null)
        getAddressBook (rd);
    }
  }



  void servantRenderXML(PrintWriter writer) throws PortalException 
  {
    Document doc = DocumentFactory.getNewDocument();

    ChannelRuntimeData rd = this.runtimeData;

    //String userID = Integer.toString(staticData.getPerson().getID());
    String userID = vr.getUserKeyColumnByPortalVersions (staticData.getPerson());

    if (!m_servant.isFinished()) 
    {

      //  Capture all servant content in a SAX buffer first
      SAX2BufferImpl documentHandler = new SAX2BufferImpl();

      m_servant.renderXML(documentHandler);

      try {

         // Serialize servant content and write it out as part of the channel content
         writer.print(PortalSAXUtils.serializeSAX(staticData.getSerializerName(), documentHandler));

      } catch (Exception e) {

          LogService.log(LogService.ERROR, "Classifieds: An error occured while serializing servant content.", e);
      }

      if (!m_servant.isFinished())
        return;
      else 
      {
        // addressbook is finished
        Object[] results = m_servant.getResults();

        // I now have the user's groups selections, must now save them

        if (results != null && results.length > 0) 
        {
          // got approver selections from address book
          staticData.put("AddressbookResults", results);

          // must remove AddressbookServant from staticData object
          staticData.remove("AddressbookServant");

          // parse results object
          processAddressBookSelection (userID, results, rd);

          doc = getIconChooserScreen (doc, rd);
          xslFile = "iconChooser";
        }
        else
        { // results came back empty - either cancel or empty selection (error)

          // remove AddressbookServant from staticData object
          staticData.remove("AddressbookServant");

          // return to topic screen
          doc = getAddTopicScreen (doc, rd);
        }

        Hashtable params = new Hashtable();

        // Set stylesheet parameters
        params.put("baseActionURL", runtimeData.getBaseActionURL());
        params.put("resourceURL", runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true));
               

        // Render the remaining channel content
        RenderingUtil.renderDocument(this, staticData.getSerializerName(),
            writer, doc, sslLocation, xslFile,
		    runtimeData.getBrowserInfo(), params);

      }
    }
  }

  private void ccRenderXML(PrintWriter out) throws PortalException
  {
    ChannelRuntimeData rd = this.runtimeData;
    String userID = Integer.toString(staticData.getPerson().getID());

    Document doc = DocumentFactory.getNewDocument();
    String m_action = rd.getParameter("action");

    if (m_action == null || m_action.equals("")) {

          renderPeephole(out);
    }
    else if(m_action.equals("main"))
    {
      doc = getMainScreen (doc, rd);
    }
    else if (m_action.equals("new"))
    {
      doc = getNewItemScreen (doc, rd);
    }
    else if (m_action.equals("previewNew"))
    {
      if (rd.getParameter("cancel") != null)
      {
        // user has hit cancel
        clearItemVars();
        doc = getMainScreen (doc, rd);
      }
      else
      {
        // preview new item
        if (checkInput (rd, "newclassified"))
        {
            doc = getPreviewNewItemScreen (doc, rd);
        }
        else
        {
          setNewItemInfo(rd);
          doc = getNewItemScreen (doc, rd);
        }
      }
    }
    else if (m_action.equals("saveNew"))
    {
      if (rd.getParameter("back") != null)
      {
        // user has hit back, return to New Item screen
        doc = getNewItemScreen (doc, rd);
      }
      else if (rd.getParameter("cancel") != null)
      {
        // user has hit cancel, return to Main screen
        clearItemVars();
        doc = getMainScreen (doc, rd);
      }
      else
      {
        // save new item to DB
        doc = getSaveClassifiedScreen (doc, rd);
      }
    }
    else if (m_action.equals("ClassifiedSaved"))
    {
      clearItemVars();
      if (rd.getParameter("createanother") != null)
      {
        doc = getNewItemScreen (doc, rd);
      }
      else if (rd.getParameter("exit") != null)
      {
        doc = getMainScreen (doc, rd);
      }
    }
    else if (m_action.equals("manageTopics"))
    {
      doc = getManageTopicsScreen (doc, rd);
    }
    else if (m_action.equals("addTopic"))
    {
      doc = getAddTopicScreen (doc, rd);
    }
    else if (m_action.equals("assignApprover"))
    {
      if (rd.getParameter("cancel") != null)
      {
        clearTopicVars();
        doc = getManageTopicsScreen (doc, rd);
      }
      else if (rd.getParameter("next") != null)
        getAddressBook(rd);
      else
        if (checkInput (rd, "newtopic"))
          doc = getGroupsListScreen (doc, rd);
        else
          doc = getAddTopicScreen (doc, rd);
    }
    else if (m_action.equals("PreviewTopic"))
    {
      if (rd.getParameter("go") != null)
      {
          // user has entered a userID to user
          String userName = rd.getParameter("person_name");
          if (userName != null) {
            try {
              namesFound = IdentityDataSearcher.personSearch(userName.trim(), "", 50);
            } catch (Exception e) {
              LogService.log (LogService.ERROR,
                  "CClassifieds::ccRenderXML threw Exception:"+e);
            }
          }

          //rd.setParameter("nameFound", nameFound == null ? "notfound" : nameFound);
          doc = getGroupsListScreen (doc, rd);
      }
      else if (rd.getParameter("next") != null)
      {
        doc = getIconChooserScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {
        doc = getAddTopicScreen (doc, rd);
      }
      else if (rd.getParameter("cancel") != null)
      {
        // show Topics menu
        clearTopicVars();
        doc = getManageTopicsScreen (doc, rd);
      }
      else
      {
        doc = getGroupsListScreen (doc, rd);
      }
    }
    else if (m_action.equals("chooseIcon"))
    {
      if (rd.getParameter("next") != null)
      {
        // need to check that atleast one radio button was selected
        if (checkInput (rd, "icon"))
          doc = getPreviewTopicScreen (doc, rd);
        else
          doc = getIconChooserScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {
        doc = getGroupsListScreen (doc, rd);
      }
      else if (rd.getParameter("cancel") != null)
      {
        clearTopicVars();
        doc = getManageTopicsScreen (doc, rd);
      }
      else if (rd.getParameter("add") != null)
      {
        saveTopicIcon (rd);
        doc = getIconChooserScreen (doc, rd);
      }
      else if (rd.getParameter("remove") != null)
      {
        deleteIcon (rd.getParameter("remove"));
        doc = getIconChooserScreen (doc, rd);
      }
    }
    else if (m_action.equals("SaveTopic"))
    {
      if (rd.getParameter("next") != null)
      {
        doc = getSaveTopicScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {
        doc = getIconChooserScreen (doc, rd);
      }
      else if (rd.getParameter("cancel") != null)
      {
        clearTopicVars();
        doc = getManageTopicsScreen (doc, rd);
      }
    }
    else if (m_action.equals("SaveTopicError"))
    {
      if (rd.getParameter("createanother") != null)
      {
        clearTopicVars();
        doc = getAddTopicScreen (doc, rd);
      }
      else if (rd.getParameter("exit") != null)
      {
        clearTopicVars();
        doc = getManageTopicsScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {
        doc = getPreviewTopicScreen (doc, rd);
      }
    }
    else if (m_action.equals("TopicSearch"))
    {
      doc = getTopicSearchResultsScreen (doc, rd);
    }
    else if (m_action.equals("DeleteTopic"))
    {
      doc = getConfirmDeleteTopicScreen (doc, rd);
    }
    else if (m_action.equals("DeleteTopicConfirmed"))
    {
      if (rd.getParameter("ok") != null)
      {
        // user has confirmed delete operation
        if (deleteTopic ())// topic deleted, go back to search results
          doc = getTopicSearchResultsScreen (doc, rd);
        else  // error occurred, display error then send back to search results
          doc = getDeleteTopicErrorScreen (doc);
      }
      else if (rd.getParameter("cancel") != null)
      {
        // user has canceled delete operation, return to search results screen
        doc = getTopicSearchResultsScreen (doc, rd);
      }

    }
    else if (m_action.equals("DeleteApprover"))
    {
      String approverID = (String) rd.getObjectParameter("approverID");
      ArrayList mySelection = myTopicInfo.getSelectedGroups();
      mySelection.remove(mySelection.indexOf(approverID));
      myTopicInfo.setSelectedGroups(mySelection);
      doc = getGroupsListScreen (doc, rd);
    }
    else if (m_action.equals("EditTopic"))
    {
      // get all Topic information for selected topic to edit
      getTopicInfo (rd);
      doc = getAddTopicScreen (doc, rd);
    }
    else if (m_action.equals("approver"))
    {
      doc = getApproverScreen (doc, rd);
    }
    else if (m_action.equals("approval"))
    {
     // check to see if you clicked on Next button
     if (rd.getParameter("next") != null)
     {
       // must first verify that the user selected a choice, else prompt with error message
       if (checkInput (rd, "approval"))
       {
         // if selected, save as approved or denied and display next item
         // and save to database
         if (rd.getParameter("choice") != null)
           saveApprovedOrDeniedItem (rd);
       }
       // show next item to approve
       doc = getItemsToApprove (doc, rd);
     }
     else if (rd.getParameter("finished") != null)
     {
       if (checkInput (rd, "approval"))
       {
         // if selected, save as approved or denied and display ApproverScreen
         // and save to database
         if (rd.getParameter("choice") != null)
           saveApprovedOrDeniedItem (rd);
         // show approver screen
         doc = getApproverScreen (doc, rd);
       }
       else
       {
         // show next item to approve
         doc = getItemsToApprove (doc, rd);
       }
     }
     else if (rd.getParameter("cancel") != null)
     {
       // user has requested to cancel and return to Approver Screen
       doc = getApproverScreen (doc, rd);
     }
     else
     {
       // user request to approve item for the first time
       doc = getItemsToApprove (doc, rd);
     }
    }
    else if (m_action.equals("detail"))
    {
      // view classifieds under certain topic
      doc = getItemsForTopic (doc, rd);
    }
    else if (m_action.equals("vdetail"))
    {
      // view classified item detail
      doc = getItemDetails (doc, rd);
    }
    else if (m_action.equals("myClassifieds"))
    {
      // view classifieds submitted by the user
      doc = getMyClassifieds (doc, rd);
    }
    else if (m_action.equals("details"))
    {
      // view myClassifieds item detail
      doc = getMyClassifiedsDetails (doc, rd);
    }
    else if (m_action.equals("DeleteItem"))
    {
      // user requesting to delete myClassifieds items
      doc = getConfirmDeleteItemScreen (doc, rd);
    }
    else if (m_action.equals("DeleteItemConfirmed"))
    {
      if (rd.getParameter("ok") != null)
      {
        // user has confirmed delete operation
        if (deleteItems ())// topic deleted, go back to search results
          doc = getMyClassifieds (doc, rd);
        else  // error occurred, display error then send back to search results
          doc = getDeleteTopicErrorScreen (doc);
      }
      else if (rd.getParameter("cancel") != null)
      {
        // user has canceled delete operation, return to myClassifieds screen
        doc = getMyClassifieds (doc, rd);
      }

    }

    /*
     org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
     String result = printString.serializeNode(doc);
     System.out.println ("DOM AS STRING == " + result + "\n");
    */
     Hashtable params = new Hashtable();

     // Set stylesheet parameters
     params.put("baseActionURL", runtimeData.getBaseActionURL());
     params.put("resourceURL", runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, true));


     // Render XML document as channel content output
     RenderingUtil.renderDocument(this, staticData.getSerializerName(),
         out, doc, sslLocation, xslFile,
         runtimeData.getBrowserInfo(), params);
  }

  protected void processAddressBookSelection (String userID, Object[] m_results, ChannelRuntimeData rd) 
  {
    boolean noGroupChecked = true;
    ArrayList mySelection = myTopicInfo.getSelectedGroups(); // new ArrayList ();

    if (mySelection == null)
      mySelection = new ArrayList();

    // populate the ArrayList mySelection
    String EntityID, EntityType;
    IdentityData select;

    for (int i = 0; i < m_results.length; i++) 
    {
      // addressbook returns these variables
      //G@u@4@Developers@Developers  = Group
      //E@u@2@demo@demo  = Portal User
      //E@p@flopez@flopez@Freddy Lopez  == Ldap user

      select = (IdentityData)m_results[i];

      //String EntityID = select.getID();
      //added by Jing
      EntityType = select.getType();

      if (VersionResolver.getPortalVersion().startsWith("2.0") || EntityType.equals("G"))
        EntityID = select.getID();
      else if (VersionResolver.getPortalVersion().startsWith("2.1"))
        EntityID = select.getAlias();
      else
        EntityID = select.getID();

      // name=groupID-entityType (2=person or 3=group)
      if (!mySelection.contains(EntityID + "-" + (EntityType.equals("G") ? "3" : "2")))
        mySelection.add(EntityID + "-" + (EntityType.equals("G") ? "3" : "2"));
    }
    myTopicInfo.setSelectedGroups(mySelection);
  }

  private void getAddressBook (ChannelRuntimeData rd) 
  {
    // Create communication with AddressBook
    m_servant = getAddressBookServant (rd);

    if (!m_servant.isFinished()) 
    {
      m_servant.setStaticData(staticData);
      m_servant.setRuntimeData(rd);
    }
  }

  private Servant getAddressBookServant (ChannelRuntimeData rd) 
  {
    if (staticData.get("AddressbookServant") == null) 
    {
      String name = "net.unicon.portal.channels.addressbook.Select";
      m_servant = new Servant ();
      m_servant.start(name);
      // "do" and "go" are RAD reserved parameter names; if you use them in
      // your own XSLs, you must remove them for the initial call to a RAD
      // servant
      rd.remove("do");
      rd.remove("go");
      // Tien 1120 for Unicon
      rd.put("sources", "portal,campus");
      //
      staticData.put("AddressbookServant", m_servant);
    }
    else 
    {
      m_servant = (Servant)  staticData.get("AddressbookServant");
    }
    return m_servant;
  }

    /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderCharacters (PrintWriter out) throws PortalException
  {

    try {
        // If Addressbook Servant is active, then call AddressBook
        m_servant = (Servant) staticData.get("AddressbookServant");

        if (m_servant != null)
            servantRenderXML(out);
        else
            ccRenderXML(out);

    } finally {

        // Ensure that the serialized execution lock is released
        releaseLock();
    }    
  }

  // Renders the peehole view for the Classifieds channel
  private void renderPeephole(PrintWriter out) {

      String baseActionURL = runtimeData.getBaseActionURL();

      String peepholeView = PeepholeManager.getInstance().getPeephole(this.getClass(), baseActionURL);

      out.print(peepholeView);
  }

  private void clearTopicVars()
  {
    myTopicInfo.setCreateDate(null);
    myTopicInfo.setDescription(null);
    myTopicInfo.setName(null);
    myTopicInfo.setSelectedGroups(null);
    myTopicInfo.setTopicID(null);
    myTopicInfo.setTotalEntry(null);
    myTopicInfo.setImageName(null);
    myTopicInfo.setImageType(null);

    folderEditGroupIDs = null;
    myTopicInfo.currentState = null;
  }

  private void clearItemVars()
  {
    newItemInfo.setContactName(null);
    newItemInfo.setCost(null);
    newItemInfo.setEmail(null);
    newItemInfo.setItemContent(null);
    newItemInfo.setItemID(null);
    newItemInfo.setPhone(null);
    newItemInfo.setTopicID(null);
    newItemInfo.setAuthorID(null);
    newItemInfo.setItemImage(null);
    newItemInfo.setImageInputStream(null);
    newItemInfo.setImageMimeType(null);
  }

  private void getTopicInfo (ChannelRuntimeData rd)
  {
    String topicID = rd.getParameter("topicID");

    // need to get Topic Name, Description, and Selected Groups
    try {
      myTopicInfo = classifiedsDB.getTopicInfo(topicID);
      myTopicInfo.currentState = "Edit";
    }catch (SQLException sqe)
    {
      System.out.print("java.sql.SQLException sqe = "+sqe+"\n");

    }

  }

  private Document getAddTopicScreen (Document doc, ChannelRuntimeData rd)
  {

    Element AddTopic = doc.createElement("AddTopic");

    Element TopicName = doc.createElement("topic_name");

    if (sError.contains("TOPIC"))
    {
      TopicName.setAttribute("error","error");
      sError.clear();
    }

    TopicName.appendChild(doc.createTextNode(myTopicInfo.getName() != null ? myTopicInfo.getName() : ""));
    AddTopic.appendChild(TopicName);

    Element TopicDescription = doc.createElement("topic_description");
    TopicDescription.appendChild(doc.createTextNode(myTopicInfo.getDescription() != null ? myTopicInfo.getDescription() : ""));
    AddTopic.appendChild(TopicDescription);

    doc.appendChild(AddTopic);
    xslFile = "addTopic";
    return doc;

  }

  //Freddy Added for group support
  private Document getGroupsListScreen (Document doc, ChannelRuntimeData rd)
  {
    if (selectedGroups == null)
    {
      selectedGroups = new ArrayList();
    }

    // user is editing current group selections
    if (myTopicInfo.getSelectedGroups() != null)
    {
      folderEditGroupIDs = myTopicInfo.getSelectedGroups();
    }

/*    if (folderEditGroupIDs == null)
    {
      folderEditGroupIDs = new ArrayList ();

      if (myTopicInfo.currentState != null && myTopicInfo.currentState.equals("Edit"))
      {
        //folderEditGroupIDs = newsDB.getTopicGroupID(myTopicInfo.editTopicID);
      }
     // else if (
    }
**/
    String groupSelected = rd.getParameter("selected");
    String groupExpand = rd.getParameter("expand");
    String isLeafNode = rd.getParameter("leafnode");

    Element SelectGroupTopicElement = doc.createElement("SelectGroupTopic");

    Element TopicNameElement = doc.createElement("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getName()));
    SelectGroupTopicElement.appendChild(TopicNameElement);

    if (rd.getParameter("error") != null)
    {
      Element ErrorElement = doc.createElement("error");
      SelectGroupTopicElement.appendChild(ErrorElement);
    }

    // if not empty then user is editing topic and has previously selected groups
      // check to see if user selected previous search name
      Enumeration keys = rd.keys();

      while (keys.hasMoreElements())
      {
        String paramKey = (String) keys.nextElement();
        //fixes for tt 03690 by Jing 7/7/2003
        String value;
        try{
          value = (String) rd.getObjectParameter(paramKey);
        }
        catch (ClassCastException e)
        {
          value = "";
        }
        //end of fixes for tt 03690
// *** JK TT03690 fix
//        if (value.equals("checked"))
        if (value != null && value.equals("checked"))
        {
          if (!folderEditGroupIDs.contains(paramKey))
            folderEditGroupIDs.add(paramKey);
        }
      }


    if (folderEditGroupIDs != null && !folderEditGroupIDs.isEmpty())
    {
      Element PreviousElement = doc.createElement("previousSelection");

      for (int i=0; i < folderEditGroupIDs.size(); i++)
      {

        Element GroupNameElement = doc.createElement("groupName");
        String groupID = (String) folderEditGroupIDs.get(i);
        int dashat = groupID.indexOf("-");
        String viewerType = gs.checkIfGroupOrPerson (groupID.substring(dashat+1));
        GroupNameElement.setAttribute("type", viewerType);
        GroupNameElement.setAttribute("name", gs.getReadableName (viewerType, groupID.substring(0, dashat)));
        GroupNameElement.setAttribute("entity", groupID.substring(dashat+1));
        GroupNameElement.setAttribute("ID", groupID.substring(0, dashat));
        PreviousElement.appendChild(GroupNameElement);
      }
      SelectGroupTopicElement.appendChild(PreviousElement);

    }

    if (namesFound != null)
    {
    // user has attempted a person search
       Element NameSearchElement = doc.createElement("name_search");
      if (namesFound.size() == 0)
      {


      Element NotFoundElement = doc.createElement("nameNotFound");
      NotFoundElement.setAttribute("name", rd.getParameter("person_name"));
      NameSearchElement.appendChild(NotFoundElement);
      }
      else
      {

        for (int i=0; i < namesFound.size(); i++)
        {
          IdentityData user = (IdentityData)namesFound.get(i);
          Element PersonNameElement = doc.createElement("personName");
          PersonNameElement.setAttribute("ID", user.getAlias());
          PersonNameElement.setAttribute("name", user.getName());
          PersonNameElement.setAttribute("entity", "2");
          NameSearchElement.appendChild(PersonNameElement);
        }

      }
       SelectGroupTopicElement.appendChild(NameSearchElement);
      // reset name search ArrayList container
      namesFound = null;
    }

    try
    {
      Element GroupsListElement = doc.createElement("groups_list");


        if (groupSelected == null)
        {
          GroupsListElement.appendChild(gs.getGroupElement (groupSelected, doc));
        }
        else if (groupSelected != null)
        {
          if(selectedGroups.size() > 1 && groupSelected.equals("0"))
            selectedGroups.clear();
          // add groups selected to tree
          if(selectedGroups.size() > 1)
            gs.addSelection (selectedGroups, groupSelected);
          else
            selectedGroups.add(groupSelected);

          if (isLeafNode == null)
        {
          gs.expandGroup (selectedGroups, GroupsListElement, groupSelected);
        }
          else
          {
             String userID =  Integer.toString(staticData.getPerson().getID());
             gs.expandGroup (selectedGroups, GroupsListElement, groupSelected, isLeafNode, userID);
          }
        }
      SelectGroupTopicElement.appendChild(GroupsListElement);
      doc.appendChild(SelectGroupTopicElement);

/*
      org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
*/
    }
    catch (GroupsException ge)
    {

    }
    xslFile = "selectApprover";

    return doc;
  }

  private Document getSaveTopicScreen (Document doc, ChannelRuntimeData rd)
  {

    Element TopicSaved = doc.createElement("TopicSaved");


    String topicID = null;
    try
    {
      String userID = Integer.toString(staticData.getPerson().getID());
      if (myTopicInfo.currentState != null && myTopicInfo.currentState.equals("Edit"))
      {
        classifiedsDB.updateTopic(myTopicInfo.getTopicID(), userID, myTopicInfo);
        topicID = "passed";
      }
      else if(!(isTopicNameDuplicate(myTopicInfo.getName())))
      {
        topicID = classifiedsDB.createTopic(myTopicInfo);
         if(topicID != null)
         {

          classifiedsDB.setTopicApprover (topicID, userID, myTopicInfo.getSelectedGroups());

         }
      }
      else
      {
//  System.out.print("Topic already exist...\n");
      sError.add("Violation of unique index");
      }
    }
    catch (java.sql.SQLException sqe)
    {
//      System.out.print("java.sql.SQLException sqe = "+sqe+"\n");
      sError.add(sqe.toString());
    }

    if (topicID != null)
    {
      Element Saved = doc.createElement("saved");
      Saved.setAttribute("topic_name", myTopicInfo.getName());
      TopicSaved.appendChild(Saved);
      // need to reset hashtable containing topic names
      listTopics();
    }
    else
    {
      Element NotSaved = doc.createElement("notsaved");
      NotSaved.setAttribute("error", getFriendlyError((String)sError.get(0)));
      NotSaved.setAttribute("topic_name", myTopicInfo.getName());
      TopicSaved.appendChild(NotSaved);
    }
    doc.appendChild(TopicSaved);
    xslFile = "saveTopic";
    return doc;

  }

  private String getFriendlyError (String exceptionString)
  {

    String friendlyError;

    if (exceptionString.indexOf("Violation of unique index") != -1)
      exceptionString = "Topic name already exists, please choose a different name";

    return exceptionString;
  }

  private Document getSaveClassifiedScreen (Document doc, ChannelRuntimeData rd)
  {

    Element ClassifiedSaved = doc.createElement("ClassifiedSaved");

    String topicID = null;

    // save New Classified article
    saveNewItem ();

    if (!sError.isEmpty())
    {
      Element NotSaved = doc.createElement("notsaved");
      NotSaved.appendChild(doc.createTextNode(""));//(String)sError.get(0)));
      ClassifiedSaved.appendChild(NotSaved);
    }
    else
    {
      Element Saved = doc.createElement("saved");
      Saved.appendChild(doc.createTextNode(""));
      ClassifiedSaved.appendChild(Saved);
    }
    doc.appendChild(ClassifiedSaved);
    xslFile = "saveClassified";
    return doc;

  }

  private void writeMainPage(StringWriter w)
  {
    writeMainUserRole(w);
    boolean isApprovedlist=true;
    java.util.ArrayList topics=classifiedsDB.getCurrentTopicList();

    if (topics.isEmpty())
    {
      w.write("<needWarning>true</needWarning>");
      w.write("<warning>There are no Classifieds Topics listed!</warning>");
    }
    else
    {
      writeTopicList(w,isApprovedlist);
    }

  }

  private Element displayToolBar (Document doc, Toolbar mytoolbar)
  {

    Element ToolBarElement = doc.createElement("toolbar");
    String userKey = vr.getUserKeyColumnByPortalVersions (staticData.getPerson());

    Element IconElement;
    try
    {

    // check if user is Topic Administrator, Will check UP_PERMISSION TABLE
    if (mytoolbar.checkPermission(staticData.getPerson(), CHANNEL, VIEW, "TOPICS_ICON"))

    {
      IconElement = doc.createElement("topicIcon");
      ToolBarElement.appendChild(IconElement);
    }

    // check if user is classifieds approver,
    // will need to check two things 1) user has approver rights 2) there are documents that need approvals
    IconElement = mytoolbar.displayIcon ("approverIcon", staticData.getPerson(), classifiedsDB.getAllApproverIDs(), doc);

    ArrayList itemsExist = null;
    if (IconElement != null)
     itemsExist = classifiedsDB.getNeedApprovedItemList(userKey);
    if (IconElement != null && itemsExist != null && !itemsExist.isEmpty())
      ToolBarElement.appendChild(IconElement);

    // display new classified icon (group = Everyone)
    // read group type from staticData
    IconElement = mytoolbar.displayIcon ("newClassifedIcon", doc);
    if (IconElement != null)
      ToolBarElement.appendChild(IconElement);

    // will always show the classifieds status link for admins
    boolean canViewAll =
      vr.getPrincipalByPortalVersions(staticData.getPerson()).
        hasPermission(CHANNEL, VIEW, "MY_CLASSIFIEDS_ICON");
    if (canViewAll) {
      IconElement = mytoolbar.displayIcon ("statusIcon", doc);
    } else {
      // display classifieds status (must hit DB and check)
      IconElement = mytoolbar.displayIcon ("statusIcon", staticData.getPerson(), classifiedsDB, doc);
    }

    if (IconElement != null)
      ToolBarElement.appendChild(IconElement);

   }catch (Exception e)
   {
     IconElement = doc.createElement("error");
     ToolBarElement.appendChild(IconElement);
   }
    return ToolBarElement;
  }

  private Document getMainScreen (Document doc, ChannelRuntimeData rd)
  {

    Element Main = doc.createElement("Main");

    Toolbar mytoolbar = new Toolbar ();
    // insert Toolbar
    Main.appendChild(displayToolBar (doc, mytoolbar));

//    Main.appendChild(get) writeMainUserRole(w);

    ArrayList topicsToDisplay = classifiedsDB.getAllTopicList("A");

    if (topicsToDisplay.isEmpty())
    {
      Element Error = doc.createElement("Empty");
      Main.appendChild(Error);
//      w.write("<needWarning>true</needWarning>");
//      w.write("<warning>There are no Classifieds Topics listed!</warning>");
    }
    else
    {
      Element TopicList = doc.createElement("topic_list");
      for (int i=0; i < topicsToDisplay.size(); i++)
      {
        Element Topic = doc.createElement("topic");
        Topic.setAttribute("image", ((TopicInfo) topicsToDisplay.get(i)).getImageName());
        Topic.setAttribute("mime_type", ((TopicInfo) topicsToDisplay.get(i)).getImageType());
        Topic.setAttribute("id", ((TopicInfo) topicsToDisplay.get(i)).getTopicID());
        Topic.setAttribute("name", ((TopicInfo) topicsToDisplay.get(i)).getName());
        Topic.setAttribute("count", ((TopicInfo) topicsToDisplay.get(i)).getTotalEntry());
        TopicList.appendChild(Topic);
      }
      Main.appendChild(TopicList);
    }

    doc.appendChild(Main);
    xslFile = "main";
    return doc;

  }

  private boolean hasStatus()
  {
    Map countlist=new HashMap();
    countlist=getUserStatusCount();
    boolean status=true;
    if (countlist.isEmpty())
    {
      status=false;
    }
    return status;
  }

  private void writeMainUserRole(StringWriter w)
  {
    StringBuffer sBuff = w.getBuffer();
//    System.out.println("hasApprovalMsg="+hasApprovalMsg);
    sBuff.append("<hasApvdMsg>").append(hasApprovalMsg).append("</hasApvdMsg>");
    sBuff.append("<hasStatus>").append(hasStatus).append("</hasStatus>");
    sBuff.append("<isAdministration>").append(isAdminRole).append("</isAdministration>");
    sBuff.append("<isApproval>").append(isApprovalRole).append("</isApproval>");

  }

  private Map getUserStatusCount()
  {
    java.util.ArrayList status=getStatusName();
    Map countList =new HashMap();
    Iterator it = status.iterator();
    while(it.hasNext())
    {
      String s = (String)it.next();
      String count = classifiedsDB.getUserStatusCount(personID, s);
      if((count!=null)&&(Integer.parseInt(count)>0))
        countList.put(s, count);
    }
    return countList;
  }
  private java.util.ArrayList getStatusName()
  {
    java.util.ArrayList status=new java.util.ArrayList();
    status.add(APPROVED);
    status.add(DENIED);
    status.add(PENDING);
    return status;
  }
  private void writeMyClassifiedStatus(StringWriter w)
  {
    Map countlist=new HashMap();
    countlist=getUserStatusCount();
    java.util.ArrayList status=getStatusName();
    Iterator it = status.iterator();
    if (countlist.isEmpty())
    {
      w.write("<isempty>true</isempty>");
      w.write("<emptyMessage>There is no items for you, please create one!</emptyMessage>");
    }
    else
    {
      while(it.hasNext())
      {
        String name=(String)it.next();
        String count=(String)countlist.get(name);
        String action=null;
        if ((count!=null)&&(Integer.parseInt(count)>0))
        {
          w.write("<status>");
          if (name.equals("A"))
          {
            name="Approved";
            action="publish";
          }
          else if (name.equals("D"))
          {
            name="Denied";
            action="denied";
          }
          else if (name.equals("P"))
          {
            name="Pending";
            action="pending";
          }
          w.write("<name>"+name+"</name>");
          w.write("<action>"+action+"</action>");
          w.write("<total>"+count+"</total>");
          w.write("</status>");
        }
      }
    }
  }

  private Document getItemDetails (Document doc, ChannelRuntimeData rd)
  {
    Element Item = doc.createElement("item_details");
    String itemID = rd.getParameter("itemID");

    if (rd.getParameter("next") != null)
    {
      il.next();
      itemID = il.getItemID();
    } else if (rd.getParameter("prev") != null)
    {
      il.previous();
      itemID = il.getItemID();
    }

    // more than one exists, create page navigational bar

    // need information on topic

    Item.setAttribute("name", topicDisplayed.getName());
    Item.setAttribute("image", topicDisplayed.getImageName());
    Item.setAttribute("mime_type", topicDisplayed.getImageType());

    Item.setAttribute("content", il.getContent(itemID));
    Item.setAttribute("topicID", il.getTopicID(itemID));
    Item.setAttribute("cost", il.getCost(itemID));
    Item.setAttribute("email", il.getEmail(itemID));
    Item.setAttribute("phone", il.getPhone(itemID));
    Item.setAttribute("contact", il.getContact(itemID));
    Item.setAttribute("date", getDate(il.getExpireDate(itemID)));
    Item.setAttribute("itemID", itemID);

    Item.setAttribute("imagecontenttype",  il.getContentType(itemID));

    if (il.getImage(itemID) != null)
      Item.setAttribute("hasimage", "yes");
    else
      Item.setAttribute("hasimage", "no");


    // need to set the currentIndex for item being viewed
    il.setCurrentIndex(itemID);
    // now check if there should be a next|prev link
    if (il.getCount() > 1)
    {
      if (il.getCurrentIndex() != 0)
        Item.setAttribute("prev", "prev");
      if (il.getCurrentIndex() < (il.getCount() - 1))
        Item.setAttribute("next", "next");
    }

    doc.appendChild(Item);
    xslFile = "vdetails";
    return doc;


  }

  private Document getMyClassifiedsDetails (Document doc, ChannelRuntimeData rd)
  {
    Element Item = doc.createElement("item_details");
    String itemID = rd.getParameter("itemID");
    il = classifiedsDB.getItem(itemID, rd.getParameter("name"), rd.getParameter("type"));

    Item.setAttribute("name", rd.getParameter("name"));
    Item.setAttribute("content", il.getContent(itemID));
    Item.setAttribute("topicID", il.getTopicID(itemID));
    Item.setAttribute("cost", il.getCost(itemID));
    Item.setAttribute("email", il.getEmail(itemID));
    Item.setAttribute("phone", il.getPhone(itemID));
    Item.setAttribute("contact", il.getContact(itemID));
    Item.setAttribute("date", getDate(il.getExpireDate(itemID)));
    Item.setAttribute("itemID", itemID);
    Item.setAttribute("type", rd.getParameter("type"));
    Item.setAttribute("daysleft", getDaysTillExpire(il.getExpireDate(itemID)));
    if (il.getMsgToAuth(itemID) != null)
      Item.setAttribute("messagetoauthor", il.getMsgToAuth(itemID));
    Item.setAttribute("imagecontenttype",  il.getContentType(itemID));

    if (il.getImage(itemID) != null)
      Item.setAttribute("image", "yes");
    else
      Item.setAttribute("image", "no");

    doc.appendChild(Item);
    xslFile = "vdetails";
    return doc;
  }

  private Document getItemsForTopic (Document doc, ChannelRuntimeData rd)
  {
    il = classifiedsDB.getItems(rd.getParameter("topicID"), "A");
    il.begin();
    Element TopicItems = doc.createElement("topic_items");
    // need information on topic
    try{
      topicDisplayed = classifiedsDB.getTopicInfo(rd.getParameter("topicID"));
    }catch (SQLException sql){}
    TopicItems.setAttribute("total", Integer.toString(il.getCount()));
    TopicItems.setAttribute("name", topicDisplayed.getName());
    TopicItems.setAttribute("image", topicDisplayed.getImageName());
    TopicItems.setAttribute("mime_type", topicDisplayed.getImageType());

    for (int i=0; i < il.getCount(); i++)
    {

      il.next();

      Element Item = doc.createElement("item");
      Item.setAttribute("total", Integer.toString(il.getCount()));
      Item.setAttribute("content", il.getContent());
      Item.setAttribute("itemID", il.getItemID());
      Item.setAttribute("topicID", il.getTopicID());
      Item.setAttribute("cost", il.getCost());
      Item.setAttribute("email", il.getEmail());
      Item.setAttribute("phone", il.getPhone());
      Item.setAttribute("image", (il.getImage() != null ? "yes" : "no"));
      Item.setAttribute("imagecontenttype",  il.getContentType());
      Item.setAttribute("contact", il.getContact());
      // classified date posted
      // will subtract 29 days from expire date
      Item.setAttribute("date", getDate(il.getExpireDate()));

      TopicItems.appendChild(Item);
    }

    xslFile = "detail";
    doc.appendChild(TopicItems);
    return doc;

  }

  private void writePubItem(StringWriter w,int beginIndex,int endIndex)
  {
    for (int i=beginIndex-1; i<endIndex; i++)
    {
      String itemID=(String)m_currentPubItemInATopic.itemIDList.get(i);
      String content=m_currentPubItemInATopic.getContent(itemID);
      String cost=m_currentPubItemInATopic.getCost(itemID);
      String phone=m_currentPubItemInATopic.getPhone(itemID);
      String email=m_currentPubItemInATopic.getEmail(itemID);
      String message=null;
      if ((content!=null)&&!(content.equals("")))
        message=content;
      if ((cost!=null)&&!(cost.equals("")))
        message=message+" Cost is "+cost;
      if ((phone!=null)&&!(phone.equals("")))
        message=message+". Please call "+phone;
      if((email!=null)&&!(email.equals("")))
      {
        if ((phone!=null)&&!(phone.equals("")))
          message=message+". Please email to "+email;
        else
          message=message+" or email to "+email;
      }
      w.write("<message>");
      w.write("<content>"+xmlescaper.escape(message)+"</content>");
      w.write("</message>");
    }
  }

  private ItemList getItemsForTopicID(boolean isItemPublished)
  {
    String userKey = vr.getUserKeyColumnByPortalVersions (staticData.getPerson());

    java.util.ArrayList itemlist=new java.util.ArrayList();
//    System.out.println("in getItemsForTOpicID method isPublished = "+isItemPublished);
    if (isItemPublished)
    {
      itemlist=classifiedsDB.getPublishedItemList();
//      System.out.println("in getItemsForTOpicID method  publishedItemList is "+itemlist.size());
    }
    else
    {
      itemlist=classifiedsDB.getNeedApprovedItemList(userKey);
    }
    Iterator it = itemlist.iterator();
    ItemList itemList=new ItemList();
    while (it.hasNext())
    {
      ItemList temp=(ItemList)it.next();
      temp.begin();
      temp.next();
      if (temp.getTopicID().equals(m_currentTopicID))
      {
        itemList=temp;
        m_currentTopicName=itemList.getTopicName();
      }
    }
    return itemList;
  }

  private Document getDeleteTopicErrorScreen (Document doc) throws PortalException
  {

    Element ErrorElement = doc.createElement("DeleteError");

/*    Element TopicNameElement = doc.createElement ("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    SaveElement.appendChild(TopicNameElement);
*/
    Element ErrorFoundElement = doc.createElement ("error");
    ErrorFoundElement.appendChild(doc.createTextNode((String)sError.get(0)));
    ErrorElement.appendChild(ErrorFoundElement);
    sError.clear();

    doc.appendChild(ErrorElement);
    xslFile = "deleteTopicError";
    return doc;
  }

  private Document getConfirmDeleteItemScreen (Document doc, ChannelRuntimeData rd)
  {
    String itemID = rd.getParameter("itemID");
    String checkBox = rd.getParameter("checkbox");

    Element DeleteItemElement = doc.createElement("DeleteItem");

    // user has selected a single itemID to delete
    if (itemID != null)
    {
      Element ItemElement = doc.createElement("DeleteItem");
      String ItemContent = classifiedsDB.getItemContent(itemID);
      ItemElement.appendChild(doc.createTextNode(ItemContent));
      DeleteItemElement.appendChild(ItemElement);
      ArrayList delete = new ArrayList ();
      delete.add(itemID);
      myTopicInfo.setDeleteList(delete);
    }
    else
    {  // user has selected to delete multiple topics
       // parse through all checkboxes
      Enumeration keys = rd.keys();

      Element DeleteList = doc.createElement("DeleteList");

      boolean foundChecked = false;
      ArrayList delete = new ArrayList();
      while (keys.hasMoreElements())
      {
        String paramKey = (String) keys.nextElement();
        String value = (String) rd.getObjectParameter(paramKey);

        if (value.equals("checked"))
        {
          Element DeleteItemID = doc.createElement("itemID");
          String ItemContent = classifiedsDB.getItemContent(paramKey);
          DeleteItemID.setAttribute("content", ItemContent);
          DeleteList.appendChild(DeleteItemID);

          delete.add(paramKey);
          foundChecked = true;
        }
      }

      if (!foundChecked)
      { // user has clicked delete all checked, but didnt check any items
        // will display friendly error screen
          Element ErrorElement = doc.createElement("NoItemsChecked");
          ErrorElement.appendChild(doc.createTextNode("No items checked to delete."));
          DeleteList.appendChild(ErrorElement);

      }
      else
      {
          myTopicInfo.setDeleteList(delete);
      }
     DeleteItemElement.appendChild(DeleteList);
   }


        doc.appendChild(DeleteItemElement);
       /* org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
*/
        xslFile = "deleteItem";

          return doc;
  }

  private Document getConfirmDeleteTopicScreen (Document doc, ChannelRuntimeData rd)
  {
    String topicID = rd.getParameter("topicID");
    String checkBox = rd.getParameter("checkbox");

    Element DeleteTopicElement = doc.createElement("DeleteTopic");

    // user has selected a single topic to delete
    if (topicID != null)
    {
      Element ItemElement = doc.createElement("DeleteItem");
      String topicName = (String) topicNames.get(topicID);
      ItemElement.appendChild(doc.createTextNode(topicName));
      DeleteTopicElement.appendChild(ItemElement);
      ArrayList delete = new ArrayList ();
      delete.add(topicID);
      myTopicInfo.setDeleteList(delete);
    }
    else
    {  // user has selected to delete multiple topics
       // parse through all checkboxes
      Enumeration keys = rd.keys();

      Element DeleteList = doc.createElement("DeleteList");

      boolean foundChecked = false;
      ArrayList delete = new ArrayList();
      while (keys.hasMoreElements())
      {
        String paramKey = (String) keys.nextElement();
        String value = (String) rd.getObjectParameter(paramKey);

        if (value.equals("checked"))
        {
          Element DeleteTopicID = doc.createElement("topicID");
          String topicName = (String) topicNames.get(paramKey);
          DeleteTopicID.setAttribute("name", topicName);
          DeleteList.appendChild(DeleteTopicID);

          delete.add(paramKey);
          foundChecked = true;
        }
      }

      if (!foundChecked)
      { // user has clicked delete all checked, but didnt check any items
        // will display friendly error screen
          Element ErrorElement = doc.createElement("NoItemsChecked");
          ErrorElement.appendChild(doc.createTextNode("No items checked to delete."));
          DeleteList.appendChild(ErrorElement);

      }
      else
      {
          myTopicInfo.setDeleteList(delete);
      }
     DeleteTopicElement.appendChild(DeleteList);
   }


        doc.appendChild(DeleteTopicElement);
/**        org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
**/
        xslFile = "deleteTopic";

          return doc;
  }

  private void getContentList(StringWriter w,int count,String pageIndex)
  {
    int total=0;
    if (count % 10 > 0)
      total=count/10+1;
    else
      total=count/10;
    int index=Integer.parseInt(pageIndex);
    int beginIndex=0;
    int endIndex=0;
    if (total==index)
    {
      if (total==1)   // count is less than 10
      {
        beginIndex=1;
        endIndex=count;
        w.write("<beginIndex>");
        w.write(Integer.toString(beginIndex));  // the first item
        w.write("</beginIndex>");
        w.write("<endIndex>");
        w.write(Integer.toString(endIndex));
        w.write("</endIndex>");
        w.write("<hasPrevious>false</hasPrevious>");
        w.write("<hasNext>false</hasNext>");
      }
      else   // last page
      {
        beginIndex=(index-1)*10+1;
        endIndex=count;
        w.write("<beginIndex>");
        w.write(xmlescaper.escape(Integer.toString(beginIndex)));
        w.write("</beginIndex>");
        w.write("<endIndex>");
        w.write(xmlescaper.escape(Integer.toString(endIndex)));
        w.write("</endIndex>");
        w.write("<hasPrevious>true</hasPrevious>");
        w.write("<hasNext>false</hasNext>");
      }
    }
    else
    {
      beginIndex=(index-1)*10+1;
      endIndex=index*10;
      w.write("<beginIndex>");
      w.write(xmlescaper.escape(Integer.toString(beginIndex)));
      w.write("</beginIndex>");
      w.write("<endIndex>");
      w.write(xmlescaper.escape(Integer.toString(endIndex)));
      w.write("</endIndex>");
      if (index==1)
        w.write("<hasPrevious>false</hasPrevious>");
      else
        w.write("<hasPrevious>true</hasPrevious>");
      w.write("<hasNext>true</hasNext>");
    }
    writePubItem(w,beginIndex,endIndex);
  }


  private void writeNewItemPage(StringWriter w)
  {
    writeTopicDropTable(w, null);
  }

  private Document getNewItemScreen (Document doc, ChannelRuntimeData rd)
  {
    Element NewItem = doc.createElement("NewItem");

    NewItem.appendChild(listTopics(doc));

    Element Message = doc.createElement("message");

    if (sError.contains("MSG"))
    {
      Message.setAttribute("error","noMsg");
      sError.remove("MSG");
    }
    if (sError.contains("OVERSIZE"))
    {
        Message.setAttribute("error", "oversize");
        sError.remove("OVERSIZE");
    }
    Message.appendChild(doc.createTextNode((newItemInfo.getItemContent() != null ? newItemInfo.getItemContent() : "")));
    NewItem.appendChild(Message);

    Element Cost = doc.createElement("cost");
    Cost.appendChild(doc.createTextNode((newItemInfo.getCost() != null ? newItemInfo.getCost() : "")));
    NewItem.appendChild(Cost);

    if (sError.contains("FILE"))
    {
    	Element Image = doc.createElement("image");
    	Image.setAttribute("error","filetoobig");
    	sError.remove("FILE");      
    	NewItem.appendChild(Image);
    }

    Element Name = doc.createElement("name");
    Name.appendChild(doc.createTextNode((newItemInfo.getContactName() != null ? newItemInfo.getContactName() : "")));
    NewItem.appendChild(Name);

    Element Phone = doc.createElement("phone");

    if (sError.contains("PHONE"))
    {
      Phone.setAttribute("error","error");
      sError.remove("PHONE");
    }
    Phone.appendChild(doc.createTextNode((newItemInfo.getPhone() != null ? newItemInfo.getPhone() : "")));
    NewItem.appendChild(Phone);    

    Element Email = doc.createElement("email");
    Email.appendChild(doc.createTextNode((newItemInfo.getEmail() != null ? newItemInfo.getEmail() : "")));
    NewItem.appendChild(Email);

    doc.appendChild(NewItem);
    xslFile = "new";
    return doc;
  }

  private Document getApproverScreen (Document doc, ChannelRuntimeData rd)
  {
    Element Approver = doc.createElement("Approver");

    Approver.appendChild(getTopicsForApproval(doc));
    doc.appendChild(Approver);
    xslFile = "Approver";
    return doc;
  }

  //added by jing
  private boolean checkIconSize (MultipartDataSource fileSource)
  {
      int filesize;
      try
      {
          InputStream ins = fileSource.getInputStream ();
          filesize = ins.available();

          if (filesize > 40960)
          {
              sError.add("ICONSIZE");
              return false;
          }
          return true;
      }
      catch (Exception e)
      {
      }
      return false;
  }
  private void saveTopicIcon (ChannelRuntimeData rd)
  {
    if (rd.getObjectParameterValues("icon_image") != null)
    {
      MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("icon_image"))[0];
      //added by Jing
      //check if icon size is less than 2kb
      if (checkIconSize (fileSource))
      {
          // store image to disk
          storeImageToDisk ("icon_library", fileSource);
          // store image references to database
          classifiedsDB.createIcon(fileSource.getName(), fileSource.getContentType());
      }
    }
  }

  private Document getPreviewTopicScreen (Document doc, ChannelRuntimeData rd)
  {
    Element Preview = doc.createElement("PreviewTopic");

    // adding an image tag for topic icon image
    // check if user selected a radio option, else setImageName = default

    Element ImageElement = doc.createElement("image");
    ImageElement.appendChild(doc.createTextNode(myTopicInfo.getImageName()));
    Preview.appendChild(ImageElement);

    Element MimeElement = doc.createElement("mime_type");
    MimeElement.appendChild(doc.createTextNode(myTopicInfo.getImageType()));
    Preview.appendChild(MimeElement);

    Element TopicElement = doc.createElement("topic");
    TopicElement.appendChild(doc.createTextNode(myTopicInfo.getName()));
    Preview.appendChild(TopicElement);

    Element DescriptionElement = doc.createElement("description");
    DescriptionElement.appendChild(doc.createTextNode(myTopicInfo.getDescription() != null ? myTopicInfo.getDescription() : "&#160;"));
    Preview.appendChild(DescriptionElement);

    try {
      // get all the selected groups/person's
      Preview.appendChild(getSelectedGroupsXML(rd, doc));
    }catch (GroupsException ge)
    {
     LogService.instance().log (LogService.ERROR, "NewsAdmin:getSelectedGroupsXML threw GroupsException:"+ge);
    }

    doc.appendChild(Preview);
    xslFile = "previewTopic";
    return doc;
  }

  private boolean deleteTopic ()
  {
    int deleted = 0;
    // get ArrayList of Delete Topic ID's
    try
    {
      if (myTopicInfo.getDeleteList().size() == 1)
      {
        deleted = classifiedsDB.deleteTopic((String)myTopicInfo.getDeleteList().get(0));
      }
      else
      {  // user has requested to delete multiple Topics

        deleted = classifiedsDB.deleteTopic(myTopicInfo.getDeleteList());
      }
    }
    catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:deleteTopic threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(deleted == 0)
      return false;

    // clear out topic entry inside of classifieds_topic_approver table
    try
    {
      if (myTopicInfo.getDeleteList().size() == 1)
      {
        classifiedsDB.removeTopicGroupIDs ((String)myTopicInfo.getDeleteList().get(0));
      }
      else
      {  // user has requested to delete multiple Topics
        for (int i=0; i < myTopicInfo.getDeleteList().size(); i++)
          classifiedsDB.removeTopicGroupIDs ((String)myTopicInfo.getDeleteList().get(i));

      }
    }catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:removeTopicGroupIDs threw SQLException:"+sqe);
    }

    // clear out hashtable and reconstruct it
    //listTopics ();
    return true;
  }

  private Element getSelectedGroupsXML (ChannelRuntimeData rd, Document doc) throws GroupsException
  {

    ArrayList mySelection = new ArrayList ();
    Element GroupsList = doc.createElement("GroupsList");

    // User has hit back because an error has occurred during saving topic
    if (rd.getParameter("action").equals("SaveTopicError"))
    {
      // in this case we will simply keep the existing GroupSelection
      for (int i=0; i < myTopicInfo.getSelectedGroups().size(); i++)
      {
        String item = (String) myTopicInfo.getSelectedGroups().get(i);
        int sepIndex = item.indexOf("-");
        String entity = item.substring(sepIndex+1);
        String id = item.substring(0, sepIndex);

        Element Group = doc.createElement("group");
        Group.setAttribute("name", gs.getReadableName(gs.checkIfGroupOrPerson(entity), id));
        Group.setAttribute("entity", entity);
        GroupsList.appendChild(Group);
      }
      return GroupsList;
    }
    // must loop through topic because user can select multiple topics to subscribe

    for (int i=0; i < myTopicInfo.getSelectedGroups().size(); i++)
    {
      String paramKey = (String) myTopicInfo.getSelectedGroups().get(i);
      int sepIndex = paramKey.indexOf("-");
      String entity = paramKey.substring(sepIndex+1);
      String id = paramKey.substring(0, sepIndex);

      Element Group = doc.createElement("group");
      Group.setAttribute("name", gs.getReadableName(gs.checkIfGroupOrPerson(entity), id));
      Group.setAttribute("entity", entity);
      GroupsList.appendChild(Group);
    }
    return GroupsList;
  }

  private void setSelectedGroups (ChannelRuntimeData rd) throws GroupsException
  {

    ArrayList mySelection = new ArrayList ();
    // must loop through topic because user can select multiple topics to subscribe
    Enumeration keys = rd.keys();
    while (keys.hasMoreElements())
    {
      String paramKey = (String) keys.nextElement();
      String value = (String) rd.getObjectParameter(paramKey);

      if (value.equals("checked"))
        mySelection.add(paramKey);
    }
    myTopicInfo.setSelectedGroups(mySelection);
  }

  private void writeTopicDropTable(StringWriter w,String topicid)
  {
    java.util.ArrayList apvdTopicList = classifiedsDB.getCurrentTopicList();
    Iterator it = apvdTopicList.iterator();
    while (it.hasNext())
    {
      w.write("<topic>");
      TopicInfo eachInfo=(TopicInfo)it.next();
      w.write("<name>"+xmlescaper.escape(eachInfo.getName())+"</name>");
      w.write("<id>"+xmlescaper.escape(eachInfo.getTopicID())+"</id>");
      if (eachInfo.getTopicID().equals(topicid))  //default topic
        w.write("<select>true</select>");
      else
        w.write("<select>false</select>");
      w.write("</topic>");
    }
  }

  private void listTopics()
  {
    // remove all elements
    topicNames.clear();

    ArrayList topicList = classifiedsDB.getAllTopic();

    for (int i=0; i < topicList.size(); i++)
    {
      String sTopicName = ((TopicInfo) topicList.get(i)).getName();
      String sTopicID = ((TopicInfo) topicList.get(i)).getTopicID();
      topicNames.put(sTopicID, sTopicName);
    }

  }

  private Element listTopics (Document doc)
  {
    ArrayList topicList = classifiedsDB.getCurrentTopicList();

    Element List = doc.createElement("topic_list");

    if (topicList.isEmpty())
    {
      Element EmptyElement = doc.createElement("Empty");
      List.appendChild(EmptyElement);
    }

    for (int i=0; i < topicList.size(); i++)
    {
      TopicInfo topicInfo = (TopicInfo) topicList.get(i);
      Element OptionElement = doc.createElement("option");
      OptionElement.setAttribute("value", topicInfo.getTopicID());
      if (newItemInfo.getTopicID() != null && newItemInfo.getTopicID().equals(topicInfo.getTopicID()))
        OptionElement.setAttribute("selected", "yes");
      else
        OptionElement.setAttribute("selected", "no");
      OptionElement.appendChild(doc.createTextNode(topicInfo.getName()));
      List.appendChild(OptionElement);
    }
    return List;
  }

  private Element getTopicsForApproval (Document doc)
  {
    //ArrayList topicList = classifiedsDB.getNeedApvdTopicList();
      //get topics assigned to for approval
      //add by Jing Chen, 2/26/03
      String user_key = vr.getUserKeyColumnByPortalVersions (staticData.getPerson());
      ArrayList topicList = classifiedsDB.getNeedApvdTopicList (user_key);

    Element List = doc.createElement("topic_list");

    if (topicList.isEmpty())
    {
      Element EmptyElement = doc.createElement("Empty");
      List.appendChild(EmptyElement);
    }

    for (int i=0; i < topicList.size(); i++)
    {
      TopicInfo topicInfo = (TopicInfo) topicList.get(i);
      Element OptionElement = doc.createElement("option");
      OptionElement.setAttribute("value", topicInfo.getTopicID());
      OptionElement.setAttribute("selected", "no");
      OptionElement.setAttribute("count", topicInfo.getTotalEntry());
      OptionElement.setAttribute("image", topicInfo.getImageName());
      OptionElement.setAttribute("mime_type", topicInfo.getImageType());
      OptionElement.appendChild(doc.createTextNode(topicInfo.getName()));
      List.appendChild(OptionElement);
    }
    return List;
  }

  private boolean checkInput (ChannelRuntimeData rd, String type)
  {

    if (type.equals("newtopic"))
    {
      myTopicInfo.setName(rd.getParameter("newTopicName"));
      myTopicInfo.setDescription(rd.getParameter("description"));
      // clear out error list
      sError.clear();

      if (myTopicInfo.getName() == null || myTopicInfo.getName().trim().equals(""))
        sError.add ("TOPIC");

      if (!sError.isEmpty())
        return false;
    }
    else if (type.equals("newclassified"))
    {
      String topicid = rd.getParameter("topics");
      String msg = rd.getParameter("message");
      String phone = rd.getParameter("phone");
      // clear out error list
      sError.clear();

      if (topicid == null)
        sError.add ("TOPIC");
      if (msg == null || msg.trim().equals(""))
        sError.add ("MSG");
      /*if (phone == null || phone.trim().equals(""))
        sError.add ("PHONE");*/
      if (msg != null && msg.length() > 2500)
          sError.add ("OVERSIZE");
      
      // check if the file is too big
      if (rd.getObjectParameterValues("classified_image") != null)
      {
      	MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("classified_image"))[0];
        try{
	        if(fileSource.getInputStream().available() > FILE_SIZE){
	        	sError.add("FILE");
	        }
        }catch(Exception e){
        	LogService.log(LogService.ERROR, e);
        }
		
      }

      if (!sError.isEmpty())
        return false;
    }
    else if (type.equals("approval"))
    {
// TT03446 - Don't require approval or denial, allow user to skip over an item...
/*
      String choice = rd.getParameter("choice");
      if (choice == null || choice.trim().equals(""))
        sError.add("CHOICE");

      if (!sError.isEmpty())
        return false;
*/
    }
    else if (type.equals("icon"))
    {
      String icon = rd.getParameter("icon");
      if (icon == null || icon.trim().equals(""))
        sError.add("ICON");

      if (!sError.isEmpty())
        return false;

      // must parse value because it is in image.gif:image/gif format
      int sepIndex = icon.indexOf(":");
      String mimeType = icon.substring(sepIndex+1);
      String image = icon.substring(0, sepIndex);
      myTopicInfo.setImageName(image);
      myTopicInfo.setImageType(mimeType);
    }
    
    return true;
  }

  private Document getPreviewNewItemScreen (Document doc, ChannelRuntimeData rd)
  {
    Element Preview = doc.createElement("Preview");

//   newItemInfo = new ItemInfo();
    String topicid = rd.getParameter("topics");
    String msg = rd.getParameter("message");
    String cost = rd.getParameter("cost");
    String phone = rd.getParameter("phone");
    String email = rd.getParameter("email");

    newItemInfo.setTopicID(topicid);
    newItemInfo.setAuthorID(Integer.toString(staticData.getPerson().getID()));
    newItemInfo.setItemContent(msg);
    newItemInfo.setCost(cost);
    newItemInfo.setPhone(phone);
    newItemInfo.setEmail(email);

    Element NewItem = doc.createElement("NewItem");
    NewItem.setAttribute("topic", topicid);
    NewItem.setAttribute("message", msg);
    NewItem.setAttribute("cost", cost);
    NewItem.setAttribute("phone", phone);
    NewItem.setAttribute("email", email);

    // Freddy added
    String contact_name = rd.getParameter("contact_name");
    newItemInfo.setContactName(contact_name);
    NewItem.setAttribute("contact", contact_name);

    // need to get next itemID value and set it
    if (newItemInfo.getItemID() == null)
      newItemInfo.setItemID(classifiedsDB.getNextID());

    NewItem.setAttribute("itemID", newItemInfo.getItemID());

    // if user has entered an image, then get it
    if (rd.getObjectParameterValues("classified_image") != null)
    {

      MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("classified_image"))[0];

      newItemInfo.setItemImage(fileSource.getName());
      newItemInfo.setImageMimeType(fileSource.getContentType());
      NewItem.setAttribute("image", fileSource.getName());
      String userID = Integer.toString(staticData.getPerson().getID());
      storeImageToDisk (userID, fileSource);

     try
     {
      newItemInfo.setImageInputStream(fileSource.getInputStream());
     }
     catch (IOException ioe)
     {
       System.out.println("IOException ioe ==== "+ioe+"\n");
     }
    }
    else if (newItemInfo.getItemImage() != null)
    {
      // user has hit back and has not modified the image
      NewItem.setAttribute("image", newItemInfo.getItemImage());
    }

    Preview.appendChild(NewItem);
    doc.appendChild(Preview);
    xslFile = "previewNew";
    return doc;
  }


  private void saveNewItem ()
  {
    int saved;
    try
    {
      saved = classifiedsDB.createItem(newItemInfo);

    }
    catch (SQLException sqe)
    {
    System.out.println("SQLException has occurred ==== "+sqe+"\n");
    sError.add(sqe.toString());
    }
  }


  private void writeTopicList(StringWriter w,boolean isApprovedList)
  {
    String userKey = vr.getUserKeyColumnByPortalVersions (staticData.getPerson());
    java.util.ArrayList topicList = null;
    String APPROVED ="A";
    if (isApprovedList)
    {
      topicList=classifiedsDB.getAllTopicList(APPROVED);    // getApproved list
      if ((topicList !=null)&&(topicList.size()>0))
      {
        writeEmptyTopic(w,topicList);
      }
      else
      {
        w.write("<needWarning>true</needWarning>");
        w.write("<warning>There is no published Classifieds item.</warning>");
      }
    }
    else
    {
      topicList = classifiedsDB.getNeedApvdTopicList(userKey);  // get need Approved list
      writeEmptyTopic(w,topicList);
    }
  }

  private Element getTopicList (Document doc)
  {

    Element TopicList = doc.createElement("topic_list");

    //java.util.ArrayList topicList = null;
   // String APPROVED ="A";
   // if (isApprovedList)
   // {
      ArrayList topicList = classifiedsDB.getAllTopicList("A");
      for (int i=0; i < topicList.size(); i++)
      {
        Element Topic = doc.createElement("topic");
        Topic.setAttribute("image", ((TopicInfo) topicList.get(i)).getImageName());
        Topic.setAttribute("mime_type", ((TopicInfo) topicList.get(i)).getImageType());
        Topic.setAttribute("id", ((TopicInfo) topicList.get(i)).getTopicID());
        Topic.setAttribute("name", ((TopicInfo) topicList.get(i)).getName());
        Topic.setAttribute("count", ((TopicInfo) topicList.get(i)).getTotalEntry());
        TopicList.appendChild(Topic);
      }
/*      else
      {
        w.write("<needWarning>true</needWarning>");
        w.write("<warning>There is no published Classifieds item.</warning>");
      }
    }
    else
    {
      topicList = classifiedsDB.getNeedApvdTopicList();  // get need Approved list
      writeEmptyTopic(w,topicList);
    }
*/
    return TopicList;
  }

  private void writeTopicList(StringWriter w,java.util.ArrayList topiclist)
  {
    StringBuffer sBuff = w.getBuffer();
    Iterator it = topiclist.iterator();
    while (it.hasNext())
    {
      TopicInfo eachInfo=(TopicInfo)it.next();
      sBuff.append("<topic>");
//      sBuff.append("<smallIcon>").append(xmlescaper.escape(eachInfo.getSmallIcon())).append("</smallIcon>");
//      sBuff.append("<bigIcon>").append(xmlescaper.escape(eachInfo.getBigIcon())).append("</bigIcon>");
      sBuff.append("<name>").append(xmlescaper.escape(eachInfo.getName())).append("</name>");
      sBuff.append("<id>").append(xmlescaper.escape(eachInfo.getTopicID())).append("</id>");
      sBuff.append("<total>").append(xmlescaper.escape(eachInfo.getTotalEntry())).append("</total>");
      sBuff.append("<description>").append(xmlescaper.escape(eachInfo.getDescription())).append("</description>");
      sBuff.append("</topic>");
    }
  }


  private Document getMyClassifieds (Document doc, ChannelRuntimeData rd)
  throws PortalException
  {
    try {
    // get list of Items Pending Approval's for the topic requested
    Element ItemList = doc.createElement("Classifieds_List");

    String personID = Integer.toString(staticData.getPerson().getID());
    int total = 0;
    boolean canViewAll =
      vr.getPrincipalByPortalVersions(staticData.getPerson()).
        hasPermission(CHANNEL, VIEW, "MY_CLASSIFIEDS_ICON");

    // loop 3 times
    for (int k=0; k < 3; k++)
    {
      String approved = (k==0 ? "A" : (k==1 ? "P" : "D"));
      // get list of all topics user has article in (1) APPROVED (2) PENDING (3) DENIED
      ArrayList topicList = null;

      if (canViewAll) {
        topicList = classifiedsDB.getAllTopicList(approved);
      } else {
        topicList = classifiedsDB.getUserTopicList(personID, approved);
      }

      for (int i=0; i < topicList.size(); i++)
      {
        // found List of Topics, now loop through and get items
        TopicInfo topicFound = (TopicInfo) topicList.get(i);

        if (canViewAll) {
          il = classifiedsDB.getItems(topicFound.getTopicID(), approved);
        } else {
          il = classifiedsDB.getUserItem(
            personID, topicFound.getTopicID(), approved);
        }

        // will store ItemList object inside a global ArrayList
        myClassifiedsList.add (il);
        total += il.getCount();
        il.begin();
        il.next();
        while (il.hasMore())
        {
          Element Item = doc.createElement("Item");
          Item.setAttribute("type", approved);
          Item.setAttribute("topic", il.getTopicName());
          Item.setAttribute("id", il.getTopicID());
          Item.setAttribute("content", il.getContent());
          Item.setAttribute("cost", il.getCost());
          Item.setAttribute("email", il.getEmail());
          Item.setAttribute("phone", il.getPhone());
          Item.setAttribute("itemID", il.getItemID());
          Item.setAttribute("image", (il.getImage() != null ? "yes" : "no"));
          Item.setAttribute("imagecontenttype", il.getContentType());
          Item.setAttribute("contact", il.getContact());

          Item.setAttribute("checked", checkIfChecked(rd.getParameter("checkbox")));
          Item.setAttribute("date", getDate(il.getExpireDate()));
          Item.setAttribute("daysleft", getDaysTillExpire(il.getExpireDate()));
          Item.setAttribute("messagetoauthor", (il.getMsgToAuth() != null ? "yes" : "no"));
          ItemList.appendChild(Item);
          il.next();
        }

      }

     }
    ItemList.setAttribute("total", Integer.toString(total));
    doc.appendChild(ItemList);
    xslFile = "myClassifieds";
    return doc;
    } catch (Exception ae) {
      LogService.log(LogService.ERROR, ae);
      throw new PortalException("Error getting My Classifieds", ae);
    }
  }


  private Document getItemsToApprove (Document doc, ChannelRuntimeData rd)
  {
//    writeTopicDropTable(w,m_currentTopicID);
    Element Item = doc.createElement("Item");
    // get list of Items Pending Approval's for the topic requested

    if (rd.getParameter("next") == null && rd.getParameter("finished") == null)
    {
      il = classifiedsDB.getItems(rd.getParameter("topicID"), "P");
      il.begin();
    }

    if (sError.contains("CHOICE"))
    {
      Item.setAttribute("error", "choice");
      sError.clear();
     // il.previous();
    }
    else
    { il.next();}


    Item.setAttribute("topic", il.getTopicName());
    Item.setAttribute("id", il.getTopicID());
    Item.setAttribute("total", Integer.toString(il.getCount()));
    Item.setAttribute("content", il.getContent());
    Item.setAttribute("cost", il.getCost());
    Item.setAttribute("email", il.getEmail());
    Item.setAttribute("phone", il.getPhone());
    Item.setAttribute("itemID", il.getItemID());
    Item.setAttribute("image", (il.getImage() != null ? "yes" : "no"));
    Item.setAttribute("imagecontenttype", il.getContentType());
    Item.setAttribute("contact", il.getContact());
    il.next();
    Item.setAttribute("hasnext", (il.hasMore() ? "yes" : "no"));
    il.previous();

    doc.appendChild(Item);
    xslFile = "ApproveItem";
    return doc;
  }

  private void saveApprovedOrDeniedItem(ChannelRuntimeData rd)
  {
    String sChoice = rd.getParameter("choice");
//    String topicid=runtimeData.getParameter("topics");
    String isApproved=null;
    if (sChoice.equals("approved"))
      isApproved=APPROVED;
    else
      isApproved=DENIED;

    String msg = rd.getParameter("messageToAuth");
    if (msg.equals(""))
      msg=null;

    String itemid = rd.getParameter("itemID");
    String userID = Integer.toString(staticData.getPerson().getID());
    classifiedsDB.approvedOrDenyItem(il.getItemID(), isApproved, msg, userID, il.getTopicID());
  }

  private void writeUserPubItemsForEachTopics(StringWriter w,String topicid)
  {
    writeUserTopicBoxAllTopics(w,topicid);
    java.util.ArrayList topiclist = classifiedsDB.getUserTopicList(personID,APPROVED);
    writeUserTopicDropBox(w,topicid,topiclist);
    //ItemList itemlist = classifiedsDB.getIndPublishedItem(personID,topicid);
    if (m_currentUserPublishedItem.getCount()>0)
    {
      writeItems(w,m_currentUserPublishedItem);
    }
    else
    {
      w.write("<isEmpty>true</isEmpty>");
      w.write("<emptyTopic>There is no item in this topic.</emptyTopic>");
    }
  }
  private void writeUserDeniedItemsForEachTopics(StringWriter w,String topicid)
  {
    writeUserTopicBoxAllTopics(w,topicid);
    java.util.ArrayList topiclist = classifiedsDB.getUserTopicList(personID,DENIED);
    writeUserTopicDropBox(w,topicid,topiclist);
    if(m_currentUserDeniedItem.getCount()>0)
    {
      writeItems(w,m_currentUserDeniedItem);
    }
    else
    {
      w.write("<isEmpty>true</isEmpty>");
      w.write("<emptyTopic>There is no item in this topic.</emptyTopic>");
    }
  }
  private void writeUserPendingItemsForEachTopics(StringWriter w,String topicid)
  {
    writeUserTopicBoxAllTopics(w,topicid);
    java.util.ArrayList topiclist = classifiedsDB.getUserTopicList(personID,PENDING);
    writeUserTopicDropBox(w,topicid,topiclist);
    if (m_currentUserPendingItem.getCount()>0)
    {
      writeItems(w,m_currentUserPendingItem);
    }
    else
    {
      w.write("<isEmpty>true</isEmpty>");
      w.write("<emptyTopic>There is no item in this topic.</emptyTopic>");
    }
  }
  private void writeItems(StringWriter w,ItemList itemlist)
  {
    itemlist.begin();
    itemlist.next();
    try
    {
      while (itemlist.hasMore())
      {
        SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yy HH:mm");
        Timestamp ts=Timestamp.valueOf(itemlist.getExpireDate());
        String date=sdf.format(ts);
        w.write("<item>");
        String cost=itemlist.getCost();
        String phone=itemlist.getPhone();
        String email=itemlist.getEmail();
        String msg=itemlist.getContent();
        if (msg!=null)
        {
          if ((cost!=null)&&(!cost.equals("")))
          {
            msg=msg+" Cost is"+cost+".";
          }
          if ((phone!=null)&&(!phone.equals("")))
          {
            msg=msg+" Please call "+phone;
          }
          if ((email!=null)&&(!email.equals("")))
          {
            if ((phone!=null)&&(!phone.equals("")))
              msg=msg+" or email to "+email+".";
            else
              msg=msg+" Please email to "+email+".";
          }
        }
        w.write("<content>"+xmlescaper.escape(msg)+"</content>");
        w.write("<itemid>"+xmlescaper.escape(itemlist.getItemID())+"</itemid>");
        w.write("<expireDate>"+date+"</expireDate>");
        w.write("<topicname>"+xmlescaper.escape(itemlist.getTopicName())+"</topicname>");
        w.write("</item>");
        itemlist.next();
      }
    }
    catch (Exception e)
    {
      LogService.instance().log(LogService.ERROR, e);
    }
  }
  private void writeUserTopicDropBox(StringWriter w,String topicid,java.util.ArrayList topiclist)
  {
    Iterator it = topiclist.iterator();
    while (it.hasNext())
    {
      w.write("<topic>");
      TopicInfo eachInfo=(TopicInfo)it.next();
      w.write("<name>"+xmlescaper.escape(eachInfo.getName())+"</name>");
      w.write("<id>"+xmlescaper.escape(eachInfo.getTopicID())+"</id>");
      if (eachInfo.getTopicID().equals(topicid))  //default topic
        w.write("<select>true</select>");
      else
        w.write("<select>false</select>");
      w.write("</topic>");
    }
  }
  private void writeUserTopicBoxAllTopics(StringWriter w, String topicid)
  {
    w.write("<topic>");
    w.write("<name>All Topics</name>");
    w.write("<id>-1</id>");
    if (topicid.equals("-1"))
      w.write("<select>true</select>");
    else
      w.write("<select>false</select>");
    w.write("</topic>");
  }

  private void writeDeleteItemList (StringWriter w,ItemList itemlist)
  {
    if(itemlist.getCount()>0)
    {
      writeItems(w,itemlist);
    }
    else
    {
      writeEmptyTopic(w,null);
    }

  }

   private void writeEmptyTopic(StringWriter w,java.util.ArrayList topicList)
  {
    if ((topicList!=null)&&(topicList.size()>0))
    {
      w.write("<isempty>");
      w.write("false");
      w.write("</isempty>");
      writeTopicList(w,topicList);
    }
    else
    {
      w.write("<isempty>");
      w.write("true");
      w.write("</isempty>");
      w.write("<emptyTopic>");
      if (m_action.equals("delete"))
        w.write("Please select an entry");
      else
        w.write("There is no item available.");
      w.write("</emptyTopic>");
    }
  }

  private boolean deleteItems()
  {
    int success = 0;
    //modified by Jing 5/8/03
    int itemCounts;
    try {
      itemCounts = myTopicInfo.getDeleteList().size();
      success = classifiedsDB.deleteItem(myTopicInfo.getDeleteList());
    }
    catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "Classifieds:deleteItem threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if (success == itemCounts)
      return true;
    return false;
  }

  private boolean deleteIcon(String iconID)
  {
    int success = 0;
    success = classifiedsDB.deleteIcon(iconID);
    if (success == 1)
      return true;
    return false;
  }

  private ItemList getUserDeleteItemList(ItemList itemList)
  {
    int total=0;
    ItemList currentItemList=new ItemList();
    total = itemList.getCount();
    currentItemList=itemList;
    ItemList itemlist=new ItemList();
    for (int i=1; i<=total; i++)
    {
      String itemid=runtimeData.getParameter("checkDelete_"+i);
      if (itemid!=null)
      {
        String content=currentItemList.getContent(itemid);
        String topicname=currentItemList.getTopicName(itemid);
        String topicid=currentItemList.getTopicID(itemid);
       // Timestamp expiredate=currentItemList.getExpireDate(itemid);
        String cost=currentItemList.getCost(itemid);
        String phone=currentItemList.getPhone(itemid);
        String email=currentItemList.getEmail(itemid);
        String msgtoauth=currentItemList.getMsgToAuth(itemid);

       // itemlist.addItem(itemid,content,expiredate,topicname,topicid,email,phone,cost,msgtoauth,null);
      }
    }
    return itemlist;
  }

  private String getEntryIndex()
  {
    String entry=runtimeData.getParameter("entry");
    int index=1;
    if (entry==null)
    {
      entry="1";      //first entry
    }
    else
    {
      int newEntry=Integer.parseInt(entry)+1;
      index=newEntry;
      entry=Integer.toString(newEntry);
    }
    return entry;
  }

/*  private void manageTopic(StringWriter w)
  {
    String desc=runtimeData.getParameter("description");
//    String active=runtimeData.getParameter("active");
//    String smallIcon=runtimeData.getParameter("smallIcon");
//    String bigIcon=runtimeData.getParameter("bigIcon");
    if (runtimeData.getParameter("create")!=null)
    {
      String topicname=runtimeData.getParameter("newTopicName");
      String topicid=null;
      if ((topicname==null)||(topicname.equals("")))
      {
        w.write("<needWarning>true</needWarning>");
        w.write("<warning>If you want to create a new topic, please fill up the New Topic Name column!</warning>");
        topicid=m_currentTopicID;
      }
      else
      {
        if(!(isTopicNameDuplicate(topicname)))
        {
          topicid = classifiedsDB.createTopic(topicname,desc);
        }
        else
        {
          w.write("<needWarning>true</needWarning>");
          w.write("<warning>The Topic name is duplicated, please try another one.</warning>");
          topicid=runtimeData.getParameter("topic");
        }
      }
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic(); // get all topic(including inactive one)
      writeTopicList(w,topiclist);
    }
    else if(runtimeData.getParameter("rename")!=null)
    {
      String topicid=runtimeData.getParameter("topic");
      String newname=runtimeData.getParameter("newname");
      if ((newname==null)||(newname.equals("")))
      {
        w.write("<needWarning>true</needWarning>");
        w.write("<warning>Please fill up the new name column!</warning>");
      }
      else
      {
         if(isTopicNameDuplicate(newname))
         {
           w.write("<needWarning>true</needWarning>");
           w.write("<warning>The Topic name is duplicated, please try another one.</warning>");
         }
         else
         {
           classifiedsDB.renameTopic(topicid,newname);
         }
      }
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic();
      writeTopicList(w,topiclist);
    }
    else if(runtimeData.getParameter("delete")!=null)  //delete the topic
    {
      String topicid=runtimeData.getParameter("topic");
      classifiedsDB.deleteTopic(topicid);
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic();
      writeTopicList(w,topiclist);
    }
    else if(runtimeData.getParameter("finish")!=null)
    {
      m_action="main";
      writeMainPage(w);
      m_currentTopicID=null;
      m_currentExpireItemTopicList=null;
    }
    else if (runtimeData.getParameter("deleteAll")!=null)  //delete all expired items
    {
      classifiedsDB.deleteExpireItem();
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic();
      writeTopicList(w,topiclist);
      m_currentExpireItemTopicList=null; //all expired item has been deleted.
      w.write("<content>");
      writeExpireItemTopicList(w,m_currentExpireItemTopicList);
      w.write("</content>");
    }
    else if (runtimeData.getParameter("deleteChecked")!=null)  //delete expire items in select topic
    {
      TopicList expireItemTopicList=getExpireItemInATopic(m_currentExpireItemTopicList);
      int size=expireItemTopicList.getCount();
      expireItemTopicList.begin();
      expireItemTopicList.next();
      while(expireItemTopicList.hasMore())
      {
        String topicid=expireItemTopicList.getTopicID();
        classifiedsDB.deleteExpireItemByTopic(topicid);
        expireItemTopicList.next();
      }

      m_currentExpireItemTopicList=classifiedsDB.getExpireItemTopicList();
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic();
      writeTopicList(w,topiclist);
      w.write("<content>");
      writeExpireItemTopicList(w,m_currentExpireItemTopicList);
      w.write("</content>");
    }
    else
    {
      java.util.ArrayList topiclist=classifiedsDB.getAllTopic();
      writeTopicList(w,topiclist);
      m_currentExpireItemTopicList=classifiedsDB.getExpireItemTopicList();
      w.write("<content>");
      writeExpireItemTopicList(w,m_currentExpireItemTopicList);
      w.write("</content>");
    }
  }
***/

  private Document getIconChooserScreen (Document doc, ChannelRuntimeData rd)
  {
    Element IconChooser = doc.createElement("Icon_Library");

    IconChooser.setAttribute("topic_name", myTopicInfo.getName());

    if (sError.contains("ICON"))
    {
      IconChooser.setAttribute("error","error");
      sError.remove("ICON");
    }
    if (sError.contains("ICONSIZE"))
    {
      IconChooser.setAttribute("error","error1");
      sError.remove("ICONSIZE");
    }
    // add <icon name="icon.gif"/> elements
    ArrayList iconList = classifiedsDB.getIconList();
    for (int i=0; i < iconList.size(); i++)
    {
      Element Icon = doc.createElement("icon");
      IconInfo icon = (IconInfo) iconList.get(i);
      Icon.setAttribute("id", icon.getIconID());
      Icon.setAttribute("name", icon.getImageName());
      Icon.setAttribute("mime_type", icon.getImageType());
      if (myTopicInfo.getImageName() != null && myTopicInfo.getImageName().equals(icon.getImageName()))
        Icon.setAttribute("selected", "yes");
      else
        Icon.setAttribute("selected", "no");

      IconChooser.appendChild(Icon);
    }
    doc.appendChild(IconChooser);

    xslFile = "iconChooser";
    return doc;
  }

  private Document getManageTopicsScreen (Document doc, ChannelRuntimeData rd)
  {
    Element TopicsAdmin = doc.createElement("ManageTopics");

    doc.appendChild(TopicsAdmin);

    xslFile = "manageTopics";
    return doc;
  }

  private Document getTopicSearchResultsScreen (Document doc, ChannelRuntimeData rd)
  {
    Element TopicsAdmin = doc.createElement("ManageTopics");
    TopicsAdmin.appendChild(getSearchResults (rd, doc));
    doc.appendChild(TopicsAdmin);

    xslFile = "manageTopics";
    return doc;
  }

  private boolean checkIfNoGroupSelected (ChannelRuntimeData rd)
  {
    Enumeration keys = rd.keys();
    while (keys.hasMoreElements())
    {
      String paramKey = (String) keys.nextElement();
      String value = (String) rd.getObjectParameter(paramKey);
      if (value.equals("checked"))
        return true;
    }
    return false;
  }

  private String checkIfChecked (String sCheckBox)
  {
    if (sCheckBox != null && sCheckBox.equals("all"))
      return "checked";
    else if (sCheckBox != null && sCheckBox.equals("none"))
      return "no";

    return "no";
  }

    private Element getSearchResults (ChannelRuntimeData rd, Document doc)
  {
    Element SearchResultsElement = doc.createElement("search_results");
    String sCheckBox = rd.getParameter("checkbox");
    String sSearch = null;
    // if null, then user has not entered a search string
    if (sCheckBox == null)
    {
      sSearch = rd.getParameter("topic_to_find");
      if (sSearch == null || sSearch.equals(""))
      { // allow for no input search
        sSearch = "*";
        myTopicInfo.setSearchString(sSearch);
      }
      else if (sSearch != null && sSearch.trim().length() > 0)
      {
        // user has entered new search string
        if (sSearch.indexOf("*") == -1)
        {
          // wildcard (*) not found will add for user
          sSearch += "*";
        }
        myTopicInfo.setSearchString(sSearch);
      }
      else
      {
         sSearch = myTopicInfo.getSearchString();
      }
    }
    else
    {
      // user has entered previously entered a search string
      sSearch = myTopicInfo.getSearchString();
    }

    TopicList list = myTopicInfo.getSearchList();
    if (list == null || !list.hasMore())
    {
      list = classifiedsDB.getTopicList(sSearch);
      myTopicInfo.setSearchList(list);
    }
    if (!list.hasMore())
    {
         Element ErrorElement = doc.createElement("Empty");
         ErrorElement.appendChild(doc.createTextNode("No Topics found with search string \""+sSearch+"\"."));
        SearchResultsElement.appendChild(ErrorElement);

    }

    while (list.next())
    {

      String sTopicName = list.getTopicName();
      String sTopicID = list.getTopicID();
      String sTopicDescription = list.getTopicDescription();

     Element TopicElement = doc.createElement("topic");
     TopicElement.setAttribute("value", sTopicID);
     TopicElement.setAttribute("checked", checkIfChecked (sCheckBox));

     Element TopicNameElement = doc.createElement("topic_name");
     TopicNameElement.appendChild(doc.createTextNode(sTopicName));
     TopicElement.appendChild(TopicNameElement);

     Element DescriptionElement = doc.createElement("description");
     DescriptionElement.appendChild(doc.createTextNode(sTopicDescription));
     TopicElement.appendChild(DescriptionElement);

     Element ArticlesElement = doc.createElement("numberofitems");
     ArticlesElement.appendChild(doc.createTextNode(classifiedsDB.getCountForTopic(sTopicID)));
     TopicElement.appendChild(ArticlesElement);

     SearchResultsElement.appendChild(TopicElement);

    }


    return SearchResultsElement;
  }


  private boolean isTopicNameDuplicate(String topicname)
  {
    boolean duplicate=false;
    java.util.ArrayList topiclist = classifiedsDB.getAllTopic();
    Iterator it = topiclist.iterator();
    while (it.hasNext())
    {
      TopicInfo eachInfo=(TopicInfo)it.next();
      if (eachInfo.getName().equals(topicname))
       duplicate=true;
    }
    return duplicate;
  }
  private void writeMsgToUser(StringWriter w,String topicid)
  {
    writeUserTopicBoxAllTopics(w,topicid);
    java.util.ArrayList topiclist = classifiedsDB.getMsgToUserTopicList(personID);
    writeUserTopicDropBox(w,topicid,topiclist);
    writeMsgToUserItem(w,m_currentMsgToAuth);
  }

  private void writeMsgToUserItem(StringWriter w,ItemList itemlist)
  {
    if (itemlist!=null)
    {
      w.write("<isempty>false</isempty>");
      itemlist.begin();
      itemlist.next();
      try
      {
        while (itemlist.hasMore())
        {
          SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yy HH:mm");
          Timestamp ts=Timestamp.valueOf(itemlist.getExpireDate());
          String date=sdf.format(ts);
          w.write("<item>");
          String cost=itemlist.getCost();
          String phone=itemlist.getPhone();
          String email=itemlist.getEmail();
          String msg=itemlist.getContent();
          if (msg!=null)
          {
            if ((cost!=null)&&(!cost.equals("")))
            {
              msg=msg+" Cost is"+cost+".";
            }
            if ((phone!=null)&&(!phone.equals("")))
            {
              msg=msg+" Please call "+phone;
            }
            if ((email!=null)&&(!email.equals("")))
            {
              if ((phone!=null)&&(!phone.equals("")))
                msg=msg+" or email to "+email+".";
              else
                msg=msg+" Please email to "+email+".";
            }
          }
          w.write("<content>"+xmlescaper.escape(msg)+"</content>");
          w.write("<itemid>"+xmlescaper.escape(itemlist.getItemID())+"</itemid>");
          w.write("<expireDate>"+date+"</expireDate>");
          w.write("<topicname>"+xmlescaper.escape(itemlist.getTopicName())+"</topicname>");
          w.write("<msgToAuth>"+xmlescaper.escape(itemlist.getMsgToAuth())+"</msgToAuth>");
          w.write("<approve>"+xmlescaper.escape(itemlist.getApprove())+"</approve>");
          w.write("</item>");
          itemlist.next();
        }
      }
      catch (Exception e)
      {
        LogService.instance().log(LogService.ERROR, e);
      }
    }
    else
    {
      w.write("<isempty>");
      w.write("true");
      w.write("</isempty>");
      w.write("<emptyTopic>");
      w.write("There is no item available.");
      w.write("</emptyTopic>");
    }
  }
  private TopicList getExpireItemInATopic(TopicList topiclist)
  {
    int total=0;
    TopicList currentTopicList=new TopicList();
    total = topiclist.getCount() ;
    currentTopicList=topiclist;
    TopicList temp=new TopicList();
    for (int i=1; i<=total; i++)
    {
      String topicid=runtimeData.getParameter("checkDelete_"+i);
      if (topicid!=null)
      {
        String name=topiclist.getTopicName(topicid);
        String totalEntry=topiclist.getTotalEntry(topicid);
        temp.addTopic(name,topicid,totalEntry);
      }
    }
    return temp;
  }

  private String getDaysTillExpire (String date)
  {
    // this method will return an int in String format of days left till expiration
    String year = date.substring(0,4);
    String month = date.substring(5,7);
    String day = date.substring(8,10);

    String newDate = month + "/" + day + "/" + year;
    return newDate;
  }

  private String getDate (String date)
  {
    String year = date.substring(0,4);
    String month = date.substring(5,7);
    int imonth = Integer.parseInt(month);
    if (imonth > 1)
    {  imonth -= 1; }
    else
    {
      imonth = 12;
      // subtract one year from year object
      year = Integer.toString(Integer.parseInt(year) - 1);
    }
    String day = date.substring(8,10);

    String newDate = Integer.toString(imonth) + "/" + day + "/" + year;
    return newDate;
  }

  private void writeExpireItemTopicList(StringWriter w,TopicList topicList)
  {
    StringBuffer sBuff = w.getBuffer();
    if ((topicList!=null)&&(topicList.getCount()>0))
    {
      sBuff.append ("<isempty>false</isempty>");
      writeExpireItemTopic(w,topicList);
    }
    else
    {
      sBuff.append("<isempty>true</isempty>");
      sBuff.append("<emptyTopic>There is no item available.</emptyTopic>");
    }
  }
  private void writeExpireItemTopic(StringWriter w,TopicList topiclist)
  {
    StringBuffer sBuff = w.getBuffer();
    topiclist.begin();
    topiclist.next();
    while (topiclist.hasMore())
    {
      sBuff.append("<topic>");
      sBuff.append("<name>").append(xmlescaper.escape(topiclist.getTopicName())).append("</name>");
      sBuff.append("<id>").append(xmlescaper.escape(topiclist.getTopicID())).append("</id>");
      sBuff.append("<total>").append(xmlescaper.escape(topiclist.getTotalEntry())).append("</total>");
      sBuff.append("</topic>");
      topiclist.next();
    }
  }

  private void storeImageToDisk ( String userID, org.jasig.portal.MultipartDataSource fileSource)
  {

    try
    {
      java.io.File uploadDir = getUploadDirectory (userID, true);
      java.io.File uploadFile = new java.io.File(uploadDir, fileSource.getName());
      java.io.FileOutputStream fos = new java.io.FileOutputStream(uploadFile);
      java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(fos);
      java.io.InputStream ios = fileSource.getInputStream();
      java.io.BufferedInputStream bis = new java.io.BufferedInputStream(ios);
      byte[] byteArray = new byte[500];
      int size = 0;
      long totalSize = 0;
      while ((size = bis.read(byteArray)) != -1)
      {
        bos.write(byteArray, 0, size);
        totalSize += size;
      }
      bos.flush(); // need an explicit flush to write the last 'n' bytes to the file.
      ios.close();
      fos.close();

    }
    catch (IOException ioe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to write file");
    }

  }

  private java.io.File getUploadDirectory (String userID, boolean createDir) throws IOException
  {
      // *** Alex - fix for TT03858
      // String dirPath = staticData.getParameter("datastore").trim();
      String dirPath = datastore.trim();

      if (!dirPath.endsWith(fs))
        dirPath += fs;
      LogService.instance().log(LogService.DEBUG, "Base directory path is : " + dirPath);

      File storageDir = null;

      // Verify that the temporary directory exists
      try
      {
        storageDir = new File (dirPath);
      }
      catch (Exception e)
      {
        LogService.instance().log (LogService.ERROR, "Upload base directory is not available or lacks proper permissions - " + e.getMessage());
        return null;
      }

      if (storageDir.exists ())
      {
        if (!storageDir.isDirectory ())
        {
          LogService.instance().log(LogService.ERROR, dirPath + " already exists as a file.  A directory was expected.");
          throw new IOException ("\"" + dirPath + "\" exists as a file, and a directory was expected.");
        }
      }
      else
      {
        storageDir.mkdirs ();
      }

      // This should assure that no two users clash
      dirPath += userID + fs;
      storageDir = new File (dirPath);

      if (storageDir.exists())
      {
        if (!storageDir.isDirectory())
        {
          LogService.instance().log(LogService.ERROR, dirPath + " already exists as a file.  A directory was expected.");
          throw new IOException (dirPath + " already exists as a file.  A directory was expected.");
        }
      }
      else
      {
        storageDir.mkdirs ();
      }

      if (!userID.equals("icon_library"))
      {
        // directory name is classified item ID
        String dirName = newItemInfo.getItemID();

        dirPath += dirName + '/';
        storageDir = new File (dirPath);

        if (storageDir.exists())
        {
          if (!storageDir.isDirectory())
          {
            LogService.instance().log(LogService.ERROR, dirPath + " already exists as a file.  A directory was expected.");
            throw new IOException (dirPath + " already exists as a file.  A directory was expected.");
          }
        }
        else
        {
          storageDir.mkdirs ();
        }
      }

      return storageDir;
  }



  private void updateApprovalMsg()
  {
    String topicid=runtimeData.getParameter("topic");
    String cost=runtimeData.getParameter("cost");
    String content=runtimeData.getParameter("message");
    String email=runtimeData.getParameter("email");
    String phone=runtimeData.getParameter("phone");
    String itemid=runtimeData.getParameter("itemID");
    classifiedsDB.updateItem(topicid,cost,content,phone,email,itemid);
  }

  private void writeEditPage(StringWriter w, ItemList itemlist)
  {
    StringBuffer sBuff = w.getBuffer();
    String itemid=runtimeData.getParameter("itemid");
    writeTopicDropTable(w,itemlist.getTopicID(itemid));
    sBuff.append("<item>");
    sBuff.append("<itemID>").append(xmlescaper.escape(itemid)).append("</itemID>");
    sBuff.append("<content>").append(xmlescaper.escape(itemlist.getContent(itemid))).append("</content>");
    sBuff.append("<cost>").append(xmlescaper.escape(itemlist.getCost(itemid))).append("</cost>");
    sBuff.append("<phone>").append(xmlescaper.escape(itemlist.getPhone(itemid))).append("</phone>");
    sBuff.append("<email>").append(xmlescaper.escape(itemlist.getEmail(itemid))).append("</email>");
    sBuff.append("<approve>").append(xmlescaper.escape(itemlist.getApprove(itemid))).append("</approve>");
    sBuff.append("<msgToAuth>").append(xmlescaper.escape(itemlist.getMsgToAuth(itemid))).append("</msgToAuth>");
    sBuff.append("</item>");
  }

 // Implementation of org.jasig.portal.IMimeResponse methods.
  public void downloadData(OutputStream out) throws IOException
  {
  }

  public String getName()
  {
    return null;
  }

  public String getContentType()
  {

    if (newItemInfo.getImageMimeType() != null)
      return newItemInfo.getImageMimeType();
    else if (runtimeData.getParameter("icon") != null)
      return runtimeData.getParameter("mime_type");
    else
      return il.getContentType(runtimeData.getParameter("itemID"));
  }

  public InputStream getInputStream() throws IOException
  {
      try {

          return __getInputStream();

      } finally {

          // Ensure that the serialized execution lock is released
          releaseLock();
      }

  }

  private InputStream __getInputStream()
  throws IOException {

    // *** Alex - fix for TT0358
    // String repositoryPath = staticData.getParameter("datastore").trim();
    String repositoryPath = datastore.trim();

    if (repositoryPath != null)
    {
      if (!repositoryPath.endsWith(fs))
        repositoryPath += fs;

      StringBuffer sBuff = new StringBuffer(repositoryPath);

      String image = null;
      if (runtimeData.getParameter("icon") != null)
      {
        sBuff.append ("icon_library");
        image = runtimeData.getParameter("image");
      }
      else
      {
        if (newItemInfo.getImageMimeType() != null)
        {
          // userID/ArticleID/
          String userID = Integer.toString(staticData.getPerson().getID());
          sBuff.append(userID).append(fs).append(runtimeData.getParameter("itemID"));
          image = newItemInfo.getItemImage();
        }
        else
        {
          // userID/ArticleID/
          String userID = il.getAuthor(runtimeData.getParameter("itemID"));
          sBuff.append(userID).append(fs).append(runtimeData.getParameter("itemID"));
          image = il.getImage(runtimeData.getParameter("itemID"));
        }
      }

      String fileDirectory = sBuff.toString();
      sBuff = null;  // help GC

      File userDirectory = new File (fileDirectory);
      if (userDirectory.exists())
      {
        File userFile = new File (fileDirectory + fs + image);
        if (userFile.exists())
        {
          FileInputStream in = new FileInputStream (userFile);
          return in;
        }
      }
      fileDirectory = null;
    }
    //throw new IOException ("File " + image + " not found in the repository");
    return null;

  }

  /**
   * No Headers in the News channel.  Return a null.
   */
  public Map getHeaders()
  {
    return null;
  }


  private void setNewItemInfo(ChannelRuntimeData rd)
  {
    String topicid = rd.getParameter("topics");
    String msg = rd.getParameter("message");
    String cost = rd.getParameter("cost");
    String phone = rd.getParameter("phone");
    String email = rd.getParameter("email");

    newItemInfo.setTopicID(topicid);
    newItemInfo.setAuthorID(Integer.toString(staticData.getPerson().getID()));
    newItemInfo.setItemContent(msg);
    newItemInfo.setCost(cost);
    newItemInfo.setPhone(phone);
    newItemInfo.setEmail(email);

    // Freddy added
    String contact_name = rd.getParameter("contact_name");
    newItemInfo.setContactName(contact_name);

/*    // if user has entered an image, then get it
    if (rd.getObjectParameterValues("classified_image") != null)
    {
System.out.println("attempting to get Image information.\n");
      MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("classified_image"))[0];
System.out.println("fileSource Image ==== "+fileSource+"\n");

      newItemInfo.setItemImage(fileSource.getName());
      newItemInfo.setImageMimeType(fileSource.getContentType());
      String userID = Integer.toString(staticData.getPerson().getID());

     try                      
     {
      newItemInfo.setImageInputStream(fileSource.getInputStream());
     }
     catch (IOException ioe)
     {
       System.out.println("IOException ioe ==== "+ioe+"\n");
     }
    }*/ 
  }

  /** The caching scheme caches only caches the peephole view of
      the classifieds channel. It creates a caching entry based on
      a channel and user ID combination (one cache entry for each classifieds user).
      Other views are not cached since the channel always operates in
      focused view.
  */
  public ChannelCacheKey generateKey() {

     ChannelCacheKey k = null;

     // Generate a cache key only for the peephole view
     if (!runtimeData.isRenderingAsRoot()) {
        
          k = new ChannelCacheKey();

		StringBuffer sbKey = new StringBuffer();

          k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);

		sbKey.append(this.getClass().getPackage().getName());
		sbKey.append(":userId:");
		sbKey.append(staticData.getPerson().getID());
                                                 
          k.setKey(sbKey.toString());         
     }

     return k;
  }

  /**
   * The cache entry is valid in all cases. Only the peephole view
   * is being cached which is static for a specific classifieds user.
   *
   * @param validity
   * @return Returns true if cache entry is valid, false otherwise.
   */
  public boolean isCacheValid(Object validity) {

     boolean cacheValid = true;

     //Remove any running/active servant
     staticData.remove("AddressbookServant");

     // Release lock here since content will be retrieved
     // from cache
     releaseLock();

     return cacheValid;
  }


  // IPermissible methods

  public String getOwnerName() {
    return "Academus Classifieds";
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

  /**
   * Let the channel know that there were problems with the download
   * @param e
   */
  public void reportDownloadError(Exception e) {
    LogService.log(LogService.ERROR, "CClassifieds::reportDownloadError(): " + e.getMessage());
  }

} // end CClassifieds
