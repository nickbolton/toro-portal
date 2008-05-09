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
package net.unicon.academus.service.adapters;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import net.unicon.academus.common.AcademusException;
import net.unicon.academus.service.ClubData;
import net.unicon.academus.service.DepartmentData;
import net.unicon.academus.service.DivisionData;
import net.unicon.academus.service.GroupData;
import net.unicon.academus.service.MeetingData;
import net.unicon.academus.service.MemberData;
import net.unicon.academus.service.OfferingData;
import net.unicon.academus.service.RelationshipsData;
import net.unicon.academus.service.SubgroupData;
import net.unicon.academus.service.TimeframeData;
import net.unicon.academus.service.TopicData;
import net.unicon.academus.service.UserData;

/**
 * Class for importing the UNICON Canonical format of a Datatel export document.
 *
 * @author      Alex Bragg
 */

public class UCFImportAdapter
{

   /**
    * This method parses a Dom4j Element and returns a <code>UserData</code>
    * object.
    *
    * @param  e  Dom4j element containing user data in the UNICON Canonical
    *            Format
    *
    * @see UserData
    * @throws AcademusException
    */

    public static UserData parseUser(Element e) throws AcademusException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        UserData rslt = null;

        try {
            // Source info.
            String id = e.valueOf("source-info/id");
            String src = e.valueOf("source-info/source");
            String fId_val = e.valueOf("source-info/foreign-id");
            String foreignId = fId_val.equals("")?null:fId_val;

            // Username.
            String userName  = e.attributeValue("username");

            // Name.
            String prefix = e.valueOf("name/prefix");
            String firstName = e.valueOf("name/first");
            String lastName = e.valueOf("name/last");
            String suffix = e.valueOf("name/suffix");

            // Contact information.
            String email = e.valueOf("contact-info/email");
            HashMap phoneNumbers = new HashMap();
            Iterator it = e.selectNodes("contact-info/phone").iterator();
            while (it.hasNext()) {
                Element ph = (Element) it.next();
                phoneNumbers.put(ph.attributeValue("type"), ph.getText());
            }

            // Address.
            String addr1 = e.valueOf("contact-info/*/line1");
            String addr2 = e.valueOf("contact-info/*/line2");
            String city = e.valueOf("contact-info/*/city");
            String state = e.valueOf("contact-info/*/state");
            String zip = e.valueOf("contact-info/*/zip");
            String country = e.valueOf("contact-info/*/country");
            String role = e.valueOf("role/system");

            // Optional information.
            HashMap attrs = new HashMap();
            attrs.put(UserData.PREFIX, prefix);
            attrs.put(UserData.SUFFIX, suffix);
            attrs.put(UserData.ADDRESS1, addr1);
            attrs.put(UserData.ADDRESS2, addr2);
            attrs.put(UserData.CITY, city);
            attrs.put(UserData.STATE, state);
            attrs.put(UserData.ZIP, zip);
            attrs.put(UserData.PHONE, phoneNumbers);
            attrs.put(UserData.COUNTRY, country);
            attrs.put(UserData.SYSTEM_ROLE, role);

            rslt = new UserData(id, src, foreignId, userName, firstName,
                            lastName, email, null /* passwd */, attrs);

        } catch (Throwable t) {
            String msg = "Unable to parse UserData.";
            throw new AcademusException(msg, t);
        }

