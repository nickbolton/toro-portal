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

package net.unicon.academus.apps.download;

import java.io.IOException;
import java.util.Properties;

public class DownloadServiceFactory {
   
   private static IDownloadService instance = null;
   
   private static final String propertyFile = "/config/downloadservice.properties";
   private static Properties props = null;

   public static IDownloadService getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance; 
   }
   
   private static synchronized void createInstance() {
      if (instance == null) {
         String impl = getProperty(DownloadServiceFactory.class, "impl");
         
         try {
           instance = (IDownloadService)Class.forName(impl)
                          .getConstructor(new Class[0])
                          .newInstance(new Object[0]); 
         } catch (Exception ex) {
            throw new RuntimeException(
                  "Unable to instansiate DownloadService implementation: "+impl,
                  ex);
         }
      }
   }
   
   private static synchronized void loadProperties() {
      if (props == null) {
	      Properties p = new Properties();
	      try {
	         p.load(DownloadServiceFactory.class.getResourceAsStream(propertyFile));
	      } catch (IOException e) {
	         throw new RuntimeException(
	               "Unable to load DownloadService properties file",
	               e);
	      }
	      props = p;
      }
   }
   
   public static String getProperty(Class clazz, String name) {
      if (props == null) {
         loadProperties();
      }

      return props.getProperty(clazz.getName()+"."+name);
   }
   
   public static int getPropertyAsInt(Class clazz, String name) {
      return Integer.parseInt(getProperty(clazz, name)); 
   }
   
}
