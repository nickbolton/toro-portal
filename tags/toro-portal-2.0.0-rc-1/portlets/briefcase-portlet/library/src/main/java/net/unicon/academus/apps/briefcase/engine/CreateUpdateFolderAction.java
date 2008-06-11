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

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.shared.SharedResourceFactory;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
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

public final class CreateUpdateFolderAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private BriefcaseApplicationContext app;
    private IScreen screen;
    private IScreen addScreen;

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
            StringTokenizer tokens = new StringTokenizer(p.getValue(), ",");
            choices = new String[tokens.countTokens()];
            for (int i=0; tokens.hasMoreTokens(); i++) {
                choices[i] = tokens.nextToken();
            }
        }

        return new CreateUpdateFolderAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("folderview"));
        addScreen =  m.getScreen(Handle.create("addedit_folder"));

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

        // User Input.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("cName")));
        String name = (String) d.getFirstSelectionValue();

        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) user.getHistory().getLocation().getObject();
        boolean created = false;
        String msg = "";
        String solution = null;
        String problem = "";

        //check permissions.
        List access = Arrays.asList(user.getAccessRules(f.getOwner()));
        
        if ((!user.hasResourceSelection() && !access.contains(new AccessRule(BriefcaseAccessType.ADD, true)))
                || (user.hasResourceSelection() && !access.contains(new AccessRule(BriefcaseAccessType.EDIT, true)))
                ) {
            msg = "User does not have 'add' or 'edit' access to the context folder";
            problem = "Unable to add/edit folder. " + msg;
            created = false;
        } else if (name == null) {
            msg = "No folder name was specified.";
            problem = "Unable to add/edit folder. " + msg;
            solution = "Specify a folder name when creating or editing.";
            created = false;
        } else {

            // Make the change.
            try {

                // Metadata.
                IChoiceCollection metaChoices = f.getOwner()
                        .getResourceMetadata(ResourceType.FOLDER);
                IEntityStore store = metaChoices.getOwner();
                IChoice nameChoice = metaChoices
                        .getChoice(net.unicon.penelope.Handle
                                .create("foldername"));
                IOption nameOption = nameChoice.getOptions()[0];
                ISelection nameSelection = store.createSelection(nameOption,
                        nameOption.getComplementType().parse(name));
                IDecision nameDecision = store.createDecision(null, nameChoice,
                        new ISelection[] { nameSelection });
                IDecisionCollection metaDecisions = store
                        .createDecisionCollection(metaChoices,
                                new IDecision[] { nameDecision });

                // Choose add or update.
                if (user.hasResourceSelection()) {

                    // Update the folder.
                    try {
	                    IFolder target = (IFolder) user.getResourceSelection()[0];
	                    boolean isShared = isShared(target, user);
	                    IAccessEntry[] entries = null;
	                    if(isShared){
	                        entries = app.getSharedEntries(target, user.getUsername()
	                                , app.getDrive(user.getDriveSelection().getShareTarget()));
	                    }
	                    f.getOwner().updateResource(metaDecisions, target);
	                   
	                    // update the shared entries
	                    if(entries != null){
	                        AccessBroker broker = app.getDrive(
	                                user.getDriveSelection().getShareTarget()).getBroker();
	                        broker.removeAccess(entries);
	                        
	                        IResourceFactory shared 
	                        			= new SharedResourceFactory(target, user.getUsername());
	                        for(int i = 0; i < entries.length; i++){
	                             broker.setAccess(entries[i].getIdentity()
	                                    , entries[i].getAccessRules(), shared);
	                        }
	                    }
	                    created = true;
                    } catch (OperationTimeoutException ote) {
                        problem = "Operation timed out.";
                        solution = "";
                        created = false;
                    }catch (IllegalArgumentException e) {
                        // send error with message...
                        msg = e.getMessage();
                        problem = "Unable to edit folder '"
                            + (name != null ? name : "")
                            + "'.  "
                            + msg;
                        created = false;
                        solution = "";
                    }catch (DemetriusException e) {
                        // send error with message...
                        msg = e.getMessage();
                        problem = "Unable to edit folder '"
                            + (name != null ? name : "")
                            + "'.  "
                            + msg;
                        created = false;
                        solution = "Please use a different name when creating" +
                                " a folder or rename the existing resource.";
                    }

                    
                } else {

                    // Add the folder.
                    try {
                        f.getOwner().createFolder(metaDecisions, f);
                        created = true;
                    } catch (OperationTimeoutException ote) {
                        problem = "Operation timed out.";
                        solution = "";
                        created = false;
                    } catch (IllegalArgumentException iae) {
                        // send error with message...
                        msg = iae.getMessage();
                        problem = "Unable to create folder '"
                            + (name != null ? name : "")
                            + "'.  "
                            + msg;
                        created = false;
                        solution = "Please use a different name when creating" +
                                " a folder or rename the existing resource.";
                    }

                }

            } catch (Throwable t) {
                msg = "CreateUpdateFolderAction failed for the specified folder.";
                throw new WarlockException(msg, t);
            }
        }

        user.clearResourceSelection();

        if (created) {
            return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user));
        } else {
            ErrorMessage[] errors = new ErrorMessage[1];
            errors[0] = new ErrorMessage("other", problem, solution);
            return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user, errors));
        }

    }

    /*
     * Package API.
     */

    private CreateUpdateFolderAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.addScreen = null;

    }
    
    private boolean isShared(IFolder target, BriefcaseUserContext user){
    	boolean rslt = false;
    	
  		// find out if the drive accessbroker has any entries.
    	if(user.getHistory().getLocation().getDrive().isSharing()){
	   		IAccessEntry[] entries = 
	   			app.getSharedEntries(target, user.getUsername()
	   					, app.getDrive(user.getDriveSelection().getShareTarget()));
	   		if(entries.length > 0){
	   			rslt = true;
	   		}
   		}
    	
    	return rslt;
    }


}
