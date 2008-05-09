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

import java.util.List;

import net.unicon.mercury.IAddress;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Element;
import net.unicon.alchemist.EntityEncoder;

public class AddressImpl implements IAddress {

    // Instance Members.
    private final String label;
    private final String nativeFormat;

    /*
     * Public API.
     */

    public AddressImpl(String label,
            String nativeFormat) {
        
        // Assertions.
        if (label == null || label.trim().length() == 0) {
            String msg = "Argument 'label' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (nativeFormat == null) {
            String msg = "Argument 'nativeFormat' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.label = label;
        this.nativeFormat = nativeFormat;

    }

    public final String getLabel() {
        return label;
    }

    public final String toNativeFormat() {
        return nativeFormat;
    }

    /**
     * Serialize to an XML fragment.
     * @return An XML fragment representing the Address
     */
    public String toXml() {
        StringBuffer rslt = new StringBuffer();
        // Begin.
        rslt.append("<address ");

        // Id.
        rslt.append("native-format=\"");
        rslt.append(EntityEncoder.encodeEntities(toNativeFormat()));
        rslt.append("\">");

        // Label.
        rslt.append("<label>");
        rslt.append(EntityEncoder.encodeEntities(label));
        rslt.append("</label>");

        // End.
        rslt.append("</address>");
        
        return rslt.toString();

    }

    public static IAddress parse(Element e)
                    throws MercuryException {
        String label = null;
        String nativeFormat = null;

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("address")) {
            String msg = "Argument 'e [Element]' must be a <address> "
                + "element.";
            throw new IllegalArgumentException(msg);
        }
        
        // Native-format attribute.
        Attribute t = e.attribute("native-format");
        if (t == null) {
            String msg = "Element <address> is missing required "
                + "attribute 'native-format'";
            throw new XmlFormatException(msg);
        }
        nativeFormat = t.getValue();

        // Label.
        List list = e.elements("label");
        if (list.size() == 1)
            label = ((Element)list.get(0)).getText();
        else
            label = nativeFormat;

        return new AddressImpl(label, nativeFormat);
    }
    
    public boolean equals(Object o){
        if(!(o instanceof IAddress)){
            return false;
        }
        
        if(((IAddress)o).toNativeFormat().equalsIgnoreCase(this.toNativeFormat())){
            return true;
        }
        
        return false;
    }
    
    public int hashCode(){
         return this.nativeFormat.hashCode();
    }
    
    
}

