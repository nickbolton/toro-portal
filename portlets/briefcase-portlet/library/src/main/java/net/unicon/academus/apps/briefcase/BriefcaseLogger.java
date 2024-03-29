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

import org.dom4j.Element;

import net.unicon.alchemist.log.ILoggerCategory;
import net.unicon.alchemist.log.Logger;
import net.unicon.alchemist.log.SystemOutLogger;

/**
 * @author ibiswas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BriefcaseLogger{

    private static Logger logger;
    
    public static void bootStrap(Logger logger){
        BriefcaseLogger.logger = logger;
    }
    
    public static void log(String message, ILoggerCategory[] categories) {
        if(logger != null){
            logger.log(message, categories);
        }else{
            throw new IllegalArgumentException("The logger instance variable needs to be initailized" +
            		"in the bootStrap method.");
        }
    }
    
    public static void log(String message) {
        log(message, new ILoggerCategory[] {});
    }
        
    public static void main(String[] args) {
        Element e = null;
        
        bootStrap(SystemOutLogger.parse(e));
        
        logger.log("With 1 Category ", new ILoggerCategory[] {BriefcaseLoggerCategory.BRIEFCASE});
        
        logger.log("With 1 Category ", new ILoggerCategory[] {BriefcaseLoggerCategory.ACTION});
        
        logger.log("With no Category ");
        
        logger.log("With 2 Category ", new ILoggerCategory[] {BriefcaseLoggerCategory.ACTION, BriefcaseLoggerCategory.QUERY});
        
    }

}
