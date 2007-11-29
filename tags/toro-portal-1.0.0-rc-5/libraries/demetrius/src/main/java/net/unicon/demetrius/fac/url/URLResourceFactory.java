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

package net.unicon.demetrius.fac.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

import org.apache.commons.io.FilenameUtils;

/**
 * A read-only URL-oriented Resource factory implementation. It does not have
 * any support for folders, resource metadata, or write operations. In short,
 * the only thing it can do is getResource() which returns an IFile and
 * IFile.getInputStream() which returns the URL contents. It's a heavy-weight
 * wrapper around (new URL(myurl)).openStream().
 */
public class URLResourceFactory implements IResourceFactory {
   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(URLResourceFactory.class);

   private final URL baseURL;
   private final String facURL;

   public URLResourceFactory(String baseURL) throws MalformedURLException {
      // Assertions.
      if (baseURL == null) {
         String msg = "Argument 'baseURL' cannot be null.";
         throw new IllegalArgumentException(msg);
      }

      this.baseURL = new URL(baseURL);
      this.facURL = new StringBuffer("FSA://")
            .append(this.getClass().getName()).append("/")
            .toString();
   }

   public String getUrl() {
      return this.facURL+this.baseURL.toString();
   }

   private String getBaseFactoryURL() {
      return this.facURL;
   }

   public static IResourceFactory fromUrl(String facURL)
         throws DemetriusException {

      // Assertions.
      if (facURL == null || facURL.trim().equals("")) {
         throw new IllegalArgumentException("The Factory"
               + " Url must not be null or empty.");
      }

      IResourceFactory rf = null;
      /*
       * FSA:
       * 
       * classname
       * params
       */

      String[] tokens = facURL.split("/", 4);
      String baseURL = tokens[3];
      
      if (log.isDebugEnabled()) {
         log.debug("Factory URL: "+facURL+" Using base url: "+baseURL);
      }

      try {
         rf = new URLResourceFactory(baseURL);
      } catch (MalformedURLException e) {
         throw new DemetriusException("Invalid URL: " + facURL, e);
      }

      return rf;
   }

   public IResource getResource(String relativePath) throws DemetriusException {
      String cleanPath = FilenameUtils.normalize(relativePath);
      if (cleanPath == null || cleanPath.equals("")) {
         throw new IllegalArgumentException("Illegal path specified: "+relativePath);
      }
      
      URLFile urlFile = null;
      try {
         URL res = new URL(baseURL, cleanPath);
         urlFile = new URLFile(this, cleanPath, res);
         
         if (log.isDebugEnabled()) {
            log.debug("Creating resource object for original path '"+relativePath+"' clean path '"+
                  cleanPath+"' on URL: "+baseURL+" Result: "+res);
         }
      } catch (MalformedURLException e) {
         throw new DemetriusException(e.getMessage(), e);
      }
      return urlFile;
   }
   
   private static class URLFile implements IFile {
      private URLResourceFactory fac;
      private String relative;
      private URL url;

      public URLFile(URLResourceFactory fac, String relative, URL url) {
         this.fac = fac;
         this.relative = relative;
         this.url = url;
      }
      
      public InputStream getInputStream() throws DemetriusException {
         try {
            return this.url.openStream();
         } catch (IOException e) {
            throw new DemetriusException(e.getMessage(), e);
         }
      }

      public String getContentType() {
         return this.getMimeType();
      }

      public IFolder getParent() {
         throw new UnsupportedOperationException("Folders not implemented");
      }

      public String getName() {
         return url.getFile();
      }

      public IResourceFactory getOwner() {
         return fac;
      }

      public ResourceType getType() {
         return ResourceType.FILE;
      }

      public long getSize() {
         return 0;
      }

      public Date getDateModified() {
         return new Date();
      }

      public boolean isHidden() {
         return false;
      }

      public String getPath(String delimiter, boolean includeResource) {
         return this.url.toString();
      }

      public String getRelativePath(String delimiter, boolean includeResource) {
         return relative;
      }

      public String[] getPath(boolean includeResource) {
         throw new UnsupportedOperationException();
      }

      public String getMimeType() {
         throw new UnsupportedOperationException();
      }

      public String getUrl() {
         return fac.getBaseFactoryURL()+this.url.toString();
      }
      
      public boolean isAvailable() {
    	  return true;
      }
   }
   
   public IDecisionCollection getMetadata(IResource r)
         throws DemetriusException {
      throw new UnsupportedOperationException("This implementation does not support resource metadata");
   }

   public IFolder getRoot() {
      throw new UnsupportedOperationException("This implementation does not support folders.");
   }

   public long getSizeLimit() {
      return 0;
   }
   
   public long getSizeLimit(IResource r) {
	   throw new UnsupportedOperationException(
   		"Getting size limit on resource not supported for this resource factory");
   }
   
   public boolean isAvailable(IResource r) {
	   return true;
   }

   public IFolder createFolder(IDecisionCollection dc, IFolder parent)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public IFile addFile(IDecisionCollection dc, IFolder parent, InputStream is)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public void updateResource(IDecisionCollection dc, IResource r)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public IChoiceCollection getResourceMetadata(ResourceType type)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public void delete(IResource r) throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public void move(IResource r, IFolder destinationFolder, boolean overWrite)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }

   public void copy(IResource r, IFolder destinationFolder, boolean overWrite)
         throws DemetriusException {
      throw new UnsupportedOperationException(
            "This implementation is read-only");
   }
}
