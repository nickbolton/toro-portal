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
package net.unicon.portal.util;

import java.io.IOException;
import java.io.StringWriter;

import org.jasig.portal.PortalException;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.SAXException;

/**
 * @author Kevin Gary
 *
 */
public class PortalSAXUtils {

    /**
     * Refactored from net.unicon.common.util.XmlUtils, as its 
     * implementation relies on uPortal classes. --KG
     * @param saxBuffer
     * @return String
     * @throws IOException
     * @throws SAXException
     */
	public static String serializeSAX(String serializerName, SAX2BufferImpl saxBuffer)
	throws IOException, SAXException, PortalException {
	    StringWriter sw = new StringWriter();
	    BaseMarkupSerializer serializer = SerializerFactory.instance().getSerializer(serializerName, sw);
	    serializer.setOutputCharStream(sw);
	    serializer.asDocumentHandler();
	    saxBuffer.outputBuffer(serializer);
	    return sw.toString();
	}
}
