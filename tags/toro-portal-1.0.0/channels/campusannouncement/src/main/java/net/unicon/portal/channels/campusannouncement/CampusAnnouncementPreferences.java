/*



 *******************************************************************************



 *



 * File:       CampusAnnouncementPreferences.java



 *



 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved



 *



 * This source code is the confidential and proprietary information of Unicon.



 * No part of this work may be modified or used without the prior written



 * consent of Unicon.



 *



 *******************************************************************************



 */



package net.unicon.portal.channels.campusannouncement;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.unicon.portal.channels.campusannouncement.util.CollectionUtil;
import net.unicon.sdk.util.StringUtil;

public class CampusAnnouncementPreferences {



    protected boolean getCampusAnnouncements;



    protected List offeringIDs;



    protected int pageSize;



    protected int ageLimitInDays;


    public CampusAnnouncementPreferences() {



        this(null);



    }



    public CampusAnnouncementPreferences(Map rawPreferences) {



        getCampusAnnouncements = getBooleanValue(rawPreferences, "getCampusAnnouncements", true);

        ArrayList al = new ArrayList();
        /*if (VersionResolver.getPortalVersion().startsWith("2.0"))
          al.add("0");
        else if (VersionResolver.getPortalVersion().startsWith("2.1"))
          al.add("local.0");*/

        offeringIDs = getStringListValue(rawPreferences, "offeringIDs", al);

        pageSize = getIntValue(rawPreferences, "pageSize", 10);

        ageLimitInDays = getIntValue(rawPreferences, "ageLimit", 14);

    }
    
	public CampusAnnouncementPreferences(Map rawPreferences, List defaultGroups) {



		   getCampusAnnouncements = getBooleanValue(rawPreferences, "getCampusAnnouncements", true);

		   ArrayList al =(ArrayList)defaultGroups;
		   /*if (VersionResolver.getPortalVersion().startsWith("2.0"))
			 al.add("0");
		   else if (VersionResolver.getPortalVersion().startsWith("2.1"))
			 al.add("local.0");*/

		   offeringIDs = getStringListValue(rawPreferences, "offeringIDs", al);

		   pageSize = getIntValue(rawPreferences, "pageSize", 10);

		   ageLimitInDays = getIntValue(rawPreferences, "ageLimit", 14);

	   }



    public CampusAnnouncementPreferences(boolean getCampusAnnouncements, List offeringIDs, int pageSize, int ageLimitInDays) {



        this.getCampusAnnouncements = getCampusAnnouncements;



        this.offeringIDs = offeringIDs;



        this.pageSize = pageSize;



        this.ageLimitInDays = ageLimitInDays;



    }



    public boolean getCampusAnnouncements() {



        return getCampusAnnouncements;



    }



    public List getOfferingIDs() {



        return offeringIDs;



    }



    public int getPageSize() {



        return pageSize;



    }



    public int getAgeLimit() {



        return ageLimitInDays;



    }



    protected boolean getBooleanValue(Map rawPreferences, String key, boolean defaultValue) {



        if (rawPreferences == null || !rawPreferences.containsKey(key)) {



            return defaultValue;



        }



        return Boolean.valueOf((String) rawPreferences.get(key)).booleanValue();



    }



    protected int getIntValue(Map rawPreferences, String key, int defaultValue) {



        if (rawPreferences == null || !rawPreferences.containsKey(key)) {



            return defaultValue;



        }



        return Integer.parseInt((String) rawPreferences.get(key));



    }



    protected List getStringListValue(Map rawPreferences, String key, List defaultValue) {



        if (rawPreferences == null || !rawPreferences.containsKey(key)) {



            return defaultValue;



        }



        String encodedList = (String) rawPreferences.get(key);



        return StringUtil.listFromDelimitedString(encodedList, "|");



    }



    public Map asMap() {



        Map answer = new HashMap(10);



        answer.put("getCampusAnnouncements", getCampusAnnouncements ? "true" : "false");


        if (offeringIDs != null)
          answer.put("offeringIDs", CollectionUtil.toString(offeringIDs, "|"));



        answer.put("pageSize", new Integer(pageSize));



        answer.put("ageLimit", new Integer(ageLimitInDays));



        return answer;



    }



    public String toString() {



        return "CampusAnnouncementPreferences\n\tgetCampusAnnouncements = " + getCampusAnnouncements + "\n\tofferingIDs = " +

        offeringIDs + "\n\tpageSize = " + pageSize + "\n\tageLimit = " + ageLimitInDays;



    }



}



