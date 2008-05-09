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

import org.apache.tools.ant.*;

/**
 JavaVersionTask is an Ant task for testing if
 the installed Java version is greater than a 
 minimum required version.
 **/
public class JavaVersionTask extends Task {

  // Minimum required Java version.
  private String minVersion;
  // Installed Java version.
  private String installedVersion;
  // The name of the property that gets set when
  // the installed Java version is ok.
  private String propertyName;

  /**
   * Constructor of the JavaVersionTask class.
   **/
  public JavaVersionTask() {
    super();
    installedVersion = System.getProperty
                       ("java.version");
  }

  /**
   * Set the attribute minVersion.
   **/
  public void setMinVersion(String version) {
    minVersion = version;
  }

  /**
   Set the property name that the task sets when
   the installed Java version is ok.
   **/
  public void setProperty(String propName) {
    propertyName = propName;
  }

  /**
   * Execute the task.
   **/
  public void execute() throws BuildException {
    if (propertyName==null) {
      throw new BuildException("No property name set.");
    } else if (minVersion==null) {
      throw new BuildException("No minimum version set.");
    }

    if(installedVersion.compareTo(minVersion) >= 0) {
      getProject().setProperty(propertyName, "true");
    }
  }
}
