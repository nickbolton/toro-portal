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
package net.unicon.academus.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;



public class OfferingData
    extends GroupData
{    
   /**
    * Constructor for a <code>OfferingData</code> object.
    *
    * @param  groupID       The offering's group ID 
    * @param  groupSource   The offering's group source
    * @param  shortname     The offering's group name 
    * @param  title         The offering's group title
    * @param  description   The offering's group description
    * @param  data          Extra data applicable to the offering
    *
    * @see    UCFImportAdapter
    */

    public OfferingData(String groupID, String groupSource, 
                        String shortName, String title, String description,
                        String email, String url, TimeframeData timeData,
                        String enrollModel, String role, 
                        RelationshipsData r, MeetingData[] m, HashMap data)
    {
        super(groupID, groupSource, 
            shortName, title, description, r, data);
        __email = email;
        __url = url;
        __timeData = timeData;
        __enrollModel = enrollModel;
        __defaultRole = role;
        
        // save the Meeting Data
        __meetingData = m;
    }
    
    public String getOfferingID() {
        return super.getGroupId();
    }
    public void setOfferingID(String offeringID) {
        super.setGroupId(offeringID);
    }
    public void setOfferingID(long offeringID) {
        setOfferingID(String.valueOf(offeringID));
    }
    
    public String getEmail() {
        return __email;
    }
    public String getURL() {
        return __url;
    }
    public String[] getTopicIDs() {
        return super.getParentIDs();
    }
    public void setTopicIDs(String[] topicIDs) {
        super.setParentIDs(topicIDs);
    }
    
    public TimeframeData getTimeData() {
        return __timeData;
    }
    public String getEnrollmentModel() {
        return __enrollModel;
    }
    public String getDefaultRole() {
        return __defaultRole;
    }
    public MeetingData[] getMeetingData() {
        return __meetingData;
    }

    public String toString() {
        StringBuffer meetingData = new StringBuffer();
        if (__meetingData != null && __meetingData.length > 0) {
            for (int i = 0; i < __meetingData.length; ) {
                meetingData.append(__meetingData[i++].toString() + '\t');
            }
        } 
        StringBuffer topicData = new StringBuffer();
        String[] topicIDs = getTopicIDs();
        if (topicIDs != null) {
            for (int i = 0; i < topicIDs.length; ) {
                topicData.append(topicIDs[i++] + '\t');
            }
        } 

        return "Offering ID:\t " + getOfferingID() + '\n' +
               "Email:\t" + getEmail() + '\n' +
               "URL:\t" + getURL() + '\n' +
               "Topic IDs:\t" + topicData.toString() + '\n' +
                "MeetingData:\t" + meetingData.toString() + '\n' +
                "TimeframeData:\t" + getTimeData().toString() + '\n' +
                "Default Role:\t" + getDefaultRole() + '\n' +
                "Enrollment Model:\t" + getEnrollmentModel() + '\n';
    }
                
    private String __email;
    private String __url;
    private TimeframeData __timeData;    
    private String __enrollModel;
    private String __defaultRole;
    private MeetingData[] __meetingData;
}
