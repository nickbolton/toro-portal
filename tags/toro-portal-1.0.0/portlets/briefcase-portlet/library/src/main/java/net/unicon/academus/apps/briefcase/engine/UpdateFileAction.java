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
import net.unicon.alchemist.access.AccessRule;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;
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

public final class UpdateFileAction extends AbstractWarlockFactory
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

        return new UpdateFileAction(owner, handle, choices);

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

        // User Input.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("fileName")));
        String name = (String) d.getSelections()[0].getComplement().getValue();

        BriefcaseUserContext user = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) user.getHistory().getLocation().getObject();
        
        ErrorMessage[] errors = new ErrorMessage[0];
        String problem = "";
        String msg = "";
        String solution = null;

        // Make the change.
        try {

            IFile target = (IFile) user.getResourceSelection()[0];
            
            // Check if user has edit permissions
            List access = Arrays.asList(user.getAccessRules(f.getOwner()));
	        if (!access.contains(new AccessRule(BriefcaseAccessType.EDIT, true))) {
	            msg = "You do not have edit permissions.";
	            problem = "Unable to edit '" +
	                        target.getName() +
	                        "'.  " + msg;
	            errors = new ErrorMessage[] {new ErrorMessage("other", problem, solution)};
	            
	        }else{
	            // Metadata.
	            IChoiceCollection metaChoices = f.getOwner().getResourceMetadata(ResourceType.FILE);
	            IEntityStore store = metaChoices.getOwner();
	            IChoice nameChoice = metaChoices.getChoice(net.unicon.penelope.Handle.create("filename"));
	            IOption nameOption = nameChoice.getOptions()[0];
	            ISelection nameSelection = store.createSelection(nameOption, nameOption.getComplementType().parse(name));
	            IDecision nameDecision = store.createDecision(null, nameChoice, new ISelection[] { nameSelection });
	            IDecisionCollection metaDecisions = store.createDecisionCollection(metaChoices, new IDecision[] { nameDecision });
	
	            // Update the folder.
	            f.getOwner().updateResource(metaDecisions, target);
	        }
        } catch (OperationTimeoutException ote) {
            errors = new ErrorMessage[] {new ErrorMessage("other", "Operation timed out.", "")};
        } catch (Throwable t) {
            errors = new ErrorMessage[1];
            problem = "Unable to update file.  " + t.getMessage();
            errors = new ErrorMessage[] {new ErrorMessage("other", problem, null) };            
        }
        
        user.clearResourceSelection();
        
        if(errors.length > 0){
            return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user, errors));
        }

        return new SimpleActionResponse(screen, new FolderQuery(user.getHistory().getLocation(), app, user));

    }

    /*
     * Package API.
     */

    private UpdateFileAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }

}