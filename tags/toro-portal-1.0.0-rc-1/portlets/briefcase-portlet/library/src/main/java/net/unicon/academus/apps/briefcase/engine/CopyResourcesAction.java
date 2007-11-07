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

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Location;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.fac.AbstractResourceFactory;
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

public final class CopyResourcesAction extends AbstractWarlockFactory
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

        return new CopyResourcesAction(owner, handle, choices);

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

        BriefcaseUserContext buc = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) buc.getHistory().getLocation().getObject();

        // Error handling.
        List err = new ArrayList();
        String msg;
        String problem;
        String solution;

        // Find the destination folder.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("chooseDestination")));
        ISelection[] sel = d.getSelections();

        if(sel.length > 0){
	        EncryptionService encryptionService = app.getEncryptionService();
	        String decryptedUrl = sel[0].getOption().getHandle().getValue();
	        decryptedUrl = encryptionService.decrypt(decryptedUrl);
	
	        IFolder destination = null;
            try {
                destination = (IFolder) AbstractResourceFactory.resourceFromUrl(decryptedUrl);
                
                // Make sure the user has create permission on the destination folder.
    	        List access = Arrays.asList(buc.getAccessRules(destination.getOwner()));
    	        if (!access.contains(new AccessRule(BriefcaseAccessType.ADD, true))) {
    	            msg = "User does not have 'create' access to the destination folder";
    	            problem = "Unable to copy resoure to '" + destination.getName() +
    	                        "'. " + msg;
    	            solution = "Please choose a different destination folder.";
    	            err.add(new ErrorMessage("other", problem, solution));
    	        }
            } catch (DemetriusException de) {
                problem = "The selected destination folder is not accessible.";
	            solution = "Please choose a different destination folder.";
	            err.add(new ErrorMessage("other", problem, solution));
	            de.printStackTrace();
            }
            
	        
	        if (err.size() > 0) {
	            // Display the error.
	            return new SimpleActionResponse(screen, new FolderQuery(buc.getHistory().getLocation(), app,
	                buc, (ErrorMessage[]) err.toArray(new ErrorMessage[err.size()])));
	        }
	
	        // Copy the (previously) selected items.
	        Iterator it = Arrays.asList(buc.getResourceSelection()).iterator();
	        while (it.hasNext()) {
	            IResource r = (IResource) it.next();
	            if (r.getParent().getUrl().equals(destination.getUrl())) {
	                msg = "Original folder and destination folder are the same folder.";
	                problem = "Unable to copy '" + r.getName() + "'. " + msg;
	                solution = "Please choose a different destination folder.";
	                err.add(new ErrorMessage("other", problem, solution));
	            } else {
	                try {
	                    destination.getOwner().copy(r, destination, false);
                    } catch (OperationTimeoutException ote) {
                        err.add(new ErrorMessage("other", "Operation timed out.", ""));
	                } catch (Exception e) {
	                    msg = e.getMessage();
	                    problem = "Unable to copy '" + r.getName() + "'. " + msg;
	                    solution = "";
	                    err.add(new ErrorMessage("other", problem, solution));
	                    e.printStackTrace();
	                }
	            }
	        }
        }
        Location fromLocation = (Location)buc.getSession().getAttribute("fromLocation");
        // Clean up the user context & session
        // Purge the move/copy related info 
        buc.clearResourceSelection();
        buc.getSession().removeAttribute("fromLocation");
        buc.getSession().removeAttribute("destinationMode");
        
        if (err.size() == 0) {
            return new SimpleActionResponse(screen, new FolderQuery(fromLocation, app, buc));
        } else {
            // Display the error.
            return new SimpleActionResponse(screen, new FolderQuery(fromLocation, app,
                buc, (ErrorMessage[]) err.toArray(new ErrorMessage[err.size()])));
        }
    }

    /*
     * Package API.
     */

    private CopyResourcesAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }

}