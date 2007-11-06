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

import net.unicon.academus.domain.*;
import net.unicon.academus.domain.lms.*;
import net.unicon.portal.common.SubChannelFactory;
import net.unicon.portal.domain.*;
import net.unicon.portal.permissions.IPermissions;
import net.unicon.portal.permissions.PermissionsService;

import org.jasig.portal.PortalException;
import org.jasig.portal.IChannel;
import org.jasig.portal.services.LogService;

import java.util.*;

/**
 *  Description of the Class
 *@author     nbolton
 *@created    August 6, 2002
 */

public class LmsChannel extends SuperChannel {

    /** Description of the Field */

    protected final static String NAVIGATION_CHANNEL = "NavigationChannel";

    /** Constructor for the LmsChannel object */

    public LmsChannel() {

        super();

    }

    protected void initialize(String upId) throws Exception {

        String offeringIdStr = getRuntimeData(upId).getParameter(
            "offeringId");

        String targetChannel = getTargetChannelId(upId);

        Context context = getDomainUser(upId).getContext();

        String mode = getRuntimeData(upId).getParameter("mode");

        if (mode == null) {

            mode = (String)getChannelAttribute(upId, CHANNEL_MODE_KEY);

        }

        // make sure the mode is valid

        if (!getAdminMode().equals(mode) &&

        !getSubscriptionMode().equals(mode) &&

        !getOfferingMode().equals(mode)) {

            // default to the offering mode

            mode = getOfferingMode();

        }

        // set the channel mode

        putChannelAttribute(upId, CHANNEL_MODE_KEY, mode);

        // check if we need to change context

        setContextChanged(upId, false);

        String navigatorId = getNavigatorChannelId(upId);

        if (getOfferingMode().equals(mode) &&

        navigatorId.equals(targetChannel) && offeringIdStr != null) {

            try {

                int offeringId = Integer.parseInt(offeringIdStr);

                context.setCurrentOffering(offeringId, TopicType.ACADEMICS);

                setContextChanged(upId, true);

            } catch (NumberFormatException e) {

                String msg = "Invalid offeringId: " + offeringIdStr;

                LogService.log(LogService.ERROR, msg);

            }

        }

        // **> Get out now if the mode isn't OFFERING.

        if (!getOfferingMode().equals(mode)) return;

        /*

         * EVALUATE & VALIDATE THE USER'S CONTEXT:

         * We need to make sure the user's context still

         * makes sense in the present state of the universe.

         */

        List offerings = null;

        Offering target = context.getCurrentOffering(TopicType.ACADEMICS);

        // **> Get out now if the user is a SuperUser.

        if (getDomainUser(upId).isSuperUser()) {

            if (target == null) { // Damn! -- we still have to choose.

                offerings = OfferingFactory.getNavigationOfferings(getDomainUser(upId));

                // If there are no available offerings for the super user,

                // place them in admin mode

                if (offerings.size() <= 0) {

                    putChannelAttribute(upId, CHANNEL_MODE_KEY, getAdminMode());

                } else {

                    changeTarget(upId, offerings);

                }

            }

            return;

        }

        // Otherwise, evaluate whether we need to choose a target.

        offerings = OfferingFactory.getNavigationOfferings(getDomainUser(upId));

        // **> Get out now if the user doesn't have any offerings.

        if (offerings.size() == 0) {

            // This user is in the Offering mode but has no offerings,

            // set the mode to subscription.

            putChannelAttribute(upId, CHANNEL_MODE_KEY, getSubscriptionMode());

            return;

        }

        // The user at least has offerings.

        if (target != null) {

            // Check to see if the target is

            // invalid for one of several reasons:

            // (1) --> The user is not enrolled.

            if (!Memberships.isEnrolled(getDomainUser(upId), target)) {

                changeTarget(upId, offerings);

                return;

            }

            // (2) --> The offering is inactive and the

            // user doesn't have permissions to view it.

            if (target.getStatus() == Offering.INACTIVE) {

                Role r = Memberships.getRole(getDomainUser(upId), target);

                ChannelClass nav = ChannelClassFactory.getChannelClass(
                    NAVIGATION_CHANNEL);

                IPermissions p = PermissionsService.instance().
                    getPermissions(nav, r.getGroup());

                if (!p.canDo(getDomainUser(upId).getUsername(),
                    "viewInactiveOfferings")) {
                    changeTarget(upId, offerings);
                    return;
                }
            }
        } else {

            // There isn't one, so we have to choose a target.

            changeTarget(upId, offerings);

            return;

        }

    }

