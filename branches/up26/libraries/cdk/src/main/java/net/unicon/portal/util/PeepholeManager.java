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

// Java API classes
import java.util.Hashtable;
import java.io.IOException;

// uPortal API classes
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.services.LogService;
import org.jasig.portal.ResourceMissingException;

public class PeepholeManager {

     // Filename expected for peephole HTML fragments
     private static final String PEEPHOLE_MARKUP = "peephole.html";

     // String token replaced in peephole HTML fragments
     private static final String BASE_ACTION_URL = "@BASE_ACTION_URL";

     private static PeepholeManager _instance  = null;
     private static Hashtable    peepholeCache = null;

     /**
      * PeepholeManager constructor. Defined as private so that
      * PeepholeManager can be used as a singleton class and
      * only instantiated through the <code>getInstance()</code> method.
      */
     private PeepholeManager () {

          peepholeCache = new Hashtable();
     }

     /**
      * Returns a PeepholeManager instance. Assures
      * that only one instance of PeepholeManager is created.           
      *
      * @return Returns a PeepholeManager instance.
      */
     public static PeepholeManager getInstance () {
                                                                           
          if  (_instance == null) {

               _instance = new PeepholeManager();

               LogService.log(LogService.DEBUG, "PeepholeManager initialized successfully.");               
          }

          return _instance;
     }

     /**
      * Returns a peephole view in the form of a <code>String</code>. The appropriate
      * peephole view is determined based on the package structure of the <code>
      * requestingClass</code> parameter.
      *
      * @param requestingClass The Class instance of the class requesting the
      * peephole view. The package structure of the class will determine the
      * location of the peephole HTML fragment to be used.
      * @param baseActionURL The base action URL of the user requesting the
      * peephole view. Each peephole view needs to have a base
      * action URL that matches the requesting user.
      *
      * @return Returns a peephole view (HTML fragment) in the form of a <code>
      * String</code>.
      */
     public String getPeephole (Class requestingClass, String baseActionURL) {

          String cacheKey = requestingClass.getPackage().getName();
          String peepholeView = null;
          StringBuffer msg = new StringBuffer();

          try {
                                                                      
               // Determine if the peephole view is already in cache
               if (peepholeCache.containsKey(cacheKey)) {	

                    peepholeView = (String) peepholeCache.get(cacheKey);

                    msg.append("PeepholeManager: Cache hit for ");
                    msg.append(cacheKey);
                    msg.append(". Retrieving peephole view from cache.");

                    LogService.log(LogService.DEBUG, msg.toString());

               } else {

                    peepholeView = ResourceLoader.getResourceAsString (requestingClass, PEEPHOLE_MARKUP);

                    peepholeCache.put(cacheKey, peepholeView);

                    msg.append("PeepholeManager: Cache miss for ");
                    msg.append(cacheKey);
                    msg.append(". Retrieving peephole view from filesystem.");

                    LogService.log(LogService.DEBUG, msg.toString());
               }

               // Adjust the baseActionURL in the peephole content
               peepholeView =  adjustBaseActionURL(peepholeView, baseActionURL);


          } catch (IOException ioe) {

               msg.append("PeepholeManager: An I/O error occured while retrieving peephole view for ");
               msg.append(cacheKey);
                 
               LogService.log(LogService.ERROR, msg.toString(), ioe);
                                            
          } catch (ResourceMissingException rme) {

               msg.append("PeepholeManager: Peephole view for  ");
               msg.append(cacheKey);                                    
               msg.append(" not found.");                                                   

                LogService.log(LogService.ERROR, msg.toString(), rme);

          } catch (Exception e) {

               msg.append("PeepholeManager: An unknown error occured while retrieving peephole view for  ");
               msg.append(cacheKey);
               
               LogService.log(LogService.ERROR, msg.toString(), e);
          }

          return peepholeView;
     }

     /**
      * Returns a peephole <code>String</code> representation. The representation
      * has the <code>BASE_ACTION_URL</code> token in the provided peephole view replaced
      * with the provided base action URL.
      *
      * @return Returns a peephole view <code>String</code> representation with the
      * <code>BASE_ACTION_URL</code> token replaced with the provided base action URL.
     */
     private String adjustBaseActionURL(String peepholeView, String baseActionURL) {

          String adjustedPeepholeView = peepholeView.replaceAll(BASE_ACTION_URL, baseActionURL);

          return adjustedPeepholeView;
     }

}// end of PeepholeManager class                               
