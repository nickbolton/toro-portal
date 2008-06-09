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

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ImportPomProperties extends Task {

    /**
     * Constructor of the JavaVersionTask class.
     */
    public ImportPomProperties() {
        super();
    }

    /**
     * Execute the task.
     */
    public void execute() throws BuildException {
        File pom = new File("pom.xml");
        if (!pom.exists()) {
            throw new BuildException("pom.xml does not exist.");
        }
        
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(pom);
            Element source = doc.getRootElement();
            
            /*
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( System.out, format );
            writer.write( doc );
            */
            
            Element properties = (Element)source.selectSingleNode("//*[name()='properties']");
            if (properties != null) {
                for ( Iterator i = properties.elements().iterator(); i.hasNext(); ) {
                    Element property = (Element) i.next();
                    getProject().setProperty(property.getQualifiedName(), property.getText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
}