    // No controlling class in the old model
    protected ChannelClass getControllingChannelClass() {
        return null;
    }

    protected ChannelObjects getControllingChannelObjects(String upId) {
        return null;
    }

    private void changeTarget(String upId, List offerings) {

        if (upId == null) {

            String msg = "Parameter upId cannot be null.";

            throw new IllegalArgumentException(msg);

        }

        if (offerings == null) {

            String msg = "Parameter offerings cannot be null.";

            throw new IllegalArgumentException(msg);

        }

        if (offerings.size() == 0) {

            String msg = "Parameter offerings must contain at least one element.";

            throw new IllegalArgumentException(msg);

        }

        User u = getDomainUser(upId);

        Context c = u.getContext();

        offerings = sortOfferings(offerings);

        Offering newTarget = (Offering) offerings.get(0);

        c.setCurrentOffering(newTarget.getId(), TopicType.ACADEMICS);

        setContextChanged(upId, true);

    }

    protected List sortOfferings(List offerings) {

        if (offerings == null) return null;

        Map map = new TreeMap();

        Offering offering = null;

        Iterator itr = offerings.iterator();

        while (itr.hasNext()) {

            offering = (Offering)itr.next();

            String key = offering.getTopic().getName() + offering.getName();

            map.put(key.toLowerCase(), offering);

        }

        return new ArrayList(map.values());

    }

    public void buildXML(String upId) throws Exception {

        //net.unicon.portal.util.Debug.instance().markTime();

        //net.unicon.portal.util.Debug.instance().timeElapsed("BEGIN BUILDING LmsChannel", true);

        initialize(upId);

        super.buildXML(upId);

        //net.unicon.portal.util.Debug.instance().timeElapsed("DONE BUILDING LmsChannel", true);

    }

    /**
     *  Gets the channelTitle attribute of the LmsChannel object
     *@param  upId           Description of the Parameter
     *@param  cc             Description of the Parameter
     *@return                The channelTitle value
     *@exception  Exception  Description of the Exception
     */

    protected String getChannelTitle(String upId, ChannelClass cc)

    throws Exception {

        StringBuffer title = new StringBuffer(cc.getLabel());
        // commented out by IBiswas as per Team Track #04598 

       /* String subId = SubChannelFactory.getSubChannelSubscribeId(upId, cc);

        if (getNavigatorChannelId(upId).equals(subId)) {

            Offering offering = getDomainUser(upId).getContext().

            getCurrentOffering(TopicType.ACADEMICS);

            if (offering != null) {

                Topic topic = offering.getTopic();

                title.append(": " + topic.getName());

                title.append(" - " + offering.getName());

            }

        }*/

        return title.toString();

    }

    /**
     *  Gets the navigatorChannelId attribute of the LmsChannel object
     *@param  upId           Description of the Parameter
     *@return                The navigatorChannelId value
     *@exception  Exception  Description of the Exception
     */

    protected String getNavigatorChannelId(String upId)

    throws Exception {

        String navigatorId =

        (String) getChannelAttribute(upId, NAVIGATION_CHANNEL);

        if (navigatorId != null) {

            return navigatorId;

        }

        Iterator itr = ChannelClassFactory.getChannelClasses(
            ChannelMode.GLOBAL).iterator();

        while (itr != null && itr.hasNext()) {

            ChannelClass cc = (ChannelClass) itr.next();

            if (NAVIGATION_CHANNEL.equals(cc.getHandle())) {

                navigatorId =

                SubChannelFactory.getSubChannelSubscribeId(upId, cc);

                putChannelAttribute(upId, NAVIGATION_CHANNEL, navigatorId);

                return navigatorId;

            }

        }

        String msg = "Failed to find Navigator Channel";

        throw new PortalException(msg);

    }

