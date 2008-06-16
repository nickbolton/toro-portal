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
package net.unicon.portal.channels;

import java.io.PrintWriter;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.xml.sax.ContentHandler;

/**
 * @author nbolton
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CHello implements IMultithreadedCharacterChannel, IMultithreadedCacheable {

	private ChannelCacheKey cacheKey;
	
	/**
	 * 
	 */
	public CHello() {
		super();
		// TODO Auto-generated constructor stub
		
		cacheKey = new ChannelCacheKey();
		cacheKey.setKey("CHello");
		cacheKey.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
		cacheKey.setKeyValidity(null);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedCacheable#generateKey(java.lang.String)
	 */
	public ChannelCacheKey generateKey(String arg0) {
		// TODO Auto-generated method stub
		return cacheKey;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedCacheable#isCacheValid(java.lang.Object, java.lang.String)
	 */
	public boolean isCacheValid(Object arg0, String arg1) {
		// TODO Auto-generated method stub
		return true;
	}
	

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedChannel#renderXML(org.xml.sax.ContentHandler, java.lang.String)
	 */
	public void renderXML(ContentHandler arg0, String arg1)
			throws PortalException {
		
		
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedCharacterChannel#renderCharacters(java.io.PrintWriter, java.lang.String)
	 */
	public void renderCharacters(PrintWriter out, String upId) throws PortalException {
		// TODO Auto-generated method stub
		
		out.println("<p>Hello</p>");
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedChannel#setStaticData(org.jasig.portal.ChannelStaticData, java.lang.String)
	 */
	public void setStaticData(ChannelStaticData arg0, String arg1) throws PortalException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData, java.lang.String)
	 */
	public void setRuntimeData(ChannelRuntimeData arg0, String arg1) throws PortalException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedChannel#receiveEvent(org.jasig.portal.PortalEvent, java.lang.String)
	 */
	public void receiveEvent(PortalEvent arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IMultithreadedChannel#getRuntimeProperties(java.lang.String)
	 */
	public ChannelRuntimeProperties getRuntimeProperties(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
