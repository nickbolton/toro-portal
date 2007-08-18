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

import ca.ubc.itservices.channels.webmail.IAddressBook;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeProperties;

import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.EntityNameFinderService;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntity;


import org.xml.sax.ContentHandler;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Collection;

import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.Servant;


public class AddressBook implements IAddressBook {

  Servant servant;
  private String option;
  public AddressBook() {
    servant =  new Servant ();
  }

  public void setupScreen (ChannelRuntimeData rd){
    String name = null;
    // get the RAD class that implements "select" from rad.properties
    String operation = rd.getParameter("callAB");
    if (operation != null && !operation.equals("addsender"))
      name = "net.unicon.portal.channels.addressbook.Select";
    else
      name = "net.unicon.portal.channels.addressbook.Contact2";

    servant.start(name);

    // "do" and "go" are RAD reserved parameter names; if you use them in
    // your own XSLs, you must remove them for the initial call to a RAD
    // servant
    rd.remove("do");
    rd.remove("go");
    if (operation != null && operation.equals("addsender"))
    {
      IdentityData id = new IdentityData();
      id.putName(rd.getParameter("name"));
      id.putEmail(rd.getParameter("email"));
      rd.put("identity-data",id);
    }
    else
    {
      rd.put("sources", "portal,campus,contact");
    }

  }

  public void setStaticData(ChannelStaticData sd) throws PortalException{
    servant.setStaticData(sd);
  }

  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException{
    if (option == null) {
      option = rd.getParameter("callAB");
    }

    servant.setRuntimeData(rd);
  }

  public Map getEmailAddresses () {

    PersonDirectory pd = PersonDirectory.instance();

    Map addressesFound = new HashMap ();
    List to = new ArrayList ();
    List cc = new ArrayList ();
    List bcc = new ArrayList ();

    IdentityData[] infoFound = (IdentityData[]) servant.getResults();
    /*for (int i=0; i < infoFound.length; i++) {

      String abName = infoFound[i].getName();
      String abEmail = infoFound[i].getEmail();
      if (option != null && option.equals("To:"))
        to.add (new String[] {abName, abEmail});
      else if (option != null && option.equals("Cc:"))
        cc.add (new String[] {abName, abEmail});
      else
        bcc.add (new String[] {abName, abEmail});

    }

    if (option != null && option.equals("To:"))
      addressesFound.put("To", to);
    else if (option != null && option.equals("Cc:"))
      addressesFound.put("Cc", cc);
    else
      addressesFound.put("Bcc", bcc);
    */
   try
    {
      for (int i=0; i < infoFound.length; i++)
      {
        String abType = infoFound[i].getType();

        if (abType.equals("E")) // Entity (person)
        {
          String abName = infoFound[i].getName();
          String abEmail = infoFound[i].getEmail();
          // don't insert duplicates or null
          /*if (!addressesFound.containsValue(abEmail) && abEmail != null)
          {
            addressesFound.put(abName, abEmail);
          }*/
          if (option != null && option.equals("To:"))
            to.add(new String[] {abName, abEmail});
          else if (option != null && option.equals("Cc:"))
            cc.add(new String[] {abName, abEmail});
          else
            bcc.add (new String[] {abName, abEmail});

        }
        else if (abType.equals("G"))  // Group
        {
          String groupName = infoFound[i].getName();
          String groupID = infoFound[i].getID();
          IGroupMember abMember = GroupService.findGroup(groupID);
          Iterator it = abMember.getAllEntities();

          while (it.hasNext())
          {
            IEntity ent = (IEntity)it.next();
            String userName = ent.getKey();

            // if running version 2.0.x, get the use the NameFinderService to find the real userName
            if(com.interactivebusiness.portal.VersionResolver.getPortalVersion().startsWith("2.0"))
            {
              // with this userID, find name
              userName = EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.security.IPerson")).getName(userName);

              if (userName != null && userName.trim().length() > 0)
              {
                // if full name returned, extract username
                if (userName.indexOf("(") >= 0)
                {
                  userName = userName.substring(userName.indexOf("(")+1, userName.indexOf(")"));
                }
              }
            }
            Hashtable person = pd.getUserDirectoryInformation(userName);
            String abName =  person.get("givenName") + " " + person.get("sn");
            String abEmail = (String)person.get("mail");

            // don't insert duplicates or null
            /*if (!addressesFound.containsValue(abEmail) && abEmail != null)
            {
              addressesFound.put(abName, abEmail);
            }*/
            if (option != null && option.equals("To:"))
              to.add(new String[] {abName, abEmail});
            else if (option != null && option.equals("Cc:"))
              cc.add(new String[] {abName, abEmail});
            else
              bcc.add (new String[] {abName, abEmail});
          }
        }
      }
    }
    catch (Exception e)
    {
      LogService.instance().log(LogService.ERROR,e);
//      e.printStackTrace();
    }

    if (option != null && option.equals("To:"))
       addressesFound.put("To", to);
     else if (option != null && option.equals("Cc:"))
       addressesFound.put("Cc", cc);
     else
       addressesFound.put("Bcc", bcc);

    return addressesFound;
  }

  public Map resolveNickNames (String[] nicknames) throws Exception {
    return null;
  }

  public void receiveEvent(PortalEvent ev){
    servant.receiveEvent(ev);
  }

  public ChannelRuntimeProperties getRuntimeProperties(){
    return servant.getRuntimeProperties();
  }

  public void renderXML(ContentHandler out) throws PortalException {
    servant.renderXML(out);
  }

  public boolean isFinished (){
    return servant.isFinished();
  }

  public Object[] getResults (){
    return servant.getResults();
  }

}
