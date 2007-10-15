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
package net.unicon.toro.installer.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class Launcher {

    public static void main(String[] args) {
        boolean guiMode = true;
        boolean autoMode = false;
        if (args.length > 0) {
            if ("text".equals(args[0])) {
                guiMode = false;
            } else if ("text-auto".equals(args[0])) {
                guiMode = false;
                autoMode = true;
            }
        }
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            System.err.println("JAVA_HOME environment variable is not set.");
            System.exit(-1);
        }
        File javaHomeDir = new File(javaHome);
        if (!javaHomeDir.exists()) {
            System.err.println("JAVA_HOME(" + javaHome + ") does not point to a valid directory.");
            System.exit(-1);
        }
        File javaBinDir = new File(javaHomeDir, "bin");
        File jvmExe = new File(javaBinDir, "java");
        if (!jvmExe.exists()) {
            jvmExe = new File(javaBinDir, "java.exe");
            if (!jvmExe.exists()) {
                System.err.println("JVM executable does not exist: JAVA_HOME/bin/java(" + jvmExe.getAbsolutePath() + ")");
                System.exit(-1);
            }
        }
        ProcessBuilder pb;
        if (guiMode) {
            pb = new ProcessBuilder(jvmExe.getAbsolutePath(), "-Djava.awt.headless=false", "-Xmx1024m", "-XX:MaxPermSize=384m", "-classpath", System.getProperty("java.class.path"), "org.tp23.antinstaller.selfextract.SelfExtractor");
        } else if (autoMode) {
            File propertyFile = new File("ant.install.properties");
            if (!propertyFile.exists()) {
                System.err.println("Missing ant.install.properties file.");
                System.err.println("In order to run in auto mode, a property " +
                    "file must exist in the current directory. " +
                    "Try running the installer in non-auto mode and select " +
                    "'Remote Configuration' to generate a property file.");
                System.exit(-1);
            }
            pb = new ProcessBuilder(jvmExe.getAbsolutePath(), "-Djava.awt.headless=true", "-Xmx1024m", "-XX:MaxPermSize=384m", "-classpath", System.getProperty("java.class.path"), "org.tp23.antinstaller.selfextract.SelfExtractor", "text-auto");
        } else {
            pb = new ProcessBuilder(jvmExe.getAbsolutePath(), "-Djava.awt.headless=true", "-Xmx1024m", "-XX:MaxPermSize=384m", "-classpath", System.getProperty("java.class.path"), "org.tp23.antinstaller.selfextract.SelfExtractor");
        }
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (!guiMode) {
                Writer writer = new OutputStreamWriter(p.getOutputStream());
                StandardInputWorker stdinWorker = new StandardInputWorker(writer);
                Thread stdinThread = new Thread(stdinWorker);
                stdinThread.start();
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = reader.readLine()) != null) {
                  System.out.println(line);
                }
                stdinWorker.stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseInstallerJar() {
        String classpathSep = System.getProperty("path.separator");
        String[] paths = System.getProperty("java.class.path").split(classpathSep);
        String retVal = null;
        for (int i=0; retVal == null && i<paths.length; i++) {
            if (paths[i].indexOf("toro-installer") >= 0) {
                retVal = paths[i];
            }
        }
        if (retVal == null) {
            System.err.println("Failed to parse classpath to find installer jar (" + classpathSep + ", " + System.getProperty("java.class.path"));
        }
        return retVal;
    }
    
    private static class StandardInputWorker implements Runnable {
        private PrintWriter writer;
        private boolean running = true;
       
        public StandardInputWorker(Writer writer) {
            this.writer = new PrintWriter(writer);
        }
        
        public void stop() {
            running = false;
        }
        
        public void run() {
            LineNumberReader stdinReader = new LineNumberReader(new InputStreamReader(System.in));
            String line;
            try {
                while (running && (line = stdinReader.readLine()) != null) {
                  writer.println(line);
                  writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
