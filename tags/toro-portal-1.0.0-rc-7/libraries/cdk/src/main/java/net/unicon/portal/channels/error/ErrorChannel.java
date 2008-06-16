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
package net.unicon.portal.channels.error;
import java.util.Hashtable;

import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.DocumentFactory;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public class ErrorChannel extends BaseChannel {
    public static final int TIMEOUT_EXCEPTION = 1;
    public static final int RENDER_TIME_EXCEPTION = 2;
    protected int errorCode = 0;
    protected String channelId = null;
    public ErrorChannel(String channelId, int error, Exception e) {
        super();
        this.errorCode = error;
        this.channelId = channelId;
        System.out.println("ErrorChannel() channelId: " + channelId);
        if (e != null) {
            e.printStackTrace();
        }
    }
    public void renderXML(ContentHandler out) throws PortalException {
        try {
            XSLT xsl = new XSLT(this);
            xsl.setTarget(out);
            Hashtable ht = new Hashtable();
            ht.put("baseActionURL", runtimeData.getBaseActionURL());
            xsl.setXML(toDocument());
            xsl.setXSL("error.ssl", "renderFailed",
            runtimeData.getBrowserInfo());
            xsl.setStylesheetParameters(ht);
            xsl.transform();
        } catch (Exception e) {
            e.printStackTrace();
            throw new PortalException(e);
        }
    }
    protected Document toDocument() throws Exception {
        String message = null;
        switch (errorCode) {
            case TIMEOUT_EXCEPTION:
                message = "This channel timed out.";
                break;
            case RENDER_TIME_EXCEPTION:
                message = "This channel failed to render.";
                break;
            default:
                message = "This channel failed to render (-1).";
                break;
        }
        Document doc = DocumentFactory.getNewDocument();
        Element root = doc.createElement("errorPage");
        XmlUtils.addNewNode(doc, root, "message", null, message);
        doc.appendChild(root);
        return doc;
    }
}
