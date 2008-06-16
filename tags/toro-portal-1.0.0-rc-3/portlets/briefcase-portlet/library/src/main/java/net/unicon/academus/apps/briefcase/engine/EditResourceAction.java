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

import java.util.StringTokenizer;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
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

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class EditResourceAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private BriefcaseApplicationContext app;
    private IScreen fileScreen;
    private IScreen folderScreen;
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
            StringTokenizer tokens = new StringTokenizer(p.getValue(), ",");
            choices = new String[tokens.countTokens()];
            for (int i=0; tokens.hasMoreTokens(); i++) {
                choices[i] = tokens.nextToken();
            }
        }

        return new EditResourceAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
        fileScreen = m.getScreen(Handle.create("edit_file_details"));
        folderScreen = m.getScreen(Handle.create("addedit_folder"));
        screen = m.getScreen(Handle.create("folderview"));
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

        // Find & select the target.
        IDecisionCollection d_mff = decisions[0];
        IChoiceCollection c_mff = d_mff.getChoiceCollection();
        IDecision cl = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("editResource")));
        String url = cl.getSelections()[0].getOption().getHandle().getValue();

        EncryptionService encryptionService = app.getEncryptionService();
        url = encryptionService.decrypt(url);
        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        
        ErrorMessage[] errors = new ErrorMessage[1];
        String problem = "";
        String msg = "";
        String solution = null;
        
        boolean hasPermission = true;
    	
        IResource target = null;
        try {
            target = AbstractResourceFactory.resourceFromUrl(url);
            user.setResourceSelection(new IResource[] { target });
        
	        IFolder f = (IFolder) user.getHistory().getLocation().getObject();
	
	        // Check to see if the user has edit permissions, otherwise, they cannot edit.
	        /*List access = Arrays.asList(app.getAccessRules(
	                user.getUsername(), f.getOwner()));
	        if (!access.contains(new AccessRule(BriefcaseAccessType.EDIT, true))) {
	            msg = "You do not have edit permissions.";
	            problem = "Unable to edit '" +
	                        target.getName() +
	                        "'.  " + msg;
	            errors[0] = new ErrorMessage("other", problem, solution);
	            hasPermission = false;
	        }*/
        } catch (DemetriusException e) {
            msg = "The selected resource is currently not accessible.";
            problem = "Unable to edit the resource. " + msg;
            errors[0] = new ErrorMessage("other", problem, solution);
            hasPermission = false;
            e.printStackTrace();
        }
        
        IActionResponse rslt = null;
        if (hasPermission) {

            // Choose whither to go.
            switch (target.getType().toInt()) {
            case ResourceType.FOLDER_SWITCHABLE:
            	boolean shared = isShared((IFolder)target, user);
                rslt = new SimpleActionResponse(folderScreen,
                        new EditFolderQuery((IFolder) target, shared
                                , user.getHistory().getLocation().getDrive().isSharing(), app, user)
                                );
                break;
            case ResourceType.FILE_SWITCHABLE:
                rslt = new SimpleActionResponse(fileScreen, new EditFileQuery(
                        (IFile) target, app, user));
                break;
            }
        } else {
            rslt = new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user, errors));
        }

        return rslt;

    }

    /*
     * Package API.
     */

    private EditResourceAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.app = null;
        this.fileScreen = null;
        this.folderScreen = null;
        this.screen = null;

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