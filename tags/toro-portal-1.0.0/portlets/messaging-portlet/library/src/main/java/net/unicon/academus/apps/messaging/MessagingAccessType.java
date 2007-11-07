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

package net.unicon.academus.apps.messaging;

import java.util.HashMap;
import java.util.Map;

import net.unicon.alchemist.access.AccessType;

public class MessagingAccessType extends AccessType {
    
    private static Map types = new HashMap();
    private static MessagingAccessType[] instances = new MessagingAccessType[8];
    
    private static final int ATTACH_VALUE = 1;
    private static final int DELETE_VALUE = 2;
    private static final int COMPOSE_VALUE = 3;
    private static final int SAVE_VALUE = 4;
    private static final int IMPORT_VALUE = 5;
    private static final int EXPORT_VALUE = 6;
    private static final int DETAIL_REPORT_VALUE = 7;
    private static final int VIEW_ALL_VALUE = 8;
    
	
	public static final MessagingAccessType COMPOSE 		
			= new MessagingAccessType("COMPOSE", COMPOSE_VALUE
			        , "This privilege allows the composing and sending of new messages.");
	public static final MessagingAccessType SAVE 			
			= new MessagingAccessType("SAVE", SAVE_VALUE
			        , "This privilege allows the saving of messages.");
	public static final MessagingAccessType IMPORT 			
			= new MessagingAccessType("IMPORT", IMPORT_VALUE
			        , "This privilege allows the importing and sending of new messages.");
	public static final MessagingAccessType EXPORT 			
			= new MessagingAccessType("EXPORT", EXPORT_VALUE
			        , "This privilege allows the exporting of messages.");
	public static final MessagingAccessType DETAIL_REPORT 	
			= new MessagingAccessType("DETAIL_REPORT", DETAIL_REPORT_VALUE
			        , "This priveledge will allows the viewing of the message report (read/unread status).");
	public static final MessagingAccessType VIEW_ALL 	
			= new MessagingAccessType("VIEW_ALL", VIEW_ALL_VALUE
			        , "This privilege allows the viewing of all active messages in the system (regardless of the recipients selected).");
	public static final MessagingAccessType DELETE 	
			= new MessagingAccessType("DELETE", DELETE_VALUE
			        , "This privilege allows the deleting of messages in folders.");
	public static final MessagingAccessType ATTACH 	
			= new MessagingAccessType("ATTACH", ATTACH_VALUE
			        , "This privilege allows the attaching of files to new messages. It does not restrict the downloading of attachments.");
	
	/**  
	 * @param type int value representing the access type.
	 * @return <code>MessagingAccessType</code> that the access id represents
	 */
	public static MessagingAccessType getAccessType(int type){
	    try{
	        MessagingAccessType rslt = instances[type - 1];
	        return rslt;
	    }catch(Exception e){
			throw new IllegalArgumentException("Invalid Access type value. " + type);
		}
	}
	
	/**  
	 * @return an arrayof all the messaging access types <code>MessagingAccessType</code> 
	 */
	public static MessagingAccessType[] getInstances(){
	    return instances;
	}
	
	/**  
	 * @param type String value representing the access Label.
	 * @return <code>MessagingAccessType</code> that the access Label represents
	 */
	public static MessagingAccessType getAccessType(String name){
		return (MessagingAccessType)types.get(name);
	}
	
	private MessagingAccessType(String name, int type, String description){
		super(name, type, description);
		types.put(name, this);
		instances[type - 1] = this;
	}
	
	
}
