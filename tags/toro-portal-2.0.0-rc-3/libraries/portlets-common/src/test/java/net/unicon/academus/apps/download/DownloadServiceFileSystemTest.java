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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import net.unicon.academus.apps.download.IDownloadService.NameResourcePair;

import junit.framework.TestCase;

public class DownloadServiceFileSystemTest extends TestCase {

   private DownloadServiceFileSystem ds;
   private String resourceId;
   private static final String testString = "Hello, World!\n";

   protected void setUp() throws Exception {
       /*
      super.setUp();
      this.ds = new DownloadServiceFileSystem();

      byte[] b = testString.getBytes();
      String file = "hello.txt";
      InputStream is = new ByteArrayInputStream(b);
      try {
         this.resourceId = ds.registerResource(file, is);
      } catch (IOException e) {
         e.printStackTrace();
         fail(e.getMessage());
      }
      */
   }
   
   protected void tearDown() throws Exception {
      //ds.unregisterResource(this.resourceId);
      //super.tearDown();
   }

   /*
    * Test method for 'net.unicon.academus.apps.download.DownloadServiceFileSystem.registerResource(String, InputStream)'
    */
   public void testRegisterResource() {
      //assertNotNull(this.resourceId);
   }

   /*
    * Test method for 'net.unicon.academus.apps.download.DownloadServiceFileSystem.unregisterResource(String)'
    */
   public void testUnregisterResource() {
      //assertTrue(ds.unregisterResource(resourceId));
   }

   /*
    * Test method for 'net.unicon.academus.apps.download.DownloadServiceFileSystem.getResource(String)'
    */
   public void testGetResource() throws IOException {
       /*
      NameResourcePair npr = ds.getResource(resourceId);
      assertNotNull(npr);
      assertTrue("File size not greater than 0", npr.getSize() > 0);
      InputStream is = null;
      String rslt = null;
      try {
	      is = npr.getResource();
	      assertNotNull(is);
	
         StringBuffer sb = new StringBuffer();
	      byte[] buf = new byte[256];
	      int r = 0;
         while ((r = is.read(buf)) >= 0) {
            sb.append(new String(buf, 0, r));
         }
         rslt = sb.toString();
      } finally {
         is.close();
      }
      assertEquals(testString, rslt);
      */
   }

}
