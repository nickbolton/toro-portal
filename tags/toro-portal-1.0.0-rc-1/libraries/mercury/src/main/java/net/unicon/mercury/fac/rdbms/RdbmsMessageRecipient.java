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

package net.unicon.mercury.fac.rdbms;

import java.util.List;

import net.unicon.mercury.EntityType;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientDetail;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;
import net.unicon.mercury.fac.AbstractRecipient;
import net.unicon.mercury.fac.AddressImpl;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * @author bszabo
 *
 */
public class RdbmsMessageRecipient extends AbstractRecipient 
							implements IRecipientDetail{
    
    
    // instance members
    private boolean readMessage;
    
    public RdbmsMessageRecipient(IAddress address,
                                 IRecipientType type, boolean read, EntityType etype) {
        super(address, type, etype);
        this.readMessage = read;
    }
    
    public RdbmsMessageRecipient(IAddress address,
                                 IRecipientType type, boolean read) {
    	this(address, type, read, EntityType.USER);
    }
    
    public static IRecipient parse(Element e)
                                           throws MercuryException {
        IRecipientType type;
        IAddress address;
        
        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("recipient")) {
            String msg = "Argument 'e [Element]' must be a <recipient> "
                + "element.";
            throw new IllegalArgumentException(msg);
        }        
        
        // Type attribute.
        Attribute t = e.attribute("type");
        if (t == null) {
            String msg = "Element <recipient> is missing required "
                + "attribute 'type'";
            throw new XmlFormatException(msg);
        }

        type = RdbmsRecipientType.getType(t.getValue());
     
        // Address.
        List list = e.elements("address");
        if (list.size() != 1) {
            String msg = "Exactly one <address> element required per "
                + "recipient.";
            throw new XmlFormatException(msg);
        }
        address = AddressImpl.parse(((Element)list.get(0)));
        
        return new RdbmsMessageRecipient(address, type, false);
    }
    
    public boolean hasReadMessage() {
        return readMessage;
    } 
    
    public void setReadMessage(boolean value) {
        this.readMessage = value;
    }
    
}
