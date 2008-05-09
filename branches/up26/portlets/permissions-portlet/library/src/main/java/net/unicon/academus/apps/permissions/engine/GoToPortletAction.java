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

package net.unicon.academus.apps.permissions.engine;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.permissions.CivisAccessEntry;
import net.unicon.academus.apps.permissions.PermissionsApplicationContext;
import net.unicon.academus.apps.permissions.PermissionsUserContext;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.permissions.Dummy;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

public final class GoToPortletAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private PermissionsApplicationContext app;
    private IScreen screen;

    /*
     * Public API.
     */

    public static IAction parse(Element e, IWarlockFactory owner)
                                    throws WarlockException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("action")) {
            String msg = "Argument 'e [Element]' must be an <action> element.";
            throw new IllegalArgumentException(msg);
        }
        if (owner == null) {
            String msg = "Argument 'owner' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'handle'.";
            throw new XmlFormatException(msg);
        }
        Handle handle = Handle.create(h.getValue());

        // Choices.
        String[] choices = new String[0];
        Attribute p = e.attribute("inpt");
        if (p != null) {
            choices = p.getValue().split(",");
        }

        return new GoToPortletAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (PermissionsApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("permissions_list"));

    }

    public IActionResponse invoke(IDecisionCollection[] decisions,
                    IUserContext ctx) throws WarlockException {
        // Assertions.
        if (decisions == null) {
            String msg = "Argument 'decisions' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        PermissionsUserContext puc = (PermissionsUserContext)ctx;

        List errors = new ArrayList();

        // Find the target.
        if (decisions.length > 0){
            String id = decisions[0].getDecision("goLocation").getSelections()[0].getOption().getHandle().getValue();
            puc.setPortletSelection(puc.getAppContext().getPortlet(id));
            // TODO should redirect to a screen where it can be set
            puc.setPortletAccessSelection(puc.getPortletSelection().listAccessHelpers()[0]);

            // get the entries from the broker and set up the information.
            AccessBroker broker = puc.getPortletAccessSelection().getAccessBroker();
            Object dummyTarget = new Dummy();
            IAccessEntry[] entries = broker.getEntries();
            puc.clearCivicAccessEntries();
            String entityId;
            String groupPathStr;
            String groupId = "noKey";
            ICivisFactory[] factories = puc.getAppContext().getCivisFactories();
            ICivisEntity c = null;
            boolean updatePath = false;
            for(int i = 0; i < entries.length; i++){
                entityId = entries[i].getIdentity().getId();
                if (entries[i].getIdentity().getType().equals(IdentityType.GROUP)){
                    for(int index = 0; index < factories.length && c == null; index++){
		          	// Test the entity path for "[]" containing the groupId
		              	if (entityId.indexOf("[") != -1) {
		              		try{
		                  		groupPathStr = entityId.substring(0,entityId.indexOf("["));
		                  		groupId = entityId.substring(entityId.indexOf("[")+1, entityId.indexOf("]"));
		              		}catch (Exception e) {
		              			// Use entire entityId as groupPathStr if substring or parsing fails
		              			groupPathStr = entityId;
		              		}
		              	}else {
		              		groupPathStr = entityId;
		              		updatePath = true;
		              	}
		                  try{
		                  	// Attempt to get group by path
		                      c = factories[index].getGroupByPath(groupPathStr);
		                  }catch (Exception e){
		                	  log.debug("Expected group not found -- was it removed?", e);
		                      // don't do anything
		                  }
		                  if (c == null && !groupId.equals("noKey")) {
		                  	try{
		                  		// If getGroupByPath failed, try to get the group by Id
		                  		c = factories[index].getGroupById(groupId);
		                  		if (!groupPathStr.equals(((IGroup)c).getPath())) {
		                  			updatePath = true;
		                  		}
		                  	}catch (Exception e) {
			                	log.debug("Expected group not found -- was it removed?", e);
		                  		// again, don't do anything
		                  	}
		                  }
		                  groupId = "noKey";
                    }
                }else{
                    for(int index = 0; index < factories.length && c == null; index++){
                        try{
                            c = factories[index].getPerson(entityId);
                        }catch (Exception e){
//                       	 do nothing...
//                          puc.getPortletAccessSelection().getAccessBroker().removeAccess(entries[i]);
//                          String msg = "Unable to locate the specified user:  " + entityId
//                                  + ".  The corresponding permissions entry was removed.";
//                          errors.add(new ErrorMessage("other", msg, null));
                        }
                    }
                }

                if(c == null) {
                    //throw new RuntimeException("There was an error in finding the Civis Entity in the " +
                    //        "given Civis Factories. ");
                               	
                	// Remove access for invalid access entry
                    broker.removeAccess(entries[i]);
                    String msg = "Unable to locate the specified entry:  " + entityId
                    + ".  The corresponding permissions entry was removed.";
                    errors.add(new ErrorMessage("other", msg, null));
                    updatePath = false;
                } else {
                    puc.addCivisAccessEntry(new CivisAccessEntry(c.getUrl()
                            , entries[i].getAccessRules()));
                    
	                if (updatePath) {
	                	// Update access entry by removing old entry and inserting a new Identity with path and Id
		                broker.removeAccess(entries[i]);
	                	Identity principal = new Identity(((IGroup)c).getPathAndId(), IdentityType.GROUP);
	            		broker.setAccess(principal, entries[i].getAccessRules(), dummyTarget);
	            		updatePath = false;
	                }
                }

                c = null;
            }
        }

        return new SimpleActionResponse(screen, new PortletPermissionsListQuery(puc,
                        (ErrorMessage[]) errors.toArray(new ErrorMessage[0])));
    }

    /*
     * Package API.
     */

    private GoToPortletAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.app = null;
    }

}
