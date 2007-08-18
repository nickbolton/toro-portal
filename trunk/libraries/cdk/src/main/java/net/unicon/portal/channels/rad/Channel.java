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


package net.unicon.portal.channels.rad;

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.xml.sax.ContentHandler;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ChannelCacheKey;

import net.unicon.academus.apps.rad.*;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.cscr.CscrChannelRuntimeData;

/**
 * The RAD channel class is an implementation of uPortal channel, i.e. the interface IChannel.
 * The main roles of class are:
 *  - Manage all screens (i.e. Screen class) in the channel.
 *  - Receive input from uPortal framework and dispath them to the corresponding Screen.
 *  - Pass the uPortal's render method to the corresponding Screen.
 *
 * As owner of Screen objects it give the screens all the resources it may have:
 *  - local resources
 *  - rad properties
 *  - EJBs
 *  - Shared memory area
 *
 * In other words, the Channel class is the interface point between uPortal frameword and its Screen objects.
 */
public class Channel implements IChannel, ICacheable {
  // Command separator used in HTML attribute name="do..."
  protected static final String CMD_SEPARATOR = "~";

  // Save channel ids
  protected String m_subscribeId = null;
  protected String m_publishId = null;

  public String getSubscribeId() {
    return m_subscribeId;
  }
  public String getPublishId() {
    return m_publishId;
  }

    private ChannelRuntimeData m_trueCrd = null;

  //- Version information----------------------------------------------------//
  /**
   * The RAD version major number
   */
  static final int VERSION_MAJOR = 1;

  /**
   * The RAD version minor number
   */
  static final int VERSION_MINOR = 0;

  /**
   * The RAD version revision string
   */
  static final String VERSION_REVISION = "0";

  /**
   * The full RAD version string
   */
  static final String VERSION = ""+VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_REVISION;

  //- Log option ------------------------------------------------------------//
  /**
   * Constant for logging level: turn off
   */
  protected static final int NONE = 0;

  /**
   * Constant for logging level: turn on, log into console
   */
  protected static final int CONSOLE = 1;

  /**
   * Constant for logging level: turn on, logging uses the uPortal Log service
   */
  protected static final int SERVICE = 2;

  /**
   * Constant for logging level: turn on, log only executing time.
   */
  protected static final int TIME = 3;

  //- Channel Properties ----------------------------------------------------//

  protected Map m_peParams = null;      // Portal event parameters

  /**
   * All the rad properties.
   */
  public static Properties m_radProps = new Properties();

  /**
   * Global flag for RAD's Screen Info. It this flag is cleared then the RAD's screen Info will be ignored
   */
  public static boolean m_information = false;

  /**
   * Global flag for rad's Screen Confirm. It this flag is cleared then the RAD's screen Confirm will be ignored
   */
  public static boolean m_confirmation = true;

  /**
   * Global directory where the screen xml stream will dumped to. If it is null, there is no dump file.
   */
  public static String m_xmlDebugDir = null;

  /**
   * The global skin. It provide an ability to change all the graphics files at one by
   * switching the graphics directories to another, specified by this variable.
   */
  public static String m_skin = null;

  /**
   * The global debug option. It can have the follwing values:
   *  - NONE no output ( default)
   *  - CONSOLE output on standard console
   *  - SERVICE ouput using uPortal logging service
   *  - TIME output only the executing time for measuse the performance.
   *
   * The rad.properties will define what debug option by property name "debug".
   * Its value is one of:
   *  - "console" the debug option will be CONSOLE
   *  - "service" the debug option will be SERVICE
   *  - "time" the debug option will be TIME
   * All the other values of this property will set the debug option to NONE
   */
  public static int m_debug = NONE;

  /**
   * The name of machine where is running the uPortal server.
   */
  public static String m_server = null;

  /**
   * The uPortal version such as 2.0.3, 2.1.1,...
   */
  public static String m_upVersion = null;

