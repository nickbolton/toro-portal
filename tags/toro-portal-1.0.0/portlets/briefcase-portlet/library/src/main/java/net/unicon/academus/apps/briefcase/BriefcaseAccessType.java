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

import java.util.HashMap;
import java.util.Map;

import net.unicon.alchemist.access.AccessType;

public class BriefcaseAccessType extends AccessType {

	private static Map types = new HashMap();
	private static BriefcaseAccessType[] instances = new BriefcaseAccessType[5];
	
	private static final int VIEW_VALUE = 1;
	private static final int ADD_VALUE = 2;
	private static final int DELETE_VALUE = 3;
	private static final int EDIT_VALUE = 4;
	private static final int SHARE_VALUE = 5;
	
	public static final BriefcaseAccessType VIEW = new BriefcaseAccessType("VIEW", VIEW_VALUE
	        , "This privilege allows read access.");
	public static final BriefcaseAccessType ADD = new BriefcaseAccessType("ADD", ADD_VALUE
	        , "This privilege allows access to create folders and add files.");
	public static final BriefcaseAccessType DELETE = new BriefcaseAccessType("DELETE", DELETE_VALUE
	        , "This privilege allows access to delete files and folders.");
	public static final BriefcaseAccessType EDIT = new BriefcaseAccessType("EDIT", EDIT_VALUE
	        , "This privilege allows access to edit folder and file names.");
	public static final BriefcaseAccessType SHARE = new BriefcaseAccessType("SHARE", SHARE_VALUE
	        , "This privilege allows access to share folders.");
	
	/**  
	 * @param type int value representing the access type.
	 * @return <code>AbstractAccessType</code> that the access type represents
	 */
	public static BriefcaseAccessType getAccessType(int type){
	    try{
	        BriefcaseAccessType rslt = instances[type - 1];
	        return rslt;
	    }catch(Exception e){
	        throw new IllegalArgumentException("Illegal Access type " + type);
	    }	    
	}
	
	public static BriefcaseAccessType[] getInstances(){
	    return instances;
	}
	
	public static BriefcaseAccessType getAccessType(String name){
		return (BriefcaseAccessType)types.get(name);
	}
	
	private BriefcaseAccessType(String name, int type, String description){
		super(name, type, description);
		types.put(name, this);
		instances[type - 1] = this;
	}
	
}
