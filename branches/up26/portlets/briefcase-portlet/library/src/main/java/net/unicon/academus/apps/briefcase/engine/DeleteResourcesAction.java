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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.shared.SharedResourceFactory.SharedResource;
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

public final class DeleteResourcesAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.
    private BriefcaseApplicationContext app;
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

        return new DeleteResourcesAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
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

        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) user.getHistory().getLocation().getObject();

        // Make sure the user clicked 'yes' to confirm.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("deleteConfirmation")));
        ISelection sel = d.getSelections()[0];

        boolean deleted = false;
        String msg = "";
        String solution = null;
        String name = f.getName();

        String confirmation = sel.getOption().getHandle().getValue();
        boolean confirmed = confirmation.equalsIgnoreCase("yes");
        
        if (confirmed) {
            // user confirmed the delete...

            // Make sure the user has delete permission on the parent folder.
            List access = Arrays.asList(user.getAccessRules(f.getOwner()));
            if (!access.contains(new AccessRule(BriefcaseAccessType.DELETE, true))) {
                msg = "User does not have delete access to the context folder";
                deleted = false;
            } else {
                IResource r = null;
                try{
                Iterator it = Arrays.asList(user.getResourceSelection()).iterator();
                while (it.hasNext()) {
                    r = (IResource) it.next();
                    if(r instanceof IFolder){
	                    if (user.getDriveSelection().isSharing()) {
	                        // We may need to clean up sharing associations.
	                        deleteSharing((IFolder)r
	                                , app.getDrive(user.getDriveSelection().getShareTarget())
	                                , user.getUsername());
	                    }else{
	                        deleteSharedSharing((IFolder)r
	                                , user.getDriveSelection());
	                    }
                    }
                    r.getOwner().delete(r);
                }
                deleted = true;
                } catch (OperationTimeoutException ote) {
                    msg = "Operation timed out.";
                    name = r.getName();
                    deleted = false;
                }catch(DemetriusException e){
                    deleted = false;
                    name = r.getName();
                    msg = "The selected resource may have been renamed or deleted. ";
                    e.printStackTrace();
                }
            }
        }
        user.clearResourceSelection();

        if (!confirmed || deleted) {
            return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user));
        } else {
            ErrorMessage[] errors = new ErrorMessage[1];
            String problem = "Unable to delete folder '"
                + name
                + "'.  "
                + msg;
            errors[0] = new ErrorMessage("other", problem, solution);
            return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user, errors));
        }

    }

    /*
     * Package API.
     */

    private DeleteResourcesAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }  
    
    private void deleteSharing(IFolder folder
            , Drive shared, String username) throws DemetriusException{
    	ResourceType[] types = new ResourceType[] { ResourceType.FOLDER };
    	if(folder.getContents(types).length > 0){
    		IResource[] subFolders = folder.getContents(types);
    		for(int i = 0; i < subFolders.length; i++){
    			deleteSharing((IFolder)subFolders[i], shared, username);
    		}       
    	}
		if (folder instanceof IFolder) {
            // We may need to clean up sharing associations.
            IResourceFactory movedResource = null;
            
            IAccessEntry[] entries = app.getSharedEntries(folder, username, shared);                        
            if (entries.length > 0) {
            	// remove old sharing 
                shared.getBroker().removeAccess(entries);                                        
            }
		} 		

    }
    
    private void deleteSharedSharing(IFolder target, Drive shared) throws DemetriusException{
        if(!(target instanceof SharedResource)){
            return;
        }
        
        String rootName = target.getOwner().getRoot().getName();
        String username = rootName.substring(rootName.lastIndexOf("(") + 1, rootName.lastIndexOf(")"));
        
        IResource iTarget = ((SharedResource)target).getResource();
        
        if(iTarget instanceof IFolder){
            deleteSharing((IFolder)iTarget, shared, username);
        }
    }


}