  static {
    try {
      // Load rad properties
      InputStream is = ResourceLoader.getResourceAsStream(Channel.class, "/properties/rad.properties");
      m_radProps.load(is);

      // uPortal server name
      m_server = InetAddress.getLocalHost().getHostName();

      // information option
      String s = m_radProps.getProperty("information");
      if ( s != null && s.toLowerCase().equals("true"))
        m_information = true;

      // confirmation option
      s = m_radProps.getProperty("confirmation");
      if ( s != null && s.toLowerCase().equals("false"))
        m_confirmation = false;

      // Skin
      m_skin = m_radProps.getProperty("skin");
      if( m_skin == null)
        m_skin = "rad";

      // Debug info.
      m_xmlDebugDir = m_radProps.getProperty("xmldir");
      String debug = m_radProps.getProperty("debug");
      m_debug = (debug==null)?NONE:debug.equals("console")?CONSOLE:
                debug.equals("time")?TIME:
                debug.equals("service")?SERVICE:NONE;

      // Bo layer debug option
      StdBo.m_debug = m_debug;

      // uPortal version
      m_upVersion =
          com.interactivebusiness.portal.VersionResolver.getPortalVersion();
      //m_radProps.getProperty("uPortal.version","2.1.0");


      // SQL concat operator string
      SQL.CONCAT = m_radProps.getProperty("SQL.concat","||");

      // information finders
      Enumeration e = m_radProps.keys();
      while( e.hasMoreElements()) {
        String key = (String)e.nextElement();
        if( key.startsWith("finder.")) {
          String finder = m_radProps.getProperty(key);
          String name = key.substring(7);//length of "finder."
          Finder.m_finders.put(name, Class.forName(finder).newInstance());
        }
      }
      log("m_finders.size="+Finder.m_finders.size());
    } catch (Exception e) {
      log(e);
    }
  }

  /**
   * Get property value stored in the rad.properties file
   * @param name The name of property to get
   * @return The String value of this property defined in the file rad.properties.
   */
  public static String getRADProperty(String name) {
    String ret = m_radProps.getProperty(name);
    return ret;
  }

  public static java.util.Date getCurrentDate() {
    return new java.util.Date();
  }

  //- Version---------------------------------------------------------------//

  /**
   * Check the version compatible of a screen. The RAD library may have version
   * different from version of Screen and can produce some problems on the result.
   * The method will check and throws an exception if the versions are not compatible.
   * @param scr The Screen to check with the RAD version.
   * @throws Exception if there are some version uncompatible.
   */
  public static void checkVersion( Screen scr) throws Exception {
    StringTokenizer st = new StringTokenizer(scr.getVersion(),".");
    int major = Integer.parseInt(st.nextToken());
    int minor = Integer.parseInt(st.nextToken());
    String revision = st.nextToken();

    // Major: package name would be different
    if( major != VERSION_MAJOR)
      throw new Exception(scr.getLocalText("ERROR_VERSION_MAJOR", Channel.class));
    if( minor < VERSION_MINOR || (minor == VERSION_MINOR && !revision.equals(VERSION_REVISION)))
      log("Version warning: RAD version = " + VERSION + ", your screen version = "+ scr.getVersion()+", channel="+scr.m_channel.getClassName());
  }

  //- Local ----------------------------------------------------------------//

  protected String m_localID = null; // as code

  String getLocalKey( Class resource) {
    return getPackage( resource) + "." + getLocalFile();
  }

  Hashtable loadLocal( Class resource) {
    /*
    <local>
        <text id='...'>...</text>
        ...
    </local>
    */
    XMLData local = new XMLData();
    try {
      // Load local data
      InputStream is = ResourceLoader.getResourceAsStream(resource, getLocalFile());
      local.parse(is);
    } catch (Exception e) {
      Channel.log(e);
      return null;
    }

    // Children of local are elements "text"
    Hashtable ret = new Hashtable();
    Object[] texts = local.rgetE("text");
    if( texts != null)
      for ( int i = 0; i < texts.length; i++) {
        XMLData data = (XMLData)texts[i];
        if( data.get() != null)
          ret.put(data.getA("id"),data.get());
      }
    return ret;
  }

  String getLocalFile() {
    return (m_localID != null? "local_" + m_localID: "local") + ".xml";
  }

  String getSSLFile() {
    return (m_localID != null? "xsl_" + m_localID: "xsl") + ".ssl";
  }

  /**
   * Get the local String based on an ID defined in the file local.xml.
   * If given id not found original input will return.
   * @param text The id of local String to get.
   * @return The local text from local.xml with the given id or original text if it is not define here.
   */
  public String getLocalText( String text) {
    // Nothing to do with the null text
    if( text == null)
      return null;

    // Key of resource
    String localKey = getLocalKey(getClass());

    // Get local data from key
    Hashtable local = (Hashtable)Screen.m_locals.get(localKey);
    if( local == null) { // not loaded yet, load it now
      local = loadLocal(getClass());
      if( local != null)
        Screen.m_locals.put(localKey,local);
    }

    // If no local is here...
    if( local == null)
      return text;

    String val = (String)local.get(text);
    return val!=null?val:text;
  }

  //- log -------------------------------------------------------------------//

  /**
   * Provide the log of a message based on the debug option.
   * @param s The String to log
   */
  public static void log(String s) {
    if( m_debug == CONSOLE)
      System.out.println(s);
    else if( m_debug == SERVICE)
      LogService.instance().log(LogService.INFO, s);
  }

