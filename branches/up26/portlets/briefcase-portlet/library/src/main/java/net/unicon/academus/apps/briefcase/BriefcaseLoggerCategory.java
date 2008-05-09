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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.unicon.alchemist.log.ILoggerCategory;

/**
 * @author ibiswas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class BriefcaseLoggerCategory implements ILoggerCategory {

    private String name;
    private boolean isActive;
    
    public static final BriefcaseLoggerCategory BRIEFCASE = new BriefcaseLoggerCategory("BRIEFCASE");
    public static final BriefcaseLoggerCategory ACTION = new BriefcaseLoggerCategory("ACTION");
    public static final BriefcaseLoggerCategory QUERY = new BriefcaseLoggerCategory("QUERY");
    
    static{
        // read the logger config file to activate the specified categories
        String configPath = "c:/workingDir/uportal/src/dev/academus-apps/config/briefcase.config";
        SAXReader reader = new SAXReader();
        InputStream fis;
        try {
            fis = new FileInputStream(new File(configPath));            
            Element configElement = (Element) reader.read(fis).selectSingleNode("briefcase/logger/logger-categories"); 
            if(configElement == null){
                throw new IllegalArgumentException("Element  ");
            }
            Iterator categories = configElement.selectNodes("category").iterator();
            
            while(categories.hasNext()){
                Element e = (Element)categories.next();
                String category = e.attributeValue("name");
                if(category.equalsIgnoreCase(BRIEFCASE.getName())){
                    BRIEFCASE.isActive = true;
                }else if(category.equalsIgnoreCase(ACTION.getName())){
                    ACTION.isActive = true;
                }else if(category.equalsIgnoreCase(QUERY.getName())){
                    QUERY.isActive = true;
                }
            }            
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File with path " + configPath + "not found", e);
        } catch (DocumentException e) {
            throw new RuntimeException("Error in XML document ", e);
        }
        
    }
    
    public ILoggerCategory[] getAllCategories() {
        return new ILoggerCategory[] {BRIEFCASE, ACTION, QUERY};
    }

    /* (non-Javadoc)
     * @see net.unicon.alchemist.log.ILoggerCategory#toInt()
     */
    public int toInt() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.unicon.alchemist.log.ILoggerCategory#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see net.unicon.alchemist.log.ILoggerCategory#isActive()
     */
    public boolean isActive() {
        return isActive;
    }
    
    private BriefcaseLoggerCategory(String name) {
        this.name = name;
    }

}
