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
package net.unicon.portal.common;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import net.unicon.academus.domain.lms.*;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.portal.channels.IOfferingSubChannel;

import org.jasig.portal.PortalException;
import org.w3c.dom.*;
public class BaseOfferingSubChannel extends BaseSubChannel
implements IOfferingSubChannel {
    public BaseOfferingSubChannel() {
        super();
    }
    // These methods need to be implemented by all subchannels
    // that want to import/export it's Offering data
    public String exportChannel(Offering offering) throws Exception {
        return "";
    }
    public synchronized Map importChannel(Offering offering, Document dom)
    throws Exception {
        throw new PortalException("importChannel not implemented!");
    }
    public boolean isCacheValid(Object validity, String upId) {
        boolean contextChanged = ChannelDataManager.isContextChanged(upId);
        if (contextChanged) {
            setDirty(upId, false);
            return false;
        }
        return super.isCacheValid(validity, upId);
    }
}
