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

package net.unicon.penelope.store.jvm;

import java.util.Map;
import java.util.WeakHashMap;

import org.dom4j.Element;

import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.PenelopeException;
import net.unicon.penelope.store.AbstractEntityStore;
import net.unicon.penelope.store.ISequencer;

public final class JvmEntityStore extends AbstractEntityStore {

    // Instance Members.
    private final ISequencer seq;
    private final Map cache;

    /*
     * Public API.
     */

    public JvmEntityStore() {

        // Instance Members.
        this.seq = new JvmSequencer(0);
        this.cache = new WeakHashMap();

    }

    public IChoiceCollection parseChoiceCollection(Element e)
                                throws PenelopeException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("choice-collection")) {
            String msg = "Argument 'e' must be an <choice-collection> element.";
            throw new IllegalArgumentException(msg);
        }

        // Parse the entities.
        IChoiceCollection rslt = null;
        try {
            rslt = ChoiceCollectionImpl.parse(e, this);
        } catch (Throwable t) {
            String msg = "Unable to construct a choice collection from the "
                                                        + "specified XML.";
            throw new EntityCreateException(msg, t);
        }

        // Update the cache.
        cache.put(new Long(rslt.getId()), rslt);
        IChoice[] choices = rslt.getChoices();
        for (int i=0; i < choices.length; i++) {
            IChoice c = choices[i];
            cache.put(new Long(c.getId()), c);
            IOption[] options = c.getOptions();
            for (int j=0; j < options.length; j++) {
                IOption o = options[j];
                cache.put(new Long(o.getId()), o);
            }
        }

        return rslt;

    }

    public IOption createOption(Handle handle, Label label,
                    IComplementType complementType)
                    throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IOption rslt = new OptionImpl(id, this, handle, label, complementType);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    public IChoice createChoice(Handle handle, Label label, IOption[] options,
                                    int minSelections, int maxSelections)
                                    throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IChoice rslt = new ChoiceImpl(id, this, handle, label, options,
                                    minSelections, maxSelections);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    public IChoiceCollection createChoiceCollection(Handle handle, Label label,
                            IChoice[] choices) throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IChoiceCollection rslt = new ChoiceCollectionImpl(id, this, handle,
                                                        label, choices);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    public ISelection createSelection(IOption option, IComplement complement)
                                            throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        ISelection rslt = new SelectionImpl(id, this, option, complement);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    public IDecision createDecision(Label l, IChoice choice,
                                ISelection[] selections)
                                throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IDecision rslt = new DecisionImpl(id, this, l, choice, selections);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    public IDecisionCollection createDecisionCollection(
                            IChoiceCollection choiceCollection,
                            IDecision[] decisions)
                            throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IDecisionCollection rslt = new DecisionCollectionImpl(id, this,
                                        choiceCollection, decisions);
        cache.put(new Long(id), rslt);
        return rslt;

    }

    /*
     * Protected API.
     */

    protected ISequencer sequencer() {
        return seq;
    }

}