  /**
   * Provide the log of Throwable object based on the debug option.
   * @param e The Throwable to log
   */
  public static void log(Throwable e) {
    if( m_debug == CONSOLE) {
      if( e != null) {
        System.out.println("Throwable:"+e.getMessage());
        e.printStackTrace(System.out);
      } else
        System.out.println("Throwable: null Throwable object!!!");
    } else if( m_debug == SERVICE)
      LogService.instance().log(LogService.INFO, e);
  }

  //- class, package, etc. ---------------------------------------------------//

  String getClassName() {
    String className = getClass().getName();
    int dot = className.lastIndexOf('.');
    return dot == -1? className:className.substring(dot+1);
  }

  String getPackage() {
    return getPackage( getClass());
  }

  static String getPackage( Class cls) {
    String className = cls.getName();
    int dot = className.lastIndexOf('.');
    return dot == -1? null:className.substring(0, dot);
  }

  /**
   * Each screen in the channel is controled by a Java Screen-derived class.
   * All the actions on the screen are handled by this object and what is
   * rendered on the screen is also specified by this one. The method returns
   * the class name of the first Screen of the channel (Peephole).
   * @return The Java class name of the Peephole screen. The default is named "Main".
   */
  public String getMain() {
    return getPackage()+".Main";
  }

  //- IChannel ---------------------------------------------------------------//

  /**
   * The refference to the channel static data.
   */
  public ChannelStaticData m_csd = null;

  /**
   * The last channel runtime data.
   */
  public ChannelRuntimeData m_crd = null;

  /**
   * Implementation of method getRuntimeProperties() in the IChannel interface
   */
  public ChannelRuntimeProperties getRuntimeProperties() {
    return new ChannelRuntimeProperties();
  }

  boolean m_portalEvent = false;

  /**
   * Implementation of method receiveEvent(PortalEvent ev) in the IChannel interface.
   * The events handled in this method are:
   *  - PortalEvent.ABOUT_BUTTON_EVENT: go to the About screen
   *  - PortalEvent.DETACH_BUTTON_EVENT: save the parameters when detach the channel.
   */
  public void receiveEvent(PortalEvent ev) {
    switch( ev.getEventNumber()) {
    case PortalEvent.ABOUT_BUTTON_EVENT:
      if( m_lastScreen != null && !About.SID.equals(m_lastScreen.sid()))
        m_lastScreen = m_lastScreen.about(m_lastScreen.sid());
      break;

    case PortalEvent.DETACH_BUTTON_EVENT:
      m_portalEvent = true;
      break;

    case PortalEvent.SESSION_DONE:
      m_screens = null;
      m_lastScreen = null;
      m_lastPeephole = null;
      m_peParams = null;
      m_csd = null;
      m_crd = null;
      break;

    default:
      break;
    }
  }

  /**
   * Implementation of method setStaticData(ChannelStaticData csd) in the IChannel interface.
   * This method will initialize all the channel data. The default implementation is
   * get local identifier from channel parameter.
   */
  public void setStaticData(ChannelStaticData csd) {
    m_csd = csd;

    // Save subscribe and publish Id
    m_subscribeId = m_csd.getChannelSubscribeId();
    m_publishId = m_csd.getChannelPublishId();

    m_localID = m_csd.getParameter("local");

    // Register channel
    IPerson p = m_csd.getPerson();
    p.setAttribute( getClass().getName(), this);
  }

  /**
   * Implementation of method setRuntimeData(ChannelRuntimeData crd) in the IChannel interface.
   */
  public void setRuntimeData(ChannelRuntimeData crd) {

    // Only non-cms channels are passed the CscrChannelRuntimeData object.
    // Cms channels (sub-channels) are handled by the super channel.
    // Sub-channels will not have an assigned publishId.
    String publishId = m_csd.getChannelPublishId();
    if (publishId == null || "".equals(publishId.trim())) {
        m_trueCrd = crd;
    } else {
        m_trueCrd = new CscrChannelRuntimeData(publishId, crd);
    }
      

    // Save runtime data
    if( m_portalEvent) {
      m_portalEvent = false;
// System.out.println(getClass().getName() + "  portal event");
    } else {
      m_crd = m_trueCrd;
// System.out.println(getClass().getName() + "  set crd");
    }
  }

  //---------------------------------------------------------------------//
  //------ICacheable-----------------------------------------------------//

