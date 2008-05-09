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

package net.unicon.mercury;

import java.util.Date;

public class MockMessage implements IMessage {
	Date date;
	
	public MockMessage(Date date) {
		this.date = date;
	}

	public String getId() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IAddress getSender() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IRecipient[] getRecipients() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IRecipient[] getRecipients(IRecipientType[] types)
			throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubject() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate() throws MercuryException {
		return new Date(date.getTime());
	}

	public String getAbstract() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBody() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public IAttachment[] getAttachments() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public Priority getPriority() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isUnread() throws MercuryException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeleted() throws MercuryException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setRead(boolean seen) throws MercuryException {
	// TODO Auto-generated method stub
	}

	public IMessageFactory getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toXml() throws MercuryException {
		// TODO Auto-generated method stub
		return null;
	}
}
