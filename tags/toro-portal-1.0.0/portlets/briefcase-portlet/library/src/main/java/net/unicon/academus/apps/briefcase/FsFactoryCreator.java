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

import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.jit.ICreator;
import net.unicon.demetrius.fac.filesystem.FsResourceFactory;

import org.dom4j.Element;

/**
 * @author ibiswas
 *
 * This class is used by the ResourceBroker to create new instances of the FSResourceFactory.
 * The parse method is used to create an instance of the class.
 */
public class FsFactoryCreator implements ICreator {

    // Instance Members.
    private String seedPath = null;
    private String rootName = null; 
    private long maxLimit = 0;

    /**
     * Returns an instance of the class by parsing the <code>Element</code>.
     * @param e XML <code>Element</code>
     * @return an instance of the class
     */
    public static ICreator parse(Element e){

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("creator")) {
            String msg = "Argument 'e [Element]' must be a <creator> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Root Path.
        Element ePath = (Element) e.selectSingleNode("seed-path");
        if (ePath == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <seed-path>.";
            throw new IllegalArgumentException(msg);
        }
        String seedPath = ePath.getText();
        
        // Root Name.
        Element eName = (Element) e.selectSingleNode("root-name");
        if (eName == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <root-name>.";
            throw new IllegalArgumentException(msg);
        }
        String rootName = eName.getText();
        
        // Size.
        Element eSize = (Element) e.selectSingleNode("size");
        if (eSize == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <size>.";
            throw new IllegalArgumentException(msg);
        }
        long size = Long.parseLong(eSize.getText()) * 1024 * 1024;

        return new FsFactoryCreator(seedPath, rootName, size);

    }

    /*
     * Implementation.
     */

    private FsFactoryCreator(String seedPath, String rootName, long size) {

        // Assertions.
        if (seedPath == null) {
            String msg = "Argument 'seedPath' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rootName == null) {
            String msg = "Argument 'rootName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.seedPath = seedPath;
        this.rootName = rootName;
        this.maxLimit = size;

    }

    /* (non-Javadoc)
     * @see net.unicon.academus.access.jit.ICreator#create()
     */
    public Object create(Identity id) {
        Kernel kernel = Kernel.create(seedPath);
        return new FsResourceFactory(kernel.getRootPath(),rootName, maxLimit);
    }

}
