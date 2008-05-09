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

package net.unicon.academus.delivery.virtuoso.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.domain.lms.Topic;
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
import net.unicon.portal.channels.topicadmin.IAdjunctAgent;
import net.unicon.portal.channels.topicadmin.AdjunctTopicData;

public final class ContentAdjunctAgent implements IAdjunctAgent {

    private static ContentAdjunctAgent instance = null;

    private static final Label dLabel = Label.create("Available Content Groups");

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

            // Obtain the content groups.
            IContentGroup[] groups = ContentGroupBroker.getAvailableContentGroups();
            if (groups.length == 0) {
                // Don't make an entry.
                return;
            }

            // Options...
            List options = new ArrayList();
            Iterator it = Arrays.asList(groups).iterator();
            while (it.hasNext()) {
                IContentGroup g = (IContentGroup) it.next();
                IOption o = store.createOption(
                        Handle.create(g.getHandle()),
                        Label.create(g.getName()),
                        TypeNone.INSTANCE
                    );
                options.add(o);
            }

            // Choice...
            IChoice c = store.createChoice(
                    Handle.create("groups"),
                    Label.create("Select the Content Groups that shall be "
                                    + "available for this Topic below:"),
                    (IOption[]) options.toArray(new IOption[0]),
                    0,
                    0
                );

            // Choice Collection...
            choices = store.createChoiceCollection(
                    Handle.create("content"),
                    Label.create("Content Integration"),
                    new IChoice[] { c }
                );

        } catch (Throwable t) {
            String msg = "ContentAdjunctAgent failed to initialize properly.";
            throw new DeliveryException(msg, t);
        }

        // Create & Register
        instance = new ContentAdjunctAgent(choices);
        AdjunctTopicData.getInstance().addData(instance, choices);

    }

    public static synchronized void refresh() throws DeliveryException {

        if (instance != null && instance.choices != null) {
            AdjunctTopicData.getInstance().removeData(instance.choices);
        }
        bootstrap();

    }

    public IDecisionCollection getDecisions(Topic t) throws ChannelException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IEntityStore store = choices.getOwner();
        IChoice c = choices.getChoices()[0];

        IDecisionCollection rslt = null;

        try {

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

        } catch (Throwable w) {
            String msg = "Unable to reconstruct associations.";
            throw new ChannelException(msg, w);
        }

        return rslt;

    }

    public void setDecisions(Topic t, IDecisionCollection c)
                                throws ChannelException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (c == null) {
            String msg = "Argument 'c [IDecisionCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        try {

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

        } catch (Throwable w) {
            String msg = "Unable to create associations.";
            throw new ChannelException(msg, w);
        }

    }

    /*
     * Implementation.
     */

    private ContentAdjunctAgent(IChoiceCollection choices) {

        // Assertions.
        if (choices == null) {
            String msg = "Argument 'choices' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.choices = choices;

    }

}
