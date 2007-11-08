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

public interface IResource {
	
    
	/**
	 * Returns the parent folder of the resource.
	 * @return returns the <code>IFolder</code> object that 
	 * represents the parent of the resource
	 */
	public IFolder getParent();
	
	/**
	 * Returns the name of the resource
	 * @return gets the name of the resource
	 */
	public String getName();
	
	/**
	 * Returns the owner of the resource.
	 * @return get the <code>IResourceFactory</code> owner of the resource 
	 */
	public IResourceFactory getOwner();
	
	/**
	 * Returns the type of the given resource
	 * @return gets the type of the given resource 
	 */
	public ResourceType getType();
	
	/**
	 * @return
	 */
	public long getSize();
	
	/**
	 * @return
	 */
	public Date getDateModified();
	
	/**
	 * @return
	 */
	public boolean isHidden();
	
	/**
	 * @param delimiter
	 * @param includeResource
	 * @return
	 */
	public String getPath(String delimiter, boolean includeResource);
	
	/**
	 * @param delimiter
	 * @param includeResource
	 * @return
	 */
	public String getRelativePath(String delimiter, boolean includeResource);
	
	/**
	 * @param includeResource
	 * @return
	 */
	public String[] getPath(boolean includeResource);	
	
	/**
	 * @return
	 */
	public String getMimeType();
	
	/**
	 * Returns the URL representation of the resource
	 * @return
	 */
	public String getUrl();
	
	/**
	 * Returns if this resource is available to the caller.
	 */
	public boolean isAvailable();
}
