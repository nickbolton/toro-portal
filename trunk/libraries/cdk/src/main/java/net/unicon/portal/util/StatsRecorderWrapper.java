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

import net.unicon.portal.domain.ChannelClass;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.ChannelLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelRenderedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelTargetedInLayoutPortalEvent;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

/**
 * This implementation of <code>IChannelRendererWrapper</code> provides
 * the wrapper for a uPortal 2.1.x <code>ChannelRenderer</code>.
 */
public class StatsRecorderWrapper {

	private static final StatsRecorderWrapper __instance = new StatsRecorderWrapper();

	public static StatsRecorderWrapper getInstance() {
		return __instance;
	}
	
    public void recordChannelRendered(String channelSubscribeId, ChannelClass cc,
    		IPerson person, UserProfile profile) {
        EventPublisherLocator.getApplicationEventPublisher().publishEvent(new ChannelRenderedEvent(this, person, profile, channelSubscribeId, cc));
    }
    
    public void recordChannelTargeted(String channelSubscribeId, ChannelClass cc,
    		IPerson person, UserProfile profile) {
        EventPublisherLocator.getApplicationEventPublisher().publishEvent(new ChannelTargetedEvent(this, person, profile, channelSubscribeId, cc));
    }
    
	private StatsRecorderWrapper() {}
    
    /*
     * This wrapper allows for lazy instantiation of the actual event class.
     */
	private static class ChannelRenderedEvent extends AbstractChannelEvent {
        
        private static final String action = "rendered";
        
        public ChannelRenderedEvent(Object source, IPerson person, UserProfile profile, String channelSubscribeId, ChannelClass channelClass) {
            super(source, person, profile, channelSubscribeId, channelClass);
        }
        
        public String getAction() {
            return action;
        }
    }

    /*
     * This wrapper allows for lazy instantiation of the actual event class.
     */
	private static class ChannelTargetedEvent extends AbstractChannelEvent {
        
        private static final String action = "targeted";
        
        public ChannelTargetedEvent(Object source, IPerson person, UserProfile profile, String channelSubscribeId, ChannelClass channelClass) {
            super(source, person, profile, channelSubscribeId, channelClass);
        }
        
        public String getAction() {
            return action;
        }
    }
    
	private static abstract class AbstractChannelEvent extends PortalEvent {
        
        private String channelSubscribeId;
        private ChannelClass channelClass;
        private IUserLayoutChannelDescription channelDescription;
        private UserProfile profile;
        
        public AbstractChannelEvent(Object source, IPerson person, UserProfile profile, String channelSubscribeId, ChannelClass channelClass) {
            super(source, person);
            this.channelSubscribeId = channelSubscribeId;
            this.channelClass = channelClass;
            this.profile = profile;
        }
        
        public String toString() {
            return "Channel [" + getChannelDescription().getName() + ", "
                    + getChannelDescription().getChannelPublishId() + ", "
                    + getChannelDescription().getChannelSubscribeId()
                    + "] was " + getAction() + " in layout " + getProfile().getLayoutId()
                    + " by " + getDisplayName() + " at " + getTimestampAsDate();
        }
        
        public final UserProfile getProfile() {
            return this.profile;
        }
        
        public IUserLayoutChannelDescription getChannelDescription() {
            if (channelDescription == null) {
                channelDescription = new UserLayoutChannelDescription();
                channelDescription.setName(channelClass.getLabel());
                channelDescription.setChannelPublishId(channelClass.getHandle());
                channelDescription.setChannelSubscribeId(channelSubscribeId);
            }
            return channelDescription;
        }
        
        public abstract String getAction();
    }
}