  /**
   * Implementation of the method generateKey() in the ICacheable interface.
   * The cache will with the instance scope only, except for the guest user that comes
   * with the system cache.
   */
  public ChannelCacheKey generateKey() {
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append(getClass().getName()+": ");
    sbKey.append("userId:").append(m_csd.getPerson().getID()).append(", ");
    sbKey.append("authenticated:").append(m_csd.getPerson().getSecurityContext().isAuthenticated()).append(", ");
    sbKey.append("SID:").append(m_csd.getChannelSubscribeId());

    // sbKey.append(", go:").append(getCmdValue("go")).append(", do=").append(getCmdValue("do"));

/*
    // Get key from screen
    if( m_lastScreen != null)
      sbKey.append(", screen:").append(m_lastScreen.sid()).append(", renderRoot:").append(m_lastScreen.lastRenderRoot());
*/

    // Generate channel key
    ChannelCacheKey k = new ChannelCacheKey();
    k.setKeyScope(m_csd.getPerson().isGuest()?ChannelCacheKey.SYSTEM_KEY_SCOPE:ChannelCacheKey.INSTANCE_KEY_SCOPE);
    k.setKey(sbKey.toString());
    k.setKeyValidity("key");
    // k.setKeyValidity(new Long(Channel.getCurrentDate().getTime()));
    return k;
  }


  /**
   * Implementation of the method isCacheValid(Object validity) in the ICacheable interface.
   */
  public boolean isCacheValid(Object validity) {

    boolean cacheValid = true;

/* We don't care :)
    // ToDo:  I'm *not* actually sure we don't care about
    // this one, we need to review...
    if( getShared("dirty") != null) {
      cacheValid = false;
      reason = "shared-dirty";
    }
*/

/*
System.out.println("***** SPILLING CRD: ");
java.util.Enumeration e = m_trueCrd.getParameterNames();
while (e.hasMoreElements()) {
    String s = (String) e.nextElement();
    System.out.println("\tkey=" + s + " , value=" + m_trueCrd.getParameter(s));
}
*/

    if (m_crd.isTargeted()) {
        cacheValid = false;
    }

/* We don't care, as such (see above).
    if( getCmdValue("go") != null || getCmdValue("do") != null) {
      cacheValid = false;
      reason = "cmd-value";
    }
*/

/* We don't care.
    if (validity instanceof Long) {
      Long oldtime = (Long)validity;
      if( m_lastScreen != null) {
        cacheValid = m_lastScreen.isCacheValid(oldtime.longValue());
        reason = "screen-timeout";
      }
    }
*/

    return cacheValid;

  }

  //------End of ICacheable----------------------------------------------//
  //---------------------------------------------------------------------//

  //- managing screens -------------------------------------------------------//

  protected Hashtable m_screens = new Hashtable();
  protected Screen m_lastScreen = null;
  protected Screen m_lastPeephole = null; // NLe 0403

  /**
   * Create an instance of screen with the given class name. It appends the channel package name
   * if the given name is not fully qualified. It also verify the version compatibility
   * of the screen.
   * @param name The class name of the Screen
   * @return The Screen instance of the given class name
   * @throws when the class name is not defined or when the verion of screen is less than RAD version.
   */
  public Screen makeScreen(String name) throws Exception {
    if (name.indexOf('.') == -1)
      name = getPackage()+"."+name;
    //added by Jing 6/26/2003, for servant contact
    boolean isServantContact = false;
    if (name.endsWith("Contact2"))
    {
      name = name.substring(0, name.length() - 1);
      isServantContact = true;
    }
    Screen screen = (Screen)Class.forName(name).newInstance();
    screen.m_channel = this;
    //added by Jing
    if (isServantContact)
      screen.m_isServant = true;
    else
      screen.m_isServant = false;
    // Check version compatible, throw exception if not compatible
    checkVersion(screen);

    // Valid
    m_screens.put(screen.sid(), screen);
    return screen;
  }

  /**
   * Get the Screen object by Screen Identifier (sid).
   * @param sid The Identifier of screen (sid) to get
   * @return The Screen object with the given sid.
   * @throws if sid is null
   */
  public Screen getScreen(String sid) // throws Exception
  {
    return (Screen)m_screens.get(sid);
  }

  //- rendering --------------------------------------------------------------//

  /**
   * Get the parameters from channel runtime data.
   * @return a Hashtable containing all the channel parameters.
   */
  public Hashtable getParameters() {
    Hashtable params = new Hashtable();
    for (Enumeration e = m_crd.getParameterNames(); e.hasMoreElements();) {
      Object key = e.nextElement();
      Object value = m_crd.get(key);
      //Object[] value = m_crd.getParameters((String)key);
      if (value instanceof String[]) {
        String[] values = (String[])value;
        if (values.length > 0 && values[0] != null)
          params.put(key, values[0]);
      } else if (value != null)
        params.put(key, value);

    }
    return params;
  }

