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
package net.unicon.toro.ant;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 SetHostname is an Ant task for setting the
 current hostname where ant is being executed.
 **/
public class DumpProperties extends Task {

  /**
   * Constructor of the JavaVersionTask class.
   **/
  public DumpProperties() {
    super();
  }

  /**
   * Execute the task.
   **/
  public void execute() throws BuildException {
        java.util.Hashtable h = getProject().getProperties();
        Map m = new TreeMap(h);
        Iterator itr = m.keySet().iterator();
        System.out.println("Project Properties:");
        while (itr.hasNext()) {
            String name = (String)itr.next();
            System.out.println(name+"="+m.get(name));
        }
  }
}
