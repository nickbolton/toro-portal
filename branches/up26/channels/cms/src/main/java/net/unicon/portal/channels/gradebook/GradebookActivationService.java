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

package net.unicon.portal.channels.gradebook;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.service.activation.Activation;
//import net.unicon.academus.apps.common.activation.Activation;
import java.sql.Connection;
import java.util.List;
import java.util.Date;
import java.util.Map;

import net.unicon.penelope.IDecisionCollection;

public interface GradebookActivationService {


    public void addGradebookActivations(
    List gradebook,
    User user,
    Offering o,
    int gradebookItemID,
    Connection conn);


    public List getGradebookItemWithActivations(
    int gbItemID,
    User user,
    Offering o,
    Connection conn);


    public List getGradebookItemWithActivations(
    Offering offering,
    User user,
    boolean allUsers,
    Connection conn);


    public List getGradebookActivation(
    int activationID,
    User user,
    Offering o,
    Connection conn);


    public Activation addActivation(
    int gbItemID,
    int offeringID,
    String gbType,
    Date startDate,
    Date endDate,
    long startTime,
    long endTime,
    Map attributes,
    List userList,
    boolean allUsers,
    User user,
    Connection conn,
    IDecisionCollection dc);

    public Activation retrieveActivation(int activationID, Connection conn);

    //added 10/8/2004 by John Bodily: Pass through to allow events to
    //be associated with an activation.
    public boolean addEventsToActivation(Activation newact, Connection conn);

    //added 10/8/2004 by John Bodily: Pass through to allow for deletion of
    //events that are associated with an activation.
    public void removeEventsFromActivation(int activationID, Connection conn);

    public void removeActivation(
    int activation,
    Offering offering,
    User user,
    Connection conn);

}

