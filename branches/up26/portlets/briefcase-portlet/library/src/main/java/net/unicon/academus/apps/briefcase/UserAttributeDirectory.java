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

package net.unicon.academus.apps.briefcase;

import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.userattribute.IUserAttribute;
import net.unicon.demetrius.fac.filesystem.FsResourceFactory;

import org.dom4j.Element;

public class UserAttributeDirectory implements IUserAttribute {

    // Instance Members
    private AccessRule[] accessRules = new AccessRule[0];
    private String rootName = null;
    private long maxSize = 0;

    public static IUserAttribute parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("user-attribute")) {
            String msg = "Argument 'e [Element]' must be an <user-attribute> "
                + "element.";
            throw new IllegalArgumentException(msg);
        }


        // Access.
        Element r = (Element) e.selectSingleNode("access");
        if (r == null) {
            String msg = "Element <access> is missing. ";
            throw new IllegalArgumentException(msg);
        }
        AccessRule[] access = AccessRule.parse(r);

        // Name.
        Element eName = (Element) e.selectSingleNode("root-name");
        String name= "";
        if (eName != null) {
            name = eName.getText();
        } else {
            String msg = "Element <user-attribute> is missing required child "
                + "element <root-name>.";
            throw new IllegalArgumentException(msg);
        }

        // Size.
        Element eSize = (Element) e.selectSingleNode("max-size");
        long size = 0;
        if (eSize != null) {
            size = Long.parseLong(eSize.getText());
        } else {
            String msg = "Element <user-attribute> is missing required child "
                + "element <max-size>.";
            throw new IllegalArgumentException(msg);
        }

        return new UserAttributeDirectory(access, name, size);

    }

    public UserAttributeDirectory(AccessRule[] rules, String name, long value){
        this.accessRules = new AccessRule[rules.length];
        System.arraycopy(rules, 0, accessRules, 0, rules.length);
        this.rootName = name;
        this.maxSize = value;
    }

    public AccessRule[] getAccessRules() {
        return accessRules;
    }

    public Object getObject(String value) {
        return new FsResourceFactory(
                         value.replaceAll("\\\\", "/"),
                         getRootName(),
                         getMaxSize());
    }

    public String getRootName() {
        return rootName;
    }

    public long getMaxSize() {
        return this.maxSize;
    }

}
