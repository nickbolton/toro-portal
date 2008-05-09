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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.academus.apps.briefcase.Location;
import net.unicon.academus.apps.ErrorMessage;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.fac.AbstractWarlockFactory;
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

public final class SelectDestinationAction extends AbstractWarlockFactory
                                                    .AbstractAction {

    // Instance Members.
    private BriefcaseApplicationContext app;
    private IScreen screen;
    private IScreen errScreen;
    private DestinationMode mode;

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

        // Mode.
        Attribute m = e.attribute("mode");
        if (m == null) {
            String msg = "Element <action> is missing required attribute "
                                                            + "'mode'.";
            throw new XmlFormatException(msg);
        }
        DestinationMode mode = DestinationMode.getInstance(m.getValue());
        
        return new SelectDestinationAction(owner, handle, choices, mode);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("movecopy"));
        errScreen = m.getScreen(Handle.create("folderview"));

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

        List<ErrorMessage> err = new ArrayList<ErrorMessage>();
        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) user.getHistory().getLocation().getObject();
        Location parentLocation = null;
        
        // Open the curent folder for selecting destinations 
        // (May be closed in NavTree)
        user.getHistory().openFolder();
        
        // Add mode to user context
        user.getSession().setAttribute("destinationMode", this.mode);
        
        String problem = "";
        String msg = "";
        String solution = null;
        boolean error = false; 
        
        // Find the resources.
        IDecisionCollection d_mff = null;
        IChoiceCollection c_mff = null;
        net.unicon.penelope.Handle h = net.unicon.penelope.Handle.create("mainFolderForm");
        for (int i=0; d_mff == null && i < decisions.length; i++) {
            if (decisions[i].getChoiceCollection().getHandle().equals(h)) {
                d_mff = decisions[i];
                c_mff = d_mff.getChoiceCollection();
            }
        }
        IDecision si = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("selectedItems")));
        IResource[] targets = null;
        ISelection[] sel = si.getSelections();

        EncryptionService encryptionService = app.getEncryptionService();

        try{
	        if (sel.length == 0) {
	            // choose the parent folder...
	            targets = new IResource[] { (IResource) user.getHistory().getLocation().getObject() };
	        } else {
	            // the user selected targets...
	            targets = new IResource[sel.length];

	            for (int i=0; i < sel.length; i++) {
	
					String url = sel[i].getOption().getHandle().getValue();
					// Decrypt url before using it
	                url = encryptionService.decrypt(url);
					targets[i] = AbstractResourceFactory.resourceFromUrl(url);
	            }
	        }
            
            parentLocation = new Location(user.getDriveSelection(), targets[0].getParent());
            // Save this to enable returning after intermediate navigation
            user.getSession().setAttribute("fromLocation", parentLocation);
	        
	        // Make sure that a root folder was not selected.
	        Iterator it = Arrays.asList(targets).iterator();
	        while (it.hasNext()) {
	            IResource r = (IResource) it.next();
	            if (r.getOwner().getRoot().equals(r)) {
	                problem = "Unable to choose folder '"
	                                    + r.getName()
	                                    + "'.  Root folders may not be "
	                                    + "moved, copied, deleted, or shared.";
	                err.add(new ErrorMessage("other", problem, null));
	                error = true;
	            }
	        }
	        
	        // Check permissions depending on mode
	        List accessTypes = Arrays.asList(user.getAccessRules(f.getOwner()));
	        if (mode.equals(DestinationMode.MOVE)) {
	            for (int i = 0; i < targets.length; i++) {
	                if (!accessTypes.contains(new AccessRule(BriefcaseAccessType.DELETE, true))) {
	                    msg = "User does not have 'delete' access to " +
	                            "the context folder.";
	                    problem = "Unable to move '" + targets[i].getName() + "'. "
	                            + msg;
	                    err.add(new ErrorMessage("other", problem, solution));
	                    error = true;
	                }
	            }
	        }
	        
	        //	Set the selection.
	        user.setResourceSelection(targets);
        }catch(Throwable de){
            problem = "One or more of the selected resources are not available.";
            solution = "Please select the resources again.";
            err.add(new ErrorMessage("other", problem, null));
            error = true;
            de.printStackTrace();
        }

        List<IResourceFactory> facs = new ArrayList<IResourceFactory>();
        if(!error){
	        // Obtain the user's entry points.
	        List<IAccessEntry> entries = new ArrayList<IAccessEntry>();
	        List<Drive> drives = user.getDrives();
            for (Drive drive : drives) {
	        	entries.addAll(Arrays.asList(drive.getBroker().getEntries(user.getPrincipal())));
	        }
	        
	        for (int i=0; i < entries.size(); i++) {
	            List access = Arrays.asList(((IAccessEntry)entries.get(i)).getAccessRules());
	            if (access.contains(new AccessRule(BriefcaseAccessType.ADD, true))) {
	                facs.add((IResourceFactory)
                            ((IAccessEntry)entries.get(i)).getTarget());
	            }
	        }
	        
	        if(facs.size() == 0){
	        	  msg = "User does not have any entry points to move or copy to.";
	        	  problem = "Unable to move any targets. " + msg;
	        	  err.add(new ErrorMessage("other", problem, solution));
	        }
        }

        // Choose success or failure.
        if (err.size() == 0) {
            // check if the targets have sharing
            boolean isSharing;
            try {
                isSharing = isSharing(targets, user);
               
	            // Set the response
	            if(isSharing && mode.equals(DestinationMode.MOVE)){
	                problem = "One or more folders / subfolders is shared. Sharing information will be lost on moving.";
	          	  	solution = "Cancel the move and COPY the folder / subfolder instead.";
	          	  	return new SimpleActionResponse(screen, 
                            new SelectDestinationQuery(app, user, 
                                    parentLocation, new ErrorMessage[] { 
                                            new ErrorMessage("other", problem, 
                                                    solution) }));
	            }else{
	          	  	return new SimpleActionResponse(screen, 
                            new SelectDestinationQuery(app, user, 
                                    parentLocation));
	            }
            } catch (DemetriusException e) {
                problem = "Error in getting the target sharing details.";
          	  	err.add(new ErrorMessage("other", problem, ""));
          	  	e.printStackTrace();
          	  	return new SimpleActionResponse(errScreen, new FolderQuery(
                        user.getHistory().getLocation(), app, user, 
                        (ErrorMessage[]) err.toArray(new ErrorMessage[0])));
            }
        } else {
            
            /* 
             * TT 05182 -- Briefcase: Shared Folder failure to render after 
             * clicking on move, copy ,delete [and then creating a new folder].
             * Clear the resource selection so that the resource is not acted 
             * upon and updated/overwritten.
             */
            user.clearResourceSelection();
            
            // Display the error.
            return new SimpleActionResponse(errScreen, 
                    new FolderQuery(user.getHistory().getLocation(), app,
                            user, (ErrorMessage[]) err.toArray(
                                    new ErrorMessage[0])));
        }

    }

    /*
     * Package API.
     */

    private SelectDestinationAction(IWarlockFactory owner, Handle handle,
                            String[] choices, DestinationMode mode) {

        super(owner, handle, choices);

        // Assertions.
        if (mode == null) {
            String msg = "Argument 'mode' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = null;
        this.screen = null;
        this.errScreen = null;
        this.mode = mode;
        

    }
    
    private boolean isSharing(IResource[] targets, BriefcaseUserContext buc) 
    		throws DemetriusException{
        for(int i = 0; i < targets.length; i++){
            if(targets[i] instanceof IFolder){
                if(checkIfSharing((IFolder) targets[i], buc, 
                        targets[i].getName())){
                    return true;
                }                
            }
        }
        return false;
    }
    
    private boolean checkIfSharing(IFolder folder, BriefcaseUserContext buc, 
            String folderPath) throws DemetriusException{
    	ResourceType[] types = new ResourceType[] { ResourceType.FOLDER };
        boolean isShared=false;
    	
    	if(folder.getContents(types).length > 0){
    		IResource[] subFolders = folder.getContents(types);
    		for(int i = 0; i < subFolders.length; i++){
                if(checkIfSharing((IFolder)subFolders[i], buc, folderPath 
                        + "/" + subFolders[i].getName())) {
                    isShared = true;
                }
    		}       
    	}
		if (buc.getDriveSelection().isSharing()) {
            // We may need to clean up sharing associations.
            Drive shared = app.getDrive(
                    buc.getDriveSelection().getShareTarget());
            IResourceFactory movedResource = null;
            IAccessEntry[] entries = app.getSharedEntries(
                    (IFolder)folder, buc.getUsername(), shared);                        
            if (entries.length > 0) {
            	isShared = true;                    
            }
		}
		return isShared;

    }

}
