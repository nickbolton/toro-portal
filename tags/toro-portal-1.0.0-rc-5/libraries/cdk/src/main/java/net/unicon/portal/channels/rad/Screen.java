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

import java.lang.*;
import java.util.*;
import java.text.*;
import java.io.*;
import org.xml.sax.ContentHandler;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.PortalException;

import org.jasig.portal.utils.XSLT;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UploadStatus;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.apps.rad.XMLData;

/**
 * The Screen is unit element in the RAD framework. A channel can have several screens
 * and only one active at time. Screen objects are owned by channel and each Screen has
 * the unique identifier, called Screen IDentifier (sid). There can reffer to Screen
 * through its SID, especially in the HTML form or link.
 *
 * The Screen is reponsible to:
 *  - Provide XML data stream to render.
 *  - Perform an action indicated by command "do" from the channel runtime data.
 */
abstract public class Screen {
  // Some helpfull constants
  public static final String EMPTY = "";
  public static Boolean TRUE = new Boolean( true);
  public static Boolean FALSE = new Boolean( false);
  protected boolean m_idempotentURL = false; // You should set this to true in your derived class
  //added by Jing 6/26/2003
  protected boolean m_isServant = false;
  /**
   * Last view mode: m_renderRoot==true -> peephole mode
   */
  protected boolean m_renderRoot = false;
  
  private static boolean perfTestingSid = new Boolean(Channel.getRADProperty(
	"performance.testing.sid")).booleanValue();

  /**
   * Get render mode
   * @return last render root status
   */
  public boolean lastRenderRoot() {
    return m_renderRoot;
  }

  /**
   * Get the SID of screen
   * @return the screen identifier
   */
  public String sid() {
  	if (perfTestingSid) {
  		return getClass().getName();
  	}
    return Integer.toString(hashCode());
  }

  /**
   * Indicate that the screen can render in Peephole mode. Some Screens that has
   * the large HTML form can render only in the full screen mode. For those screens
   * the method should return false.
   * @return true if the Screen can reander in peephole mode ( i.e. render together with the other channels),
   * If can not - false.
   */
  public boolean canRenderAsPeephole() {
    return ( getClass().getName().equals(m_channel.getMain()));
  }

  /**
   * Get the xsl name of Screen ( that name was registered in the stylesheet list file
   * to point to actual stylesheet file .xsl) to combine with the XML data stream for rendering.
   * The xsl name depends on localization so it may prefixed by local identifier.
   * Normally it has the format:
   *  [<LocalID>_]<Lower case of Screen name>
   * Screen name is the name of Java class without the package name.
   *
   * @return The name of xsl of Screen.
   */
  public String getXSL() {
    String className = getClass().getName();
    int dot = className.lastIndexOf('.');
    String xsl = ((dot == -1)? className:className.substring(dot+1));
    if (m_isServant)
      xsl = xsl + "2";
    return (m_channel.m_localID != null ? xsl + "_" + m_channel.m_localID: xsl);
  }

  /**
   * Provide a way to pass parameter to XSL file.
   * @return a Hashtable containing all parameter names ( as key) and values.
   */
  public Hashtable getXSLTParams() {
    return null;
  }

  /**
   * Validate the uPortal cache for the channel.
   * @param oldMilisTime the last time the cache still is valid
   * @return The validity of cache
   */
  public boolean isCacheValid( long oldMilisTime) {
    // render as root
    if( m_channel.isRenderingAsPeephole() && canRenderAsPeephole() == false)
      return false;
    else if( m_channel.m_lastScreen != null && !About.SID.equals(m_channel.m_lastScreen.sid()))
      return false;
    else
      return true; //return (System.currentTimeMillis() - oldMilisTime < 1*60*1000)
  }

  protected Screen executeMore() throws Exception {
    Hashtable hs = m_channel.getParameters();
    if( !hs.isEmpty())
      reinit(hs);
    else
      refresh();
    return this;
  }

