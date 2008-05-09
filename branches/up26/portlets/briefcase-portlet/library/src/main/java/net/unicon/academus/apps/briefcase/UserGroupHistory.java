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

import java.util.LinkedList;

import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusGroup;

public class UserGroupHistory {

    // Static Members.
    //private static final int MAX_LOCATIONS = 10;

    // Instance Members.
    private LinkedList locations;
    private int position;
    private boolean showMembers;
    private LinkedList path;

    /*
     * Public API.
     */

    public UserGroupHistory() {

        // Instance Members.
        this.locations = new LinkedList();
        //this.locations.add("");
        this.position = -1;
        this.showMembers = true;
        this.path = new LinkedList();
        this.path.add("");

    }

    public IAcademusGroup getLocation() {
        return (IAcademusGroup) locations.get(position);
    }

    public void setLocation(IAcademusGroup g) {

        // Assertions.
        if (g == null) {
            String msg = "Argument 'g [IAcademusGroup]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Clear anything ahead of our current position.
        while (locations.size() > position + 1) {
            locations.removeLast();
        }

        // Add the new location.
        locations.add(g);

        // Move the cursor.
        ++position;     

    }

    public boolean hasBack() {
        return position > 0;
    }
    
    public boolean hasGroup() {
        return position >= 0;
    }
    
    public IAcademusGroup goBack() {
        // Assertions.
        if (position == 0) {
            String msg = "UserGroupHistory is already at the first position.";
            throw new IndexOutOfBoundsException(msg);
        }

        // Move the cursor.
        --position;

        return (IAcademusGroup) locations.get(position);

    }

    public String getCrumbTrail(String delimiter){
        StringBuffer strb = new StringBuffer();
        for(int i = 0; i <= position; i++){
            try {
                strb.append(((IAcademusGroup)locations.get(i)).getName());
                if(i < position){
                    strb.append(delimiter);
                }
            } catch (AcademusFacadeException e) {
                throw new RuntimeException("UserGroupHistory was not able to " +
                		"get the group name.", e);
            }
        }
        return strb.toString();
    } 
    
    public boolean showMembers(){
        return showMembers;
    }
    
    public void setShowMembers(boolean value){
        showMembers = value;
    }
    
    public void clear(){
        this.locations.clear();
        this.position = -1;
    }
    
    public IAcademusGroup[] getLocations(){
        while (locations.size() > position + 1) {
            locations.removeLast();
        }
        
        return (IAcademusGroup[])locations.toArray(new IAcademusGroup[0]);
    }
    
    public String getPath(){
    	if(path.size() > 1){
    		return (String)this.path.getLast();
    	}
    	return "";
    }
    
    public String goBackPath(){
    	if(path.size() > 1){
    		this.path.removeLast();
    		return (String)this.path.getLast();
    	}
    	return "";
    }
    public void setPath(String path){
        this.path.add(path);
    }
    

}