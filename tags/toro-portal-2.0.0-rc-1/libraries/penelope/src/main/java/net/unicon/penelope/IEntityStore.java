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

package net.unicon.penelope;

import org.dom4j.Element;
import org.dom4j.io.SAXEventRecorder;
import org.xml.sax.ContentHandler;

public interface IEntityStore {

    IChoiceCollection parseChoiceCollection(Element e) throws PenelopeException;

    IOption createOption(Handle h, Label l, IComplementType complementType)
                                            throws EntityCreateException;

    IChoice createChoice(Handle h, Label l, IOption[] options,
                    int minSelections, int maxSelections)
                    throws EntityCreateException;

    IChoiceCollection createChoiceCollection(Handle h, Label l,
                                IChoice[] choices)
                                throws EntityCreateException;

    ISelection createSelection(IOption option, IComplement complement)
                                    throws EntityCreateException;

    IDecision createDecision(Label l, IChoice choice, ISelection[] selections)
                                            throws EntityCreateException;

    IDecisionCollection createDecisionCollection(
                IChoiceCollection choiceCollection,
                IDecision[] decisions)
                throws EntityCreateException;
    
    IChoiceCollectionParser getChoiceCollectionParser(ContentHandler ch);

}