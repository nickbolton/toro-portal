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
package net.unicon.academus.domain.lms;

import net.unicon.academus.domain.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jasig.portal.services.LogService;
/**
 * Contains data relevant to an individual user's current state within the
 * portal system.  For example, it references the current offering being viewed
 * by the user for each topic type.  Context information exists for a single session only.
 */
public final class Context {
    /* Instance Members */
    private Map currentOfferings = null;
    Context() {
        currentOfferings = new HashMap();
    }
    public Object clone() {
        Context newContext = new Context();
        Iterator itr = this.currentOfferings.keySet().iterator();
        while (itr.hasNext()) {
            TopicType key = (TopicType)itr.next();
            newContext.setCurrentOffering(
            ((Long)this.currentOfferings.get(key)).longValue(), key);
        }
        return newContext;
    }
    /**
     * Provides the current offering being viewed by the user for the
     * specified topic type or <code>null</code> if no offering has yet been
     * set for the specified type.  <code>TopicType</code> follows the type-safe enumeration pattern (see Effective Java).
     * @param type a topic type within the portal system.
     * @return the current offering for the specified topic type.
     * @throws IllegalArgumentException if <code>type</code> is <code>null</code>.
     */
    public Offering getCurrentOffering(TopicType type)
    throws IllegalArgumentException {
        if (type == null) {
            String msg = "Topic type can't be null.";
            throw new IllegalArgumentException(msg);
        }
        Offering currentOffering = null;
        try {
            Long currentOfferingId = (Long) currentOfferings.get(type);
            if (currentOfferingId != null && currentOfferingId.longValue() > 0) {
                currentOffering =
                    OfferingFactory.getOffering(currentOfferingId.longValue());
            }
        } catch (OperationFailedException ofe) {
            LogService.instance().log(LogService.ERROR,
            "getCurrentOffering Operation Failed : Context.getCurrentOffering()");
        } catch (ItemNotFoundException infe) {
            // it's possible that the offering was deleted when they're trying to
            // access it. Just log an INFO
            LogService.instance().log(LogService.INFO,
            "Cannot Find Offering Object :  Context.getCurrentOffering()");
        } catch (NullPointerException npe) {
            LogService.instance().log(LogService.INFO,
            "No Current Offering : getCurrentOffering()");
        }
        return currentOffering;
    }
    /**
     * Modifies the current offering being viewed by the user for the specified
     * topic type.  <code>TopicType</code> follows the type-safe enumeration pattern (see Effective Java).
     * @param current an offering id that the user wants to view.
     * @param type a topic type within the portal system.
     * @throws IllegalArgumentException if <code>type</code> is <code>null</code>.
     */
    public void setCurrentOffering(long current, TopicType type)
    throws IllegalArgumentException {
        if (type == null) {
            String msg = "Topic type can't be null.";
            throw new IllegalArgumentException(msg);
        }
        //Storing only offering ID in the map
        currentOfferings.put(type, new Long(current));
    }
}
