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
package net.unicon.portal.servants;

import java.io.IOException;
import java.io.PrintWriter;

import net.unicon.portal.util.PortalSAXUtils;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class ServantUtil {

    public static void renderServant(ContentHandler out, IServant servant,
        ChannelRuntimeData runtimeData)
    throws PortalException {

        // we can only render IChannels
        if (!(servant instanceof IChannel)) {
            throw new PortalException("Servant is not an IChannel!");
        }

        IChannel ch = (IChannel)servant;

        renderServant(out, ch, runtimeData);
    }

    public static void renderServant(PrintWriter out, IServant servant,
        ChannelStaticData staticData, ChannelRuntimeData runtimeData)
    throws PortalException {

        // we can only render IChannels
        if (!(servant instanceof IChannel)) {
            throw new PortalException("Servant is not an IChannel!");
        }

        IChannel ch = (IChannel)servant;

        renderServant(out, ch, staticData, runtimeData);
    }

    private static void renderServant(ContentHandler out, IChannel ch,
        ChannelRuntimeData runtimeData)
    throws PortalException {
        ch.setRuntimeData(runtimeData);
        ch.renderXML(out);
    }

    private static void renderServant(PrintWriter out, IChannel ch,
        ChannelStaticData staticData, ChannelRuntimeData runtimeData)
    throws PortalException {
        try {
            ch.setRuntimeData(runtimeData);
            SAX2BufferImpl buffer = new SAX2BufferImpl();
            ch.renderXML(buffer);
            out.print(PortalSAXUtils.serializeSAX(staticData.getSerializerName(), buffer));
        } catch (IOException ioe) {
            throw new PortalException(ioe);
        } catch (SAXException se) {
            throw new PortalException(se);
        }
    }

    private ServantUtil() {}
}
