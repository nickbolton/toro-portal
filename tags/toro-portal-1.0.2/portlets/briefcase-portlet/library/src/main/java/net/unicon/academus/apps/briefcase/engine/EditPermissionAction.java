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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.civisselector.GoUpGroupAction;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.demetrius.fac.shared.SharedResourceFactory;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
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

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class EditPermissionAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private IScreen screen;
    private IScreen errScreen;
    private BriefcaseApplicationContext bac = null;

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

        return new EditPermissionAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        screen = m.getScreen(Handle.create("edit_permissions"));
        errScreen = m.getScreen(Handle.create("folderview"));
        bac = (BriefcaseApplicationContext)m.getContext();
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
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking Briefcase Action: " + getClass().getName());
        }

        BriefcaseUserContext buc = (BriefcaseUserContext) ctx;

        // For error handling.
        List err = new ArrayList();
        String problem = "";
        String msg = "";
        String solution = null;
        boolean share = true;

        // Find the share target.
        IFolder f = null;
        IDecisionCollection d_mff = null;
        IChoiceCollection c_mff = null;
        ISelection[] sel = new ISelection[0];
                
        // get the folder that is coming from the editfolder screen
        net.unicon.penelope.Handle h = net.unicon.penelope.Handle.create("folderInfoForm");
        for (int i=0; d_mff == null && i < decisions.length; i++) {
            if (decisions[i].getChoiceCollection().getHandle().equals(h)) {
                d_mff = decisions[i];
                c_mff = d_mff.getChoiceCollection();                
            }
        }
        if(sel.length == 0 && d_mff != null){
        	IDecision si = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("cId")));
        	IResource[] targets = null;
        	sel = si.getSelections();
        }
        
        h = net.unicon.penelope.Handle.create("mainFolderForm");
        for (int i=0; d_mff == null && i < decisions.length; i++) {
            if (decisions[i].getChoiceCollection().getHandle().equals(h)) {
                d_mff = decisions[i];
                c_mff = d_mff.getChoiceCollection();                
            }
        }
        if(sel.length == 0 && d_mff != null){
        	IDecision si = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("selectedItems")));
        	IResource[] targets = null;
        	sel = si.getSelections();        	
        }       
            
        EncryptionService encryptionService = bac.getEncryptionService();

        try{
	        switch (sel.length) {
	            case 0:
	                // choose the parent folder...
	                f = (IFolder) buc.getHistory().getLocation().getObject();
	                break;
	            case 1:
	                String url = sel[0].getOption().getHandle().getValue();
	                // Decrypt url before using it
	                url = encryptionService.decrypt(url);
	                IResource r = AbstractResourceFactory.resourceFromUrl(url);
	                if (r.getType().equals(ResourceType.FOLDER)) {
	                    f = (IFolder) r;
	                } else {
	                    problem = "Unable to share the specified resource.  Only folders may be shared.";
	                    err.add(new ErrorMessage("other", problem, solution));
	                    share = false;
	                }
	                break;
	            default:
	                problem = "Unable to share the specified resources.";
	                err.add(new ErrorMessage("other", problem, "Select a single folder and click 'share' again."));
	                share = false;
	                break;
	        }

	        // Check to be sure we don't share a root folder.
	        if (share && f.getOwner().getRoot().equals(f)) {
	            problem = "Unable to share folder '" + f.getName()
	                    + "'.  Root folders may not be "
	                    + "moved, copied, deleted, or shared.";
	            err.add(new ErrorMessage("other", problem, solution));
	            share = false;
	        }
        }catch(Throwable t){
            problem = "Unable to share the selected folder. The folder may have been renamed or deleted.";
            err.add(new ErrorMessage("other", problem, solution));
            share = false;
            t.printStackTrace();
        }
        // Check to be sure we have the right permissions.
        if (share) {
            List types  = Arrays.asList(buc.getAccessRules(f.getOwner()));
            if (!types.contains(new AccessRule(BriefcaseAccessType.SHARE, true))) {
                problem = "Unable to share folder '" + f.getName()
                        + "'. You do not have permissions to share the folder.";
                err.add(new ErrorMessage("other", problem, solution));
                share = false;
            }
        }

        if (share) {

            // not a root -- proceed...
        	
        	// create a shared resource and look for it.        	
        	IResourceFactory shared = new SharedResourceFactory(f, buc.getUsername());

        	AccessBroker broker = ((BriefcaseApplicationContext) bac).getDrive(
                    buc.getDriveSelection().getShareTarget()).getBroker();
        	
            IAccessEntry[] entries = broker.getEntries(
                            shared,
                            new AccessRule[0]);

            List sharees = new ArrayList();
            List accessRules = new ArrayList();

            String entityId;
            String groupPathStr;
            String groupId = "noKey";
            ICivisFactory[] factories = buc.getAppContext().getCivisFactories();
            ICivisEntity c = null;
            boolean updatePath = false;
            for(int i = 0; i < entries.length; i++){
                entityId = entries[i].getIdentity().getId();
                if (entries[i].getIdentity().getType().equals(IdentityType.GROUP)){
                    for(int index = 0; index < factories.length && c == null; index++){
                    	// Test the entity path for "[]" containing the groupId
                    	if (entityId.contains("[")) {
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
                        		// again, don't do anything
                        	}
                        }
                        groupId ="noKey";
                    }
                }else{
                    for(int index = 0; index < factories.length && c == null; index++){
                        try{
                            c = factories[index].getPerson(entityId);
                        }catch (Exception e){
                            // don't do anything
                        }
                    }
                }
                
                if(c == null){
                    //throw new RuntimeException("There was an error in finding the Civis Entity in the " +
                    //        "given Civis Factories. ");
                	
                	// No longer throwing exception if null. Removing this access_entry to recover.
                	if (getLog().isInfoEnabled()) {
	                	getLog().info("[briefcase.engine][EditPermissionAction] There was an error in " +
	                			"finding the Civis Entity (" + entityId + ") in the given Civis Factories. ");
	                	String tUrl = "target unknown.";
						try {
							Class cl = entries[0].getTarget().getClass();
							Method mthd = cl.getDeclaredMethod("getUrl", null);
							tUrl = (String) mthd.invoke(entries[i].getTarget(),	null);
						} catch (Exception e) {
							// unable to get target url (for logging purposes only)
						}
						getLog().info("[briefcase.engine][EditPermissionAction] Removing access for " +
	                    		"identity: " + entityId + " with target: " + tUrl);
                	}
                	// Remove access for invalid access entry
                    broker.removeAccess(entries[i]);
                    updatePath = false;
                } else {

                	sharees.add(c);
                	accessRules.add(entries[i].getAccessRules());
					
	                if (updatePath) {
	                	if (getLog().isInfoEnabled()) {
		                	getLog().info("[briefcase.engine][EditPermissionAction] There was an error in " +
		                			"the Civis Entity path (" + entityId + ") in the given Civis Factory. " +
		                			"Updating to new path (" + ((IGroup)c).getPathAndId() + ").");
	                	}
	                	// Update access entry by removing old entry and inserting a new Identity with path and Id
		                broker.removeAccess(entries[i]);
	                	
	                	Identity principal = new Identity(((IGroup)c).getPathAndId(), IdentityType.GROUP);
	            		broker.setAccess(principal, entries[i].getAccessRules(), shared);
	            		updatePath = false;
	                }
	                
	                c = null;
				}
            }
            
            // Converting dynamic AccessRule List to a double array
            AccessRule[][] rules = new AccessRule[accessRules.size()][];
            for (int i = 0; i < accessRules.size(); i++) {
            	rules[i] = (AccessRule[]) accessRules.get(i);
            }
            
            buc.getTargetSelection().setShareeSelection( (ICivisEntity[])sharees.toArray(new ICivisEntity[sharees.size()])
                    , rules
                    , true);       
        }         
        
        if (err.size() == 0) {
        	buc.setResourceSelection(new IResource[] { f });
            return new SimpleActionResponse(screen, new EditPermissionQuery(buc));
        } else {
            return new SimpleActionResponse(errScreen, new FolderQuery(buc
                .getHistory().getLocation(), bac,
                buc, (ErrorMessage[])err.toArray(new ErrorMessage[err.size()])));
        }

    }

    /*
     * Package API.
     */

    private EditPermissionAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.errScreen = null;

    }

}
