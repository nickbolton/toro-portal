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

package net.unicon.portal.common.service.activation;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import net.unicon.academus.delivery.ReferenceObject;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.PortalObject;
import net.unicon.sdk.time.*;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class ActivationImpl implements Activation {

    protected int activationID;
    protected int offeringID;

    protected String type;

    protected Date startDate;
    protected Date endDate;

    protected long startTime;
    protected long endTime;
    protected long duration;

    protected Map  attributes;

    protected List userNameList;

    protected boolean allUsers;
    protected boolean hasFile;

    protected ReferenceObject urlLink = null;
    
    public ActivationImpl(
        int activationID,
        int offeringID,
        String type,
        Date startDate,
        Date endDate,
        long startTime,
        long endTime,
        Map attributes,
        List userList,
        boolean allUsers) {

        this.activationID = activationID;
        this.offeringID   = offeringID;
        this.type         = type;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.attributes   = attributes;
        this.userNameList = userList;
        this.allUsers     = allUsers;
        this.hasFile      = false;

    }

    public ActivationImpl(
        int activationID,
        int offeringID,
        String type,
        Date startDate,
        Date endDate,
        long startTime,
        long endTime) {
        this (activationID, offeringID,
        type, startDate, endDate,
        startTime, endTime,
        null, null, false);
    }

    /**
     * returns the activation id
     * @return <code>int</code> of the activation id
     */

    public int getActivationID() {
        return this.activationID;
    }

    /**
     * returns the offering id
     * @return <code>int</code> of the offering id
     */
    public int getOfferingID() {
        return this.offeringID;
    }

    /**
     * returns the type of the activation
     * @return <code>String</code> of the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * returns the start date of the activation
     * @return <code>Date</code> of the start date
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * returns the start time of the activation in long
     * @return <code>long</code> of the start time
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * returns the end date of the activation
     * @return <code>Date</code> of the end date
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * returns the end time of the activation in long
     * @return <code>long</code> of the end time
     */
    public long getEndTime() {
        return this.endTime;
    }

    /**
     * returns the duration of  time of the activation in long
     * @return <code>long</code> of the time of the duration
     */
    public long getDuration() {
        return this.duration;
    }

    /**
     * returns the attributes of the activation
     * @return <code>Map<code> of the attributes of the activation
     */
    public Map getAttributes() {
        return this.attributes;
    }

    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        attributes.put(key, value);
    }

    /**
     * returns the List of usernames that are part of the activation
     * In the case that the activation is for all members, the list will be empty
     * @return <code>List</code> of the usernames
     */
    public List getUsernames() {
        return this.userNameList;
    }

    /**
     * return true of false if the activation effects all users
     * @return <code>boolean</code>
     */

    public boolean forAllUsers() {
        return this.allUsers;
    }

    /**
     * set the attributes of the activation
     * @param <code>Map<code> of the attributes of the activation
     */
    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    /**
     * sets the List of usernames that are part of the activation
     * In the case that the activation is for all members, the list will be empty
     * @param <code>List</code> of the usernames
     */
    public void setUsernames(List userList) {
        this.userNameList = userList;
    }

    /**
     * sets true of false if the activation effects all users
     * @param <code>boolean</code>
     */
    public void setForAllUsers(boolean forAllUsers) {
        this.allUsers = forAllUsers;
    }

    /**
     * sets the refrence object in which the activation has to 
     * link too
     * @param <code>ReferenceObject</code>
     */
    public void setReferenceObject(ReferenceObject ref) {
        this.urlLink = ref;
    }
    
    Timestamp getCurrentTime() {
        Timestamp rtnTime = null;
        try {
        TimeService ts = TimeServiceFactory.getService();
        rtnTime = ts.getTimestamp();    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnTime;
    }
    
    private static final String formatpattern = "EEE, MMM d, yyyy";

    private static final String timepattern   = "h:mm a";

    public String toXML() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatpattern);
        SimpleDateFormat timeFormat = new SimpleDateFormat(timepattern);

        StringBuffer xml = new StringBuffer();

        xml.append("<activation");

        /* activation id */
        xml.append(" id=\"");
        xml.append("" + activationID);
        xml.append("\"");

        /* offering id */
        xml.append(" offering_id=\"");
        xml.append("" + offeringID);
        xml.append("\"");

        /* type */
        xml.append(" type=\"");
        xml.append("" + type);
        xml.append("\"");

        /* time_status */
        xml.append(" time_status=\"");

        long currTime = getCurrentTime().getTime();
        Date newDateObject = new Date();
        newDateObject.setTime(getStartTime());
        newDateObject.setMonth(startDate.getMonth());
        newDateObject.setDate(startDate.getDate());
        newDateObject.setYear(startDate.getYear());

        if (currTime < newDateObject.getTime()) {
            // Future
            xml.append("future");
        } else {
            xml.append("available");
        }
        // XXX to check expired if they got through
        // do the a new data object on the end time
        // thanks - H2
        // xml.append("expire");

        xml.append("\"");

        /* has a file */
        xml.append(" hasFile=\"");
        xml.append("" + hasFile);
        xml.append("\"");
        xml.append(">");

        /* CHILD ELEMENTS */

        /* start_date */

        xml.append("<start_date>");
        xml.append(dateFormat.format(startDate));
        xml.append("</start_date>");

        /* start_time */
        xml.append("<start_time>");
        xml.append(timeFormat.format(new Date(startTime)));
        xml.append("</start_time>");

        /* end_date */
        xml.append("<end_date>");
        xml.append(dateFormat.format(endDate));
        xml.append("</end_date>");

        /* end_time */
        xml.append("<end_time>");
        xml.append(timeFormat.format(new Date(endTime)));
        xml.append("</end_time>");

        /* duration */
        xml.append("<duration>");
        xml.append("" + duration);
        xml.append("</duration>");

        /* attributes */
        if (attributes != null) {
            xml.append("<attributes>");
            Iterator iterator = attributes.keySet().iterator();
            String key = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                xml.append("<attribute");
                xml.append(" name=\"");
                xml.append(key);
                xml.append("\"");
                xml.append(">");

                xml.append("<value><![CDATA[");
                xml.append((String) attributes.get(key));
                xml.append("]]></value>");
                xml.append("</attribute>");
            }
            xml.append("</attributes>");
        }

        if (urlLink != null) {
            xml.append(urlLink.toXML());
        }
        
        xml.append("<user-list");
        xml.append(" allusers=\"");
        xml.append("" + allUsers);
        xml.append("\"");
        xml.append(">");

        /* User List */
        if (userNameList != null && !allUsers) {
            try {
                for (int ix = 0; ix < userNameList.size(); ++ix) {
                    xml.append(((User)userNameList.get(ix)).toXML());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        xml.append("</user-list>");
        xml.append("</activation>");
        return xml.toString();
    }

    /**
     * get the Node presentation of the xml object
     * @param Document doc - this is the Document the returned Node will belong to
     * @return <code>Node</code> the xml of the object
     */

    public Node toNode(Document doc) throws Exception {
        throw new Exception("Method not implemented!");
    }

    public boolean hasFile() {
        return hasFile;
    }

    public void setFile(boolean b) {
        hasFile = b;
    }
}

