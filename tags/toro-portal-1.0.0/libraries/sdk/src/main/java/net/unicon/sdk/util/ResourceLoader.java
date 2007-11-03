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
package net.unicon.sdk.util;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Properties;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * <p>This utility provides methods for accessing resources.
 * The methods generally use the classpath to find the resource
 * if the requested URL isn't already specified as a fully-qualified
 * URL string.</p>
 */
public class ResourceLoader {

  private static DocumentBuilderFactory f;
  static {
    f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
  }

  public static URL getResourceAsURL(Class requestingClass, String resource) throws Exception {
    URL resourceURL = null;
    try {
      resourceURL = new URL(resource);
    } catch (MalformedURLException murle) {
      // URL is invalid, now try to load from classpath
      resourceURL = requestingClass.getResource(resource);
      if (resourceURL == null) {
        String resourceRelativeToClasspath = null;
        if (resource.startsWith("/"))
          resourceRelativeToClasspath = resource;
        else
          resourceRelativeToClasspath = '/' + requestingClass.getPackage().getName().replace('.', '/') + '/' + resource;
        throw new Exception("Resource not found in classpath: " + resourceRelativeToClasspath);
      }
    }
    return resourceURL;
  }

  public static String getResourceAsURLString(Class requestingClass, String resource) throws Exception {
    return getResourceAsURL(requestingClass, resource).toString();
  }

  public static File getResourceAsFile(Class requestingClass, String resource) throws Exception {
    return new File(getResourceAsFileString(requestingClass, resource));
  }

  public static String getResourceAsFileString(Class requestingClass, String resource) throws Exception {
    return getResourceAsURL(requestingClass, resource).getFile();
  }

  /**
   * Returns the requested resource as a stream.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a stream
   */
  public static InputStream getResourceAsStream(Class requestingClass, String resource) throws Exception {
    return getResourceAsURL(requestingClass, resource).openStream();
  }

  /**
   * Returns the requested resource as a SAX input source.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a SAX input source
   */
  public static InputSource getResourceAsSAXInputSource(Class requestingClass, String resource) throws Exception {
    return new InputSource(getResourceAsURL(requestingClass, resource).openStream());
  }

  /**
   * Get the contents of a URL as an XML Document
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as an XML Document
   */
  public static Document getResourceAsDocument (Class requestingClass, String resource) throws Exception {
    InputStream inputStream = getResourceAsStream(requestingClass, resource);
    return f.newDocumentBuilder().parse(inputStream);
  }

  /**
   * Get the contents of a URL as a java.util.Properties object
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as a Properties object
   */
  public static Properties getResourceAsProperties (Class requestingClass, String resource) throws Exception {
    InputStream inputStream = getResourceAsStream(requestingClass, resource);
    Properties props = new Properties();
    props.load(inputStream);
    return props;
  }  
  
  /**
   * Get the contents of a URL as a String
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as a String
   */
  public static String getResourceAsString (Class requestingClass, String resource) throws Exception {
    String line = null;
    BufferedReader in = new BufferedReader (new InputStreamReader(getResourceAsStream(requestingClass, resource)));
    StringBuffer sbText = new StringBuffer (1024);
    while ((line = in.readLine()) != null)
      sbText.append (line).append ("\n");
    return sbText.toString ();
  }

} // end ResoureLoader class
