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
import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.dom4j.Element;

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
import net.unicon.warlock.portlet.FileUpload;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;

public final class UploadAction extends AbstractWarlockFactory
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

        return new UploadAction(owner, handle, choices);

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

        // Identify the destination folder.
        BriefcaseUserContext buc = (BriefcaseUserContext) ctx;
        IFolder f = (IFolder) buc.getHistory().getLocation().getObject();

        boolean uploaded = true;    // default...
        String msg = null;

        // Add each item.
        IDecisionCollection dCol = decisions[0];
        IChoiceCollection cCol = dCol.getChoiceCollection();
        IDecision d = dCol.getDecision(cCol.getChoice(net.unicon.penelope.Handle.create("fileUploads")));
        Iterator it = Arrays.asList(d.getSelections()).iterator();
        while (uploaded && it.hasNext()) {

            ISelection s = (ISelection) it.next();
            FileUpload fu = (FileUpload) s.getComplement().getValue();

            // Check to be sure we don't exceed the size limit.
            long maxSize = buc.getDriveSelection().getMaxFileSize();
            if (maxSize != Drive.NO_MAXIMUM_FILESIZE && fu.getSize() > maxSize) {
                msg = "The following file exceeds the size limit for file uploads:  " + fu.getName();
                uploaded = false;
                break;
            }

            try {

                // File Metadata.
                IChoiceCollection metaChoices = f.getOwner().getResourceMetadata(ResourceType.FILE);
                IEntityStore store = metaChoices.getOwner();
                IChoice nameChoice = metaChoices.getChoice(net.unicon.penelope.Handle.create("filename"));
                IOption nameOption = nameChoice.getOptions()[0];
                ISelection nameSelection = store.createSelection(nameOption, nameOption.getComplementType().parse(fu.getName()));
                IDecision nameDecision = store.createDecision(null, nameChoice, new ISelection[] { nameSelection });
                IDecisionCollection metaDecisions = store.createDecisionCollection(metaChoices, new IDecision[] { nameDecision });

                // Add the file.
                f.getOwner().addFile(metaDecisions, f, fu.getContents());    // TODO  we'll need to handle some errors here...
            } catch (OperationTimeoutException ote) {
                msg = "Operation timed out.";
                uploaded = false;
            } catch (Throwable t) {
                msg = t.getMessage();
                uploaded = false;
            }
        }

        if (uploaded) {
            return new SimpleActionResponse(screen, new FolderQuery(buc.getHistory().getLocation(), app, buc));
        } else {
            ErrorMessage[] errors = new ErrorMessage[1];
            String problem = "Unable to upload file.  " + msg;
            errors[0] = new ErrorMessage("other", problem, null);
            return new SimpleActionResponse(screen, new FolderQuery(buc.getHistory().getLocation(), app, buc, errors));
        }

    }

    /*
     * Implementation.
     */

    private UploadAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }

}