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

package net.unicon.academus.apps.briefcase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.civis.ICivisFactory;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.fac.shared.SharedResourceFactory;
import net.unicon.warlock.IApplicationContext;

public class BriefcaseApplicationContext implements IApplicationContext {

    // Instance Members.
    private final Drive[] drives;
    private final EncryptionService encryptionService;
    private final String name;
    private final ICivisFactory[] factories;
    private final Map restrictors; 

    /*
     * Public API.
     */

    public BriefcaseApplicationContext(Drive[] drives, String name
            , ICivisFactory[] factories, Map restrictors) {

        // Assertions.
        if (drives == null) {
            String msg = "Argument 'drives' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (drives.length == 0) {
            String msg = "Argument 'drives' must contain at least one element.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.drives = new Drive[drives.length];
        System.arraycopy(drives, 0, this.drives, 0, drives.length);
        this.encryptionService = EncryptionService.getInstance(name);
        this.name = name;
        this.factories = new ICivisFactory[factories.length];
        System.arraycopy(factories, 0, this.factories, 0, factories.length);
        this.restrictors = restrictors;

        if (this.encryptionService == null)
            throw new IllegalArgumentException("Unable to acquire EncryptionService for name: "+name);

    }

    public Drive getDrive(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Drive rslt = null;

        Iterator it = Arrays.asList(drives).iterator();
        while (it.hasNext()) {
            Drive d = (Drive) it.next();
            if (d.getHandle().equals(handle)) {
                rslt = d;
                break;
            }
        }

        // Make sure we found one.
        if (rslt == null) {
            String msg = "Drive not found:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return rslt;

    }

    public Drive[] getDrives() {
        Drive[] rslt = new Drive[drives.length];
        System.arraycopy(drives, 0, rslt, 0, drives.length);
        return rslt;
    }
    
    public IAccessEntry[] getSharedEntries(IFolder target, String username, Drive drive){
    	
    	IResourceFactory shared = new SharedResourceFactory(target, username);
    	
    	return drive.getBroker().getEntries(shared);
    }
    
    public Drive getDrive(IResource r){
    	AccessBroker broker = null;
        IAccessEntry[] entries = null;
        
        for(int i = 0; i < drives.length; i++){
        	broker = drives[i].getBroker();
        	entries = broker.getEntries(r.getOwner());        	
        	if(entries.length > 0){
        		return drives[i];
        	}
        }
        
        throw new RuntimeException("Drive not found");
    	
    }
    
    public EncryptionService getEncryptionService(){
    	return this.encryptionService;
    }
    
    public String getName(){
    	return this.name;
    }
    
    public ICivisFactory[] getCivisFactories() { 
        return this.factories; 
    }
    
    public Map getGroupRestrictors() { return this.restrictors; } 
}
