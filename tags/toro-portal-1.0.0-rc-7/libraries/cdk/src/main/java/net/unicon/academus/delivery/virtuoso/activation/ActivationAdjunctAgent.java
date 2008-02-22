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

package net.unicon.academus.delivery.virtuoso.activation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.delivery.DeliveryException;
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
import net.unicon.portal.channels.ChannelException;
import net.unicon.portal.channels.gradebook.IAdjunctAgent;
import net.unicon.portal.common.service.activation.Activation;

public final class ActivationAdjunctAgent implements IAdjunctAgent {

    private static IAdjunctAgent instance = null;

    // private static final Label dLabel = Label.create("XXX");

    // Instance Members.
    private IChoiceCollection choices;

    /*
     * Public API.
     */

    public static void bootstrap() throws DeliveryException {

        IEntityStore store = new JvmEntityStore();

        IChoiceCollection choices = null;

        // Create the choice collection.
        try {

            // Hours...
            List hOptions = new ArrayList();
            for (int i=0; i < 5; i++) {
                IOption o = store.createOption(
                        Handle.create(Integer.toString(i)),
                        Label.create(Integer.toString(i)),
                        TypeNone.INSTANCE
                    );
                hOptions.add(o);
            }
            IChoice c1 = store.createChoice(
                    Handle.create("hours"),
                    Label.create("Duration (hours):"),
                    (IOption[]) hOptions.toArray(new IOption[0]),
                    1,
                    1
                );

            // Minutes...
            List mOptions = new ArrayList();
            for (int i=0; i < 60; i = i+5) {
                String min = Integer.toString(i);
                min = min.length() == 2 ? min : "0" + min;
                IOption o = store.createOption(
                        Handle.create(Integer.toString(i)),
                        Label.create(min),
                        TypeNone.INSTANCE
                    );
                mOptions.add(o);
            }
            IChoice c2 = store.createChoice(
                    Handle.create("minutes"),
                    Label.create("Duration (minutes):"),
                    (IOption[]) mOptions.toArray(new IOption[0]),
                    1,
                    1
                );

            // Choice Collection...
            choices = store.createChoiceCollection(
                    Handle.create("activation"),
                    Label.create("Additional Settings"),
                    new IChoice[] { c1, c2 }
                );

        } catch (Throwable t) {
            String msg = "ActivationAdjunctAgent failed to initialize properly.";
            throw new DeliveryException(msg, t);
        }

        // Create.
        instance = new ActivationAdjunctAgent(choices);

    }

    public static IAdjunctAgent getInstance() {
        if (instance == null) {
            String msg = "ActivationAdjunctAgent has not been initialized.  "
                    + "You must call bootstrap() before using this agent.";
            throw new IllegalStateException(msg);
        }
        return instance;
    }

    public IChoiceCollection getChoices() {
        return choices;
    }

    public IDecisionCollection getDecisions(Activation a)
                            throws ChannelException {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a [Activation]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IEntityStore store = choices.getOwner();
        IChoice c = choices.getChoices()[0];

        IDecisionCollection rslt = null;

        try {

/*
            // Selctions...
            List sel = new ArrayList();
            IContentGroup[] groups = ContentAssociationManager.getAssociations(t);
            Iterator it = Arrays.asList(groups).iterator();
            while (it.hasNext()) {
                IContentGroup g = (IContentGroup) it.next();
                IOption o = c.getOption(Handle.create(g.getHandle()));
                sel.add(store.createSelection(o, o.getComplementType()
                                                    .parse(null)));
            }

            // Decision...
            ISelection[] selections = (ISelection[]) sel.toArray(
                                            new ISelection[0]);
            IDecision d = store.createDecision(dLabel, c, selections);

            // Decision Collection...
            rslt = store.createDecisionCollection(choices,
                                new IDecision[] { d });
*/

        } catch (Throwable w) {
            String msg = "Unable to reconstruct associations.";
            throw new ChannelException(msg, w);
        }

throw new UnsupportedOperationException();

        // return rslt;

    }

    public void setDecisions(Activation a, IDecisionCollection c)
                                throws ChannelException {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a [Activation]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (c == null) {
            String msg = "Argument 'c [IDecisionCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        try {

/*
            // Clear existing associations.
            IContentGroup[] assc = ContentAssociationManager.getAssociations(t);
            if (assc.length > 0) {
                ContentAssociationManager.disassociate(t, assc);
            }

            // Associate the new bunch, if there are any.
            ISelection[] sel = c.getDecisions()[0].getSelections();
            if (sel.length > 0) {
                List l = new ArrayList();
                Iterator it = Arrays.asList(sel).iterator();
                while (it.hasNext()) {
                    ISelection s = (ISelection) it.next();
                    l.add(ContentGroupBroker.getContentGroup(s.getOption().getHandle()
                                                                    .getValue()));
                }
                IContentGroup[] groups = (IContentGroup[]) l.toArray(
                                            new IContentGroup[0]);
                ContentAssociationManager.associate(t, groups);
            }
*/

        } catch (Throwable w) {
            String msg = "Unable to create associations.";
            throw new ChannelException(msg, w);
        }

throw new UnsupportedOperationException();

    }

    /*
     * Implementation.
     */

    private ActivationAdjunctAgent(IChoiceCollection choices) {

        // Assertions.
        if (choices == null) {
            String msg = "Argument 'choices' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.choices = choices;

    }

}
