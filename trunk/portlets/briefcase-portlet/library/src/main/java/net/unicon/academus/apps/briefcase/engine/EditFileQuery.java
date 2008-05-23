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

import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.demetrius.IFile;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeTextConfigurableLimit;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IApplicationContext;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.WarlockException;


public class EditFileQuery implements IStateQuery {

    // Static Members.
    private static final Integer MAX_FILE_AND_FOLDER_NAME_CHARS = Integer.valueOf(255);
    private static final Integer MAX_DESCRIPTION_CHARS = Integer.valueOf(1024);
    private static DateFormat fmt = DateFormat.getDateTimeInstance();
    private static DecimalFormat formatter = new DecimalFormat("##0.##");

    // Instance members.
    private IFile target;
    private BriefcaseApplicationContext app;
    private BriefcaseUserContext buc;
    private final Log log = LogFactory.getLog(getClass());

    /*
     * Public API.
     */

    public EditFileQuery(IFile target, IApplicationContext app, 
            IUserContext user) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.target = target;
        this.app = (BriefcaseApplicationContext)app;
        this.buc = (BriefcaseUserContext)user;

    }

    public String query() throws WarlockException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }
        
        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<state><properties>");

        // Type.
        rslt.append("<metadata type=\"type\"><label>Type</label><value>File</value></metadata>");

        // Location.
        rslt.append("<metadata type=\"location\"><label>Location</label><value>")
            .append(EntityEncoder.encodeEntities(target.getParent().getPath("/", false)))
            .append("</value></metadata>");

        // Size.
        rslt.append("<metadata type=\"size\"><label>Size</label><value>")
            .append(formatSize(target.getSize()))
            .append("</value></metadata>");

        // Modified.
        rslt.append("<metadata type=\"lastmod\"><label>Last Modified</label><value>")
            .append(fmt.format(target.getDateModified()))
            .append("</value></metadata>");

        // End.
        rslt.append("</properties>");
        
        // add location element
        
        rslt.append("<location> <current> "); 
        
        AccessRule[] access = buc.getAccessRules(target.getOwner());
        if (access != null) {
            for(int i = 0; i < access.length; i++){
                if(access[i].getStatus()){
	                rslt.append("<accesstype>")
	                	.append(access[i].getAccessType().getName())
	                	.append("</accesstype>");
                }
            }
        }
    	rslt.append("</current></location>");
        
        rslt.append("</state>");
        return rslt.toString();

    }

    public IDecisionCollection[] getDecisions() throws WarlockException {

        IDecisionCollection decisions = null;

        try {

            IEntityStore store = new JvmEntityStore();

            // Folder Name.
            IOption oName = store.createOption(Handle.create("file_name"), null,
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_NAME_CHARS));
            IChoice cName = store.createChoice(Handle.create("fileName"),
                                        Label.create("File Name"),
                                        new IOption[] { oName },
                                        1, 1);

            // Description.
            IOption oDescription = store.createOption(
                    Handle.create("file_Description"), null, 
                    TypeTextConfigurableLimit.createInstance(MAX_DESCRIPTION_CHARS));
            IChoice cDescription = store.createChoice(
                    Handle.create("fileDescription"), 
                    Label.create("Description"), 
                    new IOption[] { oDescription }, 1, 0);

            // Choices.
            Handle h = Handle.create("fileInfoForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null,
                                    new IChoice[] { cName, cDescription });

            // Folder Name Value.
            ISelection sName = store.createSelection(oName, 
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_NAME_CHARS).parse(
                                    target.getName()));
            IDecision dName = store.createDecision(null, cName, 
                    new ISelection[] { sName });

            // Description Value.
            ISelection sDescription = store.createSelection(oDescription, 
                    TypeTextConfigurableLimit.createInstance(
                            MAX_DESCRIPTION_CHARS).parse(""));
            // ToDo:  Implement if we end up going w/ description.
            IDecision dDescription = store.createDecision(null, cDescription, 
                    new ISelection[] { sDescription });

            decisions = store.createDecisionCollection(choices, 
                    new IDecision[] { dName, dDescription });

        } catch (Throwable t) {
            String msg = "EditFileQuery failed to build its decision "
                                                + "collection(s).";
            throw new WarlockException(msg, t);
        }

        return new IDecisionCollection[] { decisions };

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

}
