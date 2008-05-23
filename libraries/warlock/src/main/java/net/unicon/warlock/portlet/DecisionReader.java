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

package net.unicon.warlock.portlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.WarlockException;

public final class DecisionReader {

    /*
     * Public API.
     */

    public static IDecisionCollection readDecisionCollection(
                            IChoiceCollection c, Map inpt)
                            throws WarlockException {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c [IChoiceCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (inpt == null) {
            String msg = "Argument 'inpt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IDecisionCollection rslt = null;
        try {

            List choices = Arrays.asList(c.getChoices());
            IDecision[] decisions = new IDecision[choices.size()];
            StringBuffer key = new StringBuffer();
            key.append(c.getHandle().getValue());
            int preLength = key.length();
            for (int i=0; i < choices.size(); i++) {
                IChoice h = (IChoice) choices.get(i);
                decisions[i] = readDecision(h, key, inpt);
                key.setLength(preLength);
            }

            rslt = c.getOwner().createDecisionCollection(c, decisions);

        } catch (Throwable t) {
            String msg = "An error occured marshalling user input.";
            throw new WarlockException(msg, t);
        }
        return rslt;

    }

    public static IDecision readDecision(IChoice c, StringBuffer key,
                                    Map inpt) throws Throwable {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c [IChoice]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (key == null) {
            String msg = "Argument 'key' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (inpt == null) {
            String msg = "Argument 'inpt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

/*
System.out.println("SPILLING PARAMETERS...for "+c.getHandle().getValue());
Iterator it = inpt.keySet().iterator();
while (it.hasNext()) {
    String param = (String) it.next();
    System.out.println("\t"+param);
    Object[] values = (Object[]) inpt.get(param);
    for (int i=0; i < values.length; i++) {
        System.out.println("\t\t"+values[i]);
    }
}
*/

        // Manage the key.
        List selections = new ArrayList();
        key.append("_").append(c.getHandle().getValue());
        int preLength = key.length();

        // Look at the request.
        String[] vals = (String[]) inpt.get(key.toString());
        if (vals != null && vals[0].trim().length() != 0) { // see below ...
            // NB:  The rendering layer *may* send a blank to signify no
            // selection.  The choice is ignored as though it's handle weren't
            // included.
            for (int i=0; i< vals.length; i++) {
                IOption o = c.getOption(Handle.create(vals[i]));
                key.append("_").append(o.getHandle().getValue());
                Object[] value = (Object[]) inpt.get(key.toString());
                Object data = null;
                if (value != null) {
                    data = value[0];
                }
                IComplement p = o.getComplementType().parse(data);
                selections.add(c.getOwner().createSelection(o, p));
                key.setLength(preLength);
            }
        }

        ISelection[] ary = (ISelection[]) selections.toArray(new ISelection[0]);
        return c.getOwner().createDecision(null, c, ary);

    }

}