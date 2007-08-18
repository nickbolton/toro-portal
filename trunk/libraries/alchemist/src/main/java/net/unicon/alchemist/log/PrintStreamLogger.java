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

import java.io.PrintStream;

import org.dom4j.Element;

/**
 * @author ibiswas
 *
 */
public class PrintStreamLogger extends Logger {

    private PrintStream ps;
    
    public PrintStreamLogger(PrintStream ps){
        this.ps = ps;
    }
    
    public void log(String message, ILoggerCategory[] categories) {
        if(categories.length > 0){
            for(int i = 0; i < categories.length; i++){
                if(categories[i].isActive()){
                    ps.println(new StringBuffer(categories[i].getName()).append(" : ").append(message));
                    break;
                }
            }
        }else{
            ps.println(message);
        }
    }
    
    public static Logger parse(Element e){
        throw new UnsupportedOperationException("We need to come up with a strategy to be able" +
        		"to pass a printstream object");
    }

}
