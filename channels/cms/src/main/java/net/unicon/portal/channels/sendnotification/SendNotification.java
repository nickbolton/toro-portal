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

package net.unicon.portal.channels.sendnotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.channels.rad.GroupData;
import net.unicon.portal.common.service.notification.NotificationService;
import net.unicon.portal.common.service.notification.NotificationServiceFactory;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;


public class SendNotification extends BaseChannel implements IServant {

    private static final String sslLocation = "SendNotification.ssl";

    ChannelRuntimeData runtimeData;

    ChannelStaticData staticData;

    boolean INCOMPLETE_DATA = false;

    boolean SENT = false;
    
    String MESSAGE = "";

    int PAGE = 0;

    int TOTALITEMS = 0;

    int CURRENTBLOCK = 0;

    int TOTALPAGES = 0;

    List users;

    boolean expanded = false;

    Map pageHolder = new HashMap();

    boolean done = false;

    public SendNotification () { }

    public void setStaticData(ChannelStaticData sd) {

        this.staticData = sd;

    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {

        this.runtimeData = rd;

        String send = rd.getParameter("send");

        if (send != null) {

            saveSelectedUsers(this.PAGE);

            done = sendNotification(rd);

            //done = true;

        }

    }

    /**
     * Output channel content to the portal
     * @param out a sax document handler
     */

    public void renderXML (ContentHandler documentHandler) throws PortalException {

        Document doc = new org.apache.xerces.dom.DocumentImpl();

        String xslfile = null;

        if (this.SENT) {

            Element InitialScreenElement = doc.createElement("sent");

            doc.appendChild(InitialScreenElement);
            
            if(!this.MESSAGE.equals("")){
                Element warningElement = doc.createElement("warning");
                org.w3c.dom.Text textNode = doc.createTextNode(this.MESSAGE);
                warningElement.appendChild(textNode);
                InitialScreenElement.appendChild(warningElement);
            }

            this.SENT = false;

            this.expanded = false;

            pageHolder.clear();

        } else {

            doc = getMainScreen (doc);

       }

        xslfile = "main";

/* org.jasig.portal.utils.XML printString = new org.jasig.portal.utils.XML ();

      String result = printString.serializeNode(doc);

      System.out.println ("DOM AS STRING == " + result + "\n");

*/

        XSLT xslt = new XSLT (this);

        xslt.setXML(doc);

        xslt.setXSL(sslLocation, xslfile, runtimeData.getBrowserInfo());

        xslt.setTarget(documentHandler);

        String upId = runtimeData.getParameter("upId");

        xslt.setStylesheetParameter("baseActionURL",
            runtimeData.getBaseActionURL());

        xslt.setStylesheetParameter("catPageSize", Integer.toString(evaluatePageSize(runtimeData)));

        xslt.transform();

    }

    private Document getMainScreen (Document doc) {

        Element InitialScreenElement = doc.createElement("display_form");

        // retrieve appName which should have been passed through

        if (this.INCOMPLETE_DATA) {

            Element IncElement = doc.createElement("incomplete");

            InitialScreenElement.appendChild(IncElement);

            this.INCOMPLETE_DATA = false;

        }

        // Offering should be in StaticData, will get it and display Offering name

        Offering offering = (Offering) staticData.get("Offering");

        InitialScreenElement.setAttribute("topic", offering.getTopic().getName());

        InitialScreenElement.setAttribute("offering", offering.getName());

        InitialScreenElement.setAttribute("id", "" + offering.getId());

        // hold any message the user has already entered

        String message = runtimeData.getParameter("message");

        InitialScreenElement.setAttribute("message", (message != null && message.trim().length() > 0) ? message : "");

        // setup state of either expanded or closed

        if (runtimeData.getParameter("action=expand.x") != null)

            this.expanded = true;

        if (runtimeData.getParameter("action=close.x") != null)

            this.expanded = false;

        InitialScreenElement.setAttribute("expand", (this.expanded ? "yes" : "no"));

        if (this.expanded) {

            Element MembersElement = doc.createElement("members");

            // need to setup a pager

            if (runtimeData.getParameter("go=next.x") != null) {

                // must keep track of user's selected when navigating through pages (clicked on Next)

                saveSelectedUsers(this.PAGE);

                this.CURRENTBLOCK += evaluatePageSize(runtimeData);

                this.PAGE++;

            } else if (runtimeData.getParameter("go=back.x") != null) {

                // must keep track of user's selected when navigating through pages (clicked on Back)

                saveSelectedUsers(this.PAGE);

                this.CURRENTBLOCK -= evaluatePageSize(runtimeData);

                this.PAGE--;

            }

            // for loop

            try {

                if (users == null) {

                    users = Memberships.getMembers(offering);

                    // get number of pages of 10 students per page

                    this.PAGE = 1;

                    if (evaluatePageSize(runtimeData) == 0 ||   // Page size set to 'All'
                                users.size() < evaluatePageSize(runtimeData)) {

                        this.TOTALPAGES = 1;

                    } else {

                        this.TOTALPAGES = users.size() / evaluatePageSize(runtimeData);

                        if ((users.size() % evaluatePageSize(runtimeData)) > 0)

                            this.TOTALPAGES++;

                    }

                    this.TOTALITEMS =  users.size();

                }

                MembersElement.setAttribute("page", "" + this.PAGE);

                MembersElement.setAttribute("total", "" + this.TOTALPAGES);

                // Calculate the last in this block.
                int lastInBlock = -1;
                switch (evaluatePageSize(runtimeData)) {
                    case 0:     // All.
                        lastInBlock = this.TOTALITEMS;
                        break;
                    default:    // Some #.
                        lastInBlock = (this.CURRENTBLOCK + evaluatePageSize(runtimeData) < this.TOTALITEMS)
                                                    ?this.CURRENTBLOCK + evaluatePageSize(runtimeData)
                                                    :this.TOTALITEMS;
                        break;
                }

                // Loop & display.
                for (int i = this.CURRENTBLOCK; i < lastInBlock; i++) {

                    User usr = (User) users.get(i);

                    Element MemberElement = doc.createElement("member");

                    MemberElement.setAttribute("name", usr.getFullName());

                    MemberElement.setAttribute("id", usr.getUsername());

                    MemberElement.setAttribute("email", usr.getEmail());

                    // need to check if they exists in the pageHolder MAP

                    if (pageHolder.get(Integer.toString(this.PAGE)) != null) {

                        Map holder = (Map) pageHolder.get(Integer.toString(this.PAGE));

                        MemberElement.setAttribute("selected",
                        (holder.containsKey("m:" + usr.getUsername() + ":" + usr.getFullName() + ":" + usr.getEmail()) ? "yes" : "no"));

                    }

                    MembersElement.appendChild(MemberElement);

                }

            } catch (Exception e) {

                Element ErrorElement = doc.createElement("error");

                ErrorElement.appendChild(doc.createTextNode("Unable to retrieve offering members"));

                InitialScreenElement.appendChild(ErrorElement);

            }

            // end loop

            InitialScreenElement.appendChild(MembersElement);

        }

        doc.appendChild(InitialScreenElement);

        return doc;

    }

    public boolean sendNotification (ChannelRuntimeData rd) throws PortalException {

        String message   = rd.getParameter("message");
        String sendEmail = rd.getParameter("email");
        IdentityData[] recipients = getSelection();

        // Ensure that both a message and recipients have been selected.
        if (recipients == null || (message == null || message.equals(""))) {
            this.INCOMPLETE_DATA = true;
            return false;
        }

        // Begin: Send to Notification

        int sendFlag = NotificationService.TYPE_NOTIFICATION;
        if (sendEmail != null)
            sendFlag |= NotificationService.TYPE_EMAIL;

        String subject = staticData.getPerson().getFullName() + " has sent you a Notification";

        // Prepend the offering and topic names to the subject
        Offering offering = (Offering) staticData.get("Offering");
        if (offering != null)
            subject = (new StringBuffer("["))
                .append(offering.getTopic().getName())
                .append(" - ")
                .append(offering.getName())
                .append("] ")
                .append(subject)
                .toString();

        String body = message;

        // Sender IdentityData (E#u#id#displayname#email)
        IdentityData sender = new IdentityData();
        sender.putEntityType(GroupData.S_USER);
        sender.putType(IdentityData.ENTITY);
        sender.putID("" + staticData.getPerson().getID());
        sender.putAlias((String)staticData.getPerson().getAttribute("username"));
        sender.putName(staticData.getPerson().getFullName());
        sender.putEmail((String)staticData.getPerson().getAttribute("mail"));

        try {
            NotificationService notifcationService = NotificationServiceFactory.getService();
            notifcationService.sendNotifications(
                                recipients, sender, subject, body, sendFlag);
            this.SENT = true;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, "SendNotification::sendNotification. Error sending Notification; Exception = " + e);
            e.printStackTrace(System.err);
            this.SENT = true;
            this.MESSAGE = e.getMessage();
        }
        // End: Send to Notification

        return true;
    }

