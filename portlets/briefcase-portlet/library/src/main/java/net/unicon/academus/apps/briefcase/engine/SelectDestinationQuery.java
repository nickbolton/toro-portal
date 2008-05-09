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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseSortMethod;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.academus.apps.briefcase.Location;
import net.unicon.academus.apps.ErrorMessage;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SelectDestinationQuery implements IStateQuery {

    private final Log log = LogFactory.getLog(getClass());

    // Instance members.
    private IResource[] targets;
    private Location location;
    private DestinationMode mode;
    private BriefcaseApplicationContext app;
    private BriefcaseUserContext user;
    private List<ErrorMessage> errorList = new LinkedList<ErrorMessage>();

    /*
     * Public API.
     */

    public SelectDestinationQuery(BriefcaseApplicationContext app,
            BriefcaseUserContext user, Location location) {

        // Assertions.
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (user == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        IResource[] targets = user.getResourceSelection();
        this.targets = new IResource[targets.length];
        System.arraycopy(
                targets, 0, this.targets, 0, targets.length);
        this.mode = (DestinationMode) user.getSession().getAttribute(
                "destinationMode");
        this.app = app;
        this.user = user;
        this.location = location;

    }

    public SelectDestinationQuery(BriefcaseApplicationContext app,
            BriefcaseUserContext user, Location location, 
            ErrorMessage[] errors) {
        this(app, user, location);
        this.errorList.addAll(Arrays.asList(errors));
    }

    public String query() throws WarlockException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }

        StringBuffer rslt = new StringBuffer();
        List<String> targetUrls = new ArrayList<String>();
        EncryptionService encryptionService = app.getEncryptionService();
        boolean addedTimeoutError = false;

        // access levels that the user has on the current factory
        AccessRule[] access = null;

        rslt.append("<state>");
        if (!errorList.isEmpty()) {
            rslt.append("<status>");
            Iterator it = errorList.iterator();
            while (it.hasNext()) {
                ErrorMessage msg = (ErrorMessage) it.next();
                rslt.append(msg.toXml());
            }
            rslt.append("</status>");
        }

        Object currentLocation = location.getObject();
        String id = null;
        String type = null;
        String name = null;
        String path = null;
        if (currentLocation instanceof IFolder) {
            IFolder f = (IFolder) currentLocation;
            id = f.getUrl();
            type = "folder";
            name = f.getName();
            path = f.getPath("/", true);
            
            access = user.getAccessRules(f.getOwner());
            
        } else if (currentLocation instanceof Drive) {
            Drive d = (Drive) currentLocation;
            id = d.getHandle();
            type = "drive";
            name = d.getLabel();
            path = d.getHandle();
        }
        
        id = encryptionService.encrypt(id);

        // Append the current open folder location
        rslt.append("<location><current id=\"");
        rslt.append(EntityEncoder.encodeEntities(id));
        rslt.append("\" type=\"");
        rslt.append(type);
        rslt.append("\" isOpen=\"");
        rslt.append(user.getHistory().isFolderOpen());
        rslt.append("\">");
        rslt.append("<name>");
        rslt.append(EntityEncoder.encodeEntities(name));
        rslt.append("</name>");
        rslt.append("<path>");
        rslt.append(EntityEncoder.encodeEntities(path));
        rslt.append("</path>");

        if (access != null) {
            for (int i = 0; i < access.length; i++) {
                if (access[i].getStatus()) {
                    rslt.append(
                            "<accesstype>").append(
                            access[i].getAccessType().getName()).append(
                            "</accesstype>");
                }
            }
        }
        rslt.append("</current>");
        rslt.append("</location>");

        rslt.append("<select mode=\"");
        rslt.append(EntityEncoder.encodeEntities(mode.getHandle()));
        rslt.append("\">");
        
        rslt.append("<targets>");        
        Iterator it = Arrays.asList(targets).iterator();
        while (it.hasNext()) {
            IResource r = (IResource) it.next();
            rslt.append(
                    "<resource name=\"").append(
                    EntityEncoder.encodeEntities(r.getName())).append(
                    "\">");
            rslt.append(
                    "<mimetype>").append(
                    EntityEncoder.encodeEntities(r.getType().getName()))
                    .append(
                            "</mimetype>");
            rslt.append("</resource>");
            if (r.getType().equals(
                    ResourceType.FOLDER)) {
                targetUrls.add(r.getUrl());
            }
        }
        rslt.append("</targets>");
        
        try {
            rslt.append("<destinations>");

            // Drives.
            List<Drive> drives = user.getDrives();

            for (Drive d : drives) {

                String encryptedHandle = encryptionService.encrypt(
                        d.getHandle());

                rslt.append("<drive id=\"");
                rslt.append(EntityEncoder.encodeEntities(
                        encryptedHandle));
                rslt.append("\" class-large=\"");
                rslt.append(d.getLargeIcon());
                rslt.append("\" class-opened=\"");
                rslt.append(d.getOpenIcon());
                rslt.append("\" class-closed=\"");
                rslt.append(d.getClosedIcon());
                rslt.append("\">");
                rslt.append("<label>");
                rslt.append(EntityEncoder.encodeEntities(d.getLabel()));
                rslt.append("</label>");
                rslt.append("<description>");
                rslt.append(EntityEncoder.encodeEntities(d.getDescription()));
                rslt.append("</description>");

                if (user.getDriveSelection().equals(d)) {

                    // Append Factories if the drive matches the current drive.
                    Iterator<IAccessEntry> entries = Arrays.asList(
                            d.getBroker().getEntries(
                                    user.getPrincipal())).iterator();
                    Set<IResourceFactory> facs = new HashSet<IResourceFactory>();

                    // check for duplicate targets
                    while (entries.hasNext()) {
                        IAccessEntry e = entries.next();
                        if (hasAccess(e.getAccessRules(), 
                                BriefcaseAccessType.VIEW)) {
                            facs.add((IResourceFactory) e.getTarget());
                        }
                    }

                    Iterator<IResourceFactory> factories = facs.iterator();
                    while (factories.hasNext()) {
                        IResourceFactory fac = factories.next();
                        try {
                            StringBuffer folderXml = new StringBuffer();
                            evaluateFolderXml(fac.getRoot(), location, 
                                    folderXml);
                            rslt.append(folderXml);
                        } catch (OperationTimeoutException ote) {
                            if (!addedTimeoutError) {
                                addedTimeoutError = true;
                                errorList.add(new ErrorMessage("other",
                                        "Operation timed out.", ""));
                            }
                        }
                    }
                }

                rslt.append("</drive>");

            }
            rslt.append("</destinations>");
            rslt.append("</select></state>");
            
        } catch (DemetriusException de) {
            throw new WarlockException("Error in creating state query " 
                    + "for SelectDestinationQuery. ", de);
        }

        if (log.isDebugEnabled()) {
            log.debug("SelectDestinationQuery: " + rslt);
        }

        return rslt.toString();

    }

    public IDecisionCollection[] getDecisions() throws WarlockException {

        IDecisionCollection decisions = null;

        try {

            IEntityStore store = new JvmEntityStore();

            // From Location.
            Object currentLocation = location.getObject();
            String url = null;
            String name = null;
            if (currentLocation instanceof IFolder) {
                IFolder f = (IFolder) currentLocation;
                url = f.getUrl();
                name = f.getName();
            } 
            else if (currentLocation instanceof Drive) {
                Drive d = (Drive) currentLocation;
                url = d.getHandle();
                name = d.getLabel();
            }
            EncryptionService encryptionService = app.getEncryptionService();
            url = encryptionService.encrypt(url);
            IOption oDestination = store.createOption(
                    Handle.create(url), Label.create(name),
                    TypeNone.INSTANCE);

            String instructions = "Select the destination folder to "
                    + "move your items into.";
            IChoice cDestination = store.createChoice(
                    Handle.create("chooseDestination"), Label
                            .create(instructions),
                    new IOption[] { oDestination }, 1, 1);

            // Choices.
            IChoiceCollection choices = store.createChoiceCollection(
                    Handle.create("folderInfoForm"), null,
                    new IChoice[] { cDestination });

            // From Location Value.
            ISelection sDestination = store.createSelection(
                    oDestination, TypeNone.INSTANCE.parse(null));
            IDecision dDestination = store.createDecision(
                    null, cDestination, new ISelection[] { sDestination });

            // Decisions
            decisions = store.createDecisionCollection(
                    choices, new IDecision[] { dDestination });

        } catch (Throwable t) {
            String msg = "SelectDestinationQuery failed to build its decision "
                    + "collection(s).";
            throw new WarlockException(msg, t);
        }

        return new IDecisionCollection[] { decisions };

    }

    /*
     * Implementation.
     */

    private void evaluateFolderXml(IFolder f, Location location,
            StringBuffer out) throws DemetriusException {

        // Assertions.
        if (f == null) {
            String msg = "Argument 'f [IFolder]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (out == null) {
            String msg = "Argument 'out' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        String fUrl = f.getUrl();
        String lUrl = ""; // default;;
        if (location.getObject() instanceof IFolder) {
            lUrl = ((IFolder) location.getObject()).getUrl();
        }

        // Evaluate shared status.
        String shared = "no"; // default...
        if (user.getDriveSelection().isSharing()) {
            Drive shareDrive = app.getDrive(user.getDriveSelection()
                    .getShareTarget());
            shared = app.getSharedEntries(
                    f, user.getUsername(), shareDrive).length > 0 ? "yes"
                    : "no";
        }

        EncryptionService encryptionService = app.getEncryptionService();
        String encryptedUrl = encryptionService.encrypt(fUrl);

        // Begin.
        out.append("<folder id=\"");
        out.append(EntityEncoder.encodeEntities(encryptedUrl));
        out.append("\" name=\"");
        out.append(EntityEncoder.encodeEntities(f.getName()));
        out.append("\" shared=\"");
        out.append(shared);
        out.append("\" hasChildren=\"");
        out.append(f.getNumFolders() > 0 ? "true" : "false"); 
        out.append("\">");

        // Contents.
        if (lUrl.indexOf(fUrl) != -1) {
            // show contents up to and including the location folder...
            // check if the folder is in closed state
            if (!lUrl.equals(fUrl)
                    || (lUrl.equals(fUrl) && user.getHistory().isFolderOpen())) {
                ResourceType[] types = new ResourceType[] { ResourceType.FOLDER };
                IResource[] contents = f.getContents(types);
                AbstractResourceFactory.sortResources(
                        contents, BriefcaseSortMethod.NAME_ASCENDING
                                .getComparator());
                Iterator it = Arrays.asList(contents).iterator();
                while (it.hasNext()) {
                    IFolder sub = (IFolder) it.next();
                    evaluateFolderXml(
                            sub, location, out);
                }
            }
        }

        // End.
        out.append("</folder>");

    }

    static boolean hasAccess(AccessRule[] rules, AccessType type) {

        for (int i = 0; i < rules.length; i++) {
            if (rules[i].getAccessType().equals(
                    type) && rules[i].getStatus()) {
                return true;
            }
        }
        return false;
    }

}
