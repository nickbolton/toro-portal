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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 GenerateSecurityContextProperties is an Ant task for generating
 properties to use for configuring security.properties.
 **/
public class GenerateSecurityContextProperties extends Task {

  /**
   * Constructor of the JavaVersionTask class.
   **/
  public GenerateSecurityContextProperties() {
    super();
  }

  /**
   * Execute the task.
   **/
  public void execute() throws BuildException {
    int scIndex = 1;
    for (int i=1; i<=4; i++) {
      Boolean b1 = Boolean.valueOf(getProject().getProperty("ldap"+getIndex(i)+".checkbox"));
      Boolean b2 = Boolean.valueOf(getProject().getProperty("ldap"+getIndex(i)+".security.checkbox"));
      if (b1 && b2) {
        getProject().setProperty("ldap"+getIndex(scIndex)+".security.comment", "");
        getProject().setProperty("ldap"+getIndex(scIndex)+".name.security", getProject().getProperty("ldap"+getIndex(i)+".name"));
        scIndex++;
      }
    }
    for (int i=scIndex; i<=4; i++) {
      getProject().setProperty("ldap"+getIndex(scIndex)+".security.comment", "!--");
    }
  }
  
  private String getIndex(int i) {
     if (i>1) {
         return Integer.toString(i);
     }
     return "";
  }
}
