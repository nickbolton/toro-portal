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

package net.unicon.mercury.fac.email;

import java.util.List;

import net.unicon.mercury.EntityType;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;
import net.unicon.mercury.fac.AbstractRecipient;
import net.unicon.mercury.fac.AddressImpl;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Representation of a message recipient with a scoping type.
 * @see IRecipient
 * @see EmailRecipientType
 */
public class EmailRecipient extends AbstractRecipient {    
    
    /**
     * Construct a recipient object.
     * @param address Address to represent
     * @param type Scoping type to pair with the address
     */
    public EmailRecipient(IAddress address, IRecipientType type) {
        super(address, type, EntityType.USER);
        if (!(type instanceof EmailRecipientType))
            throw new IllegalArgumentException(
                    "Argument 'type' is not of type EmailRecipientType.");
    }
    
    /**
     * Parse a recipient from an XML fragment.
     * @param e The XML fragment to parse.
     * @return The resultant recipient object.
     * @throws MercuryException if the parsing fails
     */
    public static IRecipient parse(Element e)
                                   throws MercuryException
    {
        IRecipientType type;
        IAddress address;
        
        assert e != null : "Argument 'e [Element]' cannot be null.";
        
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
        type = EmailRecipientType.getType(t.getValue());
        
        // Address.
        List list = e.elements("address");
        if (list.size() != 1) {
            String msg = "Exactly one <address> element required per "
                + "recipient.";
            throw new XmlFormatException(msg);
        }
        address = AddressImpl.parse(((Element)list.get(0)));
        
        return new EmailRecipient(address, type);
    }
    
}
