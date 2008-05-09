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
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.academus.apps.briefcase.Location;
import net.unicon.demetrius.IFolder;
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

public final class GoToFolderAction extends AbstractWarlockFactory
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

        return new GoToFolderAction(owner, handle, choices);

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
        if (decisions.length != 1) {
            String msg = "Argument 'decisions' must contain exactly one "
                                                        + "element.";
            throw new IllegalArgumentException(msg);
        }
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking Briefcase Action: " + getClass().getName());
        }

        // Find the target.
        IDecisionCollection d_mff = decisions[0];
        IChoiceCollection c_mff = d_mff.getChoiceCollection();
        IDecision cl = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("goLocation")));
        String url = cl.getSelections()[0].getOption().getHandle().getValue();

        Location loc = null;

        // Decrypt url before using it
        EncryptionService encryptionService = app.getEncryptionService();
        url = encryptionService.decrypt(url);
        BriefcaseUserContext user = (BriefcaseUserContext) ctx;

        List err = new ArrayList();
        
        if (url.startsWith("FSA://")) {
            // IFolder...
            try{
	            IFolder f = (IFolder) AbstractResourceFactory.resourceFromUrl(url);
	            // check if user has view access (usefule in case of a shared factory)
	            AccessRule[] access = user.getAccessRules(f.getOwner());
	            if(!FolderQuery.hasAccess(access, BriefcaseAccessType.VIEW)){
	                throw new RuntimeException("You do not have view access on the given resource. ")	                        ;
	            }
	            loc = new Location(user.getDriveSelection(), f);
            }catch(Throwable t){
                String problem = t.getMessage();                            
                err.add(new ErrorMessage("other", problem, ""));
                t.printStackTrace();
                return new SimpleActionResponse(screen, new FolderQuery(new Location(user.getDriveSelection())
                        , app, user
                        , (ErrorMessage[]) err.toArray(new ErrorMessage[err.size()])));
            }
        } else {
            // Drive...
            Drive d = app.getDrive(url);
            loc = new Location(d);
            ((BriefcaseUserContext)ctx).setDriveSelection(d);
        }

        // Adjust the history.
        if(user.getHistory().getLocation().equals(loc)){
            if(user.getHistory().isFolderOpen()){
                user.getHistory().closeFolder();
            }else{
                user.getHistory().openFolder();
            }
        }else{
            if(loc.before(user.getHistory().getLocation())){
                user.getHistory().closeFolder();
            }else{
                user.getHistory().openFolder();
            }
            user.getHistory().setLocation(loc);
        }

        // Clear the selection (if any).
        user.clearResourceSelection();

        // Adjust the page.
        user.getFolderPaging().setCurrentPage(1);

        return new SimpleActionResponse(screen, new FolderQuery(loc, app, user));

    }

    /*
     * Package API.
     */

    private GoToFolderAction(IWarlockFactory owner, Handle handle,
                                            String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.app = null;
        this.screen = null;

    }

}