  /** 
   * Redirect to another Screen. 
   * 
   * @param sid The Screen identifier of redirected screen 
   * @throws Exception if some errors occurs. 
   */ 
  public void redirect( String sid ) throws Exception { 
    // Get screen being calling method 
    m_lastScreen = getScreen(sid); 
    if( m_lastScreen == null) { 
      m_lastScreen = makeScreen(sid); 
      m_lastScreen.init(getParameters()); 
    } else {
      m_lastScreen.reinit(getParameters()); 
    }
  } 


  /**
   * Implementation of renderXML(ContentHandler out) in the IChannel interface
   */
  public void renderXML(ContentHandler out) throws PortalException {
    log("renderXML:channel="+m_csd.getChannelSubscribeId());
    try {
      execute();
      if( m_lastScreen != null)
        m_lastScreen.processXML(out, null);
    } catch (Exception e) {
      log(e);
      String msg = (e==null)?"Object Exception is null":e.getMessage();
      throw new PortalException(msg,e) {
        public int getExceptionCode() {
          return -1;
        }
      };
    }
  }

  /**
   * Perform an action stored in the channel runtime data. The actions divides to 2 types:
   * 1. "go" action: The value of this parameter specifies where to go.
   * 2. "do" action: The value of this parameter defines what method to be called in the current screen.
   * If the channel runtime data do not contain either "go" or "do", the action "refresh" will take in place.
   */
  protected void execute() throws Exception {
    removeShared("dirty");

    String op = null;

    // "go" command
    if ((op = getCmdValue("go")) != null && op.length() > 0) {
      log("execute:chan="+m_csd.getChannelSubscribeId()+",scr="+getClass().getName()+",go="+op);
      m_lastScreen = getScreen(op);
      if (m_lastScreen == null) {
        m_lastScreen = makeScreen(op);
        m_lastScreen.init(getParameters());
      } else {
        m_lastScreen.reinit(getParameters());
      }
    }

    // "do" command
    else if ((op = getCmdValue("do")) != null && op.length() > 0) {
      log("execute:chan="+m_csd.getChannelSubscribeId()+",scr="+getClass().getName()+",do="+op);
      String sid = m_crd.getParameter("sid");
      if (sid == null || sid.length() == 0)
        throw new Exception("Ill-formed XSL");
      try {
          m_lastScreen = getScreen(sid).invoke(op, getParameters());
          putShared("dirty","true");
      } catch (Exception e) {
          log("execute:channel:do="+m_csd.getChannelSubscribeId()+ " Unable to get Screen for sid : " + sid);
          m_lastScreen = null;
      }
    }

    // No go, do command --> we refresh current screen, call reinit
    else {
      log("execute:chan="+m_csd.getChannelSubscribeId()+",scr="+getClass().getName()+" no go/do");
      if( m_lastScreen != null) {
        // render as root
        if( isRenderingAsPeephole() && m_lastScreen.canRenderAsPeephole() == false)
          m_lastScreen = m_lastPeephole;

        // Do more when there is not go/do command, normally the action is  refreshing screen
        if( m_lastScreen != null)
          m_lastScreen = m_lastScreen.executeMore();
      }
    }

    // Check
    if( m_lastScreen == null && getMain() != null) // NLe 0504
    {
      m_lastScreen = makeScreen(getMain());
      m_lastScreen.init(getParameters());
    }

    // Save last peephole
    if( isRenderingAsPeephole() && m_lastScreen != null && m_lastScreen.canRenderAsPeephole() && !m_lastScreen.sid().equals(About.SID) && !m_lastScreen.sid().equals(Info.SID)) // NLe 0504, Thach-May20
      m_lastPeephole = m_lastScreen;
  }

  /**
   * Check whether is channel currently rendering as peephole
   * @return true if the channel is in peephole mode ( i.e. render together with the other channels).
   * If channel is rendering as full screen it return false.
   */
  public boolean isRenderingAsPeephole() {
    return !m_crd.isRenderingAsRoot();
  }

  //- shared data ------------------------------------------------------------//

  protected Hashtable m_shared = new Hashtable();

  /**
   * Provides the Screen-level shared data area.
   * @param key The key associated with the shared data to get.
   * @return The shared data with given key.
   */
  public Object getShared(Object key) {
    return m_shared.get(key);
  }

  /**
   * Provides the Screen-level shared data area.
   * @param key The key of the shared data
   * @param value the shared data to put to shared data area.
   */
  public void putShared(Object key, Object value) {
    m_shared.put(key, value);
  }

  /**
   * Remove the shared data.
   * @param key the key of shared data to remove
   * @return The shared data associated with the given key.
   */
  public Object removeShared(Object key) {
    return m_shared.remove(key);
  }

  //- security ---------------------------------------------------------------//

  /**
   * Get the Security object for this channel.
   * @return an instance of the Security object for this channel.
   * @see Security
   */
  public Security getSecurity() throws Exception {
    // owner is application package
    String className = getClass().getName();
    int dot = className.lastIndexOf(".channels.");
    return new Security(dot == -1? "IBSDP_DEFAULT":className.substring(0, dot));
  }

