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
package net.unicon.portal.util;

import org.jasig.portal.channels.permissionsmanager.RDBMPermissibleRegistry;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.ResourceMissingException;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for registering Academus channels with
 * the uPortal framework Permissions Manager channel.
 */
public final class PermissionsManagerRegistry {

  private static final String resourceFilename = "/properties/PermissionsManagerRegistry.xml";

  public static void initialize() {

    Class[] permissibleClasses = getPermissibleClasses();

    for (int i=0; i<permissibleClasses.length; i++) {
      LogService.log(LogService.INFO, "Registering permissible class - " +
        permissibleClasses[i].getName());
      RDBMPermissibleRegistry.registerPermissible(
        permissibleClasses[i].getName());
    }
  }

  private static Class[] getPermissibleClasses() {
    List classes = new ArrayList();
    try {
      Document doc = ResourceLoader.getResourceAsDocument(
        PermissionsManagerRegistry.class, resourceFilename);

      Element el;
      NodeList nl = doc.getElementsByTagName("permissible");
      for (int i = 0; i < nl.getLength(); i++) {
        el = (Element)nl.item(i);
        classes.add(Class.forName(el.getAttribute("classPath")));
      }
    } catch (ResourceMissingException rme) {
      LogService.log(LogService.ERROR,
        "PermissionsManagerRegistry::getPermissibleClasses() : " +
        resourceFilename + " not found", rme);
    } catch (Exception e) {
      LogService.log(LogService.ERROR,
        "PermissionsManagerRegistry::getPermissibleClasses() : " +
        "Error retrieving permissible classes from " + resourceFilename, e);
    }

    return (Class[])classes.toArray(new Class[0]);
  }
}
