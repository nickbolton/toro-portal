/*

 *******************************************************************************

 *

 * File:       CampusAnnouncement.java

 *

 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved

 *

 * This source code is the confidential and proprietary information of Unicon.

 * No part of this work may be modified or used without the prior written

 * consent of Unicon.

 *

 *******************************************************************************

 */

package net.unicon.portal.channels.campusannouncement.domain;

import java.text.DateFormat;
import java.util.Date;

import net.unicon.portal.channels.campusannouncement.types.Announcement;

abstract public class AnnouncementImpl implements Announcement {

    protected Number announcementID;

    protected String categoryID;

    protected String categoryName;

    protected String body;

    protected Date date;
    
    private static DateFormat formatter = DateFormat.getDateInstance();

    public AnnouncementImpl(Number announcementID, String categoryID, String categoryName, String body,
    Date submitDate, Date revisedDate) {

        this.announcementID = announcementID;

        this.categoryID = categoryID;

        this.categoryName = categoryName;

        this.body = body;

        this.date = (revisedDate == null) ? submitDate : revisedDate;

    }

    abstract public String getType();

    public Number getID() {

        return announcementID;

    }

    public String getCategoryID() {

        return categoryID;

    }

    public String getCategoryName() {

        return categoryName;

    }

    public String getBody() {
        return body;
    }

    public Date getDate() {

        return date;

    }

    public boolean isCampusWide() {

        return false;

    }

    public boolean isOfferingSpecific() {

        return false;

    }

    public int compareTo(Object other) {

        int sortByVal = 0;
                                                                                                                          
        Announcement otherAnnouncement = (Announcement) other;
                                                                                                                          
        sortByVal = date.compareTo(otherAnnouncement.getDate());
                                                                                                                          
        //John Bodily - The Date type in postgres only tracks dates on a daily resolution...so you don't know which announcement
        //came first if you create several in a day...should have used Timestamp as the db type. As a recourse
        //this will be fixed in the schema...however this code needs to remain as a fix for data from the
        //original schema. So if we know the date is the same we need to sort on database sequence number...
        if (sortByVal == 0){
          try{
            if (((Integer)announcementID).compareTo(((Integer)otherAnnouncement.getID())) > 0){
              sortByVal = 1;
            } else if (((Integer)announcementID).compareTo(((Integer)otherAnnouncement.getID())) < 0){
              sortByVal = -1;
            }
          }catch(ClassCastException cce){
            //for Oracle databases...
            if (((java.math.BigDecimal)announcementID).compareTo(((java.math.BigDecimal)otherAnnouncement.getID())) > 0){
              sortByVal = 1;
            } else if (((java.math.BigDecimal)announcementID).compareTo(((java.math.BigDecimal)otherAnnouncement.getID())) < 0){
              sortByVal = -1;
            }
 
          }
        }
        return -1 * sortByVal;


    }

    public String toXML() {

        StringBuffer buffer = new StringBuffer();

        toXML(buffer);

        return buffer.toString();

    }

    public void toXML(StringBuffer buffer) {
    	buffer.append("<announcement announcement-id=\"");
        buffer.append(getID());
        buffer.append("\" category-id=\"");
        buffer.append(getCategoryID());
        buffer.append("\" category-name=\"");
        buffer.append(getCategoryName());
        
        Date date = getDate();
        if (date != null) {
        buffer.append("\" date=\"");
        //buffer.append(getDate());
        buffer.append(formatter.format(date));
        }
        
        buffer.append("\" type=\"");
        buffer.append(getType());
        buffer.append("\"><announcement-body>");
        // handle returns in body 
        String body = getBody();
        if (body != null) {
        	String[] lines = getBody().split("\\r");
        	for (int i = 0; i < lines.length; i++) {
        		buffer.append("<![CDATA[").append(lines[i]).append("]]>").append("<br/>");
        	}
        }
        buffer.append("</announcement-body></announcement>");
    }
    
    public String toString() {
    	return toXML();
    }

}

