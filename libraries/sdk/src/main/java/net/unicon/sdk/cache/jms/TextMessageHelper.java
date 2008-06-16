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
package net.unicon.sdk.cache.jms;

public interface TextMessageHelper {
    /**
     * Returns a text Message string of the helper object.
     * @return text message - a text message representation of the helper object.
     */
    public String getTextMessage();
    /**
     * sets the values of the helper object based on the passed int text message.  The message is parsed for data.
     * @param textmessage - a text message that was built by the helper.
     */
    public void setTextMessage(String textMessage);
    /**
     * Handles the text message that was passed in.  This call will actually
     * dirty the channel based on the data received from the text message or from the private member data.
     * @see net.unicon.portal.cache.DirtyCacheExecutor
     */
    public void handleTextMessage() throws Exception;
}
