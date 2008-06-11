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

package net.unicon.academus.apps.messaging.engine;

import java.util.Arrays;
import java.util.Iterator;

import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
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
import net.unicon.warlock.WarlockException;

public class ConfirmDeleteQuery extends InitialQuery {

    // Static members.
    private static IDecisionCollection[] decisions;

    static {
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

    /*
     * Public API.
     */

    public ConfirmDeleteQuery(MessagingUserContext user) {
        super(user);
    }

    public String query() throws WarlockException {

        StringBuffer rslt = new StringBuffer();

        rslt.append("<state>");
        super.commonQueries(rslt);

        /* EA: Message selections are handled by InitialQuery.
        rslt.append("<delete>");
        try {
            for (int i = 0; i < targets.length; i++) {
                rslt.append("<message id=\"")
                    .append(targets[i].getId())
                    .append("\">")
                    .append("<subject>")
                    .append(targets[i].getSubject())
                    .append("</subject>")
                    .append("</message>");
            }
        } catch (MercuryException e) {
            throw new RuntimeException("ConfirmDeleteQuery : Error in getting the message id or subject. ", e);
        }
        
        rslt.append("</delete>")
        */

        rslt.append("</state>");

        return rslt.toString();

    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        return this.decisions;
    }
}
