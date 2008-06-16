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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.demetrius.IFolder;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class CheckPermissionAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Static Members.
    private static DecimalFormat formatter = new DecimalFormat("##0.##");

    // Instance Members.
    private BriefcaseApplicationContext app;
    private String toScreen;
    private IScreen screen;
    private IScreen folderScreen;

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

        // ToScreen.
        Attribute s = e.attribute("to-screen");
        if (s == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'to-screen'.";
            throw new XmlFormatException(msg);
        }
        String toScreen = s.getValue();

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

        return new CheckPermissionAction(owner, handle, choices, toScreen);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (BriefcaseApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create(toScreen));
        folderScreen = m.getScreen(Handle.create("folderview"));
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

        ErrorMessage[] errors = new ErrorMessage[1];
        String problem = "";
        String msg = "";
        String solution = null;
        boolean hasPermission = true;

        // Check permissions for ADD
        List access = Arrays.asList(buc.getAccessRules(f.getOwner()));
        if (this.toScreen.equals("addedit_folder")) {
            if (!access.contains(new AccessRule(BriefcaseAccessType.ADD, true))) {
                msg = "User does not have 'add' access to the context folder.";
                problem = "Unable to create a folder.  " + msg;
                errors[0] = new ErrorMessage("other", problem, solution);
                hasPermission = false;
            }
        }else if (this.toScreen.equals("addfile")) {
            if (!access.contains(new AccessRule(BriefcaseAccessType.ADD, true))) {
                msg = "User does not have 'add' access to the context folder.";
                problem = "Unable to upload file.  " + msg;
                errors[0] = new ErrorMessage("other", problem, solution);
                hasPermission = false;
            }
        }


        if (hasPermission) {

            String sizeLimit = formatSize(buc.getDriveSelection().getMaxFileSize());
            StringBuffer state = new StringBuffer();
            state.append("<state><settings><upload-limit>").append(sizeLimit)
                    .append("</upload-limit></settings><status>")
                    .append("<command>add</command></status>")
					.append("<location><current><name>")
					.append(EntityEncoder.encodeEntities(f.getName()))
					.append("</name></current></location>")
                    .append("</state>");

            return new SimpleActionResponse(screen, new StateQueryImpl(state.toString()));
        } else {
            return new SimpleActionResponse(folderScreen, new FolderQuery(buc.getHistory().getLocation(), app, buc, errors));
        }
    }

    /*
     * Implementation.
     */

    private CheckPermissionAction(IWarlockFactory owner, Handle handle,
                                            String[] choices, String toScreen) {

        super(owner, handle, choices);

        // Instance Members.
        this.app = null;
        this.toScreen = toScreen;
        this.screen = null;
        this.folderScreen = null;

    }

    private String formatSize(long size) {
        StringBuffer formatted = new StringBuffer();
        double dSize = size;
        String units;
        if (dSize > 1048576)    {
            dSize /= 1048576.0;
            units = "MB";
        } else {
            dSize /= 1024.0;
            units = "KB";
        }
        formatted.append(formatter.format(dSize));
        formatted.append(" ");
        formatted.append(units);
        return formatted.toString();
    }

    /*
     * Nested Types.
     */

    private static final class StateQueryImpl implements IStateQuery {

        // Instance Members.
        private String state;

        /*
         * Public API.
         */

        public StateQueryImpl(String state) {

            // Assertions.
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            this.state = state;

        }

        public String query() {
            return state;
        }

        public IDecisionCollection[] getDecisions() {
            return new IDecisionCollection[0];
        }

    }

}
