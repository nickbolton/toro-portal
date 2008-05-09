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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import net.unicon.portal.channels.error.ErrorChannel;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.domain.ChannelClass;

import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRendererFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IChannelRenderer;
import org.jasig.portal.IChannelRendererFactory;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.ChannelRenderedInLayoutPortalEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.StatsRecorder;
import org.jasig.portal.services.stats.StatsRecorderSettings;
import org.jasig.portal.utils.SAX2BufferImpl;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;

/**
 * This implementation of <code>IChannelRendererWrapper</code> provides
 * the wrapper for a uPortal 2.1.x <code>ChannelRenderer</code>.
 */
public class ChannelRendererWrapper {

    public static final AtomicLong activeRenderers = new AtomicLong();
    public static final AtomicLong maxRenderThreads = new AtomicLong();

    private static final IChannelRendererFactory channelRendererFactory =
        ChannelRendererFactory.newInstance(
            ChannelRendererWrapper.class.getName(), activeRenderers, maxRenderThreads);

    private Object buffer;
    private String upId;
    private ChannelClass channelClass;
    private IChannel ichannel;
    private PortalControlStructures pcs;
    private ChannelManager channelManager;
    private IChannelRenderer channelRenderer;
    private long timeout;
    private ChannelRuntimeData runtimeData;
    private IPerson person;
    private boolean isCharCaching;

    /**
     * The constructor for this object. This creates a wrapper object
     * for a <code>ChannelRenderer</code> uPortal object.
     * @param upId The internal channel object identifier.
     * @param cc The <code>ChannelClass</code> object that defines the channel.
     * @param ch The uPortal channel object.
     * @param cm The <code>ChannelManager</code> that manages the rendering.
     * @param timeout The timeout of the channel.
     * @param rd The runtime data of the channel.
     * @param person The person that is invoking the channel.
     * @param isCharCaching A flag to indicate if the
     * <code>ChannelRenderer</code> should perform character level caching.
     */
    public ChannelRendererWrapper(String upId, PortalControlStructures pcs,
        ChannelClass cc, IChannel ch, ChannelManager cm, long timeout,
        ChannelRuntimeData rd, IPerson person, boolean isCharCaching) {

        this.upId = upId;
        this.pcs = pcs;
        this.channelClass = cc;
        this.ichannel = ch;
        this.channelManager = cm;
        this.timeout = timeout;
        this.runtimeData = rd;
        this.person = person;
        this.channelRenderer = null;
        this.isCharCaching = isCharCaching;
    }

    /**
     * Gets the <code>ChannelClass</code> attribute.
     * @return The channel class for this wrapper.
     */
    public ChannelClass getChannelClass() {
        return channelClass;
    }

    /**
     * This will invoke the startRendering() method of a
     * <code>ChannelRenderer</code> object.
     * @throws PortalException if the rendering fails.
     */
    public void startRendering(Map channelCacheTables) throws PortalException {
try {
        channelRenderer = channelRendererFactory.newInstance(ichannel, runtimeData, pcs);
} catch (Throwable t) {
//t.printStackTrace();
throw new PortalException(new Exception(t));
}
        channelRenderer.setTimeout(timeout);
        channelRenderer.setCharacterCacheable(isCharCaching);
        // This is required for ICacheable channels
        // that cache with INSTANCE scope
        channelRenderer.setCacheTables(channelCacheTables);
        channelRenderer.startRendering();
    }

    /**
     * Retrieve the results of the rendering. The result can either be
     * a String or a SAX event buffer. If the rendering was not successful,
     * a generic error channel will be rendered instead.
     * @return The results as either a String or a SAX event buffer.
     * @throws Exception if the <code>ChannelRenderer</code> object
     * fails to return an object or returns an unidentified error code.
     * Valid codes are <code>ChannelRenderer</code>.RENDERING_SUCCESSFUL and
     * <code>ChannelRenderer</code>.RENDERING_TIMED_OUT.
     */
    public Object getResults() throws Exception {
        if (buffer != null) return buffer;
        try {
            int rc = channelRenderer.completeRendering();
            if ( rc == channelRenderer.RENDERING_SUCCESSFUL ) {
                buffer = channelRenderer.getCharacters();
                if (buffer == null) {
                    buffer = channelRenderer.getBuffer();
                }
            } else if ( rc == channelRenderer.RENDERING_TIMED_OUT ) {
                buffer =
                renderErrorChannel(ErrorChannel.TIMEOUT_EXCEPTION, null);
            } else {
                String msg = "unrecognized return code: " + rc;
                LogService.instance().log(LogService.ERROR,
                "SuperChannel::renderState() : " + msg);
                throw new Exception(msg);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            String msg = "failed to render channel: " +
            channelClass.getClassName();
            LogService.instance().log(LogService.ERROR,
            "SuperChannel::renderState() : " + msg, t);
            buffer = renderErrorChannel(ErrorChannel.RENDER_TIME_EXCEPTION,
            new PortalException(new Exception(t)));
        }

        recordChannelRendered();
        return buffer;
    }

    private void recordChannelRendered() throws PortalException {
    	String channelSubscribeId = SubChannelFactory.getSubChannelSubscribeId(upId, channelClass);
        StatsRecorderWrapper.getInstance().recordChannelRendered(channelSubscribeId,
        	channelClass, person, pcs.getUserPreferencesManager().getCurrentProfile());
    }

    private SAX2BufferImpl renderErrorChannel(int error, Exception exception)
    throws Exception {
        SAX2BufferImpl contentHandler = new SAX2BufferImpl();
        IChannel badChannel = ichannel;
        if (badChannel == null) {
            String msg = "failed to find Channel for " +
            channelClass.getClassName();
            LogService.instance().log(LogService.ERROR,
            "SuperChannel::renderErrorChannel() : " + msg);
            return contentHandler;
        }
        String channelId =
        SubChannelFactory.getSubChannelSubscribeId(upId, channelClass);
        ErrorChannel errorChannel =
        new ErrorChannel(channelId, error, exception);
        // demand output
        try {
            ChannelRuntimeData rd =
            (ChannelRuntimeData)runtimeData.clone();
            // bypass cscr
            UPFileSpec upfs = RenderingUtil.setupUPURL(upId, channelClass,
                rd.getBrowserInfo(), false);
            rd.setUPFile(upfs);
            errorChannel.setRuntimeData(rd);
            errorChannel.renderXML(contentHandler);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sw.flush();
            LogService.instance().log(LogService.ERROR,
            "ChannelManager::outputChannels : Error channel threw ! " +
            sw.toString(), e);
        }
        return contentHandler;
    }
}
