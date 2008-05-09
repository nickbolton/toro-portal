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

public class UserFolderHistory {

    // Static Members.
    private static final int MAX_LOCATIONS = 10;

    // Instance Members.
    private LinkedList locations;
    private int position;
    private boolean folderOpen = true;

    /*
     * Public API.
     */

    public UserFolderHistory(Location start) {

        // Assertions.
        if (start == null) {
            String msg = "Argument 'start' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.locations = new LinkedList();
        this.locations.add(start);
        this.position = 0;

    }

    public Location getLocation() {
        return (Location) locations.get(position);
    }

    public void setLocation(Location f) {

        // Assertions.
        if (f == null) {
            String msg = "Argument 'f [Location]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Clear anything ahead of our current position.
        while (locations.size() > position + 1) {
            locations.removeLast();
        }

        // Add the new location.
        locations.add(f);

        // Move the cursor.
        ++position;

        // Shift down if we've grown too big.
        if (locations.size() > MAX_LOCATIONS) {
            locations.removeFirst();
            --position;
        }

    }

    public boolean hasBack() {
        return position > 0;
    }

    public Location goBack() {

        // Assertions.
        if (position == 0) {
            String msg = "UserHistory is already at the first position.";
            throw new IndexOutOfBoundsException(msg);
        }

        // Move the cursor.
        --position;

        return (Location) locations.get(position);

    }

    public boolean hasForward() {
        return position < locations.size() - 1;
    }

    public Location goForward() {

        // Assertions.
        if (!(position < locations.size() - 1)) {
            String msg = "UserHistory is already at the last position.";
            throw new IndexOutOfBoundsException(msg);
        }

        // Move the cursor.
        ++position;

        return (Location) locations.get(position);

    }
    
    public void closeFolder(){
        this.folderOpen = false;
    }
    
    public void openFolder(){
        this.folderOpen = true;
    }
    
    public boolean isFolderOpen(){
        return this.folderOpen;
    }

}