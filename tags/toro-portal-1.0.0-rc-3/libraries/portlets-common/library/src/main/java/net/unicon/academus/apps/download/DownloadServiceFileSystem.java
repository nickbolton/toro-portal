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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * IDownloadService implementation that utilizes the file system for storage.
 * 
 * This implementation is multi-box compatible given that all participating 
 * servers have the same repositoryPath.
 * 
 * @author eandresen
 */
public class DownloadServiceFileSystem implements IDownloadService {
   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(DownloadServiceFileSystem.class);
   
   private static final String repositoryPath =
         DownloadServiceFactory.getProperty(
               DownloadServiceFileSystem.class,
               "directory");
   private static final String metaSuffix = ".meta";

   private final File repository;
   private final long hostId;

   public DownloadServiceFileSystem() {
      this.repository = new File(repositoryPath);
      
      if (!repository.exists()) {
         repository.mkdirs();
      }

      if (!repository.exists()) {
         throw new IllegalStateException(
               "Unable to create repository directory: "+repositoryPath);
      }

      if (!repository.canWrite()) {
         throw new IllegalStateException(
               "Unable to write to repository directory: "+repositoryPath);
      }

      if (!repository.canRead()) {
         throw new IllegalStateException(
               "Unable to read from repository directory: "+repositoryPath);
      }

      this.hostId = generateHostIdentifier();
      
      cleanupRepository();
   }

   private void cleanupRepository() {
      final String prefix = String.valueOf(this.hostId)+".";
      File[] oldFiles = repository.listFiles(
            new FilenameFilter() {
		         public boolean accept(File dir, String name) {
		            return name.startsWith(prefix);
		         }
            });

      for (int i = 0; i < oldFiles.length; i++) {
         log.debug("Deleting old resource: "+oldFiles[i].getName());
         oldFiles[i].delete();
      }
   }

   public String registerResource(String name, InputStream resource) throws IOException {
      String id = generateIdentifier(name);
      storeResource(name, id, resource); 
      return id;
   }

   public boolean unregisterResource(String id) {
      validateIdentifier(id);
      return deleteResource(id);
   }

   public NameResourcePair getResource(String id) throws IOException {
      validateIdentifier(id);
      return findResource(id);
   }

   private NameResourcePair findResource(String id) throws IOException {
      NameResourcePair nrp = null;
      File rfile = new File(repository, id);
      File mfile = new File(repository, id+metaSuffix);

      if (rfile.exists() && mfile.exists()) {
         Properties p = new Properties();

         InputStream in = null;
         try {
            in = new FileInputStream(mfile);
            p.load(in);
         } finally {
            if (in != null) try { in.close(); } catch (IOException ex) {}
         }

         String fname = p.getProperty("name");
         nrp = new NameResourcePairImpl(fname, rfile);
      }
      
      return nrp;
   }
   
   private static final Pattern idPattern =
      Pattern.compile("[0-9]+\\.[0-9a-f-]+\\.[0-9a-f-]+\\.[0-9a-f-]+");

   /**
    * Validate the syntax of the globally unique identifier.
    * @param id Identifier to validate
    */
   private void validateIdentifier(String id) {
      boolean valid = idPattern.matcher(id).matches();

      if (!valid)
         throw new IllegalArgumentException(
               "The provided resource identifier is invalid: "+id);
   }
   
   /**
    * Generate a globally unique identifier for the named resource.
    * @param name Name of the resource to generate an identifier for
    * @return A globally unique identifier for the named resource.
    */
   private String generateIdentifier(String name) {
      StringBuffer rslt = new StringBuffer();
      
      // the UID class is guaranteed to be unique within the same host.
      // Coupled with a host unique identifier, it becomes globally unique.
      rslt.append(hostId).append('.');
      rslt.append(new UID().toString().replace(':', '.'));
      
      if (log.isDebugEnabled()) {
         log.debug("Resource '"+name+"' unique identifier: "+rslt);
      }
      return rslt.toString();
   }

   /**
    * Store the resource and metadata.
    * @param fname Resources name
    * @param id Unique identifier for the resource
    * @param resource InputStream containing the resource
    * @throws IOException if unable to store the resource
    */
   private void storeResource(String fname, String id, InputStream resource) throws IOException {
      File rfile = new File(repository, id);
      File mfile = new File(repository, id+metaSuffix);

      // First create the metadata file
      Properties p = new Properties();
      p.setProperty("name", fname);
      p.setProperty("creationTime", String.valueOf(System.currentTimeMillis()));
      OutputStream os = null;
      try {
         os = new FileOutputStream(mfile);
	      p.store(os, null);
      } finally { 
         if (os != null) try { os.close(); } catch (IOException ex) {}
         os = null;
      }
      
      // Now copy the resource
      byte[] b = new byte[4096];
      int r = 0;
      try {
         os = new FileOutputStream(rfile);
         while ((r = resource.read(b)) >= 0) {
            os.write(b, 0, r);
         }
      } finally {
         if (os != null) {
	         try { os.close(); } catch (IOException ex) {}
         }
         try { resource.close(); } catch (IOException ex) {}
      }
   }
   
   /**
    * Delete the resource identified by <code>id</code>.
    * @param id Globally unique identifier of the resource to delete.
    * @return true if successfully deleted.
    */
   private boolean deleteResource(String id) {
      File rfile = new File(repository, id);
      File mfile = new File(repository, id+metaSuffix);

      mfile.delete();
      return rfile.delete();
   }
   
   public static class NameResourcePairImpl implements NameResourcePair {
      private String name;
      private File resource;

      private NameResourcePairImpl(String name, File resource) {
          if (resource == null)
              throw new IllegalArgumentException("Argument 'resource' cannot be null.");

          this.name = name;
          this.resource = resource;
      }

      public String getName() {
          return this.name;
      }

      public InputStream getResource() throws IOException {
          return new FileInputStream(resource);
      }

      public long getSize() {
         return resource.length();
      }
   } 

   /**
    * Generate a unique host identifier using the host's IP address.
    * @return unique host identifier
    */
   private static long generateHostIdentifier() {
      long hostId;
      InetAddress lhost = null;
      try {
         lhost = InetAddress.getLocalHost();
         byte[] addr = lhost.getAddress();
         hostId = ((addr[0]&0xff) << 24) +
			         ((addr[1]&0xff) << 16) +
			         ((addr[2]&0xff) << 8) +
			         (addr[3]&0xff);
      } catch (UnknownHostException e) {
         e.printStackTrace();
         hostId = -1;
      }
      if (log.isDebugEnabled()) {
	      log.debug("System host identifier: "+hostId);
      }
      return hostId;
   }

   public static void main(String[] args) throws Exception {
      DownloadServiceFileSystem ds = new DownloadServiceFileSystem();
      System.out.println("Identifier: "+generateHostIdentifier());
      String id = ds.generateIdentifier(args[0]);
      System.out.println("Resource Identifier: "+id);
      try {
         ds.validateIdentifier(id);
         System.out.println("Identifier validated.");
      } catch (IllegalArgumentException ex) {
         System.out.println("Identifier invalid!");
      }
   }
}