  /**
   * Render the Screen content to a ContentHandler. It will combine XML data stream
   * and the xsl file and use the XSLT processor to transform all of them into the output.
   * @param out1 The first output
   * @param out2 The secondary (reserved) ouput
   * @throws Exception if an error happens while transform data.
   */
  public void processXML(ContentHandler out1, OutputStream out2) throws Exception {
    /* check upload status before continuing */
    UploadStatus uploadStatus = 
        (UploadStatus)m_channel.m_crd.getObjectParameter(
            "upload_status");
    if (uploadStatus != null 
            && uploadStatus.getStatus() == UploadStatus.FAILURE) {
        // generate error message
        StringBuffer errMsg = new StringBuffer("<errorPage><message>");
        errMsg.append("File exceeds max size limit of ");
        errMsg.append(uploadStatus.getFormattedMaxSize());
        errMsg.append("</message></errorPage>");

        // transform
        XSLT xslt = new XSLT(this);
        xslt.setXSL("/net/unicon/portal/channels/error/error.ssl", "error", 
            m_channel.m_crd.getBrowserInfo());
        xslt.setXML(errMsg.toString());
        if (out1 != null)
          xslt.setTarget(out1);
        else
          xslt.setTarget(out2);
        xslt.transform();

        return;
    }

    // Save last render mode
    m_renderRoot = m_channel.isRenderingAsPeephole();
    log("#####Screen.processXML="+getClass().getName()+",renderRoot="+m_renderRoot);

    // xml
    timeDebug("before Screen.buildXML");
    String xml = buildXML();
    timeDebug("after Screen.buildXML");
    if( xml == null)
      return;

    //-- Debug only,remove soon ----
    if( m_channel.m_xmlDebugDir != null && m_channel.m_xmlDebugDir.length() > 0) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(m_channel.m_xmlDebugDir + File.separator + m_channel.getClassName() + "." + getXSL() + ".xml",false));
      writer.write(xml);
      writer.flush();
      writer.close();
    }

    // xslt parameters
    Hashtable params = getXSLTParams();
    if (params == null)
      params = new Hashtable();
    putSystemXSLTParams(params);


    // transform
    XSLT xslt = new XSLT(this);
    xslt.setXSL(m_channel.getSSLFile(), getXSL(), m_channel.m_crd.getBrowserInfo());
    xslt.setStylesheetParameters(params);
    xslt.setXML(xml);
    if (out1 != null)
      xslt.setTarget(out1);
    else
      xslt.setTarget(out2);
    timeDebug("before Screen.transform");
    xslt.transform();
    timeDebug("after Screen.transform");
  }

  //- Version---------------------------------------------------------------//

  /**
   * Get the version string of Screen.
   * @return the version of Screen.
   */
  abstract public String getVersion();

  /**
   * Get the XML data stream of Screen.
   * @return the data in the xml format
   * @throws Exception if there are some errors while getting the data.
   */
  abstract public String buildXML() throws Exception;

  //--------------------------------------------------------------------------//

  public Hashtable m_params = null;

  /**
   * The first entry point of Screen. The RAD framework ensures that this method
   * always called just after Screen's creation and before any call to other methods.
   * @param params The parameters passed from channel runtime data
   * @throws Exception if some errors occurs.
   */
  public void init(Hashtable params) throws Exception {
    m_params = params;
  }

  /**
   * The RAD framework (or who play the role as RAD framework) will call this method every time
   * when the Screen exists and needs to change status from innactive to active. Default
   * implementation is to delegate to the method init()
   * @param params The parameters passed from channel runtime data
   * @throws Exception if some errors occurs.
   */
  public void reinit(Hashtable params) throws Exception {
    init(params);
  }

  /**
   * The RAD framework will call the method when the channel runtime data do not
   * contain either "do" or "go" command. The typical type of this case is when
   * user click refresh on browser. The method will refresh any data Screen has.
   * @throws Exception if some errors occurs while refresh the data.
   */
  public void refresh() throws Exception {}

  /**
   * Redirect to another Screen. This method should be called from within the methods:
   *  init()
   *  reinit()
   *  refresh()
   *
   * @param sid The Screen identifier of redirected screen
   * @param params The parameters passed from channel runtime data
   * @throws Exception if some errors occurs.
   */
  protected void redirect( String sid, Hashtable params) throws Exception {
    // Get screen being calling method
    m_channel.m_lastScreen = getScreen(sid);
    if( m_channel.m_lastScreen == null) {
      m_channel.m_lastScreen = makeScreen(sid);
      m_channel.m_lastScreen.init(params);
    } else
      m_channel.m_lastScreen.reinit( params);
  }

  /**
   * Save Screen's parameter.
   * @param param The name of the parameter to save
   * @param value The value of the parameter to save
   */
  public void putParameter(Object param, Object value) {
    if (m_params == null)
      m_params = new Hashtable();
    m_params.put(param, value);
  }

  /**
   * Get the Screen's paramter value with the given name
   * @param param The parameter name to get
   * @return the parameter value associated with the given name
   */
  public Object getParameter(Object param) {
    return m_params == null? null:m_params.get(param);
  }

  /**
   * Remove the Screen's parameter.
   * @param param the parameter name to remove.
   * @return The parameter value associated with the given name
   */
  public Object removeParameter(Object param) {
    return m_params == null? null:m_params.remove(param);
  }

  //- Local ----------------------------------------------------------------//

  public static Hashtable m_locals = new Hashtable();// key <--> local data

  protected Class getLocalResource() {
    return getClass();
  }

  /**
   * Get the local text from channel local resource with the embbed parameters.
   * @param text The id from that will take the local text from local resource.
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @param resource Where to take the local resource.
   * @return The local text if the given id found in the local resource, otherwise the original text will return.
   */
  public String getLocalText( String text, Object[] mfps, Class resource) {
    String local = getLocalText( text, resource);
    return mfps==null?local:MessageFormat.format( local, mfps);
  }

  /**
   * It delegates to
   * getLocalText( String text, (Class)null);
   */
  public String getLocalText( String text) {
    return getLocalText(text, null);
  }

  /**
   * Get local string with specified resource
   * @param text The id from that will take the local text from local resource.
   * @param resource Where to take the local resource.
   * @return The local text if the given id found in the local resource, otherwise the original text will return.
   */
  public String getLocalText( String text, Class resource) {
    // Nothing to do with the null text
    if( text == null)
      return null;

    // Key of resource
    if( resource == null)
      resource = getLocalResource();
    String localKey = m_channel.getLocalKey(resource);

    // Get local data from key
    Hashtable local = (Hashtable)m_locals.get(localKey);
    if( local == null) { // not loaded yet, load it now
      local = m_channel.loadLocal(resource);
      if( local != null)
        m_locals.put(localKey,local);
    }

    // If no local is here...
    if( local == null)
      return text;

    String val = (String)local.get(text);
    return val!=null?val:text;
  }

  //------------------------------------------------------------//

  /**
   * Get the Select screen to select users/groups from the different person sources.
   * The person sources to select are:
   * 1. portal listing
   * 2. campus directory (LDAP)
   * 3. personal contacts
   *
   * @param sources The list of person sources, separated by comma (Ex. "portal.group,campus,contact")
   * @param initSel The initial selections of users/groups
   * @param okSID The sid of Screen where will call the method okMethod when
   * the Screen Select done with the OK button.
   * @param okMethod The name of the method will be called when the Screen Select done with the OK button.
   * @param okParams The parameters will pass to the method (okSID,okNethod)
   * @param backSID Where to go when the Select screen done with the cancel button.
   * @return an instance of Select screen
   */
  public Screen select( String sources, IdentityData[] initSel,
                        String okSID, String okMethod, Hashtable okParams, String backSID) {
    try {
      Screen scr = getScreen(Select.SID);
      if (scr == null) {
        String cls = m_channel.getRADProperty("select");
        scr = makeScreen((cls==null?"net.unicon.portal.channels.rad.Select":cls));
      }

      // Parameters of Select
      if( okParams == null)
        okParams = new Hashtable();
      okParams.put("back",backSID);
      if( okSID != null)
        okParams.put("methodSID",okSID);
      okParams.put("methodName",okMethod);
      okParams.put("sources", sources);
      if( initSel != null)
        okParams.put("selected",initSel);
      scr.init(okParams);
      return scr;
    } catch (Exception e) {
      log(e);
      return this;
    }
  }

  /**
   * Delegates to the method:
   * if sources is null:
   *  select( ("portal", initSel, sid(), okMethod, okParams, sid());
   * else
   *  select( sources, initSel, sid(), okMethod, okParams, sid());
   */
  public Screen select( String sources, IdentityData[] initSel, String okMethod, Hashtable okParams) {
    return select( (sources==null?"portal":sources), initSel, sid(), okMethod, okParams, sid());
  }

  //------------------------------------------------------------//

  /**
   * Get the Confirm screen to ask something.
   * @param text the confirm text or local text id ( define in the file local.xml)
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @param okParams It must contain 2 parameters: "methodSID" and "methodName" which define
   * what method to call after Confirm done with the positive response. Other parameters will pass
   * to the method above.
   * @param backSID The sid of back screen.
   * @return an instance of Confirm screen
   */
  public Screen confirm(String text, Object[] mfps, Hashtable okParams, String backSID) {
    try {
      // back parameter
      if( backSID == null)
        backSID = sid();

      // Check confirmation option
      if( Channel.m_confirmation == false) {
        String methodSID = (String)okParams.remove("methodSID");
        String methodName = (String)okParams.remove("methodName");
        if( methodSID == null)
          methodSID = backSID;
        Screen scr = getScreen(methodSID);
        return scr.invoke(methodName, okParams);
      }

      /*
       * Because there are two different Confirm classes, and because getScreen() cannot differentiate between them,
       * we had to test the result of getScreen against the correct class before proceeding.
       */
      
      Screen cfm_screen = getScreen(Confirm.SID);
      Confirm cfm = null;
      if(!(cfm_screen instanceof net.unicon.portal.channels.rad.Confirm) || (cfm_screen == null)) {
    	  cfm = (Confirm)makeScreen("net.unicon.portal.channels.rad.Confirm");  
      } else {
    	  cfm = (Confirm)cfm_screen;
      }

      cfm.m_back = backSID;
      cfm.m_methodSID = (String)okParams.remove("methodSID");
      cfm.m_methodName = (String)okParams.remove("methodName");
      if( cfm.m_methodSID == null)
        cfm.m_methodSID = backSID;
      cfm.m_text = getLocalText(text,mfps, null);
      cfm.m_okParams = okParams;
      return cfm;
    } catch (Exception e) {
      log(e);
      return this;
    }
  }

  //------------------------------------------------------------//

  /**
   * Get the Warning screen. The warning texts can be multiple.
   * @param texts The array of warning texts ( or local text identifiers).
   * @param back The sid of back screen.
   * @param reinit The flag indicating that the back screen will be
   * re-initialized (i.e. make the call to the method reinit()) when Warning screen is done.
   * @return an instance of the screen WarningMulti
   */
  public Screen warningMulti(String[] texts, String back, boolean reinit) {
    try {
      WarningMulti warning = (WarningMulti)getScreen(WarningMulti.SID);
      if (warning == null)
        warning = (WarningMulti)makeScreen("net.unicon.portal.channels.rad.WarningMulti");

      // Local text
      String[] locals = new String[texts.length];
      for( int i = 0; i < texts.length; i++)
        locals[i] = getLocalText(texts[i]);
      warning.m_back = back == null? sid():back;
      warning.m_refresh = reinit;
      warning.m_texts = locals;
      return warning;
    } catch (Exception e) {
      log(e);
      return this;
    }
  }

  //------------------------------------------------------------//
  /**
   * Get the Info screen.
   * @param icon The one of Info.INFO, Info.ERROR, Info.EXCEPTION
   * @param text The text (or the local text identifier) to display on Info screen.
   * @param back The sid of back screen.
   * @param reinit The flag indicating that the back screen will be
   * re-initialized (i.e. make the call to the method reinit()) when the Info screen is done.
   * @return an instance of the screen Info
   */
  Screen info(String icon, String text, String back, boolean reinit) {
    try {
      Screen info = getScreen(Info.SID);
      if (info == null)
        info = makeScreen("net.unicon.portal.channels.rad.Info");
      info.putParameter("icon", icon);
      info.putParameter("text", text);
      info.putParameter("back", back == null? sid():back);
      if( reinit)
        info.putParameter("refresh","yes");
      return info;
    } catch (Exception e) {
      log(e);
      return this;
    }
  }

  /**
   * Get the Info screen. The RAD property "information" will globally control the Info screen.
   * If it has the value "false" then the method will ignore Info screen and return the back screen.
   * @param text The text (or the local text identifier) to display on Info screen.
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @param back The sid of back screen.
   * @param reinit The flag indicating that the back screen will be
   * re-initialized (i.e. make the call to the method reinit()) when the Info screen is done.
   * @return an instance of the screen Info or the back screen if the RAD property "information" has the "false" value.
   */
  public Screen info(String text, Object[] mfps, String back, boolean reinit) {
    // Check RAD Property information
    if (Channel.m_information == false)
      try {
        Screen scr = (back==null)?this:getScreen(back);
        if( reinit)
          scr.reinit(m_channel.getParameters());
        return scr;
      } catch( Exception e) {
        log(e);
      }

    return info(Info.INFO, getLocalText(text,mfps,null), back, reinit);
  }

  /**
   * Get the Info screen. The RAD property "information" will globally control the Info screen.
   * If it has the value "false" then the method will ignore Info screen and return the back screen.
   * @param text The text (or the local text identifier) to display on Info screen.
   * @param back The sid of back screen.
   * @param reinit The flag indicating that the back screen will be
   * re-initialized (i.e. make the call to the method reinit()) when the Info screen is done.
   * @return an instance of the screen Info or the back screen if the RAD property "information" has the "false" value.
   */
  public Screen info(String text, String back, boolean reinit) {
    // Check RAD Property information
    if (Channel.m_information == false)
      try {
        Screen scr = (back==null)?this:getScreen(back);
        if( reinit)
          scr.reinit(m_channel.getParameters());
        return scr;
      } catch( Exception e) {
        log(e);
      }

    return info(Info.INFO, getLocalText(text,null), back, reinit);
  }

  /**
   * Get the Info screen. The RAD property "information" will globally control the Info screen.
   * If it has the value "false" then the method will ignore Info screen and return the current screen.
   * @param text The text (or the local text identifier) to display on Info screen.
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @param reinit The flag indicating that the back screen (caller of this method) will be
   * re-initialized (i.e. make the call to the method reinit()) when the Info screen is done.
   * @return an instance of the screen Info or the back screen (caller of this method) if the RAD property "information" has the "false" value.
   */
  public Screen info(String text, Object[] mfps, boolean reinit) {
    // Check RAD Property information
    if (Channel.m_information == false)
      try {
        if( reinit)
          reinit(m_channel.getParameters());
        return this;
      } catch( Exception e) {
        log(e);
      }

    return info(Info.INFO, getLocalText(text,mfps,null), null, reinit);
  }

  /**
   * Get the Info screen. The RAD property "information" will globally control the Info screen.
   * If it has the value "false" then the method will ignore Info screen and return the current screen.
   * @param text The text (or the local text identifier) to display on Info screen.
   * @param reinit The flag indicating that the back screen (caller of this method) will be
   * re-initialized (i.e. make the call to the method reinit()) when the Info screen is done.
   * @return an instance of the screen Info or the back screen (caller of this method) if the RAD property "information" has the "false" value.
   */
  public Screen info(String text, boolean reinit) {
    // Check RAD Property information
    if (Channel.m_information == false)
      try {
        if( reinit)
          reinit(m_channel.getParameters());
        return this;
      } catch( Exception e) {
        log(e);
      }

    return info(Info.INFO, getLocalText(text,null), null, reinit);
  }
  //------------------------------------------------------------//

  /**
   * Get the Error screen. It is the Info screen with the error icon.
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @param back The sid of back screen.
   * @return an instance of the screen Info with the error icon.
   */
  public Screen error(String text, Object[] mfps, String back) {
    return info(Info.ERROR, getLocalText(text,mfps,null), back, false);
  }

  /**
   * Get the Error screen. It is the Info screen with the error icon.
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @param back The sid of back screen.
   * @return an instance of the screen Info with the error icon.
   */
  public Screen error(String text, String back) {
    return info(Info.ERROR, getLocalText(text,null), back, false);
  }

  /**
   * Get the Error screen with the given the icon.
   * @param icon The one of Info.INFO, Info.ERROR, Info.EXCEPTION
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @param back The sid of back screen.
   * @return an instance of the screen Info with the given icon.
   */
  public Screen error(String icon, String text, String back) {
    return info(icon, getLocalText(text,null), back, false);
  }


  /**
   * Get the Error screen. It is the Info screen with the error icon. The back screen is the current one.
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @param mfps The parameters passed to the local text. The format is the same
   * as the standard method MessageFormat.format() from the MessageFormat class.
   * @return an instance of the screen Info with the error icon.
   */
  public Screen error(String text, Object[] mfps) {
    return info(Info.ERROR, getLocalText(text,mfps,null), null, false);
  }

  /**
   * Get the Error screen. It is the Info screen with the error icon. The back screen is the current one.
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @return an instance of the screen Info with the error icon.
   */
  public Screen error(String text) {
    return info(Info.ERROR, getLocalText(text,null), null, false);
  }

  //------------------------------------------------------------//

  /**
   * The back screen is the current one.
   * Get the Exception screen. It is the Info screen with the exception icon.
   * @param text The error text (or the local text identifier) to display on Error screen.
   * @return an instance of the screen Info with the error icon.
   */
  public Screen exception(Exception e) {
    return exception(e, null);
  }

  /**
   * Get the Exception screen. It is the Info screen with the exception icon.
   * @param e The message of this Exception object will be displayed on Exception screen.
   * @return an instance of the screen Info with the exception icon.
   */
  public Screen exception(Exception e, String back) {
    Channel.log(e);
    return info(Info.EXCEPTION, getLocalText(e.getMessage(),null), back, false);
  }

  //------------------------------------------------------------//

  /**
   * Get the About screen.
   * @param version The version information.
   * @param copyright The copyright note.
   * @param desc The product description.
   * @param logo The logo icon.
   * @param back The sid of back screen.
   * @return an instance of the About screen.
   */
  public Screen about(String version, String copyright, String desc, String logo, String back) {
    try {
      Screen about = getScreen(About.SID);
      if (about == null)
        about = makeScreen("net.unicon.portal.channels.rad.About");
      Hashtable params = new Hashtable();
      params.put("back",(back == null?sid():back));
      if( version != null)
        params.put("version", version);
      if( copyright != null)
        params.put("copyright", copyright);
      if( desc != null && !desc.equals("PRODUCT_DESC"))
        params.put("desc", desc);
      if( logo != null)
        params.put("logo", logo);
      about.init(params);
      return about;
    } catch (Exception e) {
      m_channel.log(e);
      return this;
    }
  }

  /**
   * Get the About screen with the informations (version, copyright, product description, logo)
   * extracted from the file local.xml
   * @param back The sid of back screen.
   * @return an instance of the About screen.
   */
  public Screen about(String back) {
    return about( getVersion(),
                  getLocalText("COPYRIGHT",null),
                  getLocalText("PRODUCT_DESC",null),
                  getLocalText("LOGO",null), back);
  }

  //--------------------------------------------------------------------------//

  /**
   * The owner ( channel).
   */
  public Channel m_channel = null;

  /**
   * Create an instance of screen with the given class name. It appends the channel package name
   * if the given name is not fully qualified. It also verify the version compatibility
   * of the screen.
   * @param name The class name of the Screen
   * @return The Screen instance of the given class name
   * @throws when the class name is not defined or when the verion of screen is less than RAD version.
   */
  public Screen makeScreen(String name) throws Exception {
    // If full name --> call m_channel method makeScreen
    // If not, get this package and append this name to get full name, then pass to channel method
    if (name.indexOf('.') == -1)
      name = Channel.getPackage( getClass())+"."+name;
    return m_channel.makeScreen(name);
  }

  /**
   * Get an existing instance of Screen based on the given sid.
   * @param sid the sid to get
   * @return the screen with the given sid.
   * @throws Exception if thid sid is null.
   */
  public Screen getScreen(String sid) {
    return m_channel.getScreen(sid);
  }

  //--------------------------------------------------------------------------//

  /**
   * Get data from channel shared area.
   * @param key The key name to get
   * @return the data associated with the given key.
   */
  public Object getShared(Object key) {
    return m_channel.getShared(key);
  }

  /**
   * Change data in the channel shared area.
   * @param key the key name of data to change.
   * @param value the new value of data.
   */
  public void putShared(Object key, Object value) {
    m_channel.putShared(key, value);
  }

  /**
   * Remove the data from channel shared area.
   * @param key the key name of data to remove.
   * @return the data associated with the given key.
   */
  public Object removeShared(Object key) {
    return m_channel.removeShared(key);
  }

  //--------------------------------------------------------------------------//

  /**
   * Invoke a method inside this class, using the java reflection. The method must have the prototype:
   *  public Screen methodName( Hashtable params) throws Exception;
   * @param method the method name to invoke.
   * @param params the Hashtable to pass to the invoked method.
   * @return The return value of invoked method ( Screen).
   */
  public Screen invoke(String method, Hashtable params) {
    try {
      Class decs[] = {Class.forName("java.util.Hashtable")};
      Object args[] = {params};
      return (Screen)getClass().getMethod(method, decs).invoke(this, args);
    } catch (java.lang.reflect.InvocationTargetException i) {
      i.printStackTrace();
      return exception((Exception)i.getTargetException());
    } catch (Exception e) {
      e.printStackTrace();
      return exception(e);
    }
  }

  //----------------------------------------------------------------------//

  protected void log( String s) {
    m_channel.log(s);
  }

  protected void log( Throwable e) {
    m_channel.log(e);
  }

  //----------------------------------------------------------------------//

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

  //----------------------------------------------------------------------//

  /**
   * The debug method used when debug option is TIME. The log message will appended with the datetime information.
   * @param msg The message to log with the datetime information.
   */
  public void timeDebug( String msg) {
    if( Channel.m_debug == Channel.TIME)
      System.out.println(m_channel.logonUser().getAlias() + ": " + getXSL() + ": " + msg + " at " + Channel.getCurrentDate());
  }

  //----------------------------------------------------------------------//

  /**
   * The date formaters.
   */
  protected static SimpleDateFormat[] m_dateFormats = new SimpleDateFormat[] {
        new java.text.SimpleDateFormat("MM/dd/yy hh:mm a"), // default format
        new java.text.SimpleDateFormat("MM/dd/yy") // Alternative format
      };

  /**
   * Format the Date object to String.
   * @param d The Date to format.
   * @return the String with the predefined format.
   */
  public static String format( Date d) {
    return m_dateFormats[0].format(d).toLowerCase();
  }

  /**
   * Parse a string to Date, using the predefined formaters. The acceptable formats are:
   *  - "MM/dd/yy hh:mm a" (default)
   *  - "MM/dd/yy"
   * @param str The String to parse.
   * @return The Date object.
   */
  public static Date parseDate(String str) {
    Date d = null;
    for( int i = 0; i < m_dateFormats.length && d == null; i++)
      d = m_dateFormats[i].parse( str ,new ParsePosition(0));
    return d;
  }

  //-------------------------------------//
  protected Hashtable extractParams(Hashtable params) {
    Hashtable ret = new Hashtable();
    ret.putAll( params);
    ret.remove("sid");
    ret.remove("go");
    ret.remove("do");
    return ret;
  }

  protected void putSystemXSLTParams(Hashtable params) throws Exception {
    params.put("skin", m_channel.m_skin);
    params.put("resourceURL", m_channel.m_crd.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER, m_idempotentURL));
    params.put("sid", sid());
    if( m_renderRoot)
      params.put("root", "true");

    // depending on client side optimization...
    String firstSeparator = "?";
    String baseURL = m_channel.m_crd.getBaseActionURL();
    String other = "targetChannel="+targetChannelId();

    params.put("baseActionURL", baseURL);
    params.put("goURL", baseURL + firstSeparator + other + "&back=" + sid() + "&go");
    params.put("doURL", baseURL + firstSeparator + other + "&sid=" + sid() + "&do");

    // "root" actions
    params.put("rgoURL", baseURL + firstSeparator + other + "&uP_root=root&back=" + sid() + "&go");
    params.put("rdoURL", baseURL + firstSeparator + other + "&uP_root=root&sid=" + sid() + "&do");

    // "me" actions
    String focus = "&focusedChannel="+targetChannelId();
    params.put("mgoURL", baseURL + firstSeparator + other + focus + "&uP_root=me&back=" + sid() + "&go");
    params.put("mdoURL", baseURL + firstSeparator + other + focus + "&uP_root=me&sid=" + sid() + "&do");

    // Xml worker
    UPFileSpec upfs= new UPFileSpec(m_channel.m_crd.getBaseWorkerURL("xml"));
    upfs.setTagId("xml");
    params.put("xmlURL", upfs.getUPFile());
    //System.out.println("+++++ Screen.putSystemXSLTParams+++++++");
    //System.out.println("goURL="+params.get("goURL"));
    //System.out.println("doURL="+params.get("doURL"));
    //System.out.println("rgoURL="+params.get("rgoURL"));
    //System.out.println("rdoURL="+params.get("rdoURL"));
    //System.out.println("mgoURL="+params.get("mgoURL"));
    //System.out.println("mdoURL="+params.get("mdoURL"));
    //System.out.println("++++++++++++++++++++++++++++++++++++++");
  }

  protected String targetChannelId() {
    return m_channel.getSubscribeId();
  }

}




