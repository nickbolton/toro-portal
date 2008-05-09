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

package net.unicon.mercury.fac;

import net.unicon.mercury.EntityType;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;

import net.unicon.alchemist.EntityEncoder;

public abstract class AbstractRecipient
                          implements IRecipient {

    // Instance Members.
    private final IRecipientType type;
    private final IAddress address;
    private final EntityType eType;

    /*
     * Public API.
     */

    public AbstractRecipient(IAddress address,
                         IRecipientType type
                         , EntityType eType) {

        // Assertions.
        if (address == null) {
            String msg = "Argument 'address' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (type == null) {
            String msg = "Argument 'type' cannot be null.";
            throw new IllegalArgumentException(msg);
        }        
        if (eType == null) {
            String msg = "Argument 'eType' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.address = address;
        this.type = type;
        this.eType = eType;

    }

    public IAddress getAddress() {
        return address;
    }

    public IRecipientType getType() {
        return type;
    }
    
    public EntityType getEntityType() {
        return eType;
    }

    /**
     * Serialize to an XML fragment.
     * @param rslt StringBuffer to append the fragment to.
     * @return A reference to the StringBuffer
     */
    public StringBuffer toXml(StringBuffer rslt) throws MercuryException {
        // Begin.
        rslt.append("<recipient ");

        // Type.
        rslt.append("type=\"");
        rslt.append(EntityEncoder.encodeEntities(getType().getLabel()));
        rslt.append("\">");

        // Address.
        rslt.append(address.toXml());

        // End.
        rslt.append("</recipient>");
        return rslt;

    }
    
    public final String toXml() throws MercuryException {
        return toXml(new StringBuffer()).toString();
    }
    
    public boolean equals(Object o){
        if(!(o instanceof IRecipient)){
            return false;
        }
        
        if(((IRecipient)o).getAddress().equals(this.getAddress()) &&
                ((IRecipient)o).getType().equals(this.getType())){
            return true;
        }
        return false;
    }
    
    public int hashCode(){
        return this.address.hashCode();
    }

}
