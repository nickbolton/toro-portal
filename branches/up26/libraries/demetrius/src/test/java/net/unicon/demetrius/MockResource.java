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

package net.unicon.demetrius;

import java.util.Date;

public class MockResource implements IResource {
    private String name = null;
    private long size = -1;
    private Date dateModified = null;
	
    public MockResource (String name,
    		long size,
    		Date dateModified) {
    	this.name   = name;
    	this.size   = size;
    	this.dateModified = dateModified;
    }

	public IFolder getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}

	public IResourceFactory getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getSize() {
		return size;
	}

	public Date getDateModified() {
		return new Date(dateModified.getTime());
	}

	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getPath(String delimiter, boolean includeResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRelativePath(String delimiter, boolean includeResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getPath(boolean includeResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}
}
