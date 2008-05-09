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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jasig.portal.services.LogService;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
/**
 * This class is used as a utility for invoking Velocity templates.
 *@author     mfreestone
 *@created    December 6, 2002
 */
public class TemplateUtils {
    private static final String PROPERTIES_FILE_NAME =
    "/properties/velocity.properties";
    private static final Properties props = new Properties();
    static {
        loadProps();
    }
    /** Load the properties. */
    protected static void loadProps () {
        try {
            System.out.println("Loading Velocity Properties\n");
            props.load(TemplateUtils.class.getResourceAsStream(
            PROPERTIES_FILE_NAME));
            Velocity.init(props);
        } catch (IOException ioe) {
            LogService.instance().log(LogService.ERROR,
            "Unable to read velocity.properties file.");
            LogService.instance().log(LogService.ERROR, ioe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *  Merges map parameters with Velocity template
     *@param params    Parameters to be merged with template
     *@param vmFile    File name and path to the template (Path is relative to the resources root directory)
     *@return          The merged results as a String
     */
    public static String mergeToString(Map params, String vmFile) {
        return (mergeToBuffer(params, vmFile)).toString();
    } // end mergeToString
    /**
     *  Merges map parameters with Velocity template
     *@param params    Parameters to be merged with template
     *@param vmFile    File name and path to the template (Path is relative to the resources root directory)
     *@return          The merged results as a StringBuffer
     */
    public static StringBuffer mergeToBuffer(Map params, String vmFile) {
        StringBuffer result = new StringBuffer("");
        try {
            // create new context object to merge with template.
            VelocityContext context = new VelocityContext();
            // put parameters into context object.
            Iterator it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                context.put(key, params.get(key));
            }
            // get template.
            Template template =  null;
            try {
                template = Velocity.getTemplate(vmFile);
            }
            catch (ResourceNotFoundException rnfe) {
                rnfe.printStackTrace();
            }
            catch (ParseErrorException pee) {
                pee.printStackTrace();
            }
            // create StringWriter and write out merged results.
            StringWriter writer = new StringWriter();
            if (template != null) {
                template.merge(context, writer);
            }
            // flush and append to results buffer.
            writer.flush();
            result.append(writer.getBuffer());
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    } // end mergeToBuffer
    public static void main(String[] args) {
        TemplateUtils utils = new TemplateUtils();
        Map params = new HashMap();
        params.put("width", "50");
        params.put("mediaPath", "media");
        params.put("skin", "unicon");
        System.out.println(TemplateUtils.mergeToString(params, "SuperChannel/SuperChannel.vm"));
    } // end main
    protected TemplateUtils() { } // protected default constructor
} // end TemplateUtils class