    private IdentityData[] getSelection() throws PortalException {

        String value = runtimeData.getParameter("checked");

        if (value != null){
        	if(value.toLowerCase().startsWith("o")) {
	            // user is sending to the entire offering	
	            int pos1 = value.indexOf(":");	
	            int pos2 = value.indexOf(":", pos1 + 1);	
	            long oId = Long.parseLong(value.substring(pos1 + 1, pos2));	
	            Offering o = null;	
	            List targets = null;

	            try {
	                o = OfferingFactory.getOffering(oId);	
	                targets = Memberships.getMembers(o);
	            } catch (Exception e) {
	            	throw new PortalException("Unable to obtain offering or members where offeringId=" + oId, e);
	            }
	            
	            // Get out if there are no members.
	            if (targets.size() == 0) return null;

	            // Loop & make the array.
	            IdentityData[] ids = new IdentityData[targets.size()];
	            User u = null;
	            IdentityData id = null;

	            for (int i = 0; i < targets.size(); i++) {	
	                u = (User) targets.get(i);	
	                id = new IdentityData();	
	                id.putType(IdentityData.ENTITY);	
	                id.putEntityType(GroupData.S_USER);	
                    id.putID(""+u.getId());
	                id.putAlias(u.getUsername());	
	                id.putName(u.getFullName());	
	                id.putEmail(u.getEmail());	
	                ids[i] = id;	
	            }
	            return ids;
            } else {
                // send to selected users.
                ArrayList ids = new ArrayList(this.TOTALITEMS);

                for (int i = 1; i < (this.TOTALPAGES + 1); i++) {
                    Map holder = (Map) pageHolder.get(Integer.toString(i));
                    if (holder == null) {
                        continue;
                    }

                    Iterator itr = holder.values().iterator();
                    while (itr.hasNext()) {	
                        IdentityData id = (IdentityData)itr.next();
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }

                return (IdentityData[])ids.toArray(new IdentityData[0]);
	        }
    	}


        return null;
    
    }

    public void saveSelectedUsers (int Page) {

        Map holder = null;

        if (pageHolder.get(Integer.toString(Page)) == null)

            holder = new HashMap();

        else

            holder = (Map) pageHolder.get(Integer.toString(Page));

        String[] values = runtimeData.getParameterValues("checked");

        if (!holder.isEmpty())

            holder.clear();

        if (values != null) {

            for (int i = 0; i < values.length; i++) {

                // name=(Offering|Member):(OfferingID|MemberUserName)

                // ie: o:39494 indicates Offering with id of 39494

                // ie: m:flopez indicates Member with username flopez

                IdentityData id = new IdentityData ();

                StringTokenizer st = new StringTokenizer(values[i], ":");

                if (st.hasMoreTokens())

                    id.putType((st.nextToken().equals("m") ? id.ENTITY : "O"));

                id.putEntityType("u");

                if (st.hasMoreTokens()) id.putID(st.nextToken());

                if (st.hasMoreTokens())

                    id.putName(st.nextToken());

                if (st.hasMoreTokens())

                    id.putEmail(st.nextToken());

                holder.put(values[i], id);

            }

        }

        // store this pageHolder map inside another map

        pageHolder.put(Integer.toString(Page), holder);

    }

    // IServant method

    public boolean isFinished() {

        return done;

    }

    // IServant method

    public Object[] getResults () {

        return null;

    }

    private int evaluatePageSize(ChannelRuntimeData rd) {

        int pgSize = 10;    // Default.

        String strPageSize = rd.getParameter("catPageSize");

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

    private static void spillParameters(ChannelRuntimeData crd) {

        StringBuffer rslt = new StringBuffer();

        java.util.Enumeration keys = crd.getParameterNames();

        while (keys.hasMoreElements()) {

            String key = (String) keys.nextElement();

            String value = (String) crd.getParameter(key);

            rslt.append("\t" + key + "=" + value + "\n");

        }
        
    }

}

