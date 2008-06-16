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

package com.interactivebusiness.portal.channel.utils;

import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.IServant;
import org.jasig.portal.services.LogService;
import ca.ubc.itservices.portal.cryptowallet.Wallet;
import ca.ubc.itservices.portal.cryptowallet.WalletManager;
import ca.ubc.itservices.portal.cryptowallet.WalletException;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Enumeration;

/**
 * This is a WalletBase Utility class used to communicate with
 * the UBC CryptoWallet API.
 * @author Freddy Lopez, flopez@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class WalletBase extends BaseChannel implements IServant
{
  private static final String sslLocation = "WalletBase/WalletBase.ssl";
  private Map credentials;
  public WalletBase () {}

  public boolean storeWalletCredentials (String sApp, Map mCredentials)
  {
    try
    {
      // retrieve user's wallet if exists
      byte[] userID = ((String) staticData.getPerson().getAttribute("username")).getBytes();
      byte[] password = getPassword();
      Wallet userwallet = WalletManager.getWallet(userID, password);

      if (userwallet == null)
        userwallet = new Wallet(userID, password, null);

      if (userwallet != null && userwallet.containsKey(sApp))
        userwallet.remove(sApp);

      userwallet.put(sApp, mCredentials);
      // save wallet to DB
      WalletManager.putWallet(userID, password, userwallet);
    }
    catch (WalletException we)
    {
      LogService.instance().log (LogService.ERROR, "storeWalletCredentials Failed. "+we);
      return false;
    }
    return true;
  }

  public Map getWalletCredentials (IPerson p) {
    return getWalletCredentials ((String) staticData.get("appName"));
  }

  public Map getWalletCredentials (String sApp)
  {
    try
    {
      if (this.credentials != null && this.credentials.size() != 0)
        return this.credentials;

      byte[] userID = ((String) staticData.getPerson().getAttribute("username")).getBytes();
      byte[] password = getPassword();

      WalletManager.init();
      Wallet myWallet = WalletManager.getWallet(userID, password);
      if (myWallet != null)
      {
        Map myInfo = (Map) myWallet.get(sApp);
        if (myInfo != null)
          return myInfo;
        else
          return null;
      }
    }
    catch (WalletException we)
    {
      LogService.instance().log (LogService.ERROR, "getWalletCredentials Failed. "+we);
      return null;
    }
    return null;
  }

  public byte[] getPassword ()
  {

    String pw = null;
    ISecurityContext ic = staticData.getPerson().getSecurityContext();
    IOpaqueCredentials oc = ic.getOpaqueCredentials();
    if (oc instanceof NotSoOpaqueCredentials)
    {
      NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)oc;
      pw = nsoc.getCredentials();
    }

    if (pw == null) {
      // loop through subcontexts to find cached credentials
      Enumeration en = ic.getSubContexts();
      while (en.hasMoreElements())
      {
        ISecurityContext sctx = (ISecurityContext)en.nextElement();
        IOpaqueCredentials soc = sctx.getOpaqueCredentials();
        if (soc instanceof NotSoOpaqueCredentials)
        {
          NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)soc;
          pw = nsoc.getCredentials();
          if (pw != null)
            break;
        }
      }
    }

    if (pw != null)
      return pw.getBytes();
    else
      return null;
  }

  public void removeWalletCredentials (String sApp)
  {
    try
    {
      // retrieve user's wallet if exists
      byte[] userID = ((String) staticData.getPerson().getAttribute("username")).getBytes();
      byte[] password = getPassword();
      Wallet userwallet = WalletManager.getWallet(userID, password);

      if (userwallet.containsKey(sApp))
        userwallet.remove(sApp);

      WalletManager.putWallet(userID, password, userwallet);
    }
    catch (WalletException we)
    {
      LogService.instance().log (LogService.ERROR, "removeWalletCredentials Failed. "+we);
    }

  }


  public void setRuntimeData(ChannelRuntimeData rd)
  {
    this.runtimeData = rd;

    String username = rd.getParameter("username");
    String password = rd.getParameter("password");

    if (username != null && password != null)
      storeCredentials (username, password);

    if (rd.getParameter("loginError") != null) {
      setLoginError (rd);
      this.credentials = null;
    }
  }


  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (org.xml.sax.ContentHandler documentHandler) throws PortalException
  {
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    ChannelRuntimeData rd = this.runtimeData;
    String xslfile = null;

    String sAction = rd.getParameter("x_action");

    if (getLoginError())
    {
      doc = getInputDataScreen (doc, true);
      xslfile = "displayinitialscreen";
    }
    else if(sAction == null || sAction.equals(""))
    {
      doc = getInputDataScreen (doc, false);
      xslfile = "displayinitialscreen";
    }

    XSLT xslt = new XSLT (this);
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, xslfile, runtimeData.getBrowserInfo());
    xslt.setTarget(documentHandler);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.transform();

  }

  private void storeCredentials (String username, String password)
  {
    if (username.equals("") || password.equals(""))
    {
      staticData.setParameter("WalletServantFinished", "false");
      LOGINERROR = true;
      this.credentials = null;
      if (staticData.containsKey("Credentials"))
        staticData.remove("Credentials");
    }
    else
    {
      staticData.setParameter("WalletServantFinished", "true");
      credentials = new HashMap ();
      credentials.put ("username", username);
      credentials.put ("password", password);
      staticData.put("Credentials", credentials);
    }
  }

  private Document getInputDataScreen (Document doc, boolean withError)
  {
    Element InitialScreenElement = doc.createElement("display_form");
    // retrieve appName which should have been passed through
    String appName = (String) staticData.get("appName");
    InitialScreenElement.setAttribute("appName", (appName != null ? appName : ""));
    if (withError)
    {
      Element ErrorElement = doc.createElement("error");
      ErrorElement.appendChild(doc.createTextNode("error"));
      InitialScreenElement.appendChild(ErrorElement);
    }

    doc.appendChild(InitialScreenElement);
    return doc;
  }

  private boolean LOGINERROR = false;
  public boolean getLoginError () { return LOGINERROR;}
  public void setLoginError (boolean value) {LOGINERROR = value; }
  public void setLoginError (ChannelRuntimeData rd)
  {
    Boolean value = Boolean.valueOf(rd.getParameter("loginError"));
    LOGINERROR = value.booleanValue();
  }

  // IServant method
  public boolean isFinished ()
  {
    boolean isFinished = false;
    if (staticData.containsKey("WalletServantFinished") && staticData.getParameter("WalletServantFinished").equals("true"))
      isFinished = true;
    return  isFinished;
  }

  // IServant method
  public Object[] getResults ()
  {
    Object[] results = new Object[1];
    if (staticData.containsKey("Credentials"))
      results[0] = staticData.get("Credentials");
    return results;
  }

}
