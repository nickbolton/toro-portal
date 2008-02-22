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
package net.unicon.toro.installer.anttask;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.types.FilterSet;

/**
 * SetHostname is an Ant task for setting the current hostname where ant is
 * being executed.
 */
public class MoveWithAllPropertiesFilter extends Move {

    /**
     * Constructor of the JavaVersionTask class.
     */
    public MoveWithAllPropertiesFilter() {
        super();
    }

    @Override
    public void setProject(Project project) {
        super.setProject(project);
        if (project == null) return;
        FilterSet allPropertiesFilterSet = new FilterSet();
        Hashtable h = getProject().getProperties();
        Iterator itr = h.keySet().iterator();
        while (itr.hasNext()) {
            String name = (String) itr.next();
            allPropertiesFilterSet.addFilter(name, (String) h.get(name));
        }
        super.getFilterSets().add(allPropertiesFilterSet);
    }
}
