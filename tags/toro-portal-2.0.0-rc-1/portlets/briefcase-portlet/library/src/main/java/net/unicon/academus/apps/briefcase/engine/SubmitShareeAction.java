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

package net.unicon.academus.apps.briefcase.engine;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.fac.shared.SharedResourceFactory;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IApplicationContext;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class SubmitShareeAction extends BriefcaseAbstractAction {

    // Instance Members.   
    private IScreen screen;
    private IApplicationContext bac = null;
    private String toScreen;

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
            String msg = "Argument 'owner+' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // ToScreen.
        Attribute s = e.attribute("to-screen");
        if (s == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'to-screen'.";
            throw new XmlFormatException(msg);
        }
        String toScreen = s.getValue();

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
            StringTokenizer tokens = new StringTokenizer(p.getValue(), ",");
            choices = new String[tokens.countTokens()];
            for (int i=0; tokens.hasMoreTokens(); i++) {
                choices[i] = tokens.nextToken();
            }
        }

        return new SubmitShareeAction(owner, handle, choices, toScreen);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
       
         screen = m.getScreen(Handle.create(toScreen));
         bac = m.getContext();

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
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking Briefcase Action: " + getClass().getName());
        }
        
        BriefcaseUserContext buc = (BriefcaseUserContext) ctx;
        
        IFolder f = (IFolder) buc.getResourceSelection()[0];

        // add the sharees to the targetShareeBasket
        updatePermissions(decisions[0], buc);
        
        ICivisEntity[] cEntities = buc.getTargetSelection().getSharees();
        AccessRule[][] accessRules = new AccessRule[cEntities.length][];
        Identity[] principals = new Identity[cEntities.length];
        Set entities = new HashSet();			//set of identity ids that exist in the current selections  
        
        for(int i = 0; i < cEntities.length; i++){
            accessRules[i] = buc.getTargetSelection().getShareeAccess(cEntities[i].getUrl());
            if(cEntities[i] instanceof IPerson){
                principals[i] = new Identity(cEntities[i].getName(), IdentityType.USER);               
            }else{
            	// Create identity that contains group path and groupId
                principals[i] = new Identity(((IGroup)cEntities[i]).getPathAndId(), IdentityType.GROUP);
            }
            entities.add(principals[i]);
        }
        
        IResourceFactory shared = new SharedResourceFactory(f, buc.getUsername());
        AccessBroker broker = ((BriefcaseApplicationContext) bac).getDrive(
                buc.getDriveSelection().getShareTarget()).getBroker();
        
        
        // remove all the users that were removed from the basket 
        //using the remove button on the edit permission screen.
        // get all the sharees from the database
        IAccessEntry[] entries = broker.getEntries(
                        shared,
                        new AccessRule[0]);

        for(int i = 0; i < entries.length; i++){           
	        if(!entities.contains(entries[i].getIdentity())){
	       	    broker.removeAccess(entries[i]);       		
	       	}
        }
        
        // update the remaining sharee permissions
        for (int i = 0; i < principals.length; i++) {
        	if(accessRules[i].length > 0){
        	    AccessRule[] rules = new AccessRule[accessRules[i].length + 1];
        	    System.arraycopy(accessRules[i], 0, rules, 1, accessRules[i].length);
        	    rules[0] = new AccessRule(BriefcaseAccessType.VIEW, true);
        		broker
                    .setAccess(
                            principals[i],
                            rules,
                            shared);
        	}else{
        		// add sharee with default view access
        	    broker
                .setAccess(
                        principals[i],
                        new AccessRule[] {new AccessRule(BriefcaseAccessType.VIEW, true)},
                        shared);                                
        	}
        }
        
        // clean up the previous selections
        buc.getTargetSelection().clearShareeSelection();
        buc.setEntitySelection(new ICivisEntity[0]);
        buc.setPrevEntitySelection(new ICivisEntity[0]);
        
        // TT 04984 -- BriefcasePortlet: Shared folder is lost after a 
        // new folder is created. Clear the resource selection so that
        // the resource is not acted upon and updated/overwritten.
        buc.clearResourceSelection();
        return new SimpleActionResponse(screen
                , new FolderQuery(buc.getHistory().getLocation()
                        , (BriefcaseApplicationContext) bac
                        , buc));

    }

    /*
     * Package API.
     */

    private SubmitShareeAction(IWarlockFactory owner, Handle handle,
                                            String[] choices, String toScreen) {

        super(owner, handle, choices);

        // Instance Members.        
        this.screen = null;
        this.toScreen = toScreen;

    }

}
