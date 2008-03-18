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

package net.unicon.academus.cms.api;

/**
 *  The interface to the Academus CMS Facade API. The methods in the API
 *  allow manipulating topic, offering and offering membership information.
 */
public interface IAcademusCMSFacade {

    /**
     * Retrieves the specified topic.
     * @param topicId The topic Id of the topic to be retrieved.
     * @return The Academus topic instance with the specified
     * topic id.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusTopic getTopic (long topicId)
    throws AcademusCMSFacadeException ;

    /**
     * Retrieves all the available topics.
     * @return All the existing academus topic instances.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusTopic[] getAllTopics ()
    throws AcademusCMSFacadeException;

    /**
     * Retrieves the offering associated with the given
     * offering id. Any adjunct offering data will also be retrieved.
     * @param offeringId The offering id of the offering
     * to be retrieved.
     * @return The offering associated with the given
     * offering id.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusOffering getOffering (long offeringId)
    throws AcademusCMSFacadeException;

    /**
     * Retrieves all active offerings available under
     * the specified topic.
     * @param topicId The topic id of the topic of which the offerings
     * will retrieved.
     * @param includeAdjunctData Determines if the adjunct offering data
     * will be retrieved along with every offering. If set to false
     * any parameters that are consider adjunct offering data will
     * not be retrieved.
     * @return All the offerings of the specified topic.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusOffering[] getAllOfferings (
            long topicId, boolean includeAdjunctData)
                    throws AcademusCMSFacadeException;

    /**
     * Enrolls the given user to the specified offering.
     * @param username The username of the user to be enrolled.
     * @param offeringId The id of the offering the user will
     * be enrolled in.
     * @throws EnsembleException
     */
    public void enrollUser (String username, long offeringId)
    throws AcademusCMSFacadeException;
}

