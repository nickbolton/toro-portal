/*
 *
 * Copyright (c) 2001 - 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with IBS.
 *
 * IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 *  $Log:
 *   2    Channels  1.1         12/20/2001 3:54:07 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:39 AMFreddy Lopez
 *  $
 *
 */

package com.interactivebusiness.news.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.IdentityDataSearcher;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrChannelRuntimeData;
import net.unicon.portal.util.GroupsSearch;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IServant;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.interactivebusiness.news.data.NewsDb;
import com.interactivebusiness.news.data.NewsInfo;
import com.interactivebusiness.news.data.TopicList;
import com.interactivebusiness.portal.VersionResolver;

/**
 * This is the News Admin channel.
 * Contains methods to manage Topics and News Articles
 *
 * @author Freddy Lopez, flopez@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class NewsAdmin extends BaseChannel implements IServant
{
  private static NewsDb newsDB;
  private final XSLT xslt = new XSLT (this);
  private static final String sslLocation = "NewsAdmin/NewsAdmin.ssl";
  private ArrayList sError = new ArrayList ();
  private TopicInfo myTopicInfo;
  private NewsInfo newArticle = new NewsInfo();

  private GroupsSearch gs = new GroupsSearch ();
  private static final String fs = File.separator;
  private String xslfile;
  private String sAction;

  // private ArrayList folderEditGroupIDs = null;
  private Vector namesFound = null;
  private String sCurrentMonth;
  private String sUserName;
  private Hashtable topicNames = new Hashtable();
  private Calendar rightNow = new GregorianCalendar();

  private static VersionResolver vr = VersionResolver.getInstance();
  //test variable
  private ArrayList selectedGroups;
//  private GroupsSearch gs;

// *** Alex - fix for TT03858
  private static String DATASOURCE;
  private static String datastore;

  static
  {
      DATASOURCE = UniconPropertiesFactory.getManager(
                      PortalPropertiesType.RAD).getProperty(
                        "com.interactivebusiness.news.channel.datasource"
                      );
      datastore = UniconPropertiesFactory.getManager(
                      PortalPropertiesType.RAD).getProperty(
                        "com.interactivebusiness.news.channel.datastore"
                      );
  }
// *** end fix for TT03858


  public NewsAdmin () {}


  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    CscrChannelRuntimeData ccrd = new CscrChannelRuntimeData( this.staticData.getChannelPublishId(), rd );
    this.runtimeData = ccrd;

    if (sUserName == null)
    {
      sUserName = staticData.getPerson().getFullName();
    }


  String value = rd.getParameter("action");
  if (value != null && value.equals("SaveArticle"))
  {
   // user has chosen a command from the previewArticle screen
   sAction =  value;

  }

  }

  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (org.xml.sax.ContentHandler documentHandler) throws PortalException
  {
    ChannelRuntimeData rd = this.runtimeData;
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    // get a handle to the DB connection
    newsDB = (NewsDb) staticData.get("NewsDB");

/* -->  uncomment to debug this channel...
// Spill parameters.
System.out.println("*****> Spilling Parameters...");
Enumeration keys = rd.getParameterNames();
while (keys.hasMoreElements()) {
    String key = null;
    try {
        key = (String) keys.nextElement();
        Object value = rd.getParameter(key);
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
/**/

    if (myTopicInfo == null)
    {
      myTopicInfo = new TopicInfo ();
      // setup topicNames hashtable
      listTopics ();
    }

    String sAction = rd.getParameter("action");
    // Topics Administration
    if (sAction.equals("TopicsAdmin"))
    {
      resetall();
      doc = getTopicMenuScreen (doc);
    }
    else if (sAction.equals("CreateTopic"))
    {
      if (myTopicInfo.getTopicName() != null)
        resetall ();
      doc = getCreateTopicScreen (doc, rd);
    }
    else if (sAction.equals("CheckTopicInput"))
    {
      if (rd.getParameter("next") != null)
      {
        boolean errorFound = parseForm ("topic", rd);
        if (errorFound)
          doc = getCreateTopicScreen (doc, rd);
        else
          doc = getGroupsListScreen (doc, rd);

      }
      else
      {
        // show Topics menu
        resetall();
        doc = getTopicMenuScreen (doc);
      }
    }
    else if (sAction.equals("SelectGroupTopic"))
    {
      doc = getGroupsListScreen (doc, rd);
    }
    else if (sAction.equals("PreviewTopic"))
    {

      if (rd.getParameter("go") != null)
      {

          // user has entered a userID to user
          String userName = rd.getParameter("person_name");
          if (userName != null) {
            try {
              namesFound = IdentityDataSearcher.personSearch(userName.trim(), "", 50);
            } catch (Exception e) {
              LogService.log (LogService.ERROR, "NewsAdmin:renderXML threw Exception:"+e);
            }
          }

          //rd.setParameter("nameFound", nameFound == null ? "notfound" : nameFound);
          doc = getGroupsListScreen (doc, rd);
      }
      else if (rd.getParameter("next") != null)
      {

        if (checkIfNoGroupSelected (rd))
        {  doc = getPreviewTopicScreen (doc, rd);}
        else
        {
          rd.setParameter("error", "error");
          doc = getGroupsListScreen (doc, rd);
        }
      }
      else if (rd.getParameter("back") != null)
      {

      doc = getCreateTopicScreen (doc, rd);
      }
      else if (rd.getParameter("cancel") != null)
      {

        // show Topics menu
        resetall();
        doc = getTopicMenuScreen (doc);
      }
      else
      {
         doc = getGroupsListScreen (doc, rd);
      }
    }
    else if (sAction.equals("SaveTopic"))
    {
      if (rd.getParameter("next") != null)
      {  doc = getSaveTopicScreen (doc);}
      else if (rd.getParameter("back") != null)
      {  doc = getGroupsListScreen (doc, rd);}
      if (rd.getParameter("cancel") != null)
      {
        // show Topics menu
        resetall();
        doc = getTopicMenuScreen (doc);
      }

    }
    else if (sAction.equals("SaveTopicError"))
    {
      if (rd.getParameter("createanother") != null)
      {
        if (myTopicInfo.getTopicName() != null)
          resetall ();
        doc = getCreateTopicScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {  doc = getPreviewTopicScreen (doc, rd);
      }
      else if (rd.getParameter("exit") != null)
      {  resetall();
      doc = getTopicMenuScreen (doc);
      }

    }
    else if (sAction.equals("DeleteTopic"))
    {
      doc = getConfirmDeleteTopicScreen (doc, rd);
    }
    else if (sAction.equals("DeleteTopicConfirmed"))
    {
      if (rd.getParameter("ok") != null)
      {
        // user has confirmed delete operation
        if (deleteTopic ())// topic deleted, go back to search results
          doc = getSearchResultsScreen (doc, rd);
        else  // error occurred, display error then send back to search results
          doc = getDeleteTopicErrorScreen (doc);
      }
      else if (rd.getParameter("cancel") != null)
      {
        // user has canceled delete operation, return to search results screen

        // *** JK - fix for TT03946 - reset on cancel
        resetall();

        doc = getSearchResultsScreen (doc, rd);
      }

    }
    else if (sAction.equals("EditTopic"))
    {
      myTopicInfo.currentState = "Edit";
      doc = getCreateTopicScreen (doc, rd);
    }
    else if (sAction.equals("TopicSearch"))
    {
      doc = getSearchResultsScreen (doc, rd);
    }
      // End Topic Administration
      // News Publisher Administration
      else if (sAction.equals("ArticlesAdmin"))
      {
        resetall();
        doc = getArticleMenuScreen (doc);
      }
      else if (sAction.equals("CreateArticle"))
      {

        if (newArticle.getTitle() != null)
          resetall ();
        doc = getCreateArticleScreen (doc, rd);
      }
      else if (sAction.equals("CheckInputFirst"))
      {
        if (rd.getParameter("next") != null)
        {
         // user has chosen to continue to next page
          boolean errorFound = parseForm ("Article1", rd);
          if (errorFound)
            doc = getCreateArticleScreen (doc, rd);
          else
            doc = getCreateStoryScreen (doc, rd);

        }
        else if (rd.getParameter("cancel") != null)
        {
         // user has chosen to cancel and return to Article Menu
          resetall();
        doc = getArticleMenuScreen (doc);
        }

      }
      else if (sAction.equals("CreateStory"))
      {
       doc = getCreateStoryScreen (doc, rd);

      }
     else if (sAction.equals("CheckInputSecond"))
      {
        if (rd.getParameter("load") != null)
        {

         // user has selected a *.txt file and want to load it
          if (rd.getObjectParameterValues("storyfile") != null)
            getUploadStoryFile (rd);
          else  if (rd.getObjectParameterValues("imagefile") != null)
            getUploadImageFile (rd);

          doc = getCreateStoryScreen (doc, rd);

        }
        else if (rd.getParameter("next") != null)
        {
         // user has chosen to continue to next page
        boolean errorFound = parseForm ("Article2", rd);
        if (errorFound)
          doc = getCreateStoryScreen (doc, rd);
        else
          doc = getPreviewArticleScreen (doc, rd);

        }
        else if (rd.getParameter("back") != null)
        {
         // user has chosen to cancel and return to create Article
         // must first keep items that he entered in the create story screen
         if (rd.getParameter("content") != null)
           newArticle.setStory(rd.getParameter("content"));

         getUploadImageFile (rd);
         newArticle.setLayoutType(rd.getParameter("layout"));

         doc = getCreateArticleScreen (doc, rd);
        }
        else if (rd.getParameter("cancel") != null)
        {
         // user has chosen to cancel and return to Article Menu
          resetall();
          doc = getArticleMenuScreen (doc);
        }

      }

      else if (sAction.equals("SaveArticle"))
      {


        if (rd.getParameter("next") != null)
        {
         // user has chosen to save article to DB
         doc = getSaveArticleScreen (doc);
        }
        else if (rd.getParameter("back") != null)
        {
         // user has chosen to cancel and return to create Story
         doc = getCreateStoryScreen (doc, rd);
        }
        else if (rd.getParameter("cancel") != null)
        {
         // user has chosen to cancel and return to Article Menu
          resetall();
        doc = getArticleMenuScreen (doc);
        }



      }
    else if (sAction.equals("SaveArticleError"))
    {
      if (rd.getParameter("createanother") != null)
      {
       if (newArticle.getTitle() != null)
          resetall ();
        doc = getCreateArticleScreen (doc, rd);
      }
      else if (rd.getParameter("back") != null)
      {  doc = getPreviewArticleScreen (doc, rd);}
      else if (rd.getParameter("exit") != null)
      {     resetall();
        doc = getArticleMenuScreen (doc);
      }

    }
    else if (sAction.equals("ArticleSearch"))
    {
      // User has requested to search for Article
      // 3 search types
      String searchType = rd.getParameter("searchstyle");
      if (searchType == null)
      {
        myTopicInfo.setArticleSearchList(null);
        myTopicInfo.setArticleSearchString(null);
        doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
      }
      else if (searchType.equals("listall"))
      {
        myTopicInfo.setArticleSearchType("3");
        doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
      }
      else
      {
        doc = getArticleSearchStyle (doc, searchType);
      }
    }
    else if (sAction.equals("ArticleSearchResults"))
    {
      if (rd.getParameter("checkbox") != null)
      {
        doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
      }
      else if (rd.getParameter("go") != null)
      {
      // user is searching with Topic Names

        if (!("".equals(rd.getParameter("topicID")))  
            || rd.getParameter("topicID") != null) {
          doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
        }
        else
        {
         //send back with error
         sError.add("topic");
         doc = getArticleSearchStyle (doc, "topic");
        }
      }
      else if (rd.getParameter("search") != null)
      {

         doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);

      }
    }
    else if (sAction.equals("DeleteArticle"))
    {
      doc = getConfirmDeleteArticleScreen (doc, rd);
    }
    else if (sAction.equals("DeleteArticleConfirmed"))
    {
      if (rd.getParameter("ok") != null)
      {
        // user has confirmed delete operation
        if (deleteArticle ())// Article deleted, go back to search results
        {
          myTopicInfo.setArticleSearchList(null);
          myTopicInfo.setArticleSearchString(null);
          doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
        }
        else  // error occurred, display error then send back to search results
        {  doc = getDeleteArticleErrorScreen (doc);}
      }
      else if (rd.getParameter("cancel") != null)
      {
        // user has canceled delete operation, return to search results screen

        // *** JK - fix for TT03946 - reset delete list on cancel
        myTopicInfo.setDeleteList(new ArrayList ());

        doc = getArticleSearchResults (myTopicInfo.getArticleSearchType(), doc, rd);
      }

    }
    else if (sAction.equals("EditArticle"))
    {
      myTopicInfo.currentState = "Edit";
      doc = getCreateArticleScreen (doc, rd);
    }

