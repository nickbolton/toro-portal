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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dom4j.Element;

/**
 * @author ibiswas
 * 
 */
public abstract class Logger {
    
    public abstract void log(String message, ILoggerCategory[] categories);
    
    public void log(String message){
        log(message, new ILoggerCategory[] {});
    }
    
    public static Logger parse(Element e){
        
        String impl = e.attributeValue("impl");
        if(impl == null){
            throw new IllegalArgumentException("Element <logger> is missing " +
            		"a required attribute 'impl'.");
        }
        try{
            Class c = Class.forName(impl);
            Method m = c.getDeclaredMethod("parse", new Class[] {Element.class});
            Object logger = m.invoke(c, new Object[] {e});
            
            return (Logger)logger;
            
        }catch(ClassNotFoundException cnfe){
            throw new RuntimeException("Logger unable to parse implmentation. ", cnfe);
        }catch(NoSuchMethodException nme){
            throw new RuntimeException("Logger unable to find parse method on implmentation. ", nme);
        }catch(IllegalAccessException iae){
            throw new RuntimeException("Logger unable to access parse method on implmentation. ", iae);
        }catch(InvocationTargetException ite){
            throw new RuntimeException("Logger unable to call parse method on given implmentation. ", ite);
        }      
        
    }
}

