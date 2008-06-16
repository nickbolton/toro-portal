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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jetspeed.util.OverwriteProperties;

/**
 * EnsureSecurityContextsHaveCaching is an Ant task that speaks for itself.
 */
public class EnsureSecurityContextsHaveCaching {
    
    private String uPortalHome;

    /**
     * Constructor of the JavaVersionTask class.
     */
    public EnsureSecurityContextsHaveCaching() {
        super();
    }

    /**
     * Execute the task.
     */
    public void execute() {
        if (uPortalHome == null) {
            throw new RuntimeException("Missing uPortalHome attribute.");
        }
        
        File securityContextFile = new File(uPortalHome, "/WEB-INF/classes/properties/security.properties");
        
        if (!securityContextFile.exists()) {
            throw new RuntimeException("uPortalHome path does not contain the security context file: " + securityContextFile.getAbsolutePath());
        }
        
        try {
            FileInputStream is = new FileInputStream(securityContextFile);
            Properties p = new Properties();
            p.load(is);
            is.close();
            
            String root = p.getProperty("root");
            if (root == null) {
                throw new RuntimeException("No root context defined in file: " + uPortalHome);
            }
            
            if ("org.jasig.portal.security.provider.CacheSecurityContextFactory".equals(root)) return;
            
            if ("org.jasig.portal.security.provider.SimpleSecurityContextFactory".equals(root)) {
                p.setProperty("root", "org.jasig.portal.security.provider.CacheSecurityContextFactory");
                save(p, securityContextFile);
                return;
            }
            
            boolean saveFile = false;
            String name;
            for (Object ob : p.keySet()) {
                name = (String)ob;
                if (name.indexOf("root.") == 0) {
                    if ("org.jasig.portal.security.provider.SimpleSecurityContextFactory".equals(p.getProperty(name)) ||
                        "org.jasig.portal.security.provider.SimpleLdapSecurityContextFactory".equals(p.getProperty(name)) ||
                        "edu.columbia.ais.portal.security.provider.CasSecurityContextFactory".equals(p.getProperty(name))) {
                        
                        List<String> l = getSubContexts(name, p);
                        Iterator<String> itr = l.iterator();
                        boolean isCached = false;
                        while (!isCached && itr.hasNext()) {
                            isCached = "org.jasig.portal.security.provider.CacheSecurityContextFactory".equals(p.getProperty(itr.next()));
                        }
                        if (!isCached) {
                            if (p.getProperty(name+".cache") == null) {
                                p.setProperty(name+".cache", "org.jasig.portal.security.provider.CacheSecurityContextFactory");
                            } else {
                                p.setProperty(name+".toro_cache", "org.jasig.portal.security.provider.CacheSecurityContextFactory");
                            }
                            saveFile = true;
                        }
                    }
                }
            }
            
            if (saveFile) {
                save(p, securityContextFile);
            }
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + uPortalHome);
        } catch (IOException e) {
            throw new RuntimeException("Failed reading file: " + uPortalHome, e);
        }
    }
    
    private List<String> getSubContexts(String context, Properties p) {
        List<String> l = new LinkedList<String>();
        String name;
        for (Object ob : p.keySet()) {
            name = (String)ob;
            if (name.indexOf(context+".") == 0 && name.substring(context.length()+1).indexOf('.') < 0) {
                l.add(name);
            }
        }
        return l;
    }
    
    private void save(Properties p, File securityContextFile) {
        
        Utils.instance().backupFile(securityContextFile, true);
        
        FileOutputStream os;
        try {
            File mergeFile = new File(securityContextFile.getAbsoluteFile()+".merge");
            os = new FileOutputStream(mergeFile);
            p.store(os, "modified by Toro Installer to add caching contexts.");
            os.close();
            
            OverwriteProperties overwriter = new OverwriteProperties();
            overwriter.setBaseProperties(securityContextFile);
            overwriter.setProperties(mergeFile);
            overwriter.setIncludeRoot(new File("."));
            overwriter.setVerbose(false);
            overwriter.execute();
            
            mergeFile.delete();

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open file: " + securityContextFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + securityContextFile.getAbsolutePath());
        }
    }
    
    public String getUPortalHome() {
        return uPortalHome;
    }

    public void setUPortalHome(String portalHome) {
        uPortalHome = portalHome;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: EnsureSecurityContextsHaveCaching <uportal-webapp-dir>");
            System.exit(-1);
        }
        EnsureSecurityContextsHaveCaching eschc = new EnsureSecurityContextsHaveCaching();
        eschc.setUPortalHome(args[0]);
        eschc.execute();
    }
}