/**          org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
**/

    xslt.setXML(doc);
    xslt.setXSL(sslLocation, xslfile, runtimeData.getBrowserInfo());
    xslt.setTarget(documentHandler);

   //String w = runtimeData.getWorkerActionURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
    String w = runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
    if (w.indexOf("worker") != -1)
    {
      w = w.substring(w.indexOf("worker"));
    }

    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("resourceURL", w);
    xslt.setStylesheetParameter("mayManageTopics"
            , Toolbar.checkPermission(staticData.getPerson()) ? "true" : "false");
    xslt.setStylesheetParameter("mayManageArticles"
            , Toolbar.canPublishArticles(staticData.getPerson(), newsDB.getAllGroupIDs()) ? "true" : "false");
    
//    xslt.setStylesheetParameter("resourceURL", runtimeData.getWorkerActionURL(UPFileSpec.FILE_DOWNLOAD_WORKER));
    xslt.transform();

  }

  // Freddy Added to search through selected Groups
  protected void processGroupSelection (String userID, ChannelRuntimeData rd, StringWriter w)
  {
    boolean noGroupChecked = true;
    ArrayList mySelection = new ArrayList ();

    // must loop through group because user can select multiple groups to share with
    Enumeration keys = rd.keys();

    while (keys.hasMoreElements())
    {
      String paramKey = (String) keys.nextElement();
      String value = (String) rd.getObjectParameter(paramKey);
      if (value.equals("checked"))
      {
        // name=groupID-entityType (2=person or 3=group)
        mySelection.add(paramKey);
        noGroupChecked = false;
      }
    }

    // newfolder flag
    boolean newFolder = false;
    if (rd.getParameter("newfolder") == null)
     newFolder = true;


  }

  //Freddy Added for group support
  private Document getGroupsListScreen (Document doc, ChannelRuntimeData rd)
  {

    if (selectedGroups == null) {
      selectedGroups = new ArrayList();
    }

    String groupSelected = rd.getParameter("selected");
    String groupExpand = rd.getParameter("expand");
    String isLeafNode = rd.getParameter("leafnode");

    Element SelectGroupTopicElement = doc.createElement("SelectGroupTopic");

    Element TopicNameElement = doc.createElement("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    SelectGroupTopicElement.appendChild(TopicNameElement);

    if (rd.getParameter("error") != null)
    {
      Element ErrorElement = doc.createElement("error");
      SelectGroupTopicElement.appendChild(ErrorElement);
    }

    // if not empty then user is editing folder and has previously selected groups
    // check to see if user selected previous search name
    Enumeration keys = rd.keys();

    while (keys.hasMoreElements()) {
        String paramKey = (String) keys.nextElement();
        String value = (String) rd.getObjectParameter(paramKey);

        if (value.equals("checked")) {
            if (!myTopicInfo.getSelectedGroups().contains(paramKey)) {
                myTopicInfo.getSelectedGroups().add(paramKey);
            }
        }
    }


    if (!myTopicInfo.getSelectedGroups().isEmpty())
    {
      Element PreviousElement = doc.createElement("previousSelection");

      for (int i=0; i < myTopicInfo.getSelectedGroups().size(); i++)
      {

        Element GroupNameElement = doc.createElement("groupName");
        String groupID = (String) myTopicInfo.getSelectedGroups().get(i);
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

          if (selectedGroups.contains(groupSelected)) {
            selectedGroups.remove(groupSelected);
          } else {
            selectedGroups.add(groupSelected);
          }

          if (isLeafNode == null)
          {  gs.expandGroup (selectedGroups, GroupsListElement, groupSelected);}
          else
          {
             String userID =  Integer.toString(staticData.getPerson().getID());
             gs.expandGroup (selectedGroups, GroupsListElement, groupSelected, isLeafNode, userID);
          }
        }
 //     }
      SelectGroupTopicElement.appendChild(GroupsListElement);
      doc.appendChild(SelectGroupTopicElement);

    }
    catch (GroupsException ge)
    {

    }
    xslfile = "SelectGroup";

    return doc;
  }

  private Document getArticleMenuScreen (Document doc)
  {
    Element ArticlesMenuElement = doc.createElement("ArticlesMenu");
    ArticlesMenuElement.appendChild(doc.createTextNode(""));
    doc.appendChild(ArticlesMenuElement);

    xslfile = "ArticlesMenu";
    return doc;
  }

  private Document getTopicMenuScreen (Document doc)
  {
    Element TopicsMenuElement = doc.createElement("TopicsMenu");
    doc.appendChild(TopicsMenuElement);
    xslfile = "TopicsMenu";

    return doc;
  }

  private Document getCreateArticleScreen (Document doc, ChannelRuntimeData rd)
  {

    Element CreateArticleElement = doc.createElement("CreateArticle");

   // if articleID exists, then user is requesting to edit from search results
    if (rd.getParameter("articleID") != null)
      getEditArticleValues (rd.getParameter("articleID"));

    CreateArticleElement.appendChild(getCreateArticleXML (doc));
    doc.appendChild(CreateArticleElement);

    xslfile = "CreateArticle";
    return doc;
  }

   private Document getPreviewArticleScreen (Document doc, ChannelRuntimeData rd)
  {

    Element PreviewArticleElement = doc.createElement("PreviewArticle");
    PreviewArticleElement.appendChild(getPreviewArticleXML (doc));
    doc.appendChild(PreviewArticleElement);

    xslfile = "PreviewArticle";
    return doc;
  }

  private Document getPreviewTopicScreen (Document doc, ChannelRuntimeData rd)
  {


    String sTopicID = rd.getParameter("topicID");

    Element PreviewTopicElement = doc.createElement("PreviewTopic");

     if (sTopicID != null)
     {
      Element TopicIDElement = doc.createElement("topicID");
      TopicIDElement.appendChild(doc.createTextNode(sTopicID));
     PreviewTopicElement.appendChild(TopicIDElement);
     }
     else
    {
      Element NoTopicIDElement = doc.createElement("notopicID");
      NoTopicIDElement.appendChild(doc.createTextNode(""));
     PreviewTopicElement.appendChild(NoTopicIDElement);

    }

      PreviewTopicElement.appendChild(getPreviewTopicXML (doc));

      try{
        PreviewTopicElement.appendChild(getSelectedGroupsXML (rd, doc));
      }catch (GroupsException ge)
      {
       LogService.instance().log (LogService.ERROR, "NewsAdmin:getSelectedGroupsXML threw GroupsException:"+ge);
      }

      doc.appendChild(PreviewTopicElement);
      xslfile = "PreviewTopic";

      return doc;
 }

  private Element getSelectedGroupsXML (ChannelRuntimeData rd, Document doc) throws GroupsException
  {

    boolean noGroupChecked = true;
    ArrayList mySelection = new ArrayList ();

    // must loop through topic because user can select multiple topics to subscribe
    Enumeration keys = rd.keys();

    Element GroupsList = doc.createElement("GroupsList");

    while (keys.hasMoreElements())
    {
      String paramKey = (String) keys.nextElement();
      String value = (String) rd.getObjectParameter(paramKey);

      if (value.equals("checked"))
      {
         int sepIndex = paramKey.indexOf("-");
        String entity = paramKey.substring(sepIndex+1);
        String id = paramKey.substring(0, sepIndex);

        Element Group = doc.createElement("group");
        Group.setAttribute("name", gs.getReadableName(gs.checkIfGroupOrPerson(entity), id));
        Group.setAttribute("entity", entity);
        GroupsList.appendChild(Group);
        mySelection.add(paramKey);
        noGroupChecked = false;
      }
    }


    myTopicInfo.setSelectedGroups(mySelection);

    return GroupsList;
  }

  private String getGroupName (String groupID) throws GroupsException
  {

  IEntityGroup groupToFind = GroupService.findGroup(groupID);

  return groupToFind.getName();

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
      ArrayList delete = myTopicInfo.getDeleteList();
      delete.add(topicID);
      myTopicInfo.setDeleteList(delete);
     }
     else
     {  // user has selected to delete multiple topics
       // parse through all checkboxes
      Enumeration keys = rd.keys();

      Element DeleteList = doc.createElement("DeleteList");

      boolean foundChecked = false;
      ArrayList delete = null;
      while (keys.hasMoreElements())
      {
        String paramKey = (String) keys.nextElement();
        String value = (String) rd.getObjectParameter(paramKey);

        if (value.equals("checked"))
        {
          Element DeleteTopicID = doc.createElement("topicID");
          DeleteTopicID.setAttribute("name", topicNames.get(paramKey).toString());
          DeleteList.appendChild(DeleteTopicID);

          delete = myTopicInfo.getDeleteList();
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
        xslfile = "DeleteTopic";

          return doc;
 }

   private Document getConfirmDeleteArticleScreen (Document doc, ChannelRuntimeData rd)
  {
    String articleID = rd.getParameter("articleID");
    String checkBox = rd.getParameter("checkbox");

    Element DeleteArticleElement = doc.createElement("DeleteArticle");

     // user has selected a single topic to delete
     if (articleID != null)
     {
      Element ItemElement = doc.createElement("DeleteItem");

    NewsInfo found = getNewsInfo (articleID);
    ItemElement.appendChild(doc.createTextNode(found.getTitle()));
    DeleteArticleElement.appendChild(ItemElement);



      ArrayList delete = myTopicInfo.getDeleteList();
      delete.add(articleID);
      myTopicInfo.setDeleteList(delete);
     }
     else
    {  // user has selected to delete multiple topics
       // parse through all checkboxes
      Enumeration keys = rd.keys();

      Element DeleteList = doc.createElement("DeleteList");

      boolean foundChecked = false;
      ArrayList delete = null;
      while (keys.hasMoreElements())
      {
        String paramKey = (String) keys.nextElement();
        String value = (String) rd.getObjectParameter(paramKey);

        if (value.equals("checked"))
        {
          Element DeleteArticleID = doc.createElement("articleID");
          NewsInfo found = getNewsInfo (paramKey);
          DeleteArticleID.setAttribute("name", found.getTitle());
          DeleteList.appendChild(DeleteArticleID);

          delete = myTopicInfo.getDeleteList();
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
     DeleteArticleElement.appendChild(DeleteList);
    }


        doc.appendChild(DeleteArticleElement);
        xslfile = "DeleteArticle";

          return doc;
 }


  private void getEditTopicValues (String topicID)
  {

  // user has requested to edit a topic from search results screen
    myTopicInfo.editTopicID = topicID;
    myTopicInfo.setTopicName(topicNames.get(topicID).toString());
    myTopicInfo.setTopicDescription(getTopicDescription(topicID));
    myTopicInfo.setSelectedGroups(newsDB.getTopicGroupID(topicID));
  }

  private void getEditArticleValues (String articleID)
  {

  // user has requested to edit a article from search results screen
   newArticle = getNewsInfo (articleID);
  }

  private Document getSaveTopicScreen (Document doc) throws PortalException
  {
    boolean saved = false;
    if (myTopicInfo.currentState != null && myTopicInfo.currentState.equals("Edit"))
      saved = saveEditedTopic ();
    else
      saved = saveTopic ();

    Element SaveElement = doc.createElement("SaveTopic");
    if (saved)
      SaveElement.setAttribute("saved", "yes");
    else
      SaveElement.setAttribute("saved", "no");

    Element TopicNameElement = doc.createElement ("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    SaveElement.appendChild(TopicNameElement);

    if (!saved)
    {
      Element ErrorFoundElement = doc.createElement ("error");
      ErrorFoundElement.appendChild(doc.createTextNode(getFriendlyError((String)sError.get(0))));
      SaveElement.appendChild(ErrorFoundElement);
      sError.clear();
    }
    doc.appendChild(SaveElement);
    xslfile = "SaveTopic";
    return doc;
 }

  private Document getSaveArticleScreen (Document doc) throws PortalException
  {
    boolean saved = false;
    String articleName = newArticle.getTitle();

    if (myTopicInfo.currentState != null && myTopicInfo.currentState.equals("Edit"))
      saved = saveEditedNewsItem ();
    else
      saved = saveNewsItem ();



    Element SaveElement = doc.createElement("SaveArticle");
    if (saved)
      SaveElement.setAttribute("saved", "yes");
    else
      SaveElement.setAttribute("saved", "no");

    Element ArticleNameElement = doc.createElement ("articlename");
    ArticleNameElement.appendChild(doc.createTextNode(articleName));
    SaveElement.appendChild(ArticleNameElement);

    if (!saved)
    {
      Element ErrorFoundElement = doc.createElement ("error");
      if (!sError.isEmpty())
        ErrorFoundElement.appendChild(doc.createTextNode(getFriendlyError((String)sError.get(0))));
      else
        ErrorFoundElement.appendChild(doc.createTextNode("Unknown error has occurred!"));

      SaveElement.appendChild(ErrorFoundElement);
      sError.clear();
    }
    doc.appendChild(SaveElement);
/**
          org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
 **/
    xslfile = "SaveArticle";
    return doc;
 }

  private Document getDeleteTopicErrorScreen (Document doc) throws PortalException
  {

    Element ErrorElement = doc.createElement("DeleteError");

/*    Element TopicNameElement = doc.createElement ("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    SaveElement.appendChild(TopicNameElement);
*/
    Element ErrorFoundElement = doc.createElement ("error");
    ErrorFoundElement.appendChild(doc.createTextNode(getFriendlyError((String)sError.get(0))));
    ErrorElement.appendChild(ErrorFoundElement);
    sError.clear();

    doc.appendChild(ErrorElement);
    xslfile = "DeleteTopicError";
    return doc;
 }

  private Document getDeleteArticleErrorScreen (Document doc) throws PortalException
  {

    Element ErrorElement = doc.createElement("DeleteError");

/*    Element TopicNameElement = doc.createElement ("topicname");
    TopicNameElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    SaveElement.appendChild(TopicNameElement);
*/
    Element ErrorFoundElement = doc.createElement ("error");
    ErrorFoundElement.appendChild(doc.createTextNode(getFriendlyError((String)sError.get(0))));
    ErrorElement.appendChild(ErrorFoundElement);
    sError.clear();

    doc.appendChild(ErrorElement);
    xslfile = "DeleteArticleError";
    return doc;
 }

  private String getFriendlyError (String exceptionString)
  {

    String friendlyError;

    if (exceptionString.indexOf("Violation of unique index") != -1)
      exceptionString = "Topic name already exists, please choose a different name";

    return exceptionString;
  }

  private Document getSearchResultsScreen (Document doc, ChannelRuntimeData rd)
  {

    Element TopicsMenuElement = doc.createElement("TopicsMenu");

    TopicsMenuElement.appendChild(getSearchResults (rd, doc));
    doc.appendChild(TopicsMenuElement);
    xslfile = "TopicsMenu";
    return doc;
  }

  private Document getCreateTopicScreen (Document doc, ChannelRuntimeData rd)
  {
    Element CreateTopicElement = doc.createElement("CreateTopic");

    Element TopicElement = doc.createElement("topic");
    if (sError.contains("topic"))
    {
      TopicElement.setAttribute("error","error");
      sError.clear();
    }
    else if (sError.contains("top_descr"))
    {
      TopicElement.setAttribute("error","descr");
      sError.clear();
    }
    // if topicID exists, then user is requesting to edit from search results
    if (rd.getParameter("topicID") != null)
      getEditTopicValues (rd.getParameter("topicID"));

    TopicElement.appendChild(doc.createTextNode((myTopicInfo.getTopicName() != null ? myTopicInfo.getTopicName() : "")));
    CreateTopicElement.appendChild(TopicElement);

    Element DescriptionElement = doc.createElement("description");
    DescriptionElement.appendChild(doc.createTextNode((myTopicInfo.getTopicDescription () != null ? myTopicInfo.getTopicDescription () : "")));
    CreateTopicElement.appendChild(DescriptionElement);

    doc.appendChild(CreateTopicElement);
    xslfile = "CreateTopic";
    return doc;
  }

  private String getMonth ()
  {
    return new Integer ((rightNow.get(Calendar.MONTH)+1)).toString();
  }

  private String getDay ()
  {
    return new Integer (rightNow.get(Calendar.DATE)).toString();
  }

  private String getYear ()
  {
    return new Integer (rightNow.get(Calendar.YEAR)).toString();
  }

  private String getTopicDescription (String sTopicID)
  {
    TopicList list = newsDB.getTopicList(topicNames.get(sTopicID).toString());

    while (list.next())
     return list.getTopicDescription();

    return null;
  }

  private boolean deleteTopic ()
  {
    int deleted = 0;
    // get ArrayList of Delete Topic ID's
    try
    {
      if (myTopicInfo.getDeleteList().size() == 1)
      {
        deleted = newsDB.deleteTopic((String)myTopicInfo.getDeleteList().get(0));
      }
      else
      {  // user has requested to delete multiple Topics

        deleted = newsDB.deleteTopic(myTopicInfo.getDeleteList());
      }
    }
    catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:deleteTopic threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(deleted == 0) {
      sError.add("Unable to delete the selected topic(s): No topics deleted.");
      return false;
    }

    // clear out topic entry inside of NEws_topic_groups table
    try
    {
      if (myTopicInfo.getDeleteList().size() == 1)
      {
        newsDB.removeTopicGroupIDs ((String)myTopicInfo.getDeleteList().get(0));
      }
      else
      {  // user has requested to delete multiple Topics
        for (int i=0; i < myTopicInfo.getDeleteList().size(); i++)
          newsDB.removeTopicGroupIDs ((String)myTopicInfo.getDeleteList().get(i));

      }
    }catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:removeTopicGroupIDs threw SQLException:"+sqe);
    }

    // clear out hashtable and reconstruct it
    listTopics ();
    return true;
  }

  private boolean deleteArticle ()
  {

    int deleted = 0;
    // get ArrayList of Delete Article ID's
    try
    {
      if (myTopicInfo.getDeleteList().size() == 1)
      {
        deleted = newsDB.deleteNewsItem((String)myTopicInfo.getDeleteList().get(0));
      }
      else
      {  // user has requested to delete multiple Topics

        deleted = newsDB.deleteNewsItem(myTopicInfo.getDeleteList());
      }
    }
    catch (java.sql.SQLException sqe)
    {
   LogService.instance().log (LogService.ERROR, "NewsAdmin:deleteNewsItem threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(deleted == 0)
      return false;
    else
      return true;
  }

  private Document getArticleSearchStyle (Document doc, String sSearchStyle)
  {

    Element ArticlesMenuElement = doc.createElement("ArticlesMenu");

    if (sSearchStyle.equals("topic"))
    {
      myTopicInfo.setArticleSearchType("1");
      ArticlesMenuElement.appendChild(listTopics (doc));
    }
    else if  (sSearchStyle.equals("dates"))
    {
      myTopicInfo.setArticleSearchType("2");
      Element DateSearchElement = doc.createElement("DateSearch");
      DateSearchElement.appendChild(getBegEndDate (doc, "begin"));
      DateSearchElement.appendChild(getBegEndDate (doc, "end"));
      ArticlesMenuElement.appendChild(DateSearchElement);

    }
    else if  (sSearchStyle.equals("listall"))
    {
    //  w.write ("<listall>To be implemented.</listall>");
    }

    doc.appendChild(ArticlesMenuElement);

    xslfile = "ArticlesMenu";
    return doc;

  }

  private Document getArticleSearchResults (String type, Document doc, ChannelRuntimeData rd)
  {
    Element ArticlesMenuElement = doc.createElement("ArticlesMenu");
    Element ResultsElement = doc.createElement("search_results");
    String sCheckBox = rd.getParameter("checkbox");
    String topicID = null;
    Timestamp beginDate = null;
    Timestamp endDate = null;
    // if null, then user has not entered a search string
    if (sCheckBox == null)
    {
     // save search type
      if (type.equals("1"))
      {
        topicID = rd.getParameter("topicID");
        if (myTopicInfo.getArticleSearchString() != null && myTopicInfo.getArticleSearchString().equals(topicID))
        {
            // same string previously entered do nothing
        }
        else if (topicID == null || topicID.equals(""))
        {
            // allow for no input search
            topicID = "*";
            myTopicInfo.setArticleSearchString(topicID);
            myTopicInfo.setArticleSearchBegDate(null);
            myTopicInfo.setArticleSearchEndDate(null);
            myTopicInfo.setArticleSearchList(null);
        }
        else
        {
            // topic Name search
            myTopicInfo.setArticleSearchString(topicID);
            myTopicInfo.setArticleSearchBegDate(null);
            myTopicInfo.setArticleSearchEndDate(null);
            myTopicInfo.setArticleSearchList(null);
        }
      }
      else if (type.equals("2"))
      {

        if (myTopicInfo.getArticleSearchBegDate() != null &&
            myTopicInfo.getArticleSearchEndDate() != null &&
            myTopicInfo.getArticleSearchEndDate().equals(createTimeStamp ("end", rd)) &&
            myTopicInfo.getArticleSearchBegDate().equals(createTimeStamp ("begin", rd)))
        {

        }
        else
        {

        // search with dates
        beginDate = createTimeStamp ("begin", rd);
        endDate = createTimeStamp ("end", rd);

//*** JK - TT03512 - add date check
        if (endDate.before(beginDate))
        {
          Element ErrorElement = doc.createElement("Dates");
          ErrorElement.appendChild(doc.createTextNode("Invalid date selection. Start date must precede end date."));
          ResultsElement.appendChild(ErrorElement);
        }
//***

        myTopicInfo.setArticleSearchList(null);
        myTopicInfo.setArticleSearchString(null);
        myTopicInfo.setArticleSearchBegDate(beginDate);
        myTopicInfo.setArticleSearchEndDate(endDate);
        }
      }
      else
      {
        // selected list all search
        // check if list all was previously in searchstring object
        if (myTopicInfo.getArticleSearchString() != null && myTopicInfo.getArticleSearchString().equals("*"))
        {
        // found
        }
        else
        {
          topicID = "*";
          myTopicInfo.setArticleSearchList(null);
          myTopicInfo.setArticleSearchString(topicID);
          myTopicInfo.setArticleSearchBegDate(null);
          myTopicInfo.setArticleSearchEndDate(null);
        }
      }
    }
    else
    {
    // user has clicked on check all or uncheck all. will display old list
     // do nothing for now.. :)
    }

    // Search using Topic Names
     ArrayList articlesFound = myTopicInfo.getArticleSearchList();

    try
    {
      if (articlesFound == null || articlesFound.isEmpty())
      {
        //String userID = Integer.toString(staticData.getPerson().getID());
          String userID = vr.getUserKeyColumnByPortalVersions(staticData.getPerson());
        articlesFound = (ArrayList) newsDB.getNewsItems (topicID, beginDate, endDate, gs.getMyGroups (userID), userID);
        myTopicInfo.setArticleSearchList(articlesFound);
      }
    }
    catch (java.sql.SQLException sqe)
    {
    // do something...
   LogService.instance().log (LogService.ERROR, "NewsAdmin:getNewsItems threw SQLException:"+sqe);
       }

    if (articlesFound.isEmpty())
    {
      Element ErrorElement = doc.createElement("Empty");
      ErrorElement.appendChild(doc.createTextNode("No Articles found under Topic."));
      ResultsElement.appendChild(ErrorElement);
    }
    if (articlesFound.size() > 0)
    {
      for(int i=0; i < articlesFound.size(); i++)
      {
        NewsInfo newsFound = (NewsInfo) articlesFound.get(i);
        Element ArticleElement = doc.createElement("article");
        ArticleElement.setAttribute("ID", newsFound.getID());
        ArticleElement.setAttribute("title", newsFound.getTitle());
        ArticleElement.setAttribute("abstract", newsFound.getAbstract());
        ArticleElement.setAttribute("checked", checkIfChecked (sCheckBox));
        ResultsElement.appendChild(ArticleElement);
      }
    }
ArticlesMenuElement.appendChild(ResultsElement);
   doc.appendChild(ArticlesMenuElement);
     /** org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
      ss
**/
    xslfile = "ArticlesMenu";
    return doc;
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
      if ((sSearch == null || sSearch.equals("")))
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
      list = newsDB.getTopicList(sSearch);
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

     Element ArticlesElement = doc.createElement("numberofarticles");
     ArticlesElement.appendChild(doc.createTextNode(newsDB.getNewsItemsCount(sTopicID)));
     TopicElement.appendChild(ArticlesElement);

     SearchResultsElement.appendChild(TopicElement);

    }


    return SearchResultsElement;
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

  private boolean parseForm (String NewsorTopic, ChannelRuntimeData rd)
  {

    if (NewsorTopic != null && NewsorTopic.equals("topic"))
    {
      // Used for creating a Topic
      myTopicInfo.setTopicName(rd.getParameter("topic"));
      myTopicInfo.setTopicDescription(rd.getParameter("topic_description"));
    }
    else if (NewsorTopic != null && NewsorTopic.equals("Article1"))
    {
      // Used for creating a news Item
      newArticle.setTopicID(rd.getParameter("topicID"));
      newArticle.setTitle(rd.getParameter("title"));
      newArticle.setAbstract(rd.getParameter("abstract"));
      newArticle.setBeginDate (createTimeStamp ("begin", rd));
      newArticle.setEndDate (createTimeStamp ("end", rd));

    }
    else
    {
      getUploadImageFile (rd);
      String story = rd.getParameter("content");

      if (story != null && !story.equals(""))
      {
        // user has went back and entered different file name
        newArticle.setStory(story);
      }
      // get layout type
      String LayoutType = rd.getParameter("layout");
      if (LayoutType != null && !LayoutType.equals("3"))
      {
        // user has selected an Image Layout, must check that he entered an image
        if (newArticle.getImage() == null)
          newArticle.setLayoutType("3");
        else
          newArticle.setLayoutType(LayoutType);
      }
      else
      {
        // user has selected layout type 3 (non-image)
        // check that no image is present
// *** JK - fix for TT03622 - don't force image to be displayed, even if it's uploaded
//        if (newArticle.getImage() == null)
          newArticle.setLayoutType(LayoutType);
//        else
//          newArticle.setLayoutType("1");
      }

      if (sError.contains("nonimageicon"))
        return true;
    }
    // Check for missing/empty fields
    boolean errorFound = ErrorCheck (NewsorTopic);

    return errorFound;
  }

  private void getUploadImageFile (ChannelRuntimeData rd)
  {


    if (rd.getObjectParameterValues("imagefile") != null)
    {
      MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("imagefile"))[0];

   if (fileSource != null && !fileSource.getName().equals(newArticle.getImage()))
   {

    String userID = null;
    if (newArticle.getAuthor() == null)
      userID = Integer.toString(staticData.getPerson().getID());
    else
      userID = newArticle.getAuthor();
      if (fileSource.getContentType().startsWith("image")){
        storeImageToDisk(userID, fileSource);
        newArticle.setAuthor(userID);
      }
      else
        sError.add("nonimageicon");
   }

  }

  }

  private void getUploadStoryFile (ChannelRuntimeData rd)
  {

    // user has entered a text document must read and set to Story String
      StringBuffer sb = new StringBuffer(1024);
      String sTemp = null;

      MultipartDataSource fileSource = ((MultipartDataSource[]) rd.getObjectParameterValues("storyfile"))[0];
      //add by Jing
     if(rd.getObjectParameterValues("storyfile") != null && fileSource.getContentType().startsWith("text"))
     {

    try
    {
     BufferedReader in = new BufferedReader( new InputStreamReader( fileSource.getInputStream()));



      //Read in entire XML document into a StringBuffer
      while ((sTemp = in.readLine()) != null)
      {

        if (sTemp.equals(""))
        {

        sb.append(sTemp).append("\r\n");
        }
        else
        {
        sb.append(sTemp).append("\n");
        }
      }
      //close stream
      if (in != null)
        in.close();

    }
    catch (Exception e)
    {
     LogService.instance().log (LogService.ERROR, "NewsAdmin:getUploadStoryFile threw Exception:"+e);
    }
     }
     else
         sError.add("nontextstory");
      if (sb.length() > 0)
        newArticle.setStory(sb.toString());
      else
          newArticle.setStory(rd.getParameter ("content"));
    // must check if user entered an image or layout type
    newArticle.setLayoutType(rd.getParameter("layout"));
    if (rd.getObjectParameterValues("imagefile") != null)
    {
      // retrieve image
      getUploadImageFile (rd);
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

      newArticle.setImage(fileSource.getName());
      newArticle.setImageContentType(fileSource.getContentType());


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

      // directory name is article ID
      String dirName = null;
      if (newArticle.getID() == null)
      {
        dirName = newsDB.getArticleID();
        newArticle.setID(dirName);
      }
      else
      {
        dirName = newArticle.getID();
      }


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

      return storageDir;
  }


  private Timestamp createTimeStamp (String beginOrEnd, ChannelRuntimeData rd)
  {
    Timestamp rval = new Timestamp(System.currentTimeMillis());

    String sDate = checkdate(rd.getParameter(beginOrEnd+"_date"));
    String sMonth = checkdate(rd.getParameter(beginOrEnd+"_month"));
    String sYear = rd.getParameter(beginOrEnd+"_year");


    try {
        rval = Timestamp.valueOf(sYear + "-" + sMonth + "-" + sDate + " 00:00:00");
    }
    catch (NumberFormatException nfe) {
        LogService.log(LogService.ERROR, "NumberFormatException in NewsAdmin::createTimestamp, bad RuntimeData: " + nfe);
        // we should return null, but at this point no time to debug
    }
    return rval;
  }

  private boolean ErrorCheck (String NewsorTopic)
  {

    if (NewsorTopic != null && NewsorTopic.equals("topic"))
    {
      if (myTopicInfo.getTopicName() == null || myTopicInfo.getTopicName().trim().equals(""))
      {
        sError.add("topic");
        return true;
      }
      if (myTopicInfo.getTopicDescription() != null && myTopicInfo.getTopicDescription().length() > 250)
      {
        sError.add("top_descr");
        return true;
      }
    }
    else if (NewsorTopic != null && NewsorTopic.equals("Article1"))
    {
      boolean flag = false;
      if (newArticle.getTopicID() == null || newArticle.getTopicID().trim().equals(""))
      {
        sError.add("topic");
        flag = true;
      }
      if (newArticle.getTitle() == null || newArticle.getTitle().trim().equals("") || newArticle.getTitle().trim().length() > 100)
      {
        sError.add("title");
        flag = true;
      }
      if (newArticle.getAbstract() == null || newArticle.getAbstract().trim().equals("") || newArticle.getAbstract().trim().length() > 500)
      {
        sError.add("abstract");
        flag = true;
      }
      if (newArticle.getEndDate().before(newArticle.getBeginDate()))
      {
        sError.add("date");
        flag = true;
      }
      return flag;
    }
    else
    {
      if (newArticle.getStory() == null || newArticle.getStory().trim().equals(""))
      {
        sError.add("content");
        return true;
      }
      else
      {
          java.util.StringTokenizer st = new java.util.StringTokenizer(newArticle.getStory());
          if (st.countTokens() > 2500)
          {
              sError.add("story");
              return true;
          }
      }
    }
     /* if (Integer.parseInt(sEndMonth) < Integer.parseInt(sBeginMonth) && sEndYear.equals(sBeginYear))
      {
        sError.add("date");
        flag = true;
      }*/




   return false;
  }

  private String checkdate (String date)
  {
    if (date != null && !date.equals(""))
    {
      if (date.trim().length() == 1)
      {
        date = "0" + date;
      }
      return date;
    }
    return null;
  }


  private Element getPreviewTopicXML(Document doc)
  {

    Element PreviewTopicXMLElement = doc.createElement("PreviewTopicXML");

    Element TopicElement = doc.createElement("topic");
    TopicElement.appendChild(doc.createTextNode(myTopicInfo.getTopicName()));
    PreviewTopicXMLElement.appendChild(TopicElement);

    Element DescriptionElement = doc.createElement("description");
    DescriptionElement.appendChild(doc.createTextNode(myTopicInfo.getTopicDescription() != null ? myTopicInfo.getTopicDescription() : "&#160;"));
    PreviewTopicXMLElement.appendChild(DescriptionElement);

    return PreviewTopicXMLElement;

  }

  private Element getPreviewArticleXML(Document doc)
  {

      Element PreviewElement = doc.createElement("Article");
      // get Layout Type (1,2, or 3)
      PreviewElement.setAttribute("layout", newArticle.getLayoutType());

      Element TitleElement = doc.createElement("title");
      TitleElement.appendChild(doc.createTextNode(newArticle.getTitle()));
      PreviewElement.appendChild(TitleElement);

// *** JK - fix for TT03622 - don't show image if "no image" layout selected
//      if (newArticle.getImage() != null && !newArticle.getImage().equals(""))
      if (!newArticle.getLayoutType().equals("3") && newArticle.getImage() != null && !newArticle.getImage().equals(""))
      {
        Element ImageElement = doc.createElement("imagefile");
        ImageElement.appendChild(doc.createTextNode(newArticle.getImage()));
        PreviewElement.appendChild(ImageElement);

        staticData.put("NewsAdminServantFinished", "true");
      }
      Element AbstractElement = doc.createElement("abstract");
      AbstractElement.appendChild(doc.createTextNode(newArticle.getAbstract()));
      PreviewElement.appendChild(AbstractElement);


       PreviewElement.appendChild(parseContent (doc));

      Element BGMElement = doc.createElement("begin_date");
      BGMElement.appendChild(doc.createTextNode(getDate(newArticle.getBeginDate().toString())));
      PreviewElement.appendChild(BGMElement);

      Element EDMElement = doc.createElement("end_date");
      EDMElement.appendChild(doc.createTextNode(getDate(newArticle.getEndDate().toString())));
      PreviewElement.appendChild(EDMElement);


          Element AuthorElement = doc.createElement("author");
      AuthorElement.appendChild(doc.createTextNode(sUserName));
      PreviewElement.appendChild(AuthorElement);


    return PreviewElement;
  }

  private Element parseContent (Document doc)
  {

    Element StoryElement = doc.createElement("story");

    int foundNewLine = 0;
    int foundNewParagraph = 0;
    int before = 0;
    boolean parsingFinished = false;

    while (foundNewParagraph != -1)
    {
      // look for a new paragraph

      foundNewParagraph = newArticle.getStory().indexOf("\r\n\r\n",(foundNewParagraph+4));

      if (foundNewParagraph != -1)
      {
        Element ParagraphElement = doc.createElement("paragraph");
        ParagraphElement.appendChild(doc.createTextNode(newArticle.getStory().substring(before, foundNewParagraph)));
        StoryElement.appendChild(ParagraphElement);

        before = foundNewParagraph + 2;
      }

    }

    if (foundNewParagraph == -1 && before != 0)
    {
        Element ParagraphElement = doc.createElement("paragraph");
        ParagraphElement.appendChild(doc.createTextNode(newArticle.getStory().substring(before, newArticle.getStory().length())));
        StoryElement.appendChild(ParagraphElement);


    }
    else
    {
        Element ParagraphElement = doc.createElement("paragraph");
        ParagraphElement.appendChild(doc.createTextNode(newArticle.getStory()));
        StoryElement.appendChild(ParagraphElement);


    }
/**     org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
**/
    return StoryElement;
  }

  private Element getCreateArticleXML (Document doc)
  {

    Element newArticleElement = doc.createElement("newArticle");

    if (!sError.isEmpty())
    {
      Element ErrorElement = doc.createElement("error");
      newArticleElement.appendChild(ErrorElement);
    }

   // Drop down list box for topic Names (need to add groups to this!)
    newArticleElement.appendChild(listTopics (doc));
    // Drop down list boxes for dates
    newArticleElement.appendChild(getBegEndDate (doc, "begin"));
    newArticleElement.appendChild(getBegEndDate (doc, "end"));

    Element TitleElement = doc.createElement("title");
    if (sError.contains("title"))
    {
     TitleElement.setAttribute("error", "error");
        sError.remove("title");
    }

    TitleElement.appendChild(doc.createTextNode((newArticle.getTitle() != null ? newArticle.getTitle() : "")));
    newArticleElement.appendChild(TitleElement);

    Element AbstractElement = doc.createElement("abstract");
    if (sError.contains("abstract"))
    {
     AbstractElement.setAttribute("error", "error");
        sError.remove("abstract");
    }

    AbstractElement.appendChild(doc.createTextNode((newArticle.getAbstract() != null ? newArticle.getAbstract() : "")));
    newArticleElement.appendChild(AbstractElement);

    return newArticleElement;
  }


  private Document getCreateStoryScreen (Document doc, ChannelRuntimeData rd)
  {
    Element newArticleElement = doc.createElement("CreateStory");

    if (!sError.isEmpty())
    {
      Element ErrorElement = doc.createElement("error");
      newArticleElement.appendChild(ErrorElement);
    }

    if (newArticle.getImage() != null)
    {
     // image has been loaded
      Element ImageElement = doc.createElement("imagefile");
      ImageElement.appendChild(doc.createTextNode(newArticle.getImage()));
      newArticleElement.appendChild(ImageElement);
    }

    Element StoryElement = doc.createElement("story");
    if (sError.contains("content"))
    {
     StoryElement.setAttribute("error", "error");
        sError.remove("content");
    }
    else if (sError.contains("story"))
    {
     StoryElement.setAttribute("error", "error1");
        sError.remove("story");
    }
    else if (sError.contains("nontextstory"))
    {
     StoryElement.setAttribute("error", "error2");
        sError.remove("nontextstory");
    }
    else if (sError.contains("nonimageicon"))
    {
      StoryElement.setAttribute("error", "error3");
      sError.remove("nonimageicon");
    }
    sError.clear();
    StoryElement.appendChild(doc.createTextNode((newArticle.getStory() != null ? newArticle.getStory() : "")));
    newArticleElement.appendChild(StoryElement);

    Element LayoutTypeElement = doc.createElement("layouttype");
    for (int i=0; i<3; i++)
    {
      Element LayoutElement = doc.createElement("layout");
      // if user has previously selected a layout type, then have it displayed as checked
      if (newArticle.getLayoutType() != null && newArticle.getLayoutType().equals(Integer.toString((i+1))))
        LayoutElement.setAttribute("selected", "yes");
      else if (newArticle.getLayoutType() == null)
        LayoutElement.setAttribute("selected", (i==2 ? "yes" : "no"));

      LayoutElement.setAttribute("id", Integer.toString((i+1)));
      LayoutTypeElement.appendChild(LayoutElement);
    }
    newArticleElement.appendChild(LayoutTypeElement);


    doc.appendChild(newArticleElement);
    xslfile = "CreateStory";


    return doc;
  }


  private Element getBegEndDate (Document doc, String Type)
  {
    boolean nextone = false;

    Element BegDateElement = doc.createElement(Type+"_date");

    if (sError.contains("date") && Type.equals("end"))
    {
      BegDateElement.setAttribute("error", "error");
      sError.remove("date");
    }

    Element MonthElement = doc.createElement("month");
    MonthElement.setAttribute("type", Type);

    //w.write ("<month type=\""+Type+"\">");
    rightNow.getInstance();
    int Year = rightNow.get(Calendar.YEAR);
    int DaysInMonth = rightNow.getActualMaximum(Calendar.DATE);
    Element OptionElement = null;

    String sBeginMonth, sBeginDate, sBeginYear, sEndMonth, sEndDate, sEndYear;
    try
    {
    // Null Pointer exception is thrown, then user has not entered anything so is null
    sBeginMonth = getDate(newArticle.getBeginDate().toString()).substring(0,2);
    sBeginDate = getDate(newArticle.getBeginDate().toString()).substring(3,5);
    sBeginYear = getDate(newArticle.getBeginDate().toString()).substring(6,10);
    sEndMonth = getDate(newArticle.getEndDate().toString()).substring(0,2);
    sEndDate = getDate(newArticle.getEndDate().toString()).substring(3,5);
    sEndYear = getDate(newArticle.getEndDate().toString()).substring(6,10);
    }catch (java.lang.NullPointerException npe)
    {
    sBeginMonth = null;
    sBeginDate = null;
    sBeginYear = null;
    sEndMonth = null;
    sEndDate = null;
    sEndYear = null;
    }

    // loop through months
    for (int i=0; i < 12; i++)
    {
      if (Type.equals("begin") && sBeginMonth != null)
      {
        if (i == ((new Integer (sBeginMonth).intValue()) - 1))
        {
          OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i + 1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
            MonthElement.appendChild(OptionElement);
          continue;
        }
      }
      else if (Type.equals("end") && sEndMonth != null)
      {      	
        if (i == ((new Integer (sEndMonth).intValue()) - 1))
        {
          OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i + 1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
            MonthElement.appendChild(OptionElement);
          continue;
        }

      }
      else
      {
        if (i == ((new Integer (getMonth()).intValue()) - 1))
        {
          if(Type.equals("begin"))
          {
         OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i+1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
            MonthElement.appendChild(OptionElement);
            continue;
          }
          else
          {
            nextone = true;
     OptionElement = doc.createElement("option");
          OptionElement.setAttribute("value", Integer.toString(i+1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
  MonthElement.appendChild(OptionElement);
//            w.write ("<option value=\"" + (i+1) +"\">" + (i+1) + "</option>");
            continue;
          }
        }
      }

      //should only happen once
      if (nextone)
      {
  OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i+1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
  MonthElement.appendChild(OptionElement);
//        w.write ("<option selected=\"yes\" value=\"" + (i+1) +"\">" + (i+1) + "</option>");
        nextone = false;
      }
      else
      {
 OptionElement = doc.createElement("option");
          OptionElement.setAttribute("value", Integer.toString(i+1));
          OptionElement.appendChild(doc.createTextNode(getMonthAsString(i)));
  MonthElement.appendChild(OptionElement);
//        w.write ("<option value=\""+(i+1)+"\">" + (i+1) + "</option>");
      }
    }
  //  w.write ("</month>");

BegDateElement.appendChild(MonthElement);

      Element DayElement = doc.createElement("day");
    DayElement.setAttribute("type", Type);

    Calendar oneMonthFromNow = new GregorianCalendar();
    oneMonthFromNow.add(Calendar.MONTH, 1);
    int DaysInNextMonth = oneMonthFromNow.getActualMaximum(Calendar.DATE);

    if (Type.equals("begin"))
    {
      for (int i=1; i < (DaysInMonth+1); i++)
      {
        if (sBeginDate != null)
        {
          if (i == new Integer (sBeginDate).intValue())
          {
            OptionElement = doc.createElement("option");
            OptionElement.setAttribute("selected", "yes");
            OptionElement.setAttribute("value", Integer.toString(i));
            OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
            DayElement.appendChild(OptionElement);
            continue;
          }
        }
        else
        {
          if (i == new Integer (getDay()).intValue())
          {
            OptionElement = doc.createElement("option");
            OptionElement.setAttribute("selected", "yes");
            OptionElement.setAttribute("value", Integer.toString(i));
            OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
            DayElement.appendChild(OptionElement);
            continue;
          }
        }
        OptionElement = doc.createElement("option");
        OptionElement.setAttribute("value", Integer.toString(i));
        OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
        DayElement.appendChild(OptionElement);
      }
    }
    else // Type.equals("end")
    {
      for (int i=1; i < (DaysInNextMonth+1); i++)
      {
        if (sEndDate != null)
        {
          if (i == new Integer (sEndDate).intValue())
          {
            OptionElement = doc.createElement("option");
            OptionElement.setAttribute("selected", "yes");
            OptionElement.setAttribute("value", Integer.toString(i));
            OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
            DayElement.appendChild(OptionElement);
            continue;
          }
        }
        else
        {
          if (i == new Integer (getDay()).intValue())
          {
            OptionElement = doc.createElement("option");
            OptionElement.setAttribute("selected", "yes");
            OptionElement.setAttribute("value", Integer.toString(i));
            OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
            DayElement.appendChild(OptionElement);
            continue;
          }
        }
        OptionElement = doc.createElement("option");
        OptionElement.setAttribute("value", Integer.toString(i));
        OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
        DayElement.appendChild(OptionElement);
      }
    }
// *** JK - end new code

   // w.write ("</day>");

   BegDateElement.appendChild(DayElement);


 Element YearElement = doc.createElement("year");
    YearElement.setAttribute("type", Type);
//    w.write ("<year type=\""+Type+"\">");

    if(Type.equals("end")){
    	Year = oneMonthFromNow.get(Calendar.YEAR);
    }

    for(int i=Year; i < (Year+3); i++)
    {
      if (Type.equals("begin") && sBeginYear != null)
      {
        if (i == new Integer (sBeginYear).intValue())
        {
       OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i));
          OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
  YearElement.appendChild(OptionElement);
     //     w.write ("<option selected=\"yes\" value=\""+i+"\">" + i + "</option>");
          continue;
        }
      }
      else if (Type.equals("end") && sEndYear != null)
      {
        if (i == new Integer (sEndYear).intValue())
        {
       OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i));
          OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
  YearElement.appendChild(OptionElement);
 //         w.write ("<option selected=\"yes\" value=\""+i+"\">" + i + "</option>");
          continue;
        }
      }
      else
      {
        if (i == new Integer (Year).intValue())
        {
       OptionElement = doc.createElement("option");
          OptionElement.setAttribute("selected", "yes");
          OptionElement.setAttribute("value", Integer.toString(i));
          OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
  YearElement.appendChild(OptionElement);
 //         w.write ("<option selected=\"yes\" value=\""+i+"\">" + i + "</option>");
          continue;
        }
      }
       OptionElement = doc.createElement("option");
          OptionElement.setAttribute("value", Integer.toString(i));
          OptionElement.appendChild(doc.createTextNode(Integer.toString(i)));
  YearElement.appendChild(OptionElement);
 //     w.write ("<option value=\""+i+"\">" + i + "</option>");
    }

 //   w.write("</year>\n");
   // w.write("</"+Type+"_date>\n");


BegDateElement.appendChild(YearElement);

    return BegDateElement;
  }

  private NewsInfo getNewsInfo (String sNewsID)
  {
    try
    {
      NewsInfo mynews = (NewsInfo) newsDB.getNewsInfo(sNewsID);
      return mynews;
    } catch (java.sql.SQLException sqe)
    {
    LogService.instance().log (LogService.ERROR, "NewsAdmin:getNewsInfo threw SQLException:"+sqe);
     return new NewsInfo ();
    }

  }

  private String getDate (String date)
  {
    String year = date.substring(0,4);
    String month = date.substring(5,7);
    String day = date.substring(8,10);

    String newDate = month + "/" + day + "/" + year;
    return newDate;
  }

  private boolean saveNewsItem()
  {

    try{

    if (newArticle.getAuthor() == null)
      newArticle.setAuthor(Integer.toString(staticData.getPerson().getID()));
    String newsID = newsDB.createNews(newArticle);

    if(newsID != null && !newsID.equals(""))
    {

      newArticle = new NewsInfo ();
      staticData.put("NewsAdminServantFinished", "false");
      return true;
    }
    }
    catch (java.sql.SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Error Saving News Item " + sqe);
      sError.add(sqe.toString());
      return false;
    }
    return false;
  }

  private boolean saveEditedNewsItem()
  {
    int success = 0;

    try
    {
      success = newsDB.updateNewsItem(newArticle);
    }catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:updateNewsItem threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(success == 0)
      return false;

    return true;
  }

  private boolean saveEditedTopic ()
  {

    int success = 0;
    try
    {
      success = newsDB.updateTopic(myTopicInfo.editTopicID, myTopicInfo.getTopicName(), myTopicInfo.getTopicDescription());
    }
    catch (java.sql.SQLException sqe)
    {
     LogService.instance().log (LogService.ERROR, "NewsAdmin:updateTopic threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(success == 0)
      return false;

    try{
      //  clear out GroupID with TopicID that is being modified
      newsDB.removeTopicGroupIDs(myTopicInfo.editTopicID);
      String userID = Integer.toString(staticData.getPerson().getID());
      // recreate entry with new groupsSelected
      newsDB.setTopicGroups(myTopicInfo.editTopicID, userID, myTopicInfo.getSelectedGroups());
    }catch (java.sql.SQLException sqe)
    {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:setTopicGroups threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    // clear out hashtable and reconstruct it
    listTopics ();
    return true;

  }

  private boolean saveTopic ()
  {
    newsDB = (NewsDb) staticData.get("NewsDB");

    String topicID;
    try
    {
      topicID = newsDB.createTopic(myTopicInfo.getTopicName(), myTopicInfo.getTopicDescription());
    }
    catch (java.sql.SQLException sqe)
    {
       LogService.instance().log (LogService.ERROR, "NewsAdmin:createTopic threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;
    }

    if(topicID != null && !topicID.equals(""))
    {
      try{
        String userID = Integer.toString(staticData.getPerson().getID());
        newsDB.setTopicGroups(topicID, userID, myTopicInfo.getSelectedGroups());
      }catch (java.sql.SQLException sqe)
      {
      LogService.instance().log (LogService.ERROR, "NewsAdmin:setTopicGroups threw SQLException:"+sqe);
      sError.add(sqe.toString());
      return false;

      }

      // clear out hashtable and reconstruct it
      listTopics ();
      return true;
    }
    return false;
  }


 private ArrayList getMyGroups (String userID)
  {
 ArrayList myGroupsList = new ArrayList ();
    try
    {

      IEntityGroup ie = GroupService.getRootGroup(Class.forName("org.jasig.portal.security.IPerson")); // the type

      Iterator iter = ie.getMembers();
      while (iter.hasNext())
      {
        IGroupMember member = (IGroupMember)iter.next();
        if (member.isGroup())
        {
          IEntityGroup memberGroup = (IEntityGroup)member;
          String groupkey = memberGroup.getKey();
          String name = memberGroup.getName();
//          IamMember (myGroupsList, groupkey);
          IEntityGroup group = GroupService.findGroup(groupkey);
          checkSubGroup (group, userID, myGroupsList);
        }
        else if(member.isEntity())
        {
          // Do something with this child entity.
          String id = member.getKey(); // it is the id in table UP_USER
          if (id.equals(userID))
          {
             myGroupsList.add(ie.getKey());
          }

        }
     }
   // will use this if I decide to create a TopicsAdmin and PublishAdmin group
   // IEntityGroup ie = GroupService.findGroup(groupID);

    }
    catch (GroupsException ge)
    {

    return myGroupsList;
    }
    catch (java.lang.ClassNotFoundException cnfe)
    {

    return myGroupsList;
    }

    return myGroupsList;
  }

  private void checkSubGroup (IEntityGroup group, String userID, ArrayList myGroupsList) throws GroupsException
  {

     Iterator iter = group.getMembers();
    while (iter.hasNext())
    {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup())
      {
        IEntityGroup memberGroup = (IEntityGroup)member;

        String key = memberGroup.getKey();
        String name = memberGroup.getName();


          IEntityGroup groupsel = GroupService.findGroup(key);
          checkSubGroup (groupsel, userID, myGroupsList);
      }
      else if(member.isEntity())
      {
        // Do something with this child entity.
        String id = member.getKey(); // it is the id in table UP_USER
          if (id.equals(userID))
          {
             myGroupsList.add(group.getKey());
          }

      }
    }
  }

  private Element listTopics (Document doc)
  {
    // remove all elements
    topicNames.clear();

    // retrieve all Topics that this user has access to
    // first get users GroupIDs
    //String userID = Integer.toString(staticData.getPerson().getID());

    String userID = vr.getUserKeyColumnByPortalVersions(staticData.getPerson());
    ArrayList myGroupsFound = gs.getMyGroups (userID);
    TopicList list = null;

 Element TopicListElement = doc.createElement("topic_list");
    try
    {
      // get ArrayList of TopicID's shared to me or groups I exists in
      ArrayList idsFound = newsDB.getSharedTopicID (userID, myGroupsFound);

      if (idsFound.isEmpty())
      {
        Element EmptyElement = doc.createElement("Empty");
        EmptyElement.appendChild(doc.createTextNode("No Topics listed."));
        TopicListElement.appendChild(EmptyElement);
        return TopicListElement;
      }
      // get a TopicList Object of the topicIDs found
      list = newsDB.getTopicList(idsFound);
    }
    catch (java.sql.SQLException sqe)
    {

    }


    if (sError.contains("topic"))
    {
     TopicListElement.setAttribute("error", "error");
     sError.remove("topic");
    }


    while (list.next())
    {
      String sTopicName = list.getTopicName();
      String sTopic_ID = list.getTopicID();
      topicNames.put(sTopic_ID, sTopicName);

      if (newArticle.getTopicID() != null && !newArticle.getTopicID().equals("") && sTopic_ID.equals(newArticle.getTopicID()))
      {

        Element OptionElement = doc.createElement("option");
        OptionElement.setAttribute("value", sTopic_ID);
        OptionElement.setAttribute("selected", "yes");
        OptionElement.appendChild(doc.createTextNode(sTopicName));
        TopicListElement.appendChild(OptionElement);
        continue;
      }

        Element OptionElement = doc.createElement("option");
        OptionElement.setAttribute("value", sTopic_ID);
        OptionElement.appendChild(doc.createTextNode(sTopicName));
        TopicListElement.appendChild(OptionElement);

//      w.write ("<option value=\"" + sTopic_ID + "\">" + sTopicName + "</option>");

    }

    return TopicListElement;
  }

  private void listTopics()
  {
    // remove all elements
    topicNames.clear();

    TopicList list = newsDB.getTopicList();

    while (list.next())
    {
      String sTopicName = list.getTopicName();
      String sTopicID = list.getTopicID();
      topicNames.put(sTopicID, sTopicName);
    }

  }

  private void resetall ()
  {
    //reset object classes
    if (myTopicInfo != null)
      myTopicInfo = new TopicInfo ();

    if (newArticle != null)
      newArticle = new NewsInfo ();

  }

  private String getMonthAsString (int month)
  {
    String MonthAsString = null;
    switch (month)
    {
      case 0:  MonthAsString = "January";break;
      case 1:  MonthAsString = "Febuary";break;
      case 2:  MonthAsString = "March";break;
      case 3:  MonthAsString = "April";break;
      case 4:  MonthAsString = "May";break;
      case 5:  MonthAsString = "June";break;
      case 6:  MonthAsString = "July";break;
      case 7:  MonthAsString = "August";break;
      case 8:  MonthAsString = "September";break;
      case 9:  MonthAsString = "October";break;
      case 10:  MonthAsString = "November";break;
      case 11:  MonthAsString = "December";break;
    }
    return MonthAsString;
  }


  // IServant method
  public boolean isFinished ()
  {
    boolean isFinished = false;
    if (staticData.containsKey("NewsAdminServantFinished") && staticData.getParameter("NewsAdminServantFinished").equals("true"))
      isFinished = true;
    return  isFinished;
  }

  // IServant method
  public Object[] getResults ()
  {
    Object[] results = new Object[1];

    results[0] = newArticle;

    return results;
  }



 public class TopicInfo
 {
   // edit variables
   private String currentState;
   private String editTopicID;

   private String SearchString;
   private String getSearchString () { return SearchString;}
   private void setSearchString (String search) { SearchString = search;}

   private TopicList SearchList;
   private TopicList getSearchList () { return SearchList;}
   private void setSearchList (TopicList listFound) { SearchList = listFound;}

   private ArrayList ArticleSearchList;
   private ArrayList getArticleSearchList () { return ArticleSearchList;}
   private void setArticleSearchList (ArrayList listFound) { ArticleSearchList = listFound;}

   private String ArticleSearchString;
   private String getArticleSearchString () { return ArticleSearchString;}
   private void setArticleSearchString (String search) { ArticleSearchString = search;}

   private Timestamp ArticleSearchBegDate;
   private Timestamp getArticleSearchBegDate () { return ArticleSearchBegDate;}
   private void setArticleSearchBegDate (Timestamp begdate) { ArticleSearchBegDate = begdate;}

   private Timestamp ArticleSearchEndDate;
   private Timestamp getArticleSearchEndDate () { return ArticleSearchEndDate;}
   private void setArticleSearchEndDate (Timestamp enddate) { ArticleSearchEndDate = enddate;}

   private String ArticleSearchType;
   private String getArticleSearchType () { return ArticleSearchType;}
   private void setArticleSearchType (String search) { ArticleSearchType = search;}

   private ArrayList DeleteList;
   private ArrayList getDeleteList () { return DeleteList;}
   private void setDeleteList (ArrayList deletelist) { DeleteList = deletelist;}

   private String TopicName;
   private String getTopicName () { return TopicName;}
   private void setTopicName (String topicname) { TopicName = topicname;}

   private String TopicDescription;
   private String getTopicDescription () { return TopicDescription;}
   private void setTopicDescription (String topicdescription) { TopicDescription = topicdescription;}

   private ArrayList selectedGroupsList;
   private ArrayList getSelectedGroups () { return selectedGroupsList; }
   private void setSelectedGroups (ArrayList selection) { selectedGroupsList = selection;}

   public TopicInfo ()
   {
     selectedGroupsList = new ArrayList ();
     ArticleSearchList = new ArrayList ();
     SearchList = new TopicList ();
     DeleteList = new ArrayList ();

   }
 }
}


