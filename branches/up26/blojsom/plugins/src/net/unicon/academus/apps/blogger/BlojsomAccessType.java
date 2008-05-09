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
package net.unicon.academus.apps.blogger;

import java.util.HashMap;
import java.util.Map;

import net.unicon.alchemist.access.AccessType;

public class BlojsomAccessType extends AccessType {
    
    private static Map types = new HashMap();
    private static BlojsomAccessType[] instances = new BlojsomAccessType[4];
    
    // Blojsom Administration Plugins Access Values 
    private static final int EDIT_BLOG_CATEGORIES_VALUE = 1;
    private static final int EDIT_BLOG_ENTRIES_VALUE = 2;
    private static final int FILE_UPLOAD_VALUE = 3;
    private static final int EDIT_BLOG_PROPERTIES_VALUE = 4;

    public static final BlojsomAccessType EDIT_BLOG_CATEGORIES = new BlojsomAccessType("edit_blog_categories", EDIT_BLOG_CATEGORIES_VALUE, "Allows editing of blog categories.");

    public static final BlojsomAccessType EDIT_BLOG_ENTRIES = new BlojsomAccessType("edit_blog_entries", EDIT_BLOG_ENTRIES_VALUE, "Allows editing of blog entries.");

    public static final BlojsomAccessType FILE_UPLOAD = new BlojsomAccessType("file_upload", FILE_UPLOAD_VALUE, "Allows one to upload files to the blog.");

    public static final BlojsomAccessType EDIT_BLOG_PROPERTIES = new BlojsomAccessType("edit_blog_properties", EDIT_BLOG_PROPERTIES_VALUE, "Allows changing of blog properties.");

    /**  
     * @param type int value representing the access type.
     * @return <code>BlojsomAccessType</code> that the access id represents
     */
    public static BlojsomAccessType getAccessType(int type) {
        try {
	        BlojsomAccessType rslt = instances[type - 1];
	        return rslt;
        } catch(Exception e) {
	        throw new IllegalArgumentException("Invalid Access type value. " + type);
        }
    }
	
    /**  
     * @return an arrayof all the blojsom access types <code>BlojsomAccessType</code>
     */
    public static BlojsomAccessType[] getInstances() {
        return instances;
    }
	
    /**  
     * @param type String value representing the access Label.
     * @return <code>BlojsomAccessType</code> that the access Label represents
     */
    public static BlojsomAccessType getAccessType(String name) {
        return (BlojsomAccessType)types.get(name);
    }
	
    private BlojsomAccessType(String name, int type, String description) {
        super(name, type, description);
        types.put(name, this);
	    instances[type - 1] = this;
    }
	
} // end BlojsomAccessType class
