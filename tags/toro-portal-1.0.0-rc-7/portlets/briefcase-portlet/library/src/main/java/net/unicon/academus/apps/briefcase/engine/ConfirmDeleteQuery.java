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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

import net.unicon.demetrius.IResource;

public class ConfirmDeleteQuery implements IStateQuery {

    // Instance members.
    IResource[] targets;
    IDecisionCollection[] decisions;
    
    private final Log log = LogFactory.getLog(getClass());

    /*
     * Public API.
     */

    public ConfirmDeleteQuery(IResource[] targets) {

        // Assertions.
        if (targets == null) {
            String msg = "Argument 'targets' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (targets.length == 0) {
            String msg = "Argument 'targets' must contain at least 1 element.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.targets = new IResource[targets.length];
        System.arraycopy(targets, 0, this.targets, 0, targets.length);
        try {

            IEntityStore store = new JvmEntityStore();

            // Confirm.
            Label lbl = Label.create("Are you sure you want to delete these items?");
            IOption oYes = store.createOption(Handle.create("yes"), null,
                                                    TypeNone.INSTANCE);
            IOption oNo = store.createOption(Handle.create("no"), null,
                                                    TypeNone.INSTANCE);
            IChoice cConf = store.createChoice(Handle.create("deleteConfirmation"),
                                            lbl, new IOption[] { oYes, oNo },
                                            1, 1);

            // Choices.
            Handle h = Handle.create("confirmationForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null,
                                                new IChoice[] { cConf });

            // Confirm Value.
            ISelection sConf = store.createSelection(oNo, TypeNone.INSTANCE.parse(null));
            IDecision dConf = store.createDecision(null, cConf, new ISelection[] { sConf });

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dConf });
            decisions = new IDecisionCollection[] { dColl };

        } catch (Throwable t) {
            String msg = "ConfirmDeleteQuery failed to build its decision collection.";
            throw new RuntimeException(msg, t);
        }

    }

    public String query() throws WarlockException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }

        StringBuffer rslt = new StringBuffer();

        rslt.append("<state><delete>");
        Iterator it = Arrays.asList(targets).iterator();
        while (it.hasNext()) {
            IResource r = (IResource) it.next();
            rslt.append("<resource name=\"").append(EntityEncoder.encodeEntities(r.getName())).append("\">");
            rslt.append("<mimetype>").append(EntityEncoder.encodeEntities(r.getType().getName())).append("</mimetype>");
            rslt.append("</resource>");
        }

/*
        while (it.hasNext()) {
            IResource r = (IResource) it.next();
            rslt.append("<resource type=\"").append(r.getType().toString())
                                    .append("\">").append(r.getName())
                                    .append("</resource>");
        }
*/
        rslt.append("</delete></state>");

        return rslt.toString();

    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        return decisions;
    }

}
