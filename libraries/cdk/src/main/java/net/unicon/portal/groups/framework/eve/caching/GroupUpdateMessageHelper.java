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
package net.unicon.portal.groups.framework.eve.caching;

import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.services.GroupService;


public class GroupUpdateMessageHelper implements TextMessageHelper {
    
    private static final Log log =
        LogFactory.getLog(GroupUpdateMessageHelper.class);
    private String groupKey = null;
    private ActionType action = null;
    private String serviceName = null;
    
    /** Constructor */
    public GroupUpdateMessageHelper () {
    }
    
    public String getGroupKey() {
        return groupKey;
    }
    
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
    
    public ActionType getAction() {
        return action;
    }
    
    public void setAction(ActionType action) {
        this.action = action;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * Returns a text Message string of the helper object.
     * @return text message - a text message representation of the helper object.
     */
    public String getTextMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(action.getLabel()).append(MessageTypeConstants.SEPERATOR);
        sb.append(MessageTypeConstants.GROUP_DEL).append(groupKey);
        sb.append(MessageTypeConstants.SEPERATOR);
        sb.append(MessageTypeConstants.SERVICE_NAME_DEL).append(serviceName);
        String ret = sb.toString();
        
        if (log.isDebugEnabled()) {
            log.debug("GroupUpdateMessageHelper::handleTextMessage() sending text message: " + ret); 
        }
        return ret;
    }
    
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage) {
        if (log.isDebugEnabled()) {
            log.debug("GroupUpdateMessageHelper::handleTextMessage() receiving text message: " + textMessage); 
        }
        
        if (textMessage != null) {
            String [] splitString = textMessage.split(MessageTypeConstants.SEPERATOR);
            
            if (splitString.length > 0) {
                try {
                    // The order is based on the above method getTextMessage
                    // Getting group action
                    this.action = ActionType.getAction(splitString[0]);
                    
                    // Getting Offering Data
                    groupKey = splitString[1].replaceAll(
                        MessageTypeConstants.GROUP_DEL, "");
                    
                    serviceName = splitString[2].replaceAll(
                        MessageTypeConstants.SERVICE_NAME_DEL, "");
                    
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error(
                    "Invalid format for Group Update Message: Missing action or group ID data");
                }
            }
            
            if (log.isDebugEnabled()) {
	            log.debug("GroupUpdateMessageHelper::handleTextMessage() groupKey, action: " + groupKey + ", " + action); 
	        }
        }
    }
    
    
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private member data.
     * @param executor - a Dirty Cache Executer given by the <code>DirtyCacheExecuterFactory.</code>
     * @see net.unicon.portal.cache.DirtyCacheExecutor
     */
    public void handleTextMessage() throws GroupsException {
        if (groupKey != null && action != null && serviceName != null) {
            
            if (log.isDebugEnabled()) {
	            log.debug("GroupUpdateMessageHelper::handleTextMessage() group, action, serviceName: " +
	                groupKey + ", " + action + ", " + serviceName);
	        }
            
            CachingGroupService service =
                CachingGroupService.cachingInstance(serviceName);
            
            
            if (ActionType.GROUP_DELETE.equals(action) || ActionType.GROUP_UPDATE.equals(action)) {
                
                IEntityGroup group = GroupService.findGroup(groupKey);
                
                if (log.isDebugEnabled()) {
    	            log.debug("GroupUpdateMessageHelper::handleTextMessage() removing group: " + group);
    	        }
                service.cacheRemove(group);
            }
            
            // now load the group back into the cache
            if (ActionType.GROUP_ADD.equals(action) || ActionType.GROUP_UPDATE.equals(action)) {
                
                IEntityGroup group = GroupService.findGroup(groupKey);
                
                if (log.isDebugEnabled()) {
    	            log.debug("GroupUpdateMessageHelper::handleTextMessage() finding group: " + groupKey + " : " + group);
    	        }
            }
        } else {
            log.error("GroupId, action or serviceName not set: unable to dirty group cache " +
                "(" + groupKey + ", " + action + ", " + serviceName + ")");
        }
    }
}
