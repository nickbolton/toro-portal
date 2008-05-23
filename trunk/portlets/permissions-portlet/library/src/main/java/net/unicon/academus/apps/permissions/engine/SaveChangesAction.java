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

import java.util.LinkedList;
import java.util.List;

import net.unicon.academus.apps.permissions.PermissionsApplicationContext;
import net.unicon.academus.apps.permissions.PermissionsUserContext;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.permissions.Dummy;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.fac.SimpleActionResponse;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class SaveChangesAction extends PermissionsAbstractAction {

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

        return new SaveChangesAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (PermissionsApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("permissions_welcome"));
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
        if (decisions.length < 1) {
            String msg = "Argument 'decisions' cannot be empty.";
            throw new IllegalArgumentException(msg);
        }
        
       PermissionsUserContext puc = (PermissionsUserContext)ctx;
       
       // update the current entity selection permissions
       updatePermissions(decisions[0], puc);
       
       // get all the sharees from the database
       AccessBroker broker = puc.getPortletAccessSelection()
								.getAccessBroker();
       IAccessEntry[] entries = broker.getEntries();
       Object dummyTarget = new Dummy();

       List entities = new LinkedList();			//list of identity ids that exist in the current selections  
       
       // update the remaining entity permissions
       ICivisEntity[] cEntities = puc.getCivisEntities();
       AccessRule[][] accessRules = new AccessRule[cEntities.length][];
       Identity[] principals = new Identity[cEntities.length];
       
       for(int i = 0; i < cEntities.length; i++){
           accessRules[i] = puc.getCivisEntryAccessRules(cEntities[i].getUrl());
           if(cEntities[i] instanceof IPerson){
               principals[i] = new Identity(cEntities[i].getName(), IdentityType.USER);               
           }else{
        	   principals[i] = new Identity(((IGroup)cEntities[i]).getPathAndId(), IdentityType.GROUP);
           }
           entities.add(principals[i].getId());
       }
       
       // check if the current entity selection contains the entries from the broker
       // if current selection does not contain broker entry, remove from broker
       // these were entries that were removed using the remove button       
       for(int i = 0; i < entries.length; i++){           
	        if(!entities.contains(entries[i].getIdentity().getId())){
	       	    broker.removeAccess(entries[i]);       		
	       	}
      }

       // update access rules for the entities in current selection
       for (int i = 0; i < principals.length; i++) {
       	if(accessRules[i].length > 0){
       	    broker.setAccess(
                           principals[i]
                           , accessRules[i]                            
                           , dummyTarget);
       	}else{
       		// call remove access
       		IAccessEntry entry = broker.getEntry(principals[i], dummyTarget);
       		// remove access if the entry exists
       		if(entry != null){
       			broker.removeAccess(entry);
       		}
                               
       	}
       }

       return new SimpleActionResponse(screen, new InitialQuery(puc));

    }

    /*
     * Package API.
     */

    private SaveChangesAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.app = null;
    }

}