    /**
     *  Gets the renderableChannels attribute of the LmsChannel object
     *@param  upId           Description of the Parameter
     *@return                The renderableChannels value
     *@exception  Exception  Description of the Exception
     */

    protected List getRenderableChannels(String upId)

    throws Exception {


        List renderableChannels = new ArrayList();

        Offering currentOffering = getDomainUser(upId).getContext().

        getCurrentOffering(TopicType.ACADEMICS);

        String focusedChannel = getFocusedChannel(upId);

        List potentialChannels = new ArrayList();

        String mode = (String)getChannelAttribute(upId, CHANNEL_MODE_KEY);

        if (getAdminMode().equals(mode)) {

            potentialChannels = getAdminChannels();

        } else if (getSubscriptionMode().equals(mode)) {

            potentialChannels = getSubscriptionChannels();

        } else {

            potentialChannels = getOfferingChannels(currentOffering);

            if (currentOffering == null) {

                potentialChannels = getGlobalChannels();

            }

        }

        Iterator itr = potentialChannels.iterator();

        String subId = null;

        while (itr != null && itr.hasNext()) {

            ChannelClass cc = (ChannelClass) itr.next();

            subId = SubChannelFactory.getSubChannelSubscribeId(upId, cc);

            if (focusedChannel != null && ! "".equals(focusedChannel) &&

            !focusedChannel.equals(subId)) {

                continue;

            }

            IChannel ch = null;

            ch = SubChannelFactory.getChannelInstance(upId, cc,
                getStaticData(upId), getUPortalUser(upId),
                getAuthorizationPrincipal(upId),
                getPortalControlStructures(upId).getUserPreferencesManager());

            if (ch != null) {

                renderableChannels.add(new ChannelObjects(cc, ch));

            }

        }

        return renderableChannels;

    }

    protected List getGlobalChannels()
    throws ItemNotFoundException, OperationFailedException, DomainException {
        List channels = 
            new ArrayList(ChannelClassFactory.getOrderedChannelClassMap(
        ChannelMode.GLOBAL).values());
        return channels;
    }

    protected List getAdminChannels()

    throws ItemNotFoundException, OperationFailedException, DomainException {

        // first add all the global channels

        List channels = new ArrayList(getGlobalChannels());

        channels.addAll(ChannelClassFactory.getOrderedChannelClassMap(

        ChannelMode.ADMIN).values());

        return channels;

    }

    protected List getSubscriptionChannels()

    throws ItemNotFoundException, OperationFailedException, DomainException {

        // first add all the global channels

        List channels = new ArrayList(getGlobalChannels());

        channels.addAll(ChannelClassFactory.getOrderedChannelClassMap(

        ChannelMode.SUBSCRIPTION).values());

        return channels;

    }

    protected List getOfferingChannels(Offering currentOffering)

    throws ItemNotFoundException, OperationFailedException, DomainException {

        // first add all the global channels

        List channels = new ArrayList(getGlobalChannels());

        // Now add all the channels for this offering

        if (currentOffering != null) {

            channels.addAll(currentOffering.getChannelMap().values());

        }

        return channels;

    }

/*
    protected String getAdditionalUPParams(String upId, String subUID)
    throws PortalException {
        StringBuffer sb = new StringBuffer(512);
        String mode = (String)getChannelAttribute(upId, CHANNEL_MODE_KEY);

        if (ChannelMode.OFFERING.toString().equals(mode)) {
            sb.append(EXTRA_FIELD_SEP);
            Offering offering = getDomainUser(subUID).getContext().
                getCurrentOffering(TopicType.ACADEMICS);
            sb.append(ChannelRendererWorker.CSCR_OFFERING_ID);
            sb.append(EXTRA_FIELD_SEP);
            sb.append(offering.getId());
        }
        return sb.toString();
    }
*/

    protected String getOfferingMode() {

        return ChannelMode.OFFERING.toString();

    }

    protected String getSubscriptionMode() {

        return ChannelMode.SUBSCRIPTION.toString();

    }

    protected String getAdminMode() {

        return ChannelMode.ADMIN.toString();

    }

}

