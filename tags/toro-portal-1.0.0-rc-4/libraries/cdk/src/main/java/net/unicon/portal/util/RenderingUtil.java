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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.channels.SuperChannel;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.cscr.CscrBrowserRegistry;
import net.unicon.portal.cscr.ICscrConstants;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.PortalException;
import net.unicon.portal.util.PortalSAXUtils;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.w3c.dom.Document;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.serialize.HTMLSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.Method;
import org.jasig.portal.services.LogService;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class RenderingUtil {

    private static final boolean useCSCR =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
        getPropertyAsBoolean("net.unicon.portal.cscr.useCSCR");

    public static void renderXML(Object caller, ContentHandler out,
        String xml, String sslLocation, String sheet, BrowserInfo bi,
        Hashtable params)
    throws PortalException  {

    	XSLT xsl = new XSLT(caller);
        xsl.setTarget(out);
        xsl.setXML(xml);
        renderXML(xsl, sslLocation, sheet, bi, params);
    }

    public static void renderXML(Object caller, String serializerName, PrintWriter out, String xml,
        String sslLocation, String sheet, BrowserInfo bi, Hashtable params)
    throws PortalException  {

    	XSLT xsl = new XSLT(caller);
        SAX2BufferImpl documentHandler = new SAX2BufferImpl();
        OutputStream os = new ByteArrayOutputStream();
        
        //xsl.setTarget(documentHandler);
        xsl.setTarget(os);
        xsl.setXML(xml);
        renderXML(xsl, sslLocation, sheet, bi, params);
        out.print(os.toString());
        
        try {
          out.print(PortalSAXUtils.serializeSAX(serializerName, documentHandler));
        } catch (Exception se) {

          LogService.log (LogService.ERROR, "RenderingUtil:renderDocument():Error while rendering document content.", se);

          throw new PortalException(se);
        }
    }

    public static void renderDocument(Object caller, ContentHandler out,
        Document doc, String sslLocation, String sheet, BrowserInfo bi,
        Hashtable params)
    throws PortalException  {

    	XSLT xsl = new XSLT(caller);
        xsl.setTarget(out);                                                                  
        xsl.setXML(doc);
        renderXML(xsl, sslLocation, sheet, bi, params);
    }

    public static void renderDocument(Object caller, String serializerName, PrintWriter out,
        Document doc, String sslLocation, String sheet, BrowserInfo bi,
        Hashtable params)
    throws PortalException {

    	XSLT xsl = new XSLT(caller);

        SAX2BufferImpl documentHandler = new SAX2BufferImpl();

        xsl.setTarget(documentHandler);
        xsl.setXML(doc);
        renderXML(xsl, sslLocation, sheet, bi, params);

        try {
          out.print(PortalSAXUtils.serializeSAX(serializerName, documentHandler));
        } catch (Exception se) {

          LogService.log (LogService.ERROR, "RenderingUtil:renderDocument():Error while rendering document content.", se);

          throw new PortalException(se);
        }
    }

    private static void renderXML(XSLT xsl, String sslLocation,
        String sheet, BrowserInfo bi, Hashtable params)
    throws PortalException {

        xsl.setXSL(sslLocation, sheet, bi);
        xsl.setStylesheetParameters(params);
        xsl.transform();
    }
    
    public static void renderSAX(Object caller, String serializerName, PrintWriter out,
        SAX2BufferImpl sax, String sslLocation, String sheet, BrowserInfo bi,
        Hashtable params)
    throws SAXException, PortalException {
        
         BaseMarkupSerializer serializer = SerializerFactory.instance().getSerializer(serializerName, out);
         renderSAX(caller, sax, serializer, sslLocation, sheet, bi, params);
    }

    private static void renderSAX(Object caller, SAX2BufferImpl sax,
        BaseMarkupSerializer serializer, String sslLocation, String sheet,
        BrowserInfo bi, Hashtable params)
    throws SAXException, PortalException {

        String uri = XSLT.getStylesheetURI(
            ResourceLoader.getResourceAsURLString(caller.getClass(),
                sslLocation), sheet, bi);
        uri = ResourceLoader.getResourceAsURLString(caller.getClass(), uri);
        TransformerHandler th =
        	XSLT.getTransformerHandler(uri);
        Transformer t = th.getTransformer();

        // pass xsl parameters
        String name = null;
        Iterator itr = params.keySet().iterator();
        while (itr.hasNext()) {
            name = (String)itr.next();
            t.setParameter(name, (String)params.get(name));
        }
        th.setResult(new SAXResult(serializer));
        sax.outputBuffer(th);
    }

    
    /**
     * this will parse the UPFileSpec an put all the params in the 
     * extras field into a <code>Map</code>
     * @return <code>Map</code>
     */
    public static Map parseExtrasField(UPFileSpec up)
    throws PortalException {
        Map retMap = new HashMap();

        String extras = up.getUPFileExtras();

        if (extras == null) return retMap;

        StringTokenizer st = new StringTokenizer(extras,
            UPFileSpec.PORTAL_URL_SEPARATOR);

        // needs to be an odd number of separators
        if ((st.countTokens() % 2) != 0) {
            throw new PortalException("Invalid extra field: " + extras);
        }

        while (st.hasMoreTokens()) {
            retMap.put(st.nextToken(), st.nextToken());
        }
        return retMap;
    }

    public static UPFileSpec setupUPURL(HttpServletRequest req,
        String parentUID, ChannelClass cc, BrowserInfo bi)
    throws PortalException {
        return setupUPURL(parentUID, cc, bi, true);
    }

    // this method is for channels inside the lms
    public static UPFileSpec setupUPURL(String parentUID, ChannelClass cc,
        BrowserInfo bi, boolean cscr)
    throws PortalException {
        String subUID =
            SubChannelFactory.convertChannelHandleToSubUIDFromParent(
                parentUID, cc.getHandle());
        String parentSubId = SubChannelFactory.convertUIDToSubscribeId(
            parentUID);
        StringBuffer extras = new StringBuffer(512);
        extras.append(SuperChannel.SC_TARGET_HANDLE).append(
            UPFileSpec.PORTAL_URL_SEPARATOR);
        extras.append(cc.getHandle());

       UPFileSpec up = new UPFileSpec(PortalSessionManager.INTERNAL_TAG_VALUE,
           UPFileSpec.RENDER_METHOD, UserInstance.USER_LAYOUT_ROOT_NODE,
               parentSubId, extras.toString());

        // if cscr is requested AND it's enabled for the system AND
        // it's enabled for the channel AND
        // the user agent is registed and enabled in the CscrBrowserRegistry
        if (cscr && useCSCR && cc.isCscrEnabled() &&
            CscrBrowserRegistry.instance().isEnabled(bi)) {

            // client-side channel rendering (cscr) url
            up.setMethod(UPFileSpec.WORKER_METHOD);
            up.setMethodNodeId(ICscrConstants.CSCR_METHOD_ID);
        }
        return up;
    }

    // this method is for channels outside the lms
    public static UPFileSpec setupUPURL(String upId, BrowserInfo bi, boolean cscr)
    throws PortalException {
        String subId = SubChannelFactory.convertUIDToSubscribeId(upId);

       UPFileSpec up = new UPFileSpec(PortalSessionManager.INTERNAL_TAG_VALUE,
           UPFileSpec.RENDER_METHOD, UserInstance.USER_LAYOUT_ROOT_NODE,
               subId, null);

        // if cscr is requested AND it's enabled for the system AND
        // the user agent is registed and enabled in the CscrBrowserRegistry
        if (cscr && useCSCR && CscrBrowserRegistry.instance().isEnabled(bi)) {
            // client-side channel rendering (cscr) url
            up.setMethod(UPFileSpec.WORKER_METHOD);
            up.setMethodNodeId(ICscrConstants.CSCR_METHOD_ID);
        }
        return up;
    }

    private RenderingUtil() {}
}