  /**
   * Currently logged on user.
   */
  protected IdentityData m_logon = null;

  /**
   * Get the logon user in the IdentityData notation with
   * the full information from all the person sources.
   * @return The IdentityData object for the logon user.
   * @see net.unicon.academus.apps.rad.IdentityData
   */
  public IdentityData logonUser() {
    if( m_logon == null) {
      IPerson p = m_csd.getPerson();
      m_logon = new IdentityData(IdentityData.ENTITY, GroupData.S_USER, ""+p.getID(), (String)p.getAttribute("username"), p.getFullName());
      String email = (String)p.getAttribute("mail");
      if( email != null)
        m_logon.putEmail(email);

      Finder.findDetail(m_logon,null);
      Finder.findRefferences(m_logon, m_logon);
      IdentityData portal = m_logon.getRef("portal");
      IdentityData campus = m_logon.getRef("campus");
      IdentityData contact = m_logon.getRef("contact");
      log("logon="+m_logon+
          (portal!=null?",portal="+portal:"")+
          (campus!=null?",campus="+campus:"")+
          (contact!=null?",contact="+contact:"")
         );
    }
    return m_logon;
  }

  /**
   * Get user profile for current channel. It is in the xml format.
   * The user profile data is stored in the uPortal database.
   * @return The XMLData object containing user profile.
   */
  public XMLData getUserProfile() {
    return getUserProfile(m_csd.getChannelSubscribeId());
  }

  public XMLData getUserProfile( String key) {
    String s = getUserData(key);
    XMLData data = null;
    if( s != null)
      try {
        data = new XMLData();
        data.parse( new ByteArrayInputStream(s.getBytes()));
      } catch( Exception e) {
        log(e);
        data = null;
      }
    return data;
  }

  /**
   * Get user data for this channel, previously set by method
   *  public boolean setUserData( String data) throws Exception
   * @return The String of user data.
   */
  public String getUserData() {
    return getUserData(m_csd.getChannelSubscribeId());
  }

