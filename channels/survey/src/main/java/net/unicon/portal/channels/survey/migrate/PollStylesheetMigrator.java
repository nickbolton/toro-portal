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

package net.unicon.portal.channels.survey.migrate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is a migration utility for the Poll channel. It updates 
 * existing Poll stylesheets that have been generated prior to Academus 2.0.
 * 
 * <p>The modifications are necessary to enable the Poll channel to function 
 * correctly with all open survey distributions. Prior to Academus 2.0, only 
 * the most recent distribution of each survey was visible by the Poll 
 * channel.</p> 
 */
public class PollStylesheetMigrator {
    
    private static final int POLL_PEEPHOLE = 0;
    private static final int POLL_RESULTS  = 1;

    /**
     * Executes a migration utility to update Poll stylesheets created prior
     * to Academus 2.0.
     * Requires two command-line arguments specifying the locations of required
     * directories.
     * 
     * @param args a String array containing the paths for required directories.
     *   <p>args[0]: path to the existing Survey data repository.</p>
     *   <p>args[1]: path to Academus 2.0 version of Poll stylesheets.</p> 
     */
    public static void main(String[] args) {

        if (args.length == 2) {
            
            runMigration(args);

        } else {
            
            usage();
            
        }
    }
    
    /**
     * Displays the proper usage of this migration utility.
     * 
     * <br/>Usage:<br/>
     *   <p>java -cp &lt;classpath&gt; 
     *   net.unicon.portal.channels.survey.migrate.PollStylesheetMigrator 
     *   [arguments]</p>
     *   
     *   [arguments] include: 
     *   
     *   <p>argument 1: The directory that contains the pre-existing survey 
     *               data. This directory is specified by the survey.repository 
     *               property in properties/rad.properties.</p>
     *                
     *   <p>argument 2: The directory that contains the new versions of the 
     *               Poll stylesheets.</p>
     */
    public static void usage() {
        System.out.println("Usage for " + PollStylesheetMigrator.class.getName()
                + ":");
        System.out.println("\tjava -cp <classpath> "
                + PollStylesheetMigrator.class.getName() + " [arguments] ");
        System.out.println();
        System.out.println("[arguments] include: ");

        System.out.println("argument 1: The directory that contains the"
                + " pre-existing survey data. This directory is specified by"
                + " the survey.repository property in"
                + " properties/rad.properties.");
        System.out.println("\nargument 2: The directory that contains the new"
                + " versions of the Poll stylesheets.");
    }
    
    /**
     * Performs the migration of existing Academus Poll files created prior to
     * Academus 2.0. 
     * 
     * @param dirPaths a String array with two elements:
     *   <p>args[0]: path to the existing Survey data repository.</p>
     *   <p>args[1]: path to Academus 2.0 version of Poll stylesheets.</p>
     */
    private static void runMigration(String[] dirPaths) {
        
        // Assertions
        if (dirPaths == null) {
            String msg = "Argument 'dirPaths' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (dirPaths.length != 2) {
            String msg = "Argument 'dirPaths' must contain two elements.";
            throw new IllegalArgumentException(msg);            
        }
        
        boolean success = false;
        
        System.out.println("Starting migration of Poll stylesheets from"
                + " previous versions to Academus 2.0...");
        
        System.out.println("\n(args[0]) Survey Respository = " 
                + dirPaths[0]);
        System.out.println("(args[1]) Modified stylesheet directory = " 
                + dirPaths[1]);

        try {

            File dir = new File(dirPaths[0]);
            
            if (dir.isDirectory()) {
                
                String[] newContents = getModifiedStylesheets(dirPaths[1]);

                // Get directory names for any users that have created surveys
                File[] userDirs = dir.listFiles();
                
                if (userDirs.length != 0) {

                    File[] files = null;
                    
                    // For each user directory, modify the files in each
                    for (int x = 0; x < userDirs.length; x++) {
                        
                        files = userDirs[x].listFiles();
                        if (files != null) {
                            modifyFiles(files, newContents);
                        }
                        
                    }

                } else {

                    System.out.println("\nThere are no pre-existing surveys"
                            + " on your system that need to be migrated.");

                }
                
                success = true;

            } else { 
                // Directory does not exist. By default, when Academus
                // deploys, this directory is created. So there must be 
                // a problem with the argument.
                System.out.println("\nThe specified Survey Repository"
                        + " directory does not exist, or is not a"
                        + " directory.");
                
                success = false;
            }

        } catch (FileNotFoundException fnf) {
            
            System.out.println(fnf.getMessage());
            success = false;
            
        } catch (IOException ioe) {    
            
            System.out.println(ioe.getMessage());
            success = false;       
            
        }

        
        if (success) {
            System.out.println("\n\n*** Poll Stylesheet Migration Tool"
                    + " COMPLETED SUCCESSFULLY ***");
        } else {
            System.out.println("\n\n*** Poll Stylesheet Migration Tool FAILED."
                    + " Verify the directory paths... ***\n");
            usage();
        }
    }

