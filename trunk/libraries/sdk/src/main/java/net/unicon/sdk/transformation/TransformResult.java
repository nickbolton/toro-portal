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
package net.unicon.sdk.transformation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

/** TransformResult holds information related to the transform results */ 
public class TransformResult {
    private static File DEBUG_BASE_DIR = null; 
    private static boolean DEBUG_ON    = false; 

    /* Instance Members */
    protected String content = null;

    static {
        try {
            DEBUG_ON = UniconPropertiesFactory.getManager(
                CommonPropertiesType.SERVICE).getPropertyAsBoolean(
                    "net.unicon.sdk.transformation.debug");

            String baseDir = UniconPropertiesFactory.getManager(
                CommonPropertiesType.SERVICE).getProperty(
                    "net.unicon.sdk.transformation.debugBaseDir");

            DEBUG_BASE_DIR = new File(baseDir, "");
            DEBUG_BASE_DIR.mkdirs(); 

        } catch (Exception e) { e.printStackTrace(); }
    }

    /** 
     * TransformResult constructor takes transformation results as a parameter.
     *
     * Results can be represented in various formats, such as XML
     * or comma delimited text.
     *
     * @param content Results from a transformation.
     */
    public TransformResult(String content) {
        this.content = content;    
    }

    /**
     * Provides access to the results of a transformation. 
     *
     * Results can be represented in various formats, such as XML
     * or comma delimited text.
     *
     * @return Transform results as a string.
     */
    public String getContent() throws TransformException {
        try {
            if (DEBUG_ON && (content != null)) {
                writeDebugFile("TransformResult.out", content);
            }
        
            return content;
        } catch (Throwable t) {
            throw new TransformException(t.getMessage(), t);
        }

    } // end getContent

    /* Persists debug data to the file system */
    private void writeDebugFile(String fileName, String content) 
    throws IOException {
        ByteArrayInputStream stream = null; 
        FileOutputStream fout       = null; 

        try {
            // write out the debug file 
            File file = new File(DEBUG_BASE_DIR, fileName);
            file.delete();

            int numRead = 0;
            byte[] b = new byte[4096];
            fout = new FileOutputStream(file);
            stream = new ByteArrayInputStream(content.getBytes());

            while ((numRead = stream.read(b, 0, 4096)) != -1) {
                fout.write(b, 0, numRead);
            }

        } finally {

            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e) {}
            }

        } // end try block

    } // end writeDebugFile 

} // end TransformResult
