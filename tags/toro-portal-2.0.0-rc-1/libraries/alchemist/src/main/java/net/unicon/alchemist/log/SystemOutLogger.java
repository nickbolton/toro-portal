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

package net.unicon.alchemist.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * @author ibiswas
 *
 */
public class SystemOutLogger extends PrintStreamLogger {

    /**
     * @param ps
     */
    public SystemOutLogger() {
        super(System.out);        
    }
    
    public static Logger parse(Element e){
        return new SystemOutLogger();
    }
    
    public static void main(String[] args){
        
        // read the logger config file to activate the specified categories
        String configPath = "c:/workingDir/uportal/src/dev/alchemist/properties/logger.xml";
        SAXReader reader = new SAXReader();
        InputStream fis;
        try {
            fis = new FileInputStream(new File(configPath));            
            Element configElement = (Element) reader.read(fis).selectSingleNode("logger");            
            Logger logger = Logger.parse(configElement);
            
            logger.log("With 1 Category ", new ILoggerCategory[] {SimpleLoggerCategory.DEBUG});
            
            logger.log("With 1 Category ", new ILoggerCategory[] {SimpleLoggerCategory.ERROR});
            
            logger.log("With no Category ");
            
            logger.log("With 2 Category ", new ILoggerCategory[] {SimpleLoggerCategory.DEBUG, SimpleLoggerCategory.ERROR});
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File with path " + configPath + "not found", e);
        } catch (DocumentException e) {
            throw new RuntimeException("Error in XML document ", e);
        }
    }

}
