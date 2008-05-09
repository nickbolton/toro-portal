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
package net.unicon.mercury.cache.impl;

import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MercuryMessageHelper implements TextMessageHelper {
    private static final Log log =
        LogFactory.getLog(MercuryMessageHelper.class);
    
    private String msg = null;
    
    public void setMessage(IMessage mesg) {
    	// Locate all recipients and sender to mark their caches as dirty.
    	StringBuffer buf = new StringBuffer();
    	
    	IRecipient[] recips = null;
		try {
			recips = mesg.getRecipients();
	    	for (int i = 0; i < recips.length; i++) {
	    		buf.append(recips[i].getAddress().toNativeFormat());
	    		buf.append(',');
	    	}
	    	buf.append(mesg.getSender().toNativeFormat());
		} catch (MercuryException e) {
			log.error("Failed to receive message recipients", e);
		}
    	
    	this.msg = buf.toString();
    }
    
    public void setTargets(String[] targets) {
    	StringBuffer buf = new StringBuffer();
    	for (int i = 0; i < targets.length-1; i++) {
    		buf.append(targets[i]);
    		buf.append(',');
    	}
    	buf.append(targets[targets.length-1]);
    	this.msg = buf.toString();
    }

	public String getTextMessage() {
		return this.msg;
	}

	public void setTextMessage(String msg) {
		if (log.isDebugEnabled())
			log.debug("MercuryMessageHelper::setTextMessage() receiving text message: "+msg);
		this.msg = msg;
	}

	public void handleTextMessage() throws Exception {
		((MercuryCacheValidatorImpl)MercuryCacheValidatorContainer.getInstance())
			.markAsDirty(this.msg.split(","), false);
	}

}