  public String getUserData( String key) {
    if( key.length() >= 16)
      key = key.substring(0, 15);
    String userID = logonUser().getID();
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    String data = null;
    try {
      conn = RDBMServices.getConnection();
      try {
        stmnt = conn.createStatement();
        String sql = "SELECT DATA FROM UPC_USER_DATA WHERE CHANNEL_ID="+SQL.esc(key)+" AND USER_ID="+userID;
        rs = stmnt.executeQuery(sql);
        if (rs.next()) {
          data = rs.getString(1);
        }
      } finally {
        try {
          if (rs != null ) rs.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          if (stmnt != null ) stmnt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception sqle) {
      Channel.log(sqle);
    } finally {
      RDBMServices.releaseConnection(conn);
    }

    return data;
  }

  /**
   * Store user profile in xml format in the uPortal database for this channel.
   * @param profile The data to store.
   * @return true if successfully, otherwise - false
   * @throws if io error occurs
   */
  public boolean setUserProfile( XMLData profile) throws Exception {
    return setUserProfile( profile, m_csd.getChannelSubscribeId());
  }

  public boolean setUserProfile( XMLData profile, String key) throws Exception {
    // Convert to xml String
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("<?xml version=\"1.0\"?>");
    ps.println("<user-profile>");
    profile.print(ps, 1);
    ps.println("</user-profile>");
    String data = baos.toString();
    return setUserData(baos.toString(),key);
  }

  /**
   * Store the user data for this channel in the uPortal database
   * @param data The String of user data to store
   * @return true if successfully, else - false
   * @throws if some error occurs
   */
  public boolean setUserData( String data) throws Exception {
    return setUserData(data,m_csd.getChannelSubscribeId());
  }

  public boolean setUserData( String data, String key) throws Exception {
    // Channel and user info
    if( key.length() >= 16)
      key = key.substring(0, 15);
    String userID = logonUser().getID();

    // Insert/Update to database
    Connection conn = RDBMServices.getConnection();
    boolean oldAutoCommit = conn.getAutoCommit();
    Statement stmt = null;
    try {
      RDBMServices.setAutoCommit(conn,false);
      stmt = conn.createStatement();

      // Updating...
      String sql = "UPDATE UPC_USER_DATA SET DATA=" + SQL.esc(data) +
                   " WHERE CHANNEL_ID="+SQL.esc(key)+" AND USER_ID="+userID;
      log("setUserData.update...sql="+sql);
      if( stmt.executeUpdate(sql) == 0) {
        sql = "INSERT INTO UPC_USER_DATA(CHANNEL_ID,USER_ID,DATA)"+
              " VALUES("+SQL.esc(key)+","+userID+","+SQL.esc(data)+")";
        log("setUserData.update failed, insert now...sql="+sql);
        stmt.executeUpdate(sql);
      }

      // commit all
      RDBMServices.commit(conn);
    } catch (SQLException e) {
      e.printStackTrace();
      RDBMServices.rollback(conn);
      return false;
    } finally {
      try {
        if (stmt != null) stmt.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      RDBMServices.setAutoCommit(conn,oldAutoCommit);
      RDBMServices.releaseConnection(conn);
    }
    return true;
  }

  //- ejbs -------------------------------------------------------------------//

  protected Hashtable m_ejbs = new Hashtable();

  /**
   * Get the EJB with given name from the channel cache.
   * @param name The name of the EJB to get
   * @return The EJB with the given name
   */
  public Object getEjb(String name) {
    return m_ejbs.get(name);
  }

  /**
   * Store EJB object to channel cache for later usage.
   * @param name The name of EJB to store
   * @param ejb The EJB
   */
  public void putEjb(String name, Object ejb) {
    m_ejbs.put(name, ejb);
  }

  /**
   * Remove the EJB from the channel cache.
   * @param name The name of EJB to remove
   * @return The EJB with the given name.
   */
  public Object removeEjb(Object name) {
    return m_ejbs.remove(name);
  }

  protected void finalize() throws Throwable {
    Enumeration e = m_ejbs.keys();
    while( e.hasMoreElements()) {
      javax.ejb.EJBObject ejb = (javax.ejb.EJBObject)m_ejbs.get((String)e.nextElement());
      if( ejb != null)
        ejb.remove();
    }
  }

  //---------------------------------------------------------------------//

  /**
   * Cache for the EJB home interfaces
   */
  static Hashtable m_homes = new Hashtable();

  /**
   * Get EJB home interface with the given EJB name. The EJB must be registered in
   * the file "rad.properties" as rad property with the information below:
   *  - the property name is EJB name
   *  - the value of this property is the EJB description.
   *
   * In BNF, an EJB entry in the rad.properties has the type:
   *  <EJB name> = <Remote EJB description> | <Skel EJB description>
   *
   * The remote EJB description:
   *  <Remote EJB description> := remote,<context>,<url>,<user>,<password>
   *  <context> := <Predefined context> | <Full JNDI context class name>
   *  <Predefined context> := eas | jboss | weblogic
   *
   * The skel ( RAD local EJB) EJB description:
   *  <Skel EJB description> := skel,<SkelBo name>[,<EJB properties>]
   *  <EJB properties> := <propname>=<propvalue> [;<propname>=<propvalue>]*
   *
   * However the skel EJB can register its properties as rad properties, the entry is below:
   *  <EJB name>.<EJB property name> = <EJB property value>
   *
   * Here is an example of EJB for Forums channel from the file rad.properties
   *  ForumsBo=skel,net.unicon.academus.apps.forums.ForumsBoSkel,Forums=devportal
   *  ForumsBo.Expire=1
   *  ForumsBo.NewsServer=127.0.0.1
   *  ForumsBo.Port=119
   *  ForumsBo.AdminEmail=uportal@columbia.edu
   *
   * In this example, the EJB ForumsBo has the following properties:
   *  Forums=devportal
   *  Expire=1
   *  NewsServer=127.0.0.1
   *  Port=119
   *  AdminEmail=uportal@columbia.edu
   *
   * @param name The EJB name to get its home interface
   * @return The home interface.
   * @throws Exception if the EJB do not register with the RAD. If EJB is remote there may be
   * JNDI exception when it can not look up the EJB with the given name.
   */
  public static Object ejbHome(String name) throws Exception {
    Object home = m_homes.get(name);
    if (home == null) {
      // Get map
      String desc = m_radProps.getProperty(name);
      if (desc == null)
        throw new Exception("Unrecognized ejb name!");
      log(name + "=" +desc);

      // Format of ejb desc:
      // skel,<skelbo name>[,<ejb properties>]
      // remote,<context>,<url>,<user>,<password>
      // where:
      // <ejb properties> = <propname>=<propvalue> [;<propname>=<propvalue>]*
      // <context> = eas | jboss | weblogic | <full class name>
      StringTokenizer token = new StringTokenizer(desc, ",", false);
      String type = token.nextToken();

      // Skel
      if (type.equals("skel")) {
        // Create home
        String skelBo = token.nextToken();
        home = Class.forName(skelBo).newInstance();

        // Save properties in home
        if (token.hasMoreTokens())
          ((BaseSkel)home).m_props = parseEJBProperties(name, token.nextToken());
      }

      // Remote
      else if (type.equals("remote")) {
        String factory = token.hasMoreTokens()? token.nextToken():null;
        String url = token.hasMoreTokens()? token.nextToken():null;
        String user = token.hasMoreTokens()? token.nextToken():null;
        String password = token.hasMoreTokens()? token.nextToken():null;

        Hashtable jndi = new Hashtable(5, 0.75f);
        jndi.put(Context.INITIAL_CONTEXT_FACTORY, getContextFactory(factory));
        jndi.put(Context.PROVIDER_URL, url);
        if (user != null) {
          jndi.put(Context.SECURITY_PRINCIPAL, user);
          if (password != null)
            jndi.put(Context.SECURITY_CREDENTIALS, password);
        }

        Context cxt = new InitialContext(jndi);
        home = cxt.lookup(name);
      }

      // Cache home
      if (home != null)
        m_homes.put(name, home);
    }
    return home;
  }

  //---Helpers----------------------------------------------------------------//

  static Hashtable extractProperties( String prefix) {
    Hashtable h = new Hashtable();
    int start = prefix.length();
    Enumeration e = m_radProps.propertyNames();
    while( e.hasMoreElements()) {
      String key = (String)e.nextElement();
      if( key.startsWith(prefix))
        h.put(key.substring(start),m_radProps.getProperty(key));
    }

    return h;
  }

  static Hashtable parseEJBProperties(String ejbName, String props) {
    // props = <propname>=<propvalue> [;<propname>=<propvalue>]*
    Hashtable h = extractProperties( ejbName+".");
    StringTokenizer token = new StringTokenizer(props, ";", false);
    while (token.hasMoreTokens()) {
      String p = token.nextToken().trim();

      // p: <propname>=<propvalue>
      int idx = p.indexOf('=');
      if (idx != -1)
        h.put(p.substring(0,idx).trim(),p.substring(idx+1).trim());
    }
    return h;
  }

  static String getContextFactory(String ctxName) {
    if (ctxName.equals("weblogic"))
      return "weblogic.jndi.WLInitialContextFactory";
    else if (ctxName.equals("jboss"))
      return "org.jnp.interfaces.NamingContextFactory";
    else if (ctxName.equals("eas"))
      return "com.ibs.Framework.appsrv.EjbInitCtxFactory";
    else
      return ctxName;
  }

  /**
   * Get the value of action "do" or "go" from channel runtime data.
   * @param cmd is "do" or "go" command
   * @return The value of given command.
   */
  public String getCmdValue(String cmd) {
    // Find in ChannelRuntimeData
    String op = m_crd.getParameter(cmd);

    // Look up for submit button if not found
    //<input type="submit" value="OK" name="go={back}"/>
    //<input type="image" src="..." name="do=addUser"/>
    //<input type="image" src="..." name="cp=addUser"/>
    if (op == null) {
      cmd += CMD_SEPARATOR;
      int l = cmd.length();
      Enumeration e = m_crd.getParameterNames();
      while (e.hasMoreElements()) {
        Object key = e.nextElement();
        if (key instanceof String) {
          String sKey = (String)key;
          if( sKey.startsWith(cmd))
            return processCmdValue( sKey, cmd);
        }
      }

      // Default action
      String defaultAction = decode( m_crd.getParameter("default"));
      if( defaultAction != null && defaultAction.startsWith(cmd))
        return processCmdValue( defaultAction, cmd);
    }

    return op;
  }

  public static String decode(String s) {
    if (s != null && s.length() > 0) {
      s = s.replaceAll("&#96;", "`");
      s = s.replaceAll("&#126;", "~");
      s = s.replaceAll("&#37;", "%");
      s = s.replaceAll("&#60;","<");
      s = s.replaceAll("&#62;", ">");
      s = s.replaceAll("&#34;", "\"");
      s = s.replaceAll("&amp;", "&");
    }
    return s;
  }

  /**
   * @param params query string
   * @param cmd "do=" or "go="
   */
  String processCmdValue( String params, String cmd) {
    String val = ( params.endsWith(".x") || params.endsWith(".y"))?
                 params.substring(cmd.length(),params.length()-2):
                 params.substring(cmd.length());

    // there maybe some parameters: Ex: val = "expand&key=1&group=2"
    StringTokenizer st = new StringTokenizer(val,"&");
    if (st.hasMoreTokens()) // value being to return
      val = st.nextToken();

    // others parameters:
    while( st.hasMoreTokens()) {
      String param = st.nextToken(); // xxx=yyy
      int idx = param.indexOf('=');
      m_crd.setParameter(param.substring(0,idx),param.substring(idx+1));
    }

    // normalize parameters of m_crd
    //m_crd.remove(key);
    //m_crd.setParameter();
    return val;
  }
}