        return rslt;

    }

    /**
    * This method parses a Dom4j Element and returns a
    * <code>GroupData</code> object.
    *
    * @param  e  Dom4j element containing group data (CLUBS, DIVISIONS, DEPARTMENTS in the UNICON Canonical
    *            Format
    *
    * @see GroupData
    * @throws AcademusException
    */
    public static GroupData parseGroup(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        GroupData rslt = null;

           // Create the GroupData object and add the data

        if (e.selectSingleNode("club") != null)
        {
            rslt = parseClub(e);
        }
        else if (e.selectSingleNode("division") != null)
        {
            rslt = parseDivision(e);
        }
        else if (e.selectSingleNode("department") != null)
        {
            rslt = parseDepartment(e);
        }
        else if (e.selectSingleNode("generic-group") != null)
        {
            rslt = parseGenericGroup(e);
        }

        return rslt;
    }

   /**
    * This method parses a Dom4j Element and returns a
    * <code>OfferingData</code> object.
    *
    * @param  e  Dom4j element containing offering data in the UNICON Canonical
    *            Format
    *
    * @see UserData
    * @throws AcademusException
    */

    public static OfferingData parseOffering(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (e.selectSingleNode("offering") == null)
        {
            String msg = "Argument 'e [Element]' must be of type offering.";
            throw new IllegalArgumentException(msg);
        }

        OfferingData rslt = null;
        HashMap data = new HashMap();
        List l;
        RelationshipsData relationData = null;
        TimeframeData timeData = null;
        MeetingData[] meetingData;

            // get the id and source for the group

        String id = e.valueOf("source-info/id");
        String src = e.valueOf("source-info/source");
        String shortName = e.valueOf("offering/shortname");
        String title = e.valueOf("offering/title");
        String description = e.valueOf("offering/description");
        String email = e.valueOf("offering/email");
        String url = e.valueOf("offering/url");
        String enrollModel = e.valueOf("offering/enrollment/model");
        String role = e.valueOf("offering/enrollment/default-role");

            // parse timeframe data and create a timeframe object

        String startDate= e.valueOf("offering/timeframe/start/date");
        startDate = startDate.equals("")?null:startDate + " 00:00:00.0";

        String endDate = e.valueOf("offering/timeframe/end/date");
        endDate = endDate.equals("")?null:endDate + " 00:00:00.0";

        String term = e.valueOf("offering/timeframe/term");
        term = term.equals("")?null:term;

        timeData = new TimeframeData(startDate, endDate, term);

        // parse meeting data and create meeting objects
        l = e.selectNodes("offering/meeting");
        meetingData = new MeetingData[l.size()];
        for(int i = 0; i < l.size(); i++)
        {
            Element n = (Element) l.get(i);
            meetingData[i] = new MeetingData(
                     n.valueOf("start/date") + " " +
                     n.valueOf("start/time"),

                     n.valueOf("end/date") + " " +
                     n.valueOf("end/time"),

                     n.valueOf("location"),
                     n.valueOf("days"),
                     n.valueOf("recurrence")
                    );
        }

           // Get any relationships for the offering

        Element n = (Element) e.selectSingleNode("offering/relationships");
        if (n != null)
        {
            relationData = parseRelationships(n);
        }

           // Create the OfferingData object and add the data

        rslt = new OfferingData(
                                id,
                                src,
                                shortName,
                                title,
                                description,
                                email,
                                url,
                                timeData,
                                enrollModel,
                                role,
                                relationData,
                                meetingData,
                                data
                                );

        return rslt;
    }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>RelationshipsData</code> object.
    *
    * @param  e  Dom4j element containing relationships data in the
    *            UNICON Canonical Format
    *
    * @see Relationships
    * @throws AcademusException
    */

    public static RelationshipsData parseRelationships(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (!"relationships".equalsIgnoreCase(e.getName()) )
        {
            String msg = "Argument 'e [Element]' must be of type relationships.";
            throw new IllegalArgumentException(msg);
        }


        String key, value, type;
        List l;
        RelationshipsData r = new RelationshipsData();

        r.setSource(e.attributeValue("groupsource-ref"));
        r.setGroupIdRef(e.attributeValue("groupid-ref"));

        l = e.selectNodes("*");
        for(int i = 0; i < l.size(); i++)
        {
            type = ((Element)l.get(i)).getName();
            key = ((Element)l.get(i)).attributeValue("groupid-ref");
            value = ((Element)l.get(i)).getText();

            if ( "child".equalsIgnoreCase(type) )
                r.addChild(key,value);
            if ( "parent".equalsIgnoreCase(type) )
                r.addParent(key,value);
            if ( "sibling".equalsIgnoreCase(type) )
                r.addSibling(key,value);
            if ( "alias".equalsIgnoreCase(type) )
                r.addAlias(key,value);
        }

        return r;

    }

   /**
    * This method parses a Dom4j Element and returns a
    * <code>SubgroupData</code> object.
    *
    * @param  e  Dom4j element containing SubgroupData data in the
    *            UNICON Canonical Format
    *
    * @see SubgroupData
    * @throws AcademusException
    */

    public static SubgroupData parseSubgroup(Element e)
        throws AcademusException
    {
            // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (!"subgroup".equalsIgnoreCase(e.getName()) )
        {
            String msg = "Argument 'e [Element]' must be of type subgroup.";
            throw new IllegalArgumentException(msg);
        }

            // new subgroup
        SubgroupData s = new SubgroupData(
               e.attributeValue("source-ref"),
               e.attributeValue("groupid-ref"),
               e.attributeValue("subgroupid-ref"),
               e.valueOf("status"),
               e.valueOf("group-role")
               );

        return s;
    }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>TopicData</code> object.
    *
    * @param  e  Dom4j element containing topic data in the
    *            UNICON Canonical Format
    *
    * @see TopicData
    * @throws AcademusException
    */

    public static TopicData parseTopic(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (e.selectSingleNode("topic") == null)
        {
            String msg = "Argument 'e [Element]' must be of type topic.";
            throw new IllegalArgumentException(msg);
        }

        HashMap data = new HashMap();
        RelationshipsData r = new RelationshipsData();

        String id = e.valueOf("source-info/id");
        String src = e.valueOf("source-info/source");
        String shortName = e.valueOf("topic/shortname");
        String title = e.valueOf("topic/title");
        String description = e.valueOf("topic/description");

           // Get any relationships for the topic and add them
           // to the group object

        Element rdata = (Element) e.selectSingleNode("topic/relationships");
        if(rdata != null)
        {
            r = parseRelationships(rdata);
        }

        return new TopicData(id, src, shortName, title, description, r, data);
    }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>ClubData</code> object.
    *
    * @param  e  Dom4j element containing club data in the
    *            UNICON Canonical Format
    *
    * @see ClubData
    * @throws AcademusException
    */

    public static ClubData parseClub(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (e.selectSingleNode("club") == null)
        {
            String msg = "Argument 'e [Element]' must be of type club.";
            throw new IllegalArgumentException(msg);
        }

        HashMap data = new HashMap();
        RelationshipsData r = new RelationshipsData();

        String id = e.valueOf("source-info/id");
        String src = e.valueOf("source-info/source");
        String shortName = e.valueOf("club/shortname");
        String title = e.valueOf("club/title");
        String description = e.valueOf("club/description");

           // Get any relationships for the topic and add them
           // to the group object

        Element rdata = (Element) e.selectSingleNode("club/relationships");
        if(rdata != null)
        {
            r = parseRelationships(rdata);
        }

        return new ClubData(id, src, shortName, title, description, r, data);
    }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>DepartmentData</code> object.
    *
    * @param  e  Dom4j element containing department data in the
    *            UNICON Canonical Format
    *
    * @see DepartmentData
    * @throws AcademusException
    */

    public static DepartmentData parseDepartment(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (e.selectSingleNode("department") == null)
        {
            String msg = "Argument 'e [Element]' must be of type department.";
            throw new IllegalArgumentException(msg);
        }

        HashMap data = new HashMap();
        RelationshipsData r = new RelationshipsData();

        String id = e.valueOf("source-info/id");
        String src = e.valueOf("source-info/source");
        String shortName = e.valueOf("department/shortname");
        String title = e.valueOf("department/title");
        String description = e.valueOf("department/description");

           // Get any relationships for the topic and add them
           // to the group object

        Element rdata = (Element) e.selectSingleNode("department/relationships");
        if(rdata != null)
        {
            r = parseRelationships(rdata);
        }

        return new DepartmentData(id, src, shortName, title, description, r, data);
    }
    
    /**
     * This method parses a Dom4j Element and returns a
     * <code>GroupData</code> object.
     *
     * @param  e  Dom4j element containing department data in the
     *            UNICON Canonical Format
     *
     * @see GroupData
     * @throws AcademusException
     */

     public static GroupData parseGenericGroup(Element e)
         throws AcademusException
     {
         // Assertions.
         if (e == null)
         {
             String msg = "Argument 'e [Element]' cannot be null.";
             throw new IllegalArgumentException(msg);
         }
         else if (e.selectSingleNode("generic-group") == null)
         {
             String msg = "Argument 'e [Element]' must be of type generic-group.";
             throw new IllegalArgumentException(msg);
         }

         HashMap data = new HashMap();
         RelationshipsData r = new RelationshipsData();

         String id = e.valueOf("source-info/id");
         String src = e.valueOf("source-info/source");
         String shortName = e.valueOf("generic-group/shortname");
         String title = e.valueOf("generic-group/title");
         String description = e.valueOf("generic-group/description");

            // Get any relationships for the topic and add them
            // to the group object

         Element rdata = (Element) e.selectSingleNode("generic-group/relationships");
         if(rdata != null)
         {
             r = parseRelationships(rdata);
         }

         return new GroupData(id, src, shortName, title, description, r, data);
     }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>DivisionData</code> object.
    *
    * @param  e  Dom4j element containing division data in the
    *            UNICON Canonical Format
    *
    * @see DivisionData
    * @throws AcademusException
    */

    public static DivisionData parseDivision(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (e.selectSingleNode("division") == null)
        {
            String msg = "Argument 'e [Element]' must be of type division.";
            throw new IllegalArgumentException(msg);
        }

        HashMap data = new HashMap();
        RelationshipsData r = new RelationshipsData();

        String id = e.valueOf("source-info/id");
        String src = e.valueOf("source-info/source");
        String shortName = e.valueOf("division/shortname");
        String title = e.valueOf("division/title");
        String description = e.valueOf("division/description");

           // Get any relationships for the topic and add them
           // to the group object

        Element rdata = (Element) e.selectSingleNode("division/relationships");
        if(rdata != null)
        {
            r = parseRelationships(rdata);
        }

        return new DivisionData(id, src, shortName, title, description, r, data);
    }


   /**
    * This method parses a Dom4j Element and returns a
    * <code>MemberData</code> object.
    *
    * @param  e  Dom4j element containing member data in the
    *            UNICON Canonical Format
    *
    * @see MemberData
    * @throws AcademusException
    */

    public static MemberData parseMember(Element e)
        throws AcademusException
    {
        // Assertions.
        if (e == null)
        {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        else if (!"member".equalsIgnoreCase(e.getName()) )
        {
            String msg = "Argument 'e [Element]' must be of type member.";
            throw new IllegalArgumentException(msg);
        }

        MemberData m = new MemberData(
                 e.attributeValue("source-ref"),
                 e.attributeValue("groupid-ref"),
                 e.attributeValue("personid-ref"),
                 e.valueOf("status"),
                 e.valueOf("group-role")
               );

        return m;
    }

}
