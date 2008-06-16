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

package net.unicon.portal.channels.topicadmin;

import java.util.HashMap;
import java.util.Map;

import net.unicon.penelope.IChoiceCollection;

public final class AdjunctTopicData {

    // Static Members.
    private static AdjunctTopicData instance = null;

    // Instance Members.
    private Map data;

    /*
     * Public API.
     */

    public static AdjunctTopicData getInstance() {

        // Initialize if we need to.
        if (instance == null) {
            synchronized(AdjunctTopicData.class) {
                if (instance == null) {
                    instance = new AdjunctTopicData();
                }
            }
        }

        return instance;

    }

    public void addData(IAdjunctAgent a, IChoiceCollection c) {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a [IAdjunctAgent]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (c == null) {
            String msg = "Argument 'c [IChoiceCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        data.put(c, a);

    }

    public void removeData(IChoiceCollection c) {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c [IChoiceCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!data.containsKey(c)) {
            String msg = "AdjunctTopicData does not contain an entry for the "
                                        + "specified choice collection.";
            throw new IllegalArgumentException(msg);
        }

        data.remove(c);

    }

    public IChoiceCollection[] getData() {
        return (IChoiceCollection[]) data.keySet().toArray(
                            new IChoiceCollection[0]);
    }

    public IAdjunctAgent getAgent(IChoiceCollection c) {

        // Assertions.
        if (c == null) {
            String msg = "Argument 'c [IChoiceCollection]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return (IAdjunctAgent) data.get(c);

    }

    /*
     * Implementation.
     */

    private AdjunctTopicData() {

        // Instance Members.
        this.data = new HashMap();

    }

}
