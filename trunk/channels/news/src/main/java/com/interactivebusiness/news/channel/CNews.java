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
 *   2    Channels  1.1         12/20/2001 3:54:08 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:39 AMFreddy Lopez
 *  $
 *
 */


package com.interactivebusiness.news.channel;

//import org.jasig.portal.*;
import org.jasig.portal.IPermissible;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.IChannel;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.xml.sax.InputSource;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.sql.Timestamp;
import java.sql.SQLException;
import com.interactivebusiness.news.data.*;

import org.jasig.portal.services.GroupService;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.GroupsException;
import com.interactivebusiness.news.data.UserConfig;

// Added by Alex
//import net.unicon.portal.common.properties.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.PropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.UniconProperties;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrChannelRuntimeData;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.channels.permissionsmanager.RDBMPermissibleRegistry;


/**
 * This is the News Subscriber channel.
 * Users can subscribe and view new channels by topic names.
 * @author Freddy Lopez, flopez@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class CNews extends BaseChannel implements IMimeResponse, IPrivilegedChannel, ICacheable, IPermissible
{
  private static NewsDb newsDB;
  private final XSLT xslt;
  private static final String sslLocation = "CNews/CNews.ssl";
  private String sAction;
  private String sUserID;
  private String sCheckBox;
  private String xslfile;
  private Hashtable topicNames = new Hashtable();
  private ArrayList subscribeList = new ArrayList ();
  private TopicList topicList = new TopicList();
  private ArrayList ArticleIDList = new ArrayList ();
  // Freddy Added 02/05/02
  private IServant servant;
  private UserConfig userConfig;
  private static final String fs = File.separator;
  // IPrivileged Interface
  private PortalControlStructures pcs;
  private boolean IDLISTCHANGED = false;
  private boolean KILL_CACHE = false;

// *** Alex - fix for TT03858
  private static String DATASOURCE;
  private static String datastore;


  static final String CHANNEL = "AcademusCampusNews";
  private static HashMap activities = null;
  private static HashMap targets = null;
  public static final String VIEW_TOPICS = "VIEW";

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

      activities = new HashMap();
      activities.put(VIEW_TOPICS, "User may view/edit news topics.");

      targets = new HashMap();
      targets.put("TOPICS_ICON", "Manage Topics");
  }
// *** end fix for TT03858


  public CNews ()
  {
    this.xslt = new XSLT(this);
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    this.staticData = sd;

    if (newsDB == null)
    {
      // *** Alex - fix for TT03858
      // String DATASOURCE = staticData.getParameter("datasource").trim();

      // The synchronization is omitted here since we are creating only a singleton.
      // The ideal case is to put newsDB object as a stateless session bean.
      // Read the properties from the news.properties file.
      try
      {
        newsDB = NewsDb.getInstance(DATASOURCE);  // create the singleton object
      }
      catch (java.lang.NullPointerException npe)
      {
        LogService.instance().log (LogService.ERROR, "Datasource name is null, please check channel static parameters");
      }
      catch (Exception e)
      {
        LogService.instance().log (LogService.ERROR, "Exception connecting to database with datasource name "+DATASOURCE+".\n" );
      }
    }
    // setup topicNames hashtable
    listTopics ();
  }


  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    CscrChannelRuntimeData ccrd = new CscrChannelRuntimeData( this.staticData.getChannelPublishId(), rd );
    this.runtimeData = ccrd;

   try
   {
    if (sUserID == null)
      sUserID = Integer.toString(staticData.getPerson().getID());

    sAction = rd.getParameter("action");
    String sAdmin = rd.getParameter("admin");

  // getHttpServletRequest if user wants to navigate through articles
  String value = this.pcs.getHttpServletRequest().getParameter("view");
  if (value != null && value.equals("nextprev"))
  {
   // user has chosen to view next or prev
   sAction =  this.pcs.getHttpServletRequest().getParameter("action");

  }

  // testing for problem in previewArticle screen
 String value2 = this.pcs.getHttpServletRequest().getParameter("action");
  if (value2 != null && value2.equals("SaveArticle"))
  {
   // user has chosen a command from the previewArticle screen
   sAdmin = "yes";
   sAction =  value2;
   KILL_CACHE = true;
  }

  if (sAdmin != null && sAdmin.equals("yes"))
   {
      KILL_CACHE = true;
      // Create communication with NewsAdmin
      servant = getNewsAdminServant (ccrd);

      if (value2 != null && value2.equals("SaveArticle"))
      {

        ccrd.setParameter("action", value2);
        ccrd.setParameter("next", this.pcs.getHttpServletRequest().getParameter("next"));
        ccrd.setParameter("back", this.pcs.getHttpServletRequest().getParameter("back"));
        ccrd.setParameter("cancel", this.pcs.getHttpServletRequest().getParameter("cancel"));
      }

      ((IChannel) servant).setRuntimeData(ccrd);

  }
  else
  {
      if (servant != null && servant.isFinished())
      {
      // must only make this execute when in NewsAdmin mode
        Object[] results = servant.getResults();
        if (results != null && results.length > 0)
        {
            if (((NewsInfo)results[0]).getID() != null)
            staticData.put("NewsInfoObject", results[0]);
        }
     }
  }
  }
  catch (Exception e)
  {
      e.printStackTrace();
     LogService.instance().log (LogService.ERROR, "Exception occurred while trying to get wallet credentials: " + e);
  }
  } //ends method


  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (org.xml.sax.ContentHandler out) throws PortalException
  {
    ChannelRuntimeData rd = this.runtimeData;
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    if (rd.getParameter("admin") == null)
    {
      staticData.remove("NewsAdminServant");
      staticData.put("NewsAdminServantFinished", "false");
    }
    // Freddy Added 02/05/02
    NewsAdmin servant = (NewsAdmin)staticData.get("NewsAdminServant");
    if (servant != null)
      servant.renderXML(out);
    else
    {
    if (sAction == null || sAction.equals(""))
    {
      IDLISTCHANGED = false;
      doc = getInitScreen (doc);

    }
    else if (sAction.equals("subscribe"))
    {
      sCheckBox = rd.getParameter("checkbox");
      doc = getSubscribeScreen (doc);

    }
    else if (sAction.equals("save_subscribed"))
    {
      if (rd.getParameter("subscribe") != null)
      {
        parseForm (rd);
        doc = getSavedSubscribedScreen (doc);
        sCheckBox = null;
        this.KILL_CACHE = true;
      }
      else
      {
        doc = getInitScreen (doc);
      }
    }
    else if (sAction.equals("view_full_article"))
    {
      String articleID = rd.getParameter("articleID");

      if (articleID == null)
        articleID = this.pcs.getHttpServletRequest().getParameter("articleID");

      doc = getViewFullArticleScreen (doc, articleID);
      xslfile = "view_full_article";
    }
    else if (sAction.equals("configure_news"))
    {
      doc = getConfigureNewsScreen (doc);
      xslfile = "configure_news";
    }
    else if (sAction.equals("save_configuration"))
    {
      if (rd.getParameter("save") != null)
      {
        // user has saved configs
        saveConfig (rd);
        doc = getInitScreen (doc);
        this.KILL_CACHE = true;
     }
     else
     {
       doc = getInitScreen (doc);
     }
    }
    
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, xslfile, runtimeData.getBrowserInfo());
    xslt.setTarget(out);

    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    
    xslt.setStylesheetParameter("mayManageTopics"
            , Toolbar.checkPermission(staticData.getPerson()) ? "true" : "false");
    xslt.setStylesheetParameter("mayManageArticles"
            , Toolbar.canPublishArticles(staticData.getPerson(), newsDB.getAllGroupIDs()) ? "true" : "false");
    //String w = runtimeData.getWorkerActionURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
    String w = runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
    if (w.indexOf("worker") != -1)
    {
      w = w.substring(w.indexOf("worker"));
    }
  xslt.setStylesheetParameter("resourceURL", w);
//    xslt.setStylesheetParameter("resourceURL", runtimeData.getWorkerActionURL(UPFileSpec.FILE_DOWNLOAD_WORKER));
    xslt.transform();
  }

  }

  private IServant getNewsAdminServant (ChannelRuntimeData rd)
  {
    try
    {
      if (staticData.get("NewsAdminServant") == null)
      {
        servant = (IServant) new NewsAdmin ();
        ChannelStaticData servantStatic = (ChannelStaticData) staticData.clone();
        servantStatic.put("NewsDB", newsDB);
        servantStatic.put("pcs", this.pcs);
        ((NewsAdmin)servant).setStaticData(servantStatic);
        staticData.put("NewsAdminServant", servant);

      }
      else
      {
        servant = (IServant) staticData.get("NewsAdminServant");
      }    
      
      this.IDLISTCHANGED = false;
    }
    catch (Exception e)
    {
     return null;
    }

    return servant;
  }

  private void checkIfFinished ()
  {

    if (servant.isFinished())
    {
      Object[] results = servant.getResults();
      if (results != null && results.length > 0)
      {
//        boolean successful = checkCredentials ((HashMap) results[0]);
      }
      else
      {
//        ((WalletBase)servant).setLoginError(true);
//        m_csd.setParameter("WalletServantFinished","false");
      }
    }

  }

  private void saveConfig (ChannelRuntimeData rd)
  {

    String layout = rd.getParameter("layout");
    String itemPerTopic = rd.getParameter("ItemsPerTopic");

    userConfig.setLayout(layout != null ? layout : "1");
    userConfig.setItemsPerTopic((itemPerTopic != null && !itemPerTopic.equals("")) ? itemPerTopic : "4");

    // save configurations to DB
    try
    {
      newsDB.updateUserConfig(userConfig, sUserID);
    }catch (java.sql.SQLException sqe)
    {
      LogService.instance().log(LogService.ERROR, "Unable to save to user_config table for userID = " + sUserID);
    }

  }

  private Document getConfigureNewsScreen (Document doc)
  {
    Element ConfigureNewsElement = doc.createElement("ConfigureNews");
    if (userConfig == null)
    {
      UserConfig uc = newsDB.getUserConfig (sUserID);
      userConfig = new UserConfig (sUserID, uc);
    }
    if (userConfig.getLayout() != null)
      ConfigureNewsElement.setAttribute("layout", userConfig.getLayout());
    if (userConfig.getItemsPerTopic() != null)
      ConfigureNewsElement.setAttribute("itemsPerTopic", userConfig.getItemsPerTopic());


   ConfigureNewsElement.appendChild(doc.createTextNode(""));
    doc.appendChild(ConfigureNewsElement);
    return doc;
  }

  private Document getViewFullArticleScreen (Document doc, String articleID) throws java.lang.NullPointerException
  {
    Element ViewFullElement = doc.createElement("View_Full");
    NewsInfo nInfo = null;

    if (!IDLISTCHANGED)
    {
      ArrayList newArticleIDList = new ArrayList ();
      
      TopicList topicList = newsDB.getSubscribedTopics(sUserID);
      List topicIdList = new ArrayList(topicList.getCount());
      topicList.begin();
      while (topicList.hasMore()) {
          topicList.next();
          if (topicList.getTopicID() != null) {
              topicIdList.add(topicList.getTopicID());
          }
      }
      String topicId = null;
      
      Map subscribedNewsItems = null;

      try {
          subscribedNewsItems = newsDB.getNewsItems(topicIdList);
      } catch (java.sql.SQLException sqe) {
         LogService.instance().log (LogService.ERROR, "News:getNewsItems threw SQLException:"+sqe, sqe);
      }

      Iterator itr = subscribedNewsItems.keySet().iterator();
      while (itr.hasNext()) {
        topicId = (String)itr.next();
        List subscribeList = (List)subscribedNewsItems.get(topicId);

        if (subscribeList != null && subscribeList.size() > 0)
        {
          for(int i=0; i < subscribeList.size(); i++)
          {
            NewsInfo mynews = (NewsInfo) subscribeList.get(i);

            if (!newArticleIDList.contains(mynews.getID()))
              newArticleIDList.add(mynews.getID());
            
            if(nInfo == null && mynews.getID().equals(articleID)){
                nInfo = mynews;
            }
          }
        }       
      }

      //if (newArticleIDList.size() != ArticleIDList.size())
      //{
        ArticleIDList.clear();
        ArticleIDList = newArticleIDList;
        IDLISTCHANGED = true;
      //}
    }
    if(nInfo == null){
        try
        {
            nInfo = newsDB.getNewsInfo(articleID);
        }
        catch (java.sql.SQLException sqe)
        {
        }
    }
    ViewFullElement.appendChild(getNextPrev (doc, articleID));
    ViewFullElement.appendChild(getFullArticle (doc, nInfo));

    doc.appendChild(ViewFullElement);


/**       org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();
      String result = printString.serializeNode(doc);
      System.out.println ("DOM AS STRING == " + result + "\n");
**/
    return doc;
  }

  private Document getInitScreen (Document doc) throws java.lang.NullPointerException
  {
    if (userConfig == null)
    {
      UserConfig uc = newsDB.getUserConfig (sUserID);
      userConfig = new UserConfig (sUserID, uc);
    }
    Element InitialScreenElement = doc.createElement("News_Main");

    InitialScreenElement.setAttribute("layout", userConfig.getLayout() != null ? userConfig.getLayout() : "1");
    //InitialScreenElement.setAttribute("itemsPerTopic", userConfig.getItemsPerTopic()!=null ? userConfig.getItemsPerTopic() : "4");
    // display toolbar with admin icons if user has admin privledges
    // otherwise just show one icon to subscribe to News Topics
    Toolbar mytoolbar = new Toolbar ();
    // insert Toolbar
     InitialScreenElement.appendChild(displayToolBar (doc, mytoolbar));
    // will make the top caption (heading) and the image static parameters
    String NEWSLOGO = staticData.getParameter("imagename");
    String NEWSCAPTION = staticData.getParameter("caption");

    // Allow for use of custom logo
    Element ImageElement = doc.createElement("imagename");
    // Insert institution logo if exists else use default logo
    if (NEWSLOGO == null)
      ImageElement.appendChild(doc.createTextNode("campusnews.gif"));
    else
      ImageElement.appendChild(doc.createTextNode(NEWSLOGO));
    InitialScreenElement.appendChild(ImageElement);

    // Allow for custom caption
    Element CaptionElement = doc.createElement("caption");
    // Insert institution logo if exists else use default logo
    if (NEWSCAPTION == null)
      CaptionElement.appendChild(doc.createTextNode("University Campus News."));
    else
      CaptionElement.appendChild(doc.createTextNode(NEWSCAPTION));
    InitialScreenElement.appendChild(CaptionElement);

    // insert users subscribed list
    InitialScreenElement.appendChild(getSubscribedList (doc));

    doc.appendChild(InitialScreenElement);
    xslfile = "news_Main";
    return doc;
  }

  private Document getSavedSubscribedScreen (Document doc) throws java.lang.NullPointerException
  {
    Element SavedSubscribeScreenElement = doc.createElement("Saved");
    SavedSubscribeScreenElement.appendChild(getSavedList (doc));
    doc.appendChild(SavedSubscribeScreenElement);
    xslfile = "saved_subscribed_list";
    return doc;
  }

  private Document getSubscribeScreen (Document doc) throws java.lang.NullPointerException
  {
    Element SubscribeScreenElement = doc.createElement("Subscribe");
    SubscribeScreenElement.appendChild(listTopics (doc));
    doc.appendChild(SubscribeScreenElement);
    xslfile = "subscribe";
    return doc;
  }

  private Element displayToolBar (Document doc, Toolbar mytoolbar)
  {

    Element ToolBarElement = doc.createElement("toolbar");

    Element IconElement;
    
    this.runtimeData.setParameter("mayManageArticles", "false");
    this.runtimeData.setParameter("mayManageTopics", "false");
    try
    {
    // display subscribe icon (group = Everyone)
    // read group type from staticData
    IconElement = mytoolbar.displayIcon ("subscribeIcon", doc);
    if (IconElement != null)
      ToolBarElement.appendChild(IconElement);

    // check if user is Topic Administrator, Will check UP_PERMISSION TABLE
    if (Toolbar.checkPermission(staticData.getPerson()))
    {
      IconElement = doc.createElement("topicIcon");
      ToolBarElement.appendChild(IconElement);      
    }

    // If user has access to publish news articles, then display icon
    IconElement = mytoolbar.displayIcon ("publishIcon", staticData.getPerson(), newsDB.getAllGroupIDs(), doc);
    if (IconElement != null){
      ToolBarElement.appendChild(IconElement);      
    }

    // display config icon (group = Everyone)
    // read group type from staticData
    IconElement = mytoolbar.displayIcon ("configIcon", doc);
    if (IconElement != null)
      ToolBarElement.appendChild(IconElement);

   }catch (GroupsException ge)
   {
     IconElement = doc.createElement("error");
     IconElement.appendChild(doc.createTextNode("No toolbar to disply, Groups Exception!"));
     ToolBarElement.appendChild(IconElement);
   }
    return ToolBarElement;
  }
  
  private Element getSavedList (Document doc)
  {
    Element SubscribedListElement = doc.createElement("subscribed_list");
    try
    {
        TopicList topicList = newsDB.getSubscribedTopics(sUserID);
        
      // User has no topics subscribed
      if (!topicList.hasMore())
      {
        Element EmptyElement = doc.createElement("Empty");
        EmptyElement.appendChild(doc.createTextNode("Successfully unsubscribed to news topics."));
        SubscribedListElement.appendChild(EmptyElement);
      }

      // loop through list and display all
      while (topicList.next())
      {
        Element TopicElement = doc.createElement("topic");
        TopicElement.appendChild(doc.createTextNode(topicList.getTopicName()));
        SubscribedListElement.appendChild(TopicElement);

      }
    }
    catch (java.lang.NullPointerException npe)
    {

    }
    return SubscribedListElement;
  }

  private Element getSubscribedList (Document doc)
  {
    // remove all in list if previously used
    subscribeList.clear();

    Element SubscribedListElement = doc.createElement("subscribedlist");
    //SubscribedListElement.setAttribute("itemsPerTopic", userConfig.getItemsPerTopic() != null ? userConfig.getItemsPerTopic() : "4");

    // Read all topics subscribed by user
    try
    {
      TopicList topicList = newsDB.getSubscribedTopics(sUserID);
      List topicIdList = new ArrayList(topicList.getCount());
      topicList.begin();
      // User has no topics subscribed
      if (!topicList.hasMore()) {
        Element EmptyElement = doc.createElement("Empty");
        EmptyElement.appendChild(doc.createTextNode("No Subscribed News."));
        SubscribedListElement.appendChild(EmptyElement);
      } else {

        while (topicList.hasMore()) {
          topicList.next();
          if (topicList.getTopicID() != null) {
            topicIdList.add(topicList.getTopicID());
          }
        }
        String topicId = null;
      
        Map subscribedNewsItems = newsDB.getNewsItems(topicIdList);

        // loop through list and display all
        topicList.begin();
        while (topicList.hasMore()) {
            topicList.next();
          topicId = topicList.getTopicID();
          if (topicId != null) {
          List subscribeList = (List)subscribedNewsItems.get(topicId);

          if (subscribeList != null && subscribeList.size() > 0) {
            Element TopicElement = doc.createElement("Topic");
            TopicElement.setAttribute("name", topicList.getTopicName());

            for(int i=0; i < subscribeList.size(); i++) {
              NewsInfo mynews = (NewsInfo) subscribeList.get(i);

              Element ArticleElement = doc.createElement("Article");
              ArticleElement.setAttribute("articleID", mynews.getID());
              ArticleElement.setAttribute("title", mynews.getTitle());
              ArticleElement.setAttribute("abstract", mynews.getAbstract());
              TopicElement.appendChild(ArticleElement);

              // Add ID to ArrayList. Used to navigate in Full-Article view mode
              // First check if it exists
              if (!ArticleIDList.contains(mynews.getID()))
                ArticleIDList.add(mynews.getID());

            }
            SubscribedListElement.appendChild(TopicElement);
          }
          //subscribeList.clear();
        }
        }
      }
    } catch (java.sql.SQLException sqe) {
      LogService.instance().log (LogService.ERROR, "News:getNewsItems threw SQLException:"+sqe, sqe);
    } catch (java.lang.NullPointerException npe) {
      LogService.instance().log (LogService.ERROR, npe);
    }

    return SubscribedListElement;
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

  private Element listTopics (Document doc)
  {
    // remove all elements
    topicNames.clear();

    Element TopicListElement = doc.createElement("topic_list");
    try
    {
      // get a list of topics from database
      TopicList list = newsDB.getTopicList();

      // check to make sure there are topics to subscribe to
      if (!list.hasMore())
      {
        Element EmptyElement = doc.createElement("Empty");
        EmptyElement.appendChild(doc.createTextNode("No Topics listed."));
        TopicListElement.appendChild(EmptyElement);
      }

      // loop through list of topics
      while (list.next())
      {
        String sTopicName = list.getTopicName();
        String sTopicID = list.getTopicID();
        String sTopicDescription = list.getTopicDescription();

        topicNames.put(sTopicID, sTopicName);

        Element TopicElement = doc.createElement("topic");

        Element TopicNameElement = doc.createElement("topic_name");
        TopicNameElement.setAttribute("value", sTopicID);
        TopicNameElement.setAttribute("checked", checkIfSubscribed(sTopicID));
        TopicNameElement.appendChild(doc.createTextNode(sTopicName));
        TopicElement.appendChild(TopicNameElement);

        Element DescriptionElement = doc.createElement("description");
        DescriptionElement.appendChild(doc.createTextNode(sTopicDescription));
        TopicElement.appendChild(DescriptionElement);

        Element ArticleCountElement = doc.createElement("article_count");
        ArticleCountElement.appendChild(doc.createTextNode(newsDB.getNewsItemsCount(sTopicID)));
        TopicElement.appendChild(ArticleCountElement);

        TopicListElement.appendChild(TopicElement);
      }
    }
    catch (java.lang.NullPointerException npe)
    {

    }
    return TopicListElement;
  }

  private String checkIfSubscribed (String sTopicID)
  {

    if (sCheckBox != null && sCheckBox.equals("all"))
      return "checked";
    else if (sCheckBox != null && sCheckBox.equals("none"))
      return "no";
    else
    {
        TopicList topicList = newsDB.getSubscribedTopics(sUserID);
        
      if (!topicList.hasMore())
        return "no";

      while (topicList.next())
      {
        if (topicList.getTopicID().equals(sTopicID))
          return "checked";
      }

      return "no";
    }
  }

  private void parseForm (ChannelRuntimeData rd)
  {
    // clear out if previously used
    subscribeList.clear();

    // must loop through topic because user can select multiple topics to subscribe
    Enumeration keys = rd.keys();

    while (keys.hasMoreElements())
    {
      String paramKey = (String) keys.nextElement();
      String value = (String) rd.getObjectParameter(paramKey);

      if (value.equals("check"))
        subscribeList.add(paramKey);
    }

    // built list...must now save to database
    if (!subscribeList.isEmpty())
    {
      newsDB.deleteAllSubscriptionTopics(sUserID);
      newsDB.subscribe(sUserID, subscribeList);
    }
    else
    {
      newsDB.deleteAllSubscriptionTopics(sUserID);
    }    
    
    this.ArticleIDList.clear();

  }

  private Element getFullArticle (Document doc, NewsInfo mynews)
  {

      Element FullArticleElement = doc.createElement("fullarticle");

      if (mynews != null)
      {

        // get Article Layout (1=imageright,2=imageleft, or 3=noimage)
        String articleLayout = mynews.getLayoutType();
        FullArticleElement.setAttribute("layout", articleLayout);

        Element BeginDateElement = doc.createElement("begindate");
        BeginDateElement.appendChild(doc.createTextNode(getDate (mynews.getBeginDate().toString())));
        FullArticleElement.appendChild(BeginDateElement);

        Element TitleElement = doc.createElement("title");
        TitleElement.appendChild(doc.createTextNode(mynews.getTitle()));
        FullArticleElement.appendChild(TitleElement);


        // if image exists display it

        if (mynews.getImage() != null && !mynews.getImage().equals(""))
        {
          Element ImageElement = doc.createElement("imagefile");
          ImageElement.appendChild(doc.createTextNode(mynews.getImage()));
          FullArticleElement.appendChild(ImageElement);
          staticData.put("NewsInfoObject", mynews);
        }
        // need to break story into 3 parts
        FullArticleElement.appendChild(parseContent (mynews.getStory(), doc));
      }

    return FullArticleElement;
  }

  private Element parseContent (String sContent, Document doc)
  {

    Element StoryElement = doc.createElement("story");

    int foundat = 0;
    int before = 0;
    while (foundat != -1)
    {
      foundat = sContent.indexOf("\r\n\r\n",(foundat+4));
      if (foundat != -1)
      {
        Element ParagraphElement = doc.createElement("paragraph");
        ParagraphElement.appendChild(doc.createTextNode(sContent.substring(before, foundat)));
        StoryElement.appendChild(ParagraphElement);
        before = foundat + 4;
      }
    }

    if (foundat == -1 && before != 0)
    {
      Element ParagraphElement = doc.createElement("paragraph");
      ParagraphElement.appendChild(doc.createTextNode(sContent.substring(before, sContent.length())));
      StoryElement.appendChild(ParagraphElement);
    }
    else
    {
      Element ParagraphElement = doc.createElement("paragraph");
      ParagraphElement.appendChild(doc.createTextNode(sContent));
      StoryElement.appendChild(ParagraphElement);
    }

    return StoryElement;
  }

  private String getDate (String date)
  {
    String year = date.substring(0,4);
    String month = date.substring(5,7);
    String day = date.substring(8,10);

    String newDate = month + "/" + day + "/" + year;
    return newDate;
  }

  private Element getNextPrev (Document doc, String sArticleID)
  {

    Element NextPrevBarElement = doc.createElement("nextprevbar");
    // first go to DB to see if ArticleIDList has changed

    if (ArticleIDList.size() > 0)
    {
      int foundat = ArticleIDList.indexOf(sArticleID);
      int total = ArticleIDList.size();

      Element ViewingElement = doc.createElement("viewing");
      ViewingElement.setAttribute("numberviewing", Integer.toString(foundat+1));
      ViewingElement.setAttribute("total", Integer.toString(total));
      NextPrevBarElement.appendChild(ViewingElement);

      if (foundat != (total-1))
      {
        Element NextElement = doc.createElement("next");
        NextElement.setAttribute("nextArticle",(String)ArticleIDList.get(foundat+1));
        NextElement.setAttribute("lastArticle",(String)ArticleIDList.get(total-1));
        NextPrevBarElement.appendChild(NextElement);

      }
      else
      {
        Element NextDisabledElement = doc.createElement("next_disabled");
        NextDisabledElement.appendChild(doc.createTextNode(""));
        NextPrevBarElement.appendChild(NextDisabledElement);
      }

      if (foundat != 0)
      {
        Element PreviousElement = doc.createElement("previous");
        PreviousElement.setAttribute("previousArticle", (String)ArticleIDList.get(foundat-1));
        PreviousElement.setAttribute("firstArticle", (String)ArticleIDList.get(0));
        NextPrevBarElement.appendChild(PreviousElement);
      }
      else
      {
        Element PreviousDisabledElement = doc.createElement("previous_disabled");
        PreviousDisabledElement.appendChild(doc.createTextNode(""));
        NextPrevBarElement.appendChild(PreviousDisabledElement);
      }

    }

    return NextPrevBarElement;
  }

  public ChannelCacheKey generateKey ()
  {

    ChannelRuntimeData runtimeData = this.runtimeData;
    ChannelCacheKey k = null;
    if (runtimeData.getParameter("action") == null)
    {
      k = new ChannelCacheKey();
      StringBuffer sbKey = new StringBuffer(1024);
      k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
      sbKey.append("com.interactivebusiness.news.channel.CNews: ");
      sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
      sbKey.append("stylesheetURI:");
      try {
        String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
        sbKey.append(XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo()));
      } catch (Exception e) {
        sbKey.append("not defined");
      }
      k.setKey(sbKey.toString());
    }
    return k;
  }

  /**
   * put your documentation comment here
   * @param validity
   * @return
   */
  public boolean isCacheValid (Object validity)
  {
    if (KILL_CACHE)
    {
      KILL_CACHE = false;
      return false;
    }
    return this.runtimeData.getParameter("action") == null;
  }


 // Implementation of org.jasig.portal.IMimeResponse methods.
  public void downloadData(OutputStream out) throws IOException
  {
  }

  public String getName()
  {
NewsInfo newArticle = (NewsInfo) staticData.get("NewsInfoObject");


    return newArticle.getImage();
  }

  public String getContentType()
  {
NewsInfo newArticle = (NewsInfo) staticData.get("NewsInfoObject");


    return newArticle.getImageContentType();
  }

  public InputStream getInputStream() throws IOException
  {
NewsInfo newArticle = (NewsInfo) staticData.get("NewsInfoObject");


    String userID = newArticle.getAuthor();

    // *** Alex - fix for TT03858
    // String repositoryPath = staticData.getParameter("datastore").trim();
    String repositoryPath = datastore.trim();

    if (repositoryPath != null)
    {
      if (!repositoryPath.endsWith(fs))
        repositoryPath += fs;

      StringBuffer sBuff = new StringBuffer(repositoryPath);
      // userID/
      sBuff.append(userID).append(fs);
      // userID/ArticleID
      sBuff.append(newArticle.getID());

      String fileDirectory = sBuff.toString();
      sBuff = null;  // help GC

      File userDirectory = new File (fileDirectory);
      if (userDirectory.exists())
      {
        File userFile = new File (fileDirectory + fs + newArticle.getImage());
        if (userFile.exists())
        {
          FileInputStream in = new FileInputStream (userFile);
          return in;
        }
      }
      fileDirectory = null; // help GC
    }
    throw new IOException ("File " + newArticle.getImage() + " not found in the repository");
  }

  /**
   * No Headers in the News channel.  Return a null.
   */
  public Map getHeaders()
  {
    return null;
  }

  // IPrivileged Interface
  public void setPortalControlStructures (PortalControlStructures pcs)
  {
    this.pcs = pcs;
  }


  // IPermissible methods
  public String getOwnerName() {
    return "Academus Campus News";
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
    LogService.log(LogService.ERROR, "CNews::reportDownloadError(): " + e.getMessage());
  }
}


