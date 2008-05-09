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

package net.unicon.mercury.fac.ge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.fac.AbstractRecipient;
import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.fac.rdbms.RdbmsMessageFactory;
import net.unicon.penelope.IDecisionCollection;

/**
 * @author ibiswas
 */
public class GroupExpandMessageFactory extends RdbmsMessageFactory {
    
    private final ICivisFactory cFac;
    
    public GroupExpandMessageFactory(DataSource ds, String attachPath, int layers
            , String username, int expiration, ICivisFactory cFac){
        
        super(ds, attachPath, layers, username, expiration);
        
        // Assertions 
        if (cFac == null) {
            throw new IllegalArgumentException("Argument 'cFac' cannot be null.");
        }        
        
        this.cFac = cFac;
    }    
    
    public IMessage sendMessage(IRecipient[] recipients, String subject,
            String body, IAttachment[] attachments, Priority priority)
            throws MercuryException {
        
        IRecipient[] recs = expandRecipients(recipients);
        
        return super.sendMessage(recs, subject, body, attachments, priority);
    }
    
    private IRecipient[] expandRecipients(IRecipient[] recipients) throws MercuryException {
    	List rslt = new ArrayList(recipients.length);
        
        for(int i = 0; i < recipients.length; i++){
            if (recipients[i].getEntityType().equals(EntityType.USER)) {
            	rslt.add(recipients[i]);
            } else {
                rslt.addAll(Arrays.asList(expandGroup(recipients[i])));
            }
        }
        
        return (IRecipient[])rslt.toArray(new IRecipient[0]);
    }

	private IRecipient[] expandGroup(IRecipient recipient) throws MercuryException {
		//  get all the members in the group 
		IGroup group = null;
	    try {
	        group = cFac.getGroupByPath(recipient.getAddress().toNativeFormat());
	    } catch(CivisRuntimeException cre) {
		    throw new MercuryException(
		                "Unable to find the specified group: " +
		                recipient.getAddress().toNativeFormat(), cre);
	    }
		if (group == null) {
		    throw new MercuryException(
		                "Unable to find the specified group: " +
		                recipient.getAddress().toNativeFormat());
		}

		IPerson[] persons = group.getMembers(true);
		IDecisionCollection dColl = null;
		StringBuffer name = new StringBuffer();

		IRecipient[] rslt = new IRecipient[persons.length];
		for(int j = 0; j < persons.length; j++){
		    dColl = persons[j].getAttributes();
		    name.append(dColl.getDecision("lName").getFirstSelectionValue())
			.append(", ")
			.append(dColl.getDecision("fName").getFirstSelectionValue());
		    rslt[j] = new RecipientImpl(
		    		new AddressImpl(name.toString(), persons[j].getName()),
		    		recipient.getType(),
		    		EntityType.USER);
		    name.setLength(0);
		}
		return rslt;
	}

    class RecipientImpl extends AbstractRecipient {

        public RecipientImpl(IAddress address,
                           IRecipientType type, EntityType etype) {
            super(address, type, etype);
        }
    }
}