    /**
     * Retrieves the contents of Poll stylesheets that have been modified
     * in Academus 2.0.
     *  
     * @param directory a String path to a directory that contains the modified
     *   stylesheets.
     * @return a String array that contains a String with the contents of each
     *   modified stylesheet.
     * <p>index 0 - A String representing the contents of poll_peephole.xsl.</p>
     * <p>index 1 - A String representing the contents of poll_results.xsl.</p> 
     * @throws FileNotFoundException if the named file does not exist, is a 
     *   directory rather than a regular file, or for some other reason cannot 
     *   be opened for reading.
     * @throws IOException if an I/O error occurs
     */
    private static String[] getModifiedStylesheets(String directory) 
            throws FileNotFoundException, IOException {
        
        // Assertions
        if (directory == null) {
            String msg = "Argument 'directory' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        String[] rslt = new String[2];
        
        File newStylesheet = new File(directory + "/poll_peephole.xsl");

        rslt[POLL_PEEPHOLE] = getFileContents(newStylesheet);

        newStylesheet = new File(directory + "/poll_results.xsl");

        rslt[POLL_RESULTS] = getFileContents(newStylesheet);

        newStylesheet = null;
        
        return rslt;
        
    }
    
    /**
     * Retrieves a String containing the contents of a specified file.
     * Assumes the file being read contains a maximum of 12K characters.
     * 
     * @param fileName a File to be read
     * @return a String containing the contents of the specified file
     * @throws FileNotFoundException if the named file does not exist, is a 
     *   directory rather than a regular file, or for some other reason cannot 
     *   be opened for reading.
     * @throws IOException if an I/O error occurs
     */
    private static String getFileContents(File fileName)
            throws FileNotFoundException, IOException {

        // Assertions
        if (fileName == null) {
            String msg = "Argument 'fileName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        char[] buffer = new char[12000];
        FileReader fr = null;
        try {            
            fr = new FileReader(fileName);
            fr.read(buffer);
        } finally {
            if (fr != null) {
                fr.close();
            }
        }
        
        return String.valueOf(buffer).trim();

    }
    
    /**
     * Checks the given group of files and modifies those that match certain 
     * filename conventions.
     * All filenames containing 'poll_peephole.xsl' or 'poll_results.xsl'
     * are modified.
     * 
     * @param files an array of Files to be checked for modification
     * @param newContents a String array that contains the contents of the
     *   modified stylesheets.
     *   <p>newContents[0] - A String representing the contents of 
     *   poll_peephole.xsl.</p>
     *   <p>newContents[1] - A String representing the contents of 
     *   poll_results.xsl.</p>
     * @throws FileNotFoundException if the named file does not exist, is a 
     *   directory rather than a regular file, or for some other reason cannot 
     *   be opened for reading.
     * @throws IOException if an I/O error occurs
     */
    private static void modifyFiles(File[] files, String[] newContents)
            throws FileNotFoundException, IOException {
        
        // Assertions
        if (files == null) {
            String msg = "Argument 'files' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (newContents == null) {
            String msg = "Argument 'newContents' cannot be null.";
            throw new IllegalArgumentException(msg);
        }        
        if (newContents.length != 2) {
            String msg = "Argument 'newContents' must contain two elements.";
            throw new IllegalArgumentException(msg);            
        }
        
        for (int y = 0; y < files.length; y++) {

            if (files[y].getName().matches(".*poll_peephole.xsl")) {

                updateContents(files[y], newContents[POLL_PEEPHOLE]);

            } else if (files[y].getName().matches(".*poll_results.xsl")) {

                updateContents(files[y], newContents[POLL_RESULTS]);

            }
        }
    }

    /**
     * Overwrites the contents of the given file with new contents provided.
     * Saves the xsl:include information from the old file and inserts it into
     * the new contents for compatibility.
     * 
     * @param oldFile a File to be modified
     * @param newContents a String containing the contents of the Academus 2.0 
     *   version of the stylesheet.
     * @throws FileNotFoundException if the named file does not exist, is a 
     *   directory rather than a regular file, or for some other reason cannot 
     *   be opened for reading.
     * @throws IOException if an I/O error occurs
     */
    private static void updateContents(File oldFile, String newContents) 
            throws FileNotFoundException, IOException {
        
        // Assertions
        if (oldFile == null) {
            String msg = "Argument 'oldFile' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (newContents == null) {
            String msg = "Argument 'newContents' cannot be null.";
            throw new IllegalArgumentException(msg);
        } 
        
        // Get the xsl include file statement from old file
        String oldContents = getFileContents(oldFile);
        int start = oldContents.indexOf("<xsl:include href=");
        int end = oldContents.indexOf("/>", start) + 2;
        String includeInfo = oldContents.substring(start, end);

        // Modify the new xsl file with old include statement
        StringBuffer tempContents = new StringBuffer(newContents);

        int replaceIndex = tempContents.toString().indexOf("insertinclude");

        tempContents.replace(replaceIndex, replaceIndex + 13, includeInfo);

        // Update file with new contents
        FileWriter fw = null;
        try {    
            
            fw = new FileWriter(oldFile);
            fw.write(tempContents.toString());
            
            System.out.println("Modified File: " + oldFile.getCanonicalPath());

        } finally {
          if (fw != null) {
              fw.close();
          }
        }
    }

}
