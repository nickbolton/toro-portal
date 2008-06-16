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

package net.unicon.portal.channels.login;

import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import javax.servlet.http.HttpSession;

import net.unicon.academus.apps.notification.Notification;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.common.service.notification.NotificationServiceImpl;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.ContentHandler;

/**
 * <p>Allows a user to login to the portal.  Login info is posted to
 * <code>LoginServlet</code>.  If user enters incorrect username and
 * password, he/she is instructed to login again with a different
 * password (the username of the previous attempt is preserved).</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $LastChangedRevision$
 */
public class CLogin implements IPrivilegedChannel, ICacheable
{
  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;
  private String channelName = "Log in...";
  private String attemptedUserName="";
  private static final String sslLocation = "CLogin.ssl";
  private boolean bAuthenticated = false;
  private boolean bauthenticationAttemptFailed = false;
  private boolean bSecurityError = false;
  private String xslUriForKey = null;

  private static final String systemCacheId="net.unicon.uportal.channels.login.CLogin";

  private boolean hasNotification = false;
  private boolean hasChanged = false;

  private static final boolean checkNotis = checkNotiSupport();

  private ISecurityContext ic;

  private static boolean checkNotiSupport() {
      boolean rslt = false;
      String className =
          UniconPropertiesFactory
                .getManager(PortalPropertiesType.FACTORY)
                .getProperty("net.unicon.portal.common.service.notification.NotificationService.implementation");
      if (NotificationServiceImpl.class.getName().equals(className))
          rslt = true;
      return rslt;
  }

  public CLogin()
  {
  }

  public void setPortalControlStructures(PortalControlStructures pcs)
  {
    HttpSession session = pcs.getHttpSession();
    String authenticationAttempted = (String)session.getAttribute("up_authenticationAttempted");
    String authenticationError = (String)session.getAttribute("up_authenticationError");
    attemptedUserName = (String)session.getAttribute("up_attemptedUserName");

    if (authenticationAttempted != null)
      bauthenticationAttemptFailed = true;

    if (authenticationError != null)
      bSecurityError = true;
  }

  public ChannelRuntimeProperties getRuntimeProperties()
  {
    return new ChannelRuntimeProperties();
  }

  public void receiveEvent(PortalEvent ev)
  {
  }

  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
    ic = staticData.getPerson().getSecurityContext();

    if (ic!=null && ic.isAuthenticated())
      bAuthenticated = true;

    if (checkNotis)
        hasNewNotifications();
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
  }

  public void renderXML (ContentHandler out) throws PortalException
  {
    String fullName = (String)staticData.getPerson().getFullName();
    Document doc = DocumentFactory.getNewDocument();

    // Create <login-status> element
    Element loginStatusElement = doc.createElement("login-status");

    if (bSecurityError)
    {
      // Create <error> element under <login-status>
      Element errorElement = doc.createElement("error");
      loginStatusElement.appendChild(errorElement);
    }
    else if (bauthenticationAttemptFailed && !bAuthenticated)
    {
      // Create <failure> element under <login-status>
      Element failureElement = doc.createElement("failure");
      failureElement.setAttribute("attemptedUserName", attemptedUserName);
      loginStatusElement.appendChild(failureElement);
    }
    else if (fullName != null)
    {
      // Create <full-name> element under <header>
      Element fullNameElement = doc.createElement("full-name");
      fullNameElement.appendChild(doc.createTextNode(fullName));
      loginStatusElement.appendChild(fullNameElement);
    }

    doc.appendChild(loginStatusElement);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("unauthenticated", String.valueOf(!staticData.getPerson().getSecurityContext().isAuthenticated()));

	if (checkNotis && !staticData.getPerson().isGuest()) {
		if (this.hasNotification) {
			xslt.setStylesheetParameter("message", "Y");
			//globalIDContext = null;
			try {
				// Get the context that holds the global IDs for this user
				Context globalIDContext = (Context)staticData.getJNDIContext().lookup("/channel-ids");
				String subscrID = (String)globalIDContext.lookup("notifications");
				xslt.setStylesheetParameter("noti_id", subscrID);
			} catch (NotContextException nce) {
				LogService.instance().log(LogService.ERROR, "CLogin.getUserXML(): Could not find subcontext /channel-ids in JNDI");
			} catch (NamingException e) {
				LogService.instance().log(LogService.ERROR, e);
			}
		}
	}

    xslt.transform();
  }

	public boolean hasNewNotifications () {

		if (staticData.getPerson().isGuest() ) {
			// We do not check notifications for a guest user.
			this.hasChanged = false;
			this.hasNotification = false;
			return false;
		}

		Vector m_notifications;
		try {
			IdentityData id = new IdentityData ();
			id.putEntityType("u");
			id.putType(id.ENTITY);
			String userName = (String)staticData.getPerson().getAttribute("username");
			id.putID(userName);
			id.putAlias(userName);
			m_notifications = Notification.listNotifications(id);
			// if found something, then exit right away
			if (!m_notifications.isEmpty()) {
				this.hasChanged = (this.hasNotification ? false : true);
				this.hasNotification = true;
				return true;
			} else {
				this.hasChanged = (!this.hasNotification ? false : true);
				this.hasNotification = false;
				return false;
			}

		} catch (Exception e) {
			this.hasChanged = (!this.hasNotification ? false : true);
			this.hasNotification = false;
			return false;
		}
	}

  public ChannelCacheKey generateKey() {
    ChannelCacheKey k = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);
    // guest pages are cached system-wide
    if(staticData.getPerson().isGuest()) {
      k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
      sbKey.append(systemCacheId);
    } else {
      k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    }
    sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
    sbKey.append("authenticated:").append(staticData.getPerson().getSecurityContext().isAuthenticated()).append(", ");

    if(xslUriForKey == null) {
      try {
        String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
        xslUriForKey=XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo());
      } catch (PortalException pe) {
        xslUriForKey = "Not attainable!";
      }
    }
    sbKey.append("xslUri:").append(xslUriForKey).append(", ");
    sbKey.append("bAuthenticated:").append(bAuthenticated).append(", ");
    sbKey.append("bauthenticationAttemptFailed:").append(bauthenticationAttemptFailed).append(", ");
    sbKey.append("attemptedUserName:").append(attemptedUserName).append(", ");
    sbKey.append("bSecurityError:").append(bSecurityError).append(", ");
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return k;
  }

  public boolean isCacheValid(Object validity) {

    if (checkNotis)
        hasNewNotifications();
	return !this.hasChanged;
  }

}

